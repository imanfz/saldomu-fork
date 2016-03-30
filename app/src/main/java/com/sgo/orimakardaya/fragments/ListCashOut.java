package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.CashoutActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.dialogs.InformationDialog;

import java.util.ArrayList;
import java.util.Collections;

/*
  Created by Administrator on 11/5/2014.
 */
public class ListCashOut extends ListFragment implements InformationDialog.OnDialogOkCallback {



    View v;
    ArrayList<String> _listType;
    EasyAdapter adapter;
    private InformationDialog dialogI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_topup, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _listType = new ArrayList<>();
        Collections.addAll(_listType, getResources().getStringArray(R.array.list_cash_out));

        adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _listType);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

        dialogI = InformationDialog.newInstance(this,4);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;
        String itemName = String.valueOf(l.getAdapter().getItem(position));
        if(position == 0) {
            i = new Intent(getActivity(), CashoutActivity.class);
            i.putExtra(DefineValue.CASHOUT_TYPE,DefineValue.CASHOUT_BANK);
            switchActivity(i);
        }
        else if(position == 1) {
            i = new Intent(getActivity(), CashoutActivity.class);
            i.putExtra(DefineValue.CASHOUT_TYPE,DefineValue.CASHOUT_AGEN);
            switchActivity(i);
        }

    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onOkButton() {

    }
}