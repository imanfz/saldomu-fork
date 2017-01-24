package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.EvoucherHPActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.DefineValue;

/*
  Created by Administrator on 1/30/2015.
 */
public class ListEvoucherHP extends ListFragment {

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_evoucher_hp, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] _data = getResources().getStringArray(R.array.list_e_voucher);

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle mBun;
        Fragment f;
        switch (position) {
            case 0:
                f = new BuyEVoucherHPInput();
                mBun = new Bundle();
                mBun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.INTERNET_BANKING);
                f.setArguments(mBun);
                switchFragment(f, getString(R.string.listevoucherhp_ab_title_ib), true);
                break;
            case 1:
                f = new BuyEVoucherHPInput();
                mBun = new Bundle();
                mBun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.SMS_BANKING);
                f.setArguments(mBun);
                switchFragment(f, getString(R.string.listevoucherhp_ab_title_sb), true);
                break;
        }
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        EvoucherHPActivity fca = (EvoucherHPActivity) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        EvoucherHPActivity fca = (EvoucherHPActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        EvoucherHPActivity fca = (EvoucherHPActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        setActionBarTitle(getString(R.string.evoucherhp_ab_title));
        super.onResume();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}