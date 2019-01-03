package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 5/5/2017.
 */
public class GridHome extends BaseAdapter {

    private Context mContext;
    private final ArrayList<String> text;
    private final int[] icons;

    public GridHome(Context c, ArrayList<String> text, int[] icons ) {
        mContext = c;
        this.icons = icons;
        this.text = text;
    }

    @Override
    public int getCount() {
        return text.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_home, parent, false);
            holder.textView = convertView.findViewById(R.id.grid_text);
            holder.imageView = convertView.findViewById(R.id.grid_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(text.get(position));
        holder.imageView.setImageResource(icons[position]);
        return convertView;
    }


    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
