package com.sgo.saldomu.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.fragments.BBSTransaksiPagerItem;

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
        if(bundle!=null) {
            args.putString(DefineValue.TYPE, bundle.getString(DefineValue.TYPE));
            args.putString(DefineValue.AMOUNT, bundle.getString(DefineValue.AMOUNT));
            args.putString(DefineValue.KEY_CODE, bundle.getString(DefineValue.KEY_CODE));
            args.putString(DefineValue.PRODUCT_CODE, bundle.getString(DefineValue.PRODUCT_CODE));
        }
        mFrag.setArguments(args);
        return mFrag;
    }
}
