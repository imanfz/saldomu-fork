package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.fragments.FragNotification;
import com.sgo.saldomu.fragments.FragSourceOfFund;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class SourceOfFundActivity extends BaseActivity
{

    private String txId;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_sof;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        if (findViewById(R.id.sofactivity_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Intent intent = getIntent();
            if (intent.hasExtra(DefineValue.TX_ID)) {
                txId = intent.getStringExtra(DefineValue.TX_ID);
            }

            Bundle bundle = new Bundle();
            bundle.putString(DefineValue.TX_ID, txId);

            Fragment newFragment = new FragSourceOfFund();
            newFragment.setArguments(bundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.sofactivity_content, newFragment,"sourceOfFund");
            fragmentTransaction.commit();
        }
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.sofactivity_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.sofactivity_content, mFragment, fragName)
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

    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.payment_confirm));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        RetrofitService.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setResultActivity(int result){
        setResult(MainPage.RESULT_BALANCE);
    }
}
