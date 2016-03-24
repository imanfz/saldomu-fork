package com.sgo.orimakardaya.coreclass;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.sgo.orimakardaya.R;

/*
  Created by Administrator on 11/24/2014.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar detoolbar;
    protected TextView title_detoolbar;
    protected ProgressBar deprogressbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        detoolbar = (Toolbar) findViewById(R.id.main_toolbar);
        deprogressbar = (ProgressBar) findViewById(R.id.main_toolbar_progress_spinner);

        title_detoolbar = (TextView) findViewById(R.id.main_toolbar_title);
        if (detoolbar != null) {
            setSupportActionBar(detoolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        detoolbar.setNavigationIcon(iconRes);
    }

    protected void disableHomeIcon() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    protected void setActionBarTitle(String _title) {
        title_detoolbar.setText(_title);
    }

    protected String getActionBarTitle() {
        return title_detoolbar.getText().toString();
    }

    protected Toolbar getToolbar() {
        return detoolbar;
    }

    protected ProgressBar getProgressSpinner() {
        return deprogressbar;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.wtf(this.getClass().getSimpleName(), "onStart");
        Log.wtf("is Application visible", String.valueOf(LifeCycleHandler.isApplicationVisible()));
        Log.wtf("is Application foreground", String.valueOf(LifeCycleHandler.isApplicationInForeground()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.wtf(this.getClass().getSimpleName(), "onResume");
        Log.wtf("is Application visible", String.valueOf(LifeCycleHandler.isApplicationVisible()));
        Log.wtf("is Application foreground", String.valueOf(LifeCycleHandler.isApplicationInForeground()));
    }

    @Override
    protected void onPause() {
//        serviceReferenceBalance.StopCallBalance();
        super.onPause();
        Log.wtf(this.getClass().getSimpleName(), "onPause");
        Log.wtf("is Application visible", String.valueOf(LifeCycleHandler.isApplicationVisible()));
        Log.wtf("is Application foreground", String.valueOf(LifeCycleHandler.isApplicationInForeground()));
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.wtf(this.getClass().getSimpleName(), "onStop");
        Log.wtf("is Application visible", String.valueOf(LifeCycleHandler.isApplicationVisible()));
        Log.wtf("is Application foreground", String.valueOf(LifeCycleHandler.isApplicationInForeground()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.wtf(this.getClass().getSimpleName(), "onDestroy");
        Log.wtf("is Application visible", String.valueOf(LifeCycleHandler.isApplicationVisible()));
        Log.wtf("is Application foreground", String.valueOf(LifeCycleHandler.isApplicationInForeground()));
    }
}