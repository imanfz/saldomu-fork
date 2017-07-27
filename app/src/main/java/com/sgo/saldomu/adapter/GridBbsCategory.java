package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.ShopCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo Thinkpad on 5/5/2017.
 */
public class GridBbsCategory extends BaseAdapter {



    public interface myInterface
    {
        void click();
    }

    private Context mContext;
    TextView textView;
    ImageView imageView;
    List<ShopCategory> shopCategories;


    public GridBbsCategory(Context c, List<ShopCategory> shopCategories) {
        mContext = c;
        this.shopCategories = shopCategories;
    }

    class ViewHolder
    {
        TextView tvCategoryName;
        ImageView ivCategory;
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
        return shopCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return shopCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid = convertView;
        ShopCategory shopCategory = (ShopCategory) this.getItem(position);
        GridBbsCategory.ViewHolder holder;

        if (grid == null) {

            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            grid = inflater.inflate(R.layout.grid_bbs_category, null);

            holder                  = new GridBbsCategory.ViewHolder();
            holder.tvCategoryName   = (TextView) grid.findViewById(R.id.grid_text);
            holder.ivCategory       = (ImageView) grid.findViewById(R.id.grid_image);

            grid.setTag(holder);

        } else {
            holder =  (GridBbsCategory.ViewHolder) grid.getTag();
        }



            holder.tvCategoryName.setText(shopCategory.getCategoryName());
            holder.ivCategory.setImageResource(shopCategory.getCategoryImage());

        return grid;
    }

}
