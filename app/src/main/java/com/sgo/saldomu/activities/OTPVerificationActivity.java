package com.sgo.saldomu.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.fragments.OTPVerification;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class OTPVerificationActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (findViewById(R.id.otpVerificationContent) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Fragment newFrag = new OTPVerification();
            FragmentManager fragmentManager= getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.otpVerificationContent, newFrag, "otpVerification");
            fragmentTransaction.commit();
        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_verification_otp;
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {

        if (isBackstack) {
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.otpVerificationContent, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        } else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.otpVerificationContent, mFragment, fragName)
                    .commit();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitService.dispose();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }


}
