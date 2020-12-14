package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sgo.saldomu.R;
import com.sgo.saldomu.models.ListPOModel;

import java.util.ArrayList;

public class ListPOAdapter extends RecyclerView.Adapter<ListPOAdapter.ViewHolder>  {
    private final Activity mContext;
    private ArrayList<ListPOModel> docListArrayList;
    static ListPOAdapter.listener listener;


    public ListPOAdapter(ArrayList<ListPOModel> docListArrayList, Activity mContext, ListPOAdapter.listener _listener) {
        this.docListArrayList = docListArrayList;
        this.mContext = mContext;
        listener = _listener;
    }

    public interface listener{
        void onClick(ListPOModel item);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListPOAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_po, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListPOAdapter.ViewHolder holder, int position) {
        holder.docNo.setText(docListArrayList.get(position).getDoc_no());
        holder.docStatus.setText(docListArrayList.get(position).getDoc_status());
        holder.totalAmount.setText(docListArrayList.get(position).getTotal_amount());
        holder.dueDate.setText(docListArrayList.get(position).getDue_date());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener.onClick(docListArrayList.get(position));

            }
        });
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
        TextView docNo, docStatus, totalAmount, dueDate;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            docNo = itemView.findViewById(R.id.tv_docNo);
            docStatus = itemView.findViewById(R.id.tv_docStatus);
            totalAmount = itemView.findViewById(R.id.tv_totalAmount);
            dueDate = itemView.findViewById(R.id.tv_due_date);
            layout = itemView.findViewById(R.id.layout1);
        }
    }
}
