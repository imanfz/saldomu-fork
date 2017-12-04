package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;

import com.sgo.saldomu.Beans.NotificationModelClass;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.NotificationActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.RejectNotifDialog;
import com.sgo.saldomu.fragments.FragNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
  Created by thinkpad on 3/19/2015.
 */
public class NotificationListAdapter extends RecyclerView.Adapter<NotificationHolder> implements RejectNotifDialog.OnItemSelectedListener{

    private final int VIEW_TYPE_READED = 10;
    private final int VIEW_TYPE_UNREAD = 20;

    private ArrayList<NotificationModelClass> mData;
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClick;
    private Context mContext;
    private boolean rejectSuccess;
    private FragNotification fragment;

    @Override
    public void onItemSelected(boolean success) {
        rejectSuccess = success;
        fragment.refreshAdapter();
    }

    public interface OnItemClickListener {
        void onItemClickView(View view, int position, Boolean isLongClick);
        void onItemBtnAccept(View view, int position, Boolean isLongClick);
        void onItemBtnClaim(View view, int position, Boolean isLongClick);
    }

    public NotificationListAdapter(FragNotification _fragment, Context context, ArrayList<NotificationModelClass> _data, OnItemClickListener _onItemClick) {
        mInflater = LayoutInflater.from(context);
        mData = _data;
        mContext = context;
        mOnItemClick = _onItemClick;
        fragment = _fragment;
    }

    @Override
    public NotificationHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View row;
        switch (viewtype){
            case VIEW_TYPE_UNREAD : row = mInflater.inflate(R.layout.list_notification_item, viewGroup, false);
                                    break;
            case VIEW_TYPE_READED : row = mInflater.inflate(R.layout.list_notification_item_readed, viewGroup, false);
                                    break;
            default: row = mInflater.inflate(R.layout.list_notification_item, viewGroup, false);
        }
        return new NotificationHolder(row);
    }

    @Override
    public int getItemViewType(int position) {
        NotificationModelClass mNotif = mData.get(position);
        if(mNotif.isRead())return VIEW_TYPE_READED;
        else return VIEW_TYPE_UNREAD;
    }



    @Override
    public void onBindViewHolder(final NotificationHolder simpleHolder, final int position) {

        simpleHolder.setClickListener(new NotificationHolder.ClickListener() {
            @Override
            public void onClickView(View v, boolean isLongClick) {
                mOnItemClick.onItemClickView(v,position,isLongClick);
            }

            @Override
            public void onClickBtnAccept(View v, boolean isLongClick) {
                mOnItemClick.onItemBtnAccept(v,position,isLongClick);
            }

            @Override
             public void onClickBtnClaim(View v, boolean isLongClick) {
                mOnItemClick.onItemBtnClaim(v,position,isLongClick);
            }
        });

        final NotificationModelClass mNotif = mData.get(position);
        int notif_type = mNotif.getNotif_type();
        if(notif_type == NotificationActivity.TYPE_TRANSFER) {
            simpleHolder.layout_button_ask.setVisibility(View.VISIBLE);
        }
        else {
            simpleHolder.layout_button_ask.setVisibility(View.GONE);
        }
        if(notif_type == NotificationActivity.TYPE_NON_MEMBER) {
            simpleHolder.layout_button_claim.setVisibility(View.VISIBLE);
        }
        else {
            simpleHolder.layout_button_claim.setVisibility(View.GONE);
        }
        simpleHolder.name.setText(mNotif.getTitle());
        simpleHolder.detail.setText(mNotif.getDetail());
        simpleHolder.time.setText(mNotif.getTime());

        if(notif_type == NotificationActivity.TYPE_COMMENT || notif_type == NotificationActivity.TYPE_LIKE) {
            simpleHolder.icon.setVisibility(View.GONE);
            simpleHolder.iconPicture.setVisibility(View.VISIBLE);
            setImageProfPic(mNotif.getFrom_profile_picture(), simpleHolder.iconPicture);
        }
        else {
            simpleHolder.iconPicture.setVisibility(View.GONE);
            simpleHolder.icon.setVisibility(View.VISIBLE);
            simpleHolder.icon.setImageResource(mNotif.getImage());
        }

        simpleHolder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleHolder.btnReject.setEnabled(false);
                simpleHolder.btnReject.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        simpleHolder.btnReject.setEnabled(true);
                    }
                }, 3000);

                String req_id = "", trx_id = "", from = "", amount = "", ccy_id = "";

                try {
                    JSONObject notif_detail = mNotif.getNotif_detail();
                    req_id = notif_detail.getString(WebParams.REQUEST_ID);
                    trx_id = notif_detail.getString(WebParams.TRX_ID);
                    from = notif_detail.getString(WebParams.FROM);
                    amount = notif_detail.getString(WebParams.AMOUNT);
                    ccy_id = notif_detail.getString(WebParams.CCY_ID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showDialog(req_id, trx_id, from, amount, ccy_id);
            }
        });

        if(mNotif.getNotif_type() == NotificationActivity.TYPE_TRANSFER && mNotif.getNotif_detail() != null){
            JSONObject mObj = mNotif.getNotif_detail();
            try {
                int idxStat = mObj.getInt(WebParams.STATUS);
                String trx_stat = mContext.getString(R.string.pending);
                switch (idxStat){
                    case NotificationActivity.P2PSTAT_PAID :
                        trx_stat = mContext.getString(R.string.paid);
                        break;
                    case NotificationActivity.P2PSTAT_FAILED :
                        trx_stat = mContext.getString(R.string.failed);
                        break;
                    case NotificationActivity.P2PSTAT_SUSPECT :
                        trx_stat = mContext.getString(R.string.suspect);
                        break;
                    case NotificationActivity.P2PSTAT_CANCELLED :
                        trx_stat = mContext.getString(R.string.cancelled);
                        break;
                }
                simpleHolder.dll.setText(String.valueOf(trx_stat));
                simpleHolder.dll.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            simpleHolder.dll.setVisibility(View.GONE);
        }
    }

    private void showDialog(String _req_id, String _trx_id, String _from, String _amount, String _ccy_id) {

        FragmentManager fm = ((Activity)mContext).getFragmentManager();
        RejectNotifDialog dialog_frag = new RejectNotifDialog();

        Bundle args = new Bundle();
        args.putString(DefineValue.REQUEST_ID, _req_id);
        args.putString(DefineValue.TRX_ID, _trx_id);
        args.putString(DefineValue.FROM, _from);
        args.putString(DefineValue.AMOUNT, _amount);
        args.putString(DefineValue.CCY_ID, _ccy_id);

        dialog_frag.setArguments(args);
        dialog_frag.setOnItemSelectedListener(this);
        dialog_frag.show(fm, RejectNotifDialog.TAG);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void setImageProfPic(String _data, QuickContactBadge _holder){
        /*
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

        if(density <= 1) _url_profpic = sp.getString(CoreApp.IMG_SMALL_URL, null);
        else if(density < 2) _url_profpic = sp.getString(CoreApp.IMG_MEDIUM_URL, null);
        else _url_profpic = sp.getString(CoreApp.IMG_LARGE_URL, null);

        Log.wtf("url prof pic", _url_profpic);

        */

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(mContext);
//        else
//            mPic= Picasso.with(mContext);

        if(_data.equals("") || _data.isEmpty()){
            GlideManager.sharedInstance().initializeGlide(mContext, R.drawable.user_unknown_menu, roundedImage, _holder);
        }
        else {
            GlideManager.sharedInstance().initializeGlide(mContext, _data, roundedImage, _holder);
        }
    }

}
