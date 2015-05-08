package com.caju.utils.getRequests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.caju.uheer.core.Channel;
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
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public List<Channel> getResultResponse() {
        List<Channel> activeChannels = new ArrayList<Channel>();
        for(int i=0; i<activeChannel.length(); i++){
            try {
                Channel channel = new Channel();
                JSONObject channelJson = activeChannel.getJSONObject(i);
                channel.Id = channelJson.getInt("Id");
                channel.Name = channelJson.getString("Name");
                channel.Owner = channelJson.getString("Owner");
                channel.HostIpAddress = channelJson.getString("HostIpAddress");
                channel.HostMacAddress = channelJson.getString("HostMacAddress");
                channel.Loops = channelJson.getBoolean("Loops");
                channel.CurrentId = channelJson.getInt("CurrentId");
                channel.CurrentStartTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS",
                 Locale.US).parse(channelJson.getString("CurrentStartTime"));
                channel.Musics = null;
                channel.DateCreated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS",
                 Locale.US).parse(channelJson.getString("DateCreated"));
                channel.DateUpdated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS",
                 Locale.US).parse(channelJson.getString("DateUpdated"));
                channel.DateDeactivated = null; // Assuming that active channels don't have a deactivated date.
                activeChannels.add(channel);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        return activeChannels;
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