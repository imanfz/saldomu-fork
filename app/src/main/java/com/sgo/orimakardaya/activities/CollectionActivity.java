package com.sgo.orimakardaya.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.fragments.ListCollectionPayment;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 3/4/2015.
 */
public class CollectionActivity extends BaseActivity {

    FragmentManager fragmentManager;
    String _collection_data;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle mBun = getIntent().getExtras();

        InitializeToolbar(mBun.getString(DefineValue.COMMUNITY_NAME,""));

        if (findViewById(R.id.collection_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Bundle mArgs = new Bundle();
            mArgs.putString(DefineValue.COMMUNITY_CODE,mBun.getString(DefineValue.COMMUNITY_CODE,""));
            mArgs.putString(DefineValue.COMMUNITY_ID,mBun.getString(DefineValue.COMMUNITY_ID,""));
            mArgs.putString(DefineValue.COMMUNITY_API_KEY,mBun.getString(DefineValue.COMMUNITY_API_KEY,""));
            mArgs.putString(DefineValue.CALLBACK_URL,mBun.getString(DefineValue.CALLBACK_URL,""));
            mArgs.putString(DefineValue.COMMUNITY_NAME,mBun.getString(DefineValue.COMMUNITY_NAME,""));
            Fragment mFrag = new ListCollectionPayment();
            mFrag.setArguments(mArgs);
            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.collection_content, mFrag,mBun.getString(DefineValue.COMMUNITY_NAME,""));
            fragmentTransaction.commit();
            setResult(MainPage.RESULT_NORMAL);

        }
    }


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_collection;
    }

    public String getToolbarTitle() {
        return getActionBarTitle();
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

    public void setResultActivity(int result){
        setResult(result);
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.collection_content, mFragment, fragName)
                    .addToBackStack(fragName)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.collection_content, mFragment, fragName)
                    .commit();

        }
        if(fragName!=null)setActionBarTitle(fragName);
        ToggleKeyboard.hide_keyboard(this);
    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                this.startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.wtf("masuk onActivityResultActivity");
        if(requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == MainPage.RESULT_BALANCE) {
                if(getSupportFragmentManager().getBackStackEntryCount()>1)
                    getSupportFragmentManager().popBackStack();
            }

        }

    }

    public void InitializeToolbar(String title){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(title);
    }

    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){
        Timber.wtf("masuk turnOnBR");
        if(_on){
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(_myreceiver,filter);
            filter.setPriority(999);
            filter.addCategory("android.intent.category.DEFAULT");
        }
        else unregisterReceiver(_myreceiver);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount()>0)getFragmentManager().popBackStack();
        else super.onBackPressed();
    }
}