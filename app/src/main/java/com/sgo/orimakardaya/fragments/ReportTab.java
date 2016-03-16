package com.sgo.orimakardaya.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.adapter.ReportTabAdapter;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/*
  Created by Administrator on 7/7/2015.
 */
public class ReportTab extends Fragment{

    private ReportTabAdapter currentAdapternya;
    SecurePreferences sp;
    private View currentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_report_tab, container, false);
        setCurrentView(v);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                    .getDisplayMetrics());
            ReportTabAdapter adapternya;
            TabPageIndicator tabs;
            ViewPager pager;
            String[] titles = getActivity().getResources().getStringArray(R.array.report_list);

            List<ListFragment> mList = new ArrayList<ListFragment>();
            mList.add(FragReport.newInstance(FragReport.REPORT_ESPAY));
            mList.add(FragReport.newInstance(FragReport.REPORT_SCASH));
            mList.add(FragReport.newInstance(FragReport.REPORT_ASK));

            tabs = (TabPageIndicator) getCurrentView().findViewById(R.id.report_tabs);
            pager = (ViewPager) getCurrentView().findViewById(R.id.report_pager);
            adapternya = new ReportTabAdapter(getChildFragmentManager(), getActivity(), mList, titles);
            setTargetFragment(this, 0);
            pager.setAdapter(adapternya);
            pager.setPageMargin(pageMargin);
            tabs.setViewPager(pager);
            pager.setCurrentItem(0);

            setCurrentAdapternya(adapternya);
        }
    }

    public View getCurrentView() {
        return currentView;
    }

    public void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public ReportTabAdapter getCurrentAdapternya() {
        return currentAdapternya;
    }

    public void setCurrentAdapternya(ReportTabAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }

}