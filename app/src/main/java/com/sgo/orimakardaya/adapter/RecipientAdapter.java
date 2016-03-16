package com.sgo.orimakardaya.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sgo.orimakardaya.Beans.RecepientModel;
import com.sgo.orimakardaya.R;

import java.util.List;

/**
 * Created by thinkpad on 3/15/2015.
 */
public class RecipientAdapter extends BaseAdapter {

    private static final String failed = "F";

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
        if(mObject.get(position).getStatus().equals(failed)){
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                holder.backgroundLayout.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_background_red));
            } else {
                holder.backgroundLayout.setBackground( mContext.getResources().getDrawable(R.drawable.rounded_background_red));
            }
        }
        else {
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                holder.backgroundLayout.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_background_green) );
            } else {
                holder.backgroundLayout.setBackground( mContext.getResources().getDrawable(R.drawable.rounded_background_green));
            }
        }

        holder.name.setText(mObject.get(position).getName());
        holder.dest.setText(mObject.get(position).getNumber());

        return view;
    }

    private class ViewHolder {
//        public ImageView avatar;
        public TextView name, dest;
        public LinearLayout backgroundLayout;
    }
}
