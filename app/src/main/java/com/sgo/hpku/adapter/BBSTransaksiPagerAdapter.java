package com.sgo.hpku.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.fragments.BBSTransaksiPagerItem;

/**
 * Created by thinkpad on 5/8/2017.
 */

public class BBSTransaksiPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private FragmentManager fm;
    private Bundle bundle;

    public BBSTransaksiPagerAdapter(Context mContext, FragmentManager fm, Bundle bundle) {
        super(fm);
        this.mContext = mContext;
        this.fm = fm;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment mFrag = new BBSTransaksiPagerItem();
        Bundle args = new Bundle();
        if(position == 0) {
            args.putString(DefineValue.TRANSACTION, mContext.getString(R.string.cash_in));
        }
        else {
            args.putString(DefineValue.TRANSACTION, mContext.getString(R.string.cash_out));

        }
        args.putBundle(DefineValue.DATA, this.bundle);
        mFrag.setArguments(args);
        return mFrag;
    }


}
