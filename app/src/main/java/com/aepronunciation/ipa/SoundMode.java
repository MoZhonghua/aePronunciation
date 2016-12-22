package com.aepronunciation.ipa;

/**
 * Created by yonghu on 16-11-17.
 */

public enum SoundMode {

    // Do not change the hardcoded string values unless you take into consideration
    // that they are used in persistent memory.
    Single("single"),
    Double("double");

    private String persistentMemoryString;

    private SoundMode(String s) {
        persistentMemoryString = s;
    }

    public String getPersistentMemoryString() {
        return persistentMemoryString;
    }

    public static SoundMode fromString(String text) {
        if (text != null) {
            for (SoundMode mode : SoundMode.values()) {
                if (text.equalsIgnoreCase(mode.persistentMemoryString)) {
                    return mode;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
