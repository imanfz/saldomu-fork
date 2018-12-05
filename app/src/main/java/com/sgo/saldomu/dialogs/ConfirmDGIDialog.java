package com.sgo.saldomu.dialogs;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;

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
