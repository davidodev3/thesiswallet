package com.example.mobilewallet

import android.app.Application

import id.walt.crypto.keys.KeyType
import android.content.Context
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import id.walt.did.dids.DidService
import id.walt.crypto.keys.jwk.JWKKey
import kotlinx.coroutines.*
import java.security.Security
import kotlin.coroutines.CoroutineContext


class MyApp : Application(), CoroutineScope {

  override val coroutineContext : CoroutineContext get() = Job() + Dispatchers.Main
  override fun onCreate() {
    super.onCreate()
    //Remove Android's default BouncyCastle implementation because walt.id uses another one
    Security.removeProvider("BC")
    Security.addProvider(BouncyCastleProviderSingleton.getInstance())
    val preferences = applicationContext.getSharedPreferences("did", Context.MODE_PRIVATE)

    //Generate key and DID pair if none was found.
    if (preferences.all.isEmpty()) {
      launch {
        val keydid = async {
          generateKeyDid()
        }
        with(preferences.edit()) {
          val resolved = keydid.await()
          putString("did", resolved.second)

          putString("key", resolved.first.exportJWK())

          apply()
        }
      }











    }
  }
}

suspend fun generateKeyDid() : Pair<JWKKey, String> {
  DidService.minimalInit()
  val key =JWKKey.generate(KeyType.Ed25519)
  val did = DidService.registerByKey("key", key).did
  return Pair(key, did)
}