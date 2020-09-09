package com.sgo.saldomu.dialogs;

import android.app.Activity;
import android.app.AlertDialog;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;

import timber.log.Timber;

public class AlertDialogMaintenance {
    private static AlertDialogMaintenance instance = null;
    private static AlertDialog adInstance;

    private AlertDialogMaintenance() {
        // Exists only to defeat instantiation.
    }

    public static AlertDialogMaintenance getInstance() {
        if (instance == null) {
            instance = new AlertDialogMaintenance();
        }
        return instance;
    }

    public void showDialogMaintenance(final Activity mContext, String message) {
//        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mContext.finish();
//                android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(0);
//                mContext.getParent().finish();
//            }
//        };
//        android.support.v7.app.AlertDialog alertDialog = DefinedDialog.BuildAlertDialog(mContext, mContext.getString(R.string.maintenance),
//                message, android.R.drawable.ic_dialog_alert, false,
//                mContext.getString(R.string.ok), okListener);
//        alertDialog.show();
//
//        Timber.d("showDialogUpdate");

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.maintenance)).setMessage(mContext.getResources().getString(R.string.maintenance_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton(mContext.getResources().getString(R.string.ok), (dialog, which) ->
                {
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

        MainPage fca = new MainPage();
        fca.switchLogout();
    }

    private AlertDialog getAdInstance() {
        return adInstance;
    }

    private void setAdInstance(AlertDialog adInstance) {
        AlertDialogMaintenance.adInstance = adInstance;
    }
}
