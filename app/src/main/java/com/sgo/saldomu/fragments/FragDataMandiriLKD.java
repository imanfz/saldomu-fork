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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.BBSCommBenef;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class FragDataMandiriLKD extends BaseFragment {
    public final static String TAG = "com.sgo.saldomu.fragments.FragDataMandiriLKD";
    View v;
    SecurePreferences sp;
    Bundle bundle;
    Button btn_submit, btn_cancel;
    EditText et_name, et_address, et_noID, et_noHp, et_pob, et_mothersname, et_sumberdana;
    Spinner sp_sumberdana;
    TextView tv_dob;
    String tx_id;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;
    private DateFormat fromFormat;
    private DateFormat toFormat;
    private DateFormat toFormat2;
    private String dateNow;
    private String dedate;
    private String date_dob;
    private String sumberdana;
    ProgressDialog progressDialog;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey;
    private String comm_code, tx_product_code, source_product_type,
            benef_city, source_product_h2h, api_key, callback_url, tx_bank_code, tx_bank_name, tx_product_name,
            fee, amount, share_type, comm_id, benef_product_name, name_benef, no_benef,
            no_hp_benef, remark, source_product_name, total_amount, transaksi, benef_product_code, tx_status,
            benef_product_type, max_resend, custIDtypes, birthplace_id;
    LinearLayout layout_sender, layout_pob, layout_dob;
    private Boolean TCASHValidation = false, MandiriLKDValidation = false, code_success = false, isOwner = false;
    private Activity act;
    private Realm realm;
    private List<List_BBS_Birth_Place> list_bbs_birth_place;
    private List<String> list_name_bbs_birth_place;
    private ArrayList<BBSCommBenef> listDataBank;
    public Boolean isUpdate = false;
    AutoCompleteTextView city_textview_autocomplete;
    private Integer CityAutocompletePos = -1;


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
        realm = Realm.getInstance(RealmManager.BBSConfiguration);

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID, "");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID, "");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        custIDtypes = "KTP";

        btn_submit = v.findViewById(R.id.btn_submit_data_mandirilkd);
        btn_cancel = v.findViewById(R.id.btn_cancel_mandirilkd);
        et_name = v.findViewById(R.id.name_mandiriLKD);
        et_address = v.findViewById(R.id.address_value);
        et_noID = v.findViewById(R.id.socialId_value);
        et_noHp = v.findViewById(R.id.noHP_value);
        sp_sumberdana = v.findViewById(R.id.datalkd_sumber_dana);
        tv_dob = v.findViewById(R.id.dob_value);
        layout_sender = v.findViewById(R.id.layout_name);
        layout_pob = v.findViewById(R.id.layout_tempat_lahir);
        layout_dob = v.findViewById(R.id.layout_tanggal_lahir);
        city_textview_autocomplete = v.findViewById(R.id.mandiriLKD_pob);
        et_sumberdana = v.findViewById(R.id.et_sumber_dana);

        bundle = getArguments();
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            tx_id = bundle.getString(DefineValue.TX_ID, "");
            if (bundle.containsKey(DefineValue.BENEF_CITY)) {
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
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID);
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            benef_product_code = bundle.getString(DefineValue.BENEF_PRODUCT_CODE);
            name_benef = bundle.getString(DefineValue.NAME_BENEF);
            no_benef = bundle.getString(DefineValue.NO_BENEF);
            no_hp_benef = bundle.getString(DefineValue.NO_HP_BENEF);
            remark = bundle.getString(DefineValue.REMARK);
            max_resend = bundle.getString(DefineValue.MAX_RESEND);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);
            TCASHValidation = bundle.getBoolean(DefineValue.TCASH_HP_VALIDATION, false);
            MandiriLKDValidation = bundle.getBoolean(DefineValue.MANDIRI_LKD_VALIDATION, false);
            code_success = bundle.getBoolean(DefineValue.CODE_SUCCESS);
            benef_product_type = bundle.getString(DefineValue.TYPE_BENEF, "");
            isOwner = bundle.getBoolean(DefineValue.IS_OWNER, false);
        }

        RealmResults<List_BBS_Birth_Place> results = realm.where(List_BBS_Birth_Place.class).findAll();

        Timber.d("REALM RESULTS:" + results.toString());

        list_bbs_birth_place = new ArrayList<>();
        list_name_bbs_birth_place = new ArrayList<>();
        list_bbs_birth_place = realm.copyFromRealm(results);

        for (int i = 0; i < results.size(); i++) {

            if (results.get(i).getBirthPlace_city() == null || results.get(i).getBirthPlace_city().equalsIgnoreCase("")) {
//                list_name_bbs_birth_place.add("Unknown");
            } else {
                list_name_bbs_birth_place.add(results.get(i).getBirthPlace_city());
            }
        }


