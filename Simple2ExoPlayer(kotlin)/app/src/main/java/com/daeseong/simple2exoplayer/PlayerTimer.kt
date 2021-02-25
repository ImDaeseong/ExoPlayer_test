package com.daeseong.simple2exoplayer

import android.os.Handler
import android.os.Message

class PlayerTimer : Handler() {

    companion object {
        private const val DEFAULT_INTERVAL_MILLIS = 1000
    }

    private var isUpdate = false
    private var callback: Callback? = null
    private var startTimeMillis: Long = 0

    private fun init() {
        startTimeMillis = System.currentTimeMillis()
    }

    fun start() {
        init()
        isUpdate = true
        handleMessage(Message())
    }

    fun stop() {
        isUpdate = false
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun handleMessage(msg: Message) {
        this.removeMessages(0)
        if (isUpdate) {
            sendMessageDelayed(obtainMessage(0), DEFAULT_INTERVAL_MILLIS.toLong())
            callback!!.onTick(System.currentTimeMillis() - startTimeMillis)
        }
    }

    interface Callback {
        fun onTick(timeMillis: Long)
    }

}