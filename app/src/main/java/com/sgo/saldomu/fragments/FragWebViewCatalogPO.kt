package com.sgo.saldomu.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_webview_catalog_po.*
import timber.log.Timber

class FragWebViewCatalogPO : BaseFragment() {

    var url =""
    var bundle = Bundle()
    var isDisconnected : Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_webview_catalog_po, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bundle = arguments!!
        url = bundle.getString(DefineValue.URL, "")

        loadURL(url)
    }

    private fun loadURL(url: String)
    {
        Timber.d(url)
        val webSettings = webview_catalog!!.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        if (Build.VERSION.SDK_INT <= 11) {
            webSettings.setAppCacheMaxSize((1024 * 1024 * 8).toLong())
        }


        webview_catalog!!.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                //setSupportProgress(progress * 100);
                progbar.setVisibility(View.VISIBLE)
                //activity.setProgress(progress * 100);
                if (progress == 100) progbar.setVisibility(View.GONE)
            }
        })
        webview_catalog!!.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Timber.d("isi url tombol-tombolnya:$url")
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show()
                isDisconnected = true
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                super.onReceivedHttpError(view, request, errorResponse)
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
                super.onReceivedClientCertRequest(view, request)
            }
        })


        webview_catalog!!.loadUrl(url)
    }
}