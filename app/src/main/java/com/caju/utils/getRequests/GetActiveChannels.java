package com.caju.utils.getRequests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.caju.uheer.infrastructure.interfaces.OnFailedListener;
import com.caju.uheer.infrastructure.interfaces.OnFinishedListener;
import com.caju.uheer.infrastructure.interfaces.Routes;
import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.exceptions.NoIDSelectedException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

public class GetActiveChannels {

    private String resultResponse;

    private OnFinishedListener onFinishedLoad;
    private OnFailedListener onFailedLoad;

    private JSONArray activeChannel;

    public GetActiveChannels(Context context) throws NoConnectionException, NoIDSelectedException
    {

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
            client.get(Routes.CHANNELS + Routes.ACTIVE, new TextHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String response)
                {
                    if(statusCode == 200)
                    {
                        resultResponse = new String(response);
                        try
                        {
                            activeChannel = new JSONArray(response);
                        } catch (JSONException e)
                        {
                            activeChannel = null;
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
                    if(activeChannel != null)
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



    public JSONArray getResultResponse() { return activeChannel; }


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