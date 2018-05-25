package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

public class DenomItemOrderListAdapter extends RecyclerView.Adapter<DenomItemOrderListAdapter.holder>{

    Context context;
    ArrayList<DenomOrderListModel> itemList;
    listener listener;

    public interface listener{
        void delete(int pos);
    }

    public DenomItemOrderListAdapter(Context _context, ArrayList<DenomOrderListModel> itemList, listener listener){
        this.context = _context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.adapter_denom_item_order_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, final int position) {
        holder.number.setText(itemList.get(position).getPhoneNumber());
        holder.pulsa.setText(itemList.get(position).getPulsa());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.delete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class holder extends RecyclerView.ViewHolder{

        TextView number, pulsa, delete;

        public holder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.adapter_denom_item_order_list_number);
            pulsa = itemView.findViewById(R.id.adapter_denom_item_order_list_pulsa);
            delete = itemView.findViewById(R.id.adapter_denom_item_order_list_delete);
        }
    }
}
