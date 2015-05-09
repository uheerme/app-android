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

public class UheerPlayer {
    private Context context;
    private MediaPlayer player;

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
            synchronizer = new Synchronizer(playsetIterator);
        }

        playsetIterator.restoreCurrentToOriginals();

        Music current = playsetIterator.getCurrent();
        if (current == null) {
            Log.e("UheerPlayer", channel.Name + " is currently stalled.");
        }

        /// TODO: receive music from Synchronizer.
        Music music = channel.Musics[0];

        return play(music);
    }

    protected UheerPlayer play(Music music) {
        return play(music, 0);
    }

    protected UheerPlayer play(Music music, int startingAt) {
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

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
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
