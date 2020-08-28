package com.sgo.saldomu.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sgo.saldomu.R;

public class DetailInvoiceTagihDialog extends DialogFragment
{
    View v;
    Bundle bundle;
    TextView tv_paymentType, tv_remark, tv_phone;
    Button btn_ok;
    String paymentType,remark,phone;

    public static DetailInvoiceTagihDialog newDialog(String paymentType
            , String remark
            , String phone){
        DetailInvoiceTagihDialog dialog = new DetailInvoiceTagihDialog();
        dialog.paymentType = paymentType;
        dialog.remark = remark;
        dialog.phone = phone;
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window dialog = getDialog().getWindow();
        if (dialog != null) {
            dialog.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle=getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_detail_invoice, container, false);

        getDialog().setTitle(getString(R.string.detail));

        tv_paymentType = v.findViewById(R.id.tv_paymentType);
        tv_remark = v.findViewById(R.id.tv_remark);
        tv_phone= v.findViewById(R.id.tv_mobilePhone);
        btn_ok = v.findViewById(R.id.btnDone);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tv_paymentType.setText(paymentType);
        tv_remark.setText(remark);
        tv_phone.setText(phone);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
