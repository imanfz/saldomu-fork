package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.coreclass.DefineValue;

/**
 * Created by thinkpad on 5/4/2017.
 */

public class BBSTransaksiPagerItem extends Fragment {

    private View v;
    private String title;
    private boolean isShowRegAccountMenu = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.bbs_transaksi_pager_item, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        String type = "", defaultAmount = "", noHpPengirim = "", defaultProductCode = "";
        if (bundle != null) {
            title = bundle.getString(DefineValue.TRANSACTION, "");
            isShowRegAccountMenu = false;
            if (bundle.containsKey(DefineValue.TYPE)) {
                type = bundle.getString(DefineValue.TYPE);
            }
            if (bundle.containsKey(DefineValue.AMOUNT)) {
                defaultAmount = bundle.getString(DefineValue.AMOUNT);
            }
            if (bundle.containsKey(DefineValue.KEY_CODE)) {
                noHpPengirim = bundle.getString(DefineValue.KEY_CODE);
            }
            if (bundle.containsKey(DefineValue.PRODUCT_CODE)) {
                defaultProductCode = bundle.getString(DefineValue.PRODUCT_CODE);
            }
        }
        Fragment newFrag = new Fragment();
        if (title.equalsIgnoreCase(getString(R.string.cash_in)) && type.equalsIgnoreCase(DefineValue.BBS_CASHIN))
            newFrag = new BBSCashIn();
        else if (title.equalsIgnoreCase(getString(R.string.cash_out)) && type.equalsIgnoreCase(DefineValue.BBS_CASHOUT))
            newFrag = new BBSCashOut();
        Bundle args = new Bundle();
        args.putString(DefineValue.TRANSACTION, title);
        args.putString(DefineValue.TYPE, type);
        args.putString(DefineValue.AMOUNT, defaultAmount);
        args.putString(DefineValue.KEY_CODE, noHpPengirim);
        args.putString(DefineValue.FAVORITE_CUSTOMER_ID, bundle.getString(DefineValue.FAVORITE_CUSTOMER_ID, ""));
        if (defaultProductCode != null)
            args.putString(DefineValue.PRODUCT_CODE, defaultProductCode);
        newFrag.setArguments(args);
        getChildFragmentManager().beginTransaction().add(R.id.bbsTransaksiFragmentContent, newFrag, "").commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (title.equalsIgnoreCase(getString(R.string.cash_out))) {
            if (isShowRegAccountMenu)
                inflater.inflate(R.menu.bbs_reg_acct, menu);
        }
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

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        BBSActivity fca = (BBSActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    public Fragment getChildFragment() {
        return getChildFragmentManager().findFragmentById(R.id.bbsTransaksiFragmentContent);
    }
}
