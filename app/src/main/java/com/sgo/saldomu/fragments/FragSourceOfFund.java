package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SourceOfFundActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.InqSOFModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;


public class FragSourceOfFund extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {
    private SecurePreferences sp;
    private String memberId, txId, comm_name, payment_remark, amount, admin_fee, total_amount, orderNo,
            merchantCode, commCode, productCode, commId, isInAPP;
    private TextView tv_commName, tv_txId, tv_paymentRemark, tv_amount, tv_fee, tv_total;
    private Button btnProses, btnCancel;
    private int attempt = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_source_of_fund, container, false);
        tv_commName = v.findViewById(R.id.merchant_name_value);
        tv_txId = v.findViewById(R.id.tx_id_value);
        tv_paymentRemark = v.findViewById(R.id.payment_remark_value);
        tv_amount = v.findViewById(R.id.amount_value);
        tv_fee = v.findViewById(R.id.fee_value);
        tv_total = v.findViewById(R.id.total_value);
        btnProses = v.findViewById(R.id.btnProsesSOF);
        btnCancel = v.findViewById(R.id.btnCancelSOF);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        memberId = sp.getString(DefineValue.MEMBER_ID, "");

        Bundle bundle = getArguments();
        if (bundle != null) {
            txId = bundle.getString(DefineValue.TX_ID, "");
            isInAPP = bundle.getString(DefineValue.IS_INAPP, DefineValue.STRING_NO);

            if (isInAPP.equals(DefineValue.STRING_YES)) {
                Handler handler = new Handler();
                Runnable runnable = () -> sentInquirySOF();
                handler.postDelayed(runnable, 2000);
            } else
                sentInquirySOF();

        }

        btnCancel.setOnClickListener(view12 -> cancelPayment());

        btnProses.setOnClickListener(view1 -> paySOF());
    }

    public void initializeData() {

        tv_commName.setText(comm_name);
        tv_txId.setText(txId);
        tv_paymentRemark.setText(payment_remark);
        tv_amount.setText("IDR" + ". " + CurrencyFormat.format(amount));
        tv_fee.setText("IDR" + ". " + CurrencyFormat.format(admin_fee));
        tv_total.setText("IDR" + ". " + CurrencyFormat.format(total_amount));

    }

    public void sentInquirySOF() {
        try {
            showProgressDialog();

            extraSignature = txId;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_SOF, extraSignature);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params sent inquiry SOF:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_SOF, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            dismissProgressDialog();

                            InqSOFModel model = getGson().fromJson(object, InqSOFModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                comm_name = model.getComm_name();
                                txId = model.getTx_id();
                                payment_remark = model.getPayment_remark();
                                amount = model.getAmount();
                                admin_fee = model.getAdmin_fee();
                                total_amount = model.getTotal_amount();
                                orderNo = model.getOrder_no();
                                commCode = model.getComm_code();
                                merchantCode = model.getMerchant_code();
                                productCode = model.getProduct_code();
                                commId = model.getComm_id();

                                initializeData();

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", object.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Timber.tag("eror").d("eror inquiry SOF");
                        }

                        @Override
                        public void onComplete() {
                            dismissProgressDialog();
                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient inquiry SOF:%s", e.getMessage());
        }
    }

    public void cancelPayment() {
        try {
            showProgressDialog();

            extraSignature = orderNo;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_PAYMENT_SOF, extraSignature);
            params.put(WebParams.ORDER_ID, orderNo);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params cancel payment SOF:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CANCEL_PAYMENT_SOF, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            InqSOFModel model = getGson().fromJson(object, InqSOFModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                showDialog();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", object.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            dismissProgressDialog();
                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient cancel payment SOF:%s", e.getMessage());
        }
    }

    public void paySOF() {
        try {
            showProgressDialog();

            extraSignature = txId + commCode + merchantCode;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAY_SOF, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MERCHANT_CODE, merchantCode);
            Timber.d("isi params pay Inquiry SOF:%s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_PAY_SOF, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {

                                jsonModel model = getGson().fromJson(response.toString(), InqSOFModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response pay Inquiry SOF: %s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    sentInquiry();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
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

                        }

                        @Override
                        public void onComplete() {
                            dismissProgressDialog();
                            btnProses.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient pay Inquiry SOF:%s", e.getMessage());
        }
    }

    public void sentInquiry() {
        try {
            showProgressDialog();

            extraSignature = txId + commCode + productCode;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params InquiryTrx SOF:%s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response InquiryTrx SOF: %s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    dismissProgressDialog();
                                    CallPINinput(attempt);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    Timber.d("Error resendTokenSOF:%s", response.toString());
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                            btnProses.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void CallPINinput(int _attempt) {
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    @Override
    public void onResume() {
        super.onResume();

        getFailedPin();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainPage.REQUEST_FINISH) {
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);

                //call insert trx
                sentInsertTrx(value_pin);
            }
        }
    }

    public void sentInsertTrx(String tokenValue) {
        try {
            showProgressDialog();
            final Bundle args = getArguments();

            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = txId + commCode + productCode + tokenValue;
            params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            attempt = args.getInt(DefineValue.ATTEMPT, -1);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberId);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, tokenValue, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:%s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(link, params,
                    new ObjListeners() {

                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                dismissProgressDialog();
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response insertTrxTOpupSGOL:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    getTrxStatus(txId);
                                    setResultActivity(MainPage.RESULT_BALANCE);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    Toast.makeText(getActivity(), code + ":" + message, Toast.LENGTH_LONG).show();
                                    if (message.equals("PIN tidak sesuai")) {
                                        Intent i = new Intent(getActivity(), InsertPIN.class);

                                        attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                        int failed = response.optInt(WebParams.MAX_FAILED, 0);

                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                                    }
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
                            btnProses.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }

    }

    private void getTrxStatus(final String txId) {
        try {
            extraSignature = txId + commId;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, commId);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params sent get Trx Status:%s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                dismissProgressDialog();
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                Timber.d("isi response sent get Trx Status:%s", response.toString());
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {
                                    showReportBillerDialog(response);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
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
                            btnProses.setEnabled(true);
                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void showReportBillerDialog(JSONObject response) {
        Bundle args = new Bundle();
        String txStatus = response.optString(WebParams.TX_STATUS);

        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.TX_ID, response.optString(WebParams.TX_ID));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.SG3);
        args.putString(DefineValue.DATE_TIME, response.optString(WebParams.CREATED));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT)));
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.ADMIN_FEE)));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT)));

        boolean txStat = false;
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
        args.putString(DefineValue.TRX_STATUS_REMARK, response.optString(WebParams.TX_STATUS_REMARK));
        if (!txStat)
            args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK));


        args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE));
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME));
        args.putString(DefineValue.COMMUNITY_NAME, response.optString(WebParams.COMM_NAME));
        args.putString(DefineValue.REMARK, response.optString(WebParams.PAYMENT_REMARK));

        dialog.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }

    private void showDialog() {
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
        Title.setText(getString(R.string.success));
        Message.setText(getString(R.string.success_cancel_sof));

        btnDialogOTP.setOnClickListener(view -> {
            getActivity().finish();
            //SgoPlusWeb.this.finish();
        });

        dialog.show();
    }

    void getFailedPin() {
        new UtilsLoader(getActivity(), sp).getFailedPIN(userPhoneID, new OnLoadDataListener() {
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void setResultActivity(int result) {
        if (getActivity() == null)
            return;

        SourceOfFundActivity fca = (SourceOfFundActivity) getActivity();
        fca.setResultActivity(result);
    }

    @Override
    public void onOkButton() {
        getActivity().finish();
    }
}
