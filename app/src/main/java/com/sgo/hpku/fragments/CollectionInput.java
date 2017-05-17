package com.sgo.hpku.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.sgo.hpku.Beans.listbankModel;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.CollectionActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.RegisterSMSBankingActivity;
import com.sgo.hpku.activities.TopUpActivity;
import com.sgo.hpku.coreclass.*;
import com.sgo.hpku.dialogs.AlertDialogFrag;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 6/12/2015.
 */
public class CollectionInput extends Fragment {

    private SecurePreferences sp;
    private List<String> listProductName;
    private HashMap<String,String> listBankProduct;
    private List<listbankModel> listDB;

    private View v;
    private Button btn_subSGO;
    private Spinner spin_namaBank;
    private Spinner spin_produkBank;
    private EditText et_amount;
    private EditText et_remark;
    private String topupType;
    private String nama_bank;
    private String userID;
    private String accessKey;
    private Bundle args;
    private ProgressDialog progdialog;
    private ArrayAdapter<String> adapter3;
    private ImageView spinWheelBankName;
    private ImageView spinWheelBankProduct;
    private Animation frameAnimation;
    private Spinner sp_privacy;
    private int privacy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_collection_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        args = getArguments();
        topupType = args.getString(DefineValue.TRANSACTION_TYPE);
        String dataJson = args.getString(DefineValue.BANKLIST_DATA);

        spin_namaBank = (Spinner) v.findViewById(R.id.spinner_nameBank);
        spin_produkBank = (Spinner) v.findViewById(R.id.spinner_productBank);
        et_amount = (EditText) v.findViewById(R.id.collectinput_jumlah_value);
        et_remark = (EditText) v.findViewById(R.id.collectinput_remark_value);
        btn_subSGO = (Button) v.findViewById(R.id.btn_submit_sgoplus_input);
        spinWheelBankName = (ImageView) v.findViewById(R.id.spinning_wheel_bank_name);
        spinWheelBankProduct = (ImageView) v.findViewById(R.id.spinning_wheel_bank_product);
        sp_privacy = (Spinner) v.findViewById(R.id.privacy_spinner);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        btn_subSGO.setOnClickListener(prosesListener);
        et_amount.addTextChangedListener(jumlahChangeListener);

