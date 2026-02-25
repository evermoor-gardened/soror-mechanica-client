package com.soror.mechanica

import android.app.Application

class SororApp : Application() {
  lateinit var services: Services
    private set

  override fun onCreate() {
    super.onCreate()
    services = Services.create(this)
  }
}
