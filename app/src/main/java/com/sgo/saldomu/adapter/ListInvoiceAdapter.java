package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.models.ListPOModel;

import java.util.ArrayList;
import java.util.Locale;

public class ListInvoiceAdapter extends RecyclerView.Adapter<ListInvoiceAdapter.ViewHolder> {

    private ArrayList<ListPOModel> docListArrayList;
    static ListInvoiceAdapter.listener listener;


    public ListInvoiceAdapter(ArrayList<ListPOModel> docListArrayList, ListInvoiceAdapter.listener _listener) {
        this.docListArrayList = docListArrayList;
        listener = _listener;
    }

    public interface listener {
        void onClick(ListPOModel item);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListInvoiceAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_inv, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListInvoiceAdapter.ViewHolder holder, int position) {
        holder.docNo.setText(docListArrayList.get(position).getDoc_no());
        holder.docStatus.setText(docListArrayList.get(position).getDoc_status());
        holder.totalAmount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(docListArrayList.get(position).getNett_amount()));
        holder.dueDate.setText(docListArrayList.get(position).getDue_date());
        holder.paidStatus.setText(docListArrayList.get(position).getPaid_status_remark());


        holder.tv_custId.setText(docListArrayList.get(position).getCust_id());
        holder.tv_comm_code.setText(docListArrayList.get(position).getComm_code());
        holder.tv_member_code.setText(docListArrayList.get(position).getMember_code());
        holder.tv_type_id.setText(docListArrayList.get(position).getType_id());
        holder.tv_reff_no.setText(docListArrayList.get(position).getReff_no());
        holder.tv_reff_id.setText(docListArrayList.get(position).getReff_id());
        holder.tv_issued_date.setText(docListArrayList.get(position).getIssue_date());
        holder.tv_created_at.setText(docListArrayList.get(position).getCreated_at());
        holder.tv_partner.setText(docListArrayList.get(position).getPartner());

        holder.layout.setOnClickListener(view -> listener.onClick(docListArrayList.get(position)));
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
        TextView docNo, docStatus, totalAmount, dueDate, paidStatus, tv_custId, tv_comm_code, tv_member_code, tv_type_id,
                tv_reff_no,
                tv_reff_id,
                tv_issued_date,
                tv_created_at,
                tv_partner;
        ;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            docNo = itemView.findViewById(R.id.tv_docNo);
            docStatus = itemView.findViewById(R.id.tv_docStatus);
            totalAmount = itemView.findViewById(R.id.tv_totalAmount);
            dueDate = itemView.findViewById(R.id.tv_due_date);
            paidStatus = itemView.findViewById(R.id.tv_paid_status);
            layout = itemView.findViewById(R.id.layout1);

            tv_custId = itemView.findViewById(R.id.tv_custId);
            tv_comm_code = itemView.findViewById(R.id.tv_comm_code);
            tv_member_code = itemView.findViewById(R.id.tv_member_code);
            tv_type_id = itemView.findViewById(R.id.tv_type_id);
            tv_reff_no = itemView.findViewById(R.id.tv_reff_no);
            tv_reff_id = itemView.findViewById(R.id.tv_reff_id);
            tv_issued_date = itemView.findViewById(R.id.tv_issued_date);
            tv_created_at = itemView.findViewById(R.id.tv_created_at);
            tv_partner = itemView.findViewById(R.id.tv_partner);


        }
    }


}
