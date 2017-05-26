package com.sgo.hpku.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsSearchTokoActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DateTimeFormat;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.HashMessage;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.models.ShopCategory;
import com.sgo.hpku.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

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
            //dtvCategoryName      = (TextView) view.findViewById(R.id.tvCategoryName);

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

    private void requestSearchTokoByCategory(View v, int position) {
        //Toast.makeText(v.getContext(), "Button Clicked " + categoryList.get(position).getCategoryCode(), Toast.LENGTH_SHORT).show();

        RequestParams params = new RequestParams();
        UUID rcUUID = UUID.randomUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.RECEIVER_ID);
        params.put(WebParams.CATEGORY_ID, categoryList.get(position).getCategoryId());
        //params.put(WebParams.LATITUDE, sp.getDouble(DefineValue.LAST_CURRENT_LATITUDE, 0.0));
        //dparams.put(WebParams.LONGITUDE, sp.getDouble(DefineValue.LAST_CURRENT_LONGITUDE, 0.0));
        params.put(WebParams.RADIUS, 1);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.SENDER_ID + DefineValue.RECEIVER_ID + BuildConfig.AppID + categoryList.get(position).getCategoryId()
                + BbsSearchTokoActivity.latitude + BbsSearchTokoActivity.longitude));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.searchToko(context, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);
                try {

                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {


                    } else {
                        Toast.makeText(context, response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {
                //llHeaderProgress.setVisibility(View.GONE);
                //pbHeaderProgress.setVisibility(View.GONE);

                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(context, Resources.getSystem().getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, throwable.toString(), Toast.LENGTH_SHORT).show();

                Timber.w("Error Koneksi login:" + throwable.toString());

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
