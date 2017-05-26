package com.sgo.hpku.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sgo.hpku.fragments.AgentHistoryFragment;
import com.sgo.hpku.fragments.AgentListFrameFragment;
import com.sgo.hpku.fragments.AgentMapFragment;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class TabAgentPagerAdapter extends FragmentPagerAdapter
{
    private String tabTitles[] = new String[] {"List", "Map", "History"};

    public TabAgentPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public int getCount()
    {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch(position)
        {
            case 0:
                AgentListFrameFragment agentListFrameFragment = new AgentListFrameFragment();
                return agentListFrameFragment;

            case 1:
                AgentMapFragment agentMapFragment = new AgentMapFragment();
                return agentMapFragment;

            case 2:
                AgentHistoryFragment agentHistoryFragment = new AgentHistoryFragment();
                return agentHistoryFragment;

            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
