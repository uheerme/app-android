package com.caju.http_requests;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.getters.GetChannel;
import com.caju.utils.getters.GetChannelMusics;
import com.caju.utils.interfaces.OnLoadFailedListener;
import com.caju.utils.interfaces.OnLoadFinishedListener;


public class MainActivity extends ActionBarActivity implements OnLoadFinishedListener, OnLoadFailedListener {

    private SharedPreferences app_settings;
    private SharedPreferences.Editor editor;

    private EditText channelNumber;
    private TextView textView;

    private GetChannel channel;
    private GetChannelMusics channelMusics;
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
    }

    @Override
    public void onLoadFailed() {
        System.out.println("Executing OnLoadFailed");
        textView.setText("Unable to retrieve information from Channel.");
    }
}