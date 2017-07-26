package com.sgo.hpku.adapter;

/**
 * Created by Lenovo on 16/05/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsMemberLocationActivity;
import com.sgo.hpku.activities.BbsMerchantSetupHourActivity;
import com.sgo.hpku.activities.BbsRegisterOpenClosedShopActivity;
import com.sgo.hpku.models.ShopDetail;

import java.util.ArrayList;

public class BbsMemberListAdapter extends BaseAdapter {
    private ArrayList<ShopDetail> shopDetails;
    private Context context;
    private LayoutInflater inflater;
    private String memberType;

    public BbsMemberListAdapter(Context context, ArrayList<ShopDetail> shopDetails)
    {
        this.context = context;
        this.shopDetails = shopDetails;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder
    {
        TextView tvShopName, tvCommName, tvMemberName, tvMemberCode;
        ImageView ivLocation, ivOpenHour, ivTutupManual;
        int position;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        BbsMemberListAdapter.ViewHolder holder;

        if(rootView == null) {
            //viewHolder = new BbsMerchantCommunityListAdapter.ViewHolder();
            rootView                = inflater.inflate(R.layout.bbs_member_list_adapter, null);

            holder                  = new BbsMemberListAdapter.ViewHolder();
            holder.tvShopName       = (TextView) rootView.findViewById(R.id.text_shop_name);
            holder.tvCommName       = (TextView) rootView.findViewById(R.id.text_comm_name);
            holder.tvMemberName     = (TextView) rootView.findViewById(R.id.text_member_name);
            holder.tvMemberCode     = (TextView) rootView.findViewById(R.id.text_member_code);
            holder.ivLocation       = (ImageView) rootView.findViewById(R.id.ivLocation);
            holder.ivTutupManual    = (ImageView) rootView.findViewById(R.id.ivTutupManual);
            holder.ivOpenHour       = (ImageView) rootView.findViewById(R.id.ivOpenHour);

            rootView.setTag(holder);
        } else {
            holder = (BbsMemberListAdapter.ViewHolder) rootView.getTag();
        }

        ShopDetail shopDetail   = (ShopDetail) getItem(position);
        holder.tvShopName.setText(shopDetail.getShopName());
        holder.tvCommName.setText(shopDetail.getCommName());
        holder.tvMemberName.setText(shopDetail.getMemberName());
        holder.tvMemberCode.setText(shopDetail.getMemberCode());
        holder.setPosition(position);

        holder.ivLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShopDetail tempDetail = (ShopDetail) getItem(position);


                Intent intent = new Intent(context.getApplicationContext(), BbsMemberLocationActivity.class);
                intent.putExtra("memberId", tempDetail.getMemberId());
                intent.putExtra("shopId", tempDetail.getShopId());
                intent.putExtra("shopName", tempDetail.getShopName());
                intent.putExtra("memberType", tempDetail.getMemberType());
                context.startActivity(intent);

            }
        });

        holder.ivTutupManual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ShopDetail tempDetail = (ShopDetail) getItem(position);

                Intent intent = new Intent(context.getApplicationContext(), BbsRegisterOpenClosedShopActivity.class);
                intent.putExtra("memberId", tempDetail.getMemberId());
                intent.putExtra("shopId", tempDetail.getShopId());
                context.startActivity(intent);

            }
        });

        holder.ivOpenHour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShopDetail tempDetail = (ShopDetail) getItem(position);
                Intent intent = new Intent(context.getApplicationContext(), BbsMerchantSetupHourActivity.class);
                intent.putExtra("memberId", tempDetail.getMemberId());
                intent.putExtra("shopId", tempDetail.getShopId());
                context.startActivity(intent);

            }
        });

        return rootView;

    }
}
