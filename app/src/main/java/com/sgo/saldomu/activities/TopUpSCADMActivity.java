package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragListTopUpSCADM;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/14/2018.
 */

public class TopUpSCADMActivity extends BaseActivity {
    private SecurePreferences sp;
    FragmentManager fragmentManager;
    Fragment mContent;
    Fragment newFragment = null;
    public static String TOPUP = "topup";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_topup_scadm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        if (findViewById(R.id.topup_scadm_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            newFragment = new FragListTopUpSCADM();
        }

        mContent = newFragment;

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.topup_scadm_content, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.scadm_topup));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.topup_scadm_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.topup_scadm_content, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setResultActivity(int result){
        setResult(MainPage.RESULT_BALANCE);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                this.setResult(MainPage.RESULT_BALANCE);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }
}
