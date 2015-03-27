package com.caju.http_requests;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private SharedPreferences app_settings;
    private SharedPreferences.Editor editor;

    private EditText urlText;
    private EditText contentText;
    private Spinner type_of_request;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app_settings = getSharedPreferences(
                getString(R.string.app_settings_pref_file), Context.MODE_PRIVATE);

        //Initiating app_settings if not defined
        editor = app_settings.edit();
        if(!app_settings.contains(getString(R.string.request_method))){
            editor.putString(getString(R.string.request_method),"GET");
        }
        editor.commit();

        //Finding important elements in the layout
        urlText = (EditText) findViewById(R.id.URL_to_be_requested);
        contentText = (EditText) findViewById(R.id.method_parameters);
        type_of_request = (Spinner) findViewById(R.id.http_request);
        textView = (TextView) findViewById(R.id.request_container);

        // Updating Spinner element options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.request_method_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_of_request.setAdapter(adapter);
        String type = app_settings.getString(getString(R.string.request_method),"GET");
        type_of_request.setSelection(getCurrentTypeRequest());
        type_of_request.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = (String) type_of_request.getItemAtPosition(position);
                editor = app_settings.edit();
                editor.putString(getString(R.string.request_method), type);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler(View view) {
        // Gets the URL from the UI's text field.
        String stringUrl = urlText.getText().toString();
        System.out.println(stringUrl);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve information.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {

        InputStream is = null;

        try {
            URL url = new URL(myurl);

            HttpClient http = new DefaultHttpClient();
            HttpResponse response = null;

            String type = app_settings.getString(getString(R.string.request_method), "GET");

            if(type.compareTo("POST") == 0){
                HttpPost post = new HttpPost(urlText.getText().toString());
                post.addHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(contentText.getText().toString()));
                response = http.execute(post);
            }else if(type.compareTo("GET") == 0){
                HttpGet get = new HttpGet(urlText.getText().toString());
                response = http.execute(get);
            }


            // Starts the query

            System.out.println("The response is: " + response.getStatusLine().getStatusCode());

            System.out.println(response.getEntity().getContentType().getValue());

            // Convert the InputStream into a string
            String contentAsString = readIt(response.getEntity().getContent());
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[128];
        String content = new String();
        System.out.println("Teste2");
        while(reader.read(buffer) >= 0){
            System.out.println(buffer);
            content = content + new String(buffer);
            buffer = new char[128];
        }
        System.out.println("END");
        return content;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int getCurrentTypeRequest(){
        String[] types = getResources().getStringArray(R.array.request_method_list);
        String current_type = app_settings.getString(getString(R.string.request_method), "GET");
        for(int i= 0; i < types.length; i++){
            if(types[i].compareTo(current_type) == 0)
                return i;
        }
        return -1;
    }
}
