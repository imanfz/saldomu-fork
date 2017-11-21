package com.sgo.saldomu.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.github.paolorotolo.appintro.AppIntro;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.fcm.FCMManager;
import com.sgo.saldomu.fragments.IntroPage;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
 Created by Lenovo Thinkpad on 12/21/2015.
 */
public class Introduction extends AppIntro implements EasyPermissions.PermissionCallbacks {
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(IntroPage.newInstance(R.layout.intro1_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro2_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro3_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro4_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro5_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro6_fragment));

        setFlowAnimation();
        Button skipbtn = (Button)skipButton;
        Button donebtn = (Button)doneButton;
        skipbtn.setText(getString(R.string.start_now));
        donebtn.setText(getString(R.string.done));

        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    DefineValue.RC_LOCATION_PERMISSION, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        Timber.d("FCM ID :" + FCMManager.getTokenFCM());
    }

    @Override
    public void onSkipPressed() {
        openLogin();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        openLogin();
    }

    @Override
    public void onSlideChanged() {

    }

    private void openLogin(){
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if ( requestCode == DefineValue.RC_LOCATION_PERMISSION ) {
            this.finish();
        }
    }
}
