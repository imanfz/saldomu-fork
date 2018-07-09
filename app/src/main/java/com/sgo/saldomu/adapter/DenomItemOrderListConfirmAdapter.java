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

public class DenomItemOrderListConfirmAdapter extends RecyclerView.Adapter<DenomItemOrderListConfirmAdapter.holder>{

    Context context;
    ArrayList<DenomOrderListModel> itemList;
    boolean isFragConfirm;



    public DenomItemOrderListConfirmAdapter(Context _context, ArrayList<DenomOrderListModel> itemList){
        this.context = _context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.adapter_denom_item_order_list_confirm, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, final int position) {
        holder.number.setText(itemList.get(position).getPhoneNumber());
        holder.pulsa.setText(itemList.get(position).getPulsa());
        holder.itemID.setText(itemList.get(position).getItemID());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class holder extends RecyclerView.ViewHolder{

        TextView number, pulsa, itemID;

        public holder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.adapter_denom_item_order_list_confirm_number);
            pulsa = itemView.findViewById(R.id.adapter_denom_item_order_list_confirm_pulsa);
            itemID = itemView.findViewById(R.id.adapter_denom_item_order_list_confirm_item_id);
        }
    }
}
