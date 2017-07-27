package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.OpenHourDays;

import java.util.ArrayList;

/**
 * Created by Lenovo on 29/03/2017.
 */

public class KelolaMerchantAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> menuItems;
    ArrayList<Integer> menuIcons;
    LayoutInflater inflater;

    public KelolaMerchantAdapter(Context applicationContext, ArrayList<String> menuItems, ArrayList<Integer> menuIcons) {
        this.context    = applicationContext;
        this.menuItems  = menuItems;
        this.menuIcons  = menuIcons;
        inflater        = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return this.menuItems.size();
    }
    @Override
    public Object getItem(int i) {
        return this.menuItems.get(i);
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View rootView = view;
        ViewHolder holder;

        if(rootView == null)
        {
            //viewHolder = new BbsMerchantCommunityListAdapter.ViewHolder();

            rootView = inflater.inflate(R.layout.adapter_kelola_merchant, null);
            holder = new KelolaMerchantAdapter.ViewHolder();
            holder.tvName           = (TextView) rootView.findViewById(R.id.tvName);
            holder.ivIcon           = (ImageView) rootView.findViewById(R.id.ivIcon);
            rootView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) rootView.getTag();
        }

        holder.tvName.setText( menuItems.get(i) );
        holder.ivIcon.setImageResource(menuIcons.get(i));

        return rootView;
    }

    class ViewHolder
    {
        TextView tvName;
        ImageView ivIcon;
    }
}