package com.caju.uheer.activities;

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
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.caju.uheer.R;
import com.caju.uheer.core.ActiveChannels;
import com.caju.uheer.core.Channel;
import com.caju.uheer.core.Music;
import com.caju.uheer.services.UheerPlayer;
import com.caju.uheer.services.adapters.PlayingFragmentAdapter;
import com.caju.uheer.interfaces.Routes;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class PlayingActivity extends FragmentActivity
{
    ViewPager tabsInfoContainer;
    TabLayout tabsContainer;
    FloatingActionButton playAndStopFAB;

    FrameLayout loadingFragment;
    FrameLayout errorFragment;

    private UheerPlayer player;
    int currentPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        currentPlaying = 1;
        new fetchActiveChannelsAndMusicTask().execute();

        tabsInfoContainer = (ViewPager) findViewById(R.id.viewpager);
        tabsContainer = (TabLayout) findViewById(R.id.sliding_tabs);
        playAndStopFAB = (FloatingActionButton) findViewById(R.id.playOrStopFAB);
        loadingFragment = (FrameLayout) findViewById(R.id.loading_image_in_Playing_Activity);
        errorFragment = (FrameLayout) findViewById(R.id.error_image_in_Playing_Activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("uheer");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    public void playOrStopChannel(View view)
    {

        int currentFragment = tabsInfoContainer.getCurrentItem();
        Log.d("Current", "Current Fragment:" + String.valueOf(tabsInfoContainer.getCurrentItem()));
        if(player != null)
            player.stop();

        if(currentFragment == currentPlaying)
        {
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_play_icon));
            currentPlaying = 1;
            return;
        }

        currentPlaying = currentFragment;
        player = new UheerPlayer(getApplicationContext(),
                ActiveChannels.getActiveChannel(currentFragment)).start();
        playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
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
                tabsInfoContainer.setVisibility(View.VISIBLE);
                playAndStopFAB.setVisibility(View.VISIBLE);

                tabsInfoContainer.setAdapter(new PlayingFragmentAdapter(getSupportFragmentManager(), PlayingActivity.this));
                tabsInfoContainer.addOnPageChangeListener(new PlayingActivity.onHorizontalSwypeListener());

                // Give the TabLayout the ViewPager
                tabsContainer.setupWithViewPager(tabsInfoContainer);
            } else {
                loadingFragment.setVisibility(View.GONE);
                errorFragment.setVisibility(View.VISIBLE);
            }

        }
    }

    class onHorizontalSwypeListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            Log.d("Page","1" + String.valueOf(position));
        }

        @Override
        public void onPageSelected(int position)
        {
            Log.d("Page","2" + String.valueOf(position));
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
            Log.d("Page","3" + String.valueOf(state));
        }
    }
}