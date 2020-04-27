package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragB2B;
import com.sgo.saldomu.fragments.ListTransfer;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class B2BActivity extends BaseActivity {

    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        if (findViewById(R.id.b2b_activity_content) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment newFragment;
            newFragment = new FragB2B();

            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.b2b_activity_content, newFragment,"b2b");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_b2b;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_scadm));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack, String tag) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.b2b_activity_content, mFragment, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.b2b_activity_content, mFragment, fragName)
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

}
