package com.sgo.saldomu.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;

public class PaymentRemarkDialog extends DialogFragment {
    View v;

    Button ok, cancel;
    EditText inputMsg;
    String paymentTypeName;
    LinearLayout layoutBankName, layoutDueDate;

    onTap listener;

    public interface onTap{
        void onOK(String msg);
    }

    public static PaymentRemarkDialog newDialog(onTap listener){
        PaymentRemarkDialog dialog = new PaymentRemarkDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window dialog = getDialog().getWindow();
        if (dialog != null) {
            dialog.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            dialog.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.payment_remark_dialog_layout, container, false);

        ok = v.findViewById(R.id.pay_remark_dialog_ok_btn);
        cancel  = v.findViewById(R.id.pay_remark_dialog_cancel_btn);
        inputMsg = v.findViewById(R.id.pay_remark_dialog_input_msg);
        layoutBankName = v.findViewById(R.id.layout_bank_name);
        layoutDueDate = v.findViewById(R.id.layout_duedate);
        Bundle bundle = getArguments();
        paymentTypeName = bundle.getString(DefineValue.PAYMENT_TYPE_DESC, "");

        if (paymentTypeName.equalsIgnoreCase("Bilyet Giro"))
        {
            layoutBankName.setVisibility(View.VISIBLE);
            layoutDueDate.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOK(inputMsg.getText().toString());
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
