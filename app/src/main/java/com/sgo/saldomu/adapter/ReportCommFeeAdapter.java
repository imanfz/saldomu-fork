package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.models.ReportListCommFeeModel;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 2/20/2018.
 */

public class ReportCommFeeAdapter extends ArrayAdapter<ReportListCommFeeModel> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<ReportListCommFeeModel> data = null;

    public ReportCommFeeAdapter(Context context, int resource, ArrayList<ReportListCommFeeModel> objects) {
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

            holder = new ReportCommFeeAdapter.ListHolder();
            holder.tv_date = (TextView)row.findViewById(R.id.text_tgl_trans);
            holder.tv_type = (TextView)row.findViewById(R.id.text_trans_type);
            holder.tv_desc = (TextView)row.findViewById(R.id.description_value);
            holder.tv_ccy = (TextView)row.findViewById(R.id.text_ccyID);
            holder.tv_amount = (TextView)row.findViewById(R.id.text_amount);
            holder.tv_status = (TextView)row.findViewById(R.id.text_status);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

        ReportListCommFeeModel itemnya = data.get(position);

        holder.tv_date.setText(DateTimeFormat.formatToID(itemnya.getDatetime()));
        holder.tv_type.setText(itemnya.getType());
        holder.tv_desc.setText(itemnya.getDescription());
        holder.tv_ccy.setText(itemnya.getCcyID());
        holder.tv_amount.setText(CurrencyFormat.format(itemnya.getAmount()));
        holder.tv_status.setText(itemnya.getStatus());

        return row;
    }

    class ListHolder
    {
        TextView tv_date,tv_type,tv_desc,tv_ccy,tv_amount, tv_status;
    }
}
