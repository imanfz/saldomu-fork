package com.sgo.hpku.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sgo.hpku.fragments.AgentHistoryFragment;
import com.sgo.hpku.fragments.AgentListFragment;
import com.sgo.hpku.fragments.AgentListFrameFragment;
import com.sgo.hpku.fragments.AgentMapFragment;
import com.sgo.hpku.fragments.FragKelolaAgent;
import com.sgo.hpku.fragments.FragKelolaMerchant;
import com.sgo.hpku.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 29/03/2017.
 */

public class TabSearchAgentAdapter extends FragmentPagerAdapter {
    private Context context;
    private String[] menuItems;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private FragmentManager fm;
    private Double currentLatitude;
    private Double currentLongitude;
    private String mobility;

    public TabSearchAgentAdapter(android.support.v4.app.FragmentManager fm, Context context, String[] menuItems, ArrayList<ShopDetail> shopDetails,
                                 Double currentLatitude, Double currentLongitude, String mobility) {
        super(fm);
        this.fm = fm;
        this.context = context;
        this.menuItems = menuItems;
        this.shopDetails = shopDetails;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.mobility = mobility;
    }

    @Override
    public int getCount() {
        return menuItems.length;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch(position)
        {
            case 0:
                fragment = new AgentMapFragment(currentLatitude, currentLongitude, mobility);
                break;
            case 1:
                fragment = new AgentListFragment(mobility);
                break;
            default:
                fragment = null;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return menuItems[position];
    }

    private FragmentManager getFragment()
    {
        return this.fm;
    }

    @Override
    public int getItemPosition(Object object) {
        shopDetails.toString();
        if ( object instanceof AgentMapFragment ) {
            ((AgentMapFragment) object).updateView(shopDetails);
        } else if ( object instanceof AgentListFragment) {
            ((AgentListFragment) object).updateView(shopDetails);
        }
        return super.getItemPosition(object);
    }
}
