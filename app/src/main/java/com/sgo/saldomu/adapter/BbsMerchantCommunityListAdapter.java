package com.sgo.saldomu.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;
import java.util.ArrayList;
import io.realm.Realm;


/**
 * Created by Lenovo on 07/03/2017.
 */

public class BbsMerchantCommunityListAdapter extends BaseAdapter {

    private ArrayList<MerchantCommunityList> merchantCommunityListModel;
    private Context context;
    private LayoutInflater inflater;
    private Realm myRealm;

    public BbsMerchantCommunityListAdapter(Context context, ArrayList<MerchantCommunityList> merchantCommunityListModel)
    {
        this.context = context;
        this.merchantCommunityListModel = merchantCommunityListModel;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    class ViewHolder
    {
        TextView tvMemberName, tvShopName, tvCommName, tvAddress;
        ImageView ivRegister;
    }

    @Override
    public int getCount() {
        return merchantCommunityListModel.size();
    }

    @Override
    public Object getItem(int position) {
        return merchantCommunityListModel.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        View rootView = convertView;
        //BbsMerchantCommunityListAdapter.ViewHolder viewHolder;
        ViewHolder holder;
        if(rootView == null)
        {
            //viewHolder = new BbsMerchantCommunityListAdapter.ViewHolder();

            rootView = inflater.inflate(R.layout.inflate_community_list_item, null);
            holder = new ViewHolder();
            holder.tvMemberName  = (TextView) rootView.findViewById(R.id.tvMemberName);
            holder.tvShopName  = (TextView) rootView.findViewById(R.id.tvShopName);
            holder.ivRegister       = (ImageView) rootView.findViewById(R.id.ivRegister);
            holder.tvCommName   = (TextView) rootView.findViewById(R.id.tvCommName);
            holder.tvAddress   = (TextView) rootView.findViewById(R.id.tvAddress);
            rootView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) rootView.getTag();
        }

        //MerchantCommunityList result = myRealm.where(MerchantCommunityList.class).equalTo("id", "CAT1").findFirst();

        MerchantCommunityList merchantCommunityList = (MerchantCommunityList) getItem(position);
        holder.tvMemberName.setText(context.getString(R.string.member_name) + " : " + merchantCommunityList.getMemberName());
        holder.tvShopName.setText(context.getString(R.string.shop_name) + " : " + merchantCommunityList.getShopName());
        holder.tvCommName.setText(context.getString(R.string.community) + " : " + merchantCommunityList.getCommName());
        holder.tvAddress.setText(context.getString(R.string.myprofile_text_address) + " : " + merchantCommunityList.getAddress1() + ", " + merchantCommunityList.getDistrict() + ", "+ merchantCommunityList.getProvince() );

        holder.ivRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MerchantCommunityList merchantCommunityList = (MerchantCommunityList) getItem(position);
                Intent intent=new Intent(context, BbsMemberLocationActivity.class);
                intent.putExtra("memberId", merchantCommunityList.getMemberId());
                intent.putExtra("shopId", merchantCommunityList.getShopId());
                intent.putExtra("shopName", merchantCommunityList.getShopName());
                context.startActivity(intent);
            }
        });


        return rootView;
    }

}
