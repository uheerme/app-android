package com.caju.uheer.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        //It is final because it is accessed within another subclass.
        final AsyncTask fetchingInfo = new fetchActiveChannelsAndMusicTask().execute();

        //Associate Views with class objects for subsequent manipulation
        tabsInfoContainer = (ViewPager) findViewById(R.id.viewpager);
        tabsContainer = (TabLayout) findViewById(R.id.sliding_tabs);
        playAndStopFAB = (FloatingActionButton) findViewById(R.id.playOrStopFAB);
        loadingFragment = (FrameLayout) findViewById(R.id.loading_image_in_Playing_Activity);
        errorFragment = (FrameLayout) findViewById(R.id.error_image_in_Playing_Activity);

        //Inflate toolbar with app name
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.WHITE);

        //timeout for AsyncTask of 5 seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(fetchingInfo.getStatus() == AsyncTask.Status.RUNNING)
                    fetchingInfo.cancel(true);
            }
        }, 5000);
    }

    /*
        This routine is called when the floating button is pressed
     */

    public void playOrStop(View view)
    {

        int currentTab = tabsInfoContainer.getCurrentItem();
        Log.d("Current", "Current Fragment:" + String.valueOf(tabsInfoContainer.getCurrentItem()));
        if(UheerPlayer.isPlaying()){
            UheerPlayer.stop();
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_play_icon));
        } else if (UheerPlayer.isInitiated()){
            UheerPlayer.changeChannel(ActiveChannels.getActiveChannel(currentTab));
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
        } else {
            UheerPlayer.initPlayer(getApplicationContext(),ActiveChannels.getActiveChannel(currentTab));
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
        }
    }

    /*
        This AsyncTask fetches the currently active channels and their song list.
     */

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
            //It is removed from the view in both situations
            loadingFragment.setVisibility(View.GONE);

            ActiveChannels.setActiveChannels(channels);
            if(channels != null)
            {
                tabsInfoContainer.setVisibility(View.VISIBLE);
                playAndStopFAB.setVisibility(View.VISIBLE);

                //set Adapter that creates the Channel Info Fragments
                tabsInfoContainer.setAdapter(new PlayingFragmentAdapter(getSupportFragmentManager(), PlayingActivity.this));
                tabsInfoContainer.addOnPageChangeListener(new onSwypeWithinPlayingListener());

                //bind the tabs with their fragment (page)
                tabsContainer.setupWithViewPager(tabsInfoContainer);

                //Start Playing
                int currentTab = tabsInfoContainer.getCurrentItem();
                if (!UheerPlayer.isInitiated()) {
                    UheerPlayer.initPlayer(getApplicationContext(),ActiveChannels.getActiveChannel(0));
                //This is necessary in the case the app was closed with the back button
                } else if (UheerPlayer.currentChannelId() != ActiveChannels.getActiveChannel(currentTab).Id ) {
                    UheerPlayer.changeChannel(ActiveChannels.getActiveChannel(currentTab));
                }
            } else {
                //No channels were retrieved
                errorFragment.setVisibility(View.VISIBLE);
                TextView error_message = (TextView) findViewById(R.id.error_fragment_text);
                error_message.setText(R.string.no_connection);
            }
        }

        @Override
        protected void onCancelled()
        {
            loadingFragment.setVisibility(View.GONE);
            errorFragment.setVisibility(View.VISIBLE);
            TextView error_message = (TextView) findViewById(R.id.error_fragment_text);
            error_message.setText(R.string.slow_connection);
        }
    }

    class onSwypeWithinPlayingListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
        }
        /*
            This callback is called every time a page is fully selected and visible.
         */
        @Override
        public void onPageSelected(int position)
        {
            Log.d("Tab Selected", "Selected " + String.valueOf(position));
            if(UheerPlayer.isInitiated())
                UheerPlayer.changeChannel(ActiveChannels.getActiveChannel(position));
            else
                UheerPlayer.initPlayer(getApplicationContext(),ActiveChannels.getActiveChannel(position));
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
        }
    }
}