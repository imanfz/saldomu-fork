package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.adapter.InvoiceDGIAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DividerItemDecoration;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DetailInvoiceTagihDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class FragInvoiceDGIConfirm extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {
    View view;
    RecyclerView listInvoice;
    Button btn_resend, btn_confirm;
    SecurePreferences sp;
    EditText et_otp;
    InvoiceDGIAdapter invoiceDGIAdapter;
    String tx_id, ccy_id;
    int attempt = 0;
    DetailInvoiceTagihDialog detailInvoiceTagihDialog;
    String notes, cust_id, tx_favorite_type, product_type;
    TextView tv_total, tv_desc;
    String paymentType, remark, phone, total, product_code, paymentTypeCode, anchorId, commCode;
    Bundle bundle;
    Boolean click = false;
    Boolean isFav = false;

    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_confirm_invoicedgi, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        bundle = getArguments();
        if (bundle != null) {
            paymentType = bundle.getString(DefineValue.PAYMENT_TYPE_DESC, "");
            remark = bundle.getString(DefineValue.REMARK, "");
            total = bundle.getString(DefineValue.TOTAL_AMOUNT, "");
            ccy_id = bundle.getString(DefineValue.CCY_ID, "");
            product_code = bundle.getString(DefineValue.PRODUCT_CODE, "");
            phone = bundle.getString(DefineValue.MOBILE_PHONE, "");
            attempt = bundle.getInt(DefineValue.ATTEMPT, -1);
            paymentTypeCode = bundle.getString(DefineValue.PAYMENT_TYPE);
            if (bundle.getBoolean(DefineValue.IS_FAVORITE) == true) {
                isFav = true;
                notes = bundle.getString(DefineValue.NOTES, "");
                cust_id = bundle.getString(DefineValue.CUST_ID, "");
                tx_favorite_type = bundle.getString(DefineValue.TX_FAVORITE_TYPE, "");
                product_type = bundle.getString(DefineValue.PRODUCT_TYPE, "");
                commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
                anchorId = bundle.getString(DefineValue.ANCHOR_ID, "");
            }
        }

        listInvoice = view.findViewById(R.id.listMenu);
        et_otp = view.findViewById(R.id.txtOtp);
        tv_desc = view.findViewById(R.id.tv_desc);
        btn_resend = view.findViewById(R.id.btnResend);
        btn_confirm = view.findViewById(R.id.btnDone);
        tv_total = view.findViewById(R.id.lbl_total_pay_amount);


        tv_desc.setOnClickListener(tvDetailListener);
        btn_resend.setOnClickListener(btnResendListener);
        btn_confirm.setOnClickListener(btnConfirmListener);

        tv_total.setText(ccy_id + ". " + CurrencyFormat.format(total));

        initializeRecyclerview();

        resendToken();
    }

    @Override
    public void onResume() {
        super.onResume();

//        getFailedPin();
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

    public void initializeRecyclerview() {
        invoiceDGIModelArrayList = new ArrayList<>();

        invoiceDGIModelArrayList.addAll(DataManager.getInstance().getListInvoice());

        invoiceDGIAdapter = new InvoiceDGIAdapter(invoiceDGIModelArrayList,
                model -> {

                });

        listInvoice.setAdapter(invoiceDGIAdapter);
        listInvoice.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listInvoice.addItemDecoration(new DividerItemDecoration(ResourcesCompat.getDrawable(getResources(), R.drawable.row_divider, null),
                false, false));
    }

    private TextView.OnClickListener tvDetailListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            detailInvoiceTagihDialog = DetailInvoiceTagihDialog.newDialog(
                    paymentType,
                    remark,
                    phone);
            detailInvoiceTagihDialog.show(getActivity().getSupportFragmentManager(), "detailInvoiceTagihDialog");
        }
    };

    private Button.OnClickListener btnResendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            click = true;
            et_otp.setText("");
            resendToken();
        }
    };

    private Button.OnClickListener btnConfirmListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inputValidation()) {
                btn_confirm.setEnabled(false);
                if (isFav)
                    onSaveToFavorite();
                else
                    confirmToken();
            }
        }
    };

    public Boolean inputValidation() {
        if (et_otp.getText().toString().length() == 0) {
            et_otp.requestFocus();
            et_otp.setError(getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    public void resendToken() {
        showProgressDialog();

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_INVOICE_DGI, DataManager.getInstance().getInvoiceParam(),
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();
                            jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response req token DGI : %s", response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (click) {
                                    Toast.makeText(getActivity(), "Token berhasil dikirim ulang!", Toast.LENGTH_LONG).show();
                                }
                                tx_id = response.getString(WebParams.TX_ID);
                            } else if (code.equals("0057")) {
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
                                getFragmentManager().popBackStack();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
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
                        dismissProgressDialog();
                    }
                });
    }

    public void confirmToken() {
        showProgressDialog();

        extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "");
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.TX_ID, tx_id);
        params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
        params.put(WebParams.USER_COMM_CODE, sp.getString(DefineValue.COMMUNITY_CODE, ""));
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params confirm payment DGI : " + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();
                            jsonModel model = gson.fromJson(response.toString(), jsonModel.class);

                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response confirm payment DGI : " + response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                sentInquiry();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity());
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

            extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "") + product_code;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params InquiryTrx DGI:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response InquiryTrx DGI: %s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    if (!paymentTypeCode.equalsIgnoreCase(DefineValue.CT_CODE)) {
                                        sentInsertTrxNew();
                                    } else {
                                        getFailedPin();
                                        CallPINinput(attempt);
                                    }
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
                                    Timber.d("Error resendTokenSGOL:%s", response.toString());
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                                btn_resend.setEnabled(true);
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
                            btn_confirm.setEnabled(true);
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

            String kode_otp = et_otp.getText().toString();
            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "") + product_code + tokenValue;
            params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            attempt = args.getInt(DefineValue.ATTEMPT, -1);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, tokenValue, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.KODE_OTP, kode_otp);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(link, params,
                    new ObjListeners() {

                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                dismissProgressDialog();
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response insertTrxTOpupSGOL:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    getTrxStatus(tx_id);
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
                                    Toast.makeText(getActivity(), code + " : " + message, Toast.LENGTH_LONG).show();
                                    if (message.equals("PIN tidak sesuai")) {
                                        Intent i = new Intent(getActivity(), InsertPIN.class);

                                        attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                        int failed = response.optInt(WebParams.MAX_FAILED, 0);

                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                                    } else {
                                        resendToken();
                                        et_otp.setText("");
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
                            btn_confirm.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }

    }

    public void sentInsertTrxNew() {
        try {
            showProgressDialog();
            final Bundle args = getArguments();

            String kode_otp = et_otp.getText().toString();

            extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "") + product_code + "";

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP_NEW, extraSignature);

            attempt = args.getInt(DefineValue.ATTEMPT, -1);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, "");
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.KODE_OTP, kode_otp);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP_NEW, params,
                    new ObjListeners() {

                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                dismissProgressDialog();
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response insertTrxTOpupSGOL:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    getTrxStatus(tx_id);
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
                                    } else {
                                        resendToken();
                                        et_otp.setText("");
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
                            btn_confirm.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }

    }

    private void getTrxStatus(final String txId) {
        try {
            extraSignature = txId + MyApiClient.COMM_ID_TAGIH;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID_TAGIH);
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
                                } else {
                                    showDialog(message);
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
                            btn_confirm.setEnabled(true);
                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void onSaveToFavorite() {
        extraSignature = cust_id + product_type + tx_favorite_type;
        Timber.e("extraSignature params %s", extraSignature);
        String url = MyApiClient.LINK_TRX_FAVORITE_SAVE;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(url, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.PRODUCT_TYPE, product_type);
        params.put(WebParams.CUSTOMER_ID, cust_id);
        params.put(WebParams.TX_FAVORITE_TYPE, tx_favorite_type);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID_TAGIH);
        params.put(WebParams.NOTES, notes);
        params.put(WebParams.DENOM_ITEM_ID, "");
        params.put(WebParams.GROUP_CODE, commCode);
        params.put(WebParams.COMPANY_CODE, anchorId);

        Timber.d("params fav DGI :%s", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(response.toString(), jsonModel.class);
                            Timber.e(response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                confirmToken();
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
                        Log.e("onResponse fav DGI", throwable.getLocalizedMessage());
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        confirmToken();
                    }
                });
    }

    private void showReportBillerDialog(JSONObject response) {
        Bundle args = new Bundle();
        String txStatus = response.optString(WebParams.TX_STATUS);
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.optString(WebParams.MEMBER_CUST_NAME));
        args.putString(DefineValue.DATE_TIME, response.optString(WebParams.CREATED));
        args.putString(DefineValue.TX_ID, response.optString(WebParams.TX_ID));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.DGI);

        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT)));

        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.ADMIN_FEE)));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT)));
        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, true);

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

        args.putString(DefineValue.TRX_STATUS_REMARK, response.optString(WebParams.TX_STATUS_REMARK));
        if (!txStat)
            args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK));


        args.putString(DefineValue.DETAILS_BILLER, response.optString(WebParams.DETAIL, ""));


        args.putString(DefineValue.INVOICE, response.optString(WebParams.INVOICE));
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE));
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME));
        args.putString(DefineValue.PRODUCT_NAME, response.optString(WebParams.PRODUCT_NAME));
        args.putString(DefineValue.PAYMENT_TYPE_DESC, response.optString(WebParams.PAYMENT_TYPE_DESC));
        args.putString(DefineValue.DGI_MEMBER_NAME, response.optString(WebParams.DGI_MEMBER_NAME));
        args.putString(DefineValue.DGI_ANCHOR_NAME, response.optString(WebParams.DGI_ANCHOR_NAME));
        args.putString(DefineValue.DGI_COMM_NAME, response.optString(WebParams.DGI_COMM_NAME));

        dialog.setArguments(args);
//        dialog.show(getFragmentManager(), "report biller dialog");
//        dialog.setTargetFragment(this, 0);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }

    private void setResultActivity(int result) {
        if (getActivity() == null)
            return;

        TagihActivity fca = (TagihActivity) getActivity();
        fca.setResultActivity(result);
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

    @Override
    public void onOkButton() {
        getActivity().finish();
    }
}
