package com.sgo.saldomu.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ListPOModel;

import java.util.ArrayList;
import java.util.Locale;

public class ListPOTokoAdapter extends RecyclerView.Adapter<ListPOTokoAdapter.ViewHolder> implements Filterable {
    private final Activity mContext;
    private ArrayList<ListPOModel> docListArrayList;
    private ArrayList<ListPOModel> originalList;
    static ListPOTokoAdapter.listener listener;


    public ListPOTokoAdapter(ArrayList<ListPOModel> docListArrayList, Activity mContext, ListPOTokoAdapter.listener _listener) {
        this.docListArrayList = docListArrayList;
        this.originalList = docListArrayList;
        this.mContext = mContext;
        listener = _listener;
    }

    public interface listener {
        void onClick(String docNo);
        void onCancel(String docNo);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListPOTokoAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_po, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ListPOTokoAdapter.ViewHolder holder, int position) {
        holder.docNo.setText(docListArrayList.get(position).getDoc_no());
        holder.docStatus.setText(docListArrayList.get(position).getDoc_status());
        holder.totalAmount.setText(mContext.getString(R.string.currency) + CurrencyFormat.format(docListArrayList.get(position).getNett_amount()));
        holder.issueDate.setText(docListArrayList.get(position).getIssue_date());
        holder.paidStatus.setText(docListArrayList.get(position).getPaid_status_remark());
        holder.partner.setText(docListArrayList.get(position).getPartner());
        holder.layout.setOnClickListener(view -> listener.onClick(docListArrayList.get(position).getDoc_no()));
        holder.cancel.setOnClickListener(view -> listener.onCancel(docListArrayList.get(position).getDoc_no()));

//        if (docListArrayList.get(position).getPaid_status().equals(DefineValue.STRING_NO))
//            holder.cancel.setVisibility(View.VISIBLE);
//        else
            holder.cancel.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return docListArrayList.size();
    }

    public void updateData(ArrayList<ListPOModel> docListArrayList) {
        this.docListArrayList = docListArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView docNo, docStatus, totalAmount, issueDate, paidStatus, partner;
        Button cancel;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            docNo = itemView.findViewById(R.id.tv_docNo);
            docStatus = itemView.findViewById(R.id.tv_docStatus);
            totalAmount = itemView.findViewById(R.id.tv_totalAmount);
            issueDate = itemView.findViewById(R.id.tv_issued_date);
            paidStatus = itemView.findViewById(R.id.tv_paid_status);
            cancel = itemView.findViewById(R.id.btn_cancel_po);
            partner = itemView.findViewById(R.id.tv_partner);
            layout = itemView.findViewById(R.id.layout1);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().toLowerCase(Locale.ROOT);
                ArrayList<ListPOModel> temp = new ArrayList<>();
                if (charString.isEmpty())
                    temp.addAll(originalList);
                else
                    for (ListPOModel model : originalList) {
                        if (model.getDoc_no().toLowerCase(Locale.ROOT).contains(charString))
                            temp.add(model);
                    }
                FilterResults filterResults = new FilterResults();
                filterResults.values = temp;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                docListArrayList = new ArrayList<>((ArrayList<ListPOModel>)filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    
}
