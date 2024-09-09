package com.daeseong.simpleexoplayer

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.*
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private val VIDEO_URL = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4"
    private val AUDIO_ASSET = "asset:///The Lazy Song.mp3"

    private lateinit var button1: Button
    private lateinit var playerView: PlayerView
    private var simpleExoPlayer: ExoPlayer? = null

    private var currentPosition: Long = 0
    private var currentWindowIndex = 0
    private var playWhenReady = true
    private var isVideoPlaying = false

    private fun initTitleBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.rgb(255, 255, 255)
        }

        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
            Log.e(tag, ex.message.orEmpty())
        }
    }

    // ExoPlayer 초기화
    @OptIn(UnstableApi::class)
    private fun initializePlayer(mediaUrl: String) {
        if (simpleExoPlayer == null) {
            val defaultTrackSelector = DefaultTrackSelector(this)
            val defaultLoadControl = DefaultLoadControl()

            simpleExoPlayer = ExoPlayer.Builder(this)
                .setTrackSelector(defaultTrackSelector)
                .setLoadControl(defaultLoadControl)
                .build()

            // ExoPlayer 이벤트 리스너 설정
            simpleExoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    handlePlayerStateChanged(playbackState)
                }

                override fun onPlayerError(error: PlaybackException) {
                    handleError()
                }
            })

            // 화면 플레이어 연결
            playerView.player = simpleExoPlayer
        }

        // 미디어 소스 준비
        val mediaSource: MediaSource = getMediaSource(Uri.parse(mediaUrl))
        simpleExoPlayer?.setMediaSource(mediaSource)
        simpleExoPlayer?.prepare()
        simpleExoPlayer?.playWhenReady = playWhenReady
    }

    // ExoPlayer 상태에 따른 처리
    private fun handlePlayerStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                Log.d(tag, "재생 준비 완료")
                button1.visibility = View.GONE
            }
            Player.STATE_BUFFERING -> {
                Log.d(tag, "재생 준비 중")
                button1.visibility = View.GONE
            }
            Player.STATE_IDLE -> {
                Log.d(tag, "재생 실패")
                button1.visibility = View.VISIBLE
            }
            Player.STATE_ENDED -> {
                Log.d(tag, "재생 마침")
                button1.visibility = View.VISIBLE
            }
        }
    }

    // ExoPlayer 에러 처리
    private fun handleError() {
        button1.visibility = View.VISIBLE
        stopPlayer()
    }

    // ExoPlayer 해제
    @OptIn(UnstableApi::class)
    private fun releasePlayer() {
        simpleExoPlayer?.let {
            currentPosition = it.currentPosition
            currentWindowIndex = it.currentWindowIndex
            playWhenReady = it.playWhenReady

            // 플레이어와 화면 연결 해제
            playerView.player = null
            it.release()
            simpleExoPlayer = null
        }
    }

    // 미디어 재생
    @OptIn(UnstableApi::class)
    private fun playMedia(uri: Uri) {
        simpleExoPlayer?.let {
            val mediaSource: MediaSource = getMediaSource(uri)
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = playWhenReady
        }
    }

    // 미디어 정지
    @OptIn(UnstableApi::class)
    private fun stopPlayer() {
        simpleExoPlayer?.let {
            currentPosition = it.currentPosition
            currentWindowIndex = it.currentWindowIndex
            playWhenReady = it.playWhenReady

            // 플레이어 정지
            it.stop()
        }
    }

    // 미디어 소스 가져오기
    @OptIn(UnstableApi::class)
    private fun getMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, packageName))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTitleBar()

        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        button1 = findViewById(R.id.button1)
        button1.setOnClickListener {
            stopPlayer()

            if (isVideoPlaying) {
                playMedia(Uri.parse(VIDEO_URL))
                isVideoPlaying = false
            } else {
                val audioUri: Uri = Uri.parse(AUDIO_ASSET)
                playMedia(audioUri)
                isVideoPlaying = true
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) {
            initializePlayer(VIDEO_URL)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }
}
