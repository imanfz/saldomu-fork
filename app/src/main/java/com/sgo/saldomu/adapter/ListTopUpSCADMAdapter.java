package com.sgo.saldomu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/16/2018.
 */

public class ListTopUpSCADMAdapter extends RecyclerView.Adapter<ListTopUpSCADMAdapter.ViewHolder> {
    private ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList;
    listener listener;


    public ListTopUpSCADMAdapter(ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList, listener _listener) {
        this.scadmCommunityModelArrayList = scadmCommunityModelArrayList;

        listener = _listener;
    }

    public interface listener{
        void onClick(SCADMCommunityModel item);
    }

    @Override
    public ListTopUpSCADMAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListTopUpSCADMAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topup_denom_scadm, parent, false));
    }

    @Override
    public void onBindViewHolder(final ListTopUpSCADMAdapter.ViewHolder holder, final int position) {
        holder.communityCode.setText(scadmCommunityModelArrayList.get(position).getComm_code());
        holder.communityName.setText(scadmCommunityModelArrayList.get(position).getComm_name());
        holder.view.setVisibility(View.VISIBLE);
        holder.layout.setOnClickListener(view -> listener.onClick(scadmCommunityModelArrayList.get(position)));
    }

    @Override
    public int getItemCount() {
        return scadmCommunityModelArrayList.size();
    }

    public void updateData(ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList) {
        this.scadmCommunityModelArrayList = scadmCommunityModelArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView communityName, communityCode;
        View view;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            communityName = itemView.findViewById(R.id.community_name);
            communityCode = itemView.findViewById(R.id.community_code);
            view = itemView.findViewById(R.id.view_list_scadm);
            layout = itemView.findViewById(R.id.layout1);
        }
    }
}
