package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileNewActivity;
import com.sgo.saldomu.adapter.MainFragmentAdapter;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/*
  Created by Administrator on 12/1/2014.
 */
public class FragMainPage extends Fragment {

    private MainFragmentAdapter currentAdapternya;
    private PtrFrameLayout currentPtrFrame;
    private SecurePreferences sp;
    private View currentView;
    private boolean isAgent;
    ViewPager pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main_page, container, false);
        getActivity().invalidateOptionsMenu();
        setCurrentView(v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        MainFragmentAdapter adapternya;
        TitlePageIndicator tabs;
        getActivity().invalidateOptionsMenu();
        final List<BaseFragmentMainPage> mList = new ArrayList<>();

        mList.add(new FragHomeNew());

        tabs = (TitlePageIndicator)getCurrentView().findViewById(R.id.main_tabs);
        pager = (ViewPager) getCurrentView().findViewById(R.id.main_pager);
        adapternya = new MainFragmentAdapter(getChildFragmentManager(),getActivity(),mList);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (pager.getCurrentItem()==0)
                {
                    if (!isAgent)
                        showDialogNotAgent();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pager.setAdapter(adapternya);
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
        pager.setCurrentItem(0);
        pager.setOffscreenPageLimit(1);

        setCurrentAdapternya(adapternya);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

//        setupFab();

        PtrFrameLayout mPtrFrame = (PtrClassicFrameLayout) getCurrentView().findViewById(R.id.view_pager_ptr_frame);
        mPtrFrame.disableWhenHorizontalMove(true);

        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return getCurrentAdapternya().checkCanDoRefresh();
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                if (getActivity() != null && getCurrentAdapternya().mCurrentFragment != null) {
                    getCurrentAdapternya().mCurrentFragment.refresh(frame);
                }
            }


        });

        setCurrentPtrFrame(mPtrFrame);

        getCurrentPtrFrame().autoRefresh(true);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        tabs.setOnCenterItemClickListener(position -> getCurrentAdapternya().mCurrentFragment.goToTop());

        tabs.setVisibility(View.GONE);
    }

    public void showDialogNotAgent()
    {
        final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getActivity().getString(R.string.level_dialog_agent),
                getActivity().getString(R.string.level_dialog_agent1), getActivity().getString(R.string.level_dialog_btn_ok),
                getActivity().getString(R.string.cancel), false);
        dialog_frag.setOkListener((dialog, which) -> {
            Intent mI = new Intent(getActivity(), MyProfileNewActivity.class);
            getActivity().startActivityForResult(mI, MainPage.ACTIVITY_RESULT);
        });
        dialog_frag.setCancelListener((dialog, which) -> {
            dialog_frag.dismiss();
            pager.setCurrentItem(1);
        });

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.add(dialog_frag,null);
        ft.commitAllowingStateLoss();
    }

    private MainFragmentAdapter getCurrentAdapternya() {
        return currentAdapternya;
    }

    private void setCurrentAdapternya(MainFragmentAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }

    private PtrFrameLayout getCurrentPtrFrame() {
        return currentPtrFrame;
    }

    private void setCurrentPtrFrame(PtrFrameLayout currentPtrFrame) {
        this.currentPtrFrame = currentPtrFrame;
    }

    private View getCurrentView() {
        return currentView;
    }

    private void setCurrentView(View currentView) {
        this.currentView = currentView;
    }
}