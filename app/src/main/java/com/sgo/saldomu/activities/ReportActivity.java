package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;

import androidx.fragment.app.ListFragment;
import androidx.viewpager.widget.ViewPager;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ReportTabAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.fragments.FragReport;
import com.sgo.saldomu.widgets.BaseActivity;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yessica on 12/6/2017.
 */

public class ReportActivity extends BaseActivity {

    private ReportTabAdapter currentAdapternya;
    private View currentView;
    private InformationDialog dialogI;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_report;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);

        if (savedInstanceState == null) {
            final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                    .getDisplayMetrics());
            ReportTabAdapter adapternya;
            TabPageIndicator tabs;
            ViewPager pager;
            String[] titles;

            if (isAgent ){
                titles = this.getResources().getStringArray(R.array.report_list_agen);
            }else
                titles = this.getResources().getStringArray(R.array.report_list);

            dialogI = InformationDialog.newInstance(10);
//            dialogI.setTargetFragment(this,0);
            List<ListFragment> mList = new ArrayList<>();
//            mList.add(FragReport.newInstance(FragReport.REPORT_PENDING));
            mList.add(FragReport.newInstance(FragReport.REPORT_ESPAY));
            if (isAgent) {
                mList.add(FragReport.newInstance(FragReport.REPORT_FEE));
                mList.add(FragReport.newInstance(FragReport.REPORT_ADDITIONAL_FEE));
            }

            tabs = findViewById(R.id.report_tabs_activity);
            pager = findViewById(R.id.report_pager_activity);
            adapternya = new ReportTabAdapter(getSupportFragmentManager(), mList, titles);
//            setTargetFragment(this, 0);
            pager.setAdapter(adapternya);
            pager.setPageMargin(pageMargin);
            tabs.setViewPager(pager);
            pager.setCurrentItem(0);

            setCurrentAdapternya(adapternya);
        }
    }

    private void setCurrentAdapternya(ReportTabAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }




    public void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_report));
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(this.getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            case android.R.id.home :
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
