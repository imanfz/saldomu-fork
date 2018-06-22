package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.SelectBankProductAdapter;
import com.sgo.saldomu.coreclass.Singleton.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class BankProductSelectionBottomSheet extends BottomSheetDialogFragment {

    View v;
    ExpandableListView productBankExLV;
    SelectBankProductAdapter bankProductAdapter;

    List<listBankModel> listDataHeader2;
    HashMap<String, listBankModel> listDataChild2;

    OnClick listener;

    public interface OnClick{
        void onClick(listBankModel obj);
    }

    public static BankProductSelectionBottomSheet newDialog(OnClick listener){
        BankProductSelectionBottomSheet dialog = new BankProductSelectionBottomSheet();
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_bank_product_list_topup, container, false);
        productBankExLV = v.findViewById(R.id.frag_product_bank_list_expandableListView);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listDataChild2 = new HashMap<>();
        listDataHeader2 = new ArrayList<>();

        bankProductAdapter = new SelectBankProductAdapter(getActivity(), listDataHeader2, listDataChild2);

        productBankExLV.setAdapter(bankProductAdapter);

        productBankExLV.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (!listDataHeader2.get(groupPosition).getProduct_type().equals("ATM")) {
                    listener.onClick(listDataHeader2.get(groupPosition));
                    dismiss();
                }

                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        parseProductBank();
    }

    public void parseProductBank(){
        try {
            if (listDataHeader2.size()>0) {
                listDataHeader2.clear();
                listDataChild2.clear();
            }

            listDataHeader2.addAll(DataManager.getInstance().getBankData());
            for (listBankModel obj: listDataHeader2) {
                if (obj.getProduct_type().equals("ATM")){
                    listDataChild2.put(obj.getProduct_code(), obj);
                }
            }

            bankProductAdapter.notifyDataSetChanged();
            productBankExLV.expandGroup(listDataHeader2.size()-1);
        }catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }
}
