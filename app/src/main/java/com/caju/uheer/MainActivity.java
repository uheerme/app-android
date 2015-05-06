package com.caju.uheer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.exceptions.NoIDSelectedException;
import com.caju.utils.getRequests.GetChannel;
import com.caju.utils.getRequests.GetChannelMusic;
import com.caju.utils.getRequests.GetMusic;
import com.caju.utils.getRequests.GetStatusNow;
import com.caju.utils.interfaces.OnFailedListener;
import com.caju.utils.interfaces.OnFinishedListener;
import com.caju.utils.postRequests.PostMusic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements OnFinishedListener, OnFailedListener {

    private SharedPreferences app_settings;
    private SharedPreferences.Editor editor;

    private EditText number;
    private TextView textView;

    private int lastButtonClicked;

    private GetStatusNow getStatusNow;
    private GetChannel getChannel;
    private GetChannelMusic getChannelMusic;
    private GetMusic getMusic;
    private PostMusic postMusic;



    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Finding important elements in the layout
        number = (EditText) findViewById(R.id.channel_id);
        textView = (TextView) findViewById(R.id.result_container);
    }

    /* When user clicks in the button*/
    public void getChannel(View view) {
        lastButtonClicked = 1;
        // Gets the URL from the UI's text field.
        int id;
        String s_id = number.getText().toString();
        try { id = Integer.parseInt(s_id); } catch (NumberFormatException e) { id = 0; }

        try
        {
            getChannel = new GetChannel(id, getApplicationContext());
            getChannel.setOnLoadFinishedListener(this);
            getChannel.setOnLoadFailedListener(this);
            getStatusNow = new GetStatusNow(getApplicationContext());
        }
        catch (NoConnectionException e)
        {
            textView.setText("You have no connection.");
        }
        catch (NoIDSelectedException e)
        {
            textView.setText("You have no ID.");
        }
    }

    public void getChannelMusics(View view) {
        lastButtonClicked = 2;
        // Gets the URL from the UI's text field.
        int id;
        String s_id = number.getText().toString();
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
            getChannelMusic = new GetChannelMusic(id, getApplicationContext());
            getChannelMusic.setOnLoadFinishedListener(this);
            getChannelMusic.setOnLoadFailedListener(this);
        }
        catch (NoConnectionException e)
        {
            textView.setText("You have no connection.");
        }
        catch (NoIDSelectedException e)
        {
            textView.setText("You have no ID.");
        }
    }

    public void postMusic(View view) {
        lastButtonClicked = 3;
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

    public void getMusic(View view) {
        lastButtonClicked = 4;
        // Gets the URL from the UI's text field.
        int id;
        String s_id = number.getText().toString();
        try { id = Integer.parseInt(s_id); } catch (NumberFormatException e) { id = 0; }

        try
        {
            getMusic = new GetMusic(id, getApplicationContext());
            getMusic.setOnLoadFinishedListener(this);
            getMusic.setOnLoadFailedListener(this);
        }
        catch (NoConnectionException e)
        {
            textView.setText("You have no connection.");
        }
        catch (IOException e)
        {
            textView.setText("File not loaded.");
            e.printStackTrace();
        }
        catch (NoIDSelectedException e)
        {
            textView.setText("You have no ID.");
        }
    }

    public void gameNightTest(View view) {
        lastButtonClicked = 5;

        Intent intent = new Intent(this, GameNightActivity.class);
        startActivity(intent);
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
        /*if(lastButtonClicked == 1){
            if(getChannel.getResultResponse() != null)
                textView.setText(getChannel.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }*/
        if(lastButtonClicked == 1){
            if(getChannel.getResultResponse() != null)
                textView.setText(getChannel.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }
        else if(lastButtonClicked == 2){
            if(getChannelMusic.getResultResponse() != null)
                textView.setText(getChannelMusic.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }
        else if(lastButtonClicked == 3){
            if(postMusic.getResultResponse() != null)
                textView.setText(postMusic.getResultResponse());
            else
                textView.setText("This shouldn't happen");
        }
        else if(lastButtonClicked == 4){
            if(getMusic.getResultResponse() != null)
                textView.setText(getMusic.getResultResponse());
            else
                textView.setText("This shouldn't happen");

            File file = new File(getFileStreamPath(getMusic.getFilename()).getPath());
            Uri u = Uri.fromFile(file);
            if(mediaPlayer != null)
                mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            try
            {
                mediaPlayer.setDataSource(this,u);
                mediaPlayer.prepare();
                //mediaPlayer.seekTo(30*1000);
                mediaPlayer.start();

            } catch (IOException e)
            {
                System.err.println("SONG COULD NOT BE PREPARED AND PLAYED");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onLoadFailed() {
        System.out.println("Executing OnLoadFailed");
        textView.setText("Something went wrong in op " + lastButtonClicked);
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
            }

            int id;
            String s_id = number.getText().toString();
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
            catch (NoIDSelectedException e)
            {
                textView.setText("You have no ID.");
            }

        }

    }
}