package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import timber.log.Timber;

public class FragDataMandiriLKD extends BaseFragment {
    public final static String TAG = "com.sgo.saldomu.fragments.FragDataMandiriLKD";
    View v;
    SecurePreferences sp;
    Bundle bundle;
    Button btn_submit, btn_cancel;
    EditText et_name, et_address, et_noID, et_noHp, et_pob, et_mothersname;
    Spinner sp_id_types;
    TextView tv_dob;
    String tx_id;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;
    private DateFormat fromFormat;
    private DateFormat toFormat;
    private DateFormat toFormat2;
    private String dateNow;
    private String dedate;
    private String date_dob;
    private String socialIdType;
    ProgressDialog progressDialog;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey;
    private String comm_code, tx_product_code, source_product_type,
            benef_city, source_product_h2h, api_key, callback_url, tx_bank_code, tx_bank_name, tx_product_name,
            fee, amount, share_type, comm_id, benef_product_name, name_benef, no_benef,
            no_hp_benef, remark, source_product_name, total_amount, transaksi, benef_product_code, tx_status,
            benef_product_type, max_resend;
    private Boolean TCASHValidation=false, MandiriLKDValidation=false, code_success =false;
    private Activity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_data_mandirilkd, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        act = getActivity();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        memberIDLogin = sp.getString(DefineValue.MEMBER_ID,"");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID,"");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        btn_submit = v.findViewById(R.id.btn_submit_data_mandirilkd);
        btn_cancel = v.findViewById(R.id.btn_cancel_mandirilkd);
        et_name = v.findViewById(R.id.name_mandiriLKD);
        et_address = v.findViewById(R.id.address_value);
        et_noID = v.findViewById(R.id.socialId_value);
        et_noHp = v.findViewById(R.id.noHP_value);
        et_pob = v.findViewById(R.id.pob_value);
        et_mothersname = v.findViewById(R.id.mothers_name_value);
        sp_id_types = v.findViewById(R.id.spinner_socialid_type);
        tv_dob = v.findViewById(R.id.dob_value);

        bundle = getArguments();
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            tx_id = bundle.getString(DefineValue.TX_ID, "");
            if(bundle.containsKey(DefineValue.BENEF_CITY)) {
                benef_city = bundle.getString(DefineValue.BENEF_CITY);
            }
            source_product_h2h = bundle.getString(DefineValue.PRODUCT_H2H);
            source_product_type = bundle.getString(DefineValue.PRODUCT_TYPE);
            tx_product_code = bundle.getString(DefineValue.PRODUCT_CODE);
            tx_bank_code = bundle.getString(DefineValue.BANK_CODE);
            tx_bank_name = bundle.getString(DefineValue.BANK_NAME);
            tx_product_name = bundle.getString(DefineValue.PRODUCT_NAME);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            tx_id = bundle.getString(DefineValue.TX_ID);
            amount = bundle.getString(DefineValue.AMOUNT);
            fee = bundle.getString(DefineValue.FEE);
            total_amount = bundle.getString(DefineValue.TOTAL_AMOUNT);
            share_type = bundle.getString(DefineValue.SHARE_TYPE);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID );
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            benef_product_code = bundle.getString(DefineValue.BENEF_PRODUCT_CODE);
            name_benef = bundle.getString(DefineValue.NAME_BENEF);
            no_benef =  bundle.getString(DefineValue.NO_BENEF);
            no_hp_benef  = bundle.getString(DefineValue.NO_HP_BENEF);
            remark = bundle.getString(DefineValue.REMARK);
            max_resend = bundle.getString(DefineValue.MAX_RESEND);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);
            TCASHValidation = bundle.getBoolean(DefineValue.TCASH_HP_VALIDATION);
            MandiriLKDValidation = bundle.getBoolean(DefineValue.MANDIRI_LKD_VALIDATION);
            code_success = bundle.getBoolean(DefineValue.CODE_SUCCESS);
            benef_product_type = bundle.getString(DefineValue.TYPE_BENEF,"");
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

        tv_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getActivity().getFragmentManager(), "asd");
            }
        });

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.social_id_type, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_id_types.setAdapter(spinAdapter);
        sp_id_types.setOnItemSelectedListener(spinnerIdTypes);

        btn_submit.setOnClickListener(submitlistener);
        btn_cancel.setOnClickListener(cancellistener);

    }

    private Spinner.OnItemSelectedListener spinnerIdTypes = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            socialIdType = sp_id_types.getItemAtPosition(i).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitlistener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputvalidation()) {
                sendData();
            } else btn_submit.setEnabled(false);
        }
    };

    public void sendData()
    {
        progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progressDialog.show();
        try{
        extraSignature = tx_id + sp.getString(DefineValue.MEMBER_ID,"") + socialIdType;
        RequestParams param = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_BBS_SEND_DATA,
                userPhoneID, accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_SEND_DATA, extraSignature);

        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.TX_ID, tx_id);
        params.put(WebParams.CUST_NAME, et_name.getText().toString());
        params.put(WebParams.MEMBER_ID, memberIDLogin);
        params.put(WebParams.CUST_PHONE, et_noHp.getText().toString());
        params.put(WebParams.CUST_ADDRESS, et_address.getText().toString());
        params.put(WebParams.CUST_ID_TYPE, socialIdType);
        params.put(WebParams.CUST_ID_NUMBER, et_noID.getText().toString());
        params.put(WebParams.CUST_BIRTH_PLACE, et_pob.getText().toString());
        params.put(WebParams.CUST_BIRTH_DATE, date_dob);
        params.put(WebParams.CUST_MOTHER_NAME, et_mothersname.getText().toString());

        Timber.d("params bbs send data : ", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_SEND_DATA, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {


                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("response bbs send data : ", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    changeToBBSCashInConfirm();

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                }else {
                                    Timber.d("isi error bbs send data:"+response.toString());
                                    String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            btn_submit.setEnabled(true);
                        }

                        @Override
                        public void onComplete() {
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    });
    }catch (Exception e){
        Timber.d("httpclient:"+e.getMessage());
    }

    }

    private void changeToBBSCashInConfirm() {

        Bundle mArgs = new Bundle();
        if(benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
            mArgs.putString(DefineValue.BENEF_CITY, benef_city);
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, tx_product_code);
        mArgs.putString(DefineValue.BANK_CODE, tx_bank_code);
        mArgs.putString(DefineValue.BANK_NAME, tx_bank_name);
        mArgs.putString(DefineValue.PRODUCT_NAME,tx_product_name);
        mArgs.putString(DefineValue.FEE, fee);
        mArgs.putString(DefineValue.COMMUNITY_CODE,comm_code);
        mArgs.putString(DefineValue.TX_ID,tx_id);
        mArgs.putString(DefineValue.AMOUNT,amount);
        mArgs.putString(DefineValue.TOTAL_AMOUNT,total_amount);
        mArgs.putString(DefineValue.SHARE_TYPE,"1");
        mArgs.putString(DefineValue.CALLBACK_URL,callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.NAME_BENEF, name_benef);
        mArgs.putString(DefineValue.NO_BENEF, no_benef);
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type);
        mArgs.putString(DefineValue.NO_HP_BENEF, no_hp_benef);
        mArgs.putString(DefineValue.REMARK, remark);
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.MAX_RESEND, max_resend);
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        mArgs.putString(DefineValue.BENEF_PRODUCT_CODE, benef_product_code);
        if (TCASHValidation!=null)
            mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, TCASHValidation);
        if (MandiriLKDValidation!=null)
            mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, MandiriLKDValidation);
        if (code_success!=null)
            mArgs.putBoolean(DefineValue.CODE_SUCCESS, code_success);
        btn_submit.setEnabled(true);

        Fragment mFrag = new BBSCashInConfirm();
        mFrag.setArguments(mArgs);

        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent , mFrag, BBSCashInConfirm.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_in), true);
    }

    Button.OnClickListener cancellistener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            }
            else
                getActivity().finish();
        }
    };


    private boolean inputvalidation()
    {
        int compare = 100;
        if(date_dob != null) {
            Date dob = null;
            Date now = null;
            try {
                dob = fromFormat.parse(date_dob);
                now = fromFormat.parse(dateNow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (dob != null) {
                if (now != null) {
                    compare = dob.compareTo(now);
                }
            }
            Timber.d("compare date:"+ Integer.toString(compare));
        }

        if (et_name.getText().toString().length()==0 )
        {
            et_name.requestFocus();
            et_name.setError("Nama dibutuhkan!");
            return false;
        }
        else if (et_noID.getText().toString().length()==0)
        {
            et_noID.requestFocus();
            et_noID.setError("No. ID dibutuhkan!");
            return false;
        }
        else if (et_address.getText().toString().length()==0)
        {
            et_address.requestFocus();
            et_address.setError("Alamat dibutuhkan!");
            return false;
        }
        else if (et_noHp.getText().toString().length()==0)
        {
            et_noHp.requestFocus();
            et_noHp.setError("No. Handphone dibutuhkan!");
            return false;
        }
        else if (et_pob.getText().toString().length()==0)
        {
            et_pob.requestFocus();
            et_pob.setError("Tempat Lahir dibutuhkan!");
            return false;
        }
        else if (et_mothersname.getText().length()==0)
        {
            et_mothersname.requestFocus();
            et_mothersname.setError("Nama Ibu dibutuhkan!");
            return false;
        }
        else if(compare == 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
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
        else if(compare >= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date))
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

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
            Timber.d("masuk date picker dob");
            try {
                date_dob = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : "+date_dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
        }
    };
}
