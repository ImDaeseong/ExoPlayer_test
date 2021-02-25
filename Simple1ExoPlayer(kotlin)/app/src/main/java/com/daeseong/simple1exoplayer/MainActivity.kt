package com.daeseong.simple1exoplayer

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.daeseong.simple1exoplayer.MusicService.MusicBinder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private var btnPre: ImageButton? = null
    private var btnPlay:ImageButton? = null
    private var btnPause:ImageButton? = null
    private var btnNext:ImageButton? = null
    private var btnPrevious:ImageButton? = null
    private var btnNextgo:ImageButton? = null
    private var btnSearch:ImageButton? = null
    private var txtStartTime: TextView? = null
    private var txtEndTime:TextView? = null
    private var txtDesc:TextView? = null
    private var seekBar: SeekBar? = null
    private var playerTimer: PlayerTimer? = null

    private var musicService: MusicService? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentservice: Intent? = null

    private var musicList = ArrayList<MusicInfo>()
    private var CurrentPlayIndex = -1

    private var taskMarquee: MarqueeTask? = null
    private var timerMarquee: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main)

        //SimpleExoPlayer 초기화
        initalizePlayer()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)
        txtDesc = findViewById(R.id.tvDesc)

        btnSearch = findViewById(R.id.btnSearch)
        btnSearch!!.setOnClickListener(View.OnClickListener {

            checkPermissions()

            //음악 폴더 선택
            val item = getMusicList()
            if (item.getData()) {
                musicList = item.musicList
            }

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex = 0

            //Marquee
            txtDesc!!.text = musicList[CurrentPlayIndex].musicName
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre!!.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener

            CurrentPlayIndex--

            if (CurrentPlayIndex < 0) CurrentPlayIndex = musicList.size - 1

            //Marquee
            txtDesc!!.text = musicList[CurrentPlayIndex].musicName
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

            //Marquee
            txtDesc!!.text = musicList[CurrentPlayIndex].musicName
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay!!.setOnClickListener(View.OnClickListener {

            setPlayWhenReady(true)
            btnPlay!!.visibility = View.INVISIBLE
            btnPause!!.visibility = View.VISIBLE
        })

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause!!.setOnClickListener(View.OnClickListener {

            setPlayWhenReady(false)
            btnPlay!!.visibility = View.VISIBLE
            btnPause!!.visibility = View.INVISIBLE
        })

        //진행바
        seekBar = findViewById<View>(R.id.seekBar) as SeekBar
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                seekTo((progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                setPlayWhenReady(false)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                setPlayWhenReady(true)
            }
        })

        checkPermissions()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder) {
            val mServiceBinder = iBinder as MusicBinder
            musicService = mServiceBinder.musicService
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            exitProcess(0)
        }
    }

    private fun InitTitleBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_bg)
        }
    }

    private fun checkPermissions() {

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        musicList.clear()
        closeMarqueeTimer()
        stopplayerTimer()
        releasePlayer()

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }

        stopService(intentservice)
        unbindService(serviceConnection)
    }

    private fun initalizePlayer() {

        broadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                if (tm != null) {

                    if (tm.callState == TelephonyManager.CALL_STATE_RINGING) {
                        if (isPlaying()) {
                            setPlayWhenReady(false)
                            btnPlay!!.visibility = View.VISIBLE
                            btnPause!!.visibility = View.INVISIBLE
                        }
                    }
                }

                val playerState = intent.getIntExtra("state", 0)
                if (playerState == PlaybackState.STATE_BUFFERING) {

                    Log.e(tag, "PlaybackState.STATE_BUFFERING")
                } else if (playerState == PlaybackState.STATE_PLAYING) {

                    Log.e(tag, "PlaybackState.STATE_PLAYING")
                } else if (playerState == PlaybackState.STATE_PAUSED) {

                    Log.e(tag, "PlaybackState.STATE_PAUSED")
                } else if (playerState == PlaybackState.STATE_NONE) {

                    if (!isPlaying()) {

                        if (musicList.size == 0) return

                        CurrentPlayIndex++

                        if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

                        //Marquee
                        txtDesc!!.text = musicList[CurrentPlayIndex].musicName
                        val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
                        playPlayer(uri)
                        btnPlay!!.visibility = View.INVISIBLE
                        btnPause!!.visibility = View.VISIBLE
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(broadcastReceiver, filter)

        //music service
        intentservice = Intent(this, MusicService::class.java)
        bindService(intentservice, serviceConnection, BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver!!,
            IntentFilter("com.daeseong.simple1exoplayer.PLAYER_STATUS")
        )
    }

    private fun releasePlayer() {
        if (musicService != null) {
            musicService!!.releasePlayer()
        }
    }

    private fun playPlayer(sUrl: String) {
        if (musicService != null) {
            musicService!!.playPlayer(sUrl)
            setSeekBarProgress()
        }
    }

    private fun playPlayer(uri: Uri) {
        if (musicService != null) {
            musicService!!.playPlayer(uri)
            setSeekBarProgress()
        }
    }

    private fun stopPlayer() {
        if (musicService != null) {
            musicService!!.stopPlayer()
        }
    }

    private fun isPlaying(): Boolean {
        return if (musicService != null) {
            musicService!!.isPlaying()
        } else false
    }

    private fun PreplayPlayer() {
        if (musicService != null) {
            musicService!!.PreplayPlayer()
        }
    }

    private fun NextplayPlayer() {
        if (musicService != null) {
            musicService!!.NextplayPlayer()
        }
    }

    private fun setPlayWhenReady(bReady: Boolean) {
        if (musicService != null) {
            musicService!!.setPlayWhenReady(bReady)
        }
    }

    private fun getCurrentPosition(): Long {
        var position: Long = 0
        if (musicService != null) {
            position = musicService!!.getCurrentPosition()
        }
        return position
    }

    private fun getDuration(): Long {
        var duration: Long = 0
        if (musicService != null) {
            duration = musicService!!.getDuration()
        }
        return duration
    }

    private fun seekTo(progress: Long) {
        if (musicService != null) {
            musicService!!.seekTo(progress)
        }
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

        //Marquee
        startMarqueeTimer()
        stopplayerTimer()
        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback {

            override fun onTick(timeMillis: Long) {

                val position = getCurrentPosition()
                val duration = getDuration()
                if (duration <= 0) return
                seekBar!!.max = duration.toInt() / 1000
                seekBar!!.progress = position.toInt() / 1000
                txtStartTime!!.text = stringForTime(getCurrentPosition().toInt())
                txtEndTime!!.text = stringForTime(getDuration().toInt())
            }
        })
        playerTimer!!.start()
    }

    private fun closeMarqueeTimer() {
        if (timerMarquee != null) {
            timerMarquee!!.cancel()
            timerMarquee = null
        }
    }

    private fun startMarqueeTimer() {
        closeMarqueeTimer()
        taskMarquee = MarqueeTask(txtDesc!!)
        timerMarquee = Timer()
        timerMarquee!!.schedule(taskMarquee, 0, 10000)
    }
}
