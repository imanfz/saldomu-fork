package com.sgo.saldomu.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.fragments.BBSCashInConfirm;
import com.sgo.saldomu.fragments.BBSJoinAgentInput;
import com.sgo.saldomu.fragments.BBSTransaksiInformasi;
import com.sgo.saldomu.fragments.BBSTransaksiPager;
import com.sgo.saldomu.fragments.BBSTransaksiPagerItem;
import com.sgo.saldomu.fragments.Cashoutbbs_describ_member;
import com.sgo.saldomu.fragments.FragApprovalAgent;
import com.sgo.saldomu.fragments.FragListSettingKelola;
import com.sgo.saldomu.fragments.FragMenuKelola;
import com.sgo.saldomu.fragments.FragSetttingKelola;
import com.sgo.saldomu.fragments.ListAccountBBS;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/25/2017.
 */

public class BBSActivity extends BaseActivity implements ListAccountBBS.ActionListener, BBSJoinAgentInput.ActionListener,
        BBSCashInConfirm.ActionListener, BBSTransaksiInformasi.ActionListener {

    public static final int JOINAGENT = 0;
    public static final int LISTACCBBS = 1;
    public static final int TRANSACTION = 2;
    public static final int CONFIRMCASHOUT = 4;

    public static final int BBSKELOLA           = 5;
    public static final int BBSAPPROVALAGENT    = 6;
    public static final int BBSTRXAGENT         = 7;


    FragmentManager fragmentManager;
    Fragment mContent;
    FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        if (findViewById(R.id.bbs_content) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Intent intent    = getIntent();

            Fragment newFragment = null;
            int index = intent.getIntExtra(DefineValue.INDEX,0);
            String tag = "agent";
            switch (index) {
                case JOINAGENT:
                    newFragment = new BBSJoinAgentInput();
                    tag = BBSJoinAgentInput.TAG;
                    break;
                case LISTACCBBS:
                    newFragment = new ListAccountBBS();
                    tag = ListAccountBBS.TAG;
                    break;
                case TRANSACTION:
                    newFragment = new BBSTransaksiPager();
                    tag = BBSTransaksiPager.TAG;
                    Bundle bundle = getIntent().getExtras();
                    if(bundle != null){
                        newFragment.setArguments(bundle);
                    }
                    break;
                case CONFIRMCASHOUT:
                    newFragment = new Cashoutbbs_describ_member();
                    tag = Cashoutbbs_describ_member.TAG;
                    break;
                case BBSKELOLA:
                    newFragment = new FragSetttingKelola();
                    tag = FragSetttingKelola.TAG;

                    //newFragment = new FragMenuKelola();
                    //tag = FragMenuKelola.TAG;
                    //Intent intentApproval = new Intent(getApplicationContext(), BbsListSettingKelolaActivity.class);
                    //intentApproval.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //intentApproval.putExtra("flagApprove", DefineValue.STRING_NO);
                    //startActivity(intentApproval);
                    break;
                case BBSAPPROVALAGENT:
                    //Intent intentApproval2 = new Intent(getApplicationContext(), BbsListSettingKelolaActivity.class);
                    //intentApproval2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //intentApproval2.putExtra("flagApprove", DefineValue.STRING_NO);
                    //startActivity(intentApproval2);
                    break;
                case BBSTRXAGENT:
                    newFragment = new FragApprovalAgent();
                    tag = FragApprovalAgent.TAG;
                    //Intent intentTrxAgent = new Intent(getApplicationContext(), BbsApprovalAgentActivity.class);
                    //intentTrxAgent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(intentTrxAgent);
                    break;

            }


            fab = (FloatingActionButton) findViewById(R.id.fab_add_account);
            mContent = newFragment;

            fragmentManager = getSupportFragmentManager();
            fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    InitializeTitle();
                }
            });
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.bbs_content, newFragment, tag);
            fragmentTransaction.commitAllowingStateLoss();
            setResult(MainPage.RESULT_NORMAL);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent    = getIntent();
        int index = intent.getIntExtra(DefineValue.INDEX,0);

        InitializeTitle();

    }

    private void InitializeTitle(){
        Fragment fragment = fragmentManager.findFragmentById(R.id.bbs_content);
        fab.setVisibility(View.GONE);
        if(fragment instanceof ListAccountBBS) {
            setActionBarTitle(getString(R.string.title_bbs_list_account_bbs));
            fab.setVisibility(View.VISIBLE);
        }else if(fragment instanceof BBSJoinAgentInput)
            setActionBarTitle(getString(R.string.join_agent));
        else if(fragment instanceof Cashoutbbs_describ_member)
            setActionBarTitle(getString(R.string.cash_out));
        else if(fragment instanceof BBSTransaksiPager)
            setActionBarTitle(getString(R.string.transaction));
        else if(fragment instanceof FragMenuKelola)
            setActionBarTitle(getString(R.string.menu_item_title_kelola));
        else if(fragment instanceof FragSetttingKelola)
            setActionBarTitle(getString(R.string.menu_item_title_kelola));
        else if(fragment instanceof FragApprovalAgent)
            setActionBarTitle(getString(R.string.menu_item_title_trx_agent));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_bbs;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_bbs));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainPage.REQUEST_FINISH) {
            if(resultCode == MainPage.RESULT_BBS){
                finish();
            }
            else if(resultCode == MainPage.RESULT_BBS_MEMBER_OTP){
                if(mContent instanceof Cashoutbbs_describ_member) {
                    Cashoutbbs_describ_member mFrag = (Cashoutbbs_describ_member) mContent;
                    mFrag.setMemberOTP(data.getStringExtra(DefineValue.BBS_MEMBER_OTP));
                }
            }
            else if (resultCode == MainPage.RESULT_LOGOUT) {
                setResult(MainPage.RESULT_LOGOUT);
                finish();
            }
            else if(resultCode == MainPage.RESULT_BBS_STATUS){
                if(mContent instanceof BBSTransaksiPager) {
                    BBSTransaksiPager mFrag = (BBSTransaksiPager) mContent;
                    Fragment confirmFrag =  mFrag.getConfirmFragment();
                    if(confirmFrag instanceof BBSTransaksiPagerItem) {
                        Fragment childFragment = ((BBSTransaksiPagerItem) confirmFrag).getChildFragment();
                        if(childFragment instanceof BBSCashInConfirm) {
                            BBSCashInConfirm cashInConfirm = (BBSCashInConfirm) childFragment;
                            cashInConfirm.setToStatus(data.getStringExtra(DefineValue.TX_STATUS));
                        }
                    }
                }

            }

        }

    }

    public void switchActivity(Intent mIntent, int j) {
        switch (j){
            case MainPage.ACTIVITY_RESULT:
                startActivityForResult(mIntent,MainPage.REQUEST_FINISH);
                this.setResult(MainPage.RESULT_BALANCE);
                break;
            case 2:
                break;
        }
        ToggleKeyboard.hide_keyboard(this);
    }

    public void switchContent(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(this);
        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bbs_content, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bbs_content, mFragment, fragName)
                    .commitAllowingStateLoss();

        }
        setActionBarTitle(fragName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnAddAccountListener() {
        Intent i = new Intent(this, BBSRegAccountActivity.class);
        i.putExtras(new Bundle());
        getSupportFragmentManager().findFragmentByTag(ListAccountBBS.TAG).startActivityForResult(i, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void OnUpdateAccountListener(Bundle data) {
        Intent i = new Intent(this, BBSRegAccountActivity.class);
        i.putExtras(data);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ListAccountBBS.TAG);
        fragment.startActivityForResult(i, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onBackPressed() {

        Intent intent    = getIntent();
        int index = intent.getIntExtra(DefineValue.INDEX,0);

        if ( index == BBSAPPROVALAGENT || index == BBSTRXAGENT || index == BBSKELOLA) {
            super.onBackPressed();
        } else {
            if (fragmentManager.getBackStackEntryCount() > 1)
                fragmentManager.popBackStack();
            else
                super.onBackPressed();
        }
    }

    @Override
    public void onFinishProcess() {
        this.finish();
    }

    @Override
    public void onCommunityEmpty() {
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApiClient.CancelRequestWS(this,true);
    }

    @Override
    public void ChangeActivityFromCashInput(Intent data) {
        switchActivity(data,MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void ChangeActivityFromCashInConfirm(Intent data) {
        switchActivity(data,MainPage.ACTIVITY_RESULT);
    }
}