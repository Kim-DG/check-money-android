package com.checkmoney

import android.app.Application

//자동로그인
class AppPref : Application() {
    companion object {
        lateinit var prefs : MySharedPreferences
    }

    override fun onCreate() {
        prefs = MySharedPreferences(applicationContext)
        super.onCreate()
    }
}