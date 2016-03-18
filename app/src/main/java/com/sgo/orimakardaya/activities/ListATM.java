package com.sgo.orimakardaya.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.adapter.ExpandListATMAdapter;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
  Created by Administrator on 12/18/2014.
 */
public class ListATM extends BaseActivity {

    private String[] _namaBank;

    ExpandListATMAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, String> listDataChild;
    SecurePreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        _namaBank = getResources().getStringArray(R.array.listatm_namabank_list);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expand_list_atm);
        // preparing list data
        prepareListData();

        listAdapter = new ExpandListATMAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, String>();

        listDataHeader.addAll(Arrays.asList(_namaBank));

        for (String aListDataHeader : listDataHeader) {
            listDataChild.put(aListDataHeader, sp.getString(DefineValue.CUST_ID, "")); // Header, Child data
        }

    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.atm));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(MainPage.RESULT_NORMAL);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_atm;
    }
}