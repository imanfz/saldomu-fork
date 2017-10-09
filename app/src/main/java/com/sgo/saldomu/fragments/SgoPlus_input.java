package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.R;
//import com.sgo.saldomu.activities.TagihanActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.dialogs.SMSDialog;
//import com.sgo.saldomu.widgets.CustomFacebookButton;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class SgoPlus_input extends Fragment implements EasyPermissions.PermissionCallbacks{
    private final static int RC_SENDSMS = 103;
    private HashMap<String,String> listBankName;
    private HashMap<String,String> listBankProduct;
    private List<listBankModel> listDB;
    private ArrayList<String> BankProduct;
    private InformationDialog dialogI;

    View v;
    Button btn_subSGO;
    Spinner spin_namaBank,spin_produkBank;
    EditText jumlahSGO_value;
    listBankModel listBankModel;
    String memberID, topupType,userID,accessKey, data, bank_name, product_name, bank_code, product_code, pairing_id, is_pairing;
    ProgressDialog progdialog;
    ArrayAdapter<String> adapter3;
    ImageView spinWheelBankName, spinWheelBankProduct;
    Animation frameAnimation;
    Spinner sp_privacy;
    int privacy;
    boolean isSMSBanking = false, isTagihan = false, isFacebook = false;
    private SMSDialog smsDialog;
    private SentObject sentObject;

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_SENDSMS:
                smsDialog.sentSms();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_SENDSMS:
                Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
                if (progdialog.isShowing())
                    progdialog.dismiss();
                if (smsDialog != null) {
                    smsDialog.dismiss();
                    smsDialog.reset();
                }
                break;
        }
    }
//    CustomFacebookButton facebookButton;

