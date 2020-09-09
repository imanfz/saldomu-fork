package com.sgo.saldomu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/14/2018.
 */

public class ListBankSCADMAdapter extends RecyclerView.Adapter<ListBankSCADMAdapter.ViewHolder> {
    private ArrayList<listBankModel> scadmListTopUpModelArrayList = new ArrayList<>();

    @Override
    public ListBankSCADMAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scadm, parent, false));
    }

    @Override
    public void onBindViewHolder(ListBankSCADMAdapter.ViewHolder holder, int position) {
        holder.communityCode.setText(scadmListTopUpModelArrayList.get(position).getBank_code());
        holder.communityName.setText(scadmListTopUpModelArrayList.get(position).getBank_name());
    }

    @Override
    public int getItemCount() {
        return scadmListTopUpModelArrayList.size();
    }

    public void updateDataBank(ArrayList<listBankModel> scadmListTopUpModelArrayList) {
        this.scadmListTopUpModelArrayList = scadmListTopUpModelArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView communityName, communityCode;

        public ViewHolder(View itemView) {
            super(itemView);
            communityName = itemView.findViewById(R.id.community_name);
            communityCode = itemView.findViewById(R.id.community_code);
        }
    }
}
