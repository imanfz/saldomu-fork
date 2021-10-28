package com.sgo.saldomu.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sgo.saldomu.R;

public class PopUpDialog extends DialogFragment {


    Button buttonSubmit;

    public PopUpListener listener;

    View v;

    DialogFragment dialogssss;
    public interface PopUpListener {
        void onClick(DialogFragment dialog);
    }

    public static PopUpDialog newDialog(Bundle bundle, PopUpListener listener) {
        PopUpDialog dialog = new PopUpDialog();
        dialog.setCancelable(false);
        dialog.listener = listener;
        dialog.setArguments(bundle);

        return dialog;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         v = inflater.inflate(R.layout.dialog_pop_up_inv, container, false);
         initializeBackground();

        dialogssss = this;
        buttonSubmit = v.findViewById(R.id.btnSubmit);

        buttonSubmit.setOnClickListener(view -> listener.onClick(dialogssss));


        return v;


    }


    private void initializeBackground(){

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    }

    @Override
    public void onResume() {
        super.onResume();
    }
}