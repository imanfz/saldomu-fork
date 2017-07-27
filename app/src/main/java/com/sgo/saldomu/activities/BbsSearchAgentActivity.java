package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.AgentListArrayAdapter;
import com.sgo.saldomu.adapter.TabAgentPagerAdapter;
import com.sgo.saldomu.adapter.TabSearchAgentAdapter;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MainAgentIntentService;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.AgentDetail;
import com.sgo.saldomu.entityRealm.AgentServiceDetail;
import com.sgo.saldomu.fragments.FragListCategoryBbs;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.services.UpdateLocationService;

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
import io.realm.RealmResults;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsSearchAgentActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        EasyPermissions.PermissionCallbacks
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

    private boolean backStatus = false;
    private LocationRequest mLocationRequest;

    Intent locationIntent;
    private TextView errorMsg;
    private Button backBtn;
    public ViewPager viewPager;
    Realm realm;
    private String categoryId, categoryName;
    private String mobility, amount;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private Double currentLatitude;
    private Double currentLongitude;
    SecurePreferences sp;
    private String completeAddress, districtName, provinceName, countryName, txId;
    private int timeDelayed = 30000;
    EditText etJumlah;
    Button btnProses;
    private static final int RC_LOCATION_PERM = 500;
    Boolean clicked = false;
    ProgressDialog progdialog;

    // Init
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkTransactionMember();
            handler.postDelayed(this, timeDelayed);
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentData          = getIntent();
        sp                  = CustomSecurePref.getInstance().getmSecurePrefs();

        categoryId          = intentData.getStringExtra(DefineValue.CATEGORY_ID);
        mobility            = intentData.getStringExtra(DefineValue.BBS_AGENT_MOBILITY);
        categoryName        = intentData.getStringExtra(DefineValue.CATEGORY_NAME);
        amount              = intentData.getStringExtra(DefineValue.AMOUNT);

        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            runningApp();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
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
                            startActivity(new Intent(getApplicationContext(), MainPage.class));
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {


        }

        realm = Realm.getDefaultInstance();

        locationIntent = new Intent(this, UpdateLocationService.class);
        etJumlah                = (EditText) findViewById(R.id.etJumlah);
        etJumlah.addTextChangedListener(jumlahChangeListener);

        btnProses               = (Button) findViewById(R.id.btnProses);

        //startService(locationIntent);


        /*LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("UpdateLocationIntent"));*/


