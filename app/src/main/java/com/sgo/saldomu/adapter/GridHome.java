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

    public interface myInterface
    {
        void click();
    }

    private Context mContext;
    private final ArrayList<String> text;
    private final int[] icons;
    TextView textView;
    ImageView imageView;

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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = inflater.inflate(R.layout.grid_home, null);
            textView = (TextView) grid.findViewById(R.id.grid_text);
            imageView = (ImageView)grid.findViewById(R.id.grid_image);

        } else {
            grid = (View) convertView;
        }
        textView.setText(text.get(position));
        imageView.setImageResource(icons[position]);
        return grid;
    }

}
