package com.caju.uheer.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.caju.uheer.R;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.BackendStatus;
import com.caju.uheer.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class GameNightActivity extends Activity {

    private Channel[] activeChannels;
    private BackendStatus currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_night);

        new AllActiveChannelsTask().execute();
        new StatusNowTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private class AllActiveChannelsTask extends AsyncTask<Void, Void, Channel[]> {
        @Override
        protected Channel[] doInBackground(Void... params) {
            Channel[] channels = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                channels = restTemplate.getForObject(Routes.CHANNELS + "/active", Channel[].class);
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            return activeChannels = channels;
        }

        @Override
        protected void onPostExecute(Channel[] channels) {
            Log.d("GameNightActivity", "Active channels retrieved!");
            for (Channel channel : channels) {
                Log.d("GameNightActivity", channel.Name);
            }
        }
    }

    private class StatusNowTask extends AsyncTask<Void, Void, BackendStatus> {
        @Override
        protected BackendStatus doInBackground(Void... params) {
            BackendStatus backendStatus = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                backendStatus = restTemplate.getForObject(Routes.STATUS + "now/", BackendStatus.class);
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            return currentTime = backendStatus;
        }

        @Override
        protected void onPostExecute(BackendStatus backendStatus) {
            if (backendStatus.Now != null) {
                Log.d("GameNightActivity", backendStatus.Now.toString());
            } else {
                Log.d("GameNightActivity", "backendStatus.Now is null!");
            }
        }
    }
}
