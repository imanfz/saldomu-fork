package com.sgo.saldomu.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.fragments.Login;
import com.sgo.saldomu.fragments.Regist1;
import com.sgo.saldomu.widgets.BaseActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;


/*
  Created by Administrator on 11/4/2014.
 */
public class LoginActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_EXIT = 0;
    public static final int RESULT_PIN = 1;
    public static final int RESULT_NORMAL = 2;
    public static final int RESULT_FINISHING = 5;
    public static final int ACTIVITY_RESULT = 3;
    private static final int RC_GPS_REQUEST = 4;

    private FragmentManager fragmentManager;
    private SecurePreferences sp;

    Double latitude, longitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        if (InetHandler.isNetworkAvailable(this))
//            new UtilsLoader(this).getAppVersion();

            if (isHasAppPermission()) {
                if (!GlobalSetting.isLocationEnabled(this)) {
                    showAlertEnabledGPS();
                } else {
                    runningApp();
                }
            } else {
//             Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_location), BaseActivity.RC_LOCATION_PERM, perms);
            }

        if (findViewById(R.id.loginContent) != null) {
            if (savedInstanceState != null) {
                return;
            }

            String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
            if (flagLogin == null)
                flagLogin = DefineValue.STRING_NO;

            if (flagLogin.equals(DefineValue.STRING_YES)) {
                Intent i = new Intent(this, MainPage.class);
                startActivity(i);
                finish();
            }


            Fragment newFrag = new Login();

            Bundle m = getIntent().getExtras();
            if (m != null && m.containsKey(DefineValue.USER_IS_NEW)) {
                if (m.getInt(DefineValue.USER_IS_NEW, 0) == 1) {
                    newFrag = new Regist1();
                    newFrag.setArguments(m);
//                } else if (BuildConfig.DEBUG == true && BuildConfig.FLAVOR.equalsIgnoreCase( "development") && m.getInt(DefineValue.USER_IS_NEW, 0) != 0 ) { //untuk shorcut dari tombol di activity introduction
                } else if (m.getInt(DefineValue.USER_IS_NEW, 0) != 0) { //untuk shorcut dari tombol di activity introduction prod
                    if (m.getInt(DefineValue.USER_IS_NEW, 0) == -1) {
                        newFrag = new Regist1();

                    } else if (m.getInt(DefineValue.USER_IS_NEW, 0) == -2) {
                        newFrag = new Login();
                    }
                    newFrag.setArguments(m);
                }
            }

            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.loginContent, newFrag, "login");
            fragmentTransaction.commit();
        }
    }

    public void SaveImeiICCIDDevice() {
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor edit = sp.edit();
        SMSclass smSclass = new SMSclass(this);
        edit.putString(DefineValue.DEIMEI, smSclass.getDeviceAndroidId());
//        edit.putString(DefineValue.DEICCID, smSclass.getDeviceICCID());
        edit.apply();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoginActivity.ACTIVITY_RESULT) {
            if (resultCode == LoginActivity.RESULT_FINISHING)
                this.finish();
        }else if (requestCode == RC_GPS_REQUEST) {
            //if ( requestCode == Activity.RESULT_OK ) {
            if (GlobalSetting.isLocationEnabled(this)) {
                runningApp();
            }
            //}
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {

        if (isBackstack) {
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.loginContent, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        } else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.loginContent, mFragment, fragName)
                    .commit();

        }
    }

    public void switchActivity(Intent mIntent) {
        startActivityForResult(mIntent, REQUEST_EXIT);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("reg2") == null)
            super.onBackPressed();
    }


    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver) {

        if (_on) {
            Timber.wtf("masuk turnOnBR");
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            filter.addCategory("android.intent.category.DEFAULT");
            registerReceiver(_myreceiver, filter);
        } else {
            Timber.wtf("masuk turnOffBR");
            unregisterReceiver(_myreceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitService.dispose();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            if (mGoogleApiClient != null) {

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation == null) {

                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();

                    sp.edit().putDouble(DefineValue.LATITUDE_UPDATED, latitude).apply();
                    sp.edit().putDouble(DefineValue.LONGITUDE_UPDATED, longitude).apply();

                    Timber.d("GPS TEST Onconnected : Latitude : " + String.valueOf(latitude) + ", Longitude : " + String.valueOf(longitude));
                }
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        Timber.d("GPS Test Connection Failed");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("GPS Test Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            sp.edit().putDouble(DefineValue.LATITUDE_UPDATED, latitude).apply();
            sp.edit().putDouble(DefineValue.LONGITUDE_UPDATED, longitude).apply();

            Timber.d("GPS TEST OnChanged : Latitude : " + String.valueOf(latitude) + ", Longitude : " + String.valueOf(longitude));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        mLocationRequest.setInterval(2 * 8000);
        mLocationRequest.setFastestInterval(1 * 8000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(DefineValue.AGENT_DISPLACEMENT);

    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        Timber.d("GPS Test checkPlayServices : %s", String.valueOf(result));
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                Toast.makeText(this, "GOOGLE API LOCATION CONNECTION FAILED", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        return true;
    }

    private void showAlertEnabledGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> {

                    Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(ilocation, RC_GPS_REQUEST);

                })
                .setNegativeButton(R.string.no, (dialog, id) -> {
                    dialog.cancel();
                    startActivity(new Intent(getApplicationContext(), MainPage.class));
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void runningApp() {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        Timber.d("GPS Test googleapiclient : %s", mGoogleApiClient.toString());
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Timber.d("GPS Test googleapiclient connect : %s", mGoogleApiClient.toString());
        }

    }

}