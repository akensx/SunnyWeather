package com.sunnyweather.android

import android.app.Application
import android.content.Context
import android.media.session.MediaSession.Token

class SunnyWeatherApplication: Application() {

    companion object{
        lateinit var context: Context
        const val TOKEN = "ziNKHDnVbJecc0Ai"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}