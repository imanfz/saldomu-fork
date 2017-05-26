package com.sgo.hpku.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.fragments.OpenCloseDatePickerFragment;
import com.sgo.hpku.fragments.OpenHourPickerFragment;
import com.sgo.hpku.models.MerchantCommunityList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

public class BbsRegisterOpenClosedShopActivity extends BaseActivity implements OpenCloseDatePickerFragment.OpenCloseDatePickerListener,
        OpenHourPickerFragment.OpenHourPickerListener {

    ProgressDialog progdialog;
    ToggleButton tbOpenClosed;
    Button btnShopDate, btnProses, btnOpenHour;
    TextView tvDate, tvStartHour, tvEndHour;
    ArrayList<String> selectedDates = new ArrayList<>();
    ArrayList<Date> listDates = new ArrayList<>();
    SecurePreferences sp;
    String shopId, memberId, shopStatus, shopRemark, shopStartOpenHour, shopEndOpenHour;
    int iStartHour = 0, iStartMinute = 0, iEndHour = 0, iEndMinute = 0;
    LinearLayout llSetupHourForm, llSetupHourFormEnd, llHourForm;
    Boolean isClosed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        tbOpenClosed    = (ToggleButton) findViewById(R.id.tbOpenClosed);
        tbOpenClosed.setTextOn(getString(R.string.shop_open));
        tbOpenClosed.setTextOff(getString(R.string.shop_close));


        llSetupHourForm = (LinearLayout) findViewById(R.id.llSetupHourForm);
        llSetupHourFormEnd  = (LinearLayout) findViewById(R.id.llSetupHourFormEnd);
        llHourForm      = (LinearLayout) findViewById(R.id.llHourForm);

        llSetupHourForm.setVisibility(View.GONE);
        llSetupHourFormEnd.setVisibility(View.GONE);
        llHourForm.setVisibility(View.GONE);

        shopStatus      = DefineValue.SHOP_CLOSE;
        shopRemark      = "";
        shopStartOpenHour   = "";
        shopEndOpenHour     = "";

        memberId        = getIntent().getStringExtra("memberId");
        shopId          = getIntent().getStringExtra("shopId");

        tvStartHour     = (TextView) findViewById(R.id.tvStartHour);
        tvEndHour       = (TextView) findViewById(R.id.tvEndHour);

        btnShopDate     = (Button) findViewById(R.id.btnShopDate);
        btnShopDate.setOnClickListener(btnShopDateListener);

        btnOpenHour     = (Button) findViewById(R.id.btnOpenHour);
        btnOpenHour.setOnClickListener(btnOpenHourListener);

        btnProses       = (Button) findViewById(R.id.btnProses);
        tvDate          = (TextView) findViewById(R.id.tvDate);

        tbOpenClosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llSetupHourForm.setVisibility(View.VISIBLE);
                    llSetupHourFormEnd.setVisibility(View.VISIBLE);
                    llHourForm.setVisibility(View.VISIBLE);
                    isClosed = false;
                } else {
                    llSetupHourForm.setVisibility(View.GONE);
                    llSetupHourFormEnd.setVisibility(View.GONE);
                    llHourForm.setVisibility(View.GONE);
                    isClosed = true;
                }
            }
        });

        btnProses.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        progdialog = DefinedDialog.CreateProgressDialog(BbsRegisterOpenClosedShopActivity.this, "");
                        String shopDate = new Gson().toJson(selectedDates);
                        Boolean hasError = false;

                        if (shopDate.equals("")) {
                            hasError = true;
                            Toast.makeText(getApplication(), R.string.err_empty_shop_date, Toast.LENGTH_SHORT).show();
                        }

                        if ( !isClosed ) {

                            if ( shopStartOpenHour.equals("") || shopEndOpenHour.equals("") ) {
                                hasError = true;
                                Toast.makeText(getApplication(), R.string.err_empty_shop_hour, Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (!hasError) {
                            RequestParams params = new RequestParams();
                            UUID rcUUID = UUID.randomUUID();
                            String dtime = DateTimeFormat.getCurrentDateTime();


                            params.put(WebParams.RC_UUID, rcUUID);
                            params.put(WebParams.RC_DATETIME, dtime);
                            params.put(WebParams.APP_ID, BuildConfig.AppID);
                            params.put(WebParams.SENDER_ID, DefineValue.SENDER_ID);
                            params.put(WebParams.RECEIVER_ID, DefineValue.RECEIVER_ID);
                            params.put(WebParams.SHOP_ID, shopId);
                            params.put(WebParams.MEMBER_ID, memberId);
                            params.put(WebParams.SHOP_STATUS, shopStatus);
                            params.put(WebParams.SHOP_REMARK, shopRemark);
                            if( !isClosed ) {
                                params.put(WebParams.SHOP_START_OPEN_HOUR, shopStartOpenHour);
                                params.put(WebParams.SHOP_END_OPEN_HOUR, shopEndOpenHour);
                            }
                            params.put(WebParams.SHOP_DATE, shopDate);

                            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.SENDER_ID + DefineValue.RECEIVER_ID + memberId + shopId + BuildConfig.AppID + shopStatus));

                            params.put(WebParams.SIGNATURE, signature);

                            MyApiClient.registerOpenCloseShop(getApplication(), params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    progdialog.dismiss();

                                    try {

                                        String code = response.getString(WebParams.ERROR_CODE);
                                        if (code.equals(WebParams.SUCCESS_CODE)) {

                                            //                                    Intent intent=new Intent(BbsRegisterOpenClosedShopActivity.this, .class);
                                            //                                    //intent.putExtra("PersonID", personDetailsModelArrayList.get(position).getId());
                                            //                                    startActivity(intent);
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
                                    //if (MyApiClient.PROD_FAILURE_FLAG)
                                    //Toast.makeText(getApplication(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    //else
                                    Toast.makeText(getApplication(), throwable.toString(), Toast.LENGTH_SHORT).show();

                                    progdialog.dismiss();
                                    Timber.w("Error Koneksi login:" + throwable.toString());

                                }

                            });
                        }
                    }

                }
        );
    }

    Button.OnClickListener btnShopDateListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            OpenCloseDatePickerFragment openCloseDatePickerFragment = new OpenCloseDatePickerFragment(BbsRegisterOpenClosedShopActivity.this, selectedDates, listDates);
            openCloseDatePickerFragment.show(getFragmentManager(), OpenCloseDatePickerFragment.TAG  );
        }
    };

    Button.OnClickListener btnOpenHourListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            OpenHourPickerFragment openHourPickerFragment = new OpenHourPickerFragment(BbsRegisterOpenClosedShopActivity.this);

            Bundle bundle = new Bundle();
            bundle.putString("startHour", shopStartOpenHour);
            bundle.putString("endHour", shopEndOpenHour);
            bundle.putInt("iStartHour", iStartHour );
            bundle.putInt("iStartMinute", iStartMinute );
            bundle.putInt("iEndHour", iEndHour );
            bundle.putInt("iEndMinute", iEndMinute );
            openHourPickerFragment.setArguments(bundle);

            openHourPickerFragment.show(getFragmentManager(), OpenHourPickerFragment.TAG  );
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_register_open_closed_shop;
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
        setActionBarTitle(getString(R.string.title_setup_shop_status));
    }

    @Override
    public void onOkDatePickerClick(ArrayList<String> selectedDates, ArrayList<Date> listDates) {
        this.selectedDates = selectedDates;

        ArrayList<String>   tempDates = new ArrayList<>();
        //Collections.sort(listDates);

        /*for(int i = 0; i < listDates.size(); i++) {
            String fDate = new SimpleDateFormat("yyyy-MM-dd").format(listDates.get(i));
            tempDates.add(fDate.toString());
        }*/

        ArrayList<String> tempStringDates = new ArrayList<>();

        for(int j = 0; j < listDates.size(); j++ ) {
            //String testonly  = listDates.get(j).toString();
            tempStringDates.add(DateTimeFormat.convertDatetoString(listDates.get(j), "dd MMMM yyyy"));

        }
        tvDate.setText(TextUtils.join(", ", tempStringDates));


    }

    @Override
    public void onCancelDatePickerClick() {

    }

    @Override
    public void onOkTimePickerClick(String startTime, String endTime, int iStartHour, int iStartMinute, int iEndHour, int iEndMinute) {
        shopStartOpenHour = startTime;
        shopEndOpenHour = endTime;
        this.iStartHour = iStartHour;
        this.iStartMinute = iStartMinute;
        this.iEndHour = iEndHour;
        this.iEndMinute = iEndMinute;

        tvStartHour.setText(shopStartOpenHour);
        tvEndHour.setText(shopEndOpenHour);
    }

    @Override
    public void onCancelTimePickerClick() {

    }
}
