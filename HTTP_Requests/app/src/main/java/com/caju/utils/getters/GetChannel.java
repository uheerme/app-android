package com.caju.utils.getters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.interfaces.OnFailedListener;
import com.caju.utils.interfaces.OnFinishedListener;
import com.caju.utils.interfaces.Routes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by camilo on 01/04/15.
 */

public class GetChannel implements Routes {

    private String resultResponse;
    private int id;

    private OnFinishedListener onFinishedLoad;
    private OnFailedListener onFailedLoad;

    private JSONObject channel;

    public GetChannel(int channelID, Context context) throws NoConnectionException {

        this.id = channelID;
        onFinishedLoad = null;
        onFailedLoad = null;

        AsyncHttpClient client; //HTTP Client for the requests

        //get connection information
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            //starting a connection with server
            client = new AsyncHttpClient();
            client.get(CHANNEL_ROUTE + id, new TextHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String response)
                {
                    if(statusCode == 200)
                    {
                        resultResponse = new String(response);
                        try
                        {
                            channel = new JSONObject(response);
                        } catch (JSONException e)
                        {
                            channel = null;
                            System.err.println(response);
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable)
                {
                    errorResponse = new String(errorResponse);
                    doFailed();
                }

                @Override
                public void onFinish()
                {
                    if(channel != null)
                        doFinished();
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