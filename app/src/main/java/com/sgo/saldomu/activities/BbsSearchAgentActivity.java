package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.material.tabs.TabLayout;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.TabSearchAgentAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.GoogleAPIUtils;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.fragments.AgentListFragment;
import com.sgo.saldomu.fragments.FragCancelTrxRequest;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsSearchAgentActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        EasyPermissions.PermissionCallbacks,
        AgentListFragment.OnListAgentItemClick,
        FragCancelTrxRequest.CancelTrxRequestListener, OnMapsSdkInitializedCallback {

    private int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    private String searchLocationString;
    Intent intentData;
    private String[] menuItems;
    TabSearchAgentAdapter tabPageAdapter;
    private GoogleApiClient googleApiClient;
    public MainResultReceiver agentMapResultReceiver;
    public MainResultReceiver agentListMapResultReceiver;
    private Location lastLocation;
    private String gcmId;

    private boolean isAllowed = false, isCalled = false;
    private LocationRequest mLocationRequest;

    public ViewPager viewPager;
    private String categoryId, categoryName, bbsNote;
    private String mobility, amount;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private Double currentLatitude;
    private Double currentLongitude;
    SecurePreferences sp;
    private String completeAddress, districtName, provinceName, txId,
            bbsProductName, bbsProductCode, bbsProductType, bbsProductDisplay, bbsSchemeCode;
    private int timeDelayed = 30000;
    private static final int RC_LOCATION_PERM = 500;
    private final int RC_SEND_SMS = 504;
    private static final int RC_LOCATION_PHONE_SMS = 505;
    private static final int RC_GPS_REQUEST = 1;
    private ImageView imgDelete;

    Boolean clicked = false;
    ProgressDialog progDialog;
    private Realm realmBBSMemberBank;

    // Init
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkTransactionMember();
            handler.postDelayed(this, timeDelayed);
        }
    };

    //FragmentManager fragment;
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
//    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        realmBBSMemberBank = Realm.getInstance(RealmManager.BBSMemberBankConfiguration);

        intentData = getIntent();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        categoryId = intentData.getStringExtra(DefineValue.CATEGORY_ID);
        mobility = intentData.getStringExtra(DefineValue.BBS_AGENT_MOBILITY);
        categoryName = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
        amount = intentData.getStringExtra(DefineValue.AMOUNT);
        completeAddress = intentData.getStringExtra(DefineValue.BBS_COMPLETE_ADDRESS);
        bbsNote = intentData.getStringExtra(DefineValue.BBS_NOTE);
        bbsProductName = intentData.getStringExtra(DefineValue.BBS_PRODUCT_NAME);
        bbsSchemeCode = intentData.getStringExtra(DefineValue.BBS_SCHEME_CODE);

        BBSBankModel bbsBankModel = null;

        if (bbsSchemeCode.equals(DefineValue.CTA)) {
            bbsBankModel = realmBBSMemberBank.where(BBSBankModel.class).
                    equalTo(BBSBankModel.SCHEME_CODE, DefineValue.CTA).
                    equalTo(BBSBankModel.PRODUCT_NAME, bbsProductName)
                    .findFirst();
        } else if (bbsSchemeCode.equals(DefineValue.CTR)) {
            bbsBankModel = realmBBSMemberBank.where(BBSBankModel.class).
                    equalTo(BBSBankModel.SCHEME_CODE, DefineValue.CTR).
                    equalTo(BBSBankModel.PRODUCT_NAME, bbsProductName)
                    .findFirst();
        } else {
            bbsBankModel = realmBBSMemberBank.where(BBSBankModel.class).
                    equalTo(BBSBankModel.SCHEME_CODE, DefineValue.ATC).
                    equalTo(BBSBankModel.PRODUCT_NAME, bbsProductName)
                    .findFirst();
        }

        if (bbsBankModel != null) {
            bbsProductCode = bbsBankModel.getProduct_code();
            bbsProductType = bbsBankModel.getProduct_type();
            bbsProductDisplay = bbsBankModel.getProduct_display();
        }

        methodRequiresTwoPermission();

        txId = "";

        gcmId = "";

        initializeToolbar(getString(R.string.search_agent) + " " + categoryName);
    }

    public void runningApp() {

        if (isCalled) {
            return;
        } else {
            isCalled = true;
        }

        if (intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE)) {
            currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
            currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);

        }

        menuItems = getResources().getStringArray(R.array.list_tab_bbs_search_agent);
        tabPageAdapter = new TabSearchAgentAdapter(getSupportFragmentManager(), getApplicationContext(), menuItems, shopDetails, currentLatitude, currentLongitude, mobility, completeAddress);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(tabPageAdapter);

        imgDelete = findViewById(R.id.imgCancel);

        if (!mobility.equals(DefineValue.STRING_NO)) {
            imgDelete.setOnClickListener(this);
        }
        imgDelete.setVisibility(View.INVISIBLE);


        if (intentData.hasExtra(DefineValue.IS_AUTOSEARCH)) {
            if (intentData.getStringExtra(DefineValue.IS_AUTOSEARCH).equals(DefineValue.STRING_YES)) {
                //currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
                //currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);

                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                SecurePreferences.Editor mEditor = prefs.edit();
                mEditor.putString(DefineValue.BBS_TX_ID, "");
                mEditor.putString(DefineValue.AMOUNT, amount);
                mEditor.apply();

                Timber.d("Masuk Sini runningApp()");
                //searchToko(currentLatitude, currentLongitude);

                getCompleteLocationAddress();
            }
        }


        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        process();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_search_agent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //listener ketika button back di action bar diklik
        if (id == android.R.id.home) {
            //kembali ke activity sebelumnya
            onBackPressed();
        }

        if (mobility.equals(DefineValue.STRING_NO)) {
            return super.onOptionsItemSelected(item);
        } else {
            return true;
        }
    }

    public void initializeToolbar(String title) {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    private void custominitializeToolbar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left);

        TextView toolbarTitle = findViewById(R.id.main_toolbar_title);
        toolbarTitle.setText("Cari Agen");
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgCancel) {
            FragCancelTrxRequest fragCancelTrxRequest = new FragCancelTrxRequest();

            Bundle bundle = new Bundle();
            bundle.putString(DefineValue.CUST_ID, sp.getString(DefineValue.USERID_PHONE, ""));
            bundle.putString(DefineValue.TX_ID, sp.getString(DefineValue.BBS_TX_ID, ""));

            fragCancelTrxRequest.setArguments(bundle);
            fragCancelTrxRequest.setCancelable(false);
//            fragCancelTrxRequest.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
            fragCancelTrxRequest.show(getSupportFragmentManager(), FragCancelTrxRequest.TAG);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Timber.d("onConnected Started");
        //startLocationUpdate();

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {
                //btnProses.setEnabled(true);
                if (mobility.equals(DefineValue.STRING_NO) && currentLatitude != null) {

                } else {
                    //currentLatitude = lastLocation.getLatitude();
                    //currentLongitude = lastLocation.getLongitude();
                }

                Timber.d("Location Found%s", lastLocation.toString());
                //viewPager.getAdapter().notifyDataSetChanged();
                //getCompleteLocationAddress();
                //searchToko(lastLocation.getLatitude(), lastLocation.getLongitude());
                googleApiClient.disconnect();

                //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
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
    public void onLocationChanged(Location location) {
        lastLocation = location;
        //btnProses.setEnabled(true);
//        googleApiClient.disconnect();

        if (mobility.equals(DefineValue.STRING_NO) && currentLatitude != null) {

        } else {
            //currentLatitude = lastLocation.getLatitude();
            //currentLongitude = lastLocation.getLongitude();
        }

        //viewPager.getAdapter().notifyDataSetChanged();
        //getCompleteLocationAddress();

        if (mobility.equals(DefineValue.STRING_NO)) {
            //searchToko(currentLatitude, currentLongitude);
        }

        googleApiClient.disconnect();

        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //startIntentService(location);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void mainProcess() {
        //jika google play service tersedia, maka :
        if (checkPlayServices()) {
            //set receiver
            agentMapResultReceiver = new MainResultReceiver(new Handler());
            agentListMapResultReceiver = new MainResultReceiver(new Handler());

            buildGoogleApiClient();
            createLocationRequest();
        }
    }

    private boolean checkPlayServices() {
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


    private void process() {
        mainProcess();
    }

    private Double convertToKm(Double value) {
        //absolute - menghilangkan minus jika ada
        value = Math.abs(value);

        //get degree & convert to meter
        double degree = Math.floor(value);
        double degreeKm = degree * 111.322;   //111.320

        //get hour & convert to meter
        double hour = value % 1;
        double hourMinute = hour * 60;
        double hourKm = hourMinute * 1.88537;  //1.855

        return degreeKm + hourKm;
    }

    private void getCompleteLocationAddress() {

        completeAddress = "";

        HashMap<String, Object> options = new HashMap<>();
        options.put("sensor", false);
        options.put("key", MyApiClient.getGoogleMapsKeyWS());
        options.put("language", "id");
        options.put("latlng", currentLatitude + "," + currentLongitude);

        RetrofitService.getInstance().QueryRequestSSL(
                MyApiClient.LINK_GOOGLE_MAPS_API_GEOCODE_BASE
                , options,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String status = response.getString(WebParams.GMAP_API_STATUS);
                            Timber.w("JSON Response: %s", response.toString());

                            if (status.equals(DefineValue.GMAP_STRING_OK)) {
                                ArrayList<HashMap<String, String>> gData = GoogleAPIUtils.getResponseGoogleAPI(response);

                                for (HashMap<String, String> hashMapObject : gData) {
                                    for (String key : hashMapObject.keySet()) {
                                        switch (key) {
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
                                            case "country":
                                                break;
                                        }
                                    }
                                }
                                if (completeAddress.equals("")) {
                                    completeAddress += districtName + ", ";
                                    completeAddress += provinceName;
                                }
                                if (isAllowed)
                                    searchToko(currentLatitude, currentLongitude);
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

    private void searchToko(Double latitude, Double longitude) {

        if (!EasyPermissions.hasPermissions(this, perms) || !GlobalSetting.isLocationEnabled(this)) {
            return;
        }

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        txId = sp.getString(DefineValue.BBS_TX_ID, "");

        if (mobility.equals(DefineValue.STRING_NO)) {
            clicked = false;
        }

        if (txId.equals("") && !amount.equals("") && !clicked) {

            clicked = true;
            progDialog = DefinedDialog.CreateProgressDialog(this, getString(R.string.menu_item_search_agent));

            String extraSignature = categoryId + bbsProductType + bbsProductCode;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_SEARCH_TOKO,
                    extraSignature);

            SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
            SecurePreferences.Editor mEditor = prefs.edit();
            mEditor.putDouble(DefineValue.LAST_LATITUDE, latitude);
            mEditor.putDouble(DefineValue.LAST_LONGITUDE, longitude);
            mEditor.apply();

            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.CATEGORY_ID, categoryId);
            params.put(WebParams.LATITUDE, latitude);
            params.put(WebParams.LONGITUDE, longitude);
            params.put(WebParams.RADIUS, DefineValue.MAX_RADIUS_SEARCH_AGENT);
            params.put(WebParams.BBS_MOBILITY, mobility);
            params.put(WebParams.BBS_NOTE, bbsNote);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.SHOP_TYPE, sp.getString(DefineValue.COMPANY_TYPE, ""));

            params.put(WebParams.PRODUCT_CODE, bbsProductCode);
            params.put(WebParams.PRODUCT_NAME, bbsProductName);
            params.put(WebParams.PRODUCT_TYPE, bbsProductType);
            params.put(WebParams.PRODUCT_DISPLAY, bbsProductDisplay);

            if (mobility.equals(DefineValue.STRING_YES)) {
                params.put(WebParams.KEY_VALUE, gcmId);
                params.put(WebParams.KEY_CCY, DefineValue.IDR);
                params.put(WebParams.KEY_CODE, sp.getString(DefineValue.USERID_PHONE, ""));
                params.put(WebParams.KEY_PHONE, sp.getString(DefineValue.USERID_PHONE, ""));
                params.put(WebParams.KEY_NAME, sp.getString(DefineValue.CUST_NAME, ""));
                params.put(WebParams.KEY_ADDRESS, getAddress(longitude, latitude));
                params.put(WebParams.KEY_AMOUNT, amount);
                params.put(WebParams.KEY_EMAIL, sp.getString(DefineValue.PROFILE_EMAIL, ""));

                //Start
                handler.postDelayed(runnable, timeDelayed);
            }

            Timber.d("Params search toko :%s", params);

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SEARCH_TOKO, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            //llHeaderProgress.setVisibility(View.GONE);
                            //pbHeaderProgress.setVisibility(View.GONE);
//                            Timber.d("Response search toko:" + response.toString());

                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    clicked = true;
                                    txId = response.getString(DefineValue.TX_ID2);

                                    JSONArray shops = response.getJSONArray("shop");

                                    shopDetails.clear();

                                    if (shops.length() > 0) {

                                        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                        SecurePreferences.Editor mEditor = prefs.edit();
                                        mEditor.putString(DefineValue.BBS_TX_ID, response.getString(WebParams.TX_ID));
                                        mEditor.putString(DefineValue.CATEGORY_NAME, categoryName);
                                        mEditor.putString(DefineValue.AMOUNT, amount);
                                        mEditor.apply();

                                        for (int j = 0; j < shops.length(); j++) {
                                            JSONObject object = shops.getJSONObject(j);
                                            ShopDetail shopDetail = new ShopDetail();

                                            shopDetail.setShopName(object.getString(WebParams.SHOP_NAME));
                                            shopDetail.setMemberCust(object.getString("member_cust"));
                                            shopDetail.setMemberId(object.getString(WebParams.MEMBER_ID));
                                            shopDetail.setShopLatitude(object.getDouble("shop_latitude"));
                                            shopDetail.setShopLongitude(object.getDouble("shop_longitude"));
                                            shopDetail.setMemberName(object.getString(WebParams.MEMBER_NAME));
                                            shopDetail.setShopAddress(object.getString("shop_address"));
                                            shopDetail.setUrlSmallProfilePicture(object.getString("shop_picture"));
                                            shopDetail.setLastActivity(object.getString("shop_lastactivity"));
                                            shopDetail.setShopMobility(object.getString("shop_mobility"));
                                            shopDetail.setShopScore(object.getString("shop_score"));
                                            shopDetail.setShopCount(object.getString("shop_count"));
                                            shopDetail.setNumStars(Integer.valueOf(response.getString(WebParams.MEMBER_MAX_RATING)));
                                            shopDetails.add(shopDetail);
                                        }
                                    }

                                    //tabPageAdapter.notifyDataSetChanged();

                                    if (mobility.equals(DefineValue.STRING_YES)) {

                                        imgDelete.setVisibility(View.VISIBLE);

                                        //popup
                                        AlertDialog alertDialog = new AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                        alertDialog.setCanceledOnTouchOutside(false);
                                        alertDialog.setCancelable(false);
                                        alertDialog.setTitle(getString(R.string.alertbox_title_information));


                                        alertDialog.setMessage(getString(R.string.message_notif_waiting_agent_approval));


                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                                (dialog, which) -> dialog.dismiss());
                                        alertDialog.show();

                                    }

                                } else if (code.equals(WebParams.INPROGRESS_CODE)) {
                                    androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                    alertDialog.setCanceledOnTouchOutside(false);
                                    alertDialog.setCancelable(false);
                                    alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                    String tempMessage = getString(R.string.alertbox_message_search_agent_inprogress_trx);
                                    alertDialog.setMessage(tempMessage);

                                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                            (dialog, which) -> {
                                                dialog.dismiss();

                                                Intent i = new Intent(getApplicationContext(), MainPage.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(i);
                                                finish();

                                            });
                                    alertDialog.show();
                                } else {
                                    shopDetails.clear();
                                    //Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);


                                    if (mobility.equals(DefineValue.STRING_YES)) {

                                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                        alertDialog.setCanceledOnTouchOutside(false);
                                        alertDialog.setCancelable(false);
                                        alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                        String tempMessage = getString(R.string.alertbox_message_search_agent_not_found);
                                        alertDialog.setMessage(tempMessage);

                                        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                                                (dialog, which) -> {
                                                    dialog.dismiss();

                                                    Intent i = new Intent(getApplicationContext(), BbsSearchAgentActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                                                    i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                                                    i.putExtra(DefineValue.LAST_CURRENT_LATITUDE, currentLatitude);
                                                    i.putExtra(DefineValue.LAST_CURRENT_LONGITUDE, currentLongitude);
                                                    i.putExtra(DefineValue.BBS_PRODUCT_NAME, bbsProductName);
                                                    i.putExtra(DefineValue.BBS_SCHEME_CODE, bbsSchemeCode);

                                                    if (mobility.equals(DefineValue.STRING_YES)) {

                                                        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                                                        i.putExtra(DefineValue.AMOUNT, amount);
                                                        i.putExtra(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);


                                                    } else {
                                                        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                                                        i.putExtra(DefineValue.AMOUNT, "");

                                                    }

                                                    startActivity(i);
                                                    finish();

                                                });


                                        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                                                (dialog, which) -> {
                                                    dialog.dismiss();

                                                    SecurePreferences prefs1 = CustomSecurePref.getInstance().getmSecurePrefs();
                                                    SecurePreferences.Editor mEditor1 = prefs1.edit();
                                                    mEditor1.remove(DefineValue.BBS_AGENT_MOBILITY);
                                                    mEditor1.remove(DefineValue.BBS_TX_ID);
                                                    mEditor1.remove(DefineValue.AMOUNT);
                                                    mEditor1.apply();

                                                    finish();

                                                });
                                        alertDialog.show();


                                    } else {
                                        //alertDialog.setMessage(getString(R.string.alertbox_message_search_agent_fixed_not_found));

                                        Intent i = new Intent(getApplicationContext(), MainPage.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                                new GoogleMapRouteTask(shopDetails, currentLatitude, currentLongitude).execute();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            if (progDialog.isShowing())
                                progDialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            if (progDialog.isShowing())
                                progDialog.dismiss();
                        }
                    });
        }

    }

    String getAddress(double lang, double lat) {
        // 1
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        Address address;
        String addressText = "";

        try {
            // 2
            addresses = geocoder.getFromLocation(lat, lang, 1);
            // 3
            if (addresses != null && !addresses.isEmpty()) {
                address = addresses.get(0);
                int c = address.getMaxAddressLineIndex();
                for (int i = 0; i <= c; i++) {
                    if (i == 0) {
                        addressText += address.getAddressLine(i);
                    } else addressText += "\n" + address.getAddressLine(i);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressText;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.FASTEST_INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.DISPLACEMENT);
    }

    public ArrayList<Double> getCurrentCoordinate() {
        ArrayList<Double> tempCoordinate = new ArrayList<>();
        if (currentLatitude != null) {
            tempCoordinate.add(currentLatitude);
        } else {
            tempCoordinate.add(0.0);
        }

        if (currentLongitude != null) {
            tempCoordinate.add(currentLongitude);
        } else {
            tempCoordinate.add(0.0);
        }

        return tempCoordinate;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //runningApp();
        switch (requestCode) {
            //case RC_LOCATION_PERM:
            case RC_LOCATION_PHONE_SMS:

                if (intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE)) {
                    currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
                    currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);
                }

                if (intentData.hasExtra(DefineValue.IS_AUTOSEARCH)) {
                    if (intentData.getStringExtra(DefineValue.IS_AUTOSEARCH).equals(DefineValue.STRING_YES)) {
                        currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
                        currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);

                        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                        SecurePreferences.Editor mEditor = prefs.edit();
                        mEditor.putString(DefineValue.BBS_TX_ID, "");
                        mEditor.putString(DefineValue.AMOUNT, amount);
                        mEditor.apply();

                        searchToko(currentLatitude, currentLongitude);


                    }
                } else {

                    Intent i = new Intent(getApplicationContext(), BbsSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                    i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, mobility);
                    i.putExtra(DefineValue.AMOUNT, "");
                    i.putExtra(DefineValue.BBS_PRODUCT_NAME, bbsProductName);
                    i.putExtra(DefineValue.BBS_SCHEME_CODE, bbsSchemeCode);
                    startActivityForResult(i, MainPage.ACTIVITY_RESULT);
                    //startActivity(i);
                    finish();
                }
                break;

        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        AlertDialog alertDialog = new AlertDialog.Builder(BbsSearchAgentActivity.this).create();

        switch (requestCode) {
            case RC_LOCATION_PERM:
                // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
                // This will display a dialog directing them to enable the permission in app settings.
                /*if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this).build().show();
                } else {*/

                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.setTitle(getString(R.string.alertbox_title_warning));
                alertDialog.setMessage(getString(R.string.alertbox_message_warning));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
                alertDialog.show();
                //}
                break;
            case RC_LOCATION_PHONE_SMS:
                /*if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this).build().show();
                } else {*/
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.setTitle(getString(R.string.alertbox_title_warning));
                alertDialog.setMessage(getString(R.string.alertbox_message_phone_permission_warning));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
                alertDialog.show();
                //}
                break;
            case RC_SEND_SMS:
                break;
            default:
                finish();
                break;
        }

    }

    @Override
    public void OnIconLocationClickListener(int position) {


        if (shopDetails.size() > 0) {
            for (int idx = 0; idx < shopDetails.size(); idx++) {
                if (position == idx) {
                    shopDetails.get(idx).setIsPolyline("1");
                } else {
                    shopDetails.get(idx).setIsPolyline("0");
                }
            }

        }

        viewPager.setCurrentItem(0);
        viewPager.arrowScroll(View.FOCUS_LEFT);
        tabPageAdapter.OnLocationClickListener(position, shopDetails);
    }

    @Override
    public void onSuccessCancelTrx() {

        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor mEditor = prefs.edit();
        mEditor.remove(DefineValue.BBS_AGENT_MOBILITY);
        mEditor.remove(DefineValue.BBS_TX_ID);
        mEditor.remove(DefineValue.AMOUNT);

        mEditor.apply();
        finish();
    }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Timber.tag("MapsDemo").d("The latest version of the renderer is used.");
                break;
            case LEGACY:
                Timber.tag("MapsDemo").d("The legacy version of the renderer is used.");
                break;
        }
    }

    private class GoogleMapRouteTask extends AsyncTask<Void, Void, ArrayList<ShopDetail>> {

        private ArrayList<ShopDetail> dataDetails;
        private Double dataCurrentLatitude;
        private Double dataCurrentLongitude;

        public GoogleMapRouteTask(ArrayList<ShopDetail> shopDetails, Double currentLatitude, Double currentLongitude) {
            this.dataDetails = shopDetails;
            dataCurrentLatitude = currentLatitude;
            dataCurrentLongitude = currentLongitude;
        }

        protected void onPostExecute(ArrayList<ShopDetail> result) {

            if (viewPager != null)
                viewPager.getAdapter().notifyDataSetChanged();
        }

        @Override
        protected ArrayList<ShopDetail> doInBackground(Void... params) {
            ArrayList<ShopDetail> newShopDetail = new ArrayList<>();

            String nextParams = "origin=" + dataCurrentLatitude.toString() + "," + dataCurrentLongitude.toString();
            nextParams += "&sensor=false";
            nextParams += "&units=metric";
            nextParams += "&mode=" + DefineValue.GMAP_MODE;
            nextParams += "&language=" + DefineValue.DEFAULT_LANGUAGE_CODE;

            HashMap<String, Object> query = new HashMap<>();
            query.put("origin", dataCurrentLatitude.toString() + "," + dataCurrentLongitude.toString());
            query.put("sensor", false);
            query.put("units", "metric");
            query.put("mode", DefineValue.GMAP_MODE);
            query.put("language", DefineValue.DEFAULT_LANGUAGE_CODE);
            query.put("key", MyApiClient.getGoogleMapsKey());

//            RequestParams rqParams = new RequestParams();
//            rqParams.put("origin", dataCurrentLatitude.toString()+","+dataCurrentLongitude.toString());
//            rqParams.put("sensor", "false");
//            rqParams.put("units", "metric");
//            rqParams.put("mode", DefineValue.GMAP_MODE);
//            rqParams.put("language", DefineValue.DEFAULT_LANGUAGE_CODE);

            for (int idx = 0; idx < dataDetails.size(); idx++) {
                ShopDetail tempShopDetail = dataDetails.get(idx);
                String tempParams = nextParams;
                tempParams += "&destination=" + tempShopDetail.getShopLatitude().toString() + "," + tempShopDetail.getShopLongitude();

                query.put("destination", tempShopDetail.getShopLatitude().toString() + "," + tempShopDetail.getShopLongitude());

                getGoogleMapRoute(query, idx);
            }
            return newShopDetail;
        }
    }

    public void getGoogleMapRoute(
//            String tempParams
            HashMap<String, Object> query, final int idx) {

        RetrofitService.getInstance().QueryRequestSSL(MyApiClient.LINK_GOOGLE_MAP_API_ROUTE, query,
//                        + "?" + tempParams,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            JSONArray array = response.getJSONArray("routes");
                            JSONObject routes = array.getJSONObject(0);
                            JSONArray legs = routes.getJSONArray("legs");
                            JSONObject steps = legs.getJSONObject(0);
                            JSONObject distance = steps.getJSONObject("distance");
                            String parsedDistance = distance.getString("text");

                            JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                            String points = overviewPolyline.getString("points");

                            //routes.getOverviewPolyline().getPoints();

                            //Timber.w("isi index : %d",idx);
                            shopDetails.get(idx).setCalculatedDistance(parsedDistance);
                            shopDetails.get(idx).setEncodedPoints(points);
                            //viewPager.getAdapter().notifyDataSetChanged();


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

    @AfterPermissionGranted(RC_LOCATION_PHONE_SMS)
    private void methodRequiresTwoPermission() {

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            if (!GlobalSetting.isLocationEnabled(this)) {
                showAlertEnabledGPS();
            } else {

                isAllowed = true;
                try {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Timber.d("Masuk Sini methodRequiresTwoPermission");

            }

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location_phone_sms),
                    RC_LOCATION_PHONE_SMS, perms);
        }

        runningApp();
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
                showAlertEnabledGPS();
            } else {

                if (intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE)) {
                    currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
                    currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);
                }

                if (intentData.hasExtra(DefineValue.IS_AUTOSEARCH)) {
                    if (intentData.getStringExtra(DefineValue.IS_AUTOSEARCH).equals(DefineValue.STRING_YES)) {
                        if (EasyPermissions.hasPermissions(this, perms)) {
                            Timber.d("Masuk Sini onActivityResult");
                            isAllowed = true;
                            runningApp();
                        }
                    }
                } else {

                    Intent i = new Intent(this, BbsSearchAgentActivity.class);
                    i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                    i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, mobility);
                    i.putExtra(DefineValue.AMOUNT, amount);
                    i.putExtra(DefineValue.BBS_PRODUCT_NAME, bbsProductName);
                    i.putExtra(DefineValue.BBS_SCHEME_CODE, bbsSchemeCode);
                    startActivity(i);
                    finish();
                }
            }
        }
    }


    private void checkTransactionMember() {
        if (!txId.equals("")) {

            String extraSignature = txId;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CHECK_TRANSACTION_MEMBER,
                    extraSignature);

            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.KEY_VALUE, gcmId);
            params.put(WebParams.KEY_PHONE, userPhoneID);
            params.put(WebParams.USER_ID, userPhoneID);

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CHECK_TRANSACTION_MEMBER, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    String txStatus = response.getString(WebParams.TX_STATUS);

                                    if (txStatus.equals(DefineValue.TX_STATUS_OP)) {
                                        handler.removeCallbacks(runnable);

                                        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                        SecurePreferences.Editor mEditor = prefs.edit();
                                        mEditor.putString(DefineValue.TX_ID2, txId);
                                        mEditor.putString(DefineValue.CATEGORY_NAME, categoryName);
                                        mEditor.putString(DefineValue.AMOUNT, amount);
                                        mEditor.apply();

                                        Intent i = new Intent(getApplicationContext(), BbsMapViewByMemberActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                        finish();

                                    } else if (txStatus.equals(DefineValue.TX_STATUS_RJ)) {

                                        handler.removeCallbacks(runnable);

                                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                        alertDialog.setCanceledOnTouchOutside(false);
                                        alertDialog.setCancelable(false);
                                        alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                        String tempMessage = getString(R.string.alertbox_message_search_agent_not_found);
                                        alertDialog.setMessage(tempMessage);

                                        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                                                (dialog, which) -> {
                                                    dialog.dismiss();

                                            /*SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                            SecurePreferences.Editor mEditor = prefs.edit();
                                            mEditor.putString(DefineValue.BBS_TX_ID, "");
                                            mEditor.apply();

                                            clicked = false;

                                            searchToko(currentLatitude,currentLongitude);*/

                                                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                                    SecurePreferences.Editor mEditor = prefs.edit();
                                                    mEditor.putString(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                                                    mEditor.putString(DefineValue.BBS_TX_ID, "");
                                                    mEditor.putString(DefineValue.AMOUNT, amount);
                                                    mEditor.apply();

                                                    Intent i = new Intent(BbsSearchAgentActivity.this, BbsSearchAgentActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    i.putExtra(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);
                                                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                                                    i.putExtra(DefineValue.AMOUNT, amount);
                                                    i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                                                    i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                                                    i.putExtra(DefineValue.LAST_CURRENT_LATITUDE, currentLatitude);
                                                    i.putExtra(DefineValue.LAST_CURRENT_LONGITUDE, currentLongitude);
                                                    i.putExtra(DefineValue.BBS_PRODUCT_NAME, bbsProductName);
                                                    i.putExtra(DefineValue.BBS_SCHEME_CODE, bbsSchemeCode);
                                                    startActivity(i);
                                                    finish();

                                                });

                                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                                                (dialog, which) -> {
                                                    dialog.dismiss();

                                                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                                    SecurePreferences.Editor mEditor = prefs.edit();
                                                    mEditor.remove(DefineValue.BBS_AGENT_MOBILITY);
                                                    mEditor.remove(DefineValue.BBS_TX_ID);
                                                    mEditor.remove(DefineValue.AMOUNT);
                                                    mEditor.apply();

                                                    finish();
                                                });
                                        if (!isFinishing())
                                            alertDialog.show();
                                    }
                                } else {
                                    finish();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
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

    @Override
    public void onBackPressed() {
        if (mobility.equals(DefineValue.STRING_YES)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.alertbox_waiting_agent_approval))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), (dialog, id) -> dialog.dismiss())
            ;
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onAccessFineLocationGranted() {
        super.onAccessFineLocationGranted();

        Timber.d("BbsSearchAgent masuk AccessFineLocation");
        if (!GlobalSetting.isLocationEnabled(this)) {
            showAlertEnabledGPS();
        }
    }

    @Override
    public void onDeny() {
        super.onDeny();
        finish();
    }
}
