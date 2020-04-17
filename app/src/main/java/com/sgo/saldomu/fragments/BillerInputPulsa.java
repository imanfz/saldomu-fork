package com.sgo.saldomu.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.PrefixOperatorValidator;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.BankBillerItem;
import com.sgo.saldomu.models.BillerDenomResponse;
import com.sgo.saldomu.models.BillerItem;
import com.sgo.saldomu.models.DenomDataItem;
import com.sgo.saldomu.models.retrofit.GetTrxStatusModel;
import com.sgo.saldomu.models.retrofit.InqBillerModel;
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class BillerInputPulsa extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    private static final String TAG = "BillerInputPulsa";
    public final static int REQUEST_BillerInqReq = 22;
    private View v;
    private TextView tv_denom;
    private ImageView img_operator;
    private TextView tv_payment_remark;
    private TextView tv_add_fee;
    private EditText et_payment_remark;
    private EditText et_add_fee;
    private Spinner spin_denom;
    private Button btn_submit;
    private RadioGroup radioGroup;
    private LinearLayout layout_add_fee;
    private LinearLayout layout_detail;
    private LinearLayout layout_denom;
    private LinearLayout layout_payment_method;
    private RelativeLayout layout_detail_add_fee;
    private TextView tv_detail_item_name;
    private TextView tv_detail_item_price;
    private TextView tv_detail_admin_fee;
    private TextView tv_detail_total;
    private Switch favoriteSwitch;
    private EditText notesEditText;

    private SecurePreferences sp;
    private String biller_type_code;
    private String biller_comm_id;
    private String biller_comm_name;
    private String biller_item_id;
    private String cust_id;
    private String buy_type_detail = "PRABAYAR";
    private String denom_item_id;
    private double additional_fee;
    String value_pin = "";
    String _amount = "";
    private String amount_desire = "";
    private int attempt;
    private int failed;

    private Realm realm2;
    private BillerItem mBillerData;
    private BillerItem mDenomData;
    private List<DenomDataItem> mListDenomData;
    private List<BankBillerItem> mListBankBiller;
    private ArrayList<String> _data = new ArrayList<>();
    private ArrayList<String> _denomData;
    private ArrayAdapter<String> adapterDenom;
    private boolean is_display_amount;
    private boolean is_input_amount;
    private String tx_id;
    private String item_id;
    private String ccy_id;
    private String item_name;
    private String description;
    private String enabledAdditionalFee;
    private Double amount = 0.0;
    private Double totalAmount = 0.0;
    private Double fee = 0.0;
    private boolean isAgent;
    private boolean isShowDescription;
    private String biller_comm_code;
    private String biller_api_key;
    private String callback_url;
    private List<String> paymentData = new ArrayList<>();
    private ArrayAdapter<String> adapterPaymentOptions;
    private Spinner spin_payment_options;
    private String payment_name;
    private listBankModel mTempBank;
    private SentPaymentBillerModel sentPaymentBillerModel;
    List<BillerItem> billerItemList = new ArrayList<>();
    private boolean is_sgo_plus;
    private boolean isPIN;
    private int buy_type;
    private Bundle args;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_input_new, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        args = getArguments();
        biller_type_code = args.getString(DefineValue.BILLER_TYPE, "");
//        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        realm2 = Realm.getInstance(RealmManager.realmConfiguration);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        spin_denom = v.findViewById(R.id.billerinput_spinner_denom);
        tv_denom = v.findViewById(R.id.billerinput_text_denom);
        img_operator = v.findViewById(R.id.img_operator);
        tv_payment_remark = v.findViewById(R.id.billerinput_text_id_remark);
        tv_add_fee = v.findViewById(R.id.billerinput_detail_admin_add_fee);
        et_payment_remark = v.findViewById(R.id.billerinput_et_id_remark);
        et_add_fee = v.findViewById(R.id.billerinput_et_add_fee);
        btn_submit = v.findViewById(R.id.btn_submit_billerinput);
        radioGroup = v.findViewById(R.id.billerinput_radio);
        layout_add_fee = v.findViewById(R.id.billerinput_layout_add_fee);
        layout_detail = v.findViewById(R.id.billerinput_layout_detail);
        layout_detail_add_fee = v.findViewById(R.id.billerinput_detail_layout_add_fee);
        layout_denom = v.findViewById(R.id.billerinput_layout_denom);
        layout_payment_method = v.findViewById(R.id.billerinput_layout_payment_method);
        tv_detail_item_name = v.findViewById(R.id.billerinput_detail_text_name);
        tv_detail_item_price = v.findViewById(R.id.billerinput_detail_price);
        tv_detail_admin_fee = v.findViewById(R.id.billerinput_detail_admin_fee);
        tv_detail_total = v.findViewById(R.id.billerinput_detail_total);
        spin_payment_options = v.findViewById(R.id.billerinput_spinner_payment_options);
        favoriteSwitch = v.findViewById(R.id.favorite_switch);
        notesEditText = v.findViewById(R.id.notes_edit_text);

        btn_submit.setOnClickListener(submitInputListener);
        radioGroup.setOnCheckedChangeListener(radioListener);

        initLayout();
