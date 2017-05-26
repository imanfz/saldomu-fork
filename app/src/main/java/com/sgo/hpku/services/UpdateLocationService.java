package com.sgo.hpku.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sgo.hpku.coreclass.DefineValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lenovo on 05/04/2017.
 */

public class UpdateLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = true;

    private LocationRequest mLocationRequest;

    Double longitude, latitude;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //intent = new Intent(BROADCAST_ACTION);


        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("UpdateLocationIntent"));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("LOG_CONNECT_FAILED", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Log.d("LAST LONGITUDE", String.valueOf(mLastLocation.getLongitude()) );
        Log.d("LAST LATITUDE", String.valueOf(mLastLocation.getLatitude()) );

        longitude   = mLastLocation.getLongitude();
        latitude    = mLastLocation.getLatitude();

        //Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Geocoder geocoder = new Geocoder(this, new Locale("id"));

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.d("IOEXCEPTION", "IO EXCEPTION", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e("ILLEGAL", "ILLEGAL EXP" + ". " +
                    "Latitude = " + mLastLocation.getLatitude() +
                    ", Longitude = " +
                    mLastLocation.getLongitude(), illegalArgumentException);
        }


        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {

        } else {
            Address address = addresses.get(0);

            ArrayList<String> addressFragments = new ArrayList<String>();
        }

        updateLocationMessageToActivity();

        //Toast.makeText(getApplicationContext(), "Location changed! Longitude : " + String.valueOf(mLastLocation.getLongitude()) + " Latitude :" + String.valueOf(mLastLocation.getLatitude()),
        //Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {


        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                Toast.makeText(this, "GOOGLE API LOCATION CONNECTION FAILED", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        return true;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.FASTEST_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.DISPLACEMENT);
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        try {
            PendingResult<Status> statusPendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch ( SecurityException se) {
            se.printStackTrace();
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void updateLocationMessageToActivity() {
        Intent intent = new Intent("UpdateLocationIntent");
        sendLocationBroadcast(intent);
    }

    private void sendLocationBroadcast(Intent intent){
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Double currentLatitude = intent.getDoubleExtra("latitude", 0);
            Double currentLongitude = intent.getDoubleExtra("longitude", 0);



            //TextView lblLocation = (TextView) findViewById(R.id.lblLocation);
            //lblLocation.setText(currentLatitude + ", " + currentLongitude + ", "+ subAdminArea +  ", "+ adminArea + ", " + countryName);
        }
    };


    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }
}
