package com.sgo.saldomu.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.models.ShopCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 22/03/2017.
 */

public class BbsSearchCategoryAdapter extends RecyclerView.Adapter<BbsSearchCategoryAdapter.MyViewHolder> implements Filterable {

    private ArrayList<ShopCategory> categoryList, bufferCategoryList;
    private Context context;
    private LayoutInflater inflater;
    SecurePreferences sp;
    private List<ShopCategory> filterList;

    private OnCategoryItemClickListener onCategoryItemClickListener;

    public interface OnCategoryItemClickListener {
        void onCategoryItemClick(View v, ShopCategory filterList);
    }

    public BbsSearchCategoryAdapter(Context context, ArrayList<ShopCategory> shopCategories, OnCategoryItemClickListener _onItemClick)
    {
        this.context            = context;
        this.categoryList       = shopCategories;
        this.filterList         = new ArrayList<>();
        this.bufferCategoryList = shopCategories;
        this.onCategoryItemClickListener    = _onItemClick;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sp                      = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        ImageView ivCategory;
        TextView tvCategoryName;
        private CategoryClickListener categoryClickListener;

        @Override
        public void onClick(View v) {
            categoryClickListener.onCategoryClickListener(v);
        }

        public interface CategoryClickListener {
            void onCategoryClickListener(View v);
        }

        /* Setter for listener. */
        public void setCategoryClickListener(MyViewHolder.CategoryClickListener categoryClickListener) {
            this.categoryClickListener = categoryClickListener;
        }

        public MyViewHolder(View view) {
            super(view);
            ivCategory          = (ImageView) view.findViewById(R.id.ivCategory);
            tvCategoryName      = (TextView) view.findViewById(R.id.tvCategoryName);

            view.setOnClickListener(this);
        }

    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_category_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.tvCategoryName.setText(categoryList.get(position).getCategoryName());

        holder.setCategoryClickListener(new MyViewHolder.CategoryClickListener() {
            @Override
            public void onCategoryClickListener(View v) {
                ShopCategory tempCategory;
                if ( filterList.size() > 0 ) {
                    tempCategory = filterList.get(holder.getAdapterPosition());
                } else {
                    tempCategory = categoryList.get(holder.getAdapterPosition());
                }
                onCategoryItemClickListener.onCategoryItemClick(v, tempCategory);
            }

        });

    }

    @Override
    public int getItemCount()
    {
        return categoryList.size();
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View rootView = convertView;
//
//        ViewHolder holder;
//        if(rootView == null)
//        {
//            rootView                    = inflater.inflate(R.layout.inflate_category_item, null);
//            holder                      = new ViewHolder();
//            holder.tvCategoryName       = (TextView) rootView.findViewById(R.id.tvCategoryName);
//            rootView.setTag(holder);
//        }
//        else
//        {
//            holder = (ViewHolder) rootView.getTag();
//        }
//
//        ShopCategory shopCategory = categoryList.get(position);
//        holder.tvCategoryName.setText(shopCategory.getCategoryName());
//
//        return rootView;
//    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                filterList = new ArrayList<ShopCategory>();

                if (constraint != null && constraint.length() > 0) {

                    for (int i = 0; i < bufferCategoryList.size(); i++) {
                        if ((bufferCategoryList.get(i).getCategoryName().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                            filterList.add(bufferCategoryList.get(i));
                        }
                    }
                    results.count   = filterList.size();
                    results.values  = filterList;
                } else {
                    results.count   = bufferCategoryList.size();
                    results.values  = bufferCategoryList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                categoryList = (ArrayList<ShopCategory>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}
