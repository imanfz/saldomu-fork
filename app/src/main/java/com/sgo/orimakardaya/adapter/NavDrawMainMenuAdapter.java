package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 12/9/2014.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sgo.orimakardaya.Beans.navdrawmainmenuModel;
import com.sgo.orimakardaya.R;

import java.util.ArrayList;

public class NavDrawMainMenuAdapter extends ArrayAdapter<navdrawmainmenuModel> {

  private Context mContext;
  private int mSelectedItem;
  private ArrayList<navdrawmainmenuModel> mModelArrayList;

  public NavDrawMainMenuAdapter(Context context, ArrayList<navdrawmainmenuModel> _modelArrayList) {
    super(context,R.layout.nav_draw_main_menu_item,_modelArrayList);
    this.mContext = context;
    this.mModelArrayList = _modelArrayList;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {

    // 1. Create inflater
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    // 2. Get rowView from inflater
    View rowView;
    if(!mModelArrayList.get(position).isGroupHeader()){


      if (position == mSelectedItem) {
        rowView = inflater.inflate(R.layout.nav_draw_main_menu_selected_item, parent, false);
        TextView titleView = (TextView) rowView.findViewById(R.id.title_item_main_menu);
        titleView.setText(mModelArrayList.get(position).getTitle());
        titleView.setCompoundDrawablesWithIntrinsicBounds(mModelArrayList.get(position).getIndexImageSelected(),0,0,0);
      }
      else {
        rowView = inflater.inflate(R.layout.nav_draw_main_menu_item, parent, false);
        TextView titleView = (TextView) rowView.findViewById(R.id.title_item_main_menu);
        titleView.setText(mModelArrayList.get(position).getTitle());
        titleView.setCompoundDrawablesWithIntrinsicBounds(mModelArrayList.get(position).getIndexImage(),0,0,0);
      }
    }
    else{
      rowView = inflater.inflate(R.layout.nav_draw_main_menu_group, parent, false);
      TextView titleView = (TextView) rowView.findViewById(R.id.nav_draw_main_menu_header);
      titleView.setText(mModelArrayList.get(position).getTitle());

    }
    // 5. retrn rowView
    return rowView;
  }

  public int getSelectedItem() {
    return mSelectedItem;
  }

  public void setSelectedItem(int selectedItem) {
    if(!mModelArrayList.get(selectedItem).isGroupHeader())mSelectedItem = selectedItem;
  }
  public void setDefault() {
    mSelectedItem = 0;
  }
}
