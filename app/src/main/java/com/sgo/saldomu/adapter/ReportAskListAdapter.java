package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sgo.saldomu.Beans.ReportAskListModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.models.retrofit.GetReportDataModel;
import com.sgo.saldomu.models.retrofit.ReportDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thinkpad on 10/22/2015.
 */
public class ReportAskListAdapter extends ArrayAdapter<ReportDataModel> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<ReportAskListModel> data = null;
    List<ReportDataModel> reportListModel;

//    public ReportAskListAdapter(Context context, int resource, ArrayList<ReportAskListModel> objects) {
//        super(context, resource, objects);
//        this.layoutResourceId = resource;
//        this.context = context;
//        this.data = objects;
//    }

    public ReportAskListAdapter(Context context, int resource, List<ReportDataModel> reportListModel) {
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

            holder = new ListHolder();
            holder.tv_date = row.findViewById(R.id.text_tgl_trans);
            holder.tv_type = row.findViewById(R.id.text_trans_type);
            holder.tv_desc = row.findViewById(R.id.description_value);
            holder.tv_ccy = row.findViewById(R.id.text_ccyID);
            holder.tv_amount = row.findViewById(R.id.text_amount);
            holder.tv_remark = row.findViewById(R.id.text_remark);
            holder.tv_status = row.findViewById(R.id.text_status);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

//        ReportAskListModel itemnya = data.get(position);

        ReportDataModel itemnya = reportListModel.get(position);

        holder.tv_date.setText(DateTimeFormat.formatToID(itemnya.getDatetime()));
        holder.tv_type.setText("Minta Saldo");
        holder.tv_desc.setText(itemnya.getDescription());
        holder.tv_ccy.setText(itemnya.getCcy_id());
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
