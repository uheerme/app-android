package com.caju.uheer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import com.caju.uheer.R;
import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.exceptions.NoIDSelectedException;
import com.caju.utils.getRequests.GetActiveChannels;
import com.caju.utils.getRequests.GetStatusNow;
import com.caju.uheer.infrastructure.interfaces.OnFailedListener;
import com.caju.uheer.infrastructure.interfaces.OnFinishedListener;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Jp on 06/05/2015.
 */
public class GameNightActivity extends ActionBarActivity implements OnFinishedListener, OnFailedListener {
    private int request;
    private GetActiveChannels getActiveChannels;
    private GetStatusNow getStatusNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_night);

        request = 1;
        // Gets the URL from the UI's text field.
        try
        {
            getActiveChannels = new GetActiveChannels(getApplicationContext());
            getActiveChannels.setOnLoadFinishedListener(this);
            getActiveChannels.setOnLoadFailedListener(this);
            getStatusNow = new GetStatusNow(getApplicationContext());
        }
        catch (NoConnectionException e)
        {
            Log.e("onCreate getChannel", "You have no connection.");
        }
        catch (NoIDSelectedException e)
        {
            Log.e("onCreate getChannel", "You have no ID.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadFinished() {
        if(request == 1){
            if(getActiveChannels.getResultResponse() != null) {
                JSONArray activeChannels = getActiveChannels.getResultResponse();
                try {
                    Log.d("onLoadFinished1", activeChannels.getJSONObject(activeChannels.length()-1).getString("Name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Log.d("onLoadFinished2", activeChannels.getJSONObject(activeChannels.length()-2).getString("Name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
                Log.e("onLoadFinished", "This shouldn't happen");
        }
    }

    @Override
    public void onLoadFailed() {
        Log.e("onLoadFailed", "Executing OnLoadFailed");
        Log.e("onLoadFailed", "Something went wrong in request " + request);
    }
}
