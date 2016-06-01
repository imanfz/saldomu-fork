package com.sgo.orimakardaya.coreclass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.Introduction;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.dialogs.DefinedDialog;

/*
  Created by Administrator on 11/24/2014.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar detoolbar;
    protected TextView title_detoolbar;
    protected ProgressBar deprogressbar;
    protected SMSclass smsClass;
    protected boolean isActive;


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

        smsClass = new SMSclass(this);

        smsClass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if(!isExist){
                    Intent i = new Intent(BaseActivity.this,Introduction.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    BaseActivity.this.startActivity(i);
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
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


}