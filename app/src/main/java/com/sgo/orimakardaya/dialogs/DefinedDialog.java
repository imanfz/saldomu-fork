package com.sgo.orimakardaya.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.sgo.orimakardaya.R;

import timber.log.Timber;

public class DefinedDialog {

    public interface DialogButtonListener{
        void onClickButton(View v, boolean isLongClick);
    }

    public static ProgressDialog CreateProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            Timber.w("define dialog error:" + e.getMessage());
        }
        dialog.setContentView(R.layout.dialog_progress);
        TextView text1 = (TextView) dialog.findViewById(R.id.progressText1);
        text1.setText(message);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void showErrorDialog(Context context, String message, final DialogButtonListener mButtonListener) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_error);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_error_ok);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog_error);

        Message.setText(message);
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mButtonListener!=null)
                    mButtonListener.onClickButton(view,false);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static Dialog MessageDialog(Context context, String _title, String _message, final DialogButtonListener _dialogListener ){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(_title);
        Message.setText(_message);
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dialogListener.onClickButton(v,false);
                dialog.dismiss();
            }
        });
//        dialog.show();
        return dialog;
    }
}