package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fragments.FragPayFriendsConfirm;
import timber.log.Timber;

/*
  Created by Administrator on 12/10/2014.
 */
public class PayFriendsConfirmTokenActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.payfriends_confirm_token_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
            String _memberID = sp.getString(DefineValue.MEMBER_ID, "");

            Intent intent    = getIntent();
            Bundle args = new Bundle();
            args.putString(WebParams.DATA_TRANSFER, intent.getStringExtra(WebParams.DATA_TRANSFER));
            args.putString(WebParams.DATA, intent.getStringExtra(WebParams.DATA));
            args.putString(WebParams.MESSAGE, intent.getStringExtra(WebParams.MESSAGE));
            args.putString(WebParams.DATA_MAPPER, intent.getStringExtra(WebParams.DATA_MAPPER));
            args.putBoolean(DefineValue.TRANSACTION_TYPE, intent.getBooleanExtra(DefineValue.TRANSACTION_TYPE,false));

            Fragment newFragment = new FragPayFriendsConfirm();
            newFragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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

    private void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.payfriends_ab_title_activity));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }
}