package com.badlogic.masaki.bgmservice.library;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Worker class that plays music
 * Created by shojimasaki on 2016/05/28.
 */
public class BgmWorker extends Thread {

    /**
     * States that represents MediaPlayer's playback-state
     */
    enum PlaybackState {
        IDLED,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        ENDED,
        ERROR,
    }

    public static final String TAG = BgmWorker.class.getSimpleName();

    /**
     * Service this worker is associated with
     */
    private BgmService mService;

    /**
     * Player to play music
     */
    private MediaPlayer mMediaPlayer;

    /**
     * Listener that listens for player's playback events
     */
    private PlaybackListener mListener;

    /**
     * Current music file's name
     */
    private String mCurrentFileName;

    /**
     * Current playback state
     */
    private volatile PlaybackState mCurrentPlaybackState = PlaybackState.IDLED;

    /**
     * Queue that stores commands
     */
    private final Queue<BgmCommand> mCommandQueue = new LinkedBlockingQueue<>();

    /**
     * Monitor lock for commands
     */
    private final ReentrantLock mCommandLock = new ReentrantLock();

    /**
     * Condition of mCommandLock
     */
    private final Condition mCommandCondition = mCommandLock.newCondition();

    /**
     * Flag whether this class exits or not
     */
    private volatile boolean mQuit = false;

    /**
     * Constructor
     * @param service
     */
    public BgmWorker (final BgmService service) {
        mService = service;
    }

    /**
     * Constructor
     * @param service
     * @param fileName
     */
    public BgmWorker (final BgmService service, final String fileName) {
        mService = service;
        mCurrentFileName = fileName;
        initPlayer(mCurrentFileName);
    }

    /**
     * Main run loop
     */
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        while (true) {
            /*
            wait if there is no commands
             */
            mCommandLock.lock();
            try {
                while (mCommandQueue.peek() == null) {
                    try {
                        mCommandCondition.await();
                    } catch (InterruptedException e) {
                        if (mQuit) {
                            return;
                        }
                        continue;
                    }
                }
            } finally {
                mCommandLock.unlock();
            }

            BgmCommand command = mCommandQueue.poll();
            handleCommand(command);
        }
    }

    /**
     * Handles a command
     * @param command
     */
    private void handleCommand(final BgmCommand command) {
        switch (command.getType()) {
            case START:
                startMusic((String) command.getData());
                break;

            case PAUSE:
                pauseMusic();
                break;

            case RESUME:
                resumeMusic();
                break;

            case RELEASE:
                release();
                break;

            case DESTROY:
                onDestroy();

            default:
                break;
        }
    }

    /**
     * Adds a command this class would handle
     * @param command
     * @return true if the command is successfully added
     */
    boolean addCommand(final BgmCommand command) {
        mCommandLock.lock();
        try {
            final boolean result = mCommandQueue.offer(command);
            mCommandCondition.signal();

            return result;

        } finally {
            mCommandLock.unlock();
        }
    }

    /**
     * Initializes
     * @param fileName music file that MediaPlayer would play
     */
    private void initPlayer(final String fileName) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            } else {
                mMediaPlayer.reset();
            }

            if (mListener == null) {
                mListener = new PlaybackListener();
                setPlaybackListener(mListener);
            }

            mCurrentFileName = fileName;
            AssetFileDescriptor afd = mService.getAssets().openFd(mCurrentFileName);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setLooping(true);

            mCurrentPlaybackState = PlaybackState.IDLED;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets listeners for playback events
     * @param listener
     */
    private void setPlaybackListener(PlaybackListener listener) {
        mMediaPlayer.setOnBufferingUpdateListener(listener);
        mMediaPlayer.setOnCompletionListener(listener);
        mMediaPlayer.setOnErrorListener(listener);
        mMediaPlayer.setOnInfoListener(listener);
        mMediaPlayer.setOnPreparedListener(listener);
        mMediaPlayer.setOnSeekCompleteListener(listener);

    }

    /**
     * Starts music
     * @param fileName music file's name
     */
    private void startMusic(final String fileName) {
        try {
            if (TextUtils.isEmpty(fileName) || fileName.equals(mCurrentFileName)) {
                return;
            }

            if (isPlaying()) {
                mMediaPlayer.stop();
                mCurrentPlaybackState = PlaybackState.STOPPED;
            }

            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }

            initPlayer(fileName);

            mMediaPlayer.prepareAsync();
            mCurrentPlaybackState = PlaybackState.PREPARING;

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pauses music
     */
    private void pauseMusic() {
        try {
            if (isPausable()) {
                mMediaPlayer.pause();
                mCurrentPlaybackState = PlaybackState.PAUSED;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if MediaPlayer is able to pause
     * @return true if palyer is able to pause
     */
    private boolean isPausable() {
        if (mMediaPlayer == null) {
            return false;
        }

        if (mCurrentPlaybackState != PlaybackState.STARTED &&
            mCurrentPlaybackState != PlaybackState.PAUSED &&
            mCurrentPlaybackState != PlaybackState.PLAYBACK_COMPLETED) {
            return false;
        }

        return true;
    }

    /**
     * Resumes music
     */
    private void resumeMusic() {
        try {
            if (TextUtils.isEmpty(mCurrentFileName) || !isStartable()) {
                return;
            }

            mMediaPlayer.start();
            mCurrentPlaybackState = PlaybackState.STARTED;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if player is able to start
     * @return true if player is able to start
     */
    private boolean isStartable() {
        if (mCurrentPlaybackState == PlaybackState.PREPARED ||
            mCurrentPlaybackState == PlaybackState.STARTED ||
            mCurrentPlaybackState == PlaybackState.PAUSED ||
            mCurrentPlaybackState == PlaybackState.PLAYBACK_COMPLETED) {
            return true;
        }
        return false;
    }

    /**
     * Releases MediaPlayer
     */
    private void release() {
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mCurrentPlaybackState = PlaybackState.STOPPED;
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;

                mCurrentPlaybackState = PlaybackState.IDLED;
            }
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }


    /**
     * Finishes the run loop
     */
    private void onDestroy() {
        mQuit = true;
        interrupt();
    }


    /**
     * Checks if the player is playing
     * @return true if the player is playing
     */
    boolean isPlaying() {
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if player is not null
     * @return
     */
    boolean isPlayable() {
        return mMediaPlayer != null;
    }

    /**
     * Listener class that listens for playback events
     */
    private class PlaybackListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnInfoListener {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.d(TAG, "onBufferingUpdate called");
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG, "onCompletion called");
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onError called");
            return false;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onInfo called");
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            /*
            if prepared, starts music
             */
            mCurrentPlaybackState = PlaybackState.PREPARED;
            mMediaPlayer.start();
            mCurrentPlaybackState = PlaybackState.STARTED;
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.d(TAG, "onSeekComplete called");
        }
    }
}
