package com.sgo.orimakardaya.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.InsertPIN;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.PulsaAgentActivity;
import com.sgo.orimakardaya.activities.PulsaAgentWeb;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.ReportBillerDialog;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 9/15/2015.
 */
public class PulsaAgentConfirm extends Fragment implements ReportBillerDialog.OnDialogOkCallback{
    View v;
    TextView tv_operator_value,tv_nominal_value,tv_amount_value,tv_phone_number, tv_payment_name, tv_fee_value, tv_total_amount_value;
    EditText et_token_value;
    Button btn_submit,btn_cancel;
    SecurePreferences sp;
    ProgressDialog progdialog;

    String tx_id,ccy_id,amount,item_name,cust_id, payment_name,fee,total_amount, merchant_type, phone_number, shareType, bank_code,
            product_code,product_payment_type, api_key, callback_url, comm_code, comm_id, product_value, operator_id, operator_name,
            userID,accessKey;
    Boolean is_sgo_plus, isPIN;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_pulsa_agent_confirm, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = new SecurePreferences(getActivity());
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        tv_phone_number = (TextView) v.findViewById(R.id.pulsatoken_pulsa_id_value);
        tv_operator_value = (TextView) v.findViewById(R.id.pulsatoken_operator_value);
        tv_nominal_value = (TextView) v.findViewById(R.id.pulsatoken_nominal_value);
        tv_payment_name = (TextView) v.findViewById(R.id.pulsatoken_item_payment_value);
        tv_amount_value = (TextView) v.findViewById(R.id.pulsatoken_amount_value);
        tv_fee_value = (TextView) v.findViewById(R.id.pulsatoken_fee_value);
        tv_total_amount_value = (TextView) v.findViewById(R.id.pulsatoken_total_amount_value);
        btn_submit = (Button) v.findViewById(R.id.pulsatoken_btn_verification);
        btn_cancel = (Button) v.findViewById(R.id.pulsatoken_btn_cancel);

        btn_submit.setOnClickListener(submitListener);
        btn_cancel.setOnClickListener(cancelListener);

