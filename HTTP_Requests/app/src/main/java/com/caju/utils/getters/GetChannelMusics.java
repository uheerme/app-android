package com.caju.utils.getters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.caju.utils.exceptions.NoConnectionException;
import com.caju.utils.interfaces.Getter;
import com.caju.utils.interfaces.OnLoadFailedListener;
import com.caju.utils.interfaces.OnLoadFinishedListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;

/**
 * Created by camilo on 01/04/15.
 */

public class GetChannelMusics implements Getter {

    private String resultResponse;
    private int id;
    private boolean success;

    private OnLoadFinishedListener onLoad;
    private OnLoadFailedListener onFail;

    /*
        Constructor of the class. It needs the id of the channel to be fetched and an Android Context
        (system variables and information) to know about the current app instance.
     */

    public GetChannelMusics (int channelID, Context context) throws NoConnectionException {
        this.id = channelID;
        onLoad = null;
        onFail = null;

        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            GetChannelMusicsTask get = new GetChannelMusicsTask();
            get.execute(CHANNEL_ROUTE + channelID + MUSIC_SUB_ROUTE);

        } else
        {
            throw new NoConnectionException("No network connection available.");
        }
    }

    /*
        This class creates a background task for the GET method.
     */
    private class GetChannelMusicsTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            // params comes from the execute() call: params[0] is the url.
            try
            {
                success = true;
                return downloadContent(urls[0]);
            }
            catch (IOException e)
            {
                success = false;
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result)
        {
            System.out.println("Result of the ASync Task to obtain ChannelInfo: " + result);
            resultResponse = result;
            if(success)
                doLoadFinished();
            else
                doLoadFailed();
        }
    }

    /*
        The method is the connection to the server via GET.
    */
    private String downloadContent(String route) throws IOException
    {
        System.out.println("Getting URL content");

        URL url = new URL(route);

        HttpClient http = new DefaultHttpClient();

        HttpResponse response;
        HttpGet get = new HttpGet(route);
        response = http.execute(get);
        // Starts the query
        System.out.println("The response of the GET of ChannelInfo is: " + response.getStatusLine().getStatusCode());

        // Convert the InputStream into a string
        return convertResponse(response.getEntity().getContent());
    }

    // Reads an InputStream and converts it to a String.
    public String convertResponse(InputStream stream) throws IOException
    {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[512]; //512 is the size of the string to be fetched each time
        int retrieval_length = 0;
        String content = new String();

        while ((retrieval_length = reader.read(buffer)) >= 0)
        {
            content = content + new String(buffer, 0, retrieval_length); //copy contents of buffer from 0 to retrieval_length
        }

        if (content.length() == 0)
        {
            throw new IOException("The Channel is empty or couldn't be fetched");
        }

        return content;
    }

    public String getResultResponse()
    {
        return resultResponse;
    }

    public void setOnLoadFinishedListener(OnLoadFinishedListener listener)
    {
        onLoad = listener;
    }

    public void setOnLoadFailedListener(OnLoadFailedListener listener)
    {
        onFail = listener;
    }

    private void doLoadFinished()
    {
        if (onLoad != null)
            onLoad.onLoadFinished();
    }

    private void doLoadFailed()
    {
        if (onFail != null)
            onFail.onLoadFailed();
    }

}
