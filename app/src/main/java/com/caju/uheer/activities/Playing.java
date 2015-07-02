package com.caju.uheer.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.caju.uheer.R;
import com.caju.uheer.core.ActiveChannels;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.services.UheerPlayer;
import com.caju.uheer.services.adapters.PlayingFragmentAdapter;
import com.caju.uheer.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class Playing extends FragmentActivity
{
    ViewPager mViewPager;
    TabLayout mTabLayout;
    LinearLayout mLinearLayout;
    FloatingActionButton mFAB;

    private UheerPlayer player;
    int currentPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        currentPlaying = 1;
        new fetchActiveChannelsAndMusicTask().execute();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mLinearLayout = (LinearLayout) findViewById(R.id.empty_channel_image_in_playing);
        mFAB = (FloatingActionButton) findViewById(R.id.playOrStopFAB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("uheer");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    public void playOrStopChannel(View view)
    {

        int currentFragment = mViewPager.getCurrentItem();
        Log.d("Current", "Current Fragment:" + String.valueOf(mViewPager.getCurrentItem()));
        if(player != null)
            player.stop();

        if(currentFragment == currentPlaying)
        {
            mFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_play_icon));
            currentPlaying = 1;
            return;
        }

        currentPlaying = currentFragment;
        player = new UheerPlayer(getApplicationContext(),
                ActiveChannels.getActiveChannel(currentFragment)).start();
        mFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
    }

    class fetchActiveChannelsAndMusicTask extends AsyncTask<Void, Void, Channel[]>
    {
        @Override
        protected Channel[] doInBackground(Void... params)
        {
            Channel[] activeChannels = null;
            Music[] channelSongs = null;

            try
            {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Log.d("Channels Route", Routes.CHANNELS + "active");
                activeChannels = restTemplate.getForObject(Routes.CHANNELS + "active", Channel[].class);

                if(activeChannels != null)
                {
                    for(Channel c : activeChannels)
                    {
                        channelSongs = restTemplate.getForObject(Routes.CHANNELS + c.Id + "/musics", Music[].class);
                        c.Musics = channelSongs;
                    }
                }
            } catch (Exception e)
            {
                Log.e("ChannelsActivity", e.getMessage(), e);
            }

            return activeChannels;
        }

        @Override
        protected void onPostExecute(Channel[] channels)
        {

            ActiveChannels.setActiveChannels(channels);
            if(channels != null)
            {
                mLinearLayout.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);
                mFAB.setVisibility(View.VISIBLE);

                mViewPager.setAdapter(new PlayingFragmentAdapter(getSupportFragmentManager(), Playing.this));

                // Give the TabLayout the ViewPager
                mTabLayout.setupWithViewPager(mViewPager);
            }

        }
    }
}