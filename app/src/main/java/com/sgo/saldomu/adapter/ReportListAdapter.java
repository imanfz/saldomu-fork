package com.sgo.saldomu.adapter;/*
  Created by Administrator on 2/10/2015.
 */

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sgo.saldomu.Beans.ReportListModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.models.retrofit.ReportDataModel;

import java.util.ArrayList;
import java.util.List;

public class ReportListAdapter extends ArrayAdapter<ReportDataModel>{

    private Context context;
    private int layoutResourceId;
    private ArrayList<ReportListModel> data = null;
    List<ReportDataModel> report_data;

//    public ReportListAdapter(Context context, int resource, ArrayList<ReportListModel> objects) {
//        super(context, resource, objects);
//        this.layoutResourceId = resource;
//        this.context = context;
//        this.data = objects;
//    }

    public ReportListAdapter(Context context, int resource, List<ReportDataModel> report_data) {
        super(context, resource, report_data);
        this.layoutResourceId = resource;
        this.context = context;
        this.report_data = report_data;
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
            holder.tv_date = row.findViewById(R.id.text_tgl_trans);
            holder.tv_type = row.findViewById(R.id.text_trans_type);
            holder.tv_desc = row.findViewById(R.id.description_value);
            holder.tv_ccy = row.findViewById(R.id.text_ccyID);
            holder.tv_amount = row.findViewById(R.id.text_amount);
            holder.tv_remark = row.findViewById(R.id.text_remark);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

        ReportDataModel itemnya = report_data.get(position);

        holder.tv_date.setText(itemnya.getDatetime());
        holder.tv_type.setText(itemnya.getBuss_scheme_name());
        holder.tv_desc.setText(itemnya.getTo_alias());
        holder.tv_ccy.setText(itemnya.getCcy_id());
        holder.tv_amount.setText(CurrencyFormat.format(itemnya.getAmount()));
        holder.tv_remark.setText(itemnya.getRemark());

        return row;
    }

    @Override
    public int getCount() {
        return report_data.size();
    }

    class ListHolder
    {
        TextView tv_date,tv_type,tv_desc,tv_ccy,tv_amount,tv_remark;
    }

}
