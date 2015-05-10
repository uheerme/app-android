package com.caju.uheer.services;

import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.Channel;
import com.caju.uheer.core.CurrentTimeViewModel;
import com.caju.uheer.core.Music;
import com.caju.uheer.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Synchronizer {
    private OnSynchronizedListener listener;
    private PlaysetIterator playsetIterator;
    private Channel channel;
    private int channelsLength;

    boolean _synchronized;
    long localTime, remoteTime;

    public interface OnSynchronizedListener {
        public void onSynchronized(long startAt);
    }

    public Synchronizer(Channel channel, PlaysetIterator playsetIterator) {
        this.playsetIterator = playsetIterator;
        this.channel = channel;
    }

    public Synchronizer updateSynchronizedTime() {
        long now = System.currentTimeMillis();

        long timeFrame = now - localTime;

        localTime += timeFrame;
        remoteTime += timeFrame;

        return this;
    }

    public Synchronizer setOnSynchronizedListener(OnSynchronizedListener listener) {
        this.listener = listener;
        return this;
    }

    public Synchronizer start() {
        Log.d("Synchronizer", "Synchronization procedure method has started.");

        _synchronized = false;

        // Let's find the channel's length. This information will help us optimizing the
        // sync operation performance when the channel has been on for many hours.
        channelsLength = 0;
        for (Music music : channel.Musics) {
            channelsLength += music.LengthInMilliseconds;
        }

        new CristiansTask().execute();

        return this;
    }

    /// Translates all the time-frame gotten from the server and moves
    /// the stack to the music that is currently being played.
    public Synchronizer translatePlayset() {
        this.updateSynchronizedTime();

        long startTime = channel.CurrentStartTime.getTime();
        long timeline = this.remoteTime - startTime;

        if (timeline > channelsLength && !channel.Loops) {
            playsetIterator.kill();
            return this;
        }

        // The channel may have looped already and the cycle would put us in the exact same spot.
        // We don't need to iterate throughout the entire list to check this. Instead, let's just consider the last one.
        if (channelsLength > 0) {
            timeline %= channelsLength;
        }

        Music current = playsetIterator.getCurrent();

        while (timeline > current.LengthInMilliseconds) {
            timeline -= current.LengthInMilliseconds;

            current = playsetIterator.next().getCurrent();
            if (current == null) {
                return this;
            }
        }

        // If there is a callback, invoke it passing the timeline in seconds, which represents the current position of the song's playment.
        if (listener != null) {
            listener.onSynchronized(timeline);
        }

        return this;
    }


    private class CristiansTask extends AsyncTask<Void, Void, CurrentTimeViewModel> {
        @Override
        protected CurrentTimeViewModel doInBackground(Void... params) {
            CurrentTimeViewModel currentTimeViewModel = null;
            localTime = System.currentTimeMillis();

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                currentTimeViewModel = restTemplate.getForObject(Routes.STATUS + "now/", CurrentTimeViewModel.class);
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            long timeFrame = System.currentTimeMillis() - localTime;

            remoteTime = currentTimeViewModel.Now.getTime();
            remoteTime += timeFrame / 2;
            localTime += timeFrame / 2;

            Log.d("Synchronizer", "The synchronization window was " + timeFrame + "ms.");
            return currentTimeViewModel;
        }

        @Override
        protected void onPostExecute(CurrentTimeViewModel currentTimeViewModel) {
            translatePlayset();
        }
    }
}
