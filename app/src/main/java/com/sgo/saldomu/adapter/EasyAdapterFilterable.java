package com.sgo.saldomu.adapter;/*
  Created by Administrator on 1/18/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sgo.saldomu.R;

import java.util.ArrayList;

public class EasyAdapterFilterable extends ArrayAdapter<String> implements Filterable {

    private Context context;
    private int layoutResourceId;
    private ArrayList<String> itemList;
    private ArrayList<String> originalList;
    private Listener listener;

    public interface Listener {
        void onClick(String item);
    }

    public EasyAdapterFilterable(Context context, int layoutResourceId, ArrayList<String> data, Listener listener) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.originalList = data;
        this.itemList = data;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ListHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ListHolder();
            holder.layout = (FrameLayout) row.findViewById(R.id.layout);
            holder.txtTitle = (TextView) row.findViewById(R.id.txtTitleList);

            row.setTag(holder);
        } else {
            holder = (ListHolder) row.getTag();
        }

        String title = itemList.get(position);
        holder.txtTitle.setText(title);
        holder.layout.setOnClickListener(view -> {
            listener.onClick(title);
        });

        return row;
    }

    static class ListHolder {
        TextView txtTitle;
        FrameLayout layout;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase();

                ArrayList<String> temp = new ArrayList<>();
                if (charString.length() < 3)
                    temp.addAll(originalList);
                else
                    for (String string : originalList) {
                        if (string.toLowerCase().contains(charString))
                            temp.add(string);
                    }

                FilterResults filterResults = new FilterResults();
                filterResults.values = temp;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemList = new ArrayList<>((ArrayList<String>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }
}
