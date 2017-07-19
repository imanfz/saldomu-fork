package com.sgo.hpku.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CurrencyFormat;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.fragments.NavigationDrawMenu;
import com.sgo.hpku.models.ShopDetail;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import android.speech.tts.TextToSpeech;

import timber.log.Timber;

public class BbsMapViewByAgentActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, TextToSpeech.OnInitListener {

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
    TextView tvCategoryName, tvMemberName, tvAmount, tvShop;
    Boolean isFirstLoad = true;
    List<Polyline> lines;
    Polyline line;
    String encodedPoints = "";
    String htmlDirections   = "";
    TextToSpeech textToSpeech;
    Boolean isTTSActive = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp      = CustomSecurePref.getInstance().getmSecurePrefs();
        if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            //is member
            startActivity(new Intent(this, MainPage.class));
        }

        if ( checkPlayServices() ) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        try {
            textToSpeech = new TextToSpeech(BbsMapViewByAgentActivity.this, BbsMapViewByAgentActivity.this);
            textToSpeech.setLanguage(Locale.getDefault());
        } catch ( Exception e) {
            e.printStackTrace();
            isTTSActive = false;
        }

        lines                   = new ArrayList<>();
        tvCategoryName          = (TextView) findViewById(R.id.tvCategoryName);
        tvMemberName            = (TextView) findViewById(R.id.tvMemberName);
        tvAmount                = (TextView) findViewById(R.id.tvAmount);
        tvShop                  = (TextView) findViewById(R.id.tvShop);

        progdialog              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");
        shopDetail              = new ShopDetail();
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

        txId                    = sp.getString(DefineValue.BBS_TX_ID, "");
        memberId                = sp.getString(DefineValue.BBS_MEMBER_ID, "");
        shopId                  = sp.getString(DefineValue.BBS_SHOP_ID, "");
        agentLatitude           = sp.getDouble(DefineValue.AGENT_LATITUDE, -6.2271133);
        agentLongitude          = sp.getDouble(DefineValue.AGENT_LONGITUDE, 106.6578917);

        benefLatitude           = sp.getDouble(DefineValue.BENEF_LATITUDE, -6.222227);
        benefLongitude          = sp.getDouble(DefineValue.BENEF_LONGITUDE, 106.651973);

        tvCategoryName.setText(shopDetail.getCategoryName());
        tvMemberName.setText(shopDetail.getKeyName());
        //tvShop.setText(shopDetail.getShopId());
        tvAmount.setText(shopDetail.getCcyId()+" "+ CurrencyFormat.format(shopDetail.getAmount()));

        title                   = getString(R.string.menu_item_title_map_agent);
        initializeToolbar(title);

        TextView t = (TextView) findViewById(R.id.name);
        t.setText(Html.fromHtml(getString(R.string.bbs_trx_detail_agent)));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.bbs_map_view_by_agent_activity;
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
            setMapCamera();
        }
    }

    private void setMapCamera()
    {
        if ( benefLatitude != 0 && benefLongitude != 0 ) {
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
        lastLocation    = location;
        agentLatitude   = lastLocation.getLatitude();
        agentLongitude  = lastLocation.getLongitude();

        updateLocationAgent();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        } catch (RuntimeException e) {
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
        mLocationRequest.setInterval(DefineValue.AGENT_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.AGENT_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.AGENT_DISPLACEMENT);
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

            if ( lastLocation == null ){
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {

                agentLatitude     = lastLocation.getLatitude();
                agentLongitude    = lastLocation.getLongitude();

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
        googleApiClient.disconnect();
        textToSpeech.shutdown();
    }

    //for resize icon
    public Bitmap resizeMapIcons(int image, int width, int height)
    {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), image);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void updateLocationAgent() {

        //temporary only
        //setMapCamera();



        RequestParams params    = new RequestParams();


        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.SHOP_ID, shopId);
        params.put(WebParams.MEMBER_ID, memberId);
        params.put(WebParams.LATITUDE, agentLatitude);
        params.put(WebParams.LONGITUDE, agentLongitude);


        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + txId + memberId + shopId ));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.updateLocationAgent(getApplication(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

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

                        tvCategoryName.setText(response.getString(DefineValue.CATEGORY_NAME));

                        if ( response.has(DefineValue.KEY_TX_STATUS) ) {
                            if (response.getString(DefineValue.KEY_TX_STATUS).equals(DefineValue.SUCCESS)) {
                                Bundle bundle = new Bundle();
                                bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                                if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equals(DefineValue.CTA)) {
                                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                                } else if (response.getString(DefineValue.CATEGORY_SCHEME_CODE).equals(DefineValue.ATC)) {
                                    bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                                }
                                bundle.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(response.getString(DefineValue.AMOUNT))));

                                Intent intent = new Intent(getApplicationContext(), BBSActivity.class);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, 0);
                            }
                            else
                            {
                                setMapCamera();
                            }
                        }
                        else
                        {
                            setMapCamera();
                        }
                    } else {
                        //progdialog.dismiss();
                        code = response.getString(WebParams.ERROR_MESSAGE);
                        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                        startActivity(new Intent(getApplicationContext(), MainPage.class));
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
    public void onInit(int Text2SpeechCurrentStatus) {

        if ( isTTSActive && Text2SpeechCurrentStatus == TextToSpeech.SUCCESS) {

            textToSpeech.setLanguage(Locale.getDefault());
            TextToSpeechFunction();
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

            if ( isTTSActive && !htmlDirections.equals("") ) {
                //tvDirection.setText(Html.fromHtml(htmlDirections));
                TextToSpeechFunction();
            }
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
        MyApiClient.getGoogleMapRoute(getApplicationContext(), tempParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Timber.w("Error Koneksi login:" + response.toString());
                try {

                    JSONArray array = response.getJSONArray("routes");
                    JSONObject routes = array.getJSONObject(0);
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject steps = legs.getJSONObject(0);
                    JSONObject distance = steps.getJSONObject("distance");
                    String parsedDistance = distance.getString("text");

                    JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");

                    encodedPoints = points;

                    JSONArray directions = steps.getJSONArray("steps");

                    if ( directions.length() > 0 ) {
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

        if ( !encodedPoints.equals("") && globalMap != null) {

            if ( lines.size() > 0 ) {
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

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    public void TextToSpeechFunction()
    {
        if ( !htmlDirections.equals("") ) {
            textToSpeech.speak(android.text.Html.fromHtml(htmlDirections).toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void switchMenu(int idx_menu,Bundle data){
        if (getApplicationContext() == null)
            return;

        MainPage fca = (MainPage) getApplicationContext();
        fca.switchMenu(idx_menu, data);
    }
}
