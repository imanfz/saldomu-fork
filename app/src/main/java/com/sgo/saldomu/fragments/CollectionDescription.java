package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CollectionActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/*
  Created by Administrator on 6/12/2015.
 */
public class CollectionDescription extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    private String txID;
    private String productCode;
    private String productName;
    private String commCode;
    private String commId;
    private String apiKey;
    private String callbackUrl;
    private String remark;
    private String bankName;
    private String jumlahnya;
    private String bankCode;
    private String fee;
    private String shareType;
    private String topuptype;
    private Button btnSubmit;
    private Button btnCancel;
    private Button btnResend;
    private EditText et_token;
    private int max_token_resend = 3;
    private int max_length_token;
    private int attempt = -1;
    private ProgressDialog progdialog;
    private View v;
    private Boolean isPIN = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_collection_description, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnSubmit = v.findViewById(R.id.collectdesc_btn_verification);
        btnCancel = v.findViewById(R.id.collectdesc_btn_cancel);

        btnSubmit.setOnClickListener(submitListener);
        btnCancel.setOnClickListener(cancelListener);

        initializeData();
    }

    private void initializeData(){
        Bundle args = getArguments();

        txID = args.getString(DefineValue.TX_ID,"");
        productCode = args.getString(DefineValue.PRODUCT_CODE,"");
        productName = args.getString(DefineValue.PRODUCT_NAME,"");
        commCode = args.getString(DefineValue.COMMUNITY_CODE,"");
        commId = args.getString(DefineValue.COMMUNITY_ID,"");
        apiKey = args.getString(DefineValue.COMMUNITY_API_KEY,"");
        callbackUrl = args.getString(DefineValue.CALLBACK_URL,"");
        jumlahnya = args.getString(DefineValue.AMOUNT,"");
        remark = args.getString(DefineValue.REMARK,"");
        String ccy_id = args.getString(DefineValue.CCY_ID,"");
        bankName = args.getString(DefineValue.BANK_NAME,"");
        bankCode = args.getString(DefineValue.BANK_CODE,"");
        fee = args.getString(DefineValue.FEE);
        shareType = args.getString(DefineValue.SHARE_TYPE,"");
        topuptype = args.getString(DefineValue.TRANSACTION_TYPE, "");
        isPIN = args.getString(DefineValue.AUTHENTICATION_TYPE,"").equals(DefineValue.AUTH_TYPE_PIN);
        Timber.d("isi args:"+args.toString());


        if(topuptype.equals(DefineValue.SMS_BANKING) || (topuptype.equals(DefineValue.EMONEY) && !isPIN)){
            View layout_token = v.findViewById(R.id.input_token_layout);
            View layout_btn_resend = v.findViewById(R.id.layout_btn_resend);
            layout_btn_resend.setVisibility(View.VISIBLE);
            btnResend = v.findViewById(R.id.collectdesc_btn_resend_token);
            btnResend.setOnClickListener(resendListener);
            layout_token.setVisibility(View.VISIBLE);
            et_token = v.findViewById(R.id.token_value);
            et_token.requestFocus();
            max_length_token = 6;
            changeTextBtnSub();

            if(bankCode.equals("114")){
                max_length_token = 5;
                et_token.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length_token)});
            }
        }

        if(isPIN){
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
        }

        TextView tv_bank_name = v.findViewById(R.id.collectdesc_bank_name);
        TextView tv_product_name = v.findViewById(R.id.collectdesc_bank_product);
        TextView tv_amount = v.findViewById(R.id.collectdesc_amount);
        TextView tv_fee = v.findViewById(R.id.collectdesc_bank_fee);
        TextView tv_total_fee = v.findViewById(R.id.collectdesc_bank_total_fee);
        TextView tv_remark = v.findViewById(R.id.collectdesc_remark_value);

        tv_bank_name.setText(bankName);
        tv_product_name.setText(productName);
        tv_amount.setText(ccy_id +". "+CurrencyFormat.format(jumlahnya));
        tv_fee.setText(ccy_id +". "+CurrencyFormat.format(fee));

        double total_amount = Integer.parseInt(jumlahnya) + Integer.parseInt(fee);
        tv_total_fee.setText(ccy_id +". "+CurrencyFormat.format(total_amount));

        tv_remark.setText(remark);

    }

    private void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){

                btnSubmit.setEnabled(false);

                if(topuptype.equals(DefineValue.EMONEY) && isPIN){
                    Intent i = new Intent(getActivity(), InsertPIN.class);
                    //i.putExtra(CoreApp.IS_MD5,false)
                    if(attempt != -1 && attempt < 2)
                        i.putExtra(DefineValue.ATTEMPT,attempt);
                    btnSubmit.setEnabled(true);
                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                }
                else if(topuptype.equals(DefineValue.SMS_BANKING) || (topuptype.equals(DefineValue.EMONEY) && !isPIN)){
                    if(inputValidation()) {
                        sentInsertTransTopup(et_token.getText().toString());
                    }
                }
                else {
                    changeToSGOPlus(txID,productCode,productName,commCode,commId,bankCode,bankName,jumlahnya,remark );
                }

            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    private Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){

                btnResend.setEnabled(false);
                if(max_token_resend!=0)requestResendToken();

            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);

                sentInsertTransTopup(value_pin);
            }

        }
    }

    private void sentInsertTransTopup(String tokenValue){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final Bundle args = getArguments();

            extraSignature = txID+args.getString(DefineValue.COMMUNITY_CODE)+productCode+tokenValue;

            final RequestParams param = MyApiClient.getSignatureWithParams(commIDLogin
                    ,MyApiClient.LINK_INSERT_TRANS_TOPUP, userPhoneID,accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP, extraSignature);
            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, args.getString(DefineValue.COMMUNITY_CODE));
            params.put(WebParams.COMM_ID, args.getString(DefineValue.COMMUNITY_ID));
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(tokenValue));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrx Collection:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                getTrxStatus(txID, args.getString(DefineValue.COMMUNITY_ID), jumlahnya);
                                setResultActivity(MainPage.RESULT_BALANCE);

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {

                                if(code.equals("0031") && topuptype.equals(DefineValue.EMONEY) && isPIN){
                                    Intent i = new Intent(getActivity(), InsertPIN.class);
                                    attempt = attempt-1;
                                    if(attempt != -1 && attempt < 2)
                                        i.putExtra(DefineValue.ATTEMPT, attempt);
                                    btnSubmit.setEnabled(true);
                                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                                }

                                code = model.getError_code() + ":" + model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();

                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();

                            }

                            if(progdialog.isShowing())
                                progdialog.dismiss();

                            btnSubmit.setEnabled(true);
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void requestResendToken(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = txID+getArguments().getString(DefineValue.COMMUNITY_CODE)+productCode;

            RequestParams param;
            HashMap<String, Object> params;
            String url;

            if(bankCode.equals("114")) {
//                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_REQ_TOKEN_SGOL,
//                        userPhoneID, accessKey, extraSignature);
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);
                url = MyApiClient.LINK_REQ_TOKEN_SGOL;
            }else {
//                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_RESEND_TOKEN_SGOL,
//                        userPhoneID, accessKey);
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RESEND_TOKEN_SGOL);
                url = MyApiClient.LINK_RESEND_TOKEN_SGOL;
            }


            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, getArguments().getString(DefineValue.COMMUNITY_CODE));
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params resendToken Collection:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                            Timber.w("max token sebelum", String.valueOf(max_token_resend));
                                max_token_resend = max_token_resend - 1;