//        txId                = "";

        //mobility            = "Y";
        txId                = "";



        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean hasError    = false;

                if(etJumlah.getText().toString().length()==0){
                    etJumlah.requestFocus();
                    etJumlah.setError(getString(R.string.sgoplus_validation_jumlahSGOplus));
                    hasError = true;
                }
                else if(Long.parseLong(etJumlah.getText().toString()) < 1){
                    etJumlah.requestFocus();
                    etJumlah.setError(getString(R.string.payfriends_amount_zero));
                    hasError = true;
                }

                if ( !hasError ) {
                    amount = etJumlah.getText().toString();

                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                    SecurePreferences.Editor mEditor = prefs.edit();
                    mEditor.putString(DefineValue.BBS_TX_ID, "");
                    mEditor.putString(DefineValue.AMOUNT, amount);
                    mEditor.apply();

                    searchToko(currentLatitude, currentLongitude);

                    etJumlah.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);


                }

            }
        });

        gcmId               = "GCM-ID";


        initializeToolbar(getString(R.string.search_agent) + " " + categoryName);




    }

    public void runningApp() {
        menuItems           = getResources().getStringArray(R.array.list_tab_bbs_search_agent);
        tabPageAdapter      = new TabSearchAgentAdapter(getSupportFragmentManager(), getApplicationContext(), menuItems, shopDetails, currentLatitude, currentLongitude, mobility);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(tabPageAdapter);

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

        return super.onOptionsItemSelected(item);
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

                currentLatitude = lastLocation.getLatitude();
                currentLongitude = lastLocation.getLongitude();

                Timber.d("Location Found" + lastLocation.toString());
                viewPager.getAdapter().notifyDataSetChanged();
                getCompleteLocationAddress();
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
//        googleApiClient.disconnect();
        currentLatitude = lastLocation.getLatitude();
        currentLongitude = lastLocation.getLongitude();
        viewPager.getAdapter().notifyDataSetChanged();
        getCompleteLocationAddress();

        if ( mobility.equals(DefineValue.STRING_NO) ) {
            searchToko(currentLatitude, currentLongitude);
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

        try
        {
            Geocoder geocoder = new Geocoder(this, new Locale("id"));

            List<Address> multiAddress = geocoder.getFromLocation(lastLocation.getLatitude(),lastLocation.getLongitude(),1);

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

                searchToko(lastLocation.getLatitude(), lastLocation.getLongitude());

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

        sp   = CustomSecurePref.getInstance().getmSecurePrefs();
        txId = sp.getString(DefineValue.BBS_TX_ID, "");

        if ( txId.equals("") && !amount.equals("") && !clicked ) {


            progdialog              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");

            RequestParams params = new RequestParams();
            UUID rcUUID = UUID.randomUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();

            SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
            SecurePreferences.Editor mEditor = prefs.edit();
            mEditor.putDouble(DefineValue.LAST_LATITUDE, latitude);
            mEditor.putDouble(DefineValue.LAST_LONGITUDE, longitude);
            mEditor.apply();

            params.put(WebParams.RC_UUID, rcUUID);
            params.put(WebParams.RC_DATETIME, dtime);
            params.put(WebParams.APP_ID, BuildConfig.AppID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params.put(WebParams.CATEGORY_ID, categoryId);
            params.put(WebParams.LATITUDE, latitude);
            params.put(WebParams.LONGITUDE, longitude);
            params.put(WebParams.RADIUS, "10");
            params.put(WebParams.BBS_MOBILITY, mobility);

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

            currentLatitude = latitude;
            currentLongitude = longitude;

            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                    DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + categoryId
                    + latitude + longitude));

            params.put(WebParams.SIGNATURE, signature);

            MyApiClient.searchToko(getApplicationContext(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //llHeaderProgress.setVisibility(View.GONE);
                    //pbHeaderProgress.setVisibility(View.GONE);
                    try {

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            clicked                 = true;
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


                                    shopDetail.setMemberId(object.getString("member_id"));
                                    shopDetail.setShopLatitude(object.getDouble("shop_latitude"));
                                    shopDetail.setShopLongitude(object.getDouble("shop_longitude"));
                                    shopDetail.setMemberName(object.getString("member_name"));
                                    shopDetail.setShopAddress(object.getString("shop_address"));

                                    shopDetails.add(shopDetail);
                                }
                            }

                            //tabPageAdapter.notifyDataSetChanged();


                        } else {
                            shopDetails.clear();
                            //Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);

                            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(BbsSearchAgentActivity.this).create();
                            alertDialog.setTitle(getString(R.string.alertbox_title_information));
                            alertDialog.setMessage(getString(R.string.alertbox_message_information));
                            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                        }
                                    });
                            alertDialog.show();



                        }
                        progdialog.dismiss();
                        viewPager.getAdapter().notifyDataSetChanged();
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

                    Timber.w("Error Koneksi:" + throwable.toString());
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            currentLatitude = intent.getDoubleExtra("latitude", 0);
            currentLongitude = intent.getDoubleExtra("longitude", 0);

            searchToko(currentLatitude, currentLongitude);

            stopService(locationIntent);

            //Kabupaten
            String subAdminArea = intent.getStringExtra("subAdminArea");

            //Propinsi
            String adminArea = intent.getStringExtra("adminArea");

            //Negara
            String countryName = intent.getStringExtra("countryName");
            //  ... react to local broadcast message

            //TextView lblLocation = (TextView) findViewById(R.id.lblLocation);
            //lblLocation.setText(currentLatitude + ", " + currentLongitude + ", "+ subAdminArea +  ", "+ adminArea + ", " + countryName);
        }
    };

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

    public void setCoordinate(Double lastLatitude, Double lastLongitude) {
        this.currentLatitude = lastLatitude;
        this.currentLongitude   = lastLongitude;
        searchToko(lastLatitude, lastLongitude);
    }

    public void onIconMapClick(int position) {
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
        viewPager.getAdapter().notifyDataSetChanged();
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
        Intent i = new Intent(getApplicationContext(), BbsSearchAgentActivity.class);
        i.putExtra(DefineValue.CATEGORY_ID, categoryId);
        i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
        i.putExtra(DefineValue.BBS_AGENT_MOBILITY, mobility);
        i.putExtra(DefineValue.AMOUNT, "");
        startActivityForResult(i, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {


        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(BbsSearchAgentActivity.this).create();
            alertDialog.setTitle(getString(R.string.alertbox_title_warning));
            alertDialog.setMessage(getString(R.string.alertbox_message_warning));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), MainPage.class));
                        }
                    });
            alertDialog.show();
        }
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

    private void checkTransactionMember() {
        if ( !txId.equals("") ) {
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

                                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                SecurePreferences.Editor mEditor = prefs.edit();
                                mEditor.putString(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                                mEditor.putString(DefineValue.BBS_TX_ID, "");
                                mEditor.putString(DefineValue.AMOUNT, amount);
                                mEditor.apply();

                                Intent i = new Intent(getApplicationContext(), BbsSearchAgentActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                i.putExtra(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                                i.putExtra(DefineValue.AMOUNT, amount);
                                i.putExtra(DefineValue.CATEGORY_ID, categoryId);
                                i.putExtra(DefineValue.CATEGORY_NAME, categoryName);
                                startActivity(i);
                                finish();

//                                Intent intent = new Intent();
//                                intent.putExtra(DefineValue.MSG_NOTIF, getString(R.string.msg_notif_batal_agen));
//                                setResult(DefineValue.IDX_CATEGORY_SEARCH_AGENT,intent);
//                                finish();//finishing activity

                                //startActivity(new Intent(getApplicationContext(), Bb.class));
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

}
