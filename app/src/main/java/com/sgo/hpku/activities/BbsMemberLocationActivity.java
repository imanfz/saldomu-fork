package com.sgo.hpku.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.adapter.GooglePlacesAutoCompleteArrayAdapter;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CustomAutoCompleteTextView;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.InetHandler;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.entityRealm.MerchantCommunityList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import timber.log.Timber;

public class BbsMemberLocationActivity extends BaseActivity implements OnMapReadyCallback,
        AdapterView.OnItemClickListener, TextView.OnEditorActionListener {

    String memberId, memberDefaultAddress, countryName, provinceName, districtName, shopId;
    Realm myRealm;
    TextView tvDetailMemberName, tvCommName, tvAddress, tvDistrict, tvProvince;
    private GoogleMap mMap;
    MapView mapView;
    Double selectedLat, selectedLong;
    Double defaultLat, defaultLong;
    Button btnSubmit;
    ProgressDialog progdialog;
    SecurePreferences sp;
    MerchantCommunityList memberDetail;
    GooglePlacesAutoCompleteArrayAdapter googlePlacesAutoCompleteBbsArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myRealm = Realm.getDefaultInstance();

        setActionBarIcon(R.drawable.ic_arrow_left);


        memberId            = getIntent().getStringExtra("memberId");
        shopId              = getIntent().getStringExtra("shopId");

        memberDetail        = myRealm.where(MerchantCommunityList.class).equalTo("memberId", memberId).equalTo("shopId", shopId).findFirst();

        googlePlacesAutoCompleteBbsArrayAdapter = new GooglePlacesAutoCompleteArrayAdapter(getApplicationContext(), R.layout.google_places_auto_complete_listview);

        setActionBarTitle(getString(R.string.update_merchant_location) + " - " + memberDetail.getMemberName());

        tvDetailMemberName  = (TextView) findViewById(R.id.tvDetailMemberName);
        tvCommName          = (TextView) findViewById(R.id.tvCommName);
        tvAddress           = (TextView) findViewById(R.id.tvAddress);
        tvProvince          = (TextView) findViewById(R.id.tvProvince);
        tvDistrict          = (TextView) findViewById(R.id.tvDistrict);

        tvDetailMemberName.setText(memberDetail.getMemberName());
        tvCommName.setText(memberDetail.getCommName());
        tvProvince.setText(memberDetail.getProvince());
        tvDistrict.setText(memberDetail.getDistrict());
        tvAddress.setText(memberDetail.getAddress1());

        memberDefaultAddress    = memberDetail.getDistrict() + ", "+ memberDetail.getProvince();

        defaultLat      = -6.121435;
        defaultLong     = 106.774124;

        btnSubmit       = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(btnSubmitListener);

    }

    Button.OnClickListener btnSubmitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getApplicationContext())) {
                if ( selectedLat == null || countryName == null )
                {
                    DefinedDialog.showErrorDialog(getApplicationContext(), getString(R.string.err_empty_coordinate_message));
                }
                else {
                    progdialog              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");

                    RequestParams params    = new RequestParams();
                    UUID rcUUID             = UUID.randomUUID();
                    String  dtime           = DateTimeFormat.getCurrentDateTime();

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.AppID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, memberDetail.getShopId());
                    params.put(WebParams.MEMBER_ID, memberDetail.getMemberId());
                    params.put(WebParams.DISTRICT, districtName);
                    params.put(WebParams.PROVINCE, provinceName);
                    params.put(WebParams.COUNTRY, countryName);
                    params.put(WebParams.LATITUDE, selectedLat);
                    params.put(WebParams.LONGITUDE, selectedLong);

                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId.toUpperCase() + memberDetail.getShopId().toUpperCase()
                            + BuildConfig.AppID + selectedLat + selectedLong));

                    params.put(WebParams.SIGNATURE, signature);

                    MyApiClient.updateMemberLocation(getApplication(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            progdialog.dismiss();

                            try {

                                String code = response.getString(
                                        WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    myRealm.beginTransaction();
                                    memberDetail.setSetupOpenHour(response.getString("setup_open_hour"));
                                    myRealm.copyToRealmOrUpdate(memberDetail);
                                    myRealm.commitTransaction();

                                    Intent intent=new Intent(getApplicationContext(), BbsMerchantCategoryActivity.class);
                                    intent.putExtra("memberId", memberDetail.getMemberId());
                                    intent.putExtra("shopId", memberDetail.getShopId());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(intent);

                                } else if ( code.equals(WebParams.LOGOUT_CODE) ) {
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    //test.showDialoginActivity(getActi,message);
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
                }
            }
            else DefinedDialog.showErrorDialog(getApplicationContext(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {

            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    public void setAdministrativeName() {
        Geocoder geocoder = new Geocoder(this, new Locale("id"));

        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocation(
                    selectedLat,
                    selectedLong,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.d("IOEXCEPTION", "IO EXCEPTION", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e("ILLEGAL", "ILLEGAL EXP" + ". " +
                    "Latitude = " + selectedLat +
                    ", Longitude = " +
                    selectedLong, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addressList == null || addressList.size()  == 0) {

        } else {
            Address addressDetail = addressList.get(0);

            ArrayList<String> addressFragments = new ArrayList<String>();
            districtName    = addressDetail.getSubAdminArea();
            provinceName    = addressDetail.getAdminArea();
            countryName     = addressDetail.getCountryName();

        }
    }

    public void onMapSearch(View view) {

        CustomAutoCompleteTextView locationSearch = (CustomAutoCompleteTextView) findViewById(R.id.editText);
        locationSearch.setAdapter(googlePlacesAutoCompleteBbsArrayAdapter);
        locationSearch.setOnItemClickListener(this);
        locationSearch.setOnEditorActionListener(this);

        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        Double latitude        = defaultLat;
        Double longitude       = defaultLong;
        mMap.clear();

        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(locationSearch.getWindowToken(), 0);

        if (!location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

                if ( addressList.size() > 0 ) {
                    Address address = addressList.get(0);
                    latitude        = address.getLatitude();
                    longitude       = address.getLongitude();
                    selectedLat     = latitude;
                    selectedLong    = longitude;

                    setAdministrativeName();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            LatLng latLng = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.err_empty_merchant_address), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        List<Address> addressList = null;
        Double latitude = defaultLat, longitude = defaultLong;

        if (memberDefaultAddress != null || !memberDefaultAddress.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(memberDefaultAddress, 1);
                Address address = addressList.get(0);
                latitude        = address.getLatitude();
                longitude       = address.getLongitude();
                selectedLat     = latitude;
                selectedLong    = longitude;

                setAdministrativeName();
            } catch (IOException e) {
                e.printStackTrace();
            }

            LatLng latLng = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(memberDefaultAddress));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // clearing map and generating new marker points if user clicks on map more than two times
                mMap.clear();
                selectedLat     = point.latitude;
                selectedLong    = point.longitude;
                mMap.addMarker(new MarkerOptions().position(point).title(memberDefaultAddress));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));



            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_member_location;
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
        setActionBarTitle(getString(R.string.shop_member_detail));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }
}
