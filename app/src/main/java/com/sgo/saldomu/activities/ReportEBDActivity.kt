package com.sgo.saldomu.activities

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.ListFragment
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.ReportTabAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.dialogs.InformationDialog
import com.sgo.saldomu.fragments.FragReport
import com.sgo.saldomu.fragments.FragReportEBD
import com.sgo.saldomu.fragments.FragTokoEBD
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_report.*
import timber.log.Timber
import java.util.*

class ReportEBDActivity : BaseActivity() {

    var fragmentManager: FragmentManager? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_basic
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar(getString(R.string.menu_item_title_report_ebd))
        if (findViewById<View?>(R.id.content) != null) {
            if (savedInstanceState != null) {
                return
            }
            val newFragment: Fragment
            newFragment = FragReportEBD()
            fragmentManager = supportFragmentManager
            val bundle = Bundle()
            bundle.putString(DefineValue.EBD, intent.getStringExtra(DefineValue.EBD))
            newFragment.arguments = bundle
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.add(R.id.content, newFragment, "FragReportEBD")
            fragmentTransaction.commit()
            setResult(MainPage.RESULT_NORMAL)
        }
    }

    fun initializeToolbar(title : String) {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
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
                    .replace(R.id.content, mFragment, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss()
        } else {
            Timber.d("bukan backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, mFragment, tag)
                    .commitAllowingStateLoss()
        }
        initializeToolbar(fragName)
    }
}