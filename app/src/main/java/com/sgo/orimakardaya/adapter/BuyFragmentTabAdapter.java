package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 3/3/2015.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.fragments.ListCollection;
import com.sgo.orimakardaya.fragments.TabBuyItem;
import org.json.JSONArray;

import java.util.HashMap;

import timber.log.Timber;

public class BuyFragmentTabAdapter extends FragmentStatePagerAdapter {

    private final String[] TITLES;
    private int ITEMS;
    private Context mContext;
    private HashMap<String,String> mPurchase,mPayment;
    private JSONArray mCollection;


    public BuyFragmentTabAdapter(FragmentManager fm, Context context, HashMap<String,String> _purchase,
                                 HashMap<String,String> _payment, JSONArray _collection,
                                 ArrayList<String> _title_tab) {
        super(fm);
        this.mContext =  context;

        TITLES = new String[_title_tab.size()];
        _title_tab.toArray(TITLES);

        ITEMS = TITLES.length;
        this.mPurchase = _purchase;
        this.mPayment = _payment;
        this.mCollection = _collection;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public Fragment getItem(int i) {

        if(TITLES[i].equals(mContext.getString(R.string.purchase)))
            return TabBuyItem.newInstance(mPurchase);
        else if(TITLES[i].equals(mContext.getString(R.string.payment)))
            return TabBuyItem.newInstance(mPayment);
        else if(TITLES[i].equals(mContext.getString(R.string.collection)))
            return ListCollection.newInstance(mCollection);

        return null;
    }

    @Override
    public int getCount() {
        return ITEMS;
    }
}
