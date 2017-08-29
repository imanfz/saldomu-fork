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
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listbankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
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
public class SgoPlus_input extends Fragment implements EasyPermissions.PermissionCallbacks {
    final int RC_SEND_SMS = 13;
    private HashMap<String,String> listBankName;
    private HashMap<String,String> listBankProduct;
    private List<listbankModel> listDB;
    private ArrayList<String> BankProduct;
    private InformationDialog dialogI;

    private View v;
    private Button btn_subSGO;
    private Spinner spin_namaBank;
    private Spinner spin_produkBank;
    private EditText jumlahSGO_value;
    private String   memberID;
    private String topupType;
    private String nama_bank;
    private String userID;
    private String accessKey;
    private ProgressDialog progdialog;
    private ArrayAdapter<String> adapter3;
    private ImageView spinWheelBankName;
    private ImageView spinWheelBankProduct;
    private Animation frameAnimation;
    private Spinner sp_privacy;
    private int privacy;
    private boolean isSMSBanking = false;
    private SMSclass smSclass;
    private SMSDialog smsDialog;
    private SentObject sentObject;

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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_sgoplus_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Bundle args = getArguments();
        memberID = sp.getString(DefineValue.MEMBER_ID,"");
        topupType = args.getString(DefineValue.TRANSACTION_TYPE);

        spin_namaBank = (Spinner) v.findViewById(R.id.spinner_nameBank);
        spin_produkBank = (Spinner) v.findViewById(R.id.spinner_productBank);
        jumlahSGO_value = (EditText) v.findViewById(R.id.jumlahSGOplus_value);
        btn_subSGO = (Button) v.findViewById(R.id.btn_submit_sgoplus_input);
        spinWheelBankName = (ImageView) v.findViewById(R.id.spinning_wheel_bank_name);
        spinWheelBankProduct = (ImageView) v.findViewById(R.id.spinning_wheel_bank_product);
        sp_privacy = (Spinner) v.findViewById(R.id.payfriend_privacy_spinner);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        if(topupType.equals(DefineValue.SMS_BANKING)){
            isSMSBanking = true;
            dialogI = InformationDialog.newInstance(3);
            dialogI.setTargetFragment(this,0);
            initializeSmsClass();
            smsDialog = new SMSDialog(getActivity(), new SMSDialog.DialogButtonListener() {
                @Override
                public void onClickOkButton(View v, boolean isLongClick) {
                    if (EasyPermissions.hasPermissions(getActivity(),Manifest.permission.SEND_SMS)){
                        smsDialog.sentSms();
                    }
                    else {
                        EasyPermissions.requestPermissions(getActivity(), getString(R.string.rationale_send_sms),
                                RC_SEND_SMS, Manifest.permission.SEND_SMS);
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
                        smsDialog.dismiss();
                        smsDialog.reset();
                        sentDataReqToken(sentObject);
                    }
                }
            });
        }
        else {
            dialogI = InformationDialog.newInstance(2);
            dialogI.setTargetFragment(this,0);
        }

        InitializeSpinner();

