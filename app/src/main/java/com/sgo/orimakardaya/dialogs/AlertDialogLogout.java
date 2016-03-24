package com.sgo.orimakardaya.dialogs;/*
  Created by Administrator on 1/26/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;

import timber.log.Timber;

public class AlertDialogLogout {

  private static AlertDialogLogout instance = null;
  private static AlertDialog adInstance;
  protected AlertDialogLogout() {
    // Exists only to defeat instantiation.
  }
  public static AlertDialogLogout getInstance() {
    if(instance == null) {
        instance = new AlertDialogLogout();
    }
    return instance;
  }


    public void showDialoginActivity(final Activity mContext, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.logout)).setMessage(message)
                .setCancelable(false)
                .setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.setResult(MainPage.RESULT_LOGOUT);
                        mContext.finish();
                    }
                });
        if(getAdInstance() == null ) {
            setAdInstance(builder.create());
            getAdInstance().show();
        }
        else if(!getAdInstance().isShowing()) {
            setAdInstance(builder.create());
            getAdInstance().show();
        }
    }

    public void showDialoginMain(final Activity mContext, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.logout)).setMessage(message)
                .setCancelable(false)
                .setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switchLogout(mContext);
                    }
                });

        Timber.d("showDialoginMain");
        if(getAdInstance() == null ) {
            Timber.d("showDialoginMain");
            setAdInstance(builder.create());
            getAdInstance().show();
        }
        else if(!getAdInstance().isShowing()) {
            Timber.d("showDialoginMain");
            setAdInstance(builder.create());
            getAdInstance().show();
        }
    }


    private void switchLogout(Activity mActivity){
        if (mActivity == null)
            return;

        MainPage fca = (MainPage) mActivity;
        fca.switchLogout();
    }

    public AlertDialog getAdInstance() {
        return adInstance;
    }

    public void setAdInstance(AlertDialog adInstance) {
        AlertDialogLogout.adInstance = adInstance;
    }
}
