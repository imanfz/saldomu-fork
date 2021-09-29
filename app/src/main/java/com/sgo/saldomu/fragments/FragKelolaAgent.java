package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.GridViewIconAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragKelolaAgent extends Fragment implements View.OnClickListener {

    ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    ArrayList<ShopDetail> tempDetails = new ArrayList<>();
    private GridViewIconAdapter gridViewIconAdapter;
    GridView gvIconSetting;

    public FragKelolaAgent() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.shopDetails = (ArrayList<ShopDetail>) args.getSerializable("shopDetails");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_kelola_agent, container, false);

        if (shopDetails.size() > 0) {
            for (int i = 0; i < shopDetails.size(); i++) {
                if (shopDetails.get(i).getMemberType().equals(DefineValue.SHOP_AGENT)) {
                    tempDetails.add(shopDetails.get(i));
                }
            }

        }

        //bbsMemberListAdapter = new BbsMemberListAdapter(getActivity(), tempDetails);
        gridViewIconAdapter = new GridViewIconAdapter(getActivity(), tempDetails, getChildFragmentManager());

        gvIconSetting = (GridView) v.findViewById(R.id.gvIconSetting);
        gvIconSetting.setAdapter(gridViewIconAdapter);

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onClick(View v) {

    }
}
