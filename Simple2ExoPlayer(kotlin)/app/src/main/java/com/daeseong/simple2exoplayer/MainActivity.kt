package com.daeseong.simple2exoplayer

import android.Manifest.permission.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private lateinit var btnPre: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNextgo: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var seekBar: SeekBar

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var playerTimer: PlayerTimer? = null
    private val musicList = ArrayList<MusicInfo>()
    private var CurrentPlayIndex = -1

    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>

    private val PERMISSIONS = arrayOf(READ_PHONE_STATE, READ_EXTERNAL_STORAGE)
    private val PERMISSIONS33 = arrayOf(READ_PHONE_STATE, READ_MEDIA_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main)

        initPermissionsLauncher()

        checkPermissions()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)
        btnSearch = findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener(View.OnClickListener {

            checkPermissions()

            //음악 폴더 선택
            musicList.clear()
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.DATA),
                "${MediaStore.Audio.AudioColumns.IS_MUSIC} > 0",
                null,
                null
            )

            val nIndex: Int = cursor?.getColumnIndex(MediaStore.Audio.AudioColumns.DATA) ?: -1

            cursor?.use {

                while (cursor.moveToNext()) {

                    if (nIndex == -1) {
                        continue
                    }

                    val sMusicPath: String = it.getString(nIndex)
                    //val sMusicPath = cursor.getString(nIndex)
                    if (sMusicPath.contains("music2")) {
                        val info = MusicInfo()
                        info.musicPath = sMusicPath
                        musicList.add(info)
                    }
                }
            }

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex = 0

            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious.setOnClickListener(View.OnClickListener {

            PreplayPlayer()
        })

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo!!.setOnClickListener(View.OnClickListener {

            NextplayPlayer()
        })

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex--

            if (CurrentPlayIndex < 0) CurrentPlayIndex = musicList.size - 1

            //Log.e(tag, "PreCurrentPlayIndex:" + CurrentPlayIndex)
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //다음곡
        btnNext = findViewById(R.id.btnNext)
        btnNext.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex++

            if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

            //Log.e(tag, "NextCurrentPlayIndex:" + CurrentPlayIndex)
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener(View.OnClickListener {
            simpleExoPlayer!!.playWhenReady = true
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener(View.OnClickListener {
            simpleExoPlayer!!.playWhenReady = false
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.INVISIBLE
        })

        //진행바
        seekBar = findViewById<View>(R.id.seekBar) as SeekBar
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (simpleExoPlayer == null) return
                if (!fromUser) return
                simpleExoPlayer!!.seekTo((progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                simpleExoPlayer!!.playWhenReady = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                simpleExoPlayer!!.playWhenReady = true
            }
        })
    }

    private fun InitTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.rgb(255, 255, 255)
        }

        try {
            //안드로이드 8.0 오레오 버전에서만 오류 발생
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
            Log.e(tag, ex.message.toString())
        }
    }

    private fun checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            var bPermissResult = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                for (permission in PERMISSIONS33) {
                    bPermissResult = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                    if (!bPermissResult) {
                        break
                    }
                }

                if (!bPermissResult) {
                    requestPermissions.launch(PERMISSIONS33)
                } else {
                    Log.e(tag, "PERMISSIONS33 권한 소유-SimpleExoPlayer 초기화")
                    initalizePlayer()
                }

            } else {

                for (permission in PERMISSIONS) {
                    bPermissResult = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                    if (!bPermissResult) {
                        break
                    }
                }

                if (!bPermissResult) {
                    requestPermissions.launch(PERMISSIONS)
                } else {
                    Log.e(tag, "PERMISSIONS 권한 소유-SimpleExoPlayer 초기화")
                    initalizePlayer()
                }
            }
        }
    }

    private fun initPermissionsLauncher() {

        requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

            var bPhone = false
            var bAudio = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bPhone =  java.lang.Boolean.TRUE == result[READ_PHONE_STATE]
                bAudio =  java.lang.Boolean.TRUE == result[READ_MEDIA_AUDIO]
            } else {
                bPhone =  java.lang.Boolean.TRUE == result[READ_PHONE_STATE]
                bAudio =  java.lang.Boolean.TRUE == result[READ_EXTERNAL_STORAGE]
            }

            if (bPhone && bAudio) {
                Log.e(tag,"PERMISSIONS 권한 소유-SimpleExoPlayer 초기화")
                initalizePlayer()
            } else {
                Log.e(tag, "PERMISSIONS 권한 미소유")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        musicList.clear()
        stopplayerTimer()
        releasePlayer()
    }

    private fun initalizePlayer() {

        if (simpleExoPlayer == null) {

            val defaultRenderersFactory = DefaultRenderersFactory(this.applicationContext)
            val defaultTrackSelector = DefaultTrackSelector()
            val defaultLoadControl = DefaultLoadControl()
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.applicationContext, defaultRenderersFactory, defaultTrackSelector, defaultLoadControl)

            simpleExoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_READY -> Log.e(tag, "재생 준비 완료")
                        ExoPlayer.STATE_BUFFERING -> Log.e(tag, "재생 준비")
                        ExoPlayer.STATE_IDLE -> Log.e(tag, "재생 실패")
                        ExoPlayer.STATE_ENDED -> {
                            Log.e(tag, "재생 마침")

                            //현재곡 완료시 다음곡 자동시작
                            if (simpleExoPlayer != null) {

                                if (!isPlaying) {

                                    if (musicList.size == 0) return

                                    CurrentPlayIndex++

                                    if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

                                    //Log.e(tag, "NextCurrentPlayIndex:" + CurrentPlayIndex)
                                    val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
                                    playPlayer(uri)
                                    btnPlay.visibility = View.INVISIBLE
                                    btnPause.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            })
            simpleExoPlayer!!.volume = 1.0f
        }
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.stop()
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    private fun playPlayer(sUrl: String) {
        if (simpleExoPlayer != null) {
            val mediaSource = getMediaSource(Uri.parse(sUrl))
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = true
            setSeekBarProgress()
        }
    }

    private fun playPlayer(uri: Uri) {
        if (simpleExoPlayer != null) {
            val mediaSource = getMediaSource(uri)
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            simpleExoPlayer!!.playWhenReady = true
            setSeekBarProgress()
        }
    }

    private fun stopPlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.stop()
        }
    }

    private val isPlaying: Boolean
        private get() = if (simpleExoPlayer != null) {
            simpleExoPlayer!!.playbackState == Player.STATE_READY
        } else false

    private fun PreplayPlayer() {
        if (simpleExoPlayer != null) {
            var position = simpleExoPlayer!!.currentPosition
            position -= 3000
            simpleExoPlayer!!.seekTo(position)
        }
    }

    private fun NextplayPlayer() {
        if (simpleExoPlayer != null) {
            var position = simpleExoPlayer!!.currentPosition
            position += 3000
            simpleExoPlayer!!.seekTo(position)
        }
    }

    private fun getMediaSource(uri: Uri): MediaSource {
        val sUserAgent = Util.getUserAgent(this, packageName)
        return ProgressiveMediaSource.Factory(DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(uri)
    }

    private fun stringForTime(timeMs: Int): String {
        val mFormatter: Formatter
        val mFormatBuilder: StringBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun stopplayerTimer() {
        if (playerTimer != null) {
            playerTimer!!.stop()
            playerTimer = null
        }
    }

    private fun setSeekBarProgress() {
        stopplayerTimer()
        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {
                if (simpleExoPlayer!!.isPlaying) {
                    val position = simpleExoPlayer!!.currentPosition
                    val duration = simpleExoPlayer!!.duration
                    if (duration <= 0) return
                    seekBar.max = duration.toInt() / 1000
                    seekBar.progress = position.toInt() / 1000
                    txtStartTime.text = stringForTime(simpleExoPlayer!!.currentPosition.toInt())
                    txtEndTime.text = stringForTime(simpleExoPlayer!!.duration.toInt())
                }
            }
        })
        playerTimer!!.start()
    }

}