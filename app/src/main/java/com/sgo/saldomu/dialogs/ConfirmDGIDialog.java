package com.sgo.saldomu.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sgo.saldomu.R;

public class ConfirmDGIDialog extends DialogFragment {
    View v;
    TextView tvPaymentType, tvRemark, tvPhone;
    Button btnOk;

    String paymentType, remark, phone;

    Bundle bundle;

    public static ConfirmDGIDialog newDialog(String paymentType, String remark, String phone){
        ConfirmDGIDialog dialog = new ConfirmDGIDialog();
        dialog.paymentType = paymentType;
        dialog.remark = remark;
        dialog.phone = phone;
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_confirm_payment_dgi, container, false);

        tvPaymentType = v.findViewById(R.id.tv_paymentType);
        tvRemark = v.findViewById(R.id.tv_paymentRemark);
        tvPhone = v.findViewById(R.id.tv_mobilePhone);

        tvPaymentType.setText(paymentType);
        tvRemark.setText(remark);
        tvPhone.setText(phone);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
    }
}
