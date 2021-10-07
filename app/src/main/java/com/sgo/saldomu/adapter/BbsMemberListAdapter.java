package com.sgo.saldomu.adapter;

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

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.activities.BbsMerchantSetupHourActivity;
import com.sgo.saldomu.activities.BbsRegisterOpenClosedShopActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

public class BbsMemberListAdapter extends BaseAdapter {
    private ArrayList<ShopDetail> shopDetails;
    private Context context;
    private LayoutInflater inflater;

    public BbsMemberListAdapter(Context context, ArrayList<ShopDetail> shopDetails) {
        this.context = context;
        this.shopDetails = shopDetails;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {
        TextView tvShopName, tvCommName, tvMemberName, tvMemberCode;
        ImageView ivLocation, ivOpenHour, ivTutupManual;
        int position;

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

        if (rootView == null) {
            //viewHolder = new BbsMerchantCommunityListAdapter.ViewHolder();
            rootView = inflater.inflate(R.layout.bbs_member_list_adapter, null);

            holder = new ViewHolder();
            holder.tvShopName = (TextView) rootView.findViewById(R.id.text_shop_name);
            holder.tvCommName = (TextView) rootView.findViewById(R.id.text_comm_name);
            holder.tvMemberName = (TextView) rootView.findViewById(R.id.text_member_name);
            holder.tvMemberCode = (TextView) rootView.findViewById(R.id.text_member_code);
            holder.ivLocation = (ImageView) rootView.findViewById(R.id.ivLocation);
            holder.ivTutupManual = (ImageView) rootView.findViewById(R.id.ivTutupManual);
            holder.ivOpenHour = (ImageView) rootView.findViewById(R.id.ivOpenHour);

            rootView.setTag(holder);
        } else {
            holder = (BbsMemberListAdapter.ViewHolder) rootView.getTag();
        }

        ShopDetail shopDetail = (ShopDetail) getItem(position);
        holder.tvShopName.setText(shopDetail.getShopName());
        holder.tvCommName.setText(shopDetail.getCommName());
        holder.tvMemberName.setText(shopDetail.getMemberName());
        holder.tvMemberCode.setText(shopDetail.getMemberCode());
        holder.setPosition(position);

        holder.ivLocation.setOnClickListener(v -> {
            ShopDetail tempDetail = (ShopDetail) getItem(position);


            Intent intent = new Intent(context.getApplicationContext(), BbsMemberLocationActivity.class);
            intent.putExtra(DefineValue.MEMBER_ID, tempDetail.getMemberId());
            intent.putExtra(DefineValue.SHOP_ID, tempDetail.getShopId());
            intent.putExtra(DefineValue.SHOP_NAME, tempDetail.getShopName());
            intent.putExtra("memberType", tempDetail.getMemberType());
            context.startActivity(intent);

        });

        holder.ivTutupManual.setOnClickListener(v -> {

            ShopDetail tempDetail = (ShopDetail) getItem(position);

            Intent intent = new Intent(context.getApplicationContext(), BbsRegisterOpenClosedShopActivity.class);
            intent.putExtra(DefineValue.MEMBER_ID, tempDetail.getMemberId());
            intent.putExtra(DefineValue.SHOP_ID, tempDetail.getShopId());
            context.startActivity(intent);

        });

        holder.ivOpenHour.setOnClickListener(v -> {
            ShopDetail tempDetail = (ShopDetail) getItem(position);
            Intent intent = new Intent(context.getApplicationContext(), BbsMerchantSetupHourActivity.class);
            intent.putExtra(DefineValue.MEMBER_ID, tempDetail.getMemberId());
            intent.putExtra(DefineValue.SHOP_ID, tempDetail.getShopId());
            context.startActivity(intent);

        });

        return rootView;

    }
}
