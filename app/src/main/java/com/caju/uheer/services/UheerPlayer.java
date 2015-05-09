package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class UheerPlayer {

    private Context context;
    private MediaPlayer player;

    public UheerPlayer(Context context) {
        this(context, new MediaPlayer());
    }

    public UheerPlayer(Context context, MediaPlayer player) {
        this.context = context;
        this.player = player;
    }

    public UheerPlayer play(Uri musicUrl) {
        return play(musicUrl, 0);
    }

    public UheerPlayer play(Uri musicUrl, int startingAt) {
        if (player.isPlaying()) {
            player.stop();
        }

        try {
            player.setDataSource(context, musicUrl);
            player.prepareAsync();
            player.seekTo(startingAt);
            player.start();
        } catch (IOException e) {
            Log.e("UheerPlayer", e.getMessage());
        } catch (Exception e) {
            Log.e("UheerPlayer", e.getMessage());
        }

        return this;
    }
}
