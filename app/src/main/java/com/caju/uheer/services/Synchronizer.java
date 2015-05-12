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

    private long remoteAndLocalTimeDifference;

    private ISyncedListener syncedListener;

    public interface ISyncedListener {
        void onSyned();
    }

    public Synchronizer(Channel channel) {
        this.channel = channel;

        channel.analyzePlaylist();
    }

    public Synchronizer sync() {
        Log.d("Synchronizer", "Synchronization procedure method has started.");

        isSynced = false;

        new CristianTask().execute();

        return this;
    }

    public PlaylistItem findCurrent() {

        Log.d("Initial current", channel.current.toString());

        long remoteTime = remoteAndLocalTimeDifference + System.currentTimeMillis();
        long timeline = remoteTime - channel.CurrentStartTime.getTime();

        Log.d("findCurrent timeline1", "" + timeline);
        Log.d("findCurrent remLocalDif", "" + remoteAndLocalTimeDifference);
        Log.d("findCurrent remoteTime", "" + remoteTime);
        Log.d("findCurr channCurrTime", "" + channel.CurrentStartTime.getTime());

        // If the timeline is greater than the playlist length and the
        // channel doesn't loop, we now the channel is stalled!
        if (timeline > channel.LengthInMilliseconds && !channel.Loops) {
            return null;
        }

        // Disregards all loops that have occurred in the playlist.
        timeline %= channel.LengthInMilliseconds;

        while (timeline > channel.current.LengthInMilliseconds) {
            timeline -= channel.current.LengthInMilliseconds;
            channel.next();
            Log.d("Timeline reduced", "" + timeline);
        }

        GlobalVariables.playingSong = channel.current;

        //Recalculating timeline to get better precision.
        timeline += System.currentTimeMillis() - remoteTime + remoteAndLocalTimeDifference;

        Log.d("Current found", channel.current + ", starting at " + timeline);

        return new PlaylistItem(channel.current, timeline);
    }

    public Synchronizer onSyncedListener(ISyncedListener listener) {
        this.syncedListener = listener;

        return this;
    }

    private class CristianTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                RestTemplate serializer = new RestTemplate();
                serializer.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ", Locale.US);

                HashMap<Long, Long> rttAndRemote = new HashMap<>();

                for (int i = 0; i < CHRISTIAN_ITERATION_NUMBER; i++) {
                    long localTime = System.currentTimeMillis();

                    BackendStatus rawResponse = serializer.getForObject(Routes.STATUS + "now/", BackendStatus.class);
                    Log.d("response raw", rawResponse.Now);

                    Date responseNow = formatter.parse(rawResponse.Now.replaceAll("(\\.[0-9]{3})[0-9]*(Z$)", "$1+0000"));

                    long roundTimeTrip = System.currentTimeMillis() - localTime;

                    Log.d("Synchronizer", "The Round Time Trip was " + roundTimeTrip + "ms.");

                    Log.d("response date", responseNow.toString());

                    remoteAndLocalTimeDifference = responseNow.getTime();
                    remoteAndLocalTimeDifference += roundTimeTrip / 2;
                    remoteAndLocalTimeDifference -= System.currentTimeMillis();

                    rttAndRemote.put(roundTimeTrip, remoteAndLocalTimeDifference);
                }

                Log.d("rttAndRemote", rttAndRemote.toString());

                List rtt = new ArrayList(rttAndRemote.keySet());
                Collections.sort(rtt);

                Log.d("sorted rtt", rtt.toString());

                long sum = 0;
                for (int i = 0; i < RTT_NUMBER_TO_CALC_AVARAGE; i++) {
                    Log.d(i + " pick", rttAndRemote.get(rtt.get(i)).toString());
                    sum += rttAndRemote.get(rtt.get(i));
                }

                remoteAndLocalTimeDifference = sum / RTT_NUMBER_TO_CALC_AVARAGE;
                GlobalVariables.roundTimeTrip = (long) rtt.get(0);

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
