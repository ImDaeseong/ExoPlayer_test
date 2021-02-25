package com.daeseong.simple2exoplayer

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var btnPre: ImageButton? = null
    private var btnPlay:ImageButton? = null
    private var btnPause:ImageButton? = null
    private var btnNext:ImageButton? = null
    private var btnPrevious:ImageButton? = null
    private var btnNextgo:ImageButton? = null
    private var btnSearch:ImageButton? = null
    private var txtStartTime: TextView? = null
    private var txtEndTime:TextView? = null
    private var seekBar: SeekBar? = null
    private var playerTimer: PlayerTimer? = null

    private val musicList = ArrayList<MusicInfo>()
    private var CurrentPlayIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main)

        checkPermissions()

        //SimpleExoPlayer 초기화
        initalizePlayer()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch!!.setOnClickListener(View.OnClickListener {

            checkPermissions()

            //음악 폴더 선택
            musicList.clear()
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Audio.AudioColumns.ARTIST,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.DATA
                ), MediaStore.Audio.AudioColumns.IS_MUSIC + " > 0", null, null
            )

            while (cursor!!.moveToNext()) {

                val sMusicPath: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA))

                /*
                val nIndex = sMusicPath.lastIndexOf("/")
                val sFileName = sMusicPath.substring(nIndex + 1)
                val sFilePath = sMusicPath.substring(0, nIndex + 1)
                Log.e(tag, sFileName)
                Log.e(tag, sFilePath)
                */

                /*
                //music3 폴더만 검색해서 가져오기
                if (sMusicPath.contains("music3")) {
                    val info = MusicInfo()
                    info.musicPath = sMusicPath
                    musicList.add(info)
                }
                */

                val info = MusicInfo()
                info.musicPath = sMusicPath
                musicList.add(info)
            }

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex = 0

            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPrevious!!.setOnClickListener {
            PreplayPlayer()
        }

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo)
        btnNextgo!!.setOnClickListener {
            NextplayPlayer()
        }

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre!!.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex--

            if (CurrentPlayIndex < 0) CurrentPlayIndex = musicList.size - 1

            //Log.e(tag, "PreCurrentPlayIndex:$CurrentPlayIndex")
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //다음곡
        btnNext = findViewById(R.id.btnNext)
        btnNext!!.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex++

            if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

            //Log.e(tag, "NextCurrentPlayIndex:$CurrentPlayIndex")
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay!!.setOnClickListener {
            simpleExoPlayer!!.playWhenReady = true
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        }

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause!!.setOnClickListener {
            simpleExoPlayer!!.playWhenReady = false
            btnPlay!!.visibility = View.VISIBLE
            btnPause!!.visibility = View.INVISIBLE
        }


        //진행바
        seekBar = findViewById<View>(R.id.seekBar) as SeekBar
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (simpleExoPlayer == null) return
                if (!fromUser) return
                simpleExoPlayer!!.seekTo(progress * 1000.toLong())
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
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_bg)
        }
    }

    private fun checkPermissions() {

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
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

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
                this.applicationContext,
                defaultRenderersFactory,
                defaultTrackSelector,
                defaultLoadControl
            )

            simpleExoPlayer!!.addListener(object : Player.EventListener {

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                    when (playbackState) {
                        ExoPlayer.STATE_READY -> {
                        }
                        ExoPlayer.STATE_BUFFERING -> {
                        }
                        ExoPlayer.STATE_IDLE -> {
                        }
                        ExoPlayer.STATE_ENDED ->

                            //현재곡 완료시 다음곡 자동시작
                            if (simpleExoPlayer != null) {

                                if (!isPlaying()) {

                                    if (musicList.size == 0) return

                                    CurrentPlayIndex++

                                    if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

                                    //Log.e(tag, "NextCurrentPlayIndex:$CurrentPlayIndex")

                                    val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
                                    playPlayer(uri)
                                    btnPlay!!.visibility = View.INVISIBLE
                                    btnPause!!.visibility = View.VISIBLE
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

    private fun isPlaying(): Boolean {
        return if (simpleExoPlayer != null) {
            simpleExoPlayer!!.playbackState == Player.STATE_READY
        } else false
    }

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

    private fun getMediaSource(uri: Uri): MediaSource? {
        val sUserAgent = Util.getUserAgent(
            this,
            packageName
        )
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, sUserAgent))
            .createMediaSource(uri)
    }

    private fun stringForTime(timeMs: Int): String? {

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
            playerTimer!!.removeMessages(0)
        }
    }

    private fun setSeekBarProgress() {

        stopplayerTimer()

        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback {

            override fun onTick(timeMillis: Long) {

                val position = simpleExoPlayer!!.currentPosition
                val duration = simpleExoPlayer!!.duration

                if (duration <= 0) return

                seekBar!!.max = duration.toInt() / 1000
                seekBar!!.progress = position.toInt() / 1000
                txtStartTime!!.text = stringForTime(simpleExoPlayer!!.currentPosition.toInt())
                txtEndTime!!.text = stringForTime(simpleExoPlayer!!.duration.toInt())
            }
        })
        playerTimer!!.start()
    }
}
