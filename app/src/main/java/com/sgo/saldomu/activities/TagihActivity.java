package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.activeandroid.util.Log;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.TagihModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragListCommunitySCADM;
import com.sgo.saldomu.fragments.FragTagihInput;
import com.sgo.saldomu.widgets.BaseActivity;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class TagihActivity extends BaseActivity {
    SecurePreferences sp;
    FragmentManager fragmentManager;
    Fragment mContent;
    Fragment newFragment = null;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_tagih;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        InitializeToolbar();

        if (findViewById(R.id.layout_tagih) != null) {
            if (savedInstanceState != null) {
                return;
            }

            newFragment = new FragTagihInput();
        }

        mContent = newFragment;

        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layout_tagih, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);

    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_tagih_agent));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    protected void onResume() {
        super.onResume();
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_tagih, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_tagih, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    public void setResultActivity(int result){
        setResult(MainPage.RESULT_BALANCE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
