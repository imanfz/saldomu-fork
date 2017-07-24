package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import com.sgo.saldomu.Beans.HelpModel;
import com.sgo.saldomu.R;

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
            holder.trPhone = (TableRow)view.findViewById(R.id.tr_phone);
            holder.trMail = (TableRow)view.findViewById(R.id.tr_mail);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }
        holder.name.setText(data.get(position).getName());

        String phone = data.get(position).getPhone();
        String mail = data.get(position).getMail();

        if(phone.equals("")) {
            holder.trPhone.setVisibility(View.GONE);
        }
        else {
            holder.trPhone.setVisibility(View.VISIBLE);
            holder.phone.setText(phone);
        }

        if(mail.equals("")) {
            holder.trMail.setVisibility(View.GONE);
        }
        else {
            holder.trMail.setVisibility(View.VISIBLE);
            holder.mail.setText(mail);
        }
        return view;
    }

    private class ViewHolder {
        public TextView name, phone, mail;
        public TableRow trPhone, trMail;
    }
}
