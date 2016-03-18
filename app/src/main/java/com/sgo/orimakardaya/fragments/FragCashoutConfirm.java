package com.sgo.mdevcash.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.mdevcash.R;
import com.sgo.mdevcash.activities.CashoutActivity;
import com.sgo.mdevcash.activities.InsertPIN;
import com.sgo.mdevcash.activities.MainPage;
import com.sgo.mdevcash.coreclass.*;
import com.sgo.mdevcash.dialogs.AlertDialogLogout;
import com.sgo.mdevcash.dialogs.DefinedDialog;
import com.sgo.mdevcash.dialogs.ReportBillerDialog;
import com.sgo.mdevcash.interfaces.OnLoadDataListener;
import com.sgo.mdevcash.loader.UtilsLoader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 11/20/2015.
 */
public class FragCashoutConfirm extends Fragment implements ReportBillerDialog.OnDialogOkCallback {
    View v;
    SecurePreferences sp;
    LinearLayout layoutOTP;
    TextView txtTxId, txtBankName, txtAccno, txtAccName, txtCurrency, txtNominal, txtFee, txtTotal;
    EditText tokenValue;
    Button btnProcess;
    ProgressDialog progdialog;
    String name, userID, accessKey, txId, bankName, accNo, ccyId, nominal, accName, fee, total;
    boolean isPIN, isOTP;
    int pin_attempt=-1;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String authType = sp.getString(DefineValue.AUTHENTICATION_TYPE,"");
        name = sp.getString(DefineValue.USER_NAME,"");
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        isPIN = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN);

        isOTP = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP);

        txtTxId = (TextView) v.findViewById(R.id.cashout_value_tx_id);
        txtBankName = (TextView) v.findViewById(R.id.cashout_value_bank_name);
        txtAccno = (TextView) v.findViewById(R.id.cashout_value_bank_acc_no);
        txtAccName = (TextView) v.findViewById(R.id.cashout_value_bank_acc_name);
        txtCurrency = (TextView) v.findViewById(R.id.cashout_value_ccy);
        txtNominal  = (TextView) v.findViewById(R.id.cashout_value_nominal);
        txtFee = (TextView) v.findViewById(R.id.cashout_value_fee);
        txtTotal = (TextView) v.findViewById(R.id.cashout_value_total);
        layoutOTP = (LinearLayout) v.findViewById(R.id.cashout_layout_OTP);
        tokenValue = (EditText) v.findViewById(R.id.cashout_value_otp);
        btnProcess = (Button) v.findViewById(R.id.cashoutconfirm_btn_process);
        btnProcess.setOnClickListener(btnProcessListener);

        if(isOTP) layoutOTP.setVisibility(View.VISIBLE);
        else {
            layoutOTP.setVisibility(View.GONE);
            new UtilsLoader(getActivity(),sp).getFailedPIN(new OnLoadDataListener() { //get pin attempt
                @Override
                public void onSuccess(Object deData) {
                    pin_attempt = (int) deData;
                }

                @Override
                public void onFail(String message) {

                }

                @Override
                public void onFailure() {

                }
            });
        }

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            txId = bundle.getString(DefineValue.TX_ID,"");
            bankName = bundle.getString(DefineValue.BANK_NAME,"");
            accNo = bundle.getString(DefineValue.ACCOUNT_NUMBER,"");
            ccyId = bundle.getString(DefineValue.CCY_ID,"");
            nominal = bundle.getString(DefineValue.NOMINAL,"");
            accName = bundle.getString(DefineValue.ACCT_NAME,"");
            fee = bundle.getString(DefineValue.FEE,"");
            total = bundle.getString(DefineValue.TOTAL_AMOUNT,"");

            txtTxId.setText(txId);
            txtBankName.setText(bankName);
            txtAccno.setText(accNo);
            txtCurrency.setText(ccyId);
            txtNominal.setText(ccyId + " " + CurrencyFormat.format(nominal));
            txtAccName.setText(accName);
            txtFee.setText(ccyId + " " + CurrencyFormat.format(fee));
            txtTotal.setText(ccyId + " " + CurrencyFormat.format(total));

        }
    }

    Button.OnClickListener btnProcessListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                btnProcess.setEnabled(false);
                btnProcess.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnProcess.setEnabled(true);
                    }
                }, 2000);

                if (isOTP) {
                    if (inputValidation()) {
                        confirmCashout(tokenValue.getText().toString());
                    }
                } else {
                    Intent i = new Intent(getActivity(), InsertPIN.class);
                    if(pin_attempt != -1 && pin_attempt < 2)
                        i.putExtra(DefineValue.ATTEMPT,pin_attempt);
                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));

        }
    };

    public void confirmCashout(String _token){
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_CONFIRM_CASHOUT,
                    userID, accessKey);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.TOKEN_ID, _token);

            MyApiClient.sentConfCashout(getActivity(),params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response confirm cashout:"+response.toString());
                            showReportBillerDialog(name, DateTimeFormat.getCurrentDateTime(), userID, txId, bankName, accNo,
                                    accName, ccyId + " " + CurrencyFormat.format(nominal),
                                    ccyId + " " + CurrencyFormat.format(fee), ccyId + " " + CurrencyFormat.format(total));

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else {
                            Timber.d("isi error confirm cashout:"+response.toString());
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            if(isPIN && code.equals(ErrorDefinition.WRONG_PIN_CASHOUT)){
                                Intent i = new Intent(getActivity(), InsertPIN.class);
                                pin_attempt = pin_attempt - 1;
                                if(pin_attempt != -1 && pin_attempt < 2)
                                    i.putExtra(DefineValue.ATTEMPT, pin_attempt);

                                startActivityForResult(i, MainPage.REQUEST_FINISH);
                            }
                            else {
                                onOkButton();
                            }

                            Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
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

                    Timber.w("Error Koneksi confirm cashout:"+throwable.toString());
                }

            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showReportBillerDialog(String _name,String _date,String _userId, String _txId, String _bankName,String _accNo,
                                        String _accName, String _nominal, String _fee,String _totalAmount) {

        Bundle args = new Bundle();
        ReportBillerDialog dialog = new ReportBillerDialog();
        args.putString(DefineValue.USER_NAME,_name);
        args.putString(DefineValue.DATE_TIME,_date);
        args.putString(DefineValue.USERID_PHONE,_userId);
        args.putString(DefineValue.TX_ID,_txId);
        args.putString(DefineValue.BANK_NAME,_bankName);
        args.putString(DefineValue.ACCOUNT_NUMBER,_accNo);
        args.putString(DefineValue.ACCT_NAME,_accName);
        args.putString(DefineValue.NOMINAL,_nominal);
        args.putString(DefineValue.FEE,_fee);
        args.putString(DefineValue.TOTAL_AMOUNT,_totalAmount);
        args.putString(DefineValue.REPORT_TYPE,DefineValue.CASHOUT);

        dialog.setArguments(args);
        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(),ReportBillerDialog.TAG);
    }

    public boolean inputValidation(){
        if(tokenValue.getText().toString().length()==0){
            tokenValue.requestFocus();
            tokenValue.setError(getString(R.string.cashout_validation_otp));
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if(requestCode == MainPage.REQUEST_FINISH){
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                confirmCashout(value_pin);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on){
        if (getActivity() == null)
            return;

        CashoutActivity fca = (CashoutActivity) getActivity();
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
        Timber.d("isi _kode_otp, _member_kode, member kode session:"+_kode_otp + " / " + _member_kode + " / " + sp.getString(DefineValue.MEMBER_CODE, ""));
        if(_member_kode.equals(sp.getString(DefineValue.MEMBER_CODE,""))){
            tokenValue.setText(_kode_otp);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_cashout_confirm_token, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
    public void onOkButton() {
        getActivity().setResult(MainPage.RESULT_BALANCE);
        getActivity().finish();
    }
}
