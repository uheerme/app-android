/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.caju.uheer.app.services.infrastructure;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.widget.TextView;

import com.caju.uheer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class to handle all the callbacks that occur when interacting with loaders.  Most of the
 * interesting code in this sample app will be in this file.
 */
public class ContactablesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    Context mContext;

    public static final String QUERY_KEY = "query";

    public static final String TAG = "ContactLoaderCallbacks";

    JSONArray usersFound = new JSONArray();
//    HashMap<String, double[]> usersFound = new HashMap<String, double[]>();

    public ContactablesLoaderCallbacks(Context context) {
        mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderIndex, Bundle args) {
        String query = "";
        JSONArray nearbyUsers = new JSONArray();

        Uri uri = Uri.withAppendedPath(
                CommonDataKinds.Contactables.CONTENT_FILTER_URI, query);

        // Sort results such that rows for the same contact stay together.
        String sortBy = CommonDataKinds.Contactables.LOOKUP_KEY;

        CursorLoader cursorLoader = new CursorLoader(
                mContext,  // Context
                uri,       // URI representing the table/resource to be queried
                null,      // projection - the list of columns to return.  Null means "all"
                null,      // selection - Which rows to return (condition rows must match)
                null,      // selection args - can be provided separately and subbed into selection.
                sortBy);   // string specifying sort order

        try {
            // Parsing the JSON received from server.
            String nearbyUsersString = args.getString("jsonString");
            nearbyUsers = new JSONObject(nearbyUsersString).getJSONArray("nearbyUsers");
        }catch (JSONException e) {e.printStackTrace();}

        for(int i=0; i<nearbyUsers.length(); i++) {
//            Log.d("json", "" + nearbyUsers.getJSONObject(0).get("email"));
            try {
                query = nearbyUsers.getJSONObject(i).getString("email");
            } catch (JSONException e) { e.printStackTrace(); }
//            query = args.getString("query");

            uri = Uri.withAppendedPath(
                    CommonDataKinds.Contactables.CONTENT_FILTER_URI, query);

            cursorLoader = new CursorLoader(
                    mContext,  // Context
                    uri,       // URI representing the table/resource to be queried
                    null,      // projection - the list of columns to return.  Null means "all"
                    null,      // selection - Which rows to return (condition rows must match)
                    null,      // selection args - can be provided separately and subbed into selection.
                    sortBy);   // string specifying sort order

            Cursor cursor = cursorLoader.loadInBackground();

            if (cursor.getCount() == 0) {
                return cursorLoader;
            }

            int nameColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.DISPLAY_NAME);
            int lookupColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.LOOKUP_KEY);

            cursor.moveToFirst();

            String lookupKey = "";
            String displayName = "";
            do {
                String currentLookupKey = cursor.getString(lookupColumnIndex);
                if (!lookupKey.equals(currentLookupKey)) {
                    displayName = cursor.getString(nameColumnIndex);
                    lookupKey = currentLookupKey;
                }
            } while (cursor.moveToNext());

            try {
                double[] geoPoint = new double[2];
                query = nearbyUsers.getJSONObject(i).getString("email");
                geoPoint[0] = nearbyUsers.getJSONObject(i).getJSONObject("geoPoint").getDouble("latitude");
                geoPoint[1] = nearbyUsers.getJSONObject(i).getJSONObject("geoPoint").getDouble("longitude");

                JSONObject jobj = new JSONObject().put("name", displayName);
                jobj.put("lat", geoPoint[0]);
                jobj.put("lon", geoPoint[1]);
                usersFound.put(jobj);
                //            usersFound.put("Lucas", geoPoint);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        final TextView tv  = (TextView) ((Activity)mContext).findViewById(R.id.contact);
        if(tv == null) {
            Log.e(TAG, "TextView is null?!");
        } else if (mContext == null) {
            Log.e(TAG, "Context is null?");
        } else {
            Log.e(TAG, "Nothing is null?!");
        }

        try {
            for(int i=0; i<usersFound.length(); i++) {
                tv.append(usersFound.getJSONObject(i).getString("name") + "\n");
            }
        }catch (JSONException e) {e.printStackTrace();}

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location geoPointLocation = new Location("geoPoint");
                try {
                    for(int i=0; i<usersFound.length(); i++) {
                        geoPointLocation.setLongitude(usersFound.getJSONObject(i).getDouble("lon") );
                        geoPointLocation.setLatitude(usersFound.getJSONObject(i).getDouble("lat"));
                        float distance = location.distanceTo(geoPointLocation)/1000;
                        tv.setText(usersFound.getJSONObject(i).getString("name") + "\t" + String.format("%.1f",distance) + "Km" + "\n");
                    }
                }catch (JSONException e) {e.printStackTrace();}
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}