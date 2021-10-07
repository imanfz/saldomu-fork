package com.sgo.saldomu.coreclass.Singleton;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sgo.saldomu.interfaces.ConfirmDialogInterface;

public class InterfaceManager {
    public static void showConfirmDialog(Context context, String title, final ConfirmDialogInterface listener){
        AlertDialog.Builder alertdialog;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            alertdialog = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        }else alertdialog = new AlertDialog.Builder(context);
        alertdialog.setCancelable(true);

        alertdialog.setMessage(title);
        alertdialog.setPositiveButton("Ya", (dialog, which) -> listener.OnOK());
//        alertdialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        alertdialog.show();
    }
}
