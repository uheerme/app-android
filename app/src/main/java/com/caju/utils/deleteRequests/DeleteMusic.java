package com.caju.utils.deleteRequests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.exceptions.NoIDSelectedException;
import com.caju.uheer.infrastructure.interfaces.OnFailedListener;
import com.caju.uheer.infrastructure.interfaces.OnFinishedListener;
import com.caju.uheer.infrastructure.interfaces.Routes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class DeleteMusic implements Routes {

    private String resultResponse;
    private int id;

    private OnFinishedListener onFinishedLoad;
    private OnFailedListener onFailedLoad;

    private JSONObject song;

    public DeleteMusic(int musicID, Context context) throws NoConnectionException, NoIDSelectedException
    {

        this.id = musicID;
        onFinishedLoad = null;
        onFailedLoad = null;

        if(musicID <= 0)
            throw new NoIDSelectedException();

        AsyncHttpClient client; //HTTP Client for the requests

        //get connection information
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            //starting a connection with server
            client = new AsyncHttpClient();
            client.delete(MUSICS + id, new TextHttpResponseHandler()
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
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable)
                {
                    if(statusCode == 404){
                        song = null;
                        doFinished();
                    }
                    else
                        doFailed();
                }
            });
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

    public void setOnLoadFinishedListener(OnFinishedListener listener)
    {
        onFinishedLoad = listener;
    }

    public void setOnLoadFailedListener(OnFailedListener listener)
    {
        onFailedLoad = listener;
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