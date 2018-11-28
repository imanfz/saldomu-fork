package com.sgo.saldomu.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.InvoiceDGI;

public class InputAmountTagihBillerDialog extends DialogFragment {

    View v;
    OnTap listener;
    InvoiceDGI obj;
    int pos;

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
        v = inflater.inflate(R.layout.item_invoice, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
