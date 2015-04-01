package com.caju.http_requests;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caju.utils.com.caju.utils.getters.GetChannel;


public class MainActivity extends ActionBarActivity implements OnLoadFinishedListener {

    private SharedPreferences app_settings;
    private SharedPreferences.Editor editor;

    private EditText channelNumber;
    private TextView textView;
    private ProgressBar loadingChannel;

    GetChannel getChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*app_settings = getSharedPreferences(
                getString(R.string.app_settings_pref_file), Context.MODE_PRIVATE);

        //Initiating app_settings if not defined
        editor = app_settings.edit();
        if(!app_settings.contains(getString(R.string.request_method))){
            editor.putString(getString(R.string.request_method),"GET");
        }
        editor.commit();*/

        //Finding important elements in the layout
        channelNumber = (EditText) findViewById(R.id.channel_id);
        textView = (TextView) findViewById(R.id.result_container);
        loadingChannel = (ProgressBar) findViewById(R.id.progressBar1);
        loadingChannel.setMax(100);
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler(View view) {
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

        getChannel = new GetChannel(id, getApplicationContext());
        getChannel.setOnLoadFinishedListener(this);
        System.out.println("Constructor Finished");

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
        if(getChannel.getResult() != null)
            textView.setText(getChannel.getResult());
        else
            textView.setText(getChannel.getErrorResponse());
    }
}