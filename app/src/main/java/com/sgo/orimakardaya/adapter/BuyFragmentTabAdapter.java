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
                                 HashMap<String,String> _payment, JSONArray _collection) {
        super(fm);
        this.mContext =  context;
//        _collection = new JSONArray();
        Timber.d("collection length", Integer.toString(_collection.length()));
        if(_collection.length() > 0) {
            TITLES = context.getResources().getStringArray(R.array.buy_vpi_title);
        }
        else {
            TITLES = context.getResources().getStringArray(R.array.buy_vpi_title_1);
        }
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
        switch (i){
            case 0: return TabBuyItem.newInstance(mPurchase);
            case 1: return TabBuyItem.newInstance(mPayment);
            case 2: return ListCollection.newInstance(mCollection);

        }
        return null;
    }

    @Override
    public int getCount() {
        return ITEMS;
    }
}
