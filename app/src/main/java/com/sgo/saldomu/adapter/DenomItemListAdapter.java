package com.sgo.saldomu.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;

import java.util.ArrayList;

public class DenomItemListAdapter extends RecyclerView.Adapter<DenomItemListAdapter.holder> implements Filterable {

    Context context;
    ArrayList<DenomListModel> itemList;
    ArrayList<DenomListModel> originalList;
    listener listener;
    boolean isFragConfirm;

    public interface listener {
        void onClick(int pos);

        void onDelete(int pos);

        void onChangeQty(String itemId, String qty);
    }

    public DenomItemListAdapter(Context _context, ArrayList<DenomListModel> itemList, listener listener, boolean isFragConfirm) {
        this.context = _context;
        this.itemList = itemList;
        originalList = itemList;
        this.listener = listener;
        this.isFragConfirm = isFragConfirm;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.adapter_denom_item_list, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull holder holder, final int position) {
        holder.itemID.setText(itemList.get(position).getItemID());
        holder.itemName.setText(itemList.get(position).getItemName());
        holder.itemPrice.setText(context.getString(R.string.rp_) + " " + CurrencyFormat.format(itemList.get(position).getItemPrice()));

        if (!itemList.get(position).getOrderList().isEmpty())
            holder.itemQty.setText(itemList.get(position).getOrderList().get(0).getPulsa());
        else
            holder.itemQty.setText("");

        holder.itemQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.onChangeQty(itemList.get(position).getItemID(), s.toString());
            }
        });

//        holder.inputDenom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.
//                        onClick(position);
//            }
//        });

        if (position == 0) {
            holder.border.setVisibility(View.VISIBLE);
        } else holder.border.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class holder extends RecyclerView.ViewHolder {

        TextView itemName, itemID, itemPrice;
        EditText itemQty;
        LinearLayout inputDenom;
        View border;

        public holder(View itemView) {
            super(itemView);
            border = itemView.findViewById(R.id.border);
            itemName = itemView.findViewById(R.id.adapter_denom_item_name_field);
            itemID = itemView.findViewById(R.id.adapter_denom_item_id_field);
            itemQty = itemView.findViewById(R.id.adapter_denom_item_et_qty);
            itemPrice = itemView.findViewById(R.id.adapter_denom_item_price_field);
            inputDenom = itemView.findViewById(R.id.adapter_denom_item_layout);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase();
                ArrayList<DenomListModel> temp = new ArrayList<>();
                if (charString.isEmpty())
                    temp.addAll(originalList);
                else
                    for (DenomListModel model : originalList) {
                        if (model.getItemName().toLowerCase().contains(charString))
                            temp.add(model);
                    }

                FilterResults filterResults = new FilterResults();
                filterResults.values = temp;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemList = new ArrayList<>((ArrayList<DenomListModel>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
