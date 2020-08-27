package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.models.InvoiceDGI;

import java.util.ArrayList;

public class InvoiceDGIAdapter extends RecyclerView.Adapter<InvoiceDGIAdapter.ViewHolder> implements Filterable {
    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList;
    private ArrayList<InvoiceDGI> originalList;
    private Context context;
    OnTap listener;

    public interface OnTap {
        void onTap(InvoiceDGI model);
    }

    public InvoiceDGIAdapter(ArrayList<InvoiceDGI> invoiceDGIModelArrayList, Activity mContext, OnTap listener) {
        this.invoiceDGIModelArrayList = invoiceDGIModelArrayList;
        originalList = invoiceDGIModelArrayList;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvinvoiceNo, tvremainAmount, tvInputremainAmount, tvdueDate;
        View view;
        LinearLayout topLayout, remainAmountLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tvinvoiceNo = itemView.findViewById(R.id.tv_invoice_no);
            tvremainAmount = itemView.findViewById(R.id.tv_remain_amount);
            tvInputremainAmount = itemView.findViewById(R.id.tv_input_remain_amount);
            tvdueDate = itemView.findViewById(R.id.tv_due_date);
            view = itemView.findViewById(R.id.view_list_invoice);
            topLayout = itemView.findViewById(R.id.top_layout);
            remainAmountLayout = itemView.findViewById(R.id.ll_remain_amount);
        }
    }

    public void updateData(ArrayList<InvoiceDGI> invoiceDGIModelArrayList) {
        this.invoiceDGIModelArrayList = invoiceDGIModelArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceDGIAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceDGIAdapter.ViewHolder holder, final int position) {

        InvoiceDGI obj = invoiceDGIModelArrayList.get(position);

        holder.tvinvoiceNo.setText(context.getString(R.string.invoice) + " " + invoiceDGIModelArrayList.get(position).getDoc_no());
        holder.tvremainAmount.setText(CurrencyFormat.format(invoiceDGIModelArrayList.get(position).getRemain_amount()));
        holder.tvdueDate.setText(invoiceDGIModelArrayList.get(position).getDue_date());

        if (obj.getInput_amount().equalsIgnoreCase("0") || obj.getInput_amount().equalsIgnoreCase("")) {
            holder.remainAmountLayout.setVisibility(View.GONE);
        } else {
            holder.remainAmountLayout.setVisibility(View.VISIBLE);
            holder.tvInputremainAmount.setText(CurrencyFormat.format(invoiceDGIModelArrayList.get(position).getInput_amount()));
        }
        holder.view.setVisibility(View.VISIBLE);

        holder.topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTap(invoiceDGIModelArrayList.get(position));
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
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase();
                ArrayList<InvoiceDGI> temp = new ArrayList<>();
                if (charString.isEmpty()) {
                    temp.addAll(originalList);
                } else {
                    for (InvoiceDGI model : originalList){
                        if (model.getDoc_no().toLowerCase().contains(charString))
                            temp.add(model);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = temp;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                invoiceDGIModelArrayList = new ArrayList<>((ArrayList<InvoiceDGI>)results.values);
                notifyDataSetChanged();
            }
        };
    }
}
