package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ContactTabAdapter;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.widgets.BaseActivity;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Created by Lenovo Thinkpad on 12/12/2017.
 */

public class ContactActivity extends BaseActivity {
    private ContactTabAdapter currentAdapternya;
    SecurePreferences sp;
    private View currentView;
    private InformationDialog dialogI;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_tab;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();
        if (savedInstanceState == null) {
            final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                    .getDisplayMetrics());
            ContactTabAdapter adapternya;
            TabPageIndicator tabs;

            dialogI = InformationDialog.newInstance(12);
//            dialogI.setTargetFragment(this,0);

            ViewPager pager;
            String[] titles = this.getResources().getStringArray(R.array.contact_tab_list);

            tabs = findViewById(R.id.contact_tabs);
            pager = findViewById(R.id.contact_pager);
            adapternya = new ContactTabAdapter(getSupportFragmentManager(), this, titles);
//            setTargetFragment(this, 0);
            pager.setAdapter(adapternya);
            pager.setPageMargin(pageMargin);
            tabs.setViewPager(pager);
            pager.setCurrentItem(0);

            setCurrentAdapternya(adapternya);
        }
    }

    private View getCurrentView() {
        return currentView;
    }

    private void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public ContactTabAdapter getCurrentAdapternya() {
        return currentAdapternya;
    }

    private void setCurrentAdapternya(ContactTabAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
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

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_group_title_supports));
    }
}
