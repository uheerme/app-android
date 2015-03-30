package com.caju.http_requests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by camilo on 25/03/15.
 */
public class GetChannel{

    private static final String CHANNEL_ROUTE_URL = "https://samesound.azurewebsites.net/api/Channels/";
    private String errorResponse;
    private String result;
    private int id;
    private boolean ready;

    /*
        Constructor of the class. It needs the id of the client to be fetched and an Android Context
        (system variables and information) to know about the current app instance.
     */

    GetChannel(int id, Context context)
    {
        this.id = id;
        ready = false;
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            GetChannelByIDTask get = new GetChannelByIDTask();
            get.execute(CHANNEL_ROUTE_URL + id);

        } else {
            errorResponse = "No network connection available.";
        }
    }
    /*
        This class creates a background task for the GET method.
     */
    private class GetChannelByIDTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadContent(urls[0]);
            } catch (IOException e) {
                errorResponse =  "Unable to retrieve information.";
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println("Result of the ASync Task to obtain ChannelInfo: " + result);
            ready = true;
        }
    }

    /*
        The method is the connection to the server via GET.
    */
    private String downloadContent(String URL_route) throws IOException {

        InputStream inputStream = null;

        try {
            URL url = new URL(URL_route);

            HttpClient http = new DefaultHttpClient();
            HttpResponse response = null;

            HttpGet get = new HttpGet(CHANNEL_ROUTE_URL + id);

            response = http.execute(get);

            // Starts the query
            System.out.println("The response of the GET of ChannelInfo is: " + response.getStatusLine().getStatusCode());

            // Convert the InputStream into a string
            return convertResponse(response.getEntity().getContent());

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String convertResponse(InputStream stream) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[512]; //512 is the size of the string to be fetched each time
        int retrieval_length = 0;
        String content = new String();
        while((retrieval_length = reader.read(buffer)) >= 0)
        {
            content = content + new String(buffer, 0, retrieval_length); //copy contents of buffer from 0 to retrieval_length
        }
        if(content.length() == 0)
        {
            errorResponse = "The Channel is empty or couldn't be fetched";
            return null;
        }

        return content;
    }

    public String getErrorResponse() {
        return errorResponse;
    }

    public String getResult() {
        return result;
    }

    public boolean isReady() {
        return ready;
    }

}
