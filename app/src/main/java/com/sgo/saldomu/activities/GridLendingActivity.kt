package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.fragments.FragGridLendingMenu
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_lending.*
import timber.log.Timber

class GridLendingActivity : BaseActivity() {
    override fun getLayoutResource(): Int {
        return R.layout.activity_lending;
    }

    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(R.drawable.ic_arrow_left)
        webView.visibility = View.GONE
        actionBarTitle = getString(R.string.menu_item_lending)
        fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.content_lending, FragGridLendingMenu())
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun switchContent(mFragment: Fragment?, fragName: String?, tag: String?) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_lending, mFragment!!)
                .addToBackStack(tag)
                .commitAllowingStateLoss()
        actionBarTitle = fragName
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun showWebView(url: String) {
        webView.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        showProgressDialog()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        if (Build.VERSION.SDK_INT >= 21 && BuildConfig.DEBUG) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) {
                    dismissProgressDialog()
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl("javascript:(function () {document.getElementsByTagName('body')[0].style.marginBottom = '0'})()")
                super.onPageFinished(view, url)
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                Timber.d("isi url tombol-tombolnya:$url")
                if (url.contains("isclose=1")||url.contains("isback=1")) {
                    hideWebView()
                } else
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                Timber.d("isi failing url tombol-tombolnya:$failingUrl")
            }
        }
        webView.loadUrl(url)
    }

    fun hideWebView() {
        webView.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
    }
}