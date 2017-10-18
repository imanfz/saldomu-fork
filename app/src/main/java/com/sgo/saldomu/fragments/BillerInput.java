package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.Beans.Denom_Data_Model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import timber.log.Timber;

/*
  Created by Administrator on 3/4/2015.
 */
public class BillerInput extends Fragment {

    public final static String TAG = "BILLER_INPUT";

    private String[] billerType = {  "PLS",  //pulsa  0
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
                                    "BPJS"}; //BILLER_TYPE_BPJS 17

    private View v;
    private View layout_denom;
    private View layout_month;
    private TextView tv_denom;
    private TextView tv_payment_remark;
    private TextView tv_month;
    private EditText et_payment_remark;
    private Spinner spin_denom;
    private Spinner spin_month;
    private Button btn_submit;
    private ImageView spinWheelDenom;
    private ImageView spinWheelMonth;
    private ProgressDialog progdialog;
    private Animation frameAnimation;
    private String biller_type_code;
    private String biller_comm_id;
    private String biller_comm_name;
    private String denom_item_id;
    String biller_api_key;
    private String biller_item_id;
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
    private Spinner sp_privacy;
    private int privacy;
    private String digitsListener ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private Realm realm;
    private ArrayAdapter<String> adapterDenom;
    private ArrayAdapter<String> adapterMonth;
    private String selectedMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        biller_type_code = args.getString(DefineValue.BILLER_TYPE,"");
        biller_comm_id = args.getString(DefineValue.COMMUNITY_ID,"");
        biller_comm_name = args.getString(DefineValue.COMMUNITY_NAME,"");
        biller_item_id = args.getString(DefineValue.BILLER_ITEM_ID,"");

        isToken = false;

        spin_denom = (Spinner) v.findViewById(R.id.spinner_billerinput_denom);
        tv_denom = (TextView) v.findViewById(R.id.billerinput_text_denom);
        tv_payment_remark = (TextView) v.findViewById(R.id.billerinput_text_payment_remark);
        et_payment_remark = (EditText) v.findViewById(R.id.payment_remark_billerinput_value);
        spinWheelDenom = (ImageView) v.findViewById(R.id.spinning_wheel_billerinput_denom);
        btn_submit = (Button) v.findViewById(R.id.btn_submit_billerinput);
        layout_denom = v.findViewById(R.id.billerinput_layout_denom);
        sp_privacy = (Spinner) v.findViewById(R.id.privacy_spinner);
        spin_month = (Spinner) v.findViewById(R.id.spinner_billerinput_month);
        tv_month = (TextView) v.findViewById(R.id.billerinput_text_month);
        spinWheelMonth = (ImageView) v.findViewById(R.id.spinning_wheel_billerinput_month);
        layout_month = v.findViewById(R.id.billerinput_layout_month);
        if(args.containsKey(DefineValue.BILLER_ID_NUMBER))
        {
            et_payment_remark.setText(args.getString(DefineValue.BILLER_ID_NUMBER));
        }
        btn_submit.setOnClickListener(submitInputListener);
        layout_denom.setVisibility(View.VISIBLE);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        realm = Realm.getInstance(RealmManager.BillerConfiguration);

        initializeLayout();
        initializeSpinnerDenom();

        realmListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {

                if(isVisible()){


                    initializeLayout();
                    initializeSpinnerDenom();

                    if(_denomData != null) {
                        Timber.d("Masuk realm listener denomdata isi");
                        _denomData.clear();
                        for (int i = 0; i < mListDenomData.size(); i++) {
                            _denomData.add(mListDenomData.get(i).getItem_name());
                        }

                        layout_denom.setVisibility(View.VISIBLE);
                        spin_denom.setVisibility(View.VISIBLE);
                        adapterDenom.notifyDataSetChanged();
                    }

                    if(progdialog != null && progdialog.isShowing()) {
                        progdialog.dismiss();
                    }
                }
            }};
        realm.addChangeListener(realmListener);
    }

    private void initializeLayout(){

        mBillerTypeData = new Biller_Type_Data_Model();
        mBillerTypeData = realm.where(Biller_Type_Data_Model.class).
                equalTo(WebParams.BILLER_TYPE_CODE,biller_type_code).
                findFirst();


        if(mBillerTypeData.getBiller_data_models().size() == 1){
            biller_comm_id = mBillerTypeData.getBiller_data_models().get(0).getComm_id();
            biller_comm_name = mBillerTypeData.getBiller_data_models().get(0).getComm_name();
            biller_item_id = mBillerTypeData.getBiller_data_models().get(0).getItem_id();
        }

        mBillerData = new Biller_Data_Model();
        mBillerData = realm.where(Biller_Data_Model.class).
                equalTo(WebParams.COMM_ID,biller_comm_id).
                equalTo(WebParams.COMM_NAME,biller_comm_name).
                equalTo(WebParams.DENOM_ITEM_ID,biller_item_id).
                findFirst();


        if(mBillerData == null || mBillerData.getItem_id().isEmpty() && mBillerData.getDenom_data_models().size() == 0){
            Timber.d("masukk sini kosong mbiller data");
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        }

        if(mBillerData != null)
            mListDenomData = realm.copyFromRealm(mBillerData.getDenom_data_models());


        String[] _buy_type = getResources().getStringArray(R.array.buy_vpi_title);

        if(biller_type_code.equals(billerType[17])){
            _monthData = new ArrayList<>();
            for(int i = 0 ; i < 12 ; i++) {
                _monthData.add(Integer.toString(i + 1));
            }
            adapterMonth = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _monthData);
            adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_month.setAdapter(adapterMonth);
            spin_month.setOnItemSelectedListener(spinnerMonthListener);
            spin_month.setVisibility(View.GONE);
            tv_month.setVisibility(View.GONE);
        }
        else {
            layout_month.setVisibility(View.GONE);
        }

        if(biller_type_code.equals(billerType[0])){
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
            et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type_code.equals(billerType[1])){
            buy_type = _buy_type[0];
            buy_code = BillerActivity.PURCHASE_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Listrik));
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
        else if(biller_type_code.equals(billerType[2])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_CC));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type_code.equals(billerType[6])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_PAM));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type_code.equals(billerType[7])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_Pulsa));
            et_payment_remark.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type_code.equals(billerType[8])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_PST));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
        else if(biller_type_code.equals(billerType[11])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_asuransi));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        else if(biller_type_code.equals(billerType[16])){
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            tv_payment_remark.setText(getString(R.string.billerinput_text_payment_remark_RMH));
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
        else {
            buy_type = _buy_type[1];
            buy_code = BillerActivity.PAYMENT_TYPE;
            et_payment_remark.setInputType(InputType.TYPE_CLASS_TEXT);
            et_payment_remark.setKeyListener(DigitsKeyListener.getInstance(digitsListener));
        }
    }

    private void initializeSpinnerDenom(){
        if(mListDenomData.size() > 0){
            _denomData = new ArrayList<>();
            adapterDenom = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, _denomData);
            adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_denom.setAdapter(adapterDenom);
            spin_denom.setOnItemSelectedListener(spinnerDenomListener);

            spin_denom.setVisibility(View.GONE);
            spinWheelDenom.setVisibility(View.VISIBLE);
            spinWheelDenom.startAnimation(frameAnimation);

            Thread deproses = new Thread(){
                @Override
                public void run() {
                    _denomData.clear();
                    for (int i = 0 ;i< mListDenomData.size();i++){
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

        }
        else {
            layout_denom.setVisibility(View.GONE);
            denom_item_id = mBillerData.getItem_id();
        }

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

    }


    private Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i+1;
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
            selectedMonth = Integer.toString(pos+1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitInputListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    if (biller_type_code.equals(billerType[0]))
                        final_payment_remark = NoHPFormat.formatTo62(String.valueOf(et_payment_remark.getText()));
                    else
                        final_payment_remark = String.valueOf(et_payment_remark.getText());
                    showDialog(final_payment_remark);
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private void showDialog(String _payment_remark) {


        Bundle mArgs = getArguments();
        mArgs.putString(DefineValue.CUST_ID, _payment_remark);
        mArgs.putString(DefineValue.ITEM_ID, denom_item_id);
        mArgs.putInt(DefineValue.BUY_TYPE, buy_code);
        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
        mArgs.putString(DefineValue.BILLER_TYPE, biller_type_code);
//        if(biller_type_code.equalsIgnoreCase(billerType[17]))
//            mArgs.putString(DefineValue.VALUE_ITEM_DATA, selectedMonth);

        Fragment mFrag = new BillerDesciption();
        mFrag.setArguments(mArgs);
        switchFragment(mFrag, BillerActivity.FRAG_BIL_INPUT, null, true,BillerDesciption.TAG);

    }

    private void switchFragment(android.support.v4.app.Fragment i, String name,String next_name, Boolean isBackstack, String tag){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity ) getActivity();
        fca.switchContent(i,name,next_name,isBackstack,tag);
        et_payment_remark.setText("");
        spin_denom.setSelection(0);
        spin_month.setSelection(0);
    }

    private boolean inputValidation(){
        if(et_payment_remark.getText().toString().length() == 0 || et_payment_remark.getText().toString().equals("0") || et_payment_remark.length() == 1){
            et_payment_remark.requestFocus();
            if(biller_type_code.equals(billerType[0]))
                et_payment_remark.setError(this.getString(R.string.regist1_validation_nohp));
            else
                et_payment_remark.setError(this.getString(R.string.billerinput_validation_payment_remark));
            return false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()>0)
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
        if(!realm.isInTransaction() && !realm.isClosed()) {
            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }
}