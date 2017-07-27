package com.sgo.saldomu.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.sgo.saldomu.adapter.TabAgentPagerAdapter;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.MainAgentIntentService;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.dialogs.AgentDetailFragmentDialog;
import com.sgo.saldomu.entityRealm.AgentDetail;
import com.sgo.saldomu.entityRealm.AgentServiceDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import com.sgo.saldomu.R;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class MainAgentActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{

    private int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;
    private float ZOOM_CAMERA_POSITION = 16.0f;
    private long INTERVAL_LOCATION_REQUEST = 10000; //10000
    private long FASTEST_INTERVAL_LOCATION_REQUEST = 5000; //5000

    public ViewPager viewPager;
    private String searchLocationString;
    private Dialog dialog;
    private TextView errorMsg;
    private Button backBtn;
    public MainResultReceiver agentMapResultReceiver;
    public MainResultReceiver agentListResultReceiver;
    public MainResultReceiver agentListMapResultReceiver;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Location updateLocation;
    private Address searchLocation;
    private Handler handler = null;
    private static Runnable runnable = null;
    private boolean backStatus = false;
    private int searchLocationResult = AgentConstant.FALSE;
    private int lastLocationResult   = AgentConstant.FALSE;
    private int agentInfoResult      = AgentConstant.FALSE;
    private int pickupLocationResult = AgentConstant.FALSE;
    private String errorDesc;
    private boolean searchLocationChecked;
    Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //set tittle in action bar
        //getSupportActionBar().setTitle("Cash In / Out Agent");

        //Mengaktifkan button back di action bar
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        initializeToolbar("Cari Agen");

        //delete or clear all session
        deleteAllSharedPreferences();

        //get data search location from previous activity
        getBundle();

        process();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.main_agent_activity;
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

    //get bundle from previous activity
    private void getBundle()
    {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            searchLocationString  = bundle.getString("searchLocationString");
            searchLocationChecked = bundle.getBoolean("searchLocationChecked");

            //menghilangkan spasi kiri dan kanan
            searchLocationString = searchLocationString.trim();
        }
    }

    private void displayDialog()
    {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //menghilangkan frame windows
        dialog.setContentView(R.layout.progress_dialog_agent);
        dialog.setCancelable(false); //agar dialog tidak bisa di-cancel
        dialog.show();
    }


    private void process()
    {
        //display progress dialog
        displayDialog();

        //jika radio button search location yang dipilih maka proses ini :
        if(searchLocationChecked)
        {
            if(searchLocationString.equals(""))
            {
                String errorDesc = "Please enter your search location first";
                displayErrorLayout(errorDesc);
            }
            else
            {
                //setContentView(R.layout.main_bbs_activity);
                mainProcess();
            }
        }
        //jika radio button current location yang dipilih maka proses ini :
        else
        {
            //setContentView(R.layout.main_bbs_activity);
            mainProcess();
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

    //implements View.OnClickListener
    @Override
    public void onClick(View view)
    {
        if(view.getId() == backBtn.getId())
        {
            //kembali ke activity sebelumnya
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        //set tittle in action bar
        //getSupportActionBar().setTitle("Cash In / Out Agent");
        initializeToolbar("Cari Agen");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        //setelah prosess booking berhasil, maka akan kembali ke main activity dan tab history
        checkHistoryResult();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //delete or clear all session
        deleteAllSharedPreferences();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        //stop listener for location update
        stopLocationUpdate();

        //stop handler for get agent info
        if(handler != null)
        {
            handler.removeCallbacks(runnable);
        }

        //disconnect google api client
        if (googleApiClient != null)
        {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
       /* if(googleApiClient.isConnected()) {
            //startLocationUpdates();
        }*/
    }

    private void checkHistoryResult()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean historyResult = preferences.getBoolean(AgentConstant.HISTORY_RESULT_SHARED_PREFERENCES, false);

        if(viewPager != null && historyResult)
        {
            //close fragment dialog of detail agent
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(AgentConstant.AGENT_DETAIL_FRAGMENT_DIALOG_TAG);

            if(fragment != null)
            {
                //DialogFragment dialogFragment = (DialogFragment)fragment;
                AgentDetailFragmentDialog dialogFragment = (AgentDetailFragmentDialog)fragment;
                dialogFragment.dismiss();
            }

            //mengarahkan viewpager ke tab history
            viewPager.setCurrentItem(2);

            //save data to session
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(AgentConstant.HISTORY_RESULT_SHARED_PREFERENCES, false);
            editor.apply();
        }
    }

    private void deleteAllSharedPreferences()
    {
        deleteSingleSharedPreferences(AgentConstant.SEARCH_LOCATION_SHARED_PREFERENCES);
        deleteSingleSharedPreferences(AgentConstant.LAST_LOCATION_SHARED_PREFERENCES);
        deleteSingleDefaultSharedPreferences(AgentConstant.AGENT_INFO_SHARED_PREFERENCES);
        deleteSingleDefaultSharedPreferences(AgentConstant.AGENT_INFO_SINGLE_SHARED_PREFERENCES);
        deleteSingleDefaultSharedPreferences(AgentConstant.HISTORY_RESULT_SHARED_PREFERENCES);
    }

    private void deleteSingleSharedPreferences(String name)
    {
        SharedPreferences preferences = getSharedPreferences(name, MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    private void deleteSingleDefaultSharedPreferences(String name)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().remove(name).apply();
    }

    private void setTabViewPager()
    {
        TabAgentPagerAdapter tabBbsPagerAdapter = new TabAgentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(4); // use a number higher than half your fragments.
        viewPager.setAdapter(tabBbsPagerAdapter);

        //PagerSlidingTabStrip tabPagerSliding = (PagerSlidingTabStrip) findViewById(R.id.tabPagerSliding);
        // tabPagerSliding.setViewPager(viewPager);
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

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //implements GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle)
    {
        /*startIntentService(); */

        startLocationUpdate();


        handler  = new Handler();
        runnable = new Runnable()
        {
            public void run()
            {




                //proses ini akan diexecute sekali saat activity oncreate.
                //ketika kembali ke activity ini dari activity lain, maka proses ini tidak akan dijalankan
                if(!backStatus)
                {
                    //set true, agar proses dibawah tidak dijalankan lagi
                    backStatus = true;

                    if(checkInternetConnectivity())
                    {
                        //jika radio button search location yang dipilih maka proses ini :
                        if(searchLocationChecked)
                        {
                            setSearchLocation(); //connect to internet

                            if (searchLocationResult == AgentConstant.TRUE) {
                                setLastLocation(null); //connect to satelit

                                if (lastLocationResult == AgentConstant.TRUE) {
                                    setAgentInfoDummy(); //connect to internet
                                    //setAgentInfo();

                                    if (agentInfoResult == AgentConstant.TRUE) {
                                        //set tab & view pager and generate tab fragment also
                                        setTabViewPager();
                                    }
                                }
                            }

                            if (agentInfoResult == AgentConstant.FALSE) {
                                displayErrorLayout(errorDesc);
                            }
                        }
                        //jika radio button current location yang dipilih maka proses ini :
                        else {
                            setLastLocation(null); //connect to satelit

                            if (lastLocationResult == AgentConstant.TRUE) {
                                setPickupLocation();

                                if (pickupLocationResult == AgentConstant.TRUE) {
                                    setAgentInfoDummy(); //connect to internet
                                    //setAgentInfo();

                                    if (agentInfoResult == AgentConstant.TRUE) {
                                        //set tab & view pager and generate tab fragment also
                                        setTabViewPager();
                                    }
                                }
                            }

                            if (agentInfoResult == AgentConstant.FALSE) {
                                displayErrorLayout(errorDesc);
                            }
                        }
                    }
                    else
                    {
                        displayErrorLayout(errorDesc);
                    }

                    dialog.dismiss();
                }

                //jika ketiga proses berhasil, baru start handler dan listener
                if(searchLocationResult == AgentConstant.TRUE && lastLocationResult == AgentConstant.TRUE && agentInfoResult == AgentConstant.TRUE)
                {
                    //proses ini akan selalu dijalankan saat start or resume activity
                    //startLocationUpdate();
                    //startAgentInfoHandler();
                }




            }
        };
        handler.postDelayed(runnable, 4000);

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

    private void setSearchLocation()
    {
        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address> multiAddress = geocoder.getFromLocationName(searchLocationString, 1);

            if(multiAddress != null && !multiAddress.isEmpty() && multiAddress.size() > 0)
            {
                Address singleAddress = multiAddress.get(0);
                ArrayList<String> addressArray = new ArrayList<String>();

                for (int i = 0; i < singleAddress.getMaxAddressLineIndex(); i++) {
                    addressArray.add(singleAddress.getAddressLine(i));
                }

                String fullAddress = TextUtils.join(System.getProperty("line.separator"), addressArray);

                //set to global variabel
                searchLocation = singleAddress;

                //save data to session
                String latitudeString = Double.toString(searchLocation.getLatitude());
                String longitudeString = Double.toString(searchLocation.getLongitude());

                SharedPreferences preferences = getSharedPreferences(AgentConstant.SEARCH_LOCATION_SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("latitude", latitudeString);
                editor.putString("longitude", longitudeString);
                editor.apply();

                //set true for allow next process
                searchLocationResult = AgentConstant.TRUE;
            }
            else
            {
                errorDesc = "Search Location is not found. Please try again with different location";
            }
        }
        catch(IOException ioException)
        {
            // Catch network or other I/O problems.
            //errorMessage = "Catch : Network or other I/O problems - No geocoder available";
            errorDesc = "Catch : Network or other I/O problems - No geocoder available";
        }
        catch(IllegalArgumentException illegalArgumentException)
        {
            // Catch invalid latitude or longitude values.
            //errorMessage = "Catch : Invalid latitude or longitude values";
            errorDesc = "Catch : Invalid latitude or longitude values";
        }

    }

    private void setLastLocation(Location updatedLocation)
    {
        try
        {
            if(updatedLocation == null)
            {
                /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1600);
                }
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    //call function : onRequestPermissionsResult
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, BbsConstants.TRUE);
                }
                */

                if(lastLocation == null) lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            }
            else
            {
                //get updated location
                lastLocation = updatedLocation;
            }

            if(lastLocation != null)
            {
                //save data to session
                String latitudeString  = Double.toString(lastLocation.getLatitude());
                String longitudeString = Double.toString(lastLocation.getLongitude());

                SharedPreferences preferences = getSharedPreferences(AgentConstant.LAST_LOCATION_SHARED_PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("latitude", latitudeString);
                editor.putString("longitude", longitudeString);
                //store checked radio button from previous activity
                editor.putBoolean("searchLocationChecked", searchLocationChecked);
                editor.putString("pickupLocation", searchLocationString);
                editor.apply();

                //set true for allow next process
                lastLocationResult = AgentConstant.TRUE;
            }
            else
            {
                errorDesc = "Problem when trying to get location with GPS. Please try again..";
            }

            /*SharedPreferences preferences = getSharedPreferences("lastLocation", MODE_PRIVATE);
            String latitudeString  = preferences.getString("latitude", "0");
            String longitudeString = preferences.getString("longitude", "0");

            //convert string to double
            lastLatitude  = Double.parseDouble(latitudeString);
            lastLongitude = Double.parseDouble(longitudeString);*/
        }
        catch (SecurityException e)
        {
            //dialogGPS(this.getContext()); // lets the user know there is a problem with the gps
            errorDesc = "Exception : Problem when trying to get last location with GPS. Please try again.";
        }
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

    private void setAgentInfo()
    {
        //String url = "http://192.168.0.100/webserver/gethotlist/testing";
        String url = "http://192.168.43.206/webserver/gethotlist/testing";

        AsyncHttpClient client = new SyncHttpClient();

        //client.setConnectTimeout(5000);
        //client.setMaxConnections(1);
        //client.setResponseTimeout(10000);

        //sets the connect/socket timeout - default = 10s
        //client.setTimeout(5000);

        //sets the maximum retries and timeout between those retries
        //client.setMaxRetriesAndTimeout(1, 3000);

        client.get(url, null, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody)
            {
                //modification responseBody with adding parameter distance
                try
                {
                    int length = responseBody.length();

                    for (int i = 0; i < length; i++)
                    {
                        //convert json array to json object
                        JSONObject object      = responseBody.getJSONObject(i);
                        Double agentLatitude   = object.getDouble("latitude");
                        Double agentLongitude  = object.getDouble("longitude");

                        //jika radio button search location yang dipilih maka proses ini :
                        if(searchLocationChecked)
                        {
                            String distance = getDistance(agentLatitude, agentLongitude, searchLocation.getLatitude(), searchLocation.getLongitude());
                            //String distanceString = Double.toString(distance);
                            object.put("distance", distance);
                        }
                        //jika radio button current location yang dipilih maka proses ini :
                        else
                        {
                            String distance = getDistance(agentLatitude, agentLongitude, lastLocation.getLatitude(), lastLocation.getLongitude());
                            //String distanceString = Double.toString(distance);
                            object.put("distance", distance);
                        }
                    }
                }
                catch(JSONException ex)
                {
                    ex.printStackTrace();
                }

                //save data to session
                SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, responseBody.toString());
                editor.apply();

                //set true for allow next process
                agentInfoResult = AgentConstant.TRUE;

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //abc.setText("Gagal - Format Response Bukan JSON");
                //Toast.makeText(getBaseContext(), "json", Toast.LENGTH_SHORT).show();
                errorDesc = "Wrong Json Format";
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                //errMsg.setText(headers.toString() + " Gagal Koneksi");
                //Toast.makeText(getBaseContext(), "gagal koneksi", Toast.LENGTH_SHORT).show();
                errorDesc = "Connection Failed";
            }
        });

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

                //jika radio button search location yang dipilih maka proses ini :
                if(searchLocationChecked)
                {
                    String distance = getDistance(agentLatitude, agentLongitude, searchLocation.getLatitude(), searchLocation.getLongitude());
                    //String distanceString = Double.toString(distance);
                    object.put("distance", distance);

                    /*double latitude  = searchLocation.getLatitude() - incrementLatitude;
                    double longitude = searchLocation.getLongitude() + incrementLongitude;*/

                    double latitude;
                    double longitude;

                    if(i==0)
                    {
                        latitude  = searchLocation.getLatitude() - incrementLatitude;
                        longitude = searchLocation.getLongitude() - incrementLongitude;
                    }
                    else if(i==1)
                    {
                        latitude  = searchLocation.getLatitude() + incrementLatitude;
                        longitude = searchLocation.getLongitude() + incrementLongitude;
                    }
                    else if(i==2)
                    {
                        latitude  = searchLocation.getLatitude() - incrementLatitude;
                        longitude = searchLocation.getLongitude() - incrementLongitude;
                    }
                    else if(i==3)
                    {
                        latitude  = searchLocation.getLatitude() + incrementLatitude;
                        longitude = searchLocation.getLongitude() - incrementLongitude;
                    }
                    else if(i==4)
                    {
                        latitude  = searchLocation.getLatitude() + incrementLatitude;
                        longitude = searchLocation.getLongitude() + incrementLongitude;
                    }
                    else
                    {
                        latitude  = searchLocation.getLatitude() - incrementLatitude;
                        longitude = searchLocation.getLongitude() + incrementLongitude;
                    }

                    object.put("latitude",  Double.toString(latitude) );
                    object.put("longitude", Double.toString(longitude) );
                }
                //jika radio button current location yang dipilih maka proses ini :
                else
                {
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

    private void startLocationUpdate()
    {
        try
        {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(INTERVAL_LOCATION_REQUEST);
            locationRequest.setFastestInterval(FASTEST_INTERVAL_LOCATION_REQUEST);
            //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            //will call function : onLocationChanged
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
        catch (SecurityException e) {

        }
    }

    private void stopLocationUpdate()
    {
        if(googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void startIntentService(Location location)
    {
        Intent intent = new Intent(this, MainAgentIntentService.class);
        intent.putExtra("agentMapResultReceiver", agentMapResultReceiver);
        intent.putExtra("agentListMapResultReceiver", agentListMapResultReceiver);
        intent.putExtra("location",location);
        intent.putExtra("searchLocationChecked",searchLocationChecked);
        startService(intent);
    }

    /*//implements LocationListener
    @Override
    public void onLocationChanged(Location location)
    {
        //set updated location to session
        setLastLocation(location);

        //set pickup location to session
        setPickupLocation();

        //beritahu dan refresh fragment agent map bahwa ada location terbaru
        agentMapResultReceiver.send(0, null);

        //beritahu dan refresh fragment agent list map bahwa ada data agent terbaru
        agentListMapResultReceiver.send(0, null);
    }*/

    //implements LocationListener
    @Override
    public void onLocationChanged(Location location)
    {
        //Toast.makeText(getBaseContext(), "update : " + location.getLatitude(), Toast.LENGTH_SHORT).show();
        lastLocation = location;
        startIntentService(location);
    }

    private void startAgentInfoHandler()
    {
        //handler
        handler  = new Handler();
        runnable = new Runnable()
        {
            public void run()
            {
                //get & set updated agent to session
                //setAgentInfoUpdated();
                setAgentInfoUpdatedDummy();

                handler.postDelayed(runnable, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private void setAgentInfoUpdated()
    {
        //String url = "http://192.168.0.100/webserver/gethotlist/testing";
        String url = "http://192.168.43.206/webserver/gethotlist/testing";

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                //modification responseBody with adding parameter distance
                try {
                    int length = responseBody.length();

                    for (int i = 0; i < length; i++) {
                        //convert json array to json object
                        JSONObject object = responseBody.getJSONObject(i);
                        Double agentLatitude = object.getDouble("latitude");
                        Double agentLongitude = object.getDouble("longitude");

                        //jika radio button search location yang dipilih maka proses ini :
                        if (searchLocationChecked) {
                            String distance = getDistance(agentLatitude, agentLongitude, searchLocation.getLatitude(), searchLocation.getLongitude());
                            //String distanceString = Double.toString(distance);
                            object.put("distance", distance);
                        }
                        //jika radio button current location yang dipilih maka proses ini :
                        else {
                            String distance = getDistance(agentLatitude, agentLongitude, lastLocation.getLatitude(), lastLocation.getLongitude());
                            //String distanceString = Double.toString(distance);
                            object.put("distance", distance);
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                //save data to session
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, responseBody.toString());
                editor.apply();

                //beritahu dan refresh fragment agent list bahwa ada data agent terbaru
                agentListResultReceiver.send(0, null);

                //beritahu dan refresh fragment agent map bahwa ada data agent terbaru
                agentMapResultReceiver.send(0, null);

                //beritahu dan refresh fragment agent list map bahwa ada data agent terbaru
                agentListMapResultReceiver.send(0, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //abc.setText("Gagal - Format Response Bukan JSON");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                //errMsg.setText(headers.toString() + " Gagal Koneksi");

            }
        });

    }

    private void setAgentInfoUpdatedDummy()
    {
        JSONArray array = null;

        try
        {
            String string = "[{\"latitude\":\"-6.2233611\",\"longitude\":106.654809,\"name\":\"Yudistira Kiki\",\"date\":\"07:12:35\",\"address\":\"Jl. Kp. Sawah 26, Lengkong Kulon, Pagedangan, Tangerang\",\"image\":\"profile1\"}," +
                    "{\"latitude\":\"-6.2243611\",\"longitude\":106.655809,\"name\":\"Alan Warker\",\"date\":\"07:31:20\",\"address\":\"Jalan Geofisika Blok 1 P No.2 Pagedangan, Tangerang\",\"image\":\"profile2\"}," +
                    "{\"latitude\":\"-6.2253611\",\"longitude\":106.656809,\"name\":\"Erwin Junarta\",\"date\":\"08:05:05\",\"address\":\"Jl.Diponogoro Raya No 12, Tangerang\",\"image\":\"profile3\"}," +
                    "{\"latitude\":\"-6.2253611\",\"longitude\":106.656809,\"name\":\"Sulianto\",\"date\":\"07:13:20\",\"address\":\"Jl.Pembangunan Utara No 15, Tangerang\",\"image\":\"profile4\"}," +
                    "{\"latitude\":\"-6.2253611\",\"longitude\":106.656809,\"name\":\"Harry Prakoso\",\"date\":\"08:10:55\",\"address\":\"Jl.Sukasari bencongan Blok B No 2, Tangerang\",\"image\":\"profile5\"}," +
                    "{\"latitude\":\"-6.2253611\",\"longitude\":106.656809,\"name\":\"Raymond Chandra\",\"date\":\"08:17:22\",\"address\":\"Jl.Raya Serpong No 199, Tangerang\",\"image\":\"profile6\"}," +
                    "{\"latitude\":\"-6.2253611\",\"longitude\":106.656809,\"name\":\"Sasya Verisa\",\"date\":\"07:15:00\",\"address\":\"Jl.Cibodas Raya, sektor AB No 75, Tangerang\",\"image\":\"profile7\"}]";
            array = new JSONArray(string);

            for (int i = 0; i < 7; i++)
            {
                double incrementLatitude  = randomLocation();
                double incrementLongitude = randomLocation();

                //convert json array to json object
                JSONObject object = array.getJSONObject(i);
                Double agentLatitude   = object.getDouble("latitude");
                Double agentLongitude  = object.getDouble("longitude");

                //jika radio button search location yang dipilih maka proses ini :
                if(searchLocationChecked)
                {
                    String distance = getDistance(agentLatitude, agentLongitude, searchLocation.getLatitude(), searchLocation.getLongitude());
                    //String distanceString = Double.toString(distance);
                    object.put("distance", distance);

                    double latitude  = searchLocation.getLatitude() - incrementLatitude;
                    double longitude = searchLocation.getLongitude() + incrementLongitude;
                    object.put("latitude",  Double.toString(latitude) );
                    object.put("longitude", Double.toString(longitude) );
                }
                //jika radio button current location yang dipilih maka proses ini :
                else
                {
                    String distance = getDistance(agentLatitude, agentLongitude, lastLocation.getLatitude(), lastLocation.getLongitude());
                    //String distanceString = Double.toString(distance);
                    object.put("distance", distance);

                    double latitude  = lastLocation.getLatitude() - incrementLatitude;
                    double longitude = lastLocation.getLongitude() + incrementLongitude;
                    object.put("latitude",  Double.toString(latitude) );
                    object.put("longitude", Double.toString(longitude) );
                }
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }

        //save data to session
        SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, array.toString());
        editor.apply();

        //beritahu dan refresh fragment agent list bahwa ada data agent terbaru
        agentListResultReceiver.send(0, null);

        //beritahu dan refresh fragment agent map bahwa ada data agent terbaru
        agentMapResultReceiver.send(0, null);

        //beritahu dan refresh fragment agent list map bahwa ada data agent terbaru
        agentListMapResultReceiver.send(0, null);
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

    private Double randomLocation()
    {
        double random = Math.random() * 10;
        double x = (int) random;
        Double y =  x / 10;
        Double z =  y * 0.01;
        Double angkaRandom = 0.001 + z;

        return angkaRandom;
    }

    //implements GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {

    }

    //implements OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}