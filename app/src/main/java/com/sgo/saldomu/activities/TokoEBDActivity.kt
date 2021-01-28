package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragTokoEBD
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber

class TokoEBDActivity : BaseActivity() {
    var fragmentManager: FragmentManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar(getString(R.string.menu_item_title_b2b_eratel))
        if (findViewById<View?>(R.id.toko_ebd_content) != null) {
            if (savedInstanceState != null) {
                return
            }
            val newFragment: Fragment
            newFragment = FragTokoEBD()
            fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.add(R.id.toko_ebd_content, newFragment, "FragTokoEBD")
            fragmentTransaction.commit()
            setResult(MainPage.RESULT_NORMAL)
        }
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_toko_ebd
    }

    fun initializeToolbar(title : String) {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                ToggleKeyboard.hide_keyboard(this)
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun switchContent(mFragment: Fragment, fragName: String, isBackstack: Boolean, tag: String) {
        ToggleKeyboard.hide_keyboard(this)
        if (isBackstack) {
            Timber.d("backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.toko_ebd_content, mFragment, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss()
        } else {
            Timber.d("bukan backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.toko_ebd_content, mFragment, tag)
                    .commitAllowingStateLoss()
        }
        initializeToolbar(fragName)
    }
}