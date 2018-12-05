package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.InvoiceDGI;

import java.util.ArrayList;

public class InvoiceDGIAdapter extends RecyclerView.Adapter<InvoiceDGIAdapter.ViewHolder> implements Filterable {
    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList;
    private final Activity mContext;
    OnTap listener;

    public interface OnTap{
        void onTap(int pos);
    }

    public InvoiceDGIAdapter(ArrayList<InvoiceDGI> invoiceDGIModelArrayList, Activity mContext, OnTap listener) {
        this.invoiceDGIModelArrayList = invoiceDGIModelArrayList;
        this.mContext = mContext;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvinvoiceNo, tvremainAmount, tvInputremainAmount, tvdueDate;
        View view;
        LinearLayout topLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tvinvoiceNo = itemView.findViewById(R.id.tv_invoice_no);
            tvremainAmount = itemView.findViewById(R.id.tv_remain_amount);
            tvInputremainAmount = itemView.findViewById(R.id.tv_input_remain_amount);
            tvdueDate = itemView.findViewById(R.id.tv_due_date);
            view = itemView.findViewById(R.id.view_list_invoice);
            topLayout = itemView.findViewById(R.id.top_layout);
        }
    }
    public void updateData(ArrayList<InvoiceDGI> invoiceDGIModelArrayList) {
        this.invoiceDGIModelArrayList = invoiceDGIModelArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceDGIAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceDGIAdapter.ViewHolder holder, final int position) {

        InvoiceDGI obj = invoiceDGIModelArrayList.get(position);

        holder.tvinvoiceNo.setText("INVOICE " +invoiceDGIModelArrayList.get(position).getDoc_no());
        holder.tvremainAmount.setText("Sisa : " +invoiceDGIModelArrayList.get(position).getRemain_amount());
        holder.tvdueDate.setText("Due date : " +invoiceDGIModelArrayList.get(position).getDue_date());

        if (obj.getInput_amount().equalsIgnoreCase("0") || obj.getInput_amount().equalsIgnoreCase("")){
            holder.tvInputremainAmount.setVisibility(View.GONE);
        }else {
            holder.tvInputremainAmount.setVisibility(View.VISIBLE);
            holder.tvInputremainAmount.setText("Bayar : " +invoiceDGIModelArrayList.get(position).getInput_amount());
        }
        holder.view.setVisibility(View.VISIBLE);

        holder.topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTap(position);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return invoiceDGIModelArrayList.size();
    }

    @Override
    public Filter getFilter() {return null;
    }
}
