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
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.fragments.FragPayFriends;
import com.sgo.saldomu.fragments.FragPayFriendsConfirm;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/*
  Created by Administrator on 12/10/2014.
 */
public class PayFriendsActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        InitializeToolbar();

        if (findViewById(R.id.payfriends_confirm_token_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Intent intent    = getIntent();
            Bundle bundle = intent.getBundleExtra("data");
            Fragment newFragment;
            if (intent.getBooleanExtra(DefineValue.CONFIRM_PAYFRIEND,false)) {
                Bundle args = new Bundle();
                args.putString(WebParams.CUSTOMER_ID, intent.getStringExtra(WebParams.CUSTOMER_ID));
                args.putString(WebParams.DATA_TRANSFER, intent.getStringExtra(WebParams.DATA_TRANSFER));
                args.putString(WebParams.DATA, intent.getStringExtra(WebParams.DATA));
                args.putString(WebParams.MESSAGE, intent.getStringExtra(WebParams.MESSAGE));
                args.putString(WebParams.DATA_MAPPER, intent.getStringExtra(WebParams.DATA_MAPPER));
                args.putBoolean(DefineValue.TRANSACTION_TYPE, intent.getBooleanExtra(DefineValue.TRANSACTION_TYPE, false));

                newFragment = new FragPayFriendsConfirm();
                newFragment.setArguments(args);
            }else
            {
                Bundle args = new Bundle();
                if (bundle!=null) {
                    args.putBoolean(DefineValue.CONFIRM_PAYFRIEND, true);
                    args.putString(DefineValue.AMOUNT, bundle.getString(DefineValue.AMOUNT));
                    args.putString(DefineValue.CUST_NAME, bundle.getString(DefineValue.CUST_NAME));
                    args.putString(DefineValue.MESSAGE, bundle.getString(DefineValue.MESSAGE));
                    args.putString(DefineValue.USERID_PHONE, bundle.getString(DefineValue.USERID_PHONE));
                    args.putString(DefineValue.TRX, bundle.getString(DefineValue.TRX));
                    args.putString(DefineValue.REQUEST_ID, bundle.getString(DefineValue.REQUEST_ID));
                }
                newFragment = new FragPayFriends();
                newFragment.setArguments(args);
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.payfriends_confirm_token_content, newFragment,"payfriendconfirmtoken");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
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

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.payfriends_confirm_token_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.payfriends_confirm_token_content, mFragment, fragName)
                    .commit();

        }
        setActionBarTitle(fragName);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                break;
            case 2:
                break;
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pay_friends_confirm_token;
    }

    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.payfriends_ab_title_activity));
    }


    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}