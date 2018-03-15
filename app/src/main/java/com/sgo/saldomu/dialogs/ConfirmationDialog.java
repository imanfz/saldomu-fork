package com.sgo.saldomu.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sgo.saldomu.R;

/**
 * Created by LENOVO on 08/03/2018.
 */

public class ConfirmationDialog extends DialogFragment {
    View v;
    clickListener listener;
    Button okButton, cancelButton;
    TextView fromTextview, toTextview, destinationTextview, remarkTextview, amountTextview;

    String amount, fromUserID, toUserID, destinationUserID, remark;

    Bundle bundle;

    public static ConfirmationDialog newDialog(clickListener listener
            , String amount
            , String fromUserID
            , String toUserID
            , String destinationUserID
            , String remark){
        ConfirmationDialog dialog = new ConfirmationDialog();
        dialog.listener = listener;
        dialog.amount = amount;
        dialog.fromUserID = fromUserID;
        dialog.toUserID = toUserID;
        dialog.destinationUserID = destinationUserID;
        dialog.remark = remark;
        return dialog;
    }

    public interface clickListener{
        void onOK();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.confirmation_dialog_layout, container, false);

        okButton = v.findViewById(R.id.confirmation_dialog_ok_button);
        cancelButton = v.findViewById(R.id.confirmation_dialog_back_button);
        amountTextview = v.findViewById(R.id.confirmation_dialog_amount);
        fromTextview = v.findViewById(R.id.confirmation_dialog_from);
        toTextview = v.findViewById(R.id.confirmation_dialog_to);
        destinationTextview = v.findViewById(R.id.confirmation_dialog_value_user_id);
        remarkTextview = v.findViewById(R.id.confirmation_dialog_remark);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        amountTextview.setText(amount);
        fromTextview.setText(fromUserID);
        toTextview.setText(toUserID);
        destinationTextview.setText(destinationUserID);
        remarkTextview.setText(remark);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOK();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
