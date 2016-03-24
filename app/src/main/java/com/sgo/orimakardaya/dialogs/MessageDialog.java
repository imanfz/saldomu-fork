package com.sgo.orimakardaya.dialogs;/*
  Created by Administrator on 11/20/2015.
 */

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.sgo.orimakardaya.R;

public class MessageDialog extends Dialog {


    private String title, message;
    private DialogButtonListener mDialogButtonListener;


    public interface DialogButtonListener{
        void onClickButton(View v, boolean isLongClick);
    }

    public void setDialogButtonClickListener(DialogButtonListener clickListener) {
        this.mDialogButtonListener = clickListener;
    }

    public MessageDialog(Context context, String _title, String _message) {
        super(context);
        setTitle(_title);
        setMessage(_message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)this.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)this.findViewById(R.id.title_dialog);
        TextView Message = (TextView)this.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getTitle());
        Message.setText(getMessage());
        btnDialogOTP.setOnClickListener(buttonListenerCustom);
    }

    View.OnClickListener buttonListenerCustom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDialogButtonListener.onClickButton(v,false);
            dismiss();
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
