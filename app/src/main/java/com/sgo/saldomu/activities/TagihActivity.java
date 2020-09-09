package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragTagihInput;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class TagihActivity extends BaseActivity {
    SecurePreferences sp;
    FragmentManager fragmentManager;
    Fragment mContent;
    Fragment newFragment = null;
    private String memberCode,commCode,commName,anchorName, txIdPG;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_tagih;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Boolean is_search = intent.getBooleanExtra(DefineValue.IS_SEARCH_DGI, false);
        Timber.d("is search tagih activity : ", is_search.toString());
        if (intent.hasExtra(DefineValue.MEMBER_CODE_PG)) {
            memberCode = intent.getStringExtra(DefineValue.MEMBER_CODE_PG);
            commCode = intent.getStringExtra(DefineValue.COMM_CODE_PG);
            commName = intent.getStringExtra(DefineValue.COMM_NAME_PG);
            anchorName = intent.getStringExtra(DefineValue.ANCHOR_NAME_PG);
            txIdPG = intent.getStringExtra(DefineValue.TXID_PG);
        }
        initializeToolbar();

        if (findViewById(R.id.layout_tagih) != null) {
            if (savedInstanceState != null) {
                return;
            }

            newFragment = new FragTagihInput();
            Bundle bundle = new Bundle();
            bundle.putBoolean(DefineValue.IS_SEARCH_DGI, is_search);
            if (memberCode != null || commCode != null || commName!=null || anchorName!=null) {
                bundle.putString(DefineValue.MEMBER_CODE_PG, memberCode);
                bundle.putString(DefineValue.COMM_CODE_DGI, commCode);
                bundle.putString(DefineValue.COMM_NAME_PG, commName);
                bundle.putString(DefineValue.ANCHOR_NAME_PG, anchorName);
                bundle.putString(DefineValue.TXID_PG, txIdPG);
            }
            newFragment.setArguments(bundle);
        }

        mContent = newFragment;

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layout_tagih, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);

    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_tagih_agent));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if (isBackstack) {
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_tagih, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_tagih, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    public void setResultActivity(int result) {
        setResult(MainPage.RESULT_BALANCE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
