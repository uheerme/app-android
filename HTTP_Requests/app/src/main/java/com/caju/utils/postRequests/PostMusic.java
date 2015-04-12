package com.caju.utils.postRequests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.interfaces.OnFailedListener;
import com.caju.utils.interfaces.OnFinishedListener;
import com.caju.utils.interfaces.Routes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PostMusic implements Routes
{
    private String resultResponse;

    private ArrayList<File> uploaded;
    private ArrayList<File> unuploaded;
    private ArrayList<JSONObject> song_JSONs;
    private ArrayList<Integer> song_IDs;

    private OnFinishedListener onFinishedUpload;
    private OnFailedListener onFailedUpload;

    private JSONObject music;

    public PostMusic(Context context, int channelID, ArrayList<String> song_paths) throws NoConnectionException
    {

        onFinishedUpload = null;
        onFailedUpload = null;

        uploaded = new ArrayList<File>();
        unuploaded = new ArrayList<File>();
        song_JSONs = new ArrayList<JSONObject>();
        song_IDs = new ArrayList<Integer>();

        File song_file;
        RequestParams http_params = new RequestParams();
        AsyncHttpClient client; //HTTP Client for the requests

        for(String s : song_paths)
        {
            //get Substring with "useful part" of the filepath
            int index = s.lastIndexOf("/emulated/");
            s = s.substring(index + "/emulated/0/".length());
            //get file pointer
            song_file = new File(Environment.getExternalStorageDirectory(), s);

            if(song_file == null || !song_file.exists() || !song_file.isFile())
                unuploaded.add(song_file);
            else
                uploaded.add(song_file);
        }

        //get connection information
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
        {
            for(File f : uploaded)
            {
                http_params.remove("Name");
                http_params.remove("ChannelID");
                http_params.remove("Music");

                http_params.put("Name", f.getName());
                http_params.put("ChannelID", channelID);
                try
                {
                    http_params.put("Music", f, "audio/mpeg");
                } catch (FileNotFoundException e)
                {
                    uploaded.remove(f);
                    unuploaded.add(f);
                    continue;
                }

                //starting a connection with server
                client = new AsyncHttpClient();
                client.post(MUSIC_ROUTE, http_params, new TextHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response)
                    {
                        if(statusCode == 200)
                        {
                            resultResponse = new String(response);
                            try
                            {
                                music = new JSONObject(response);
                                song_JSONs.add(music);
                                song_IDs.add(music.getInt("Id"));
                            } catch (JSONException e)
                            {
                                System.err.println(response);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable)
                    {
                        doFailed();
                    }

                    @Override
                    public void onFinish()
                    {
                        if(!uploaded.isEmpty())
                            doFinished();
                        else
                            doFailed();
                    }

                });
            }
        } else
        {
            throw new NoConnectionException("No network connection available.");
        }
        System.out.println("Constructor PostMusic Finished");
    }

    public String getResultResponse()
    {
        return resultResponse;
    }

    public void setOnLoadFinishedListener(OnFinishedListener listener)
    {
        onFinishedUpload = listener;
    }

    public void setOnLoadFailedListener(OnFailedListener listener)
    {
        onFailedUpload = listener;
    }

    private void doFinished()
    {
        System.out.println(resultResponse);
        if(onFinishedUpload != null)
            onFinishedUpload.onLoadFinished();
    }

    private void doFailed()
    {
        if(onFailedUpload != null)
            onFailedUpload.onLoadFailed();
    }
}
