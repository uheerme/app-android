package com.caju.uheer.app.services.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.app.core.Channel;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.app.services.exceptions.EndOfPlaylistException;
import com.caju.uheer.app.services.exceptions.NoneNextMusicException;
import com.caju.uheer.app.services.infrastructure.PlayItem;
import com.caju.uheer.app.services.infrastructure.StreamItem;
import com.caju.uheer.app.services.infrastructure.SyncItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UheerPlayer {
    private static Context context = null;
    private static Channel channel = null;
    private static String email = null;
    private static AsyncTask task = null;

    private static Streamer streamer;
    private static Synchronizer synchronizer;
    private static MediaPlayer player;
    private static PlayItem currentOnPlay;
    private static AsyncPlayerResync resyncService;

    public static boolean initPlayer(Context context,Channel channel, String email) {

        if(UheerPlayer.context != null || UheerPlayer.channel != null)
            return false;

        UheerPlayer.context = context;
        UheerPlayer.channel = channel;
        UheerPlayer.email = email;

        if(UheerPlayer.player != null){
            UheerPlayer.player.stop();
            UheerPlayer.player.release();
        }

        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentOnPlay != null && currentOnPlay.sync != null)
                    Log.d("UheerPlayer", currentOnPlay.sync.music + " completed!");

                if (resyncService != null)
                    resyncService.cancel(true);
                currentOnPlay = null;

                softPlay();
            }
        });

        synchronizer = new Synchronizer(channel)
                .setListener(new Synchronizer.ISyncListener() {
                    @Override
                    public void onFinished() {
                        softPlay();
                    }
                });

        streamer = new Streamer(context)
                .setListener(new Streamer.IStreamingListener() {
                    @Override
                    public void onFinished() {
                        softPlay();
                    }
                });

        resyncService = null;
        start();

        task = new ConnectedToChannelTask().execute();

        return true;
    }

    public static boolean isInitiated(){
        if(context != null && channel != null)
            return true;
        else
            return false;
    }

    private static void start() {
        synchronizer.sync();
    }

    public static boolean isPlaying(){
        if(player != null)
            return player.isPlaying();
        else
            return false;
    }

    public static void stop() {
        if (resyncService != null)
            resyncService.cancel(true);
        player.stop();
        //player.reset();
        currentOnPlay = null;
    }

    public static void changeChannel(Channel channel){
        stop();

        UheerPlayer.channel = channel;
        synchronizer = new Synchronizer(channel)
                .setListener(new Synchronizer.ISyncListener() {
                    @Override
                    public void onFinished() {
                        softPlay();
                    }
                });

        start();

        task = new ConnectedToChannelTask().execute();

    }

    public static int currentChannelId(){
        if(channel != null)
            return channel.Id;
        else
            return -1;
    }

    protected static void softPlay() {
        try {
            SyncItem sync = synchronizer.findCurrent();
            StreamItem stream = streamer.stream(sync.music);

            // Does nothing when the player is already playing the expected song or when the download hasn't finished yet.
            if (currentOnPlay != null && sync.music == currentOnPlay.sync.music || !stream.finished) return;

            playNow(new PlayItem(stream, sync));

            streamer.stream(channel.peak(1));

        } catch (EndOfPlaylistException e) {
            Log.d("UheerPlayer", "We've reached the end of the playlist!");
        } catch (NoneNextMusicException e) {
            Log.e("UheerPlayer", e.toString(), e);
        }
    }

    protected static void playNow(PlayItem item) {
        if (item.sync.music == null) {
            Log.e("UheerPlayer", "Play attempt on a channel which is stalled.");
        }

        if (player.isPlaying()) {
            player.stop();
        }

        if (resyncService != null)
            resyncService.cancel(true);

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

            resyncService = new AsyncPlayerResync();
            resyncService.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Log.d("UheerPlayer", currentOnPlay.sync.music + " will start to play at " + startingAt + "ms!");
        } catch (Exception e) {
            Log.e("UheerPlayer", e.toString());
        }
    }

    private static class AsyncPlayerResync extends AsyncTask {
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
                    player.seekTo((int)(expectedPosition + .5 * (expectedPosition - actualPosition)));
                }
            } catch (InterruptedException e) {
            }
            return null;
        }
    }

    static class ConnectedToChannelTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params)
        {
            URL url = null;
            HttpURLConnection urlConnection = null;
            try{
                String s = "http://debugmaster.koding.io:9000/" + channel.Id + "/" + email;
                System.out.println(s);
                url = new URL(s);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.getResponseMessage();
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return null;
        }
    }
}
