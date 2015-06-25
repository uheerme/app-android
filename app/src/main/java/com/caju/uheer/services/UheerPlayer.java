package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.services.exceptions.NoneNextMusicException;
import com.caju.uheer.services.infrastructure.PlayItem;
import com.caju.uheer.services.infrastructure.StreamItem;
import com.caju.uheer.services.infrastructure.SyncItem;

import java.io.FileInputStream;

public class UheerPlayer {
    private final Context context;
    private final Channel channel;

    private final Streamer streamer;
    private final Synchronizer synchronizer;
    private final MediaPlayer player;
    private PlayItem currentOnPlay;
    private AsyncPlayerResync resyncService;

    public UheerPlayer(Context context, final Channel channel) {
        this.context = context;
        this.channel = channel;

        this.player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentOnPlay != null && currentOnPlay.sync != null)
                    Log.d("UheerPlayer", currentOnPlay.sync.music + " completed!");

                if (resyncService != null) resyncService.cancel(true);
                currentOnPlay = null;

                softPlay();
            }
        });

        this.synchronizer = new Synchronizer(channel)
                .setListener(new Synchronizer.ISyncListener() {
                    @Override
                    public void onFinished() {
                        softPlay();
                    }
                });

        this.streamer = new Streamer(context)
                .setListener(new Streamer.IStreamingListener() {
                    @Override
                    public void onFinished() {
                        softPlay();
                    }
                });

        resyncService = null;
    }

    public UheerPlayer dispose() {
        if (resyncService != null) resyncService.cancel(true);
        player.stop();
        currentOnPlay = null;

        return this;
    }

    public UheerPlayer start() {
        synchronizer.sync();

        return this;
    }

    protected void softPlay() {
        try {
            SyncItem sync = synchronizer.findCurrent();
            StreamItem stream = streamer.stream(sync.music);

            // Does nothing when the player is already playing the expected song or when the download hasn't finished yet.
            if (currentOnPlay != null && sync.music == currentOnPlay.sync.music || !stream.finished) return;

            play(new PlayItem(stream, sync));

            streamer.stream(channel.peak(1));

        } catch (EndOfPlaylistException e) {
            Log.d("UheerPlayer", "We've reached the end of the playlist!");
        } catch (NoneNextMusicException e) {
            Log.e("UheerPlayer", e.toString(), e);
        }
    }

    protected UheerPlayer play(PlayItem item) {
        if (item.sync.music == null) {
            Log.e("UheerPlayer", "Play attempt on a channel which is stalled.");
            return this;
        }

        if (player.isPlaying()) {
            player.stop();
        }

        if (resyncService != null) resyncService.cancel(true);
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

            Log.d("UheerPlayer", "Hello!");

            resyncService = new AsyncPlayerResync();
            resyncService.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Log.d("UheerPlayer", currentOnPlay.sync.music + " will start to play at " + startingAt + "ms!");
        } catch (Exception e) {
            Log.e("UheerPlayer", e.toString());
        }

        return this;
    }

    private class AsyncPlayerResync extends AsyncTask {
        private static final int executionPeriodInSeconds = 10;
        private static final long maxDelayAllowedInMilliseconds = 200;
        private static final double deltaErrorInfluence = .5;

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                while (true) {
                    Thread.sleep(executionPeriodInSeconds * 1000);

                    // Does nothing when player isn't playing.
                    if (!player.isPlaying()) continue;

                    long expectedPosition = synchronizer.findCurrent().startingAt;
                    long actualPosition = player.getCurrentPosition();
                    long difference = expectedPosition - actualPosition;

                    Log.d("PlayerResync", "Resynchronization service will execute!");
                    Log.d("PlayerResync", expectedPosition + " was expected, "
                            + actualPosition + " is the actual. Difference is " + difference + ".");

                    // Does nothing when delay is bellow the maximum allowed or if our adjustment would overflow the song's duration.
                    if (Math.abs(difference) < maxDelayAllowedInMilliseconds ||
                            difference > player.getDuration() - player.getCurrentPosition()) continue;

                    Log.d("PlayerResync", "resynchronizing...");

                    // Only inject an delay if the player is late compared to the expected position,
                    // as the player is the only object that delays the execution.
                    double delayInjected = (difference > 0) ? .5 * (expectedPosition - actualPosition) : 0;

                    player.seekTo((int) (expectedPosition + delayInjected));
                }
            } catch (InterruptedException e) {
            }
            return null;
        }
    }
}