//    @Override
//    public void onSuccessLogin() {
//        isFacebook = true;
//        Timber.wtf("masuk Success login Facebook");
//    }
//
//    @Override
//    public void onFailedLogin() {
//        isFacebook = false;
//        Timber.wtf("masuk failed login Facebook");
//    }
//
//    @Override
//    public void onCheckedChange(Boolean isChecked) {
////        if(isChecked) {
////            v.findViewById(R.id.txtFacebookNote).setVisibility(View.VISIBLE);
////            isFacebook = true;
////        }else {
////            v.findViewById(R.id.txtFacebookNote).setVisibility(View.GONE);
////            isFacebook = false;
////        }
//    }

    private class SentObject{
        String tx_id;
        String product_code;
        String bank_kode;
        String product_name;
        String comm_code;
        String fee;
        String ccy_id;
        String nama_bank;
        String amount;
        String productValue;
        SentObject(){}
        public String getData(){
            String tempt = this.tx_id+"//"+
                            this.product_code+"//"+
                            this.bank_kode+"//"+
                            this.product_name+"//"+
                            this.comm_code+"//"+
                            this.fee+"//"+
                            this.ccy_id+"//"+
                            this.nama_bank+"//"+
                            this.amount+"//"+
                            this.productValue;
            return tempt;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Bundle args = getArguments();
        memberID = sp.getString(DefineValue.MEMBER_ID,"");

        if(sentObject == null)
            sentObject = new SentObject();
        if(args.containsKey(DefineValue.TAGIHAN))
            isTagihan = args.getBoolean(DefineValue.TAGIHAN, false);

//        if(isTagihan){
//            data = getArguments().getString(DefineValue.BANKLIST_DATA, "");
//            topupType = args.getString(DefineValue.TRANSACTION_TYPE);
//            pairing_id = args.getString(DefineValue.PAIRING_ID);
//            is_pairing = args.getString(DefineValue.IS_PAIRING);
//            if(topupType.equals(DefineValue.SMS_BANKING))
//                isSMSBanking = true;
//        }
        else {
            listBankModel = new listBankModel(
                    args.getString(DefineValue.BANK_CODE,""),
                    args.getString(DefineValue.BANK_NAME,""),
                    args.getString(DefineValue.PRODUCT_CODE,""),
                    args.getString(DefineValue.PRODUCT_NAME,""),
                    args.getString(DefineValue.PRODUCT_TYPE,""),
                    args.getString(DefineValue.PRODUCT_H2H,""));
            if(listBankModel.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS))
                isSMSBanking = true;
        }


        if(isSMSBanking){
            isSMSBanking = true;
            dialogI = InformationDialog.newInstance(this,3);

            initializeSmsClass();
            smsDialog = new SMSDialog(getActivity(), new SMSDialog.DialogButtonListener() {
                @Override
                public void onClickOkButton(View v, boolean isLongClick) {
                    if (EasyPermissions.hasPermissions(getActivity(),Manifest.permission.SEND_SMS)) {
                        smsDialog.sentSms();
                    }
                    else {
                        EasyPermissions.requestPermissions(SgoPlus_input.this,
                                getString(R.string.rational_sent_sms),
                                RC_SENDSMS, Manifest.permission.SEND_SMS);
                    }
                }

                @Override
                public void onClickCancelButton(View v, boolean isLongClick) {
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                }

                @Override
                public void onSuccess(int user_is_new) {

                }

                @Override
                public void onSuccess(String product_value) {
                    if(sentObject != null) {
                        sentObject.productValue = product_value;
                        Timber.d("onSuccess SMS verifikasi "+ sentObject.getData());
                        smsDialog.dismiss();
                        smsDialog.reset();
                        sentDataReqToken();
                    }
                }
            });
        }
        else
            dialogI = InformationDialog.newInstance(this,2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_sgoplus_input, container, false);
//        facebookButton = (CustomFacebookButton) v.findViewById(R.id.fb_button);
//        facebookButton.setFragment(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        InitializeData();


    }

    private void initializeSmsClass(){
        SMSclass smSclass = new SMSclass(getActivity());

        smSclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if(!isExist){
                    Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }
        });

        try{
            getActivity().unregisterReceiver(smSclass.simStateReceiver);
        }
        catch (Exception ignored){}
        getActivity().registerReceiver(smSclass.simStateReceiver,SMSclass.simStateIntentFilter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        if(getActivity() instanceof  TopUpActivity) {
            TopUpActivity fca = (TopUpActivity) getActivity();
            fca.setToolbarTitle(_title);
        }
//        else if(getActivity() instanceof TagihanActivity){
//            TagihanActivity fca = (TagihanActivity) getActivity();
//            fca.setToolbarTitle(_title);
//        }
    }

    private void InitializeData() {

        jumlahSGO_value = (EditText) v.findViewById(R.id.jumlahSGOplus_value);
        btn_subSGO = (Button) v.findViewById(R.id.btn_submit_sgoplus_input);
        sp_privacy = (Spinner) v.findViewById(R.id.payfriend_privacy_spinner);
        spin_namaBank = (Spinner) v.findViewById(R.id.spinner_nameBank);
        spin_produkBank = (Spinner) v.findViewById(R.id.spinner_productBank);
        String titleAb;
//            if(isTagihan){
//
//                spinWheelBankName = (ImageView) v.findViewById(R.id.spinning_wheel_bank_name);
//                spinWheelBankProduct = (ImageView) v.findViewById(R.id.spinning_wheel_bank_product);
//                frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
//                frameAnimation.setRepeatCount(Animation.INFINITE);
//
//                String amountTagihan = getArguments().getString(DefineValue.AMOUNT_TAGIHAN, "");
//                jumlahSGO_value.setText(amountTagihan);
//                jumlahSGO_value.setEnabled(false);
//
//                spin_namaBank.setVisibility(View.VISIBLE);
//                spin_produkBank.setVisibility(View.VISIBLE);
//
//                JSONArray mData = new JSONArray(getArguments().getString(DefineValue.BANKLIST_DATA,""));
//
//                ArrayList<String> bankName = new ArrayList<>();
//                BankProduct = new ArrayList<>();
//                listBankName = new HashMap<>();
//                listDB = new ArrayList<>();
//
//                for (int i = 0 ; i < mData.length(); i++) {
//                    JSONObject mObj = mData.getJSONObject(i);
//                    listBankModel mDObj = new listBankModel(mObj.getString(WebParams.BANK_CODE),
//                            mObj.getString(WebParams.BANK_NAME),
//                            mObj.getString(WebParams.PRODUCT_CODE),
//                            mObj.getString(WebParams.PRODUCT_NAME),
//                            mObj.getString(WebParams.PRODUCT_TYPE),
//                            mObj.getString(WebParams.PRODUCT_H2H));
//                    listDB.add(mDObj);
//
//                    if(!bankName.contains(mDObj.getBank_name())) {
//                        bankName.add(mDObj.getBank_name());
//                        listBankName.put(mDObj.getBank_name(),mDObj.getBank_code());
//                    }
//
//                }
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankName);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spin_namaBank.setAdapter(adapter);
//                spin_namaBank.setOnItemSelectedListener(spinnerNamaBankListener);
//
//                adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, BankProduct);
//                adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spin_produkBank.setAdapter(adapter3);
//
//                if(isSMSBanking)
//                    titleAb = getString(R.string.smsBanking_ab_title);
//                else
//                    titleAb = getString(R.string.internetBanking_ab_title);
//            }
//            else {

        spin_namaBank.setVisibility(View.GONE);
        spin_produkBank.setVisibility(View.GONE);
        TextView productname = (TextView) v.findViewById(R.id.text_productbank);
        TextView bankname = (TextView) v.findViewById(R.id.text_namebank);

        bank_name = listBankModel.getBank_name();
        bank_code = listBankModel.getBank_code();
        product_name= listBankModel.getProduct_name();
        product_code = listBankModel.getProduct_code();

        productname.setText(product_name);
        bankname.setText(bank_name);
        titleAb = getString(R.string.topuplist_ab_title)+" - "+ bank_name;
