package com.sgo.hpku.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsSearchAgentActivity;
import com.sgo.hpku.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentListFrameFragment extends Fragment {
    View rootView;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    AgentListFragment agentListFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
        {
            rootView = inflater.inflate(R.layout.agent_list_frame_fragment, container, false);

            agentListFragment = new AgentListFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.listContent, agentListFragment).commit();
        }

        return rootView;
    }

    public void updateView(ArrayList<ShopDetail> shopDetails)
    {
        this.shopDetails.clear();
        this.shopDetails.addAll(shopDetails);

        agentListFragment.updateView(this.shopDetails);
    }


}
