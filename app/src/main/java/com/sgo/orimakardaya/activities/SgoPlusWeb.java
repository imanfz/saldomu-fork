package com.sgo.orimakardaya.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.ReportBillerDialog;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class SgoPlusWeb extends BaseActivity implements ReportBillerDialog.OnDialogOkCallback {

    private WebView webview;
    private String app_name = "SGO Plus Payment Gateaway";
    private String SGO_PLUS_URL = "";
    String masterDomainSGOplus,userID,accessKey;
    String devDomainSGOPlus = "http://secure-dev.sgo.co.id/";
    String prodDomainSGOPlus = "https://secure.sgo.co.id/";
    String prodDomainSGOPlusMandiri = "https://scm.bankmandiri.co.id/sgo+/";
    String bankName, bankProduct,bankCode;
    Boolean isBCA, isDisconnected;
    Intent mIntent;
    ProgressDialog out;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        isDisconnected = !isOnline();

        mIntent   = getIntent();
        String productCode  = mIntent.getStringExtra(DefineValue.PRODUCT_CODE);
        bankProduct = mIntent.getStringExtra(DefineValue.PRODUCT_NAME);
        bankCode = mIntent.getStringExtra(DefineValue.BANK_CODE);
        bankName = mIntent.getStringExtra(DefineValue.BANK_NAME);
        String commCode = mIntent.getStringExtra(DefineValue.COMMUNITY_CODE);
        String fee = mIntent.getStringExtra(DefineValue.FEE);
        String paymentId = mIntent.getStringExtra(DefineValue.TX_ID);
        String apikey  = mIntent.getStringExtra(DefineValue.API_KEY)  ;
        String amount = mIntent.getStringExtra(DefineValue.AMOUNT);
        String comm_id = mIntent.getStringExtra(DefineValue.COMMUNITY_ID);
        String reportType = mIntent.getStringExtra(DefineValue.REPORT_TYPE);
        String transType = mIntent.getStringExtra(DefineValue.TRANSACTION_TYPE);
        String shareType = mIntent.getStringExtra(DefineValue.SHARE_TYPE);
        String totalAmount = mIntent.getStringExtra(DefineValue.TOTAL_AMOUNT);
        Timber.d("isi intent:"+ mIntent.getExtras().toString());

        //if(MyApiClient.PROD_FAILURE_FLAG && topUpType.equals(CoreApp.PULSA))masterDomainSGOplus = prodDomainSGOPlus;
        if(MyApiClient.IS_PROD){
            if(bankCode.equals("008"))masterDomainSGOplus = prodDomainSGOPlusMandiri;
            else masterDomainSGOplus = prodDomainSGOPlus;
        }
        else masterDomainSGOplus = devDomainSGOPlus;


        SGO_PLUS_URL = masterDomainSGOplus + "index/order/?key=" + apikey +
                "&paymentId="+paymentId+
                "&commCode="+commCode+
                "&bankCode="+bankCode+
                "&productCode="+productCode+"&mobile=1";


        if(!bankCode.equals("008")){
            try {
                String callbackUrl = mIntent.getStringExtra(DefineValue.CALLBACK_URL);
                if(!callbackUrl.isEmpty())
                    SGO_PLUS_URL = SGO_PLUS_URL + "&url=" + URLEncoder.encode(callbackUrl+"?refid=" + gen_numb() + "&ref_back_url=" + productCode + "&isclose=1", "utf-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        //Toast.makeText(this, SGO_PLUS_URL, Toast.LENGTH_LONG).show();
        Timber.d("sgo+ urlnya:"+ SGO_PLUS_URL);
        //showReportBillerDialog(MyApiClient.getCurrentDateTime(), paymentId, sp.getString(CoreApp.USERID_PHONE, "")
        //        , bankName,productCode,fee,amount);
        loadUrl(sp.getString(DefineValue.USER_NAME,""),SGO_PLUS_URL, paymentId,userID,totalAmount,
                fee,amount,reportType,comm_id,transType,shareType);
        setResult(MainPage.RESULT_NORMAL);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public int gen_numb() {
        Random r = new Random( System.currentTimeMillis() );
        return ((1 + r.nextInt(9)) * 100000000 + r.nextInt(100000000));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_sgo_plus_web;
    }

    @SuppressWarnings("deprecation")
    public void loadUrl(final String userName, String url, final String payment_id, final String userId, final String totalAmount,
                        final String fee, final String amount, final String reportType, final String commId,
                        final String transType,final String shareType) {
        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (android.os.Build.VERSION.SDK_INT<=11) {
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
                Timber.d("isi url tombol-tombolnya:"+ url);
                if (url.contains("isclose=1")){
                    setResult(MainPage.RESULT_BALANCE);
                    getTrxStatus(userName, DateTimeFormat.getCurrentDateTime(), payment_id, userId, totalAmount,
                                 fee, amount,reportType,commId,transType, shareType);
                }
                else if (url.contains("isback=1")){
                    setResult(MainPage.RESULT_BALANCE);
                    onOkButton();
                } else view.loadUrl(url);

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
                else if(errorCode == ERROR_TIMEOUT)
                    message = getString(R.string.webview_err_timeout);
                else if(errorCode == ERROR_BAD_URL)
                    message = getString(R.string.webview_err_bad_url);
                else if(errorCode == ERROR_TOO_MANY_REQUESTS)
                    message = getString(R.string.webview_err_too_many_req);

                if(!message.isEmpty()) {
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
                if(MyApiClient.IS_PROD)
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


    public void getTrxStatus(final String userName, final String date, final String txId, final String userId,
                             final String totalAmount, final String fee, final String amount, final String reportType,
                             final String comm_id,final String transtype, final String shareType){
        try{
            out = DefinedDialog.CreateProgressDialog(this, null);
            out.show();


            RequestParams params =  MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_GET_TRX_STATUS,
                    userID,accessKey);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TYPE, transtype);
            params.put(WebParams.PRIVACY, shareType);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userId);

            if(reportType.equals(DefineValue.BILLER_PLN)){
                params.put(WebParams.IS_DETAIL, DefineValue.STRING_YES);
            }

            Timber.d("isi params sent get Trx Status:" + params.toString());

            MyApiClient.sentGetTRXStatus(this, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        out.dismiss();
                        Timber.d("isi response sent get Trx Status:"+ response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            String txstatus = response.getString(WebParams.TX_STATUS);
                            showReportBillerDialog(userName,DateTimeFormat.formatToID(response.optString(WebParams.CREATED,"")),txId, userId,totalAmount,fee,amount,
                                    txstatus,response.getString(WebParams.TX_REMARK),
                                    reportType,response);
                        }else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+ response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(SgoPlusWeb.this,message);
                        }
                        else {
                            String msg = response.getString(WebParams.ERROR_MESSAGE);
                            if(code.equals("0003")){
                                showReportBillerDialog(userName,date,txId, userId,totalAmount,fee,amount,
                                        DefineValue.FAILED,getString(R.string.transaction_failed_tx_id),reportType,response);
                            }
                            else
                                showDialog(msg);
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
                        Toast.makeText(SgoPlusWeb.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(SgoPlusWeb.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(out.isShowing()) {
                        out.dismiss();
                        showDialog(getString(R.string.network_connection_failure_toast));
                    }
                   Timber.w("Error Koneksi app version registration:"+ throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    void showDialog(String msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
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
                SgoPlusWeb.this.finish();
            }
        });

        dialog.show();
    }


    private void showReportBillerDialog(String userName, String date,String txId, String userId,String total_amount,
                                        String fee, String amount, String txStatus, String txRemark, String reportType,
                                        JSONObject response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, userName);
        args.putString(DefineValue.DATE_TIME,date);
        args.putString(DefineValue.TX_ID,txId);
        args.putString(DefineValue.REPORT_TYPE, reportType);
        args.putString(DefineValue.USERID_PHONE, userId);

        args.putString(DefineValue.FEE,MyApiClient.CCY_VALUE+". "+ CurrencyFormat.format(fee));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

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

        if(reportType.equals(DefineValue.BILLER_PLN)){
            if(getIntent().hasExtra(DefineValue.IS_PLN)) {
                Boolean isPLN = getIntent().getBooleanExtra(DefineValue.IS_PLN,false);
                if (isPLN && response.has(WebParams.DETAIL)) {
                    args.putString(DefineValue.BILLER_TYPE, getIntent().getStringExtra(DefineValue.BILLER_TYPE));
                    args.putString(DefineValue.DETAIL, response.optString(WebParams.DETAIL, ""));
                }
            }
            setResult(MainPage.RESULT_BILLER);

        }
        else if(reportType.equals(DefineValue.BILLER)){
            args.putString(DefineValue.DENOM_DATA, mIntent.getStringExtra(DefineValue.DENOM_DATA));
            args.putInt(DefineValue.BUY_TYPE, mIntent.getIntExtra(DefineValue.BUY_TYPE, 0));
            args.putString(DefineValue.DESC_FIELD, mIntent.getStringExtra(DefineValue.DESC_FIELD));
            args.putString(DefineValue.DESC_VALUE, mIntent.getStringExtra(DefineValue.DESC_VALUE));
            args.putString(DefineValue.PAYMENT_NAME, mIntent.getStringExtra(DefineValue.PAYMENT_NAME));
            args.putString(DefineValue.DESTINATION_REMARK,mIntent.getStringExtra(DefineValue.DESTINATION_REMARK));
            args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, mIntent.getBooleanExtra(DefineValue.IS_SHOW_DESCRIPTION,false));
            String amountDesired = mIntent.getStringExtra(DefineValue.AMOUNT_DESIRED);
            if(amountDesired.isEmpty())
                args.putString(DefineValue.AMOUNT_DESIRED,amountDesired);
            else
                args.putString(DefineValue.AMOUNT_DESIRED, MyApiClient.CCY_VALUE + ". " +CurrencyFormat.format(amountDesired));
            setResult(MainPage.RESULT_BILLER);
        }
        else if(reportType.equals(DefineValue.COLLECTION)||reportType.equals(DefineValue.TOPUP)){
            args.putString(DefineValue.BANK_NAME, bankName);
            args.putString(DefineValue.BANK_PRODUCT, bankProduct);
            args.putString(DefineValue.REMARK,mIntent.getStringExtra(DefineValue.REMARK));
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
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public void InitializeToolbar(){
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
                if(webview != null) {
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