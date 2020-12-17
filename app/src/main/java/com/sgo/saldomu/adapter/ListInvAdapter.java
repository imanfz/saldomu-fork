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

public class ListInvAdapter extends RecyclerView.Adapter<ListInvAdapter.ViewHolder> {
    private final Activity mContext;
    private ArrayList<ListPOModel> docListArrayList;
    static ListInvAdapter.listener listener;


    public ListInvAdapter(ArrayList<ListPOModel> docListArrayList, Activity mContext, ListInvAdapter.listener _listener) {
        this.docListArrayList = docListArrayList;
        this.mContext = mContext;
        listener = _listener;
    }

    public interface listener {
        void onClick(ListPOModel item);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListInvAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_inv, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListInvAdapter.ViewHolder holder, int position) {
        holder.tv_cCode.setText(docListArrayList.get(position).getDoc_no());
        holder.tv_mCode.setText(docListArrayList.get(position).getDoc_status());
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
        TextView tv_mCode, tv_cCode;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_mCode = itemView.findViewById(R.id.tv_mCode);
            tv_cCode = itemView.findViewById(R.id.tv_cCode);
            layout = itemView.findViewById(R.id.layout1);
        }
    }

}

