package com.sgo.saldomu.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.SearchAgentUpgradeActivity;
import com.sgo.saldomu.coreclass.LifeCycleHandler;

import timber.log.Timber;

public class DefinedDialog {

    public interface DialogButtonListener {
        void onClickButton(View v, boolean isLongClick);
    }

    public static ProgressDialog CreateProgressDialog(Context context) {
        return CreateProgressDialog(context, "");
    }

    public static ProgressDialog CreateProgressDialog(Context context, String message) {
        if (context!=null){
            ProgressDialog dialog = new ProgressDialog(context);
            try {
                dialog.show();
            } catch (WindowManager.BadTokenException e) {
                Timber.w("define dialog error:" + e.getMessage());
            }
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.dialog_progress);
            TextView text1 = dialog.findViewById(R.id.progressText1);
            text1.setText(message);
            return dialog;
        }
        return null;
    }

    public static void showErrorDialog(Context context, String message) {
        showErrorDialog(context, message, null);
    }

    public static void showErrorDialog(Context context, String message, final DialogButtonListener mButtonListener) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_error);
        dialog.setCancelable(false);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_error_ok);
        TextView Message = dialog.findViewById(R.id.message_dialog_error);
        Message.setText(message);
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mButtonListener != null)
                    mButtonListener.onClickButton(view, false);
                dialog.dismiss();
            }
        });


        if (LifeCycleHandler.isApplicationVisible())
            dialog.show();
    }

    public static Dialog MessageDialog(Context context, String _title, String _message, final DialogButtonListener _dialogListener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(_title);
        Message.setText(_message);
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _dialogListener.onClickButton(v, false);
                dialog.dismiss();
            }
        });
//        dialog.show();
        return dialog;
    }

    public static Dialog MessageP2P(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_p2p_color_help);
        // Include dialog.xml file
        dialog.findViewById(R.id.btn_dialog_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static Dialog MessageSearchAgent(Context context, String _title, String _message){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_agent);
        dialog.setCancelable(false);

        TextView title = dialog.findViewById(R.id.title_dialog);
        TextView message = dialog.findViewById(R.id.message_dialog);

        title.setText(_title);
        message.setText(_message);
        dialog.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.findViewById(R.id.btn_dialog_search).setOnClickListener(v -> {
            dialog.dismiss();
            context.startActivity(new Intent(context, SearchAgentUpgradeActivity.class));
        });
        return dialog;
    }

    public static AlertDialog BuildAlertDialog(Context context, String title, String msg, int icon, Boolean isCancelable,
                                               String okbtn, DialogInterface.OnClickListener ok) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(icon)
                .setCancelable(isCancelable)
                .setPositiveButton(okbtn, ok);
        return builder.create();
    }
}