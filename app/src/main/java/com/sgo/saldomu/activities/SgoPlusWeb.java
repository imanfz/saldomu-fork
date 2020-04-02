package com.sgo.saldomu.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
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
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class SgoPlusWeb extends BaseActivity implements ReportBillerDialog.OnDialogOkCallback {

    private WebView webview;
    private String app_name = "SGO Plus Payment Gateaway";
    private String SGO_PLUS_URL = "";
    private String masterDomainSGOplus;
    private String userID;
    private String accessKey;
    private String devDomainSGOPlus = MyApiClient.domainSgoPlusDev;
    private String prodDomainSGOPlus = MyApiClient.domainSgoPlusProd;
    private String bankName;
    private String bankProduct;
    private String bankCode;
    Boolean isBCA;
    private Boolean isDisconnected;
    private Intent mIntent;
    private ProgressDialog out;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        isDisconnected = !isOnline();

        mIntent = getIntent();
        String productCode = mIntent.getStringExtra(DefineValue.PRODUCT_CODE);
        bankProduct = mIntent.getStringExtra(DefineValue.PRODUCT_NAME);
        bankCode = mIntent.getStringExtra(DefineValue.BANK_CODE);
        bankName = mIntent.getStringExtra(DefineValue.BANK_NAME);
        String commCode = mIntent.getStringExtra(DefineValue.COMMUNITY_CODE);
        String fee = mIntent.getStringExtra(DefineValue.FEE);
        String paymentId = mIntent.getStringExtra(DefineValue.TX_ID);
        String apikey = mIntent.getStringExtra(DefineValue.API_KEY);
        String amount = mIntent.getStringExtra(DefineValue.AMOUNT);
        String comm_id = mIntent.getStringExtra(DefineValue.COMMUNITY_ID);
        String reportType = mIntent.getStringExtra(DefineValue.REPORT_TYPE);
        String transType = mIntent.getStringExtra(DefineValue.TRANSACTION_TYPE);
        String shareType = mIntent.getStringExtra(DefineValue.SHARE_TYPE);
        String totalAmount = mIntent.getStringExtra(DefineValue.TOTAL_AMOUNT);
        Timber.d("isi intent:" + mIntent.getExtras().toString());

        //if(MyApiClient.PROD_FAILURE_FLAG && topUpType.equals(CoreApp.PULSA))masterDomainSGOplus = prodDomainSGOPlus;
        if (MyApiClient.IS_PROD) {
            if (bankCode.equals("008")) masterDomainSGOplus = prodDomainSGOPlus;
            else masterDomainSGOplus = prodDomainSGOPlus;
        } else masterDomainSGOplus = devDomainSGOPlus;


        SGO_PLUS_URL = masterDomainSGOplus + "index/order/?key=" + apikey +
                "&paymentId=" + paymentId +
                "&commCode=" + commCode +
                "&bankCode=" + bankCode +
                "&productCode=" + productCode + "&mobile=1";


        if (!bankCode.equals("008")) {
            try {
                String callbackUrl = mIntent.getStringExtra(DefineValue.CALLBACK_URL);
                if (!callbackUrl.isEmpty())
                    SGO_PLUS_URL = SGO_PLUS_URL + "&url=" + URLEncoder.encode(callbackUrl + "?refid=" + gen_numb() + "&ref_back_url=" + productCode + "&isclose=1", "utf-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        //Toast.makeText(this, SGO_PLUS_URL, Toast.LENGTH_LONG).show();
        Timber.d("sgo+ urlnya:" + SGO_PLUS_URL);
        //showReportBillerDialog(MyApiClient.getCurrentDateTime(), paymentId, sp.getString(CoreApp.USERID_PHONE, "")
        //        , bankName,productCode,fee,amount);
        loadUrl(sp.getString(DefineValue.USER_NAME, ""), SGO_PLUS_URL, paymentId, userID, totalAmount,
                fee, amount, reportType, comm_id, transType, commCode, shareType);
        setResult(MainPage.RESULT_NORMAL);
    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private int gen_numb() {
        Random r = new Random(System.currentTimeMillis());
        return ((1 + r.nextInt(9)) * 100000000 + r.nextInt(100000000));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_sgo_plus_web;
    }

    @SuppressWarnings("deprecation")
    private void loadUrl(final String userName, String url, final String payment_id, final String userId, final String totalAmount,
                         final String fee, final String amount, final String reportType, final String commId,
                         final String transType, final String commCode, final String shareType) {
        webview = findViewById(R.id.webview);
        assert webview != null;
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= 21 && BuildConfig.DEBUG) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (android.os.Build.VERSION.SDK_INT <= 11) {
            webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        }

        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //setSupportProgress(progress * 100);
                getProgressSpinner().setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                //activity.setProgress(progress * 100);
                if (progress == 100) {
                    getProgressSpinner().setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                }
            }
        });

        webview.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.d("isi url tombol-tombolnya:" + url);
                if (URLUtil.isValidUrl(url)) {
                    if (url.contains("isclose=1")) {
                        setResult(MainPage.RESULT_BALANCE);
                        if (reportType.equalsIgnoreCase(DefineValue.BBS_CASHIN) || reportType.equalsIgnoreCase(DefineValue.BBS_MEMBER_OTP)
                                || reportType.equalsIgnoreCase(DefineValue.BBS_CASHOUT)) {
                            getTrxStatusBBS(userName, DateTimeFormat.getCurrentDateTime(), payment_id, userId, totalAmount,
                                    fee, amount, reportType, commId, transType, shareType, commCode);
                        } else {
                            getTrxStatus(userName, DateTimeFormat.getCurrentDateTime(), payment_id, userId, totalAmount,
                                    fee, amount, reportType, commId, transType, shareType);
                        }
                        Timber.wtf("masuk is close");
                    } else if (url.contains("isback=1")) {
                        setResult(MainPage.RESULT_BALANCE);
                        onOkButton();
                    } else
                        view.loadUrl(url);
                }

                return true;
            }


            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                isDisconnected = true;
                invalidateOptionsMenu();
                Timber.d("isi error code :" + String.valueOf(errorCode));
                String message = "";
                if (errorCode > ERROR_IO)
                    message = getString(R.string.webview_err_connect);
                else if (errorCode == ERROR_TIMEOUT)
                    message = getString(R.string.webview_err_timeout);
                else if (errorCode == ERROR_BAD_URL)
                    message = getString(R.string.webview_err_bad_url);
                else if (errorCode == ERROR_TOO_MANY_REQUESTS)
                    message = getString(R.string.webview_err_too_many_req);

                if (!message.isEmpty()) {
                    try {
                        String content = IOUtils.toString(getAssets().open("webnotavailable.html"))
                                .replaceAll("%ERR_DESC%", message);
                        view.loadDataWithBaseURL("file:///android_asset/webnotavailable.html", content, "text/html", "UTF-8", null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error) {
                if (MyApiClient.IS_PROD)
                    super.onReceivedSslError(view, handler, error);
                else
                    handler.proceed();
            }

            @Override
            public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                super.onReceivedClientCertRequest(view, request);
            }
        });


        webview.loadUrl(url);
    }

    public void getTrxStatusBBS(final String userName, final String date, final String txId, final String userId,
                                final String totalAmount, final String fee, final String amount, final String reportType,
                                final String comm_id, final String transtype, final String shareType, String commCode) {
        try {
            out = DefinedDialog.CreateProgressDialog(this, null);
            out.show();

            extraSignature = txId + commCode;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_STATUS_BBS, extraSignature);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userId);

            Timber.d("isi params sent get Trx Status bbs:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_STATUS_BBS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                Gson gson = new Gson();
                                GetTrxStatusReportModel model = gson.fromJson(object, GetTrxStatusReportModel.class);
                                String code = model.getError_code();

                                JSONObject response = new JSONObject(gson.toJson(model));

                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    String txstatus = response.getString(WebParams.TX_STATUS);

                                    showReportBillerDialog(userName, DateTimeFormat.formatToID(response.optString(WebParams.CREATED, "")),
                                            txId, userId, totalAmount, fee, amount,
                                            txstatus, response.getString(WebParams.TX_REMARK),
                                            reportType, response.getString(WebParams.BUSS_SCHEME_CODE), response.getString(WebParams.BUSS_SCHEME_NAME),
                                            response, response.optString(WebParams.COMM_CODE, ""), response.optString(WebParams.MEMBER_CODE, ""),
                                            response.optString(WebParams.ORDER_ID, ""));
                                } else if (code.equals("0288")) {
                                    Timber.d("isi error sent trx status bbs:" + response.toString());
                                    String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(SgoPlusWeb.this, code_msg, Toast.LENGTH_LONG).show();
                                    setResult(MainPage.RESULT_RETRY);
                                    finish();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(SgoPlusWeb.this, message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(SgoPlusWeb.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(SgoPlusWeb.this, model.getError_message());
                                }else {
                                    String msg = model.getError_message();
                                    if (code.equals("0003")) {
                                        showReportBillerDialog(userName, date, txId, userId, totalAmount, fee, amount,
                                                DefineValue.FAILED, getString(R.string.transaction_failed_tx_id), reportType,
                                                response.getString(WebParams.BUSS_SCHEME_CODE), response.getString(WebParams.BUSS_SCHEME_NAME), response,
                                                response.optString(WebParams.COMM_CODE, ""), response.optString(WebParams.MEMBER_CODE, ""),
                                                response.optString(WebParams.ORDER_ID, ""));
                                    } else
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
                            if (out.isShowing()) {
                                out.dismiss();
                            }
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void getTrxStatus(final String userName, final String date, final String txId, final String userId,
                              final String totalAmount, final String fee, final String amount, final String reportType,
                              final String comm_id, final String transtype, final String shareType) {
        try {
            out = DefinedDialog.CreateProgressDialog(this, null);
            out.show();


            extraSignature = txId + comm_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TYPE, transtype);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);

            if (reportType.equals(DefineValue.BILLER_PLN)) {
                params.put(WebParams.IS_DETAIL, DefineValue.STRING_YES);
            }

            Timber.d("isi params sent get Trx Status:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            GetTrxStatusReportModel model = gson.fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            Timber.d("isi respons sent get Trx Status:" + object.toString());

                            try {
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    String txstatus = model.getTx_status();

                                    showReportBillerDialog(userName, DateTimeFormat.formatToID(model.getCreated()), txId, userId, totalAmount, fee, amount,
                                            txstatus, model.getTx_remark(),
                                            reportType, model.getBuss_scheme_code(), model.getBuss_scheme_name(), new JSONObject(gson.toJson(model)),
                                            model.getComm_code(), model.getMember_code(), model.getOrder_id());

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(SgoPlusWeb.this, message);
                                }else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(SgoPlusWeb.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + object.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(SgoPlusWeb.this, model.getError_message());
                                } else {
                                    if (code.equals("0003")) {
                                        showReportBillerDialog(userName, date, txId, userId, totalAmount, fee, amount,
                                                DefineValue.FAILED, getString(R.string.transaction_failed_tx_id), reportType, model.getBuss_scheme_code(),
                                                model.getBuss_scheme_name(), new JSONObject(gson.toJson(model)), model.getComm_code(),
                                                model.getMember_code(), model.getOrder_id());
                                    } else
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
                            if (out.isShowing())
                                out.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showDialog(String msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
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
                SgoPlusWeb.this.finish();
            }
        });

        dialog.show();
    }


    private void showReportBillerDialog(String userName, String date, String txId, String userId, String total_amount,
                                        String fee, String amount, String txStatus, String txRemark, String reportType,
                                        String buss_scheme_code, String buss_scheme_name,
                                        JSONObject response, String comm_code, String member_code, String order_id) throws JSONException {

        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        Bundle args = dialog.getArguments();
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, reportType);
        args.putString(DefineValue.BUSS_SCHEME_CODE, buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME, buss_scheme_name);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.COMMUNITY_CODE, comm_code);
        args.putString(DefineValue.MEMBER_CODE, member_code);
        args.putString(DefineValue.ORDER_ID, order_id);
        args.putString(DefineValue.USERID_PHONE, userId);

        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

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
        if (!txStat) args.putString(DefineValue.TRX_REMARK, txRemark);

        if (response.optString(WebParams.BUSS_SCHEME_CODE).equalsIgnoreCase("BDK")) {
            args.putString(DefineValue.DENOM_DETAIL, response.optString(WebParams.DENOM_DETAIL));
        }

        if (reportType.equals(DefineValue.BILLER_PLN)) {
            if (getIntent().hasExtra(DefineValue.IS_PLN)) {
                Boolean isPLN = getIntent().getBooleanExtra(DefineValue.IS_PLN, false);
                if (isPLN && response.has(WebParams.DETAIL)) {
                    args.putString(DefineValue.BILLER_TYPE, getIntent().getStringExtra(DefineValue.BILLER_TYPE));
                    args.putString(DefineValue.DETAIL, response.optString(WebParams.DETAIL, ""));
                }
            }
            setResult(MainPage.RESULT_BILLER);

        } else if (reportType.equals(DefineValue.BILLER)) {
            args.putString(DefineValue.DENOM_DATA, mIntent.getStringExtra(DefineValue.DENOM_DATA));
            args.putInt(DefineValue.BUY_TYPE, mIntent.getIntExtra(DefineValue.BUY_TYPE, 0));
            args.putString(DefineValue.DETAILS_BILLER, response.optString(DefineValue.DETAIL));
            args.putString(DefineValue.DETAILS_BILLER, response.optString(DefineValue.DETAIL));
            args.putString(DefineValue.PAYMENT_NAME, mIntent.getStringExtra(DefineValue.PAYMENT_NAME));
            args.putString(DefineValue.DESTINATION_REMARK, mIntent.getStringExtra(DefineValue.DESTINATION_REMARK));
            args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, mIntent.getBooleanExtra(DefineValue.IS_SHOW_DESCRIPTION, false));
            String amountDesired = mIntent.getStringExtra(DefineValue.AMOUNT_DESIRED);
            if (amountDesired.isEmpty())
                args.putString(DefineValue.AMOUNT_DESIRED, amountDesired);
            else
                args.putString(DefineValue.AMOUNT_DESIRED, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amountDesired));
            setResult(MainPage.RESULT_BILLER);
        } else if (reportType.equals(DefineValue.COLLECTION) || reportType.equals(DefineValue.TOPUP)) {
            args.putString(DefineValue.BANK_NAME, bankName);
            args.putString(DefineValue.BANK_PRODUCT, bankProduct);
            args.putString(DefineValue.REMARK, mIntent.getStringExtra(DefineValue.REMARK));
        } else if (reportType.equals(DefineValue.BBS_CASHIN) || reportType.equalsIgnoreCase(DefineValue.BBS_MEMBER_OTP)) {
            try {
                args.putString(DefineValue.MEMBER_NAME, response.getString(WebParams.MEMBER_NAME));
                args.putString(DefineValue.SOURCE_ACCT, response.getString(WebParams.SOURCE_BANK_NAME));
                args.putString(DefineValue.SOURCE_ACCT_NO, response.getString(WebParams.SOURCE_ACCT_NO));
                args.putString(DefineValue.SOURCE_ACCT_NAME, response.getString(WebParams.SOURCE_ACCT_NAME));
                args.putString(DefineValue.BANK_BENEF, response.getString(WebParams.BENEF_BANK_NAME));
                args.putString(DefineValue.NO_BENEF, response.getString(WebParams.BENEF_ACCT_NO));
                args.putString(DefineValue.NAME_BENEF, response.getString(WebParams.BENEF_ACCT_NAME));
                args.putString(DefineValue.PRODUCT_NAME, response.getString(WebParams.PRODUCT_NAME));
                args.putString(DefineValue.MEMBER_SHOP_PHONE, response.getString(WebParams.MEMBER_SHOP_PHONE));
                args.putString(DefineValue.MEMBER_SHOP_NO, response.getString(WebParams.MEMBER_SHOP_NO));
                args.putString(DefineValue.MEMBER_SHOP_NAME, response.getString(WebParams.MEMBER_SHOP_NAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            args.putString(DefineValue.BANK_PRODUCT, bankProduct);
            if (reportType.equals(DefineValue.BBS_CASHIN)) {
                Intent data = new Intent();
                data.putExtra(DefineValue.TX_STATUS, txStatus);
                setResult(MainPage.RESULT_BBS_STATUS, data);
            } else if (reportType.equals(DefineValue.BBS_MEMBER_OTP)) {
                args.putString(DefineValue.OTP_MEMBER, response.getString(WebParams.OTP_MEMBER));
            }
        }

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    @Override
    public void onDetachedFromWindow() {
        /*setVisible(false);
        if(webview != null){
            ViewGroup view = (ViewGroup) getWindow().getDecorView();
            view.removeAllViews();
            webview.destroy();
            webview = null;
        }
        */
        super.onDetachedFromWindow();
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void InitializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.sgoplusweb_ab_title_saldomu));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemData = menu.findItem(R.id.action_refresh);

        itemData.setVisible(isDisconnected);
        isDisconnected = false;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_button_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                if (webview != null) {
                    webview.loadUrl(SGO_PLUS_URL);
                    invalidateOptionsMenu();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        /*
        setVisible(false);
        if(webview != null){
            ViewGroup view = (ViewGroup) getWindow().getDecorView();
            view.removeAllViews();
            webview.destroy();
            webview = null;
        }*/
        super.onDestroy();
    }

    @Override
    public void onOkButton() {
        finish();
    }
}