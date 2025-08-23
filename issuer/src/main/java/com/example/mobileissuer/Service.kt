package com.example.mobileissuer

import android.app.Service

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log

class IssuanceService : Service() {

  private lateinit var iBinder: Messenger

  internal class HandlerIncoming(
    context: Context,
    val service: IssuanceService,
    val applicationContext : Context = context.applicationContext,
  ) : Handler() {
    override fun handleMessage(msg: Message) {
      val token = msg.data.getString("token")

      Log.i("AAAAAA", "Token: $token")
      val res = Message.obtain()
      res.data.putString("credential", "credenziale")
      msg.replyTo.send(res)
      super.handleMessage(msg)
    }
  }

  override fun onBind(intent: Intent): IBinder? {

    Log.i("AAAAAA", "Bindato")
    iBinder = Messenger(HandlerIncoming(this, this))
    return iBinder.binder
  }
}