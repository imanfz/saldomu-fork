package com.sgo.saldomu.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ClientCertRequest;
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
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.widgets.BaseActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;

import timber.log.Timber;

/**
 * Created by thinkpad on 9/15/2015.
 */
public class PulsaAgentWeb extends BaseActivity implements ReportBillerDialog.OnDialogOkCallback {

    private WebView webview;
    private String app_name = "SGO Plus Payment Gateaway";
    private String SGO_PLUS_URL = "";
    private String masterDomainSGOplus;
    private String userID;
    private String accessKey;
    private String devDomainSGOPlus = "https://sandbox-kit.espay.id/";
    private String prodDomainSGOPlus = "https://kit.espay.id/";
    private String prodDomainSGOPlusMandiri = "https://scm.bankmandiri.co.id/sgo+/";
    String bankName;
    private String bankProduct;
    private String bankCode;
    private String paymentName;
    private String item_name;
    private String operator_name;
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
//        bankName = mIntent.getStringExtra(CoreApp.BANK_NAME);
        String commCode = mIntent.getStringExtra(DefineValue.COMMUNITY_CODE);
        String fee = mIntent.getStringExtra(DefineValue.FEE);
        String paymentId = mIntent.getStringExtra(DefineValue.TX_ID);
        String apikey = mIntent.getStringExtra(DefineValue.API_KEY);
        String amount = mIntent.getStringExtra(DefineValue.AMOUNT);
        String comm_id = mIntent.getStringExtra(DefineValue.COMMUNITY_ID);
        String totalAmount = mIntent.getStringExtra(DefineValue.TOTAL_AMOUNT);
        String reportType = mIntent.getStringExtra(DefineValue.REPORT_TYPE);
        String transType = DefineValue.BIL_PURCHASE_TYPE;
        String shareType = mIntent.getStringExtra(DefineValue.SHARE_TYPE);
        paymentName = mIntent.getStringExtra(DefineValue.PAYMENT_NAME);
        item_name = mIntent.getStringExtra(DefineValue.DENOM_DATA);
        operator_name = mIntent.getStringExtra(DefineValue.OPERATOR_NAME);
        Timber.d("isi intent", mIntent.getExtras().toString());

        //if(MyApiClient.PROD_FLAG && topUpType.equals(CoreApp.PULSA))masterDomainSGOplus = prodDomainSGOPlus;
        if (MyApiClient.IS_PROD) {
            if (bankCode.equals("008")) masterDomainSGOplus = prodDomainSGOPlusMandiri;
            else masterDomainSGOplus = prodDomainSGOPlus;
        } else masterDomainSGOplus = devDomainSGOPlus;


        SGO_PLUS_URL = masterDomainSGOplus + "index/order/?key=" + apikey +
                "&paymentId=" + paymentId +
                "&commCode=" + commCode +
                "&bankCode=" + bankCode +
                "&productCode=" + productCode + "&mobile=1";


//        if(!bankCode.equals("008")){
        try {
            String callbackUrl = mIntent.getStringExtra(DefineValue.CALLBACK_URL);
            if (!callbackUrl.isEmpty())
                SGO_PLUS_URL = SGO_PLUS_URL + "&url=" + URLEncoder.encode(callbackUrl + "?refid=" + gen_numb() + "&ref_back_url=" + productCode + "&isclose=1", "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        }


        //Toast.makeText(this, SGO_PLUS_URL, Toast.LENGTH_LONG).show();
        Timber.d("sgo+ urlnya", SGO_PLUS_URL);
        //showReportBillerDialog(MyApiClient.getCurrentDateTime(), paymentId, sp.getString(CoreApp.USERID_PHONE, "")
        //        , bankName,productCode,fee,amount);
        loadUrl(sp.getString(DefineValue.USER_NAME, ""), SGO_PLUS_URL, paymentId, userID, totalAmount,
                fee, amount, reportType, comm_id, transType, shareType);
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
                         final String transType, final String shareType) {
        webview = (WebView) findViewById(R.id.webview);
        assert webview != null;
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        if (bankCode.equals("014")) {
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
        }
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (android.os.Build.VERSION.SDK_INT <= 11) {
            webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        }

        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //setSupportProgress(progress * 100);
                getProgressSpinner().setVisibility(View.VISIBLE);
                //activity.setProgress(progress * 100);
                if (progress == 100)
                    getProgressSpinner().setVisibility(View.GONE);
            }
        });
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.d("isi url tombol-tombolnya", url);
                if (url.contains("isclose=1")) {
                    setResult(MainPage.RESULT_BALANCE);
                    getTrxStatus(userName, DateTimeFormat.getCurrentDateTime(), payment_id, userId, totalAmount,
                            fee, amount, reportType, commId, transType, shareType);
                } else if (url.contains("isback=1")) {
                    setResult(MainPage.RESULT_BALANCE);
                    onOkButton();
                } else view.loadUrl(url);

                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                isDisconnected = true;
                invalidateOptionsMenu();

            }

//            @Override
//            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error) {
//                if(!bankCode.equals("008"))handler.proceed();
//                else super.onReceivedSslError(view, handler, error);
//            }

            @Override
            public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                super.onReceivedClientCertRequest(view, request);
            }
        });


        webview.loadUrl(url);
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
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.TYPE, transtype);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);

            Timber.d("isi params sent get Trx Status", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            GetTrxStatusReportModel model = gson.fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                showReportBillerDialog(userName, DateTimeFormat.formatToID(model.getCreated()),
                                        txId, userId, totalAmount, fee, amount,
                                        model.getTx_status(), model.getTx_remark(), reportType);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(PulsaAgentWeb.this, message);
                            } else {
                                if (code.equals("0003")) {
                                    showReportBillerDialog(userName, date, txId, userId, totalAmount, fee, amount,
                                            DefineValue.FAILED, getString(R.string.transaction_failed_tx_id), reportType);
                                } else
                                    showDialog(message);
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
            Timber.d("httpclient", e.getMessage());
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
        Button btnDialogOTP = (Button) dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView) dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView) dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.error));
        Message.setText(msg);

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                PulsaAgentWeb.this.finish();
            }
        });

        dialog.show();
    }


    private void showReportBillerDialog(String userName, String date, String txId, String userId, String total_amount,
                                        String fee, String amount, String txStatus, String txRemark, String reportType) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.REPORT_TYPE, reportType);
        args.putString(DefineValue.USERID_PHONE, userId);

        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));
        args.putString(DefineValue.PAYMENT_NAME, paymentName);
        args.putString(DefineValue.DENOM_DATA, item_name);
        args.putString(DefineValue.OPERATOR_NAME, operator_name);
        args.putString(DefineValue.DESTINATION_REMARK, getIntent().getStringExtra(DefineValue.DESTINATION_REMARK));

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


        if (reportType.equals(DefineValue.PULSA_AGENT)) {
            setResult(PulsaAgentActivity.RESULT_DAP);
        }

        Timber.d("isi args", args.toString());
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
                    //if(webview.canGoBack()){
                    //    webview.goBack();
                    //}else{
                    //    finish();
                    //}
                    finish();
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void InitializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.sgoplusweb_ab_title));
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
