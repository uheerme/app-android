package com.caju.uheer.beta.activities;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.app.services.infrastructure.ContactablesLoaderCallbacks;

public class FriendsInOtherPlacesActivity extends ActionBarActivity {

    // Fake data, a simulation of what would we receive from the server.
    public static final String nearbyUsersString = "{\"nearbyUsers\": [" +
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

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location locationTest = new Location("test");
                // Test location in Jockey Club, Sao Carlos.
//                locationTest.setLongitude(-47.895520);
//                locationTest.setLatitude(-21.982965);
                // Test location in Sao Paulo
                locationTest.setLongitude(-46.639126);
                locationTest.setLatitude(-23.566787);
                TextView tv = (TextView)findViewById(R.id.gps_data);
                float distance = location.distanceTo(locationTest)/1000;
                tv.setText("Location: "+location.toString() + "\nDistance: "+String.format("%.1f",distance) +"Km");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Minimum of 2 minutes between checks (120000 milisecs).
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 120000, 0, locationListener);

        Bundle bundle = new Bundle();
        bundle.putString("jsonString", nearbyUsersString);
//        bundle.putString("query", "lucasolivdavid@gmail.com");

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
