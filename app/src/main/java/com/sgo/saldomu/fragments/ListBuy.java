package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Account_Collection_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.adapter.BuyFragmentTabAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;

/*
  Created by Administrator on 1/30/2015.
 */
public class ListBuy extends Fragment {

    private View v;
    private View layout_empty;
    private TabPageIndicator tabs;
    private ViewPager pager;
    private BuyFragmentTabAdapter adapternya;
    private ProgressDialog out;
    private ListBuyRF mWorkFragment;
    private RealmChangeListener realmListener;
    private ArrayList<String> Title_tab;
    private SecurePreferences sp;
//    String userID,accessKey;

    private InformationDialog dialogI;

    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
//        v = inflater.inflate(R.layout.frag_list_buy, container, false);
        v = inflater.inflate(R.layout.activity_list_buy, container, false);

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        super.onActivityCreated(savedInstanceState);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());

        tabs = v.findViewById(R.id.buy_tabs);
        pager = v.findViewById(R.id.buy_pager);
        layout_empty = v.findViewById(R.id.empty_layout);

        layout_empty.setVisibility(View.GONE);
        Button btn_refresh = layout_empty.findViewById(R.id.btnRefresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getDataBiller();
            }
        });

        pager.setPageMargin(pageMargin);
        dialogI = InformationDialog.newInstance(8);
        dialogI.setTargetFragment(this,0);
        realm = Realm.getInstance(RealmManager.BillerConfiguration);

//        // auto updater realm biller
//        FragmentManager fm = getFragmentManager();
//        // Check to see if we have retained the worker fragment.
//        mWorkFragment = (ListBuyRF) fm.findFragmentByTag(ListBuyRF.LISTBUYRF_TAG);
//        // If not retained (or first time running), we need to create it.
//        if (mWorkFragment == null) {
//            mWorkFragment = new ListBuyRF();
//            // Tell it who it is working with.
//            mWorkFragment.setTargetFragment(this, 0);
//            fm.beginTransaction().add(mWorkFragment, ListBuyRF.LISTBUYRF_TAG).commit();
//        }
//        else
//            mWorkFragment.getDataBiller();
//
//        realmListener = new RealmChangeListener() {
//            @Override
//            public void onChange() {
//                if(isVisible()){
////                    Timber.d("masukk realm listener gannnn");
//                }
//            }};
//        realm.addChangeListener(realmListener);

        initializeData();

    }



    private void initializeData(){

        List<Biller_Type_Data_Model> mListBillerTypeData = realm.where(Biller_Type_Data_Model.class).findAll();
        List<Account_Collection_Model> mListACL = realm.where(Account_Collection_Model.class).findAll();
        Title_tab = new ArrayList<>();
        Boolean isBuy = false;
        Boolean isPay = false;

        if(mListBillerTypeData.size() > 0) {
            for (int i = 0; i < mListBillerTypeData.size(); i++) {
                if (mListBillerTypeData.get(i).getBiller_type().equals(DefineValue.BIL_TYPE_BUY)) {
                    isBuy = true;
                } else {
                    isPay = true;
                }
            }

            if (isBuy)
                Title_tab.add(getString(R.string.purchase));
            if (isPay)
                Title_tab.add(getString(R.string.payment));
        }


        if(mListACL.size() > 0) {
            Title_tab.add(getString(R.string.collection));
        }

        if(Title_tab.isEmpty())
            layout_empty.setVisibility(View.VISIBLE);
        else
            layout_empty.setVisibility(View.GONE);

        adapternya = new BuyFragmentTabAdapter(getChildFragmentManager(),getActivity(),Title_tab);
        pager.setAdapter(adapternya);
        tabs.setViewPager(pager);
        if(out != null && out.isShowing())
            out.dismiss();
        pager.setVisibility(View.VISIBLE);
        tabs.setVisibility(View.VISIBLE);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        setTargetFragment(null, -1);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        setTargetFragment(null, -1);
//        cleanupRetainInstanceFragment();
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed()) {
//            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }

    public void cleanupRetainInstanceFragment() {
        if(!getActivity().isFinishing()) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(this.mWorkFragment).commitAllowingStateLoss();
        }
    }
}