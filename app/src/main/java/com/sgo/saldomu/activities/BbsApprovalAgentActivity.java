package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class BbsApprovalAgentActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        EasyPermissions.PermissionCallbacks {

    private SecurePreferences sp;
    ProgressDialog progdialog, progdialog2;
    String customerId, title, gcmId, flagTxStatus, txId, memberId, shopId;
    ShopDetail shopDetail;
    List<ShopDetail> shopDetails;
    TextView tvCategoryName, tvMemberName, tvAmount, tvCountTrx, tvTotalTrx;
    RelativeLayout rlApproval;
    Button btnApprove;
    int itemId;
    Double currentLatitude, currentLongitude;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    createLocationRequest();
                }

                googleApiClient.connect();
            } else {
                // Ask for one permission
                EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                        GlobalSetting.RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
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

        } catch (Exception e) {
            e.printStackTrace();
        }


        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        title = getString(R.string.menu_item_title_trx_agent);
        initializeToolbar();

        gcmId = "";
        flagTxStatus = "";
        txId = "";
        customerId = sp.getString(DefineValue.USERID_PHONE, "");

        btnApprove = (Button) findViewById(R.id.btnApprove);
        //btnReject               = (Button) findViewById(R.id.btnReject);
        tvCategoryName = (TextView) findViewById(R.id.tvCategoryName);
        tvMemberName = (TextView) findViewById(R.id.tvMemberName);
        tvAmount = (TextView) findViewById(R.id.tvAmount);
        tvCountTrx = (TextView) findViewById(R.id.tvCountTrx);
        tvTotalTrx = (TextView) findViewById(R.id.tvTotalTrx);
        //tvShop                  = (TextView) findViewById(R.id.tvShop);
        //spPilihan               = (Spinner) findViewById(R.id.spPilihan);

        //tvShop.setVisibility(View.GONE);
        //spPilihan.setVisibility(View.GONE);
        btnApprove.setEnabled(false);

        rlApproval = (RelativeLayout) findViewById(R.id.rlApproval);
        rlApproval.setVisibility(View.GONE);

        shopDetail = new ShopDetail();
        shopDetails = new ArrayList<>();


        if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
            //is member
            Intent i = new Intent(this, MainPage.class);
            startActivity(i);
            finish();
        }

        progdialog = DefinedDialog.CreateProgressDialog(this, "");

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRANSACTION_AGENT);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_PHONE, customerId);
        params.put(WebParams.SHOP_REMARK, gcmId);
        params.put(WebParams.USER_ID, customerId);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_TRANSACTION_AGENT, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);

                            if (code.equals(WebParams.SUCCESS_CODE)) {
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
                                tvMemberName.setText(response.getString(WebParams.KEY_NAME));
                                //tvShop.setText(shopDetail.getShopName());
                                tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(shopDetail.getAmount()));

                                tvCountTrx.setText(response.getString(WebParams.COUNT_TRX));
                                tvTotalTrx.setText(DefineValue.IDR + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_TRX)));
                                if (progdialog.isShowing())
                                    progdialog.dismiss();

                            } else {

                                rlApproval.setVisibility(View.GONE);

                                AlertDialog alertDialog = new AlertDialog.Builder(BbsApprovalAgentActivity.this).create();
                                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                alertDialog.setMessage(getString(R.string.alertbox_message_information));
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                        (dialog, which) -> {
                                            dialog.dismiss();
                                            finish();

                                        });
                                alertDialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (progdialog.isShowing())
                            progdialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        if (progdialog.isShowing())
                            progdialog.dismiss();
                    }
                });

        btnApprove.setOnClickListener(
                v -> {
                    progdialog2 = DefinedDialog.CreateProgressDialog(BbsApprovalAgentActivity.this, "");
                    flagTxStatus = DefineValue.STRING_ACCEPT;
                    itemId = 0;
                    if (shopDetails.size() > 0) {
                        shopId = shopDetails.get(itemId).getShopId();
                        memberId = shopDetails.get(itemId).getMemberId();
                        gcmId = "";
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
        String extraSignature = txId + memberId + shopId + flagTxStatus;
        HashMap<String, Object> params3 = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_APPROVAL_TRX_AGENT, extraSignature);

        params3.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params3.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params3.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params3.put(WebParams.TX_ID, shopDetails.get(itemId).getTxId());
        params3.put(WebParams.MEMBER_ID, memberId);
        params3.put(WebParams.SHOP_ID, shopId);
        params3.put(WebParams.TX_STATUS, flagTxStatus);
        params3.put(WebParams.USER_ID, userPhoneID);

        if (flagTxStatus.equals(DefineValue.STRING_ACCEPT)) {
            params3.put(WebParams.KEY_VALUE, gcmId);
            params3.put(WebParams.LATITUDE, currentLatitude);
            params3.put(WebParams.LONGITUDE, currentLongitude);
        }

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_APPROVAL_TRX_AGENT, params3,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);

                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                if (flagTxStatus.equals(DefineValue.STRING_ACCEPT)) {
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

                                    Intent i = new Intent(getApplicationContext(), BbsMapViewByAgentActivity.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Intent i = new Intent(getApplicationContext(), MainPage.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }
                            } else {
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                                Intent i = new Intent(getApplicationContext(), MainPage.class);
                                startActivity(i);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        progdialog2.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        progdialog2.dismiss();
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");
        //startLocationUpdate();

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            } else {

                currentLatitude = lastLocation.getLatitude();
                currentLongitude = lastLocation.getLongitude();
                btnApprove.setEnabled(true);
                Timber.d("Location Found" + lastLocation.toString());
                googleApiClient.disconnect();
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
        currentLatitude = lastLocation.getLatitude();
        currentLongitude = lastLocation.getLongitude();

        btnApprove.setEnabled(true);
        googleApiClient.disconnect();

        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
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
        mLocationRequest.setInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();


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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        googleApiClient.connect();

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        switch (requestCode) {
            case GlobalSetting.RC_LOCATION_PERM:
                // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
                // This will display a dialog directing them to enable the permission in app settings.
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    new AppSettingsDialog.Builder(this).build().show();
                } else {
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.alertbox_title_warning));
                    alertDialog.setMessage(getString(R.string.alertbox_message_warning));
                    alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            });
                    alertDialog.show();
                }
                break;

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
