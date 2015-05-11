package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.services.exceptions.UheerPlayerException;
import com.caju.uheer.services.infrastructure.PlaylistItem;

import java.io.IOException;
import java.util.Date;

public class UheerPlayer {
    private Context context;
    private MediaPlayer player;

    private Synchronizer synchronizer;

    private PlaylistItem currentOnPlay;
    private long prepareFrame;

    public UheerPlayer(Context context, Channel channel) {
        this.context = context;

        this.synchronizer = new Synchronizer(channel);
        this.player = new MediaPlayer();
    }

    public UheerPlayer start() {
        synchronizer.onSyncedListener(new Synchronizer.ISyncedListener() {
            @Override
            public void onSyned() {
                try {
                    play(synchronizer.findCurrent());
                } catch (EndOfPlaylistException e) {
                    Log.d("UheerPlayer", "We've reached the end of the playlist!");
                }
            }
        });

        synchronizer.sync();

        return this;
    }

    protected UheerPlayer play(PlaylistItem item) {
        if (item.getMusic() == null) {
            Log.e("UheerPlayer", "Play attempt on a channel which is stalled.");
            return this;
        }

        Uri streamUrl = Uri.parse(Routes.MUSICS + item.getMusic().Id + "/stream");

        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();

        try {
            Log.d("UheerPlayer", "Preparing to buffer " + streamUrl.toString());
            player.setDataSource(context, streamUrl);
            player.prepareAsync();

            currentOnPlay = item;
            prepareFrame = new Date().getTime();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    prepareFrame = new Date().getTime() - prepareFrame;

                    Log.d("UheerPlayer", "The preparation frame was " + prepareFrame);

                    // It took us a while to prepare (prepareFrame).
                    // Let's also consider this before playing.
                    long startingAt = currentOnPlay.getStartingAt() + prepareFrame;

                    Log.d("UheerPlayer", currentOnPlay.getMusic().Name + " will start to play at " +startingAt +"!");

                    player.seekTo((int) startingAt);
                    player.start();
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("UheerPlayer", currentOnPlay.getMusic().Name + " completed!");

                    try {
                        play(synchronizer.findCurrent());
                    } catch (EndOfPlaylistException e) {
                        Log.d("UheerPlayer", "We've reached the end of the playlist!");
                    }
                }
            });
        } catch (IOException e) {
            Log.e("UheerPlayer", e.toString());
        } catch (Exception e) {
            Log.e("UheerPlayer", e.toString());
        }

        return this;
    }
}
