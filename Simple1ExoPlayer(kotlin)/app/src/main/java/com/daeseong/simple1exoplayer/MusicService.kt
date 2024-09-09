package com.daeseong.simple1exoplayer

import android.app.Service
import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

class MusicService : Service() {

    private val tag = MusicService::class.java.simpleName

    private var exoPlayer: ExoPlayer? = null
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

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        exoPlayer = ExoPlayer.Builder(applicationContext).build()

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.e(tag, "재생 준비")
                        sendPlayerStatusBroadcast(PlaybackState.STATE_BUFFERING)
                    }

                    Player.STATE_READY -> {
                        Log.e(tag, "재생 준비 완료")
                        sendPlayerStatusBroadcast(
                            if (exoPlayer?.playWhenReady == true) PlaybackState.STATE_PLAYING
                            else PlaybackState.STATE_PAUSED
                        )
                    }

                    Player.STATE_ENDED -> {
                        Log.e(tag, "재생 마침")
                        sendPlayerStatusBroadcast(PlaybackState.STATE_NONE)
                    }

                    Player.STATE_IDLE -> {
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

    @OptIn(UnstableApi::class)
    fun playPlayer(sUrl: String) {
        exoPlayer?.let {
            val mediaSource = getMediaSource(Uri.parse(sUrl))
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = true
        }
    }

    @OptIn(UnstableApi::class)
    fun playPlayer(uri: Uri) {
        exoPlayer?.let {
            val mediaSource = getMediaSource(uri)
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = true
        }
    }

    fun stopPlayer() {
        exoPlayer?.stop()
    }

    fun releasePlayer() {
        exoPlayer?.let {
            it.stop()
            it.release()
            exoPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.playbackState == Player.STATE_READY && exoPlayer?.playWhenReady == true
    }

    fun preplayPlayer() {
        exoPlayer?.let {
            var position = it.currentPosition
            position -= 3000
            it.seekTo(position)
        }
    }

    fun nextplayPlayer() {
        exoPlayer?.let {
            var position = it.currentPosition
            position += 3000
            it.seekTo(position)
        }
    }

    fun setPlayWhenReady(bReady: Boolean) {
        exoPlayer?.playWhenReady = bReady
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    fun seekTo(progress: Long) {
        exoPlayer?.seekTo(progress)
    }

    @OptIn(UnstableApi::class)
    private fun getMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(this)
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }

    private fun sendPlayerStatusBroadcast(state: Int) {
        val intent = Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS")
        intent.putExtra("state", state)
        localBroadcastManager.sendBroadcast(intent)
    }
}
