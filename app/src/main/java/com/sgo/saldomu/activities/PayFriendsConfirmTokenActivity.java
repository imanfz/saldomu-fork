package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fragments.FragPayFriendsConfirm;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/*
  Created by Administrator on 12/10/2014.
 */
public class PayFriendsConfirmTokenActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.payfriends_confirm_token_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();

            Intent intent    = getIntent();
            Bundle args = new Bundle();
            args.putString(WebParams.DATA_TRANSFER, intent.getStringExtra(WebParams.DATA_TRANSFER));
            args.putString(WebParams.DATA, intent.getStringExtra(WebParams.DATA));
            args.putString(WebParams.MESSAGE, intent.getStringExtra(WebParams.MESSAGE));
            args.putString(WebParams.DATA_MAPPER, intent.getStringExtra(WebParams.DATA_MAPPER));
            args.putBoolean(DefineValue.TRANSACTION_TYPE, intent.getBooleanExtra(DefineValue.TRANSACTION_TYPE,false));

            Fragment newFragment = new FragPayFriendsConfirm();
            newFragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.payfriends_confirm_token_content, newFragment,"payfriendconfirmtoken");
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pay_friends_confirm_token;
    }

    private void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.payfriends_ab_title_activity));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }
}