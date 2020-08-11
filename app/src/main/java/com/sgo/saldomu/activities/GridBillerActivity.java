package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragGridBiller;
import com.sgo.saldomu.fragments.FragGridEmoney;
import com.sgo.saldomu.widgets.BaseActivity;

import io.realm.Realm;
import timber.log.Timber;

import static com.sgo.saldomu.fragments.FragHomeNew.BILLER_TYPE_CODE_EMONEY;

/**
 * Created by Lenovo Thinkpad on 12/11/2017.
 */

public class GridBillerActivity extends BaseActivity {

    private SecurePreferences sp;
    Fragment fragment;
    FragmentManager fragmentManager;
    private Realm realm;
    private String billerType;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_biller;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.ic_arrow_left);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        Intent intent = getIntent();
        billerType = intent.getStringExtra(DefineValue.BILLER_TYPE);
        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        initializeData();
    }

    private void initializeData() {
        if (billerType.equals(BILLER_TYPE_CODE_EMONEY)) {
            fragment = new FragGridEmoney();
            setToolbarTitle(getString(R.string.newhome_emoney));
        } else {
            fragment = new FragGridBiller();
            setToolbarTitle(getString(R.string.menu_item_title_biller));
        }


        Bundle bundle = new Bundle();
        bundle.putString(DefineValue.BILLER_TYPE, billerType);
        fragment.setArguments(bundle);
        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commitAllowingStateLoss();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (!realm.isInTransaction() && !realm.isClosed())
            realm.close();

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public void switchContent(Fragment mFragment, String fragName, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, mFragment)
                .addToBackStack(tag)
                .commitAllowingStateLoss();
        setToolbarTitle(fragName);
    }

    public void setToolbarTitle(String title) {
        setActionBarTitle(title);
    }
}
