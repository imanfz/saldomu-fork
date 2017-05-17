package com.sgo.hpku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.fragments.FragFriendsViewDetail;

import timber.log.Timber;

/**
 * Created by thinkpad on 3/23/2015.
 */
public class FriendsViewDetailActivity extends BaseActivity {

    private SecurePreferences sp;
    private FragmentManager fragmentManager;

    private int RESULT;
    private String imgUrl;
    private String name;
    private String id;
    private String phone;
    private String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sp = CustomSecurePref.getInstance().getmSecurePrefs();


        Intent i = getIntent();
        if(i != null) {
            imgUrl = i.getStringExtra("image");
            name = i.getStringExtra("name");
            id = i.getStringExtra("id");
            phone = i.getStringExtra("phone");
            email = i.getStringExtra("email");
        }

        InitializeToolbar();

        if (findViewById(R.id.friend_detail_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            FragFriendsViewDetail mFrag = new FragFriendsViewDetail();
            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.friend_detail_content, mFrag, "Friend detail");
            fragmentTransaction.commit();
//            setResult(MainPage.RESULT_NORMAL);
        }

        RESULT = MainPage.RESULT_NORMAL;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_friends_view_detail;
    }

    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.friend_detail_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.friend_detail_content, mFragment, fragName)
                    .commit();

        }

        setActionBarTitle(name+" - "+fragName);
    }

    public void setToolbarTitle(String _title) {
        setActionBarTitle(_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == MainPage.RESULT_BALANCE) {
                Timber.d("masuk friendsViewDetail Activity");
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }
}
