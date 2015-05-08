package com.caju.uheer.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.caju.uheer.R;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.CurrentTimeViewModel;
import com.caju.uheer.infrastructure.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class GameNightActivity extends Activity {

    private Channel[] activeChannels;
    private CurrentTimeViewModel currentTime;

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

                channels = restTemplate.getForObject(Routes.CHANNELS + Routes.ACTIVE, Channel[].class);
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            return channels;
        }

        @Override
        protected void onPostExecute(Channel[] channels) {
            activeChannels = channels;

            for (Channel channel : channels) {
                Log.d("GameNightActivity", channel.Name);
            }
        }
    }

    private class StatusNowTask extends AsyncTask<Void, Void, CurrentTimeViewModel> {
        @Override
        protected CurrentTimeViewModel doInBackground(Void... params) {
            CurrentTimeViewModel currentTimeViewModel = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                currentTimeViewModel = restTemplate.getForObject(Routes.NOW, CurrentTimeViewModel.class);
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            return currentTimeViewModel;
        }

        @Override
        protected void onPostExecute(CurrentTimeViewModel currentTimeViewModel) {
            Log.d("GameNightActivity", currentTimeViewModel.Now.toString());

            currentTime = currentTimeViewModel;
        }
    }
}
