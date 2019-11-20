package com.sgo.saldomu.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Lenovo Thinkpad on 5/5/2017.
 */
public class GridHome extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> text = new ArrayList<>();
    private ArrayList<Drawable> drawable = new ArrayList<>();
    private int[] icons = new int[100];
    SecurePreferences sp;

    public GridHome(Context c, ArrayList<String> text, ArrayList<Drawable> drawable) {
        mContext = c;
        this.text = text;
        this.drawable = drawable;
        notifyDataSetChanged();
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
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
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
        holder.imageView.setImageDrawable(drawable.get(position));

        return convertView;
    }

    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

}
