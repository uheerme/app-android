package com.caju.uheer.debug.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.caju.uheer.R;
import com.caju.uheer.beta.activities.FriendsHereActivity;
import com.caju.uheer.beta.activities.FriendsInOtherPlacesActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//    private SharedPreferences app_settings;
//    private SharedPreferences.Editor editor;
//
//    private EditText number;
//    private TextView textView;
//
//    private int lastButtonClicked;
//
//    private GetStatusNow getStatusNow;
//    private GetChannel getChannel;
//    private GetChannelMusic getChannelMusic;
//    private GetMusic getMusic;
//    private PostMusic postMusic;
//
//    private MediaPlayer mediaPlayer;

//    /* When user clicks in the button*/
//    public void getChannel(View view) {
//        lastButtonClicked = 1;
//        // Gets the URL from the UI's text field.
//        int id;
//        String s_id = number.getText().toString();
//        try {
//            id = Integer.parseInt(s_id);
//        } catch (NumberFormatException e) {
//            id = 0;
//        }
//
//        try {
//            getChannel = new GetChannel(id, getApplicationContext());
//            getStatusNow = new GetStatusNow(getApplicationContext());
//        } catch (NoConnectionException e) {
//            textView.setText("You have no connection.");
//        } catch (NoIDSelectedException e) {
//            textView.setText("You have no ID.");
//        }
//    }
//
//    public void getChannelMusics(View view) {
//        lastButtonClicked = 2;
//        // Gets the URL from the UI's text field.
//        int id;
//        String s_id = number.getText().toString();
//        try {
//            id = Integer.parseInt(s_id);
//        } catch (NumberFormatException e) {
//            id = 0;
//        }
//
//        try {
//            getChannelMusic = new GetChannelMusic(id, getApplicationContext());
//        } catch (NoConnectionException e) {
//            textView.setText("You have no connection.");
//        } catch (NoIDSelectedException e) {
//            textView.setText("You have no ID.");
//        }
//    }
//
//    public void postMusic(View view) {
//        lastButtonClicked = 3;
//        Intent intent;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//        } else {
//            intent = new Intent(Intent.ACTION_PICK);
//        }
//        intent.setType("audio/*");
//        startActivityForResult(intent, 10);
//    }
//
//    public void getMusic(View view) {
//        lastButtonClicked = 4;
//        // Gets the URL from the UI's text field.
//        int id;
//        String s_id = number.getText().toString();
//        try {
//            id = Integer.parseInt(s_id);
//        } catch (NumberFormatException e) {
//            id = 0;
//        }
//
//        try {
//            getMusic = new GetMusic(id, getApplicationContext());
//        } catch (NoConnectionException e) {
//            textView.setText("You have no connection.");
//        } catch (IOException e) {
//            textView.setText("File not loaded.");
//            e.printStackTrace();
//        } catch (NoIDSelectedException e) {
//            textView.setText("You have no ID.");
//        }
//    }
//
    public void gameNightTest(View view) {
        Intent intent = new Intent(this, GameNightActivity.class);
        startActivity(intent);
    }

    public void goToChannelsActivity(View view) {
        startActivity(new Intent(this, ChannelsActivity.class));
    }

    public void goToFriendsHereActivity(View view) {
        startActivity(new Intent(this, FriendsHereActivity.class));
    }

    public void goToFriendsInOtherPlaces(View view) {
        startActivity(new Intent(this, FriendsInOtherPlacesActivity.class));
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
}