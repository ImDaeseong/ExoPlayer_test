package com.daeseong.simple1exoplayer


import android.app.Service
import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MusicService : Service() {

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var localBroadcastManager: LocalBroadcastManager? = null
    private val binder: Binder = MusicBinder()

    inner class MusicBinder : Binder() {
        val musicService: MusicService
            get() = this@MusicService
    }

    override fun onCreate() {

        val defaultRenderersFactory = DefaultRenderersFactory(this.applicationContext)
        val defaultTrackSelector = DefaultTrackSelector()
        val defaultLoadControl = DefaultLoadControl()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            this.applicationContext,
            defaultRenderersFactory,
            defaultTrackSelector,
            defaultLoadControl
        )

        simpleExoPlayer!!.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                when (playbackState) {

                    ExoPlayer.STATE_BUFFERING -> {

                        //재생 준비
                        val intent = Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS")
                        intent.putExtra("state", PlaybackState.STATE_BUFFERING)
                        localBroadcastManager!!.sendBroadcast(intent)
                    }

                    ExoPlayer.STATE_READY -> {

                        //재생 준비 완료
                        val intent = Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS")
                        if (playWhenReady) {
                            intent.putExtra("state", PlaybackState.STATE_PLAYING)
                        } else {
                            intent.putExtra("state", PlaybackState.STATE_PAUSED)
                        }
                        localBroadcastManager!!.sendBroadcast(intent)
                    }

                    ExoPlayer.STATE_ENDED -> {

                        //재생 마침
                        val intent = Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS")
                        intent.putExtra("state", PlaybackState.STATE_NONE)
                        localBroadcastManager!!.sendBroadcast(intent)
                    }

                    ExoPlayer.STATE_IDLE -> {

                        //재생실패
                        val intent = Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS")
                        intent.putExtra("state", PlaybackState.STATE_ERROR)
                        localBroadcastManager!!.sendBroadcast(intent)
                    }
                }
            }
        })
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    fun playPlayer(sUrl: String?) {
        if (simpleExoPlayer != null) {
            val mediaSource = getMediaSource(Uri.parse(sUrl))
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = true
        }
    }

    fun playPlayer(uri: Uri?) {
        if (simpleExoPlayer != null) {
            val mediaSource = getMediaSource(uri!!)
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = true
        }
    }

    fun stopPlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.stop()
        }
    }

    fun releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.stop()
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return if (simpleExoPlayer != null) {
            simpleExoPlayer!!.playbackState == Player.STATE_READY
        } else false
    }

    fun PreplayPlayer() {
        if (simpleExoPlayer != null) {
            var position = simpleExoPlayer!!.currentPosition
            position -= 3000
            simpleExoPlayer!!.seekTo(position)
        }
    }

    fun NextplayPlayer() {
        if (simpleExoPlayer != null) {
            var position = simpleExoPlayer!!.currentPosition
            position += 3000
            simpleExoPlayer!!.seekTo(position)
        }
    }

    fun setPlayWhenReady(bReady: Boolean) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.playWhenReady = bReady
        }
    }

    fun getCurrentPosition(): Long {
        var position: Long = 0
        if (simpleExoPlayer != null) {
            position = simpleExoPlayer!!.currentPosition
        }
        return position
    }

    fun getDuration(): Long {
        var duration: Long = 0
        if (simpleExoPlayer != null) {
            duration = simpleExoPlayer!!.duration
        }
        return duration
    }

    fun seekTo(progress: Long) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.seekTo(progress)
        }
    }

    private fun getMediaSource(uri: Uri): MediaSource? {

        val sUserAgent = Util.getUserAgent(this, packageName)
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(
            uri
        )
    }
}