package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.fragments.ListEvoucherHP;

import timber.log.Timber;

/*
  Created by Administrator on 1/30/2015.
 */
public class EvoucherHPActivity extends BaseActivity {

    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.evoucher_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            ListEvoucherHP mFrag = new ListEvoucherHP();
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.evoucher_content, mFrag,"evoucher");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_evoucher_hp;
    }

    private void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setToolbarTitle(getString(R.string.evoucherhp_ab_title));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.evoucher_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.evoucher_content, mFragment, fragName)
                    .commit();

        }
        setToolbarTitle(fragName);
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                break;
            case 2:
                break;
        }
    }
}