package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.coreclass.DefineValue;

/**
 * Created by thinkpad on 5/4/2017.
 */

public class BBSTransaksiPagerItem extends Fragment {
    public final static String TAG = "com.sgo.hpku.fragments.BBSTransaksiPagerItem";

    private View v, layout;
    private String title;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v =  inflater.inflate(R.layout.bbs_transaksi_pager_item, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layout = v.findViewById(R.id.bbsTransaksiFragmentContent);

        Bundle bundle = getArguments();
        if(bundle != null) {
            title = bundle.getString(DefineValue.TRANSACTION,"");
        }

        Fragment newFrag = new BBSTransaksiAmount();
        Bundle args = new Bundle();
        args.putString(DefineValue.TRANSACTION, title);
        newFrag.setArguments(args);
        getChildFragmentManager().beginTransaction().add(R.id.bbsTransaksiFragmentContent , newFrag, BBSTransaksiAmount.TAG).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(title.equalsIgnoreCase(getString(R.string.cash_out)))
            inflater.inflate(R.menu.bbs_reg_acct, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;

            case R.id.action_reg_acct:
                Fragment mFrag = new ListAccountBBS();
                switchFragment(mFrag, ListAccountBBS.TAG, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity ) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    public Fragment getChildFragment(){
        return getChildFragmentManager().findFragmentById(R.id.bbsTransaksiFragmentContent);
    }
}
