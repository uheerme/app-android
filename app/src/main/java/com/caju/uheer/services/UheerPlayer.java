package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.services.infrastructure.PlaylistItem;

import java.io.IOException;
import java.util.Date;

public class UheerPlayer {
    private Context context;
    private MediaPlayer player;

    private Synchronizer synchronizer;

    private PlaylistItem currentOnPlay;

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
                    PlaylistItem item = synchronizer.findCurrent();
                    long prepareStart = new Date().getTime();
                    play(item, prepareStart);
                } catch (EndOfPlaylistException e) {
                    Log.d("UheerPlayer", "We've reached the end of the playlist!");
                }
            }
        });

        synchronizer.sync();

        return this;
    }

    protected UheerPlayer play(PlaylistItem item, final long prepareStart) {
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

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // It took us a while to prepare (prepareFrame).
                    // Let's also consider this before playing.
                    long prepareFrame = new Date().getTime() - prepareStart;
                    long startingAt = currentOnPlay.getStartingAt() + prepareFrame;

                    if(startingAt < 0) {
                        try {
                            Log.d("UheerPlayer","Player is waiting "+(-startingAt)+" ms to start song.");
                            player.wait((int)-startingAt);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        player.seekTo((int) startingAt);
                    }
                    player.start();

                    Log.d("UheerPlayer", currentOnPlay.getMusic().Name + " will start to play at " +startingAt +"!");
                    Log.d("UheerPlayer", "The preparation frame was " + prepareFrame);
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("UheerPlayer", currentOnPlay.getMusic().Name + " completed!");

                    try {
                        PlaylistItem item = synchronizer.nextItem(currentOnPlay);
                        long prepareStart = new Date().getTime();
                        play(item, prepareStart);
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
