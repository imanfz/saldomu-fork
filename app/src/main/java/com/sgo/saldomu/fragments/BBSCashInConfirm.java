package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 2/1/2017.
 */

public class BBSCashInConfirm extends Fragment implements ReportBillerDialog.OnDialogOkCallback {
    public final static String TAG = "com.sgo.saldomu.fragments.BBSCashInConfirm";
    private static final int MAX_TOKEN_RESENT = 3;

    private SecurePreferences sp;
    private ProgressDialog progdialog;
    private TextView tvTitle;
    private View v, cityLayout, layout_btn_resend, layout_OTP, layoutTCASH;
    private TextView tvSourceAcct, tvBankBenef, tvBenefCity, tvAmount, tvNoBenefAcct,
            tvNameBenefAcct, tvNoHp, tvRemark, tvFee, tvTotal, tvNoDestination;
    private TableRow tbNameBenef;
    private EditText tokenValue, noHpTCASH;
    private Button btnSubmit, btnResend, btnBack;
    private String userID, accessKey, comm_code, tx_product_code, source_product_type,
            benef_city, source_product_h2h, api_key, callback_url, tx_bank_code, tx_bank_name, tx_product_name,
            fee, tx_id, amount, share_type, comm_id, benef_product_name, name_benef, no_benef,
            no_hp_benef, remark, source_product_name, total_amount, transaksi, tx_status;
    private int max_token_resend = MAX_TOKEN_RESENT;
    private boolean isSMS = false, isIB = false, isPIN = false, TCASH_hp_validation=false, isTCASH = false, validasiNomor=false;
    private int attempt;
    private int failed;
    private SMSclass smSclass;
    private ActionListener actionListener;
    private Boolean finishTransaction = false;

