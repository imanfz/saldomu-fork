package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopDetail;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static com.sgo.saldomu.coreclass.GlobalSetting.RC_LOCATION_PERM;

public class BbsMapViewByMemberActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SecurePreferences sp;
    private String title;
    String txId, memberId, shopId, categoryName, amount;
    SupportMapFragment mapFrag;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    Double memberLatitude, memberLongitude, agentLatitude, agentLongitude, benefLatitude, benefLongitude;
    ShopDetail shopDetail;
    private GoogleMap globalMap;
    TextView tvCategoryName, tvMemberName, tvAmount, tvShop;
    Boolean isFirstLoad = true, isRunning = false;
    String gcmId;
    Button btnDone, btnCancel;
    ProgressDialog progdialog, progdialog2;
    Intent intentData;

    private int timeDelayed = 30000;

    // Init
    private Handler handler = new Handler();
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            updateLocationMember();
            handler.postDelayed(this, timeDelayed);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp      = CustomSecurePref.getInstance().getmSecurePrefs();
        if ( sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            //is agent
            Intent intent = new Intent(this, MainPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        if ( checkPlayServices() ) {
            buildGoogleApiClient();
            createLocationRequest();
        }


        gcmId                   = "";
        tvCategoryName          = (TextView) findViewById(R.id.tvCategoryName);
        tvMemberName            = (TextView) findViewById(R.id.tvMemberName);
        tvAmount                = (TextView) findViewById(R.id.tvAmount);
        //tvShop                  = (TextView) findViewById(R.id.tvShop);
        btnDone                 = (Button) findViewById(R.id.btnDone);
        btnCancel               = (Button) findViewById(R.id.btnCancel);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.agentMap);
        mapFrag.getMapAsync(this);
        mapFrag.getView().setVisibility(View.GONE);

        intentData              = getIntent();

        if ( intentData.hasExtra(DefineValue.BBS_TX_ID) ) {
            Timber.d("isi intent amount oncreate " + intentData.getStringExtra(DefineValue.AMOUNT));

            txId                    = intentData.getStringExtra(DefineValue.BBS_TX_ID);
            categoryName                    = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
            amount                    = intentData.getStringExtra(DefineValue.AMOUNT);
        } else {
            txId                    = sp.getString(DefineValue.BBS_TX_ID, "");
            categoryName            = sp.getString(DefineValue.CATEGORY_NAME, "");
            amount                  = sp.getString(DefineValue.AMOUNT, "");
        }


        //temporary only
        agentLatitude           = -6.222699;
        agentLongitude          = 106.653412;

        btnDone.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        progdialog2              = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");

                        confirmTransactionMember();
                    }
                }
        );

        btnCancel.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsMapViewByMemberActivity.this).create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.setTitle(getString(R.string.alertbox_title_information));


                        alertDialog.setMessage(getString(R.string.message_notif_cancel_trx));



                        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //dialog.dismiss();

                                        progdialog2              = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");
                                        cancelTransactionMember();



                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();



                                    }
                                });
                        alertDialog.show();


                    }
                }
        );

        title                   = getString(R.string.menu_item_title_map_member);
        initializeToolbar(title);

        TextView t = (TextView) findViewById(R.id.name);
        t.setText(Html.fromHtml(getString(R.string.bbs_trx_detail_member)));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.bbs_map_view_by_member_activity;
    }

    public void initializeToolbar(String title)
    {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        globalMap = googleMap;

        if ( globalMap != null ) {
            mapFrag.getView().setVisibility(View.VISIBLE);

        }
    }

    private void setMapCamera()
    {
        if ( memberLatitude != 0 && memberLongitude != 0 ) {
            globalMap.clear();

            //new BbsMapNagivationActivity.GoogleMapRouteDirectionTask(targetLatitude, targetLongitude, currentLatitude, currentLongitude).execute();
            globalMap.getUiSettings().setMapToolbarEnabled(false);

            LatLng latLng = new LatLng(agentLatitude, agentLongitude);

            globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.house));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_person, 90, 90)));
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Marker marker = globalMap.addMarker(markerOptions);

            LatLng targetLatLng = new LatLng(memberLatitude, memberLongitude);
            MarkerOptions markerTargetOptions = new MarkerOptions()
                    .position(targetLatLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
            globalMap.addMarker(markerTargetOptions);


            //add camera position and configuration
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng) // Center Set
                    .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                    .build(); // Creates a CameraPosition from the builder

            globalMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    //jika animate camera position sudah selesai, maka on receiver baru boleh dijalankan.
                    //jika receiver dijalankan sebelum camera position selesai, maka map tidak akan ter-rendering sempurna

                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                    globalMap.getUiSettings().setAllGesturesEnabled(true);
                }

                @Override
                public void onCancel() {
                }
            });


        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation        = location;

        if ( lastLocation.getLatitude() != memberLatitude )
            memberLatitude      = lastLocation.getLatitude();

        if ( lastLocation.getLongitude() != memberLongitude )
            memberLongitude     = lastLocation.getLongitude();

        if ( progdialog != null ) {
            if ( progdialog.isShowing() ) progdialog.dismiss();
        }

        if ( !isRunning ) {
            handler.removeCallbacks(runnable2);
            updateLocationMember();
            handler.postDelayed(runnable2, timeDelayed);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if ( !GlobalSetting.isLocationEnabled(this) )
        {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.alertbox_gps_warning))
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                            Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(ilocation, 1);

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                            startActivity(new Intent(getApplicationContext(), MainPage.class));
                        }
                    });
            final android.app.AlertDialog alert = builder.create();
            alert.show();
        }

        try {
            googleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            handler.removeCallbacks(runnable2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, DefineValue.REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            }

            return false;
        }

        return true;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DefineValue.MEMBER_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.MEMBER_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.MEMBER_DISPLACEMENT);
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
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");
        startLocationUpdate();

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);

            if ( lastLocation == null ){

            } else {

                memberLatitude      = lastLocation.getLatitude();
                memberLongitude     = lastLocation.getLongitude();
                setMapCamera();
                Timber.d("Location Found" + lastLocation.toString());

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
    public void onDestroy() {
        super.onDestroy();
        try {
            handler.removeCallbacks(runnable2);
        } catch(Exception e) {
            e.printStackTrace();
        }
        googleApiClient.disconnect();
    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height)
    {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void updateLocationMember() {

        //temporary only
//        tvMemberName.setText("NAMA AGEN");
//        tvShop.setText("NAMA TOKO");
//        tvCategoryName.setText(categoryName);
//        tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(amount) );
//
//        setMapCamera();

        if ( sp.getString(DefineValue.USERID_PHONE, "").equals("") )
            return;

        progdialog              = DefinedDialog.CreateProgressDialog(this, "");
        RequestParams params    = new RequestParams();


        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        //params.put(WebParams.SHOP_ID, shopId);
        //params.put(WebParams.MEMBER_ID, memberId);

        params.put(WebParams.KEY_PHONE, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.LATITUDE, memberLatitude);
        params.put(WebParams.LONGITUDE, memberLongitude);

        handler.removeCallbacks(runnable2);
        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + txId + sp.getString(DefineValue.USERID_PHONE, "") ));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.updateLocationMember(getApplication(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {
                    isRunning = false;
                    String code = response.getString(WebParams.ERROR_CODE);

                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        agentLatitude      = response.getDouble(WebParams.SHOP_LATITUDE);
                        agentLongitude     = response.getDouble(WebParams.SHOP_LONGITUDE);

                        tvMemberName.setText(response.getString(WebParams.MEMBER_NAME));
                        //tvShop.setText(response.getString(WebParams.SHOP_NAME));
                        tvCategoryName.setText(categoryName);
                        tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(amount) );

                        setMapCamera();

                    } else {
                        //progdialog.dismiss();
                        code = response.getString(WebParams.ERROR_MESSAGE);
                        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                        //startActivity(new Intent(getApplicationContext(), MainPage.class));
                    }
                    handler.postDelayed(runnable2, timeDelayed);
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

                progdialog.dismiss();

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplication(), throwable.toString(), Toast.LENGTH_SHORT).show();

                isRunning = false;

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdate() {
        try {
            PendingResult<Status> statusPendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
        } catch ( SecurityException se) {
            se.printStackTrace();
        }
    }

    private void confirmTransactionMember() {

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.KEY_PHONE, sp.getString(DefineValue.USERID_PHONE, ""));

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + txId + sp.getString(DefineValue.USERID_PHONE, "")));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.confirmTransactionMember(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    progdialog2.dismiss();
                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        handler.removeCallbacks(runnable2);

                        Intent intent = new Intent(getApplicationContext(), MainPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
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
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);
                progdialog2.dismiss();
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

    }

    private void cancelTransactionMember() {

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.KEY_PHONE, sp.getString(DefineValue.USERID_PHONE, ""));

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + txId + sp.getString(DefineValue.USERID_PHONE, "")));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.cancelTransactionMember(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    progdialog2.dismiss();
                    String code = response.getString(WebParams.ERROR_CODE);

                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        Intent intent = new Intent(getApplicationContext(), MainPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
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
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);
                progdialog2.dismiss();
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }

            if ( !GlobalSetting.isLocationEnabled(this) )
            {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.alertbox_gps_warning))
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                                Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(ilocation, 1);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                                startActivity(new Intent(getApplicationContext(), MainPage.class));
                                finish();
                            }
                        });
                final android.app.AlertDialog alert = builder.create();
                alert.show();
            } else {
                Intent i = new Intent(this, BbsMapViewByMemberActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {

            intentData              = getIntent();

            if ( intentData.hasExtra(DefineValue.BBS_TX_ID) ) {

                Timber.d("isi intent amount onresume " + intentData.getStringExtra(DefineValue.AMOUNT));
                txId                    = intentData.getStringExtra(DefineValue.BBS_TX_ID);
                categoryName            = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
                amount                  = intentData.getStringExtra(DefineValue.AMOUNT);
            } else {
                txId                    = sp.getString(DefineValue.BBS_TX_ID, "");
                categoryName            = sp.getString(DefineValue.CATEGORY_NAME, "");
                amount                  = sp.getString(DefineValue.AMOUNT, "");
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}