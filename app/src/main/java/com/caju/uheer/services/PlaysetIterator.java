package com.caju.uheer.services;

import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;

public class PlaysetIterator {
    private Channel channel;

    private int currentIndex;

    private int originalIndex;
    private Music originalCurrent;

    public PlaysetIterator(Channel channel) {
        setChannel(channel);
    }

    /// Retrieves the index of the music with Id=musicId. If none, returns -1.
    /// Please avoid using this method, considering it iterates through the entire list.
    public int getIndexOf(int musicId) {
        for (int i = 0; i < channel.Musics.length; i++) {
            Music music = channel.Musics[i];
            if (music.Id == musicId) {
                return i;
            }
        }

        return -1;
    }

    /// Retrieves the next music that follows the one with Id=musicId.
    /// Please avoid using this method, considering it iterates through the entire list.
    public Music getNextOf(int musicId) {
        int i = getIndexOf(musicId);
        if (i == channel.Musics.length -1 && !channel.Loops) {
            return null;
        }

        return channel.Musics[(i +1) % channel.Musics.length];
    }

    /// Define which music is the current based on Channel.CurrentId.
    public PlaysetIterator restoreCurrentToOriginals() {
        currentIndex = originalIndex;

        return this;
    }

    /// Retrieve current music.
    public Music getCurrent() {
        return channel.Musics[currentIndex];
    }

    /// Set the next music as the current of the playset.
    /// Consider the next music as the one that follows the current in the channel.Musics list,
    /// or the first one, case the current is also the last in the list.
    public PlaysetIterator next() {
        if (this.currentIndex > -1) {
            if (this.currentIndex == channel.Musics.length - 1 && !channel.Loops) {
                Log.d("PlaysetIterator", "Iterator has reached end of list.");
                currentIndex = -1;
            } else {
                currentIndex = (currentIndex + 1) % channel.Musics.length;
            }
        }

        return this;
    }

    /// Set current music to null, forcing the channel to stop.
    public PlaysetIterator kill() {
        currentIndex = -1;

        return this;
    }

    public PlaysetIterator setChannel(Channel channel) {
        this.channel = channel;

        if (channel.CurrentId > 0) {
            for (int i = 0; i < channel.Musics.length; i++) {
                if (channel.Musics[i].Id == channel.CurrentId) {
                    originalIndex = i;
                    originalCurrent = this.channel.Musics[i];
                    break;
                }
            }
        }

        return restoreCurrentToOriginals();
    }
}
