package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragmentScan
import com.sgo.saldomu.widgets.BaseActivity

class QrisActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar()
        supportFragmentManager.beginTransaction().replace(R.id.view, FragmentScan()).commitAllowingStateLoss()
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_qris
    }

    fun initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.scan)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            ToggleKeyboard.hide_keyboard(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}