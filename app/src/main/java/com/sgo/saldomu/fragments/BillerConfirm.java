package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.SmsMessage;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.LevelClass;
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
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusModel;
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/*
  Created by Administrator on 3/5/2015.
 */
public class BillerConfirm extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {


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
    private ImageView mIconArrow;
    private TableLayout mTableLayout;
    private LinearLayout ly_additionalFee;
    private TextView tv_additionalFee;
    private String additionalFee;
    private LevelClass levelClass;
    private Boolean isAgent;

    private Switch favoriteSwitch;
    private EditText notesEditText;
    private String productType = "";
    private String item_id = "";
    String value_pin = "";
    String _amount = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_token_confirm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        tv_id_cust = v.findViewById(R.id.billertoken_biller_id_value);
        tv_item_name_value = v.findViewById(R.id.billertoken_item_name_value);
        tv_payment_name = v.findViewById(R.id.billertoken_item_payment_value);
        tv_amount_value = v.findViewById(R.id.billertoken_amount_value);
        tv_fee_value = v.findViewById(R.id.billertoken_fee_value);
        ly_additionalFee = v.findViewById(R.id.layout_additionalFee);
        tv_additionalFee = v.findViewById(R.id.tv_additionalFee);
        tv_total_amount_value = v.findViewById(R.id.billertoken_total_amount_value);
        btn_submit = v.findViewById(R.id.billertoken_btn_verification);
        btn_cancel = v.findViewById(R.id.billertoken_btn_cancel);
        favoriteSwitch = v.findViewById(R.id.favorite_switch);
        notesEditText = v.findViewById(R.id.notes_edit_text);

        btn_submit.setOnClickListener(submitListener);
        btn_cancel.setOnClickListener(cancelListener);

        initializeLayout();
    }

    private void initializeLayout() {

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(getActivity(), sp);
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        Bundle args = getArguments();
        cust_id = args.getString(DefineValue.CUST_ID, "");
        tx_id = args.getString(DefineValue.TX_ID, "");
        ccy_id = args.getString(DefineValue.CCY_ID, "");
        amount = args.getString(DefineValue.AMOUNT, "");
        fee = args.getString(DefineValue.FEE, "");
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
        biller_name = args.getString(DefineValue.BILLER_NAME, "");
        attempt = args.getInt(DefineValue.ATTEMPT, -1);
        isShowDescription = args.getBoolean(DefineValue.IS_SHOW_DESCRIPTION, false);
        biller_type_code = args.getString(DefineValue.BILLER_TYPE);
        additionalFee = args.getString(DefineValue.ADDITIONAL_FEE);
        item_id = args.getString(DefineValue.BILLER_ITEM_ID);
        productType = ((BillerActivity) Objects.requireNonNull(getActivity()))._biller_type_code;
        Timber.d("isi args:" + args.toString());

        if (biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS) ||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_NON_TAG) ||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN) ||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN_TKN)) {
            isPLN = true;
        }

        tv_item_name_value.setText(item_name);
        tv_id_cust.setText(cust_id);
        tv_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(amount));

