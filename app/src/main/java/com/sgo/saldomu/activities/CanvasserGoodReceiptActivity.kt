package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.fragments.FragInputStoreCode
import com.sgo.saldomu.widgets.BaseActivity

class CanvasserGoodReceiptActivity : BaseActivity() {
    override fun getLayoutResource(): Int {
        return com.sgo.saldomu.R.layout.activity_b2b;
    }


    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(com.sgo.saldomu.R.drawable.ic_arrow_left)
        actionBarTitle = getString(com.sgo.saldomu.R.string.good_receipt_title)
        val newFragment: Fragment
        newFragment = FragInputStoreCode()
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

    fun switchContent(mFragment: Fragment?, fragName: String?, tag: String?) {
        supportFragmentManager
                .beginTransaction()
                .replace(com.sgo.saldomu.R.id.b2b_activity_content, mFragment!!)
                .addToBackStack(tag)
                .commitAllowingStateLoss()
        actionBarTitle = fragName
    }
}