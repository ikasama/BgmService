package com.badlogic.masaki.bgmservice.library;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Class that plays bgm in background
 * Created by shojimasaki on 2016/05/21.
 */
public class BgmService extends Service implements Music {
    public static final String TAG = BgmService.class.getSimpleName();

    /**
     * Binder through which clients and BgmService communicate with each other
     */
    private final IBinder mBinder = new BgmBinder();

    /**
     * Worker that runs in background to play music
     */
    private BgmWorker mWorker;

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        initializes mWorker
         */
        mWorker = new BgmWorker(this);
        mWorker.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind called");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onUnbind called");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        release mWorker when destroyed
         */
        mWorker.addCommand(new BgmCommand(BgmCommand.Type.RELEASE));
        mWorker.addCommand(new BgmCommand(BgmCommand.Type.DESTROY));
    }

    /**
     * Starts music
     * @param fileName music file's name
     */
    @Override
    public void start(String fileName) {
        if (mWorker == null) {
            mWorker = new BgmWorker(this, fileName);
            mWorker.start();
        }

        BgmCommand command = new BgmCommand(fileName, BgmCommand.Type.START);
        mWorker.addCommand(command);
    }

    /**
     * Pauses music
     */
    @Override
    public void pause() {
        BgmCommand command = new BgmCommand(BgmCommand.Type.PAUSE);
        mWorker.addCommand(command);
    }

    /**
     * Resumes music
     */
    @Override
    public void resume() {
        BgmCommand command = new BgmCommand(BgmCommand.Type.RESUME);
        mWorker.addCommand(command);
    }

    @Override
    public void stop() {
        BgmCommand command = new BgmCommand(BgmCommand.Type.STOP);
        mWorker.addCommand(command);
    }

    /**
     * Releases worker
     */
    @Override
    public void release() {
        BgmCommand command = new BgmCommand(BgmCommand.Type.RELEASE);
        mWorker.addCommand(command);
    }

    /**
     * Checks if BgmWorker is playing
     * @return true if BgmWorker is playing
     */
    @Override
    public boolean isPlaying() {
        try {
            return mWorker != null && mWorker.isPlaying();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if BgmWorker is playable
     * @return true if BgmWorker is playable
     */
    public boolean isPlayable() {
        return mWorker.isPlayable();
    }

    /**
     * Binder through which clients and BgmService communicate with each other
     */
    public class BgmBinder extends Binder {
        public BgmService getService() {
            return BgmService.this;
        }
    }
}
