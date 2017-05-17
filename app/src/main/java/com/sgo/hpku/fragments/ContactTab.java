package com.sgo.hpku.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.adapter.ContactTabAdapter;
import com.sgo.hpku.dialogs.InformationDialog;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Created by thinkpad on 1/14/2016.
 */
public class ContactTab extends Fragment {

    private ContactTabAdapter currentAdapternya;
    SecurePreferences sp;
    private View currentView;
    private InformationDialog dialogI;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                    .getDisplayMetrics());
            ContactTabAdapter adapternya;
            TabPageIndicator tabs;

            dialogI = InformationDialog.newInstance(this,12);

            ViewPager pager;
            String[] titles = getActivity().getResources().getStringArray(R.array.contact_tab_list);

            tabs = (TabPageIndicator) getCurrentView().findViewById(R.id.contact_tabs);
            pager = (ViewPager) getCurrentView().findViewById(R.id.contact_pager);
            adapternya = new ContactTabAdapter(getChildFragmentManager(), getActivity(), titles);
            setTargetFragment(this, 0);
            pager.setAdapter(adapternya);
            pager.setPageMargin(pageMargin);
            tabs.setViewPager(pager);
            pager.setCurrentItem(0);

            setCurrentAdapternya(adapternya);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.frag_contact_tab, container, false);
        setCurrentView(v);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

}
