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

import timber.log.Timber;

/**
 * Created by LENOVO on 02/04/2018.
 */

public abstract class BaseFragment extends Fragment {

    protected SecurePreferences sp;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey;
    protected String extraSignature = "";
    protected ProgBarDialog loadingDialog;
<<<<<<< HEAD
    protected ProgressDialog progdialog;
=======
    ProgressDialog progressDialog;
>>>>>>> 476900affe9d972c4914c9bac3468360f183c54b

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
    }

<<<<<<< HEAD
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
=======
    protected void SwitchFragment(Fragment mFragment, String fragName, Boolean isBackstack) {
>>>>>>> 476900affe9d972c4914c9bac3468360f183c54b
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
            progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        return progressDialog;
    }

    protected void showProgressDialog() {
        getProgDialog();
//        if (!getProgDialog().isShowing())
//            getProgDialog().show();
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

<<<<<<< HEAD
    ProgressDialog getProgressDialog(){
        if (progdialog == null)
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        return progdialog;
    }

    protected void showProgressDialog(){
        if (!getProgressDialog().isShowing())
            getProgressDialog().show();
    }

    protected void dismissProgressDialog(){
        if (getProgressDialog().isShowing())
            getProgressDialog().dismiss();
    }

    public FragmentManager getFragManager(){
=======
    public FragmentManager getFragManager() {
>>>>>>> 476900affe9d972c4914c9bac3468360f183c54b
        return getActivity().getSupportFragmentManager();
    }
}
