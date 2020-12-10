package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragB2BCanvasserMenu
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber

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

    fun switchContent(mFragment: Fragment, fragName: String, isBackstack: Boolean, tag: String) {
        ToggleKeyboard.hide_keyboard(this)
        if (isBackstack) {
            Timber.d("backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(com.sgo.saldomu.R.id.b2b_activity_content, mFragment, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss()
        } else {
            Timber.d("bukan backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(com.sgo.saldomu.R.id.b2b_activity_content, mFragment, tag)
                    .commitAllowingStateLoss()
        }
        initializeToolbar(fragName)
    }

    fun initializeToolbar(title: String) {
        setActionBarIcon(com.sgo.saldomu.R.drawable.ic_arrow_left)
        actionBarTitle = title
    }

    fun switchActivity(mIntent: Intent?, j: Int) {
        ToggleKeyboard.hide_keyboard(this)
        when (j) {
            MainPage.ACTIVITY_RESULT -> startActivityForResult(mIntent, MainPage.REQUEST_FINISH)
            2 -> {
            }
        }
    }
}