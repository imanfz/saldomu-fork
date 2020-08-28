package com.sgo.saldomu.adapter;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/16/2018.
 */

public class ListTopUpSCADMAdapter extends RecyclerView.Adapter<ListTopUpSCADMAdapter.ViewHolder> {
    private final Activity mContext;
    Fragment mFrag;
    private ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList;
    listener listener;


    public ListTopUpSCADMAdapter(ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList, Activity mContext, listener _listener) {
        this.scadmCommunityModelArrayList = scadmCommunityModelArrayList;
        this.mContext = mContext;
        listener = _listener;
//        this.frameLayout = frameLayout;
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
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                listener.onClick(scadmCommunityModelArrayList.get(position));

//                Bundle bundle = new Bundle();
//                bundle.putString(DefineValue.COMMUNITY_NAME,scadmCommunityModelArrayList.get(position).getComm_name());
//                bundle.putString(DefineValue.COMM_ID_SCADM,scadmCommunityModelArrayList.get(position).getComm_id());
//                bundle.putString(DefineValue.COMMUNITY_CODE,scadmCommunityModelArrayList.get(position).getComm_code());
//                bundle.putString(DefineValue.MEMBER_CODE,scadmCommunityModelArrayList.get(position).getMember_code());
//                bundle.putString(DefineValue.API_KEY,scadmCommunityModelArrayList.get(position).getApi_key());
//                bundle.putString(DefineValue.MEMBER_ID_SCADM,scadmCommunityModelArrayList.get(position).getMember_id_scadm());
//                TopUpSCADMActivity ftf = (TopUpSCADMActivity) mContext;
//
//                mFrag = new FragTopUpSCADM();
//                ftf.switchContent(mFrag, ListTopUpSCADMAdapter.this.toString(), true);
//
//                mFrag.setArguments(bundle);
//
//                if (mContext == null) {
//                    return;
//                }
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
