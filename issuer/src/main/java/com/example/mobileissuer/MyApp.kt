package com.example.mobileissuer

import android.app.Application

import android.content.Context
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
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