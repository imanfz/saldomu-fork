package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragInpInvCode
import com.sgo.saldomu.fragments.FragInputStoreCode
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber


class CanvasserInvoiceActivity : BaseActivity() {

    private var ListPO: String = "list_po"

    override fun getLayoutResource(): Int {
        return com.sgo.saldomu.R.layout.activity_b2b;
    }


    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(com.sgo.saldomu.R.drawable.ic_arrow_left)
        actionBarTitle = getString(com.sgo.saldomu.R.string.invoice_title)
        val newFragment: Fragment
        newFragment = FragInpInvCode()
        fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.add(com.sgo.saldomu.R.id.b2b_activity_content, newFragment, "b2b")
        fragmentTransaction.commit()
        setResult(MainPage.RESULT_NORMAL)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun switchContent(mFragment: Fragment?, fragName: String?, next_frag_title: String?, isBackstack: Boolean, tag: String?) {
        if (isBackstack) {
            Timber.d("backstack:" + "masuk")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.b2b_activity_content, mFragment!!, tag)
                    .addToBackStack(fragName)
                    .commitAllowingStateLoss()
        } else {
            Timber.d("bukan backstack:" + "masuk")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.b2b_activity_content, mFragment!!, tag)
                    .commitAllowingStateLoss()
        }
        if (next_frag_title != null) actionBarTitle = next_frag_title
        ToggleKeyboard.hide_keyboard(this)
    }

    fun initializeToolbar(title: String) {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = title
    }
}