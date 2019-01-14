package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;

import java.util.ArrayList;

/**
 * Created by Lenovo on 22/08/2017.
 */

public class GridBbsMenu extends BaseAdapter {

    public interface myInterface {
        void click();
    }

    private Context mContext;
    private final ArrayList<String> menuItems;
    private final int[] menuIcons;

    public GridBbsMenu(Context c, ArrayList<String> menuItems, int[] menuIcons) {
        mContext = c;
        this.menuIcons = menuIcons;
        this.menuItems = menuItems;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    class ViewHolder {
        TextView tvMenuName;
        ImageView ivMenuIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        GridBbsMenu.ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.grid_bbs_menu, null);

            holder = new GridBbsMenu.ViewHolder();
            holder.tvMenuName = rootView.findViewById(R.id.tvMenuName);
            holder.ivMenuIcon = (ImageView) rootView.findViewById(R.id.ivMenuIcon);

            rootView.setTag(holder);
        } else {
            holder = (GridBbsMenu.ViewHolder) rootView.getTag();
        }

        holder.tvMenuName.setText(menuItems.get(position));
        holder.ivMenuIcon.setImageResource(getIcons(menuItems.get(position)));

        return rootView;
    }

    int getIcons(String item) {
        switch (item) {
            case "Tagih.id":
                return R.drawable.tagih_id;
            case "Pengaturan":
                return R.drawable.settings;
            case "Dalam Proses":
                return R.drawable.ic_location_on_black;
            default:
                return R.drawable.ic_tariktunai;

        }
    }
}
