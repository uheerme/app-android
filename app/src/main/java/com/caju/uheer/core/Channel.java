package com.caju.uheer.core;

import com.caju.uheer.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.services.exceptions.NoneNextMusicException;

import java.io.Serializable;
import java.util.Date;

public class Channel
{

    private int currentIndex;

    public int Id;
    public String Name;

    public String Owner;
    public String HostIpAddress;
    public String HostMacAddress;

    public boolean Loops;

    public int CurrentId;
    public Music current;
    public Date CurrentStartTime;

    public Music[] Musics;
    public int LengthInMilliseconds;

    public Date DateCreated;
    public Date DateUpdated;
    public Date DateDeactivated;

    public Channel analyzePlaylist() {
        if (CurrentId == 0) {
            currentIndex = -1;
            current = null;

            return this;
        }

        LengthInMilliseconds = 0;

        for (int i = 0; i < Musics.length; i++) {
            Music music = Musics[i];

            LengthInMilliseconds += music.LengthInMilliseconds;

            if (music.Id == CurrentId) {
                currentIndex = i;
                current = music;
            }
        }

        return this;
    }

    public Music peak(int count) {
        return Musics[(currentIndex + count) % Musics.length];
    }

    public Music next() {
        if (currentIndex == -1) {
            throw new NoneNextMusicException();
        }

        int nextIndex = currentIndex + 1;

        if (nextIndex + 1 == Musics.length && !Loops) {
            currentIndex = -1;
            CurrentId = -1;
            current = null;
            throw new EndOfPlaylistException();
        }

        CurrentStartTime.setTime(CurrentStartTime.getTime() + current.LengthInMilliseconds);

        currentIndex = nextIndex % Musics.length;
        current = Musics[currentIndex];
        CurrentId = current.Id;

        return current;
    }

    @Override
    public String toString() {
        return "#" + Id + " " + this.Name;
    }
}
