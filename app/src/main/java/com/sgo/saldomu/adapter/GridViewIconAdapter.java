package com.sgo.saldomu.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsListSettingKelolaActivity;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

/**
 * Created by Lenovo on 21/07/2017.
 */

public class GridViewIconAdapter extends BaseAdapter {

    private ArrayList<ShopDetail> shopDetails;
    private Context context;
    private LayoutInflater inflater;
    private String memberType;
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

        holder.ivSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShopDetail tempDetail = (ShopDetail) getItem(position);


                /*FragListSettingKelola fragSettingKelola = new FragListSettingKelola();
                Bundle dataBundle = new Bundle();
                dataBundle.putString("memberId", tempDetail.getMemberId() );
                dataBundle.putString("shopId", tempDetail.getShopId() );

                fragSettingKelola.setArguments(dataBundle);
                switchFragment(fragSettingKelola, context.getString(R.string.setting_agent) );
                */

                Intent intent = new Intent(context.getApplicationContext(), BbsListSettingKelolaActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("memberId", tempDetail.getMemberId());
                intent.putExtra("shopId", tempDetail.getShopId());
                intent.putExtra("shopName", tempDetail.getShopName());
                intent.putExtra("memberType", tempDetail.getMemberType());
                intent.putExtra("memberName", tempDetail.getMemberName());
                intent.putExtra("category", TextUtils.join(", ", tempDetail.getCategories()));
                intent.putExtra("commName", tempDetail.getCommName());
                intent.putExtra("province", tempDetail.getShopProvince());
                intent.putExtra("district", tempDetail.getShopDistrict());
                intent.putExtra("address", tempDetail.getShopFirstAddress());
                context.startActivity(intent);


            }
        });

        return rootView;
    }

    private void switchFragment(Fragment i, String name){
        if (context == null)
            return;

        //MainPage fca = (MainPage) i.getActivity();
        //fca.switchContent(i,name);

        fm.beginTransaction().add(R.id.main_page_content, i, "com.sgo.saldomu.fragments.FragKelolaAgent").commit();

    }
}
