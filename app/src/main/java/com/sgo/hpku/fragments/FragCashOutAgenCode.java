package com.sgo.hpku.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.coreclass.CurrencyFormat;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.ErrorDefinition;
import com.sgo.hpku.coreclass.InetHandler;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.dialogs.ReportBillerDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class FragCashOutAgenCode extends Fragment implements ReportBillerDialog.OnDialogOkCallback {

    private View v;
    private SecurePreferences sp;
    private String userid,accesskey,memberId,tx_id,nameadmin,amount,fee,total,ccy;
    private ProgressDialog progdialog;


    public static FragCashOutAgenCode newInstance(String otpmember, JSONObject dataInq) {
        FragCashOutAgenCode fragment = new FragCashOutAgenCode();
        Bundle args = new Bundle();
        args.putString(DefineValue.OTP_MEMBER, otpmember);
        args.putString(DefineValue.DATA, dataInq.toString());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_cash_out_agen_code, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userid = sp.getString(DefineValue.USERID_PHONE, "");
        accesskey = sp.getString(DefineValue.ACCESS_KEY, "");
        memberId = sp.getString(DefineValue.MEMBER_ID,"");

        Bundle args = getArguments();
        JSONObject dataInq = new JSONObject();
        String kodeAdmin = "";
        try {
            dataInq = new JSONObject(args.getString(DefineValue.DATA,""));
            kodeAdmin = args.getString(DefineValue.OTP_MEMBER,"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InitializeData(dataInq,kodeAdmin);
    }

    private void InitializeData(JSONObject mJson, String kodeAdmin){
        TextView tvUserID = (TextView) v.findViewById(R.id.cashoutagen_userId_value);
        TextView tvTxID = (TextView) v.findViewById(R.id.cashoutagen_trxid_value);
        TextView tvNameAdmin = (TextView) v.findViewById(R.id.cashout_admin_name_value);
        TextView tvAmount = (TextView) v.findViewById(R.id.cashoutagen_amount_value);
        TextView tvFee = (TextView) v.findViewById(R.id.cashoutagen_fee_value);
        TextView tvTotal = (TextView) v.findViewById(R.id.cashoutagen_total_amount_value);
        TextView tvKodeAdmin = (TextView) v.findViewById(R.id.cashoutagen_code_value);
        Button btn_proses = (Button) v.findViewById(R.id.btn_verification);


        tx_id = mJson.optString(WebParams.TX_ID, "");
        nameadmin = mJson.optString(WebParams.NAME_ADMIN,"");
        amount = mJson.optString(WebParams.AMOUNT,"");
        fee = mJson.optString(WebParams.FEE,"");
        total = mJson.optString(WebParams.TOTAL,"");
        ccy = mJson.optString(WebParams.CCY_ID,"");
        tvKodeAdmin.setText(kodeAdmin);

        tvUserID.setText(userid);
        tvTxID.setText(tx_id);
        tvNameAdmin.setText(nameadmin);
        tvAmount.setText(ccy+" "+CurrencyFormat.format(amount));
        tvFee.setText(ccy+" "+CurrencyFormat.format(fee));
        tvTotal.setText(ccy+" "+CurrencyFormat.format(total));


        btn_proses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(InetHandler.isNetworkAvailable(getActivity())) {
                    getTrxStatus();
                }
                else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
            }
        });

    }

    private void getTrxStatus(){
        try{

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_GET_TRX_STATUS,
                    userid,accesskey);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.TYPE, DefineValue.CASHOUT_TUNAI_TYPE);
            params.put(WebParams.PRIVACY, DefineValue.PRIVATE);
            params.put(WebParams.TX_TYPE, DefineValue.EMO);
            params.put(WebParams.USER_ID, userid);

            Timber.d("isi params sent get Trx Status:"+params.toString());

            MyApiClient.sentGetTRXStatus(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        progdialog.dismiss();
                        Timber.d("isi response sent get Trx Status:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            showReportBillerDialog( DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
                                   response.optString(WebParams.TX_STATUS,""), response.optString(WebParams.TX_REMARK, ""));
                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());

                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else if(code.equals(ErrorDefinition.ERROR_CODE_ADMIN_NOT_INPUT)) {
                            showDialogNotInput(message);
                        }
                        else {
                            Toast.makeText(getActivity(), message,Toast.LENGTH_LONG).show();
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

                    Timber.w("Error Koneksi trx stat biller confirm:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialogNotInput(String message){
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


    private void showReportBillerDialog(String datetime, String txStatus, String txRemark) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = new ReportBillerDialog();
        args.putString(DefineValue.TX_ID, tx_id);
        args.putString(DefineValue.USERID_PHONE, userid);
        args.putString(DefineValue.DATE_TIME, datetime);
        args.putString(DefineValue.NAME_ADMIN, nameadmin);
        args.putString(DefineValue.AMOUNT, ccy+" "+CurrencyFormat.format(amount));
        args.putString(DefineValue.FEE, ccy + " " + CurrencyFormat.format(fee));
        args.putString(DefineValue.TOTAL_AMOUNT, ccy+" "+CurrencyFormat.format(total));
        args.putString(DefineValue.REPORT_TYPE,DefineValue.CASHOUT_TUNAI);

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

        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount()>0)
                    getActivity().getSupportFragmentManager().popBackStack();
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