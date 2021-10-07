package com.sgo.saldomu.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

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