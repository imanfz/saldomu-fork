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
import com.sgo.saldomu.fragments.FragListDenomSCADM;
import com.sgo.saldomu.fragments.FragmentDenomConfirm;
import com.sgo.saldomu.fragments.FragmentDenomInputPromoCode;
import com.sgo.saldomu.fragments.InputPartnerCode;
import com.sgo.saldomu.widgets.BaseActivity;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/14/2018.
 */

public class DenomSCADMActivity extends BaseActivity {
    FragmentManager fragmentManager;
    Fragment newFragment = null;

    public static String DENOM_PAYMENT = "denom_payment";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_denom_scadm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        if (findViewById(R.id.denom_scadm_content) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Intent intent = getIntent();
            if (intent.hasExtra(DefineValue.FAVORITE_CUSTOMER_ID)) {
                String comm_id = intent.getStringExtra(DefineValue.COMM_ID_SCADM);
                String comm_code = intent.getStringExtra(DefineValue.COMMUNITY_CODE);
                String comm_name = intent.getStringExtra(DefineValue.COMMUNITY_NAME);
                String member_code = intent.getStringExtra(DefineValue.MEMBER_CODE);
                String member_id_scadm = intent.getStringExtra(DefineValue.MEMBER_ID_SCADM);

                SCADMCommunityModel scadmCommunityModel = new SCADMCommunityModel();
                scadmCommunityModel.setComm_id(comm_id);
                scadmCommunityModel.setComm_code(comm_code);
                scadmCommunityModel.setComm_name(comm_name);
                scadmCommunityModel.setMember_code(member_code);
                scadmCommunityModel.setMember_id_scadm(member_id_scadm);
                DataManager.getInstance().setSCADMCommMod(scadmCommunityModel);

                Bundle bundle = new Bundle();
                bundle.putString(DefineValue.CUST_ID, intent.getStringExtra(DefineValue.FAVORITE_CUSTOMER_ID));
                bundle.putString(DefineValue.COMMUNITY_ID, intent.getStringExtra(DefineValue.COMM_ID_SCADM));
                bundle.putString(DefineValue.COMMUNITY_CODE, intent.getStringExtra(DefineValue.COMMUNITY_CODE));
                bundle.putString(DefineValue.MEMBER_CODE, intent.getStringExtra(DefineValue.MEMBER_CODE));
                bundle.putString(DefineValue.MEMBER_ID_SCADM, intent.getStringExtra(DefineValue.MEMBER_ID_SCADM));
                newFragment = new InputPartnerCode();
                newFragment.setArguments(bundle);
            } else
                newFragment = new FragListDenomSCADM();
        }

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.denom_scadm_content, newFragment);
        fragmentTransaction.commitAllowingStateLoss();
        setResult(MainPage.RESULT_NORMAL);
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.scadm_denom));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setResultActivity() {
        setResult(MainPage.RESULT_BALANCE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.denom_scadm_content);
        if (fragment instanceof FragmentDenomInputPromoCode || fragment instanceof FragmentDenomConfirm ) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment).commit();
        } else
            super.onBackPressed();
    }
}
