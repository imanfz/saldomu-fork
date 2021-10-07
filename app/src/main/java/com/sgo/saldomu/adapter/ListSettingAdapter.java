package com.sgo.saldomu.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.activities.BbsSetupShopClosedActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 24/07/2017.
 */

public class ListSettingAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> menu;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private String shopId, memberId, agentName, category, memberType, shopName, commName, province, district, address, flagApprove, stepApprove;

    public ListSettingAdapter(Context context, ArrayList<String> menu, String flagApprove) {
        this.context = context;
        this.menu = menu;
        this.flagApprove = flagApprove;

        if (shopDetails.size() > 0) {
            ShopDetail shopDetail = shopDetails.get(0);

            this.shopId = shopDetail.getShopId();
            this.memberId = shopDetail.getMemberId();
            this.agentName = shopDetail.getMemberName();
            this.category = "";
            this.memberType = shopDetail.getMemberType();
            this.shopName = shopDetail.getShopName();
            this.commName = shopDetail.getCommName();
            this.province = shopDetail.getShopProvince();
            this.district = shopDetail.getShopDistrict();
            this.address = shopDetail.getShopFirstAddress();
            this.stepApprove = shopDetail.getStepApprove();

        }
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder {
        TextView tvListName;
    }

    @Override
    public int getCount() {
        return menu.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        ListSettingAdapter.ViewHolder holder;

        if (rootView == null) {
            //viewHolder = new BbsMerchantCommunityListAdapter.ViewHolder();
            rootView = inflater.inflate(R.layout.list_setting_adapter, null);

            holder = new ListSettingAdapter.ViewHolder();
            holder.tvListName = (TextView) rootView.findViewById(R.id.tvListName);


            rootView.setTag(holder);
        } else {
            holder = (ListSettingAdapter.ViewHolder) rootView.getTag();
        }

        holder.tvListName.setText(menu.get(position));

        if (position > 1) {

        }

        holder.tvListName.setOnClickListener(v -> {

            if (stepApprove.equals(DefineValue.STRING_NO)) {
                if (position == 2) {
                    Intent intent = new Intent(context.getApplicationContext(), BbsMemberLocationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(DefineValue.MEMBER_ID, memberId);
                    intent.putExtra(DefineValue.SHOP_ID, shopId);
                    intent.putExtra(DefineValue.SHOP_NAME, shopName);
                    intent.putExtra("memberType", memberType);
                    intent.putExtra("memberName", agentName);
                    intent.putExtra("commName", commName);
                    intent.putExtra("province", province);
                    intent.putExtra("district", district);
                    intent.putExtra("address", address);
                    intent.putExtra("category", category);
                    inflater.getContext().startActivity(intent);
                } else if (position == 3) {
                    Intent intent = new Intent(context.getApplicationContext(), BbsSetupShopClosedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(DefineValue.MEMBER_ID, memberId);
                    intent.putExtra(DefineValue.SHOP_ID, shopId);
                    intent.putExtra(DefineValue.FLAG_APPROVE, flagApprove);
                    inflater.getContext().startActivity(intent);
                }
            } else {
                if (position == 2) {
                    Intent intent = new Intent(context.getApplicationContext(), BbsSetupShopClosedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(DefineValue.MEMBER_ID, memberId);
                    intent.putExtra(DefineValue.SHOP_ID, shopId);
                    intent.putExtra(DefineValue.FLAG_APPROVE, flagApprove);
                    inflater.getContext().startActivity(intent);
                }
            }

            if (position == 0) {

            } else if (position == 1) {

            } else if (position == 2) {


            } else if (position == 3) {


            }
        });

        return rootView;
    }
}
