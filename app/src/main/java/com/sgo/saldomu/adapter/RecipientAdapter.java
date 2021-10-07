package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sgo.saldomu.Beans.RecepientModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;

import java.util.List;

/**
 * Created by thinkpad on 3/15/2015.
 */
public class RecipientAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<RecepientModel> mObject;

    public RecipientAdapter(Context context, List<RecepientModel> _object) {
        mInflater = LayoutInflater.from(context);
        mObject = _object;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mObject.size();
    }

    @Override
    public Object getItem(int position) {
        return mObject.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.recipient_layout, parent, false);
            holder = new ViewHolder();
//            holder.avatar = (ImageView)view.findViewById(R.id.avatar);
            holder.backgroundLayout = (LinearLayout) view.findViewById(R.id.recipient_background);
            holder.name = (TextView)view.findViewById(R.id.name);
            holder.dest = (TextView)view.findViewById(R.id.dest);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(mObject.get(position).getStatus().equals(DefineValue.FAILED)){
            holder.backgroundLayout.setBackground( ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.rounded_background_red, null));
        }
        else if(mObject.get(position).getIs_member_temp().equals(DefineValue.STRING_YES)){
            holder.backgroundLayout.setBackground( ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.rounded_background_orange, null));
        }
        else {
            holder.backgroundLayout.setBackground( ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.rounded_background_green, null));
        }

        holder.name.setText(mObject.get(position).getName());
        holder.dest.setText(mObject.get(position).getNumber());

        return view;
    }

    private static class ViewHolder {
//        public ImageView avatar;
        public TextView name, dest;
        public LinearLayout backgroundLayout;
    }
}
