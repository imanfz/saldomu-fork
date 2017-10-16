package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.sgo.saldomu.R;

import java.util.List;

/**
 * Created by thinkpad on 3/18/2015.
 */
class NominalAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<String> nominal;

    public NominalAdapter(Context context, List<String> _nominal) {
        mInflater = LayoutInflater.from(context);
        nominal =_nominal;
    }

    @Override
    public int getCount() {
        return nominal.size();
    }

    @Override
    public Object getItem(int position) {
        return nominal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.list_nominal_item, parent, false);
            holder = new ViewHolder();
            holder.txtnominal = (TextView)view.findViewById(R.id.nominal);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        holder.txtnominal.setText(nominal.get(position));

        return view;
    }

    private class ViewHolder {
        public TextView txtnominal;
    }
}
