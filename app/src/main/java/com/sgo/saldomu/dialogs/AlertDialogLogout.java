package com.sgo.saldomu.dialogs;/*
  Created by Administrator on 1/26/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.Perkenalan;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

import timber.log.Timber;

public class AlertDialogLogout {

    private static AlertDialogLogout instance = null;
    private static AlertDialog adInstance;

    private AlertDialogLogout() {
        // Exists only to defeat instantiation.
    }

    public static AlertDialogLogout getInstance() {
        if (instance == null) {
            instance = new AlertDialogLogout();
        }
        return instance;
    }


    public void showDialoginActivity(final Activity mContext, String message) {
        if (mContext != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getResources().getString(R.string.logout)).setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(mContext.getResources().getString(R.string.ok), (dialog, which) -> {
                        setSessionTimeout();
                        Intent intent = new Intent(mContext, Perkenalan.class);
                        intent.putExtra(DefineValue.LOG_OUT, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mContext.startActivity(intent);
                    });
            if (getAdInstance() == null) {
                setAdInstance(builder.create());
                getAdInstance().show();
            } else if (!getAdInstance().isShowing()) {
                setAdInstance(builder.create());
                getAdInstance().show();
            }
        }
    }

    public void showDialoginMain(final Activity mContext, String message) {
        if (mContext != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getResources().getString(R.string.logout)).setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(mContext.getResources().getString(R.string.ok), (dialog, which) -> {
                        setSessionTimeout();
                        MainPage fca = (MainPage) mContext;
                        fca.switchLogout();
                    });

            Timber.d("showDialoginMain");
            if (getAdInstance() == null) {
                Timber.d("showDialoginMain");
                setAdInstance(builder.create());
                getAdInstance().show();
            } else if (!getAdInstance().isShowing()) {
                Timber.d("showDialoginMain");
                setAdInstance(builder.create());
                getAdInstance().show();
            }
        }
    }

    private void setSessionTimeout() {
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        sp.edit().putBoolean(DefineValue.LOGOUT_FROM_SESSION_TIMEOUT, true).commit();
    }

    private AlertDialog getAdInstance() {
        return adInstance;
    }

    private void setAdInstance(AlertDialog adInstance) {
        AlertDialogLogout.adInstance = adInstance;
    }
}