//        initRealm();
        initPrefixListener();
//        if (_data.isEmpty())
//        {
        getBillerDenom();
//        }else {
        if (args.getString(DefineValue.CUST_ID, "") != "") {
            et_payment_remark.setText(NoHPFormat.formatTo08(args.getString(DefineValue.CUST_ID, "")));
            checkOperator();
            if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
                showChoosePayment();
            }
        }
//        }

        favoriteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notesEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            notesEditText.setEnabled(isChecked);
        });

    }


    private void initPrefixListener() {
        et_payment_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String string = editable.toString();
                if (string.length() > 3) {
                    checkOperator();
                }
            }
        });
        et_add_fee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                additional_fee = 0.0;
                if (!string.isEmpty())
                    if (string.equals("0")) {
                        et_add_fee.setText("");
                    } else {
                        additional_fee = Double.parseDouble(string);
                    }
                tv_add_fee.setText(getString(R.string.rp_) + " " + CurrencyFormat.format(additional_fee));
                countTotal();
            }
        });
    }

    private void countTotal() {
        totalAmount = amount + additional_fee + fee;
        tv_detail_total.setText(getString(R.string.rp_) + " " + CurrencyFormat.format(totalAmount));
    }

    private void checkOperator() {
        cust_id = et_payment_remark.getText().toString();
        PrefixOperatorValidator.OperatorModel BillerIdNumber = PrefixOperatorValidator.validation(getActivity(), cust_id);

        if (BillerIdNumber != null) {

            if (BillerIdNumber.prefix_name.toLowerCase().equalsIgnoreCase("telkomsel")) {
                img_operator.setBackground(getResources().getDrawable(R.drawable.telkomsel));
            } else if (BillerIdNumber.prefix_name.toLowerCase().equalsIgnoreCase("xl")) {
                img_operator.setBackground(getResources().getDrawable(R.drawable.xl));
            } else if (BillerIdNumber.prefix_name.toLowerCase().equalsIgnoreCase("indosat")) {
                img_operator.setBackground(getResources().getDrawable(R.drawable.indosat));
            } else if (BillerIdNumber.prefix_name.toLowerCase().equalsIgnoreCase("three")) {
                img_operator.setBackground(getResources().getDrawable(R.drawable.three));
            } else if (BillerIdNumber.prefix_name.toLowerCase().equalsIgnoreCase("smart")) {
                img_operator.setBackground(getResources().getDrawable(R.drawable.smartfren));
            } else if (BillerIdNumber.prefix_name.toLowerCase().equalsIgnoreCase("axis")) {
                img_operator.setBackground(getResources().getDrawable(R.drawable.axis));
            } else
                img_operator.setVisibility(View.GONE);

            if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
                for (int i = 0; i < _data.size(); i++) {
                    Timber.d("_data" + _data.get(i));
                    if (_data != null) {
                        String a = _data.get(i).toLowerCase();
                        String b = BillerIdNumber.prefix_name.toLowerCase();
                        if (_data.get(i).toLowerCase().contains(BillerIdNumber.prefix_name.toLowerCase())) {
                            biller_comm_id = Objects.requireNonNull(billerItemList.get(i)).getCommId();
                            biller_comm_name = Objects.requireNonNull(billerItemList.get(i)).getCommName();
                            biller_item_id = Objects.requireNonNull(billerItemList.get(i)).getItemId();

                            //leo
//                            mDenomData = new BillerItem();
//                            mDenomData = realm2.where(BillerItem.class).
//                                    equalTo(WebParams.COMM_ID, biller_comm_id).
//                                    equalTo(WebParams.COMM_NAME, biller_comm_name).
//                                    equalTo(WebParams.DENOM_ITEM_ID, biller_item_id).
//                                    findFirst();
//
//                            if (mDenomData == null) {
//                                mListDenomData = new ArrayList<>();
//                            } else {
//                                mListDenomData = realm2.copyFromRealm(mDenomData.getDenomData());
//
//                                initializeSpinnerDenom();
//                            }

                            initializeSpinnerDenom(i);
                        }
                    }
                }
            } else if (buy_type_detail.equalsIgnoreCase("PASCABAYAR")) {
                for (int i = 0; i < billerItemList.size(); i++) {
                    Timber.d(billerItemList.get(i).getCommName());
                    if (billerItemList.get(i).getCommName().contains(BillerIdNumber.prefix_name)) {
                        biller_comm_id = billerItemList.get(i).getCommId();
                        biller_comm_name = billerItemList.get(i).getCommName();
                        biller_item_id = billerItemList.get(i).getItemId();
                        break;
                    }
                    if (billerItemList.get(i).getCommName().contains("Excelcomindo Xplor")) {
                        biller_comm_id = billerItemList.get(i).getCommId();
                        biller_comm_name = billerItemList.get(i).getCommName();
                        biller_item_id = billerItemList.get(i).getItemId();
                        break;
                    }
                }
                if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) {
                    for (BillerItem object : billerItemList) {

                        Log.e(TAG, "billerItemList : " + object.getCommName());

                        if (!object.getCommId().equals("")) {
                            biller_comm_id = object.getCommId();
                        }

                        if (!object.getCommName().equals("")) {
                            biller_comm_name = object.getCommName();
                        }
                        if (!object.getItemId().equals("")) {
                            biller_item_id = object.getItemId();
                            break;
                        }
                    }
                }
            }
        }
    }

    //leo
