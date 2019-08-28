package com.sgo.saldomu.fragments;
/*
  Created by Administrator on 1/30/2017.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

public class CashOutBBS_confirm_agent extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    public final static String TAG = "com.sgo.saldomu.fragments.CashOutBBS_confirm_agent";
    private ProgressDialog progdialog;
    private View v, layout_OTP;
    private TextView tvSourceAcct, tvBankBenef, tvAmount, tvUserIdSource, tvRemark, tvUserIdTitle, tvKode, tv_amount, tvFee, tvTotal;
    private EditText tokenValue;
    private Button btnSubmit;
    private String userID, accessKey, comm_code, tx_product_code, source_product_type,
            source_product_h2h, api_key, callback_url, tx_bank_code, tx_bank_name, tx_product_name,
            tx_id, amount, share_type, comm_id, benef_product_name,
            userId_source, remark, source_product_name, transaksi, fee, totalAmount, additionalFee, benef_bank_code;
    private Boolean retryToken = false;
    private TextView tv_additionalFee;
    private Switch favoriteSwitch;
    private EditText notesEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.cashoutbbs_confirm_agent, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        CircleStepView mCircleStepView = (v.findViewById(R.id.circle_step_view));
        mCircleStepView.setTextBelowCircle("", "", getString(R.string.konfirmasi));
        mCircleStepView.setCurrentCircleIndex(2, false);

        TextView tvTitle = v.findViewById(R.id.tv_title);
        tvSourceAcct = v.findViewById(R.id.bbscashout_value_source);
        tvUserIdTitle = v.findViewById(R.id.tv_user_id);
        tvUserIdSource = v.findViewById(R.id.bbscashout_value_user_id);
        tvBankBenef = v.findViewById(R.id.bbscashout_value_benef);
        tvAmount = v.findViewById(R.id.bbscashout_value_amount);
        tv_amount = v.findViewById(R.id.tv_bbscashout_value_amount);
        tvFee = v.findViewById(R.id.bbscashout_value_fee);
        tvTotal = v.findViewById(R.id.bbscashout_value_total);
        tvRemark = v.findViewById(R.id.bbscashout_value_remark);
        tvKode = v.findViewById(R.id.tv_kode);
        btnSubmit = v.findViewById(R.id.btn_submit);
        layout_OTP = v.findViewById(R.id.layout_OTP);
        tokenValue = v.findViewById(R.id.bbscashout_value_token);
        tv_additionalFee = v.findViewById(R.id.bbscashout_additional_fee);
        favoriteSwitch = v.findViewById(R.id.favorite_switch);
        notesEditText = v.findViewById(R.id.notes_edit_text);
        Button btnBack = v.findViewById(R.id.btn_back);

        Bundle bundle = getArguments();
        Log.e("mantul : ", bundle.toString());

        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
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
            totalAmount = bundle.getString(DefineValue.TOTAL_AMOUNT);
            share_type = bundle.getString(DefineValue.SHARE_TYPE);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID);
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            userId_source = bundle.getString(DefineValue.USER_ID);
            remark = bundle.getString(DefineValue.REMARK);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);
            additionalFee = bundle.getString(DefineValue.ADDITIONAL_FEE,"0");

            benef_bank_code = bundle.getString(DefineValue.BENEF_BANK_CODE,"");

            if (source_product_h2h.equalsIgnoreCase("Y") && !tx_product_code.equalsIgnoreCase("MANDIRILKD")) {
                tvUserIdTitle.setText(getString(R.string.no_member));
            } else {
                tvUserIdTitle.setText(getString(R.string.no_rekening));
            }
            if (tx_product_code.equalsIgnoreCase("TCASH") || tx_product_code.equalsIgnoreCase("MANDIRILKD")) {
                tvKode.setText("Kode OTP");
            }
            tvTitle.setText(transaksi);
            tvAmount.setText(CurrencyFormat.format(amount));
            tv_amount.setText(CurrencyFormat.format(amount));
            tvFee.setText(CurrencyFormat.format(fee));
            tvTotal.setText(CurrencyFormat.format(totalAmount));
            tv_additionalFee.setText(CurrencyFormat.format(additionalFee));
            tvBankBenef.setText(benef_product_name);
            tvUserIdSource.setText(userId_source);
            tvRemark.setText(remark);
            tvSourceAcct.setText(source_product_name);

            layout_OTP.setVisibility(View.VISIBLE);
            tokenValue.requestFocus();
            btnBack.setOnClickListener(backListener);
            btnSubmit.setOnClickListener(submitListener);
        } else {
            getFragmentManager().popBackStack();
        }

        favoriteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notesEditText.setEnabled(isChecked);
        });

    }

    Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getFragmentManager().getBackStackEntryCount() > 0)
                getFragmentManager().popBackStack();
            else
                getActivity().finish();
        }
    };

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (favoriteSwitch.isChecked() && notesEditText.getText().toString().length() == 0) {
                    notesEditText.requestFocus();
                    notesEditText.setError(getString(R.string.payfriends_notes_zero));
                    return;
                }

                btnSubmit.setEnabled(false);
                if (retryToken) {
                    if (inputValidation())
                        sentRetryToken();
                } else {
                    if (inputValidation()) {
                        if (favoriteSwitch.isChecked()) {
                            onSaveToFavorite();
                        } else {
                            sentInsertTransTopup(tokenValue.getText().toString());
                        }
                    }else {
                        btnSubmit.setEnabled(true);
                    }
                }

            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private void sentInsertTransTopup(String token) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = tx_id + comm_code + tx_product_code + token;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP, extraSignature);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, tx_product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(token));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params insertTrxSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            progdialog.dismiss();

                            jsonModel response = getGson().fromJson(object, jsonModel.class);

                            String code = response.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0288")) {
                                getActivity().setResult(MainPage.RESULT_BALANCE);

                                getTrxStatusBBS(sp.getString(DefineValue.USER_NAME, ""), tx_id, userID);

                            }
//                        else if(code.equals("0288")){
//                            Timber.d("isi error insertTrxSGOL:"+response.toString());
//                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
//                            Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
//                            tokenValue.setText("");
//                            retryToken=true;
//                            btnSubmit.setEnabled(true);
//                        }
                            else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = response.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals("0049")) {
                                String message = response.getError_message();
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            } else if (code.equals("0061")) {
                                String message = response.getError_message();
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                tokenValue.setText("");
                            } else {
//                            btnSubmit.setEnabled(true);
//                            String message = response.getString(WebParams.ERROR_MESSAGE);
//                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                getActivity().setResult(MainPage.RESULT_BALANCE);

                                getTrxStatusBBS(sp.getString(DefineValue.USER_NAME, ""), tx_id, userID);
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

    private void sentRetryToken() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RETRY_TOKEN);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(tokenValue.getText().toString()));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params sentRetryToken:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_RETRY_TOKEN, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel response = getGson().fromJson(object, jsonModel.class);

                            String code = response.getError_code();
                            Timber.d("isi response sentRetryToken:" + response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                getActivity().setResult(MainPage.RESULT_BALANCE);

                                getTrxStatusBBS(sp.getString(DefineValue.USER_NAME, ""), tx_id, userID);

                            } else if (code.equals("0288")) {
                                Timber.d("isi error sent retry token:" + response.toString());
                                String code_msg = response.getError_message();
                                Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
                                tokenValue.setText("");
                                retryToken = true;
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:" + response.toString());
                                String message = response.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                Timber.d("isi error sentRetryToken:" + response.toString());
                                String code_msg = response.getError_message();
                                Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
                                progdialog.dismiss();
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

    public void getTrxStatusBBS(final String userName, final String txId, final String userId) {
        try {
            final ProgressDialog out = DefinedDialog.CreateProgressDialog(getActivity(), getString(R.string.check_status));
            out.show();
            extraSignature = txId + comm_code;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_STATUS_BBS, extraSignature);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.USER_ID, userId);

            Timber.d("isi params sent get Trx Status bbs:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_STATUS_BBS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetTrxStatusReportModel response = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            Timber.d("isi response sent get Trx Status bbs:" + response.toString());
                            String code = response.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                String txstatus = response.getTx_status();

                                showReportBillerDialog(userName,
//                                            DateTimeFormat.formatToID(response.optString(WebParams.CREATED,"")),
                                        txId, userId,
//                                            response.optString(WebParams.TX_BANK_NAME,""),response.optString(WebParams.PRODUCT_NAME,""),
//                                            response.optString(WebParams.ADMIN_FEE,"0"),response.optString(WebParams.TX_AMOUNT,"0"),
                                        txstatus, response);
//                                            response.getString(WebParams.TX_REMARK), response.optString(WebParams.TOTAL_AMOUNT,"0"),
//                                            response.optString(WebParams.MEMBER_NAME,""),response.optString(WebParams.SOURCE_BANK_NAME,""),
//                                            response.optString(WebParams.SOURCE_ACCT_NO,""),response.optString(WebParams.SOURCE_ACCT_NAME,""),
//                                            response.optString(WebParams.BENEF_BANK_NAME,""),response.optString(WebParams.BENEF_ACCT_NO,""),
//                                            response.optString(WebParams.BENEF_ACCT_NAME,""), response.optString(WebParams.MEMBER_SHOP_PHONE,""),
//                                            response.optString(WebParams.MEMBER_SHOP_NAME,""), response.optString(WebParams.BUSS_SCHEME_CODE),
//                                            response.optString(WebParams.BUSS_SCHEME_NAME), response.optString((WebParams.MEMBER_SHOP_NO),""));
                            } else if (code.equals("0288")) {
                                Timber.d("isi error trx status bbs:" + response.toString());
                                String code_msg = response.getError_message();
                                Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
                                tokenValue.setText("");
                                retryToken = true;
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:" + response.toString());
                                String message = response.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                String msg = response.getError_message();
                                showDialog(msg);
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (out.isShowing())
                                out.dismiss();
                            btnSubmit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
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
        Button btnDialogOTP = (Button) dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView) dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView) dialog.findViewById(R.id.message_dialog);

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

    private void showReportBillerDialog(String userName,
//                                        String date,
                                        String txId, String userId,
//                                         String bankName, String bankProduct,
//                                        String fee, String amount,
                                        String txStatus,
// String txRemark, String total_amount, String member_name,
//                                        String source_bank_name, String source_acct_no, String source_acct_name,
//                                        String benef_bank_name, String benef_acct_no, String benef_acct_name, String member_shop_phone,
//                                        String member_shop_name, String buss_scheme_code, String buss_scheme_name, String member_shop_no,
                                        GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdditional_fee()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

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
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());
        args.putString(DefineValue.MEMBER_NAME, response.getMember_name());
        args.putString(DefineValue.SOURCE_ACCT, response.getSource_bank_name());
        args.putString(DefineValue.SOURCE_ACCT_NO, response.getSource_acct_no());
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.getSource_acct_name());
        args.putString(DefineValue.BANK_BENEF, response.getBenef_bank_name());
        args.putString(DefineValue.NO_BENEF, response.getBenef_acct_no());
        args.putString(DefineValue.NAME_BENEF, response.getBenef_acct_name());
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.getMember_shop_phone());
        args.putString(DefineValue.MEMBER_SHOP_NAME, response.getMember_shop_name());
        args.putString(DefineValue.PRODUCT_NAME, response.getProduct_name());
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.MEMBER_SHOP_NO, response.getMember_shop_no());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    public boolean inputValidation() {
        if (tokenValue.getText().toString().length() == 0) {
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

    private void onSaveToFavorite() {
        extraSignature = NoHPFormat.formatTo62(userId_source) + source_product_type + "BBS";
        Log.e("extraSignature params ", extraSignature);
        String url = MyApiClient.LINK_TRX_FAVORITE_SAVE;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(url, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.PRODUCT_TYPE, source_product_type);
        params.put(WebParams.CUSTOMER_ID, NoHPFormat.formatTo62(userId_source));
        params.put(WebParams.TX_FAVORITE_TYPE, "BBS");
        params.put(WebParams.COMM_ID, comm_id);
        params.put(WebParams.NOTES, notesEditText.getText().toString());
        params.put(WebParams.BENEF_BANK_CODE, benef_bank_code);

        params.put(WebParams.SOURCE_BANK_CODE, tx_bank_code);

        Log.e("params ", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            Log.e("onResponses ", response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("onResponses ", throwable.getLocalizedMessage());
                        btnSubmit.setEnabled(true);
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        sentInsertTransTopup(tokenValue.getText().toString());
                    }
                });
    }
}
