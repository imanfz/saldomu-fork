package com.sgo.saldomu.activities;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.sgo.saldomu.Beans.ListMyProfile_model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ExpandableListProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyProfileActivityNew extends AppCompatActivity {

    ExpandableListView expandableListview;

    private List<String> mListDataHeader;
    private HashMap<String, ListMyProfile_model> mListDataChild;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_new);

        expandableListview = (ExpandableListView) findViewById(R.id.expandableListProfile);



        mListDataHeader= new ArrayList<>();
        mListDataChild = new HashMap<>();
        mListDataHeader.add("Data Member Basic");
        mListDataHeader.add("Data Verified Member");

        for (String header: mListDataHeader) {
            if (header.equals("Data Member Basic"))
            {
                mListDataChild.put(header, new ListMyProfile_model("","","", "", true));
            } else {
                mListDataChild.put(header, new ListMyProfile_model("","","","",false));
            }

        }

        ExpandableListProfile adapter= new ExpandableListProfile(this, mListDataHeader, mListDataChild);
        expandableListview.setAdapter(adapter);
    }
}
