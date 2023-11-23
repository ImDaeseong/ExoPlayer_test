package com.daeseong.simple1exoplayer

import android.os.Handler
import android.os.Looper

class PlayerTimer {
    private val DEFAULT_INTERVAL_MILLIS = 1000L
    private var isUpdate = false
    private var callback: Callback? = null
    private var startTimeMillis: Long = 0
    private val handler: Handler = Handler(Looper.getMainLooper())

    private fun init() {
        startTimeMillis = System.currentTimeMillis()
    }

    fun start() {
        init()
        isUpdate = true
        handler.post(timerRunnable)
    }

    fun stop() {
        isUpdate = false
        handler.removeCallbacksAndMessages(null)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (isUpdate) {
                callback?.onTick(System.currentTimeMillis() - startTimeMillis)
                handler.postDelayed(this, DEFAULT_INTERVAL_MILLIS)
            }
        }
    }

    interface Callback {
        fun onTick(timeMillis: Long)
    }
}