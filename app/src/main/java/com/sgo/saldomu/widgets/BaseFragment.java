package com.sgo.saldomu.widgets;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.dialogs.ProgBarDialog;

import timber.log.Timber;

/**
 * Created by LENOVO on 02/04/2018.
 */

public abstract class BaseFragment extends Fragment {

    protected SecurePreferences sp;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey;
    protected String extraSignature="";
    protected ProgBarDialog loadingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID,"");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID,"");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
    }

    protected void SwitchFragment(Fragment mFragment, String fragName, Boolean isBackstack){
        ToggleKeyboard.hide_keyboard(getActivity());
        FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
        fragManager.replace(R.id.denom_scadm_content, mFragment, fragName)
                .commitAllowingStateLoss();

        if(isBackstack){
            Timber.d("backstack");
            fragManager.addToBackStack(null);
        }
        else {
            Timber.d("bukan backstack");

        }

    }

    void buildLoadingDialog(){
        loadingDialog = ProgBarDialog.showLoading();
    }

    protected void showLoading(){
        if (loadingDialog == null) {
            buildLoadingDialog();
            loadingDialog.show(getFragmentManager(), "loading_dialog");
        }
    }

    protected void dismissLoading(){
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }
}
