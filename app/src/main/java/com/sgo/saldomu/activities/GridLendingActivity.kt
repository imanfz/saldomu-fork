package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragGridLendingMenu
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber

class GridLendingActivity : BaseActivity() {
    override fun getLayoutResource(): Int {
        return R.layout.activity_lending;
    }

    var fragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar()
        if (findViewById<View?>(R.id.content) != null) {
            if (savedInstanceState != null) {
                return
            }
            val newFragment: Fragment
            newFragment = FragGridLendingMenu()
            fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.add(R.id.content_lending, newFragment)
            fragmentTransaction.commitAllowingStateLoss()
            setResult(MainPage.RESULT_NORMAL)
        }
    }

    fun initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_lending)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else
                    finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun switchContent(mFragment: Fragment?, fragName: String?, isBackstack: Boolean, tag: String?) {
        ToggleKeyboard.hide_keyboard(this)
        if (isBackstack) {
            Timber.d("backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_lending, mFragment!!, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss()
        } else {
            Timber.d("bukan backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_lending, mFragment!!, fragName)
                    .commitAllowingStateLoss()
        }
        actionBarTitle = fragName
    }

    fun switchActivity(mIntent: Intent?, j: Int) {
        ToggleKeyboard.hide_keyboard(this)
        when (j) {
            MainPage.ACTIVITY_RESULT -> startActivityForResult(mIntent, MainPage.REQUEST_FINISH)
            2 -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RetrofitService.dispose()
    }


    fun setToolbarTitle(title: String?) {
        actionBarTitle = title
    }
}