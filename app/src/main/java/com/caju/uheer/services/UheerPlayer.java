package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.exceptions.UheerPlayerException;

import java.io.IOException;
import java.util.Date;

public class UheerPlayer {
    private Context context;
    private MediaPlayer player;
    private long startTime;

    private Channel channel;
    private PlaysetIterator playsetIterator;
    private Synchronizer synchronizer;

    public UheerPlayer(Context context) {
        this(context, null);
    }

    public UheerPlayer(Context context, Channel channel) {
        this(context, channel, new MediaPlayer());
    }

    public UheerPlayer(Context context, Channel channel, MediaPlayer player) {
        this.context = context;
        this.channel = channel;
        this.player = player;
    }

    public UheerPlayer stop() {
        if (player.isPlaying()) {
            player.stop();
        }

        return this;
    }

    public UheerPlayer start() {
        if (channel == null) {
            throw new UheerPlayerException("Cannot start an empty channel.");
        }

        if (playsetIterator == null) {
            playsetIterator = new PlaysetIterator(channel);
        }

        if (synchronizer == null) {
            synchronizer = new Synchronizer(channel, playsetIterator);
        }

        Music current = playsetIterator
                .restoreCurrentToOriginals()
                .getCurrent();

        if (current == null) {
            Log.e("UheerPlayer", channel.Name + " is currently stalled.");
        }

        Log.d("Synchronizer", "Let's start playing " + channel.Name + "!");

        synchronizer.setOnSynchronizedListener(new Synchronizer.OnSynchronizedListener() {
            @Override
            public void onSynchronized(long startAt) {
                play((int)startAt);
            }
        });

        synchronizer.start();

        return this;
    }

    protected UheerPlayer play(final int startingAt) {
        Music music = playsetIterator.getCurrent();
        if (music == null) {
            Log.e("UheerPlayer", "Play attempt on a channel which is stalled.");
            return this;
        }

        Uri streamUrl = Uri.parse(Routes.MUSICS + music.Id + "/stream");

        if (player.isPlaying()) {
            player.stop();
        }

        Log.d("UheerPlayer", "Starting to play " + streamUrl.toString());

        try {
            player.setDataSource(context, streamUrl);
            player.prepareAsync();

            if (startingAt > 0) {
                player.seekTo(startingAt);
            }

            startTime = new Date().getTime();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                    int timeFrame = (int)(new Date().getTime() - startTime);
//
//                    player.seekTo(player.getCurrentPosition() + timeFrame);
                    player.start();
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Music music = playsetIterator.getCurrent();
                    Log.d("UheerPlayer", music.Name + " completed!");

                    playsetIterator.restoreCurrentToOriginals();
                    synchronizer.translatePlayset();
                }
            });

            Log.d("UheerPlayer", "The music has started!");
        } catch (IOException e) {
            Log.e("UheerPlayer", e.getMessage());
        } catch (Exception e) {
            Log.e("UheerPlayer", e.getMessage());
        }

        return this;
    }

    public UheerPlayer take(Channel channel) {
        this.channel = channel;
        return this;
    }

    public UheerPlayer take(PlaysetIterator playsetIterator) {
        this.playsetIterator = playsetIterator;
        return this;
    }

    public UheerPlayer take(Synchronizer synchronizer) {
        this.synchronizer = synchronizer;
        return this;
    }
}
