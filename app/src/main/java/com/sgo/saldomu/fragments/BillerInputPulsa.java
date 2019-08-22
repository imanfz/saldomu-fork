package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.Beans.Denom_Data_Model;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.PrefixOperatorValidator;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import timber.log.Timber;

public class BillerInputPulsa extends BaseFragment {
    private View v;
    private TextView tv_denom;
    private ImageView img_operator;
    private TextView tv_payment_remark;
    private EditText et_payment_remark;
    private Spinner spin_denom;
    private Button btn_submit;
    private RadioGroup radioGroup;
    private LinearLayout layout_add_fee;
    private LinearLayout layout_detail;
    private LinearLayout layout_denom;
    private LinearLayout layout_payment_method;

    private SecurePreferences sp;
    private String biller_type_code;
    private String biller_comm_id;
    private String biller_comm_name;
    private String biller_item_id;
    private String final_payment_remark;
    private String buy_type;
    private String buy_type_detail = "PRABAYAR";
    private int buy_code;

    private Realm realm;
    private Biller_Data_Model mBillerData;
    private List<Denom_Data_Model> mListDenomData;
    private Biller_Type_Data_Model mBillerType;
    private List<Biller_Data_Model> mListBillerData;
    private ArrayList<String> _data = new ArrayList<>();
    private ArrayList<String> _denomData;
    private ArrayAdapter<String> adapterDenom;
    private String denom_item_id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_input_new, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        biller_type_code = args.getString(DefineValue.BILLER_TYPE, "");
        realm = Realm.getInstance(RealmManager.BillerConfiguration);

        spin_denom = v.findViewById(R.id.billerinput_spinner_denom);
        tv_denom = v.findViewById(R.id.billerinput_text_denom);
        img_operator = v.findViewById(R.id.img_operator);
        tv_payment_remark = v.findViewById(R.id.billerinput_text_nomor_hp);
        et_payment_remark = v.findViewById(R.id.billerinput_et_nomor_hp);
        btn_submit = v.findViewById(R.id.btn_submit_billerinput);
        radioGroup = v.findViewById(R.id.billerinput_radio);
        layout_add_fee = v.findViewById(R.id.billerinput_layout_add_fee);
        layout_detail = v.findViewById(R.id.billerinput_layout_detail);
        layout_denom = v.findViewById(R.id.billerinput_layout_denom);
        layout_payment_method = v.findViewById(R.id.billerinput_layout_payment_method);

        btn_submit.setOnClickListener(submitInputListener);
        radioGroup.setOnCheckedChangeListener(radioListener);

