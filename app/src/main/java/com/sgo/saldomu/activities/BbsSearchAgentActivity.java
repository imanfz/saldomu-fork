package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.TabAgentPagerAdapter;
import com.sgo.saldomu.adapter.TabSearchAgentAdapter;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.GoogleAPIUtils;
import com.sgo.saldomu.coreclass.MainAgentIntentService;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.AgentDetail;
import com.sgo.saldomu.entityRealm.AgentServiceDetail;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.fragments.AgentListFragment;
import com.sgo.saldomu.fragments.FragCancelTrxRequest;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.services.UpdateLocationService;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsSearchAgentActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        EasyPermissions.PermissionCallbacks,
        AgentListFragment.OnListAgentItemClick,
        FragCancelTrxRequest.CancelTrxRequestListener
{

    private int lastLocationResult   = AgentConstant.FALSE;
    private int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    private int pickupLocationResult = AgentConstant.FALSE;
    private int agentInfoResult      = AgentConstant.FALSE;

    private static final String TAG = BbsSearchAgentActivity.class.getSimpleName();

    private String searchLocationString;
    Intent intentData;
    private String[] menuItems;
    TabSearchAgentAdapter tabPageAdapter;
    private GoogleApiClient googleApiClient;
    public MainResultReceiver agentMapResultReceiver;
    public MainResultReceiver agentListResultReceiver;
    public MainResultReceiver agentListMapResultReceiver;
    private Location lastLocation;
    private Dialog dialog;
    private String errorDesc, gcmId;
    private Address searchLocation;

    private boolean backStatus = false, isAllowed = false, isCalled = false;
    private LocationRequest mLocationRequest;

    Intent locationIntent;
    private TextView errorMsg;
    private Button backBtn;
    public ViewPager viewPager;
    private String categoryId, categoryName, bbsNote;
    private String mobility, amount;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private Double currentLatitude;
    private Double currentLongitude;
    SecurePreferences sp;
    private String completeAddress, districtName, provinceName, countryName, txId,
        bbsProductName, bbsProductCode, bbsProductType, bbsProductDisplay, bbsSchemeCode;
    private int timeDelayed = 30000;
    EditText etJumlah;
    Button btnProses;
    private static final int RC_LOCATION_PERM = 500;
    private final int RC_PHONE_CALL = 503;
    private final int RC_SEND_SMS = 504;
    private static final int RC_LOCATION_PHONE_SMS = 505;
    private static final int RC_GPS_REQUEST = 1;
    private ImageView imgDelete;

    Boolean clicked = false;
    ProgressDialog progdialog, progdialog2, progdialog3;
    private Realm realm, realmBBSMemberBank;
    Boolean isMapIconClicked = false;
    int isMapIconPosition = 0;

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
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm               = Realm.getDefaultInstance();
        realmBBSMemberBank            = Realm.getInstance(RealmManager.BBSMemberBankConfiguration);

        intentData          = getIntent();
        sp                  = CustomSecurePref.getInstance().getmSecurePrefs();

        categoryId          = intentData.getStringExtra(DefineValue.CATEGORY_ID);
        mobility            = intentData.getStringExtra(DefineValue.BBS_AGENT_MOBILITY);
        categoryName        = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
        amount              = intentData.getStringExtra(DefineValue.AMOUNT);
        completeAddress     = intentData.getStringExtra(DefineValue.BBS_COMPLETE_ADDRESS);
        bbsNote             = intentData.getStringExtra(DefineValue.BBS_NOTE);
        bbsProductName      = intentData.getStringExtra(DefineValue.BBS_PRODUCT_NAME);
        bbsSchemeCode       = intentData.getStringExtra(DefineValue.BBS_SCHEME_CODE);

        BBSBankModel bbsBankModel = null;

        if ( bbsSchemeCode.equals(DefineValue.CTA) ) {
            bbsBankModel = realmBBSMemberBank.where(BBSBankModel.class).
                    equalTo(BBSBankModel.SCHEME_CODE, DefineValue.CTA).
                    equalTo(BBSBankModel.PRODUCT_NAME, bbsProductName)
                    .findFirst();
        } else {
            bbsBankModel = realmBBSMemberBank.where(BBSBankModel.class).
                    equalTo(BBSBankModel.SCHEME_CODE, DefineValue.ATC).
                    equalTo(BBSBankModel.PRODUCT_NAME, bbsProductName)
                    .findFirst();
        }

        if ( bbsBankModel != null ) {
            bbsProductCode      = bbsBankModel.getProduct_code();
            bbsProductType      = bbsBankModel.getProduct_type();
            bbsProductDisplay   = bbsBankModel.getProduct_display();
        }


        methodRequiresTwoPermission();
        locationIntent = new Intent(this, UpdateLocationService.class);

        txId                = "";

        gcmId               = "";

        initializeToolbar(getString(R.string.search_agent) + " " + categoryName);

    }

    public void initializeApp() {
        try {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if ( intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE) ) {
            currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
            currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);

        }

        menuItems           = getResources().getStringArray(R.array.list_tab_bbs_search_agent);
        tabPageAdapter      = new TabSearchAgentAdapter(getSupportFragmentManager(), getApplicationContext(), menuItems, shopDetails, currentLatitude, currentLongitude, mobility, completeAddress);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(tabPageAdapter);
    }

    public void runningApp() {

        if ( isCalled ) {
            return;
        } else {
            isCalled = true;
        }

        if ( intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE) ) {
            currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
            currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);

        }

        menuItems           = getResources().getStringArray(R.array.list_tab_bbs_search_agent);
        tabPageAdapter      = new TabSearchAgentAdapter(getSupportFragmentManager(), getApplicationContext(), menuItems, shopDetails, currentLatitude, currentLongitude, mobility, completeAddress);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(tabPageAdapter);

        imgDelete   = (ImageView) findViewById(R.id.imgCancel);

        if ( mobility.equals(DefineValue.STRING_NO) ) {
            imgDelete.setVisibility(View.INVISIBLE);
        } else {
            imgDelete.setOnClickListener(this);
            imgDelete.setVisibility(View.INVISIBLE);
        }



        if ( intentData.hasExtra(DefineValue.IS_AUTOSEARCH) ) {
            if (intentData.getStringExtra(DefineValue.IS_AUTOSEARCH).equals(DefineValue.STRING_YES) ) {
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
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        process();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_search_agent;
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

        if ( mobility.equals(DefineValue.STRING_NO) ) {
            return super.onOptionsItemSelected(item);
        } else {
            return true;
        }
    }

    public void initializeToolbar(String title)
    {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    private void customInitializeToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left);

        TextView toolbarTitle = (TextView) findViewById(R.id.main_toolbar_title);
        toolbarTitle.setText("Cari Agen");
    }



    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.imgCancel ) {
            FragCancelTrxRequest fragCancelTrxRequest = new FragCancelTrxRequest();

            Bundle bundle = new Bundle();
            bundle.putString(DefineValue.CUST_ID, sp.getString(DefineValue.USERID_PHONE, ""));
            bundle.putString(DefineValue.TX_ID, sp.getString(DefineValue.BBS_TX_ID, ""));

            fragCancelTrxRequest.setArguments(bundle);
            fragCancelTrxRequest.setCancelable(false);
//            fragCancelTrxRequest.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
            fragCancelTrxRequest.show(getSupportFragmentManager(),fragCancelTrxRequest.TAG  );
        }
     }

    @Override
    public void onConnected(Bundle bundle) {

        Timber.d("onConnected Started");
        //startLocationUpdate();

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if ( lastLocation == null ){
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {
                //btnProses.setEnabled(true);
                if ( mobility.equals(DefineValue.STRING_NO) && currentLatitude != null) {

                } else {
                    //currentLatitude = lastLocation.getLatitude();
                    //currentLongitude = lastLocation.getLongitude();
                }

                Timber.d("Location Found" + lastLocation.toString());
                //viewPager.getAdapter().notifyDataSetChanged();
                //getCompleteLocationAddress();
                //searchToko(lastLocation.getLatitude(), lastLocation.getLongitude());
                googleApiClient.disconnect();

                //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
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
        lastLocation = location;
        //btnProses.setEnabled(true);
//        googleApiClient.disconnect();

        if ( mobility.equals(DefineValue.STRING_NO) && currentLatitude != null ) {

        } else {
            //currentLatitude = lastLocation.getLatitude();
            //currentLongitude = lastLocation.getLongitude();
        }

        //viewPager.getAdapter().notifyDataSetChanged();
        //getCompleteLocationAddress();

        if ( mobility.equals(DefineValue.STRING_NO) ) {
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

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void mainProcess()
    {
        //jika google play service tersedia, maka :
        if(checkPlayServices())
        {
            //set receiver
            agentListResultReceiver    = new MainResultReceiver(new Handler());
            agentMapResultReceiver     = new MainResultReceiver(new Handler());
            agentListMapResultReceiver = new MainResultReceiver(new Handler());

            buildGoogleApiClient();
            createLocationRequest();
        }
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



    private void process()
    {
        //display progress dialog
        displayDialog();

        mainProcess();

    }

    private String getDistance(Double latitude1, Double longitude1, Double latitude2, Double longitude2)
    {
        Double latitude   = latitude2 - latitude1;
        Double latitudeKm = convertToKm(latitude);

        Double longitude   = longitude2 - longitude1;
        Double longitudeKm = convertToKm(longitude);

        Double latitudeKuadrat  = latitudeKm  * latitudeKm;
        Double longitudeKuadrat = longitudeKm * longitudeKm;

        Double result = latitudeKuadrat + longitudeKuadrat;
        result = Math.sqrt(result);

        //rounding become 1 decimal & convert to string
        return String.format("%.1f", result);
    }

    private Double convertToKm(Double value)
    {
        //absolute - menghilangkan minus jika ada
        value = Math.abs(value);

        //get degree & convert to meter
        Double degree   = Math.floor(value);
        Double degreeKm = degree * 111.322;   //111.320

        //get hour & convert to meter
        Double hour       = value % 1;
        Double hourMinute = hour * 60;
        Double hourKm     = hourMinute * 1.88537;  //1.855

        return degreeKm + hourKm;
    }

    private void displayDialog()
    {
        dialog = new Dialog(this);
        /*dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //menghilangkan frame windows
        dialog.setContentView(R.layout.progress_dialog_agent);
        dialog.setCancelable(false); //agar dialog tidak bisa di-cancel
        dialog.show();*/
    }



    private void getCompleteLocationAddress()
    {

        if ( mobility.equals(DefineValue.STRING_NO) && currentLatitude != null ) {

        } else {
            //currentLatitude = lastLocation.getLatitude();
            //currentLongitude = lastLocation.getLongitude();
        }

        completeAddress = "";



        MyApiClient.getGoogleAPIAddressByLatLng(this, currentLatitude, currentLongitude, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                try {

                    String status = response.getString(WebParams.GMAP_API_STATUS);
                    Timber.w("JSON Response: "+response.toString());


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

                        if ( isAllowed )
                            searchToko(currentLatitude, currentLongitude);

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

        /*try
        {
            Geocoder geocoder = new Geocoder(this, new Locale("id"));

            List<Address> multiAddress = null;

            if ( mobility.equals(DefineValue.STRING_NO) && currentLatitude != null ) {
                multiAddress = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
            } else {
                multiAddress = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                currentLatitude = lastLocation.getLatitude();
                currentLongitude = lastLocation.getLongitude();
            }

            if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
            {


                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                completeAddress = "";
                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                    completeAddress += singleAddress.getAddressLine(i) + " ";
                }


                districtName    = singleAddress.getSubAdminArea();
                provinceName    = singleAddress.getAdminArea();
                countryName     = singleAddress.getCountryName();

                if ( completeAddress.equals("") ) {
                    completeAddress += districtName + ", ";
                    completeAddress += provinceName;
                }

                searchToko(currentLatitude, currentLongitude);

                //set true for allow next process
                pickupLocationResult = AgentConstant.TRUE;

                viewPager.getAdapter().notifyDataSetChanged();
            }
            else
            {
                errorDesc = "The current location is not valid";
            }
        }
        catch(IOException ioException)
        {
            // Catch network or other I/O problems.
            //errorMessage = "Catch : Network or other I/O problems - No geocoder available";
            Log.d("ERROR :", ioException.getMessage());
            errorDesc = "Catch : Network or other I/O problems - No geocoder available";
        }
        catch(IllegalArgumentException illegalArgumentException)
        {
            // Catch invalid latitude or longitude values.
            //errorMessage = "Catch : Invalid latitude or longitude values";
            errorDesc = "Catch : Invalid latitude or longitude values";
        }
        */


        /*RequestParams params = new RequestParams();
        params.put("latlng", lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        params.put("sensor", "true");
        params.put("language", "ID");
        String url = "http://maps.googleapis.com/maps/api/geocode/json";

        AsyncHttpClient client = new SyncHttpClient();

        client.get(url, params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody)
            {
                try
                {
                    if(responseBody.getString("status").equalsIgnoreCase("OK"))
                    {
                        JSONArray results  = responseBody.getJSONArray("results");
                        JSONObject result  = results.getJSONObject(0);
                        String fullAddress = result.getString("formatted_address");

                        SharedPreferences preferences = getSharedPreferences(BbsConstants.LAST_LOCATION_SHARED_PREFERENCES, MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("pickupLocation", fullAddress);
                        editor.apply();

                        //set true for allow next process
                        pickupLocationResult = BbsConstants.TRUE;
                    }
                }
                catch(JSONException ex)
                {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                errorDesc = "Wrong Json Format";
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                errorDesc = "Connection Failed";
            }
        });*/

    }

    private void getCompleteLocationAddress2()
    {

        try
        {
            Geocoder geocoder = new Geocoder(this, new Locale("id"));

            List<Address> multiAddress = geocoder.getFromLocation(this.currentLatitude,this.currentLongitude,1);

            if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
            {

                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                completeAddress = "";
                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                    completeAddress += singleAddress.getAddressLine(i) + " ";
                }


                districtName    = singleAddress.getSubAdminArea();
                provinceName    = singleAddress.getAdminArea();
                countryName     = singleAddress.getCountryName();

                if ( completeAddress.equals("") ) {
                    completeAddress += districtName + ", ";
                    completeAddress += provinceName;
                }


                //set true for allow next process
                pickupLocationResult = AgentConstant.TRUE;


            }
            else
            {
                errorDesc = "The current location is not valid";
            }
        }
        catch(IOException ioException)
        {
            // Catch network or other I/O problems.
            //errorMessage = "Catch : Network or other I/O problems - No geocoder available";
            Log.d("ERROR :", ioException.getMessage());
            errorDesc = "Catch : Network or other I/O problems - No geocoder available";
        }
        catch(IllegalArgumentException illegalArgumentException)
        {
            // Catch invalid latitude or longitude values.
            //errorMessage = "Catch : Invalid latitude or longitude values";
            errorDesc = "Catch : Invalid latitude or longitude values";
        }


    }

    private void getBundle()
    {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            searchLocationString  = bundle.getString("searchLocationString");

            //menghilangkan spasi kiri dan kanan
            searchLocationString = searchLocationString.trim();
        }
    }

    private void startIntentService(Location location)
    {
        Intent intent = new Intent(this, MainAgentIntentService.class);
        intent.putExtra("agentMapResultReceiver", agentMapResultReceiver);
        intent.putExtra("agentListMapResultReceiver", agentListMapResultReceiver);
        intent.putExtra("location",location);

        startService(intent);
    }

    private void stopLocationUpdate()
    {
        if(googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private boolean checkInternetConnectivity()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        else
        {
            errorDesc = "No Internet Connection";
            return false;
        }

    }

    private void displayErrorLayout(String errorDesc)
    {
        setContentView(R.layout.display_error_agent);
        customInitializeToolbar();

        errorMsg = (TextView) findViewById(R.id.errorMsg);
        errorMsg.setText(errorDesc);

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
    }

    private void setAgentInfoDummy()
    {
        JSONArray array = null;

        try
        {

            ArrayList<JSONObject> dataAgents = new ArrayList();
            RealmResults<AgentDetail> results = realm.where(AgentDetail.class).findAll();

            for (int i = 0; i < results.size(); i++) {
                JSONObject dataAgent        = new JSONObject();
                AgentDetail agentDetail     = results.get(i);

                dataAgent.put("businessId", agentDetail.getBusinessId());
                dataAgent.put("latitude", agentDetail.getLatitude() );
                dataAgent.put("longitude", agentDetail.getLongitude() );
                dataAgent.put("name", agentDetail.getBusinessName() );
                dataAgent.put("date", agentDetail.getLastOnline() );
                dataAgent.put("address", agentDetail.getAddress() );

                dataAgent.put("available_flag", agentDetail.getAvailableFlag() );
                dataAgent.put("rate", agentDetail.getTotalRating() );

                RealmResults<AgentServiceDetail> serviceResults = realm.where(AgentServiceDetail.class)
                        .equalTo("businessId", agentDetail.getBusinessId()).findAll();

                List<String> dataServices   = new ArrayList<String>();
                String newServices          = "";
                for (int j = 0; j < serviceResults.size(); j++) {
                    dataServices.add(serviceResults.get(j).getServices());
                }

                if ( dataServices.size() > 0 )
                {
                    newServices             = dataServices.toString();
                }

                dataAgent.put("services", newServices);
                dataAgent.put("image", "profile1");

                dataAgents.add(dataAgent);
            }


            /*

            for(int x = 0; x < SearchAgentActivity.business_name_arr.size(); x++)
            {
                List<AgentService> itemList = new Select().all().from(AgentService.class).where("agent_no = ?", x).execute();

                JSONObject dataAgent = new JSONObject();
                dataAgent.put("latitude", SearchAgentActivity.latitude_arr.get(x));
                dataAgent.put("longitude", SearchAgentActivity.longitude_arr.get(x));
                dataAgent.put("name", SearchAgentActivity.business_name_arr.get(x));
                dataAgent.put("date", SearchAgentActivity.last_online_arr.get(x));
                dataAgent.put("address", SearchAgentActivity.address_arr.get(x));

                dataAgent.put("available_flag", SearchAgentActivity.available_flag_arr.get(x));
                dataAgent.put("rate", SearchAgentActivity.total_rating_arr.get(x));
                dataAgent.put("services", itemList.get(0).getService().replace("[", "").replace("]", ""));
                dataAgent.put("image", "profile1");

                dataAgents.add(dataAgent);

            }
            */

            String string = dataAgents.toString();

            Log.d("test_denny", string);
            array = new JSONArray(string);

            for (int i = 0; i < dataAgents.size(); i++)
            {
                double incrementLatitude  = randomLocation();
                double incrementLongitude = randomLocation();

                //convert json array to json object
                JSONObject object      = dataAgents.get(i);
                Double agentLatitude   = object.getDouble("latitude");
                Double agentLongitude  = object.getDouble("longitude");


                    String distance = getDistance(agentLatitude, agentLongitude, lastLocation.getLatitude(), lastLocation.getLongitude());
                    //String distanceString = Double.toString(distance);
                    object.put("distance", distance);

                    /*double latitude = lastLocation.getLatitude() - incrementLatitude;
                    double longitude = lastLocation.getLongitude() + incrementLongitude;*/

                    double latitude;
                    double longitude;

                    if(i==0)
                    {
                        latitude  = lastLocation.getLatitude() - incrementLatitude;
                        longitude = lastLocation.getLongitude() - incrementLongitude;
                    }
                    else if(i==1)
                    {
                        latitude  = lastLocation.getLatitude() + incrementLatitude;
                        longitude = lastLocation.getLongitude() + incrementLongitude;
                    }
                    else if(i==2)
                    {
                        latitude  = lastLocation.getLatitude() - incrementLatitude;
                        longitude = lastLocation.getLongitude() - incrementLongitude;
                    }
                    else if(i==3)
                    {
                        latitude  = lastLocation.getLatitude() + incrementLatitude;
                        longitude = lastLocation.getLongitude() - incrementLongitude;
                    }
                    else if(i==4)
                    {
                        latitude  = lastLocation.getLatitude() + incrementLatitude;
                        longitude = lastLocation.getLongitude() + incrementLongitude;
                    }
                    else
                    {
                        latitude  = lastLocation.getLatitude() - incrementLatitude;
                        longitude = lastLocation.getLongitude() + incrementLongitude;
                    }

                    object.put("latitude",  Double.toString(latitude) );
                    object.put("longitude", Double.toString(longitude) );

            }
        }
        catch(JSONException ex)
        {
            ex.printStackTrace();
        }

        //save data to session
        SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, array.toString());
        editor.apply();

        //set true for allow next process
        agentInfoResult = AgentConstant.TRUE;
    }

    private Double randomLocation()
    {
        double random = Math.random() * 10;
        double x = (int) random;
        Double y =  x / 10;
        Double z =  y * 0.01;
        Double angkaRandom = 0.001 + z;

        return angkaRandom;
    }

    private void setTabViewPager()
    {
        TabAgentPagerAdapter tabBbsPagerAdapter = new TabAgentPagerAdapter(getSupportFragmentManager());

        //viewPager = (ViewPager) findViewById(viewPager);
        //viewPager.setOffscreenPageLimit(4); // use a number higher than half your fragments.
        //viewPager.setAdapter(tabBbsPagerAdapter);

        //PagerSlidingTabStrip tabPagerSliding = (PagerSlidingTabStrip) findViewById(R.id.tabPagerSliding);
        // tabPagerSliding.setViewPager(viewPager);
    }

    private void searchToko(Double latitude, Double longitude) {

        if (!EasyPermissions.hasPermissions(this, perms) || !GlobalSetting.isLocationEnabled(this)  ) {
            return;
        }

        sp   = CustomSecurePref.getInstance().getmSecurePrefs();
        txId = sp.getString(DefineValue.BBS_TX_ID, "");

        if ( mobility.equals(DefineValue.STRING_NO) ) {
            clicked = false;
        }

        if ( txId.equals("") && !amount.equals("") && !clicked ) {

            clicked                 = true;
            progdialog              = DefinedDialog.CreateProgressDialog(this, getString(R.string.menu_item_search_agent));

            String extraSignature = categoryId + bbsProductType + bbsProductCode;
            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_SEARCH_TOKO,
                    userPhoneID, accessKey, extraSignature);

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
                params.put(WebParams.KEY_ADDRESS, completeAddress);
                //            params.put(WebParams.KEY_DISTRICT, districtName);
                //            params.put(WebParams.KEY_PROVINCE, provinceName);
                //            params.put(WebParams.KEY_COUNTRY, countryName);
                params.put(WebParams.KEY_AMOUNT, amount);
                params.put(WebParams.KEY_EMAIL, sp.getString(DefineValue.PROFILE_EMAIL, ""));

                //Start
                handler.postDelayed(runnable, timeDelayed);
            }

            //Timber.d("Current Latitude: " + currentLatitude.toString() + ", Current Longitude: " + currentLongitude.toString());
            //Timber.d("LCurrent Latitude: " + latitude.toString() + ", Current Longitude: " + longitude.toString());
            //currentLatitude = latitude;
            //currentLongitude = longitude;
            Timber.d("Params search toko :" +params);

            MyApiClient.searchToko(getApplicationContext(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //llHeaderProgress.setVisibility(View.GONE);
                    //pbHeaderProgress.setVisibility(View.GONE);
                    Timber.d("Response search toko:" + response.toString());

                    if ( progdialog.isShowing())
                        progdialog.dismiss();

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

                                    shopDetail.setShopName(object.getString("shop_name"));
                                    shopDetail.setMemberCust(object.getString("member_cust"));
                                    shopDetail.setMemberId(object.getString("member_id"));
                                    shopDetail.setShopLatitude(object.getDouble("shop_latitude"));
                                    shopDetail.setShopLongitude(object.getDouble("shop_longitude"));
                                    shopDetail.setMemberName(object.getString("member_name"));
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
                                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setCancelable(false);
                                alertDialog.setTitle(getString(R.string.alertbox_title_information));


                                alertDialog.setMessage(getString(R.string.message_notif_waiting_agent_approval));


                                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();


                                            }
                                        });
                                alertDialog.show();

                            } else {

                            }

                        } else if (code.equals(WebParams.INPROGRESS_CODE)) {
                            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setCancelable(false);
                            alertDialog.setTitle(getString(R.string.alertbox_title_information));
                            String tempMessage = getString(R.string.alertbox_message_search_agent_inprogress_trx);
                            alertDialog.setMessage(tempMessage);

                            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                            Intent i = new Intent(getApplicationContext(), MainPage.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(i);
                                            finish();

                                        }
                                    });
                            alertDialog.show();
                        } else {
                            shopDetails.clear();
                            //Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);



                            if (mobility.equals(DefineValue.STRING_YES)) {

                                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setCancelable(false);
                                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                String tempMessage = getString(R.string.alertbox_message_search_agent_not_found);
                                alertDialog.setMessage(tempMessage);

                                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                Intent i = new Intent(getApplicationContext(), BbsSearchAgentActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                                                i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                                                i.putExtra(DefineValue.LAST_CURRENT_LATITUDE, currentLatitude);
                                                i.putExtra(DefineValue.LAST_CURRENT_LONGITUDE, currentLongitude);
                                                i.putExtra(DefineValue.BBS_PRODUCT_NAME, bbsProductName);
                                                i.putExtra(DefineValue.BBS_SCHEME_CODE, bbsSchemeCode);

                                                if ( mobility.equals(DefineValue.STRING_YES) ) {

                                                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                                                    i.putExtra(DefineValue.AMOUNT, amount);
                                                    i.putExtra(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);


                                                } else {
                                                    i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_YES);
                                                    i.putExtra(DefineValue.AMOUNT, "");

                                                }

                                                startActivity(i);
                                                finish();

                                            }
                                        });



                                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                                SecurePreferences.Editor mEditor = prefs.edit();
                                                mEditor.remove(DefineValue.BBS_AGENT_MOBILITY);
                                                mEditor.remove(DefineValue.BBS_TX_ID);
                                                mEditor.remove(DefineValue.AMOUNT);
                                                mEditor.apply();

                                                finish();

                                            }
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

                    Timber.d("Error Koneksi search toko:" + throwable.toString());

                    if ( progdialog.isShowing() )
                        progdialog.dismiss();

                }

            });
        }

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
        if ( currentLatitude != null ) {
            tempCoordinate.add(currentLatitude);
        } else {
            tempCoordinate.add(0.0);
        }

        if ( currentLongitude != null ) {
            tempCoordinate.add(currentLongitude);
        } else {
            tempCoordinate.add(0.0);
        }

        return tempCoordinate;
    }

    public void setCoordinate(Double lastLatitude, Double lastLongitude, String newAddress) {
        this.currentLatitude = lastLatitude;
        this.currentLongitude   = lastLongitude;
        this.completeAddress    = newAddress;
    }

    public void onIconMapClick(int position) {
        viewPager.setCurrentItem(0);
        if ( shopDetails.size() > 0 ) {
            for(int idx = 0; idx < shopDetails.size(); idx++) {
                if ( position == idx ) {
                    shopDetails.get(idx).setIsPolyline("1");
                } else {
                    shopDetails.get(idx).setIsPolyline("0");
                }
            }

            isMapIconClicked = true;
            isMapIconPosition = position;
        }

        viewPager.arrowScroll(View.FOCUS_LEFT);

        //viewPager.getAdapter().notifyDataSetChanged();

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
        switch(requestCode) {
            //case RC_LOCATION_PERM:
            case RC_LOCATION_PHONE_SMS:

                if ( intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE) ) {
                    currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
                    currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);
                }

                if ( intentData.hasExtra(DefineValue.IS_AUTOSEARCH) ) {
                    if (intentData.getStringExtra(DefineValue.IS_AUTOSEARCH).equals(DefineValue.STRING_YES) ) {
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
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
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
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
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
    public void OnIconLocationClickListener(int position, ArrayList<ShopDetail> tempShopDetail) {


        if ( shopDetails.size() > 0 ) {
            for(int idx = 0; idx < shopDetails.size(); idx++) {
                if ( position == idx ) {
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
    public void onSuccessCancelTrx(String txId) {

        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor mEditor = prefs.edit();
        mEditor.remove(DefineValue.BBS_AGENT_MOBILITY);
        mEditor.remove(DefineValue.BBS_TX_ID);
        mEditor.remove(DefineValue.AMOUNT);

        mEditor.apply();
        finish();
    }

    private class GoogleMapRouteTask extends AsyncTask<Void, Void, ArrayList<ShopDetail>> {

        private ArrayList<ShopDetail> dataDetails = new ArrayList<>();
        private Double dataCurrentLatitude;
        private Double dataCurrentLongitude;

        public GoogleMapRouteTask(ArrayList<ShopDetail> shopDetails, Double currentLatitude, Double currentLongitude)
        {
            this.dataDetails = shopDetails;
            dataCurrentLatitude = currentLatitude;
            dataCurrentLongitude = currentLongitude;
        }

        protected void onPostExecute(ArrayList<ShopDetail> result) {
            //shopDetails.clear();
            //shopDetails.addAll(result);

            if ( viewPager != null )
                viewPager.getAdapter().notifyDataSetChanged();
        }

        @Override
        protected ArrayList<ShopDetail> doInBackground(Void... params) {
            ArrayList<ShopDetail> newShopDetail = new ArrayList<>();

            String nextParams = "origin="+dataCurrentLatitude.toString()+","+dataCurrentLongitude.toString();
            nextParams += "&sensor=false";
            nextParams += "&units=metric";
            nextParams += "&mode="+DefineValue.GMAP_MODE;
            nextParams += "&language="+DefineValue.DEFAULT_LANGUAGE_CODE;

            RequestParams rqParams = new RequestParams();
            rqParams.put("origin", dataCurrentLatitude.toString()+","+dataCurrentLongitude.toString());
            rqParams.put("sensor", "false");
            rqParams.put("units", "metric");
            rqParams.put("mode", DefineValue.GMAP_MODE);
            rqParams.put("language", DefineValue.DEFAULT_LANGUAGE_CODE);

            for(int idx=0; idx <dataDetails.size(); idx++) {
                ShopDetail tempShopDetail = dataDetails.get(idx);
                String tempParams = nextParams;
                tempParams += "&destination=" + tempShopDetail.getShopLatitude().toString() + "," + tempShopDetail.getShopLongitude();

                getGoogleMapRoute(tempParams, idx);
            }
            return newShopDetail;
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

    @AfterPermissionGranted(RC_LOCATION_PHONE_SMS)
    private void methodRequiresTwoPermission() {

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            if ( !GlobalSetting.isLocationEnabled(this) ) {
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



        /*if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            runningApp();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
        }*/

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
            if(resultCode == Activity.RESULT_OK){

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }

            if ( !GlobalSetting.isLocationEnabled(this) )
            {
                showAlertEnabledGPS();
            } else {

                if ( intentData.hasExtra(DefineValue.LAST_CURRENT_LATITUDE) ) {
                    currentLatitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LATITUDE, 0.0);
                    currentLongitude = intentData.getDoubleExtra(DefineValue.LAST_CURRENT_LONGITUDE, 0.0);
                }

                if ( intentData.hasExtra(DefineValue.IS_AUTOSEARCH) ) {
                    if (intentData.getStringExtra(DefineValue.IS_AUTOSEARCH).equals(DefineValue.STRING_YES) ) {
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
        if ( !txId.equals("") ) {

            String extraSignature = txId;
            RequestParams params            = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CHECK_TRANSACTION_MEMBER,
                    userPhoneID, accessKey, extraSignature);

            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.KEY_VALUE, gcmId);
            params.put(WebParams.KEY_PHONE, userPhoneID);
            params.put(WebParams.USER_ID, userPhoneID);

            MyApiClient.checkTransactionMember(getApplicationContext(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            String txStatus = response.getString(WebParams.TX_STATUS);

                            if ( txStatus.equals(DefineValue.TX_STATUS_OP) ) {
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

                            } else if ( txStatus.equals(DefineValue.TX_STATUS_RJ) ) {

                                handler.removeCallbacks(runnable);

                                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setCancelable(false);
                                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                String tempMessage = getString(R.string.alertbox_message_search_agent_not_found);
                                alertDialog.setMessage(tempMessage);

                                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
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

                                            }
                                        });

                                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                                SecurePreferences.Editor mEditor = prefs.edit();
                                                mEditor.remove(DefineValue.BBS_AGENT_MOBILITY);
                                                mEditor.remove(DefineValue.BBS_TX_ID);
                                                mEditor.remove(DefineValue.AMOUNT);
                                                mEditor.apply();

                                                finish();

                                            }
                                        });


                                if ( !isFinishing() )
                                    alertDialog.show();


//                                Intent intent = new Intent();
//                                intent.putExtra(DefineValue.MSG_NOTIF, getString(R.string.msg_notif_batal_agen));
//                                setResult(DefineValue.IDX_CATEGORY_SEARCH_AGENT,intent);
//                                finish();//finishing activity

                                //startActivity(new Intent(getApplicationContext(), Bb.class));
                            }
                        } else {
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
                    //llHeaderProgress.setVisibility(View.GONE);
                    //pbHeaderProgress.setVisibility(View.GONE);

                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    Timber.w("Error Koneksi login:" + throwable.toString());

                }

            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
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

    @Override
    public void onBackPressed() {


        if ( mobility.equals(DefineValue.STRING_YES) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.alertbox_waiting_agent_approval))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.dismiss();
                        }
                    })
            ;
            final AlertDialog alert = builder.create();
            alert.show();
        }

        return;
        //super.onBackPressed();
        /*if(fragment !=null) {
            if ( fragment instanceof AgentMapFragment) {

            } else if ( fragment instanceof AgentListFragment) {

            } else {
                setBackPressed();
            }
        }
        else {
            setBackPressed();
        }*/

    }

    /*public void setBackPressed() {
        if (fragment.getBackStackEntryCount() > 1)
            fragment.popBackStack();
        else
            super.onBackPressed();
    }*/

    @Override
    public void onAccessFineLocationGranted() {
        super.onAccessFineLocationGranted();

        Timber.d("BbsSearchAgent masuk AccessFineLocation");
        if ( !GlobalSetting.isLocationEnabled(this) ) {
            showAlertEnabledGPS();
        } else {
            //runningApp();
        }
    }

    @Override
    public void onDeny() {
        super.onDeny();
        finish();
    }
}
