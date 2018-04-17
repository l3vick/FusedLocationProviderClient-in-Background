package com.pablorojas.fusedlocationinbackground;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static com.pablorojas.fusedlocationinbackground.AppUtils.ACCURACY_THRESHOLD;
import static com.pablorojas.fusedlocationinbackground.AppUtils.ACTION;
import static com.pablorojas.fusedlocationinbackground.AppUtils.DISTANCE;
import static com.pablorojas.fusedlocationinbackground.AppUtils.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.pablorojas.fusedlocationinbackground.AppUtils.MESSAGE;
import static com.pablorojas.fusedlocationinbackground.AppUtils.UPDATE_INTERVAL_IN_MILLISECONDS;

/**
 * Created by pablorojas on 17/4/18.
 */

public class LocationService extends Service implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected GoogleApiClient googleApiClient;
    
    protected LocationRequest locationRequest;

    protected Location oldLocation;

    protected Location newLocation;

    protected Location currentLocation;
    private String timeCurrentLocation;
    
    private float distance;
    

    @Override
    public void onCreate() {
        super.onCreate();
        oldLocation = new Location("Point A");
        newLocation = new Location("Point B");
        timeCurrentLocation  = "";
        distance = PreferenceManager.getDefaultSharedPreferences(this).getFloat(DISTANCE, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buildGoogleApiClient();

        googleApiClient.connect();

        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }

        return START_STICKY;

    }

    protected void startLocationUpdates() {
        try {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);

        } catch (SecurityException ex) {


        }
    }

    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    protected void createLocationRequest() {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateUI() {

        if (null != currentLocation) {

            StringBuilder locationData = new StringBuilder();
            locationData
                    .append("Latitude: " + currentLocation.getLatitude())
                    .append("\n")
                    .append("Longitude: " + currentLocation.getLongitude())
                    .append("\n")
                    .append("Time: " + timeCurrentLocation)
                    .append("\n")
                    .append(getUpdatedDistance())
                    .append(" meters");


            PreferenceManager.getDefaultSharedPreferences(this).edit().putFloat(DISTANCE, distance).apply();

            Log.d(TAG, "Location Data:\n" + locationData.toString());

            sendLocationBroadcast(locationData.toString());

        } else {

            Toast.makeText(this, R.string.unable_to_find_location, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendLocationBroadcast(String locationData) {

        Intent locationIntent = new Intent();
        locationIntent.setAction(ACTION);
        locationIntent.putExtra(MESSAGE, locationData);

        LocalBroadcastManager.getInstance(this).sendBroadcast(locationIntent);

    }


    protected void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }


    @Override
    public void onDestroy() {

        PreferenceManager.getDefaultSharedPreferences(this).edit().putFloat(DISTANCE, distance).apply();

        stopLocationUpdates();

        googleApiClient.disconnect();

        Log.d(TAG, "onDestroy Distance " + distance);


        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle connectionHint) throws SecurityException {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (currentLocation == null) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            timeCurrentLocation = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        startLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        timeCurrentLocation = DateFormat.getTimeInstance().format(new Date());
        updateUI();

    }

    @Override
    public void onConnectionSuspended(int cause) {

        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private float getUpdatedDistance() {


        if (currentLocation.getAccuracy() > ACCURACY_THRESHOLD) {

            return distance;
        }


        if (oldLocation.getLatitude() == 0 && oldLocation.getLongitude() == 0) {

            oldLocation.setLatitude(currentLocation.getLatitude());
            oldLocation.setLongitude(currentLocation.getLongitude());

            newLocation.setLatitude(currentLocation.getLatitude());
            newLocation.setLongitude(currentLocation.getLongitude());

            return distance;
        } else {

            oldLocation.setLatitude(newLocation.getLatitude());
            oldLocation.setLongitude(newLocation.getLongitude());

            newLocation.setLatitude(currentLocation.getLatitude());
            newLocation.setLongitude(currentLocation.getLongitude());

        }

        distance += newLocation.distanceTo(oldLocation);

        return distance;
    }


}
