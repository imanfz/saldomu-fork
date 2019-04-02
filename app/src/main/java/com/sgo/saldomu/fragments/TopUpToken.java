package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.InputFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
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
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;

import timber.log.Timber;

/*
  Created by Administrator on 12/10/2014.
 */
public class TopUpToken extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    private String txID;
    private String productCode;
    private String productName;
    private String commCode;
    private String phoneDestination;
    private String bankName;
    private String jumlahnya;
    private String bankCode;
    private String topupType;
    private String fee;
    private String shareType;
    private int max_length_token = 6;
    private EditText tokenValue;
    private TextView mBankName;
    private TextView mBankProduct;
    private TextView mAmount;
    private TextView mBankChannel;
    private TextView mPhoneNumber;
    private Button btnSubmit;
    private Button btnCancel;
    private Button btnResend;
    private ProgressDialog progdialog;
    private Boolean isIB = false;

    private LinearLayout emoneyLayout;
    private LinearLayout pulsaLayout;
    private int max_token_resend = 3;
    private View v;
    private View layout_btn_resend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_topup_token, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        topupType = args.getString(DefineValue.TOPUP_TYPE, "");

        txID = args.getString(DefineValue.TX_ID, "");
        productCode = args.getString(DefineValue.PRODUCT_CODE, "");
        productName = args.getString(DefineValue.PRODUCT_NAME, "");
        commCode = args.getString(DefineValue.COMMUNITY_CODE, "");
        jumlahnya = args.getString(DefineValue.AMOUNT, "");
        phoneDestination = args.getString(DefineValue.PRODUCT_VALUE, "");
        shareType = args.getString(DefineValue.SHARE_TYPE, "");
        String ccy_id = args.getString(DefineValue.CCY_ID, "");

        Timber.d("isi args:" + args.toString());

        mAmount = v.findViewById(R.id.reqTopup_amount);
        mAmount.setText(ccy_id + ". " + CurrencyFormat.format(jumlahnya));
        tokenValue = v.findViewById(R.id.reqTopup_token_value);
        btnSubmit = v.findViewById(R.id.reqTopup_btn_verification);
        btnCancel = v.findViewById(R.id.reqTopup_btn_cancel);
        btnResend = v.findViewById(R.id.reqTopup_btn_resend_token);
        layout_btn_resend = v.findViewById(R.id.layout_btn_resend);

        tokenValue.requestFocus();

        btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");

        btnResend.setOnClickListener(resendListener);
        btnSubmit.setOnClickListener(submitListener);
        btnCancel.setOnClickListener(cancelListener);

        if (topupType.equals(DefineValue.EMONEY))
            initializeEmoney(args);
        else
            initializePulsa(args);
    }

    private void initializeEmoney(Bundle _args) {

        bankName = _args.getString(DefineValue.BANK_NAME, "");
        bankCode = _args.getString(DefineValue.BANK_CODE, "");
        boolean is_sms_banking = _args.getBoolean(DefineValue.IS_SMS_BANKING, false);
        String ccy_id = _args.getString(DefineValue.CCY_ID, "");

        fee = _args.getString(DefineValue.FEE);
        View layout_fee = v.findViewById(R.id.reqTopup_layout_fee);
        TextView tv_fee = v.findViewById(R.id.reqTopup_bank_fee);
        TextView tv_total_fee = v.findViewById(R.id.reqTopup_bank_total_fee);
        layout_fee.setVisibility(View.VISIBLE);
        int total_amount = Integer.parseInt(jumlahnya) + Integer.parseInt(fee);
        tv_fee.setText(ccy_id + ". " + CurrencyFormat.format(fee));
        tv_total_fee.setText(ccy_id + ". " + CurrencyFormat.format(total_amount));

        if (is_sms_banking) {
            isIB = false;
            if (bankCode.equals("114")) {
                max_length_token = 5;
                tokenValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length_token)});
            }
        } else {
            View etToken_layout = v.findViewById(R.id.reqTopup_edittext_token);
            etToken_layout.setVisibility(View.GONE);
            layout_btn_resend.setVisibility(View.GONE);
            isIB = true;
        }

        emoneyLayout = v.findViewById(R.id.topup_token_layout_emoney);
        emoneyLayout.setVisibility(View.VISIBLE);
        mBankName = v.findViewById(R.id.reqTopup_bank_name);
        mBankName.setText(bankName);
        mBankProduct = v.findViewById(R.id.reqTopup_bank_product);
        mBankProduct.setText(productName);
    }

    private void initializePulsa(Bundle _args) {
        String _bankChannel = _args.getString(DefineValue.BANK_CHANNEL, "");

        pulsaLayout = v.findViewById(R.id.topup_token_layout_pulsa);
        pulsaLayout.setVisibility(View.VISIBLE);
        mBankChannel = v.findViewById(R.id.reqTopup_bank_channel);
        mBankChannel.setText(_bankChannel);
        mPhoneNumber = v.findViewById(R.id.reqTopup_phone_number);
        mPhoneNumber.setText(phoneDestination);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                btnSubmit.setEnabled(false);
                if (isIB)
                    changeToSGOPlus(txID, productCode, productName, commCode);
                else if (inputValidation()) {
                    sentInsertTransTopup();
                } else btnSubmit.setEnabled(true);
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                btnResend.setEnabled(false);
                if (max_token_resend != 0) requestResendToken();
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");
            }
        });
    }

    private void changeToSGOPlus(String _tx_id, String _product_code, String _product_name, String _comm_code) {

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, bankCode);
        i.putExtra(DefineValue.BANK_NAME, bankName);
        i.putExtra(DefineValue.PRODUCT_NAME, _product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.COMMUNITY_CODE, _comm_code);
        i.putExtra(DefineValue.TX_ID, _tx_id);
        i.putExtra(DefineValue.AMOUNT, jumlahnya);
        i.putExtra(DefineValue.SHARE_TYPE, shareType);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        i.putExtra(DefineValue.CALLBACK_URL, sp.getString(DefineValue.CALLBACK_URL_TOPUP, ""));
        i.putExtra(DefineValue.API_KEY, sp.getString(DefineValue.API_KEY_TOPUP, ""));

        double totalAmount = Double.parseDouble(jumlahnya) + Double.parseDouble(fee);
        i.putExtra(DefineValue.TOTAL_AMOUNT, String.valueOf(totalAmount));
        i.putExtra(DefineValue.COMMUNITY_ID, sp.getString(DefineValue.COMMUNITY_ID, ""));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        btnSubmit.setEnabled(true);
        switchActivityIB(i);
    }

    private void sentInsertTransTopup() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = txID + commCode + productCode + tokenValue;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP);
            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(tokenValue.getText().toString()));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                getActivity().setResult(MainPage.RESULT_BALANCE);

                                getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txID, userPhoneID,
                                        bankName, productName, fee, jumlahnya);

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {

                                if (!code.equals(ErrorDefinition.ERROR_CODE_WRONG_TOKEN))
                                    getFragmentManager().popBackStack();
                                code = model.getError_code() + ":" + model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            btnSubmit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager().popBackStack();
        }
    };

    private void requestResendToken() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = txID + commCode + productCode;

            HashMap<String, Object> params;
            String url;

            if (bankCode.equals("114")) {
//                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_REQ_TOKEN_SGOL,
//                        userPhoneID, accessKey, extraSignature);
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);
                url = MyApiClient.LINK_REQ_TOKEN_SGOL;
            } else {
//                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_RESEND_TOKEN_SGOL,
//                        userPhoneID, accessKey);
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RESEND_TOKEN_SGOL, extraSignature);
                url = MyApiClient.LINK_RESEND_TOKEN_SGOL;
            }

            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params resendTokenSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                max_token_resend = max_token_resend - 1;

                                changeTextBtnSub();
                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                code = model.getError_message();

                                Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            btnSubmit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }


    }


    private void getTrxStatus(final String userName, final String txId, final String userId, final String bankName, final String bankProduct,
                              final String fee, final String amount) {
        try {
            final ProgressDialog out = DefinedDialog.CreateProgressDialog(getActivity(), getString(R.string.check_status));
            out.show();

            extraSignature = txId + MyApiClient.COMM_ID;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.TYPE, DefineValue.TOPUP_SMS_TYPE);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userId);

            Timber.d("isi params sent get Trx Status:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                String txstatus = model.getTx_status();

                                showReportBillerDialog(userName, model, txId, userId, bankName, bankProduct, fee, amount,
                                        txstatus);
//                                        ,response.getString(WebParams.TX_REMARK), response.optString(WebParams.BUSS_SCHEME_CODE),
//                                        response.optString(WebParams.BUSS_SCHEME_NAME));
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                showDialog(message);
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
                    }
            );
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showDialog(String msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

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

    private void showReportBillerDialog(String userName, GetTrxStatusReportModel model, String txId, String userId, String bankName, String bankProduct,
                                        String fee, String amount, String txStatus
//            , String txRemark, String buss_scheme_code,
//                                        String buss_scheme_name
    ) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.getCreated()));
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.BANK_NAME, bankName);
        args.putString(DefineValue.BANK_PRODUCT, bankProduct);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));

        double dAmount = Double.valueOf(amount);
        double dFee = Double.valueOf(fee);
        double total_amount = dAmount + dFee;

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, model.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.getBuss_scheme_name());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }


    @Override
    public void onResume() {
        super.onResume();
        Timber.wtf("masuk onResume");
        if (!isIB)
            toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isIB)
            toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on) {
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.togglerBroadcastReceiver(_on, myReceiver);
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle mBundle = intent.getExtras();
            SmsMessage[] mSMS;
            String strMessage = "";
            String _kode_otp = "";
            String _member_code = "";
            String[] kode = context.getResources().getStringArray(R.array.broadcast_kode_compare);

            if (mBundle != null) {
                Object[] pdus = (Object[]) mBundle.get("pdus");
                assert pdus != null;
                mSMS = new SmsMessage[pdus.length];

                for (int i = 0; i < mSMS.length; i++) {
                    mSMS[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    strMessage += mSMS[i].getMessageBody();
                    strMessage += "\n";
                }

                String[] words = strMessage.split(" ");
                for (int i = 0; i < words.length; i++) {
                    if (_kode_otp.equalsIgnoreCase("")) {
                        if (words[i].equalsIgnoreCase(kode[0])) {
                            if (words[i + 1].equalsIgnoreCase(kode[1]))
                                _kode_otp = words[i + 2];
                            _kode_otp = _kode_otp.replace(".", "").replace(" ", "");
                        }
                    }

                    if (_member_code.equals("")) {
                        if (words[i].equalsIgnoreCase(kode[2]))
                            _member_code = words[i + 1];
                    }
                }

                insertTokenEdit(_kode_otp, _member_code);
                //Toast.makeText(context,strMessage,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void insertTokenEdit(String _kode_otp, String _member_kode) {
        Timber.d("isi _kode_otp, _member_kode, member kode session:" + _kode_otp + " / " + _member_kode + " / " + sp.getString(DefineValue.MEMBER_CODE, ""));
//        if(_member_kode.equals(sp.getString(CoreApp.MEMBER_CODE,""))){
        tokenValue.setText(_kode_otp);
//        }
    }

    private void switchActivityIB(Intent mIntent) {
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private boolean inputValidation() {
        if (tokenValue.getText().toString().length() == 0) {
            tokenValue.requestFocus();
            tokenValue.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        /*if(tokenValue.getText().toString().length()<max_length_token){
            tokenValue.requestFocus();
            tokenValue.setError(this.getString(R.string.otp_validation_lenght_1) +" "+ max_length_token +" "+ getString(R.string.otp_validation_lenght_2));
            return false;
        }*/
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
    public void onOkButton() {
        getFragmentManager().popBackStack();
    }
}