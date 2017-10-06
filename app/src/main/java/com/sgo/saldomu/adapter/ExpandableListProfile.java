package com.sgo.saldomu.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sgo.saldomu.Beans.ListMyProfile_model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.interfaces.OnDateChooseListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Lenovo Thinkpad on 9/27/2017.
 */

public class ExpandableListProfile extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> mListDataHeader;
    private HashMap<String, List<ListMyProfile_model>> mListDataChild;
    onClick listener;
    OnDateChooseListener onDateChooseListener;

    public interface onClick{
        void onTextChange(String message, int choice);
    }

    public ExpandableListProfile(Context mContext, List<String> mListDataHeader,
                                 HashMap<String,List<ListMyProfile_model>> mListDataChild, onClick listener, OnDateChooseListener onDateChooseListener)
    {
        this.mContext = mContext;
        this.mListDataHeader = mListDataHeader;
        this.mListDataChild = mListDataChild;
        this.listener = listener;
        this.onDateChooseListener = onDateChooseListener;
    }

    @Override
    public int getGroupCount() {
        return mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mListDataChild.get(mListDataHeader.get(groupPosition)).size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
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
        groupHolder holder;
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (v == null){
            holder = new groupHolder();
            v = infalInflater.inflate(R.layout.list_profile_group, parent, false);
            holder.title = (TextView) v.findViewById(R.id.group_title);
            v.setTag(holder);
        }else {
            holder = (groupHolder) v.getTag();
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
        return 3;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final viewHolder Holder;
        if (v==null) {
            Holder = new viewHolder();
            switch (getChildType(groupPosition, childPosition)) {
                case 1:
                    v = infalInflater.inflate(R.layout.list_profile_child_item, parent, false);
                    Holder.noHP = (EditText) v.findViewById(R.id.myprofile_value_hp);
                    Holder.nama = (EditText) v.findViewById(R.id.myprofile_value_name);
                    Holder.email = (EditText) v.findViewById(R.id.myprofile_value_email);
                    Holder.dob = (TextView) v.findViewById(R.id.myprofile_value_dob);
                    Holder.button1 = (Button) v.findViewById(R.id.button1);
                    Holder.dob.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onDateChooseListener.DateChooseListener();
                        }
                    });
                    break;
                case 2:
                    v = infalInflater.inflate(R.layout.list_verified_member_item, parent, false);
                    Holder.cameraKTP = (ImageButton) v.findViewById(R.id.camera_ktp_paspor);
                    Holder.cameraSelfieKTP = (ImageButton) v.findViewById(R.id.camera_selfie_ktp_paspor);
                    Holder.cameraTTD = (ImageButton) v.findViewById(R.id.camera_ttd);
                    Holder.button2 = (Button) v.findViewById(R.id.button2);
                    break;
            }
            assert v != null;
            v.setTag(Holder);
        }else
            Holder = (viewHolder) v.getTag();

        switch (getChildType(groupPosition, childPosition)) {
            case 1:
                Holder.noHP.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length()!=0)
                        {
                            Holder.noHP.getText().toString();
                        }
                    }
                });

                Holder.nama.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                Holder.email.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

//                Holder.button1.setOnClickListener(button1Listener());
                break;
            case 2:
                break;
        }
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class groupHolder {
        TextView title;
    }
    class viewHolder {
        EditText noHP;
        EditText nama;
        EditText email;
        TextView dob;
        Button button1;
        ImageButton cameraKTP;
        ImageButton cameraSelfieKTP;
        ImageButton cameraTTD;
        Button button2;

    }

}
