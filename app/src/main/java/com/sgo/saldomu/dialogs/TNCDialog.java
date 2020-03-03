package com.sgo.saldomu.dialogs;

import android.annotation.SuppressLint;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import timber.log.Timber;

public class TNCDialog extends DialogFragment {
    Button buttonOK;
    WebView webView;
    LinearLayout layout;
    ProgressBar progressBar;
    public OnTapItemListener listener;

    public interface OnTapItemListener {
        void onSubmit(DialogFragment dialog);
    }

    public static TNCDialog newDialog(OnTapItemListener listener) {
        TNCDialog dialog = new TNCDialog();
        dialog.setCancelable(false);
        dialog.listener = listener;

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_tnc, container, false);
        buttonOK = v.findViewById(R.id.btn_ok);
        webView = v.findViewById(R.id.webView);
        layout = v.findViewById(R.id.content_layout);
        progressBar = v.findViewById(R.id.progress_bar_web);
        hideView();
        CheckBox checkBox = v.findViewById(R.id.cb_termsncondition);
        checkBox.setOnCheckedChangeListener((compoundButton, isCheck) -> {
            if (isCheck) {
                enabledButton();
            } else {
                disabledButton();
            }
        });

        buttonOK.setOnClickListener(v1 -> listener.onSubmit(this));

        if (InetHandler.isNetworkAvailable(getActivity()))
            loadUrl(MyApiClient.URL_TERMS);
        else
            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();

        return v;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT >= 21 && BuildConfig.DEBUG) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    showView();
                    disabledButton();
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl("javascript:(function () {document.getElementsByTagName('body')[0].style.marginBottom = '0'})()");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.d("isi url tombol-tombolnya:" + url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Timber.d("isi failing url tombol-tombolnya:" + failingUrl);
            }


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                super.onReceivedClientCertRequest(view, request);
            }
        });
        webView.loadUrl(url);
    }

    private void disabledButton() {
        buttonOK.setEnabled(false);
        buttonOK.setBackground(getResources().getDrawable(R.drawable.rounded_background_button_disabled));
    }

    private void enabledButton() {
        buttonOK.setEnabled(true);
        buttonOK.setBackground(getResources().getDrawable(R.drawable.rounded_background_blue));
    }

    private void hideView() {
        layout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showView() {
        layout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
