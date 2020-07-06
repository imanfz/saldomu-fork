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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.activities.TopUpSCADMActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
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
import com.sgo.saldomu.models.retrofit.FailedPinModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

public class FragTopUpConfirmSCADM extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    View v;
    SecurePreferences sp;
    Button btn_next;
    private ProgressDialog progdialog;
    int attempt, failed;
    Boolean isPIN = false;
    LinearLayout layout_otp;
    EditText et_otp;
    String comm_name, member_code, product_name, bank_gateway, comm_code, bank_code, product_code, amount, remark, storeName, storeAddress;
    String ccy_id, tx_id, member_id_scadm, member_name, comm_id, bank_name, admin_fee, total_amount, api_key, item_name;
    TextView tv_community_name, tv_community_code, tv_member_code, tv_product_name, tv_jumlah, tv_remark, tv_admin_fee, tv_total_amount,
    tv_store_name, tv_store_address;
    double dfee = 0;
    double damount = 0;
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

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID, "");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID, "");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        Bundle bundle = getArguments();
        tx_id = bundle.getString(DefineValue.TX_ID, "");
        member_id_scadm = bundle.getString(DefineValue.MEMBER_ID_SCADM, "");
        member_code = bundle.getString(DefineValue.MEMBER_CODE, "");
        member_name = bundle.getString(DefineValue.MEMBER_NAME, "");
        comm_id = bundle.getString(DefineValue.COMM_ID_SCADM, "");
        comm_code = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        comm_name = bundle.getString(DefineValue.COMMUNITY_NAME, "");
        bank_gateway = bundle.getString(DefineValue.BANK_GATEWAY, "");
        bank_code = bundle.getString(DefineValue.BANK_CODE, "");
        bank_name = bundle.getString(DefineValue.BANK_NAME, "");
        product_code = bundle.getString(DefineValue.PRODUCT_CODE, "");
        product_name = bundle.getString(DefineValue.PRODUCT_NAME, "");
        ccy_id = bundle.getString(DefineValue.CCY_ID, "");
        amount = bundle.getString(DefineValue.AMOUNT, "");
        admin_fee = bundle.getString(DefineValue.FEE, "");
        total_amount = bundle.getString(DefineValue.TOTAL_AMOUNT, "");
        remark = bundle.getString(DefineValue.REMARK, "");
        api_key = bundle.getString(DefineValue.API_KEY, "");
        attempt = bundle.getInt(DefineValue.ATTEMPT, -1);
        storeName = bundle.getString(WebParams.STORE_NAME, "");
        storeAddress = bundle.getString(WebParams.STORE_ADDRESS, "");


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
        tv_store_name = v.findViewById(R.id.tv_store_name_topupconfirm);
        tv_store_address = v.findViewById(R.id.tv_store_address_topupconfirm);

        tv_store_name.setText(storeName);
        tv_store_address.setText(storeAddress);
        tv_community_code.setText(comm_code);
        tv_community_name.setText(comm_name);
        tv_member_code.setText(member_code);
        tv_product_name.setText(product_name);
        tv_remark.setText(remark);
        damount = Double.parseDouble(bundle.getString(DefineValue.AMOUNT, ""));
        dfee = Double.parseDouble(bundle.getString(DefineValue.FEE, ""));
        dtotal_amount = Double.parseDouble(bundle.getString(DefineValue.TOTAL_AMOUNT, ""));
        tv_jumlah.setText(ccy_id + ". " + CurrencyFormat.format(damount));
        tv_admin_fee.setText(ccy_id + ". " + CurrencyFormat.format(dfee));
        tv_total_amount.setText(ccy_id + ". " + CurrencyFormat.format(dtotal_amount));

        if (product_name.equalsIgnoreCase("MANDIRI SMS")) {
            layout_otp.setVisibility(View.VISIBLE);
        }

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

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bank_gateway.equalsIgnoreCase("N")) {
                    changeToSGOPlus(tx_id, product_code, product_name, bank_code,
                            String.valueOf(damount), String.valueOf(dfee), String.valueOf(dtotal_amount), bank_name);
                } else if (bank_gateway.equalsIgnoreCase("Y")) {
                    if (product_code.equalsIgnoreCase("SCASH")) {
                        confirmToken();
                        btn_next.setEnabled(true);
                    } else {
                        if (inputValidation()) {
                            sentInsertTransTopup(et_otp.getText().toString(), amount);
//                            confirmToken();
                        } else btn_next.setEnabled(true);
                    }
                }
            }
        });
    }

    private boolean inputValidation() {
        if (et_otp.getText().toString().length() == 0) {
            et_otp.requestFocus();
            et_otp.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    private void CallPINinput(int _attempt) {
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    public void confirmToken() {
        showProgressDialog();

//        extraSignature = tx_id + comm_code;
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_PAYMENT_DGI);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.TX_ID, tx_id);
        params.put(WebParams.COMM_CODE, comm_code);
        params.put(WebParams.USER_COMM_CODE, sp.getString(DefineValue.COMMUNITY_CODE, ""));
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params confirm payment topup scadm : " + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();
                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response confirm payment topup scadm : " + response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                sentInquiry();
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
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
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

                    }
                });

    }

    public void sentInquiry() {
        try {
            showProgressDialog();

            extraSignature = tx_id + comm_code + product_code;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, commIDLogin);
            Timber.d("isi params InquiryTrx topup scadm:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String error_message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response InquiryTrx topup scadm: " + response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    CallPINinput(attempt);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
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
                                    Timber.d("Error resendTokenSGOL:" + response.toString());
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    showDialog(code);
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
                            dismissProgressDialog();
//                            btn_confirm.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void sentInsertTransTopup(String tokenValue, final String _amount) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = tx_id + comm_code + product_code + tokenValue;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.COMM_ID, comm_name);
            params.put(WebParams.MEMBER_ID, member_id_scadm);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncryptCommID(comm_name, uuid, dateTime, userPhoneID, tokenValue, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            FailedPinModel model = getGson().fromJson(object, FailedPinModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                getTrxStatus(tx_id, comm_id, _amount);
                                setResultActivity(MainPage.RESULT_BALANCE);

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
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {

                                code = model.getError_code() + ":" + model.getError_message();
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
                                    getActivity().finish();
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
                            btn_next.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                String _amount;
                _amount = amount;
                //    Log.d("onActivity result", "Biller Fragment result pin value");
                sentInsertTransTopup(value_pin, _amount);
            }else
                backToTopUpSACDM();
        }
    }

    private void getTrxStatus(final String txId, String comm_id, final String _amount) {
        try {

            extraSignature = txId + comm_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params sent get Trx Status:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                showReportBillerDialog(model, sp.getString(DefineValue.USER_NAME, ""),
                                        sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                        model.getTx_status(), _amount);
//                                        , response.optString(WebParams.BILLER_DETAIL),
//                                        response.optString(WebParams.BUSS_SCHEME_CODE), response.optString(WebParams.BUSS_SCHEME_NAME), response.optString(WebParams.PRODUCT_NAME),
//                                        response.optString(WebParams.COMM_CODE), response.optString(WebParams.MEMBER_CODE));
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {
                                showDialog(message);
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            btn_next.setEnabled(true);

                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showReportBillerDialog(GetTrxStatusReportModel model, String name, String userId, String txId,
                                        String itemName, String txStatus, String _amount) {
//            , String biller_detail,
//                                        String buss_scheme_code, String buss_scheme_name, String product_name, String comm_code, String member_code) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.getCreated()));
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(admin_fee));

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
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.getTx_remark());


        double totalAmount = Double.parseDouble(amount) + Double.parseDouble(admin_fee);
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));
        args.putString(DefineValue.BILLER_DETAIL, toJson(model.getBiller_detail()).toString()
//                model.getBiller_detail().getPhoneNumber()
        );
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.getBuss_scheme_name());
        args.putString(DefineValue.BANK_PRODUCT, product_name);
        args.putString(DefineValue.COMMUNITY_CODE, comm_code);
        args.putString(DefineValue.MEMBER_CODE, member_code);
        args.putString(DefineValue.BANK_PRODUCT, product_name);

        dialog.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }

    private void setResultActivity(int result) {
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
        i.putExtra(DefineValue.PRODUCT_NAME, product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.REMARK, remark);
        i.putExtra(DefineValue.COMMUNITY_CODE, comm_code);
        i.putExtra(DefineValue.MEMBER_CODE, member_code);
        i.putExtra(DefineValue.MEMBER_ID_SCADM, member_id_scadm);
        i.putExtra(DefineValue.MEMBER_NAME, member_name);
        i.putExtra(DefineValue.TX_ID, tx_id);
        i.putExtra(DefineValue.AMOUNT, amount);
        i.putExtra(DefineValue.TOTAL_AMOUNT, total_amount);
        i.putExtra(DefineValue.COMMUNITY_ID, comm_id);
        i.putExtra(DefineValue.API_KEY, api_key);
        i.putExtra(DefineValue.CALLBACK_URL, (DefineValue.CALLBACK_URL));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        Timber.d("isi args:" + i.toString());
        btn_next.setEnabled(true);
        switchActivityIB(i);
    }

    private void switchActivityIB(Intent mIntent) {
        if (getActivity() == null)
            return;

        TopUpSCADMActivity fca = (TopUpSCADMActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
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
                getActivity().onBackPressed();
            }
        });

        dialog.show();
    }

    @Override
    public void onOkButton() {
        backToTopUpSACDM();
    }

    void backToTopUpSACDM() {
        getFragManager().popBackStack(TopUpSCADMActivity.TOPUP, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
