package com.sgo.saldomu.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.fragment.app.FragmentManager;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsListSettingKelolaActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 21/07/2017.
 */

public class GridViewIconAdapter extends BaseAdapter {

    private ArrayList<ShopDetail> shopDetails;
    private Context context;
    private LayoutInflater inflater;
    private FragmentManager fm;


    public GridViewIconAdapter(Context context, ArrayList<ShopDetail> shopDetails, FragmentManager fm)
    {
        this.context = context;
        this.shopDetails = shopDetails;
        this.fm = fm;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder
    {
        ImageView ivSetting;
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
        GridViewIconAdapter.ViewHolder holder;

        if(rootView == null) {
            rootView                = inflater.inflate(R.layout.grid_view_icon_adapter, null);

            holder                  = new GridViewIconAdapter.ViewHolder();
            holder.ivSetting        = (ImageView) rootView.findViewById(R.id.ivSetting);
            rootView.setTag(holder);
        } else {
            holder = (GridViewIconAdapter.ViewHolder) rootView.getTag();
        }

        holder.setPosition(position);

        holder.ivSetting.setOnClickListener(v -> {
            ShopDetail tempDetail = (ShopDetail) getItem(position);

            Intent intent = new Intent(context.getApplicationContext(), BbsListSettingKelolaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(DefineValue.MEMBER_ID, tempDetail.getMemberId());
            intent.putExtra(DefineValue.SHOP_ID, tempDetail.getShopId());
            intent.putExtra(DefineValue.SHOP_NAME, tempDetail.getShopName());
            intent.putExtra("memberType", tempDetail.getMemberType());
            intent.putExtra("memberName", tempDetail.getMemberName());
            intent.putExtra("category", TextUtils.join(", ", tempDetail.getCategories()));
            intent.putExtra("commName", tempDetail.getCommName());
            intent.putExtra("province", tempDetail.getShopProvince());
            intent.putExtra("district", tempDetail.getShopDistrict());
            intent.putExtra("address", tempDetail.getShopFirstAddress());
            context.startActivity(intent);


        });

        return rootView;
    }
}
