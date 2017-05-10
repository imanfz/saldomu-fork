package com.sgo.hpku.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sgo.hpku.Beans.PromoObject;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.MyPicasso;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by thinkpad on 4/21/2015.
 */
public class PromoAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<PromoObject> promo;
    private Context context;

    public PromoAdapter(Context context, ArrayList<PromoObject> promo) {
        mInflater = LayoutInflater.from(context);
        this.promo = promo;
        this.context = context;
    }

    @Override
    public int getCount() {
        return promo.size();
    }

    @Override
    public Object getItem(int position) {
        return promo.get(position);
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
            view = mInflater.inflate(R.layout.list_promo_item, parent, false);
            holder = new ViewHolder();
            holder.imagePromo = (ImageView)view.findViewById(R.id.image_promo);
            holder.llPromoDesc = (LinearLayout)view.findViewById(R.id.llPromoDesc);
            holder.promoDesc = (TextView)view.findViewById(R.id.promo_desc);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        String pic = promo.get(position).getImage();
        String name = promo.get(position).getName();

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(context);
        else
            mPic= Picasso.with(context);

        if(!pic.equals("")){
            mPic.load(pic)
                .fit()
                .placeholder(R.drawable.progress_animation)
                .into(holder.imagePromo);

            if(name.equals("")) {
                holder.llPromoDesc.setVisibility(View.GONE);
            }
            else {
                holder.llPromoDesc.setVisibility(View.VISIBLE);
                holder.promoDesc.setText(name);
            }
        }

        return view;
    }

    private class ViewHolder {
        public ImageView imagePromo;
        public TextView promoDesc;
        public LinearLayout llPromoDesc;
    }
}
