package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 12/1/2014.
 */

import android.content.Context;
import android.support.v4.app.*;
import android.util.Log;
import android.view.ViewGroup;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseFragmentMainPage;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.List;

import timber.log.Timber;

public class MainFragmentAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {

    private final String[] TITLES;
    private final Context mContext;
    public BaseFragmentMainPage mCurrentFragment;
    public List<BaseFragmentMainPage> mListFrag;

    public MainFragmentAdapter(FragmentManager fm, Context _context, List<BaseFragmentMainPage> _mListFrag) {
        super(fm);
        mContext = _context;
        mListFrag = _mListFrag;
        TITLES = _context.getResources().getStringArray(R.array.home_vpi_title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public Fragment getItem(int position) {
        Timber.v("position: " + position);
        return mListFrag.get(position);
    }

    @Override
    public int getIconResId(int index) {
        return 0;
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position >= getCount()) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentFragment != object) {
            mCurrentFragment = mListFrag.get(position);

        }
        super.setPrimaryItem(container, position, object);
    }

    public boolean checkCanDoRefresh() {
        //Log.wtf("masuk get Main fragment adapter", String.valueOf(mCurrentFragment == null ? "null" : "isi"));
        if (mCurrentFragment == null) {
            return true;
        }
        return mCurrentFragment.checkCanDoRefresh();
    }
}