    public interface ActionListener{
        void ChangeActivityFromCashInConfirm(Intent data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof ActionListener) {
            actionListener = (ActionListener) getTargetFragment();
        } else {
            if(context instanceof ActionListener){
                actionListener = (ActionListener) context;
            }
            else {
                throw new RuntimeException(context.toString()
                        + " must implement ActionListener CashInConfirm");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_bbs_cashin_confirm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        CircleStepView mCircleStepView = ((CircleStepView) v.findViewById(R.id.circle_step_view));
        mCircleStepView.setTextBelowCircle("", "", getString(R.string.konfirmasi_agen));
        mCircleStepView.setCurrentCircleIndex(2, false);

        tvTitle = (TextView) v.findViewById(R.id.tv_title);
        cityLayout = v.findViewById(R.id.benef_city_layout);
        tvSourceAcct = (TextView) v.findViewById(R.id.bbscashin_confirm_value_source_acct);
        tvBankBenef = (TextView) v.findViewById(R.id.bbscashin_confirm_value_benef_acct);
        tvBenefCity = (TextView) v.findViewById(R.id.bbscashin_confirm_value_benef_city);
        tvAmount = (TextView) v.findViewById(R.id.bbscashin_confirm_value_amount);
        tvFee = (TextView) v.findViewById(R.id.bbscashin_confirm_value_fee);
        tvTotal = (TextView) v.findViewById(R.id.bbscashin_confirm_value_total);
        tvNoBenefAcct = (TextView) v.findViewById(R.id.bbscashin_confirm_value_benef_no);
        tvNameBenefAcct = (TextView) v.findViewById(R.id.bbscashin_confirm_value_benef_name);
        tvNoHp = (TextView) v.findViewById(R.id.bbscashin_confirm_value_no_hp);
        tvRemark = (TextView) v.findViewById(R.id.bbscashin_confirm_value_remark);
        btnSubmit = (Button) v.findViewById(R.id.btn_submit);
        layout_OTP = v.findViewById(R.id.layout_OTP);
        tokenValue = (EditText) v.findViewById(R.id.bbscashin_confirm_value_otp);
        layout_btn_resend = v.findViewById(R.id.layout_btn_resend);
        btnResend = (Button) v.findViewById(R.id.btn_resend_token);
        tvNoDestination = (TextView) v.findViewById(R.id.bbscashin_confirm_text_no_destination);
        btnBack = (Button) v.findViewById(R.id.btn_back);
        layoutTCASH = v.findViewById(R.id.layout_TCASH);
        noHpTCASH = (EditText) v.findViewById(R.id.et_no_hp_tcash);
        tbNameBenef = (TableRow) v.findViewById(R.id.tb_name_benef);

        Bundle bundle = getArguments();
        if(bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            if(bundle.containsKey(DefineValue.BENEF_CITY)) {
                benef_city = bundle.getString(DefineValue.BENEF_CITY);
            }
            source_product_h2h = bundle.getString(DefineValue.PRODUCT_H2H);
            source_product_type = bundle.getString(DefineValue.PRODUCT_TYPE);
            tx_product_code = bundle.getString(DefineValue.PRODUCT_CODE);
            tx_bank_code = bundle.getString(DefineValue.BANK_CODE);
            tx_bank_name = bundle.getString(DefineValue.BANK_NAME);
            tx_product_name = bundle.getString(DefineValue.PRODUCT_NAME);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            tx_id = bundle.getString(DefineValue.TX_ID);
            amount = bundle.getString(DefineValue.AMOUNT);
            fee = bundle.getString(DefineValue.FEE);
            total_amount = bundle.getString(DefineValue.TOTAL_AMOUNT);
            share_type = bundle.getString(DefineValue.SHARE_TYPE);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID );
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            name_benef = bundle.getString(DefineValue.NAME_BENEF);
            no_benef =  bundle.getString(DefineValue.NO_BENEF);
            no_hp_benef  = bundle.getString(DefineValue.NO_HP_BENEF);
            remark = bundle.getString(DefineValue.REMARK);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);
            TCASH_hp_validation = bundle.getBoolean(DefineValue.TCASH_HP_VALIDATION);
            String benef_product_type = bundle.getString(DefineValue.TYPE_BENEF,"");

            if(!bundle.getString(DefineValue.MAX_RESEND).equals(""))
                max_token_resend = Integer.parseInt(bundle.getString(DefineValue.MAX_RESEND));

            tvTitle.setText(transaksi);
            tvAmount.setText(CurrencyFormat.format(amount));
            tvFee.setText(CurrencyFormat.format(fee));
            tvTotal.setText(CurrencyFormat.format(total_amount));
            tvBankBenef.setText(benef_product_name);
            tvBenefCity.setText(benef_city);
            tvNameBenefAcct.setText(name_benef);
            if (name_benef.isEmpty()|| name_benef==null || name_benef.equalsIgnoreCase(""))
            {
                tbNameBenef.setVisibility(View.GONE);
            }
            tvNoBenefAcct.setText(no_benef);
            tvNoHp.setText(no_hp_benef);
            tvRemark.setText(remark);
            tvSourceAcct.setText(source_product_name);
            if(source_product_h2h.equalsIgnoreCase("N")) {
                isIB = true;
                layout_OTP.setVisibility(View.GONE);
                layout_btn_resend.setVisibility(View.GONE);

                if(benef_product_type.equalsIgnoreCase("EMO")) {
                    cityLayout.setVisibility(View.GONE);

                }
                else if(benef_product_type.equalsIgnoreCase("ACCT")) {
                    cityLayout.setVisibility(View.VISIBLE);
                }
            }
            else if(source_product_h2h.equalsIgnoreCase("Y")) {
                if(source_product_type.equalsIgnoreCase("EMO") && !tx_product_code.equalsIgnoreCase("TCASH")) {
                    isPIN = true;
                    new UtilsLoader(getActivity(),sp).getFailedPIN(userID,new OnLoadDataListener() { //get pin attempt
                        @Override
                        public void onSuccess(Object deData) {
                            attempt = (int)deData;
                        }

                        @Override
                        public void onFail(Bundle message) {

                        }

                        @Override
                        public void onFailure(String message) {

                        }
                    });
//                    if(source_product_code.equalsIgnoreCase("TCASH"))
//                    {
//                        layout_OTP.setVisibility(View.VISIBLE);
//                        layout_btn_resend.setVisibility(View.VISIBLE);
//                        tokenValue.requestFocus();
//                        btnResend.setText(getString(R.string.reg2_btn_text_resend_token_tcash) + " (" + max_token_resend + ")");
//                        btnResend.setOnClickListener(resendListener);
//                    } else {

                        layout_OTP.setVisibility(View.GONE);
                        layout_btn_resend.setVisibility(View.GONE);
//                    }
                    cityLayout.setVisibility(View.GONE);
                }
                else if(source_product_type.equalsIgnoreCase("ACCT")) {
                    isSMS = true;
                    cityLayout.setVisibility(View.VISIBLE);
                    layout_OTP.setVisibility(View.VISIBLE);
                    layout_btn_resend.setVisibility(View.VISIBLE);
                    tokenValue.requestFocus();
                    btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");
                    btnResend.setOnClickListener(resendListener);
                    initializeSmsClass();
                }
                else if (tx_product_code.equalsIgnoreCase("TCASH"))
                {
                    isTCASH = true;
                    layout_btn_resend.setVisibility(View.GONE);
                    if (TCASH_hp_validation)
                    {
                        layoutTCASH.setVisibility(View.VISIBLE);
                        layout_OTP.setVisibility(View.GONE);
                    }
                    else {
                        requestResendToken(true);
                        btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");
                        btnResend.setOnClickListener(resendListener);
                    }
                }
            }

            if(benef_product_type.equalsIgnoreCase(DefineValue.ACCT))
                tvNoDestination.setText(R.string.number_destination);
            else
                tvNoDestination.setText(R.string.number_hp_destination);



            btnBack.setOnClickListener(backListener);
            btnSubmit.setOnClickListener(submitListener);
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    private void initializeSmsClass(){
        if(smSclass == null)
            smSclass = new SMSclass(getActivity(), CustomSimcardListener);

        smSclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if(!isExist){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    SimNotExitAction();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    private BroadcastReceiver CustomSimcardListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
                if(intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")){
                    SimNotExitAction();
                }

            }
        }
    };

