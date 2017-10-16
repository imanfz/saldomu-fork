package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BbsSearchCategoryAdapter;
import com.sgo.saldomu.adapter.BbsShopCategoryList;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.models.ShopCategory;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import timber.log.Timber;

public class BbsSearchTokoActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    RecyclerView horizontal_recycler_view;
    SecurePreferences sp;
    ProgressDialog progdialog;
    JSONArray categories;
    ArrayList<ShopCategory> shopCategories = new ArrayList<>();
    BbsShopCategoryList shopCategoryAdapter;
    BbsSearchCategoryAdapter bbsSearchCategoryAdapter;
    ListView lvCategoryList, lvCategory;
    LinearLayout llHeaderProgress, llSearchView, llContent;
    ProgressBar pbHeaderProgress;
    private GoogleMap mMap;
    private Location mLastLocation;
    SearchView svSearch;
    RecyclerView rvHorizontal;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = true;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 0; // 0 meters
    public static Double longitude;
    public static Double latitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        horizontal_recycler_view = (RecyclerView) findViewById(R.id.rvHorizontal);


        //lvCategoryList          = (ListView) findViewById(R.id.lvCategoryList);
        //llHeaderProgress        = (LinearLayout) findViewById(R.id.llHeaderProgress);
        llContent                   = (LinearLayout) findViewById(R.id.llContent);
        //pbHeaderProgress            = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        llSearchView                = (LinearLayout) findViewById(R.id.llSearchView);
        svSearch                    = (SearchView) findViewById(R.id.svSearch);

        lvCategory                  = (ListView) findViewById(R.id.lvCategory);
        rvHorizontal                = (RecyclerView) findViewById(R.id.rvHorizontal);
        sp                          = CustomSecurePref.getInstance().getmSecurePrefs();

        bbsSearchCategoryAdapter    = new BbsSearchCategoryAdapter(BbsSearchTokoActivity.this, shopCategories, new BbsSearchCategoryAdapter.OnCategoryItemClickListener() {

            @Override
            public void onCategoryItemClick(View v, ShopCategory filterList) {
                requestSearchTokoByCategory(filterList);
            }
        });

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(BbsSearchTokoActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        rvHorizontal.setAdapter(bbsSearchCategoryAdapter);
        rvHorizontal.setVisibility(View.GONE);

        /*lvCategory.setAdapter(bbsSearchCategoryAdapter);
        lvCategory.setTextFilterEnabled(true);
        lvCategory.setVisibility(View.GONE);*/


        svSearch.setActivated(false);
        svSearch.setQueryHint("Type your keyword here");
        //svSearch.onActionViewExpanded();
        svSearch.setIconified(false);
        svSearch.clearFocus();
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                bbsSearchCategoryAdapter.getFilter().filter(newText);
                if ( !newText.isEmpty() ) {
                    rvHorizontal.setVisibility(View.VISIBLE);
                } else {
                    rvHorizontal.setVisibility(View.GONE);
                }
                return false;
            }
        });

        /*
        shopCategoryAdapter = new BbsShopCategoryList(BbsSearchTokoActivity.this, shopCategories);
        horizontalLayoutManager = new LinearLayoutManager(BbsSearchTokoActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        horizontal_recycler_view.setAdapter(shopCategoryAdapter);
        */

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.SHOP_ID, "");

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID));

        params.put(WebParams.SIGNATURE, signature);
        //llHeaderProgress.setVisibility(View.VISIBLE);
        //pbHeaderProgress.setVisibility(View.VISIBLE);

        MyApiClient.getCategoryList(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);
                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        categories = response.getJSONArray("category");

                        for (int i = 0; i < categories.length(); i++) {

                            JSONObject object = categories.getJSONObject(i);
                            ShopCategory shopCategory = new ShopCategory();
                            shopCategory.setCategoryId(object.getString("category_id"));
                            shopCategory.setCategoryName(object.getString("category_name"));
                            shopCategories.add(shopCategory);
                        }

                        bbsSearchCategoryAdapter.notifyDataSetChanged();
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
                pbHeaderProgress.setVisibility(View.GONE);

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    private void requestSearchTokoByCategory(ShopCategory shopCategory) {
        //Toast.makeText(v.getContext(), "Button Clicked " + categoryList.get(position).getCategoryCode(), Toast.LENGTH_SHORT).show();

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.CATEGORY_ID, shopCategory.getCategoryId());
        params.put(WebParams.LATITUDE, sp.getDouble(DefineValue.LAST_CURRENT_LATITUDE, 0.0));
        params.put(WebParams.LONGITUDE, sp.getDouble(DefineValue.LAST_CURRENT_LONGITUDE, 0.0));
        params.put(WebParams.RADIUS, 5);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + shopCategory.getCategoryId()
                + BbsSearchTokoActivity.latitude + BbsSearchTokoActivity.longitude));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.searchToko(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);
                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {


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

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplicationContext(), Resources.getSystem().getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_search_toko;
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_bbs_search_toko));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if ( latitude == null || longitude == null ) {
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();

                    SecurePreferences.Editor mEditor = sp.edit();
                    mEditor.putDouble(DefineValue.LAST_CURRENT_LATITUDE, latitude);
                    mEditor.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, longitude);
                    mEditor.apply();
                }
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }

        displayMap(latitude, longitude);

    }


    /**
     * Method to verify google play services on the device
     * */
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
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            SecurePreferences.Editor mEditor = sp.edit();
            mEditor.putDouble(DefineValue.LAST_CURRENT_LATITUDE, latitude);
            mEditor.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, longitude);
            mEditor.apply();
        }

        displayMap(latitude, longitude);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

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

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            SecurePreferences.Editor mEditor = sp.edit();
            mEditor.putDouble(DefineValue.LAST_CURRENT_LATITUDE, latitude);
            mEditor.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, longitude);
            mEditor.apply();
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch ( SecurityException se) {
            se.printStackTrace();
        }
    }

    public void displayMap(Double platitude, Double plongitude) {
        try {
            LatLng latLng;
            if ( platitude == null || plongitude == null ) {
                mMap.setMyLocationEnabled(true);
                Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if ( myLocation != null ) {
                    latLng = new LatLng(myLocation.getLatitude(),
                            myLocation.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                }
            } else {
                latLng = new LatLng(platitude, plongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            }

        } catch ( SecurityException se) {
            se.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {

            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
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

}
