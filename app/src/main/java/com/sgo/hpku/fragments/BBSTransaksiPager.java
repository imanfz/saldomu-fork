package com.sgo.hpku.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sgo.hpku.R;
import com.sgo.hpku.adapter.BBSTransaksiPagerAdapter;
import com.sgo.hpku.fragments.BBSTransaksiPagerItem;

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
