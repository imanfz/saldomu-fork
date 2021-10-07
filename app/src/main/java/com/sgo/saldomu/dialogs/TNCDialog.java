package com.sgo.saldomu.dialogs;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ClientCertRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import timber.log.Timber;

public class TNCDialog extends DialogFragment {
    Button buttonOK, buttonCancel;
    WebView webView;
    LinearLayout layout;
    ProgressBar progressBar;
    CheckBox checkBox;
    public OnTapItemListener listener;

    public interface OnTapItemListener {
        void onSubmit(DialogFragment dialog);

        void onCancel(DialogFragment dialog);
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
        buttonCancel = v.findViewById(R.id.btn_cancel);
        webView = v.findViewById(R.id.webView);
        layout = v.findViewById(R.id.content_layout);
        progressBar = v.findViewById(R.id.progress_bar_web);
        hideView();
        checkBox = v.findViewById(R.id.cb_termsncondition);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initWebViewScrollListener();
        }

        checkBox.setOnCheckedChangeListener((compoundButton, isCheck) -> {
            if (isCheck) {
                enabledButton();
            } else {
                disabledButton();
            }
        });

        buttonOK.setOnClickListener(v1 -> listener.onSubmit(this));
        buttonCancel.setOnClickListener(v1 -> listener.onCancel(this));

        if (InetHandler.isNetworkAvailable(getActivity()))
            loadUrl(MyApiClient.domainPrivacyPolicy);
        else
            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();

        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initWebViewScrollListener() {
        checkBox.setEnabled(false);
        webView.setOnScrollChangeListener((v12, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int height = (int) Math.floor(webView.getContentHeight() * webView.getScale());
            height -= (height * 0.017);
            int webViewHeight = webView.getMeasuredHeight();
            Timber.i(scrollY + webViewHeight + " = " + height);
            if (scrollY + webViewHeight >= height)
                checkBox.setEnabled(true);
        });
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
                view.loadUrl("javascript:(function () {document.getElementsByTagName('body')[0].style.marginBottom = '0'})()");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.d("isi url tombol-tombolnya:%s", url);
                view.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Timber.d("isi failing url tombol-tombolnya:%s", failingUrl);
            }


//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//            }

            @Override
            public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                super.onReceivedClientCertRequest(view, request);
            }
        });
        webView.loadUrl(url);
    }

    private void disabledButton() {
        buttonOK.setEnabled(false);
        buttonOK.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_background_button_disabled, null));
    }

    private void enabledButton() {
        buttonOK.setEnabled(true);
        buttonOK.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_background_blue, null));
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
