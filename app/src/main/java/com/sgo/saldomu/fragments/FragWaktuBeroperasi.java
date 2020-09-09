package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.OpenHourDays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragWaktuBeroperasi extends Fragment implements TimePickerFragment.TimePickerListener,
        ClosedTypePickerFragment.ClosedTypePickerListener {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Waktu_Beroperasi";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View viewLayout;
    CheckBox chkBuka24Jam, chkSetiapHari;
    TableLayout tlLabelPilihHari, tlLabelPilihJam, tlLabelPilihSetiapHari, tlTutupSetiapTanggal;
    TableRow trSelectedDate, trSettingDate;
    ArrayList<OpenHourDays> setupOpenHours = new ArrayList<OpenHourDays>();
    ArrayList<String> selectedDates = new ArrayList<>();
    ArrayList<String> optDates = new ArrayList<>();
    Boolean isSetiapHari = false;
    TextView tvSelectedDate;
    Switch swTutupToko;
    Button btnTanggal, btnSubmit;
    String[] arrClosedType;
    ProgressDialog progdialog, progdialog2;
    SecurePreferences sp;
    LinearLayout llWaktuBeroperasi;
    String shopId = "", memberId = "";

    public FragWaktuBeroperasi() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragWaktuBeroperasi.
     */
    // TODO: Rename and change types and number of parameters
    public static FragWaktuBeroperasi newInstance(String param1, String param2) {
        FragWaktuBeroperasi fragment = new FragWaktuBeroperasi();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        arrClosedType = getContext().getResources().getStringArray(R.array.list_closed_type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewLayout = inflater.inflate(R.layout.frag_waktu_beroperasi, container, false);

        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();
        chkBuka24Jam            = viewLayout.findViewById(R.id.chkBuka24Jam);
        chkSetiapHari           = viewLayout.findViewById(R.id.chkSetiapHari);
        tlLabelPilihHari        = viewLayout.findViewById(R.id.tlLabelPilihHari);
        tlLabelPilihSetiapHari  = viewLayout.findViewById(R.id.tlLabelPilihSetiapHari);
        tlLabelPilihJam         = viewLayout.findViewById(R.id.tlLabelPilihJam);
        tlTutupSetiapTanggal    = viewLayout.findViewById(R.id.tlTutupSetiapTanggal);
        swTutupToko             = viewLayout.findViewById(R.id.swTutupToko);
        btnTanggal              = viewLayout.findViewById(R.id.btnTanggal);
        btnSubmit               = viewLayout.findViewById(R.id.btnSubmit);
        btnTanggal.setVisibility(View.GONE);

        tvSelectedDate          = viewLayout.findViewById(R.id.tvSelectedDate);
        trSelectedDate          = viewLayout.findViewById(R.id.trSelectedDate);
        trSettingDate           = viewLayout.findViewById(R.id.trSettingDate);
        llWaktuBeroperasi       = viewLayout.findViewById(R.id.llWaktuBeroperasi);
        llWaktuBeroperasi.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnTanggal.setVisibility(View.GONE);

        tlTutupSetiapTanggal.setVisibility(View.GONE);

        progdialog              = DefinedDialog.CreateProgressDialog(getContext(), "");

        String extraSignature = DefineValue.STRING_NO;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, DefineValue.STRING_NO);
        params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_LIST, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                llWaktuBeroperasi.setVisibility(View.VISIBLE);
                                btnSubmit.setVisibility(View.VISIBLE);

                                JSONArray members = response.getJSONArray("member");

                                if ( members.length() > 0 ) {
                                    memberId = members.getJSONObject(0).getString("member_id");
                                    shopId = members.getJSONObject(0).getString("shop_id");
                                } else {
                                    backToPreviousFragment();
                                }

                            } else {
                                backToPreviousFragment();
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

        /*etStartSenin            = (EditText) v.findViewById(R.id.etStartSenin);
        etEndSenin              = (EditText) v.findViewById(R.id.etEndSenin);
        etStartSelasa           = (EditText) v.findViewById(R.id.etStartSelasa);
        etEndSelasa             = (EditText) v.findViewById(R.id.etEndSelasa);

        etStartRabu             = (EditText) v.findViewById(R.id.etStartRabu);
        etEndRabu               = (EditText) v.findViewById(R.id.etEndRabu);
        etStartKamis            = (EditText) v.findViewById(R.id.etStartKamis);
        etEndKamis              = (EditText) v.findViewById(R.id.etEndKamis);

        etStartJumat            = (EditText) v.findViewById(R.id.etStartJumat);
        etEndJumat              = (EditText) v.findViewById(R.id.etEndJumat);
        etStartSabtu            = (EditText) v.findViewById(R.id.etStartSabtu);
        etEndSabtu              = (EditText) v.findViewById(R.id.etEndSabtu);
        etStartMinggu           = (EditText) v.findViewById(R.id.etStartMinggu);
        etEndMinggu             = (EditText) v.findViewById(R.id.etEndMinggu);*/

        setupOpenHours.add(new OpenHourDays("MON", "Senin", "00:00", "00:00", 1));
        setupOpenHours.add(new OpenHourDays("TUE", "Selasa", "00:00", "00:00",1));
        setupOpenHours.add(new OpenHourDays("WED", "Rabu", "00:00", "00:00", 1));
        setupOpenHours.add(new OpenHourDays("THU", "Kamis", "00:00", "00:00", 1));
        setupOpenHours.add(new OpenHourDays("FRI", "Jumat", "00:00", "00:00", 1));
        setupOpenHours.add(new OpenHourDays("SAT", "Sabtu", "00:00", "00:00", 1));
        setupOpenHours.add(new OpenHourDays("SUN", "Minggu", "00:00", "00:00", 1));

        showTableDays();

        btnTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();

            }
        });

        swTutupToko.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( isChecked ) {
                    btnTanggal.setVisibility(View.VISIBLE);
                    trSelectedDate.setVisibility(View.VISIBLE);
                } else {
                    btnTanggal.setVisibility(View.GONE);
                    trSelectedDate.setVisibility(View.GONE);
                }
            }
        });

        chkBuka24Jam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( isChecked ) {
                    tlLabelPilihHari.setVisibility(View.GONE);
                    tlLabelPilihSetiapHari.setVisibility(View.GONE);
                    tlLabelPilihJam.setVisibility(View.GONE);
                    tlTutupSetiapTanggal.setVisibility(View.GONE);
                    btnTanggal.setVisibility(View.GONE);

                } else {
                    tlLabelPilihHari.setVisibility(View.VISIBLE);
                    tlLabelPilihSetiapHari.setVisibility(View.VISIBLE);
                    tlLabelPilihJam.setVisibility(View.VISIBLE);
                    //tlTutupSetiapTanggal.setVisibility(View.VISIBLE);

                    if ( swTutupToko.isChecked() ) {
                        btnTanggal.setVisibility(View.VISIBLE);
                        trSelectedDate.setVisibility(View.VISIBLE);
                    } else {
                        btnTanggal.setVisibility(View.GONE);
                        trSelectedDate.setVisibility(View.GONE);
                    }
                }
            }
        });

        chkSetiapHari.setOnCheckedChangeListener(new mySetiapHariChangeClicker());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean hasError        = false;
                Boolean isDayFound      = false;
                String errorMessage     = "";

                if ( !chkBuka24Jam.isChecked() ) {
                    for (int pos = 0; pos < setupOpenHours.size(); pos++) {
                        int intStartText    = getId("etStart" + setupOpenHours.get(pos).getNamaHari(), R.id.class);
                        int intEndText      = getId("etEnd" + setupOpenHours.get(pos).getNamaHari(), R.id.class);
                        int chkHari         = getId("chk"+setupOpenHours.get(pos).getNamaHari(), R.id.class);

                        CheckBox checkBox   = viewLayout.findViewById(chkHari);
                        TextView txtStart   = viewLayout.findViewById(intStartText);
                        TextView txtEnd     = viewLayout.findViewById(intEndText);

                        if ( checkBox.isChecked() ) {
                            if ( !txtStart.getText().equals("00:00") && !txtEnd.getText().equals("00:00") ) {
                                isDayFound  = true;
                            } else {
                                hasError    = true;
                                isDayFound  = true;
                                errorMessage    = getString(R.string.err_select_time_of_day);
                            }
                        }

                    }
                } else {
                    isDayFound = true;
                }

                if ( !isDayFound ) {
                    hasError = true;
                    errorMessage    = getString(R.string.err_select_at_least_one_day);
                }

                if ( !hasError && swTutupToko.isChecked() ) {
                    if ( optDates.size() == 0 ) {
                        hasError = true;
                        errorMessage    = getString(R.string.err_select_at_least_one_date);
                    }
                }

                if ( !hasError ) {
                    //call web service
                    //to do
                    try{
                        progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");
                        progdialog2.show();

                        HashMap<String, Object> params = new HashMap<>();

                        UUID rcUUID             = UUID.randomUUID();
                        String  dtime           = DateTimeFormat.getCurrentDateTime();
                        String flagAllDay       = "";
                        ArrayList<String> tempDays = new ArrayList<>();

                        params.put(WebParams.RC_UUID, rcUUID);
                        params.put(WebParams.RC_DATETIME, dtime);
                        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                        params.put(WebParams.SHOP_ID, shopId);
                        params.put(WebParams.MEMBER_ID, memberId);

                        if ( chkBuka24Jam.isChecked() ) {
                            params.put(WebParams.FLAG_ALL_DAY, DefineValue.STRING_YES);
                            flagAllDay              = DefineValue.STRING_YES;
                        } else {
                            params.put(WebParams.FLAG_ALL_DAY, DefineValue.STRING_NO);
                            flagAllDay              = DefineValue.STRING_NO;

                            for(int pos = 0; pos < setupOpenHours.size(); pos++ ) {

                                int intStartText    = getId("etStart" + setupOpenHours.get(pos).getNamaHari(), R.id.class);
                                int intEndText      = getId("etEnd" + setupOpenHours.get(pos).getNamaHari(), R.id.class);
                                int chkHari         = getId("chk"+setupOpenHours.get(pos).getNamaHari(), R.id.class);

                                CheckBox checkBox   = viewLayout.findViewById(chkHari);
                                TextView txtStart   = viewLayout.findViewById(intStartText);
                                TextView txtEnd     = viewLayout.findViewById(intEndText);

                                if ( checkBox.isChecked() ) {
                                    if ( !txtStart.getText().equals("00:00") && !txtEnd.getText().equals("00:00") ) {
                                        switch(pos) {
                                            case 0:
                                                params.put(WebParams.OPEN_START_HOUR_MON, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_MON, txtEnd.getText() );
                                                break;
                                            case 1:
                                                params.put(WebParams.OPEN_START_HOUR_TUE, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_TUE, txtEnd.getText() );
                                                break;
                                            case 2:
                                                params.put(WebParams.OPEN_START_HOUR_WED, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_WED, txtEnd.getText() );
                                                break;
                                            case 3:
                                                params.put(WebParams.OPEN_START_HOUR_THU, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_THU, txtEnd.getText() );
                                                break;
                                            case 4:
                                                params.put(WebParams.OPEN_START_HOUR_FRI, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_FRI, txtEnd.getText() );
                                                break;
                                            case 5:
                                                params.put(WebParams.OPEN_START_HOUR_SAT, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_SAT, txtEnd.getText() );
                                                break;
                                            case 6:
                                                params.put(WebParams.OPEN_START_HOUR_SUN, txtStart.getText() );
                                                params.put(WebParams.OPEN_END_HOUR_SUN, txtEnd.getText() );
                                                break;
                                        }
                                    }
                                } else {
                                    tempDays.add(setupOpenHours.get(pos).getKodeHari());
                                }


                            }
                        }

                        Gson gson = new Gson();
                        ArrayList<String> tempData = new ArrayList<>();
                        if ( swTutupToko.isChecked() ) {




                            for(int x = 0; x < optDates.size(); x++) {
                                String idx = String.valueOf(optDates.get(x));
                                tempData.add(idx);
                            }
                            params.put(WebParams.CLOSED_VALUE, gson.toJson(tempData));
                            params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_DATE);

                        } else {

                            if ( flagAllDay.equals(DefineValue.STRING_NO) ) {
                                if ( tempDays.size() > 0 ) {
                                    params.put(WebParams.CLOSED_VALUE, gson.toJson(tempDays));
                                    params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_DAY);
                                } else {
                                    params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_NONE);
                                }
                            } else {
                                params.put(WebParams.FLAG_CLOSED_TYPE, DefineValue.CLOSED_TYPE_NONE);
                            }

                        }

                        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId.toUpperCase() + shopId.toUpperCase() + BuildConfig.APP_ID));

                        params.put(WebParams.SIGNATURE, signature);

                        Log.d("TEST", params.toString());

                        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SETUP_OPENING_HOUR, params,
                                new ObjListeners() {
                                    @Override
                                    public void onResponses(JSONObject response) {

                                        Timber.d("isi response sent insert open hour:" + response.toString());

                                        try {
                                            String code = response.getString(WebParams.ERROR_CODE);
                                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                                androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
                                                alertDialog.setCanceledOnTouchOutside(false);
                                                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                                alertDialog.setCancelable(false);

                                                alertDialog.setMessage(getString(R.string.message_notif_update_24_hours_success));



                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                SecurePreferences.Editor mEditor = sp.edit();
                                                                mEditor.putString(DefineValue.IS_AGENT_SET_LOCATION, DefineValue.STRING_YES);
                                                                mEditor.putString(DefineValue.SHOP_AGENT_DATA, "");
                                                                mEditor.apply();
                                                    /*FragTutupManual fragTutupManual = new FragTutupManual();
                                                    FragmentManager fragmentManager = getFragmentManager();
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    fragmentTransaction.replace(R.id.bbs_content, fragTutupManual, null);

                                                    if ( getActivity() != null ) {
                                                        BBSActivity bbc = (BBSActivity) getActivity();

                                                        TextView title_detoolbar = (TextView) getActivity().findViewById(R.id.main_toolbar_title);
                                                        title_detoolbar.setText(getString(R.string.menu_item_title_tutup_manual));
                                                    }
                                                    fragmentTransaction.commit();*/
                                                                //getActivity().onBackPressed();
                                                                //getActivity().finish();
                                                                Intent i = new Intent(getActivity().getApplicationContext(), MainPage.class);
                                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(i);
                                                                getActivity().finish();

                                                            }
                                                        });

                                                alertDialog.show();

                                            }
                                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                                //test.showDialoginActivity(getApplication(),message);
                                            }
                                            else {
                                                code = response.getString(WebParams.ERROR_MESSAGE);
                                                Toast.makeText(getContext(), code, Toast.LENGTH_LONG).show();
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
                                        if (progdialog2.isShowing())
                                            progdialog2.dismiss();
                                    }
                                });

                    }catch (Exception e){
                        Timber.d("httpclient:"+e.getMessage());
                    }

                } else {
                    androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setTitle(getString(R.string.alertbox_title_information));
                    alertDialog.setCancelable(false);

                    alertDialog.setMessage(errorMessage);



                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                }
                            });

                    alertDialog.show();
                }

            }
        });


        return viewLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showTableDays() {
        for(int pos = 0; pos < setupOpenHours.size(); pos++) {
            int etStart = getId("etStart"+setupOpenHours.get(pos).getNamaHari(), R.id.class);
            int etEnd   = getId("etEnd"+setupOpenHours.get(pos).getNamaHari(), R.id.class);
            int chkHari = getId("chk"+setupOpenHours.get(pos).getNamaHari(), R.id.class);

            TextView etText = viewLayout.findViewById(etStart);
            etText.setText(setupOpenHours.get(pos).getStartHour());
            etText.setEnabled(true);
            etText.setOnTouchListener(new myEditTextClickListener());

            TextView etEndText = viewLayout.findViewById(etEnd);
            etEndText.setText(setupOpenHours.get(pos).getEndHour());
            etEndText.setEnabled(true);
            etEndText.setOnTouchListener(new myEditTextClickListener());

            CheckBox chkBoxHari = viewLayout.findViewById(chkHari);
            chkBoxHari.setOnCheckedChangeListener(null);
            /*if ( setupOpenHours.get(pos).getStartHour().equals("00:00") &&
                    setupOpenHours.get(pos).getEndHour().equals("00:00") ) {
                chkBoxHari.setChecked(false);
            } else {
                chkBoxHari.setChecked(true);
            }*/
            chkBoxHari.setOnCheckedChangeListener(new myCheckBoxChangeClicker());
        }
    }

    @Override
    public void onOkTimePickerClick(int position, String startTime, String endTime, int iStartHour, int iStartMinute, int iEndHour, int iEndMinute) {
        setupOpenHours.get(position).setStartHour(startTime);
        setupOpenHours.get(position).setEndHour(endTime);
        setupOpenHours.get(position).setiStartHour(iStartHour);
        setupOpenHours.get(position).setiStartMinute(iStartMinute);
        setupOpenHours.get(position).setiEndHour(iEndHour);
        setupOpenHours.get(position).setiEndMinute(iEndMinute);

        showTableDays();
    }

    @Override
    public void onCancelTimePickerClick(int position) {
        setupOpenHours.get(position).setStartHour("00:00");
        setupOpenHours.get(position).setEndHour("00:00");
        setupOpenHours.get(position).setiStartHour(0);
        setupOpenHours.get(position).setiStartMinute(0);
        setupOpenHours.get(position).setiEndHour(0);
        setupOpenHours.get(position).setiEndMinute(0);

        int chkHari = getId("chk"+setupOpenHours.get(position).getNamaHari(), R.id.class);
        CheckBox chkBoxHari = viewLayout.findViewById(chkHari);
        chkBoxHari.setOnCheckedChangeListener(null);
        chkBoxHari.setChecked(false);

        int etStart = getId("etStart"+setupOpenHours.get(position).getNamaHari(), R.id.class);
        int etEnd   = getId("etEnd"+setupOpenHours.get(position).getNamaHari(), R.id.class);

        TextView etText = viewLayout.findViewById(etStart);
        etText.setText(setupOpenHours.get(position).getStartHour());
        etText.setEnabled(true);
        etText.setOnTouchListener(new myEditTextClickListener());

        TextView etEndText = viewLayout.findViewById(etEnd);
        etEndText.setText(setupOpenHours.get(position).getEndHour());
        etEndText.setEnabled(true);
        etEndText.setOnTouchListener(new myEditTextClickListener());

        chkBoxHari.setOnCheckedChangeListener(new myCheckBoxChangeClicker());

        chkSetiapHari.setOnCheckedChangeListener(null);
        chkSetiapHari.setChecked(false);
        chkSetiapHari.setOnCheckedChangeListener(new mySetiapHariChangeClicker());
        //showTableDays();


    }

    @Override
    public void onOkClosedTypePickerClick(int position, ArrayList<String> selectedDays, ArrayList<String> selectedDate) {
        this.selectedDates = selectedDate;

        optDates = new ArrayList<>();
        for(int x = 0; x < selectedDate.size(); x++) {
            String idx = String.valueOf(Integer.valueOf(selectedDate.get(x)) + 1);
            optDates.add(idx);
        }

        Collections.sort(optDates);
        tvSelectedDate.setText(TextUtils.join(", ", optDates));
    }

    @Override
    public void onCancelClosedTypePickerClick(int position) {

    }


    class mySetiapHariChangeClicker implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            for (int pos = 0; pos < setupOpenHours.size(); pos++) {
                CheckBox chkBox = viewLayout.findViewById(getId("chk" + setupOpenHours.get(pos).getNamaHari(), R.id.class));
                chkBox.setOnCheckedChangeListener(null);
                chkBox.setChecked(isChecked);
                chkBox.setOnCheckedChangeListener(new myCheckBoxChangeClicker());
            }


        }
    }

    class myEditTextClickListener implements EditText.OnTouchListener {


        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int selectedPosition = 0;
            for (int pos = 0; pos < setupOpenHours.size(); pos++) {
                int intStartText    = getId("etStart" + setupOpenHours.get(pos).getNamaHari(), R.id.class);
                int intEndText      = getId("etEnd" + setupOpenHours.get(pos).getNamaHari(), R.id.class);

                if ( v.getId() == intStartText ) {
                    selectedPosition = pos;
                    break;
                } else if ( v.getId() == intEndText ) {
                    selectedPosition = pos;
                    break;
                }
            }

            CheckBox chkBox = viewLayout.findViewById(getId("chk" + setupOpenHours.get(selectedPosition).getNamaHari(), R.id.class));

            if ( chkBox.isChecked() ) {
                showTimeDialog(selectedPosition);
            } else {
                androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                alertDialog.setCancelable(false);

                alertDialog.setMessage(getString(R.string.err_select_day_first));



                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });

                alertDialog.show();
            }

            return false;
        }
    }

    class myCheckBoxChangeClicker implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                isSetiapHari = false;
            } else {
                isSetiapHari = true;
                for (int pos = 0; pos < setupOpenHours.size(); pos++) {
                    CheckBox chkBox = viewLayout.findViewById(getId("chk" + setupOpenHours.get(pos).getNamaHari(), R.id.class));
                    if (!chkBox.isChecked()) {
                        isSetiapHari = false;
                    }
                }
            }

            chkSetiapHari.setOnCheckedChangeListener(null);
            if (isSetiapHari) {
                chkSetiapHari.setChecked(true);
            } else {
                chkSetiapHari.setChecked(false);
            }

            if ( isChecked ) {
                for (int pos = 0; pos < setupOpenHours.size(); pos++) {
                    int chkHariId = getId("chk" + setupOpenHours.get(pos).getNamaHari(), R.id.class);

                    if (chkHariId == buttonView.getId()) {
                        showTimeDialog(pos);
                    }
                }
            } else {
                for (int pos = 0; pos < setupOpenHours.size(); pos++) {
                    int chkHariId = getId("chk" + setupOpenHours.get(pos).getNamaHari(), R.id.class);

                    CheckBox chkBox2 = viewLayout.findViewById(chkHariId);
                    if ( !chkBox2.isChecked() ) {
                        setupOpenHours.get(pos).setStartHour("00:00");
                        setupOpenHours.get(pos).setEndHour("00:00");
                        setupOpenHours.get(pos).setiStartHour(0);
                        setupOpenHours.get(pos).setiStartMinute(0);
                        setupOpenHours.get(pos).setiEndHour(0);
                        setupOpenHours.get(pos).setiEndMinute(0);

                        int etStart = getId("etStart"+setupOpenHours.get(pos).getNamaHari(), R.id.class);
                        int etEnd   = getId("etEnd"+setupOpenHours.get(pos).getNamaHari(), R.id.class);

                        TextView etText = viewLayout.findViewById(etStart);
                        etText.setText(setupOpenHours.get(pos).getStartHour());
                        etText.setEnabled(true);
                        etText.setOnTouchListener(new myEditTextClickListener());

                        TextView etEndText = viewLayout.findViewById(etEnd);
                        etEndText.setText(setupOpenHours.get(pos).getEndHour());
                        etEndText.setEnabled(true);
                        etEndText.setOnTouchListener(new myEditTextClickListener());
                    }
                }
            }

            chkSetiapHari.setOnCheckedChangeListener(new mySetiapHariChangeClicker());

        }
    }

    public static int getId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            throw new RuntimeException("No resource ID found for: "
                    + resourceName + " / " + c, e);
        }
    }

    public void showDateDialog() {
        ClosedTypePickerFragment closedTypePickerFragment = new ClosedTypePickerFragment();
        int typePosition = 2;



        Bundle bundle = new Bundle();
        bundle.putInt("position", typePosition);
        bundle.putString("closedType", arrClosedType[typePosition]);
        bundle.putStringArrayList("selectedDate", selectedDates);
        bundle.putStringArrayList("selectedDays", new ArrayList<String>());

        closedTypePickerFragment.setArguments(bundle);
        closedTypePickerFragment.setCancelable(false);
        closedTypePickerFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        closedTypePickerFragment.setTargetFragment(this,0);
        closedTypePickerFragment.show(getActivity().getSupportFragmentManager(), ClosedTypePickerFragment.TAG);
    }

    public void showTimeDialog(int pos) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("position", pos);
        bundle.putString("NamaHari", setupOpenHours.get(pos).getNamaHari());
        bundle.putString("startHour", setupOpenHours.get(pos).getStartHour());
        bundle.putString("endHour", setupOpenHours.get(pos).getEndHour());
        bundle.putInt("iStartHour", setupOpenHours.get(pos).getiStartHour() );
        bundle.putInt("iStartMinute", setupOpenHours.get(pos).getiStartMinute() );
        bundle.putInt("iEndHour", setupOpenHours.get(pos).getiEndHour() );
        bundle.putInt("iEndMinute", setupOpenHours.get(pos).getiEndMinute() );

        timePickerFragment.setArguments(bundle);
        timePickerFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        timePickerFragment.setTargetFragment(this,0);
        timePickerFragment.setCancelable(false);
        timePickerFragment.show(getActivity().getSupportFragmentManager(),TimePickerFragment.TAG  );
    }

    private void backToPreviousFragment() {
        //redirect back to fragment - BBSActivity;
        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle(getString(R.string.alertbox_title_information));
        alertDialog.setCancelable(false);

        alertDialog.setMessage(getString(R.string.message_notif_not_registered_agent));



        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        getActivity().finish();

                    }
                });

        alertDialog.show();
    }

}
