package com.daeseong.simple1exoplayer;

import android.app.Service;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

public class MusicService extends Service {

    private static final String TAG = MusicService.class.getSimpleName();

    private SimpleExoPlayer simpleExoPlayer;
    private LocalBroadcastManager localBroadcastManager;
    private final Binder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this.getApplicationContext());
        DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector();
        DefaultLoadControl defaultLoadControl = new DefaultLoadControl();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext(), defaultRenderersFactory, defaultTrackSelector, defaultLoadControl);

        simpleExoPlayer.addListener(new SimpleExoPlayer.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if (playbackState == ExoPlayer.STATE_BUFFERING) {

                    Log.e(TAG, "재생 준비");

                    Intent intent = new Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS");
                    intent.putExtra("state", PlaybackState.STATE_BUFFERING);
                    localBroadcastManager.sendBroadcast(intent);

                } else if (playbackState == ExoPlayer.STATE_READY) {

                    Log.e(TAG, "재생 준비 완료");

                    Intent intent = new Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS");
                    if (playWhenReady) {
                        intent.putExtra("state", PlaybackState.STATE_PLAYING);
                    } else {
                        intent.putExtra("state", PlaybackState.STATE_PAUSED);
                    }
                    localBroadcastManager.sendBroadcast(intent);

                } else if (playbackState == ExoPlayer.STATE_ENDED) {

                    Log.e(TAG, "재생 마침");

                    Intent intent = new Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS");
                    intent.putExtra("state", PlaybackState.STATE_NONE);
                    localBroadcastManager.sendBroadcast(intent);

                } else if (playbackState == ExoPlayer.STATE_IDLE) {

                    Log.e(TAG, "재생 실패");

                    Intent intent = new Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS");
                    intent.putExtra("state", PlaybackState.STATE_ERROR);
                    localBroadcastManager.sendBroadcast(intent);
                }

            }
        });

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void playPlayer(String sUrl) {
        if (simpleExoPlayer != null) {
            MediaSource mediaSource = getMediaSource(Uri.parse(sUrl));
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(true);
        }
    }

    public void playPlayer(Uri uri) {
        if (simpleExoPlayer != null) {
            MediaSource mediaSource = getMediaSource(uri);
            simpleExoPlayer.prepare(mediaSource, true, false);
            simpleExoPlayer.setPlayWhenReady(true);
        }
    }

    public void stopPlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
        }
    }

    public void releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    public boolean isPlaying() {
        if (simpleExoPlayer != null) {
            return simpleExoPlayer.getPlaybackState() == Player.STATE_READY;
        }
        return false;
    }

    public void PreplayPlayer() {
        if (simpleExoPlayer != null) {
            long position = simpleExoPlayer.getCurrentPosition();
            position -= 3000;
            simpleExoPlayer.seekTo(position);
        }
    }

    public void NextplayPlayer() {
        if (simpleExoPlayer != null) {
            long position = simpleExoPlayer.getCurrentPosition();
            position += 3000;
            simpleExoPlayer.seekTo(position);
        }
    }

    public void setPlayWhenReady(boolean bReady) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(bReady);
        }
    }

    public long getCurrentPosition() {
        long position = 0;
        if (simpleExoPlayer != null) {
            position = simpleExoPlayer.getCurrentPosition();
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        if (simpleExoPlayer != null) {
            duration = simpleExoPlayer.getDuration();
        }
        return duration;
    }

    public void seekTo(long progress) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.seekTo(progress);
        }
    }

    private MediaSource getMediaSource(Uri uri) {
        String sUserAgent = Util.getUserAgent(this, getPackageName());
        return new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, sUserAgent)).createMediaSource(uri);
    }
}
