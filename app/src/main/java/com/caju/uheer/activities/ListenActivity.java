package com.caju.uheer.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.core.Channel;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.UheerPlayer;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class ListenActivity extends AppCompatActivity {
    private int channel_id;
    private Channel channel;
    private UheerPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_listen);

        channel_id = getIntent().getIntExtra("channel_id", 0);
        if (channel_id > 0) {
            new GetChannelTask().execute();
        } else {
            Log.e("ListenActivity", "We cannot load a channel with id " + channel_id + ".");
        }
    }

    protected void onChannelLoad() {
        player = new UheerPlayer(getApplicationContext(), channel)
                .start();

        TextView quality = (TextView) findViewById(R.id.quality);
        quality.setText("Round Time Trip was " + GlobalVariables.roundTimeTrip + " ms (Sync quality).");
        TextView channelNameTextView = (TextView) findViewById(R.id.channel_name);
        channelNameTextView.setText(channel.Name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listen, menu);
        return true;
    }

    public void getGlobals(View view) {
        TextView quality = (TextView) findViewById(R.id.quality);
        quality.setText("Round Time Trip was " + GlobalVariables.roundTimeTrip + " ms (Sync quality)..");
        TextView songNameTextView = (TextView) findViewById(R.id.song_name);
        songNameTextView.setText(GlobalVariables.playingSong.Id + " - " + GlobalVariables.playingSong.Name);
        TextView progressTextView = (TextView) findViewById(R.id.progress);
        progressTextView.setText("Progress: "+GlobalVariables.downloadProgress+"%");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        getGlobals(new View(getApplicationContext()));

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetChannelTask extends AsyncTask<Void, Void, Channel> {
        @Override
        protected Channel doInBackground(Void... params) {
            Channel c = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                c = restTemplate.getForObject(Routes.CHANNELS + channel_id, Channel.class);
            } catch (Exception e) {
                Log.e("GameNightActivity", e.getMessage(), e);
            }

            return channel = c;
        }

        @Override
        protected void onPostExecute(Channel channel) {
            onChannelLoad();
        }
    }
}
