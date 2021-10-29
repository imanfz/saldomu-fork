package com.sgo.saldomu.services;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;


/**
 * Created by Lenovo on 05/04/2017.
 */

public class UpdateLocationService extends JobService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    static public final String TAG = "UpdateLocationService";
    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    Double longitude, latitude;
    SecurePreferences sp;
    JobParameters jobLocation;

    @Override
    public boolean onStartJob(JobParameters job) {
        Timber.d("OnStartJob Location Service");
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        jobLocation = job;

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Timber.d("OnStopJob Location Service");
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("Location Service onConnected Started");
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                Timber.d("Location Service Location Found%s", mLastLocation.toString());

                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();

                sp.edit().putDouble(DefineValue.LONGITUDE_UPDATED, longitude).apply();
                sp.edit().putDouble(DefineValue.LATITUDE_UPDATED, latitude).apply();
                sp.edit().apply();

                Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

                if (isAgent)
                    updateLocation();

                mGoogleApiClient.disconnect();
                jobFinished(jobLocation, true);

            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.tag("LOG_CONNECT_FAILED").i("Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Timber.tag("Location LAST LONGITUDE").d(String.valueOf(mLastLocation.getLongitude()));
        Timber.tag("Location LAST LATITUDE").d(String.valueOf(mLastLocation.getLatitude()));

        longitude = mLastLocation.getLongitude();
        latitude = mLastLocation.getLatitude();

        sp.edit().putDouble(DefineValue.LONGITUDE_UPDATED, longitude).apply();
        sp.edit().putDouble(DefineValue.LATITUDE_UPDATED, latitude).apply();

        boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        if (isAgent)
            updateLocation();

        mGoogleApiClient.disconnect();
        jobFinished(jobLocation, true);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {


        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                Toast.makeText(this, "GOOGLE API LOCATION CONNECTION FAILED", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DefineValue.AGENT_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.AGENT_FASTEST_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.AGENT_DISPLACEMENT);
    }

    private void updateLocation() {

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        SecurePreferences.Editor mEditor = sp.edit();
        try {
            mEditor.putDouble(DefineValue.LAST_CURRENT_LATITUDE, latitude);
            mEditor.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, longitude);
            mEditor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_LOCATION,
                "");

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.LONGITUDE, longitude);
        params.put(WebParams.LATITUDE, latitude);
        params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.ACCESS_KEY, sp.getString(DefineValue.ACCESS_KEY, ""));
        Timber.d("params location update: %s", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_LOCATION, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
