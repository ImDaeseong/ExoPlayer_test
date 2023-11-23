package com.daeseong.simple1exoplayer

import android.app.Service
import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util

class MusicService : Service() {

    private val tag = MusicService::class.java.simpleName

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getMusicService(): MusicService {
            return this@MusicService
        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.e(tag, "onCreate")

        val defaultRenderersFactory = DefaultRenderersFactory(applicationContext)
        val defaultTrackSelector = DefaultTrackSelector()
        val defaultLoadControl = DefaultLoadControl()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(applicationContext, defaultRenderersFactory, defaultTrackSelector, defaultLoadControl)

        simpleExoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {

                    ExoPlayer.STATE_BUFFERING -> {
                        Log.e(tag, "재생 준비")
                        sendPlayerStatusBroadcast(PlaybackState.STATE_BUFFERING)
                    }

                    ExoPlayer.STATE_READY -> {
                        Log.e(tag, "재생 준비 완료")
                        sendPlayerStatusBroadcast(
                            if (playWhenReady) PlaybackState.STATE_PLAYING
                            else PlaybackState.STATE_PAUSED
                        )
                    }

                    ExoPlayer.STATE_ENDED -> {
                        Log.e(tag, "재생 마침")
                        sendPlayerStatusBroadcast(PlaybackState.STATE_NONE)
                    }

                    ExoPlayer.STATE_IDLE -> {
                        Log.e(tag, "재생 실패")
                        sendPlayerStatusBroadcast(PlaybackState.STATE_ERROR)
                    }
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    fun playPlayer(sUrl: String) {
        simpleExoPlayer?.let {
            val mediaSource = getMediaSource(Uri.parse(sUrl))
            it.prepare(mediaSource, true, false)
            it.playWhenReady = true
        }
    }

    fun playPlayer(uri: Uri) {
        simpleExoPlayer?.let {
            val mediaSource = getMediaSource(uri)
            it.prepare(mediaSource, true, false)
            it.playWhenReady = true
        }
    }

    fun stopPlayer() {
        simpleExoPlayer?.stop()
    }

    fun releasePlayer() {
        simpleExoPlayer?.let {
            it.stop()
            it.release()
            simpleExoPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return simpleExoPlayer?.playbackState == Player.STATE_READY
    }

    fun preplayPlayer() {
        simpleExoPlayer?.let {
            var position = it.currentPosition
            position -= 3000
            it.seekTo(position)
        }
    }

    fun nextplayPlayer() {
        simpleExoPlayer?.let {
            var position = it.currentPosition
            position += 3000
            it.seekTo(position)
        }
    }

    fun setPlayWhenReady(bReady: Boolean) {
        simpleExoPlayer?.playWhenReady = bReady
    }

    fun getCurrentPosition(): Long {
        return simpleExoPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Long {
        return simpleExoPlayer?.duration ?: 0
    }

    fun seekTo(progress: Long) {
        simpleExoPlayer?.seekTo(progress)
    }

    private fun getMediaSource(uri: Uri): MediaSource {
        val userAgent = Util.getUserAgent(this, packageName)
        return ProgressiveMediaSource.Factory(DefaultDataSourceFactory(this, userAgent)).createMediaSource(uri)
    }

    private fun sendPlayerStatusBroadcast(state: Int) {
        val intent = Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS")
        intent.putExtra("state", state)
        localBroadcastManager.sendBroadcast(intent)
    }
}