//        for (List_BBS_Birth_Place bbsBirthPlace : results) {
//            list_name_bbs_birth_place.add(bbsBirthPlace.getBirthPlace_city());
//
//        }

        Timber.d("Size of List name Birth place:" + list_name_bbs_birth_place.size());
        ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_selectable_list_item, list_name_bbs_birth_place);
        city_textview_autocomplete.setThreshold(1);
        city_textview_autocomplete.setAdapter(city_adapter);

//                list_bbs_birth_place = new ArrayList<>();
//        results.addChangeListener(new RealmChangeListener<RealmResults<List_BBS_Birth_Place>>() {
//            @Override
//            public void onChange(RealmResults<List_BBS_Birth_Place> list_bbs_birth_places) {
//                if (getActivity() != null && !getActivity().isFinishing()) {
//                    for (List_BBS_Birth_Place bbsBirthPlace : list_bbs_birth_places) {
//                        list_bbs_birth_place.add(bbsBirthPlace);
//                        list_name_bbs_birth_place.add(bbsBirthPlace.getBirthPlace_city());
//                    }
//                }
//            }
//        });

        initializeLayout();

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
                R.array.sumber_dana_type, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_sumberdana.setAdapter(spinAdapter);
        sp_sumberdana.setOnItemSelectedListener(spinnerSumberDana);

        btn_submit.setOnClickListener(submitlistener);
        btn_cancel.setOnClickListener(cancellistener);

    }

    public void initializeLayout() {
        if (!isOwner) {
            layout_dob.setVisibility(View.VISIBLE);
            layout_pob.setVisibility(View.VISIBLE);

        }
    }

    private Spinner.OnItemSelectedListener spinnerSumberDana = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            sumberdana = sp_sumberdana.getItemAtPosition(i).toString();
            if (sumberdana.equalsIgnoreCase("LAINNYA")) {
                et_sumberdana.setVisibility(View.VISIBLE);
            } else et_sumberdana.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitlistener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (inputvalidation()) {
//                birthplace_id = list_bbs_birth_place.get(CityAutocompletePos).getBirthPlace_id();
                sendData();
            }
        }
    };

    public void sendData() {
        progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progressDialog.show();
        try {
            extraSignature = tx_id + sp.getString(DefineValue.MEMBER_ID, "") + custIDtypes;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_SEND_DATA_LKD, extraSignature);

            if (isOwner) {
                params.put(WebParams.TRANSFER_TO, "S");
            } else {
                params.put(WebParams.TRANSFER_TO, "O");
                params.put(WebParams.CUST_BIRTH_PLACE, city_textview_autocomplete.getText().toString());
                params.put(WebParams.CUST_BIRTH_DATE, date_dob);
            }

            params.put(WebParams.CUST_NAME, et_name.getText().toString());
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.CUST_PHONE, et_noHp.getText().toString());
            params.put(WebParams.CUST_ADDRESS, et_address.getText().toString());
            params.put(WebParams.CUST_ID_TYPE, custIDtypes);
            params.put(WebParams.CUST_ID_NUMBER, et_noID.getText().toString());
            if (sumberdana.equalsIgnoreCase("LAINNYA")) {
                params.put(WebParams.SOURCE_OF_FUND, et_sumberdana.getText().toString());
            } else params.put(WebParams.SOURCE_OF_FUND, sumberdana);

            Timber.d("params bbs send data : ", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_SEND_DATA_LKD, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("response bbs send data : ", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    changeToBBSCashInConfirm(response.getString(WebParams.ADMIN_FEE), response.getString(WebParams.AMOUNT), response.getString(WebParams.TOTAL_AMOUNT), response.optString(WebParams.ADDITIONAL_FEE));

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    Timber.d("isi error bbs send data:" + response.toString());
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
                            btn_submit.setEnabled(true);
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }

    }

    private void changeToBBSCashInConfirm(String fee, String amount, String total_amount, String additional_fee) {

        Bundle mArgs = new Bundle();
        if (benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
            mArgs.putString(DefineValue.BENEF_CITY, benef_city);
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, tx_product_code);
        mArgs.putString(DefineValue.BANK_CODE, tx_bank_code);
        mArgs.putString(DefineValue.BANK_NAME, tx_bank_name);
        mArgs.putString(DefineValue.PRODUCT_NAME, tx_product_name);
        mArgs.putString(DefineValue.FEE, fee);
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code);
        mArgs.putString(DefineValue.TX_ID, tx_id);
        mArgs.putString(DefineValue.AMOUNT, amount);
        mArgs.putString(DefineValue.TOTAL_AMOUNT, total_amount);
        mArgs.putString(DefineValue.SHARE_TYPE, "1");
        mArgs.putString(DefineValue.CALLBACK_URL, callback_url);
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
        mArgs.putString(DefineValue.ADDITIONAL_FEE, additional_fee);
        if (TCASHValidation != null)
            mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, TCASHValidation);
        if (MandiriLKDValidation != null)
            mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, MandiriLKDValidation);
        if (code_success != null)
            mArgs.putBoolean(DefineValue.CODE_SUCCESS, code_success);
        btn_submit.setEnabled(true);

        Fragment mFrag = new BBSCashInConfirm();
        mFrag.setArguments(mArgs);

        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, BBSCashInConfirm.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_in), true);
    }

    Button.OnClickListener cancellistener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else
                getActivity().finish();
        }
    };


    private boolean inputvalidation() {
        int compare = 100;
        if (date_dob != null) {
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
            Timber.d("compare date:" + Integer.toString(compare));
        }
        if (et_noID.getText().toString().length() == 0) {
            et_noID.requestFocus();
            et_noID.setError("NIK dibutuhkan!");
            return false;
        } else if (et_noID.getText().toString().length() < 16) {
            et_noID.requestFocus();
            et_noID.setError("NIK Anda kurang lengkap!");
            return false;
        }else if (layout_sender.getVisibility() == View.VISIBLE) {
            if (et_name.getText().toString().length() == 0) {
                et_name.requestFocus();
                et_name.setError("Nama dibutuhkan!");
                return false;
            } else if (et_name.getText().toString().length() < 3) {
                et_name.requestFocus();
                et_name.setError("Nama minimal 3 karakter!");
                return false;
            }
        } else if (et_address.getText().toString().length() == 0) {
            et_address.requestFocus();
            et_address.setError("Alamat dibutuhkan!");
            return false;
        } else if (et_address.getText().toString().length() < 10) {
            et_address.requestFocus();
            et_address.setError("Alamat Anda kurang lengkap!");
            return false;
        } else if (layout_pob.getVisibility() == View.VISIBLE && layout_dob.getVisibility() == View.VISIBLE) {
            if (city_textview_autocomplete.getText().toString().trim().length() == 0) {
                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Tempat Lahir dibutuhkan!");
                return false;
            } else if (!list_name_bbs_birth_place.contains(city_textview_autocomplete.getText().toString())) {

                city_textview_autocomplete.requestFocus();
                city_textview_autocomplete.setError("Nama kota tidak ditemukan!");
                return false;
            } else if (compare == 100 || tv_dob.getText().toString().equalsIgnoreCase(getString(R.string.myprofile_text_date_click))) {
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
            } else if (compare >= 0) {
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
        }

        if (et_noHp.getText().toString().length() == 0) {
            et_noHp.requestFocus();
            et_noHp.setError("No. Handphone dibutuhkan!");
            return false;
        } else if (et_noHp.getText().toString().length() < 10) {
            et_noHp.requestFocus();
            et_noHp.setError("No. Handphone minimal 10 karakter!");
            return false;
        }else if (et_sumberdana.getVisibility() == View.VISIBLE && et_sumberdana.getText().toString().length() == 0) {
            et_sumberdana.requestFocus();
            et_sumberdana.setError("Sumber Dana dibutuhkan!");
            return false;
        }
        return true;
    }

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker dob");
            try {
                date_dob = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : " + date_dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
        }
    };
}
