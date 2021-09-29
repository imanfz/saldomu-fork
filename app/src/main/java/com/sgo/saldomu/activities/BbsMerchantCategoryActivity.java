package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragMerchantCategory;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class BbsMerchantCategoryActivity extends BaseActivity {

    FragmentManager fragmentManager;
    private String memberId;
    private String shopId;
    private String flagApprove;
    private String setupOpenHour;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        memberId = getIntent().getStringExtra(DefineValue.MEMBER_ID);
        shopId = getIntent().getStringExtra(DefineValue.SHOP_ID);
        flagApprove = getIntent().getStringExtra(DefineValue.FLAG_APPROVE);
        setupOpenHour = getIntent().getStringExtra(DefineValue.SETUP_OPEN_HOUR);

        if (findViewById(R.id.category_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Intent intent = getIntent();
            int index = intent.getIntExtra(DefineValue.INDEX, 0);

            Fragment newFragment = null;
            switch (index) {
                case 0:
                    newFragment = new FragMerchantCategory();

                    Bundle bundle = new Bundle();
                    bundle.putString(DefineValue.MEMBER_ID, memberId);
                    bundle.putString(DefineValue.SHOP_ID, shopId);
                    bundle.putString(DefineValue.FLAG_APPROVE, flagApprove);
                    bundle.putString(DefineValue.SETUP_OPEN_HOUR, setupOpenHour);
                    newFragment.setArguments(bundle);
                    break;
            }

            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.category_content, newFragment, "merchant_category");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_merchant_category;
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.merchant_category));
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if (isBackstack) {
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.category_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.category_content, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    public void setTitleFragment(String _title) {
        setActionBarTitle(_title);
    }
}
