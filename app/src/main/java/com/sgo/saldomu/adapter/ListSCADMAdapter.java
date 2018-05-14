package com.sgo.saldomu.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sgo.saldomu.Beans.SCADMModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/13/2018.
 */

public class ListSCADMAdapter extends RecyclerView.Adapter<ListSCADMAdapter.ViewHolder> {
    private ArrayList<SCADMModel> scadmModelArrayList = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scadm, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.communityCode.setText(scadmModelArrayList.get(position).getComm_code());
        holder.communityName.setText(scadmModelArrayList.get(position).getComm_name());
    }

    @Override
    public int getItemCount() {
        return scadmModelArrayList.size();
    }

    public void updateData(ArrayList<SCADMModel> scadmModelArrayList) {
        this.scadmModelArrayList = scadmModelArrayList;
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
