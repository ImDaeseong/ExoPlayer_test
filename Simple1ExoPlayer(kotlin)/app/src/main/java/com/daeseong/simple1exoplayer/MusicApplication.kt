package com.daeseong.simple1exoplayer

import android.app.Application
import android.content.Context
import android.content.res.Configuration

class MusicApplication : Application() {

    companion object {
        private lateinit var mInstance: MusicApplication
        fun getInstance(): MusicApplication = mInstance

        private lateinit var mContext: Context
        fun getAppContext(): Context = mContext
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mContext = applicationContext
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}