        initializeLayout();
    }

    public void initializeLayout() {

        Bundle args = getArguments();
        cust_id = args.getString(DefineValue.CUST_ID, "");
        tx_id = args.getString(DefineValue.TX_ID, "");
        ccy_id = args.getString(DefineValue.CCY_ID, "");
        amount = args.getString(DefineValue.AMOUNT, "");
        fee = args.getString(DefineValue.FEE, "");
        item_name = args.getString(DefineValue.ITEM_NAME, "");
        payment_name = args.getString(DefineValue.PAYMENT_NAME);
        total_amount = args.getString(DefineValue.TOTAL_AMOUNT);
        bank_code = args.getString(DefineValue.BANK_CODE);
        product_code = args.getString(DefineValue.PRODUCT_CODE);
        product_payment_type = args.getString(DefineValue.PRODUCT_PAYMENT_TYPE);
        api_key = args.getString(DefineValue.API_KEY);
        callback_url = args.getString(DefineValue.CALLBACK_URL);
        is_sgo_plus = args.getBoolean(DefineValue.IS_SGO_PLUS);
        comm_code = args.getString(DefineValue.COMMUNITY_CODE);
        comm_id = args.getString(DefineValue.COMMUNITY_ID);
        shareType = args.getString(DefineValue.SHARE_TYPE);
        product_value = args.getString(DefineValue.PRODUCT_VALUE);
        phone_number = args.getString(DefineValue.PHONE_NUMBER);
        operator_id = args.getString(DefineValue.OPERATOR_ID);
        operator_name = args.getString(DefineValue.OPERATOR_NAME);
        Timber.d("isi args", args.toString());

        tv_operator_value.setText(operator_name);
        tv_nominal_value.setText(item_name);
        tv_phone_number.setText(phone_number);
        tv_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(amount));
        tv_payment_name.setText(payment_name);
        tv_fee_value.setText(ccy_id + ". " + CurrencyFormat.format(fee));
        tv_total_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(total_amount));

        if(!is_sgo_plus){
            merchant_type = args.getString(DefineValue.AUTHENTICATION_TYPE,"");
            if(merchant_type.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)|| product_payment_type.equalsIgnoreCase(DefineValue.BANKLIST_TYPE_SMS)){
                LinearLayout layoutOTP = (LinearLayout) v.findViewById(R.id.layout_token);
                layoutOTP.setVisibility(View.VISIBLE);
                et_token_value = (EditText) layoutOTP.findViewById(R.id.pulsatoken_token_value);

                et_token_value.requestFocus();
                isPIN = false;
            }
            else isPIN = true;
        }

    }

    private void changeToSgoPlus(String _tx_id, String _bank_code, String _product_code,
                                 String _fee) {
        Intent i = new Intent(getActivity(), PulsaAgentWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, _bank_code);
        i.putExtra(DefineValue.FEE, _fee);
        i.putExtra(DefineValue.COMMUNITY_CODE, comm_code);
        i.putExtra(DefineValue.TX_ID, _tx_id);
        i.putExtra(DefineValue.AMOUNT, amount);
        i.putExtra(DefineValue.API_KEY, api_key);
        i.putExtra(DefineValue.CALLBACK_URL, callback_url);
        i.putExtra(DefineValue.COMMUNITY_ID, comm_id);
        i.putExtra(DefineValue.DENOM_DATA,item_name);
        i.putExtra(DefineValue.PAYMENT_NAME,payment_name);
        i.putExtra(DefineValue.SHARE_TYPE, shareType);
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.PULSA_AGENT);
        i.putExtra(DefineValue.OPERATOR_NAME, operator_name);

        double totalAmount = Double.parseDouble(amount) + Double.parseDouble(_fee);
        i.putExtra(DefineValue.TOTAL_AMOUNT,String.valueOf(totalAmount));

        btn_submit.setEnabled(true);
        switchActivityIB(i);
    }

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){

                Timber.d("hit button submit","masukkkk");
                btn_submit.setEnabled(false);
                if(is_sgo_plus){
                    changeToSgoPlus(tx_id, bank_code, product_code, fee);
                }
                else {

                    if(isPIN){
                        Intent i = new Intent(getActivity(), InsertPIN.class);
                        btn_submit.setEnabled(true);
                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                    }
                    else{
                        if(inputValidation()) {
                            sentInsertTransTopup(et_token_value.getText().toString());
                            hideKeyboard();
                        }
                        else btn_submit.setEnabled(true);
                    }
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard();
            exit();
        }
    };

    private void sentInsertTransTopup(String tokenValue){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final RequestParams params = MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_INSERT_TRANS_TOPUP,
                    userID,accessKey);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, tokenValue);

            Timber.d("isi params insertTrxTOpupSGOL", params.toString());

            MyApiClient.sentInsertTransTopup(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        Timber.d("isi response insertTrxTOpupSGOL", response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            getTrxStatus(tx_id, comm_id);
                            setResultActivity(MainPage.RESULT_BALANCE);
                        }
                        else if(message.equals("PIN tidak sesuai")) {
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            if (isPIN) {
                                Intent i = new Intent(getActivity(), InsertPIN.class);
                                startActivityForResult(i, MainPage.REQUEST_FINISH);
                            }
                            progdialog.dismiss();
                            btn_submit.setEnabled(true);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            progdialog.dismiss();
                            btn_submit.setEnabled(true);
                            onOkButton();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progdialog.dismiss();
                    btn_submit.setEnabled(true);
                    Log.w("Error Koneksi insertTrxTOpupSGOL", throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }

    public void getTrxStatus(final String txId, String comm_id){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_GET_TRX_STATUS,
                    userID,accessKey);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);

            Timber.d("isi params sent get Trx Status", params.toString());

            MyApiClient.sentGetTRXStatus(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        progdialog.dismiss();
                        Timber.d("isi response sent get Trx Status", response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                            showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""), DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
                                    sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                    response.optString(WebParams.TX_STATUS, ""), response.optString(WebParams.TX_REMARK, ""), amount);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }else {
                            String msg = response.getString(WebParams.ERROR_MESSAGE);
                            showDialog(msg);
                        }

                        btn_submit.setEnabled(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    btn_submit.setEnabled(true);
                    progdialog.dismiss();
                    Log.w("Error Koneksi get trx status", throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }

    private void showReportBillerDialog(String name,String date,String userId, String txId,String itemName,String txStatus,
                                        String txRemark, String _amount) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = new ReportBillerDialog();
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.PULSA_AGENT);
        args.putString(DefineValue.PAYMENT_NAME, payment_name);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.OPERATOR_NAME, operator_name);

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)){
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        }else if(txStatus.equals(DefineValue.ONRECONCILED)){
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }else if(txStatus.equals(DefineValue.SUSPECT)){
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        }
        else if(!txStatus.equals(DefineValue.FAILED)){
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction)+" "+txStatus);
        }
        else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if(!txStat)args.putString(DefineValue.TRX_REMARK, txRemark);


        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(fee);
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));

        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    void showDialog(String msg) {
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
        Title.setText(getString(R.string.error));
        Message.setText(msg);

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //SgoPlusWeb.this.finish();
            }
        });

        dialog.show();
    }

    private void switchActivityIB(Intent mIntent){
        if (getActivity() == null)
            return;

        PulsaAgentActivity fca = (PulsaAgentActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    public void exit(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }


    public boolean inputValidation(){
        if(et_token_value.getText().toString().length()==0){
            et_token_value.requestFocus();
            et_token_value.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }


    private void setResultActivity(int result){
        if (getActivity() == null)
            return;

        PulsaAgentActivity fca = (PulsaAgentActivity) getActivity();
        fca.setResultActivity(result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.wtf("masuk onResume","masukkk");
        if(!is_sgo_plus)
            if(!isPIN)
                toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!is_sgo_plus)
            if(!isPIN)
                toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on){
        if (getActivity() == null)
            return;

        PulsaAgentActivity fca = (PulsaAgentActivity ) getActivity();
        fca.togglerBroadcastReceiver(_on,myReceiver);
    }


    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle mBundle = intent.getExtras();
            SmsMessage[] mSMS;
            String strMessage = "";
            String _kode_otp = "";
            String _member_code = "";
            String[] kode = context.getResources().getStringArray(R.array.broadcast_kode_compare);

            if(mBundle != null){
                Object[] pdus = (Object[]) mBundle.get("pdus");
                mSMS = new SmsMessage[pdus.length];

                for (int i = 0; i < mSMS.length ; i++){
                    mSMS[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    strMessage += mSMS[i].getMessageBody();
                    strMessage += "\n";
                }

                String[] words = strMessage.split(" ");
                for (int i = 0 ; i <words.length;i++)
                {
                    if(_kode_otp.equalsIgnoreCase("")){
                        if(words[i].equalsIgnoreCase(kode[0])){
                            if(words[i+1].equalsIgnoreCase(kode[1]))
                                _kode_otp = words[i+2];
                            _kode_otp =  _kode_otp.replace(".","").replace(" ","");
                        }
                    }

                    if(_member_code.equals("")){
                        if(words[i].equalsIgnoreCase(kode[2]))
                            _member_code = words[i+1];
                    }
                }

                insertTokenEdit(_kode_otp,_member_code);
                //Toast.makeText(context,strMessage,Toast.LENGTH_SHORT).show();
            }
        }
    };

    public final void insertTokenEdit(String _kode_otp, String _member_kode){
        Timber.d("isi _kode_otp, _member_kode, member kode session", _kode_otp+ " / " +_member_kode +" / "+ sp.getString(DefineValue.MEMBER_CODE,""));
        if(_member_kode.equals(sp.getString(DefineValue.MEMBER_CODE,""))){
            et_token_value.setText(_kode_otp);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if(requestCode == MainPage.REQUEST_FINISH){
            //  Timber.d("onActivity result", "Biller Fragment masuk request exit");
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                //    Timber.d("onActivity result", "Biller Fragment result pin value");
                sentInsertTransTopup(value_pin);
            }

        }
    }

    @Override
    public void onOkButton() {
        getActivity().finish();
    }


    public void hideKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
