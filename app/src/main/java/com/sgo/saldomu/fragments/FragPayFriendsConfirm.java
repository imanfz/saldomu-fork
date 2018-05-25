package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.SmsMessage;
import android.view.*;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.RecepientModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.PayFriendsConfirmTokenActivity;
import com.sgo.saldomu.adapter.RecipientAdapter;
import com.sgo.saldomu.coreclass.*;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
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

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/*
 Created by thinkpad on 3/12/2015.
 */
public class FragPayFriendsConfirm extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    private static String TAG_FRAGPAYFRIENDCONFIRM = "com.sgo.mdevcash.fragments.FragPayfriendsConfirm";

    private String dataJson;
    private String dataName;
    private String message;
    private String txID;
    private String dataMapper;
    private Boolean isNotification = false;
    private double amountEach;
    private double totalAmount;
    private double fee;
    private LinearLayout layoutOTP;
    private ImageView imgProfile;
    private ImageView imgRecipients;
    private ListView listRecipient;
    private TextView txtName;
    private TextView txtMessage;
    private TextView txtNumberRecipients;
    private EditText etOTP;
    private Button btnSubmit;
    private Button btnCancel;
    private Button btnResend;
    private ProgressDialog progdialog;
    private int max_token_resend = 3, total_receive_recepient = 0, attempt=-1;
    private View v;
    private AlertDialogFrag mDialogNonMember;

    private List<String> listName;
    private List<RecepientModel> listObjectRecipient;
    private List<TempTxID> mTempTxID;
    private List<RecepientModel> list_non_member;

    private String authType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_payfriends_confirm_token, container, false);
        return v;
    }

    private class TempTxID{

        private String tx_id;

        public TempTxID(String _tx_id){
            this.tx_id = _tx_id;
        }

        public String getTx_id() {
            return tx_id;
        }

        public void setTx_id(String tx_id) {
            this.tx_id = tx_id;
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE,"");

        imgProfile = v.findViewById(R.id.img_profile);
        imgRecipients = v.findViewById(R.id.img_recipients);
        txtNumberRecipients = v.findViewById(R.id.payfriends_value_number_recipients);
        txtName = v.findViewById(R.id.txtName);
        listRecipient = v.findViewById(R.id.list_recipient);

        txtMessage = v.findViewById(R.id.payfriends_confirm_value_message);
        etOTP = v.findViewById(R.id.payfriends_value_otp);
        btnSubmit = v.findViewById(R.id.btn_submit_payfriends);
        btnCancel = v.findViewById(R.id.btn_cancel_payfriends);
        layoutOTP = v.findViewById(R.id.layout_OTP);
        TextView tv_amount_each = v.findViewById(R.id.payfriends_confirm_value_amount_each);
        TextView tv_amount = v.findViewById(R.id.payfriends_confirm_value_amount);
        TextView tv_fee = v.findViewById(R.id.payfriends_confirm_value_fee);
        TextView tv_total_amount = v.findViewById(R.id.payfriends_confirm_value_total_amount);

        if(authType.equalsIgnoreCase("PIN")) {
            layoutOTP.setVisibility(View.GONE);
            btnSubmit.setText(R.string.proses);
            new UtilsLoader(getActivity(),sp).getFailedPIN(userPhoneID,new OnLoadDataListener() { // get pin attempt
                @Override
                public void onSuccess(Object deData) {
                    attempt = (int) deData;
                }

                @Override
                public void onFail(Bundle message) {

                }

                @Override
                public void onFailure(String message) {

                }
            });
        }
        else if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
            layoutOTP.setVisibility(View.VISIBLE);
            btnResend = v.findViewById(R.id.btn_resend_token);

            View layout_resendbtn = v.findViewById(R.id.layout_btn_resend);
            layout_resendbtn.setVisibility(View.VISIBLE);

            btnResend.setOnClickListener(resendListener);
            changeTextBtnSub();
        }

        btnSubmit.setOnClickListener(submitListener);
        btnCancel.setOnClickListener(cancelListener);


        Bundle bundle = this.getArguments();
        if(bundle != null) {
            isNotification = bundle.getBoolean(DefineValue.TRANSACTION_TYPE);
            dataJson = bundle.getString(WebParams.DATA_TRANSFER);
            dataName = bundle.getString(WebParams.DATA);
            message = bundle.getString(WebParams.MESSAGE);
            dataMapper = bundle.getString(WebParams.DATA_MAPPER);

            mTempTxID = new ArrayList<>();
            listObjectRecipient = new ArrayList<>();
            list_non_member = new ArrayList<>();

            double amount = 0.0;

            Gson json = new Gson();
            String finalName;
            String finalTxid;
            String number_recipient_nonmember="";
            Double total_fee = 0.0, total_amount = 0.0;
            String expired_period_date = "";

            try {
                JSONArray mArrayData = new JSONArray(dataJson);
                listName = json.fromJson(dataName, new TypeToken<List<String>>(){}.getType());

                if(mArrayData.length() > 0){
                    amountEach = mArrayData.getJSONObject(0).getDouble(WebParams.AMOUNT);
                    fee = mArrayData.getJSONObject(0).getDouble(WebParams.FEE);
                    totalAmount = mArrayData.getJSONObject(0).getDouble(WebParams.TOTAL);
                }

                for(int i = 0 ; i < mArrayData.length() ; i++){

                    if(mArrayData.getJSONObject(i).getString(WebParams.MEMBER_STATUS).equals(DefineValue.FAILED)){
                        finalName = listName.get(i);
                        finalTxid = "";
                    }
                    else {
                        total_receive_recepient++;
                        finalTxid = mArrayData.getJSONObject(i).getString(WebParams.TX_ID);
                        finalName = mArrayData.getJSONObject(i).getString(WebParams.MEMBER_NAME_TO);
                        mTempTxID.add(new TempTxID(mArrayData.getJSONObject(i).getString(WebParams.TX_ID)));
                        if(expired_period_date.isEmpty() || expired_period_date.equals(""))
                            expired_period_date = mArrayData.getJSONObject(i).optString(WebParams.EXP_DURATION_HOUR,"");
                    }

                    listObjectRecipient.add(new RecepientModel(finalTxid,
                                                                finalName,
                                                                mArrayData.getJSONObject(i).getString(WebParams.MEMBER_PHONE),
                                                                mArrayData.getJSONObject(i).getString(WebParams.MEMBER_STATUS),
                                                                mArrayData.getJSONObject(i).optString(WebParams.IS_MEMBER_TEMP,"")
                                                                ));
                    if(listObjectRecipient.get(i).getStatus().equals(DefineValue.SUCCESS)){
                        amount = amount + amountEach;
                        total_fee = total_fee + fee;
                        total_amount = total_amount + totalAmount;

                        if(listObjectRecipient.get(i).getIs_member_temp().equals(DefineValue.STRING_YES)){
                            list_non_member.add(listObjectRecipient.get(i));
                            if(number_recipient_nonmember.isEmpty())
                                number_recipient_nonmember = listObjectRecipient.get(i).getNumber();
                            else {
                                number_recipient_nonmember = number_recipient_nonmember + ", "+ listObjectRecipient.get(i).getNumber();
                            }
                        }
                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            txID = json.toJson(mTempTxID);

            tv_fee.setText(MyApiClient.CCY_VALUE +". "+CurrencyFormat.format(total_fee));
            tv_total_amount.setText(MyApiClient.CCY_VALUE +". "+CurrencyFormat.format(total_amount));
            tv_amount_each.setText(MyApiClient.CCY_VALUE +". "+CurrencyFormat.format(amountEach));
            tv_amount.setText(MyApiClient.CCY_VALUE +". "+CurrencyFormat.format(amount));
            txtMessage.setText(message);

            Timber.d("isi tx id:"+txID );

            RecipientAdapter recipientAdapter = new RecipientAdapter(getActivity().getApplicationContext(), listObjectRecipient);
            listRecipient.setAdapter(recipientAdapter);
            setListViewHeightBasedOnItems(listRecipient);

            Bitmap bmRecipients = BitmapFactory.decodeResource(getResources(), R.drawable.grey_background);
            RoundImageTransformation roundedImageRecipients = new RoundImageTransformation(bmRecipients);
            imgRecipients.setImageDrawable(roundedImageRecipients);


            setImageProfPic();

            txtName.setText(sp.getString(DefineValue.USER_NAME,""));
            txtNumberRecipients.setText(Integer.toString(total_receive_recepient));

            if(!list_non_member.isEmpty()) {
                String messageDialog = getString(R.string.dialog_p2p_nonmember_msg,
                        number_recipient_nonmember,getString(R.string.appname),expired_period_date) ;
                mDialogNonMember = AlertDialogFrag.newInstance(getString(R.string.dialog_p2p_nonmember_title,getString(R.string.appname)), messageDialog,
                        getString(R.string.yes),getString(R.string.cancel),false);
                mDialogNonMember.setOkListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnSubmitAction();
                    }
                });
                mDialogNonMember.setTargetFragment(FragPayFriendsConfirm.this,0);

            }
        }
        View layout_rec = v.findViewById(R.id.layout_recipient);
        final Dialog dialog_color_help = DefinedDialog.MessageP2P(getActivity());
        layout_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_color_help.show();
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
                if(mDialogNonMember != null)
                    mDialogNonMember.show(getFragmentManager(),TAG_FRAGPAYFRIENDCONFIRM);
                else
                    btnSubmitAction();
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));

        }
    };

    private void btnSubmitAction(){
        if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
            if (inputValidation()) {
                sentDataConfirm(txID,etOTP.getText().toString());
            }
        }
        else if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN)){
            Intent i = new Intent(getActivity(), InsertPIN.class);
            if(attempt != -1 && attempt < 2)
                i.putExtra(DefineValue.ATTEMPT,attempt);
            startActivityForResult(i, MainPage.REQUEST_FINISH);
        }
    }

    private Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
                    if (max_token_resend != 0)
                        sentResendToken(txID);

                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));

        }
    };

    private boolean inputValidation(){
        if(etOTP.getText().toString().length()==0){
            etOTP.requestFocus();
            etOTP.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    private void showReportBillerDialog(String name,String date,String userId, String txId, String recipients,String amountEach, String amount,
                                        String fee,String totalAmount, String message, String errorRecipients, String buss_scheme_code,
                                        String buss_scheme_name, String transfer_data) {

        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME,name);
        args.putString(DefineValue.DATE_TIME,date);
        args.putString(DefineValue.TX_ID,txId);
        args.putString(DefineValue.USERID_PHONE,userId);
        args.putString(DefineValue.RECIPIENTS,recipients);
        args.putString(DefineValue.AMOUNT_EACH,amountEach);
        args.putString(DefineValue.AMOUNT,amount);
        args.putString(DefineValue.FEE,fee);
        args.putString(DefineValue.TOTAL_AMOUNT,totalAmount);
        args.putString(DefineValue.MESSAGE,message);
        args.putString(DefineValue.RECIPIENTS_ERROR,errorRecipients);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.PAYFRIENDS);
        args.putString(DefineValue.BUSS_SCHEME_CODE, buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME, buss_scheme_name);
        args.putString(DefineValue.TRANSFER_DATA, transfer_data);

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(),ReportBillerDialog.TAG);
    }


    private void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms)+" ("+max_token_resend+")");
            }
        });
    }

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivity result"+ " Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if(requestCode == MainPage.REQUEST_FINISH){
              Timber.d("onActivity result", "Biller Fragment masuk request exit");
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                Timber.d("onActivity result", "Biller Fragment result pin value");
                sentDataConfirm(txID, value_pin);
            }
        }
    }

    private void sentDataConfirm(String _data, String _token){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();
            extraSignature = memberIDLogin+_token;
            RequestParams params;
            if(isNotification) {

                params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_CONFIRM_TRANS_P2P_NOTIF, extraSignature);
            }else {
                params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_CONFIRM_TRANS_P2P,extraSignature);
            }

            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(_token));
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.DATA, _data);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.DATA_MAPPER, dataMapper);
            Timber.d("isi params sent confirm token p2p:"+params.toString());

            JsonHttpResponseHandler myHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response confirm token p2p:"+response.toString());
                            //Toast.makeText(getActivity(), getString(R.string.transaction_success), Toast.LENGTH_LONG).show();

                            JSONArray mArrayData = new JSONArray(response.getString(WebParams.DATA));
                            Timber.d("isi response data:"+mArrayData.toString());
                            int isFailed=0 ;
                            String error_msg = "";

                            String _txid = "", _recipient = "", _recipient_error = null,_message;
                            double _Amount = 0.0,_fee = 0.0, _total_amount = 0.0, _total_wc = 0.0;
                            _message = message;

                            for(int i = 0 ; i < mArrayData.length() ; i++) {
                                for (RecepientModel aListObjectRecipient : listObjectRecipient) {
                                    if (aListObjectRecipient.getTx_id().equals(mArrayData.getJSONObject(i).getString(WebParams.TX_ID))) {
                                        if(mArrayData.getJSONObject(i).getString(WebParams.TX_STATUS).equals(DefineValue.SUCCESS)) {
                                            if (_txid.equals("")) {
                                                _txid = mArrayData.getJSONObject(i).getString(WebParams.TX_ID);
                                                _recipient = aListObjectRecipient.getName();
                                            } else {
                                                _txid = _txid + "\n" + mArrayData.getJSONObject(i).getString(WebParams.TX_ID);
                                                _recipient = _recipient + "\n" + aListObjectRecipient.getName();
                                            }

                                            _Amount = _Amount + amountEach;
                                            _fee = _fee + fee;
                                            _total_amount = _total_amount + totalAmount;
                                        }
                                        else if(mArrayData.getJSONObject(i).getString(WebParams.TX_STATUS).equals(DefineValue.FAILED)) {
                                            isFailed++ ;
                                            error_msg = mArrayData.getJSONObject(i).getString(WebParams.TX_REMARK);
                                            if (_recipient_error == null)
                                                _recipient_error = aListObjectRecipient.getName()+ " = " + error_msg;
                                            else _recipient_error = _recipient_error + "\n" +
                                                    aListObjectRecipient.getName()+ " = " + error_msg;
                                        }
                                        else if(mArrayData.getJSONObject(i).getString(WebParams.TX_STATUS).equals(DefineValue.WAITING_CLAIM)){
                                            _total_wc = _total_wc + totalAmount;
                                        }

                                    }
                                }
                            }


                            if(isFailed != mArrayData.length()){

                                String name = sp.getString(DefineValue.USER_NAME,"");
//                                String _totalAmount = MyApiClient.CCY_VALUE+". "+CurrencyFormat.format(_Amount);

                                if(list_non_member.size() == mArrayData.length()){
                                    showDialogClaim(getString(R.string.toast_msg_wait_claim, CurrencyFormat.format(_total_wc)));
                                }
                                else {
                                    showReportBillerDialog(name,
                                            DateTimeFormat.getCurrentDateTime(),
                                            sp.getString(DefineValue.USERID_PHONE, ""),
                                            _txid,
                                            _recipient,
                                            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amountEach),
                                            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_Amount),
                                            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_fee),
                                            MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_total_amount),
                                            _message,
                                            _recipient_error, response.optString(WebParams.BUSS_SCHEME_CODE), response.optString(WebParams.BUSS_SCHEME_NAME), response.optString(WebParams.TRANSFER_DATA));
                                }
                            }
                            else showDialog(error_msg);

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else if(code.equals(ErrorDefinition.WRONG_PIN_P2P)){
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            showDialogError(code);
                        }
                        else {
                            Timber.d("isi error confirm token p2p:" + response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            if(authType.equalsIgnoreCase("PIN")) {
                                Intent i = new Intent(getActivity(), InsertPIN.class);
                                attempt = attempt-1;
                                if(attempt != -1 && attempt < 2)
                                    i.putExtra(DefineValue.ATTEMPT, attempt);
                                startActivityForResult(i,MainPage.REQUEST_FINISH);
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
                    if (progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi confirm proses p2p confirm:"+throwable.toString());
                }
            };

            if(isNotification)
                MyApiClient.sentConfirmTransP2PNotif(getActivity(),params, myHandler );
            else
                MyApiClient.sentConfirmTransP2P(getActivity(),params, myHandler );
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialogClaim(String message){
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.transaction_success),
                message,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        onOkButton();
                    }
                });

        dialognya.show();
    }

    private void showDialogError(String message){
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.blocked_pin_title),
                message, new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {

                    }
                }) ;
        dialognya.show();
    }

    private void sentResendToken(String _data){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_RESENT_TOKEN_P2P,
                    userPhoneID,accessKey);
            params.put(WebParams.MEMBER_ID,memberIDLogin);
            params.put(WebParams.DATA,_data);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent resend token p2p:"+params.toString());

            MyApiClient.sentResentTokenP2P(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params resend confirm token p2p:"+response.toString());
                            max_token_resend = max_token_resend - 1;
                            changeTextBtnSub();
                            Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE) ){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            Timber.d("isi error resend token p2p:"+response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                        }
                        if(max_token_resend == 0 ){
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

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if (progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi resend p2p confirm:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
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
                getActivity().finish();
            }
        });

        dialog.show();
    }

    private static void setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }


    private void setImageProfPic(){
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

        if(density <= 1) _url_profpic = sp.getString(DefineValue.IMG_SMALL_URL, null);
        else if(density < 2) _url_profpic = sp.getString(DefineValue.IMG_MEDIUM_URL, null);
        else _url_profpic = sp.getString(DefineValue.IMG_LARGE_URL, null);

        Timber.wtf("url prof pic:"+_url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(getActivity());
//        else
//            mPic= Picasso.with(getActivity());

        if(_url_profpic != null && _url_profpic.isEmpty()){
            GlideManager.sharedInstance().initializeGlide(getActivity(), R.drawable.user_unknown_menu, roundedImage, imgProfile);
        }
        else {
            GlideManager.sharedInstance().initializeGlide(getActivity(), _url_profpic, roundedImage, imgProfile);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on){
        if (getActivity() == null)
            return;

        PayFriendsConfirmTokenActivity fca = (PayFriendsConfirmTokenActivity ) getActivity();
        fca.togglerBroadcastReceiver(_on,myReceiver);
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
            Timber.wtf("masuk myreceiver fragpayfriends");
            if(mBundle != null){
                Object[] pdus = (Object[]) mBundle.get("pdus");
                assert pdus != null;
                mSMS = new SmsMessage[pdus.length];

                for (int i = 0; i < mSMS.length ; i++){
                    mSMS[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    strMessage += mSMS[i].getMessageBody();
                    strMessage += "\n";
                }
                Timber.wtf("masuk myreceiver fragpayfriends:"+strMessage);
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

    private void insertTokenEdit(String _kode_otp, String _member_kode){
        Timber.d("isi _kode_otp, _member_kode, member kode session:"+_kode_otp+ " / " +_member_kode +" / "+ sp.getString(DefineValue.MEMBER_CODE,""));
        if(_member_kode.equals(sp.getString(DefineValue.MEMBER_CODE,""))){
            etOTP.setText(_kode_otp);
        }
    }

    @Override
    public void onOkButton() {
        getActivity().setResult(MainPage.RESULT_BALANCE);
        getActivity().finish();
    }

}
