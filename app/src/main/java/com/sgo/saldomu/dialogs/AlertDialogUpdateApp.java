package com.sgo.saldomu.dialogs;/*
  Created by Administrator on 1/26/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;

import timber.log.Timber;

public class AlertDialogUpdateApp {

    private static AlertDialogUpdateApp instance = null;
    private static AlertDialog adInstance;

    private AlertDialogUpdateApp() {
        // Exists only to defeat instantiation.
    }

    public static AlertDialogUpdateApp getInstance() {
        if (instance == null) {
            instance = new AlertDialogUpdateApp();
        }
        return instance;
    }

    public void showDialogUpdate(final Activity mContext, String type, String package_name, String download_url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.update)).setMessage(mContext.getResources().getString(R.string.update_msg))
                .setCancelable(true)
                .setPositiveButton(mContext.getResources().getString(R.string.ok), (dialog, which) ->
                {
                    if (type.equalsIgnoreCase("1")) {
                        try {
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                        }
                    } else if (type.equalsIgnoreCase("2")) {
                        String url = download_url;
                        if (!Patterns.WEB_URL.matcher(url).matches())
                            url = "http://www.google.com";
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }

                    switchLogout(mContext);
                    mContext.finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                    mContext.getParent().finish();
                });

        Timber.d("showDialogUpdate");
        if (getAdInstance() == null) {
            Timber.d("showDialogUpdate");
            setAdInstance(builder.create());
            getAdInstance().show();
        } else if (!getAdInstance().isShowing()) {
            Timber.d("showDialogUpdate");
            setAdInstance(builder.create());
            getAdInstance().show();
        }
    }


    private void switchLogout(Activity mActivity) {
        if (mActivity == null)
            return;

        MainPage fca = (MainPage) mActivity;
        fca.switchLogout();
    }

    private AlertDialog getAdInstance() {
        return adInstance;
    }

    private void setAdInstance(AlertDialog adInstance) {
        AlertDialogUpdateApp.adInstance = adInstance;
    }
}
