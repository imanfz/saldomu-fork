package com.sgo.saldomu.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.DenomItemOrderListDialogAdapter;

import java.util.ArrayList;

public class DenomItemDialog extends DialogFragment {

    View v;
    ArrayList<DenomOrderListModel> itemList;
    DenomItemOrderListDialogAdapter adapter;
    Button ok;
    RecyclerView itemListrv;
    TextView itemTitleTextview;
    String title;
    listener listener;

    public interface listener{
        void onOK(ArrayList<DenomOrderListModel> itemList);
    }

    public static DenomItemDialog newDialog(String title, ArrayList<DenomOrderListModel> itemList, listener listener){
        DenomItemDialog dialog = new DenomItemDialog();
        dialog.itemList = itemList;
        dialog.title = title;
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_denom_item_name, container, false);
        itemListrv = v.findViewById(R.id.dialog_denom_product_bank_rv);
        ok = v.findViewById(R.id.dialog_denom_item_ok);
        itemTitleTextview = v.findViewById(R.id.dialog_denom_item_title);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        itemTitleTextview.setText(title);

        adapter = new DenomItemOrderListDialogAdapter(getActivity(), itemList);
        itemListrv.setAdapter(adapter);
        itemListrv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOK(checkInput());
                dismiss();
            }
        });
    }

    ArrayList<DenomOrderListModel> checkInput(){
        itemList = new ArrayList<>();
        for (int i=0; i<5; i++){
            View v = itemListrv.getChildAt(i);
            TextView name = v.findViewById(R.id.adapter_denom_phone_number_field);
            if (!name.getText().toString().equals("")){
                TextView total = v.findViewById(R.id.adapter_denom_value);
                if (Integer.valueOf(total.getText().toString()) > 0) {
                    itemList.add(new DenomOrderListModel(name.getText().toString(), total.getText().toString()));
                }
            }
        }
        return itemList;
    }
}
