package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.services.exceptions.NoneNextMusicException;
import com.caju.uheer.services.infrastructure.PlayItem;
import com.caju.uheer.services.infrastructure.StreamItem;
import com.caju.uheer.services.infrastructure.SyncItem;

import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

public class UheerPlayer {
    private final Context context;
    private final Streamer streamer;
    private final Synchronizer synchronizer;
    private final MediaPlayer player;
    private final Channel channel;

    private PlayItem currentOnPlay;

    private static final int rePlayTime = 20; //Seconds

    public UheerPlayer(Context context, final Channel channel) {
        this.context = context;
        this.channel = channel;

        this.player = new MediaPlayer();

        this.synchronizer = new Synchronizer(channel)
                .setListener(new Synchronizer.ISyncListener() {
                    @Override
                    public void onFinished() {
                        SyncItem actualSong = synchronizer.findCurrent();
                        streamer.stream(actualSong.music);
                        Log.d("Synchronizer finished", "started stream "+actualSong.toString());
                    }
                });

        this.streamer = new Streamer(context)
                .setListener(new Streamer.IStreamingListener() {
                    @Override
                    public void onFinished(StreamItem streamedItem) {
                        try {
                            SyncItem currentOnRemote = synchronizer.findCurrent();
                            if(streamedItem.music == currentOnRemote.music){
                                Log.d("UheerPlayer", "play");
                                play(new PlayItem(streamedItem, currentOnRemote));
                                streamer.stream(channel.peak(1));
                            }
                        } catch (EndOfPlaylistException e) {
                            Log.d("UheerPlayer", "We've reached the end of the playlist!");
                        } catch (NoneNextMusicException e) {
                            Log.e("UheerPlayer", e.toString(), e);
                        }
                    }
                });
    }

    public UheerPlayer start() {
        synchronizer.sync();
        playAgainEvery(rePlayTime);
        Log.d("UheerPlayer start", "playAgainEvery "+rePlayTime);

        return this;
    }

    protected UheerPlayer play(PlayItem item) {
        if (item.sync.music == null) {
            Log.e("UheerPlayer", "Play attempt on a channel which is stalled.");
            return this;
        }

        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();

        try {
            player.setDataSource(new FileInputStream(item.stream.file).getFD());
            player.prepare();

            currentOnPlay = item;
            GlobalVariables.playingSong = currentOnPlay.sync.music;

            // It took us a while to prepare.
            // Let's also consider this before playing.
            currentOnPlay.sync = synchronizer.findCurrent();

            long startingAt = currentOnPlay.sync.startingAt;

            try {
                if (startingAt < 0) {
                    player.wait((int) -startingAt);
                } else {
                    player.seekTo((int) startingAt);
                }
            } catch (InterruptedException e) {
                Log.e("onPreparedListener", e.toString(), e);
            }

            player.start();

            Log.d("UheerPlayer", currentOnPlay.sync.music + " will start to play at " + startingAt + "ms!");

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("UheerPlayer", currentOnPlay.sync.music + " completed!");

                    channel.next();

                    streamer.stream(channel.peak(0));
                }
            });

        } catch (Exception e) {
            Log.e("UheerPlayer", e.toString());
        }

        return this;
    }

    private void playAgainEvery(int seconds){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                streamer.stream(channel.peak(0));
            }
        }, seconds*1000, seconds*1000);
    }

}
