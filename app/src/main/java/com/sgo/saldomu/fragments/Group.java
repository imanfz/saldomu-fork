package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.hb.views.PinnedSectionListView;
import com.sgo.saldomu.Beans.HomeGroupObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.GroupDetailActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.HomeGroupAdapter;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;

import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrFrameLayout;

/*
  Created by Administrator on 12/2/2014.
 */
public class Group extends BaseFragmentMainPage {

    private ArrayList<HomeGroupObject> groups;
    private PinnedSectionListView lvGroup;

    @Override
    protected int getInflateFragmentLayout() {
        return R.layout.frag_group;
    }

    @Override
    public boolean checkCanDoRefresh() {
        return !canScrollUp(lvGroup); // or cast with ListView
    }

    @Override
    public void refresh(PtrFrameLayout frameLayout) {

    }

    @Override
    public void goToTop() {

    }

    private boolean canScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lvGroup = (PinnedSectionListView) mView.findViewById(R.id.lvGroup);
        int sectionPosition = 0;
        int listPosition = 0;
        groups = new ArrayList<>();
        for(int i=0 ; i<1 ; i++) {
            HomeGroupObject homeGroupObject = new HomeGroupObject();
            homeGroupObject.setType(0);
            homeGroupObject.setGroupName("Teman Hangouts");
            homeGroupObject.setSectionPosition(sectionPosition);
            homeGroupObject.setListPosition(listPosition++);
            groups.add(homeGroupObject);
        }
        for(int i=0 ; i<4 ; i++) {
            HomeGroupObject homeGroupObject = new HomeGroupObject();
            homeGroupObject.setType(1);
            homeGroupObject.setProfpic("http://lorempixel.com/output/technics-q-c-480-480-6.jpg");
            homeGroupObject.setGroupName("Teman Hangouts");
            homeGroupObject.setPay("Wargito");
            homeGroupObject.setGetPaid("Lee");
            homeGroupObject.setDesc("Bayar Nasgor Mas Bento ");
            homeGroupObject.setDate("2h");
            homeGroupObject.setSectionPosition(sectionPosition);
            homeGroupObject.setListPosition(listPosition++);
            groups.add(homeGroupObject);
        }
        for(int i=0 ; i<1 ; i++) {
            HomeGroupObject homeGroupObject = new HomeGroupObject();
            homeGroupObject.setType(0);
            homeGroupObject.setGroupName("Keluarga");
            homeGroupObject.setSectionPosition(sectionPosition++);
            homeGroupObject.setListPosition(listPosition++);
            groups.add(homeGroupObject);
        }
        for(int i=0 ; i<8 ; i++) {
            HomeGroupObject homeGroupObject = new HomeGroupObject();
            homeGroupObject.setType(1);
            homeGroupObject.setProfpic("http://lorempixel.com/output/sports-q-c-480-480-4.jpg");
            homeGroupObject.setGroupName("Keluarga");
            homeGroupObject.setPay("Kim");
            homeGroupObject.setGetPaid("Sumiati");
            homeGroupObject.setDesc("Bayar Mie");
            homeGroupObject.setDate("4h");
            homeGroupObject.setSectionPosition(sectionPosition);
            homeGroupObject.setListPosition(listPosition++);
            groups.add(homeGroupObject);
        }
        for(int i=0 ; i<1 ; i++) {
            HomeGroupObject homeGroupObject = new HomeGroupObject();
            homeGroupObject.setType(0);
            homeGroupObject.setGroupName("Teman Kantor");
            homeGroupObject.setSectionPosition(sectionPosition++);
            homeGroupObject.setListPosition(listPosition++);
            groups.add(homeGroupObject);
        }
        for(int i=0 ; i<5 ; i++) {
            HomeGroupObject homeGroupObject = new HomeGroupObject();
            homeGroupObject.setType(1);
            homeGroupObject.setProfpic("http://lorempixel.com/output/technics-q-c-480-480-6.jpg");
            homeGroupObject.setGroupName("Teman Kantor");
            homeGroupObject.setPay("Wicaksono");
            homeGroupObject.setGetPaid("Wargito");
            homeGroupObject.setDesc("simpati voucher IDR 100,000 ref: a9012..");
            homeGroupObject.setDate("9h");
            homeGroupObject.setSectionPosition(sectionPosition);
            homeGroupObject.setListPosition(listPosition++);
            groups.add(homeGroupObject);
        }

        HomeGroupAdapter homeGroupAdapter = new HomeGroupAdapter(getActivity().getApplicationContext(), groups);
        lvGroup.setAdapter(homeGroupAdapter);

        lvGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), GroupDetailActivity.class);
                i.putExtra("groupname", groups.get(position).getGroupName());
                i.putExtra("pay", groups.get(position).getPay());
                i.putExtra("getpaid", groups.get(position).getGetPaid());
                i.putExtra("desc", groups.get(position).getDesc());
                i.putExtra("date", groups.get(position).getDate());
                i.putExtra("profpic", groups.get(position).getProfpic());
                switchActivity(i);
            }
        });
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }
}