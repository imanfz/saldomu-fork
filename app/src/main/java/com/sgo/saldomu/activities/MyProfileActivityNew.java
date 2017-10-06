package com.sgo.saldomu.activities;

import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ExpandableListView;

import com.sgo.saldomu.Beans.ListMyProfile_model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ExpandableListProfile;
import com.sgo.saldomu.fragments.ListMyProfile;
import com.sgo.saldomu.interfaces.OnDateChooseListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static java.util.Calendar.DATE;

public class MyProfileActivityNew extends AppCompatActivity implements ExpandableListProfile.onClick {

    ExpandableListView expandableListview;

    private List<String> mListDataHeader;
    private HashMap<String, List<ListMyProfile_model>> mListDataChild;
    View v;

    String noHP, nama, email;
    private Calendar bak_date;
    private Calendar date;


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
            List<ListMyProfile_model> lists = new ArrayList<>();
            if (header.equals("Data Member Basic"))
            {
                lists.add(new ListMyProfile_model("","","", "", true));
                mListDataChild.put(header, lists);
            } else {
                lists.add(new ListMyProfile_model("","","", "", false));
                mListDataChild.put(header, lists);
            }

        }

        ExpandableListProfile adapter= new ExpandableListProfile(this, mListDataHeader, mListDataChild, this, onDateChooseListener);
        expandableListview.setAdapter(adapter);
        expandableListview.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Log.d("group", "group post: "+groupPosition);
            }
        });

        expandableListview.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    public void onTextChange(String message, int choice) {
        switch (choice)
        {
            case 1 : noHP=message;
                nama = message;
                email = message;
                break;
            case 2:
                break;
        }
    }

    OnDateChooseListener onDateChooseListener = new OnDateChooseListener() {
        @Override
        public void DateChooseListener() {

        }
    };



}
