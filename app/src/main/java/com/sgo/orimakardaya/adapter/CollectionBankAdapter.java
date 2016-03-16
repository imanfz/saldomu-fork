package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 1/18/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sgo.orimakardaya.R;

import java.util.ArrayList;

public class CollectionBankAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    private ArrayList<String> data;

    public CollectionBankAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ListHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ListHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitleList);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }
        holder.txtTitle.setText(data.get(position));

        return row;
    }

    class ListHolder
    {
        TextView txtTitle,iconArrow;
    }
}
