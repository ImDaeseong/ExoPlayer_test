package com.daeseong.simple1exoplayer;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

public class MusicApplication extends Application {

    private static final String TAG = MusicApplication.class.getSimpleName();

    private static MusicApplication mInstance;
    private static Context mContext;

    public static synchronized MusicApplication getInstance() {
        return mInstance;
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = getApplicationContext();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
