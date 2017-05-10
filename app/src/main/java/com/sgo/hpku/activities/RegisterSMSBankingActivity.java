package com.sgo.hpku.activities;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.ToggleKeyboard;
import com.sgo.hpku.fragments.FragRegisterSMSBanking;
import timber.log.Timber;

/**
 * Created by thinkpad on 6/12/2015.
 */
public class RegisterSMSBankingActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        if (findViewById(R.id.register_sms_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            String bank_name = getIntent().getStringExtra(DefineValue.BANK_NAME);

            Bundle mBun = new Bundle();
            Fragment newFragment = new FragRegisterSMSBanking();
            mBun.putString(DefineValue.BANK_NAME, bank_name);
            newFragment.setArguments(mBun);

            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.register_sms_content, newFragment,"registersmsbanking");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.register_sms_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.register_sms_content, mFragment, fragName)
                    .commit();

        }
        setActionBarTitle(fragName);
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
    protected int getLayoutResource() {
        return R.layout.activity_register_sms_banking;
    }

    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.title_register_sms_banking));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

}
