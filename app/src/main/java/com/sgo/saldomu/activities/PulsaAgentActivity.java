package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.PulsaAgentDescription;

import timber.log.Timber;

/**
 * Created by thinkpad on 9/11/2015.
 */
public class PulsaAgentActivity extends BaseActivity {

    private String member_id;
    private String item_id;
    private String item_name;
    private String phone_number;
    private String share_type;
    private String operator_id;
    private String operator_name;

    public final static String FRAG_PULSA_DESCRIPTION = "pulsaDesc";
    public final static String FRAG_PULSA_CONFIRM = "pulsaConfirm";
    public static final int RESULT_DAP = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        member_id = intent.getStringExtra(DefineValue.MEMBER_ID);
        item_id = intent.getStringExtra(DefineValue.DENOM_ITEM_ID);
        item_name = intent.getStringExtra(DefineValue.DENOM_ITEM_NAME);
        phone_number = intent.getStringExtra(DefineValue.PHONE_NUMBER);
        share_type = intent.getStringExtra(DefineValue.SHARE_TYPE);
        operator_id = intent.getStringExtra(DefineValue.OPERATOR_ID);
        operator_name = intent.getStringExtra(DefineValue.OPERATOR_NAME);

        initializePulsaAgent();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pulsa_agent;
    }

    private void initializePulsaAgent(){
        Bundle mArgs = new Bundle();
        Fragment newFragment = new PulsaAgentDescription();
        mArgs.putString(DefineValue.MEMBER_ID, member_id);
        mArgs.putString(DefineValue.DENOM_ITEM_ID, item_id);
        mArgs.putString(DefineValue.DENOM_ITEM_NAME, item_name);
        mArgs.putString(DefineValue.PHONE_NUMBER, phone_number);
        mArgs.putString(DefineValue.SHARE_TYPE, share_type);
        mArgs.putString(DefineValue.OPERATOR_ID, operator_id);
        mArgs.putString(DefineValue.OPERATOR_NAME, operator_name);
        newFragment.setArguments(mArgs);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.pulsa_agent_content, newFragment,getString(R.string.toolbar_title_pulsa_agent));
        fragmentTransaction.commit();
        setResult(MainPage.RESULT_NORMAL);
    }


    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){
        Log.wtf("masuk turnOnBR","oke");
        if(_on){
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(_myreceiver,filter);
            filter.setPriority(999);
            filter.addCategory("android.intent.category.DEFAULT");
        }
        else unregisterReceiver(_myreceiver);

    }


    public void switchContent(Fragment mFragment,String fragName,String next_frag_title,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack", "masuk");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.pulsa_agent_content, mFragment, fragName)
                    .addToBackStack(fragName)
                    .commit();
        }
        else {
            Timber.d("bukan backstack","masuk");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.pulsa_agent_content, mFragment, fragName)
                    .commit();

        }
        if(next_frag_title!=null)setActionBarTitle(next_frag_title);
        ToggleKeyboard.hide_keyboard(this);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                this.setResult(MainPage.RESULT_BALANCE);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }

    public void setResultActivity(){
        setResult(MainPage.RESULT_BALANCE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Pulsa Activity"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
//            Timber.d("onActivity result", "Pulsa Activity masuk request exit");
            if(resultCode == RESULT_DAP){
                finish();
            }
            else if (resultCode == MainPage.RESULT_LOGOUT) {
                setResult(MainPage.RESULT_LOGOUT);
                finish();
            }

        }
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

    public void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.toolbar_title_pulsa_agent));
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount()>0)getFragmentManager().popBackStack();
        else super.onBackPressed();
    }
}
