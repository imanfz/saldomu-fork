package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
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
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fcm.FCMManager;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsMapViewByMemberActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SecurePreferences sp;
    private String title;
    String txId, memberId, shopId, categoryName, amount, cancelFee;
    SupportMapFragment mapFrag;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    Double memberLatitude, memberLongitude, agentLatitude, agentLongitude, benefLatitude, benefLongitude;
    ShopDetail shopDetail;
    private GoogleMap globalMap;
    TextView tvCategoryName, tvMemberName, tvAmount, tvShop, tvDurasi, tvAcctLabel, tvAcctName;
    Boolean isFirstLoad = true, isRunning = false, isInquiryRoute = false;
    int distanceBetween = 0;
    String gcmId, emoMemberId;
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

            if ( isInquiryRoute && DefineValue.MIN_DISTANCE_ALMOST_ARRIVE > distanceBetween ) {
                showToast();
            }

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

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        if ( flagLogin.equals(DefineValue.STRING_NO) ) {
            finish();
        } else {
            String notifDataNextLogin = sp.getString(DefineValue.NOTIF_DATA_NEXT_LOGIN, "");
            if (!notifDataNextLogin.equals("")) {
                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
            }
        }

        if ( checkPlayServices() ) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        emoMemberId             = sp.getString(DefineValue.MEMBER_ID, "");
        gcmId                   = "";
        tvCategoryName          = (TextView) findViewById(R.id.tvCategoryName);
        tvMemberName            = (TextView) findViewById(R.id.tvMemberName);
        tvAmount                = (TextView) findViewById(R.id.tvAmount);
        tvDurasi                = (TextView) findViewById(R.id.tvDurasi);
        //tvShop                  = (TextView) findViewById(R.id.tvShop);
        //btnDone                 = (Button) findViewById(R.id.btnDone);
        btnCancel               = (Button) findViewById(R.id.btnCancel);
        tvAcctLabel             = (TextView) findViewById(R.id.tvAcctLabel);
        tvAcctName              = (TextView) findViewById(R.id.tvAcctName);

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

        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(DefineValue.MODEL_NOTIF, FCMManager.SHOP_ACCEPT_TRX);
            jsonObj.put(DefineValue.BBS_TX_ID, txId);
            jsonObj.put(DefineValue.CATEGORY_NAME, categoryName);
            jsonObj.put(DefineValue.AMOUNT, amount);

            SecurePreferences.Editor mEditor = sp.edit();
            mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, jsonObj.toString());
            mEditor.apply();
        } catch (Exception e) {
        }

        //temporary only
        agentLatitude           = null;
        agentLongitude          = null;

        /*btnDone.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        progdialog2              = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");

                        confirmTransactionMember();
                    }
                }
        );*/

        btnCancel.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsMapViewByMemberActivity.this).create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.setTitle(getString(R.string.alertbox_title_information));

                        String cancelMessage = getString(R.string.message_notif_cancel_trx);
                        String newCancelMessage = cancelMessage.replace("[CANCEL_FEE]", DefineValue.IDR + " " + CurrencyFormat.format(cancelFee));

                        alertDialog.setMessage(newCancelMessage);

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
        if ( memberLatitude != 0 && memberLongitude != 0 && agentLatitude != null && agentLongitude != null ) {
            globalMap.clear();

            //new BbsMapNagivationActivity.GoogleMapRouteDirectionTask(targetLatitude, targetLongitude, currentLatitude, currentLongitude).execute();
            new BbsMapViewByMemberActivity.GoogleMapRouteDirectionTask(memberLatitude, memberLongitude, agentLatitude, agentLongitude).execute();


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

        if ( lastLocation != null ) {
            if (lastLocation.getLatitude() != memberLatitude)
                memberLatitude = lastLocation.getLatitude();

            if (lastLocation.getLongitude() != memberLongitude)
                memberLongitude = lastLocation.getLongitude();
        }
        /*if ( progdialog != null ) {
            if ( progdialog.isShowing() ) progdialog.dismiss();
        }

        if ( !isRunning ) {
            handler.removeCallbacks(runnable2);
            updateLocationMember();
            handler.postDelayed(runnable2, timeDelayed);
        }*/
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
                updateLocationMember();
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

        //progdialog              = DefinedDialog.CreateProgressDialog(this, "");
        String extraSignature = txId + memberLatitude + memberLongitude;
        RequestParams param            = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_UPDATE_LOCATION_MEMBER,
                userPhoneID, accessKey, extraSignature);
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_LOCATION_MEMBER,
                extraSignature);

        isInquiryRoute          = false;

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        //params.put(WebParams.SHOP_ID, shopId);
        //params.put(WebParams.MEMBER_ID, memberId);

        params.put(WebParams.KEY_PHONE, userPhoneID);
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.LATITUDE, memberLatitude);
        params.put(WebParams.LONGITUDE, memberLongitude);
        params.put(WebParams.USER_ID, userPhoneID);

        handler.removeCallbacks(runnable2);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_LOCATION_MEMBER, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            isRunning = false;
                            String code = response.getString(WebParams.ERROR_CODE);



                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                agentLatitude = response.getDouble(WebParams.SHOP_LATITUDE);
                                agentLongitude = response.getDouble(WebParams.SHOP_LONGITUDE);
                                cancelFee = String.valueOf(response.getDouble(WebParams.CANCEL_FEE));

                                tvMemberName.setText(response.getString(WebParams.SHOP_NAME));
                                //tvShop.setText(response.getString(WebParams.SHOP_NAME));
                                tvCategoryName.setText(categoryName);
                                tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(amount));

                                if ( response.getString(WebParams.SCHEME_CODE).equals(DefineValue.CTA) ) {
                                    tvAcctLabel.setText(getString(R.string.bbs_setor_ke));
                                } else {
                                    tvAcctLabel.setText(getString(R.string.bbs_tarik_dari));
                                }

                                tvAcctName.setText(response.getString(WebParams.PRODUCT_NAME));

                                setMapCamera();
                                handler.postDelayed(runnable2, timeDelayed);

                    /*
                    //remove redirection to rating page
                    } else if ( code.equals("9999") ) {

                        SecurePreferences.Editor mEditor = sp.edit();
                        mEditor.putString(DefineValue.BBS_MODULE, DefineValue.BBS_REVIEW);
                        mEditor.putString(DefineValue.URL_PROFILE_PICTURE, response.getString(WebParams.PROFILE_PICTURE));
                        mEditor.putString(DefineValue.CATEGORY_NAME, categoryName);
                        mEditor.putString(DefineValue.AMOUNT, amount);
                        mEditor.putString(DefineValue.BBS_TX_ID, txId);
                        mEditor.putString(DefineValue.BBS_SHOP_NAME, response.getString(WebParams.SHOP_NAME));
                        mEditor.putString(DefineValue.BBS_MAXIMUM_RATING, response.getString(WebParams.MAXIMUM_RATING));
                        mEditor.putString(DefineValue.BBS_DEFAULT_RATING, response.getString(WebParams.DEFAULT_RATING));
                        mEditor.apply();

                        sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();

                        Intent tempIntent = new Intent(getApplicationContext(), BBSActivity.class);
                        Bundle tempBundle = new Bundle();
                        tempBundle.putInt(DefineValue.INDEX, BBSActivity.BBSRATINGBYMEMBER);
                        tempBundle.putString(DefineValue.BBS_TX_ID, txId);
                        tempBundle.putString(DefineValue.CATEGORY_NAME, categoryName);
                        tempBundle.putString(DefineValue.AMOUNT, amount);
                        tempBundle.putString(DefineValue.URL_PROFILE_PICTURE, response.getString(WebParams.PROFILE_PICTURE));
                        tempBundle.putString(DefineValue.BBS_SHOP_NAME, response.getString(WebParams.SHOP_NAME));
                        tempBundle.putString(DefineValue.BBS_MAXIMUM_RATING, response.getString(WebParams.MAXIMUM_RATING));
                        tempBundle.putString(DefineValue.BBS_DEFAULT_RATING, response.getString(WebParams.DEFAULT_RATING));
                        tempIntent.putExtras(tempBundle);
                        startActivity(tempIntent);
                        finish();
*/

                            } else if ( code.equals("0001") || code.equals("0012") || code.equals("0003") || code.equals("0005") ) {

                                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
                                finish();

                            } else {
                                //progdialog.dismiss();
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
                                handler.postDelayed(runnable2, timeDelayed);
                                //startActivity(new Intent(getApplicationContext(), MainPage.class));
                            }

                        } catch (JSONException e) {
                            //Timber.d(String.valueOf(e.printStackTrace()));
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                        isRunning = false;
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
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.KEY_PHONE, sp.getString(DefineValue.USERID_PHONE, ""));

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.APP_ID + txId + sp.getString(DefineValue.USERID_PHONE, "")));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.confirmTransactionMember(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {

                    if ( progdialog2.isShowing())
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

                    handler.removeCallbacks(runnable2);

                    Intent intent = new Intent(getApplicationContext(), MainPage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

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
                if ( progdialog2.isShowing())
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

        String extraSignature = txId + sp.getString(DefineValue.MEMBER_ID, "");
        RequestParams param            = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CANCEL_TRANSACTION_MEMBER,
                userPhoneID, accessKey, extraSignature);
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_TRANSACTION_MEMBER, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.KEY_PHONE, userPhoneID);
        params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
        params.put(WebParams.USER_ID, userPhoneID);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CANCEL_TRANSACTION_MEMBER, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);

                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();

                                Intent intent = new Intent(getApplicationContext(), MainPage.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.msg_notif_tidak_bisa_batal), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                        progdialog2.dismiss();
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
            disabledBackPressed();
        }

        return true;
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

    private class GoogleMapRouteDirectionTask extends AsyncTask<Void, Void, Integer> {

        private ArrayList<ShopDetail> dataDetails = new ArrayList<>();
        private Double dataCurrentLatitude;
        private Double dataCurrentLongitude;
        private Double targetLatitude;
        private Double targetLongitude;

        public GoogleMapRouteDirectionTask(Double targetLatitude, Double targetLongitude, Double currentLatitude, Double currentLongitude)
        {
            this.targetLatitude = targetLatitude;
            this.targetLongitude = targetLongitude;
            dataCurrentLatitude = currentLatitude;
            dataCurrentLongitude = currentLongitude;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            /*if ( isTTSActive && !htmlDirections.equals("") ) {
                //tvDirection.setText(Html.fromHtml(htmlDirections));
                TextToSpeechFunction();
            }*/
            setPolyline();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            String nextParams = "origin="+dataCurrentLatitude.toString()+","+dataCurrentLongitude.toString();
            nextParams += "&sensor=false";
            nextParams += "&units=metric";
            nextParams += "&mode="+DefineValue.GMAP_MODE;
            nextParams += "&language="+ Locale.getDefault().getLanguage();

            RequestParams rqParams = new RequestParams();
            rqParams.put("origin", agentLatitude.toString()+","+agentLongitude.toString());
            rqParams.put("sensor", "false");
            rqParams.put("units", "metric");
            rqParams.put("mode", DefineValue.GMAP_MODE);
            rqParams.put("language", Locale.getDefault().getLanguage() );


            String tempParams = nextParams;
            tempParams += "&destination=" + targetLatitude.toString() + "," + targetLongitude.toString();

            getGoogleMapRoute(tempParams, 0);
            return null;
        }

    }

    public void getGoogleMapRoute(String tempParams, final int idx) {
        RetrofitService.getInstance().GetObjectRequest(MyApiClient.LINK_GOOGLE_MAP_API_ROUTE + "?" + tempParams,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            JSONArray array = response.getJSONArray("routes");
                            JSONObject routes = array.getJSONObject(0);
                            JSONArray legs = routes.getJSONArray("legs");
                            JSONObject steps = legs.getJSONObject(0);
                            JSONObject distance = steps.getJSONObject("distance");
                            JSONObject duration = steps.getJSONObject("duration");

                            String parsedDistance = distance.getString("text");
                            distanceBetween = distance.getInt("value");

                            isInquiryRoute = true;
                    /*if ( DefineValue.MIN_DISTANCE_ALMOST_ARRIVE > iDistance ) {



                    }*/

                            final String parseDuration =  duration.getString("text");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvDurasi.setText(parseDuration);
                                }
                            });


                            JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                            String points = overviewPolyline.getString("points");

                            //encodedPoints = points;

                            JSONArray directions = steps.getJSONArray("steps");

                            if ( directions.length() > 0 ) {
                                JSONObject toDirection = directions.getJSONObject(0);
                                //htmlDirections = toDirection.getString("html_instructions");

                        /*JSONArray toDistanceArray = toDirection.getJSONArray("distance");
                        JSONObject toDistanceObject = toDistanceArray.getJSONObject(0);
                        String toDistanceString = toDistanceObject.getString("text");

                        htmlDirections += " ( " + toDistanceString + " ) ";
                        //tvDirection.setText(Html.fromHtml(toDirection.getString("html_instructions")));
                        */
                            }




                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void setPolyline() {

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    private void showToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.bbs_agent_almost_arrive), Toast.LENGTH_LONG).show();
    }

    private void disabledBackPressed() {
        //kembali ke activity sebelumnya

        String cancelMessage = getString(R.string.message_notif_cancel_trx);
        String newCancelMessage = cancelMessage.replace("[CANCEL_FEE]", DefineValue.IDR + " " + CurrencyFormat.format(cancelFee));


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(newCancelMessage)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        progdialog2              = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");
                        cancelTransactionMember();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
                    }
                })
        ;
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onBackPressed() {
        disabledBackPressed();
        return;
    }
}
