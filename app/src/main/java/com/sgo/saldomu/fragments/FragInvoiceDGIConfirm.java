package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.adapter.InvoiceDGIAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DividerItemDecoration;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.ConfirmDGIDialog;
import com.sgo.saldomu.dialogs.ConfirmationDialog;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

public class FragInvoiceDGIConfirm extends BaseFragment {
    View view;
    RecyclerView listInvoice;
    Button btn_detail, btn_cancel, btn_resend, btn_confirm;
    SecurePreferences sp;
    EditText et_otp;
    InvoiceDGIAdapter invoiceDGIAdapter;
    ProgressDialog progdialog;
    String tx_id;
    ConfirmDGIDialog confirmDGIDialog;

    String paymentType, remark, phone;

    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList = new ArrayList<InvoiceDGI>();

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
        listInvoice = view.findViewById(R.id.listMenu);
        et_otp = view.findViewById(R.id.txtOtp);
        btn_detail = view.findViewById(R.id.btn_desc);
        btn_cancel = view.findViewById(R.id.btnCancel);
        btn_resend = view.findViewById(R.id.btnDone);


        btn_detail.setOnClickListener(btnDetailListener);
        btn_cancel.setOnClickListener(btnCancelListener);
        btn_resend.setOnClickListener(btnResendListener);
        btn_confirm.setOnClickListener(btnConfirmListener);

        initializeRecyclerview();
    }

    public void initializeRecyclerview() {
        invoiceDGIAdapter = new InvoiceDGIAdapter(invoiceDGIModelArrayList, getActivity(),
                new InvoiceDGIAdapter.OnTap() {
                    @Override
                    public void onTap(int pos) {
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
            confirmDGIDialog = ConfirmDGIDialog.newDialog(
                    paymentType,
                    remark,
                    phone);
        }
    };

    private Button.OnClickListener btnCancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
            Fragment newFrag = new FragListInvoiceTagih();
            if (getActivity() == null) {
                return;
            }
            TagihActivity ftf = (TagihActivity) getActivity();
            ftf.switchContent(newFrag, "List Invoice", true);
        }
    };

    private Button.OnClickListener btnResendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
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

    }

    public void confirmToken() {
        if (progdialog == null)
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        else
            progdialog.show();
        extraSignature = tx_id + sp.getString(DefineValue.COMM_CODE_DGI, "");
        RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CONFIRM_PAYMENT_DGI,
                userPhoneID, accessKey, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.TX_ID, tx_id);
        params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
        params.put(WebParams.USER_COMM_CODE, sp.getString(DefineValue.COMMUNITY_CODE,""));
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params confirm payment DGI : " + params.toString());


        MyApiClient.confirmPaymentDGI(getActivity(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    if (progdialog.isShowing())
                        progdialog.dismiss();

                    String code = response.getString(WebParams.ERROR_CODE);
                    String error_message = response.getString(WebParams.ERROR_MESSAGE);
                    Timber.d("response confirm payment DGI : " + response.toString());
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        sentInquiry();
                    } else {
                        Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error confirm payment DGI : " + throwable.toString());

            }

        });
    }

    public void sentInquiry(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = tx_id+getArguments().getString(DefineValue.BILLER_COMM_CODE)+"SCASH";

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                        userPhoneID,accessKey, extraSignature);

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.PRODUCT_CODE, "SCASH");
            params.put(WebParams.COMM_CODE, sp.getString(DefineValue.COMM_CODE_DGI, ""));
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params InquiryTrx DGI:"+params.toString());

            JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String error_message = response.getString(WebParams.ERROR_MESSAGE);
                        Timber.d("isi response InquiryTrx DGI: " +error_message);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            sentInsertTrx();
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
                        btn_confirm.setEnabled(true);
                        btn_resend.setEnabled(true);
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
                    btn_confirm.setEnabled(true);
                    Timber.w("Error Koneksi resend token biller confirm:"+throwable.toString());
                }
            };
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sentInsertTrx()
    {

    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
