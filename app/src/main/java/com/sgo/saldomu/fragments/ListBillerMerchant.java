package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.NFCActivity;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.PrefixOperatorValidator;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import timber.log.Timber;

/*
  Created by Administrator on 3/2/2015.
 */
public class ListBillerMerchant extends ListFragment {

    public final static String TAG = "LIST_BILLER_MERCHANT";

    private View v;
    private String userID;
    private String accessKey;
    private String billerType;
    private String billerTypeCode, billerIdNumber;
    private List<Biller_Data_Model> mListBillerData;
    private Biller_Type_Data_Model mBillerType;
    private EasyAdapter adapter;
    private ArrayList<String> _data;
    private RealmChangeListener realmListener;
    private Realm realm;
    private NfcAdapter nfcAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.wtf("onCreate View ListBillerMerchant", "onCreate View ListBillerMerchant");
        v = inflater.inflate(R.layout.frag_list_biller_tab, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.wtf("onActivityCreated ListBillerMerchant", "onActivityCreated ListBillerMerchant");
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

//        realm = Realm.getInstance(RealmManager.BillerConfiguration);

//        _data = new ArrayList<>();
//        adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == mListBillerData.size() && nfcAdapter != null) {
                    Intent intent = new Intent(getActivity(), NFCActivity.class);
                    startActivity(intent);
                } else {
                    onListItemClick(listView1, view, position, id);
                }
            }
        });

        if (mBillerType != null)
            setActionBarTitle(getString(R.string.biller_ab_title) + " - " + mBillerType.getBiller_type_name());

//        if(!realm.isInTransaction())
//            initializeData();

//        realmListener = new RealmChangeListener() {
//            @Override
//            public void onChange(Object element) {
//                Timber.d("Masuk realm listener bilactive asdfasdfa");
//                if(isVisible()){
//                    initializeData();
//                }
//            }};
//        realm.addChangeListener(realmListener);

//        PrefixOperatorValidator.OperatorModel BillerIdNumber = PrefixOperatorValidator.validation(getActivity().getApplicationContext(),billerIdNumber);
//        for (int i=0; i<_data.size(); i++)
//        {
//            Timber.d("_data"+_data.get(i));
//            if (_data.get(i).toLowerCase().contains(BillerIdNumber.prefix_name.toLowerCase()))
//            {
//                onListItemClick(null, null, i, 0);
//            }
//        }
    }

    private void initializeData() {
        Bundle args = getArguments();
        billerTypeCode = args.getString(DefineValue.BILLER_TYPE, "");
        billerIdNumber = args.getString(DefineValue.BILLER_ID_NUMBER, "");

        mBillerType = realm.where(Biller_Type_Data_Model.class).
                equalTo(WebParams.BILLER_TYPE_CODE, billerTypeCode).
                findFirst();

        if (mBillerType != null) {
            mListBillerData = mBillerType.getBiller_data_models();
            setActionBarTitle(getString(R.string.biller_ab_title) + " - " + mBillerType.getBiller_type_name());
            _data.clear();

            for (int i = 0; i < mListBillerData.size(); i++) {
                _data.add(mListBillerData.get(i).getComm_name());
            }
            if (BuildConfig.FLAVOR.equalsIgnoreCase("production"))
                if (billerTypeCode.equals("EMON") && nfcAdapter != null) {
                    _data.add("Cek Saldo Emoney");
                }

            adapter.notifyDataSetChanged();
        } else
            mListBillerData = new ArrayList<>();


    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        changeToInputBiller(mListBillerData.get(position).getComm_id(),
                mListBillerData.get(position).getComm_name(),
                mListBillerData.get(position).getItem_id(),
                mListBillerData.get(position).getComm_code(),
                mBillerType.getBiller_type());
    }

    private void changeToInputBiller(String _comm_id, String _comm_name, String _item_id, String _comm_code, String _buy_type) {
//        if(_item_id.isEmpty())
//            callUpdateDenom(_comm_id, _comm_name);

        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.COMMUNITY_ID, _comm_id);
        mArgs.putString(DefineValue.COMMUNITY_NAME, _comm_name);
        mArgs.putString(DefineValue.BILLER_ITEM_ID, _item_id);
        mArgs.putString(DefineValue.BILLER_COMM_CODE, _comm_code);
        mArgs.putString(DefineValue.BILLER_TYPE, billerTypeCode);
        mArgs.putString(DefineValue.BILLER_ID_NUMBER, billerIdNumber);
        mArgs.putString(DefineValue.BUY_TYPE, _buy_type);

        BillerInput mBI = new BillerInput();
        mBI.setArguments(mArgs);

        String fragname = mBillerType.getBiller_type_name() + "-" + _comm_name;
        if (_comm_name.contains("Emoney Mandiri")) {
            fragname = _comm_name;
        }


        switchFragment(mBI, BillerActivity.FRAG_BIL_LIST_MERCHANT, fragname, true, BillerInput.TAG);
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, String next_name, Boolean isBackstack, String tag) {
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

//    private void callUpdateDenom(String comm_id, String comm_name){
//        if (getActivity() == null)
//            return;
//
//        BillerActivity fca = (BillerActivity) getActivity();
//        fca.updateDenom(comm_id,comm_name);
//    }

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
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        _data = new ArrayList<>();
        adapter = new EasyAdapter(getActivity(), R.layout.list_view_item_with_arrow, _data);

        if (!realm.isInTransaction())
            initializeData();

        realmListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {
                Timber.d("Masuk realm listener bilactive asdfasdfa");
                if (isVisible()) {
                    initializeData();
                }
            }
        };
        realm.addChangeListener(realmListener);

//        if (billerIdNumber != null && !billerIdNumber.equals("")) {
//            PrefixOperatorValidator.OperatorModel BillerIdNumber = PrefixOperatorValidator.validation(getActivity(), billerIdNumber);
//            Log.wtf("billeridnumber", "billeridnumber");
//            if (BillerIdNumber != null) {
//                for (int i = 0; i < _data.size(); i++) {
//                    Timber.d("_data" + _data.get(i));
//                    if (_data != null) {
//                        Timber.d("prefix name = " + BillerIdNumber.prefix_name);
//                        if (_data.get(i).toLowerCase().contains(BillerIdNumber.prefix_name.toLowerCase())) {
//                            changeToInputBiller(mListBillerData.get(i).getComm_id(),
//                                    mListBillerData.get(i).getComm_name(),
//                                    mListBillerData.get(i).getItem_id());
//                        }
//                    }
//
//                }
//            }
//        }

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