package com.caju.uheer.services;

import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.BackendStatus;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.infrastructure.PlaylistItem;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Synchronizer {

    final static int CHRISTIAN_ITERATION_NUMBER = 10;
    final static int RTT_NUMBER_TO_CALC_AVARAGE = 3;

    private boolean isSynced;

    private Channel channel;
    private long totalPlaylistLength;

    private long remoteAndLocalTimeDifference;

    private ISyncedListener syncedListener;
    public interface ISyncedListener { void onSyned(); }

    public Synchronizer(Channel channel) {
        this.channel = channel;

        totalPlaylistLength = 0;

        for (Music music : channel.Musics) {
            totalPlaylistLength += music.LengthInMilliseconds;
        }
    }

    public Synchronizer sync() {
        Log.d("Synchronizer", "Synchronization procedure method has started.");

        isSynced = false;

        new CristianTask().execute();

        return this;
    }

    public PlaylistItem findCurrent() {

        PlaylistItem item = PlaylistItem.currentOf(channel);

        long remoteTime = remoteAndLocalTimeDifference + System.currentTimeMillis();
        long timeline = remoteTime - channel.CurrentStartTime.getTime();

        // If the timeline is greater than the playlist length and the
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

        GlobalVariables.playingSong = item.getMusic();

        //Recalculating timeline to get better precision.
        timeline += System.currentTimeMillis() - remoteTime + remoteAndLocalTimeDifference;

        item.setStartingAt(timeline);

        return item;
    }

    public PlaylistItem nextItem(PlaylistItem item){
        PlaylistItem currentOfChannel = PlaylistItem.currentOf(channel);
        item.next();

        long remoteTime = remoteAndLocalTimeDifference + System.currentTimeMillis();
        long timeline = remoteTime - channel.CurrentStartTime.getTime();

        // If the timeline is greater than the playlist length and the
        // channel doesn't loop, we now the channel is stalled!
        if (timeline > totalPlaylistLength && !channel.Loops) {
            return null;
        }

        // Disregards all loops that have occurred in the playlist.
        timeline %= totalPlaylistLength;

        while (currentOfChannel.getMusic().Id != item.getMusic().Id) {
            timeline -= currentOfChannel.getMusic().LengthInMilliseconds;
            currentOfChannel.next();
        }

        GlobalVariables.playingSong = item.getMusic();

        //Recalculating timeline to get better precision.
        timeline += System.currentTimeMillis() - remoteTime + remoteAndLocalTimeDifference;

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

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ", Locale.US);

                HashMap<Long, Long> rttAndRemote = new HashMap<Long, Long>();
                for(int i=0; i<CHRISTIAN_ITERATION_NUMBER; i++) {
                    long localTime = System.currentTimeMillis();

                    BackendStatus rawResponse = restTemplate.getForObject(Routes.STATUS + "now/", BackendStatus.class);

                    Date responseNow = formatter.parse(rawResponse.Now.toString().replaceAll("(\\.[0-9]{3})[0-9]*(Z$)","$1+0000"));

                    long roundTimeTrip = System.currentTimeMillis() - localTime;

                    Log.d("Synchronizer", "The Round Time Trip was " + roundTimeTrip + "ms.");

                    remoteAndLocalTimeDifference = responseNow.getTime();
                    remoteAndLocalTimeDifference += roundTimeTrip / 2;
                    remoteAndLocalTimeDifference -= System.currentTimeMillis();
                    rttAndRemote.put(roundTimeTrip, remoteAndLocalTimeDifference);
                }

                List rtt = new ArrayList(rttAndRemote.keySet());
                Collections.sort(rtt);
                long sum = 0;
                for(int i=0; i<RTT_NUMBER_TO_CALC_AVARAGE; i++){
                    sum += rttAndRemote.get(rtt.get(i));
                }
                remoteAndLocalTimeDifference = sum/RTT_NUMBER_TO_CALC_AVARAGE;
                GlobalVariables.roundTimeTrip = (long)rtt.get(0);

                isSynced = true;

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
