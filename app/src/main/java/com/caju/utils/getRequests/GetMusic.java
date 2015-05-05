package com.caju.utils.getRequests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.exceptions.NoIDSelectedException;
import com.caju.utils.interfaces.OnFailedListener;
import com.caju.utils.interfaces.OnFinishedListener;
import com.caju.utils.interfaces.Routes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetMusic implements Routes {

    private Context context;

    private byte[] binaryResponse;
    private String resultResponse;
    private int id;

    private OnFinishedListener onFinishedLoad;
    private OnFailedListener onFailedLoad;

    private JSONObject song;
    private boolean song_is_downloaded;
    private String filename;

    public GetMusic(int musicID, Context context) throws NoConnectionException, IOException, NoIDSelectedException
    {
        this.id = musicID;
        this.context = context;
        onFinishedLoad = null;
        onFailedLoad = null;

        if(musicID <= 0)
            throw new NoIDSelectedException();

        song_is_downloaded = (new File(context.getFileStreamPath(id + ".mp3").getPath()).exists());


        AsyncHttpClient client; //HTTP Client for the requests

        //get connection information
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            //starting a connection with server
            client = new AsyncHttpClient();
            client.get(MUSIC_ROUTE + id, new TextHttpResponseHandler()
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
                        } catch (JSONException e)
                        {
                            song = null;
                            System.err.println(response);
                        }

                        if(song_is_downloaded)
                            doFinished();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable)
                {
                    doFailed();
                }

            });
            if(song_is_downloaded){
                filename = id + ".mp3";
                System.out.println(filename);
            }
            else
            {
                client.get(MUSIC_ROUTE + id + STREAM_END_ROUTE, new AsyncHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response)
                    {
                        if(statusCode == 200)
                        {
                            binaryResponse = response;
                            doPartialFinished();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable throwable)
                    {
                        doFailed();
                    }

                });
            }

        }
        else
        {
            throw new NoConnectionException("No network connection available.");
        }
    }

    public String getResultResponse()
    {
        return resultResponse;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setOnLoadFinishedListener(OnFinishedListener listener)
    {
        onFinishedLoad = listener;
    }

    public void setOnLoadFailedListener(OnFailedListener listener)
    {
        onFailedLoad = listener;
    }


    private void doPartialFinished()
    {
        if(song == null)
            doFailed();
        try
        {
            FileOutputStream fos = context.openFileOutput(song.getString("Id") + ".mp3", Context.MODE_PRIVATE);
            fos.write(binaryResponse);
            fos.close();
            filename = song.getString("Id") + ".mp3";
            System.out.println(filename);

        } catch (FileNotFoundException e)
        {
            System.err.println("COULD NOT CREATE DOWNLOADED FILE");
            e.printStackTrace();
            doFailed();
        } catch (JSONException e)
        {
            System.err.println("JSON OBJECT DOESN'T HAVE ID PROPERTY");
            e.printStackTrace();
            doFailed();
        } catch (IOException e)
        {
            System.err.println("COULD NOT CREATE NEW FILE IN THE FOLDER");
            e.printStackTrace();
            doFailed();
        }

        doFinished();
    }

    private void doFinished()
    {
        if (onFinishedLoad != null)
            onFinishedLoad.onLoadFinished();
    }

    private void doFailed()
    {
        if (onFailedLoad != null)
            onFailedLoad.onLoadFailed();
    }

}