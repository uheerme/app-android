package com.caju.utils.postRequests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.exceptions.NoIDSelectedException;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PostMusic implements Routes
{
    private String resultResponse;

    private Context context;

    private File song_file;
    private ArrayList<File> uploaded;
    private ArrayList<File> unuploaded;
    private ArrayList<JSONObject> song_JSONs;
    private ArrayList<Integer> song_IDs;

    private OnFinishedListener onFinishedUpload;
    private OnFailedListener onFailedUpload;

    private JSONObject song;

    public PostMusic(Context context, int channelID, ArrayList<String> song_paths) throws NoConnectionException, NoIDSelectedException
    {
        this.context = context;
        onFinishedUpload = null;
        onFailedUpload = null;

        if(channelID <= 0)
            throw new NoIDSelectedException();

        uploaded = new ArrayList<File>();
        unuploaded = new ArrayList<File>();
        song_JSONs = new ArrayList<JSONObject>();
        song_IDs = new ArrayList<Integer>();


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
                    song_file = f; //Copy file reference if works to be used inside get method
                } catch (FileNotFoundException e)
                {
                    uploaded.remove(f);
                    unuploaded.add(f);
                    System.err.println("FILE TO BE UPLOADED NOT FOUND");
                    continue;
                }

                //starting a connection with server
                client = new AsyncHttpClient();
                client.post(MUSICS, http_params, new TextHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response)
                    {
                        if(statusCode == 200)
                        {
                            resultResponse = new String(response);
                            try
                            {
                                song = new JSONObject(response);
                                song_JSONs.add(song);
                                song_IDs.add(song.getInt("Id"));
                                doCopyFileToInternalStorage();

                            } catch (JSONException e)
                            {
                                System.err.println("ERROR WHEN PARSING JSON");
                                e.printStackTrace();
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

    public void doCopyFileToInternalStorage()
    {
        try
        {
            //copying file from external to internal storage
            FileOutputStream fos = context.openFileOutput(song.getString("Id") + ".mp3", Context.MODE_PRIVATE);
            FileInputStream fis = new FileInputStream(song_file);
            while(fis.available() > 0)
            {
                try { fos.write(fis.read()); }
                catch (IOException e)
                {
                    System.err.println("FILE COULD NOT BE COPIED");
                    e.printStackTrace();
                }
            }
            fos.close();
            fis.close();
        }
        catch (JSONException e)
        {
            System.err.println("ERROR WHEN PARSING JSON");
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("FILE TO BE COPIED TO INTERNAL STORAGE NOT FOUND");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.err.println("FILE NOT COPIED PROPERLY");
            e.printStackTrace();
        }
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
