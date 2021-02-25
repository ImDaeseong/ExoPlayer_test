package com.daeseong.simpleexoplayer


import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

    private var playerView: PlayerView? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null

    private var currentPosition = 0L
    private var currentWindowIndex = 0
    private var playWhenReady = true

    private var button1: Button? = null

    private var bCheck = false
    private val sUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)

        button1 = findViewById(R.id.button1)
        button1!!.setOnClickListener {

            stopPlayer()

            bCheck = if (bCheck) {
                playPlayer(sUrl)
                false
            } else {
                val uri = Uri.parse("asset:///The Lazy Song.mp3")
                playPlayer(uri)
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) {
            initalizePlayer(sUrl)
        }
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun InitTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_bg)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun initalizePlayer(sUrl: String) {

        if (simpleExoPlayer == null) {

            val defaultRenderersFactory = DefaultRenderersFactory(this.applicationContext)
            val defaultTrackSelector = DefaultTrackSelector()
            val defaultLoadControl = DefaultLoadControl()

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.applicationContext, defaultRenderersFactory, defaultTrackSelector, defaultLoadControl)
            simpleExoPlayer!!.addListener(object : Player.EventListener {

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                    when (playbackState) {
                        ExoPlayer.STATE_READY -> button1!!.visibility = View.GONE
                        ExoPlayer.STATE_BUFFERING -> button1!!.visibility = View.GONE
                        ExoPlayer.STATE_IDLE -> button1!!.visibility = View.VISIBLE
                        ExoPlayer.STATE_ENDED -> button1!!.visibility = View.VISIBLE
                    }
                }

                override fun onPlayerError(error: ExoPlaybackException) {

                    button1!!.visibility = View.VISIBLE
                    stopPlayer()
                }
            })

            //화면 플레이어 연결
            playerView!!.player = simpleExoPlayer
        }

        val mediaSource = getMediaSource(Uri.parse(sUrl))
        simpleExoPlayer!!.prepare(mediaSource, true, false)
        simpleExoPlayer!!.playWhenReady = playWhenReady
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            currentPosition = simpleExoPlayer!!.currentPosition
            currentWindowIndex = simpleExoPlayer!!.currentWindowIndex
            playWhenReady = simpleExoPlayer!!.playWhenReady
            playerView!!.player = null
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    private fun playPlayer(sUrl: String) {
        if (simpleExoPlayer != null) {
            val mediaSource = getMediaSource(Uri.parse(sUrl))
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = playWhenReady
        }
    }

    private fun playPlayer(uri: Uri) {
        if (simpleExoPlayer != null) {
            val mediaSource = getMediaSource(uri)
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = playWhenReady
        }
    }

    private fun stopPlayer() {
        if (simpleExoPlayer != null) {
            currentPosition = simpleExoPlayer!!.currentPosition
            currentWindowIndex = simpleExoPlayer!!.currentWindowIndex
            playWhenReady = simpleExoPlayer!!.playWhenReady
            simpleExoPlayer!!.stop()
        }
    }

    private fun getMediaSource(uri: Uri): MediaSource? {

        val sUserAgent = Util.getUserAgent(
            this,
            packageName
        )

        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, sUserAgent))
            .createMediaSource(uri)


        /*
        return if (uri.lastPathSegment!!.contains("mp3") || uri.lastPathSegment!!.contains("mp4")) {
            ExtractorMediaSource.Factory(DefaultHttpDataSourceFactory(sUserAgent))
                .createMediaSource(uri)
        } else if (uri.lastPathSegment!!.contains("m3u8")) {
            HlsMediaSource.Factory(DefaultHttpDataSourceFactory(sUserAgent)).createMediaSource(uri)
        } else {
            ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, sUserAgent))
                .createMediaSource(uri)
        }
        */
    }

}