        try {
            JSONArray mArrayData = new JSONArray(dataJson);
            InitializeSpinner(mArrayData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void InitializeSpinner(JSONArray mData) {

        String[] bankName = new String[mData.length()];

        listDB = new ArrayList<>();

        for(int i = 0; i < mData.length() ; i++){
            try {
                listbankModel mOb = new listbankModel(mData.getJSONObject(i).getString(WebParams.BANK_CODE),
                                                      mData.getJSONObject(i).getString(WebParams.BANK_NAME),
                                                      mData.getJSONObject(i).getString(WebParams.PRODUCT_CODE),
                                                      mData.getJSONObject(i).getString(WebParams.PRODUCT_NAME),
                                                      mData.getJSONObject(i).getString(WebParams.PRODUCT_TYPE),
                                                      mData.getJSONObject(i).getString(WebParams.PRODUCT_H2H));
                listDB.add(mOb);
                bankName[i] = mOb.getBank_name();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_namaBank.setAdapter(adapter);
        spin_namaBank.setOnItemSelectedListener(spinnerNamaBankListener);

        listProductName = new ArrayList<>();
        adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listProductName);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_produkBank.setAdapter(adapter3);


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

    private TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("0")) et_amount.setText("");
            if(s.length() > 0 && s.charAt(0) == '0'){
                int i = 0;
                for (; i < s.length(); i++){
                    if(s.charAt(i) != '0')break;
                }
                et_amount.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            spin_produkBank.setVisibility(View.GONE);
            spinWheelBankProduct.setVisibility(View.VISIBLE);
            spinWheelBankProduct.startAnimation(frameAnimation);

            Object item = adapterView.getItemAtPosition(i);
            nama_bank = item.toString();

            listProductName.clear();
            listBankProduct = new HashMap<>();

            Thread deproses = new Thread() {
                @Override
                public void run() {
                    for (listbankModel mOb : listDB) {
                        if(mOb.getBank_name().equals(nama_bank)){
                            listProductName.add(mOb.getProduct_name());
                            listBankProduct.put(mOb.getProduct_name(),mOb.getProduct_code());
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinWheelBankProduct.clearAnimation();
                            spinWheelBankProduct.setVisibility(View.GONE);
                            spin_produkBank.setVisibility(View.VISIBLE);
                            adapter3.notifyDataSetChanged();
                            if(topupType.equals(DefineValue.EMONEY)){
                                View LayoutBankName = v.findViewById(R.id.layout_bank_name);
                                LayoutBankName.setVisibility(View.GONE);
                            }
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


    private Button.OnClickListener prosesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    sentValidTopupCollection(args.getString(DefineValue.COMMUNITY_ID, ""),
                            listDB.get(spin_namaBank.getSelectedItemPosition()).getBank_code(),
                            listBankProduct.get(spin_produkBank.getSelectedItem().toString()),
                            String.valueOf(et_amount.getText()),
                            String.valueOf(et_remark.getText())
                    );
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    private void sentValidTopupCollection(String _comm_id, String _bank_code, String _product_code, String _amount,
                                          final String _payment_remark){
        try{

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = MyApiClient.getSignatureWithParams(_comm_id,MyApiClient.LINK_TOP_UP_ACCOUNT_COLLECTION,
                    userID,accessKey);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, _comm_id);
            params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID,""));
            params.put(WebParams.BANK_CODE, _bank_code);
            params.put(WebParams.PRODUCT_CODE, _product_code);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, _amount);
            params.put(WebParams.PAYMENT_REMARK, _payment_remark);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());

            params.put(WebParams.MERCHANT_CODE, sp.getString(DefineValue.COMMUNITY_CODE, ""));
            params.put(WebParams.COMM_CODE, args.getString(DefineValue.COMMUNITY_CODE, ""));

            if(topupType.equals(DefineValue.BANKLIST_TYPE_IB))
                params.put(WebParams.PRODUCT_TYPE, DefineValue.BANKLIST_TYPE_IB);
            else if(topupType.equals(DefineValue.BANKLIST_TYPE_SMS))
                params.put(WebParams.PRODUCT_TYPE, DefineValue.BANKLIST_TYPE_SMS);
            else
                params.put(WebParams.PRODUCT_TYPE, DefineValue.BANKLIST_TYPE_EMO);

            Timber.d("isi params Valid TopupCollection:" + params.toString());

            MyApiClient.sentTopUpAccountCollection(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Timber.w("isi response validTopupCollection:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if(topupType.equals(DefineValue.INTERNET_BANKING)) {
                                changeToDescription(response.getString(WebParams.TX_ID),
                                        response.getString(WebParams.PRODUCT_CODE),
                                        response.getString(WebParams.PRODUCT_NAME),
                                        response.getString(WebParams.AMOUNT),
                                        _payment_remark,
                                        MyApiClient.CCY_VALUE,
                                        response.getString(WebParams.BANK_NAME),
                                        response.getString(WebParams.BANK_CODE),
                                        response.getString(WebParams.FEE),
                                        response.optString(WebParams.AUTH_TYPE,"")
                                );
                                progdialog.dismiss();
                            }
                            else {
                                sentDataReqToken(response.getString(WebParams.TX_ID),
                                        response.getString(WebParams.PRODUCT_CODE),
                                        response.getString(WebParams.PRODUCT_NAME),
                                        response.getString(WebParams.AMOUNT),
                                        _payment_remark,
                                        MyApiClient.CCY_VALUE,
                                        response.getString(WebParams.BANK_NAME),
                                        response.getString(WebParams.BANK_CODE),
                                        response.getString(WebParams.FEE),
                                        response.optString(WebParams.AUTH_TYPE,"")
                                );
                            }
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            Timber.d("Error ListMember comlist:"+response.toString());
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            progdialog.dismiss();
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
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi valid topup collect input:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void sentDataReqToken(final String _tx_id, final String _product_code, final String _product_name, final String _amount,
                                  final String _payment_remark, final String _ccy_value, final String _bank_name, final String _bank_code,
                                  final String _fee, final String auth_type){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, args.getString(DefineValue.COMMUNITY_CODE, ""));
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.PRODUCT_CODE, _product_code);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);


            Timber.d("isi params regtoken Collection:"+params.toString());

            MyApiClient.sentDataReqTokenSGOL(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response reqtoken collection  :"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if (topupType.equals(DefineValue.SMS_BANKING) || auth_type.equals(DefineValue.AUTH_TYPE_OTP))
                                showDialog(_tx_id, _product_code, _product_name, response.getString(WebParams.PRODUCT_VALUE), _fee,
                                        _bank_code, _bank_name, _amount, auth_type);
                            else if (auth_type.equals(DefineValue.AUTH_TYPE_PIN))
                                changeToDescription(_tx_id, _product_code, _product_name, _amount,
                                        _payment_remark, _ccy_value, _bank_name, _bank_code, _fee, auth_type);
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
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(ErrorDefinition.ERROR_CODE_UNREGISTERED_SMS_BANKING)) {
                                showDialogSMS(nama_bank);
                            }
                            else if(code.equals(ErrorDefinition.ERROR_CODE_LESS_BALANCE)){
                                String message_dialog = "\""+code_msg+"\" \n"+getString(R.string.dialog_message_less_balance);

                                AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                        message_dialog,getString(R.string.ok),getString(R.string.cancel),false);
                                dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent mI = new Intent(getActivity(), TopUpActivity.class);
                                        mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                                        switchActivity(mI, MainPage.ACTIVITY_RESULT);
                                    }
                                });
                                dialog_frag.setTargetFragment(CollectionInput.this, 0);
                                dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
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
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi req token collect input:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialogSMS(final String _nama_bank) {
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

        final LevelClass levelClass = new LevelClass(getActivity());

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.topup_dialog_not_registered));
        Message.setText(getString(R.string.topup_not_registered, _nama_bank));
        btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));

        if(levelClass.isLevel1QAC())
            btnDialogOTP.setText(getString(R.string.ok));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!levelClass.isLevel1QAC()) {
                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME, _nama_bank);
                    switchActivity(newIntent, MainPage.ACTIVITY_RESULT);
                }

                dialog.dismiss();
            }
        });

        spin_namaBank.setSelection(0);
        spin_produkBank.setSelection(0);
        et_amount.setText("");

        dialog.show();
    }

    private void showDialog(final String _tx_id, final String _product_code, final String _product_name, final String _product_value,
                            final String _fee, final String _bank_code, final String _bank_name, final String _amount, final String auth_type) {
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
                changeToDescription(_tx_id, _product_code, _product_name, _amount,
                        String.valueOf(et_remark.getText()), MyApiClient.CCY_VALUE,
                        _bank_name, _bank_code, _fee, auth_type);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeToDescription(String _tx_id, String _product_code, String _product_name, String _amount, String _remark,
                                     String _ccy_id, String _bank_name, String _bank_code, String _fee, String _auth_type){

        Fragment newFrag = new CollectionDescription();
        Bundle mArgs = getArguments();
        mArgs.putString(DefineValue.TX_ID,_tx_id);
        mArgs.putString(DefineValue.PRODUCT_CODE,_product_code);
        mArgs.putString(DefineValue.PRODUCT_NAME,_product_name);
        mArgs.putString(DefineValue.AMOUNT, _amount);
        mArgs.putString(DefineValue.REMARK, _remark);
        mArgs.putString(DefineValue.CCY_ID, _ccy_id);
        mArgs.putString(DefineValue.BANK_NAME, _bank_name);
        mArgs.putString(DefineValue.BANK_CODE, _bank_code);
        mArgs.putString(DefineValue.FEE, _fee);
        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
        mArgs.putString(DefineValue.AUTHENTICATION_TYPE, _auth_type);

        newFrag.setArguments(mArgs);

        switchFragment(newFrag, null, true);

        et_remark.setText("");
        et_amount.setText("");
        spin_namaBank.setSelection(0);
        spin_produkBank.setSelection(0);
    }


    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        hiddenKeyboard(getView());
        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private void switchActivity(Intent mIntent,int j){
        if (getActivity() == null)
            return;

        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.switchActivity(mIntent, j);
    }

    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private boolean inputValidation(){
        if(et_amount.getText().toString().length()==0){
            et_amount.requestFocus();
            et_amount.setError(this.getString(R.string.sgoplus_validation_jumlahSGOplus));
            return false;
        }
        else if(Long.parseLong(et_amount.getText().toString()) < 1){
            et_amount.requestFocus();
            et_amount.setError(getString(R.string.payfriends_amount_zero));
            return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}