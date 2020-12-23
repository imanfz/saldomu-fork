package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragInputStoreCode
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber

class PaymentTokoActivity : BaseActivity (){
    override fun getLayoutResource(): Int {
        return com.sgo.saldomu.R.layout.activity_b2b;
    }

    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.purchase_order)

        var bundle = Bundle()
        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, intent.getStringExtra(DefineValue.MEMBER_CODE_ESPAY))
        bundle.putString(DefineValue.COMMUNITY_CODE, intent.getStringExtra(DefineValue.COMMUNITY_CODE))
        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, intent.getStringExtra(DefineValue.COMMUNITY_CODE_ESPAY))
        bundle.putString(DefineValue.DOC_NO, intent.getStringExtra(DefineValue.DOC_NO))
        bundle.putString(DefineValue.TX_ID, intent.getStringExtra(DefineValue.TX_ID))
        bundle.putString(DefineValue.COMMUNITY_ID, intent.getStringExtra(DefineValue.COMMUNITY_ID))
        val newFragment: Fragment
        newFragment = FragPaymentByToko()
        newFragment.arguments = bundle
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