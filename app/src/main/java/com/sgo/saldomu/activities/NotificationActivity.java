package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.fragments.FragNotification;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/*
  Created by Administrator on 5/6/2015.
 */
public class NotificationActivity extends BaseActivity {

    public final static int TYPE_LIKE = 2;
    public final static int TYPE_COMMENT = 3;
    public final static int TYPE_TRANSFER = 6;
    public final static int TYPE_PAID = 7;
    public final static int TYPE_DECLINE = 8;
    public final static int TYPE_NON_MEMBER= 10;
    public final static int CLAIM_NON_MEMBER= 11;
    public final static int REJECTED_KTP= 13;
    public final static int REJECTED_SIUP_NPWP= 14;
    public final static int BLAST_INFO= 15;

    public final static int P2PSTAT_PENDING = 1;
    public final static int P2PSTAT_PAID = 2;
    public final static int P2PSTAT_FAILED = 3;
    public final static int P2PSTAT_SUSPECT = 4;
    public final static int P2PSTAT_CANCELLED = 5;

    public final static int UNREAD = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        if (findViewById(R.id.notification_content) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment newFragment = new FragNotification();

            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.notification_content, newFragment,"notification");
            fragmentTransaction.commit();
        }
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.notification_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.notification_content, mFragment, fragName)
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
        setActionBarTitle(getString(R.string.notifications_ab_title));
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
    protected int getLayoutResource() {
        return R.layout.activity_notification;
    }
}