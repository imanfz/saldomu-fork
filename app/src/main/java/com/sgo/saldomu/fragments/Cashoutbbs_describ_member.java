package com.sgo.saldomu.fragments;
/*
  Created by Administrator on 1/31/2017.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.InetHandler;
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
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.OTPModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

public class Cashoutbbs_describ_member extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {
    public final static String TAG = "com.sgo.saldomu.fragments.Cashoutbbs_describ_member";
    View v;
    //    layout_button_transaction;
    String authType, amount, fee, total, ccyId, txId, created, comm_code,
            product_name, product_code, bank_code, bank_name, callback_url, api_key, comm_id, otp_member;
    private String product_h2h;
    TextView tvAgent, tvAmount, tvFee, tvTotal, tvCode, tvTxId, tvCreated, tvAlert, tvBankProduct, tvAdditionalFee;
    LinearLayout layoutOTP, layoutNoEmpty, layoutButton;
    RelativeLayout layoutEmpty;
    EditText tokenValue;
    Button btnOk, btnCancel, btnResend;
    int pin_attempt = -1;
    boolean isPIN = true, isOTP = false;
    int start = 0;
    Handler handlerWS;
    Runnable runnableWS;
    ProgressDialog progdialog;
    ProgressBar loading;
    private int max_token_resend = 3;
    //    private Button btn_proses_transaction;
    int failed = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.cashoutbbs_describ_member, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        if (flagLogin.equals(DefineValue.STRING_NO)) {
            getActivity().finish();
        } else {
            String notifDataNextLogin = sp.getString(DefineValue.NOTIF_DATA_NEXT_LOGIN, "");
            if (!notifDataNextLogin.equals("")) {
                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
            }
        }

//        authType = sp.getString(DefineValue.AUTHENTICATION_TYPE, "");

//        isPIN = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_PIN);
//        isOTP = authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP);

        layoutEmpty = v.findViewById(R.id.bbscashoutmember_empty_layout);
        layoutNoEmpty = v.findViewById(R.id.bbscashoutmember_layout);
//        layoutCode = (LinearLayout) v.findViewById(R.id.bbscashoutmember_code_layout);
        layoutButton = v.findViewById(R.id.bbscashoutmember_bottom_layout);
        tvTxId = v.findViewById(R.id.bbscashoutmember_tx_id_value);
        tvCreated = v.findViewById(R.id.bbscashoutmember_created_value);
        tvAgent = v.findViewById(R.id.bbscashoutmember_agent_value);
        tvAmount = v.findViewById(R.id.bbscashoutmember_amount_value);
        tvFee = v.findViewById(R.id.bbscashoutmember_fee_value);
        tvAdditionalFee = v.findViewById(R.id.bbscashoutmember_additionalfee);
        tvTotal = v.findViewById(R.id.bbscashoutmember_total_value);
        tvCode = v.findViewById(R.id.bbscashoutmember_code);
        tvBankProduct = v.findViewById(R.id.bbscashoutmember_bank_product_value);
        loading = v.findViewById(R.id.prgLoading);
        tvAlert = v.findViewById(R.id.text_alert);
        layoutOTP = v.findViewById(R.id.bbscashoutmember_layout_OTP);
        tokenValue = v.findViewById(R.id.bbscashoutmember_value_otp);
        btnOk = v.findViewById(R.id.bbscashoutmember_btn_ok);
        btnCancel = v.findViewById(R.id.bbscashoutmember_btn_cancel);
//        btn_proses_transaction = (Button) v.findViewById(R.id.btn_verification);
//        layout_button_transaction = v.findViewById(R.id.layout_button_check_transaction);

        layoutEmpty.setVisibility(View.VISIBLE);
        layoutNoEmpty.setVisibility(View.GONE);
//        layoutCode.setVisibility(View.GONE);

        handlerWS = new Handler();
        runnableWS = new Runnable() {
            @Override
            public void run() {
                sentListMemberATC();
            }
        };
        handlerWS.post(runnableWS);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InetHandler.isNetworkAvailable(getActivity())) {
                    if (getProduct_h2h().equalsIgnoreCase("Y")) {
                        if (isPIN) {
                            Intent i = new Intent(getActivity(), InsertPIN.class);
                            if (pin_attempt != -1 && pin_attempt < 2)
                                i.putExtra(DefineValue.ATTEMPT, pin_attempt);
                            startActivityForResult(i, MainPage.REQUEST_FINISH);
                        } else if (isOTP) {
                            if (inputValidation()) {
                                OTPMemberATC(tokenValue.getText().toString(), txId);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Authentication type kosong", Toast.LENGTH_LONG).show();
                        }
                    } else if (getProduct_h2h().equalsIgnoreCase("N")) {
                        changeToSGOPlus(txId, product_code, product_name, bank_code, amount, fee, total, bank_name);
                    }
//                    else
//                        Toast.makeText(getActivity(), "on click producth2h:" + String.valueOf(tempResponse), Toast.LENGTH_SHORT).show();
                } else {
                    DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
                }
            }
        });
        btnCancel.setOnClickListener(btnCancelListener);
//        btn_proses_transaction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(InetHandler.isNetworkAvailable(getActivity())) {
//                    getTrxStatus();
//                }
//                else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
//            }
//        });
        validasiTutorial();
    }

    private void validasiTutorial() {
        if (sp.contains(DefineValue.TUTORIAL_KONFIRMASI_CASHOUT_BBS)) {
            Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_KONFIRMASI_CASHOUT_BBS, false);
            if (is_first_time)
                showTutorial();
        } else {
            showTutorial();
        }
    }

    private void showTutorial() {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_konfirmasi_cashout_bbs);
        startActivity(intent);
    }

    Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.cashoutmember_cancel_message))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sentRejectConfirmCashout();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (authType.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP)) {
                    if (max_token_resend != 0)
                        sentResendToken(txId);

                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));

        }
    };

    public void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnResend.setText(getString(R.string.reg2_btn_text_resend_token_sms) + " (" + max_token_resend + ")");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                OTPMemberATC(value_pin, txId);
            }
        }
    }

    public void sentListMemberATC() {
        try {
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_LIST_MEMBER_A2C, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.CUSTOMER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent list member atc:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_LIST_MEMBER_A2C, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {

                                jsonModel model = getGson().fromJson(response.toString(), jsonModel.class);

//                                JSONObject response = new JSONObject(getGson().toJson(model));
//                                tempResponse = new JSONObject(getGson().toJson(model));
                                String error_message = response.getString(WebParams.ERROR_MESSAGE);
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    handlerWS.removeCallbacks(runnableWS);
                                    layoutEmpty.setVisibility(View.GONE);
                                    layoutNoEmpty.setVisibility(View.VISIBLE);

                                    txId = response.optString(WebParams.TX_ID, "");
                                    ccyId = response.optString(WebParams.CCY_ID, "");
                                    product_code = response.optString(WebParams.PRODUCT_CODE, "");
                                    product_name = response.optString(WebParams.PRODUCT_NAME, "");
                                    bank_code = response.optString(WebParams.BANK_CODE, "");
                                    bank_name = response.optString(WebParams.BANK_NAME, "");
                                    api_key = response.optString(WebParams.API_KEY, "");
                                    callback_url = response.optString(WebParams.CALLBACK_URL, "");
                                    comm_id = response.optString(WebParams.COMM_ID, "");
                                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date date = formatter.parse(response.optString(WebParams.CREATED, ""));
                                    SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
                                    created = newFormat.format(date);
                                    tvTxId.setText(txId);
                                    tvCreated.setText(created);
                                    tvAgent.setText(response.optString(WebParams.MEMBER_NAME, ""));
                                    tvBankProduct.setText(product_name);
                                    tvAmount.setText(ccyId + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT, "0")));
                                    tvFee.setText(ccyId + ". " + CurrencyFormat.format(response.optString(WebParams.FEE_AMOUNT, "0")));
                                    tvTotal.setText(ccyId + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT, "0")));
                                    tvAdditionalFee.setText(ccyId + ". " + CurrencyFormat.format(response.optString(WebParams.ADDITIONAL_FEE, "0")));
                                    amount = response.optString(WebParams.TX_AMOUNT, "0");
                                    fee = response.optString(WebParams.FEE_AMOUNT, "0");
                                    total = response.optString(WebParams.TOTAL_AMOUNT, "0");
                                    setProduct_h2h(response.optString(WebParams.PRODUCT_H2H, ""));
                                    comm_code = response.optString(WebParams.COMM_CODE, "");
                                    setPayment(getProduct_h2h());
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), error_message);
                                } else if (code.equals(ErrorDefinition.NO_TRANSACTION)) {
                                    loading.setVisibility(View.GONE);
                                    tvAlert.setText(getString(R.string.cashoutmember_alert_no_tx));
                                    handlerWS.postDelayed(runnableWS, 60000);
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
                                    Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
                                    handlerWS.postDelayed(runnableWS, 60000);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            if (failed < 3) {
                                failed++;
                                handlerWS.postDelayed(runnableWS, 60000);
                            } else {
                                Toast.makeText(getActivity(), "Silahkan coba kembali", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void setPayment(String _product_h2h) {
        if (_product_h2h.equalsIgnoreCase("Y")) {
            if (isOTP) {
                layoutOTP.setVisibility(View.VISIBLE);
                btnResend = v.findViewById(R.id.btn_resend_token);

                View layout_resendbtn = v.findViewById(R.id.layout_btn_resend);
                layout_resendbtn.setVisibility(View.VISIBLE);

                btnResend.setOnClickListener(resendListener);
                changeTextBtnSub();
            } else {
                layoutOTP.setVisibility(View.GONE);
                new UtilsLoader(getActivity(), sp).getFailedPIN(userPhoneID, new OnLoadDataListener() { //get pin attempt
                    @Override
                    public void onSuccess(Object deData) {
                        pin_attempt = (int) deData;
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
    }

    public void OTPMemberATC(String token, final String tx_id) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String link = MyApiClient.LINK_BBS_OTP_MEMBER_A2C;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = tx_id + token + comm_code;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, token, subStringLink));
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.CUSTOMER_ID, userPhoneID);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.SENDER_ID, "GOMOBILE");
            params.put(WebParams.RECEIVER_ID, "GOWORLD");
            Timber.d("isi params sent otp member ATC:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            OTPModel model = getGson().fromJson(object, OTPModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                            layoutCode.setVisibility(View.VISIBLE);
//                            layout_button_transaction.setVisibility(View.VISIBLE);
//                            if(isOTP) layoutOTP.setVisibility(View.GONE);
//                            layoutButton.setVisibility(View.GONE);
//                            tvCode.setText(response.getString(WebParams.OTP_MEMBER));
                                otp_member = model.getOtp_member();
//                                additonalFee = model.getAdditional_fee();
                                getTrxStatusBBS(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(ErrorDefinition.ERROR_CODE_WRONG_TOKEN)) {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            } else if (code.equals(ErrorDefinition.WRONG_PIN_CASHOUT)) {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getActivity(), InsertPIN.class);
                                pin_attempt = pin_attempt - 1;
                                if (pin_attempt != -1 && pin_attempt < 2)
                                    i.putExtra(DefineValue.ATTEMPT, pin_attempt);
                                startActivityForResult(i, MainPage.REQUEST_FINISH);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            }else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                getActivity().finish();
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
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sentRejectConfirmCashout() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String extraSignature = txId + comm_code;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REJECT_CONFIRM_CASHOUT, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.TX_ID, txId);

            Timber.d("isi params sent reject confirm cashout:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REJECT_CONFIRM_CASHOUT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals(WebParams.NO_DATA_CODE)) {
                                getActivity().finish();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            }else {
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
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sentResendToken(String _data) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_RESEND_TOKEN_LKD, extraSignature);
            params.put(WebParams.TX_ID, _data);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent resend token:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_RESEND_TOKEN_LKD, params,
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
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            }else {
                                code = model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                            }
                            if (max_token_resend == 0) {
                                btnResend.setEnabled(false);


                                Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_LONG).show();
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
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_STATUS_BBS
                    , extraSignature);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.USER_ID, userId);

            Timber.d("isi params sent get Trx Status bbs:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_STATUS_BBS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);
                                String code = model.getError_code();
                                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                    String txstatus = model.getTx_status();

                                    JSONObject response = new JSONObject(getGson().toJson(model));

                                    showReportBillerDialog(userName, DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
                                            txId, userId, response.optString(WebParams.TX_BANK_NAME, ""), response.optString(WebParams.PRODUCT_NAME, ""),
                                            response.optString(WebParams.ADMIN_FEE, "0"), response.optString(WebParams.TX_AMOUNT, "0"),
                                            txstatus, response.getString(WebParams.TX_REMARK), response.optString(WebParams.TOTAL_AMOUNT, "0"),
                                            response.optString(WebParams.MEMBER_NAME, ""), response.optString(WebParams.SOURCE_BANK_NAME, ""),
                                            response.optString(WebParams.MEMBER_SHOP_NO, ""), response.optString(WebParams.SOURCE_ACCT_NAME, ""),
                                            response.optString(WebParams.BENEF_BANK_NAME, ""), response.optString(WebParams.BENEF_ACCT_NO, ""),
                                            response.optString(WebParams.BENEF_ACCT_NAME, ""), response.optString(WebParams.MEMBER_SHOP_PHONE, ""),
                                            response.optString(WebParams.MEMBER_SHOP_NAME, ""), otp_member, response.optString(WebParams.BUSS_SCHEME_CODE),
                                            response.optString(WebParams.BUSS_SCHEME_NAME), response.optString(WebParams.MEMBER_PHONE, ""),
                                            response.optString(WebParams.ADDITIONAL_FEE, "0"), model);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + object.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                }else {
                                    String msg = model.getError_message();
//                            if(code.equals("0003")){
//                                showReportBillerDialog(userName, DateTimeFormat.formatToID(response.optString(WebParams.CREATED,"")),
//                                        txId, userId,response.optString(WebParams.TX_BANK_NAME,""),response.optString(WebParams.PRODUCT_NAME,""),
//                                        response.optString(WebParams.ADMIN_FEE,"0"),response.optString(WebParams.TX_AMOUNT,"0"),
//                                        response.getString(WebParams.TX_STATUS),response.getString(WebParams.TX_REMARK), response.optString(WebParams.TOTAL_AMOUNT,"0"),
//                                        response.optString(WebParams.MEMBER_NAME,""),response.optString(WebParams.SOURCE_BANK_NAME,""),
//                                        response.optString(WebParams.SOURCE_ACCT_NO,""),response.optString(WebParams.SOURCE_ACCT_NAME,""),
//                                        response.optString(WebParams.BENEF_BANK_NAME,""),response.optString(WebParams.BENEF_ACCT_NO,""),
//                                        response.optString(WebParams.BENEF_ACCT_NAME,""), response.optString(WebParams.MEMBER_SHOP_PHONE,""),
//                                        response.optString(WebParams.MEMBER_SHOP_NAME,""), response.optString(WebParams.OTP_MEMBER,""));
//                            }
//                            else
                                    showDialog(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (out.isShowing())
                                out.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showReportBillerDialog(String userName, String date, String txId, String userId, String bankName, String bankProduct,
                                        String fee, String amount, String txStatus, String txRemark, String total_amount, String member_name,
                                        String source_bank_name, String member_shop_no, String source_acct_name,
                                        String benef_bank_name, String benef_acct_no, String benef_acct_name, String member_shop_phone,
                                        String member_shop_name, String otp_member, String buss_scheme_code, String buss_scheme_name, String member_phone,
                                        String additional_fee, GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_MEMBER_OTP);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.BANK_NAME, bankName);
        args.putString(DefineValue.BANK_PRODUCT, bankProduct);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(additional_fee));

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
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, txRemark);
        args.putString(DefineValue.MEMBER_NAME, member_name);
        args.putString(DefineValue.SOURCE_ACCT, source_bank_name);
        args.putString(DefineValue.MEMBER_SHOP_NO, member_shop_no);
        args.putString(DefineValue.SOURCE_ACCT_NAME, source_acct_name);
        args.putString(DefineValue.BANK_BENEF, benef_bank_name);
        args.putString(DefineValue.NO_BENEF, benef_acct_no);
        args.putString(DefineValue.NAME_BENEF, benef_acct_name);
        args.putString(DefineValue.MEMBER_SHOP_PHONE, member_shop_phone);
        args.putString(DefineValue.MEMBER_SHOP_NAME, member_shop_name);
        args.putString(DefineValue.OTP_MEMBER, otp_member);
        args.putString(DefineValue.BUSS_SCHEME_CODE, buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME, buss_scheme_name);
        args.putString(DefineValue.MEMBER_PHONE, member_phone);

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    void showDialog(String msg) {
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

    public void setMemberOTP(String otp) {
//        layoutCode.setVisibility(View.VISIBLE);
//        layoutButton.setVisibility(View.GONE);
//        tvCode.setText(otp);
        sentListMemberATC();
    }

    private void changeToSGOPlus(String _tx_id, String _product_code, String _product_name, String _bank_code,
                                 String _amount, String fee, String totalAmount, String _bank_name) {

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, _product_code);
        i.putExtra(DefineValue.BANK_CODE, _bank_code);
        i.putExtra(DefineValue.BANK_NAME, _bank_name);
        i.putExtra(DefineValue.PRODUCT_NAME, _product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.COMMUNITY_CODE, comm_code);
        i.putExtra(DefineValue.TX_ID, _tx_id);
        i.putExtra(DefineValue.AMOUNT, _amount);
        i.putExtra(DefineValue.SHARE_TYPE, "1");
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        i.putExtra(DefineValue.CALLBACK_URL, callback_url);
        i.putExtra(DefineValue.API_KEY, api_key);

        i.putExtra(DefineValue.TOTAL_AMOUNT, totalAmount);
        i.putExtra(DefineValue.COMMUNITY_ID, comm_id);
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.BBS_MEMBER_OTP);

        switchActivityIB(i);
    }

    private void switchActivityIB(Intent mIntent) {
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    public boolean inputValidation() {
        if (tokenValue.getText().toString().length() == 0) {
            tokenValue.requestFocus();
            tokenValue.setError(getString(R.string.cashoutmember_validation_otp));
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        handlerWS.removeCallbacks(runnableWS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_information:
                showTutorial();
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOkButton() {
        getActivity().finish();
    }

    public String getProduct_h2h() {
        return product_h2h;
    }

    public void setProduct_h2h(String product_h2h) {
        this.product_h2h = product_h2h;
    }
}
