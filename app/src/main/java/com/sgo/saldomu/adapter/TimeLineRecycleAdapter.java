package com.sgo.saldomu.adapter;/*
  Created by Administrator on 11/24/2014.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listHistoryModel;
import com.sgo.saldomu.Beans.listTimeLineModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.LikesModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class TimeLineRecycleAdapter extends RecyclerView.Adapter<TimeLineRecycleAdapter.SimpleHolder>{

    private List<listTimeLineModel> mData;
    private int rowLayout;
    private Context mContext;
    private OnItemClickListener mItemClickListener;
    private SecurePreferences sp;
    private String user_id;
    private String accessKey;

    public TimeLineRecycleAdapter(List<listTimeLineModel> _mData, int _rowLayout, Context _mContext){
        mData = _mData;
        rowLayout = _rowLayout;
        mContext = _mContext;

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        user_id = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");


    }

    @Override
    public SimpleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout,viewGroup,false);
        return new SimpleHolder(v);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final SimpleHolder simpleHolder, int i) {
        final listTimeLineModel _data = mData.get(i);

        String string_date = _data.getDatetime();
        PrettyTime p = new PrettyTime(new Locale(DefineValue.sDefSystemLanguage));
        Date time1 = DateTimeFormat.convertStringtoCustomDateTime(string_date);
        String period = p.formatDuration(time1);

        simpleHolder.fromId.setText(_data.getOwner());
        simpleHolder.messageTransaction.setText(_data.getPost());
        simpleHolder.amount.setText(_data.getCcy_id() + " " + CurrencyFormat.format(_data.getAmount()));
        simpleHolder.dateTime.setText(period);

        setImageProfPic(_data.getOwner_profile_picture(), simpleHolder.iconPicture);

        if(_data.getTypepost().equals("5") || _data.getTypepost().equals("6") || _data.getTypepost().equals("7")) {
            simpleHolder.iconPictureRight.setVisibility(View.VISIBLE);
            setImageProfPic(_data.getWith_profile_picture(), simpleHolder.iconPictureRight);
            simpleHolder.toId.setText(_data.getWith());
            simpleHolder.status.setText(_data.getTypecaption());
        }
        else {
            simpleHolder.iconPictureRight.setVisibility(View.GONE);
            simpleHolder.toId.setText(_data.getTypecaption());
            simpleHolder.status.setText(mContext.getResources().getString(R.string.doing));

        }

        simpleHolder.likeCount.setText(_data.getNumlikes());
        simpleHolder.commentCount.setText(_data.getNumcomments());

        if(_data.getComment_id_2().equals("")) {
            simpleHolder.layoutComment1.setVisibility(View.GONE);
        }
        else {
            simpleHolder.layoutComment1.setVisibility(View.VISIBLE);
            simpleHolder.nameComment1.setText(_data.getFrom_name_2());
            simpleHolder.textComment1.setText(_data.getReply_2());
            setImageProfPic(_data.getFrom_profile_picture_2(), simpleHolder.iconPictureComment1);
        }

        if(_data.getComment_id_1().equals("")) {
            simpleHolder.layoutComment2.setVisibility(View.GONE);
        }
        else {
            simpleHolder.layoutComment2.setVisibility(View.VISIBLE);
            simpleHolder.nameComment2.setText(_data.getFrom_name_1());
            simpleHolder.textComment2.setText(_data.getReply_1());
            setImageProfPic(_data.getFrom_profile_picture_1(), simpleHolder.iconPictureComment2);
        }

        String comments = _data.getComments();
        if(comments.equals("")) {
            simpleHolder.layoutComment.setVisibility(View.GONE);
        }
        else simpleHolder.layoutComment.setVisibility(View.VISIBLE);

        final String isLike = _data.getIsLike();
        if(isLike.equals("0")) {
            simpleHolder.imageLove.setImageResource(R.drawable.ic_like_inactive);
        }
        else {
            simpleHolder.imageLove.setImageResource(R.drawable.ic_like_active);
        }

        final int timeline_id = _data.getTimeline_id();

        simpleHolder.imageLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(InetHandler.isNetworkAvailable(mContext)) {
                    v.setEnabled(false);
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setEnabled(true);
                        }
                    }, 3000);

                    if (isLike.equals("1")) {
                        final String jumlahLike = Integer.toString(Integer.parseInt(_data.getNumlikes()) - 1);
                        simpleHolder.imageLove.setImageResource(R.drawable.ic_like_inactive);
                        simpleHolder.likeCount.setText(jumlahLike);
                        listTimeLineModel.updateNumlikes(jumlahLike, timeline_id);
                        listTimeLineModel.updateIsLike("0", timeline_id);

                        final Handler mHandler = new Handler();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String likes = _data.getLikes();
                                try {
                                    JSONArray mArrayLikes = new JSONArray(likes);
                                    for (int i = 0; i < mArrayLikes.length(); i++) {
                                        String from = mArrayLikes.getJSONObject(i).getString(WebParams.FROM);
                                        if (user_id.equals(from)) {
                                            String like_id = mArrayLikes.getJSONObject(i).getString(WebParams.ID);
                                            String to = mArrayLikes.getJSONObject(i).getString(WebParams.TO);
                                            removeLike(like_id, user_id, to, Integer.toString(timeline_id), jumlahLike);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (isLike.equals("0")) {
                        final String jumlahLike = Integer.toString(Integer.parseInt(_data.getNumlikes()) + 1);
                        simpleHolder.imageLove.setImageResource(R.drawable.ic_like_active);
                        simpleHolder.likeCount.setText(jumlahLike);
                        listTimeLineModel.updateNumlikes(jumlahLike, timeline_id);
                        listTimeLineModel.updateIsLike("1", timeline_id);

                        final Handler mHandler = new Handler();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                addLike(Integer.toString(timeline_id), _data.getOwner_id(), jumlahLike);
                            }
                        });
                    }

                    swap(listTimeLineModel.getAll());
                }
            }
        });
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

    private void swap(List<listTimeLineModel> datas){
        mData.clear();
        mData.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class SimpleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout layoutComment, layoutComment1, layoutComment2;
        public TextView fromId,toId,messageTransaction,amount,dateTime, status, comment, nameComment1, textComment1, nameComment2, textComment2,
                        likeCount, commentCount;
        public ImageView imageLove;
        public QuickContactBadge iconPicture, iconPictureRight, iconPictureComment1, iconPictureComment2;

        public SimpleHolder(View itemView) {
            super(itemView);
            fromId = itemView.findViewById(R.id.from_id);
            toId = itemView.findViewById(R.id.to_id);
            messageTransaction = itemView.findViewById(R.id.message_transaction);
            amount = itemView.findViewById(R.id.amount);
            dateTime = itemView.findViewById(R.id.datetime);
            iconPicture = itemView.findViewById(R.id.icon_picture);
            iconPictureRight = itemView.findViewById(R.id.icon_picture_right);
            likeCount = itemView.findViewById(R.id.like_count);
            commentCount = itemView.findViewById(R.id.comment_count);
            iconPictureComment1 = itemView.findViewById(R.id.icon_picture_comment1);
            iconPictureComment2 = itemView.findViewById(R.id.icon_picture_comment2);
            layoutComment = itemView.findViewById(R.id.layout_comment);
            layoutComment1 = itemView.findViewById(R.id.layout_comment1);
            layoutComment2 = itemView.findViewById(R.id.layout_comment2);
            nameComment1 = itemView.findViewById(R.id.name_comment1);
            nameComment2 = itemView.findViewById(R.id.name_comment2);
            textComment1 = itemView.findViewById(R.id.text_comment1);
            textComment2 = itemView.findViewById(R.id.text_comment2);
            status = itemView.findViewById(R.id.status);
            imageLove = itemView.findViewById(R.id.image_love);
            comment = itemView.findViewById(R.id.value_comment);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    private void addLike(final String post_id, String from_id, final String jumlahLike) {
        try {
            String extraSignature = post_id + from_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_ADD_LIKE, extraSignature);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, user_id);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.USER_ID, user_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params add like:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_ADD_LIKE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            LikesModel model = gson.fromJson(object, LikesModel.class);

                            String code = model.getError_code();
                            String count = model.getCount();
                            String data_likes = gson.toJson(model.getData_likes());

                            if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {

                                listHistoryModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            } else if (code.equals(WebParams.NO_DATA_CODE)) {

                                listHistoryModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            } else {
                                Toast.makeText(mContext, model.getError_message(), Toast.LENGTH_SHORT).show();

                                listHistoryModel.updateNumlikes(Integer.toString(Integer.parseInt(jumlahLike) - 1), Integer.parseInt(post_id));
                                listHistoryModel.updateIsLike("0", Integer.parseInt(post_id));
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    } );
        }
        catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void removeLike(String like_id, String from, String to, final String post_id, final String jumlahLike) {
        try {

            String extraSignature = post_id + like_id + to;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REMOVE_LIKE, extraSignature);
            params.put(WebParams.LIKE_ID, like_id);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.TO, to);
            params.put(WebParams.USER_ID, user_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params remove like:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REMOVE_LIKE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            LikesModel model = gson.fromJson(object, LikesModel.class);

                            String code = model.getError_code();
                            String count = model.getCount();
                            String data_likes = gson.toJson(model.getData_likes());

                            if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {

                                listTimeLineModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            }
                            else if(code.equals(WebParams.NO_DATA_CODE)) {

                                listTimeLineModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            }
                            else {
                                Toast.makeText(mContext, model.getError_message(), Toast.LENGTH_SHORT).show();

                                listTimeLineModel.updateNumlikes(Integer.toString(Integer.parseInt(jumlahLike)+1), Integer.parseInt(post_id));
                                listTimeLineModel.updateIsLike("1", Integer.parseInt(post_id));
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    } );
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }
}
