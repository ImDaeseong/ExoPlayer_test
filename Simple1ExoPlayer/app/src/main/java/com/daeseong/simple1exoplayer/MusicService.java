package com.daeseong.simple1exoplayer;

import android.app.Service;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.Util;

public class MusicService extends Service {

    private static final String TAG = MusicService.class.getSimpleName();
    private ExoPlayer exoPlayer;
    private LocalBroadcastManager localBroadcastManager;
    private final Binder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {

        //Log.e(TAG, "onCreate");

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this.getApplicationContext());
        TrackSelector trackSelector = new DefaultTrackSelector(this.getApplicationContext());
        LoadControl loadControl = new DefaultLoadControl();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // ExoPlayer.Builder 사용하여 플레이어 생성
        exoPlayer = new ExoPlayer.Builder(this.getApplicationContext())
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Intent intent = new Intent("com.daeseong.simple1exoplayer.PLAYER_STATUS");

                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        Log.e(TAG, "재생 준비");
                        intent.putExtra("state", PlaybackState.STATE_BUFFERING);
                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, playWhenReady ? "재생 준비 완료" : "일시 정지");
                        intent.putExtra("state", playWhenReady ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);
                        break;
                    case Player.STATE_ENDED:
                        Log.e(TAG, "재생 마침");
                        intent.putExtra("state", PlaybackState.STATE_NONE);
                        break;
                    case Player.STATE_IDLE:
                        Log.e(TAG, "재생 실패");
                        intent.putExtra("state", PlaybackState.STATE_ERROR);
                        break;
                }
                localBroadcastManager.sendBroadcast(intent);
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
        if (exoPlayer != null) {
            MediaItem mediaItem = MediaItem.fromUri(sUrl);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void playPlayer(Uri uri) {
        if (exoPlayer != null) {
            MediaItem mediaItem = MediaItem.fromUri(uri);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void stopPlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
        }
    }

    public void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.getPlaybackState() == Player.STATE_READY;
    }

    public void prePlayPlayer() {
        if (exoPlayer != null) {
            long position = exoPlayer.getCurrentPosition();
            exoPlayer.seekTo(Math.max(position - 3000, 0));
        }
    }

    public void nextPlayPlayer() {
        if (exoPlayer != null) {
            long position = exoPlayer.getCurrentPosition();
            exoPlayer.seekTo(position + 3000);
        }
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    public long getCurrentPosition() {
        return exoPlayer != null ? exoPlayer.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return exoPlayer != null ? exoPlayer.getDuration() : 0;
    }

    public void seekTo(long position) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(position);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private ProgressiveMediaSource getMediaSource(Uri uri) {
        String userAgent = Util.getUserAgent(this, getPackageName());
        return new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, userAgent))
                .createMediaSource(MediaItem.fromUri(uri));
    }
}
