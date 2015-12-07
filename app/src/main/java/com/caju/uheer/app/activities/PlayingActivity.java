package com.caju.uheer.app.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.app.core.Channel;
import com.caju.uheer.app.core.Music;
import com.caju.uheer.app.interfaces.Routes;
import com.caju.uheer.app.services.ActiveChannels;
import com.caju.uheer.app.services.EmailLookup;
import com.caju.uheer.app.services.adapters.EmailListAdapter;
import com.caju.uheer.app.services.adapters.PlayingFragmentAdapter;
import com.caju.uheer.app.services.player.UheerPlayer;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

public class PlayingActivity extends FragmentActivity
{
    ViewPager tabsInfoContainer;
    TabLayout tabsContainer;
    FloatingActionButton playAndStopFAB, socialFAB;

    FrameLayout loadingFragment;
    FrameLayout errorFragment;

    String connectedEmail;

    DrawerLayout mDrawerLayout;

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

        //System.out.println("EMAIL " + ContactLookup.contactExists(this,"smokeonline3ju@gmail.com"));
        //System.out.println("EMAIL " + ContactLookup.getNameEmailDetails(this).toString());
        EmailLookup.init(this);

        /*
            Association of Views with their class objects for subsequent manipulation
         */
        tabsInfoContainer = (ViewPager) findViewById(R.id.viewpager);
        tabsContainer = (TabLayout) findViewById(R.id.sliding_tabs);
        playAndStopFAB = (FloatingActionButton) findViewById(R.id.playOrStopFAB);
        socialFAB = (FloatingActionButton) findViewById(R.id.socialFAB);
        loadingFragment = (FrameLayout) findViewById(R.id.loading_image_in_Playing_Activity);
        errorFragment = (FrameLayout) findViewById(R.id.error_image_in_Playing_Activity);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView)
            {

                playAndStopFAB.setVisibility(View.VISIBLE);
                socialFAB.setVisibility(View.VISIBLE);
                mDrawerLayout.setVisibility(View.GONE);
            }

            @Override public void onDrawerStateChanged(int newState) {}
        });
        mDrawerLayout.setVisibility(View.GONE);

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
        if(contas.size() > 0)
            connectedEmail = contas.get(0).toString();
        else
            connectedEmail = "undefined@email.com";
        Log.d("Connected Email", connectedEmail);

        
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

    @Override
    protected void onPause(){
        super.onPause();
        if(mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }

    @Override
    public void onBackPressed() {
        System.out.println("BACK");
        if(mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        else
            super.onBackPressed();
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
            UheerPlayer.initPlayer(getApplicationContext(),ActiveChannels.getActiveChannel(currentTab),connectedEmail);
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
        }

        System.out.println(EmailLookup.searchEmail("smokeonline@gmail.com"));
        System.out.println(EmailLookup.searchEmail("smokeonline666@gmail.com"));
        System.out.println(EmailLookup.searchEmail("smokeonlinegmail.com"));

    }

    public void enableSocial(View view)
    {
        playAndStopFAB.setVisibility(View.GONE);
        socialFAB.setVisibility(View.GONE);
        new fetchListenersTask().execute();
        mDrawerLayout.setVisibility(View.VISIBLE);
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }
        /*
        This AsyncTask fetches the currently active channels and their song list.
     */

    class fetchActiveChannelsAndMusicTask extends AsyncTask<Void, Void, Channel[]>
    {
        @Override
        protected Channel[] doInBackground(Void... params)
        {
            Channel[] allActiveChannels = null;
            Music[] channelSongs = null;
            ArrayList<Channel> possibleChannels = new ArrayList<>();

            try
            {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                allActiveChannels = restTemplate.getForObject(Routes.API + Routes.CHANNELS + Routes.ACTIVE, Channel[].class);
                Log.d("Channels Route", Routes.API + Routes.CHANNELS + Routes.ACTIVE);

                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                if(allActiveChannels != null)
                {
                    /*// Filter not available channels
                    for(int i = 0; i < allActiveChannels.length ; i++){
                        if(allActiveChannels[i].HostIpAddress.compareTo(ip) != 0)
                            allActiveChannels[i] = null;
                    }*/

                    for(Channel c : allActiveChannels)
                    {
                        if(c == null)
                            continue;
                        channelSongs = restTemplate.getForObject(Routes.API + Routes.CHANNELS + c.Id + Routes.MUSICS, Music[].class);
                        c.Musics = channelSongs;
                    }
                }
            } catch (Exception e)
            {
                Log.e("ChannelsActivity", e.getMessage(), e);
            }

            return allActiveChannels;
        }

        @Override
        protected void onPostExecute(Channel[] channels)
        {
            /*
                Remove from screen auxiliary views
             */
            loadingFragment.setVisibility(View.GONE);
            errorFragment.setVisibility(View.GONE);

            // Filter not available channels
            Channel[] possibleChannels = null;
            int j = 0;
            for(int i = 0; i < channels.length; i++){
                if(channels[i] != null)
                    j++;
            }
            if(j > 0)
            {
                possibleChannels = new Channel[j];
                j=0;
                for(int i = 0; i < channels.length; i++)
                {
                    if(channels[i] != null)
                        possibleChannels[j++] = channels[i];
                }
            }

            /*
                Update active channels locally
             */
            ActiveChannels.setActiveChannels(possibleChannels);
            if(possibleChannels != null)
            {
                tabsInfoContainer.setVisibility(View.VISIBLE);
                playAndStopFAB.setVisibility(View.VISIBLE);
                socialFAB.setVisibility(View.VISIBLE);

                //set Adapter that creates the Channel Info Fragments
                tabsInfoContainer.setAdapter(new PlayingFragmentAdapter(getSupportFragmentManager(), PlayingActivity.this));
                tabsInfoContainer.addOnPageChangeListener(new onSwypeWithinPlayingListener());

                //bind the tabs with their fragment (page)
                tabsContainer.setupWithViewPager(tabsInfoContainer);

                //Start Playing
                int currentTab = tabsInfoContainer.getCurrentItem();
                if (!UheerPlayer.isInitiated()) {
                    UheerPlayer.initPlayer(getApplicationContext(),ActiveChannels.getActiveChannel(0),connectedEmail);
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
                UheerPlayer.initPlayer(getApplicationContext(),ActiveChannels.getActiveChannel(position),connectedEmail);
            playAndStopFAB.setImageDrawable(getResources().getDrawable(R.drawable.white_stop_icon));
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {

        }
    }

    class fetchListenersTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params)
        {
            ArrayList<String>[] listeners = new ArrayList[ActiveChannels.getNumberOfActiveChannels()];
            for(int i = 0; i < ActiveChannels.getNumberOfActiveChannels(); i++){
                URL url = null;
                HttpURLConnection urlConnection = null;
                String response = null;
                try{
                    response = null;
                    String s = "http://debugmaster.koding.io:9000/" + ActiveChannels.getActiveChannel(i).Id;
                    System.out.println(s);
                    url = new URL(s);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader isw = new InputStreamReader(in);
                    char[] buffer = new char[4096];
                    while(isw.read(buffer) > 0){
                        response = response + new String(buffer);
                    };
                } catch (MalformedURLException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } finally
                {
                    if(urlConnection != null)
                        urlConnection.disconnect();
                }
                response = response.substring(response.indexOf("[")+1,response.indexOf("]"));
                if(response != null && response.length() > 0){
                    System.out.println(response);
                    String[] split = response.split(",");
                    ArrayList<String> array = new ArrayList<String>(Arrays.asList(split));
                    for(String s : split){
                        if(!EmailLookup.searchEmail(s))
                            array.remove(s);
                    }
                    listeners[i] = array;
                }

            }
            ActiveChannels.setActiveListeners(listeners);
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            doAfter();
        }
    }

    // Fake data, a simulation of what would we receive from the server.
    public static final String nearbyUsersString = "{\"nearbyUsers\": [" +
            "{" +
            "\"email\": \"flaviafernanda_moraes@hotmail.com\", " +
            "\"geoPoint\": {\"latitude\": -22.002899, \"longitude\": -47.893118} " +
            "}," +
            "{" +
            "\"email\": \"cesarteixeira@gmail.com\", " +
            "\"geoPoint\": {\"latitude\": -21.985047, \"longitude\": -47.882528} " +
            "}," +
            "{" +
            "\"email\": \"lucasolivdavid@gmail.com\", " +
            "\"geoPoint\": {\"latitude\": -22.000540, \"longitude\": -47.899306} " +
            "}," +
            "{" +
            "\"email\": \"cristiadu@gmail.com\", " +
            "\"geoPoint\": {\"latitude\": -22.904066, \"longitude\": -47.099372} " +
            "}," +
            "{" +
            "\"email\": \"francielledemattos@gmail.com\", " +
            "\"geoPoint\": {\"latitude\": -21.979670, \"longitude\": -47.880059} " +
            "}," +
            "{" +
            "\"email\": \"felipe_reis@dc.ufscar.br\", " +
            "\"geoPoint\": {\"latitude\": -15.923298, \"longitude\": -47.806466} " +
            "}," +
            "{" +
            "\"email\": \"fabiano@dc.ufscar.br\", " +
            "\"channel\": \"MDS CHANNEL A\" " +
            "}," +
            "{" +
            "\"email\": \"lucasolivdavid@gmail.com\", " +
            "\"geoPoint\": {\"latitude\": -22.000540, \"longitude\": -47.899306} " +
            "}," +
            "{" +
            "\"email\": \"thamenato@gmail.com\", " +
            "\"geoPoint\": {\"latitude\": -39.129986, \"longitude\": -77.093338} " +
            "}" +
            "]}";

    private void doAfter(){

        ArrayList<String> friendsEmails = new ArrayList<>();
        for(int i = 0; i < ActiveChannels.getNumberOfActiveChannels(); i++){
            String channel = ActiveChannels.getActiveChannel(i).Name;
            for(String s : ActiveChannels.getActiveListeners(i)){
                if(s.compareTo(connectedEmail) == 0)
                    continue;

                if(android.os.Build.VERSION.SDK_INT >= 19)
                    friendsEmails.add(channel + System.lineSeparator() + s);
                else
                    friendsEmails.add(s);
            }
        }
        Collections.sort(friendsEmails);
        EmailListAdapter listAdapter = new EmailListAdapter(this, R.layout.adapter_email_list, friendsEmails);
        ListView emails = (ListView) findViewById(R.id.email_friends_from_drawer);
        emails.setAdapter(listAdapter);

        listAdapter = new EmailListAdapter(this, R.layout.adapter_email_list, friendsEmails);
        ListView gps = (ListView) findViewById(R.id.gps_friends_from_drawer);
        gps.setAdapter(listAdapter);

        // Querying contacts with the same email received from the server.
//        Bundle bundle = new Bundle();
//        bundle.putString("jsonString", nearbyUsersString);
//        bundle.putString("connectedEmail", connectedEmail);
//        ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(this);
//        getLoaderManager().restartLoader(0, bundle, loaderCallbacks);
    }
}