package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;
import com.sgo.saldomu.services.AgentShopService;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.realm.Realm;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsMemberLocationActivity extends BaseActivity implements OnMapReadyCallback,
        AdapterView.OnItemClickListener, TextView.OnEditorActionListener, EasyPermissions.PermissionCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    String memberId, memberDefaultAddress, countryName, provinceName, districtName, shopId, shopName, address, memberType, agentName, commName, postalCode;
    Realm myRealm;
    TextView tvDetailMemberName, tvCommName, tvAddress, tvDistrict, tvProvince;
    private GoogleMap mMap;
    MapView mapView;
    Double selectedLat, selectedLong;
    Double defaultLat, defaultLong;
    Button btnSubmit, btnLokasiGPS;
    ProgressDialog progdialog;
    SecurePreferences sp;
    MerchantCommunityList memberDetail;
    GooglePlacesAutoCompleteArrayAdapter googlePlacesAutoCompleteBbsArrayAdapter;
    List<Address> addressList = null;
    CustomAutoCompleteTextView locationSearch;
    String searchLocationString;
    private int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;
    private GoogleApiClient googleApiClient;
    Location lastLocation;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        memberId            = getIntent().getStringExtra("memberId");
        shopId              = getIntent().getStringExtra("shopId");
        shopName            = getIntent().getStringExtra("shopName");
        memberType          = getIntent().getStringExtra("memberType");
        agentName           = getIntent().getStringExtra("memberName");
        commName            = getIntent().getStringExtra("commName");
        provinceName        = getIntent().getStringExtra("province");
        districtName        = getIntent().getStringExtra("district");
        address             = getIntent().getStringExtra("address");

        sp                              = CustomSecurePref.getInstance().getmSecurePrefs();

        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    GlobalSetting.RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if ( !GlobalSetting.isLocationEnabled(this) )
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.alertbox_gps_warning))
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                            Intent intent = new Intent(getApplicationContext(), MainPage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {


        }

        myRealm = Realm.getDefaultInstance();

        setActionBarIcon(R.drawable.ic_arrow_left);

        memberDefaultAddress    = address +", "+ districtName + ", "+ provinceName;

        defaultLat      = -6.121435;
        defaultLong     = 106.774124;

        if (memberDefaultAddress != null || !memberDefaultAddress.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(memberDefaultAddress, 1);
                Address address = addressList.get(0);
                selectedLat        = address.getLatitude();
                selectedLong       = address.getLongitude();


                setAdministrativeName();
            } catch (IOException e) {
                e.printStackTrace();


            }


        }



        //memberDetail        = myRealm.where(MerchantCommunityList.class).equalTo("memberId", memberId).equalTo("shopId", shopId).findFirst();

        googlePlacesAutoCompleteBbsArrayAdapter = new GooglePlacesAutoCompleteArrayAdapter(getApplicationContext(), R.layout.google_places_auto_complete_listview);

        locationSearch = (CustomAutoCompleteTextView) findViewById(R.id.editText);
        locationSearch.setAdapter(googlePlacesAutoCompleteBbsArrayAdapter);
        locationSearch.setOnItemClickListener(this);
        locationSearch.setOnEditorActionListener(this);

        setActionBarTitle(getString(R.string.update_merchant_location) + " - " + shopName);

        tvDetailMemberName  = (TextView) findViewById(R.id.tvDetailMemberName);
        tvCommName          = (TextView) findViewById(R.id.tvCommName);
        tvAddress           = (TextView) findViewById(R.id.tvAddress);
        tvProvince          = (TextView) findViewById(R.id.tvProvince);
        tvDistrict          = (TextView) findViewById(R.id.tvDistrict);

        tvDetailMemberName.setText(agentName);
        tvCommName.setText(commName);
        tvProvince.setText(provinceName);
        tvDistrict.setText(districtName);
        tvAddress.setText(address);



        btnSubmit       = (Button) findViewById(R.id.btnSubmit);
        btnLokasiGPS    = (Button) findViewById(R.id.btnLokasiGPS);

        btnSubmit.setOnClickListener(btnSubmitListener);
        btnLokasiGPS.setOnClickListener(btnLokasiGPSListener);

        try {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                googleApiClient.connect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    Button.OnClickListener btnLokasiGPSListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                selectedLat = lastLocation.getLatitude();
                selectedLong = lastLocation.getLongitude();
                mMap.clear();

                recreateAllMarker();

                locationSearch.clearFocus();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    };

    Button.OnClickListener btnSubmitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getApplicationContext())) {
                if ( selectedLat == null || countryName == null )
                {
                    DefinedDialog.showErrorDialog(getApplicationContext(), getString(R.string.err_empty_coordinate_message));
                }
                else {
                    progdialog              = DefinedDialog.CreateProgressDialog(BbsMemberLocationActivity.this, "");

                    RequestParams params    = new RequestParams();
                    UUID rcUUID             = UUID.randomUUID();
                    String  dtime           = DateTimeFormat.getCurrentDateTime();

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.AppID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, shopId);
                    params.put(WebParams.MEMBER_ID, memberId);
                    params.put(WebParams.DISTRICT, districtName);
                    params.put(WebParams.PROVINCE, provinceName);
                    params.put(WebParams.COUNTRY, countryName.toUpperCase());
                    params.put(WebParams.LATITUDE, selectedLat);
                    params.put(WebParams.LONGITUDE, selectedLong);
                    params.put(WebParams.ZIP_CODE, postalCode);

                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId.toUpperCase() + shopId.toUpperCase()
                            + BuildConfig.AppID + selectedLat + selectedLong));

                    params.put(WebParams.SIGNATURE, signature);

                    MyApiClient.updateMemberLocation(getApplication(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            //progdialog.dismiss();

                            try {

                                String code = response.getString(
                                        WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    //myRealm.beginTransaction();


                                    RequestParams params2 = new RequestParams();

                                    UUID rcUUID2             = UUID.randomUUID();
                                    String  dtime2           = DateTimeFormat.getCurrentDateTime();

                                    params2.put(WebParams.RC_UUID, rcUUID2);
                                    params2.put(WebParams.RC_DATETIME, dtime2);
                                    params2.put(WebParams.APP_ID, BuildConfig.AppID);
                                    params2.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                                    params2.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                                    params2.put(WebParams.SHOP_ID, shopId);
                                    params2.put(WebParams.MEMBER_ID, memberId);
                                    params2.put(WebParams.FLAG_ALL_DAY, DefineValue.STRING_YES);
                                    params2.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_NONE);


                                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID2 + dtime2 + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId.toUpperCase() + shopId.toUpperCase() + BuildConfig.AppID));

                                    params2.put(WebParams.SIGNATURE, signature);


                                    MyApiClient.setupOpeningHour(BbsMemberLocationActivity.this, params2, new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            progdialog.dismiss();
                                            Timber.d("isi response sent request cash in:" + response.toString());

                                            try {
                                                String code = response.getString(WebParams.ERROR_CODE);
                                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putInt(DefineValue.INDEX, BBSActivity.BBSKELOLA);

                                                    SecurePreferences.Editor mEditor = sp.edit();
                                                    mEditor.putString(DefineValue.IS_AGENT_APPROVE, DefineValue.STRING_YES);
                                                    mEditor.putString(DefineValue.AGENT_NAME, agentName);
                                                    mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_YES);
                                                    mEditor.putString(DefineValue.BBS_MEMBER_ID, memberId);
                                                    mEditor.putString(DefineValue.BBS_SHOP_ID, shopId);
                                                    mEditor.apply();
                                                    setResult(MainPage.RESULT_REFRESH_NAVDRAW );

                                                    //Intent intent = new Intent(getApplicationContext(), BBSActivity.class);
                                                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    //intent.putExtras(bundle);
                                                    //startActivityForResult(intent, MainPage.RESULT_REFRESH_NAVDRAW);

                                                    finish();
                                                }
                                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                                    //test.showDialoginActivity(getApplication(),message);
                                                }
                                                else {
                                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                                    Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                            super.onFailure(statusCode, headers, responseString, throwable);
                                            failure(throwable);
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                            super.onFailure(statusCode, headers, throwable, errorResponse);
                                            failure(throwable);
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                            super.onFailure(statusCode, headers, throwable, errorResponse);
                                            failure(throwable);
                                        }

                                        private void failure(Throwable throwable){
                                            if(MyApiClient.PROD_FAILURE_FLAG)
                                                Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
                                            if (progdialog.isShowing())
                                                progdialog.dismiss();

                                            Timber.w("Error Koneksi sent request setup open hour:"+throwable.toString());
                                        }
                                    });

                                    //String tempSetupOpenHour = response.getString("setup_open_hour");
                                    //if (tempSetupOpenHour.equals("")) {
                                        //tempSetupOpenHour = DefineValue.STRING_NO;
                                    //}
                                    //memberDetail.setSetupOpenHour(response.getString("setup_open_hour"));
                                    //myRealm.copyToRealmOrUpdate(memberDetail);
                                    //myRealm.commitTransaction();


                                    /*if (memberType.equals(DefineValue.SHOP_MERCHANT)) {
                                        Intent intent = new Intent(getApplicationContext(), BbsMerchantCategoryActivity.class);
                                        intent.putExtra("memberId", memberId);
                                        intent.putExtra("shopId", shopId);
                                        intent.putExtra("setupOpenHour", tempSetupOpenHour);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getApplicationContext().startActivity(intent);
                                        finish();
                                    } else {*/
                                        /*Intent intent = new Intent(getApplicationContext(), BbsListSettingKelolaActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();*/


                                    //}

                                } else if ( code.equals(WebParams.LOGOUT_CODE) ) {
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    //test.showDialoginActivity(getActi,message);
                                } else {



                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
            else DefinedDialog.showErrorDialog(getApplicationContext(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {

            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    public void setAdministrativeName() {
        Geocoder geocoder = new Geocoder(this, new Locale("id"));



        try {
            addressList = geocoder.getFromLocation(
                    selectedLat,
                    selectedLong,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.d("IOEXCEPTION", "IO EXCEPTION", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e("ILLEGAL", "ILLEGAL EXP" + ". " +
                    "Latitude = " + selectedLat +
                    ", Longitude = " +
                    selectedLong, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addressList == null || addressList.size()  == 0) {

        } else {
            Address addressDetail = addressList.get(0);

            ArrayList<String> addressFragments = new ArrayList<String>();
            districtName    = addressDetail.getSubAdminArea();
            provinceName    = addressDetail.getAdminArea();
            countryName     = addressDetail.getCountryName();
            postalCode      = addressDetail.getPostalCode();

        }
    }

    public void onMapSearch(View view) {

        locationSearch = (CustomAutoCompleteTextView) findViewById(R.id.editText);
        locationSearch.setAdapter(googlePlacesAutoCompleteBbsArrayAdapter);
        locationSearch.setOnItemClickListener(this);
        locationSearch.setOnEditorActionListener(this);

        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        Double latitude        = defaultLat;
        Double longitude       = defaultLong;
        mMap.clear();

        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(locationSearch.getWindowToken(), 0);

        if (!location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

                if ( addressList.size() > 0 ) {
                    Address address = addressList.get(0);
                    latitude        = address.getLatitude();
                    longitude       = address.getLongitude();
                    selectedLat     = latitude;
                    selectedLong    = longitude;

                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    setAdministrativeName();
                } else {
                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            } catch (IOException e) {
                e.printStackTrace();

                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }


        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.err_empty_merchant_address), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        List<Address> addressList = null;
        Double latitude = defaultLat, longitude = defaultLong;

        LatLng latLng;
        CameraPosition cameraPosition;
        if (selectedLat != null || selectedLong != null ) {

                latLng = new LatLng(selectedLat, selectedLong);
                mMap.addMarker(new MarkerOptions().position(latLng).title(memberDefaultAddress));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


            cameraPosition = new CameraPosition.Builder()
                    .target(latLng) // Center Set
                    .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                    .build(); // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    //jika animate camera position sudah selesai, maka on receiver baru boleh dijalankan.
                    //jika receiver dijalankan sebelum camera position selesai, maka map tidak akan ter-rendering sempurna
                    //receiverStatus = true;

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });

        } else {
            latLng = new LatLng(defaultLat, defaultLong);
            mMap.addMarker(new MarkerOptions().position(latLng).title(memberDefaultAddress));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            cameraPosition = new CameraPosition.Builder()
                    .target(latLng) // Center Set
                    .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                    .build(); // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    //jika animate camera position sudah selesai, maka on receiver baru boleh dijalankan.
                    //jika receiver dijalankan sebelum camera position selesai, maka map tidak akan ter-rendering sempurna
                    //receiverStatus = true;

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });
        }





        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // clearing map and generating new marker points if user clicks on map more than two times
                mMap.clear();
                selectedLat     = point.latitude;
                selectedLong    = point.longitude;
                mMap.addMarker(new MarkerOptions().position(point).title(memberDefaultAddress));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));



            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_member_location;
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

    private void setMapCamera()
    {
        if ( selectedLat != null && selectedLong != null ) {
            LatLng latLng = new LatLng(selectedLat, selectedLong);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.addMarker(new MarkerOptions().position(latLng).title(searchLocationString));
            //add camera position and configuration
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng) // Center Set
                    .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                    .build(); // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private void recreateAllMarker() {
        if (mMap != null ) {

            setMapCamera();



        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        searchLocationString = locationSearch.getText().toString().trim();
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

                //changeMap(singleAddress.getLatitude(), singleAddress.getLongitude());
                selectedLat = singleAddress.getLatitude();
                selectedLong = singleAddress.getLongitude();

                mMap.clear();

                recreateAllMarker();

                locationSearch.clearFocus();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        try {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                googleApiClient.connect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {


        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.setTitle(getString(R.string.alertbox_title_warning));
            alertDialog.setMessage(getString(R.string.alertbox_message_warning));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), MainPage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if ( lastLocation == null ){
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {

                selectedLat = lastLocation.getLatitude();
                selectedLong = lastLocation.getLongitude();
                defaultLat = selectedLat;
                defaultLong = selectedLong;
                Timber.d("Location Found" + lastLocation.toString());
                //googleApiClient.disconnect();

            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        if (bundle!=null) {
            Timber.d(bundle.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation    = location;
        selectedLat     = lastLocation.getLatitude();
        selectedLong    = lastLocation.getLongitude();
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            }

            return false;
        }

        return true;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.FASTEST_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            googleApiClient.disconnect();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
}
