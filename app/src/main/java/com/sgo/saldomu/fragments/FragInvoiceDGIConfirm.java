package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sgo.saldomu.dialogs.ConfirmDGIDialog;
import com.sgo.saldomu.dialogs.DetailInvoiceTagihDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

public class FragInvoiceDGIConfirm extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback{
    View view;
    RecyclerView listInvoice;
    Button btn_detail, btn_cancel, btn_resend, btn_confirm;
    SecurePreferences sp;
    EditText et_otp;
    InvoiceDGIAdapter invoiceDGIAdapter;
    String tx_id, ccy_id;
    ConfirmDGIDialog confirmDGIDialog;
    int attempt = 0, failed = 0;
    DetailInvoiceTagihDialog detailInvoiceTagihDialog;

    TextView tv_total;

    String paymentType, remark, phone, total, product_code;
    Bundle bundle;
    Boolean click = false;

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
        }

        listInvoice = view.findViewById(R.id.listMenu);
        et_otp = view.findViewById(R.id.txtOtp);
        btn_detail = view.findViewById(R.id.btn_desc);
        btn_cancel = view.findViewById(R.id.btnCancel);
        btn_resend = view.findViewById(R.id.btnResend);
        btn_confirm = view.findViewById(R.id.btnDone);
        tv_total = view.findViewById(R.id.lbl_total_pay_amount);


        btn_detail.setOnClickListener(btnDetailListener);
        btn_cancel.setOnClickListener(btnCancelListener);
        btn_resend.setOnClickListener(btnResendListener);
        btn_confirm.setOnClickListener(btnConfirmListener);

        tv_total.setText(ccy_id + ". " + CurrencyFormat.format(total));

        initializeRecyclerview();

        resendToken();
    }

    @Override
    public void onResume() {
        super.onResume();

        getFailedPin();
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

        invoiceDGIAdapter = new InvoiceDGIAdapter(invoiceDGIModelArrayList, getActivity(),
                new InvoiceDGIAdapter.OnTap() {
                    @Override
                    public void onTap(InvoiceDGI model) {

                    }
                });

        listInvoice.setAdapter(invoiceDGIAdapter);
        listInvoice.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listInvoice.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.row_divider),
                false, false));
    }

    private Button.OnClickListener btnDetailListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            detailInvoiceTagihDialog = DetailInvoiceTagihDialog.newDialog(
                    paymentType,
                    remark,
                    phone);
            detailInvoiceTagihDialog.show(getActivity().getSupportFragmentManager(), "detailInvoiceTagihDialog");
        }
    };

    private Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
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
            if (inputValidation())
                confirmToken();
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
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response list invoice DGI : " + response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (click) {
                                    Toast.makeText(getActivity(), "Token berhasil dikirim ulang!", Toast.LENGTH_LONG).show();
                                }
                                tx_id = response.getString(WebParams.TX_ID);
                            }
                            else if(code.equals("0057")) {
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
                                getFragmentManager().popBackStack();
                            }
                            else{
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

                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response confirm payment DGI : " + response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                sentInquiry();
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
                                String code = response.getString(WebParams.ERROR_CODE);
                                String error_message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response InquiryTrx DGI: " + response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    CallPINinput(attempt);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    Timber.d("Error resendTokenSGOL:" + response.toString());
                                    code = response.getString(WebParams.ERROR_MESSAGE);

                                    Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
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
            Timber.d("httpclient:" + e.getMessage());
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

            extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "") + product_code + tokenValue;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP, extraSignature);

            attempt = args.getInt(DefineValue.ATTEMPT, -1);
            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(tokenValue));
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.KODE_OTP, kode_otp);

            Timber.d("isi params insertTrxTOpupSGOL:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP, params,
                    new ObjListeners() {

                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                dismissProgressDialog();
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("isi response insertTrxTOpupSGOL:" + response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    getTrxStatus(tx_id, MyApiClient.COMM_ID);
                                    setResultActivity(MainPage.RESULT_BALANCE);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                    String message = response.getString(WebParams.ERROR_MESSAGE);

                                    if (message.equals("PIN tidak sesuai")) {
                                        Intent i = new Intent(getActivity(), InsertPIN.class);

                                        attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                        int failed = response.optInt(WebParams.MAX_FAILED, 0);

                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                                    } else{
                                        resendToken();
                                        et_otp.setText("");
                                    }
//                                getFragmentManager().popBackStack();
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

    private void getTrxStatus(final String txId, String comm_id) {
        try {
            extraSignature = txId + MyApiClient.COMM_ID_TAGIH;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID_TAGIH);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params sent get Trx Status:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                dismissProgressDialog();
                                Timber.d("isi response sent get Trx Status:" + response.toString());
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {
                                    showReportBillerDialog(response);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    String msg = response.getString(WebParams.ERROR_MESSAGE);
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

                            dismissProgressDialog();
                            btn_confirm.setEnabled(true);
                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
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
