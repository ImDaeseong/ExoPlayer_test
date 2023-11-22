package com.daeseong.simple1exoplayer

import android.app.Application
import android.content.Context
import android.content.res.Configuration


class MusicApplication : Application() {

    private val tag: String = MusicApplication::class.java.simpleName

    companion object {
        private lateinit var mContext: Context
        private lateinit var mInstance: MusicApplication

        fun getContext(): Context {
            return mContext.applicationContext
        }

        fun getInstance(): MusicApplication {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()

        mContext = this
        mInstance = this
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

}