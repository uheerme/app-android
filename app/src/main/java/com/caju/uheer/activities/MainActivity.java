package com.caju.uheer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.caju.uheer.R;

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
//
//    /* This method is called after GetChannel is finished */
//    public void onLoadFinished() {
//        System.out.println("Executing OnLoadFinished");
//        /*if(lastButtonClicked == 1){
//            if(getChannel.getResultResponse() != null)
//                textView.setText(getChannel.getResultResponse());
//            else
//                textView.setText("This shouldn't happen");
//        }*/
//        if (lastButtonClicked == 1) {
//            if (getChannel.getResultResponse() != null)
//                textView.setText(getChannel.getResultResponse());
//            else
//                textView.setText("This shouldn't happen");
//        } else if (lastButtonClicked == 2) {
//            if (getChannelMusic.getResultResponse() != null)
//                textView.setText(getChannelMusic.getResultResponse());
//            else
//                textView.setText("This shouldn't happen");
//        } else if (lastButtonClicked == 3) {
//            if (postMusic.getResultResponse() != null)
//                textView.setText(postMusic.getResultResponse());
//            else
//                textView.setText("This shouldn't happen");
//        } else if (lastButtonClicked == 4) {
//            if (getMusic.getResultResponse() != null)
//                textView.setText(getMusic.getResultResponse());
//            else
//                textView.setText("This shouldn't happen");
//
//            File file = new File(getFileStreamPath(getMusic.getFilename()).getPath());
//            Uri u = Uri.fromFile(file);
//            if (mediaPlayer != null)
//                mediaPlayer.release();
//            mediaPlayer = new MediaPlayer();
//            try {
//                mediaPlayer.setDataSource(this, u);
//                mediaPlayer.prepare();
//                //mediaPlayer.seekTo(30*1000);
//                mediaPlayer.start();
//
//            } catch (IOException e) {
//                System.err.println("SONG COULD NOT BE PREPARED AND PLAYED");
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    public void onLoadFailed() {
//        System.out.println("Executing OnLoadFailed");
//        textView.setText("Something went wrong in op " + lastButtonClicked);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//
//        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
//            Uri uri = null;
//            ArrayList<String> files = new ArrayList<>();
//
//            if (resultData != null) {
//                uri = resultData.getData();
//                files.add(uri.getPath());
//            }
//
//            int id;
//            String s_id = number.getText().toString();
//            try {
//                id = Integer.parseInt(s_id);
//            } catch (NumberFormatException e) {
//                id = 0;
//            }
//
//            try {
//                postMusic = new PostMusic(getApplicationContext(), id, files);
//            } catch (NoConnectionException e) {
//                textView.setText("You have no connection.");
//            } catch (NoIDSelectedException e) {
//                textView.setText("You have no ID.");
//            }
//
//        }
//
//    }
}