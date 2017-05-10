package com.sgo.hpku.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;


import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.ToggleKeyboard;
import com.sgo.hpku.fragments.FragCashOut;
import com.sgo.hpku.fragments.FragCashOutAgen;

import timber.log.Timber;

/**
 * Created by thinkpad on 11/20/2015.
 */
public class CashoutActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        if (findViewById(R.id.cashout_confirm_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Intent intent    = getIntent();
            Bundle args = new Bundle();
            args.putString(DefineValue.TX_ID, intent.getStringExtra(DefineValue.TX_ID));
            args.putString(DefineValue.BANK_NAME, intent.getStringExtra(DefineValue.BANK_NAME));
            args.putString(DefineValue.ACCOUNT_NUMBER, intent.getStringExtra(DefineValue.ACCOUNT_NUMBER));
            args.putString(DefineValue.CCY_ID, intent.getStringExtra(DefineValue.CCY_ID));
            args.putString(DefineValue.NOMINAL, intent.getStringExtra(DefineValue.NOMINAL));
            args.putString(DefineValue.ACCT_NAME, intent.getStringExtra(DefineValue.ACCT_NAME));
            args.putString(DefineValue.FEE, intent.getStringExtra(DefineValue.FEE));
            args.putString(DefineValue.TOTAL_AMOUNT, intent.getStringExtra(DefineValue.TOTAL_AMOUNT));

            Fragment newFragment;
            if(intent.getIntExtra(DefineValue.CASHOUT_TYPE,0) == DefineValue.CASHOUT_AGEN)
                newFragment = new FragCashOutAgen();
            else
                newFragment = new FragCashOut();

            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.cashout_confirm_content, newFragment,"cashout");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){
        Timber.wtf("masuk turnOnBR cashout");
        if(_on){
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(_myreceiver,filter);
            filter.setPriority(999);
            filter.addCategory("android.intent.category.DEFAULT");
        }
        else unregisterReceiver(_myreceiver);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_cashout_confirm;
    }

    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_cash_out));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.cashout_confirm_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.cashout_confirm_content, mFragment, fragName)
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
}