//        tv_payment_name.setText(payment_name);
        if (payment_name.equalsIgnoreCase("UNIK")) {
            tv_payment_name.setText(getString(R.string.appname));
        } else {
            tv_payment_name.setText(payment_name);
        }
        tv_fee_value.setText(ccy_id + ". " + CurrencyFormat.format(fee));
        tv_total_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(total_amount));

        if (!is_sgo_plus) {
            merchant_type = args.getString(DefineValue.AUTHENTICATION_TYPE, "");
            if (merchant_type.equals(DefineValue.AUTH_TYPE_OTP) || product_payment_type.equals(DefineValue.BANKLIST_TYPE_SMS)) {
                LinearLayout layoutOTP = v.findViewById(R.id.layout_token);
                layoutOTP.setVisibility(View.VISIBLE);
                View layout_btn_resend = v.findViewById(R.id.layout_btn_resend);
                btn_resend = v.findViewById(R.id.billertoken_btn_resend);
                et_token_value = layoutOTP.findViewById(R.id.billertoken_token_value);
                int max_length_token;
                if (product_payment_type.equals(DefineValue.BANKLIST_TYPE_SMS)) {
                    if (bank_code.equals("114"))
                        max_length_token = 5;
                    else
                        max_length_token = 6;
                } else {
                    max_length_token = 4;
                }
                et_token_value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length_token)});

                layout_btn_resend.setVisibility(View.VISIBLE);
                et_token_value.requestFocus();
                btn_resend.setOnClickListener(resendListener);
                changeTextBtnSub();
                isPIN = false;
            } else {
                isPIN = true;
                new UtilsLoader(getActivity(), sp).getFailedPIN(userPhoneID, new OnLoadDataListener() { //get pin attempt
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
        }

        if (buy_code == BillerActivity.PURCHASE_TYPE) {
            View layout_biller_name = v.findViewById(R.id.billertoken_layout_biller_name);
            layout_biller_name.setVisibility(View.VISIBLE);
            TextView tv_biller_name_value = layout_biller_name.findViewById(R.id.billertoken_biller_name_value);
            tv_biller_name_value.setText(biller_name);
        }

        if (is_display_amount && isShowDescription) {
            try {
                View layout_detail_payment = v.findViewById(R.id.billertoken_layout_payment);
                layout_detail_payment.setVisibility(View.VISIBLE);
                RelativeLayout mDescLayout = layout_detail_payment.findViewById(R.id.billertoken_layout_deskripsi);
                mTableLayout = layout_detail_payment.findViewById(R.id.billertoken_layout_table);
                mIconArrow = layout_detail_payment.findViewById(R.id.billertoken_arrow_desc);
                mDescLayout.setOnClickListener(descriptionClickListener);
                mIconArrow.setOnClickListener(descriptionClickListener);

                String description = args.getString(DefineValue.DESCRIPTION);

                JSONObject mDataDesc = new JSONObject(description);
                TextView detail_field;
                TextView detail_value;
                TableRow layout_table_row;
                String value_detail_field, value_detail_value;
                Iterator keys = mDataDesc.keys();
                List<String> tempList = new ArrayList<>();


                //jika BPJS sorting fieldnya sesuai format
//                if(biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS)) {
//                    tempList = JsonSorting.BPJSInquirySortingField();
//                }
//                else {
                while (keys.hasNext()) {
                    tempList.add((String) keys.next());
                }
//                }

                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 8f);
                rowParams.setMargins(6, 6, 6, 6);

                for (String aTempList : tempList) {
                    value_detail_field = aTempList;
                    value_detail_value = mDataDesc.getString(aTempList);

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

        if (is_input_amount) {
            View amount_desire_layout = v.findViewById(R.id.billertoken_amount_desired_layout);
            amount_desire_layout.setVisibility(View.VISIBLE);
            TextView tv_desired_amount = amount_desire_layout.findViewById(R.id.billertoken_desired_amount_value);
            amount_desire = args.getString(DefineValue.AMOUNT_DESIRED, "");
            tv_desired_amount.setText(ccy_id + ". " + CurrencyFormat.format(amount_desire));
        }

        if (isAgent) {
            ly_additionalFee.setVisibility(View.VISIBLE);
            tv_additionalFee.setText(ccy_id + ". " + CurrencyFormat.format(additionalFee));
        }


        favoriteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notesEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            notesEditText.setEnabled(isChecked);
        });

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
                    if (mTableLayout.getVisibility() == View.VISIBLE) {
                        mIconArrow.setImageResource(R.drawable.ic_circle_arrow_down);
                        mTableLayout.setVisibility(View.GONE);
                    } else {
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

            if (InetHandler.isNetworkAvailable(getActivity())) {

                if (favoriteSwitch.isChecked() && notesEditText.getText().toString().length() == 0) {
                    notesEditText.requestFocus();
                    notesEditText.setError(getString(R.string.payfriends_notes_zero));
                    return;
                }

                Timber.d("hit button submit");
                btn_submit.setEnabled(false);
                String _amount;

                if (is_input_amount)
                    _amount = amount_desire;
                else
                    _amount = amount;

                if (is_sgo_plus) {
                    changeToSgoPlus(tx_id, _amount, bank_code, product_code, fee);
                } else {
                    if (isPIN) {
//                        Toast.makeText(getActivity(), "Timeout From Service Provider", Toast.LENGTH_LONG).show();
                        CallPINinput(attempt);
                        btn_submit.setEnabled(true);
                    } else {
                        if (inputValidation()) {
                            sentInsertTransTopup(et_token_value.getText().toString(), _amount);
                        } else btn_submit.setEnabled(true);
                    }

                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    private void CallPINinput(int _attempt) {
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                btn_submit.setEnabled(false);
                btn_resend.setEnabled(false);

                if (max_token_resend != 0) requestResendToken();

            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                if (is_input_amount)
                    _amount = amount_desire;
                else
                    _amount = amount;
                //    Log.d("onActivity result", "Biller Fragment result pin value");

                if (favoriteSwitch.isChecked()) {
                    onSaveToFavorite();
                } else {
                    sentInsertTransTopup(value_pin, _amount);
                }
            }
        }
    }

    private void sentInsertTransTopup(String tokenValue, final String _amount) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final Bundle args = getArguments();

            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            String commID = args.getString(DefineValue.BILLER_COMM_ID);
            String commCode = args.getString(DefineValue.BILLER_COMM_CODE);
            extraSignature = tx_id + args.getString(DefineValue.BILLER_COMM_CODE) + product_code + tokenValue;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.COMM_ID, commID);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncryptCommID(commID, uuid, dateTime, userPhoneID, tokenValue, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject response) {
                            SentPaymentBillerModel model = getGson().fromJson(response, SentPaymentBillerModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                getTrxStatus(tx_id, args.getString(DefineValue.BILLER_COMM_ID), _amount);
                                setResultActivity(MainPage.RESULT_BALANCE);

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {

                                code = model.getError_code() + " : " + model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                String message = model.getError_message();

                                if (isPIN && message.equals("PIN tidak sesuai")) {
                                    Intent i = new Intent(getActivity(), InsertPIN.class);

                                    attempt = model.getFailed_attempt();
                                    failed = model.getMax_failed();

                                    if (attempt != -1)
                                        i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                                } else {
                                    onOkButton();
                                }

                            }


                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            btn_submit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }


    private void requestResendToken() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String url;
            HashMap<String, Object> params;
            if (bank_code.equals("114"))
                url = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            else
                url = MyApiClient.LINK_RESEND_TOKEN_SGOL;

            extraSignature = tx_id + getArguments().getString(DefineValue.BILLER_COMM_CODE) + product_code;
            params = RetrofitService.getInstance().getSignature(url, extraSignature);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, getArguments().getString(DefineValue.BILLER_COMM_CODE));
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params resendTokenSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject response) {
                            jsonModel model = getGson().fromJson(response, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                max_token_resend = max_token_resend - 1;

                                changeTextBtnSub();
                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_text_resend_token), Toast.LENGTH_SHORT).show();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {
                                code = model.getError_code();

                                Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            btn_submit.setEnabled(true);
                            btn_resend.setEnabled(true);

                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            if (max_token_resend == 0) {
                                btn_resend.setEnabled(false);
                                btn_submit.setEnabled(true);
                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }


    }


    private void getTrxStatus(final String txId, String comm_id, final String _amount) {
        try {

            extraSignature = txId + comm_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            if (buy_code == BillerActivity.PURCHASE_TYPE)
                params.put(WebParams.TYPE, DefineValue.BIL_PURCHASE_TYPE);
            else
                params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);
            if (isPLN) {
                params.put(WebParams.IS_DETAIL, DefineValue.STRING_YES);
            }

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject response) {
                            GetTrxStatusModel model = getGson().fromJson(response, GetTrxStatusModel.class);

                            String code = model.getError_code();

                            if (!model.getOn_error()) {
                                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                    String txstatus = model.getTx_status();
                                    showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""),
                                            sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                            txstatus, _amount, model);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                }else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                } else {
                                    String msg = model.getError_message();
                                    showDialog(msg);
                                }
                            } else {
                                Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            btn_submit.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showDialogError(String message) {
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
        i.putExtra(DefineValue.COMMUNITY_CODE, args.getString(DefineValue.BILLER_COMM_CODE, ""));
        i.putExtra(DefineValue.TX_ID, _tx_id);
        i.putExtra(DefineValue.AMOUNT, _amount);
        i.putExtra(DefineValue.API_KEY, args.getString(DefineValue.BILLER_API_KEY, ""));
        i.putExtra(DefineValue.CALLBACK_URL, args.getString(DefineValue.CALLBACK_URL, ""));
        i.putExtra(DefineValue.COMMUNITY_ID, args.getString(DefineValue.BILLER_COMM_ID, ""));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        i.putExtra(DefineValue.SHARE_TYPE, shareType);
        i.putExtra(DefineValue.DENOM_DATA, item_name);
        i.putExtra(DefineValue.BUY_TYPE, buy_code);
        i.putExtra(DefineValue.PAYMENT_NAME, payment_name);
        i.putExtra(DefineValue.BILLER_NAME, biller_name);
        i.putExtra(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);
        i.putExtra(DefineValue.DESTINATION_REMARK, cust_id);

        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(_fee);
        i.putExtra(DefineValue.TOTAL_AMOUNT, String.valueOf(totalAmount));

        if (buy_code == BillerActivity.PURCHASE_TYPE)
            i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.BIL_PURCHASE_TYPE);
        else
            i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.BIL_PAYMENT_TYPE);


        String _isi_amount_desired = "";

        if (is_input_amount) _isi_amount_desired = amount_desire;

        if (isPLN) {
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
            args.putString(DefineValue.BILLER_TYPE, biller_type_code);
            args.putBoolean(DefineValue.IS_PLN, isPLN);
        }

        i.putExtra(DefineValue.AMOUNT_DESIRED, _isi_amount_desired);
        Timber.d("isi args:" + args.toString());
        btn_submit.setEnabled(true);
        switchActivityIB(i);
    }

    private void showReportBillerDialog(String name, String userId, String txId, String itemName, String txStatus,
                                        String _amount, GetTrxStatusModel model) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.getCreated()));
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        args.putInt(DefineValue.BUY_TYPE, buy_code);
        args.putString(DefineValue.PAYMENT_NAME, payment_name);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additionalFee));
        args.putString(DefineValue.DESTINATION_REMARK, NoHPFormat.formatTo62(cust_id));
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, model.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.getTx_remark());


