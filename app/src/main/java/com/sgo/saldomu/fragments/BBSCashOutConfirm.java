package com.sgo.saldomu.fragments;
/*
  Created by Administrator on 1/30/2017.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

public class BBSCashOutConfirm extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    public final static String TAG = "com.sgo.saldomu.fragments.CashOutBBS";
    private ProgressDialog progdialog;
    private View v, layout_OTP;
    private TextView tvSourceAcct, tvBankBenef, tvAmount, tvUserIdSource, tvRemark, tvUserIdTitle, tvKode, tv_amount, tvFee, tvTotal;
    private EditText tokenValue;
    private Button btnSubmit;
    private String userID, comm_code, tx_product_code, source_product_type,
            source_product_h2h, tx_bank_code,
            tx_id, amount, comm_id, benef_product_name,
            userId_source, remark, source_product_name, transaksi, fee, totalAmount, source_product_code;
    private Boolean retryToken = false;
    private SwitchCompat favoriteSwitch;
    private EditText notesEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.bbs_cashout_confirm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

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
        favoriteSwitch = v.findViewById(R.id.favorite_switch);
        notesEditText = v.findViewById(R.id.notes_edit_text);
        Button btnBack = v.findViewById(R.id.btn_back);

        Bundle bundle = getArguments();
        Timber.tag("mantul : ").e(bundle.toString());

        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            source_product_h2h = bundle.getString(DefineValue.PRODUCT_H2H);
            source_product_type = bundle.getString(DefineValue.PRODUCT_TYPE);
            tx_product_code = bundle.getString(DefineValue.PRODUCT_CODE);
            tx_bank_code = bundle.getString(DefineValue.BANK_CODE);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            tx_id = bundle.getString(DefineValue.TX_ID);
            amount = bundle.getString(DefineValue.AMOUNT);
            fee = bundle.getString(DefineValue.FEE);
            totalAmount = bundle.getString(DefineValue.TOTAL_AMOUNT);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID);
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            userId_source = bundle.getString(DefineValue.USER_ID);
            remark = bundle.getString(DefineValue.REMARK);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);
            source_product_code = bundle.getString(DefineValue.SOURCE_PRODUCT_CODE);

            if (source_product_h2h.equalsIgnoreCase(DefineValue.STRING_YES) && !tx_product_code.equalsIgnoreCase("MANDIRILKD")) {
                tvUserIdTitle.setText(getString(R.string.no_member));
            } else {
                tvUserIdTitle.setText(getString(R.string.no_rekening));
            }
            if (tx_product_code.equalsIgnoreCase("TCASH") || tx_product_code.equalsIgnoreCase("MANDIRILKD")) {
                tvKode.setText("Kode OTP");
            }
            tvAmount.setText(CurrencyFormat.format(amount));
            tv_amount.setText(CurrencyFormat.format(amount));
            tvFee.setText(CurrencyFormat.format(fee));
            tvTotal.setText(CurrencyFormat.format(totalAmount));
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
            notesEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            notesEditText.setEnabled(isChecked);
        });
    }

    Button.OnClickListener backListener = view -> {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else
            getActivity().finish();
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
                    } else {
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

            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = tx_id + comm_code + tx_product_code + token;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, tx_product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncryptCommID(comm_id, uuid, dateTime, userID, token, subStringLink));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params insertTrxSGOL:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
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
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
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
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void sentRetryToken() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String link = MyApiClient.LINK_RETRY_TOKEN;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            String token = tokenValue.getText().toString();
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(uuid, dateTime, userID, token, subStringLink));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params sentRetryToken:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel response = getGson().fromJson(object, jsonModel.class);

                            String code = response.getError_code();
                            String message = response.getError_message();
                            Timber.d("isi response sentRetryToken:%s", response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                getActivity().setResult(MainPage.RESULT_BALANCE);

                                getTrxStatusBBS(sp.getString(DefineValue.USER_NAME, ""), tx_id, userID);

                            } else if (code.equals("0288")) {
                                Timber.d("isi error sent retry token:%s", response.toString());
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                tokenValue.setText("");
                                retryToken = true;
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:%s", response.toString());
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", object.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Timber.d("isi error sentRetryToken:%s", response.toString());
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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
            Timber.d("httpclient:%s", e.getMessage());
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

            Timber.d("isi params sent get Trx Status bbs:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_STATUS_BBS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetTrxStatusReportModel response = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            Timber.d("isi response sent get Trx Status bbs:%s", response.toString());
                            String code = response.getError_code();
                            String message = response.getError_message();
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
                                Timber.d("isi error trx status bbs:%s", response.toString());
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                tokenValue.setText("");
                                retryToken = true;
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:%s", response.toString());
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                showDialog(message);
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
            Timber.d("httpclient:%s", e.getMessage());
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

        btnDialogOTP.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showReportBillerDialog(String userName,
                                        String txId, String userId,
                                        String txStatus,
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
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
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
        toFragCashOut();
    }

    private void toFragCashOut() {
        Fragment mFrag = new BBSCashOut();
        Bundle args = new Bundle();
        args.putString(DefineValue.TRANSACTION, transaksi);
//      args.putString(DefineValue.TX_STATUS, tx_status);
        mFrag.setArguments(args);
        getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void onSaveToFavorite() {
        extraSignature = NoHPFormat.formatTo62(userId_source) + source_product_type + "BBS";
        Timber.tag("extraSignature params ").e(extraSignature);
        String url = MyApiClient.LINK_TRX_FAVORITE_SAVE;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(url, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.PRODUCT_TYPE, source_product_type);
        params.put(WebParams.CUSTOMER_ID, NoHPFormat.formatTo62(userId_source));
        params.put(WebParams.TX_FAVORITE_TYPE, "BBS");
        params.put(WebParams.COMM_ID, comm_id);
        params.put(WebParams.NOTES, notesEditText.getText().toString());
        params.put(WebParams.BENEF_BANK_CODE, source_product_code);
        params.put(WebParams.SOURCE_BANK_CODE, tx_bank_code);

        Timber.tag("params ").e(params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(response.toString(), jsonModel.class);
                            Timber.tag("onResponses ").e(response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.tag("onResponses ").e(throwable.getLocalizedMessage());
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
