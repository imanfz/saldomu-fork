package com.sgo.saldomu.adapter;/*
  Created by Administrator on 1/18/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sgo.saldomu.R;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

public class ListAccountBBSAdapter extends RealmBaseAdapter<BBSAccountACTModel> implements ListAdapter{

    public interface OnDeleteListener{
        void onCLick(int position, View view);
    }

    Context context;
    int layoutResourceId;
    private OrderedRealmCollection<BBSAccountACTModel> adata;
    private Boolean showDelete = false;
    private OnDeleteListener onDeleteListener;

    public ListAccountBBSAdapter(Context context, int layoutResourceId,
                                 OrderedRealmCollection<BBSAccountACTModel> data) {
        super(data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.adata = data;
    }

    @Override
    public int getCount() {
        if(this.adata == null)
            return 0;
        else
            return this.adata.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View row = convertView;
        ListHolder holder;
        parent.setClickable(true);

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ListHolder();
            holder.txtProductName = (TextView)row.findViewById(R.id.product_name_value);
            holder.txtAccountId = (TextView)row.findViewById(R.id.account_id_value);
            holder.txtAccountName = (TextView)row.findViewById(R.id.account_name_value);
            holder.txtAccountCity = (TextView)row.findViewById(R.id.account_city_value);
            holder.layoutCity = row.findViewById(R.id.listaccount_layout_city);
            holder.btn_delete = (Button) row.findViewById(R.id.btn_delete);
            holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteListener.onCLick(position,v);
                }
            });
            row.setTag(holder);
        }
        else {
            holder = (ListHolder)row.getTag();
        }

        if(adata != null) {

            holder.txtProductName.setText(adata.get(position).getProduct_name());
            holder.txtAccountId.setText(adata.get(position).getAccount_no());
            holder.txtAccountName.setText(adata.get(position).getAccount_name());
            String city = adata.get(position).getAccount_city();
            if (city.isEmpty())
                holder.layoutCity.setVisibility(View.GONE);
            else {
                holder.layoutCity.setVisibility(View.VISIBLE);
                holder.txtAccountCity.setText(city);
            }


            if (showDelete)
                holder.btn_delete.setVisibility(View.VISIBLE);
            else
                holder.btn_delete.setVisibility(View.GONE);

        }
        return row;
    }

    public void setDeleteListener(OnDeleteListener _deleteListener){
        this.onDeleteListener = _deleteListener;
    }


    public void toggleButtonDelete(){
        if(getCount() > 0) {
            showDelete = !showDelete;
            notifyDataSetChanged();
        }
    }

    public void deleteItem(int position){
        adata.deleteFromRealm(position);
        notifyDataSetChanged();
    }

    public void setButtonDeleteHide(){
        showDelete = false;
    }

    private class ListHolder
    {
        TextView txtProductName,txtAccountId, txtAccountName, txtAccountCity;
        Button btn_delete;
        View layoutCity;
    }
}
