package com.sgo.hpku.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsSearchTokoActivity;
import com.sgo.hpku.models.ShopCategory;

import java.util.ArrayList;

/**
 * Created by Lenovo on 21/03/2017.
 */

public class BbsShopCategoryList extends RecyclerView.Adapter<BbsShopCategoryList.MyViewHolder> {

    private ArrayList<ShopCategory> categoryList;
    private Context context;
    private LayoutInflater inflater;

    public BbsShopCategoryList(Context context, ArrayList<ShopCategory> shopCategories)
    {
        this.context = context;
        this.categoryList = shopCategories;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCategory;
        TextView tvCategory;
        public MyViewHolder(View view) {
            super(view);
            ivCategory  = (ImageView) view.findViewById(R.id.ivCategory);
            tvCategory  = (TextView) view.findViewById(R.id.tvCategory);
        }
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_shop_category_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        //holder.ivCategory.setImageResource(categoryList.get(position).get);
        holder.tvCategory.setText(categoryList.get(position).getCategoryName());

        holder.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSearchTokoByCategory(v, position);
            }
        });

        holder.ivCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSearchTokoByCategory(v, position);
            }
        });

    }

    private void requestSearchTokoByCategory(View v, int position) {
        Toast.makeText(v.getContext(), "Button Clicked " + categoryList.get(position).getCategoryCode(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount()
    {
        return categoryList.size();
    }

}
