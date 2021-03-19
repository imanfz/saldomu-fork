package com.sgo.saldomu.adapter;/*
  Created by Administrator on 2/10/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sgo.saldomu.Beans.ReportListEspayModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.retrofit.ReportDataModel;

import java.util.ArrayList;
import java.util.List;

public class ReportListEspayAdapter extends ArrayAdapter<ReportDataModel>{

    private Context context;
    private int layoutResourceId;
    private ArrayList<ReportListEspayModel> data = null;
    List<ReportDataModel> reportListModel;

//    public ReportListEspayAdapter(Context context, int resource, ArrayList<ReportListEspayModel> objects) {
//        super(context, resource, objects);
//        this.layoutResourceId = resource;
//        this.context = context;
//        this.data = objects;
//    }

    public ReportListEspayAdapter(Context context, int resource, List<ReportDataModel> reportListModel) {
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
            holder.tv_buss_scheme_name = row.findViewById(R.id.text_buss_scheme_name);
            holder.tv_comm_name = row.findViewById(R.id.text_comm_name);
            holder.tv_ccy = row.findViewById(R.id.text_ccyID);
            holder.tv_amount = row.findViewById(R.id.text_amount);
            holder.tv_desc = row.findViewById(R.id.text_description);
//            holder.tv_remark = (TextView)row.findViewById(R.id.text_remark);
            holder.tv_tx_status = row.findViewById(R.id.text_tx_status);
            holder.tv_product_name = row.findViewById(R.id.text_product_name);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

//        ReportListEspayModel itemnya = data.get(position);
        ReportDataModel itemnya = reportListModel.get(position);

        holder.tv_date.setText(DateTimeFormat.formatToID(itemnya.getCreated()));
        holder.tv_buss_scheme_name.setText(itemnya.getBuss_scheme_name());
        holder.tv_comm_name.setText(itemnya.getComm_name());
        holder.tv_ccy.setText(itemnya.getCcy_id());

        Double total = Double.parseDouble(itemnya.getAmount()) + Double.parseDouble(itemnya.getAdmin_fee()) + Double.parseDouble(itemnya.getAdditional_fee());

        holder.tv_amount.setText(CurrencyFormat.format(total));
        if(!itemnya.getDescription().equals(""))
        {
            holder.tv_desc.setVisibility(View.VISIBLE);
            holder.tv_desc.setText(itemnya.getTx_description());
        }
        else if (itemnya.getBuss_scheme_code().equalsIgnoreCase(DefineValue.QRS)){
            holder.tv_desc.setVisibility(View.VISIBLE);
            holder.tv_desc.setText(itemnya.getMerchant_name());
        }else
            holder.tv_desc.setVisibility(View.GONE);
//        if(!itemnya.getRemark().equals("")) holder.tv_remark.setText(itemnya.getRemark());
//        else holder.tv_remark.setVisibility(View.GONE);
        holder.tv_tx_status.setText(itemnya.getTx_status());
        holder.tv_product_name.setText(itemnya.getProduct_name());
        if(itemnya.getProduct_name().equalsIgnoreCase("UNIK"))
        {
            holder.tv_product_name.setText(context.getString(R.string.appname));
        }
        else {
            holder.tv_product_name.setText(itemnya.getProduct_name());
        }
        return row;
    }

    class ListHolder
    {
        TextView tv_date,tv_buss_scheme_name,tv_comm_name,tv_ccy,tv_amount,tv_desc,tv_remark, tv_tx_status, tv_product_name;
    }

}
