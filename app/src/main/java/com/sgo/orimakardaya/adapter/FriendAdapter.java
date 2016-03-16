package com.sgo.orimakardaya.adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.sgo.orimakardaya.Beans.friendModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundedQuickContactBadge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by thinkpad on 3/25/2015.
 */
public class FriendAdapter extends ArrayAdapter<friendModel> implements Filterable {

    Context context;
    int layoutResourceId;
    ArrayList<friendModel> data = null;
    ArrayList<friendModel> originalData = null;
    private ItemFilter mFilter;

    public FriendAdapter(Context context, int resource, ArrayList<friendModel> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.originalData = new ArrayList<friendModel>();
        this.originalData.addAll(friendModel.getAll());
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

        friendModel itemnya = data.get(position);

        holder.txt_name.setText(itemnya.getFull_name());
        holder.txt_number.setText(itemnya.getMobile_number());

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(context);
        else
            mPic= Picasso.with(context);

        if(getPhotoUri(itemnya.getContact_id()) == null)
            mPic.load(R.drawable.user_unknown_menu)
                .fit()
                .centerCrop()
                .into(holder.qc_pic);
        else
            mPic.load(getPhotoUri(itemnya.getContact_id()))
                .error(R.drawable.user_unknown_menu)
                .placeholder(R.anim.progress_animation)
                .fit()
                .centerCrop()
                .into(holder.qc_pic);

        return row;
    }

    public Uri getPhotoUri(int id) {
        try {
            Cursor cur = this.context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
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
                ArrayList<friendModel> list = new ArrayList<friendModel>(originalData);
                results.values = list;
                results.count = list.size();
            }
            else {
                final ArrayList<friendModel> list = new ArrayList<friendModel>(originalData);
                final ArrayList<friendModel> nlist = new ArrayList<friendModel>();
                int count = list.size();

                for (int i = 0; i < count; i++) {
                    final friendModel friendModel = list.get(i);
                    final String filterableString = friendModel.getFull_name();
                    final String filterablePhone = friendModel.getMobile_number();

                    if (filterableString.toLowerCase().contains(filterString) || filterablePhone.contains(filterString)) {
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
            data = (ArrayList<friendModel>) results.values;

            clear();
            addAll(data);
            notifyDataSetChanged();
        }

    }

}

