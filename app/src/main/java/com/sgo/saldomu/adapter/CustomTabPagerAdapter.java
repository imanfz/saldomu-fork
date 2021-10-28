package com.sgo.saldomu.adapter;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sgo.saldomu.fragments.FragKelolaAgent;
import com.sgo.saldomu.fragments.FragKelolaMerchant;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 29/03/2017.
 */

public class CustomTabPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] {"Merchant", "Agent"};
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();

    public CustomTabPagerAdapter(FragmentManager fm, ArrayList<ShopDetail> shopDetails) {
        super(fm);
        this.shopDetails    = shopDetails;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if ( position == 0 ) {
            Bundle args = new Bundle();
            args.putSerializable("shopDetails", this.shopDetails);
            fragment = new FragKelolaMerchant();
            fragment.setArguments(args);
        } else {
            Bundle args2 = new Bundle();
            args2.putSerializable("shopDetails", this.shopDetails);
            fragment = new FragKelolaAgent();
            fragment.setArguments(args2);
        }
        return fragment;
        //return FragCustomTab.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE;
    }
}
