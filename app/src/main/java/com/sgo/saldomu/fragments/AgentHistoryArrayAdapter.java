package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentHistoryArrayAdapter extends ArrayAdapter<String>
{
    int layoutResourceId;
    Context context;
    String[] menuList;

    public AgentHistoryArrayAdapter(Context context, int layoutResourceId, String[] menuList)
    {
        super(context, layoutResourceId, menuList);
        this.layoutResourceId = layoutResourceId;
        this.context  = context;
        this.menuList = menuList;
    }

    static class ViewHolder
    {
        TextView agentName;
        TextView agentBooked;
        TextView agentAddress;
        ImageView agentProfilePic;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View rootView = convertView;

        if(rootView == null)
        {
            //proses pengambilan layout
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            rootView = inflater.inflate(layoutResourceId, parent, false);

            ViewHolder viewHolder = new ViewHolder();

            //pengambilan ID component dari layout
            viewHolder.agentName       = (TextView)rootView.findViewById(R.id.agentName);
            viewHolder.agentBooked     = (TextView)rootView.findViewById(R.id.agentBooked);
            viewHolder.agentAddress    = (TextView)rootView.findViewById(R.id.agentAddress);
            viewHolder.agentProfilePic = (ImageView)rootView.findViewById(R.id.agentProfilePic);

            //attach all data to view holder
            viewHolder.agentName.setText("Yuddistira Kiki");
            viewHolder.agentBooked.setText("07:30:00");
            viewHolder.agentAddress.setText("Jl. Kp. Sawah 26, Lengkong Kulon, Pagedangan, Tangerang");


            /*ImageLoader imageLoader = ImageLoader.getInstance();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
            ImageLoader.getInstance().init(config);
            imageLoader.displayImage("http://192.168.43.206/public/images/person.png", viewHolder.agentProfilePic);*/

            viewHolder.agentProfilePic.setImageResource(R.drawable.profile1);

            //apply semua modifikasi ke layout
            rootView.setTag(viewHolder);

        }

        return rootView;
    }
}
