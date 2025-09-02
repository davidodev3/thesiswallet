package com.example.mobileissuer

import android.app.Service

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.core.content.edit
import id.walt.crypto.keys.jwk.JWKKey

import id.walt.oid4vc.OpenID4VC
import id.walt.oid4vc.providers.TokenTarget
import kotlinx.coroutines.runBlocking

import kotlinx.serialization.json.Json

class IssuanceService : Service() {

  private lateinit var iBinder: Messenger

  internal class HandlerIncoming(
    context: Context,
    val service: IssuanceService,
    val applicationContext: Context = context.applicationContext,
  ) : Handler() {

    override fun handleMessage(msg: Message) {
      val token = msg.data.getString("authorization")
      runBlocking {

        val keySharedPrefs = applicationContext.getSharedPreferences("did", Context.MODE_PRIVATE)
        val key = keySharedPrefs.getString("key", "")!!
        val public = JWKKey.importJWK(key).getOrNull()!!.getPublicKey()
        if (OpenID4VC.verifyTokenSignature(TokenTarget.ACCESS, token!!, public)) {
          val sessionSharedPrefs =











            applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
          val session = tokenToPayload(token!!)["sub"].toString().removeSurrounding("\"")
          val res = Message.obtain()

          res.data.putString(
            "credentials",
            Json.encodeToString(
              arrayOf(
                (mutableMapOf("credential" to (sessionSharedPrefs.getString(session, "") ?: "")))
              )
            )
          )
          msg.replyTo.send(res)

          sessionSharedPrefs.edit(commit = true) {
            remove(session)
            clear() //TODO remove
          }
          super.handleMessage(msg)
        }
      }
    }
  }

  override fun onBind(intent: Intent): IBinder? {
    Log.i("AAAAAA", "Bindato")
    iBinder = Messenger(HandlerIncoming(this, this))
    return iBinder.binder
  }

}