package com.sgo.saldomu.dialogs;/*
  Created by Administrator on 3/6/2015.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import timber.log.Timber;

public class InformationDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "information Dialog";

    private String[] mColName = {"#Cash-In", "#Cash-InViaATM", "#Cash-InViaInternetBanking",
                                 "#Cash-InViaSMSBanking","#Cash-Out","#Pay-Friends","#AskForMoney",
                                 "#GetFreeIDR","#Shopping","#MyFriends","#Report","#Setting",
                                 "#ContactUS","#Logout"};
    private String[] mBBSName = { "hapeku-setortunai-guide.html"};

    private String urlAddress = MyApiClient.URL_FAQ;


    private Activity mContext;
    private Boolean isDisconnected,isBBS = false;
    private View v;
    private int type;
    private ProgressBar progbar;
    private boolean shown = false;
    private Boolean isActivty = false;

    public interface OnDialogOkCallback {
        void onOkButton();
    }

    public static InformationDialog newInstance( int idx) {
        InformationDialog f = new InformationDialog();
        Bundle d = new Bundle();
        d.putInt(DefineValue.TYPE,idx);
        f.setArguments(d);
        return f;
    }

    public static InformationDialog newInstance(Fragment _context, int idx) {
        InformationDialog f = new InformationDialog();
        f.setTargetFragment(_context,0);
        Bundle d = new Bundle();
        d.putInt(DefineValue.TYPE,idx);
        f.setArguments(d);
        return f;
    }

    public static InformationDialog newInstanceBBS(int idx) {
        InformationDialog f = new InformationDialog();
        Bundle d = new Bundle();
        d.putInt(DefineValue.TYPE,idx);
        d.putBoolean(DefineValue.IS_BBS,true);
        f.setArguments(d);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();
    }

    public InformationDialog() {
        // Empty constructor required for DialogFragment

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_information, container);

        Button btn_ok = (Button) v.findViewById(R.id.dialog_btn_ok);
        progbar = (ProgressBar) v.findViewById(R.id.progbar);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btn_ok.setOnClickListener(this);

        isDisconnected = !InetHandler.isNetworkAvailable(getActivity());
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle mBun = getArguments();
        this.type = mBun.getInt(DefineValue.TYPE,0);
        this.isBBS = mBun.getBoolean(DefineValue.IS_BBS,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String colName;
        if(isBBS){
            colName = mBBSName[type];
            urlAddress = MyApiClient.URL_HELP_DEV + colName;
        }
        else {
            colName = mColName[type];
            urlAddress = urlAddress + colName;
        }
        progbar.setIndeterminate(true);
        Timber.e(urlAddress);

        loadUrl(urlAddress);
    }

    private void loadUrl(String url) {
        Timber.d(url);
        WebView webview = (WebView) v.findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
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


        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //setSupportProgress(progress * 100);
                progbar.setVisibility(View.VISIBLE);
                //activity.setProgress(progress * 100);
                if (progress == 100)
                    progbar.setVisibility(View.GONE);
            }
        });
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.d("isi url tombol-tombolnya:" + url);
                view.loadUrl(url);

                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), description, Toast.LENGTH_SHORT).show();
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


        webview.loadUrl(url);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        isDisconnected = false;
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (shown) return;

        super.show(manager, tag);
        shown = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        shown = false;
        super.onDismiss(dialog);
    }
}
