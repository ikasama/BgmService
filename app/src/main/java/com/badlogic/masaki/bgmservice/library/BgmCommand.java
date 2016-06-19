package com.badlogic.masaki.bgmservice.library;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Command that is handled by BgmWorker
 * Created by shojimasaki on 2016/05/29.
 */
public class BgmCommand  {

    /**
     * Command type
     */
    private final Type mType;

    /**
     * Data used by BgmWorker when this command is executed
     */
    private Object mData;

    /**
     * Enum representing command type
     */
    enum Type {
        START,
        PAUSE,
        STOP,
        RESUME,
        RELEASE,
        DESTROY,
    }


    /**
     * Constructor
     * @param data
     * @param type
     */
    BgmCommand(@Nullable Object data, @NonNull Type type) {
        mData = data;
        mType = type;
    }

    /**
     * Constructor
     * @param type
     */
    BgmCommand(@NonNull Type type) {
        this(null, type);
    }

    /**
     *
     * @return mData
     */
    Object getData() {
        return mData;
    }

    /**
     * Sets data used by BgmWorker
     * @param data
     */
    void setData(final Object data) {
        mData = data;
    }

    /**
     * @return mType
     */
    Type getType() {
        return mType;
    }
}
