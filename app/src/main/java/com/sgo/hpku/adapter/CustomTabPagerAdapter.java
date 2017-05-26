package com.sgo.hpku.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.sgo.hpku.fragments.FragKelolaAgent;
import com.sgo.hpku.fragments.FragKelolaMerchant;
import com.sgo.hpku.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 29/03/2017.
 */

public class CustomTabPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] {"Merchant", "Agent"};
    private Context context;
    private String[] menuItems;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();

    public CustomTabPagerAdapter(android.support.v4.app.FragmentManager fm, Context context, String[] menuItems, ArrayList<ShopDetail> shopDetails) {
        super(fm);
        this.context        = context;
        this.menuItems      = menuItems;
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
            fragment = new FragKelolaMerchant(this.shopDetails);
        } else {
            fragment = new FragKelolaAgent(this.shopDetails);

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
