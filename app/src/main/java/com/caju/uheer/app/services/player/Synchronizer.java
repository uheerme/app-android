package com.caju.uheer.app.services.player;

import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.app.core.Channel;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.app.interfaces.Routes;
import com.caju.uheer.app.services.BackendStatus;
import com.caju.uheer.app.services.infrastructure.SyncItem;

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

    final static int CHRISTIAN_EXECUTION_COUNT = 10;
    final static int RTT_USED_COUNT = 3;

    protected Channel channel;
    protected boolean isSynced;
    protected long remoteAndLocalTimeDifference;

    protected ISyncListener listener;
    public interface ISyncListener {
        void onFinished();
    }

    public Synchronizer(Channel channel) {
        this.channel = channel;

        channel.analyzePlaylist();
    }

    public long getRemoteTime() {
        return remoteAndLocalTimeDifference + System.currentTimeMillis();
    }

    public Synchronizer sync() {
        Log.d("Synchronizer", "Synchronization procedure method has started.");

        isSynced = false;
        new CristianTask().execute();

        return this;
    }

    public SyncItem findCurrent() {
        long remoteTime = getRemoteTime();
        long timeline = remoteTime - channel.CurrentStartTime.getTime();

        // If the timeline is greater than the playlist length and the
        // channel doesn't loop, we now the channel is stalled!
        if (timeline > channel.LengthInMilliseconds && !channel.Loops) {
            return null;
        }

        // Disregards all loops that have occurred in the playlist.
        if (channel.LengthInMilliseconds > 0) {
            long timesItLooped = timeline / channel.LengthInMilliseconds;
            
            timeline %= channel.LengthInMilliseconds;
            channel.CurrentStartTime.setTime(channel.CurrentStartTime.getTime() + channel.LengthInMilliseconds * timesItLooped);
        }
        
        while (timeline > channel.current.LengthInMilliseconds) {
            timeline -= channel.current.LengthInMilliseconds;
            channel.next();
        }

        Log.d("Current found", channel.current + ", starting at " + timeline);

        return new SyncItem(channel.current, timeline);
    }

    public Synchronizer setListener(ISyncListener listener) {
        this.listener = listener;

        return this;
    }

    public boolean isSynchronized() {
        return isSynced;
    }

    protected class CristianTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                RestTemplate serializer = new RestTemplate();
                serializer.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ", Locale.US);

                HashMap<Long, Long> rttAndRemote = new HashMap<>();

                for (int i = 0; i < CHRISTIAN_EXECUTION_COUNT; i++) {
                    long localTime = System.currentTimeMillis();

                    BackendStatus response = serializer.getForObject(Routes.API + Routes.STATUS + Routes.NOW, BackendStatus.class);

                    Date formattedDate = formatter.parse(response.Now.replaceAll("(\\.[0-9]{3})[0-9]*(Z$)", "$1+0000"));

                    long roundTimeTrip = System.currentTimeMillis() - localTime;

                    remoteAndLocalTimeDifference = formattedDate.getTime();
                    remoteAndLocalTimeDifference += roundTimeTrip / 2;
                    remoteAndLocalTimeDifference -= System.currentTimeMillis();

                    rttAndRemote.put(roundTimeTrip, remoteAndLocalTimeDifference);
                }

                List rtt = new ArrayList(rttAndRemote.keySet());
                Collections.sort(rtt);

                long sum = 0;
                for (int i = 0; i < RTT_USED_COUNT; i++) {
                    sum += rttAndRemote.get(rtt.get(i));
                }

                remoteAndLocalTimeDifference = sum / RTT_USED_COUNT;
                GlobalVariables.roundTimeTrip = (long) rtt.get(0);

                isSynced = true;
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            if (listener != null) {
                listener.onFinished();
            }

            return null;
        }
    }
}
