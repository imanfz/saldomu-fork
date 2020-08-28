package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivityOTP;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragCashOut;
import com.sgo.saldomu.interfaces.TransactionResult;

import timber.log.Timber;

/**
 * Created by thinkpad on 11/20/2015.
 */
public class CashoutActivity extends BaseActivityOTP implements TransactionResult {
    FragmentManager fragmentManager;

//    @Override
//    public void onAttachFragment(Fragment fragment) {
//        super.onAttachFragment(fragment);
//        setCurrentFragment(fragment);
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.cashout_confirm_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

//            Intent intent    = getIntent();
//            Bundle args = new Bundle();
//            args.putString(DefineValue.TX_ID, intent.getStringExtra(DefineValue.TX_ID));
//            args.putString(DefineValue.BANK_NAME, intent.getStringExtra(DefineValue.BANK_NAME));
//            args.putString(DefineValue.ACCOUNT_NUMBER, intent.getStringExtra(DefineValue.ACCOUNT_NUMBER));
//            args.putString(DefineValue.CCY_ID, intent.getStringExtra(DefineValue.CCY_ID));
//            args.putString(DefineValue.NOMINAL, intent.getStringExtra(DefineValue.NOMINAL));
//            args.putString(DefineValue.ACCT_NAME, intent.getStringExtra(DefineValue.ACCT_NAME));
//            args.putString(DefineValue.FEE, intent.getStringExtra(DefineValue.FEE));
//            args.putString(DefineValue.TOTAL_AMOUNT, intent.getStringExtra(DefineValue.TOTAL_AMOUNT));

            Fragment newFragment;
//            if(intent.getIntExtra(DefineValue.CASHOUT_TYPE,0) == DefineValue.CASHOUT_AGEN)
//                newFragment = new FragCashOutAgen();
//            else if(intent.getIntExtra(DefineValue.CASHOUT_TYPE,0) == DefineValue.CASHOUT_BANK)
                newFragment = new FragCashOut();
//            else if(intent.getIntExtra(DefineValue.CASHOUT_TYPE,0) == DefineValue.CASHOUT_LKD)
//                newFragment = new FragCashoutMember();

            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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

    public void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_cash_out));
    }

    public void setTitleToolbar(String title) {
        setActionBarTitle(title);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack, String tag) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.cashout_confirm_content, mFragment, tag)
                    .addToBackStack(tag)
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitService.dispose();
    }

    @Override
    public void TransResult(Boolean isSuccess) {
        if(isSuccess) {
            setResult(MainPage.RESULT_BALANCE);
        }
        else {
            setResult(MainPage.RESULT_NORMAL);
        }

        finish();
    }
}
