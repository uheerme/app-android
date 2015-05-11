package com.caju.uheer.services.infrastructure;

import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;

public class PlaylistItem {
    private Channel channel;
    private Music music;
    private int indexAtSongsList;
    private long startingAt;

    public PlaylistItem(Channel channel, Music music, int indexAtSongsList) {
        this(channel, music, indexAtSongsList, 0);
    }

    public PlaylistItem(Channel channel, Music music, int indexAtSongsList, long startingAt) {
        this.channel = channel;
        this.music = music;
        this.indexAtSongsList = indexAtSongsList;
        this.startingAt = startingAt;
    }

    public PlaylistItem next() {
        if (indexAtSongsList + 1 == channel.Musics.length && !channel.Loops) {
            music = null;
            indexAtSongsList = -1;
            startingAt = -1;

            throw new EndOfPlaylistException();
        }

        indexAtSongsList = (indexAtSongsList + 1) % channel.Musics.length;
        music = channel.Musics[indexAtSongsList];
        startingAt = 0;

        return this;
    }

    public Music getMusic() {
        return music;
    }

    public long getStartingAt() {
        return startingAt;
    }

    public void setStartingAt(long startingAt) {
        this.startingAt = startingAt;
    }

    public static PlaylistItem currentOf(Channel channel) {
        for (int i = 0; i < channel.Musics.length; i++) {
            Music music = channel.Musics[i];

            if (music.Id == channel.CurrentId) {
                return new PlaylistItem(channel, music, i, 0);
            }
        }

        return null;
    }
}