//            }

        setActionBarTitle(titleAb);

        btn_subSGO.setOnClickListener(prosesSGOplusListener);
        jumlahSGO_value.addTextChangedListener(jumlahChangeListener);

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

    }

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        facebookButton.callBackFacebook(requestCode, resultCode, data);
    }

    Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i+1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("0"))jumlahSGO_value.setText("");
            if(s.length() > 0 && s.charAt(0) == '0'){
                int i = 0;
                for (; i < s.length(); i++){
                    if(s.charAt(i) != '0')break;
                }
                jumlahSGO_value.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {

            spin_produkBank.setVisibility(View.GONE);
            spinWheelBankProduct.setVisibility(View.VISIBLE);
            spinWheelBankProduct.startAnimation(frameAnimation);

            Object item = adapterView.getItemAtPosition(i);
            sentObject.nama_bank = item.toString();
            Timber.d("nama bank:" + sentObject.nama_bank);

            BankProduct.clear();
            listBankProduct  = new HashMap<>();

            Thread deproses = new Thread() {
            @Override
            public void run() {
                for (listBankModel aListDB : listDB) {
                    //Timber.d("isi semua", aListDB.getProduct_type()+"; "+aListDB.getProduct_name()+"; "+aListDB.getProduct_code());
                    if(aListDB.getBank_name().equals(sentObject.nama_bank)){
                        //Timber.d("isi product name", aListDB.getProduct_name());
                        BankProduct.add(aListDB.getProduct_name());
                        listBankProduct.put(aListDB.getProduct_name(),aListDB.getProduct_code());
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinWheelBankProduct.clearAnimation();
                        spinWheelBankProduct.setVisibility(View.GONE);
                        spin_produkBank.setVisibility(View.VISIBLE);
                        adapter3.notifyDataSetChanged();
                    }
                });
            }
            };
            deproses.start();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    Button.OnClickListener prosesSGOplusListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    if(isTagihan) {
//                        changeTagihanPreview(listBankName.get(spin_namaBank.getSelectedItem().toString()),
//                                spin_namaBank.getSelectedItem().toString(),
//                                listBankProduct.get(spin_produkBank.getSelectedItem().toString()),
//                                spin_produkBank.getSelectedItem().toString());
                    }
                    else {
                        sentDataValidTopup(bank_code,
                                bank_name,
                                product_code,
                                product_name);
                    }
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };



    public void sentDataValidTopup(final String bank_kode, final String bank_name, String product_code, final String product_name){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final String amount = String.valueOf(jumlahSGO_value.getText());

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_VALID_TOPUP,
                    userID,accessKey);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.BANK_CODE, bank_kode);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, "");
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sgoplusinput:"+params.toString());

            MyApiClient.sentValidTopUp(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response sgoplusinput:"+response.toString());
                            if(isSMSBanking) {
                                Timber.d("isi product_name "+ product_name);
                                if(sentObject == null)
                                    sentObject = new SentObject();
                                sentObject.tx_id = response.getString(WebParams.TX_ID);
                                sentObject.product_code = response.getString(WebParams.PRODUCT_CODE);
                                sentObject.bank_kode = bank_kode;
                                sentObject.nama_bank = bank_name;
                                sentObject.comm_code = response.getString(WebParams.COMM_CODE);
                                sentObject.fee = response.getString(WebParams.FEE);
                                sentObject.amount = amount;
                                sentObject.product_name = product_name;
                                sentObject.ccy_id = MyApiClient.CCY_VALUE;
                                Timber.d("Valid topup "+ sentObject.getData());
                                smsDialog.show();
                            }
                            else {
                                progdialog.dismiss();
                                changeTopUpSgoPlus(response.getString(WebParams.TX_ID), response.getString(WebParams.PRODUCT_CODE),bank_kode
                                        ,product_name, response.getString(WebParams.COMM_CODE), response.getString(WebParams.FEE),
                                        MyApiClient.CCY_VALUE, bank_name,amount,false,null);
                            }
                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            Timber.d("Error ListMember comlist:"+response.toString());
                            code = response.getString(WebParams.ERROR_CODE)+":"+response.getString(WebParams.ERROR_MESSAGE);
                            progdialog.dismiss();
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable){
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi valid topup sgo input:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

//    private void changeTagihanPreview(String _bank_kode, String _bank_name, String _product_code, String _product_name) {
//
//        getActivity().getSupportFragmentManager().popBackStack();
//        Fragment newFrag = new PreviewTagihanViaBank();
//        Bundle mArgs = new Bundle();
//
//        mArgs.putString(DefineValue.TOPUP_TYPE, DefineValue.EMONEY);
//        mArgs.putString(DefineValue.PRODUCT_CODE,_product_code);
//        mArgs.putString(DefineValue.PRODUCT_NAME,_product_name);
//        mArgs.putString(DefineValue.COMMUNITY_ID,  getArguments().getString(DefineValue.COMMUNITY_ID, ""));
//        mArgs.putString(DefineValue.COMMUNITY_CODE, getArguments().getString(DefineValue.COMMUNITY_CODE, ""));
//        mArgs.putString(DefineValue.COMMUNITY_NAME, getArguments().getString(DefineValue.COMMUNITY_NAME, ""));
//        mArgs.putString(DefineValue.CALLBACK_URL, getArguments().getString(DefineValue.CALLBACK_URL, ""));
//        mArgs.putString(DefineValue.API_KEY, getArguments().getString(DefineValue.API_KEY, ""));
//        mArgs.putString(DefineValue.BANK_CODE, _bank_kode);
//        mArgs.putString(DefineValue.BANK_NAME, _bank_name);
//        mArgs.putString(DefineValue.INVOICES, getArguments().getString(DefineValue.INVOICES, ""));
//        mArgs.putString(DefineValue.AMOUNT, getArguments().getString(DefineValue.AMOUNT_TAGIHAN, ""));
//        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
//        mArgs.putBoolean(DefineValue.IS_SMS_BANKING, isSMSBanking);
//        mArgs.putString(DefineValue.IS_PAIRING, is_pairing);
//        mArgs.putString(DefineValue.PAIRING_ID, pairing_id);
//
//        newFrag.setArguments(mArgs);
//
//        switchTagihanFragment(newFrag, getString(R.string.toolbar_title_sekolahku),true);
//        if(isTagihan) {
//            spin_namaBank.setSelection(0);
//            spin_produkBank.setSelection(0);
//        }
//        else {
//            jumlahSGO_value.setText("");
//        }
//    }

    private void changeTopUpSgoPlus(String _tx_id, String _product_code, String bank_kode,String productBank_name,
                                    String _comm_code, String _fee, String _ccy_id, String _nama_bank,String _amount,
                                    Boolean isSmsBanking, String productValue) {
        Timber.d("change topup bankname " + sentObject.getData());
        Fragment newFrag = new TopUpToken();
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.TOPUP_TYPE, DefineValue.EMONEY);
        mArgs.putString(DefineValue.TX_ID,_tx_id);
        mArgs.putString(DefineValue.PRODUCT_CODE,_product_code);
        mArgs.putString(DefineValue.PRODUCT_NAME,productBank_name);
        mArgs.putString(DefineValue.COMMUNITY_CODE, _comm_code);
        mArgs.putString(DefineValue.BANK_CODE, bank_kode);
        mArgs.putString(DefineValue.BANK_NAME, _nama_bank);
        mArgs.putString(DefineValue.CCY_ID, _ccy_id);
        mArgs.putString(DefineValue.FEE, _fee);
        mArgs.putBoolean(DefineValue.IS_SMS_BANKING,isSmsBanking);
        mArgs.putString(DefineValue.AMOUNT, _amount);
        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
		mArgs.putBoolean(DefineValue.IS_FACEBOOK,isFacebook);

        if(isSmsBanking)
            mArgs.putString(DefineValue.PRODUCT_VALUE, productValue);

        newFrag.setArguments(mArgs);

        switchFragment(newFrag, getString(R.string.toolbar_title_topup),true);
        if(isTagihan) {
//            spin_namaBank.setSelection(0);
//            spin_produkBank.setSelection(0);
        }
        else {
            jumlahSGO_value.setText("");
        }
    }

    public void sentDataReqToken(){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, sentObject.comm_code);
            params.put(WebParams.TX_ID, sentObject.tx_id);
            params.put(WebParams.PRODUCT_CODE, sentObject.product_code);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PRODUCT_VALUE,sentObject.productValue);


            Timber.d("isi params regtoken Sgo+:"+params.toString());

            MyApiClient.sentDataReqTokenSGOL(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response reqtoken :"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            sentObject.productValue = response.getString(WebParams.PRODUCT_VALUE);
                            Timber.d("reqToken Sgol "+ sentObject.getData());
                            showDialog();
                            progdialog.dismiss();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            progdialog.dismiss();
                            if(code.equals("0059")||code.equals("0164")){
                                showDialogErrorSMS(sentObject.nama_bank,code,response.optString(WebParams.ERROR_MESSAGE,""));
                            }
                            else {
                                code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable){
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi reg token sgo input:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialog() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.appname)+" "+getString(R.string.dialog_token_message_sms));
        Timber.d("showdialog  "+ sentObject.getData());
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeTopUpSgoPlus(sentObject.tx_id,
                        sentObject.product_code,
                        sentObject.bank_kode,
                        sentObject.product_name,
                        sentObject.comm_code,
                        sentObject.fee,
                        sentObject.ccy_id,
                        sentObject.nama_bank,
                        sentObject.amount,
                        true,
                        sentObject.productValue);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void showDialogErrorSMS(final String _nama_bank, String error_code, String error_msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.topup_dialog_not_registered));
        if(error_code.equals("0059")){
//            Message.setText(getString(R.string.topup_not_registered,_nama_bank));
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME,_nama_bank);
                    switchActivity(newIntent, MainPage.ACTIVITY_RESULT);

                    dialog.dismiss();
                }
            });
        }
        else if(error_code.equals("0164")) {
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.close));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
        }
        if(isTagihan) {
//            spin_namaBank.setSelection(0);
//            spin_produkBank.setSelection(0);
        }
        else {
            jumlahSGO_value.setText("");
        }

        dialog.show();
    }


    private void switchActivity(Intent mIntent,int j){
        if (getActivity() == null)
            return;

        if(getActivity() instanceof  TopUpActivity) {
            TopUpActivity fca = (TopUpActivity) getActivity();
            fca.switchActivity(mIntent,j);
        }
//        else if(getActivity() instanceof TagihanActivity){
//            TagihanActivity fca = (TagihanActivity) getActivity();
//            fca.switchActivity(mIntent,j);
//        }
    }

