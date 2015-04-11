package com.caju.http_requests;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.getRequests.GetChannel;
import com.caju.utils.getRequests.GetChannelMusics;
import com.caju.utils.interfaces.OnFailedListener;
import com.caju.utils.interfaces.OnFinishedListener;
import com.caju.utils.postRequests.PostMusic;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements OnFinishedListener, OnFailedListener {

    private SharedPreferences app_settings;
    private SharedPreferences.Editor editor;

    private EditText channelNumber;
    private TextView textView;

    private GetChannel channel;
    private GetChannelMusics channelMusics;
    private PostMusic postMusic;
    private int lastButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Finding important elements in the layout
        channelNumber = (EditText) findViewById(R.id.channel_id);
        textView = (TextView) findViewById(R.id.result_container);
    }

    /* When user clicks in the button*/
    public void getChannel(View view) {
        lastButton = 1;
        // Gets the URL from the UI's text field.
        int id;
        String s_id = channelNumber.getText().toString();
        try { id = Integer.parseInt(s_id); } catch (NumberFormatException e) { id = 0; }

        try
        {
            channel = new GetChannel(id, getApplicationContext());
            channel.setOnLoadFinishedListener(this);
            channel.setOnLoadFailedListener(this);
        }
        catch (NoConnectionException e)
        {
            textView.setText("You have no connection.");
        }
    }

    public void getChannelMusics(View view) {
        lastButton = 2;
        // Gets the URL from the UI's text field.
        int id;
        String s_id = channelNumber.getText().toString();
        try
        {
            id = Integer.parseInt(s_id);
        }
        catch (NumberFormatException e)
        {
            id = 0;
        }

        try
        {
            channelMusics = new GetChannelMusics(id, getApplicationContext());
            channelMusics.setOnLoadFinishedListener(this);
            channelMusics.setOnLoadFailedListener(this);
        }
        catch (NoConnectionException e)
        {
            textView.setText("You have no connection.");
        }
    }

    public void postMusic(View view) {
        lastButton = 3;
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        else{
            intent = new Intent(Intent.ACTION_PICK);
        }
        intent.setType("audio/*");
        startActivityForResult(intent, 10);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    /* This method is called after GetChannel is finished */
    public void onLoadFinished() {
        System.out.println("Executing OnLoadFinished");
        /*if(lastButton == 1){
            if(channel.getResultResponse() != null)
                textView.setText(channel.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }*/
        if(lastButton == 1){
            if(channel.getResultResponse() != null)
                textView.setText(channel.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }
        else if(lastButton == 2){
            if(channelMusics.getResultResponse() != null)
                textView.setText(channelMusics.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }
        else if(lastButton == 3){
            if(postMusic.getResultResponse() != null)
                textView.setText(postMusic.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }
    }

    @Override
    public void onLoadFailed() {
        System.out.println("Executing OnLoadFailed");
        textView.setText("Unable to retrieve information from Channel.");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {

        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            ArrayList<String> files = new ArrayList<>();

            if (resultData != null) {
                uri = resultData.getData();
                files.add(uri.getPath());
                if(new File(uri.getPath()).exists())
                    System.out.println("EXISTS");
            }

            int id;
            String s_id = channelNumber.getText().toString();
            try { id = Integer.parseInt(s_id); } catch (NumberFormatException e) { id = 0; }

            try {
                postMusic = new PostMusic(getApplicationContext(),id,files);
                postMusic.setOnLoadFinishedListener(this);
                postMusic.setOnLoadFailedListener(this);
            }
            catch (NoConnectionException e)
            {
                textView.setText("You have no connection.");
            }

        }

    }
}