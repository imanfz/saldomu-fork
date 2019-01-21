package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.KeyEvent;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fragments.BBSTransaksiInformasi;
import com.sgo.saldomu.fragments.FragJoinCommunitySCADM;
import com.sgo.saldomu.fragments.NavigationDrawMenu;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsMapViewByAgentActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SecurePreferences sp;
    private String title;
    String txId, memberId, shopId;
    SupportMapFragment mapFrag;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    ProgressDialog progdialog, progdialog2;
    Double memberLatitude, memberLongitude, agentLatitude, agentLongitude, benefLatitude, benefLongitude;
    ShopDetail shopDetail;
    private GoogleMap globalMap;
    TextView tvCategoryName, tvMemberName, tvAmount, tvShop, tvDurasi, tvBbsNote, tvAcctLabel, tvAcctName;
    Boolean isFirstLoad = true, isRunning = false, isInquiryRoute = false;
    int distanceBetween = 0;
    List<Polyline> lines;
    Polyline line;
    String encodedPoints = "";
    String htmlDirections = "";
    //TextToSpeech textToSpeech;
    Boolean isTTSActive = true;
    Button btnTibaDiLokasi, btnCancel;
    private int timeDelayed = 30000;
    Intent intentData;

    // Init
    private Handler handler = new Handler();
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            isRunning = true;

            if (isInquiryRoute && DefineValue.MIN_DISTANCE_ALMOST_ARRIVE > distanceBetween) {
                //Toast.makeText(getApplicationContext(), getString(R.string.bbs_agent_almost_arrive), Toast.LENGTH_LONG).show();
            }

            updateLocationAgent();
            handler.postDelayed(this, timeDelayed);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
            //is member
            Intent i = new Intent(this, MainPage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        /*try {
            textToSpeech = new TextToSpeech(BbsMapViewByAgentActivity.this, BbsMapViewByAgentActivity.this);
            textToSpeech.setLanguage(Locale.getDefault());
        } catch ( Exception e) {
            e.printStackTrace();
            isTTSActive = false;
        }*/

        intentData = getIntent();

        txId = intentData.getStringExtra(DefineValue.AOD_TX_ID);
        lines = new ArrayList<>();
        tvCategoryName = (TextView) findViewById(R.id.tvCategoryName);
        tvMemberName = (TextView) findViewById(R.id.tvMemberName);
        tvAmount = (TextView) findViewById(R.id.tvAmount);
        tvDurasi = (TextView) findViewById(R.id.tvDurasi);
        //tvShop                  = (TextView) findViewById(R.id.tvShop);
        tvBbsNote = (TextView) findViewById(R.id.tvBbsNote);

        btnTibaDiLokasi = (Button) findViewById(R.id.btnTibaLokasi);
        btnCancel = findViewById(R.id.btnCancelDGI);
        tvAcctLabel = (TextView) findViewById(R.id.tvAcctLabel);
        tvAcctName = (TextView) findViewById(R.id.tvAcctName);

        shopDetail = new ShopDetail();
        shopDetail.setKeyCode(sp.getString(DefineValue.KEY_CODE, ""));
        shopDetail.setKeyName(sp.getString(DefineValue.KEY_NAME, ""));
        shopDetail.setCategoryName(sp.getString(DefineValue.CATEGORY_NAME, ""));
        shopDetail.setKeyProvince(sp.getString(DefineValue.KEY_PROVINCE, ""));
        shopDetail.setKeyDistrict(sp.getString(DefineValue.KEY_DISTRICT, ""));
        shopDetail.setKeyAddress(sp.getString(DefineValue.KEY_ADDRESS, ""));
        shopDetail.setAmount(sp.getString(DefineValue.KEY_AMOUNT, ""));
        shopDetail.setCcyId(sp.getString(DefineValue.KEY_CCY, ""));
        shopDetail.setShopId(sp.getString(DefineValue.BBS_SHOP_ID, ""));


        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.agentMap);
        mapFrag.getMapAsync(this);
        mapFrag.getView().setVisibility(View.GONE);

        if (txId == null) {
            txId = sp.getString(DefineValue.BBS_TX_ID, "");
        }
        memberId = sp.getString(DefineValue.BBS_MEMBER_ID, "");
        shopId = sp.getString(DefineValue.BBS_SHOP_ID, "");
        agentLatitude = sp.getDouble(DefineValue.AGENT_LATITUDE, 0.0);
        agentLongitude = sp.getDouble(DefineValue.AGENT_LONGITUDE, 0.0);

        benefLatitude = sp.getDouble(DefineValue.BENEF_LATITUDE, 0.0);
        benefLongitude = sp.getDouble(DefineValue.BENEF_LONGITUDE, 0.0);

        tvCategoryName.setText(shopDetail.getCategoryName());
        tvMemberName.setText(shopDetail.getKeyName());
        //tvShop.setText(shopDetail.getShopId());
        tvAmount.setText(shopDetail.getCcyId() + " " + CurrencyFormat.format(shopDetail.getAmount()));

        title = getString(R.string.menu_item_title_map_agent);
        initializeToolbar(title);

        TextView t = (TextView) findViewById(R.id.name);
        t.setText(Html.fromHtml(getString(R.string.bbs_trx_detail_agent)));

        btnTibaDiLokasi.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        progdialog2 = DefinedDialog.CreateProgressDialog(BbsMapViewByAgentActivity.this, "");

                        confirmTransactionByAgent();
                    }
                }
        );
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.bbs_map_view_by_agent_activity;
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
            setMapCamera();
        }
    }

    private void setMapCamera() {
        if (benefLatitude != 0 && benefLongitude != 0) {
            globalMap.clear();

            //new BbsMapNagivationActivity.GoogleMapRouteDirectionTask(targetLatitude, targetLongitude, currentLatitude, currentLongitude).execute();
            new BbsMapViewByAgentActivity.GoogleMapRouteDirectionTask(benefLatitude, benefLongitude, agentLatitude, agentLongitude).execute();

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

            LatLng targetLatLng = new LatLng(benefLatitude, benefLongitude);
            MarkerOptions markerTargetOptions = new MarkerOptions()
                    .position(targetLatLng)
                    //.title("")
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)));
            globalMap.addMarker(markerTargetOptions);

            ///if ( isFirstLoad ) {
            //isFirstLoad = false;

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
            //}

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        agentLatitude = lastLocation.getLatitude();
        agentLongitude = lastLocation.getLongitude();

        /*if ( progdialog != null ) {
            if ( progdialog.isShowing() ) progdialog.dismiss();
        }

        if ( !isRunning ) {
            handler.removeCallbacks(runnable2);
            updateLocationAgent();
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
        mLocationRequest.setInterval(DefineValue.AGENT_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.AGENT_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.AGENT_DISPLACEMENT);
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
        startLocationUpdate();

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {

                agentLatitude = lastLocation.getLatitude();
                agentLongitude = lastLocation.getLongitude();

                Timber.d("Location Found" + lastLocation.toString());
                updateLocationAgent();
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
        googleApiClient.disconnect();
        //textToSpeech.shutdown();
        try {
            handler.removeCallbacks(runnable2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void updateLocationAgent() {

        //temporary only
        //setMapCamera();

        isInquiryRoute = false;

        String extraSignature = txId + memberId + shopId;
        RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_UPDATE_LOCATION_AGENT,
                userPhoneID, accessKey, extraSignature);


        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.SHOP_ID, shopId);
        params.put(WebParams.MEMBER_ID, memberId);
        params.put(WebParams.LATITUDE, agentLatitude);
        params.put(WebParams.LONGITUDE, agentLongitude);
        params.put(WebParams.USER_ID, userPhoneID);
        //

        handler.removeCallbacks(runnable2);

        MyApiClient.updateLocationAgent(getApplication(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //if ( progdialog.isShowing())
                //progdialog.dismiss();

                try {
                    isRunning = false;
                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        benefLatitude = response.getDouble(WebParams.KEY_LATITUDE);
                        benefLongitude = response.getDouble(WebParams.KEY_LONGITUDE);

                        shopDetail.setKeyCode(response.getString(DefineValue.KEY_CODE));
                        shopDetail.setKeyName(response.getString(DefineValue.KEY_NAME));
                        shopDetail.setCategoryName(response.getString(DefineValue.CATEGORY_NAME));
                        //shopDetail.setKeyProvince(response.getString(DefineValue.KEY_PROVINCE));
                        //shopDetail.setKeyDistrict(response.getString(DefineValue.KEY_DISTRICT));
                        //shopDetail.setKeyAddress(response.getString(DefineValue.KEY_ADDRESS));
                        //shopDetail.setAmount(response.getString(DefineValue.KEY_AMOUNT));
                        //shopDetail.setCcyId(response.getString(DefineValue.KEY_CCY));

                        if (response.getString(WebParams.BBS_NOTE) != null) {
                            tvBbsNote.setText(response.getString(WebParams.BBS_NOTE));
                        } else {
                            tvBbsNote.setText("");
                        }
                        tvCategoryName.setText(response.getString(DefineValue.CATEGORY_NAME));

                        if (response.getString(WebParams.SCHEME_CODE).equals(DefineValue.CTA)) {
                            tvAcctLabel.setText(getString(R.string.bbs_setor_ke));
                        } else if (response.getString(WebParams.SCHEME_CODE).equalsIgnoreCase(DefineValue.DGI)) {
                            btnCancel.setVisibility(View.VISIBLE);
                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cancelDGI();
                                }
                            });
                        } else {
                            tvAcctLabel.setText(getString(R.string.bbs_tarik_dari));
                        }

                        tvAcctName.setText(response.getString(WebParams.PRODUCT_NAME));

                        if (response.has(DefineValue.KEY_TX_STATUS)) {
                            if (response.getString(DefineValue.KEY_TX_STATUS).equals(DefineValue.SUCCESS)) {

                                handler.removeCallbacks(runnable2);

                                Bundle bundle = new Bundle();
                                bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                                if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equals(DefineValue.CTA)) {
                                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                                } else if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equals(DefineValue.ATC)) {
                                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                                }

                                bundle.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(response.getString(DefineValue.AMOUNT))));
                                bundle.putString(DefineValue.KEY_CODE, response.getString(DefineValue.KEY_CODE));

                                Intent intent = new Intent(getApplicationContext(), BBSActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();

                            } else if (response.getString(DefineValue.KEY_TX_STATUS).equals(DefineValue.TX_STATUS_RJ)) {
                                Intent intent = new Intent(getApplicationContext(), MainPage.class);
                                startActivity(intent);
                                finish();
                            } else {

                                setMapCamera();
                                handler.postDelayed(runnable2, timeDelayed);
                            }
                        } else {
                            setMapCamera();
                            handler.postDelayed(runnable2, timeDelayed);
                        }
                    } else {
                        //if ( progdialog.isShowing())
                        //progdialog.dismiss();

                        code = response.getString(WebParams.ERROR_MESSAGE);
                        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), MainPage.class);
                        startActivity(intent);
                        finish();
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

                //if ( progdialog.isShowing() )
                //progdialog.dismiss();

                isRunning = false;
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdate() {
        try {
            PendingResult<Status> statusPendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
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

    /*@Override
    public void onInit(int Text2SpeechCurrentStatus) {

        if ( isTTSActive && Text2SpeechCurrentStatus == TextToSpeech.SUCCESS) {

            textToSpeech.setLanguage(Locale.getDefault());
            TextToSpeechFunction();
        }
    }*/

    private class GoogleMapRouteDirectionTask extends AsyncTask<Void, Void, Integer> {

        private ArrayList<ShopDetail> dataDetails = new ArrayList<>();
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

            RequestParams rqParams = new RequestParams();
            rqParams.put("origin", agentLatitude.toString() + "," + agentLongitude.toString());
            rqParams.put("sensor", "false");
            rqParams.put("units", "metric");
            rqParams.put("mode", DefineValue.GMAP_MODE);
            rqParams.put("language", Locale.getDefault().getLanguage());


            String tempParams = nextParams;
            tempParams += "&destination=" + targetLatitude.toString() + "," + targetLongitude.toString();

            getGoogleMapRoute(tempParams, 0);
            return null;
        }

    }

    public void getGoogleMapRoute(String tempParams, final int idx) {
        MyApiClient.getGoogleMapRoute(getApplicationContext(), tempParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Timber.w("Response google map route:" + response.toString());
                try {

                    JSONArray array = response.getJSONArray("routes");
                    JSONObject routes = array.getJSONObject(0);
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject steps = legs.getJSONObject(0);
                    JSONObject distance = steps.getJSONObject("distance");
                    JSONObject duration = steps.getJSONObject("duration");

                    distanceBetween = distance.getInt("value");

                    isInquiryRoute = true;

                    String parsedDistance = distance.getString("text");

                    int iDistance = distance.getInt("value");

                    final String parseDuration = duration.getString("text");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvDurasi.setText(parseDuration);
                        }
                    });

                    JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");

                    encodedPoints = points;

                    JSONArray directions = steps.getJSONArray("steps");

                    if (directions.length() > 0) {
                        JSONObject toDirection = directions.getJSONObject(0);
                        htmlDirections = toDirection.getString("html_instructions");

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

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });


    }

    public void setPolyline() {
        List<LatLng> list = new ArrayList<>();

        if (!encodedPoints.equals("") && globalMap != null) {

            if (lines.size() > 0) {
                Polyline dataLine = lines.get(0);
                dataLine.remove();

                lines.clear();
            }


            list = decodePoly(encodedPoints);
            line = globalMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(3)
                    .color(Color.RED)
                    .geodesic(true)
            );
            lines.add(line);


        }
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

    public void TextToSpeechFunction() {
        /*if ( !htmlDirections.equals("") ) {
            textToSpeech.speak(android.text.Html.fromHtml(htmlDirections).toString(), TextToSpeech.QUEUE_FLUSH, null);
        }*/
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
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                final AlertDialog alert = builder.create();
                alert.show();
            } else {
                Intent i = new Intent(this, BbsMapViewByAgentActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }
    }

    private void confirmTransactionByAgent() {
        if(progdialog2 == null)
            progdialog2 = DefinedDialog.CreateProgressDialog(this, "");
        else
            progdialog2.show();
        String extraSignature = txId;
        RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CONFIRM_TRANSACTION_BY_AGENT,
                userPhoneID, accessKey, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.KEY_VALUE, "");
        params.put(WebParams.SHOP_PHONE, userPhoneID);
        params.put(WebParams.USER_ID, userPhoneID);


        MyApiClient.confirmTransactionByAgent(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    if (progdialog2.isShowing())
                        progdialog2.dismiss();

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        benefLatitude = response.getDouble(WebParams.KEY_LATITUDE);
                        benefLongitude = response.getDouble(WebParams.KEY_LONGITUDE);

                        shopDetail.setKeyCode(response.getString(DefineValue.KEY_CODE));
                        shopDetail.setKeyName(response.getString(DefineValue.KEY_NAME));
                        shopDetail.setCategoryName(response.getString(DefineValue.CATEGORY_NAME));

                        tvCategoryName.setText(response.getString(DefineValue.CATEGORY_NAME));

                        if (response.has(DefineValue.KEY_TX_STATUS)) {
                            if (response.getString(DefineValue.KEY_TX_STATUS).equals(DefineValue.ONRECONCILED)) {

                                handler.removeCallbacks(runnable2);

                                Bundle bundle = new Bundle();
                                bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                                if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equals(DefineValue.CTA)) {
                                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                                } else if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equals(DefineValue.ATC)) {
                                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                                }

                                if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equalsIgnoreCase(DefineValue.DGI))
                                {
                                    Intent intent = new Intent(getApplicationContext(), TagihActivity.class);
                                    startActivity(intent);
                                    finish();

                                }else {

                                    bundle.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(response.getString(DefineValue.AMOUNT))));
                                    bundle.putString(DefineValue.KEY_CODE, response.getString(DefineValue.KEY_CODE));
                                    bundle.putString(DefineValue.PRODUCT_CODE, response.getString(WebParams.PRODUCT_CODE));

                                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                    SecurePreferences.Editor mEditor = prefs.edit();
                                    mEditor.putString(DefineValue.AOD_TX_ID, txId);
                                    mEditor.apply();

                                    Intent intent = new Intent(getApplicationContext(), BBSActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                }

                            } else if (response.getString(DefineValue.KEY_TX_STATUS).equals(DefineValue.TX_STATUS_RJ)) {
                                Intent intent = new Intent(getApplicationContext(), MainPage.class);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainPage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }


                    } else {
                        Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);

                        Intent intent = new Intent(getApplicationContext(), MainPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    handler.removeCallbacks(runnable2);


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
                if (progdialog2.isShowing())
                    progdialog2.dismiss();

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

    }

    public void cancelDGI() {
        if(progdialog2 == null)
            progdialog2 = DefinedDialog.CreateProgressDialog(this, "");
        else
            progdialog2.show();
        String extraSignature = txId;
        RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CANCEL_TRANSACTION_DGI,
                userPhoneID, accessKey, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params cancel transaction DGI : " + params.toString());


        MyApiClient.cancelTransactionDGI(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    if (progdialog2.isShowing())
                        progdialog2.dismiss();

                    String code = response.getString(WebParams.ERROR_CODE);
                    String error_message = response.getString(WebParams.ERROR_MESSAGE);
                    Timber.d("response cancel transaction DGI : " + response.toString());
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
                    }

                    handler.removeCallbacks(runnable2);

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

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error cancel transaction DGI : " + throwable.toString());

            }

        });
    }

    @Override
    public void onBackPressed() {
        disabledBackPressed();

        return;
    }

    private void disabledBackPressed() {
        //kembali ke activity sebelumnya

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alert_message_disabled_agent_approval_backpressed))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
                    }
                })
        ;
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        return false;

    }
}
