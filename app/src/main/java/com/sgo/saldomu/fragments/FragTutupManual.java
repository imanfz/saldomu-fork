package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

public class FragTutupManual extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Tutup_Manual";

    private final String DATEFROM = "tagFrom";
    private final String DATETO = "tagTo";
    LinearLayout llTanggal, llMulaiDari, llSampaiDengan, llTutupManual;
    EditText etFromDate, etToDate;
    ImageView ivStartDate, ivEndDate;
    int startDay, startMonth, startYear, endDay, endMonth, endYear;
    Calendar cTanggalAwal, cNextTomorrow;
    Button btnSubmit;
    ProgressDialog progdialog, progdialog2;
    SecurePreferences sp;
    String shopId = "", memberId = "", startDateText = "", endDateText = "";


    public FragTutupManual() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewLayout = inflater.inflate(R.layout.frag_tutup_manual, container, false);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        btnSubmit = viewLayout.findViewById(R.id.btnSubmit);
        btnSubmit.setVisibility(View.GONE);
        llTanggal = viewLayout.findViewById(R.id.llTanggal);
        llMulaiDari = viewLayout.findViewById(R.id.llMulaiDari);
        llSampaiDengan = viewLayout.findViewById(R.id.llSampaiDengan);
        llTutupManual = viewLayout.findViewById(R.id.llTutupManual);
        llTutupManual.setVisibility(View.GONE);
        llTanggal.setVisibility(View.GONE);
        llMulaiDari.setVisibility(View.GONE);
        llSampaiDengan.setVisibility(View.GONE);


        ivStartDate = viewLayout.findViewById(R.id.ivStartDate);
        ivEndDate = viewLayout.findViewById(R.id.ivEndDate);
        //ivStartDate.setOnClickListener(this);
        //ivEndDate.setOnClickListener(this);

        etFromDate = viewLayout.findViewById(R.id.etFromDate);
        etToDate = viewLayout.findViewById(R.id.etToDate);
        etFromDate.setEnabled(false);
        etToDate.setEnabled(false);

        Calendar cTomorrow = Calendar.getInstance();
        cTomorrow.add(Calendar.DAY_OF_MONTH, 1);

        startDay = cTomorrow.get(Calendar.DAY_OF_MONTH);
        startMonth = cTomorrow.get(Calendar.MONTH);
        startYear = cTomorrow.get(Calendar.YEAR);

        cNextTomorrow = Calendar.getInstance();
        cNextTomorrow.add(Calendar.DAY_OF_MONTH, 2);

        endYear = cNextTomorrow.get(Calendar.YEAR);
        endMonth = cNextTomorrow.get(Calendar.MONTH);
        endDay = cNextTomorrow.get(Calendar.DAY_OF_MONTH);

        cTanggalAwal = Calendar.getInstance();
        cTanggalAwal.set(startYear, startMonth, startDay);

        progdialog = DefinedDialog.CreateProgressDialog(getContext(), "");

        String extraSignature = DefineValue.STRING_NO;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
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

                                llTutupManual.setVisibility(View.VISIBLE);
                                btnSubmit.setVisibility(View.VISIBLE);
                                llTanggal.setVisibility(View.VISIBLE);
                                llMulaiDari.setVisibility(View.VISIBLE);
                                llSampaiDengan.setVisibility(View.VISIBLE);

                                JSONArray members = response.getJSONArray(WebParams.MEMBER);

                                if (members.length() > 0) {
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

        btnSubmit.setOnClickListener(v -> {

            //open
            boolean hasError = false;

            if (startDateText.equals("") || endDateText.equals("")) {
                hasError = true;
            }

            if (!hasError) {
                updateOpenCloseDate();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                alertDialog.setCancelable(false);

                alertDialog.setMessage(getString(R.string.err_notif_start_or_end_date_empty));


                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                        (dialog, which) -> dialog.dismiss());

                alertDialog.show();
            }
        });


        ivStartDate.setOnClickListener(v -> {

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

            if (!endDateText.equals("")) {
                dpd.setMaxDate(cAkhir);
            }

            if (getFragmentManager() != null) {
                dpd.show(getFragmentManager(), DATEFROM);
            }

        });

        ivEndDate.setOnClickListener(v -> {

            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    endYear,
                    endMonth,
                    endDay
            );
            Calendar cAwal = Calendar.getInstance();

            cAwal.set(startYear, startMonth, startDay + 1);//Year,Mounth -1,Day
            //dpd.setMinDate(cAwal);

            Calendar cAkhir = Calendar.getInstance();
            cAkhir.set(startYear, startMonth, endDay);//Year,Mounth -1,Day

            if (cAkhir.getTimeInMillis() != cTanggalAwal.getTimeInMillis()) {
                dpd.setMinDate(cAwal);
            } else {
                dpd.setMinDate(cTanggalAwal);
            }

            if (getFragmentManager() != null) {
                dpd.show(getFragmentManager(), DATETO);
            }

        });
        return viewLayout;
    }

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

            if (view.getTag() != null) {
                String text = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                if (view.getTag().equals(DATEFROM)) {
                    startYear = year;
                    startMonth = monthOfYear;
                    startDay = dayOfMonth;
                    etFromDate.setText(text);
                    startDateText = text;
                } else {
                    endYear = year;
                    endMonth = monthOfYear;
                    endDay = dayOfMonth;
                    etToDate.setText(text);
                    endDateText = text;
                }
            }
        }
    };

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

    }

    @Override
    public void onClick(View v) {

    }


    private void backToPreviousFragment() {
        //redirect back to fragment - BBSActivity;
        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext())).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle(getString(R.string.alertbox_title_information));
        alertDialog.setCancelable(false);

        alertDialog.setMessage(getString(R.string.message_notif_not_registered_agent));


        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                (dialog, which) -> Objects.requireNonNull(getActivity()).onBackPressed());

        alertDialog.show();
    }


    private void updateOpenCloseDate() {
        //closed
        try {
            progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), "");

            String shopStatus = DefineValue.SHOP_CLOSE;

            String extraSignature = memberId + shopId + shopStatus;
            HashMap<String, Object> params2 = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_OPEN_CLOSE_TOKO,
                    extraSignature);

            params2.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params2.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params2.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params2.put(WebParams.SHOP_ID, shopId);
            params2.put(WebParams.MEMBER_ID, memberId);

            String idxStartMonth = String.valueOf(startMonth + 1);
            String idxStartDay = String.valueOf(startDay);
            String idxEndMonth = String.valueOf(endMonth + 1);
            String idxEndDay = String.valueOf(endDay);

            if (idxStartMonth.length() == 1) idxStartMonth = "0" + idxStartMonth;
            if (idxStartDay.length() == 1) idxStartDay = "0" + idxStartDay;
            if (idxEndMonth.length() == 1) idxEndMonth = "0" + idxEndMonth;
            if (idxEndDay.length() == 1) idxEndDay = "0" + idxEndDay;


            params2.put(WebParams.SHOP_START_DATE, startYear + "-" + idxStartMonth + "-" + idxStartDay);
            params2.put(WebParams.SHOP_END_DATE, endYear + "-" + idxEndMonth + "-" + idxEndDay);

            params2.put(WebParams.SHOP_STATUS, shopStatus);
            params2.put(WebParams.SHOP_REMARK, "");
            params2.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_OPEN_CLOSE_TOKO, params2,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext())).create();
                                    alertDialog.setTitle(getString(R.string.alertbox_title_information));

                                    alertDialog.setMessage(getString(R.string.message_notif_update_tutup_manual_success));
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                            (dialog, which) -> Objects.requireNonNull(getActivity()).onBackPressed());
                                    alertDialog.show();

                                } else {
                                    //Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                                    AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext())).create();
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
                            if (progdialog2.isShowing())
                                progdialog2.dismiss();
                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }

    }
}