    private void SimNotExitAction(){
        if(isSMS) {
            Toast.makeText(getActivity(), R.string.smsclass_simcard_listener_absent_toast, Toast.LENGTH_LONG).show();
            MyApiClient.CancelRequestWS(getActivity(),true);
            getFragmentManager().popBackStack();
        }
    }

    Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getFragmentManager().getBackStackEntryCount() > 0)
                getFragmentManager().popBackStack();
            else
                getActivity().finish();
        }
    };

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                btnSubmit.setEnabled(false);
                if(isSMS) {
                    if(inputValidation())
                        sentInsertTransTopup(tokenValue.getText().toString());
                    else
                        btnSubmit.setEnabled(true);
                }
                else if(isIB){
                    changeToSGOPlus(tx_id,tx_product_code, tx_product_name,tx_bank_code, amount, fee, total_amount, tx_bank_name);
                }
                else if(isPIN) {
                    CallPINinput(attempt);
                    btnSubmit.setEnabled(true);
                }
                else if (isTCASH)
                {
                    btnSubmit.setEnabled(true);
                    if (validasiNoHP())
                    {
                        noHpTCASH.setEnabled(false);
                        btnSubmit.setEnabled(true);
                        layout_OTP.setVisibility(View.VISIBLE);
                        layout_btn_resend.setVisibility(View.GONE);
                        validasiNomor = true;
//                        if(InetHandler.isNetworkAvailable(getActivity())){
//                            requestResendToken();
//                        }
//                        else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
                        btnSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (inputValidation())
                                {
                                    sentInsertTransTopup(tokenValue.getText().toString());
                                    btnSubmit.setEnabled(true);

                                }
                            }
                        });
                    }
                }
                else btnSubmit.setEnabled(true);
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                btnSubmit.setEnabled(false);
                btnResend.setEnabled(false);
                if(max_token_resend!=0)
                    requestResendToken(false);
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainPage.REQUEST_FINISH){
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                sentInsertTransTopup(value_pin);
            }
        }
    }

    private void CallPINinput(int _attempt){
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if(_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT,_attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private void sentInsertTransTopup(String token){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_INSERT_TRANS_TOPUP,
                    userID,accessKey);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, tx_product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            params.put(WebParams.PRODUCT_VALUE, token);
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params insertTrxSGOL:" + params.toString());

            MyApiClient.sentInsertTransTopup(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response insertTrxSGOL:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            getActivity().setResult(MainPage.RESULT_BALANCE);

                            getTrxStatusBBS(sp.getString(DefineValue.USER_NAME, ""),  tx_id,userID);

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            btnSubmit.setEnabled(true);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if(isPIN){
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                if(message.equals("PIN tidak sesuai")) {
                                    Intent i = new Intent(getActivity(), InsertPIN.class);

                                    attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                    failed = response.optInt(WebParams.MAX_FAILED, 0);

                                    if (attempt != -1)
                                        i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                                }
                                else {
                                    onOkButton();
                                }
                            }
                            else if(isSMS){
                                if(!code.equals(ErrorDefinition.ERROR_CODE_WRONG_TOKEN))
                                    getFragmentManager().popBackStack();
                                code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        }
                        progdialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btnSubmit.setEnabled(true);
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
                    btnSubmit.setEnabled(true);
                    Timber.w("Error Koneksi insert trx:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void requestResendToken(final boolean isRequestOTP){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params;
            if(isRequestOTP)
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                        userID,accessKey);
            else
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_RESEND_TOKEN_SGOL,
                        userID,accessKey);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, tx_product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            if(noHpTCASH!=null)
                params.put(WebParams.PRODUCT_VALUE, noHpTCASH.getText().toString());

            Timber.d("isi params resendTokenSGOL:"+params.toString());

            JsonHttpResponseHandler handler =  new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if(!isRequestOTP) {
                                max_token_resend = max_token_resend - 1;
                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();
                            }

                            changeTextBtnSub();
                            layout_OTP.setVisibility(View.VISIBLE);
                            Timber.w("txid response resend tokenSGOL:"+response.toString());
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            Timber.d("Error resendTokenSGOL:"+response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                            if(isRequestOTP)
                                getFragmentManager().popBackStack();

                        }
                        progdialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    btnSubmit.setEnabled(true);
                    btnResend.setEnabled(true);
                    if(max_token_resend == 0 ){
                        btnResend.setEnabled(false);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_SHORT).show();
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
                    btnSubmit.setEnabled(true);
                    Timber.w("Error Koneksi resend token topuptoken:"+throwable.toString());
                }
            };

            if(isRequestOTP)
                MyApiClient.sentDataReqTokenSGOL(getActivity(),params,handler);
            else
                MyApiClient.sentResendTokenSGOL(getActivity(),params,handler);
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void getTrxStatusBBS(final String userName, final String txId, final String userId){
        try{
            final ProgressDialog out = DefinedDialog.CreateProgressDialog(getActivity(), getString(R.string.check_status));
            out.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_TRX_STATUS_BBS,
                    userId,accessKey);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.USER_ID, userId);

            Timber.d("isi params sent get Trx Status bbs:"+params.toString());

            MyApiClient.sentGetTRXStatusBBS(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        out.dismiss();
                        Timber.d("isi response sent get Trx Status bbs:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                            String txstatus = response.getString(WebParams.TX_STATUS);

                            showReportBillerDialog(userName, DateTimeFormat.formatToID(response.optString(WebParams.CREATED,"")),
                                    txId, userId,response.optString(WebParams.TX_BANK_NAME,""),response.optString(WebParams.PRODUCT_NAME,""),
                                    response.optString(WebParams.ADMIN_FEE,"0"),response.optString(WebParams.TX_AMOUNT,"0"),
                                    txstatus,response.getString(WebParams.TX_REMARK), response.optString(WebParams.TOTAL_AMOUNT,"0"),
                                    response.optString(WebParams.MEMBER_NAME,""),response.optString(WebParams.SOURCE_BANK_NAME,""),
                                    response.optString(WebParams.SOURCE_ACCT_NO,""),response.optString(WebParams.SOURCE_ACCT_NAME,""),
                                    response.optString(WebParams.BENEF_BANK_NAME,""),response.optString(WebParams.BENEF_ACCT_NO,""),
                                    response.optString(WebParams.BENEF_ACCT_NAME,""),response.optString(WebParams.BENEF_ACCT_TYPE),
                                    response.optString(WebParams.PRODUCT_NAME, ""));
                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            String msg = response.getString(WebParams.ERROR_MESSAGE);
                            showDialog(msg);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    btnSubmit.setEnabled(true);
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
                    if(out.isShowing())
                        out.dismiss();
                    btnSubmit.setEnabled(true);
                    Timber.w("Error Koneksi trx status bbs:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
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

    private void showReportBillerDialog(String userName, String date, String txId, String userId, String bankName, String bankProduct,
                                        String fee, String amount, String txStatus, String txRemark, String total_amount, String member_name,
                                        String source_bank_name, String source_acct_no, String source_acct_name,
                                        String benef_bank_name, String benef_acct_no, String benef_acct_name, String benef_type, String product_name) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = new ReportBillerDialog();
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.BANK_NAME, bankName);
        args.putString(DefineValue.BANK_PRODUCT, bankProduct);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

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
        args.putString(DefineValue.MEMBER_NAME, member_name);
        args.putString(DefineValue.SOURCE_ACCT, source_bank_name);
        args.putString(DefineValue.SOURCE_ACCT_NO, source_acct_no);
        args.putString(DefineValue.SOURCE_ACCT_NAME, source_acct_name);
        args.putString(DefineValue.BANK_BENEF, benef_bank_name);
        args.putString(DefineValue.TYPE_BENEF, benef_type);
        args.putString(DefineValue.NO_BENEF, benef_acct_no);
        args.putString(DefineValue.NAME_BENEF, benef_acct_name);
        args.putString(DefineValue.PRODUCT_NAME, product_name);

        dialog.setArguments(args);
        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }


    @Override
    public void onResume() {
        super.onResume();
        Timber.wtf("masuk onResume");
        if(isSMS)
            toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isSMS)
            toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on){
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity) getActivity();
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
        Timber.d("isi _kode_otp, _member_kode, member kode session:"+ _kode_otp+ " / " +_member_kode +" / "+ sp.getString(DefineValue.MEMBER_CODE,""));
