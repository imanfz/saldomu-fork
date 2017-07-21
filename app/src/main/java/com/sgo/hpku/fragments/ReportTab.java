package com.sgo.hpku.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.TutorialActivity;
import com.sgo.hpku.adapter.ReportTabAdapter;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.dialogs.InformationDialog;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/*
  Created by Administrator on 7/7/2015.
 */
public class ReportTab extends Fragment {

    private ReportTabAdapter currentAdapternya;
    SecurePreferences sp;
    private View currentView;
    private InformationDialog dialogI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.frag_report_tab, container, false);
        setCurrentView(v);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
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

            dialogI = InformationDialog.newInstance(10);
            dialogI.setTargetFragment(this,0);
            List<ListFragment> mList = new ArrayList<>();
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
            validasiTutorial();
        }
    }

    private void validasiTutorial()
    {
        if(sp.contains(DefineValue.TUTORIAL_REPORT))
        {
            Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_REPORT,false);
            if(is_first_time) {
                showTutorial();
            }
        }
        else {
            showTutorial();
        }
    }

    private void showTutorial()
    {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_report);
        startActivity(intent);
    }

    private View getCurrentView() {
        return currentView;
    }

    private void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public ReportTabAdapter getCurrentAdapternya() {
        return currentAdapternya;
    }

    private void setCurrentAdapternya(ReportTabAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                showTutorial();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}