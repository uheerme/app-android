package com.caju.uheer.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.caju.uheer.R;

public class GameNightActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_night);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

//    public void onLoadFinished() {
//        if(request == 1){
//            if(getActiveChannels.getResultResponse() != null) {
//                JSONArray activeChannels = getActiveChannels.getResultResponse();
//                try {
//                    Log.d("onLoadFinished1", activeChannels.getJSONObject(activeChannels.length()-1).getString("Name"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    Log.d("onLoadFinished2", activeChannels.getJSONObject(activeChannels.length()-2).getString("Name"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            else
//                Log.e("onLoadFinished", "This shouldn't happen");
//        }
//    }
//
//    public void onLoadFailed() {
//        Log.e("onLoadFailed", "Executing OnLoadFailed");
//        Log.e("onLoadFailed", "Something went wrong in request " + request);
//    }
}
