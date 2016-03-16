package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 2/10/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.sgo.orimakardaya.Beans.myFriendModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundedQuickContactBadge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyFriendAdapter extends ArrayAdapter<myFriendModel> implements Filterable {

    Context context;
    int layoutResourceId;
    ArrayList<myFriendModel> data = null;
    ArrayList<myFriendModel> originalData = null;
    private ItemFilter mFilter;

    public MyFriendAdapter(Context context, int resource, ArrayList<myFriendModel> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.originalData = new ArrayList<myFriendModel>();
        this.originalData.addAll(myFriendModel.getAll());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ListHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ListHolder();
            holder.txt_name = (TextView)row.findViewById(R.id.txtListName_contact_friends);
            holder.txt_number = (TextView)row.findViewById(R.id.txtListNumber_contact_friends);
            holder.qc_pic = (RoundedQuickContactBadge)row.findViewById(R.id.contact_icon_friends);

            row.setTag(holder);
        }
        else
        {
            holder = (ListHolder)row.getTag();
        }

        myFriendModel itemnya = data.get(position);

        holder.txt_name.setText(itemnya.getFull_name());
        holder.txt_number.setText(itemnya.getFriend_number());

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(context);
        else
            mPic= Picasso.with(context);

        if(itemnya != null && itemnya.getImg_url().isEmpty())
            mPic.load(R.drawable.user_unknown_menu)
                .fit()
                .centerCrop()
                .into(holder.qc_pic);
        else
            mPic.load(itemnya.getImg_url())
                .error(R.drawable.user_unknown_menu)
                .placeholder(R.anim.progress_animation)
                .fit()
                .centerCrop()
                .into(holder.qc_pic);

        return row;
    }

    class ListHolder
    {
        TextView txt_name,txt_number;
        RoundedQuickContactBadge qc_pic;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null){
            mFilter  = new ItemFilter();
        }
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0){
                ArrayList<myFriendModel> list = new ArrayList<myFriendModel>(originalData);
                results.values = list;
                results.count = list.size();
            }
            else {
                final ArrayList<myFriendModel> list = new ArrayList<myFriendModel>(originalData);
                final ArrayList<myFriendModel> nlist = new ArrayList<myFriendModel>();
                int count = list.size();

                for (int i = 0; i < count; i++) {
                    final myFriendModel friendModel = list.get(i);
                    final String filterableName = friendModel.getFull_name();
                    final String filterablePhone = friendModel.getFriend_number();

                    if (filterableName.toLowerCase().contains(filterString) || filterablePhone.contains(filterString)) {
                        nlist.add(friendModel);
                    }
                }

                results.values = nlist;
                results.count = nlist.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data = (ArrayList<myFriendModel>) results.values;

            clear();
            addAll(data);
            notifyDataSetChanged();
        }

    }

}
