package com.caju.uheer.services.infrastructure;

import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;

public class PlaylistItem {
    public Music music;
    public long startingAt;

    public PlaylistItem(Music music) {
        this(music, 0);
    }

    public PlaylistItem(Music music, long startingAt) {
        this.music = music;
        this.startingAt = startingAt;
    }
}
