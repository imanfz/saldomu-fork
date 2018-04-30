package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ExpandListATMAdapter;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
  Created by Administrator on 12/18/2014.
 */
public class ListATM extends BaseActivity {

    private String[] _namaBank;

    private List<String> listDataHeader;
    private HashMap<String, String> listDataChild;
    private SecurePreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        _namaBank = getResources().getStringArray(R.array.listatm_namabank_list);

        // get the listview
        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.expand_list_atm);
        // preparing list data
        prepareListData();

        ExpandListATMAdapter listAdapter = new ExpandListATMAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listDataHeader.addAll(Arrays.asList(_namaBank));

        for (String aListDataHeader : listDataHeader) {
            listDataChild.put(aListDataHeader, sp.getString(DefineValue.CUST_ID, "")); // Header, Child data
        }

    }

    private void InitializeToolbar(){
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