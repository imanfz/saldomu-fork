package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.activities.TopUpSCADMActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class FragTopUpConfirmSCADM extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    View v;
    SecurePreferences sp;
    Button btn_next;
    private ProgressDialog progdialog;
    int attempt, failed;
    Boolean isPIN=false;
    LinearLayout layout_otp;
    EditText et_otp;
    String comm_name, member_code, product_name, bank_gateway, comm_code, bank_code, product_code, amount, remark;
    String ccy_id, tx_id, member_id_scadm, member_name, comm_id,bank_name, admin_fee,total_amount, api_key, item_name;
    TextView tv_community_name, tv_community_code, tv_member_code, tv_product_name, tv_jumlah, tv_remark, tv_admin_fee, tv_total_amount;
    double dfee=0;
    double damount=0;
    double dtotal_amount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_topup_scadm_confirm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID,"");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID,"");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        Bundle bundle1 = getArguments();
        tx_id = bundle1.getString(DefineValue.TX_ID, "");
        member_id_scadm = bundle1.getString(DefineValue.MEMBER_ID_SCADM, "");
        member_code = bundle1.getString(DefineValue.MEMBER_CODE, "");
        member_name = bundle1.getString(DefineValue.MEMBER_NAME, "");
        comm_id = bundle1.getString(DefineValue.COMM_ID_SCADM, "");
        comm_code = bundle1.getString(DefineValue.COMMUNITY_CODE, "");
        comm_name = bundle1.getString(DefineValue.COMMUNITY_NAME, "");
        bank_gateway = bundle1.getString(DefineValue.BANK_GATEWAY, "");
        bank_code = bundle1.getString(DefineValue.BANK_CODE, "");
        bank_name = bundle1.getString(DefineValue.BANK_NAME, "");
        product_code = bundle1.getString(DefineValue.PRODUCT_CODE, "");
        product_name = bundle1.getString(DefineValue.PRODUCT_NAME, "");
        ccy_id = bundle1.getString(DefineValue.CCY_ID, "");
        amount = bundle1.getString(DefineValue.AMOUNT, "");
        admin_fee = bundle1.getString(DefineValue.FEE, "");
        total_amount = bundle1.getString(DefineValue.TOTAL_AMOUNT, "");
        remark = bundle1.getString(DefineValue.REMARK, "");
        api_key = bundle1.getString(DefineValue.API_KEY, "");
        attempt = bundle1.getInt(DefineValue.ATTEMPT,-1);


        tv_community_code = v.findViewById(R.id.community_code);
        tv_community_name = v.findViewById(R.id.community_name);
        tv_member_code = v.findViewById(R.id.member_code);
        tv_product_name = v.findViewById(R.id.bank_product);
        tv_jumlah = v.findViewById(R.id.tv_jumlah);
        tv_admin_fee = v.findViewById(R.id.tv_admin_fee);
        tv_total_amount = v.findViewById(R.id.tv_total);
        tv_remark = v.findViewById(R.id.tv_remark);
        btn_next = v.findViewById(R.id.btn_next);
        layout_otp = v.findViewById(R.id.layout_otp);
        et_otp = v.findViewById(R.id.et_otp);

        tv_community_code.setText(comm_code);
        tv_community_name.setText(comm_name);
        tv_member_code.setText(member_code);
        tv_product_name.setText(product_name);
        tv_remark.setText(remark);
        damount = Double.parseDouble(bundle1.getString(DefineValue.AMOUNT,""));
        dfee = Double.parseDouble(bundle1.getString(DefineValue.FEE,""));
        dtotal_amount = Double.parseDouble(bundle1.getString(DefineValue.TOTAL_AMOUNT,""));
        tv_jumlah.setText(ccy_id + ". " + CurrencyFormat.format(damount));
        tv_admin_fee.setText(ccy_id + ". " + CurrencyFormat.format(dfee));
        tv_total_amount.setText(ccy_id + ". " + CurrencyFormat.format(dtotal_amount));

        if (product_name.equalsIgnoreCase("MANDIRI SMS"))
        {
            layout_otp.setVisibility(View.VISIBLE);
        }

        new UtilsLoader(getActivity(),sp).getFailedPIN(userPhoneID,new OnLoadDataListener() { //get pin attempt
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

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bank_gateway.equalsIgnoreCase("N"))
                {
                    changeToSGOPlus(tx_id,product_code, product_name,bank_code,
                            String.valueOf(damount), String.valueOf(dfee), String.valueOf(dtotal_amount), bank_name);
                }
                else if (bank_gateway.equalsIgnoreCase("Y")) {
                    if (product_code.equalsIgnoreCase("SCASH")){
                        CallPINinput(attempt);
                        btn_next.setEnabled(true);
                    }
                    else
                    {
                        if(inputValidation()) {
                            sentInsertTransTopup(et_otp.getText().toString(),amount);
                        }
                        else btn_next.setEnabled(true);
                    }
                }
            }
        });
    }

    private boolean inputValidation(){
        if(et_otp.getText().toString().length()==0){
            et_otp.requestFocus();
            et_otp.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    private void CallPINinput(int _attempt){
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if(_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT,_attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private void sentInsertTransTopup(String tokenValue, final String _amount){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = tx_id+comm_code+product_code+tokenValue;

            final RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin
                    ,MyApiClient.LINK_INSERT_TRANS_TOPUP, userPhoneID,accessKey, extraSignature);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_name);
            params.put(WebParams.MEMBER_ID,member_id_scadm);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(tokenValue));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:"+params.toString());

            MyApiClient.sentInsertTransTopup(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response insertTrxTOpupSGOL:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            getTrxStatus(tx_id,comm_id,_amount);
                            setResultActivity(MainPage.RESULT_BALANCE);

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {

                            code = response.getString(WebParams.ERROR_CODE)+":"+response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            progdialog.dismiss();
                            btn_next.setEnabled(true);
                            if(isPIN && message.equals("PIN tidak sesuai")){
                                Intent i = new Intent(getActivity(), InsertPIN.class);

                                attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                failed = response.optInt(WebParams.MAX_FAILED,0);

                                if(attempt != -1)
                                    i.putExtra(DefineValue.ATTEMPT,failed-attempt);

                                startActivityForResult(i, MainPage.REQUEST_FINISH);
                            }
                            else{
                                getActivity().finish();
                            }

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
                    btn_next.setEnabled(true);
                    Timber.w("Error Koneksi insert trx topup biller confirm:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if(requestCode == MainPage.REQUEST_FINISH){
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                String _amount;
                    _amount = amount;
                //    Log.d("onActivity result", "Biller Fragment result pin value");
                sentInsertTransTopup(value_pin,_amount);
            }
        }
    }

    private void getTrxStatus(final String txId, String comm_id, final String _amount){
        try{

            extraSignature = txId + comm_id;
            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin,MyApiClient.LINK_GET_TRX_STATUS,
                    userPhoneID,accessKey, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
                params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params sent get Trx Status:"+params.toString());

            MyApiClient.sentGetTRXStatus(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        progdialog.dismiss();
                        Timber.d("isi response sent get Trx Status:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                            String txstatus = response.getString(WebParams.TX_STATUS);
                            showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""), DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
                                    sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                    txstatus, response.optString(WebParams.TX_REMARK, ""), _amount,response, response.optString(WebParams.BILLER_DETAIL),
                                    response.optString(WebParams.BUSS_SCHEME_CODE), response.optString(WebParams.BUSS_SCHEME_NAME), response.optString(WebParams.PRODUCT_NAME));
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

                        btn_next.setEnabled(true);
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
                    btn_next.setEnabled(true);
                    Timber.w("Error Koneksi trx stat biller confirm:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showReportBillerDialog(String name,String date,String userId, String txId,String itemName,String txStatus,
                                        String txRemark, String _amount, JSONObject response, String biller_detail,
                                        String buss_scheme_code, String buss_scheme_name, String product_name) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(admin_fee));

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


        double totalAmount = Double.parseDouble(amount) + Double.parseDouble(admin_fee);
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));
        args.putString(DefineValue.BILLER_DETAIL,biller_detail);
        args.putString(DefineValue.BUSS_SCHEME_CODE,buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME,buss_scheme_name);
        args.putString(DefineValue.BANK_PRODUCT,product_name);

        dialog.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }

    private void setResultActivity(int result){
        if (getActivity() == null)
            return;

        TopUpSCADMActivity fca = (TopUpSCADMActivity) getActivity();
        fca.setResultActivity(result);
    }

    private void changeToSGOPlus(String tx_id, String product_code, String product_name, String bank_code,
                                 String amount, String fee, String total_amount, String bank_name) {

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, product_code);
        i.putExtra(DefineValue.BANK_CODE, bank_code);
        i.putExtra(DefineValue.BANK_NAME, bank_name);
        i.putExtra(DefineValue.PRODUCT_NAME,product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.REMARK, remark);
        i.putExtra(DefineValue.COMMUNITY_CODE,comm_code);
        i.putExtra(DefineValue.MEMBER_CODE,member_code);
        i.putExtra(DefineValue.MEMBER_ID_SCADM,member_id_scadm);
        i.putExtra(DefineValue.MEMBER_NAME,member_name);
        i.putExtra(DefineValue.TX_ID,tx_id);
        i.putExtra(DefineValue.AMOUNT,amount);
        i.putExtra(DefineValue.TOTAL_AMOUNT,total_amount);
        i.putExtra(DefineValue.COMMUNITY_ID, comm_id);
        i.putExtra(DefineValue.API_KEY, api_key);
        i.putExtra(DefineValue.CALLBACK_URL, (DefineValue.CALLBACK_URL));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        Timber.d("isi args:"+i.toString());
        btn_next.setEnabled(true);
        switchActivityIB(i);
    }

    private void switchActivityIB(Intent mIntent){
        if (getActivity() == null)
            return;

        TopUpSCADMActivity fca = (TopUpSCADMActivity) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onOkButton() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
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
}
