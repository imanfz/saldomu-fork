package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 21/04/2017.
 */

public class BbsMemberShopAdapter extends BaseAdapter {

    private ArrayList<ShopDetail> shopDetails;
    private Context context;
    private LayoutInflater inflater;

    public BbsMemberShopAdapter(Context context, ArrayList<ShopDetail> shopDetails)
    {
        this.context = context;
        this.shopDetails = shopDetails;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder
    {
        TextView tvShopName, tvCommName, tvMemberName, tvMemberCode;
    }

    @Override
    public int getCount() {
        return shopDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return shopDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        ViewHolder holder;

        if(rootView == null) {
            //viewHolder = new BbsMerchantCommunityListAdapter.ViewHolder();
            rootView                = inflater.inflate(R.layout.bbs_member_shop_adapter, null);

            holder                  = new ViewHolder();
            holder.tvShopName       = (TextView) rootView.findViewById(R.id.text_shop_name);
            holder.tvCommName       = (TextView) rootView.findViewById(R.id.text_comm_name);
            holder.tvMemberName     = (TextView) rootView.findViewById(R.id.text_member_name);
            holder.tvMemberCode     = (TextView) rootView.findViewById(R.id.text_member_code);


            rootView.setTag(holder);
        } else {
            holder = (ViewHolder) rootView.getTag();
        }

        ShopDetail shopDetail   = (ShopDetail) getItem(position);
        holder.tvShopName.setText(shopDetail.getShopName());
        holder.tvCommName.setText(shopDetail.getCommName());
        holder.tvMemberName.setText(shopDetail.getMemberName());
        holder.tvMemberCode.setText(shopDetail.getMemberCode());

        return rootView;

    }
}
