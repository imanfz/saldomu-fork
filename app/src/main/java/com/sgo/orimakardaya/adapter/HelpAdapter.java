package com.sgo.orimakardaya.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.sgo.orimakardaya.Beans.HelpModel;
import com.sgo.orimakardaya.R;

import java.util.ArrayList;

/**
 * Created by thinkpad on 6/9/2015.
 */
public class HelpAdapter extends BaseAdapter{

    private LayoutInflater mInflater;
    private ArrayList<HelpModel> data;
    private Context context;

    public HelpAdapter(Context context, ArrayList<HelpModel> _data) {
        mInflater = LayoutInflater.from(context);
        this.data = _data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
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
            view = mInflater.inflate(R.layout.list_help_center_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView)view.findViewById(R.id.help_name_value);
            holder.phone = (TextView)view.findViewById(R.id.help_phone_value);
            holder.mail = (TextView)view.findViewById(R.id.help_mail_value);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }
        holder.name.setText(data.get(position).getName());
        holder.phone.setText(data.get(position).getPhone());
        holder.mail.setText(data.get(position).getMail());
        return view;
    }

    private class ViewHolder {
        public TextView name, phone, mail;
    }
}
