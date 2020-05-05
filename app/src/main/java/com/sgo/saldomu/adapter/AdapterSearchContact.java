package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.ActivitySearch;
import com.sgo.saldomu.models.ContactList;

import java.util.ArrayList;
import java.util.List;

public class AdapterSearchContact extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    public interface OnItemClick {
        void onTapContact(ContactList obj);
    }

    private OnItemClick listener;
    private Context mContext;
    private List<ContactList> dataContact;
    private List<ContactList> dataContactDisplay;
    private int type;

    public AdapterSearchContact(int type, List<ContactList> data, OnItemClick listener) {
        this.dataContact = data;
        this.listener = listener;
        this.dataContactDisplay = data;
        this.type = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        View v;
        switch (type) {
            case ActivitySearch.TYPE_SEARCH_CONTACT:
                v = LayoutInflater.from(mContext).inflate(R.layout.item_search_contact, parent, false);
                return new ViewHolderContact(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holders, int position) {
        final ViewHolderContact holder = (ViewHolderContact) holders;
        holder.item.setText(dataContactDisplay.get(position).getName());
        holder.tv_phone_no.setText(dataContactDisplay.get(position).getPhoneNo());
    }

    @Override
    public int getItemCount() {
        int finalSize = 0;
        finalSize = dataContactDisplay == null ? 0 : dataContactDisplay.size();
        return finalSize;
    }

    public class ViewHolderContact extends RecyclerView.ViewHolder {

        RelativeLayout layoutItem;
        TextView item;
        TextView tv_phone_no;

        ViewHolderContact(View itemView) {
            super(itemView);

            layoutItem  = itemView.findViewById(R.id.layoutItem);
            item  = itemView.findViewById(R.id.item);
            tv_phone_no  = itemView.findViewById(R.id.tv_phone_no);

            layoutItem.setOnClickListener(view -> listener.onTapContact(dataContactDisplay.get(getAdapterPosition())));
        }
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();

                if (charString.isEmpty()) {
                    dataContactDisplay.clear();
                    dataContactDisplay.addAll(dataContact);
                } else {
                    List<ContactList> filteredList = new ArrayList<>();
                    for (ContactList row : dataContact) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }
                    dataContactDisplay = filteredList;
                }

                FilterResults results = new FilterResults();
                results.values = dataContactDisplay;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notifyDataSetChanged();
            }
        };
    }
}
