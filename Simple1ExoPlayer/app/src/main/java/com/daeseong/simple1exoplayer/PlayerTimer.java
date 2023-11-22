package com.daeseong.simple1exoplayer;

import android.os.Handler;
import android.os.Looper;

public class PlayerTimer {

    private static final int DEFAULT_INTERVAL_MILLIS = 1000;

    private boolean isUpdate = false;
    private Callback callback;
    private long startTimeMillis;
    private Handler handler;

    public PlayerTimer() {
        handler = new Handler(Looper.getMainLooper());
    }

    private void init() {
        startTimeMillis = System.currentTimeMillis();
    }

    public void start() {
        init();
        isUpdate = true;
        handler.post(timerRunnable);
    }

    public void stop() {
        isUpdate = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUpdate) {
                callback.onTick(System.currentTimeMillis() - startTimeMillis);
                handler.postDelayed(this, DEFAULT_INTERVAL_MILLIS);
            }
        }
    };

    public interface Callback {
        void onTick(long timeMillis);
    }
}
