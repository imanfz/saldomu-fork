package com.sgo.orimakardaya.fragments;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.adapter.MainFragmentAdapter;
import com.sgo.orimakardaya.coreclass.BaseFragmentMainPage;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.viewpagerindicator.TitlePageIndicator;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

import java.util.ArrayList;
import java.util.List;

/*
  Created by Administrator on 12/1/2014.
 */
public class FragMainPage extends Fragment {


    private MainFragmentAdapter currentAdapternya;
    private PtrFrameLayout currentPtrFrame;
    SecurePreferences sp;
    private View currentView;

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
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        MainFragmentAdapter adapternya;
        TitlePageIndicator tabs;
        ViewPager pager;
        getActivity().invalidateOptionsMenu();
        final List<BaseFragmentMainPage> mList = new ArrayList<BaseFragmentMainPage>();
        mList.add(new Home());
        mList.add(new MyHistory());
        mList.add(new TimeLine());
//        mList.add(new Group());

        tabs = (TitlePageIndicator)getCurrentView().findViewById(R.id.main_tabs);
        pager = (ViewPager) getCurrentView().findViewById(R.id.main_pager);
        adapternya = new MainFragmentAdapter(getChildFragmentManager(),getActivity(),mList);

        pager.setAdapter(adapternya);
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
        pager.setCurrentItem(0);
        pager.setOffscreenPageLimit(3);

        setCurrentAdapternya(adapternya);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
//                updateFab(i);
                ToggleFAB(!(mList.get(i) instanceof Home));
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
                if (getActivity() != null) {
                    getCurrentAdapternya().mCurrentFragment.refresh(frame);
                }
            }


        });

        setCurrentPtrFrame(mPtrFrame);

        getCurrentPtrFrame().autoRefresh(true);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        tabs.setOnCenterItemClickListener(new TitlePageIndicator.OnCenterItemClickListener() {
            @Override
            public void onCenterItemClick(int position) {
                getCurrentAdapternya().mCurrentFragment.goToTop();
            }
        });


        ToggleFAB(false);
    }

    public Fragment getFragment(int position){
        return getCurrentAdapternya().getItem(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public MainFragmentAdapter getCurrentAdapternya() {
        return currentAdapternya;
    }

    public void setCurrentAdapternya(MainFragmentAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }

    public PtrFrameLayout getCurrentPtrFrame() {
        return currentPtrFrame;
    }

    public void setCurrentPtrFrame(PtrFrameLayout currentPtrFrame) {
        this.currentPtrFrame = currentPtrFrame;
    }

    public View getCurrentView() {
        return currentView;
    }

    public void setCurrentView(View currentView) {
        this.currentView = currentView;
    }


    private int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getActivity().getWindow().getStatusBarColor();
        }
        return 0;
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(color);
        }
    }

//    private void setupFab() {
//        Fab fab = (Fab) getActivity().findViewById(R.id.fab);
//        View sheetView = getActivity().findViewById(R.id.fab_sheet);
//        View overlay = getActivity().findViewById(R.id.overlay);
//        int sheetColor = getResources().getColor(R.color.white);
//        int fabColor = getResources().getColor(R.color.colorPrimaryDarked);
//
//        // Create material sheet FAB
//        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);
//
//        // Set material sheet event listener
//        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
//            @Override
//            public void onShowSheet() {
//                // Save current status bar color
//                statusBarColor = getStatusBarColor();
//                // Set darker status bar color to match the dim overlay
////                setStatusBarColor(getResources().getColor(R.color.fab_color));
//                setStatusBarColor(statusBarColor);
//            }
//
//            @Override
//            public void onHideSheet() {
//                // Restore status bar color
//                setStatusBarColor(statusBarColor);
//            }
//        });
//
//        // Set material sheet item click listeners
//        getActivity().findViewById(R.id.fab_sheet_item_PayFriend).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switchMenu(NavigationDrawMenu.MPAYFRIENDS,null);
//                materialSheetFab.hideSheet();
//            }
//        });
//        getActivity().findViewById(R.id.fab_sheet_item_AskForMoney).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switchMenu(NavigationDrawMenu.MASK4MONEY,null);
//                materialSheetFab.hideSheet();
//            }
//        });
//
////        android.app.Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_page_content);
////        Log.d("denny_", currentFragment.toString());
//    }
//
//    private void updateFab(int selectedPage) {
//        switch (selectedPage) {
//            case 0:
//                materialSheetFab.hideSheetThenFab();
//                break;
//            default:
//                materialSheetFab.showFab();
//                break;
//        }
//    }

    private void switchMenu(int idx_menu,Bundle data){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
            fca.switchMenu(idx_menu, data);
    }

    private void ToggleFAB(Boolean isShow){
        if (getActivity() == null)
            return;

        if(getActivity() instanceof MainPage) {
            MainPage fca = (MainPage) getActivity();
            if(isShow)
                fca.materialSheetFab.showFab();
            else
                fca.materialSheetFab.hideSheetThenFab();
        }
    }
}