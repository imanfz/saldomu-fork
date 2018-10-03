package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CashoutActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ErrorListener;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.InqDataAtcModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 8/25/2017.
 */

public class FragCashoutMember extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {
    View v, layout_button_transaction;
    SecurePreferences sp;
    String userID, accessKey, memberID, authType, amount, fee,total, ccyId, txId;
    TextView tvAgent, tvAmount, tvFee, tvTotal, tvCode, tvTxId, tvAlert;
    LinearLayout layoutOTP, layoutNoEmpty, layoutCode, layoutButton;
    RelativeLayout layoutEmpty;
    EditText tokenValue;
    Button btnOk, btnCancel, btnResend;
    int pin_attempt=-1;
    boolean isPIN, isOTP;
    int start = 0;
    Handler handlerWS;
    Runnable runnableWS;
    ProgressDialog progdialog;
    ProgressBar loading;
    private int max_token_resend = 3;
    private Button btn_proses_transaction;
    int failed = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_cash_out_member, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        memberID = sp.getString(DefineValue.MEMBER_ID, "");
        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE, "");

        isPIN = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN);
        isOTP = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP);

        layoutEmpty = (RelativeLayout) v.findViewById(R.id.cashoutmember_empty_layout);
        layoutNoEmpty = (LinearLayout) v.findViewById(R.id.cashoutmember_layout);
        layoutCode = (LinearLayout) v.findViewById(R.id.cashoutmember_code_layout);
        layoutButton = (LinearLayout) v.findViewById(R.id.cashoutmember_bottom_layout);
        tvTxId = (TextView) v.findViewById(R.id.cashoutmember_tx_id_value);
        tvAgent = (TextView) v.findViewById(R.id.cashoutmember_agent_value);
        tvAmount = (TextView) v.findViewById(R.id.cashoutmember_amount_value);
        tvFee = (TextView) v.findViewById(R.id.cashoutmember_fee_value);
        tvTotal = (TextView) v.findViewById(R.id.cashoutmember_total_value);
        tvCode = (TextView) v.findViewById(R.id.cashoutmember_code);
        loading = (ProgressBar) v.findViewById(R.id.prgLoading);
        tvAlert = (TextView) v.findViewById(R.id.text_alert);
        layoutOTP = (LinearLayout) v.findViewById(R.id.cashoutmember_layout_OTP);
        tokenValue = (EditText) v.findViewById(R.id.cashoutmember_value_otp);
        btnOk = (Button) v.findViewById(R.id.cashoutmember_btn_ok);
        btnCancel = (Button) v.findViewById(R.id.cashoutmember_btn_cancel);
        btn_proses_transaction = (Button) v.findViewById(R.id.btn_verification);
        layout_button_transaction = v.findViewById(R.id.layout_button_check_transaction);

        layoutEmpty.setVisibility(View.VISIBLE);
        layoutNoEmpty.setVisibility(View.GONE);
        layoutCode.setVisibility(View.GONE);

        handlerWS = new Handler();
        runnableWS = new Runnable() {
            @Override
            public void run() {
                sentInquiryDataATC();
            }
        };
        handlerWS.post(runnableWS);

        if(isOTP) {
            layoutOTP.setVisibility(View.VISIBLE);
            btnResend = (Button) v.findViewById(R.id.btn_resend_token);

            View layout_resendbtn = v.findViewById(R.id.layout_btn_resend);
            layout_resendbtn.setVisibility(View.VISIBLE);

            btnResend.setOnClickListener(resendListener);
            changeTextBtnSub();
        }
        else {
            layoutOTP.setVisibility(View.GONE);
            new UtilsLoader(getActivity(),sp).getFailedPIN(userID, new OnLoadDataListener() { //get pin attempt
                @Override
                public void onSuccess(Object deData) {
                    pin_attempt = (int) deData;
                }

                @Override
                public void onFail(Bundle message) {

                }

                @Override
                public void onFailure(String message) {

                }
            });
        }

        btnOk.setOnClickListener(btnOkListener);
        btnCancel.setOnClickListener(btnCancelListener);
        btn_proses_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(InetHandler.isNetworkAvailable(getActivity())) {
                    getTrxStatus();
                }
                else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
            }
        });

        CashoutActivity fca = (CashoutActivity) getActivity();
        fca.setTitleToolbar(getString(R.string.title_cashout_agen));
    }

    Button.OnClickListener btnOkListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if(isPIN) {
                    Intent i = new Intent(getActivity(), InsertPIN.class);
                    if(pin_attempt != -1 && pin_attempt < 2)
                        i.putExtra(DefineValue.ATTEMPT,pin_attempt);
                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                }
                else if(isOTP) {
                    if (inputValidation()) {
                        inquiryTokenATC(RSA.opensslEncrypt(tokenValue.getText().toString()), txId);
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Authentication type kosong", Toast.LENGTH_LONG).show();
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
                                cancelATC();
                                dialog.dismiss();
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
                inquiryTokenATC(value_pin, txId);
            }
        }
    }

    public void sentInquiryDataATC() {
//        if(isVisible()) {
        try {
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_DATA_ATC);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CASHOUT_TYPE, "ATC");

            Timber.d("isi params sent inquiry data atc:" + params.toString());

            RetrofitService.getInstance().PostWithOnError(MyApiClient.LINK_INQUIRY_DATA_ATC, params,
                    new ErrorListener() {
                        @Override
                        public void onError(Throwable e) {
                            if (failed < 3) {
                                failed++;
                                handlerWS.postDelayed(runnableWS, 60000);
                            }
                        }

                        @Override
                        public void onResponses(JsonObject object) {

                            InqDataAtcModel model = getGson().fromJson(object, InqDataAtcModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                handlerWS.removeCallbacks(runnableWS);
                                layoutEmpty.setVisibility(View.GONE);
                                layoutNoEmpty.setVisibility(View.VISIBLE);

                                txId = model.getTx_id();
                                ccyId = model.getCcy_id();
                                tvTxId.setText(txId);
                                tvAgent.setText(model.getAgent_member_name());
                                tvAmount.setText(ccyId + ". " + CurrencyFormat.format(model.getAmount()));
                                tvFee.setText(ccyId + ". " + CurrencyFormat.format(model.getFee()));
                                tvTotal.setText(ccyId + ". " + CurrencyFormat.format(model.getTotal()));
                                amount = model.getAmount();
                                fee = model.getFee();
                                total = model.getTotal();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(ErrorDefinition.NO_TRANSACTION)){
                                loading.setVisibility(View.GONE);
                                tvAlert.setText(getString(R.string.cashoutmember_alert_no_tx));
                                handlerWS.postDelayed(runnableWS, 60000);
                            } else {
                                String message = model.getError_message();
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                handlerWS.postDelayed(runnableWS, 60000);
                            }

                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void inquiryTokenATC(String _token, String _tx_id) {
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_TOKEN_ATC);

            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(_token));
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID,memberID);
            Timber.d("isi params sent inquiry token ATC:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INQUIRY_TOKEN_ATC, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            progdialog.dismiss();
                            Timber.d("isi response sent inquiry token ATC:" + response.toString());

                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    layoutCode.setVisibility(View.VISIBLE);
                                    layout_button_transaction.setVisibility(View.VISIBLE);
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
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void cancelATC() {
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_ATC);

            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params sent cancel ATC:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CANCEL_ATC, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                getActivity().finish();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                code = model.getError_message();
                                if (MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }

                            if (progdialog.isShowing())
                                progdialog.dismiss();
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

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RESEND_TOKEN_LKD);
            params.put(WebParams.TX_ID,_data);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent resend token:"+params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_RESEND_TOKEN_LKD, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
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

                                    if (MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                                }
                                if (max_token_resend == 0) {
                                    btnResend.setEnabled(false);

                                    Toast.makeText(getActivity(), getString(R.string.notif_max_resend_token_empty), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
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
    public void onStart() {
        super.onStart();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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

    public void getTrxStatus(){
        try{

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            extraSignature = txId + MyApiClient.COMM_ID;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.TYPE, DefineValue.CASHOUT_TUNAI_TYPE);
            params.put(WebParams.PRIVACY, DefineValue.PRIVATE);
            params.put(WebParams.TX_TYPE, DefineValue.EMO);
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params sent get Trx Status:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                showReportBillerDialog( model);
//                                DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
//                                        response.optString(WebParams.TX_STATUS,""), response.optString(WebParams.TX_REMARK, ""),
//                                        response.optString(WebParams.BUSS_SCHEME_CODE), response.optString(WebParams.BUSS_SCHEME_NAME));
                            } else if(code.equals(WebParams.LOGOUT_CODE)){
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else if(code.equals(ErrorDefinition.ERROR_CODE_ADMIN_NOT_INPUT)) {
                                showDialogNotInput(message);
                            }
                            else {
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(), message,Toast.LENGTH_LONG).show();
                            }


                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    }
            );
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showReportBillerDialog(GetTrxStatusReportModel model ){
//            String datetime, String txStatus, String txRemark, String buss_scheme_code,
//                                        String buss_scheme_name) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userID);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.getCreated()));
        args.putString(DefineValue.NAME_ADMIN, tvAgent.getText().toString());
        args.putString(DefineValue.AMOUNT, ccyId+" "+CurrencyFormat.format(amount));
        args.putString(DefineValue.FEE, ccyId + " " + CurrencyFormat.format(fee));
        args.putString(DefineValue.TOTAL_AMOUNT, ccyId+" "+CurrencyFormat.format(total));
        args.putString(DefineValue.REPORT_TYPE,DefineValue.CASHOUT_TUNAI);

        Boolean txStat = false;
        String txStatus = model.getTx_status();
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
        if(!txStat)args.putString(DefineValue.TRX_REMARK, model.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, model.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.getBuss_scheme_name());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    void showDialogNotInput(String message){
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOK = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.dialog_notinput_cashoutcode));
        Message.setText(message);

        btnDialogOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onOkButton() {
        getActivity().setResult(MainPage.RESULT_BALANCE);
        getActivity().finish();
    }
}
