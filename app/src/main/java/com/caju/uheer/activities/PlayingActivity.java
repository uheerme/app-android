package com.caju.uheer.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.util.Patterns;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class PlayingActivity extends FragmentActivity
{
    ViewPager tabsInfoContainer;
    TabLayout tabsContainer;
    FloatingActionButton playAndStopFAB;

    FrameLayout loadingFragment;
    FrameLayout errorFragment;

    /*
        This task will fetch the active channels around the user and
        their music. It is final to be acessed within another class.
    */
    final AsyncTask fetchingInfo = new fetchActiveChannelsAndMusicTask().execute();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        ArrayList<String> contas = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = ((AccountManager) getSystemService(ACCOUNT_SERVICE)).getAccounts();
        for (Account a : accounts) {
            if (emailPattern.matcher(a.name).matches()) {
                if(contas.size() == 0)
                    contas.add(a.name);
                else if(Collections.frequency(contas,a.name)+1 > Collections.frequency(contas,contas.get(0)))
                    contas.add(0,a.name);
                else
                    contas.add(a.name);
            }
        }

        /*
            Association of Views with their class objects for subsequent manipulation
         */
        tabsInfoContainer = (ViewPager) findViewById(R.id.viewpager);
        tabsContainer = (TabLayout) findViewById(R.id.sliding_tabs);
        playAndStopFAB = (FloatingActionButton) findViewById(R.id.playOrStopFAB);
        loadingFragment = (FrameLayout) findViewById(R.id.loading_image_in_Playing_Activity);
        errorFragment = (FrameLayout) findViewById(R.id.error_image_in_Playing_Activity);

        /*
            Load the toolbar with the app's name
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.WHITE);

        /*
            Timeout of 5 seconds for AsyncTask
            to warn user of potential delay
         */
        Handler waitMessage = new Handler();
        waitMessage.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(fetchingInfo.getStatus() == AsyncTask.Status.RUNNING)
                    onTakingTooLong();
            }
        }, 5000);

        /*
            Timeout of 10 seconds for AsyncTask
            to cancel running task
         */
        Handler cancelTask = new Handler();
        cancelTask.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(fetchingInfo.getStatus() == AsyncTask.Status.RUNNING)
                    fetchingInfo.cancel(true);
            }
        }, 10000);
    }

    /*
        This routine sets up a warning message (via an error fragment)
        to the user in case of fetching is taking too long
     */

    private void onTakingTooLong()
    {
        if(fetchingInfo.getStatus() == AsyncTask.Status.RUNNING)
        {
            loadingFragment.setVisibility(View.GONE);
            errorFragment.setVisibility(View.VISIBLE);
            TextView error_message = (TextView) findViewById(R.id.error_fragment_text);
            error_message.setText(R.string.taking_too_long);
        }
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
                activeChannels = restTemplate.getForObject(Routes.API + Routes.CHANNELS + Routes.ACTIVE, Channel[].class);
                Log.d("Channels Route", Routes.API + Routes.CHANNELS + Routes.ACTIVE);

                if(activeChannels != null)
                {
                    for(Channel c : activeChannels)
                    {
                        channelSongs = restTemplate.getForObject(Routes.API + Routes.CHANNELS + c.Id + Routes.MUSICS, Music[].class);
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
            /*
                Remove from screen auxiliary views
             */
            loadingFragment.setVisibility(View.GONE);
            errorFragment.setVisibility(View.GONE);

            /*
                Update active channels locally
             */
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