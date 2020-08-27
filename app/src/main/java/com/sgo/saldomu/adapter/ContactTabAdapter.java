package com.sgo.saldomu.adapter;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.sgo.saldomu.fragments.FragFAQ;
import com.sgo.saldomu.fragments.FragHelp;

/**
 * Created by thinkpad on 1/14/2016.
 */
public class ContactTabAdapter extends FragmentStatePagerAdapter {

    private final String[] TITLES;
    private final Context mContext;

    public ContactTabAdapter(FragmentManager fm, Context _context, String[] _titles) {
        super(fm);
        mContext = _context;
        TITLES = _titles;
    }

    @Override
    public Fragment getItem(int position) {
        Log.v("POSITION", "position: " + position);
        if(position == 0) {
            return FragHelp.newInstance();
        }
        if(position == 1) {
            return FragFAQ.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
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
}
