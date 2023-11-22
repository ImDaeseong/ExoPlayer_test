package com.daeseong.simpleexoplayer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String VIDEO_URL = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4";
    private static final String AUDIO_ASSET = "asset:///The Lazy Song.mp3";

    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;

    private Long currentPosition = 0L;
    private int currentWindowIndex = 0;
    private boolean playWhenReady = true;
    private boolean isVideoPlaying = false;

    private Button button1;

    // 타이틀 바 초기화
    private void initTitleBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.rgb(255, 255, 255));
        }

        try {
            // 안드로이드 8.0 오레오 버전에서만 오류 발생
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    // ExoPlayer 초기화
    private void initializePlayer(String mediaUrl) {
        if (simpleExoPlayer == null) {
            DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(getApplicationContext());
            DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector();
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    getApplicationContext(), defaultRenderersFactory, defaultTrackSelector, defaultLoadControl);

            simpleExoPlayer.addListener(new ExoPlayer.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    handlePlayerStateChanged(playbackState);
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    handleError();
                }
            });

            // 화면 플레이어 연결
            playerView.setPlayer(simpleExoPlayer);
        }

        MediaSource mediaSource = getMediaSource(Uri.parse(mediaUrl));
        simpleExoPlayer.prepare(mediaSource, true, false);
        simpleExoPlayer.setPlayWhenReady(playWhenReady);
    }

    // ExoPlayer 상태에 따른 처리
    private void handlePlayerStateChanged(int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_READY:
                Log.e(TAG, "재생 준비 완료");
                button1.setVisibility(View.GONE);
                break;
            case ExoPlayer.STATE_BUFFERING:
                Log.e(TAG, "재생 준비");
                button1.setVisibility(View.GONE);
                break;
            case ExoPlayer.STATE_IDLE:
                Log.e(TAG, "재생 실패");
                button1.setVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_ENDED:
                Log.e(TAG, "재생 마침");
                button1.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ExoPlayer 에러 처리
    private void handleError() {
        button1.setVisibility(View.VISIBLE);
        stopPlayer();
    }

    // ExoPlayer 해제
    private void releasePlayer() {
        if (simpleExoPlayer != null) {
            currentPosition = simpleExoPlayer.getCurrentPosition();
            currentWindowIndex = simpleExoPlayer.getCurrentWindowIndex();
            playWhenReady = simpleExoPlayer.getPlayWhenReady();

            playerView.setPlayer(null);
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    // 미디어 재생
    private void playMedia(Uri uri) {
        if (simpleExoPlayer != null) {
            MediaSource mediaSource = getMediaSource(uri);
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    // 미디어 정지
    private void stopPlayer() {
        if (simpleExoPlayer != null) {
            currentPosition = simpleExoPlayer.getCurrentPosition();
            currentWindowIndex = simpleExoPlayer.getCurrentWindowIndex();
            playWhenReady = simpleExoPlayer.getPlayWhenReady();
            simpleExoPlayer.stop();
        }
    }

    // 미디어 소스 가져오기
    private MediaSource getMediaSource(Uri uri) {
        String userAgent = Util.getUserAgent(this, getPackageName());
        return new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, userAgent)).createMediaSource(uri);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTitleBar();

        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.playerView);

        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopPlayer();

                if (isVideoPlaying) {
                    playMedia(Uri.parse(VIDEO_URL));
                    isVideoPlaying = false;
                } else {
                    Uri audioUri = Uri.parse(AUDIO_ASSET);
                    playMedia(audioUri);
                    isVideoPlaying = true;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23) {
            initializePlayer(VIDEO_URL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }
}
