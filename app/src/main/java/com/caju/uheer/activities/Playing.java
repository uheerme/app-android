package com.caju.uheer.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.caju.uheer.R;
import com.caju.uheer.core.ActiveChannels;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.debug.SampleAdapter;
import com.caju.uheer.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Playing extends FragmentActivity
{
    ViewPager mViewPager;
    TabLayout mTabLayout;
    LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        new AllChannelsTask().execute();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mLinearLayout = (LinearLayout) findViewById(R.id.empty_channel_image_in_playing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("uheer");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    class AllChannelsTask extends AsyncTask<Void, Void, Channel[]>
    {
        @Override
        protected Channel[] doInBackground(Void... params) {
            Channel[] activeChannels = null;
            Music[] channelSongs = null;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Log.d("Channels Route", Routes.CHANNELS + "active");
                activeChannels = restTemplate.getForObject(Routes.CHANNELS + "active", Channel[].class);

                if(activeChannels != null){
                    for(Channel c:activeChannels){
                        channelSongs = restTemplate.getForObject(Routes.CHANNELS + c.Id + "/musics", Music[].class);
                        c.Musics = channelSongs;
                    }
                }
            } catch (Exception e) {
                Log.e("ChannelsActivity", e.getMessage(), e);
            }

            return activeChannels;
        }

        @Override
        protected void onPostExecute(Channel[] channels) {

            ActiveChannels.setActiveChannels(channels);
            if(channels != null)
            {
                mLinearLayout.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);

                mViewPager.setAdapter(new SampleAdapter(getSupportFragmentManager(), Playing.this));

                // Give the TabLayout the ViewPager
                mTabLayout.setupWithViewPager(mViewPager);
            }

        }
    }
}
