package com.sgo.hpku.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.SmsMessage;
import android.text.InputFilter;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BillerActivity;
import com.sgo.hpku.activities.InsertPIN;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.SgoPlusWeb;
import com.sgo.hpku.coreclass.*;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.dialogs.ReportBillerDialog;
import com.sgo.hpku.interfaces.OnLoadDataListener;
import com.sgo.hpku.loader.UtilsLoader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 3/5/2015.
 */
public class BillerConfirm extends Fragment implements ReportBillerDialog.OnDialogOkCallback {


    public final static String TAG = "BILLER_CONFIRM";

    private View v;
    private String tx_id;
    private String merchant_type;
    private String ccy_id;
    private String amount;
    private String item_name;
    private String cust_id;
    private String payment_name;
    private String amount_desire;
    private String fee;
    private String total_amount;
    private String shareType;
    private String bank_code;
    private String product_code;
    private String product_payment_type;
    private String biller_name;
    private String userID;
    private String accessKey;
    private String biller_type_code;
    private TextView tv_item_name_value;
    private TextView tv_amount_value;
    private TextView tv_id_cust;
    private TextView tv_payment_name;
    private TextView tv_fee_value;
    private TextView tv_total_amount_value;
    private EditText et_token_value;
    private Button btn_submit;
    private Button btn_cancel;
    private Button btn_resend;
    private int max_token_resend = 3;
    private int buy_code;
    private int attempt;
    private int failed;
    private Boolean is_input_amount;
    private Boolean is_display_amount;
    private Boolean is_sgo_plus;
    private Boolean isPIN;
    Boolean isFacebook = false;
    private Boolean isShowDescription = false;
    private Boolean isPLN = false;
    private ProgressDialog progdialog;
    private JSONArray isi_field;
    private JSONArray isi_value;
    private ImageView mIconArrow;
    private TableLayout mTableLayout;
    private SecurePreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_token_confirm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        tv_id_cust = (TextView) v.findViewById(R.id.billertoken_biller_id_value);
        tv_item_name_value = (TextView) v.findViewById(R.id.billertoken_item_name_value);
        tv_payment_name = (TextView) v.findViewById(R.id.billertoken_item_payment_value);
        tv_amount_value = (TextView) v.findViewById(R.id.billertoken_amount_value);
        tv_fee_value = (TextView) v.findViewById(R.id.billertoken_fee_value);
        tv_total_amount_value = (TextView) v.findViewById(R.id.billertoken_total_amount_value);
        btn_submit = (Button) v.findViewById(R.id.billertoken_btn_verification);
        btn_cancel = (Button) v.findViewById(R.id.billertoken_btn_cancel);

        btn_submit.setOnClickListener(submitListener);
        btn_cancel.setOnClickListener(cancelListener);

