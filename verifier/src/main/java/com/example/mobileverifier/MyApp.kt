package com.example.mobileverifier

import android.app.Application

import java.security.Security

class MyApp : Application() {
  override fun onCreate() {
    super.onCreate()
    Security.removeProvider("BC")
  }
}