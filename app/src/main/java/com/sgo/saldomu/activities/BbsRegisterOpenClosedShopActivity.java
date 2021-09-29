package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
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
import java.util.UUID;

public class BbsRegisterOpenClosedShopActivity extends BaseActivity implements OpenCloseDatePickerFragment.OpenCloseDatePickerListener,
        OpenHourPickerFragment.OpenHourPickerListener {

    ProgressDialog progdialog;
    ToggleButton tbOpenClosed;
    Button btnShopDate, btnProses, btnOpenHour;
    TextView tvDate, tvStartHour, tvEndHour, tvOpen24Hours;
    ArrayList<String> selectedDates = new ArrayList<>();
    ArrayList<Date> listDates = new ArrayList<>();
    String shopId, memberId, shopStatus, shopRemark, shopStartOpenHour, shopEndOpenHour;
    int iStartHour = 0, iStartMinute = 0, iEndHour = 0, iEndMinute = 0;
    LinearLayout llSetupHourForm, llSetupHourFormEnd, llHourForm;
    Boolean isClosed = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        tbOpenClosed = (ToggleButton) findViewById(R.id.tbOpenClosed);
        tbOpenClosed.setTextOn(getString(R.string.shop_open));
        tbOpenClosed.setTextOff(getString(R.string.shop_close));


        llSetupHourForm = (LinearLayout) findViewById(R.id.llSetupHourForm);
        llSetupHourFormEnd = (LinearLayout) findViewById(R.id.llSetupHourFormEnd);
        llHourForm = (LinearLayout) findViewById(R.id.llHourForm);
        tvOpen24Hours = (TextView) findViewById(R.id.tvOpen24Hours);
        tvOpen24Hours.setText(getString(R.string.set_shop_closing_date));

        llSetupHourForm.setVisibility(View.GONE);
        llSetupHourFormEnd.setVisibility(View.GONE);
        llHourForm.setVisibility(View.GONE);

        shopStatus = DefineValue.SHOP_CLOSE;
        shopRemark = "";
        shopStartOpenHour = "";
        shopEndOpenHour = "";

        memberId = getIntent().getStringExtra(DefineValue.MEMBER_ID);
        shopId = getIntent().getStringExtra(DefineValue.SHOP_ID);

        tvStartHour = (TextView) findViewById(R.id.tvStartHour);
        tvEndHour = (TextView) findViewById(R.id.tvEndHour);

        btnShopDate = (Button) findViewById(R.id.btnShopDate);
        btnShopDate.setOnClickListener(btnShopDateListener);

        btnOpenHour = (Button) findViewById(R.id.btnOpenHour);
        btnOpenHour.setOnClickListener(btnOpenHourListener);

        btnProses = (Button) findViewById(R.id.btnProses);
        tvDate = (TextView) findViewById(R.id.tvDate);

        tbOpenClosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llSetupHourForm.setVisibility(View.VISIBLE);
                    llSetupHourFormEnd.setVisibility(View.VISIBLE);
                    llHourForm.setVisibility(View.VISIBLE);
                    isClosed = false;
                    tvOpen24Hours.setText(getString(R.string.set_shop_opening_date));
                } else {
                    llSetupHourForm.setVisibility(View.GONE);
                    llSetupHourFormEnd.setVisibility(View.GONE);
                    llHourForm.setVisibility(View.GONE);
                    isClosed = true;
                    tvOpen24Hours.setText(getString(R.string.set_shop_closing_date));
                }
            }
        });

        btnProses.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {


                        String shopDate = new Gson().toJson(selectedDates);
                        Boolean hasError = false;

                        if (shopDate.equals("")) {
                            hasError = true;
                            Toast.makeText(getApplication(), R.string.err_empty_shop_date, Toast.LENGTH_SHORT).show();
                        }

                        if (!isClosed) {

                            if (shopStartOpenHour.equals("") || shopEndOpenHour.equals("")) {
                                hasError = true;
                                Toast.makeText(getApplication(), R.string.err_empty_shop_hour, Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (!hasError) {
                            progdialog = DefinedDialog.CreateProgressDialog(BbsRegisterOpenClosedShopActivity.this, "");
                            HashMap<String, Object> params = new HashMap<>();
                            UUID rcUUID = UUID.randomUUID();
                            String dtime = DateTimeFormat.getCurrentDateTime();


                            params.put(WebParams.RC_UUID, rcUUID);
                            params.put(WebParams.RC_DATETIME, dtime);
                            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                            params.put(WebParams.SHOP_ID, shopId);
                            params.put(WebParams.MEMBER_ID, memberId);
                            params.put(WebParams.SHOP_STATUS, shopStatus);
                            params.put(WebParams.SHOP_REMARK, shopRemark);
                            if (!isClosed) {
                                params.put(WebParams.SHOP_START_OPEN_HOUR, shopStartOpenHour);
                                params.put(WebParams.SHOP_END_OPEN_HOUR, shopEndOpenHour);
                            }
                            params.put(WebParams.SHOP_DATE, shopDate);

                            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId + shopId + BuildConfig.APP_ID + shopStatus));

                            params.put(WebParams.SIGNATURE, signature);

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
                                                } else {
                                                    Toast.makeText(getApplication(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
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

                                            progdialog.dismiss();
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
            OpenCloseDatePickerFragment openCloseDatePickerFragment = new OpenCloseDatePickerFragment();
            Bundle args = new Bundle();
            args.putSerializable("selectDates", selectedDates);
            args.putSerializable("listDates", listDates);
            openCloseDatePickerFragment.setArguments(args);
            openCloseDatePickerFragment.show(getFragmentManager(), OpenCloseDatePickerFragment.TAG);
        }
    };

    Button.OnClickListener btnOpenHourListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            OpenHourPickerFragment openHourPickerFragment = new OpenHourPickerFragment();

            Bundle bundle = new Bundle();
            bundle.putString("startHour", shopStartOpenHour);
            bundle.putString("endHour", shopEndOpenHour);
            bundle.putInt("iStartHour", iStartHour);
            bundle.putInt("iStartMinute", iStartMinute);
            bundle.putInt("iEndHour", iEndHour);
            bundle.putInt("iEndMinute", iEndMinute);
            openHourPickerFragment.setArguments(bundle);

            openHourPickerFragment.show(getFragmentManager(), OpenHourPickerFragment.TAG);
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
