package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.sgo.saldomu.Beans.ListMyProfile_model;
import com.sgo.saldomu.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Lenovo Thinkpad on 9/27/2017.
 */

public class ExpandableListProfile extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> mListDataHeader;
    private HashMap<String, List<ListMyProfile_model>> mListDataChild;

    public ExpandableListProfile(Context mContext, List<String> mListDataHeader,
                                 HashMap<String,List<ListMyProfile_model>> mListDataChild)
    {
        this.mContext = mContext;
        this.mListDataHeader = mListDataHeader;
        this.mListDataChild = mListDataChild;
    }

    @Override
    public int getGroupCount() {
        return mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mListDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mListDataChild.get(mListDataHeader.get(groupPosition));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = convertView;
        childHolder holder;
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (v == null){
            holder = new childHolder();
            v = infalInflater.inflate(R.layout.list_profile_group, parent, false);
            holder.title = (TextView) v.findViewById(R.id.group_title);
            v.setTag(holder);
        }else {
            holder = (childHolder) v.getTag();
        }

        holder.title.setText(mListDataHeader.get(groupPosition));
        return v;
    }



    @Override
    public int getChildType(int groupPosition, int childPosition) {
        List<ListMyProfile_model> lists = (List<ListMyProfile_model>)  getChild(groupPosition, childPosition);
        ListMyProfile_model obj= lists.get(0);
        if (obj.getMemberBasic())
        {
            return 1;
        }
        else return 2;
    }

    @Override
    public int getChildTypeCount() {
        return 2;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewHolder Holder;
        if (v==null) {
            Holder = new viewHolder();
            switch (getChildType(groupPosition, childPosition)) {
                case 1:
                    v = infalInflater.inflate(R.layout.list_profile_child_item, parent, false);
                    break;
                case 2:
                    v = infalInflater.inflate(R.layout.list_verified_member_item, parent, false);
                    break;
            }
            assert v != null;
            v.setTag(Holder);
        }else
            Holder = (viewHolder) v.getTag();
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class viewHolder{
        TextView title;
    }
    class childHolder{
        TextView title;
    }
}
