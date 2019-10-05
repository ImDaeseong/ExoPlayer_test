package com.daeseong.simple2exoplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        //SimpleExoPlayer 초기화
        initalizePlayer();

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
                        new String[]
                                {
                                        MediaStore.Audio.AudioColumns.ARTIST,
                                        MediaStore.Audio.AudioColumns.TITLE,
                                        MediaStore.Audio.AudioColumns.DATA
                                },
                        MediaStore.Audio.AudioColumns.IS_MUSIC + " > 0",
                        null,
                        null
                );

                while(cursor.moveToNext())
                {
                    String sMusicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));

                    //int nIndex = sMusicPath.lastIndexOf("/");
                    //String sFileName = sMusicPath.substring(nIndex+1);
                    //String sFilePath = sMusicPath.substring(0, nIndex+1);
                    //Log.e(TAG, sFileName);
                    //Log.e(TAG, sFilePath);

                    if( sMusicPath.contains("music3")){
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

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,   new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        musicList.clear();
        stopplayerTimer();
        releasePlayer();
    }

    private void initalizePlayer(){

        if(simpleExoPlayer == null){

            DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this.getApplicationContext());
            DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector();
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext(), defaultRenderersFactory, defaultTrackSelector, defaultLoadControl);

            simpleExoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    switch (playbackState) {
                        case ExoPlayer.STATE_READY:
                            break;
                        case ExoPlayer.STATE_BUFFERING:
                            break;
                        case ExoPlayer.STATE_IDLE:
                            break;
                        case ExoPlayer.STATE_ENDED:

                            if(simpleExoPlayer != null) {
                                if(!isPlaying()){

                                    if(musicList.size() == 0) return;

                                    CurrentPlayIndex++;

                                    if (CurrentPlayIndex > (musicList.size() - 1))
                                        CurrentPlayIndex = 0;

                                    //Log.e(TAG, "NextCurrentPlayIndex:" + CurrentPlayIndex);

                                    Uri uri = Uri.parse(musicList.get(CurrentPlayIndex).getMusicPath());
                                    playPlayer(uri);
                                }
                            }

                            break;
                    }
                }
            });
        }
    }

    private void releasePlayer(){
        if(simpleExoPlayer != null){
            simpleExoPlayer.release();
            simpleExoPlayer =null;
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
        if(simpleExoPlayer != null){
            simpleExoPlayer.stop();
        }
    }

    private boolean isPlaying(){
        if(simpleExoPlayer != null) {
            return simpleExoPlayer.getPlaybackState() == Player.STATE_READY;
        }
        return false;
    }

    private void PreplayPlayer(){
        if(simpleExoPlayer != null) {
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

    private MediaSource getMediaSource(Uri uri){
        String sUserAgent = Util.getUserAgent(this, getPackageName());
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(uri);
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
        if(playerTimer != null){
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    private void setSeekBarProgress(){

        stopplayerTimer();

        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {

                long position = simpleExoPlayer.getCurrentPosition();
                long duration = simpleExoPlayer.getDuration();

                if (duration <= 0) return;

                seekBar.setMax((int) duration / 1000);
                seekBar.setProgress((int) position / 1000);

                txtStartTime.setText(stringForTime((int)simpleExoPlayer.getCurrentPosition()));
                txtEndTime.setText(stringForTime((int)simpleExoPlayer.getDuration()));
            }
        });
        playerTimer.start();
    }

}