//        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(fee);
//        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));

        String _isi_amount_desired = "";
        if (is_input_amount)
            _isi_amount_desired = amount_desire;

        args.putString(DefineValue.DETAILS_BILLER, model.getProduct_name());

        if (_isi_amount_desired.isEmpty())
            args.putString(DefineValue.AMOUNT_DESIRED, _isi_amount_desired);
        else
            args.putString(DefineValue.AMOUNT_DESIRED, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_isi_amount_desired));

        if (isPLN && model.getProduct_name() == null) {
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
            if (biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS))
                args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_BPJS);
            args.putString(DefineValue.BILLER_TYPE, biller_type_code);
        }

        args.putString(DefineValue.BILLER_DETAIL, toJson(model.getBiller_detail()).toString());
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.getBuss_scheme_name());
        args.putString(DefineValue.PRODUCT_NAME, model.getProduct_name());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this, 0);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
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

    private void switchActivityIB(Intent mIntent) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private void setResultActivity(int result) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.setResultActivity(result);
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


    private boolean inputValidation() {
        if (et_token_value.getText().toString().length() == 0) {
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
        if (!is_sgo_plus)
            if (!isPIN)
                toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!is_sgo_plus)
            if (!isPIN)
                toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
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
        if (_member_kode.equals(sp.getString(DefineValue.MEMBER_CODE, ""))) {
            et_token_value.setText(_kode_otp);
        }
    }

    private void exit() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }

    @Override
    public void onOkButton() {
        assert getFragmentManager() != null;
        getFragmentManager().popBackStackImmediate(BillerActivity.FRAG_BIL_INPUT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void onSaveToFavorite() {
        final Bundle args = getArguments();

        extraSignature = cust_id + productType + "BIL";
        Log.e("extraSignature params ", extraSignature);
        String url = MyApiClient.LINK_TRX_FAVORITE_SAVE;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(url, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.PRODUCT_TYPE, productType);
        params.put(WebParams.CUSTOMER_ID, cust_id);
        params.put(WebParams.TX_FAVORITE_TYPE, "BIL");
//        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.COMM_ID, args.getString(DefineValue.BILLER_COMM_ID));
        params.put(WebParams.NOTES, notesEditText.getText().toString());
        //DefineValue.BILLER_ITEM_ID
        params.put(WebParams.DENOM_ITEM_ID, item_id);

        Log.e("params ", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(response.toString(), jsonModel.class);
                            Log.e("onResponses ", response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            }else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("onResponses ", throwable.getLocalizedMessage());
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        sentInsertTransTopup(value_pin, _amount);
                    }
                });
    }
}