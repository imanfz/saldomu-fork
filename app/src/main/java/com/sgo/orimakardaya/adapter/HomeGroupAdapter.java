package com.sgo.orimakardaya.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hb.views.PinnedSectionListView;
import com.sgo.orimakardaya.Beans.HomeGroupObject;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundImageTransformation;
import com.sgo.orimakardaya.coreclass.RoundedQuickContactBadge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by thinkpad on 4/13/2015.
 */
public class HomeGroupAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter{
    Context context;
    private LayoutInflater mInflater;
    private ArrayList<HomeGroupObject> groups;

    public static final int FIRST = 0; //Textview
    public static final int SECOND = 1; //Listview

    public HomeGroupAdapter(Context context, ArrayList<HomeGroupObject> groups) {
        this.mInflater = LayoutInflater.from(context);
        this.groups = groups;
        this.context = context;
    }
    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            int viewType = getItemViewType(position);
            if (viewType == FIRST) {
                view = mInflater.inflate(R.layout.home_group_section, parent, false);
                holder.txtSection = (TextView) view.findViewById(R.id.tvSection);
                view.setTag(holder);
            } else if (viewType == SECOND) {
                view = mInflater.inflate(R.layout.list_home_group_item, parent, false);
                holder.qc_pic = (RoundedQuickContactBadge) view.findViewById(R.id.contact_icon_home_group);
                holder.txtPay = (TextView) view.findViewById(R.id.txtListPay_home_group);
                holder.txtGetPaid = (TextView) view.findViewById(R.id.txtListGetPaid_home_group);
                holder.txtDesc = (TextView) view.findViewById(R.id.txtListDesc_home_group);
                holder.txtDate = (TextView) view.findViewById(R.id.txtListDate_home_group);
                view.setTag(holder);
            }
        }
        else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        if(groups.get(position).getType() == FIRST) {
            holder.txtSection.setText(groups.get(position).getGroupName());
        }
        else if(groups.get(position).getType() == SECOND){
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_unknown_menu);
            RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

            String profpic = groups.get(position).getProfpic();

            Picasso mPic;
            if(MyApiClient.PROD_FLAG_ADDRESS)
                mPic = MyPicasso.getImageLoader(context);
            else
                mPic= Picasso.with(context);

            if(profpic.equals("") || profpic.equals(null)){
                mPic.load(R.drawable.user_unknown_menu)
                    .error(roundedImage)
                    .fit().centerInside()
                    .placeholder(R.anim.progress_animation)
                    .transform(new RoundImageTransformation(context))
                    .into(holder.qc_pic);
            }
            else {
                mPic.load(profpic)
                    .error(roundedImage)
                    .fit()
                    .centerCrop()
                    .placeholder(R.anim.progress_animation)
                    .transform(new RoundImageTransformation(context))
                    .into(holder.qc_pic);
            }

            holder.txtPay.setText(groups.get(position).getPay());
            holder.txtGetPaid.setText(groups.get(position).getGetPaid());
            holder.txtDesc.setText(groups.get(position).getDesc());
            holder.txtDate.setText(groups.get(position).getDate());
        }

        return view;


    }

    @Override
    public int getItemViewType(int position) {
        if (groups.get(position).getType() == SECOND){
            return SECOND;
        }else{
            return FIRST;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        if(viewType == FIRST){
            return true;
        }else{
            return false;
        }
    }

    private class ViewHolder {
        public TextView txtSection, txtPay, txtGetPaid, txtDesc, txtDate;
        public RoundedQuickContactBadge qc_pic;
    }
}
