package com.sgo.saldomu.coreclass;

import android.Manifest;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.interfaces.PermissionResult;
import com.sgo.saldomu.receivers.FcmReceiver;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
  Created by Administrator on 11/24/2014.
 */
public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,PermissionResult {

    protected static final int RC_LOCATION_PERM    = 2121;

    private Toolbar detoolbar;
    private TextView title_detoolbar;
    private ProgressBar deprogressbar;
    protected SMSclass smsClass;
    protected boolean isActive;

    private IntentFilter fcmFilter = new IntentFilter();
    FcmReceiver fcmReceiver = new FcmReceiver();

    protected PermissionResult permissionResultInterface = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        detoolbar = (Toolbar) findViewById(R.id.main_toolbar);
        deprogressbar = (ProgressBar) findViewById(R.id.main_toolbar_progress_spinner);

        title_detoolbar = (TextView) findViewById(R.id.main_toolbar_title);
        if (detoolbar != null) {
            setSupportActionBar(detoolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        fcmFilter.addAction(DefineValue.INTENT_ACTION_FCM_DATA);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(fcmReceiver, fcmFilter);
    }

    private void checkPermission(){
        String[] perms = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION};

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.rational_readphonestate_readcontacts),
                    RC_LOCATION_PERM, perms);
        }
    }

    @Override
    protected void onPause() {
        if(fcmReceiver != null){
            unregisterReceiver(fcmReceiver);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        detoolbar.setNavigationIcon(iconRes);
    }

    protected void disableHomeIcon() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    protected void setActionBarTitle(String _title) {
        title_detoolbar.setText(_title);
    }

    protected String getActionBarTitle() {
        return title_detoolbar.getText().toString();
    }

    protected Toolbar getToolbar() {
        return detoolbar;
    }

    protected ProgressBar getProgressSpinner() {
        return deprogressbar;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode){
            case RC_LOCATION_PERM :
                for (int i = 0 ; i <perms.size() ; i ++){
                    if(perms.get(i).equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE))
                        permissionResultInterface.onReadPhoneStateGranted();

                    if(perms.get(i).equalsIgnoreCase(Manifest.permission.READ_CONTACTS))
                        permissionResultInterface.onReadContactsGranted();

                    if(perms.get(i).equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION))
                        permissionResultInterface.onAccessFineLocationGranted();
                }
        }
    }

    @Override
    public void onReadPhoneStateGranted() {
    }

    @Override
    public void onAccessFineLocationGranted() {
    }

    @Override
    public void onReadContactsGranted() {
    }

    @Override
    public void onDeny() {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode){
            case RC_LOCATION_PERM :
                if(perms.size() > 0){
                    permissionResultInterface.onDeny();
                }
        }
    }
}