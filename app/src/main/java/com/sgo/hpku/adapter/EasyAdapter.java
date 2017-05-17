package com.sgo.hpku.adapter;/*
  Created by Administrator on 1/18/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sgo.hpku.R;

import java.util.ArrayList;
import java.util.Arrays;

public class EasyAdapter extends ArrayAdapter<String> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<String> adata;

    public EasyAdapter(Context context, int layoutResourceId, String[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.adata = new ArrayList<>();
        this.adata.addAll(Arrays.asList(data));
    }

    public EasyAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.adata = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
        else {
        holder = (ListHolder)row.getTag();
        }

        holder.txtTitle.setText(adata.get(position));

        return row;
    }

    class ListHolder
    {
      TextView txtTitle,iconArrow;
    }
}
