package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.R
import com.sgo.saldomu.fragments.FragGridLendingMenu
import com.sgo.saldomu.widgets.BaseActivity

class GridLendingActivity : BaseActivity() {
    override fun getLayoutResource(): Int {
        return R.layout.activity_lending;
    }

    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(R.drawable.ic_arrow_left)
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
                .replace(R.id.content, mFragment!!)
                .addToBackStack(tag)
                .commitAllowingStateLoss()
        actionBarTitle = fragName
    }
}