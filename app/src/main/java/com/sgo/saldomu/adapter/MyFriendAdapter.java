package com.sgo.saldomu.adapter;/*
  Created by Administrator on 2/10/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.sgo.saldomu.Beans.myFriendModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.RoundedQuickContactBadge;

import java.util.ArrayList;

public class MyFriendAdapter extends ArrayAdapter<myFriendModel> implements Filterable {

    private Context context;
    private int layoutResourceId;
    private ArrayList<myFriendModel> data = null;
    private ArrayList<myFriendModel> originalData = null;
    private ItemFilter mFilter;

    public MyFriendAdapter(Context context, int resource, ArrayList<myFriendModel> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.originalData = new ArrayList<>();
        this.originalData.addAll(myFriendModel.getAll());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(context);
//        else
//            mPic= Picasso.with(context);

        if(itemnya.getImg_url().isEmpty())
            GlideManager.sharedInstance().initializeGlide(context, R.drawable.user_unknown_menu, null, holder.qc_pic);
        else GlideManager.sharedInstance().initializeGlide(context, itemnya.getImg_url(), context.getResources().getDrawable(R.drawable.icon_no_photo), holder.qc_pic);


        return row;
    }

    class ListHolder
    {
        TextView txt_name,txt_number;
        RoundedQuickContactBadge qc_pic;
    }

    @NonNull
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

            if(constraint.length() == 0){
                ArrayList<myFriendModel> list = new ArrayList<>(originalData);
                results.values = list;
                results.count = list.size();
            }
            else {
                final ArrayList<myFriendModel> list = new ArrayList<>(originalData);
                final ArrayList<myFriendModel> nlist = new ArrayList<>();
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
