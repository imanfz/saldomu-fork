package com.sgo.hpku.fragments;
/*
  Created by Administrator on 1/31/2017.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BBSActivity;
import com.sgo.hpku.activities.InsertPIN;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.SgoPlusWeb;
import com.sgo.hpku.coreclass.CurrencyFormat;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.ErrorDefinition;
import com.sgo.hpku.coreclass.InetHandler;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.interfaces.OnLoadDataListener;
import com.sgo.hpku.loader.UtilsLoader;
import com.sgo.hpku.securities.Md5;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class Cashoutbbs_describ_member extends Fragment {
    public final static String TAG = "com.sgo.hpku.fragments.Cashoutbbs_describ_member";
    View v;
//    layout_button_transaction;
    SecurePreferences sp;
    String userID, accessKey, authType, amount, fee,total, ccyId, txId, product_h2h, comm_code,
    product_name, product_code, bank_code, bank_name, callback_url, api_key, comm_id;
    TextView tvAgent, tvAmount, tvFee, tvTotal, tvCode, tvTxId, tvAlert;
    LinearLayout layoutOTP, layoutNoEmpty, layoutCode, layoutButton;
    RelativeLayout layoutEmpty;
    EditText tokenValue;
    Button btnOk, btnCancel, btnResend;
    int pin_attempt=-1;
    boolean isPIN = true, isOTP = false;
    int start = 0;
    Handler handlerWS;
    Runnable runnableWS;
    ProgressDialog progdialog;
    ProgressBar loading;
    private int max_token_resend = 3;
//    private Button btn_proses_transaction;
    int failed = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.cashoutbbs_describ_member, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
//        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE, "");

//        isPIN = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN);
//        isOTP = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP);

        layoutEmpty = (RelativeLayout) v.findViewById(R.id.bbscashoutmember_empty_layout);
        layoutNoEmpty = (LinearLayout) v.findViewById(R.id.bbscashoutmember_layout);
        layoutCode = (LinearLayout) v.findViewById(R.id.bbscashoutmember_code_layout);
        layoutButton = (LinearLayout) v.findViewById(R.id.bbscashoutmember_bottom_layout);
        tvTxId = (TextView) v.findViewById(R.id.bbscashoutmember_tx_id_value);
        tvAgent = (TextView) v.findViewById(R.id.bbscashoutmember_agent_value);
        tvAmount = (TextView) v.findViewById(R.id.bbscashoutmember_amount_value);
        tvFee = (TextView) v.findViewById(R.id.bbscashoutmember_fee_value);
        tvTotal = (TextView) v.findViewById(R.id.bbscashoutmember_total_value);
        tvCode = (TextView) v.findViewById(R.id.bbscashoutmember_code);
        loading = (ProgressBar) v.findViewById(R.id.prgLoading);
        tvAlert = (TextView) v.findViewById(R.id.text_alert);
        layoutOTP = (LinearLayout) v.findViewById(R.id.bbscashoutmember_layout_OTP);
        tokenValue = (EditText) v.findViewById(R.id.bbscashoutmember_value_otp);
        btnOk = (Button) v.findViewById(R.id.bbscashoutmember_btn_ok);
        btnCancel = (Button) v.findViewById(R.id.bbscashoutmember_btn_cancel);
//        btn_proses_transaction = (Button) v.findViewById(R.id.btn_verification);
//        layout_button_transaction = v.findViewById(R.id.layout_button_check_transaction);

        layoutEmpty.setVisibility(View.VISIBLE);
        layoutNoEmpty.setVisibility(View.GONE);
        layoutCode.setVisibility(View.GONE);

        handlerWS = new Handler();
        runnableWS = new Runnable() {
            @Override
            public void run() {
                sentListMemberATC();
            }
        };
        handlerWS.post(runnableWS);

        btnOk.setOnClickListener(btnOkListener);
        btnCancel.setOnClickListener(btnCancelListener);
//        btn_proses_transaction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(InetHandler.isNetworkAvailable(getActivity())) {
//                    getTrxStatus();
//                }
//                else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
//            }
//        });
    }

    Button.OnClickListener btnOkListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if(product_h2h.equalsIgnoreCase("Y")) {
                    if (isPIN) {
                        Intent i = new Intent(getActivity(), InsertPIN.class);
                        if (pin_attempt != -1 && pin_attempt < 2)
                            i.putExtra(DefineValue.ATTEMPT, pin_attempt);
                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                    } else if (isOTP) {
                        if (inputValidation())
                            try {
                                OTPMemberATC(Md5.hashMd5(tokenValue.getText().toString()), txId);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                    } else {
                        Toast.makeText(getActivity(), "Authentication type kosong", Toast.LENGTH_LONG).show();
                    }
                }
                else if(product_h2h.equalsIgnoreCase("N")) {
                    changeToSGOPlus(txId, product_code, product_name, bank_code, amount, fee, total, bank_name);
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.cashoutmember_cancel_message))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
                    if (max_token_resend != 0)
                        sentResendToken(txId);

                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));

        }
    };

    public void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainPage.REQUEST_FINISH){
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                OTPMemberATC(value_pin, txId);
            }
        }
    }

    public void sentListMemberATC() {
        try {
            RequestParams params;
            params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_BBS_LIST_MEMBER_A2C,
                    userID, accessKey);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.CUSTOMER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent list member atc:" + params.toString());

            MyApiClient.sentBBSListMemberA2C(getActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Timber.d("isi request sent list member atc:" + response.toString());

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            handlerWS.removeCallbacks(runnableWS);
                            layoutEmpty.setVisibility(View.GONE);
                            layoutNoEmpty.setVisibility(View.VISIBLE);

                            txId = response.getString(WebParams.TX_ID);
                            ccyId = response.getString(WebParams.CCY_ID);
                            product_code = response.getString(WebParams.PRODUCT_CODE);
                            product_name = response.getString(WebParams.PRODUCT_NAME);
                            bank_code = response.getString(WebParams.BANK_CODE);
                            bank_name = response.getString(WebParams.BANK_NAME);
                            api_key = response.getString(WebParams.API_KEY);
                            callback_url = response.getString(WebParams.CALLBACK_URL);
                            comm_id = response.getString(WebParams.COMM_ID);
                            tvTxId.setText(txId);
                            tvAgent.setText(response.getString(WebParams.MEMBER_NAME));
                            tvAmount.setText(ccyId + ". " + CurrencyFormat.format(response.getString(WebParams.TX_AMOUNT)));
                            tvFee.setText(ccyId + ". " + CurrencyFormat.format(response.getString(WebParams.FEE_AMOUNT)));
                            tvTotal.setText(ccyId + ". " + CurrencyFormat.format(response.getString(WebParams.TOTAL_AMOUNT)));
                            amount = response.getString(WebParams.TX_AMOUNT);
                            fee = response.getString(WebParams.FEE_AMOUNT);
                            total = response.getString(WebParams.TOTAL_AMOUNT);
                            product_h2h = response.getString(WebParams.PRODUCT_H2H);
                            comm_code = response.getString(WebParams.COMM_CODE);
                            setPayment(product_h2h);
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else if (code.equals(ErrorDefinition.NO_TRANSACTION)){
                            loading.setVisibility(View.GONE);
                            tvAlert.setText(getString(R.string.cashoutmember_alert_no_tx));
                            handlerWS.postDelayed(runnableWS, 60000);
                        } else {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            handlerWS.postDelayed(runnableWS, 60000);
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

                private void failure(Throwable throwable) {
                    Timber.w("Error Koneksi sent list member atc" + throwable.toString());
                    if (failed < 3) {
                        failed++;
                        handlerWS.postDelayed(runnableWS, 60000);
                    }
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void setPayment(String _product_h2h) {
        if(_product_h2h.equalsIgnoreCase("Y")) {
            if (isOTP) {
                layoutOTP.setVisibility(View.VISIBLE);
                btnResend = (Button) v.findViewById(R.id.btn_resend_token);

                View layout_resendbtn = v.findViewById(R.id.layout_btn_resend);
                layout_resendbtn.setVisibility(View.VISIBLE);

                btnResend.setOnClickListener(resendListener);
                changeTextBtnSub();
            } else {
                layoutOTP.setVisibility(View.GONE);
                new UtilsLoader(getActivity(), sp).getFailedPIN(userID, new OnLoadDataListener() { //get pin attempt
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
        }
    }

    public void OTPMemberATC(String _token, String _tx_id) {
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params;
            params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_BBS_OTP_MEMBER_A2C,
                    userID, accessKey);

            params.put(WebParams.TOKEN_ID, _token);
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.CUSTOMER_ID,userID);
            params.put(WebParams.COMM_CODE,comm_code);
            params.put(WebParams.SENDER_ID,"GOMOBILE");
            params.put(WebParams.RECEIVER_ID,"GOWORLD");
            Timber.d("isi params sent otp member ATC:" + params.toString());

            MyApiClient.sentBBSOTPMemberA2C(getActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    Timber.d("isi response sent otp member ATC:" + response.toString());

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            layoutCode.setVisibility(View.VISIBLE);
//                            layout_button_transaction.setVisibility(View.VISIBLE);
                            if(isOTP) layoutOTP.setVisibility(View.GONE);
                            layoutButton.setVisibility(View.GONE);
                            tvCode.setText(response.getString(WebParams.OTP_MEMBER));
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else if(code.equals(ErrorDefinition.ERROR_CODE_WRONG_TOKEN)){
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        } else if(code.equals(ErrorDefinition.WRONG_PIN_CASHOUT)) {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getActivity(), InsertPIN.class);
                            pin_attempt = pin_attempt - 1;
                            if(pin_attempt != -1 && pin_attempt < 2)
                                i.putExtra(DefineValue.ATTEMPT,pin_attempt);
                            startActivityForResult(i, MainPage.REQUEST_FINISH);
                        }
                        else {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            getActivity().finish();
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

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if (progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi sent otp member ATC:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sentResendToken(String _data){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_RESEND_TOKEN_LKD,
                    userID,accessKey);
            params.put(WebParams.TX_ID,_data);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent resend token:"+params.toString());

            MyApiClient.sentResendTokenLKD(getActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params resend token:" + response.toString());
                            max_token_resend = max_token_resend - 1;
                            changeTextBtnSub();
                            Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else {
                            Timber.d("isi error resend token:" + response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                        }
                        if (max_token_resend == 0) {
                            btnResend.setEnabled(false);
                            Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_LONG).show();
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

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if (progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi resend token:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void setMemberOTP(String otp) {
        layoutCode.setVisibility(View.VISIBLE);
        layoutButton.setVisibility(View.GONE);
        tvCode.setText(otp);
    }

    private void changeToSGOPlus(String _tx_id, String _product_code, String _product_name, String _bank_code,
                                 String _amount, String fee, String totalAmount, String _bank_name) {

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, _bank_code);
        i.putExtra(DefineValue.BANK_NAME, _bank_name);
        i.putExtra(DefineValue.PRODUCT_NAME,_product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.COMMUNITY_CODE,comm_code);
        i.putExtra(DefineValue.TX_ID,_tx_id);
        i.putExtra(DefineValue.AMOUNT,_amount);
        i.putExtra(DefineValue.SHARE_TYPE,"1");
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        i.putExtra(DefineValue.CALLBACK_URL,callback_url);
        i.putExtra(DefineValue.API_KEY, api_key);

        i.putExtra(DefineValue.TOTAL_AMOUNT,totalAmount);
        i.putExtra(DefineValue.COMMUNITY_ID, comm_id);
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.BBS_MEMBER_OTP);

        switchActivityIB(i);
    }

    private void switchActivityIB(Intent mIntent){
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    public boolean inputValidation(){
        if(tokenValue.getText().toString().length()==0){
            tokenValue.requestFocus();
            tokenValue.setError(getString(R.string.cashoutmember_validation_otp));
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        handlerWS.removeCallbacks(runnableWS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
