package com.sgo.orimakardaya.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hb.views.PinnedSectionListView;
import com.sgo.orimakardaya.Beans.MyGroupObject;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundImageTransformation;
import com.sgo.orimakardaya.coreclass.RoundedQuickContactBadge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by thinkpad on 4/16/2015.
 */
public class MyGroupAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter{
    Context context;
    private LayoutInflater mInflater;
    private ArrayList<MyGroupObject> groups;

    public static final int FIRST = 0; //Textview
    public static final int SECOND = 1; //Listview

    public MyGroupAdapter(Context context, ArrayList<MyGroupObject> groups) {
        this.mInflater = LayoutInflater.from(context);
        this.groups = groups;
        this.context = context;
    }
    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            int viewType = getItemViewType(position);
            if (viewType == FIRST) {
                view = mInflater.inflate(R.layout.mygroup_section, parent, false);
                holder.txtSection = (TextView) view.findViewById(R.id.tvMygroupSection);
                view.setTag(holder);
            } else if (viewType == SECOND) {
                view = mInflater.inflate(R.layout.list_mygroup_item, parent, false);
                holder.qc_pic = (RoundedQuickContactBadge) view.findViewById(R.id.mygroup_contact_icon);
                holder.txtMemberName = (TextView) view.findViewById(R.id.mygroup_member_name);
                view.setTag(holder);
            }
        }
        else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        if(groups.get(position).getType() == FIRST) {
            holder.txtSection.setText(groups.get(position).getGroupName());
        }
        else if(groups.get(position).getType() == SECOND){
            MyGroupObject myGroupObject = groups.get(position);

            holder.txtMemberName.setText(groups.get(position).getMemberName());

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_unknown_menu);
            RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

            Picasso mPic;
            if(MyApiClient.PROD_FLAG_ADDRESS)
                mPic = MyPicasso.getImageLoader(context);
            else
                mPic= Picasso.with(context);

            if(myGroupObject.getMemberProfilePicture() != null && myGroupObject.getMemberProfilePicture().isEmpty())
                mPic.load(R.drawable.user_unknown_menu)
                    .fit()
                    .centerCrop()
                    .into(holder.qc_pic);
            else
                mPic.load(myGroupObject.getMemberProfilePicture())
                    .error(R.drawable.user_unknown_menu)
                    .placeholder(R.anim.progress_animation)
                    .fit()
                    .centerCrop()
                    .into(holder.qc_pic);
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (groups.get(position).getType() == SECOND){
            return SECOND;
        }else{
            return FIRST;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        if(viewType == FIRST){
            return true;
        }else{
            return false;
        }
    }

    private class ViewHolder {
        public TextView txtSection, txtMemberName;
        public RoundedQuickContactBadge qc_pic;
    }
}

