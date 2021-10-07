package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fragments.FragListTopUpSCADM;
import com.sgo.saldomu.fragments.FragTopUpSCADM;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/14/2018.
 */

public class TopUpSCADMActivity extends BaseActivity {
    FragmentManager fragmentManager;
    Fragment newFragment = null;
    public static String TOPUP = "topup";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_topup_scadm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.topup_scadm_content) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Intent intent = getIntent();
            if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                String comm_id = intent.getStringExtra(DefineValue.COMM_ID_SCADM);
                String comm_code = intent.getStringExtra(DefineValue.COMMUNITY_CODE);
                String comm_name = intent.getStringExtra(DefineValue.COMMUNITY_NAME);
                String api_key = intent.getStringExtra(DefineValue.API_KEY);
                String member_code = intent.getStringExtra(DefineValue.MEMBER_CODE);
                String member_id_scadm = intent.getStringExtra(DefineValue.MEMBER_ID_SCADM);

                SCADMCommunityModel scadmCommunityModel = new SCADMCommunityModel();
                scadmCommunityModel.setComm_id(comm_id);
                scadmCommunityModel.setComm_code(comm_code);
                scadmCommunityModel.setComm_name(comm_name);
                scadmCommunityModel.setApi_key(api_key);
                scadmCommunityModel.setMember_code(member_code);
                scadmCommunityModel.setMember_id_scadm(member_id_scadm);
                DataManager.getInstance().setSCADMCommMod(scadmCommunityModel);

                Bundle bundle = new Bundle();
                bundle.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
                bundle.putString(DefineValue.COMMUNITY_NAME, comm_name);
                bundle.putString(DefineValue.COMM_ID_SCADM, comm_id);
                bundle.putString(DefineValue.COMMUNITY_CODE, comm_code);
                bundle.putString(DefineValue.API_KEY, api_key);
                bundle.putString(DefineValue.MEMBER_CODE, member_code);
                bundle.putString(DefineValue.MEMBER_ID_SCADM, member_id_scadm);
                newFragment = new FragTopUpSCADM();
                newFragment.setArguments(bundle);
            } else
                newFragment = new FragListTopUpSCADM();
        }

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.topup_scadm_content, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.scadm_topup));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setResultActivity(int result){
        setResult(result);
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
}