//        if(_member_kode.equals(sp.getString(CoreApp.MEMBER_CODE,""))){
        tokenValue.setText(_kode_otp);
//        }
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
        i.putExtra(DefineValue.SHARE_TYPE,share_type);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        i.putExtra(DefineValue.CALLBACK_URL,callback_url);
        i.putExtra(DefineValue.API_KEY, api_key);
//        i.putExtra(DefineValue.IS_FACEBOOK,isFacebook);

        i.putExtra(DefineValue.TOTAL_AMOUNT,totalAmount);
        i.putExtra(DefineValue.COMMUNITY_ID, comm_id);
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN);
        btnSubmit.setEnabled(true);
        actionListener.ChangeActivityFromCashInConfirm(i);
    }

    public void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms)+" ("+max_token_resend+")");
            }
        });
    }

    public boolean validasiNoHP()
    {
        if(layoutTCASH.getVisibility() == View.VISIBLE) {
            if (noHpTCASH.getText().toString().length() == 0) {
                noHpTCASH.requestFocus();
                noHpTCASH.setError("No. Handphone dibutuhkan!");
                return false;
            }
        }

        if(layout_OTP.getVisibility() == View.VISIBLE) {
            if (tokenValue.getText().toString().length() == 0) {
                tokenValue.requestFocus();
                tokenValue.setError(getString(R.string.regist2_validation_otp));
                return false;
            }
        }
        else if(noHpTCASH.getText().toString().length()<5){
            noHpTCASH.requestFocus();
            noHpTCASH.setError("No. Handphone dibutuhkan!");
            return false;
        }
        tokenValue.setVisibility(View.VISIBLE);
        return true;
    }

    public boolean inputValidation(){
        if(tokenValue.getText().toString().length()==0){
            tokenValue.requestFocus();
            tokenValue.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
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

    @Override
    public void onOkButton() {
        toFragAmount();
    }

    public void setToStatus(String _tx_status) {
        finishTransaction = true;
        tx_status = _tx_status;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(finishTransaction) {
            toFragAmount();
        }
    }

    private void toFragAmount() {
        Fragment mFrag = new BBSTransaksiAmount();
        Bundle args = new Bundle();
        args.putString(DefineValue.TRANSACTION, transaksi);
//            args.putString(DefineValue.TX_STATUS, tx_status);
        mFrag.setArguments(args);
        getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction()
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, BBSTransaksiAmount.TAG)
                .addToBackStack(TAG).commit();
    }
}
