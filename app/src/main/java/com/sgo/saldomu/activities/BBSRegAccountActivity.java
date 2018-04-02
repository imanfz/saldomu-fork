package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.sgo.saldomu.R;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.BBSConfirmAcct;
import com.sgo.saldomu.fragments.BBSRegisterAcct;

import timber.log.Timber;


public class BBSRegAccountActivity extends BaseActivity implements BBSRegisterAcct.ActionListener,BBSConfirmAcct.ActionListener {

    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();

        if (findViewById(R.id.bbsreg_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Intent intent    = getIntent();
            Fragment newFragment = new BBSRegisterAcct();
            newFragment.setArguments(intent.getExtras());


            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.bbsreg_content, newFragment, BBSRegisterAcct.TAG);
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
        InitializeToolbar();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs_reg;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.register_bank_account));
        if(getIntent().getExtras().containsKey(DefineValue.IS_UPDATE)){
            if(getIntent().getExtras().getBoolean(DefineValue.IS_UPDATE))
                setActionBarTitle(getString(R.string.update_bank_account));
        }
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bbsreg_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();

        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bbsreg_content, mFragment, fragName)
                    .commitAllowingStateLoss();

        }

        if(!fragName.isEmpty())
            setActionBarTitle(fragName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount()>1)
            fragmentManager.popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public void OnSuccessReqAcct(Bundle data) {
        Fragment fragment = new BBSConfirmAcct();
        if(getIntent().getExtras().containsKey(DefineValue.IS_UPDATE))
            data.putBoolean(DefineValue.IS_UPDATE,getIntent().getExtras().getBoolean(DefineValue.IS_UPDATE,false));
        fragment.setArguments(data);
        switchContent(fragment,"",true);
    }

    @Override
    public void OnEmptyCommunity() {
        onBackPressed();
    }

    @Override
    public void onSuccess() {
        setResult(MainPage.RESULT_OK);
        this.finish();
    }
}
