package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.OpenHourDays;

import java.util.ArrayList;

/**
 * Created by Lenovo on 14/03/2017.
 */

public class GridViewAdapter extends BaseAdapter {
    ArrayList<OpenHourDays> setupOpenHours;
    LayoutInflater inflater;

    public GridViewAdapter(Context applicationContext, ArrayList<OpenHourDays> setupOpenHours) {
        this.setupOpenHours = setupOpenHours;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return this.setupOpenHours.size();
    }

    @Override
    public Object getItem(int i) {
        return this.setupOpenHours.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View rootView = view;
        ViewHolder holder;

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.adapter_gridview, null);
            holder = new GridViewAdapter.ViewHolder();
            holder.tvNamaHari = (TextView) rootView.findViewById(R.id.namaHari);
            holder.tvRowHour = (TextView) rootView.findViewById(R.id.rowHour);
            rootView.setTag(holder);
        } else {
            holder = (ViewHolder) rootView.getTag();
        }

        OpenHourDays openHourDay = (OpenHourDays) getItem(i);
        holder.tvNamaHari.setText(openHourDay.getNamaHari());

        String txtStartHour = openHourDay.getStartHour();
        String txtEndHour = openHourDay.getEndHour();

        if (!txtStartHour.isEmpty()) {
            holder.tvRowHour.setText(txtStartHour + " to " + txtEndHour);
        }

        return rootView;
    }

    class ViewHolder {
        TextView tvNamaHari, tvRowHour;
    }
}
