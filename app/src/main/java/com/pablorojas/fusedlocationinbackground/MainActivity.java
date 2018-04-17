package com.pablorojas.fusedlocationinbackground;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static com.pablorojas.fusedlocationinbackground.AppUtils.ACTION;
import static com.pablorojas.fusedlocationinbackground.AppUtils.MESSAGE;
import static com.pablorojas.fusedlocationinbackground.AppUtils.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivity extends AppCompatActivity {


    private ListView list;
    private ArrayAdapter<String> adapter;
    private List<String> locationList;
    private LocationBroadcastReceiver locationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        list = findViewById(R.id.lst_location);
        locationList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, locationList);
        list.setAdapter(adapter);

        locationBroadcastReceiver = new LocationBroadcastReceiver();

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(locationBroadcastReceiver, new IntentFilter(ACTION));


        /**
         * Runtime permissions are required on Android M and above to access User's location
         */
        if (AppUtils.hasM() && !(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            askPermissions();
        } else {
            startLocationService();
        }

    }


    public void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION){
            startLocationService();
        }
    }



    private class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (null != intent && intent.getAction().equals(ACTION)) {

                String locationData = intent.getStringExtra(MESSAGE);

                locationList.add(locationData);
                adapter.notifyDataSetChanged();
            }

        }
    }
}