//    private void switchTagihanFragment(Fragment i, String name, Boolean isBackstack){
//        if (getActivity() == null)
//            return;
//
//        hiddenKeyboard(getView());
//        TagihanActivity fca = (TagihanActivity) getActivity();
//        fca.switchContent(i, name, isBackstack);
//    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        hiddenKeyboard(getView());

        if(getActivity() instanceof  TopUpActivity) {
            TopUpActivity fca = (TopUpActivity) getActivity();
            fca.switchContent(i, name, isBackstack);
        }
//        else if(getActivity() instanceof TagihanActivity){
//            TagihanActivity fca = (TagihanActivity) getActivity();
//            fca.switchContent(i, name, isBackstack);
//        }
    }
    private void hiddenKeyboard(View v) {
        if(this.isAdded()) {
            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public boolean inputValidation(){
        if(jumlahSGO_value.getText().toString().length()==0){
            jumlahSGO_value.requestFocus();
            jumlahSGO_value.setError(this.getString(R.string.sgoplus_validation_jumlahSGOplus));
            return false;
        }
        else if(Long.parseLong(jumlahSGO_value.getText().toString()) < 1){
            jumlahSGO_value.requestFocus();
            jumlahSGO_value.setError(getString(R.string.payfriends_amount_zero));
            return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!isTagihan)
            inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(smsDialog != null) {
            smsDialog.DestroyDialog();
            if(this.isVisible())
                smsDialog.dismiss();
        }
    }
}