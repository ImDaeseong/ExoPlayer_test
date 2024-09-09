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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

    private var exoPlayer: ExoPlayer? = null
    private var playerTimer: PlayerTimer? = null
    private val musicList = ArrayList<MusicInfo>()
    private var currentPlayIndex = -1

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

        btnSearch.setOnClickListener {
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

            if (musicList.size == 0) return@setOnClickListener

            currentPlayIndex = 0

            val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious.setOnClickListener {
            PreplayPlayer()
        }

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo.setOnClickListener {
            NextplayPlayer()
        }

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre.setOnClickListener {
            if (musicList.size == 0) return@setOnClickListener

            currentPlayIndex--

            if (currentPlayIndex < 0) currentPlayIndex = musicList.size - 1

            //Log.e(tag, "PreCurrentPlayIndex:" + currentPlayIndex)
            val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //다음곡
        btnNext = findViewById(R.id.btnNext)
        btnNext.setOnClickListener {
            if (musicList.size == 0) return@setOnClickListener

            currentPlayIndex++

            if (currentPlayIndex > musicList.size - 1) currentPlayIndex = 0

            //Log.e(tag, "NextCurrentPlayIndex:" + currentPlayIndex)
            val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener {
            exoPlayer?.play()
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        }

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener {
            exoPlayer?.pause()
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.INVISIBLE
        }

        //진행바
        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (exoPlayer == null) return
                if (!fromUser) return
                exoPlayer?.seekTo((progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                exoPlayer?.playWhenReady = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                exoPlayer?.playWhenReady = true
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
            val permissionsToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) PERMISSIONS33 else PERMISSIONS
            if (permissionsToCheck.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
                requestPermissions.launch(permissionsToCheck)
            } else {
                Log.e(tag, "PERMISSIONS 권한 소유-SimpleExoPlayer 초기화")
                initalizePlayer()
            }
        }
    }

    private fun initPermissionsLauncher() {
        requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val bPhone = result[READ_PHONE_STATE] == true
            val bAudio = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result[READ_MEDIA_AUDIO] == true
            } else {
                result[READ_EXTERNAL_STORAGE] == true
            }

            if (bPhone && bAudio) {
                Log.e(tag, "PERMISSIONS 권한 소유-SimpleExoPlayer 초기화")
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
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()

            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> Log.e(tag, "재생 준비 완료")
                        Player.STATE_BUFFERING -> Log.e(tag, "재생 준비")
                        Player.STATE_IDLE -> Log.e(tag, "재생 실패")
                        Player.STATE_ENDED -> {
                            Log.e(tag, "재생 마침")
                            if (!isPlaying) {
                                if (musicList.size == 0) return

                                currentPlayIndex++
                                if (currentPlayIndex > musicList.size - 1) currentPlayIndex = 0

                                //Log.e(tag, "NextCurrentPlayIndex:" + currentPlayIndex)
                                val uri = Uri.parse(musicList[currentPlayIndex].musicPath)
                                playPlayer(uri)
                                btnPlay.visibility = View.INVISIBLE
                                btnPause.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            })
            exoPlayer?.volume = 1.0f
        }
    }

    private fun releasePlayer() {
        exoPlayer?.let {
            it.stop()
            it.release()
            exoPlayer = null
        }
    }

    private fun playPlayer(uri: Uri) {
        exoPlayer?.let {
            val mediaItem = MediaItem.fromUri(uri)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
            setSeekBarProgress()
        }
    }

    private fun stopPlayer() {
        exoPlayer?.stop()
    }

    private val isPlaying: Boolean
        get() = exoPlayer?.playbackState == Player.STATE_READY

    private fun PreplayPlayer() {
        exoPlayer?.let {
            var position = it.currentPosition
            position -= 3000
            it.seekTo(position)
        }
    }

    private fun NextplayPlayer() {
        exoPlayer?.let {
            var position = it.currentPosition
            position += 3000
            it.seekTo(position)
        }
    }

    private fun setSeekBarProgress() {
        stopplayerTimer()
        playerTimer = PlayerTimer()
        playerTimer?.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {
                exoPlayer?.let {
                    if (it.isPlaying) {
                        val position = it.currentPosition
                        val duration = it.duration
                        if (duration <= 0) return
                        seekBar.max = (duration / 1000).toInt()
                        seekBar.progress = (position / 1000).toInt()
                        txtStartTime.text = stringForTime(it.currentPosition.toInt())
                        txtEndTime.text = stringForTime(it.duration.toInt())
                    }
                }
            }
        })
        playerTimer?.start()
    }

    private fun stringForTime(timeMs: Int): String {
        val mFormatter = Formatter(StringBuilder(), Locale.getDefault())
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun stopplayerTimer() {
        playerTimer?.let {
            it.stop()
            playerTimer = null
        }
    }
}
