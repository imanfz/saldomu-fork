package com.sgo.saldomu.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.InvoiceDGI;

public class InputAmountTagihBillerDialog extends DialogFragment {

    View view;
    OnTap listener;
    InvoiceDGI obj;
    int pos;
    EditText inpAmount;
    Button btnDone,btnCancel;

    public interface OnTap{
        void onTap(int pos, String value);
    }

    public static InputAmountTagihBillerDialog newDialog(int pos, InvoiceDGI obj, OnTap listener){
        InputAmountTagihBillerDialog dialog = new InputAmountTagihBillerDialog();
        dialog.obj =obj;
        dialog.listener = listener;
        dialog.pos = pos;

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window dialog = getDialog().getWindow();
        if (dialog != null) {
            dialog.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_input_invoicedgi, container, false);

        getDialog().setTitle(getString(R.string.invoice_dgi_payment));
        inpAmount             = (EditText) view.findViewById(R.id.inpAmount);
        btnDone               = (Button) view.findViewById(R.id.btnDone);
        btnCancel             = (Button) view.findViewById(R.id.btnCancel);


        TextView lbl_doc_no = (TextView) view.findViewById(R.id.lbl_doc_no);
//        lbl_doc_no.setText(doc_no);

        TextView lbl_doc_desc = (TextView) view.findViewById(R.id.lbl_doc_desc);
//        lbl_doc_desc.setText(doc_desc);

        TextView lbl_amount = (TextView) view.findViewById(R.id.lbl_amount);
//        lbl_amount.setText(amount);

        TextView lbl_remain_amount = (TextView) view.findViewById(R.id.lbl_remain_amount);
//        lbl_remain_amount.setText(remain_amount);

        TextView lbl_partial = (TextView) view.findViewById(R.id.lbl_partial);
//        lbl_partial.setText(partial);

//        if(partial.equalsIgnoreCase("Ya") || partial.equalsIgnoreCase("Bisa Lebih")){
//            showInvoiceAmount(view);
//        }else{
//            hideInvoiceAmount(view);
//        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
