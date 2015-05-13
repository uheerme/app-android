package com.caju.uheer.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.services.exceptions.NoneNextMusicException;
import com.caju.uheer.services.infrastructure.PlayItem;
import com.caju.uheer.services.infrastructure.StreamItem;
import com.caju.uheer.services.infrastructure.SyncItem;

import java.io.FileInputStream;
import java.util.Date;

public class UheerPlayer {
    private final Context context;
    private final Streamer streamer;
    private final Synchronizer synchronizer;
    private final MediaPlayer player;
    private final Channel channel;

    private PlayItem currentOnPlay;

    public UheerPlayer(Context context, Channel channel) {
        this.context = context;
        this.channel = channel;

        this.player = new MediaPlayer();

        this.synchronizer = new Synchronizer(channel)
                .setListener(new Synchronizer.ISyncListener() {
                    @Override
                    public void onFinished() {
                        streamFollowing(1);
                    }
                });

        this.streamer = new Streamer(context)
                .setListener(new Streamer.IStreamingListener() {
                    @Override
                    public void onFinished(StreamItem item) {
                        try {

                            SyncItem currentOnRemote = synchronizer.findCurrent();
                            StreamItem currentStream = streamer.stream(currentOnRemote.music);

                            if (currentStream != null && currentStream.streamTask.getStatus()
                                    == AsyncTask.Status.FINISHED) {
                                play(new PlayItem(item, currentOnRemote));
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

        return this;
    }

    protected UheerPlayer play(PlayItem item) {

        if (item.sync.music == null) {
            Log.e("UheerPlayer", "Play attempt on a channel which is stalled.");
            return this;
        }

        final long prepareStart = new Date().getTime();

        Uri streamUrl = Uri.parse(Routes.MUSICS + item.sync.music.Id + "/stream");

        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();

        try {
            Log.d("UheerPlayer", "Preparing to buffer " + streamUrl.toString());

            player.setDataSource(new FileInputStream(item.stream.file).getFD());
            player.prepare();

            currentOnPlay = item;

            // It took us a while to prepare (prepareFrame).
            // Let's also consider this before playing.
            long prepareFrame = new Date().getTime() - prepareStart;
            long startingAt = currentOnPlay.sync.startingAt + prepareFrame;

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
            Log.d("preparation frame", "" + prepareFrame);

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("UheerPlayer", currentOnPlay.sync.music + " completed!");

                    streamFollowing(1);
                }
            });

        } catch (Exception e) {
            Log.e("UheerPlayer", e.toString());
        }

        return this;
    }

    protected UheerPlayer streamFollowing(int count) {
        synchronizer.findCurrent();

        // stream the next three songs,
        // inclusive with the current.
        for (int i = 0; i < count; i++) {
            streamer.stream(channel.peak(i));
        }

        return this;
    }
}
