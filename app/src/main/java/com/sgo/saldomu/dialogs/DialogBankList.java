package com.sgo.saldomu.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BankListAdapter;
import com.sgo.saldomu.adapter.BankListTopupAdapter;
import com.sgo.saldomu.adapter.ViewHolder;

import java.util.HashMap;
import java.util.List;

public class DialogBankList extends DialogFragment{
    View v;
    Context context;
    DialogBankListListener listener;
    List<HashMap<String, String>> bankList;
    RecyclerView recyclerView;

    public interface DialogBankListListener {
        void onChooseBank(int position);
    }

    public static DialogBankList newDialog(Context context, List<HashMap<String, String>> bankList, DialogBankListListener listener) {
        DialogBankList dialog = new DialogBankList();
        dialog.context = context;
        dialog.bankList = bankList;
        dialog.listener = listener;
        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_bank_list, container, false);
        recyclerView = v.findViewById(R.id.recycler_view);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BankListAdapter adapter = new BankListAdapter(context, bankList, position -> {
            listener.onChooseBank(position);
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}
