package com.sgo.saldomu.activities

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.ListFragment
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.ReportTabAdapter
import com.sgo.saldomu.dialogs.InformationDialog
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_report.*
import java.util.*

class ReportEBDActivity : BaseActivity() {

    private var informationDialog: InformationDialog? = null

    override fun getLayoutResource(): Int {
        return R.layout.activity_report
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToolbar()
        val pageMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        informationDialog = InformationDialog.newInstance(10);
        val titles = this.resources.getStringArray(R.array.report_list_ebd)
        val mList: List<ListFragment> = ArrayList()
        val adapter = ReportTabAdapter(supportFragmentManager, this, mList, titles)
        report_pager_activity.adapter = adapter
        report_pager_activity.pageMargin = pageMargin
        report_tabs_activity.setViewPager(report_pager_activity)
        report_pager_activity.currentItem = 0
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
            R.id.action_information -> {
                if (!informationDialog!!.isAdded) informationDialog?.show(this.supportFragmentManager, InformationDialog.TAG)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}