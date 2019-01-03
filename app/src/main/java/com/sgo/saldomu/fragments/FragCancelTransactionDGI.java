package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FragCancelTransactionDGI extends BaseFragment {
    View v;
    Button btnProses, btnCancel;
    TextView tv_next_visit;
    Spinner sp_reason;
    EditText et_reason;
    String memberCode, commCode;
    String reason;
    private DateFormat fromFormat;
    private DateFormat toFormat;
    private DateFormat toFormat2;
    private String dateNow;
    private String dedate;
    private String date_visit;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_cancel_transaction_dgi, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        et_reason = v.findViewById(R.id.et_reason);
        tv_next_visit = v.findViewById(R.id.tv_nextVisit);
        sp_reason = v.findViewById(R.id.sp_reason);
        btnProses = v.findViewById(R.id.btn_submit);
        btnCancel = v.findViewById(R.id.btn_cancel);

        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        }


        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        toFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID", "INDONESIA"));
        toFormat2 = new SimpleDateFormat("dd-M-yyyy", new Locale("ID", "INDONESIA"));

        Calendar c = Calendar.getInstance();
        dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:" + dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        tv_next_visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getActivity().getFragmentManager(), "asd");
            }
        });

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cancel_dgi_reason, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_reason.setAdapter(spinAdapter);
        sp_reason.setOnItemSelectedListener(spinnerReason);

        btnCancel.setOnClickListener(cancelListener);
        btnProses.setOnClickListener(prosesListener);
    }

    Button.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager().popBackStack();
        }
    };

    Button.OnClickListener prosesListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputValidation()) {
                cancelDGI();
            }
        }
    };

    private Spinner.OnItemSelectedListener spinnerReason = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            reason = sp_reason.getItemAtPosition(i).toString();
            if (reason.equalsIgnoreCase("LAINNYA")) {
                et_reason.setVisibility(View.VISIBLE);
            } else et_reason.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker dob");
            try {
                date_visit = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : " + date_visit);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_next_visit.setText(dedate);
        }
    };

    public Boolean inputValidation() {
        int compare = 100;
        if (date_visit != null) {
            Date visit = null;
            Date now = null;
            try {
                visit = fromFormat.parse(date_visit);
                now = fromFormat.parse(dateNow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (visit != null) {
                if (now != null) {
                    compare = visit.compareTo(now);
                }
            }
            Timber.d("compare date:" + Integer.toString(compare));
        }

        if (et_reason.getVisibility() == View.VISIBLE && reason.isEmpty()) {
            et_reason.requestFocus();
            et_reason.setError("Alasan dibutuhkan!");
            return false;
        } else if (compare == 100 || tv_next_visit.getText().toString().equalsIgnoreCase(getString(R.string.myprofile_text_date_click))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.validation_date_empty))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else if (compare <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.validation_date))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        return true;
    }

    public void cancelDGI() {
        showProgressDialog();

        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_SEARCH_DGI, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        if (reason.equalsIgnoreCase("lainnya")) {
            params.put(WebParams.REASON, et_reason.getText().toString());
        } else
            params.put(WebParams.REASON, reason);
        params.put(WebParams.MEMBER_CODE, memberCode);
        params.put(WebParams.COMM_CODE, commCode);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.NEXT_VISIT_DATE, date_visit);
        Timber.d("params cancel search DGI : " + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CANCEL_SEARCH_DGI, params,
                new ObjListeners() {

                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();

                            Timber.d("response cancel search DGI : " + response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Alert")
                                        .setMessage(getString(R.string.cancel_transaction_dgi))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                getActivity().finish();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            } else {
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
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

                    }
                });
    }
}
