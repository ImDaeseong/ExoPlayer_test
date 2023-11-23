package com.daeseong.simple1exoplayer

import android.Manifest.permission.*
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.daeseong.simple1exoplayer.MusicService.MusicBinder
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName

    private lateinit var btnPre: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView
    private lateinit var txtDesc: TextView
    private lateinit var seekBar: SeekBar

    private var playerTimer: PlayerTimer? = null
    private var musicService: MusicService? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentservice: Intent? = null
    private var musicList = ArrayList<MusicInfo>()
    private var CurrentPlayIndex = -1
    private var taskMarquee: MarqueeTask? = null
    private var timerMarquee: Timer? = null

    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>

    private val PERMISSIONS = arrayOf(READ_PHONE_STATE, READ_EXTERNAL_STORAGE)
    private val PERMISSIONS33 = arrayOf(READ_PHONE_STATE, READ_MEDIA_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InitTitleBar()

        setContentView(R.layout.activity_main)

        initPermissionsLauncher()

        txtStartTime = findViewById(R.id.startTime)
        txtEndTime = findViewById(R.id.endTime)
        txtDesc = findViewById(R.id.tvDesc)
        btnSearch = findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener(View.OnClickListener {

            checkPermissions()

            //음악 폴더 선택
            val item = getMusicList()
            musicList = item.getData()
            if (musicList.size == 0) return@OnClickListener
            CurrentPlayIndex = 0

            //Marquee
            txtDesc.text = musicList[CurrentPlayIndex].musicName
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //이전곡
        btnPre = findViewById(R.id.btnPre)
        btnPre.setOnClickListener(View.OnClickListener {

            if (musicList.size == 0) return@OnClickListener
            CurrentPlayIndex--
            if (CurrentPlayIndex < 0) CurrentPlayIndex = musicList.size - 1

            //Marquee
            txtDesc.text = musicList[CurrentPlayIndex].musicName
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

            //Marquee
            txtDesc.text = musicList[CurrentPlayIndex].musicName
            val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
            playPlayer(uri)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //연주
        btnPlay = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener(View.OnClickListener {

            setPlayWhenReady(true)
            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
        })

        //일시정지
        btnPause = findViewById(R.id.btnPause)
        btnPause.setOnClickListener(View.OnClickListener {

            setPlayWhenReady(false)
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.INVISIBLE
        })

        //진행바
        seekBar = findViewById<View>(R.id.seekBar) as SeekBar
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
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
        override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
            val mServiceBinder = iBinder as MusicBinder
            musicService = mServiceBinder.getMusicService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            exitProcess(0)
        }
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

                        if (isPlaying) {
                            setPlayWhenReady(false)
                            btnPlay.visibility = View.VISIBLE
                            btnPause.visibility = View.INVISIBLE
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

                    if (!isPlaying) {

                        if (musicList.size == 0) return
                        CurrentPlayIndex++
                        if (CurrentPlayIndex > musicList.size - 1) CurrentPlayIndex = 0

                        //Marquee
                        txtDesc!!.text = musicList[CurrentPlayIndex].musicName
                        val uri = Uri.parse(musicList[CurrentPlayIndex].musicPath)
                        playPlayer(uri)
                        btnPlay.visibility = View.INVISIBLE
                        btnPause.visibility = View.VISIBLE
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(broadcastReceiver, filter)

        //music service
        intentservice = Intent(this@MainActivity, MusicService::class.java)
        bindService(intentservice, serviceConnection, BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, IntentFilter("com.daeseong.simple1exoplayer.PLAYER_STATUS"))
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

    private val isPlaying: Boolean
        private get() = if (musicService != null) {
            musicService!!.isPlaying()
        } else false

    private fun PreplayPlayer() {
        if (musicService != null) {
            musicService!!.preplayPlayer()
        }
    }

    private fun NextplayPlayer() {
        if (musicService != null) {
            musicService!!.nextplayPlayer()
        }
    }

    private fun setPlayWhenReady(bReady: Boolean) {
        if (musicService != null) {
            musicService!!.setPlayWhenReady(bReady)
        }
    }

    private val currentPosition: Long
        private get() {
            var position: Long = 0
            if (musicService != null) {
                position = musicService!!.getCurrentPosition()
            }
            return position
        }

    private val duration: Long
        private get() {
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

        //Marquee
        startMarqueeTimer()
        stopplayerTimer()
        playerTimer = PlayerTimer()
        playerTimer!!.setCallback(object : PlayerTimer.Callback {
            override fun onTick(timeMillis: Long) {

                if (isPlaying) {
                    val position = currentPosition
                    val duration = duration
                    if (duration <= 0) return
                    seekBar.max = duration.toInt() / 1000
                    seekBar.progress = position.toInt() / 1000
                    txtStartTime.text = stringForTime(currentPosition.toInt())
                    txtEndTime.text = stringForTime(duration.toInt())
                }
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
        taskMarquee = MarqueeTask(txtDesc)
        timerMarquee = Timer()
        timerMarquee!!.schedule(taskMarquee, 0, 10000)
    }
}