        initLayout();
        initPrefixListener();
        initRealm();
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
                    PrefixOperatorValidator.OperatorModel BillerIdNumber = PrefixOperatorValidator.validation(getActivity(), string);

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
                                    if (_data.get(i).toLowerCase().contains(BillerIdNumber.prefix_name.toLowerCase())) {
                                        biller_comm_id = mListBillerData.get(i).getComm_id();
                                        biller_comm_name = mListBillerData.get(i).getComm_name();
                                        biller_item_id = mListBillerData.get(i).getItem_id();

                                        mBillerData = new Biller_Data_Model();
                                        mBillerData = realm.where(Biller_Data_Model.class).
                                                equalTo(WebParams.COMM_ID, biller_comm_id).
                                                equalTo(WebParams.COMM_NAME, biller_comm_name).
                                                equalTo(WebParams.DENOM_ITEM_ID, biller_item_id).
                                                findFirst();

                                        mListDenomData = realm.copyFromRealm(mBillerData.getDenom_data_models());

                                        initializeSpinnerDenom();
                                    }
                                }

                            }
                        } else if (buy_type_detail.equalsIgnoreCase("PASCABAYAR")) {
                            for (int i = 0; i < mListBillerData.size(); i++) {
                                Timber.d(mListBillerData.get(i).getComm_name());
                                if (mListBillerData.get(i).getComm_name().contains(BillerIdNumber.prefix_name)) {
                                    biller_comm_id = mListBillerData.get(i).getComm_id();
                                    biller_comm_name = mListBillerData.get(i).getComm_name();
                                    biller_item_id = mListBillerData.get(i).getItem_id();
                                    break;
                                }
                                if (mListBillerData.get(i).getComm_name().contains("Excelcomindo Xplor")){
                                    biller_comm_id = mListBillerData.get(i).getComm_id();
                                    biller_comm_name = mListBillerData.get(i).getComm_name();
                                    biller_item_id = mListBillerData.get(i).getItem_id();
                                    break;
                                }
                            }
                            if (BuildConfig.DEBUG&&BuildConfig.FLAVOR.equals("development")){
                                biller_comm_id = mListBillerData.get(0).getComm_id();
                                biller_comm_name = mListBillerData.get(0).getComm_name();
                                biller_item_id = mListBillerData.get(0).getItem_id();
                            }
                        }
                    }
                }
            }
        });
    }

    private void initializeSpinnerDenom() {
        if (mListDenomData.size() > 0) {
            _denomData = new ArrayList<>();
            adapterDenom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _denomData);
            adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_denom.setAdapter(adapterDenom);
            spin_denom.setOnItemSelectedListener(spinnerDenomListener);

            spin_denom.setVisibility(View.GONE);
//            spinWheelDenom.setVisibility(View.VISIBLE);
//            spinWheelDenom.startAnimation(frameAnimation);

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
//                            spinWheelDenom.clearAnimation();
//                            spinWheelDenom.setVisibility(View.GONE);
                            spin_denom.setVisibility(View.VISIBLE);
                            adapterDenom.notifyDataSetChanged();
                        }
                    });
                }
            };
            deproses.run();

        } else {
//            layout_denom.setVisibility(View.GONE);
            denom_item_id = mBillerData.getItem_id();
        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    private Spinner.OnItemSelectedListener spinnerDenomListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            denom_item_id = mListDenomData.get(i).getItem_id();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public void initLayout() {
        tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
        tv_denom.setText("Nominal Pulsa");
        et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        radioGroup.setVisibility(View.VISIBLE);
        layout_payment_method.setVisibility(View.GONE);
        layout_add_fee.setVisibility(View.GONE);
        layout_detail.setVisibility(View.GONE);
    }

    private RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioPrabayar:
                    layout_denom.setVisibility(View.VISIBLE);
                    buy_type_detail = "PRABAYAR";
                    biller_type_code = "PLS";
                    break;
                case R.id.radioPascabayar:
                    layout_denom.setVisibility(View.GONE);
                    buy_type_detail = "PASCABAYAR";
                    biller_type_code = "HP";
                    break;
            }
            initRealm();
        }
    };

    private Button.OnClickListener submitInputListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    final_payment_remark = NoHPFormat.formatTo62(String.valueOf(et_payment_remark.getText()));
                    showDialog(final_payment_remark);
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private boolean inputValidation() {
        if (et_payment_remark.getText().toString().length() == 0 || et_payment_remark.getText().toString().equals("0") || et_payment_remark.length() == 1) {
            et_payment_remark.requestFocus();
            et_payment_remark.setError(this.getString(R.string.regist1_validation_nohp));
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
        mBillerType = realm.where(Biller_Type_Data_Model.class).
                equalTo(WebParams.BILLER_TYPE_CODE, biller_type_code).
                findFirst();
        if (mBillerType != null) {
            mListBillerData = mBillerType.getBiller_data_models();
            _data.clear();
            for (int i = 0; i < mListBillerData.size(); i++) {
                _data.add(mListBillerData.get(i).getComm_name());
            }
        } else
            mListBillerData = new ArrayList<>();

//        mBillerTypePost = realm.where(Biller_Type_Data_Model.class)
//                .equalTo(WebParams.BILLER_TYPE_CODE, "HP")
//                .findFirst();
//        if (mBillerTypePost != null) {
//            mListBillerDataPost = mBillerTypePost.getBiller_data_models();
//            _data.clear();
//            for (int i = 0; i < mListBillerDataPost.size(); i++) {
//                _data.add(mListBillerDataPost.get(i).getComm_name());
//            }
//        } else
//            mListBillerDataPost = new ArrayList<>();
    }

    private void showDialog(String _payment_remark) {


        Bundle mArgs = getArguments();

        if (buy_type_detail.equalsIgnoreCase("PRABAYAR")) {
            mArgs.putString(DefineValue.CUST_ID, _payment_remark);
            mArgs.putString(DefineValue.ITEM_ID, denom_item_id);
            mArgs.putInt(DefineValue.BUY_TYPE, BillerActivity.PURCHASE_TYPE);
        } else{
            mArgs.putString(DefineValue.CUST_ID, et_payment_remark.getText().toString());
            mArgs.putString(DefineValue.ITEM_ID, biller_item_id);
            mArgs.putInt(DefineValue.BUY_TYPE, BillerActivity.PAYMENT_TYPE);
        }
        mArgs.putString(DefineValue.BILLER_TYPE, biller_type_code);
        mArgs.putString(DefineValue.COMMUNITY_ID, biller_comm_id);
        mArgs.putString(DefineValue.COMMUNITY_NAME, biller_comm_name);
//        if(biller_type_code.equalsIgnoreCase(billerType[17]))
//            mArgs.putString(DefineValue.VALUE_ITEM_DATA, "1");

        Fragment mFrag = new BillerDesciption();
        mFrag.setArguments(mArgs);
        switchFragment(mFrag, BillerActivity.FRAG_BIL_INPUT, null, true, BillerDesciption.TAG);

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
}

