package com.sgo.saldomu.adapter;

import android.app.Activity;
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
import com.sgo.saldomu.activities.JoinCommunitySCADMActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.fragments.FragJoinCommunitySCADM;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/13/2018.
 */

public class ListSCADMAdapter extends RecyclerView.Adapter<ListSCADMAdapter.ViewHolder> {
    private final Activity mContext;

    private ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList;

    public ListSCADMAdapter(ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList, Activity mContext) {
        this.scadmCommunityModelArrayList = scadmCommunityModelArrayList;
        this.mContext = mContext;
//        this.frameLayout = frameLayout;
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
                bundle.putString(DefineValue.MEMBER_NAME, scadmCommunityModelArrayList.get(position).getMember_name());
                Fragment mFrag = new FragJoinCommunitySCADM();
                mFrag.setArguments(bundle);

//                mContext.getFragmentManager().beginTransaction().replace(frameLayout, mFrag,"FragJoinCommunitySCADM" );

                if(mContext == null){
                    return;
                }
                JoinCommunitySCADMActivity ftf = (JoinCommunitySCADMActivity) mContext;
                ftf.switchContent(mFrag,"Gabung Komunitas",true);
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
