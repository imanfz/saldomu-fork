package com.sgo.saldomu.adapter;/*
  Created by Administrator on 12/19/2014.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;
import com.sgo.saldomu.R;

import java.util.HashMap;
import java.util.List;

public class ExpandListATMAdapter extends BaseExpandableListAdapter {

  private Context _context;
  private List<String> _listDataHeader; // header titles
  // child data in format of header title, child title
  private HashMap<String, String> _listDataChild;

  public ExpandListATMAdapter(Context context, List<String> listDataHeader,
                               HashMap<String, String> listChildData) {
    this._context = context;
    this._listDataHeader = listDataHeader;
    this._listDataChild = listChildData;
  }

  @Override
  public int getGroupCount() {
    return this._listDataHeader.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return 1;
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this._listDataHeader.get(groupPosition);
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return this._listDataChild.get(this._listDataHeader.get(groupPosition));
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
    String headerTitle = (String) getGroup(groupPosition);
    if (convertView == null) {
      LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = infalInflater.inflate(R.layout.list_atm_group, parent,false);
    }

    TextView lblListHeader = (TextView) convertView.findViewById(R.id.text_list_atm_group);
    View topDivider = convertView.findViewById(R.id.list_atm_top_divider);
    IconTextView txtExpandIndicator = (IconTextView) convertView.findViewById(R.id.text_icon_expand_indicator);
    lblListHeader.setText(headerTitle);
    if (isExpanded) {
      topDivider.setVisibility(View.VISIBLE);
      txtExpandIndicator.setText(_context.getString(R.string.up_indicator));
    } else {
      topDivider.setVisibility(View.GONE);
      txtExpandIndicator.setText(_context.getString(R.string.down_indicator));
    }

    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

    String childText = (String) getChild(groupPosition, childPosition);
    if (convertView == null) {
      LayoutInflater infalInflater = (LayoutInflater) this._context
              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = infalInflater.inflate(R.layout.list_atm_item_expand, parent,false);
    }

    TextView txtListChild = (TextView) convertView.findViewById(R.id.listatm_item_account_value);
    txtListChild.setText(childText);
    return convertView;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return false;
  }
}
