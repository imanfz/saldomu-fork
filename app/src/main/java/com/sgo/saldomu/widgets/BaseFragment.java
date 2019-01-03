package com.sgo.saldomu.widgets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ProgBarDialog;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by LENOVO on 02/04/2018.
 */

public abstract class BaseFragment extends Fragment {

    protected SecurePreferences sp;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey, userNameLogin;
    protected String extraSignature = "";
    protected ProgBarDialog loadingDialog;
    protected ProgressDialog progdialog;
    ProgressDialog progressDialog;
    protected HashMap<String, Object> params;

    protected View v;
    protected Gson gson;
    JsonParser jsonParser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID, "");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID, "");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        userNameLogin = sp.getString(DefineValue.USER_NAME, "");
    }

    protected Gson getGson(){
        if (gson == null){
            gson = new Gson();
        }
        return gson;
    }

    protected JsonParser getJsonParser(){
        if (jsonParser == null){
            jsonParser = new JsonParser();
        }
        return jsonParser;
    }

    protected JsonElement toJson(Object model){
        return getJsonParser().parse(getGson().toJson(model));
    }

    protected void SwitchFragment(Fragment mFragment, String fragName, Boolean isBackstack){
        ToggleKeyboard.hide_keyboard(getActivity());
        FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
        fragManager.replace(R.id.denom_scadm_content, mFragment, fragName)
                .commitAllowingStateLoss();

        if (isBackstack) {
            Timber.d("backstack");
            fragManager.addToBackStack(fragName);
        } else {
            Timber.d("bukan backstack");

        }

    }

    void buildLoadingDialog() {
        loadingDialog = ProgBarDialog.showLoading();
    }

    ProgressDialog getProgDialog() {
        if (progressDialog == null)
            progressDialog = DefinedDialog.CreateProgressDialog(getActivity());
        return progressDialog;
    }

    protected void showProgressDialog() {
        if (!getProgDialog().isShowing())
            getProgDialog().show();
    }

    protected void dismissProgressDialog() {
        if (getProgDialog().isShowing())
            getProgDialog().dismiss();
    }

    protected void showLoading() {
        if (loadingDialog == null) {
            buildLoadingDialog();
        }
        loadingDialog.show(getFragmentManager(), "loading_dialog");
    }

    protected void dismissLoading() {
        try {
            loadingDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public FragmentManager getFragManager(){
        return getActivity().getSupportFragmentManager();
    }
}
