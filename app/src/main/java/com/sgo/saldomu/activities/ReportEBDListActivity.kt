package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.NotificationTabAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.LevelClass
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.dialogs.InformationDialog
import com.sgo.saldomu.fragments.FragReportEBDList
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_notification.*

class ReportEBDListActivity : BaseActivity() {
    var memberCodeEspay = ""
    var commCodeEspay = ""

    override fun getLayoutResource(): Int {
        return R.layout.activity_notification
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar()
        if (intent.hasExtra(DefineValue.EBD)) {
            if (intent.getStringExtra(DefineValue.EBD).equals(DefineValue.CANVASSER)) {
                memberCodeEspay = ""
                commCodeEspay = ""
            }
        } else {
            memberCodeEspay = intent.getStringExtra(DefineValue.MEMBER_CODE_ESPAY)
            commCodeEspay = intent.getStringExtra(DefineValue.COMMUNITY_CODE_ESPAY)
        }
        val adapter = NotificationTabAdapter(supportFragmentManager)
        adapter.addFragment(FragReportEBDList(DefineValue.PO, memberCodeEspay, commCodeEspay), getString(R.string.purchase_order))
        adapter.addFragment(FragReportEBDList(DefineValue.GR, memberCodeEspay, commCodeEspay), getString(R.string.good_receipt_title))
        adapter.addFragment(FragReportEBDList(DefineValue.IN, memberCodeEspay, commCodeEspay), getString(R.string.invoice_title))
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_report_ebd)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                ToggleKeyboard.hide_keyboard(this)
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}