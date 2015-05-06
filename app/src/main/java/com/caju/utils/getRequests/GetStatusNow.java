package com.caju.utils.getRequests;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GetStatusNow implements Routes {

    private String resultResponse;

    private Context context;
    private NetworkInfo networkInfo;
    private AsyncHttpClient client;

    private OnFinishedListener onFinishedLoad;
    private OnFailedListener onFailedLoad;

    private JSONObject now;
    private Date server_time;
    private Date client_time;

    public GetStatusNow(Context context) throws NoConnectionException {

        this.context = context;
        onFinishedLoad = null;
        onFailedLoad = null;

        updateNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            //starting a connection with server
            client = new AsyncHttpClient();
            client.get(STATUS + NOW, new TextHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String response)
                {
                    if(statusCode == 200)
                    {
                        resultResponse = new String(response);
                        System.out.println("Server Response:" + response);
                        try
                        {
                            now = new JSONObject(response);
                            server_time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZZZZZ", Locale.US).parse((now.getString("Now")));
                            System.out.println("Parsed Response: " + server_time.getTime());
                        } catch (JSONException e)
                        {
                            now = null;
                            System.err.println(response);
                        } catch (ParseException e)
                        {
                            System.out.println("DATE PARSING ERROR");
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable)
                {
                    doFailed();
                }
            });
        }
        else
        {
            throw new NoConnectionException("No network connection available.");
        }
    }

    private void updateNetworkInfo()
    {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = conn.getActiveNetworkInfo();
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