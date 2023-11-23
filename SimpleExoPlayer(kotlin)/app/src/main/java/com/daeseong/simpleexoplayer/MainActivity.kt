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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private val VIDEO_URL = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4"
    private val AUDIO_ASSET = "asset:///The Lazy Song.mp3"

    private lateinit var button1: Button

    private lateinit var playerView: PlayerView
    private var simpleExoPlayer: SimpleExoPlayer? = null

    private var currentPosition: Long = 0
    private var currentWindowIndex = 0
    private var playWhenReady = true
    private var isVideoPlaying = false

    private fun initTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.rgb(255, 255, 255)
        }

        try {
            // 안드로이드 8.0 오레오 버전에서만 오류 발생
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
            Log.e(tag, ex.message.orEmpty())
        }
    }

    // ExoPlayer 초기화
    private fun initializePlayer(mediaUrl: String) {

        if (simpleExoPlayer == null) {

            // 기본 설정값으로 인스턴스 생성
            val defaultRenderersFactory = DefaultRenderersFactory(applicationContext)
            val defaultTrackSelector = DefaultTrackSelector()
            val defaultLoadControl = DefaultLoadControl()

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
                applicationContext, defaultRenderersFactory, defaultTrackSelector, defaultLoadControl
            )

            // ExoPlayer 이벤트 리스너 설정
            simpleExoPlayer?.addListener(object : Player.EventListener{

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                    handlePlayerStateChanged(playbackState)
                }

                override fun onPlayerError(error: ExoPlaybackException?) {
                    super.onPlayerError(error)
                    handleError()
                }
            })

            // 화면 플레이어 연결
            playerView.player = simpleExoPlayer
        }

        // 미디어 소스 준비
        val mediaSource: MediaSource = getMediaSource(Uri.parse(mediaUrl))
        simpleExoPlayer?.prepare(mediaSource, true, false)
        simpleExoPlayer?.playWhenReady = playWhenReady
    }

    // ExoPlayer 상태에 따른 처리
    private fun handlePlayerStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_READY -> {
                Log.e(tag, "재생 준비 완료")
                button1.visibility = View.GONE
            }
            ExoPlayer.STATE_BUFFERING -> {
                Log.e(tag, "재생 준비")
                button1.visibility = View.GONE
            }
            ExoPlayer.STATE_IDLE -> {
                Log.e(tag, "재생 실패")
                button1.visibility = View.VISIBLE
            }
            ExoPlayer.STATE_ENDED -> {
                Log.e(tag, "재생 마침")
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
    private fun releasePlayer() {

        if (simpleExoPlayer != null) {
            currentPosition = simpleExoPlayer!!.currentPosition
            currentWindowIndex = simpleExoPlayer!!.currentWindowIndex
            playWhenReady = simpleExoPlayer!!.playWhenReady

            // 플레이어와 화면 연결 해제
            playerView.player = null
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    // 미디어 재생
    private fun playMedia(uri: Uri) {

        if (simpleExoPlayer != null) {
            val mediaSource: MediaSource = getMediaSource(uri)
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = playWhenReady
        }
    }

    // 미디어 정지
    private fun stopPlayer() {
        if (simpleExoPlayer != null) {
            currentPosition = simpleExoPlayer!!.currentPosition
            currentWindowIndex = simpleExoPlayer!!.currentWindowIndex
            playWhenReady = simpleExoPlayer!!.playWhenReady

            // 플레이어 정지
            simpleExoPlayer!!.stop()
        }
    }

    // 미디어 소스 가져오기
    private fun getMediaSource(uri: Uri): MediaSource {
        val userAgent: String = Util.getUserAgent(this, packageName)
        return ProgressiveMediaSource.Factory(DefaultDataSourceFactory(this, userAgent)).createMediaSource(uri)
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

    override fun onStart() {
        super.onStart()

        // Android 버전이 23 이상
        if (Util.SDK_INT > 23) {
            initializePlayer(VIDEO_URL)
        }
    }

    override fun onStop() {
        super.onStop()

        // Android 버전이 23 이상
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }
}
