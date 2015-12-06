package com.caju.uheer.debug.activities;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.app.core.Channel;
import com.caju.uheer.app.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

public class ChannelsActivity extends AppCompatActivity {

    ListView channelListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        channelListView = (ListView)findViewById(R.id.channelListView);

        //new AllChannelsTask().execute();

        getWifiInformation();
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

    private class AllChannelsTask extends AsyncTask<Void, Void, Channel[]> {
        @Override
        protected Channel[] doInBackground(Void... params) {
            Channel[] activeChannels = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                activeChannels = restTemplate.getForObject(Routes.CHANNELS + "active", Channel[].class);
            } catch (Exception e) {
                Log.e("ChannelsActivity", e.getMessage(), e);
            }

            return activeChannels;
        }

        @Override
        protected void onPostExecute(Channel[] channels) {
            ArrayList<Channel> channelsNames = new ArrayList<Channel>();
            for (Channel channel : channels) {
                Log.d("ChannelsActivity", channel.Name);
                channelsNames.add(channel);
            }
            final ArrayAdapter<Channel> listAdapter = new ArrayAdapter<Channel>(getApplicationContext(),
             R.layout.adapter_music_list, channelsNames);

            channelListView.setAdapter(listAdapter);
            channelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), ListenActivity.class);
                    intent.putExtra("channel_id", listAdapter.getItem(i).Id);

                    startActivity(intent);
                }
            });
        }
    }

    void getWifiInformation(){
        Log.d("getWifiInformation","Getting wifi information");
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.d("getWifiInformation","2");
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("getWifiInformation","3");
        if(wifiInfo != null) {
            if(wifiInfo.getBSSID() != null) {
                Log.d("getWifiInformation", wifiInfo.getBSSID());
                TextView ssidTextView = (TextView) findViewById(R.id.ssid);
                ssidTextView.setText(wifiInfo.getSSID());
            }
            if(wifiInfo.getMacAddress() !=null) {
                TextView macTextView = (TextView) findViewById(R.id.mac);
                macTextView.setText(wifiInfo.getMacAddress());
            }
            if(wifiInfo.getIpAddress() != 0) {
                TextView ipTextView = (TextView) findViewById(R.id.ip);
                ipTextView.setText("" + wifiInfo.getIpAddress());
            }
        }
    }
}
