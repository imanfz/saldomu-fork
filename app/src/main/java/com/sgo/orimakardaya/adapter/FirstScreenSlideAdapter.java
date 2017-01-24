package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 12/1/2014.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import com.sgo.orimakardaya.fragments.ImageSlide;
import com.sgo.orimakardaya.fragments.TermsNConditionWeb;
import timber.log.Timber;

public class FirstScreenSlideAdapter extends FragmentStatePagerAdapter {


    private final Context mContext;
    private FragmentManager mFm;

    public FirstScreenSlideAdapter(FragmentManager fm, Context _context) {
        super(fm);
        mContext = _context;
        mFm = fm;
    }


    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0: return ImageSlide.newInstance(i);
            case 1: return ImageSlide.newInstance(i);
            case 2: return new TermsNConditionWeb();
        }
        Timber.d("isi get item index:" + String.valueOf(i));
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position > getCount()) {
            /*FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();*/
            FragmentTransaction trans = mFm.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
            super.destroyItem(container,position,object);
        }
    }
}
