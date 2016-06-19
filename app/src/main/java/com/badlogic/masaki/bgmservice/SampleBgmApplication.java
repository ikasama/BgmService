package com.badlogic.masaki.bgmservice;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.badlogic.masaki.bgmservice.library.BgmService;
import com.badlogic.masaki.bgmservice.library.BgmSettings;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample application class that plays music.
 * Music stops while application is in background, and resumes in foreground.
 * Created by shojimasaki on 2016/05/26.
 */
public class SampleBgmApplication extends Application
        implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = SampleBgmApplication.class.getSimpleName();

    private static final String DEFAULT_BGM_FILE = "bgm2.mp3";

    /**
     * Plays music in background
     */
    private BgmService mService;

    /**
     * Flag indicates this application is bound to BgmService
     */
    private volatile boolean mBoundToService = false;

    /**
     * Current Activity's count
     */
    private AtomicInteger mActivityCount = new AtomicInteger();

    /**
     * Count that is incremented when Activity pauses
     */
    private AtomicInteger mPausedActivityCount = new AtomicInteger();

    /**
     * Count that is incremented when Activity resumes
     */
    private AtomicInteger mResumedActivityCount = new AtomicInteger();

    /**
     * Connection class through which we can get IBinder
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BgmService.BgmBinder binder = (BgmService.BgmBinder) service;
            mService = binder.getService();

            /*
            when connected with service, starts music
             */
            BgmSettings.setFileName(DEFAULT_BGM_FILE);

            if (!mService.isPlaying()) {
                mService.start(BgmSettings.getFileName());
            }
            mBoundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(mConnection);
            mBoundToService = false;
            mService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (!mBoundToService) {
            bindService(new Intent(this, BgmService.class), mConnection, BIND_AUTO_CREATE);
        }

        /*
        registers callbacks to listen for activity's lifecycle
         */
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mActivityCount.incrementAndGet();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (!mBoundToService) {
            bindService(new Intent(this, BgmService.class), mConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (mService != null && mService.isPlayable()) {
            mService.resume();
        } else {
            bindService(new Intent(this, BgmService.class), mConnection, BIND_AUTO_CREATE);
        }

        mResumedActivityCount.incrementAndGet();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mPausedActivityCount.incrementAndGet();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (mService == null) {
            return;
        }

        if (activity.isChangingConfigurations()) {
            return;
        }

        /*
        pauses music
         */
        if (mPausedActivityCount.get() == mResumedActivityCount.get()) {
            mService.pause();
        }

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (mActivityCount.decrementAndGet() > 0 ||
                activity.isChangingConfigurations()) {
            return;
        }

        if (mBoundToService) {
            unbindService(mConnection);
            mBoundToService = false;
        }
    }

    public BgmService getBgmService() {
        return mService;
    }

    public boolean isBoundToService() {
        return mBoundToService;
    }
}
