package com.sgo.hpku.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.adapter.AgentListArrayAdapter;
import com.sgo.hpku.adapter.TabAgentPagerAdapter;
import com.sgo.hpku.adapter.TabSearchAgentAdapter;
import com.sgo.hpku.coreclass.AgentConstant;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MainAgentIntentService;
import com.sgo.hpku.coreclass.MainResultReceiver;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.models.AgentDetail;
import com.sgo.hpku.models.AgentServiceDetail;
import com.sgo.hpku.models.ShopDetail;
import com.sgo.hpku.services.UpdateLocationService;

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
import timber.log.Timber;

public class BbsSearchAgentActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
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
    private String errorDesc;
    private Address searchLocation;
    private Handler handler = null;
    private static Runnable runnable = null;
    private boolean backStatus = false;
    private LocationRequest mLocationRequest;

    Intent locationIntent;
    private TextView errorMsg;
    private Button backBtn;
    public ViewPager viewPager;
    Realm realm;
    private String categoryId;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private Double currentLatitude;
    private Double currentLongitude;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        locationIntent = new Intent(this, UpdateLocationService.class);
        //startService(locationIntent);


        /*LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("UpdateLocationIntent"));*/

        intentData          = getIntent();
        categoryId   = intentData.getStringExtra(DefineValue.CATEGORY_ID);
        String categoryName = intentData.getStringExtra(DefineValue.CATEGORY_NAME);

        initializeToolbar(getString(R.string.search_agent) + " " + categoryName);

        menuItems           = getResources().getStringArray(R.array.list_tab_bbs_search_agent);
        tabPageAdapter      = new TabSearchAgentAdapter(getSupportFragmentManager(), getApplicationContext(), menuItems, shopDetails, currentLatitude, currentLongitude);
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
                searchToko(lastLocation.getLatitude(), lastLocation.getLongitude());
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
        searchToko(lastLocation.getLatitude(), lastLocation.getLongitude());
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



    private void setPickupLocation()
    {

        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address> multiAddress = geocoder.getFromLocation(lastLocation.getLatitude(),lastLocation.getLongitude(),1);

            if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
            {
                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                }

                String fullAddress = TextUtils.join(" ", addressArray);

                SharedPreferences preferences = getSharedPreferences(AgentConstant.LAST_LOCATION_SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("pickupLocation", fullAddress);
                editor.apply();

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

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.RECEIVER_ID);
        params.put(WebParams.CATEGORY_ID, categoryId );
        params.put(WebParams.LATITUDE, latitude );
        params.put(WebParams.LONGITUDE, longitude );
        params.put(WebParams.RADIUS, "10");

        currentLatitude = latitude;
        currentLongitude = longitude;

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.SENDER_ID + DefineValue.RECEIVER_ID + BuildConfig.AppID + categoryId
                + latitude + longitude ));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.searchToko(getApplicationContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);
                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {



                        JSONArray shops = response.getJSONArray("shop");

                        shopDetails.clear();


                        for(int j=0; j < shops.length();j++){
                            JSONObject object = shops.getJSONObject(j);
                            ShopDetail shopDetail = new ShopDetail();




                            shopDetail.setMemberId(object.getString("member_id"));
                            shopDetail.setShopLatitude(object.getDouble("shop_latitude"));
                            shopDetail.setShopLongitude(object.getDouble("shop_longitude"));
                            shopDetail.setMemberName(object.getString("member_name"));
                            shopDetail.setShopAddress(object.getString("shop_address"));

                            shopDetails.add(shopDetail);
                        }

                        //tabPageAdapter.notifyDataSetChanged();




                    } else {
                        shopDetails.clear();
                        Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
                    }

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
                    Toast.makeText(getApplicationContext(), Resources.getSystem().getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

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

}
