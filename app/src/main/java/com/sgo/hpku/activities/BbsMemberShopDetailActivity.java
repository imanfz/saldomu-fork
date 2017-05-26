package com.sgo.hpku.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsMemberLocationActivity;
import com.sgo.hpku.coreclass.BaseActivity;
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

import java.util.UUID;

import timber.log.Timber;

public class BbsMemberShopDetailActivity extends BaseActivity {

    String shopId, memberId;
    ProgressDialog progdialog;
    SecurePreferences sp;
    ShopDetail shopDetail;
    TextView tvMemberName, tvShopName;
    Spinner spPilihan;
    ArrayAdapter<String> SpinnerAdapter;
    String[] arrayItems = new String[3];

    String[] actualValues = new String[3];
    String selectedValue    = "";
    ImageView ivLocation, ivCategory, ivCloseShop;
    String flagApprove, setupOpenHour;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvMemberName    = (TextView) findViewById(R.id.tvMemberName);
        tvShopName      = (TextView) findViewById(R.id.tvShopName);
        //spPilihan       = (Spinner) findViewById(R.id.spPilihan);
        ivLocation      = (ImageView) findViewById(R.id.ivLocation);
        ivCategory      = (ImageView) findViewById(R.id.ivCategory);
        ivCloseShop     = (ImageView) findViewById(R.id.ivCloseShop);

        progdialog      = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");
        memberId        = getIntent().getStringExtra("memberId");
        shopId          = getIntent().getStringExtra("shopId");
        flagApprove     = getIntent().getStringExtra("flagApprove");
        setupOpenHour   = "";
        sp              = CustomSecurePref.getInstance().getmSecurePrefs();
        shopDetail      = new ShopDetail();
        initializeToolbar();

        arrayItems[0]   = "Silakan Pilih";
        arrayItems[1]   = getString(R.string.yes);
        arrayItems[2]   = getString(R.string.no);

        actualValues[0] = "";
        actualValues[1] = DefineValue.STRING_YES;
        actualValues[2] = DefineValue.STRING_NO;

        /*
        SpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayItems);
        spPilihan.setAdapter(SpinnerAdapter);

        spPilihan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selectedValue = actualValues[ arg2];

                /*if ( arg2 == 1 ) {
                    progdialog              = DefinedDialog.CreateProgressDialog(getApplicationContext(), "");

                    RequestParams params    = new RequestParams();
                    UUID rcUUID             = UUID.randomUUID();
                    String  dtime           = DateTimeFormat.getCurrentDateTime();
                    String customerId       = sp.getString(DefineValue.USERID_PHONE, "");
                    String flagApprove      = DefineValue.STRING_NO;

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.AppID);
                    params.put(WebParams.SENDER_ID, BuildConfig.AodSenderID);
                    params.put(WebParams.RECEIVER_ID, BuildConfig.AodReceiverID);
                    params.put(WebParams.SHOP_ID, shopId);
                    params.put(WebParams.MEMBER_ID, memberId);

                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + BuildConfig.AodSenderID + BuildConfig.AodReceiverID + memberId + shopId + BuildConfig.AppID));

                    params.put(WebParams.SIGNATURE, signature);

                    MyApiClient.updateCloseShopToday(getApplication(), params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            progdialog.dismiss();

                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {


                                    Intent intent=new Intent(BbsMemberShopDetailActivity.this,BbsMemberShopActivity.class);
                                    startActivity(intent);

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
                } else if ( arg2 == 2 ) {
                    Intent intent=new Intent(BbsMemberShopDetailActivity.this,BbsRegisterOpenClosedShopActivity.class);
                    intent.putExtra("memberId", memberId);
                    intent.putExtra("shopId", shopId);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        */

        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();
        String customerId       = sp.getString(DefineValue.USERID_PHONE, "");

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.RECEIVER_ID );
        params.put(WebParams.SHOP_ID, shopId);
        params.put(WebParams.MEMBER_ID, memberId);
        params.put(WebParams.FLAG_APPROVE, flagApprove);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.SENDER_ID + DefineValue.RECEIVER_ID + memberId + shopId + BuildConfig.AppID + flagApprove));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getMemberShopDetail(getApplication(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        tvMemberName.setText(response.getString("member_name"));
                        tvShopName.setText(response.getString("shop_name"));
                        setupOpenHour = response.getString("setup_open_hour");

                        int defaultPosition = 0;

                        if ( response.getString("shop_closed").equals(DefineValue.STRING_YES) ) {
                            defaultPosition = 1;
                        } else if ( response.getString("shop_closed").equals(DefineValue.STRING_NO) ) {
                            defaultPosition = 2;
                        }

                        if ( flagApprove.equals(DefineValue.STRING_NO)  ) {
                            if ( response.getString("shop_latitude").equals("") && response.getString("shop_longitude").equals("") ) {
                                ivLocation.setVisibility(View.VISIBLE);
                            } else {
                                ivLocation.setVisibility(View.GONE);
                            }

                            ivCategory.setVisibility(View.VISIBLE);
                            ivCloseShop.setVisibility(View.VISIBLE);
                        } else {
                            ivCategory.setVisibility(View.GONE);
                            ivLocation.setVisibility(View.GONE);
                            ivCloseShop.setVisibility(View.VISIBLE);
                        }

                        //spPilihan.setSelection(defaultPosition);

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

        ivLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BbsMemberLocationActivity.class);
                intent.putExtra("memberId", memberId);
                intent.putExtra("shopId", shopId);
                startActivity(intent);
            }
        });

        ivCategory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BbsMerchantCategoryActivity.class);
                intent.putExtra("memberId", memberId);
                intent.putExtra("shopId", shopId);
                intent.putExtra("flagApprove", flagApprove);
                intent.putExtra("setupOpenHour", setupOpenHour);
                startActivity(intent);
            }
        });

        ivCloseShop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BbsSetupShopClosedActivity.class);
                intent.putExtra("memberId", memberId);
                intent.putExtra("shopId", shopId);
                intent.putExtra("flagApprove", flagApprove);
                startActivity(intent);
            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_member_shop_detail;
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

}
