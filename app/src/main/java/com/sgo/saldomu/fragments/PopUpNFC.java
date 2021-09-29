package com.sgo.saldomu.fragments;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sgo.saldomu.R;

public class PopUpNFC extends DialogFragment {



    TextView noNFC;
    TextView yesNFC;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        try {
//            popUpNFCListener = (PopUpNFCListener) getActivity();
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Calling Fragment must implement OnAddFriendListener");
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup_nfc, container, false);

        noNFC = view.findViewById(R.id.tv_noNFC);
        yesNFC = view.findViewById(R.id.tv_yesNFC);

        getActivity().getWindow().setBackgroundDrawableResource(R.drawable.rounded_popup_nfc);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        noNFC.setOnClickListener(v -> getDialog().dismiss());

        yesNFC.setOnClickListener(v -> {
            getDialog().dismiss();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
    }

//    interface PopUpNFCListener{
//        void onOptionClicked(String option);
//    }
}
