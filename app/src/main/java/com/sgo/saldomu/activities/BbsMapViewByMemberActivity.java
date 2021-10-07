package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.balysv.materialripple.MaterialRippleLayout;
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
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fcm.FCMManager;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsMapViewByMemberActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SecurePreferences sp;
    String txId, memberPhone, categoryName, amount, cancelFee, urlProfilePicture;
    SupportMapFragment mapFrag;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    Double memberLatitude, memberLongitude, agentLatitude, agentLongitude;
    private GoogleMap globalMap;
    TextView tvMemberName, tvAmount, tvETA, tvAcctLabel, tvAcctName;
    Boolean isInquiryRoute = false;
    int distanceBetween = 0;
    String gcmId, emoMemberId;
    MaterialRippleLayout btnCall, btnCancel;
    ProgressDialog progDialog;
    Intent intentData;
    ImageView ivPhoto;

    private int timeDelayed = 30000;

    // Init
    private Handler handler = new Handler();
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {

            if (isInquiryRoute && DefineValue.MIN_DISTANCE_ALMOST_ARRIVE > distanceBetween) {
                showToast();
            }

            updateLocationMember();
            handler.postDelayed(this, timeDelayed);
        }
    };
    private Bitmap bitmap;
    private RoundImageTransformation roundedImage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if (sp.getBoolean(DefineValue.IS_AGENT, false)) {
            //is agent
            Intent intent = new Intent(this, MainPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        if (flagLogin.equals(DefineValue.STRING_NO)) {
            finish();
        } else {
            String notifDataNextLogin = sp.getString(DefineValue.NOTIF_DATA_NEXT_LOGIN, "");
            if (!notifDataNextLogin.equals("")) {
                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
            }
        }

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        emoMemberId = sp.getString(DefineValue.MEMBER_ID, "");
        gcmId = "";
        tvMemberName = findViewById(R.id.tvName);
        tvAmount = findViewById(R.id.tvAmount);
        tvETA = findViewById(R.id.tvETA);
        btnCall = findViewById(R.id.btnCall);
        btnCancel = findViewById(R.id.btnCancel);
        tvAcctLabel = findViewById(R.id.tvAcctLabel);
        tvAcctName = findViewById(R.id.tvAcctName);
        ivPhoto = findViewById(R.id.ivPhoto);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.agentMap);
        mapFrag.getMapAsync(this);
        mapFrag.getView().setVisibility(View.GONE);

        intentData = getIntent();

        if (intentData.hasExtra(DefineValue.BBS_TX_ID)) {
            Timber.d("isi intent amount oncreate %s", intentData.getStringExtra(DefineValue.AMOUNT));

            txId = intentData.getStringExtra(DefineValue.BBS_TX_ID);
            categoryName = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
            amount = intentData.getStringExtra(DefineValue.AMOUNT);
        } else {
            txId = sp.getString(DefineValue.BBS_TX_ID, "");
            categoryName = sp.getString(DefineValue.CATEGORY_NAME, "");
            amount = sp.getString(DefineValue.AMOUNT, "");
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
        agentLatitude = null;
        agentLongitude = null;

        /*btnDone.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        progdialog2              = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");

                        confirmTransactionMember();
                    }
                }
        );*/
        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + memberPhone));
            startActivity(callIntent);
        });

        btnCancel.setOnClickListener(
                v -> {

                    AlertDialog alertDialog = new AlertDialog.Builder(BbsMapViewByMemberActivity.this).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.alertbox_title_information));

                    String cancelMessage = getString(R.string.message_notif_cancel_trx);
                    String newCancelMessage = cancelMessage.replace("[CANCEL_FEE]", DefineValue.IDR + " " + CurrencyFormat.format(cancelFee));

                    alertDialog.setMessage(newCancelMessage);

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                            (dialog, which) -> {
                                progDialog = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");
                                cancelTransactionMember();
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();


                }
        );

        String title = categoryName;
        initializeToolbar(title);

