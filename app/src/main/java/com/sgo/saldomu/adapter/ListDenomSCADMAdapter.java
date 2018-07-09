package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.coreclass.DefineValue;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/16/2018.
 */

public class ListDenomSCADMAdapter extends RecyclerView.Adapter<ListDenomSCADMAdapter.ViewHolder> {
    private final Activity mContext;
    Fragment mFrag;
    private ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList;
    listener listener;

    public ListDenomSCADMAdapter(ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList, Activity mContext, listener _listener) {
        this.scadmCommunityModelArrayList = scadmCommunityModelArrayList;
        this.mContext = mContext;
        listener = _listener;
//        this.frameLayout = frameLayout;
    }

    public interface listener{
        void onClick(SCADMCommunityModel item);
    }

    @NonNull
    @Override
    public ListDenomSCADMAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListDenomSCADMAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topup_denom_scadm, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ListDenomSCADMAdapter.ViewHolder holder, final int position) {
        holder.communityCode.setText(scadmCommunityModelArrayList.get(position).getComm_code());
        holder.communityName.setText(scadmCommunityModelArrayList.get(position).getComm_name());
        holder.memberCode.setText(scadmCommunityModelArrayList.get(position).getMember_code());
        holder.view.setVisibility(View.VISIBLE);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener.onClick(scadmCommunityModelArrayList.get(position));
            }
        });
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
        TextView communityName, memberCode, communityCode;
        View view;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            communityName = itemView.findViewById(R.id.community_name);
            communityCode = itemView.findViewById(R.id.community_code);
            memberCode = itemView.findViewById(R.id.member_code);
            view = itemView.findViewById(R.id.view_list_scadm);
            layout = itemView.findViewById(R.id.layout1);

        }
    }
}