        initializeLayout();
    }

    private void initializeLayout(){

        Bundle args = getArguments();
        cust_id = args.getString(DefineValue.CUST_ID,"");
        tx_id = args.getString(DefineValue.TX_ID, "");
        ccy_id = args.getString(DefineValue.CCY_ID, "");
        amount = args.getString(DefineValue.AMOUNT, "");
        fee = args.getString(DefineValue.FEE,"");
        item_name = args.getString(DefineValue.ITEM_NAME, "");
        is_input_amount = args.getBoolean(DefineValue.IS_INPUT);
        is_display_amount = args.getBoolean(DefineValue.IS_DISPLAY);
        payment_name = args.getString(DefineValue.PAYMENT_NAME);
        buy_code = args.getInt(DefineValue.BUY_TYPE, 0);
        is_sgo_plus = args.getBoolean(DefineValue.IS_SGO_PLUS);
        total_amount = args.getString(DefineValue.TOTAL_AMOUNT);
        bank_code = args.getString(DefineValue.BANK_CODE);
        product_code = args.getString(DefineValue.PRODUCT_CODE);
        shareType = args.getString(DefineValue.SHARE_TYPE);
        product_payment_type = args.getString(DefineValue.PRODUCT_PAYMENT_TYPE);
        biller_name = args.getString(DefineValue.BILLER_NAME,"");
        attempt = args.getInt(DefineValue.ATTEMPT,-1);
        isShowDescription = args.getBoolean(DefineValue.IS_SHOW_DESCRIPTION,false);
        biller_type_code = args.getString(DefineValue.BILLER_TYPE);
        Timber.d("isi args:"+args.toString());

        if(biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS)||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_NON_TAG)||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN)||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN_TKN)){
            isPLN = true;
        }

        tv_item_name_value.setText(item_name);
        tv_id_cust.setText(cust_id);
        tv_amount_value.setText(ccy_id+". "+ CurrencyFormat.format(amount));
        tv_payment_name.setText(payment_name);
        tv_fee_value.setText(ccy_id+". "+CurrencyFormat.format(fee));

        tv_total_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(total_amount));

        if(!is_sgo_plus){
            merchant_type = args.getString(DefineValue.AUTHENTICATION_TYPE,"");
            if(merchant_type.equals(DefineValue.AUTH_TYPE_OTP)||product_payment_type.equals(DefineValue.BANKLIST_TYPE_SMS)){
                LinearLayout layoutOTP = (LinearLayout) v.findViewById(R.id.layout_token);
                layoutOTP.setVisibility(View.VISIBLE);
                View layout_btn_resend = v.findViewById(R.id.layout_btn_resend);
                btn_resend = (Button) v.findViewById(R.id.billertoken_btn_resend);
                et_token_value = (EditText) layoutOTP.findViewById(R.id.billertoken_token_value);
                int max_length_token;
                if(product_payment_type.equals(DefineValue.BANKLIST_TYPE_SMS)){
                    if(bank_code.equals("114"))
                        max_length_token = 5;
                    else
                        max_length_token = 6;
                }
                else {
                    max_length_token = 4;
                }
                et_token_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length_token)});

                layout_btn_resend.setVisibility(View.VISIBLE);
                et_token_value.requestFocus();
                btn_resend.setOnClickListener(resendListener);
                changeTextBtnSub();
                isPIN = false;
            }
            else {
                isPIN = true;
                new UtilsLoader(getActivity(),sp).getFailedPIN(userID,new OnLoadDataListener() { //get pin attempt
                    @Override
                    public void onSuccess(Object deData) {
                        attempt = (int)deData;
                    }

                    @Override
                    public void onFail(String message) {

                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        }

        if(buy_code == BillerActivity.PURCHASE_TYPE){
            View layout_biller_name = v.findViewById(R.id.billertoken_layout_biller_name);
            layout_biller_name.setVisibility(View.VISIBLE);
            TextView tv_biller_name_value = (TextView) layout_biller_name.findViewById(R.id.billertoken_biller_name_value);
            tv_biller_name_value.setText(biller_name);
        }

        if(is_display_amount && isShowDescription){
            try {
                View layout_detail_payment = v.findViewById(R.id.billertoken_layout_payment);
                layout_detail_payment.setVisibility(View.VISIBLE);
                RelativeLayout mDescLayout = (RelativeLayout) layout_detail_payment.findViewById(R.id.billertoken_layout_deskripsi);
                mTableLayout = (TableLayout) layout_detail_payment.findViewById(R.id.billertoken_layout_table);
                mIconArrow = (ImageView) layout_detail_payment.findViewById(R.id.billertoken_arrow_desc);
                mDescLayout.setOnClickListener(descriptionClickListener);
                mIconArrow.setOnClickListener(descriptionClickListener);

                String description = args.getString(DefineValue.DESCRIPTION);

                JSONObject mDataDesc = new JSONObject(description);
                TextView detail_field;
                TextView detail_value;
                TableRow layout_table_row;
                String value_detail_field,value_detail_value;
                Iterator keys = mDataDesc.keys();
                List<String> tempList = new ArrayList<>();

                while(keys.hasNext()) {
                    tempList.add((String) keys.next());
                }
                Collections.sort(tempList);
                isi_field = new JSONArray(tempList);
                isi_value = new JSONArray();

                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT,8f);
                rowParams.setMargins(6,6,6,6);

                for (String aTempList : tempList) {
                    value_detail_field = aTempList;
                    value_detail_value = mDataDesc.getString(aTempList);
                    isi_value.put(value_detail_value);

                    detail_field = new TextView(getActivity());
                    detail_field.setGravity(Gravity.LEFT);
                    detail_field.setLayoutParams(rowParams);
                    detail_value = new TextView(getActivity());
                    detail_value.setGravity(Gravity.RIGHT);
                    detail_value.setLayoutParams(rowParams);
                    detail_value.setTypeface(Typeface.DEFAULT_BOLD);
                    layout_table_row = new TableRow(getActivity());
                    layout_table_row.setLayoutParams(tableParams);
                    layout_table_row.addView(detail_field);
                    layout_table_row.addView(detail_value);
                    detail_field.setText(value_detail_field);
                    detail_value.setText(value_detail_value);
                    mTableLayout.addView(layout_table_row);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(is_input_amount){
            View amount_desire_layout = v.findViewById(R.id.billertoken_amount_desired_layout);
            amount_desire_layout.setVisibility(View.VISIBLE);
            TextView tv_desired_amount = (TextView) amount_desire_layout.findViewById(R.id.billertoken_desired_amount_value);
            amount_desire = args.getString(DefineValue.AMOUNT_DESIRED, "");
            tv_desired_amount.setText(ccy_id+". "+CurrencyFormat.format(amount_desire));
        }

    }

    private View.OnClickListener descriptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Animation mRotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_arrow);
            mRotate.setInterpolator(new LinearInterpolator());
            mRotate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIconArrow.invalidate();
                    if(mTableLayout.getVisibility() == View.VISIBLE){
                        mIconArrow.setImageResource(R.drawable.ic_circle_arrow_down);
                        mTableLayout.setVisibility(View.GONE);
                    }
                    else {
                        mIconArrow.setImageResource(R.drawable.ic_circle_arrow);
                        mTableLayout.setVisibility(View.VISIBLE);
                    }
                    mIconArrow.invalidate();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mIconArrow.startAnimation(mRotate);


        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){

                Timber.d("hit button submit");
                btn_submit.setEnabled(false);
                String _amount;

                if(is_input_amount)
                    _amount = amount_desire;
                else
                    _amount = amount;

                if(is_sgo_plus){
                    changeToSgoPlus(tx_id,_amount,bank_code,product_code,fee);
                }
                else {
                    if(isPIN){
                        CallPINinput(attempt);
                        btn_submit.setEnabled(true);
                    }
                    else{
                        if(inputValidation()) {
                            sentInsertTransTopup(et_token_value.getText().toString(),_amount);
                        }
                        else btn_submit.setEnabled(true);
                    }

                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    private void CallPINinput(int _attempt){
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if(_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT,_attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                btn_submit.setEnabled(false);
                btn_resend.setEnabled(false);

                if(max_token_resend!=0)requestResendToken();

            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if(requestCode == MainPage.REQUEST_FINISH){
          //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                String _amount;
                if(is_input_amount)
                    _amount = amount_desire;
                else
                    _amount = amount;
            //    Log.d("onActivity result", "Biller Fragment result pin value");
                sentInsertTransTopup(value_pin,_amount);
            }
        }
    }

    private void sentInsertTransTopup(String tokenValue, final String _amount){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final Bundle args = getArguments();

            final RequestParams params = MyApiClient.getSignatureWithParams(args.getString(DefineValue.BILLER_COMM_ID),MyApiClient.LINK_INSERT_TRANS_TOPUP,
                    userID,accessKey);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, args.getString(DefineValue.BILLER_COMM_CODE));
            params.put(WebParams.COMM_ID, args.getString(DefineValue.BILLER_COMM_ID));
            params.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            params.put(WebParams.PRODUCT_VALUE, tokenValue);
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params insertTrxTOpupSGOL:"+params.toString());

            MyApiClient.sentInsertTransTopup(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response insertTrxTOpupSGOL:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            getTrxStatus(tx_id,args.getString(DefineValue.BILLER_COMM_ID),_amount);
                            setResultActivity();

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
                            btn_submit.setEnabled(true);
                            if(isPIN && message.equals("PIN tidak sesuai")){
                                Intent i = new Intent(getActivity(), InsertPIN.class);

                                attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                failed = response.optInt(WebParams.MAX_FAILED,0);

                                if(attempt != -1)
                                    i.putExtra(DefineValue.ATTEMPT,failed-attempt);

                                startActivityForResult(i, MainPage.REQUEST_FINISH);
                            }
                            else{
                                onOkButton();
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
                    btn_submit.setEnabled(true);
                    Timber.w("Error Koneksi insert trx topup biller confirm:"+throwable.toString());
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


            RequestParams params;
            if(bank_code.equals("114"))
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                        userID,accessKey);
            else
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_RESEND_TOKEN_SGOL,
                        userID,accessKey);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, getArguments().getString(DefineValue.BILLER_COMM_CODE));
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params resendTokenSGOL:"+params.toString());

            JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            max_token_resend = max_token_resend - 1;

                            changeTextBtnSub();
                            Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();
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
                        }
                        progdialog.dismiss();
                        btn_submit.setEnabled(true);
                        btn_resend.setEnabled(true);
                        if(max_token_resend == 0 ){
                            btn_resend.setEnabled(false);
                            btn_submit.setEnabled(true);
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

                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    btn_submit.setEnabled(true);
                    Timber.w("Error Koneksi resend token biller confirm:"+throwable.toString());
                }
            };

            if(bank_code.equals("114"))// if bank jatim
                MyApiClient.sentDataReqTokenSGOL(getActivity(),params,handler);
            else
                MyApiClient.sentResendTokenSGOL(getActivity(),params,handler);
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }


    }


    private void getTrxStatus(final String txId, String comm_id, final String _amount){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_GET_TRX_STATUS,
                    userID,accessKey);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            if(buy_code == BillerActivity.PURCHASE_TYPE)
                params.put(WebParams.TYPE, DefineValue.BIL_PURCHASE_TYPE);
            else
                params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userID);
            if(isPLN){
                params.put(WebParams.IS_DETAIL, DefineValue.STRING_YES);
            }

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
                                    txstatus, response.optString(WebParams.TX_REMARK, ""), _amount,response);
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

                        btn_submit.setEnabled(true);
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
                    btn_submit.setEnabled(true);
                    Timber.w("Error Koneksi trx stat biller confirm:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

 	private void showDialogError(String message){
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.blocked_pin_title),
                message,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        onOkButton();
                    }
                });

        dialognya.show();
	}
    private void changeToSgoPlus(String _tx_id, String _amount, String _bank_code, String _product_code,
                                 String _fee) {


        Bundle args = getArguments();

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, _bank_code);
        i.putExtra(DefineValue.FEE, _fee);
        i.putExtra(DefineValue.COMMUNITY_CODE, args.getString(DefineValue.BILLER_COMM_CODE,""));
        i.putExtra(DefineValue.TX_ID, _tx_id);
        i.putExtra(DefineValue.AMOUNT, _amount);
        i.putExtra(DefineValue.API_KEY, args.getString(DefineValue.BILLER_API_KEY,""));
        i.putExtra(DefineValue.CALLBACK_URL, args.getString(DefineValue.CALLBACK_URL,""));
        i.putExtra(DefineValue.COMMUNITY_ID, args.getString(DefineValue.BILLER_COMM_ID,""));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        i.putExtra(DefineValue.SHARE_TYPE, shareType);
        i.putExtra(DefineValue.DENOM_DATA,item_name);
        i.putExtra(DefineValue.BUY_TYPE, buy_code);
        i.putExtra(DefineValue.PAYMENT_NAME,payment_name);
        i.putExtra(DefineValue.BILLER_NAME,biller_name);
        i.putExtra(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);
        i.putExtra(DefineValue.DESTINATION_REMARK, cust_id);

        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(_fee);
        i.putExtra(DefineValue.TOTAL_AMOUNT,String.valueOf(totalAmount));

        if(buy_code == BillerActivity.PURCHASE_TYPE)
            i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.BIL_PURCHASE_TYPE);
        else
            i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.BIL_PAYMENT_TYPE);


        String _isi_field = "", _isi_value = "",_isi_amount_desired = "";
        if(is_display_amount){
            _isi_field = String.valueOf(isi_field);
            _isi_value = String.valueOf(isi_value);
        }
        if(is_input_amount)_isi_amount_desired = amount_desire;

        if(isPLN){
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
            args.putString(DefineValue.BILLER_TYPE,biller_type_code);
            args.putBoolean(DefineValue.IS_PLN,isPLN);
        }


        i.putExtra(DefineValue.DESC_FIELD, _isi_field);
        i.putExtra(DefineValue.DESC_VALUE, _isi_value);
        i.putExtra(DefineValue.AMOUNT_DESIRED,_isi_amount_desired);
        Timber.d("isi args:"+args.toString());
        btn_submit.setEnabled(true);
        switchActivityIB(i);
    }

    private void showReportBillerDialog(String name,String date,String userId, String txId,String itemName,String txStatus,
                                        String txRemark, String _amount, JSONObject response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = new ReportBillerDialog();
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        args.putInt(DefineValue.BUY_TYPE, buy_code);
        args.putString(DefineValue.PAYMENT_NAME, payment_name);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.DESTINATION_REMARK, cust_id);
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);

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


        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(fee);
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));

        String _isi_field = "", _isi_value = "",_isi_amount_desired = "";
        if(is_display_amount){
            _isi_field = String.valueOf(isi_field);
            _isi_value = String.valueOf(isi_value);
        }
        if(is_input_amount)_isi_amount_desired = amount_desire;

        args.putString(DefineValue.DESC_FIELD, _isi_field);
        args.putString(DefineValue.DESC_VALUE, _isi_value);

        if(_isi_amount_desired.isEmpty())
            args.putString(DefineValue.AMOUNT_DESIRED, _isi_amount_desired);
        else
            args.putString(DefineValue.AMOUNT_DESIRED, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_isi_amount_desired));

        if(isPLN && response.has(WebParams.DETAIL)){
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
            args.putString(DefineValue.BILLER_TYPE,biller_type_code);
            args.putString(DefineValue.DETAIL,response.optString(WebParams.DETAIL,""));
        }

        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
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

    private void switchActivityIB(Intent mIntent){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    private void setResultActivity(){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.setResultActivity();
    }

    private void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_resend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");

            }
        });
    }

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            exit();
        }
    };


    private boolean inputValidation(){
        if(et_token_value.getText().toString().length()==0){
            et_token_value.requestFocus();
            et_token_value.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
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
                exit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.wtf("masuk onResume");
        if(!is_sgo_plus)
            if(!isPIN)
                toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!is_sgo_plus)
            if(!isPIN)
                toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity ) getActivity();
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

            if(mBundle != null){
                Object[] pdus = (Object[]) mBundle.get("pdus");
                assert pdus != null;
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

    private void insertTokenEdit(String _kode_otp, String _member_kode){
        Timber.d("isi _kode_otp, _member_kode, member kode session:"+_kode_otp+ " / " +_member_kode +" / "+ sp.getString(DefineValue.MEMBER_CODE,""));
        if(_member_kode.equals(sp.getString(DefineValue.MEMBER_CODE,""))){
            et_token_value.setText(_kode_otp);
        }
    }

    private void exit(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }

    @Override
    public void onOkButton() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}