//        TextView t = (TextView) findViewById(R.id.name);
//        t.setText(Html.fromHtml(getString(R.string.bbs_trx_detail_member)));
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        roundedImage = new RoundImageTransformation(bitmap);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.bbs_map_view_by_member_activity;
    }

    public void initializeToolbar(String title) {
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

        if (globalMap != null) {
            mapFrag.getView().setVisibility(View.VISIBLE);

        }
    }

    private void setMapCamera() {
        if (memberLatitude != 0 && memberLongitude != 0 && agentLatitude != null && agentLongitude != null) {
            globalMap.clear();

            //new BbsMapNagivationActivity.GoogleMapRouteDirectionTask(targetLatitude, targetLongitude, currentLatitude, currentLongitude).execute();
            new BbsMapViewByMemberActivity.GoogleMapRouteDirectionTask(memberLatitude, memberLongitude, agentLatitude, agentLongitude).execute();


            globalMap.getUiSettings().setMapToolbarEnabled(false);

            LatLng latLng = new LatLng(agentLatitude, agentLongitude);

            globalMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            LatLng targetLatLng = new LatLng(memberLatitude, memberLongitude);
            MarkerOptions markerTargetOptions = new MarkerOptions()
                    .position(targetLatLng)
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
        lastLocation = location;

        if (lastLocation != null) {
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

        if (!GlobalSetting.isLocationEnabled(this)) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.alertbox_gps_warning))
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, (dialog, id) -> {

                        Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(ilocation, 1);
                    })
                    .setNegativeButton(R.string.no, (dialog, id) -> {
                        dialog.cancel();
                        startActivity(new Intent(getApplicationContext(), MainPage.class));
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

    private boolean checkPlayServices() {
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

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);

            if (lastLocation != null) {
                memberLatitude = lastLocation.getLatitude();
                memberLongitude = lastLocation.getLongitude();
                setMapCamera();
                Timber.d("Location Found %s", lastLocation.toString());
                updateLocationMember();
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        if (bundle != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleApiClient.disconnect();
    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void updateLocationMember() {
        if (sp.getString(DefineValue.USERID_PHONE, "").equals(""))
            return;

        String extraSignature = txId;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_LOCATION_MEMBER,
                extraSignature);

        isInquiryRoute = false;

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);

        params.put(WebParams.KEY_PHONE, userPhoneID);
        params.put(WebParams.KEY_VALUE, gcmId);
        params.put(WebParams.LATITUDE, memberLatitude);
        params.put(WebParams.LONGITUDE, memberLongitude);
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params update member : %s", params.toString());
        handler.removeCallbacks(runnable2);
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_LOCATION_MEMBER, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);

                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                agentLatitude = response.getDouble(WebParams.SHOP_LATITUDE);
                                agentLongitude = response.getDouble(WebParams.SHOP_LONGITUDE);
                                cancelFee = String.valueOf(response.getDouble(WebParams.CANCEL_FEE));

                                tvMemberName.setText(response.getString(WebParams.SHOP_NAME));
                                memberPhone = NoHPFormat.formatTo08(response.getString(WebParams.SHOP_PHONE));
                                urlProfilePicture = response.getString(WebParams.PROFILE_PICTURE);
                                if (urlProfilePicture.isEmpty()) {
                                    GlideManager.sharedInstance().initializeGlide(BbsMapViewByMemberActivity.this, R.drawable.user_unknown_menu, roundedImage, ivPhoto);
                                } else {
                                    GlideManager.sharedInstance().initializeGlide(BbsMapViewByMemberActivity.this, urlProfilePicture, roundedImage, ivPhoto);
                                }
                                tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(amount));

                                if (response.getString(WebParams.SCHEME_CODE).equals(DefineValue.CTA)) {
                                    tvAcctLabel.setText(getString(R.string.bbs_setor_ke));
                                } else {
                                    tvAcctLabel.setText(getString(R.string.bbs_tarik_dari));
                                }

                                tvAcctName.setText(response.getString(WebParams.PRODUCT_NAME));

                                setMapCamera();
                                handler.postDelayed(runnable2, timeDelayed);

                            } else if (code.equals("0001") || code.equals("0012") || code.equals("0003") || code.equals("0005")) {
                                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
                                finish();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout.getInstance().showDialoginActivity(BbsMapViewByMemberActivity.this, message);
                            } else {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                handler.postDelayed(runnable2, timeDelayed);
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

    private void cancelTransactionMember() {

        String extraSignature = txId + sp.getString(DefineValue.MEMBER_ID, "");
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
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(BbsMapViewByMemberActivity.this, message, Toast.LENGTH_SHORT).show();
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

                        progDialog.dismiss();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if (id == android.R.id.home) {
            //kembali ke activity sebelumnya
            disabledBackPressed();
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }

            if (!GlobalSetting.isLocationEnabled(this)) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.alertbox_gps_warning))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, id) -> {

                            Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(ilocation, 1);

                        })
                        .setNegativeButton(R.string.no, (dialog, id) -> {
                            dialog.cancel();
                            startActivity(new Intent(getApplicationContext(), MainPage.class));
                            finish();
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

            intentData = getIntent();

            if (intentData.hasExtra(DefineValue.BBS_TX_ID)) {

                Timber.d("isi intent amount onresume %s", intentData.getStringExtra(DefineValue.AMOUNT));
                txId = intentData.getStringExtra(DefineValue.BBS_TX_ID);
                categoryName = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
                amount = intentData.getStringExtra(DefineValue.AMOUNT);
            } else {
                txId = sp.getString(DefineValue.BBS_TX_ID, "");
                categoryName = sp.getString(DefineValue.CATEGORY_NAME, "");
                amount = sp.getString(DefineValue.AMOUNT, "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GoogleMapRouteDirectionTask extends AsyncTask<Void, Void, Integer> {

        private Double dataCurrentLatitude;
        private Double dataCurrentLongitude;
        private Double targetLatitude;
        private Double targetLongitude;

        public GoogleMapRouteDirectionTask(Double targetLatitude, Double targetLongitude, Double currentLatitude, Double currentLongitude) {
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

            String nextParams = "origin=" + dataCurrentLatitude.toString() + "," + dataCurrentLongitude.toString();
            nextParams += "&sensor=false";
            nextParams += "&units=metric";
            nextParams += "&mode=" + DefineValue.GMAP_MODE;
            nextParams += "&language=" + Locale.getDefault().getLanguage();

//            RequestParams rqParams = new RequestParams();
//            rqParams.put("origin", agentLatitude.toString()+","+agentLongitude.toString());
//            rqParams.put("sensor", "false");
//            rqParams.put("units", "metric");
//            rqParams.put("mode", DefineValue.GMAP_MODE);
//            rqParams.put("language", Locale.getDefault().getLanguage() );

            HashMap<String, Object> query = MyApiClient.googleDestination();
            query.put("origin", dataCurrentLatitude.toString() + "," + dataCurrentLongitude.toString());
            query.put("destination", targetLatitude.toString() + "," + targetLongitude.toString());

            getGoogleMapRoute(query);
            return null;
        }

    }

    public void getGoogleMapRoute(HashMap<String, Object> query) {
        RetrofitService.getInstance().QueryRequestSSL(MyApiClient.LINK_GOOGLE_MAP_API_ROUTE, query,
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

                            distanceBetween = distance.getInt("value");

                            isInquiryRoute = true;

                            final String parseDuration = duration.getString("text");

                            runOnUiThread(() -> tvETA.setText(getString(R.string.estimated_time_arrived, parseDuration)));
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

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
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
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {

                    progDialog = DefinedDialog.CreateProgressDialog(BbsMapViewByMemberActivity.this, "");
                    cancelTransactionMember();
                })
                .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.dismiss())
        ;
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onBackPressed() {
        disabledBackPressed();
    }
}
