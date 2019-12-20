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
    View nameLayout, notifPelangganLayout;
    TextView fromTextview, toTextview, destinationTextview, remarkTextview, amountTextview,
    nameTextview, notifPelangganTextview, tv_left, tv_right;

    String amount, fromUserID, toUserID, destinationUserID, remark, transaksi, name, notifPelanggan;

    Bundle bundle;

    public static ConfirmationDialog newDialog(clickListener listener, String transaksi
            , String amount
            , String fromUserID
            , String toUserID
            , String destinationUserID
            , String remark, String name, String notifPelanggan){
        ConfirmationDialog dialog = new ConfirmationDialog();
        dialog.transaksi = transaksi;
        dialog.listener = listener;
        dialog.amount = amount;
        dialog.fromUserID = fromUserID;
        dialog.toUserID = toUserID;
        dialog.destinationUserID = destinationUserID;
        dialog.remark = remark;
        dialog.name = name;
        dialog.notifPelanggan = notifPelanggan;
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

        getDialog().setTitle(getString(R.string.confirmation) + " " + transaksi);
        okButton = v.findViewById(R.id.confirmation_dialog_ok_button);
        cancelButton = v.findViewById(R.id.confirmation_dialog_back_button);
        amountTextview = v.findViewById(R.id.confirmation_dialog_amount);
        fromTextview = v.findViewById(R.id.confirmation_dialog_from);
        toTextview = v.findViewById(R.id.confirmation_dialog_to);
        destinationTextview = v.findViewById(R.id.confirmation_dialog_value_user_id);
        remarkTextview = v.findViewById(R.id.confirmation_dialog_remark);
        nameTextview = v.findViewById(R.id.confirmation_dialog_value_name);
        notifPelangganTextview = v.findViewById(R.id.confirmation_dialog_value_notif_pengguna);
        nameLayout = v.findViewById(R.id.name_layout);
        notifPelangganLayout = v.findViewById(R.id.notif_pelanggan_layout);
        tv_left = v.findViewById(R.id.tv_milik_kiri);
        tv_right = v.findViewById(R.id.tv_milik_kanan);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        amountTextview.setText(amount);
        fromTextview.setText(fromUserID);
        toTextview.setText(toUserID);

        if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            notifPelangganLayout.setVisibility(View.VISIBLE);
            notifPelangganTextview.setText(notifPelanggan);
            if(!name.equals("")){
                nameLayout.setVisibility(View.VISIBLE);
                nameTextview.setText(name);
            }
            destinationTextview.setText(destinationUserID);
            tv_left.setText(getString(R.string.milik_agen));
            tv_right.setText(getString(R.string.milik_pelanggan));
        }
        else
            destinationTextview.setText(destinationUserID);
        remarkTextview.setText(remark);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOK();
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
