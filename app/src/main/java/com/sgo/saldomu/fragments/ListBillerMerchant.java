package com.sgo.saldomu.fragments;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.adapter.EasyAdapterFilterable;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.models.BillerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import timber.log.Timber;

//import com.sgo.saldomu.activities.NFCActivity;

/*
  Created by Administrator on 3/2/2015.
 */
public class ListBillerMerchant extends ListFragment {

    private View v;
    private String billerTypeCode, billerIdNumber;
    private String billerMerchantName;
    private List<BillerItem> billerData;
    private EasyAdapterFilterable adapter;
    private ArrayList<String> _data;
    private RealmChangeListener realmListener;
    private Realm realm;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.tag("onCreate View ListBille").wtf("onCreate View ListBillerMerchant");
        v = inflater.inflate(R.layout.frag_list_biller_tab, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.tag("onActivityCreated ListB").wtf("onActivityCreated ListBillerMerchant");
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();

        Bundle args = getArguments();
        billerTypeCode = args.getString(DefineValue.BILLER_TYPE, "");
        billerIdNumber = args.getString(DefineValue.BILLER_ID_NUMBER, "");
        billerMerchantName = args.getString(DefineValue.BILLER_NAME, "");

        ListView listView1 = v.findViewById(android.R.id.list);
        AutoCompleteTextView searchBar = v.findViewById(R.id.search);

        listView1.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.getFilter().filter(editable.toString());
            }
        });
        setActionBarTitle(getString(R.string.biller_ab_title) + " - " + billerMerchantName);
    }

    private void initializeData() {
        billerData = realm.where(BillerItem.class).findAll();

        if (billerData != null) {
            _data.clear();

            for (int i = 0; i < billerData.size(); i++) {
                _data.add(billerData.get(i).getCommName());
            }

            adapter.notifyDataSetChanged();
        }
    }

    private void changeToInputBiller(String comm_id, String comm_name, String item_id, String comm_code, String api_key) {
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.COMMUNITY_NAME, comm_name);
        mArgs.putString(DefineValue.BILLER_ITEM_ID, item_id);
        mArgs.putString(DefineValue.BILLER_COMM_CODE, comm_code);
        mArgs.putString(DefineValue.BILLER_API_KEY, api_key);
        mArgs.putString(DefineValue.BILLER_TYPE, billerTypeCode);
        mArgs.putString(DefineValue.BILLER_ID_NUMBER, billerIdNumber);

        Fragment billerInput;
        String fragName;

        fragName = getString(R.string.biller_ab_title) + " - " + comm_name;
        billerInput = new BillerInput();

        billerInput.setArguments(mArgs);
        switchFragment(billerInput, BillerActivity.FRAG_BIL_LIST_MERCHANT, fragName, true, BillerInput.TAG);
    }

    private void switchFragment(Fragment i, String name, String next_name, Boolean isBackstack, String tag) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchContent(i, name, next_name, isBackstack, tag);
    }

    private void setActionBarTitle(String _title) {
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        realm = Realm.getInstance(RealmManager.realmConfiguration);
        _data = new ArrayList<>();
        adapter = new EasyAdapterFilterable(getActivity(), R.layout.list_view_item_with_arrow, _data, item -> {
            for (int i = 0; i < _data.size(); i++) {
                if (_data.get(i).equalsIgnoreCase(item)){
                    BillerItem billerItem = billerData.get(i);
                    changeToInputBiller(billerItem.getCommId(),
                            billerItem.getCommName(),
                            billerItem.getItemId(),
                            billerItem.getCommCode(),
                            billerItem.getApiKey());
                }
            }
        });

        if (!realm.isInTransaction())
            initializeData();

        realmListener = element -> {
            Timber.d("Masuk realm listener bilactive asdfasdfa");
            if (isVisible()) {
                initializeData();
            }
        };
        realm.addChangeListener(realmListener);

    }

    @Override
    public void onDestroy() {
        if (!realm.isInTransaction() && !realm.isClosed()) {
            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }
}