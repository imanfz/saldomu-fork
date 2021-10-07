package com.sgo.saldomu.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fragments.OpenCloseDatePickerFragment;
import com.sgo.saldomu.fragments.OpenHourPickerFragment;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BbsSetupShopClosedActivity extends BaseActivity implements OpenCloseDatePickerFragment.OpenCloseDatePickerListener,
        OpenHourPickerFragment.OpenHourPickerListener {

    ProgressDialog progDialog;
    Button btnShopDate, btnProses;
    TextView tvDate, tvStartHour, tvEndHour;
    ArrayList<String> selectedDates = new ArrayList<>();
    ArrayList<Date> listDates = new ArrayList<>();
    String shopId, memberId, shopStatus, shopRemark, shopStartOpenHour, shopEndOpenHour, flagApprove;
    int iStartHour = 0, iStartMinute = 0, iEndHour = 0, iEndMinute = 0, selectedType = 0;
    LinearLayout llSetupShopDate, tvSetupShopDate, llShopRemark;
    EditText etShopRemark;

    Spinner spPilihan;
    ArrayAdapter<String> SpinnerAdapter;
    String[] arrayItems = new String[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        spPilihan = findViewById(R.id.spPilihan);
        arrayItems[0] = "Silakan Pilih";
        arrayItems[1] = getString(R.string.yes);
        arrayItems[2] = getString(R.string.no);

        SpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arrayItems);
        spPilihan.setAdapter(SpinnerAdapter);

        spPilihan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                if (arg2 == 1) {
                    llSetupShopDate.setVisibility(View.GONE);
                    llShopRemark.setVisibility(View.GONE);
                    tvSetupShopDate.setVisibility(View.GONE);
                    selectedType = arg2;
                } else if (arg2 == 2) {
                    llSetupShopDate.setVisibility(View.VISIBLE);
                    llShopRemark.setVisibility(View.VISIBLE);
                    tvSetupShopDate.setVisibility(View.VISIBLE);
                    selectedType = arg2;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        etShopRemark = findViewById(R.id.etShopRemark);
        llSetupShopDate = findViewById(R.id.llSetupShopDate);
        llShopRemark = findViewById(R.id.llShopRemark);
        tvSetupShopDate = findViewById(R.id.tvSetupShopDate);

        llSetupShopDate.setVisibility(View.GONE);
        tvSetupShopDate.setVisibility(View.GONE);
        llShopRemark.setVisibility(View.GONE);

        shopStatus = DefineValue.SHOP_CLOSE;
        shopRemark = "";
        shopStartOpenHour = "";
        shopEndOpenHour = "";

        memberId = getIntent().getStringExtra(DefineValue.MEMBER_ID);
        shopId = getIntent().getStringExtra(DefineValue.SHOP_ID);
        flagApprove = getIntent().getStringExtra(DefineValue.FLAG_APPROVE);

        tvStartHour = findViewById(R.id.tvStartHour);
        tvEndHour = findViewById(R.id.tvEndHour);

        btnShopDate = findViewById(R.id.btnShopDate);
        btnShopDate.setOnClickListener(btnShopDateListener);

        btnProses = findViewById(R.id.btnProses);
        tvDate = findViewById(R.id.tvDate);


        btnProses.setOnClickListener(
                v -> {


                    String shopDate = new Gson().toJson(selectedDates);
                    Boolean hasError = false;

                    if (selectedType == 2) {
                        if (shopDate.equals("")) {
                            hasError = true;
                            Toast.makeText(getApplication(), R.string.err_empty_shop_date, Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (!hasError) {

                        shopRemark = etShopRemark.getText().toString();
                        progDialog = DefinedDialog.CreateProgressDialog(BbsSetupShopClosedActivity.this, "");

                        if (selectedType == 1) {

                            String extraSignature = memberId + shopId;
                            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, extraSignature);

                            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                            params.put(WebParams.SHOP_ID, shopId);
                            params.put(WebParams.MEMBER_ID, memberId);
                            params.put(WebParams.USER_ID, userPhoneID);

                            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, params,
                                    new ObjListeners() {
                                        @Override
                                        public void onResponses(JSONObject response) {
                                            try {

                                                String code = response.getString(WebParams.ERROR_CODE);
                                                if (code.equals(WebParams.SUCCESS_CODE)) {


                                                    Intent intent = new Intent(getApplicationContext(), BbsMemberShopActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    intent.putExtra(DefineValue.MEMBER_ID, memberId);
                                                    intent.putExtra(DefineValue.SHOP_ID, shopId);
                                                    intent.putExtra(DefineValue.FLAG_APPROVE, flagApprove);
                                                    startActivity(intent);
                                                    finish();

                                                } else if (code.equals(WebParams.LOGOUT_CODE)) {

                                                } else {
                                                    //Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                                                    AlertDialog alertDialog = new AlertDialog.Builder(BbsSetupShopClosedActivity.this).create();
                                                    alertDialog.setTitle(getString(R.string.alertbox_title_information));

                                                    alertDialog.setMessage(response.getString(WebParams.ERROR_MESSAGE));
                                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                                            (dialog, which) -> dialog.dismiss());
                                                    alertDialog.show();

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
                                            progDialog.dismiss();
                                        }
                                    });
                        } else if (selectedType == 2) {

                            String extraSignature = memberId + shopId + shopStatus;
                            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_OPEN_CLOSE_TOKO,
                                    extraSignature);

                            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                            params.put(WebParams.SHOP_ID, shopId);
                            params.put(WebParams.MEMBER_ID, memberId);
                            params.put(WebParams.USER_ID, userPhoneID);

                            params.put(WebParams.SHOP_STATUS, shopStatus);
                            params.put(WebParams.SHOP_REMARK, shopRemark);
                                /*if (!isClosed) {
                                    params.put(WebParams.SHOP_START_OPEN_HOUR, shopStartOpenHour);
                                    params.put(WebParams.SHOP_END_OPEN_HOUR, shopEndOpenHour);
                                }*/
                            params.put(WebParams.SHOP_DATE, shopDate);

                            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_OPEN_CLOSE_TOKO, params,
                                    new ObjListeners() {
                                        @Override
                                        public void onResponses(JSONObject response) {
                                            try {

                                                String code = response.getString(WebParams.ERROR_CODE);
                                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                                    //                                    Intent intent=new Intent(BbsRegisterOpenClosedShopActivity.this, .class);
                                                    //                                    //intent.putExtra("PersonID", personDetailsModelArrayList.get(position).getId());
                                                    //                                    startActivity(intent);

                                                    Intent intent = new Intent(getApplicationContext(), BbsMemberShopActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    intent.putExtra(DefineValue.MEMBER_ID, memberId);
                                                    intent.putExtra(DefineValue.SHOP_ID, shopId);
                                                    intent.putExtra(DefineValue.FLAG_APPROVE, flagApprove);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    //Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                                                    AlertDialog alertDialog = new AlertDialog.Builder(BbsSetupShopClosedActivity.this).create();
                                                    alertDialog.setTitle(getString(R.string.alertbox_title_information));

                                                    alertDialog.setMessage(response.getString(WebParams.ERROR_MESSAGE));
                                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                                            (dialog, which) -> dialog.dismiss());
                                                    alertDialog.show();

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
                                            progDialog.dismiss();
                                        }
                                    });
                        }


                    }

                }
        );
    }

    Button.OnClickListener btnShopDateListener = v -> {
        OpenCloseDatePickerFragment openCloseDatePickerFragment = new OpenCloseDatePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable("selectDates", selectedDates);
        args.putSerializable("listDates", listDates);
        openCloseDatePickerFragment.setArguments(args);
        openCloseDatePickerFragment.show(getFragmentManager(), OpenCloseDatePickerFragment.TAG);
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_setup_shop_closed;
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

        ArrayList<String> tempStringDates = new ArrayList<>();

        for (int j = 0; j < listDates.size(); j++) {
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
