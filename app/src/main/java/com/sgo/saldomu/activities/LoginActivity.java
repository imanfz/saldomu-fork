package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.fragments.Login;
import com.sgo.saldomu.fragments.Regist1;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/*
  Created by Administrator on 11/4/2014.
 */
public class LoginActivity extends BaseActivity {

    private static final int REQUEST_EXIT = 0 ;
    public static final int RESULT_PIN = 1 ;
    public static final int RESULT_NORMAL = 2 ;
    public static final int RESULT_FINISHING = 5 ;
    public static final int ACTIVITY_RESULT = 3;

    private FragmentManager fragmentManager;
    private SecurePreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(InetHandler.isNetworkAvailable(this))
            new UtilsLoader(this).getAppVersion();

        if (findViewById(R.id.loginContent) != null) {
            if (savedInstanceState != null) {
                return;
            }

            sp = CustomSecurePref.getInstance().getmSecurePrefs();
            String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
            if ( flagLogin == null )
                flagLogin = DefineValue.STRING_NO;

            if ( flagLogin.equals(DefineValue.STRING_YES) ) {
                Intent i = new Intent(this,MainPage.class);
                startActivity(i);
                finish();
            }


            Fragment newFrag = new Login();

            Bundle m = getIntent().getExtras();
            if (m != null && m.containsKey(DefineValue.USER_IS_NEW)) {
                if (m.getInt(DefineValue.USER_IS_NEW, 0) == 1) {
                    newFrag = new Regist1();
                    newFrag.setArguments(m);
                } else if (m.getInt(DefineValue.USER_IS_NEW, 0) != 0 ) { //untuk shorcut dari tombol di activity introduction
                    if (m.getInt(DefineValue.USER_IS_NEW, 0) == -1) {
                        newFrag = new Regist1();

                    } else if (m.getInt(DefineValue.USER_IS_NEW, 0) == -2) {
                        newFrag = new Login();
                    }
                    newFrag.setArguments(m);
                }
            }

            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.loginContent, newFrag,"login");
            fragmentTransaction.commit();
        }
    }

    public void SaveImeiICCIDDevice(){
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor edit = sp.edit();
        SMSclass smSclass = new SMSclass(this);
        edit.putString(DefineValue.DEIMEI, smSclass.getDeviceIMEI());
        edit.putString(DefineValue.DEICCID, smSclass.getDeviceICCID());
        edit.apply();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LoginActivity.ACTIVITY_RESULT){
            if(resultCode == LoginActivity.RESULT_FINISHING)
                this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.loginContent, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.loginContent, mFragment, fragName)
                    .commit();

        }
    }

    public void switchActivity(Intent mIntent) {
                startActivityForResult(mIntent, REQUEST_EXIT);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentByTag("reg2") == null)
            super.onBackPressed();
    }


    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){

        if(_on){
            Timber.wtf("masuk turnOnBR");
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            filter.addCategory("android.intent.category.DEFAULT");
            registerReceiver(_myreceiver,filter);
        }
        else {
            Timber.wtf("masuk turnOffBR");
            unregisterReceiver(_myreceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitService.dispose();
    }
}