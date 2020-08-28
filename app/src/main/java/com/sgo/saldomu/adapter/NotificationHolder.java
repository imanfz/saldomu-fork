package com.sgo.saldomu.adapter;/*
  Created by Administrator on 5/6/2015.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import com.sgo.saldomu.R;

public class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView name;
    public TextView detail;
    public TextView time;
    public TextView dll;
    public TextView btnReject;
    public ImageView icon;
    private ClickListener clickListener;
    public LinearLayout layout_button_ask, layout_button_claim;
    public QuickContactBadge iconPicture;

    public NotificationHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.txt_name);
        detail = (TextView)itemView.findViewById(R.id.txt_detail);
        time = (TextView)itemView.findViewById(R.id.txt_time);
        icon = (ImageView)itemView.findViewById(R.id.img_notif);
        dll = (TextView)itemView.findViewById(R.id.txt_dll);
        iconPicture = (QuickContactBadge)itemView.findViewById(R.id.icon_picture);
        TextView btnAccept = (TextView) itemView.findViewById(R.id.btn_accept);
        btnReject = (TextView)itemView.findViewById(R.id.btn_reject);
        TextView btnClaim = (TextView) itemView.findViewById(R.id.btn_claim);
        layout_button_ask = (LinearLayout)itemView.findViewById(R.id.layout_button_ask);
        layout_button_claim = (LinearLayout)itemView.findViewById(R.id.layout_button_claim);

        btnClaim.setOnClickListener(this);
        btnAccept.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    public interface ClickListener {

        /**
         * Called when the view is clicked.
         *
         * v view that is clicked
         * position of the clicked item
         * isLongClick true if long click, false otherwise
         */
        void onClickView(View v, boolean isLongClick);
        void onClickBtnAccept(View v, boolean isLongClick);
        void onClickBtnClaim(View v, boolean isLongClick);
    }

    /* Setter for listener. */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }



    @Override
    public void onClick(View v) {
        // If not long clicked, pass last variable as false.
        if(v instanceof TextView) {
            clickListener.onClickBtnAccept(v, false);
            clickListener.onClickBtnClaim(v, false);
        }
        else
            clickListener.onClickView(v, false);
    }

    @Override
    public boolean onLongClick(View v) {
        // If long clicked, passed last variable as true.
        if(v instanceof TextView) {
            clickListener.onClickBtnAccept(v, true);
            clickListener.onClickBtnClaim(v, true);
        }
        else
            clickListener.onClickView(v, true);
        return true;
    }
}
