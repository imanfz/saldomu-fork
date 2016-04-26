package com.sgo.orimakardaya.dialogs;/*
  Created by Administrator on 3/6/2015.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.BillerActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.InetHandler;
import com.sgo.orimakardaya.coreclass.MyApiClient;

import org.json.JSONArray;
import org.json.JSONException;

import timber.log.Timber;

public class InformationDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "information Dialog";

    private String[] mColName = {"#Cash-In", "#Cash-InViaATM", "#Cash-InViaInternetBanking",
                                 "#Cash-InViaSMSBanking","#Cash-Out","#Pay-Friends","#AskForMoney",
                                 "#GetFreeIDR","#Shopping","#MyFriends","#Report","#Setting",
                                 "#ContactUS","#Logout"};

    private String urlAddress = MyApiClient.URL_FAQ;


    private OnDialogOkCallback callback;
    private Activity mContext;
    private Boolean isActivty = false, isDisconnected;
    private View v;
    private int type;
    private ProgressBar progbar;
    WebView webview;
    private boolean shown = false;


    public interface OnDialogOkCallback {
        void onOkButton();
    }



    public static InformationDialog newInstance(Activity _context, int idx) {
        InformationDialog f = new InformationDialog();
        f.mContext = _context;
        f.type = idx;
        f.isActivty = true;
        return f;
    }

    public static InformationDialog newInstance(Fragment _context,int idx) {
        InformationDialog f = new InformationDialog();
        f.setTargetFragment(_context,0);
        f.type = idx;
        f.isActivty = false;
        return f;
    }


    public InformationDialog() {
        // Empty constructor required for DialogFragment

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if(isActivty)
                callback = (OnDialogOkCallback) getActivity();
            else
                callback = (OnDialogOkCallback) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.dismiss();
        callback.onOkButton();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Bundle mBun = getArguments();
//        this.type = mBun.getInt(DefineValue.TYPE,0);

        String colName;
        colName = mColName[type];



        progbar.setIndeterminate(true);
        Timber.e(urlAddress);
        loadUrl(urlAddress + colName);
    }

    public void loadUrl(String url) {
        Timber.d(url);
        webview = (WebView) v.findViewById(R.id.webview);
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        isDisconnected = false;
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
        callback.onOkButton();
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
