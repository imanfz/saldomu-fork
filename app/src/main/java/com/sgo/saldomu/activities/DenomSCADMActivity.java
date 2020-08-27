package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.MenuItem;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragListDenomSCADM;
import com.sgo.saldomu.fragments.FragmentDenomConfirm;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/14/2018.
 */

public class DenomSCADMActivity extends BaseActivity {
    private SecurePreferences sp;
    FragmentManager fragmentManager;
    Fragment mContent;
    Fragment newFragment = null;

    public static String DENOM_PAYMENT = "denom_payment";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_denom_scadm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        if (findViewById(R.id.denom_scadm_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            newFragment = new FragListDenomSCADM();
        }

        mContent = newFragment;

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.denom_scadm_content, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.scadm_denom));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setResultActivity(int result) {
        setResult(MainPage.RESULT_BALANCE);
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if (isBackstack) {
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.denom_scadm_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.denom_scadm_content, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j) {
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent, MainPage.REQUEST_FINISH);
                this.setResult(MainPage.RESULT_BALANCE);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        Fragment fragment = fragmentManager.findFragmentById(R.id.denom_scadm_content);
        if (fragment instanceof FragmentDenomConfirm) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment).commit();
        } else {
            super.onBackPressed();
        }
    }
}
