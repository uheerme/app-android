package com.caju.uheer.beta.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.caju.uheer.R;
import com.caju.uheer.app.services.infrastructure.ContactablesLoaderCallbacks;

public class FriendsInOtherPlacesActivity extends ActionBarActivity {

    // Fake data, a simulation of what would we receive from the server.
    public static final String nearbyUsersString = "{\"nearbyUsers\": [" +
            "{" +
                "\"email\": \"flaviafernanda_moraes@hotmail.com\", " +
                "\"geoPoint\": {\"latitude\": -22.002899, \"longitude\": -47.893118} " +
            "}," +
            "{" +
                "\"email\": \"cesarteixeira@gmail.com\", " +
                "\"geoPoint\": {\"latitude\": -21.985047, \"longitude\": -47.882528} " +
            "}," +
            "{" +
                "\"email\": \"lucasolivdavid@gmail.com\", " +
                "\"geoPoint\": {\"latitude\": -22.000540, \"longitude\": -47.899306} " +
            "}," +
            "{" +
                "\"email\": \"cristiadu@gmail.com\", " +
                "\"geoPoint\": {\"latitude\": -22.904066, \"longitude\": -47.099372} " +
            "}," +
            "{" +
                "\"email\": \"francielledemattos@gmail.com\", " +
                "\"geoPoint\": {\"latitude\": -21.979670, \"longitude\": -47.880059} " +
            "}," +
            "{" +
                "\"email\": \"felipe_reis@dc.ufscar.br\", " +
                "\"geoPoint\": {\"latitude\": -15.923298, \"longitude\": -47.806466} " +
            "}," +
            "{" +
                "\"email\": \"lucasolivdavid@gmail.com\", " +
                "\"geoPoint\": {\"latitude\": -22.000540, \"longitude\": -47.899306} " +
            "}," +
            "{" +
                "\"email\": \"thamenato@gmail.com\", " +
                "\"geoPoint\": {\"latitude\": -39.129986, \"longitude\": -77.093338} " +
            "}" +
            "]}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_in_other_places);

        Bundle bundle = new Bundle();
        bundle.putString("jsonString", nearbyUsersString);

        ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(this);

        getLoaderManager().restartLoader(0, bundle, loaderCallbacks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends_in_other_places, menu);
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
}
