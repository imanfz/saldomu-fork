package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BBSTransaksiPagerAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.dialogs.InformationDialog;

/**
 * Created by thinkpad on 5/8/2017.
 */

public class BBSTransaksiPager extends Fragment implements ViewPager.OnPageChangeListener{

    public final static String TAG = "com.sgo.hpku.fragments.BBSTransaksiPager";
    private View v;
    private ViewPager mViewPager;
    private int dotsCount;
    private ImageView[] dots;
    private LinearLayout pager_indicator;
    private BBSTransaksiPagerAdapter mAdapter;
    private InformationDialog dialogI;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogI = InformationDialog.newInstanceBBS(0);
        dialogI.setTargetFragment(this,0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v =  inflater.inflate(R.layout.frag_bbs_transaksi_pager, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pager_indicator = (LinearLayout) v.findViewById(R.id.viewPagerCountDots);
        mViewPager = (ViewPager) v.findViewById(R.id.bbs_transaksi_pager);
        mAdapter = new BBSTransaksiPagerAdapter(getActivity(), getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        setUiPageViewController();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            String type = bundle.getString(DefineValue.TYPE,"");
            if(type != null && !type.isEmpty()){
                if(type.equalsIgnoreCase(DefineValue.BBS_CASHIN))
                    mViewPager.setCurrentItem(0);
                else
                    mViewPager.setCurrentItem(1);
            }
        }
    }

    private void setUiPageViewController() {
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getActivity());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.information, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_information){
            if(!dialogI.isAdded())
                dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }

        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public Fragment getConfirmFragment() {
        return (BBSTransaksiPagerItem) mAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }
}
