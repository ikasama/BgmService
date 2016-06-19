package com.badlogic.masaki.bgmservice.library;

/**
 * Settings class to store music file's name
 * Created by shojimasaki on 2016/06/18.
 */
public final class BgmSettings {

    /**
     * music file's name
     */
    private static String sFileName;

    private BgmSettings() {}

    /**
     * Sets file's name
     * @param fileName file name
     */
    public static final void setFileName(String fileName) {
        sFileName = fileName;
    }

    /**
     * Getter
     * @return music file's name
     */
    public static final String getFileName() {
        return sFileName;
    }
}
