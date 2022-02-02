package com.sgo.saldomu.widgets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.dialogs.DefinedDialog;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by LENOVO on 02/04/2018.
 */

public abstract class BaseFragment extends Fragment {

    protected SecurePreferences sp;
    protected String memberIDLogin, commIDLogin, userPhoneID, accessKey, userNameLogin;
    protected String extraSignature = "";
    private ProgressDialog progressDialog;
    protected HashMap<String, Object> params;

    protected View v;
    protected Gson gson;
    JsonParser jsonParser;
    private LevelClass levelClass;

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

    protected LevelClass getLvlClass() {
        if (levelClass == null)
            levelClass = new LevelClass(getActivity());
        return levelClass;
    }

    protected Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    protected JsonParser getJsonParser() {
        if (jsonParser == null) {
            jsonParser = new JsonParser();
        }
        return jsonParser;
    }

    protected JsonElement toJson(Object model) {
        return getJsonParser().parse(getGson().toJson(model));
    }

    protected void SwitchFragment(Fragment mFragment, String fragName, Boolean isBackstack) {
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

    protected void SwitchFragmentTop(Fragment mFragment, String fragName, Boolean isBackstack) {
        ToggleKeyboard.hide_keyboard(getActivity());
        FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
        fragManager.replace(R.id.topup_scadm_content, mFragment, fragName)
                .commitAllowingStateLoss();

        if (isBackstack) {
            Timber.d("backstack");
            fragManager.addToBackStack(fragName);
        } else {
            Timber.d("bukan backstack");

        }

    }

    public void addFragment(Fragment mFragment, String fragName) {
        ToggleKeyboard.hide_keyboard(getActivity());
        FragmentTransaction fragManager = getActivity().getSupportFragmentManager().beginTransaction();
        fragManager.add(R.id.denom_scadm_content, mFragment, fragName).commit();
    }

    ProgressDialog getProgDialog() {
        if (progressDialog == null && getActivity() != null)
            progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), getString(R.string.please_wait));
        return progressDialog;
    }

    protected void showProgressDialog() {
        if (getProgDialog() != null)
            if (!getProgDialog().isShowing())
                if (getActivity() != null)
                    if (!getActivity().isFinishing())
                        getProgDialog().show();
    }

    protected void dismissProgressDialog() {
        if (getProgDialog() != null)
            if (getProgDialog().isShowing())
                getProgDialog().dismiss();
    }

    protected FragmentManager getFragManager() {
        return getActivity().getSupportFragmentManager();
    }

    protected void hideView(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }

    protected void showView(View view) {
        if (view.getVisibility() != View.VISIBLE)
            view.setVisibility(View.VISIBLE);
    }
}
