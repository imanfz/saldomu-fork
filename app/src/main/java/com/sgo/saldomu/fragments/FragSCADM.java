package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.sgo.saldomu.R;

/**
 * Created by Lenovo Thinkpad on 5/4/2018.
 */

public class FragSCADM extends Fragment {

    GridView GridSCADM;
    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_scadm, container, false);
        GridSCADM=(GridView)v.findViewById(R.id.grid);
        return v;
    }


}
