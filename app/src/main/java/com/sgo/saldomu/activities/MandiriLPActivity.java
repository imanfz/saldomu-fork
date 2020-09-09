package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragMandiriLP;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class MandiriLPActivity extends BaseActivity {

    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.content) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment newFragment;
            newFragment = new FragMandiriLP();

            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content, newFragment,getString(R.string.menu_item_title_mandiri_lkd));
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_mandiri_lp;
    }

    public void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_mandiri_lkd));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
