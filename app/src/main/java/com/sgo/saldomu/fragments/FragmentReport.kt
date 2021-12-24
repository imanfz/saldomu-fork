package com.sgo.saldomu.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.ListFragment
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.ReportTabAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.activity_report.*
import java.util.ArrayList

class FragmentReport : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.activity_report, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)

        if (savedInstanceState == null) {
            val pageMargin =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4f, resources
                        .displayMetrics
                ).toInt()

            val titles: Array<String> = if (isAgent)
                this.resources.getStringArray(R.array.report_list_agen)
            else this.resources.getStringArray(R.array.report_list)

            val mList: MutableList<ListFragment> = ArrayList()
//            mList.add(FragReport.newInstance(FragReport.REPORT_PENDING));
            mList.add(
                ListFragmentReport.newInstance(
                    ListFragmentReport.REPORT_ESPAY))
            if (isAgent) {
                mList.add(
                    ListFragmentReport.newInstance(
                        ListFragmentReport.REPORT_FEE))
                mList.add(
                    ListFragmentReport.newInstance(
                        ListFragmentReport.REPORT_ADDITIONAL_FEE))
            }

            val adapternya = ReportTabAdapter(activity!!.supportFragmentManager, mList, titles)
            report_pager_activity.adapter = adapternya
            report_pager_activity.pageMargin = pageMargin
            report_pager_activity.currentItem = 0
            report_tabs_activity.setViewPager(report_pager_activity)
        }
    }
}