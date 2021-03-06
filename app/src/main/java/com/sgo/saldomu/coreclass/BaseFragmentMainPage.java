package com.sgo.saldomu.coreclass;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sgo.saldomu.widgets.BaseFragment;

import in.srain.cube.views.ptr.PtrFrameLayout;

/*
  Created by Administrator on 11/24/2014.
 */
public abstract class BaseFragmentMainPage extends BaseFragment {

    protected View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(getInflateFragmentLayout(), container, false);
        return mView;
    }
    protected abstract int getInflateFragmentLayout();

    public abstract boolean checkCanDoRefresh();

    public abstract void refresh(PtrFrameLayout frameLayout);

    public abstract void goToTop();

}