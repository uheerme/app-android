package com.caju.uheer.app.services.infrastructure;

import com.caju.uheer.app.core.Music;

public class SyncItem {
    public Music music;
    public long startingAt;

    public SyncItem(Music music, long startingAt) {
        this.music = music;
        this.startingAt = startingAt;
    }

    @Override
    public boolean equals(Object o) {
        try {
            return super.equals(o) || music == ((SyncItem)o).music;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
