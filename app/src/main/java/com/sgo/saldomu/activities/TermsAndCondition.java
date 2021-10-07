package com.sgo.saldomu.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class TermsAndCondition extends BaseActivity {
    private WebView webview_privacypolicy;
    private ProgressBar progbar;
    private Boolean isDisconnected;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_privacy_policy;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        progbar = findViewById(R.id.progbar);

        isDisconnected = !InetHandler.isNetworkAvailable(this);

        loadUrl(MyApiClient.domainPrivacyPolicy);
    }

    private void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.terms_and_condition));
    }

    private void loadUrl(String url) {
        Timber.d(url);
        webview_privacypolicy = findViewById(R.id.webview_privacypolicy);
        WebSettings webSettings = webview_privacypolicy.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (android.os.Build.VERSION.SDK_INT<=11) {
            webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        }


        webview_privacypolicy.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //setSupportProgress(progress * 100);
                progbar.setVisibility(View.VISIBLE);
                //activity.setProgress(progress * 100);
                if (progress == 100)
                    progbar.setVisibility(View.GONE);
            }
        });
        webview_privacypolicy.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.d("isi url tombol-tombolnya:" + url);
                view.loadUrl(url);

                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(TermsAndCondition.this, description, Toast.LENGTH_SHORT).show();
                isDisconnected = true;

            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
//                if(InformationDialog.this.isVisible())
//                    Toast.makeText(getActivity(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
//                if(InformationDialog.this.isVisible())
//                    Toast.makeText(getActivity(), error.getDescription(), Toast.LENGTH_SHORT).show();
            }

//            @Override
//            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error) {
//                if(MyApiClient.IS_PROD)
//                    super.onReceivedSslError(view, handler, error);
//                else
//                    handler.proceed();
//
//            }

            @Override
            public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                super.onReceivedClientCertRequest(view, request);
            }
        });


        webview_privacypolicy.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                if(webview_privacypolicy != null) {
                    webview_privacypolicy.loadUrl(MyApiClient.domainPrivacyPolicy);
                    invalidateOptionsMenu();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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
}
