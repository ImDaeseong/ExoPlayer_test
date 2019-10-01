package com.daeseong.simpleexoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;

    private Long currentPosition = 0L;
    private int currentWindowIndex = 0;
    private Boolean playWhenReady = true;

    private Button button1;

    private Boolean bCheck = false;
    private String sUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.playerView);

        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopPlayer();

                if(bCheck) {
                    playPlayer(sUrl);
                    bCheck = false;
                }else {
                    Uri uri = Uri.parse("asset:///The Lazy Song.mp3");
                    playPlayer(uri);
                    bCheck = true;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23) {
            initalizePlayer(sUrl);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }

    }

    private void initalizePlayer(String sUrl){

        if(simpleExoPlayer == null){

            DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this.getApplicationContext());
            DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector();
            DefaultLoadControl defaultLoadControl = new DefaultLoadControl();

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext(),
                    defaultRenderersFactory, defaultTrackSelector, defaultLoadControl);


            simpleExoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    switch (playbackState) {
                        case ExoPlayer.STATE_READY:
                            button1.setVisibility(View.GONE);
                            break;
                        case ExoPlayer.STATE_BUFFERING:
                            button1.setVisibility(View.VISIBLE);
                            break;
                        case ExoPlayer.STATE_IDLE:
                            button1.setVisibility(View.VISIBLE);
                            break;
                        case ExoPlayer.STATE_ENDED:
                            button1.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });

            //화면 플레이어 연결
            playerView.setPlayer(simpleExoPlayer);
        }

        MediaSource mediaSource = getMediaSource(Uri.parse(sUrl));
        simpleExoPlayer.prepare(mediaSource, true, false);
        simpleExoPlayer.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer(){
        if(simpleExoPlayer != null){
            currentPosition = simpleExoPlayer.getCurrentPosition();
            currentWindowIndex = simpleExoPlayer.getCurrentWindowIndex();
            playWhenReady = simpleExoPlayer.getPlayWhenReady();

            playerView.setPlayer(null);
            simpleExoPlayer.release();
            simpleExoPlayer =null;
        }
    }

    private void playPlayer(String sUrl){
        if(simpleExoPlayer != null){
            MediaSource mediaSource = getMediaSource(Uri.parse(sUrl));
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    private void playPlayer(Uri uri){
        if(simpleExoPlayer != null){
            MediaSource mediaSource = getMediaSource(uri);
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    private void stopPlayer(){
        if(simpleExoPlayer != null){
            currentPosition = simpleExoPlayer.getCurrentPosition();
            currentWindowIndex = simpleExoPlayer.getCurrentWindowIndex();
            playWhenReady = simpleExoPlayer.getPlayWhenReady();
            simpleExoPlayer.stop();
        }
    }

    private MediaSource getMediaSource(Uri uri){

        String sUserAgent = Util.getUserAgent(this, getPackageName());

        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(uri);

        /*
        if (uri.getLastPathSegment().contains("mp3") || uri.getLastPathSegment().contains("mp4")) {

            return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(sUserAgent)).createMediaSource(uri);

        } else if (uri.getLastPathSegment().contains("m3u8")) {

            return new HlsMediaSource.Factory(new DefaultHttpDataSourceFactory(sUserAgent)).createMediaSource(uri);

        } else {

            return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(uri);

        }
        */
    }

}
