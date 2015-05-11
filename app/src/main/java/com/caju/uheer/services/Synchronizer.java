package com.caju.uheer.services;

import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.BackendStatus;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.infrastructure.PlaylistItem;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

public class Synchronizer {

    private boolean isSynced;

    private Channel channel;
    private long totalPlaylistLength;

    private long localTime;
    private long remoteTime;

    private ISyncedListener syncedListener;
    public interface ISyncedListener { void onSyned(); }

    public Synchronizer(Channel channel) {
        this.channel = channel;

        totalPlaylistLength = 0;

        for (Music music : channel.Musics) {
            totalPlaylistLength += music.LengthInMilliseconds;
        }
    }

    public Synchronizer updateTimes() {
        long frame = new Date().getTime() - localTime;

        localTime += frame;
        remoteTime += frame;

        return this;
    }

    public Synchronizer sync() {
        Log.d("Synchronizer", "Synchronization procedure method has started.");

        isSynced = false;

        new CristianTask().execute();

        return this;
    }

    public PlaylistItem findCurrent() {
        updateTimes();

        PlaylistItem item = PlaylistItem.currentOf(channel);

        long timeline = remoteTime - channel.CurrentStartTime.getTime();

        // If the timeline is greater than thte playlist length and the
        // channel doesn't loop, we now the channel is stalled!
        if (timeline > totalPlaylistLength && !channel.Loops) {
            return null;
        }

        // Disregards all loops that have occurred in the playlist.
        timeline %= totalPlaylistLength;

        while (timeline > item.getMusic().LengthInMilliseconds) {
            timeline -= item.getMusic().LengthInMilliseconds;
            item.next();
        }

        item.setStartingAt(timeline);

        return item;
    }

    public Synchronizer onSyncedListener(ISyncedListener listener) {
        this.syncedListener = listener;

        return this;
    }


    private class CristianTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                localTime = System.currentTimeMillis();

                BackendStatus response = restTemplate.getForObject(Routes.STATUS + "now/", BackendStatus.class);

                long frame = System.currentTimeMillis() - localTime;

                remoteTime = response.Now.getTime();
                remoteTime += frame / 2;
                localTime += frame / 2;

                isSynced = true;

                Log.d("Synchronizer", "The synchronization window was " + frame + "ms.");

            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            if (syncedListener != null) {
                syncedListener.onSyned();
            }

            return null;
        }
    }
    
    public boolean isSynchronized() {
        return isSynced;
    }
}
