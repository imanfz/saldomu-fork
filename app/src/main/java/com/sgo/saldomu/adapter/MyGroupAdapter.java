package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.hb.views.PinnedSectionListView;
import com.sgo.saldomu.Beans.MyGroupObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.RoundedQuickContactBadge;

import java.util.ArrayList;

/**
 * Created by thinkpad on 4/16/2015.
 */
public class MyGroupAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter{
    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<MyGroupObject> groups;

    private static final int FIRST = 0; //Textview
    private static final int SECOND = 1; //Listview

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

//            Picasso mPic;
//            if(MyApiClient.PROD_FLAG_ADDRESS)
//                mPic = MyPicasso.getUnsafeImageLoader(context);
//            else
//                mPic= Picasso.with(context);

            if(myGroupObject.getMemberProfilePicture() != null && myGroupObject.getMemberProfilePicture().isEmpty())
                GlideManager.sharedInstance().initializeGlide(context, R.drawable.user_unknown_menu, null, holder.qc_pic);
            else
                GlideManager.sharedInstance().initializeGlide(context, myGroupObject.getMemberProfilePicture()
                        , ResourcesCompat.getDrawable(context.getResources(), R.drawable.user_unknown_menu, null), holder.qc_pic);
//                mPic.load(myGroupObject.getMemberProfilePicture())
//                    .error(R.drawable.user_unknown_menu)
//                    .placeholder(R.drawable.progress_animation)
//                    .fit()
//                    .centerCrop()
//                    .into(holder.qc_pic);
        }

        return view;
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
        return viewType == FIRST;
    }

    private class ViewHolder {
        public TextView txtSection, txtMemberName;
        public RoundedQuickContactBadge qc_pic;
    }
}

