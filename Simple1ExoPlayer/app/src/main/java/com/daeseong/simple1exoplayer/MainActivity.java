package com.daeseong.simple1exoplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageButton btnPre, btnPlay, btnPause, btnNext, btnPrevious, btnNextgo, btnSearch;
    private TextView txtStartTime, txtEndTime, txtDesc;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;

    private MusicService musicService;
    private BroadcastReceiver broadcastReceiver;
    private Intent intentservice;

    private ArrayList<MusicInfo> musicList  = new ArrayList<MusicInfo>();
    private int CurrentPlayIndex = -1;

    private MarqueeTask taskMarquee;
    private Timer timerMarquee = null;

    public ActivityResultLauncher<String[]> requestPermissions;

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] PERMISSIONS33 = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_MEDIA_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitTitleBar();

        setContentView(R.layout.activity_main);

        initPermissionsLauncher();

        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);
        txtDesc = findViewById(R.id.tvDesc);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions();

                //음악 폴더 선택
                getMusicList item = new getMusicList();
                musicList = item.getData();

                if (musicList.size() == 0) return;

                CurrentPlayIndex = 0;

                //Marquee
                txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri);
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //이전곡
        btnPre = findViewById(R.id.btnPre);
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(musicList.size() == 0) return;

                CurrentPlayIndex--;

                if (CurrentPlayIndex < 0)
                    CurrentPlayIndex = musicList.size() - 1;

                //Marquee
                txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri);
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //다음곡
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(musicList.size() == 0) return;

                CurrentPlayIndex++;

                if (CurrentPlayIndex > (musicList.size() - 1))
                    CurrentPlayIndex = 0;

                //Marquee
                txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri);
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //연주
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlayWhenReady(true);
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //일시정지
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlayWhenReady(false);
                btnPlay.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
            }
        });

        //진행바
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(!fromUser) return;

                seekTo(progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setPlayWhenReady(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setPlayWhenReady(true);
            }
        });

        checkPermissions();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MusicService.MusicBinder mServiceBinder = (MusicService.MusicBinder) iBinder;
            musicService = mServiceBinder.getMusicService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.exit(0);
        }
    };

    private void InitTitleBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.rgb(255, 255, 255));
        }

        try {
            //안드로이드 8.0 오레오 버전에서만 오류 발생
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    private void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean bPermissResult = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                for (String permission : PERMISSIONS33) {
                    bPermissResult = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                    if (!bPermissResult) {
                        break;
                    }
                }

                if (!bPermissResult) {
                    requestPermissions.launch(PERMISSIONS33);
                } else {
                    Log.e(TAG, "PERMISSIONS33 권한 소유-SimpleExoPlayer 초기화");
                    initalizePlayer();
                }

            } else {

                for (String permission : PERMISSIONS) {
                    bPermissResult = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                    if (!bPermissResult) {
                        break;
                    }
                }

                if (!bPermissResult) {
                    requestPermissions.launch(PERMISSIONS);
                } else {
                    Log.e(TAG, "PERMISSIONS 권한 소유-SimpleExoPlayer 초기화");
                    initalizePlayer();
                }
            }
        }
    }

    private void initPermissionsLauncher() {

        requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

            boolean bPhone = false;
            boolean bAudio = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                bPhone = Boolean.TRUE.equals(result.get(Manifest.permission.READ_PHONE_STATE));
                bAudio = Boolean.TRUE.equals(result.get(Manifest.permission.READ_MEDIA_AUDIO));

            } else {

                bPhone = Boolean.TRUE.equals(result.get(Manifest.permission.READ_PHONE_STATE));
                bAudio = Boolean.TRUE.equals(result.get(Manifest.permission.READ_EXTERNAL_STORAGE));
            }

            if (bPhone && bAudio) {
                Log.e(TAG, "PERMISSIONS 권한 소유-SimpleExoPlayer 초기화");
                initalizePlayer();
            } else {
                Log.e(TAG, "PERMISSIONS 권한 미소유");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        musicList.clear();
        closeMarqueeTimer();
        stopplayerTimer();
        releasePlayer();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        stopService(intentservice);
        unbindService(serviceConnection);
    }

    private void initalizePlayer() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                if (tm != null) {
                    if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {

                        if(isPlaying()) {
                            setPlayWhenReady(false);
                            btnPlay.setVisibility(View.VISIBLE);
                            btnPause.setVisibility(View.INVISIBLE);
                        }

                    }
                }

                int playerState = intent.getIntExtra("state", 0);
                if (playerState == PlaybackState.STATE_BUFFERING) {
                    Log.e(TAG, "PlaybackState.STATE_BUFFERING");
                } else if (playerState == PlaybackState.STATE_PLAYING) {
                    Log.e(TAG, "PlaybackState.STATE_PLAYING");
                } else if (playerState == PlaybackState.STATE_PAUSED) {
                    Log.e(TAG, "PlaybackState.STATE_PAUSED");
                } else if (playerState == PlaybackState.STATE_NONE) {

                    if(!isPlaying()){

                        if(musicList.size() == 0) return;

                        CurrentPlayIndex++;

                        if (CurrentPlayIndex > (musicList.size() - 1))
                            CurrentPlayIndex = 0;

                        //Marquee
                        txtDesc.setText(musicList.get(CurrentPlayIndex).getMusicName());

                        Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                        playPlayer(uri);
                        btnPlay.setVisibility(View.INVISIBLE);
                        btnPause.setVisibility(View.VISIBLE);
                    }
                }

            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, filter);
        }

        //music service
        intentservice = new Intent(MainActivity.this, MusicService.class);
        bindService(intentservice, serviceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),  new IntentFilter("com.daeseong.simple1exoplayer.PLAYER_STATUS"));
    }

    private void releasePlayer(){
        if(musicService != null ){
            musicService.releasePlayer();
        }
    }

    private void playPlayer(String sUrl){
        if(musicService != null ){
            musicService.playPlayer(sUrl);
            setSeekBarProgress();
        }
    }

    private void playPlayer(Uri uri){
        if(musicService != null ){
            musicService.playPlayer(uri);
            setSeekBarProgress();
        }
    }

    private void stopPlayer(){
        if(musicService != null ){
            musicService.stopPlayer();
        }
    }

    private boolean isPlaying(){
        if(musicService != null ){
            return  musicService.isPlaying();
        }
        return false;
    }

    private void PreplayPlayer(){
        if(musicService != null ){
            musicService.prePlayPlayer();
        }
    }

    private void NextplayPlayer(){
        if(musicService != null ){
            musicService.nextPlayPlayer();
        }
    }

    private void setPlayWhenReady(boolean bReady){
        if(musicService != null ){
            musicService.setPlayWhenReady(bReady);
        }
    }

    private long getCurrentPosition(){
        long position = 0;
        if(musicService != null ){
            position = musicService.getCurrentPosition();
        }
        return position;
    }

    private long getDuration(){
        long duration = 0;
        if(musicService != null ){
            duration = musicService.getDuration();
        }
        return duration;
    }

    private void seekTo(long progress){
        if(musicService != null ){
            musicService.seekTo(progress);
        }
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void stopplayerTimer(){
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer = null;
        }
    }

    private void setSeekBarProgress(){

        //Marquee
        startMarqueeTimer();

        stopplayerTimer();

        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {

                if (isPlaying()) {

                    long position = getCurrentPosition();
                    long duration = getDuration();

                    if (duration <= 0) return;

                    seekBar.setMax((int) duration / 1000);
                    seekBar.setProgress((int) position / 1000);

                    txtStartTime.setText(stringForTime((int) getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int) getDuration()));
                }
            }
        });
        playerTimer.start();
    }

    private void closeMarqueeTimer(){
        if (timerMarquee != null) {
            timerMarquee.cancel();
            timerMarquee = null;
        }
    }

    private void startMarqueeTimer(){
        closeMarqueeTimer();
        taskMarquee = new MarqueeTask(txtDesc);
        timerMarquee = new Timer();
        timerMarquee.schedule(taskMarquee, 0, 10000);
    }
}
