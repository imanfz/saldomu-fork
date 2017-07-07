package com.sgo.hpku.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.sgo.hpku.Beans.myFriendModel;
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
import com.sgo.hpku.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class BbsApprovalAgentActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private SecurePreferences sp;
    ProgressDialog progdialog, progdialog2;
    String flagApprove, customerId, title, gcmId, flagTxStatus, txId, memberId, shopId;
    ShopDetail shopDetail;
    List<ShopDetail> shopDetails;
    TextView tvCategoryName, tvMemberName, tvAmount, tvShop;
    RelativeLayout rlApproval;
    Spinner spPilihan;
    ArrayAdapter<String> SpinnerAdapter;
    Button btnApprove, btnReject;
    int itemId;
    Double currentLatitude, currentLongitude;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( checkPlayServices() ) {
            buildGoogleApiClient();
            createLocationRequest();
        }


        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        title                   = getString(R.string.menu_item_title_trx_agent);
        initializeToolbar();

        gcmId                   = "";
        flagTxStatus            = "";
        txId                    = "";
        flagApprove             = DefineValue.STRING_NO;
        customerId              = sp.getString(DefineValue.USERID_PHONE, "");

        btnApprove              = (Button) findViewById(R.id.btnApprove);
        btnReject               = (Button) findViewById(R.id.btnReject);
        tvCategoryName          = (TextView) findViewById(R.id.tvCategoryName);
        tvMemberName            = (TextView) findViewById(R.id.tvMemberName);
        tvAmount                = (TextView) findViewById(R.id.tvAmount);
        tvShop                  = (TextView) findViewById(R.id.tvShop);
        spPilihan               = (Spinner) findViewById(R.id.spPilihan);

        //tvShop.setVisibility(View.GONE);
        spPilihan.setVisibility(View.GONE);

        rlApproval              = (RelativeLayout) findViewById(R.id.rlApproval);



        rlApproval.setVisibility(View.GONE);

        shopDetail              = new ShopDetail();
        shopDetails             = new ArrayList<>();



        if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            //is member
            startActivity(new Intent(this, MainPage.class));
        }

        /*
        shopDetail.setKeyCode("62828282");
        shopDetail.setKeyName("Ariawan Agus");
        shopDetail.setCategoryName("Tarik Tunai");
        shopDetail.setKeyProvince("Banten");
        shopDetail.setKeyCountry("Indonesia");
        shopDetail.setKeyDistrict("Alam Sutera");
        shopDetail.setKeyAddress("Jln. Haji Ali, Alam Sutera");
        shopDetail.setAmount("10000");
        shopDetail.setCcyId("IDR");

        ShopDetail shopDetail2   = new ShopDetail();
        shopDetail2.setMemberCode("KODE-ABC1");
        shopDetail2.setMemberName("NAMA-ABC1");
        shopDetail2.setShopId("KODE-ABC1");
        shopDetails.add(shopDetail2);

        ShopDetail shopDetail3   = new ShopDetail();
        shopDetail3.setMemberCode("KODE-ABC2");
        shopDetail3.setMemberName("NAMA-ABC2");
        shopDetail3.setShopId("KODE-ABC2");
        shopDetails.add(shopDetail3);
        */


        /*if ( shopDetails.size() > 1 ) {
            String[] arrayItems = new String[shopDetails.size()];
            for(int x = 0; x < shopDetails.size(); x++) {
                arrayItems[x] = shopDetails.get(x).getMemberName();
            }

            SpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arrayItems);
            spPilihan.setAdapter(SpinnerAdapter);

        }*/

        progdialog              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");

        RequestParams params    = new RequestParams();

        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppIDHpku);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_PHONE, customerId);
        params.put(WebParams.SHOP_REMARK, gcmId);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppIDHpku + customerId ));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getListTransactionAgent(getApplication(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                try {

                    String code = response.getString(WebParams.ERROR_CODE);

                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        progdialog.dismiss();
                        rlApproval.setVisibility(View.VISIBLE);

                        shopDetail.setAmount(response.getString(DefineValue.KEY_AMOUNT));
                        shopDetail.setTxId(response.getString(DefineValue.TX_ID2));
                        shopDetail.setCategoryId(response.getString(DefineValue.CATEGORY_ID));
                        shopDetail.setCategoryName(response.getString(DefineValue.CATEGORY_NAME));
                        shopDetail.setCategoryCode(response.getString(DefineValue.CATEGORY_CODE));
                        shopDetail.setKeyName(response.getString(DefineValue.KEY_NAME));
                        shopDetail.setKeyAddress(response.getString(DefineValue.KEY_ADDRESS));
                        //shopDetail.setKeyDistrict(response.getString(DefineValue.KEY_DISTRICT));
                        shopDetail.setKeyAddress(response.getString(DefineValue.KEY_ADDRESS));
                        //shopDetail.setKeyProvince(response.getString(DefineValue.KEY_PROVINCE));
                        //shopDetail.setKeyCountry(response.getString(DefineValue.KEY_COUNTRY));
                        shopDetail.setCommId(response.getString(WebParams.COMM_ID));

                        shopDetail.setMemberId(response.getString(WebParams.MEMBER_ID));
                        shopDetail.setMemberCode(response.getString(WebParams.MEMBER_CODE));
                        shopDetail.setMemberName(response.getString(WebParams.MEMBER_NAME));
                        shopDetail.setMemberType(response.getString(WebParams.MEMBER_TYPE));
                        shopDetail.setShopId(response.getString(WebParams.SHOP_ID));
                        shopDetail.setShopName(response.getString(WebParams.SHOP_NAME));

                        shopDetails.add(shopDetail);

                        tvCategoryName.setText(shopDetail.getCategoryName());
                        tvMemberName.setText(shopDetail.getMemberName());
                        tvShop.setText(shopDetail.getShopName());
                        tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(shopDetail.getAmount()));

                        /*
                        RequestParams params2    = new RequestParams();

                        UUID rcUUID2             = UUID.randomUUID();
                        String  dtime2           = DateTimeFormat.getCurrentDateTime();

                        params2.put(WebParams.RC_UUID, rcUUID2);
                        params2.put(WebParams.RC_DATETIME, dtime2);
                        params2.put(WebParams.APP_ID, BuildConfig.AppID);
                        params2.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                        params2.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                        params2.put(WebParams.CUSTOMER_ID, customerId);
                        params2.put(WebParams.FLAG_APPROVE, flagApprove);

                        String signature2 = HashMessage.SHA1(HashMessage.MD5(rcUUID2 + dtime2 + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + customerId + BuildConfig.AppID + flagApprove));

                        params2.put(WebParams.SIGNATURE, signature2);

                        MyApiClient.getMemberShopList(getApplication(), params2, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                progdialog.dismiss();

                                try {

                                    String code = response.getString(WebParams.ERROR_CODE);
                                    if (code.equals(WebParams.SUCCESS_CODE)) {

                                        rlApproval.setVisibility(View.VISIBLE);

                                        JSONArray members = response.getJSONArray("member");
                                        for (int i = 0; i < members.length(); i++) {
                                            JSONObject object       = members.getJSONObject(i);

                                            ShopDetail shopDetail2   = new ShopDetail();
                                            shopDetail2.setMemberId(object.getString("member_id"));
                                            shopDetail2.setShopId(object.getString("shop_id"));
                                            shopDetail2.setMemberCode(object.getString("member_code"));
                                            shopDetail2.setMemberName(object.getString("member_name"));
                                            shopDetail2.setMemberType(object.getString("member_type"));
                                            shopDetail2.setCommName(object.getString("comm_name"));
                                            shopDetail2.setCommCode(object.getString("comm_code"));
                                            shopDetail2.setShopAddress(object.getString("address1"));
                                            shopDetail2.setShopDistrict(object.getString("district"));
                                            shopDetail2.setShopProvince(object.getString("province"));
                                            shopDetail2.setShopCountry(object.getString("country"));

                                            shopDetails.add(shopDetail2);
                                        }

                                        if ( shopDetails.size() > 1 ) {
                                            String[] arrayItems = new String[shopDetails.size()];

                                            for(int x = 0; x < shopDetails.size(); x++) {
                                                arrayItems[x] = shopDetails.get(x).getMemberName();
                                            }

                                            SpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arrayItems);
                                            spPilihan.setAdapter(SpinnerAdapter);

                                            spPilihan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                                @Override
                                                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {


                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> arg0) {

                                                }
                                            });

                                        }

                                    } else if ( code.equals(WebParams.LOGOUT_CODE) ) {

                                    } else {
                                        code = response.getString(WebParams.ERROR_MESSAGE);
                                        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
                        */

                    } else {
                        progdialog.dismiss();
                        code = response.getString(WebParams.ERROR_MESSAGE);
                        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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


        rlApproval.setVisibility(View.VISIBLE);

        /*
        tvCategoryName.setText(shopDetail.getCategoryName());
        tvMemberName.setText(shopDetail.getKeyName());
        tvAmount.setText(shopDetail.getCcyId()+" "+ CurrencyFormat.format(shopDetail.getAmount()));

        if ( shopDetails.size() == 1 ) {
            tvShop.setVisibility(View.VISIBLE);
            spPilihan.setVisibility(View.GONE);
            shopDetails.get(0).getMemberName();
            tvShop.setText(shopDetails.get(0).getMemberName());
        } else {
            tvShop.setVisibility(View.GONE);
            spPilihan.setVisibility(View.VISIBLE);
        }
        */

        btnApprove.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    progdialog2              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");
                    flagTxStatus = DefineValue.STRING_ACCEPT;

                    if ( shopDetails.size() > 1 ) {
                        itemId  = spPilihan.getSelectedItemPosition();

                    } else {
                        itemId = 0;
                    }

                    shopId          = shopDetails.get(itemId).getShopId();
                    memberId        = shopDetails.get(itemId).getMemberId();
                    gcmId           = "";

                    updateTrxAgent();
                }
            }
        );

        btnReject.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    progdialog2              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");
                    flagTxStatus = DefineValue.STRING_CANCEL;

                    if ( shopDetails.size() > 1 ) {
                        itemId  = spPilihan.getSelectedItemPosition();

                    } else {
                        itemId = 0;
                    }


                    shopId          = shopDetails.get(itemId).getShopId();
                    memberId        = shopDetails.get(itemId).getMemberId();
                    updateTrxAgent();
                }
            }
        );



    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_approval_agent;
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

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    private void updateTrxAgent() {


        //startActivity(new Intent(getApplicationContext(), BbsMapViewByAgentActivity.class));


        RequestParams params3    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params3.put(WebParams.RC_UUID, rcUUID);
        params3.put(WebParams.RC_DATETIME, dtime);
        params3.put(WebParams.APP_ID, BuildConfig.AppIDHpku);
        params3.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params3.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params3.put(WebParams.TX_ID, shopDetails.get(itemId).getTxId());
        params3.put(WebParams.MEMBER_ID, memberId);
        params3.put(WebParams.SHOP_ID, shopId);
        params3.put(WebParams.TX_STATUS, flagTxStatus);

        if ( flagTxStatus.equals(DefineValue.STRING_ACCEPT) ) {
            params3.put(WebParams.KEY_VALUE, gcmId);
            params3.put(WebParams.LATITUDE, currentLatitude);
            params3.put(WebParams.LONGITUDE, currentLongitude);
        }

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppIDHpku + shopDetails.get(itemId).getTxId() + memberId + shopId + flagTxStatus ));

        params3.put(WebParams.SIGNATURE, signature);

        MyApiClient.updateTransactionAgent(getApplication(), params3, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog2.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        if ( flagTxStatus.equals(DefineValue.STRING_ACCEPT) ) {
                            SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                            SecurePreferences.Editor mEditor = prefs.edit();
                            mEditor.putString(DefineValue.BBS_MEMBER_ID, memberId);
                            mEditor.putString(DefineValue.BBS_SHOP_ID, shopId);
                            mEditor.putString(DefineValue.BBS_TX_ID, shopDetails.get(itemId).getTxId());
                            mEditor.putDouble(DefineValue.AGENT_LATITUDE, currentLatitude);
                            mEditor.putDouble(DefineValue.AGENT_LONGITUDE, currentLongitude);
                            mEditor.putString(DefineValue.KEY_CCY, response.getString(DefineValue.KEY_CCY));
                            mEditor.putString(DefineValue.KEY_AMOUNT, response.getString(DefineValue.KEY_AMOUNT));
                            mEditor.putString(DefineValue.KEY_ADDRESS, response.getString(DefineValue.KEY_ADDRESS));
                            mEditor.putString(DefineValue.KEY_CODE, response.getString(DefineValue.KEY_CODE));
                            mEditor.putString(DefineValue.KEY_NAME, response.getString(DefineValue.KEY_NAME));
                            mEditor.putDouble(DefineValue.BENEF_LATITUDE, response.getDouble(DefineValue.KEY_LATITUDE));
                            mEditor.putDouble(DefineValue.BENEF_LONGITUDE, response.getDouble(DefineValue.KEY_LONGITUDE));
                            mEditor.apply();

                            startActivity(new Intent(getApplicationContext(), BbsMapViewByAgentActivity.class));
                        } else {
                            startActivity(new Intent(getApplicationContext(), MainPage.class));
                        }
                    } else {
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

                progdialog2.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");
        //startLocationUpdate();

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if ( lastLocation == null ){
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {

                currentLatitude     = lastLocation.getLatitude();
                currentLongitude    = lastLocation.getLongitude();

                Timber.d("Location Found" + lastLocation.toString());
                googleApiClient.disconnect();
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
        currentLatitude = lastLocation.getLatitude();
        currentLongitude = lastLocation.getLongitude();

        googleApiClient.disconnect();

        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
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
        mLocationRequest.setInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.DISPLACEMENT);
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
