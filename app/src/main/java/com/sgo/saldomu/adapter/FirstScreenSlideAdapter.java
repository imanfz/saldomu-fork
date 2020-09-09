package com.sgo.saldomu.adapter;/*
  Created by Administrator on 12/1/2014.
 */

import android.content.Context;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.sgo.saldomu.fragments.ImageSlide;
import com.sgo.saldomu.fragments.TermsNConditionWeb;

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
