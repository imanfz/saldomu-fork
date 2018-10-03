package com.sgo.saldomu.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.GridViewAdapter;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fragments.ClosedTypePickerFragment;
import com.sgo.saldomu.fragments.TimePickerFragment;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.OpenHourDays;
import com.sgo.saldomu.models.SetupOpenHour;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import timber.log.Timber;

public class BbsSetupOpenHourActivity extends BaseActivity implements TimePickerFragment.TimePickerListener,
        AdapterView.OnItemSelectedListener, ClosedTypePickerFragment.ClosedTypePickerListener {
    String memberId, shopId;
    public SetupOpenHour setupOpenHour;
    ToggleButton tbOpen24Hours, tbTutupToko;
    Switch swOpen24Hours, swTutupToko;
    LinearLayout llSettingTutupToko, llSetupHours, llSetupClosedType, llTutupToko, llSetupOpeningHour, tvSetupShopDate;
    GridView gridview;
    GridViewAdapter customAdapter;
    Spinner spClosedType;
    TextView tvSelectedInfo;
    ArrayList<String> selectedDate, selectedDays;
    int selectedPos = 0;
    Button btnProses;
    ProgressDialog progdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //memberId = "003SGO";
        //shopId      = "003";

        progdialog          = new ProgressDialog(BbsSetupOpenHourActivity.this);
        memberId        = getIntent().getStringExtra("memberId");
        shopId          = getIntent().getStringExtra("shopId");
        setupOpenHour   = new SetupOpenHour();

        initializeToolbar();

        gridview            = (GridView) findViewById(R.id.simpleGridView);
        llSettingTutupToko  = (LinearLayout) findViewById(R.id.llSettingTutupToko);
        llSetupHours        = (LinearLayout) findViewById(R.id.llSetupHours);
        llSetupOpeningHour  = (LinearLayout) findViewById(R.id.llSetupOpeningHour);

        //llTutupToko         = (LinearLayout) findViewById(R.id.llTutupToko);

        swOpen24Hours       = (Switch) findViewById(R.id.swOpen24Hours);
        btnProses           = (Button) findViewById(R.id.btnProses);
        swOpen24Hours.setChecked(true);
        selectedDate        = new ArrayList<>();
        selectedDays        = new ArrayList<>();

        swTutupToko         = (Switch) findViewById(R.id.swTutupToko);
        llSetupClosedType   = (LinearLayout) findViewById(R.id.llSetupClosedType);
        tvSelectedInfo      = (TextView) findViewById(R.id.tvSelectedInfo);

        spClosedType        = (Spinner) findViewById(R.id.spClosedType);
        spClosedType.setOnItemSelectedListener(this);

        llSetupHours.setVisibility(View.GONE);
        llSetupOpeningHour.setVisibility(View.GONE);
        llSetupClosedType.setVisibility(View.GONE);
        //llTutupToko.setVisibility(View.GONE);
        llSettingTutupToko.setVisibility(View.GONE);

        swOpen24Hours.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llSettingTutupToko.setVisibility(View.GONE);
                    llSetupHours.setVisibility(View.GONE);
                    llSetupOpeningHour.setVisibility(View.GONE);
                    //llTutupToko.setVisibility(View.GONE);
                    llSetupClosedType.setVisibility(View.GONE);
                } else {
                    llSettingTutupToko.setVisibility(View.VISIBLE);
                    llSetupHours.setVisibility(View.VISIBLE);
                    llSetupOpeningHour.setVisibility(View.VISIBLE);
                    //llTutupToko.setVisibility(View.VISIBLE);

                    if ( swTutupToko.isChecked() ) {
                        llSetupClosedType.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        swTutupToko.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llSetupClosedType.setVisibility(View.VISIBLE);

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(BbsSetupOpenHourActivity.this,
                            R.array.list_closed_type, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spClosedType.setAdapter(adapter);

                } else {
                    llSetupClosedType.setVisibility(View.GONE);
                }
            }
        });

        customAdapter = new GridViewAdapter(getApplicationContext(), setupOpenHour.getSetupOpenHours());
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TimePickerFragment timePickerFragment = new TimePickerFragment();

                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("NamaHari", setupOpenHour.getSetupOpenHours().get(position).getNamaHari());
                bundle.putString("startHour", setupOpenHour.getSetupOpenHours().get(position).getStartHour());
                bundle.putString("endHour", setupOpenHour.getSetupOpenHours().get(position).getEndHour());
                bundle.putInt("iStartHour", setupOpenHour.getSetupOpenHours().get(position).getiStartHour() );
                bundle.putInt("iStartMinute", setupOpenHour.getSetupOpenHours().get(position).getiStartMinute() );
                bundle.putInt("iEndHour", setupOpenHour.getSetupOpenHours().get(position).getiEndHour() );
                bundle.putInt("iEndMinute", setupOpenHour.getSetupOpenHours().get(position).getiEndMinute() );

                timePickerFragment.setArguments(bundle);
                timePickerFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                timePickerFragment.show(getSupportFragmentManager(),TimePickerFragment.TAG  );


            }
        });

        btnProses.setOnClickListener(btnProsesListener);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_setup_open_hour;
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
        setActionBarTitle(getString(R.string.title_setup_shop_hour));
    }

    @Override
    public void onOkTimePickerClick(int selectedPosition, String startTime, String endTime, int iStartHour, int iStartMinute, int iEndHour, int iEndMinute) {
        setupOpenHour.updateOpenHourDaysByKode(selectedPosition, startTime, endTime, iStartHour, iStartMinute, iEndHour, iEndMinute);
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelTimePickerClick(int selectedPosition) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        if ( pos > 0 ) {
            ClosedTypePickerFragment closedTypePickerFragment = new ClosedTypePickerFragment();

            String[] arrClosedType = getApplicationContext().getResources().getStringArray(R.array.list_closed_type);

            Bundle bundle = new Bundle();
            bundle.putInt("position", pos);
            bundle.putString("closedType", arrClosedType[pos]);
            bundle.putStringArrayList("selectedDate", this.selectedDate);
            bundle.putStringArrayList("selectedDays", this.selectedDays);

            closedTypePickerFragment.setArguments(bundle);
            closedTypePickerFragment.show(getSupportFragmentManager(), ClosedTypePickerFragment.TAG);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public void onOkClosedTypePickerClick(int position, ArrayList<String> selectedDays, ArrayList<String> selectedDate) {
        this.selectedDays = selectedDays;
        this.selectedDate = selectedDate;
        if ( position == 1 ) {

            selectedPos = position;

            ArrayList<String> tempData = new ArrayList<>();
            for(int x = 0; x < selectedDays.size(); x++) {
                Integer idx = Integer.valueOf(selectedDays.get(x)) + 1;

                tempData.add(setupOpenHour.getSetupOpenHours().get(idx).getNamaHari());
            }
            tvSelectedInfo.setText("Hari : "+ android.text.TextUtils.join(", ", tempData));


        } else if ( position == 2 ) {
            selectedPos = position;

            ArrayList<String> tempData = new ArrayList<>();
            for(int x = 0; x < selectedDate.size(); x++) {
                String idx = String.valueOf(Integer.valueOf(selectedDate.get(x)) + 1);
                tempData.add(idx);
            }
            tvSelectedInfo.setText("Tanggal : "+ android.text.TextUtils.join(", ", tempData));
        }
    }

    @Override
    public void onCancelClosedTypePickerClick(int position) {
        selectedPos = position;
    }

    Button.OnClickListener btnProsesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getApplicationContext())) {
                Boolean hasError    = false;
                String errorMessage = "";

                if ( !swOpen24Hours.isChecked() ) {
                    for(int j = 0; j < setupOpenHour.getSetupOpenHours().size(); j++)
                    {
                        if ( j > 0 ){
                            if ( setupOpenHour.getSetupOpenHours().get(j).getStartHour().equals("")
                                    || setupOpenHour.getSetupOpenHours().get(j).getEndHour().equals("") ) {
                                hasError = true;

                            }
                        }

                    }

                    if ( hasError ) {
                        errorMessage = getString(R.string.err_empty_hour);
                    }
                }

                if ( !hasError && swTutupToko.isChecked() ) {
                    if ( selectedPos == 1 ) {
                        if ( selectedDays.size() == 0 ) {
                            hasError = true;
                            errorMessage = getString(R.string.err_empty_closed_days);
                        }
                    } else if ( selectedPos == 2 ){
                        if ( selectedDate.size() == 0 ) {
                            hasError = true;
                            errorMessage = getString(R.string.err_empty_closed_dates);
                        }
                    }
                }

                if (!hasError) {
                    try{
                        progdialog = DefinedDialog.CreateProgressDialog(BbsSetupOpenHourActivity.this, "");
                        progdialog.show();

                        HashMap<String, Object> params = new HashMap<>();

                        UUID rcUUID             = UUID.randomUUID();
                        String  dtime           = DateTimeFormat.getCurrentDateTime();

                        params.put(WebParams.RC_UUID, rcUUID);
                        params.put(WebParams.RC_DATETIME, dtime);
                        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                        params.put(WebParams.SHOP_ID, shopId);
                        params.put(WebParams.MEMBER_ID, memberId);

                        if ( swOpen24Hours.isChecked() ) {
                            params.put(WebParams.FLAG_ALL_DAY, DefineValue.STRING_YES);
                        } else {
                            params.put(WebParams.FLAG_ALL_DAY, DefineValue.STRING_NO);

                            for(int j =0; j < setupOpenHour.getSetupOpenHours().size(); j++ ) {
                                if ( j > 0 ) {
                                    OpenHourDays ohd = setupOpenHour.getSetupOpenHours().get(j);
                                    switch(j) {
                                        case 1:
                                            params.put(WebParams.OPEN_START_HOUR_SUN, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_SUN, ohd.getEndHour());
                                            break;
                                        case 2:
                                            params.put(WebParams.OPEN_START_HOUR_MON, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_MON, ohd.getEndHour());
                                            break;
                                        case 3:
                                            params.put(WebParams.OPEN_START_HOUR_TUE, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_TUE, ohd.getEndHour());
                                            break;
                                        case 4:
                                            params.put(WebParams.OPEN_START_HOUR_WED, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_WED, ohd.getEndHour());
                                            break;
                                        case 5:
                                            params.put(WebParams.OPEN_START_HOUR_THU, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_THU, ohd.getEndHour());
                                            break;
                                        case 6:
                                            params.put(WebParams.OPEN_START_HOUR_FRI, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_FRI, ohd.getEndHour());
                                            break;
                                        case 7:
                                            params.put(WebParams.OPEN_START_HOUR_SAT, ohd.getStartHour());
                                            params.put(WebParams.OPEN_END_HOUR_SAT, ohd.getEndHour());
                                            break;
                                    }

                                }
                            }
                        }

                        Gson gson = new Gson();
                        ArrayList<String> tempData = new ArrayList<>();
                        if ( swTutupToko.isChecked() ) {


                            if ( selectedPos == 0 ) {


                                for(int x = 0; x < selectedDays.size(); x++) {
                                    Integer idx = Integer.valueOf(selectedDays.get(x)) + 1;

                                    tempData.add(setupOpenHour.getSetupOpenHours().get(idx).getKodeHari());
                                }
                                params.put(WebParams.CLOSED_VALUE, gson.toJson(tempData));
                                params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_DAY);
                            } else {

                                for(int x = 0; x < selectedDate.size(); x++) {
                                    String idx = String.valueOf(Integer.valueOf(selectedDate.get(x)) + 1);
                                    tempData.add(idx);
                                }
                                params.put(WebParams.CLOSED_VALUE, gson.toJson(tempData));
                                params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_DATE);
                            }
                        } else {
                            params.put(WebParams.CLOSED_VALUE, gson.toJson(tempData));
                            params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_NONE);
                        }

                        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId.toUpperCase() + shopId.toUpperCase() + BuildConfig.APP_ID));

                        params.put(WebParams.SIGNATURE, signature);

                        Log.d("TEST", params.toString());

                        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SETUP_OPENING_HOUR, params,
                                new ObjListeners() {
                                    @Override
                                    public void onResponses(JSONObject response) {
                                        try {
                                            String code = response.getString(WebParams.ERROR_CODE);
                                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                                Intent intent=new Intent(getApplicationContext(),BbsMerchantCommunityList.class);
                                                startActivity(intent);
                                            }
                                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                                //test.showDialoginActivity(getApplication(),message);
                                            }
                                            else {
                                                code = response.getString(WebParams.ERROR_MESSAGE);
                                                Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
                                        if (progdialog.isShowing())
                                            progdialog.dismiss();
                                    }
                                });
                    }catch (Exception e){
                        Timber.d("httpclient:"+e.getMessage());
                    }
                } else {
                    //DefinedDialog.showErrorDialog(getApplicationContext(), errorMessage);
                    Toast.makeText(BbsSetupOpenHourActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }



            }
            else DefinedDialog.showErrorDialog(getApplicationContext(), getString(R.string.inethandler_dialog_message));
        }
    };

}
