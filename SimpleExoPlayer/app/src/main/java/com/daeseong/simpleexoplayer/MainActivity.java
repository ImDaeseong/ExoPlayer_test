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
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String VIDEO_URL = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4";
    private static final String AUDIO_ASSET = "asset:///The Lazy Song.mp3";

    private PlayerView playerView;
    private ExoPlayer player;

    private long currentPosition = 0L;
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
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    handlePlayerStateChanged(playbackState);
                }

                @Override
                public void onPlayerError(androidx.media3.common.PlaybackException error) {
                    handleError();
                }
            });

            // 화면 플레이어 연결
            playerView.setPlayer(player);
        }

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(playWhenReady);
    }

    // ExoPlayer 상태에 따른 처리
    private void handlePlayerStateChanged(int playbackState) {
        switch (playbackState) {
            case Player.STATE_READY:
                Log.e(TAG, "재생 준비 완료");
                button1.setVisibility(View.GONE);
                break;
            case Player.STATE_BUFFERING:
                Log.e(TAG, "재생 준비");
                button1.setVisibility(View.GONE);
                break;
            case Player.STATE_IDLE:
                Log.e(TAG, "재생 실패");
                button1.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
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
        if (player != null) {
            currentPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();

            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }

    // 미디어 재생
    private void playMedia(Uri uri) {
        if (player != null) {
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(playWhenReady);
        }
    }

    // 미디어 정지
    private void stopPlayer() {
        if (player != null) {
            currentPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
            player.stop();
        }
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
        initializePlayer(VIDEO_URL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }
}