//    private void initializeSpinnerDenom() {
//        if (mListDenomData.size() > 0) {
//            _denomData = new ArrayList<>();
//            adapterDenom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _denomData);
//            adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spin_denom.setAdapter(adapterDenom);
//            spin_denom.setOnItemSelectedListener(spinnerDenomListener);
//
//            spin_denom.setVisibility(View.GONE);
//
//            Thread deproses = new Thread() {
//                @Override
//                public void run() {
//                    _denomData.clear();
//                    _denomData.add(getString(R.string.billerinput_text_spinner_default_pulsa));
//                    for (int i = 0; i < mListDenomData.size(); i++) {
//                        _denomData.add(mListDenomData.get(i).getItemName());
//                    }
//
//                    getActivity().runOnUiThread(() -> {
//                        spin_denom.setVisibility(View.VISIBLE);
//                        adapterDenom.notifyDataSetChanged();
//                    });
//                }
//            };
//            deproses.run();
//
//        } else {
//            denom_item_id = mBillerData.getItemId();
//        }
//
//        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.privacy_list, android.R.layout.simple_spinner_item);
//        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        mBillerData = new BillerItem();
//        mBillerData = realm2.where(BillerItem.class).equalTo(WebParams.COMM_ID, biller_comm_id).equalTo(WebParams.COMM_NAME, biller_comm_name).findFirst();
//        mListBankBiller = realm2.copyFromRealm(mBillerData.getBankBiller());
//        biller_comm_code = mBillerData.getCommCode();
//        biller_api_key = mBillerData.getApiKey();
////        callback_url = mBillerData.getCallback_url();
//        if (!billerItemList.isEmpty()) {
//            paymentData = new ArrayList<>();
//            adapterPaymentOptions = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paymentData);
//            adapterPaymentOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spin_payment_options.setAdapter(adapterPaymentOptions);
//            spin_payment_options.setOnItemSelectedListener(spinnerPaymentListener);
//
//            if (isVisible()) {
//                ArrayList<String> tempDataPaymentName = new ArrayList<>();
//                paymentData.add(getString(R.string.billerinput_text_spinner_default_payment));
//
//                for (int i = 0; i < mListBankBiller.size(); i++) {
//                    if (mListBankBiller.get(i).getProductCode().equals(DefineValue.SCASH)) {
//                        paymentData.add(getString(R.string.appname));
//                        mListBankBiller.get(i).setProductName(getString(R.string.appname));
//                    } else {
//                        tempDataPaymentName.add(mListBankBiller.get(i).getProductName());
//                    }
//                }
//                if (!tempDataPaymentName.isEmpty())
//                    Collections.sort(tempDataPaymentName);
//
//                paymentData.addAll(tempDataPaymentName);
//                adapterPaymentOptions.notifyDataSetChanged();
//
//                spin_payment_options.setSelection(1); //set metode pembayaran jadi saldomu
//            }
//        } else {
//            biller_item_id = mBillerData.getItemId();
//        }
//    }

    private void initializeSpinnerDenom(int indexData) {
        mListDenomData = billerItemList.get(indexData).getDenomData();
        mListBankBiller = billerItemList.get(indexData).getBankBiller();

        if (mListDenomData.size() > 0) {
            _denomData = new ArrayList<>();
            adapterDenom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _denomData);
            adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_denom.setAdapter(adapterDenom);
            spin_denom.setOnItemSelectedListener(spinnerDenomListener);

            spin_denom.setVisibility(View.GONE);

            Thread deproses = new Thread() {
                @Override
                public void run() {
                    _denomData.clear();
                    _denomData.add(getString(R.string.billerinput_text_spinner_default_pulsa));
                    for (int i = 0; i < mListDenomData.size(); i++) {
                        _denomData.add(mListDenomData.get(i).getItemName());
                    }

                    getActivity().runOnUiThread(() -> {
                        spin_denom.setVisibility(View.VISIBLE);
                        adapterDenom.notifyDataSetChanged();
                    });
                }
            };
            deproses.run();

        } else {
            denom_item_id = mBillerData.getItemId();
        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBillerData = billerItemList.get(indexData);
        mListBankBiller = billerItemList.get(indexData).getBankBiller();
        biller_comm_code = billerItemList.get(indexData).getCommCode();
        biller_api_key = billerItemList.get(indexData).getApiKey();
        if (!billerItemList.isEmpty()) {
            paymentData = new ArrayList<>();
            adapterPaymentOptions = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paymentData);
            adapterPaymentOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_payment_options.setAdapter(adapterPaymentOptions);
            spin_payment_options.setOnItemSelectedListener(spinnerPaymentListener);

            if (isVisible()) {
                ArrayList<String> tempDataPaymentName = new ArrayList<>();
                paymentData.add(getString(R.string.billerinput_text_spinner_default_payment));

                for (int i = 0; i < mListBankBiller.size(); i++) {
                    if (mListBankBiller.get(i).getProductCode().equals(DefineValue.SCASH)) {
                        paymentData.add(getString(R.string.appname));
                        mListBankBiller.get(i).setProductName(getString(R.string.appname));
                    } else {
                        tempDataPaymentName.add(mListBankBiller.get(i).getProductName());
                    }
                }
                if (!tempDataPaymentName.isEmpty())
                    Collections.sort(tempDataPaymentName);

                paymentData.addAll(tempDataPaymentName);
                adapterPaymentOptions.notifyDataSetChanged();

                spin_payment_options.setSelection(1); //set metode pembayaran jadi saldomu
            }
        } else {
            biller_item_id = mBillerData.getItemId();
        }
    }

    private void showChoosePayment() {
        ArrayList<String> tempDataPaymentName = new ArrayList<>();
        paymentData.add(getString(R.string.billerinput_text_spinner_default_payment));

        if (mListBankBiller != null) {
            for (int i = 0; i < mListBankBiller.size(); i++) {
                if (mListBankBiller.get(i).getProductCode().equals(DefineValue.SCASH)) {
                    paymentData.add(getString(R.string.appname));
                    mListBankBiller.get(i).setProductName(getString(R.string.appname));
                } else {
                    tempDataPaymentName.add(mListBankBiller.get(i).getProductName());
                }
            }
            if (!tempDataPaymentName.isEmpty())
                Collections.sort(tempDataPaymentName);

            paymentData.addAll(tempDataPaymentName);
            adapterPaymentOptions.notifyDataSetChanged();

            spin_payment_options.setSelection(1); //set metode pembayaran jadi saldomu

        }
    }

    private Spinner.OnItemSelectedListener spinnerDenomListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
                if (position != 0) {
                    denom_item_id = mListDenomData.get(position - 1).getItemId();
                    if (cust_id.length() >= 10)
                        sentInquryBiller();
                } else {
                    denom_item_id = null;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerPaymentListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Object item = adapterView.getItemAtPosition(position);
            payment_name = item.toString();
            for (int i = 0; i < mListBankBiller.size(); i++) {
                if (payment_name.equals(mListBankBiller.get(i).getProductName())) {
                    mTempBank = new listBankModel(mListBankBiller.get(i).getBankCode(),
                            mListBankBiller.get(i).getBankName(),
                            mListBankBiller.get(i).getProductCode(),
                            mListBankBiller.get(i).getProductName(),
                            mListBankBiller.get(i).getProductType(),
                            mListBankBiller.get(i).getProductH2h());
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void sentInquryBiller() {
        try {
            showProgressDialog();
            ToggleKeyboard.hide_keyboard(getActivity());

            cust_id = NoHPFormat.formatTo62(String.valueOf(et_payment_remark.getText()));

            extraSignature = biller_comm_id + denom_item_id + cust_id;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_BILLER, extraSignature);
            params.put(WebParams.DENOM_ITEM_ID, denom_item_id);
            params.put(WebParams.DENOM_ITEM_REMARK, cust_id);
            params.put(WebParams.COMM_ID, biller_comm_id);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID_REMARK, MyApiClient.COMM_ID);

            Timber.d("isi params sent inquiry biller:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_BILLER, params, new ResponseListener() {
                @Override
                public void onResponses(JsonObject object) {
                    InqBillerModel model = getGson().fromJson(object, InqBillerModel.class);
                    String code = model.getError_code();
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        setIs_input_amount(model.getBiller_input_amount().equals(DefineValue.STRING_YES));
                        is_display_amount = model.getBiller_display_amount().equals(DefineValue.STRING_YES);

                        tx_id = model.getTx_id();
                        item_id = model.getItem_id();
                        ccy_id = model.getCcy_id();
                        amount = Double.parseDouble(model.getAmount()) - Double.parseDouble(model.getAdmin_fee());
                        item_name = model.getItem_name();
                        description = getGson().toJson(model.getDescription());
                        fee = Double.parseDouble(model.getAdmin_fee());
                        enabledAdditionalFee = model.getEnabled_additional_fee();

                        if (isAgent && enabledAdditionalFee.equals(DefineValue.Y)) {
                            layout_add_fee.setVisibility(View.VISIBLE);
                            layout_detail_add_fee.setVisibility(View.VISIBLE);
                        }

                        layout_detail.setVisibility(View.VISIBLE);
                        if (is_display_amount)
                            isShowDescription = true;
                        tv_detail_item_name.setText(item_name);
                        tv_detail_item_price.setText(getString(R.string.rp_) + " " + CurrencyFormat.format(amount));
                        tv_detail_admin_fee.setText(getString(R.string.rp_) + " " + CurrencyFormat.format(fee));
                    } else if (code.equals(WebParams.LOGOUT_CODE)) {
                        AlertDialogLogout dialogLogout = AlertDialogLogout.getInstance();
                        dialogLogout.showDialoginActivity(getActivity(), model.getError_message());
                    } else if (code.equals(DefineValue.ERROR_9333)) {
                        Timber.d("isi response app data:" + model.getApp_data());
                        final AppDataModel appModel = model.getApp_data();
                        AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                        alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                    } else if (code.equals(DefineValue.ERROR_0066)) {
                        Timber.d("isi response maintenance:" + object.toString());
                        AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                        alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                    } else {
                        code = model.getError_code() + " : " + model.getError_message();
                        if (isVisible()) {
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            getFragmentManager().popBackStack();
                        }
                        layout_detail.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    getFragmentManager().popBackStack();
                }

                @Override
                public void onComplete() {
                    dismissProgressDialog();
                    countTotal();
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void initLayout() {
        tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
        tv_denom.setText(getString(R.string.billerinput_text_spinner_pulsa));
        et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        radioGroup.setVisibility(View.VISIBLE);
        layout_payment_method.setVisibility(View.VISIBLE);
        layout_add_fee.setVisibility(View.GONE);
        layout_detail.setVisibility(View.GONE);
        layout_detail_add_fee.setVisibility(View.GONE);
    }

    private RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioPrabayar:
                    layout_denom.setVisibility(View.VISIBLE);
                    buy_type_detail = "PRABAYAR";
                    biller_type_code = "PLS";
                    layout_payment_method.setVisibility(View.VISIBLE);
                    layout_add_fee.setVisibility(View.GONE);
                    layout_detail.setVisibility(View.GONE);
                    setActionBarTitle(getString(R.string.biller_ab_title) + " - " + getString(R.string.prepaid_title));
                    break;
                case R.id.radioPascabayar:
                    layout_denom.setVisibility(View.GONE);
                    buy_type_detail = "PASCABAYAR";
                    biller_type_code = "HP";
                    layout_payment_method.setVisibility(View.GONE);
                    layout_add_fee.setVisibility(View.GONE);
                    layout_detail.setVisibility(View.GONE);
                    setActionBarTitle(getString(R.string.biller_ab_title) + " - " + getString(R.string.postpaid_title));
                    break;
            }
            initRealm();
            checkOperator();
        }
    };

    private Button.OnClickListener submitInputListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    btn_submit.setEnabled(false);
                    cust_id = NoHPFormat.formatTo62(String.valueOf(et_payment_remark.getText()));
                    showDialog();
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private boolean inputValidation() {
        if (et_payment_remark.getText().toString().length() == 0 || et_payment_remark.getText().toString().equals("0") || et_payment_remark.length() == 1) {
            et_payment_remark.requestFocus();
            et_payment_remark.setError(this.getString(R.string.regist1_validation_nohp));
            //leo
//            initializeSpinnerDenom();
            return false;
        }
        if (buy_type_detail.equalsIgnoreCase("PRABAYAR"))
            if (denom_item_id == null) {
                Toast.makeText(getActivity(), getString(R.string.billerinput_validation_spinner_default_pulsa), Toast.LENGTH_LONG).show();
                return false;
            }
        if (favoriteSwitch.isChecked() && notesEditText.getText().toString().length() == 0) {
            notesEditText.requestFocus();
            notesEditText.setError(getString(R.string.payfriends_notes_zero));
            return false;
        }
        return true;
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

    private void initRealm() {
        Log.v(TAG, "initRealm()");

        RealmResults<BillerItem> realmResults = realm2.where(BillerItem.class).equalTo("billerType", biller_type_code).findAll();

        billerItemList.clear();
        _data.clear();

        billerItemList.addAll(realmResults);
        for (BillerItem item : billerItemList) {
            Log.v(TAG, "_data " + item.getCommName());
            _data.add(item.getCommName());
        }
    }

    private void showDialog() {

        Bundle mArgs = new Bundle();

        if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
            mArgs.putString(DefineValue.CUST_ID, cust_id);
            mArgs.putString(DefineValue.ITEM_ID, denom_item_id);
            buy_type = BillerActivity.PURCHASE_TYPE;
        } else if (buy_type_detail.equalsIgnoreCase("PASCABAYAR")) {
            mArgs.putString(DefineValue.CUST_ID, et_payment_remark.getText().toString());
            mArgs.putString(DefineValue.ITEM_ID, biller_item_id);
            buy_type = BillerActivity.PAYMENT_TYPE;
        }
        mArgs.putInt(DefineValue.BUY_TYPE, buy_type);
        mArgs.putString(DefineValue.BILLER_TYPE, biller_type_code);
        mArgs.putString(DefineValue.COMMUNITY_ID, biller_comm_id);
        mArgs.putString(DefineValue.COMMUNITY_NAME, biller_comm_name);

        if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
            sentPaymentBiller();
        } else if (buy_type_detail.equalsIgnoreCase("PASCABAYAR")) {
            Fragment mFrag = new BillerDesciption();
            mFrag.setArguments(mArgs);
            switchFragment(mFrag, BillerActivity.FRAG_BIL_INPUT, null, true, BillerDesciption.TAG);
        }
    }

    private void sentPaymentBiller() {
        try {
            showProgressDialog();

            String bank_code = mTempBank.getBank_code();
            String product_code = mTempBank.getProduct_code();

            extraSignature = tx_id + item_id + biller_comm_id + product_code;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature);
            params.put(WebParams.DENOM_ITEM_ID, item_id);
            params.put(WebParams.DENOM_ITEM_REMARK, cust_id);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.AMOUNT, amount + fee);

            params.put(WebParams.BANK_CODE, bank_code);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.COMM_ID, biller_comm_id);
            params.put(WebParams.MEMBER_CUST, sp.getString(DefineValue.CUST_ID, ""));
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());

            params.put(WebParams.COMM_CODE, biller_comm_code);
            params.put(WebParams.USER_COMM_CODE, sp.getString(DefineValue.COMMUNITY_CODE, ""));

            params.put(WebParams.PRODUCT_H2H, mTempBank.getProduct_h2h());
            params.put(WebParams.PRODUCT_TYPE, mTempBank.getProduct_type());
            params.put(WebParams.USER_ID, userPhoneID);
            if (isAgent) {
                if (!et_add_fee.getText().toString().equals("")) {
                    params.put(WebParams.ADDITIONAL_FEE, et_add_fee.getText().toString());
                } else
                    params.put(WebParams.ADDITIONAL_FEE, "0");
            }

            Timber.d("isi params sent payment biller:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_PAYMENT_BILLER, params, new ResponseListener() {
                @Override
                public void onResponses(JsonObject object) {
                    sentPaymentBillerModel = getGson().fromJson(object, SentPaymentBillerModel.class);
                    String code = sentPaymentBillerModel.getError_code();
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        if (mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB)) {
                            submitBiller(bank_code, product_code, -1);
                        } else {
                            int attempt = sentPaymentBillerModel.getFailed_attempt();
                            if (attempt != -1)
                                attempt = sentPaymentBillerModel.getMax_failed() - attempt;
                            sentDataReqToken(tx_id, product_code, biller_comm_code, sentPaymentBillerModel.getMerchant_type(), bank_code, attempt);
                        }
                    } else if (code.equals(WebParams.LOGOUT_CODE)) {
                        AlertDialogLogout dialogLogout = AlertDialogLogout.getInstance();
                        dialogLogout.showDialoginActivity(getActivity(), sentPaymentBillerModel.getError_message());
                    } else if (code.equals(DefineValue.ERROR_9333)) {
                        Timber.d("isi response app data:" + sentPaymentBillerModel.getApp_data());
                        final AppDataModel appModel = sentPaymentBillerModel.getApp_data();
                        AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                        alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                    } else if (code.equals(DefineValue.ERROR_0066)) {
                        Timber.d("isi response maintenance:" + object.toString());
                        AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                        alertDialogMaintenance.showDialogMaintenance(getActivity(), sentPaymentBillerModel.getError_message());
                    } else {
                        code = sentPaymentBillerModel.getError_code() + " : " + sentPaymentBillerModel.getError_message();
                        if (isVisible()) {
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            getFragmentManager().popBackStack();
                        }
                        dismissProgressDialog();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    dismissProgressDialog();
                }

                @Override
                public void onComplete() {
                    btn_submit.setEnabled(true);
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void sentDataReqToken(String tx_id, String product_code, String biller_comm_code, String merchant_type, String bank_code, int attempt) {
        try {

            extraSignature = tx_id + biller_comm_code + product_code;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);
            params.put(WebParams.COMM_CODE, biller_comm_code);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params regtoken Sgo+:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS))
                                    showDialog(product_code, bank_code);
                                else if (merchant_type.equals(DefineValue.AUTH_TYPE_OTP))
                                    showDialog(product_code, bank_code);
                                else
                                    submitBiller(bank_code, product_code, attempt);

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(ErrorDefinition.WRONG_PIN_BILLER)) {
                                code = model.getError_message();
                                showDialogError(code);

                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {
                                String code_msg = model.getError_message();
                                switch (code) {
                                    case "0059":
                                        showDialogSMS(mTempBank.getBank_name());
                                        break;
                                    case ErrorDefinition.ERROR_CODE_LESS_BALANCE:
                                        String message_dialog = "\"" + code_msg + "\" \n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname));

                                        AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                                message_dialog, getString(R.string.ok), getString(R.string.cancel), false);
                                        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent mI = new Intent(getActivity(), TopUpActivity.class);
                                                mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                                                startActivityForResult(mI, REQUEST_BillerInqReq);
                                            }
                                        });
                                        dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                sentInquryBiller();
                                            }
                                        });
                                        dialog_frag.setTargetFragment(BillerInputPulsa.this, 0);
                                        dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
                                        break;
                                    default:
                                        code = model.getError_code() + " : " + model.getError_message();
                                        Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                        getFragmentManager().popBackStack();
                                        break;
                                }
                            }
                            dismissProgressDialog();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            dismissProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            btn_submit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void submitBiller(String bank_code, String product_code, int attempt) {
        isPIN = true;
        new UtilsLoader(getActivity(), sp).getFailedPIN(userPhoneID, new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {

            }

            @Override
            public void onFail(Bundle message) {

            }

            @Override
            public void onFailure(String message) {

            }
        });
        is_sgo_plus = mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB);
        if (is_sgo_plus) {
            changeToSgoPlus(bank_code, product_code);
        } else {
            if (isPIN) {
                CallPINinput(attempt);
                btn_submit.setEnabled(true);
            }
        }
    }

    private void switchFragment(Fragment i, String name, String next_name, Boolean isBackstack, String tag) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchContent(i, name, next_name, isBackstack, tag);
        et_payment_remark.setText("");
        if (buy_type_detail.equalsIgnoreCase("PRABAYAR"))
            spin_denom.setSelection(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setIs_input_amount(Boolean is_input_amount) {
        this.is_input_amount = is_input_amount;
    }

    private void showDialogSMS(final String _nama_bank) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        final LevelClass levelClass = new LevelClass(getActivity());
        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.topup_dialog_not_registered));
        Message.setText(getString(R.string.topup_not_registered, _nama_bank));
        btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));
        if (levelClass.isLevel1QAC())
            btnDialogOTP.setText(getString(R.string.ok));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!levelClass.isLevel1QAC()) {
                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME, _nama_bank);
                    switchActivity(newIntent);
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDialog(final String product_code, final String bank_code) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.smsBanking_dialog_validation_title));
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms));

        btnDialogOTP.setOnClickListener(view -> {
            submitBiller(bank_code, product_code, -1);

            dialog.dismiss();
        });


        dialog.show();
    }

    private void showDialogError(String message) {
        Dialog dialog = DefinedDialog.MessageDialog(getActivity(), getString(R.string.error),
                message,
                (v, isLongClick) -> getFragmentManager().popBackStack()
        );
        dialog.show();
    }

    private void switchActivity(Intent mIntent) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BillerInqReq)
            sentInquryBiller();
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                if (is_input_amount)
                    _amount = amount_desire;
                else
                    _amount = amount.toString();

                if (favoriteSwitch.isChecked()) {
                    onSaveToFavorite();
                } else {
                    sentInsertTransTopup(value_pin);
                }
            }
        }
    }

    private void setActionBarTitle(String _title) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    @SuppressLint("CheckResult")
    private void getBillerDenom() {
        Observable<JsonObject> observable1 = RetrofitService.getInstance().PostObjectRequest2(MyApiClient.LINK_GET_BILLER_DENOM, params("PLS"));
        Observable<JsonObject> observable2 = RetrofitService.getInstance().PostObjectRequest2(MyApiClient.LINK_GET_BILLER_DENOM, params("HP"));

        Observable.zip(observable1, observable2, new BiFunction<JsonObject, JsonObject, Object>() {
            @Override
            public Object apply(JsonObject jsonObject, JsonObject jsonObject2) throws Exception {
                Log.e(TAG, "BiFunction observe 1 : " + jsonObject.toString());
                Log.e(TAG, "BiFunction observe 2: " + jsonObject2.toString());

                Gson gson = new Gson();
                BillerDenomResponse response = gson.fromJson(jsonObject, BillerDenomResponse.class);
                BillerDenomResponse response2 = gson.fromJson(jsonObject2, BillerDenomResponse.class);

                realm2.beginTransaction();
                if (response.getErrorCode().equals(WebParams.SUCCESS_CODE)) {
                    for (BillerItem item : response.getBiller()) {
                        _data.add(item.getCommName());
                    }

                    billerItemList.addAll(response.getBiller());

                    realm2.copyToRealmOrUpdate(response.getBiller());
                    if (args.getString(DefineValue.CUST_ID, "") != "") {
                        et_payment_remark.setText(NoHPFormat.formatTo08(args.getString(DefineValue.CUST_ID, "")));

                        checkOperator();
                        if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
                            showChoosePayment();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                if (response2.getErrorCode().equals(WebParams.SUCCESS_CODE)) {
                    realm2.copyToRealmOrUpdate(response2.getBiller());
                } else {
                    Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                realm2.commitTransaction();

                if (_data.isEmpty()) {
                    initRealm();
                }

                return true;
            }
        }).subscribe();
    }

    private HashMap<String, Object> params(String billerTypeCode) {
        extraSignature = billerTypeCode;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_BILLER_DENOM, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.BILLER_TYPE, billerTypeCode);

        return params;
    }

    private void onSaveToFavorite() {
        showProgressDialog();
        extraSignature = cust_id + biller_type_code + "BIL";
        Log.e("extraSignature params ", extraSignature);
        String url = MyApiClient.LINK_TRX_FAVORITE_SAVE;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(url, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.PRODUCT_TYPE, biller_type_code);
        params.put(WebParams.CUSTOMER_ID, cust_id);
        params.put(WebParams.TX_FAVORITE_TYPE, "BIL");
        params.put(WebParams.COMM_ID, biller_comm_id);
        params.put(WebParams.NOTES, notesEditText.getText().toString());
        params.put(WebParams.DENOM_ITEM_ID, item_id);

        Log.e("params ", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(response.toString(), jsonModel.class);
                            Log.e("onResponses ", response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                sentInsertTransTopup(value_pin);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("onResponses ", throwable.getLocalizedMessage());
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        dismissProgressDialog();
                    }
                });
    }

    private void sentInsertTransTopup(String tokenValue) {
        try {
            showProgressDialog();

            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = tx_id + biller_comm_code + mTempBank.getProduct_code() + tokenValue;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, mTempBank.getProduct_code());
            params.put(WebParams.COMM_CODE, biller_comm_code);
            params.put(WebParams.COMM_ID, biller_comm_id);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, tokenValue, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject response) {
                            SentPaymentBillerModel model = getGson().fromJson(response, SentPaymentBillerModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                getTrxStatus(tx_id, biller_comm_id);
                                setResultActivity(MainPage.RESULT_BALANCE);

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {

                                code = model.getError_code() + " : " + model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                String message = model.getError_message();

                                if (isPIN && message.equals("PIN tidak sesuai")) {
                                    Intent i = new Intent(getActivity(), InsertPIN.class);

                                    attempt = model.getFailed_attempt();
                                    failed = model.getMax_failed();

                                    if (attempt != -1)
                                        i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                                } else {
                                    onOkButton();
                                }

                            }


                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            dismissProgressDialog();
                            btn_submit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void setResultActivity(int result) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.setResultActivity(result);
    }

    @Override
    public void onOkButton() {
        assert getFragmentManager() != null;
        getFragmentManager().popBackStackImmediate(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void CallPINinput(int _attempt) {
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private void changeToSgoPlus(String _bank_code, String _product_code) {
        Bundle args = getArguments();

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, _bank_code);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.COMMUNITY_CODE, biller_comm_code);
        i.putExtra(DefineValue.TX_ID, tx_id);
        i.putExtra(DefineValue.AMOUNT, amount);
        i.putExtra(DefineValue.API_KEY, biller_api_key);
        i.putExtra(DefineValue.CALLBACK_URL, callback_url);
        i.putExtra(DefineValue.COMMUNITY_ID, biller_comm_id);
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        i.putExtra(DefineValue.SHARE_TYPE, "");
        i.putExtra(DefineValue.DENOM_DATA, item_name);
        i.putExtra(DefineValue.BUY_TYPE, buy_type);
        i.putExtra(DefineValue.PAYMENT_NAME, payment_name);
        i.putExtra(DefineValue.BILLER_NAME, biller_comm_name);
        i.putExtra(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);
        i.putExtra(DefineValue.DESTINATION_REMARK, cust_id);
        i.putExtra(DefineValue.TOTAL_AMOUNT, totalAmount);

        if (buy_type == BillerActivity.PURCHASE_TYPE)
            i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.BIL_PURCHASE_TYPE);
        else
            i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.BIL_PAYMENT_TYPE);


        String _isi_amount_desired = "";

        if (is_input_amount) _isi_amount_desired = amount_desire;

        i.putExtra(DefineValue.AMOUNT_DESIRED, _isi_amount_desired);
        Timber.d("isi args:" + args.toString());
        btn_submit.setEnabled(true);

        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchActivity(i, MainPage.ACTIVITY_RESULT);
    }

    private void getTrxStatus(final String txId, String comm_id) {
        try {
            showProgressDialog();
            extraSignature = txId + comm_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            if (buy_type == BillerActivity.PURCHASE_TYPE)
                params.put(WebParams.TYPE, DefineValue.BIL_PURCHASE_TYPE);
            else
                params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.PRIVACY, "");
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject response) {
                            GetTrxStatusModel model = getGson().fromJson(response, GetTrxStatusModel.class);

                            String code = model.getError_code();

                            if (!model.getOn_error()) {
                                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                    String txstatus = model.getTx_status();
                                    showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""),
                                            sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                            txstatus, model);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                } else {
                                    String msg = model.getError_message();
                                    showDialog(msg);
                                }
                            } else {
                                Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            dismissProgressDialog();
                            btn_submit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showDialog(String msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.error));
        Message.setText(msg);

        btnDialogOTP.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showReportBillerDialog(String name, String userId, String txId, String itemName, String txStatus,
                                        GetTrxStatusModel model) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.getCreated()));
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        args.putInt(DefineValue.BUY_TYPE, buy_type);
        args.putString(DefineValue.PAYMENT_NAME, payment_name);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additional_fee));
        args.putString(DefineValue.DESTINATION_REMARK, NoHPFormat.formatTo62(cust_id));
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.getTx_remark());

        String _isi_amount_desired = "";
        if (is_input_amount)
            _isi_amount_desired = amount_desire;

        args.putString(DefineValue.DETAILS_BILLER, model.getProduct_name());

        if (_isi_amount_desired.isEmpty())
            args.putString(DefineValue.AMOUNT_DESIRED, _isi_amount_desired);
        else
            args.putString(DefineValue.AMOUNT_DESIRED, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_isi_amount_desired));

        if (model.getProduct_name() == null) {
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
            if (biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS))
                args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_BPJS);
            args.putString(DefineValue.BILLER_TYPE, biller_type_code);
        }

        args.putString(DefineValue.BILLER_DETAIL, toJson(model.getBiller_detail()).toString());
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.getBuss_scheme_name());
        args.putString(DefineValue.PRODUCT_NAME, model.getProduct_name());

        dialog.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }
}

