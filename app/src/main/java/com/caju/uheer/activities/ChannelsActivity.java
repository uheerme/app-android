package com.caju.uheer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.caju.uheer.R;
import com.caju.uheer.core.Channel;
import com.caju.uheer.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class ChannelsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        new AllChannelsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_channels, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToListenActivity(View view) {
        Intent intent = new Intent(this, ListenActivity.class);
        intent.putExtra("channel_id", 1);

        startActivity(intent);
    }

    private class AllChannelsTask extends AsyncTask<Void, Void, Channel[]> {
        @Override
        protected Channel[] doInBackground(Void... params) {
            Channel[] channels = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                channels = restTemplate.getForObject(Routes.CHANNELS, Channel[].class);
            } catch (Exception e) {
                Log.e("ChannelsActivity", e.getMessage(), e);
            }

            return channels;
        }

        @Override
        protected void onPostExecute(Channel[] channels) {
            for (Channel channel : channels) {
                Log.d("ChannelsActivity", channel.Name);
            }
        }
    }
}
