package com.sgo.hpku.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.MainAgentActivity;
import com.sgo.hpku.adapter.GooglePlacesAutoCompleteArrayAdapter;
import com.sgo.hpku.coreclass.AgentConstant;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CustomAutoCompleteTextView;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.DefinedDialog;

import com.sgo.hpku.entityRealm.AgentDetail;
import com.sgo.hpku.entityRealm.AgentServiceDetail;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class SearchAgentActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener,
        //View.OnKeyListener,
        EditText.OnEditorActionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {
    private final int PRIORITY_LOCATION_REQUEST = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private final long INTERVAL_LOCATION_REQUEST = 1000;
    private final long FASTEST_INTERVAL_LOCATION_REQUEST = 2;

    private final int RESOLUTION_RESULT = 0;
    private final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    private GoogleApiClient googleApiClient;

    private Button requestCashInBtn;
    //EditText searchLocationEditText;
    //private CustomEditText searchLocationEditText;
    private CustomAutoCompleteTextView searchLocationEditText;
    private RadioButton currentLocationRadioBtn;
    private RadioButton searchLocationRadioBtn;
    private RadioGroup locationRadioGroup;
    private ImageView deleteBtn;

    public Boolean isServiceCompleted  = false;
    public Boolean isInProgress        = false;

    SecurePreferences sp;
    ProgressDialog progdialog;
    public Boolean isKeepSearching = true;
    Location location;
    final Bundle bundle = new Bundle();

    Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        /*Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());*/

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        initializeToolbar();

        process();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.request_search_agent_activity;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if (id == android.R.id.home) {
            //kembali ke activity sebelumnya
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle("Cari Agen");
    }

    private void process() {
        //jika google play service tersedia, maka :
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        deleteBtn = (ImageView) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);

        locationRadioGroup = (RadioGroup) findViewById(R.id.locationRadioGroup);
        currentLocationRadioBtn = (RadioButton) findViewById(R.id.currentLocationRadioBtn);
        searchLocationRadioBtn = (RadioButton) findViewById(R.id.searchLocationRadioBtn);
        currentLocationRadioBtn.setOnClickListener(this);
        searchLocationRadioBtn.setOnClickListener(this);

        requestCashInBtn = (Button) findViewById(R.id.requestCashInBtn);
        requestCashInBtn.setEnabled(true);
        requestCashInBtn.setOnClickListener(this);


        searchLocationEditText = (CustomAutoCompleteTextView) findViewById(R.id.searchLocationEditText);
        searchLocationEditText.setButton(requestCashInBtn);
        searchLocationEditText.setRadioGroup(locationRadioGroup);
        searchLocationEditText.setRadioButton(searchLocationRadioBtn);
        GooglePlacesAutoCompleteArrayAdapter googlePlacesAutoCompleteBbsArrayAdapter = new GooglePlacesAutoCompleteArrayAdapter(this, R.layout.google_places_auto_complete_listview);
        searchLocationEditText.setAdapter(googlePlacesAutoCompleteBbsArrayAdapter);
        searchLocationEditText.setOnItemClickListener(this);
        searchLocationEditText.setOnEditorActionListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        activityAfterFinishTypeText();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == requestCashInBtn.getId()) {
            //configure to display gps dialog if gps is not active
            displayGpsDialog();
        } else if (view.getId() == currentLocationRadioBtn.getId()) {
            requestCashInBtn.setEnabled(true);
        } else if (view.getId() == searchLocationRadioBtn.getId()) {
            enableDisableRequestBtn();
        } else if (view.getId() == deleteBtn.getId()) {
            //get latest checked radio button
            int checkedRadioBtnId = locationRadioGroup.getCheckedRadioButtonId();
            if (checkedRadioBtnId == searchLocationRadioBtn.getId()) {
                searchLocationEditText.setText("");
                enableDisableRequestBtn();
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            activityAfterFinishTypeText();

            return true;
        }

        return false;
    }

    private void enableDisableRequestBtn() {
        //logic to enable and disable button request
        int length = searchLocationEditText.getText().toString().trim().length();
        if (length > 0) requestCashInBtn.setEnabled(true);
        else requestCashInBtn.setEnabled(false);
    }

    private void activityAfterFinishTypeText() {
        //get latest checked radio button
        int checkedRadioBtnId = locationRadioGroup.getCheckedRadioButtonId();
        if (checkedRadioBtnId == searchLocationRadioBtn.getId()) {
            enableDisableRequestBtn();
        }

        //hide the keypad
        InputMethodManager mgr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(searchLocationEditText.getWindowToken(), 0);

        //moving cursor edit text ke depan
        searchLocationEditText.setSelection(0);
    }

    private void displayGpsDialog() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL_LOCATION_REQUEST);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_LOCATION_REQUEST);
        locationRequest.setPriority(PRIORITY_LOCATION_REQUEST);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //disappear button "Never" in dialog
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(this);
    }

    private void startNewActivity() {
        boolean flag = false;
        int checkedRadioBtnId = locationRadioGroup.getCheckedRadioButtonId();
        if (checkedRadioBtnId == searchLocationRadioBtn.getId()) flag = true;


        bundle.putBoolean("searchLocationChecked", flag);
        bundle.putString("searchLocationString", searchLocationEditText.getText().toString().trim());

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double longitude = 0.0, latitude = 0.0;

        Boolean isLocated = false;

        if ( !flag ) {



            if ( location != null ) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                isLocated       = true;
            }

        } else {

            String searchLocationString = searchLocationEditText.getText().toString().trim();
            try
            {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                List<Address> multiAddress = geocoder.getFromLocationName(searchLocationString, 1);

                if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
                {
                    Address singleAddress = multiAddress.get(0);
                    ArrayList<String> addressArray = new ArrayList<String>();

                    for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                        addressArray.add(singleAddress.getAddressLine(i));
                    }

                    String fullAddress = TextUtils.join(System.getProperty("line.separator"), addressArray);

                    //save data to session
                        latitude =  singleAddress.getLatitude();
                    longitude = singleAddress.getLongitude();
                    isLocated = true;
                }
                else
                {

                }
            }
            catch(IOException ioException)
            {
                // Catch network or other I/O problems.
                //errorMessage = "Catch : Network or other I/O problems - No geocoder available";
                Log.d("onIOException ", "Catch : Network or other I/O problems - No geocoder available");
            }
            catch(IllegalArgumentException illegalArgumentException)
            {
                // Catch invalid latitude or longitude values.
                //errorMessage = "Catch : Invalid latitude or longitude values";
                //Log.d("IllegalArgumentException ", "Catch : Invalid latitude or longitude values");

            }
        }

        if ( isLocated ) {

            int startRadius = getResources().getInteger(R.integer.start_radius);
            requestSearchAgent(longitude, latitude, startRadius);


        }
    }

    public void requestSearchAgent(final double longitude, final double latitude, final int radius) {
        final int maximumRadius   = getResources().getInteger(R.integer.maximum_radius);
        final int incrementRadius = getResources().getInteger(R.integer.increment_radius);

        if ( radius <= maximumRadius ) {
            final RequestParams params = new RequestParams();
            params.put("longitude", Double.toString(longitude));
            params.put("latitude", Double.toString(latitude));
            params.put("radius", Integer.toString(radius));
            params.put("services_id", "");
            params.put("user_id", sp.getString(DefineValue.USERID_PHONE, ""));

            progdialog = DefinedDialog.CreateProgressDialog(this, "");

            MyApiClient.searchAgent(getApplication(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Log.d("searchAgent", response.toString());

                            JSONArray agentListData = response.getJSONArray("data");

                            realm.beginTransaction();
                            for (int i = 0; i < agentListData.length(); i++) {
                                JSONObject object = agentListData.getJSONObject(i);

                                realm.where(AgentDetail.class).equalTo("businessId", Integer.valueOf(object.getString("business_id")))
                                        .findAll().deleteFirstFromRealm();

                                AgentDetail agentDetailModel = realm.createObject(AgentDetail.class, Integer.valueOf(object.getString("business_id")) );
                                //agentDetailModel.setBusinessId( Integer.valueOf(object.getString("business_id")) );
                                agentDetailModel.setBusinessName( object.getString("business_name") );
                                agentDetailModel.setOwnerId( object.getString("owner_id") );
                                agentDetailModel.setUserId( object.getString("user_id") );
                                agentDetailModel.setBusinessType( object.getString("business_type") );
                                agentDetailModel.setAvailableFlag( object.getString("available_flag") );
                                agentDetailModel.setAddress( object.getString("address") );
                                agentDetailModel.setLocationType( object.getString("location_type") );
                                agentDetailModel.setLongitude( Double.valueOf(object.getString("longitude")) );
                                agentDetailModel.setLatitude( Double.valueOf(object.getString("latitude")) );
                                agentDetailModel.setCreated( object.getString("created") );
                                agentDetailModel.setLastUpdated( object.getString("last_updated") );
                                agentDetailModel.setActive( Integer.valueOf(object.getString("active")) );
                                agentDetailModel.setRadius( Integer.valueOf(object.getString("radius")) );
                                agentDetailModel.setTotalRating( Double.valueOf(object.getString("total_rating")) );
                                agentDetailModel.setProfilePicture( object.getString("profile_picture") );
                                agentDetailModel.setLastOnline( object.getString("last_online") );
                                agentDetailModel.setEmail( object.getString("email") );
                                agentDetailModel.setPhone( object.getString("phone") );
                                agentDetailModel.setHandphone( object.getString("handphone") );
                                agentDetailModel.setDistance( Double.valueOf(object.getString("distance")) );

                                JSONArray service_data = object.optJSONArray("services_data");

                                if (service_data != null) {
                                    for (int x = 0; x < service_data.length(); x++) {

                                        realm.where(AgentServiceDetail.class).equalTo("businessId", Integer.valueOf(object.getString("business_id")))
                                                .findAll().deleteAllFromRealm();

                                        AgentServiceDetail agentServiceDetailModel = realm.createObject(AgentServiceDetail.class, Integer.valueOf(service_data.getJSONObject(x).getString("business_services_id")));
                                        agentServiceDetailModel.setBusinessId( Integer.valueOf(object.getString("business_id")) );
                                        //agentServiceDetailModel.setBusinessServiceId( Integer.valueOf(service_data.getJSONObject(x).getString("business_services_id")) );
                                        agentServiceDetailModel.setServiceId( Integer.valueOf(service_data.getJSONObject(x).getString("services_id")) );
                                        agentServiceDetailModel.setServices( service_data.getJSONObject(x).getString("services") );
                                        agentServiceDetailModel.setPrice( Double.valueOf(service_data.getJSONObject(x).getString("price")) );

                                    }

                                }

/*
                                business_id_arr.add(object.getString("business_id"));
                                business_name_arr.add(object.getString("business_name"));
                                owner_id_arr.add(object.getString("owner_id"));
                                user_id_arr.add(object.getString("user_id"));
                                business_type_arr.add(object.getString("business_type"));
                                available_flag_arr.add(object.getString("available_flag"));
                                address_arr.add(object.getString("address"));
                                location_type_arr.add(object.getString("location_type"));
                                longitude_arr.add(object.getString("longitude"));
                                latitude_arr.add(object.getString("latitude"));
                                created_arr.add(object.getString("created"));
                                last_updated_arr.add(object.getString("last_updated"));
                                active_arr.add(object.getString("active"));
                                radius_arr.add(object.getString("radius"));
                                total_rating_arr.add(object.getString("total_rating"));
                                profile_picture_arr.add(object.getString("profile_picture"));
                                last_online_arr.add(object.getString("last_online"));
                                email_arr.add(object.getString("email"));
                                phone_arr.add(object.getString("phone"));
                                handphone_arr.add(object.getString("handphone"));
                                distance_arr.add(object.getString("distance"));




                                service_name_arr.clear();

                                if (service_data != null) {
                                    for (int x = 0; x < service_data.length(); x++) {
                                        service_name_arr.add(service_data.getJSONObject(x).getString("services"));
                                    }

                                    AgentService item = new AgentService();
                                    item.agent_no = Integer.toString(i);
                                    item.service = service_name_arr.toString();
                                    item.save();

                                }
*/
                            }
                            realm.commitTransaction();

                            //if ( service_name_arr.isEmpty() ) {
                                Intent intent = new Intent(getApplication(), MainAgentActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            //}

                        } else {

                            if ( radius <= maximumRadius ) {
                                int newRadius = radius + incrementRadius;
                                requestSearchAgent(longitude,latitude, newRadius);
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.err_agent_not_found), Toast.LENGTH_LONG).show();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    ifFailure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    ifFailure(throwable);
                }

                private void ifFailure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplication(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    progdialog.dismiss();
                    Timber.w("Error Koneksi login:" + throwable.toString());

                }

            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if (googleApiClient != null) {
            googleApiClient.connect();
        //}
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
        //if (googleApiClient != null) {

        //}
    }

    //implements GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle)
    {
        Timber.d("onConnected Started");


        try {
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if ( location == null ){
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(INTERVAL_LOCATION_REQUEST);
                locationRequest.setFastestInterval(FASTEST_INTERVAL_LOCATION_REQUEST);
                locationRequest.setPriority(PRIORITY_LOCATION_REQUEST);
                //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult result) {
                        Timber.d("onLocationResult");
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        Timber.d("onLocationAvailability: isLocationAvailable =  " + locationAvailability.isLocationAvailable());
                    }
                }, null);
            } else {
                Timber.d("Location Found" + location.toString());
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        if (bundle!=null) {
            Timber.d(bundle.toString());
        }
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Timber.d("DEBUG result failed");
            if (googleAPI.isUserResolvableError(result)) {
                Timber.d("DEBUG isUserResolvableError failed");
                googleAPI.getErrorDialog(this, result, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            }

            return false;
        }

        return true;
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //implements ResultCallback<LocationSettingsResult>
    @Override
    public void onResult(LocationSettingsResult result)
    {
        final Status status = result.getStatus();

        switch(status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:

                try
                {
                    askLocationPermission();
                }
                catch (SecurityException e) {}

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                try
                {
                    //check the result in onActivityResult().
                    status.startResolutionForResult(this, RESOLUTION_RESULT);
                }
                catch (IntentSender.SendIntentException e) {}

                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                break;
        }
    }

    private void askLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //call function : onRequestPermissionsResult
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, AgentConstant.TRUE);
        }
        else
        {
            startNewActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode)
        {
            case AgentConstant.TRUE:
            {
                //If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted. Do the contacts-related task you need to do.
                    startNewActivity();
                }
                else
                {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case RESOLUTION_RESULT: //if we got the request check settings callback
                switch(resultCode)
                {
                    case Activity.RESULT_OK:

                        try
                        {
                            askLocationPermission();
                        }
                        catch (SecurityException e) {}

                        break;

                    case Activity.RESULT_CANCELED:

                        break;
                }
                break;
        }
    }

    //implements GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("onConnectionSuspended Started" + String.valueOf(i));
    }

    //implements OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed Started " + connectionResult.toString());
    }

}
