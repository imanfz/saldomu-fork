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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class CashOutBBS_confirm_agent extends Fragment implements ReportBillerDialog.OnDialogOkCallback {

    public final static String TAG = "com.sgo.saldomu.fragments.CashOutBBS_confirm_agent";
    private SecurePreferences sp;
    private ProgressDialog progdialog;
    private View v, layout_OTP;
    private TextView tvSourceAcct, tvBankBenef, tvAmount, tvUserIdSource, tvRemark, tvUserIdTitle, tvKode;
    private EditText tokenValue;
    private Button btnSubmit;
    private String userID, accessKey, comm_code, tx_product_code, source_product_type,
            source_product_h2h, api_key, callback_url, tx_bank_code, tx_bank_name, tx_product_name,
            tx_id, amount, share_type, comm_id, benef_product_name,
            userId_source, remark, source_product_name, transaksi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.cashoutbbs_confirm_agent, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        CircleStepView mCircleStepView = ((CircleStepView) v.findViewById(R.id.circle_step_view));
        mCircleStepView.setTextBelowCircle("", "", getString(R.string.konfirmasi));
        mCircleStepView.setCurrentCircleIndex(2, false);

        TextView tvTitle = (TextView) v.findViewById(R.id.tv_title);
        tvSourceAcct = (TextView) v.findViewById(R.id.bbscashout_value_source);
        tvUserIdTitle = (TextView) v.findViewById(R.id.tv_user_id);
        tvUserIdSource = (TextView) v.findViewById(R.id.bbscashout_value_user_id);
        tvBankBenef = (TextView) v.findViewById(R.id.bbscashout_value_benef);
        tvAmount = (TextView) v.findViewById(R.id.bbscashout_value_amount);
        tvRemark = (TextView) v.findViewById(R.id.bbscashout_value_remark);
        tvKode = (TextView) v.findViewById(R.id.tv_kode);
        btnSubmit = (Button) v.findViewById(R.id.btn_submit);
        layout_OTP = v.findViewById(R.id.layout_OTP);
        tokenValue = (EditText) v.findViewById(R.id.bbscashout_value_token);
        Button btnBack = (Button) v.findViewById(R.id.btn_back);

        Bundle bundle = getArguments();
        if(bundle != null) {
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
            share_type = bundle.getString(DefineValue.SHARE_TYPE);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID );
            benef_product_name = bundle.getString(DefineValue.BANK_BENEF);
            userId_source  = bundle.getString(DefineValue.USER_ID);
            remark = bundle.getString(DefineValue.REMARK);
            source_product_name = bundle.getString(DefineValue.SOURCE_ACCT);

            if(source_product_h2h.equalsIgnoreCase("Y")) {
                tvUserIdTitle.setText(getString(R.string.no_member));
            }
            else {
                tvUserIdTitle.setText(getString(R.string.no_rekening));
            }
            if(tx_product_code.equalsIgnoreCase("TCASH"))
            {
                tvKode.setText("Kode OTP");
            }
            tvTitle.setText(transaksi);
            tvAmount.setText(CurrencyFormat.format(amount));
            tvBankBenef.setText(benef_product_name);
            tvUserIdSource.setText(userId_source);
            tvRemark.setText(remark);
            tvSourceAcct.setText(source_product_name);

            layout_OTP.setVisibility(View.VISIBLE);
            tokenValue.requestFocus();
            btnBack.setOnClickListener(backListener);
            btnSubmit.setOnClickListener(submitListener);
        }
        else {
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
                if(inputValidation())
                    sentInsertTransTopup(tokenValue.getText().toString());
                else
                    btnSubmit.setEnabled(true);
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

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
                                    response.optString(WebParams.BENEF_ACCT_NAME,""));
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
                                        String benef_bank_name, String benef_acct_no, String benef_acct_name) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = new ReportBillerDialog();
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT);
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
        args.putString(DefineValue.NO_BENEF, benef_acct_no);
        args.putString(DefineValue.NAME_BENEF, benef_acct_name);

        dialog.setArguments(args);
        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
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
