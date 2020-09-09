package com.sgo.saldomu.adapter;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sgo.saldomu.fragments.AgentListFragment;
import com.sgo.saldomu.fragments.AgentMapFragment;
import com.sgo.saldomu.models.ShopDetail;

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
    private String mobility, completeAddress;
    private AgentListFragment.OnListAgentItemClick mOnListAgentItemClickMap;

    public TabSearchAgentAdapter(FragmentManager fm, Context context, String[] menuItems, ArrayList<ShopDetail> shopDetails,
                                 Double currentLatitude, Double currentLongitude, String mobility, String completeAddress) {
        super(fm);
        this.fm = fm;
        this.context = context;
        this.menuItems = menuItems;
        this.shopDetails = shopDetails;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.mobility = mobility;
        this.completeAddress = completeAddress;
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
                Bundle args = new Bundle();
                if ( currentLatitude != null )
                    args.putDouble("currentLatitude", currentLatitude);

                if ( currentLongitude != null )
                    args.putDouble("currentLongitude", currentLongitude);

                args.putString("mobility", mobility);
                args.putString("completeAddress", completeAddress);
                fragment = new AgentMapFragment();
                fragment.setArguments(args);
                try {
                    mOnListAgentItemClickMap = (AgentListFragment.OnListAgentItemClick) fragment;
                } catch(ClassCastException e) {

                }
                break;
            case 1:
                Bundle args2 = new Bundle();
                args2.putString("mobility", mobility);
                fragment = new AgentListFragment();
                fragment.setArguments(args2);

                break;
            default:
                fragment = null;
        }
        return fragment;
    }

    public void OnLocationClickListener(int position, ArrayList<ShopDetail> shopDetails) {
        mOnListAgentItemClickMap.OnIconLocationClickListener(position, shopDetails);
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
        if ( object instanceof AgentMapFragment ) {
            ((AgentMapFragment) object).updateView(shopDetails);
        } else if ( object instanceof AgentListFragment) {
            ((AgentListFragment) object).updateView(shopDetails);
        }
        return super.getItemPosition(object);
    }
}
