package com.caju.http_requests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
public class GetChannel {

    static final String CHANNEL_ROUTE_URL = "https://samesound.azurewebsites.net/api/Channels/";
    String error_response;
    int id;


    GetChannel(int id, Context context, ConnectivityManager conn)
    {
        this.id = id;
        conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetChannelByIDTask().execute(CHANNEL_ROUTE_URL + id);
        } else {
            error_response = "No network connection available.";
        }
    }

    private class GetChannelByIDTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadContent(urls[0]);
            } catch (IOException e) {
                error_response =  "Unable to retrieve information.";
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println("Result of the ASync Task to obtain ChannelInfo: " + result);
        }
    }

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

            System.out.println(response.getEntity().getContentType().getValue());

            // Convert the InputStream into a string
            String contentAsString = readIt(response.getEntity().getContent());
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[128];
        String content = new String();
        while(reader.read(buffer) >= 0){
            System.out.println(buffer);
            content = content + new String(buffer);
            buffer = new char[128];
        }
        System.out.println("END");
        return content;
    }


}
