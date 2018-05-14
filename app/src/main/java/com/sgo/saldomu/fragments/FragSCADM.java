package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.ListJoinCommunitySCADM;
import com.sgo.saldomu.adapter.GridMenuSCADM;

import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/4/2018.
 */

public class FragSCADM extends Fragment {

    GridView GridSCADM;
    View v;
    SecurePreferences sp;
    Intent intent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_scadm, container, false);
        GridSCADM = (GridView)v.findViewById(R.id.grid_scadm);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GridMenuSCADM adapter = new GridMenuSCADM(getActivity(), SetupListMenu(), SetupListMenuIcons());
        GridSCADM.setAdapter(adapter);

        GridSCADM.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Timber.d("masuk gridhomeonitemclicklistener");

                String menuItemName = ((TextView) view.findViewById(R.id.grid_text)).getText().toString();

                if (menuItemName.equalsIgnoreCase("Join"))
                {
                    intent = new Intent(getActivity(), ListJoinCommunitySCADM.class);
                    startActivity(intent);
                }else  if (menuItemName.equalsIgnoreCase("Top Up"))
                {

                }else
                {

                }
            }
        });
    }

    private ArrayList<String> SetupListMenu(){
        String[] _data;
        ArrayList<String> data = new ArrayList<>() ;
        _data = getResources().getStringArray(R.array.list_menu_scadm);
        Collections.addAll(data,_data);
        return data;
    }

    private int[] SetupListMenuIcons(){

        int totalIdx            = 0;
        int overallIdx          = 0;
        TypedArray menu           = getResources().obtainTypedArray(R.array.list_menu_icon_frag_scadm);

        totalIdx                = menu.length();

        int[] data        = new int[totalIdx];

        for( int j = 0; j < menu.length(); j++) {
            data[overallIdx] = menu.getResourceId(j, -1);
            overallIdx++;
        }

        return data;
    }

}
