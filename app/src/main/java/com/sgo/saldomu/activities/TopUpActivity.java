package com.sgo.saldomu.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.ListBankTopUpFragment;
import com.sgo.saldomu.fragments.SgoPlus_input;
import com.sgo.saldomu.widgets.BaseActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
  Created by Administrator on 4/28/2015.
 */
public class TopUpActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    FragmentManager fragmentManager;
    String transaction_type;
    Boolean is_full_activity = false;
    boolean isSMSBanking = false;
    private SMSclass smSclass;
    private final static int RC_READ_PHONE_STATE = 121;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        transaction_type = i.getStringExtra(DefineValue.TRANSACTION_TYPE);
        Boolean isTagihan = i.getBooleanExtra(DefineValue.TAGIHAN,false);
        is_full_activity = i.getBooleanExtra(DefineValue.IS_ACTIVITY_FULL,false);
        InitializeToolbar();

        if (findViewById(R.id.topUpActivityContent) != null) {
            if (savedInstanceState != null) {
                return;
            }

            if(isTagihan) {
                if (transaction_type != null && !transaction_type.isEmpty()) {
                    if (transaction_type.equals(DefineValue.SMS_BANKING)) {
                        initializeSMSBanking();
                    }
                }
            }
            else {

                if(!is_full_activity && i.getStringExtra(DefineValue.PRODUCT_TYPE).equals(DefineValue.BANKLIST_TYPE_SMS))
                    initializeSMSBanking();
            }

            Fragment mFrag;
            Bundle mArgs = i.getExtras();

//            if(is_full_activity){
                mFrag = new ListBankTopUpFragment();
                mArgs.putBoolean(DefineValue.IS_ACTIVITY_FULL,is_full_activity);
                mArgs.putString("Toolbar", i.getStringExtra("Toolbar"));
                setToolbarTitle(getString(R.string.toolbar_title_topup));
//            }
//            else {
//                mFrag = new SgoPlus_input();
//            }
            mFrag.setArguments(mArgs);
            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.topUpActivityContent, mFrag, "sgoInput");
            fragmentTransaction.commitAllowingStateLoss();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    private void initializeSMSBanking(){
        isSMSBanking = true;
        if(EasyPermissions.hasPermissions(this,Manifest.permission.READ_PHONE_STATE)){
            initializeSmsClass();
        }
        else {
            EasyPermissions.requestPermissions(this,getString(R.string.rational_readphonestate),
                    RC_READ_PHONE_STATE,Manifest.permission.READ_PHONE_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(smSclass != null && isSMSBanking)
            registerReceiver(smSclass.simStateReceiver,SMSclass.simStateIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(smSclass != null && isSMSBanking)
            unregisterReceiver(smSclass.simStateReceiver);
    }

    private void initializeSmsClass(){
        smSclass = new SMSclass(this);

        smSclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if(!isExist){
                    Toast.makeText(TopUpActivity.this,msg,Toast.LENGTH_SHORT).show();
                    TopUpActivity.this.finish();
                }
            }
        });

        try{
            unregisterReceiver(smSclass.simStateReceiver);
        }
        catch (Exception e){}
        registerReceiver(smSclass.simStateReceiver,SMSclass.simStateIntentFilter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_topup;
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.topUpActivityContent, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.topUpActivityContent, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    public void switchActivity(Intent mIntent, int j) {
        ToggleKeyboard.hide_keyboard(this);
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                break;
            case 2:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("isi request code:"+ String.valueOf(requestCode));
        Timber.d("isi result Code:"+ String.valueOf(resultCode));
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == MainPage.RESULT_BALANCE) {
                setResult(MainPage.RESULT_BALANCE);
                if(is_full_activity)
                    finish();
            }
            else if(resultCode == MainPage.RESULT_NORMAL){
                if(is_full_activity)
                    getSupportFragmentManager().popBackStack();
            }
            else if (resultCode == MainPage.RESULT_LOGOUT) {
                setResult(MainPage.RESULT_LOGOUT);
                finish();
            }

        }

        if(!is_full_activity)
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){
        Timber.wtf("masuk turnOnBR");
        if(_on){
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(_myreceiver,filter);
            filter.setPriority(999);
            filter.addCategory("android.intent.category.DEFAULT");
        }
        else unregisterReceiver(_myreceiver);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitService.dispose();
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode){
            case RC_READ_PHONE_STATE:
                initializeSmsClass();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode){
            case RC_READ_PHONE_STATE:
                Toast.makeText(this, getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
}