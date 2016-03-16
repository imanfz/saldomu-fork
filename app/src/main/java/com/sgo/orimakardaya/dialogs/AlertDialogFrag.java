package com.sgo.orimakardaya.dialogs;/*
  Created by Administrator on 1/26/2015.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class AlertDialogFrag extends DialogFragment {

  public static final String TAG = "Alert Dialog";

  private Boolean onlyPositive = true;
  private DialogInterface.OnClickListener okListener;
  private DialogInterface.OnClickListener cancelListener;

  public AlertDialogFrag() {
      // Empty constructor required for DialogFragment
    }

    public static AlertDialogFrag newInstance(String title, String message, String btnOk, String btnCancel,
                                              boolean onlyPositive) {
      AlertDialogFrag frag = new AlertDialogFrag();
      Bundle args = new Bundle();
      args.putString("title", title);
      args.putString("message", message);
      args.putString("btnoke", btnOk);
      args.putString("btncancel", btnCancel);
      frag.setArguments(args);
      frag.setOnlyPositive(onlyPositive);
      return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      String title = getArguments().getString("title");
      String message  = getArguments().getString("message");
      String btnok = getArguments().getString("btnoke");
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
      alertDialogBuilder.setTitle(title);
      alertDialogBuilder.setMessage(message);
      setCancelable(false);

      alertDialogBuilder.setPositiveButton(btnok, okListener);

      if(!getOnlyPositive()){
        String btncancel = getArguments().getString("btncancel");
        alertDialogBuilder.setNegativeButton(btncancel, cancelListener);
      }

      return alertDialogBuilder.create();
    }



  public Boolean getOnlyPositive() {
    return onlyPositive;
  }

  public void setOnlyPositive(Boolean onlyPositive) {
    this.onlyPositive = onlyPositive;
  }

  public DialogInterface.OnClickListener getOkListener() {
    return okListener;
  }

  public void setOkListener(DialogInterface.OnClickListener okListener) {
    this.okListener = okListener;
  }

  public DialogInterface.OnClickListener getCancelListener() {
    return cancelListener;
  }

  public void setCancelListener(DialogInterface.OnClickListener cancelListener) {
    this.cancelListener = cancelListener;
  }
}
