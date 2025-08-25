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
import kotlinx.serialization.json.Json

class IssuanceService : Service() {

  private lateinit var iBinder: Messenger

  internal class HandlerIncoming(
    context: Context,
    val service: IssuanceService,
    val applicationContext : Context = context.applicationContext,
  ) : Handler() {

    override fun handleMessage(msg: Message) {
      val token = msg.data.getString("authorization")
      val prefs = applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
      val session = tokenToPayload(token!!)["sub"].toString().removeSurrounding("\"")
      val res = Message.obtain()

      res.data.putString(
        "credentials",
        Json.encodeToString(arrayOf(

          (mutableMapOf("credential" to (prefs.getString(session, "") ?: "")))
        ))
      )
      msg.replyTo.send(res)
      prefs.edit(commit = true) {











        remove(session)
        clear() //TODO remove
      }

      super.handleMessage(msg)
    }
  }

  override fun onBind(intent: Intent): IBinder? {
    Log.i("AAAAAA", "Bindato")
    iBinder = Messenger(HandlerIncoming(this, this))
    return iBinder.binder
  }

}