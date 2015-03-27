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

    }

    public void myClickHandler(View view) {
        // Gets the URL from the UI's text field.
        String stringUrl = urlText.getText().toString();
        GetChannel getChannel = new GetChannel(Integer.parseInt(stringUrl),getApplicationContext());
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
