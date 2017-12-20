package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.GooglePlacesAutoCompleteArrayAdapter;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomAutoCompleteTextView;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.GoogleAPIUtils;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static com.activeandroid.Cache.getContext;

public class BbsNewSearchAgentActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemClickListener,
        TextView.OnEditorActionListener,
        LocationListener,
        OnMapReadyCallback,
        EasyPermissions.PermissionCallbacks {

    SecurePreferences sp;
    Double latitude, longitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private String categoryId, categoryName;
    private Intent intentData;
    ProgressDialog progdialog;
    private Boolean showHideLayoutNote = false;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private CustomAutoCompleteTextView searchLocationEditText;
    GooglePlacesAutoCompleteArrayAdapter googlePlacesAutoCompleteBbsArrayAdapter;
    private GoogleMap globalMap;
    SupportMapFragment mapFrag;
    private Marker markerCurrent;
    Button btnProses;
    List<String> currentShops;
    List<String> latestShops;
    List<String> differentShops;
    HashMap<String,Marker> hashMapMarkers;
    EditText etJumlah, etNote;
    String amount, completeAddress, provinceName, districtName, countryName;
    private LinearLayout llNote;
    private static final int RC_LOCATION_PERM = 500;
    private static final int RC_GPS_REQUEST = 1;

    private int timeDelayed = 20000;
    // Init
    private Handler handlerSearchAgent = new Handler();
    private Runnable runnableSearchAgent = new Runnable() {
        @Override
        public void run() {
            searchAgent();
            handlerSearchAgent.postDelayed(this, timeDelayed);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp              = CustomSecurePref.getInstance().getmSecurePrefs();

        intentData      = getIntent();
        currentShops    = new ArrayList<String>();
        latestShops     = new ArrayList<String>();
        differentShops  = new ArrayList<String>();
        hashMapMarkers  = new HashMap<>();

        categoryId          = intentData.getStringExtra(DefineValue.CATEGORY_ID);
        categoryName        = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
        initializeToolbar(getString(R.string.search_agent) + " " + categoryName);

        llNote          = (LinearLayout) findViewById(R.id.llNote);
        llNote.setVisibility(View.GONE);

        etNote          = (EditText) findViewById(R.id.etNote);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.agentMap);
        mapFrag.getMapAsync(this);

        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            if ( !GlobalSetting.isLocationEnabled(this) ) {
                showAlertEnabledGPS();
            } else {
                runningApp();
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, perms);
        }



    }

    private void showAlertEnabledGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(ilocation, RC_GPS_REQUEST);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        startActivity(new Intent(getApplicationContext(), MainPage.class));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void runningApp() {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }


        searchLocationEditText = (CustomAutoCompleteTextView) findViewById(R.id.searchLocationEditText);
        googlePlacesAutoCompleteBbsArrayAdapter = new GooglePlacesAutoCompleteArrayAdapter(getContext(), R.layout.google_places_auto_complete_listview);
        searchLocationEditText.setAdapter(googlePlacesAutoCompleteBbsArrayAdapter);
        searchLocationEditText.setOnItemClickListener(this);
        searchLocationEditText.setOnEditorActionListener(this);
        searchLocationEditText.clearFocus();
        searchLocationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if ( hasFocus ) {
                    v.setSelected(true);
                } else {
                    v.setSelected(false);
                }
            }
        });
        searchLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
            }
        });

        searchLocationEditText.setSelectAllOnFocus(true);

        etJumlah                = (EditText) findViewById(R.id.etJumlah);
        etJumlah.addTextChangedListener(jumlahChangeListener);

        etJumlah.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (etJumlah.getRight() - etJumlah.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        //Toast.makeText(BbsNewSearchAgentActivity.this, "TESTING", Toast.LENGTH_SHORT).show();

                        if ( !showHideLayoutNote ) {
                            showHideLayoutNote = true;
                            llNote.setVisibility(View.VISIBLE);
                        } else {
                            showHideLayoutNote = false;
                            llNote.setVisibility(View.GONE);
                        }


                        return true;
                    }
                }
                return false;
            }
        });

        btnProses               = (Button) findViewById(R.id.btnProses);
        btnProses.setEnabled(false);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);


        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean hasError    = false;

                if(etJumlah.getText().toString().length()==0){
                    etJumlah.requestFocus();
                    etJumlah.setError(getString(R.string.sgoplus_validation_jumlahSGOplus), null);
                    hasError = true;
                }
                else if(Long.parseLong(etJumlah.getText().toString()) < 1){
                    etJumlah.requestFocus();
                    etJumlah.setError(getString(R.string.payfriends_amount_zero), null);
                    hasError = true;
                }

                if ( !hasError ) {
                    amount = etJumlah.getText().toString();

                    String note = etNote.getText().toString();

                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                    SecurePreferences.Editor mEditor = prefs.edit();
                    mEditor.putString(DefineValue.BBS_TX_ID, "");
                    mEditor.putString(DefineValue.AMOUNT, amount);
                    mEditor.apply();


                    etJumlah.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    Intent i = new Intent(BbsNewSearchAgentActivity.this, BbsSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                    i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                    i.putExtra(DefineValue.LAST_CURRENT_LATITUDE, latitude);
                    i.putExtra(DefineValue.LAST_CURRENT_LONGITUDE, longitude);

                    i.putExtra(DefineValue.BBS_COMPLETE_ADDRESS, completeAddress);
                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                    i.putExtra(DefineValue.AMOUNT, amount);
                    i.putExtra(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);
                    i.putExtra(DefineValue.BBS_NOTE, note);

                    startActivity(i);
                    finish();

                }

            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_new_search_agent;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if ( mLastLocation == null ){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                latitude  = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();

                if (globalMap != null ) {

                    //disable map gesture untuk sementara sampai camera position selesai
                    globalMap.getUiSettings().setAllGesturesEnabled(true);
                    globalMap.getUiSettings().setMapToolbarEnabled(false);
                    globalMap.setIndoorEnabled(false);
                    globalMap.setMyLocationEnabled(false);

                    if ( latitude != null && longitude != null ) {
                        LatLng latLng = new LatLng(latitude, longitude);
                        globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                        //add camera position and configuration
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng) // Center Set
                                .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                                .build(); // Creates a CameraPosition from the builder

                        globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                                globalMap.getUiSettings().setAllGesturesEnabled(true);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });


                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
                        markerCurrent = globalMap.addMarker(markerOptions);

                    }

                }

                this.getAddressByLatLng();

                //btnProses.setEnabled(true);


                mGoogleApiClient.disconnect();
                searchAgent();
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

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            if (globalMap != null ) {

                //disable map gesture untuk sementara sampai camera position selesai
                globalMap.getUiSettings().setAllGesturesEnabled(true);
                globalMap.getUiSettings().setMapToolbarEnabled(false);
                globalMap.setIndoorEnabled(false);
                globalMap.setMyLocationEnabled(false);

                if ( latitude != null && longitude != null ) {
                    LatLng latLng = new LatLng(latitude, longitude);
                    globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    //add camera position and configuration
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng) // Center Set
                            .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                            .build(); // Creates a CameraPosition from the builder

                    globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                            globalMap.getUiSettings().setAllGesturesEnabled(true);
                        }

                        @Override
                        public void onCancel() {
                        }
                    });


                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
                    markerCurrent = globalMap.addMarker(markerOptions);

                }

            }

            mGoogleApiClient.disconnect();
            this.getAddressByLatLng();
            searchAgent();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
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
        mLocationRequest.setInterval(DefineValue.AGENT_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.AGENT_FASTEST_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.AGENT_DISPLACEMENT);
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

    private void searchAgent() {
        progdialog              = DefinedDialog.CreateProgressDialog(this, getString(R.string.menu_item_search_agent));

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        final String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.CATEGORY_ID, categoryId);
        params.put(WebParams.LATITUDE, latitude);
        params.put(WebParams.LONGITUDE, longitude);
        params.put(WebParams.RADIUS, DefineValue.MAX_RADIUS_SEARCH_AGENT);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + categoryId
                + latitude + longitude));

        params.put(WebParams.SIGNATURE, signature);

        //Start
        handlerSearchAgent.removeCallbacks(runnableSearchAgent);

        MyApiClient.NewSearchAgent(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Timber.d("Response New Search Agent:" + response.toString());

                if ( progdialog.isShowing())
                    progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        //Start
                        handlerSearchAgent.postDelayed(runnableSearchAgent, timeDelayed);

                        JSONArray shops = response.getJSONArray("shop");

                        shopDetails.clear();

                        if (shops.length() > 0) {

                            Boolean firstLoad = false;
                            if ( currentShops.size() == 0 ) {
                                firstLoad = true;
                            }

                            latestShops = new ArrayList<String>();

                            for (int j = 0; j < shops.length(); j++) {
                                JSONObject object = shops.getJSONObject(j);
                                ShopDetail shopDetail = new ShopDetail();

                                shopDetail.setShopId(object.getString("shop_id"));
                                shopDetail.setMemberCust(object.getString("member_cust"));
                                shopDetail.setMemberId(object.getString("member_id"));
                                shopDetail.setShopLatitude(object.getDouble("shop_latitude"));
                                shopDetail.setShopLongitude(object.getDouble("shop_longitude"));
                                shopDetail.setMemberName(object.getString("member_name"));
                                shopDetail.setShopAddress(object.getString("shop_address"));
                                shopDetail.setUrlSmallProfilePicture(object.getString("shop_picture"));
                                shopDetail.setLastActivity(object.getString("shop_lastactivity"));
                                shopDetail.setShopMobility(object.getString("shop_mobility"));
                                shopDetails.add(shopDetail);

                                latestShops.add(shopDetail.getShopId());

                                if ( firstLoad ) {
                                    currentShops.add(shopDetail.getShopId());
                                }
                            }

                            if ( !firstLoad ) {
                                differentShops = new ArrayList<String>(currentShops);
                                differentShops.removeAll(latestShops);

                                currentShops = new ArrayList<String>(latestShops);
                            }

                            if ( differentShops.size() > 0 ) {
                                for (String tempShopId : differentShops) {
                                    if (hashMapMarkers.containsKey(tempShopId)) {
                                        Marker marker = hashMapMarkers.get(tempShopId);
                                        marker.remove();

                                        hashMapMarkers.remove(tempShopId);
                                    }
                                }
                            }

                            for(int i = 0; i < shopDetails.size(); i++){

                                if ( shopDetails.get(i).getShopLatitude() != null && shopDetails.get(i).getShopLongitude() != null ) {
                                    LatLng latLng = new LatLng(shopDetails.get(i).getShopLatitude(), shopDetails.get(i).getShopLongitude());

                                    if (hashMapMarkers.containsKey(shopDetails.get(i).getShopId())) {
                                        Marker marker = hashMapMarkers.get(shopDetails.get(i).getShopId());

                                        marker.setPosition(latLng);
                                        hashMapMarkers.remove(shopDetails.get(i).getShopId());
                                        hashMapMarkers.put(shopDetails.get(i).getShopId(), marker);
                                    } else {

                                        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                                        if (shopDetails.get(i).getShopMobility().equals(DefineValue.STRING_YES)) {
                                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_person, 90, 90)));
                                        } else {
                                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_home, 90, 90)));
                                        }

                                        hashMapMarkers.put(shopDetails.get(i).getShopId(), globalMap.addMarker(markerOptions));

                                    }
                                }
                            }

                            Timber.d("diffShops: " + differentShops.toString());
                        }


                    } else {
                        shopDetails.clear();

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
                    Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi Search Agent:" + throwable.toString());

                if ( progdialog.isShowing())
                    progdialog.dismiss();

            }

        });

    }

    public void initializeToolbar(String title)
    {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if(id == android.R.id.home)
        {
            //kembali ke activity sebelumnya
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String searchLocationString = searchLocationEditText.getText().toString().trim();
        try
        {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            List<Address> multiAddress = geocoder.getFromLocationName(searchLocationString, 1);

            if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
            {

                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                }

                String fullAddress  = TextUtils.join(System.getProperty("line.separator"), addressArray);
                latitude            = singleAddress.getLatitude();
                longitude           = singleAddress.getLongitude();

                this.getAddressByLatLng();

                searchLocationEditText.clearFocus();

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                if ( globalMap != null ) {
                    LatLng latLng = new LatLng(latitude, longitude);
                    globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    markerCurrent.setPosition(latLng);


                }

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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        globalMap = googleMap;

        if (globalMap != null ) {

            //disable map gesture untuk sementara sampai camera position selesai
            globalMap.getUiSettings().setAllGesturesEnabled(true);
            globalMap.getUiSettings().setMapToolbarEnabled(false);
            globalMap.setIndoorEnabled(false);
            globalMap.setMyLocationEnabled(false);

            if ( latitude != null && longitude != null ) {
                LatLng latLng = new LatLng(latitude, longitude);
                globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                //add camera position and configuration
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng) // Center Set
                        .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                        .build(); // Creates a CameraPosition from the builder

                globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                        globalMap.getUiSettings().setAllGesturesEnabled(true);
                    }

                    @Override
                    public void onCancel() {
                    }
                });


                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
                markerCurrent = globalMap.addMarker(markerOptions);

            }

        }

    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height)
    {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerSearchAgent.removeCallbacks(runnableSearchAgent);
    }

    private TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("0"))etJumlah.setText("");
            if(s.length() > 0 && s.charAt(0) == '0'){
                int i = 0;
                for (; i < s.length(); i++){
                    if(s.charAt(i) != '0')break;
                }
                etJumlah.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void getAddressByLatLng() {
        btnProses.setEnabled(false);
        MyApiClient.getGoogleAPIAddressByLatLng(this, latitude, longitude, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                try {

                    String status = response.getString(WebParams.GMAP_API_STATUS);
                    Timber.w("JSON Response: "+response.toString());

                    btnProses.setEnabled(true);

                    if ( status.equals(DefineValue.GMAP_STRING_OK) ) {
                        ArrayList<HashMap<String,String>> gData = GoogleAPIUtils.getResponseGoogleAPI(response);

                        for (HashMap<String, String> hashMapObject : gData) {
                            for (String key : hashMapObject.keySet()) {
                                switch(key) {
                                    case "formattedAddress":
                                        completeAddress = hashMapObject.get(key);
                                        break;
                                    case "province":
                                        provinceName = hashMapObject.get(key);
                                        break;
                                    case "district":
                                        districtName = hashMapObject.get(key);
                                        break;
                                    case "subdistrict":
                                        break;
                                    case "country":
                                        countryName = hashMapObject.get(key);
                                        break;
                                }


                            }
                        }

                        if ( completeAddress.equals("") ) {
                            completeAddress += districtName + ", ";
                            completeAddress += provinceName;
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

                Timber.w("Error Koneksi: " + throwable.toString());

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == RC_GPS_REQUEST ) {
            //if ( requestCode == Activity.RESULT_OK ) {
                if ( GlobalSetting.isLocationEnabled(this) ) {
                    runningApp();
                }
            //}
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch(requestCode) {
            //case RC_LOCATION_PERM:
            case RC_LOCATION_PERM:
                if ( !GlobalSetting.isLocationEnabled(this) ) {
                    showAlertEnabledGPS();
                } else {
                    runningApp();
                }
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_LOCATION_PERM:
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this).build().show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(BbsNewSearchAgentActivity.this).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.alertbox_title_warning));
                    alertDialog.setMessage(getString(R.string.alertbox_message_warning));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                    alertDialog.show();
                }
                break;
        }
    }
}