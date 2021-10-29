package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.models.retrofit.ReportDataModel;

import java.util.List;

/**
 * Created by Lenovo Thinkpad on 2/20/2018.
 */

public class ReportCommFeeAdapter extends ArrayAdapter<ReportDataModel> {

    private Context context;
    private int layoutResourceId;
    List<ReportDataModel> reportListModel;

    public ReportCommFeeAdapter(Context context, int resource, List<ReportDataModel> reportListModel) {
        super(context, resource, reportListModel);
        this.layoutResourceId = resource;
        this.context = context;
        this.reportListModel = reportListModel;
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
            holder.tv_date = row.findViewById(R.id.text_tgl_trans);
            holder.tv_type = row.findViewById(R.id.text_trans_type);
            holder.tv_desc = row.findViewById(R.id.description_value);
            holder.tv_ccy = row.findViewById(R.id.text_ccyID);
            holder.tv_amount = row.findViewById(R.id.text_amount);
            holder.tv_status = row.findViewById(R.id.text_status);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

//        ReportListCommFeeModel itemnya = data.get(position);

        ReportDataModel itemnya = reportListModel.get(position);

        holder.tv_date.setText(DateTimeFormat.formatToID(itemnya.getCreated()));
        holder.tv_type.setText(itemnya.getBbs_name());
        holder.tv_desc.setText(itemnya.getComm_name());
        holder.tv_ccy.setText(itemnya.getCcy_id());
        holder.tv_amount.setText(CurrencyFormat.format(itemnya.getAmount()));
        holder.tv_status.setText(itemnya.getStatus());

        return row;
    }

    class ListHolder
    {
        TextView tv_date,tv_type,tv_desc,tv_ccy,tv_amount, tv_status;
    }
}
