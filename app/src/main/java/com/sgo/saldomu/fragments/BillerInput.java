package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.Beans.Denom_Data_Model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.NFCActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.utils.Converter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import timber.log.Timber;

/*
  Created by Administrator on 3/4/2015.
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class BillerInput extends Fragment implements NfcAdapter.ReaderCallback {

    public final static String TAG = "BILLER_INPUT";

    private String[] billerType = {
            "PLS",  //pulsa  0
            "TKN",  //voucher listrik  1
            "CC",   //Kartu Kredit   2
            "CCL",  //Cicilan  3
            "TLP",  //Telkom / telpon 4
            "PLN",  // PLN listrik  5
            "AIR",  //PAM  6
            "HP",   //handphone  7
            "PST",  //Tiket pesawat  8
            "KA",   //Tiket Kereta Api  9
            "TV",   //TV vable   10
            "ASU",  //Asuransi   11
            "INT",  //Internet   12
            "PBB",  //PBB   13
            "NON",  // PLN Nont-Taglis  14
            "SPP",  // SPP   15
            "RMH",  //Perumahan  16
            "BPJS", //BILLER_TYPE_BPJS 17
            "GAPP", // Game 18
            "EMON", // Emoney 19
            "VCHR",// voucher 20
            "DATA" // data 21

    };
    private View v;
    private View layout_denom;
    private View layout_month;
    private TextView tv_denom;
    private TextView tv_payment_remark;
    private TextView tv_month;
    private TextView tv_ovo;
    private TextView tv_notes;
    private EditText et_payment_remark;
    private Spinner spin_denom;
    private Spinner spin_month;
    private Button btn_submit, btn_cekSaldo;
    private ImageView spinWheelDenom;
    private ImageView spinWheelMonth;
    private RelativeLayout lyt_cekSaldo;
    private ProgressDialog progdialog;
    private Animation frameAnimation;
    private RadioGroup radioGroup;
    private String biller_type_code;
    private String biller_comm_id;
    private String biller_comm_name;
    private String denom_item_id;
    private String biller_info;
    String biller_api_key;
    private String biller_item_id;
    private String biller_comm_code;
    private String final_payment_remark;
    private String buy_type;
    private int buy_code;
    private ArrayList<String> _denomData;
    private ArrayList<String> _monthData;
    private Biller_Data_Model mBillerData;
    private Biller_Type_Data_Model mBillerTypeData;
    private List<Denom_Data_Model> mListDenomData;
    private RealmChangeListener realmListener;
    private Boolean isToken;
    Boolean isHaveItemID;
    //    private Spinner sp_privacy;
    private int privacy;
    private String digitsListener = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private Realm realm;
    private ArrayAdapter<String> adapterDenom;
    private ArrayAdapter<String> adapterMonth;
    private String selectedMonth;
    private LinearLayout layout_warn_pln;
    SecurePreferences sp;
    private ArrayList<String> _data;
    private List<Biller_Data_Model> mListBillerData;

    private NfcAdapter nfcAdapter;
    private String cardSelect = "";
    private String cardAttribute = "";
    private String cardInfo = "";
    private String cardUid = "";
    private String cardBalance = "";
    private String saldo = "";
    private String numberCard = "";

    private String appletType = "";
    private String updateCardKey = "";
    private String session = "";
    private String pendingAmount = "";
    private String institutionReff = "";
    private String sourceOfAccount = "";
    private String merchantData = "";
    private String optionNFC = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        Bundle args = getArguments();
        biller_type_code = args.getString(DefineValue.BILLER_TYPE, "");
        biller_comm_id = args.getString(DefineValue.COMMUNITY_ID, "");
        biller_comm_name = args.getString(DefineValue.COMMUNITY_NAME, "");
        biller_item_id = args.getString(DefineValue.BILLER_ITEM_ID, "");
        biller_comm_code = args.getString(DefineValue.BILLER_COMM_CODE, "");

        isToken = false;

        radioGroup = v.findViewById(R.id.billerinput_radio);
        spin_denom = v.findViewById(R.id.spinner_billerinput_denom);
        tv_denom = v.findViewById(R.id.billerinput_text_denom);
        tv_payment_remark = v.findViewById(R.id.billerinput_text_payment_remark);
        et_payment_remark = v.findViewById(R.id.payment_remark_billerinput_value);
        spinWheelDenom = v.findViewById(R.id.spinning_wheel_billerinput_denom);
        btn_submit = v.findViewById(R.id.btn_submit_billerinput);
        btn_cekSaldo = v.findViewById(R.id.btn_cekSaldo);
        lyt_cekSaldo = v.findViewById(R.id.lyt_cekSaldo);
        layout_denom = v.findViewById(R.id.billerinput_layout_denom);
//        sp_privacy = v.findViewById(R.id.privacy_spinner);
        spin_month = v.findViewById(R.id.spinner_billerinput_month);
        tv_month = v.findViewById(R.id.billerinput_text_month);
        tv_ovo = v.findViewById(R.id.tv_ovo);
        spinWheelMonth = v.findViewById(R.id.spinning_wheel_billerinput_month);
        layout_month = v.findViewById(R.id.billerinput_layout_month);
        layout_warn_pln = v.findViewById(R.id.layout_warn_pln);
        tv_notes = v.findViewById(R.id.biller_notes);

        et_payment_remark.setText(args.getString(DefineValue.CUST_ID, ""));
        if (args.containsKey(DefineValue.BILLER_ID_NUMBER)) {
            et_payment_remark.setText(args.getString(DefineValue.BILLER_ID_NUMBER));
        }

        btn_submit.setOnClickListener(submitInputListener);
        btn_cekSaldo.setOnClickListener(cekSaldoEmoney);
        radioGroup.setOnCheckedChangeListener(radioListener);
        layout_denom.setVisibility(View.VISIBLE);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        initializeRealm();
        initializeLayout();
        if (!biller_type_code.equalsIgnoreCase(billerType[0]))
            initializeSpinnerDenom();

        realmListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {

                if (isVisible()) {

                    initializeLayout();
                    initializeSpinnerDenom();

                    if (_denomData != null) {
                        Timber.d("Masuk realm listener denomdata isi");
                        _denomData.clear();
                        for (int i = 0; i < mListDenomData.size(); i++) {
                            _denomData.add(mListDenomData.get(i).getItem_name());
                        }

                        layout_denom.setVisibility(View.VISIBLE);
                        spin_denom.setVisibility(View.VISIBLE);
                        adapterDenom.notifyDataSetChanged();
                    }

                    if (progdialog != null && progdialog.isShowing()) {
                        progdialog.dismiss();
                    }
                }
            }
        };
        realm.addChangeListener(realmListener);

    }

    private void initializeLayout() {


        String[] _buy_type = getResources().getStringArray(R.array.buy_vpi_title);

        if (biller_type_code.equals(billerType[17])) {
            _monthData = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                _monthData.add(Integer.toString(i + 1));
            }
            adapterMonth = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _monthData);
            adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_month.setAdapter(adapterMonth);
            spin_month.setOnItemSelectedListener(spinnerMonthListener);
            spin_month.setVisibility(View.GONE);
            tv_month.setVisibility(View.GONE);
        } else {
            layout_month.setVisibility(View.GONE);
        }

        if (biller_type_code.equals(billerType[0]) || biller_type_code.equals(billerType[21])) {
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
            et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (biller_type_code.equals(billerType[1]) || biller_type_code.equals(billerType[5])) {
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            radioGroup.setVisibility(View.VISIBLE);
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Listrik));
            layout_warn_pln.setVisibility(View.VISIBLE);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        } else if (biller_type_code.equals(billerType[2])) {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_CC));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (biller_type_code.equals(billerType[6])) {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_PAM));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (biller_type_code.equals(billerType[7]) || biller_type_code.equals(billerType[19])) {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PURCHASE_TYPE;
            if (biller_comm_code.equals("EMONEYSALDOMU")) {
                tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Emoney));
                et_payment_remark.setText("6032984008386579");
                et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
                btn_cekSaldo.setVisibility(View.VISIBLE);
            } else {
                tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
                et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            }
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (biller_type_code.equals(billerType[8])) {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_PST));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        } else if (biller_type_code.equals(billerType[11])) {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_asuransi));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (biller_type_code.equals(billerType[16])) {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_RMH));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        } else if (biller_type_code.equals(billerType[18])) {
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            generateRandomString(20);
            tv_payment_remark.setVisibility(View.GONE);
            et_payment_remark.setVisibility(View.GONE);
        } else if (biller_type_code.equals(billerType[20])) {
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
            et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
    }

    private void initializeRealm() {
        mBillerTypeData = new Biller_Type_Data_Model();
        mBillerTypeData = realm.where(Biller_Type_Data_Model.class).
                equalTo(WebParams.BILLER_TYPE_CODE, biller_type_code).
                findFirst();

        if (mBillerTypeData.getBiller_data_models().size() == 1) {
            biller_comm_id = mBillerTypeData.getBiller_data_models().get(0).getComm_id();
            biller_comm_name = mBillerTypeData.getBiller_data_models().get(0).getComm_name();
            biller_item_id = mBillerTypeData.getBiller_data_models().get(0).getItem_id();
            biller_info = mBillerTypeData.getBiller_data_models().get(0).getBiller_info();
        }

        mBillerData = new Biller_Data_Model();

        if (!biller_type_code.equalsIgnoreCase("EMON")) {
            mBillerData = realm.where(Biller_Data_Model.class).
                    equalTo(WebParams.COMM_ID, biller_comm_id).
                    equalTo(WebParams.COMM_NAME, biller_comm_name).
                    equalTo(WebParams.DENOM_ITEM_ID, biller_item_id).
                    equalTo(WebParams.DENOM_COMM_CODE, biller_comm_code).
                    findFirst();
        } else {
            mBillerData = realm.where(Biller_Data_Model.class).
                    equalTo(WebParams.COMM_ID, biller_comm_id).
                    findFirst();
        }

        if (mBillerData.getBiller_info() != null || !mBillerData.getBiller_info().equals("")) {
            tv_notes.setVisibility(View.VISIBLE);
            tv_notes.setText(mBillerData.getBiller_info());
        }

        if (mBillerData == null || mBillerData.getItem_id().isEmpty() && mBillerData.getDenom_data_models().size() == 0) {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        }

        if (mBillerData != null)
            mListDenomData = realm.copyFromRealm(mBillerData.getDenom_data_models());

    }

    private void initializeSpinnerDenom() {
        if (mListDenomData.size() > 0) {
            _denomData = new ArrayList<>();
            adapterDenom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _denomData);
            adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_denom.setAdapter(adapterDenom);
            spin_denom.setOnItemSelectedListener(spinnerDenomListener);

            spin_denom.setVisibility(View.GONE);
            spinWheelDenom.setVisibility(View.VISIBLE);
            spinWheelDenom.startAnimation(frameAnimation);

            Thread deproses = new Thread() {
                @Override
                public void run() {
                    _denomData.clear();
                    for (int i = 0; i < mListDenomData.size(); i++) {
                        _denomData.add(mListDenomData.get(i).getItem_name());
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinWheelDenom.clearAnimation();
                            spinWheelDenom.setVisibility(View.GONE);
                            spin_denom.setVisibility(View.VISIBLE);
                            adapterDenom.notifyDataSetChanged();
                        }
                    });
                }
            };
            deproses.run();

        } else {
            layout_denom.setVisibility(View.GONE);
            denom_item_id = mBillerData.getItem_id();
        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_privacy.setAdapter(spinAdapter);
//        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

    }


    private Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i + 1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerDenomListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            denom_item_id = mListDenomData.get(i).getItem_id();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    private Spinner.OnItemSelectedListener spinnerMonthListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            selectedMonth = Integer.toString(pos + 1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitInputListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    if (biller_type_code.equals(billerType[0]))
                        final_payment_remark = NoHPFormat.formatTo62(String.valueOf(et_payment_remark.getText()));
                    else if (biller_type_code.equals(billerType[18])) {
                        final_payment_remark = generateRandomString(20);
                    } else
                        final_payment_remark = String.valueOf(et_payment_remark.getText());
                    showDialog(final_payment_remark);
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private Button.OnClickListener cekSaldoEmoney = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), NFCActivity.class);
            startActivity(intent);
        }
    };

    private RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioPrabayar:
                    layout_denom.setVisibility(View.VISIBLE);
                    biller_type_code = "TKN";
                    buy_code = BillerActivity.PURCHASE_TYPE;
                    tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Listrik));
                    break;
                case R.id.radioPascabayar:
                    layout_denom.setVisibility(View.GONE);
                    biller_type_code = "PLN";
                    buy_code = BillerActivity.PAYMENT_TYPE;
                    denom_item_id = mBillerData.getItem_id();
                    tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark));
                    break;
            }
            realm.refresh();
            initializeRealm();
            initializeSpinnerDenom();
        }
    };

    private void showDialog(String _payment_remark) {

        Bundle mArgs = getArguments();
        mArgs.putString(DefineValue.CUST_ID, _payment_remark);
        if (denom_item_id.equals(""))
            mArgs.putString(DefineValue.ITEM_ID, biller_item_id);
        else
            mArgs.putString(DefineValue.ITEM_ID, denom_item_id);
        mArgs.putInt(DefineValue.BUY_TYPE, buy_code);
        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
        mArgs.putString(DefineValue.BILLER_TYPE, biller_type_code);
        mArgs.putString(DefineValue.COMMUNITY_ID, biller_comm_id);
        mArgs.putString(DefineValue.COMMUNITY_NAME, biller_comm_name);
//        if(biller_type_code.equalsIgnoreCase(billerType[17]))
//            mArgs.putString(DefineValue.VALUE_ITEM_DATA, "1");

        Fragment mFrag = new BillerDesciption2();
        mFrag.setArguments(mArgs);
        switchFragment(mFrag, BillerActivity.FRAG_BIL_INPUT, null, true, BillerDesciption2.TAG);

    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, String
            next_name, Boolean isBackstack, String tag) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchContent(i, name, next_name, isBackstack, tag);
        et_payment_remark.setText("");
        spin_denom.setSelection(0);
        spin_month.setSelection(0);
    }

    private boolean inputValidation() {

        if (!biller_type_code.equals(billerType[18])) {
            if (et_payment_remark.getText().toString().length() == 0 || et_payment_remark.getText().toString().equals("0") || et_payment_remark.length() == 1) {
                et_payment_remark.requestFocus();
                if (biller_type_code.equals(billerType[0]))
                    et_payment_remark.setError(this.getString(R.string.regist1_validation_nohp));
                else
                    et_payment_remark.setError(this.getString(R.string.billerinput_validation_payment_remark));
                return false;
            }
        }

        return true;
    }

    public String generateRandomString(int length) {
        String randomString = "";

        final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890".toCharArray();
        final SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            randomString = randomString + chars[random.nextInt(chars.length)];
        }

        return randomString;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() > 0)
                    getFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        if (!realm.isInTransaction() && !realm.isClosed()) {
            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

//        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        nfcAdapter.enableReaderMode(getActivity(), this,
                NfcAdapter.FLAG_READER_NFC_A |
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        if (biller_comm_code.equals("EMONEYSALDOMU")) {
            if (nfcAdapter != null) {
                //Yes NFC available
                lyt_cekSaldo.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();

            byte[] selectEmoneyResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00A40400080000000000000001"));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("SELECT_RESPONSE : ", Converter.Companion.toHex(selectEmoneyResponse));
                    cardSelect = Converter.Companion.toHex(selectEmoneyResponse);
                }
            });

            byte[] cardAttirbuteResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00F210000B"));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CARD_ATTRIBUTE : ", Converter.Companion.toHex(cardAttirbuteResponse));
                    cardAttribute = Converter.Companion.toHex(cardAttirbuteResponse);
                }
            });

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("UUID : ", Converter.Companion.toHex(tag.getId()));
                    cardUid = Converter.Companion.toHex(tag.getId());
                }
            });

            byte[] cardInfoResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00B300003F"));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CARD_INFO : ", Converter.Companion.toHex(cardInfoResponse));
                    cardInfo = Converter.Companion.toHex(cardInfoResponse);
                    et_payment_remark.setText(cardInfo.substring(0, 16));
                    numberCard = cardInfo.substring(0, 16);
                }
            });


            byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00B500000A"));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("LAST_BALANCE : ", Converter.Companion.toHex(lastBalanceResponse));
                    cardBalance = Converter.Companion.toHex(lastBalanceResponse);
//                    cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
                    Log.d("SALDO : ", String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8))));
                    saldo = String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}