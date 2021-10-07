package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BBSTransaksiPagerAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.widgets.CustomViewPager;

/**
 * Created by thinkpad on 5/8/2017.
 */

public class BBSTransaksiPager extends Fragment implements ViewPager.OnPageChangeListener{

    public final static String TAG = "com.sgo.saldomu.fragments.BBSTransaksiPager";
    private View v;
    private CustomViewPager mViewPager;
    private int dotsCount;
    private ImageView[] dots;
    private LinearLayout pager_indicator;
    private BBSTransaksiPagerAdapter mAdapter;
    SecurePreferences sp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Bundle bundle = getArguments();
        pager_indicator = v.findViewById(R.id.viewPagerCountDots);
        mViewPager = v.findViewById(R.id.bbs_transaksi_pager);
        mAdapter = new BBSTransaksiPagerAdapter(getActivity(), getChildFragmentManager(), bundle);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.disableScroll(true);
        pager_indicator.setVisibility(View.GONE);

        setUiPageViewController();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        if ( flagLogin.equals(DefineValue.STRING_NO) ) {
            getActivity().finish();
        } else {
            String notifDataNextLogin = sp.getString(DefineValue.NOTIF_DATA_NEXT_LOGIN, "");
            if (!notifDataNextLogin.equals("")) {
                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
            }
        }

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
            dots[i].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.nonselecteditem_dot, null));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.selecteditem_dot, null));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.nonselecteditem_dot, null));
        }

        dots[position].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.selecteditem_dot, null));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public Fragment getConfirmFragment() {
        return (BBSTransaksiPagerItem) mAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
    }
}
