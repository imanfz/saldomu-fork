package com.sgo.saldomu.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.FragmentSearch;
import com.sgo.saldomu.utils.LocaleManager;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

public class ActivitySearch extends BaseActivity {
    public static final int TYPE_SEARCH_CONTACT = 2;
    FrameLayout content;
    Bundle incomingBundle;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_search;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        content = findViewById(R.id.content);

        initializeToolbar();
        InitializeData();
        InitializeDashboard();
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.listcontact_text_title));
    }

    private void InitializeData() {

        if (getIntent().hasExtra(DefineValue.BUNDLE_FRAG)) {
            Bundle bundle = getIntent().getBundleExtra(DefineValue.BUNDLE_FRAG);
            incomingBundle = bundle;
        }
    }


    private void InitializeDashboard() {

        Fragment frag = new FragmentSearch();

        if (incomingBundle != null) {
            frag.setArguments(incomingBundle);
        }
        addFragment(frag);
    }


    private void addFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
                .add(content.getId(), fragment)
                .commitAllowingStateLoss();

    }

    public void switchActivity(Class<?> clsName, Bundle bundle) {
        ToggleKeyboard.hide_keyboard(this);

        Intent intent = new Intent(this, clsName);
        if (bundle.size() > 0) {
            intent.putExtra(DefineValue.BUNDLE_FRAG, bundle);
        }
        startActivity(intent);
    }

    public void switchFragment(Fragment fragment, String fragName, boolean isBackStack) {
        ToggleKeyboard.hide_keyboard(this);
        if (isBackStack) {
            getSupportFragmentManager().beginTransaction()
                    .replace(content.getId(), fragment, fragName)
                    .addToBackStack(fragName)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(content.getId(), fragment, fragName)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ToggleKeyboard.hide_keyboard(this);

        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Timber.wtf("on Configuration Changed....");

    }

}
