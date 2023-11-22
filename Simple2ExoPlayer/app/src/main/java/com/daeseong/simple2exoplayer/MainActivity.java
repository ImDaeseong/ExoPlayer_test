package com.daeseong.simple2exoplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SimpleExoPlayer simpleExoPlayer;
    private ImageButton btnPre, btnPlay, btnPause, btnNext, btnPrevious, btnNextgo, btnSearch;
    private TextView txtStartTime, txtEndTime;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;

    private ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
    private int CurrentPlayIndex = -1;

    public ActivityResultLauncher<String[]> requestPermissions;

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] PERMISSIONS33 = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_MEDIA_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitTitleBar();

        setContentView(R.layout.activity_main);

        initPermissionsLauncher();

        checkPermissions();

        txtStartTime = findViewById(R.id.startTime);
        txtEndTime = findViewById(R.id.endTime);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions();

                //음악 폴더 선택
                musicList.clear();
                Cursor cursor = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.DATA },
                        MediaStore.Audio.AudioColumns.IS_MUSIC + " > 0",
                        null,
                        null
                );

                int nIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);

                while(cursor.moveToNext()) {

                    if (nIndex == -1) {
                        continue;
                    }

                    String sMusicPath = cursor.getString(nIndex);

                    if( sMusicPath.contains("music2")){
                        MusicInfo info = new MusicInfo();
                        info.setMusicPath(sMusicPath);
                        musicList.add(info);
                    }
                }

                if (musicList.size() == 0) return;

                CurrentPlayIndex = 0;
                Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                playPlayer(uri);
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //3초 뒤로
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreplayPlayer();
            }
        });

        //3초 앞으로
        btnNextgo = findViewById(R.id.btnNextgo);
        btnNextgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NextplayPlayer();
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

                //Log.e(TAG, "PreCurrentPlayIndex:" + CurrentPlayIndex);

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

                //Log.e(TAG, "NextCurrentPlayIndex:" + CurrentPlayIndex);

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
                simpleExoPlayer.setPlayWhenReady(true);
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
            }
        });

        //일시정지
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleExoPlayer.setPlayWhenReady(false);
                btnPlay.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.INVISIBLE);
            }
        });

        //진행바
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(simpleExoPlayer == null) return;
                if(!fromUser) return;

                simpleExoPlayer.seekTo(progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                simpleExoPlayer.setPlayWhenReady(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                simpleExoPlayer.setPlayWhenReady(true);
            }
        });
    }

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
        stopplayerTimer();
        releasePlayer();
    }

    private void initalizePlayer(){

        if (simpleExoPlayer == null) {

            DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this.getApplicationContext());
            DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector();
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext(), defaultRenderersFactory, defaultTrackSelector, defaultLoadControl);

            simpleExoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    switch (playbackState) {
                        case ExoPlayer.STATE_READY:
                            Log.e(TAG, "재생 준비 완료");
                            break;
                        case ExoPlayer.STATE_BUFFERING:
                            Log.e(TAG, "재생 준비");
                            break;
                        case ExoPlayer.STATE_IDLE:
                            Log.e(TAG, "재생 실패");
                            break;
                        case ExoPlayer.STATE_ENDED:
                            Log.e(TAG, "재생 마침");

                            //현재곡 완료시 다음곡 자동시작
                            if(simpleExoPlayer != null) {
                                if(!isPlaying()){

                                    if(musicList.size() == 0) return;

                                    CurrentPlayIndex++;

                                    if (CurrentPlayIndex > (musicList.size() - 1))
                                        CurrentPlayIndex = 0;

                                    //Log.e(TAG, "NextCurrentPlayIndex:" + CurrentPlayIndex);

                                    Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                                    playPlayer(uri);
                                    btnPlay.setVisibility(View.INVISIBLE);
                                    btnPause.setVisibility(View.VISIBLE);
                                }
                            }

                            break;
                    }
                }
            });

            simpleExoPlayer.setVolume(1.0f);
        }
    }

    private void releasePlayer(){
        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    private void playPlayer(String sUrl){
        if(simpleExoPlayer != null){
            MediaSource mediaSource = getMediaSource(Uri.parse(sUrl));
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(true);
            setSeekBarProgress();
        }
    }

    private void playPlayer(Uri uri){
        if(simpleExoPlayer != null){
            MediaSource mediaSource = getMediaSource(uri);
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(true);
            setSeekBarProgress();
        }
    }

    private void stopPlayer(){
        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
        }
    }

    private boolean isPlaying(){
        if (simpleExoPlayer != null) {
            return simpleExoPlayer.getPlaybackState() == Player.STATE_READY;
        }
        return false;
    }

    private void PreplayPlayer(){
        if (simpleExoPlayer != null) {
            long position = simpleExoPlayer.getCurrentPosition();
            position -= 3000;
            simpleExoPlayer.seekTo(position);
        }
    }

    private void NextplayPlayer(){
        if(simpleExoPlayer != null) {
            long position = simpleExoPlayer.getCurrentPosition();
            position += 3000;
            simpleExoPlayer.seekTo(position);
        }
    }

    private MediaSource getMediaSource(Uri uri) {
        String sUserAgent = Util.getUserAgent(this, getPackageName());
        return new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(uri);
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

    private void setSeekBarProgress() {

        stopplayerTimer();

        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {

                if (simpleExoPlayer.isPlaying()) {

                    long position = simpleExoPlayer.getCurrentPosition();
                    long duration = simpleExoPlayer.getDuration();

                    if (duration <= 0) return;

                    seekBar.setMax((int) duration / 1000);
                    seekBar.setProgress((int) position / 1000);

                    txtStartTime.setText(stringForTime((int) simpleExoPlayer.getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int) simpleExoPlayer.getDuration()));
                }
            }
        });
        playerTimer.start();
    }
}
