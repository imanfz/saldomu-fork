package com.sgo.saldomu.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.ListJoinCommunitySCADMActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.fragments.FragJoinCommunitySCADM;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/13/2018.
 */

public class ListSCADMAdapter extends RecyclerView.Adapter<ListSCADMAdapter.ViewHolder> {
    private final Context mContext;
    private ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList;

    public ListSCADMAdapter(ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList, Context mContext) {
        this.scadmCommunityModelArrayList = scadmCommunityModelArrayList;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scadm, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.communityCode.setText(scadmCommunityModelArrayList.get(position).getComm_code());
        holder.communityName.setText(scadmCommunityModelArrayList.get(position).getComm_name());
        holder.view.setVisibility(View.VISIBLE);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext,"on click position"+position, Toast.LENGTH_SHORT).show();
                Bundle bundle=new Bundle();
                bundle.putString(DefineValue.COMMUNITY_NAME, scadmCommunityModelArrayList.get(position).getComm_name());
                Fragment mFrag = new FragJoinCommunitySCADM();
                mFrag.setArguments(bundle);
            }
        });
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (this == null)
            return;

        ListJoinCommunitySCADMActivity fca = (ListJoinCommunitySCADMActivity) ;
        fca.switchContent(i,name,isBackstack);
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
