package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ClientCertRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/14/2016.
 */
public class FragFAQ extends Fragment {
    SecurePreferences sp;
    private View v;

    public static FragFAQ newInstance() {
        FragFAQ mFrag = new FragFAQ();
        return mFrag;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Timber.e(MyApiClient.URL_FAQ);
        loadUrl(MyApiClient.URL_FAQ);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_faq, container, false);
        return v;
    }


    private void loadUrl(String url) {
        WebView webview = (WebView) v.findViewById(R.id.webViewFAQ);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (android.os.Build.VERSION.SDK_INT<=11) {
            webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        }

        final Activity activity = getActivity();
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
//                setSupportProgress(progress * 100);
//                getProgressSpinner().setVisibility(View.VISIBLE);
//                activity.setProgress(progress * 100);
//                if(progress == 100)
//                    getProgressSpinner().setVisibility(View.GONE);
            }
        });
        webview.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
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


        webview.loadUrl(url);
    }
}