//                            Timber.w("max token sesudah", String.valueOf(max_token_resend));
                                changeTextBtnSub();
                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                code = model.getError_message();
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }

                            progdialog.dismiss();
                            btnResend.setEnabled(true);
                            if(max_token_resend == 0 ){
                                btnResend.setEnabled(false);
                                btnSubmit.setEnabled(true);
                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }


    }


    private void getTrxStatus(final String txId, String comm_id, final String _amount){
        try{

            extraSignature = txId + comm_id;
            RequestParams param = MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_GET_TRX_STATUS,
                    userPhoneID,accessKey, extraSignature);

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TYPE, DefineValue.TOPUP_ACL_TYPE);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params sent get Trx Status:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""), DateTimeFormat.formatToID(model.getCreated()),
                                        sp.getString(DefineValue.USERID_PHONE, ""),bankName,productName, txId, remark ,
                                        model.getTx_status(), model.getTx_remark(), _amount);
                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                String msg = model.getError_message();
                                showDialog(msg);
                            }

                            btnSubmit.setEnabled(true);
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void changeToSGOPlus(String _tx_id, String _product_code, String _product_name , String _comm_code,
                                 String _comm_id,String _bank_code, String _bank_name, String _amount, String _remark) {

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, _bank_code);
        i.putExtra(DefineValue.BANK_NAME, _bank_name);
        i.putExtra(DefineValue.PRODUCT_NAME,_product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.COMMUNITY_CODE,_comm_code);
        i.putExtra(DefineValue.TX_ID,_tx_id);
        i.putExtra(DefineValue.AMOUNT,_amount);
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.COLLECTION);
        i.putExtra(DefineValue.REMARK,_remark);
        i.putExtra(DefineValue.SHARE_TYPE,shareType);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_ACL_TYPE);

        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(fee);
        i.putExtra(DefineValue.TOTAL_AMOUNT,String.valueOf(totalAmount));

        i.putExtra(DefineValue.API_KEY,apiKey);
        i.putExtra(DefineValue.CALLBACK_URL,callbackUrl);

        i.putExtra(DefineValue.COMMUNITY_ID, _comm_id);

        switchActivityIB(i);
    }

    private void showReportBillerDialog(String userName,String date, String userId,String _bank_name, String _product_name,
                                        String _tx_id,String _remark,String txStatus,String txRemark, String _amount) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME,date);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.BANK_NAME, _bank_name);
        args.putString(DefineValue.BANK_PRODUCT, _product_name);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE+". "+CurrencyFormat.format(fee));
        args.putString(DefineValue.TX_ID, _tx_id);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE+". "+CurrencyFormat.format(_amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.COLLECTION);
        args.putString(DefineValue.REMARK, _remark);

        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(fee);
        args.putString(DefineValue.TOTAL_AMOUNT,MyApiClient.CCY_VALUE+". "+CurrencyFormat.format( String.valueOf(totalAmount)));

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
//        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
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


    private boolean inputValidation(){
        if(et_token.getText().toString().length()==0){
            et_token.requestFocus();
            et_token.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    private void setResultActivity(int result){
        if (getActivity() == null)
            return;

        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.setResultActivity(result);
    }


    private Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager().popBackStack();
        }
    };



    private void switchActivityIB(Intent mIntent){
        if (getActivity() == null)
            return;

        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
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
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }
}