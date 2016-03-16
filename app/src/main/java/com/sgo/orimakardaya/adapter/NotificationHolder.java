package com.sgo.orimakardaya.adapter;/*
  Created by Administrator on 5/6/2015.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sgo.orimakardaya.R;

public class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView name,detail,time,dll, btnAccept, btnReject;
    public ImageView icon;
    private ClickListener clickListener;
    public LinearLayout layout_button_ask;

    public NotificationHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.txt_name);
        detail = (TextView)itemView.findViewById(R.id.txt_detail);
        time = (TextView)itemView.findViewById(R.id.txt_time);
        icon = (ImageView)itemView.findViewById(R.id.img_notif);
        dll = (TextView)itemView.findViewById(R.id.txt_dll);
        btnAccept = (TextView)itemView.findViewById(R.id.btn_accept);
        btnReject = (TextView)itemView.findViewById(R.id.btn_reject);
        layout_button_ask = (LinearLayout)itemView.findViewById(R.id.layout_button_ask);

        btnAccept.setOnClickListener(this);
    }

    public interface ClickListener {

        /**
         * Called when the view is clicked.
         *
         * v view that is clicked
         * position of the clicked item
         * isLongClick true if long click, false otherwise
         */
        void onClick(View v, boolean isLongClick);

    }

    /* Setter for listener. */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }



    @Override
    public void onClick(View v) {
        // If not long clicked, pass last variable as false.
        clickListener.onClick(v, false);
    }

    @Override
    public boolean onLongClick(View v) {
        // If long clicked, passed last variable as true.
        clickListener.onClick(v, true);
        return true;
    }
}
