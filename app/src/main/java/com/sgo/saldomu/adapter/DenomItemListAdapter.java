package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

public class DenomItemListAdapter extends RecyclerView.Adapter<DenomItemListAdapter.holder> {

    Context context;
    ArrayList<DenomListModel> itemList;
    listener listener;
    boolean isFragConfirm;

    public interface listener {
        void onClick(int pos);

        void onDelete(int pos);
    }

    public DenomItemListAdapter(Context _context, ArrayList<DenomListModel> itemList, listener listener, boolean isFragConfirm) {
        this.context = _context;
        this.itemList = itemList;
        this.listener = listener;
        this.isFragConfirm = isFragConfirm;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.adapter_denom_item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, final int position) {
        DenomItemOrderListAdapter adapter = new DenomItemOrderListAdapter(context
                , itemList.get(position).getOrderList(), isFragConfirm, new DenomItemOrderListAdapter.listener() {
            @Override
            public void delete(int pos) {
                listener.onDelete(pos);
            }
        });
        holder.orderList.setAdapter(adapter);
        holder.orderList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        holder.itemID.setText(itemList.get(position).getItemID());
        holder.itemName.setText(itemList.get(position).getItemName());

        holder.inputDenom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.
                        onClick(position);
            }
        });

        if (position == 0) {
            holder.border.setVisibility(View.VISIBLE);
        } else holder.border.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class holder extends RecyclerView.ViewHolder {

        TextView itemName, itemID;
        LinearLayout inputDenom;
        RecyclerView orderList;
        View border;

        public holder(View itemView) {
            super(itemView);
            border = itemView.findViewById(R.id.border);
            itemName = itemView.findViewById(R.id.adapter_denom_item_name_field);
            itemID = itemView.findViewById(R.id.adapter_denom_item_id_field);
            inputDenom = itemView.findViewById(R.id.adapter_denom_item_layout);
            orderList = itemView.findViewById(R.id.adapter_denom_item_list_order_list);
        }
    }
}
