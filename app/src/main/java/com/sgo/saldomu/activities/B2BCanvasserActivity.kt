package com.sgo.saldomu.activities

import android.R
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.fragments.FragB2B
import com.sgo.saldomu.fragments.FragB2BCanvasserMenu
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_lending.*

class B2BCanvasserActivity : BaseActivity() {
    override fun getLayoutResource(): Int {
        return com.sgo.saldomu.R.layout.activity_b2b;
    }

    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(com.sgo.saldomu.R.drawable.ic_arrow_left)
        actionBarTitle = getString(com.sgo.saldomu.R.string.menu_item_title_scadm)
        val newFragment: Fragment
        newFragment = FragB2BCanvasserMenu()
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