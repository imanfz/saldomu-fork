package com.sgo.saldomu.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Tutorial_page#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tutorial_page extends Fragment {
    private int imageId;
    public Tutorial_page() {
        // Required empty public constructor
    }

    public static Tutorial_page newInstance(int imageId) {
        Tutorial_page fragment = new Tutorial_page();
        Bundle args = new Bundle();
        args.putInt(DefineValue.TUTORIAL_IMAGE, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey(DefineValue.TUTORIAL_IMAGE))
            imageId = getArguments().getInt(DefineValue.TUTORIAL_IMAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.tutorial_page, container, false);
        ImageView tutorialImage = (ImageView) v.findViewById(R.id.image_tutorial);
        tutorialImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(),imageId,null));
        return v;
    }

}
