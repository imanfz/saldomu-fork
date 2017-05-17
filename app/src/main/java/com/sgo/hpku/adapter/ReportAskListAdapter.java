package com.sgo.hpku.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sgo.hpku.Beans.ReportAskListModel;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.CurrencyFormat;
import com.sgo.hpku.coreclass.DateTimeFormat;

import java.util.ArrayList;

/**
 * Created by thinkpad on 10/22/2015.
 */
public class ReportAskListAdapter extends ArrayAdapter<ReportAskListModel> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<ReportAskListModel> data = null;

    public ReportAskListAdapter(Context context, int resource, ArrayList<ReportAskListModel> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
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
            holder.tv_date = (TextView)row.findViewById(R.id.text_tgl_trans);
            holder.tv_type = (TextView)row.findViewById(R.id.text_trans_type);
            holder.tv_desc = (TextView)row.findViewById(R.id.description_value);
            holder.tv_ccy = (TextView)row.findViewById(R.id.text_ccyID);
            holder.tv_amount = (TextView)row.findViewById(R.id.text_amount);
            holder.tv_remark = (TextView)row.findViewById(R.id.text_remark);
            holder.tv_status = (TextView)row.findViewById(R.id.text_status);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

        ReportAskListModel itemnya = data.get(position);

        holder.tv_date.setText(DateTimeFormat.formatToID(itemnya.getDatetime()));
        holder.tv_type.setText(itemnya.getDetail());
        holder.tv_desc.setText(itemnya.getDescription());
        holder.tv_ccy.setText(itemnya.getCcyID());
        holder.tv_amount.setText(CurrencyFormat.format(itemnya.getAmount()));
        holder.tv_remark.setText(itemnya.getRemark());
        holder.tv_status.setText(itemnya.getStatus());

        return row;
    }

    class ListHolder
    {
        TextView tv_date,tv_type,tv_desc,tv_ccy,tv_amount,tv_remark, tv_status;
    }

}
