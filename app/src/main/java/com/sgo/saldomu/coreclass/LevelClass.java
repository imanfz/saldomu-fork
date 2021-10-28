package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 3/6/2015.
 */

import android.app.Dialog;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.UpgradeMemberActivity;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.DefinedDialog;

public class LevelClass {

    private FragmentActivity activity;
    private SecurePreferences sp;
    private Boolean isAllowedLevel, isLevel1, isRegisteredLevel;


    public LevelClass(FragmentActivity activity, SecurePreferences sp) {
        this.setActivity(activity);
        this.setSp(sp);
        refreshData();
    }

    public LevelClass(FragmentActivity activity) {
        this.setActivity(activity);
        this.setSp(CustomSecurePref.getInstance().getmSecurePrefs());
        refreshData();
    }

    public Boolean isLevel1QAC() {
        return isAllowedLevel && isLevel1;
    }

    public void showDialogLevel() {
        refreshData();
        if (isRegisteredLevel) {

            final Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getActivity().getString(R.string.upgrade_dialog_finish_title),
                    getActivity().getString(R.string.level_dialog_waiting),
                    () -> {

                    }
            );

            dialognya.show();
        } else {
            final AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getActivity().getString(R.string.level_dialog_title),
                    getActivity().getString(R.string.level_dialog_message), getActivity().getString(R.string.level_dialog_btn_ok),
                    getActivity().getString(R.string.cancel), false);
            dialog_frag.setOkListener((dialog, which) -> {
                Intent mI = new Intent(getActivity(), UpgradeMemberActivity.class);
                getActivity().startActivityForResult(mI, MainPage.ACTIVITY_RESULT);
            });
            dialog_frag.setCancelListener((dialog, which) -> dialog_frag.dismiss());

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.add(dialog_frag, null);
            ft.commitAllowingStateLoss();
        }
    }

    public void refreshData() {
        if (sp.contains(DefineValue.LEVEL_VALUE)) {
            String i = sp.getString(DefineValue.LEVEL_VALUE, "0");
            isLevel1 = Integer.parseInt(i) == 1;
        }
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        isAllowedLevel = sp.getBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);
    }

    private FragmentActivity getActivity() {
        return activity;
    }

    private void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    private SecurePreferences getSp() {
        return sp;
    }

    private void setSp(SecurePreferences sp) {
        this.sp = sp;
    }
}
