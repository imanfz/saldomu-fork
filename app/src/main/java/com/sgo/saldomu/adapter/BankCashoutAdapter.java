package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.retrofit.BankCashoutModel;

import java.util.ArrayList;
import java.util.List;

public class BankCashoutAdapter extends ArrayAdapter<BankCashoutModel> {
    private Context context;
    private List<BankCashoutModel> itemList;

    public BankCashoutAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
        this.itemList = new ArrayList<>();
    }

    public void updateAdapter(List<BankCashoutModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public BankCashoutModel getItemList(int position) {
        return itemList.get(position);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
        return getCustomView(position, parent);
    }

    public View getCustomView(int position, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item_black, parent, false);

        TextView label = row.findViewById(R.id.text1);
        label.setText(getItemList(position).getBank_name());

        return row;
    }

}
