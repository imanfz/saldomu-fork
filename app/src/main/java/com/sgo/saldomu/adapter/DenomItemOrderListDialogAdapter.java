package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

public class DenomItemOrderListDialogAdapter extends RecyclerView.Adapter<DenomItemOrderListDialogAdapter.holder>{

    Context context;
    ArrayList<DenomOrderListModel> itemList;

    public DenomItemOrderListDialogAdapter(Context _context, ArrayList<DenomOrderListModel> itemList){
        this.context = _context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.adapter_denom_item_name, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final holder holder, final int position) {
        if (position < itemList.size()) {
            DenomOrderListModel obj = itemList.get(position);
            if (obj != null) {
                holder.itemName.setText(obj.getPhoneNumber());
                holder.value.setText(obj.getPulsa());
            }
        }

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(holder.value.getText().toString());
                if (value >0){
                    holder.value.setText(String.valueOf(value-1));
                }
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(holder.value.getText().toString()) + 1;
                holder.value.setText(String.valueOf(value));
            }
        });
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    class holder extends RecyclerView.ViewHolder{

        TextView itemName, minus, add, value;

        public holder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.adapter_denom_phone_number_field);
            minus = itemView.findViewById(R.id.adapter_denom_minus);
            add = itemView.findViewById(R.id.adapter_denom_plus);
            value = itemView.findViewById(R.id.adapter_denom_value);
        }
    }
}
