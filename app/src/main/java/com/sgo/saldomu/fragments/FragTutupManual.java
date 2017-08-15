package com.sgo.saldomu.fragments;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragTutupManual#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragTutupManual extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Tutup_Manual";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String DATEFROM = "tagFrom";
    private final String DATETO = "tagTo";
    private View viewLayout;
    LinearLayout llTanggal, llMulaiDari, llSampaiDengan, llTutupManual;
    EditText etFromDate, etToDate;
    ImageView ivStartDate, ivEndDate;
    int startDay, startMonth, startYear, endDay, endMonth, endYear;

    private DatePickerDialog dateFromPickerDialog;
    private DatePickerDialog dateToPickerDialog;

    private SimpleDateFormat dateFormatter;
    Calendar calendar, cTanggalAwal, cNextTomorrow;
    Button btnSubmit;
    ProgressDialog progdialog, progdialog2;
    SecurePreferences sp;
    String shopId = "", memberId = "", startDateText = "", endDateText = "";


    public FragTutupManual() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragTutupManual.
     */
    // TODO: Rename and change types and number of parameters
    public static FragTutupManual newInstance(String param1, String param2) {
        FragTutupManual fragment = new FragTutupManual();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewLayout = inflater.inflate(R.layout.frag_tutup_manual, container, false);

        calendar        = Calendar.getInstance();
        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();

        btnSubmit       = (Button) viewLayout.findViewById(R.id.btnSubmit);
        btnSubmit.setVisibility(View.GONE);
        llTanggal       = (LinearLayout) viewLayout.findViewById(R.id.llTanggal);
        llMulaiDari     = (LinearLayout) viewLayout.findViewById(R.id.llMulaiDari);
        llSampaiDengan  = (LinearLayout) viewLayout.findViewById(R.id.llSampaiDengan);
        llTutupManual   = (LinearLayout) viewLayout.findViewById(R.id.llTutupManual);
        llTutupManual.setVisibility(View.GONE);
        llTanggal.setVisibility(View.GONE);
        llMulaiDari.setVisibility(View.GONE);
        llSampaiDengan.setVisibility(View.GONE);


        ivStartDate     = (ImageView) viewLayout.findViewById(R.id.ivStartDate);
        ivEndDate       = (ImageView) viewLayout.findViewById(R.id.ivEndDate);
        //ivStartDate.setOnClickListener(this);
        //ivEndDate.setOnClickListener(this);

        etFromDate      = (EditText) viewLayout.findViewById(R.id.etFromDate);
        etToDate        = (EditText) viewLayout.findViewById(R.id.etToDate);
        etFromDate.setEnabled(false);
        etToDate.setEnabled(false);

        Calendar cTomorrow = Calendar.getInstance();
        cTomorrow.add(Calendar.DAY_OF_MONTH, 1);

        startDay        = cTomorrow.get(Calendar.DAY_OF_MONTH);
        startMonth      = cTomorrow.get(Calendar.MONTH);
        startYear       = cTomorrow.get(Calendar.YEAR);

        cNextTomorrow = Calendar.getInstance();
        cNextTomorrow.add(Calendar.DAY_OF_MONTH, 2);

        endYear         = cNextTomorrow.get(Calendar.YEAR);
        endMonth        = cNextTomorrow.get(Calendar.MONTH);
        endDay          = cNextTomorrow.get(Calendar.DAY_OF_MONTH);

        cTanggalAwal = Calendar.getInstance();
        cTanggalAwal.set(startYear, startMonth, startDay);

        progdialog              = DefinedDialog.CreateProgressDialog(getContext(), "");

        RequestParams params    = new RequestParams();
        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();
        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
        params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
        params.put(WebParams.FLAG_APPROVE, DefineValue.STRING_NO);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID +
                sp.getString(DefineValue.USERID_PHONE, "") + BuildConfig.AppID + DefineValue.STRING_NO));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getMemberShopList(getContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progdialog.dismiss();

                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {

                        llTutupManual.setVisibility(View.VISIBLE);
                        btnSubmit.setVisibility(View.VISIBLE);
                        llTanggal.setVisibility(View.VISIBLE);
                        llMulaiDari.setVisibility(View.VISIBLE);
                        llSampaiDengan.setVisibility(View.VISIBLE);

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
                Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });


        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //open
                Boolean hasError = false;

                if ( startDateText.equals("") || endDateText.equals("") ) {
                    hasError = true;
                }

                if ( !hasError ) {
                    updateOpenCloseDate();
                } else {
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setTitle(getString(R.string.alertbox_title_information));
                    alertDialog.setCancelable(false);

                    alertDialog.setMessage(getString(R.string.err_notif_start_or_end_date_empty));



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


        ivStartDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        dobPickerSetListener,
                        startYear,
                        startMonth,
                        startDay
                );
                Calendar cAwal = Calendar.getInstance();
                cAwal.set(startYear, startMonth, startDay);//Year,Mounth -1,Day
                dpd.setMinDate(cAwal);

                Calendar cAkhir = Calendar.getInstance();
                cAkhir.set(endYear, endMonth, endDay);//Year,Mounth -1,Day

                if ( !endDateText.equals("") ) {
                    dpd.setMaxDate(cAkhir);
                }

                dpd.show(getActivity().getFragmentManager(), DATEFROM);

            }
        });

        ivEndDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        dobPickerSetListener,
                        endYear,
                        endMonth,
                        endDay
                );
                Calendar cAwal = Calendar.getInstance();

                cAwal.set(startYear, startMonth, startDay+1);//Year,Mounth -1,Day
                //dpd.setMinDate(cAwal);

                Calendar cAkhir = Calendar.getInstance();
                cAkhir.set(startYear, startMonth, endDay);//Year,Mounth -1,Day

                if ( cAkhir.getTimeInMillis() != cTanggalAwal.getTimeInMillis() ) {
                    dpd.setMinDate(cAwal);
                } else {
                    dpd.setMinDate(cTanggalAwal);
                }

                dpd.show(getActivity().getFragmentManager(), DATETO);

            }
        });

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        return viewLayout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

            if(view.getTag().equals(DATEFROM)){
                startYear = year;
                startMonth = monthOfYear;
                startDay = dayOfMonth;
                etFromDate.setText(String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year) );
                startDateText = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
            }
            else {
                endYear = year;
                endMonth = monthOfYear;
                endDay = dayOfMonth;
                etToDate.setText(String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year) );
                endDateText = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
            }
        }
    };

    /*@Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        if ( view.getId() == getId("ivStartDate", R.id.class)) {
            etFromDate.setText(String.valueOf(dayOfMonth)+"/"+String.valueOf(month)+"/"+String.valueOf(year) );
        } else if ( view.getId() == getId("ivEndDate", R.id.class) ) {
            etToDate.setText(String.valueOf(dayOfMonth)+"/"+String.valueOf(month)+"/"+String.valueOf(year) );
        }
    }*/

    /*@Override
    public void onClick(View v) {
        if ( v == ivStartDate ) {
            dateFromPickerDialog.show();
        } else if ( v == ivEndDate ) {
            dateFromPickerDialog.show();
        }
    }*/

    public static int getId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            throw new RuntimeException("No resource ID found for: "
                    + resourceName + " / " + c, e);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

    }

    @Override
    public void onClick(View v) {

    }


    private void backToPreviousFragment() {
        //redirect back to fragment - BBSActivity;
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
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


    private void updateOpenCloseDate() {
        //closed
        try{
            progdialog2             = DefinedDialog.CreateProgressDialog(getContext(), "");



            RequestParams params2    = new RequestParams();

            UUID rcUUID             = UUID.randomUUID();
            String  dtime           = DateTimeFormat.getCurrentDateTime();
            String shopStatus       = DefineValue.SHOP_CLOSE;

            params2.put(WebParams.RC_UUID, rcUUID);
            params2.put(WebParams.RC_DATETIME, dtime);
            params2.put(WebParams.APP_ID, BuildConfig.AppID);
            params2.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params2.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params2.put(WebParams.SHOP_ID, shopId);
            params2.put(WebParams.MEMBER_ID, memberId);

            String idxStartMonth   = String.valueOf(startMonth+1);
            String idxStartDay     = String.valueOf(startDay);
            String idxEndMonth     = String.valueOf(endMonth+1);
            String idxEndDay       = String.valueOf(endDay);

            if ( idxStartMonth.length() == 1) idxStartMonth = "0"+idxStartMonth;
            if ( idxStartDay.length() == 1) idxStartDay = "0"+idxStartDay;
            if ( idxEndMonth.length() == 1) idxEndMonth = "0"+idxEndMonth;
            if ( idxEndDay.length() == 1) idxStartMonth = "0"+idxEndDay;


            params2.put(WebParams.SHOP_START_DATE, String.valueOf(startYear)+"-"+idxStartMonth+"-"+idxStartDay);
            params2.put(WebParams.SHOP_END_DATE, String.valueOf(endYear)+"-"+idxEndMonth+"-"+idxEndDay);

            params2.put(WebParams.SHOP_STATUS, shopStatus);
            params2.put(WebParams.SHOP_REMARK, "");

            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + memberId + shopId + BuildConfig.AppID + shopStatus));

            params2.put(WebParams.SIGNATURE, signature);

            MyApiClient.registerOpenCloseShop(getContext(), params2, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog2.dismiss();

                    try {

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setTitle(getString(R.string.alertbox_title_information));

                            alertDialog.setMessage(getString(R.string.message_notif_update_tutup_manual_success));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            getActivity().finish();
                                        }
                                    });
                            alertDialog.show();

                        } else {
                            //Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setTitle(getString(R.string.alertbox_title_information));

                            alertDialog.setMessage(response.getString(WebParams.ERROR_MESSAGE));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

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
                    Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if (progdialog2.isShowing())
                        progdialog2.dismiss();

                    Timber.w("Error Koneksi login:" + throwable.toString());

                }

            });



        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }

    }
}
