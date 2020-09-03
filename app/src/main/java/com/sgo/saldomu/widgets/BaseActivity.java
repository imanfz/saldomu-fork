package com.sgo.saldomu.widgets;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.PermissionResult;
import com.sgo.saldomu.receivers.FcmReceiver;
import com.sgo.saldomu.utils.LocaleManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
  Created by Administrator on 11/24/2014.
 */
public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, PermissionResult {

    protected static final int RC_LOCATION_PERM = 2121;

    private Toolbar detoolbar;
    private TextView title_detoolbar, greeting_detoolbar;
    private ImageView img_detoolbar;
    private ProgressBar deprogressbar;
    protected SMSclass smsClass;
    protected boolean isActive;
    public String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE,
//    private String[] perms = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private IntentFilter fcmFilter = new IntentFilter();
    FcmReceiver fcmReceiver = new FcmReceiver();

    protected PermissionResult permissionResultInterface = this;

    protected SecurePreferences sp;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey, userName;
    protected String extraSignature = "";

    protected Gson gson;
    protected ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        gson = new Gson();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID, "");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID, "");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        userName = sp.getString(DefineValue.USER_NAME, "");

        detoolbar = findViewById(R.id.main_toolbar);
        deprogressbar = findViewById(R.id.main_toolbar_progress_spinner);

        title_detoolbar = findViewById(R.id.main_toolbar_title);
        greeting_detoolbar = findViewById(R.id.main_toolbar_title_greeting);
        img_detoolbar = findViewById(R.id.main_toolbar_img);
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

    private void checkPermission() {
        Timber.d("masuk check permission base activity");


        if (!isHasAppPermission(this, perms)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.rational_readphonestate_readcontacts),
                    RC_LOCATION_PERM, perms);
        }
    }

    private boolean isHasAppPermission(Context context, String... permissions) {
        return EasyPermissions.hasPermissions(context, permissions);
    }

    protected boolean isHasAppPermission() {
        return isHasAppPermission(this, perms);
    }

    @Override
    protected void onPause() {
        if (fcmReceiver != null) {
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

    public void setActionBarIcon(int iconRes) {
        detoolbar.setNavigationIcon(iconRes);
    }

    protected void disableHomeIcon() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    protected void setActionBarTitle(String _title) {
        title_detoolbar.setText(_title);
        if (_title.equalsIgnoreCase(userName)) {
            img_detoolbar.setVisibility(View.VISIBLE);
            title_detoolbar.setAllCaps(true);
            greeting_detoolbar.setVisibility(View.VISIBLE);
            greeting_detoolbar.setText(setGreetings());
        } else {
            title_detoolbar.setAllCaps(false);
            img_detoolbar.setVisibility(View.GONE);
            greeting_detoolbar.setVisibility(View.GONE);
        }
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
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_LOCATION_PERM:
                for (int i = 0; i < perms.size(); i++) {
                    if (perms.get(i).equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE))
                        permissionResultInterface.onReadPhoneStateGranted();

                    if (perms.get(i).equalsIgnoreCase(Manifest.permission.READ_CONTACTS))
                        permissionResultInterface.onReadContactsGranted();

                    if (perms.get(i).equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permissionResultInterface.onAccessFineLocationGranted();
                    }
                }

                if (perms.size() > 0)
                    permissionResultInterface.onGranted();
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
    public void onGranted() {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_LOCATION_PERM:
                if (perms.size() > 0) {
                    permissionResultInterface.onDeny();
                }
        }
    }

    protected Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public String setGreetings() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greeting = "";
        if (hour > 10 && hour <= 14) {
            greeting = getString(R.string.good_afternoon);
            img_detoolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.sun));
        } else if (hour > 14 && hour <= 18.30) {
            greeting = getString(R.string.good_evening);
            img_detoolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.moon));
        } else if (hour > 18.30 || hour < 4) {
            greeting = getString(R.string.good_night);
            img_detoolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.moon));
        } else {
            greeting = getString(R.string.good_morning);
            img_detoolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.sun));
        }
        return greeting;
    }

    ProgressDialog getProgDialog() {
        if (progressDialog == null)
            progressDialog = DefinedDialog.CreateProgressDialog(this, getString(R.string.please_wait));
        return progressDialog;
    }

    protected void showProgressDialog() {
        if (getProgDialog() != null)
            if (!getProgDialog().isShowing())
                getProgDialog().show();
    }

    protected void dismissProgressDialog() {
        if (getProgDialog() != null)
            if (getProgDialog().isShowing())
                getProgDialog().dismiss();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Timber.d("Logging attachBaseContext.....");
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }
}