        btn_subSGO.setOnClickListener(prosesSGOplusListener);
        jumlahSGO_value.addTextChangedListener(jumlahChangeListener);


    }

    private void initializeSmsClass(){
        smSclass = new SMSclass(getActivity());

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
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(requestCode == RC_SEND_SMS){
            smsDialog.sentSms();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if(requestCode == RC_SEND_SMS){
            Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
            if(progdialog.isShowing())
                progdialog.dismiss();
            if (smsDialog != null) {
                smsDialog.dismiss();
                smsDialog.reset();
            }
        }
    }



    private void InitializeSpinner() {

        try {
            JSONArray mData = new JSONArray(getArguments().getString(DefineValue.BANKLIST_DATA,""));

            ArrayList<String> bankName = new ArrayList<>();
            BankProduct = new ArrayList<>();
            listBankName = new HashMap<>();
            listDB = new ArrayList<>();

            for (int i = 0 ; i < mData.length(); i++) {
                JSONObject mObj = mData.getJSONObject(i);
                listbankModel mDObj = new listbankModel(mObj.getString(WebParams.BANK_CODE),
                                                        mObj.getString(WebParams.BANK_NAME),
                                                        mObj.getString(WebParams.PRODUCT_CODE),
                                                        mObj.getString(WebParams.PRODUCT_NAME),
                                                        mObj.getString(WebParams.PRODUCT_TYPE),
                                                        mObj.getString(WebParams.PRODUCT_H2H));
                listDB.add(mDObj);

                if(!bankName.contains(mDObj.getBank_name())) {
                    bankName.add(mDObj.getBank_name());
                    listBankName.put(mDObj.getBank_name(),mDObj.getBank_code());
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankName);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_namaBank.setAdapter(adapter);
            spin_namaBank.setOnItemSelectedListener(spinnerNamaBankListener);

            adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, BankProduct);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_produkBank.setAdapter(adapter3);

            ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.privacy_list, android.R.layout.simple_spinner_item);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_privacy.setAdapter(spinAdapter);
            sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

        } catch (JSONException e) {
            e.printStackTrace();
        }

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

    private TextWatcher jumlahChangeListener = new TextWatcher() {
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

    private Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {

            spin_produkBank.setVisibility(View.GONE);
            spinWheelBankProduct.setVisibility(View.VISIBLE);
            spinWheelBankProduct.startAnimation(frameAnimation);

            Object item = adapterView.getItemAtPosition(i);
            nama_bank = item.toString();
            Timber.d("nama bank:" + nama_bank);

            BankProduct.clear();
            listBankProduct  = new HashMap<>();

            Thread deproses = new Thread() {
            @Override
            public void run() {
                for (listbankModel aListDB : listDB) {
                    //Timber.d("isi semua", aListDB.getProduct_type()+"; "+aListDB.getProduct_name()+"; "+aListDB.getProduct_code());
                    if(aListDB.getBank_name().equals(nama_bank)){
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


    private Button.OnClickListener prosesSGOplusListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    sentDataValidTopup(listBankName.get(spin_namaBank.getSelectedItem().toString()),
                            listBankProduct.get(spin_produkBank.getSelectedItem().toString()),
                            spin_produkBank.getSelectedItem().toString()
                    );
                    //Log.d("bank name / bank produk ", listBankName.get(spin_namaBank.getSelectedItem().toString()) + " / " + listBankProduct.get(spin_produkBank.getSelectedItem().toString()));
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };



    private void sentDataValidTopup(final String bank_kode, String product_code, final String product_name){
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
                                sentObject = new SentObject();
                                sentObject.tx_id = response.getString(WebParams.TX_ID);
                                sentObject.product_code = response.getString(WebParams.PRODUCT_CODE);
                                sentObject.bank_kode = bank_kode;
                                sentObject.nama_bank = nama_bank;
                                sentObject.comm_code = response.getString(WebParams.COMM_CODE);
                                sentObject.fee = response.getString(WebParams.FEE);
                                sentObject.amount = amount;
                                sentObject.product_name = product_name;
                                sentObject.ccy_id = MyApiClient.CCY_VALUE;
                                smsDialog.show();
                            }
                            else {
                                progdialog.dismiss();
                                changeTopUpSgoPlus(response.getString(WebParams.TX_ID), response.getString(WebParams.PRODUCT_CODE),bank_kode
                                        ,product_name, response.getString(WebParams.COMM_CODE), response.getString(WebParams.FEE),
                                        MyApiClient.CCY_VALUE, nama_bank,amount,false,null);
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

    private void changeTopUpSgoPlus(String _tx_id, String _product_code, String bank_kode,String productBank_name,
                                    String _comm_code, String _fee, String _ccy_id, String _nama_bank,String _amount,
                                    Boolean isSmsBanking, String productValue) {

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

        if(isSmsBanking)
            mArgs.putString(DefineValue.PRODUCT_VALUE, productValue);

        newFrag.setArguments(mArgs);

        switchFragment(newFrag, getString(R.string.toolbar_title_topup),true);
        spin_namaBank.setSelection(0);
        spin_produkBank.setSelection(0);
        jumlahSGO_value.setText("");
    }

    private void sentDataReqToken(SentObject _sentObject){
        try{

            SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, _sentObject.comm_code);
            params.put(WebParams.TX_ID, _sentObject.tx_id);
            params.put(WebParams.PRODUCT_CODE, _sentObject.product_code);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PRODUCT_VALUE,_sentObject.productValue);


            Timber.d("isi params regtoken Sgo+:"+params.toString());

            MyApiClient.sentDataReqTokenSGOL(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response reqtoken :"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            sentObject.productValue = response.getString(WebParams.PRODUCT_VALUE);
                            sentObject.nama_bank = nama_bank;

                            showDialog(sentObject);
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
                                showDialogErrorSMS(nama_bank,code,response.optString(WebParams.ERROR_MESSAGE,""));
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

    private void showDialog(final SentObject _sentObject) {
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

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeTopUpSgoPlus(_sentObject.tx_id,_sentObject.product_code,_sentObject.bank_kode,
                        _sentObject.product_name,_sentObject.comm_code,_sentObject.fee, _sentObject.ccy_id,
                        _sentObject.nama_bank,_sentObject.amount,true,_sentObject.productValue);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDialogErrorSMS(final String _nama_bank, String error_code, String error_msg) {
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
        spin_namaBank.setSelection(0);
        spin_produkBank.setSelection(0);
        jumlahSGO_value.setText("");

        dialog.show();
    }


    private void switchActivity(Intent mIntent,int j){
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchActivity(mIntent,j);
    }



    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        hiddenKeyboard(getView());
        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }
    private void hiddenKeyboard(View v) {
        if(this.isAdded()) {
            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private boolean inputValidation(){
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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