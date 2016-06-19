package com.badlogic.masaki.bgmservice.library;

/**
 * Created by shojimasaki on 2016/05/21.
 */
public interface Music {
    void start(String fileName);
    void pause();
    void resume();
    void stop();
    void release();
    boolean isPlaying();
}

