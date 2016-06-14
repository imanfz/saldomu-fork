package com.sgo.orimakardaya.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.commentModel;
import com.sgo.orimakardaya.Beans.likeModel;
import com.sgo.orimakardaya.Beans.listTimeLineModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.adapter.TimelineCommentAdapter;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.squareup.picasso.Picasso;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/*
  Created by thinkpad on 4/17/2015.
 */
public class TimelineDetailActivity extends BaseActivity {

    SecurePreferences sp;
    int RESULT;
    String _ownerID,accessKey;

    RoundedQuickContactBadge iconPicture, iconPictureRight;
    TextView fromId,toId,messageTransaction,amount,dateTime;
    ImageView imageLove, imageSendComment;
    EditText etComment;
    ListView lvComment;
    TextView textLove, textStatus;
    boolean like = false;
    List<likeModel> listLike;

    List<commentModel> listComment;
    TimelineCommentAdapter commentAdapter;

    String post_id, from_name, from_id, to_name, to_id, message, datetime, amountvalue, profpic, ccy, tx_status, with_profpic, type_post;


    ProgressDialog mProg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        listLike = new ArrayList<likeModel>();
        listComment = new ArrayList<commentModel>();

        iconPicture = (RoundedQuickContactBadge) findViewById(R.id.icon_picture);
        iconPictureRight = (RoundedQuickContactBadge) findViewById(R.id.icon_picture_right);
        fromId = (TextView) findViewById(R.id.from_id);
        toId = (TextView)findViewById(R.id.to_id);
        messageTransaction = (TextView)findViewById(R.id.message_transaction);
        amount = (TextView)findViewById(R.id.amount);
        dateTime = (TextView)findViewById(R.id.datetime);
        imageLove = (ImageView)findViewById(R.id.image_love);
        imageSendComment = (ImageView)findViewById(R.id.image_comment);
        etComment = (EditText)findViewById(R.id.detail_value_comment);
        textLove = (TextView)findViewById(R.id.detail_value_love);
        lvComment = (ListView)findViewById(R.id.lvComment);
        textStatus = (TextView)findViewById(R.id.status);

        Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(this);
        else
            mPic= Picasso.with(this);

        Intent i = getIntent();
        if(i != null) {
            post_id = i.getStringExtra("post_id");
            from_name = i.getStringExtra("from_name");
            from_id = i.getStringExtra("from_id");
            to_name = i.getStringExtra("to_name");
            to_id = i.getStringExtra("to_id");
            message = i.getStringExtra("message");
            datetime = i.getStringExtra("datetime");
            amountvalue = i.getStringExtra("amount");
            profpic = i.getStringExtra("profpic");
            ccy = i.getStringExtra("ccy");
            tx_status = i.getStringExtra("tx_status");
            with_profpic = i.getStringExtra("with_profpic");
            type_post = i.getStringExtra("type_post");

            likeModel.deleteByPostId(post_id);

            if(type_post.equals("5") || type_post.equals("6") || type_post.equals("7")) {
                iconPictureRight.setVisibility(View.VISIBLE);
                if(with_profpic != null && with_profpic.equals(""))
                    mPic.load(R.drawable.user_unknown_menu)
                            .error(roundedImage)
                            .fit().centerInside()
                            .placeholder(R.anim.progress_animation)
                            .transform(new RoundImageTransformation())
                            .into(iconPictureRight);
                else
                    mPic.load(with_profpic)
                            .error(R.drawable.user_unknown_menu)
                            .placeholder(R.anim.progress_animation)
                            .fit()
                            .centerCrop()
                            .transform(new RoundImageTransformation())
                            .into(iconPictureRight);
                toId.setText(to_name);
                textStatus.setText(tx_status);
            }
            else {
                iconPictureRight.setVisibility(View.GONE);
                toId.setText(tx_status);
                textStatus.setText(getResources().getString(R.string.doing));
            }

            if(profpic != null && profpic.equals(""))
                mPic.load(R.drawable.user_unknown_menu)
                    .error(roundedImage)
                    .fit().centerInside()
                    .placeholder(R.anim.progress_animation)
                    .transform(new RoundImageTransformation())
                    .into(iconPicture);
            else
                mPic.load(profpic)
                    .error(R.drawable.user_unknown_menu)
                    .placeholder(R.anim.progress_animation)
                    .fit()
                    .centerCrop()
                    .transform(new RoundImageTransformation())
                    .into(iconPicture);

            PrettyTime p = new PrettyTime(new Locale(DefineValue.sDefSystemLanguage));
            Date time1 = DateTimeFormat.convertStringtoCustomDateTime(datetime);
            String period = p.formatDuration(time1);

//            SimpleDateFormat f = DateTimeFormat.getFormatYearHours();
//            Date d = null;
//            long long_date = 0;
//            try {
//                d = f.parse(datetime);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if (d != null) {
//                long_date = d.getTime();
//            }
//
//            String period = PeriodTime.getTimeAgo(long_date, getApplicationContext());

            fromId.setText(from_name);
            messageTransaction.setText(message);
            dateTime.setText(period);
            amount.setText(ccy + " " + amountvalue);
        } else {
            mPic.load(R.drawable.user_unknown_menu)
                .error(roundedImage)
                .fit().centerInside()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation())
                .into(iconPicture);
        }

        commentAdapter = new TimelineCommentAdapter(getApplicationContext(), listComment);
        lvComment.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lvComment.setAdapter(commentAdapter);

        lvComment.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int index = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(TimelineDetailActivity.this);
                builder.setTitle("Delete Comment");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String comment_id = Integer.toString(listComment.get(index).getComment_id());
                        String post_id = listComment.get(index).getPost_id();
                        String from = listComment.get(index).getFrom_id();
                        String to = listComment.get(index).getTo_id();
                        listComment.clear();
                        commentModel.deleteByPostId(post_id);
                        removeComment(comment_id, post_id, from, to);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDelete = builder.create();
                alertDelete.show();

                return false;
            }
        });

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    imageSendComment.setImageResource(R.drawable.ic_send_normal);
                } else {
                    imageSendComment.setImageResource(R.drawable.ic_send_clicked);
                }
            }
        });

        commentAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lvComment.setSelection(commentAdapter.getCount() - 1);
            }
        });

        imageLove.setOnClickListener(imageLikeListener);
        imageSendComment.setOnClickListener(imageCommentListener);

        listComment.addAll(commentModel.getByPostId(post_id));
        commentAdapter.notifyDataSetChanged();
        getLikeList();
        getCommentList();

        RESULT = MainPage.RESULT_NORMAL;
    }

    public void setImageLove() {
        if(like == false) {
            imageLove.setImageResource(R.drawable.ic_like_inactive);
        }
        else if(like == true){
            imageLove.setImageResource(R.drawable.ic_like_active);
        }
    }

    public void getCommentList() {
        try {

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_COMMENT_LIST,
                    _ownerID,accessKey);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get comment list:" + params.toString());

            MyApiClient.getCommentList(this,params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi params comment list:" + response.toString());

                            JSONArray mArrayComment = new JSONArray(response.getString(WebParams.DATA_COMMENTS));
                            List<commentModel> mListComment = new ArrayList<commentModel>();
                            for (int i = 0; i < mArrayComment.length(); i++) {
                                int comment_id = Integer.parseInt(mArrayComment.getJSONObject(i).getString(WebParams.ID));
                                boolean flagSameComment = false;

                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                if (listComment.size() > 0) {
                                    for (int index = 0; index < listComment.size(); index++) {
                                        if (listComment.get(index).getComment_id() != comment_id) {
                                            flagSameComment = false;
                                        } else {
                                            flagSameComment = true;
                                            break;
                                        }
                                    }
                                }

                                if(flagSameComment == false) {
                                    String comment_post_id = mArrayComment.getJSONObject(i).getString(WebParams.POST_ID);
                                    String comment_from = mArrayComment.getJSONObject(i).getString(WebParams.FROM);
                                    String comment_from_name = mArrayComment.getJSONObject(i).getString(WebParams.FROM_NAME);
                                    String comment_from_profile_picture = mArrayComment.getJSONObject(i).getString(WebParams.FROM_PROFILE_PICTURE);
                                    String comment_to = mArrayComment.getJSONObject(i).getString(WebParams.TO);
                                    String comment_to_name = mArrayComment.getJSONObject(i).getString(WebParams.TO_NAME);
                                    String comment_to_profile_picture = mArrayComment.getJSONObject(i).getString(WebParams.TO_PROFILE_PICTURE);
                                    String comment_reply = mArrayComment.getJSONObject(i).getString(WebParams.REPLY);
                                    String comment_datetime = mArrayComment.getJSONObject(i).getString(WebParams.DATETIME);

                                    mListComment.add(new commentModel(comment_id, comment_post_id,
                                            comment_from, comment_from_name, comment_from_profile_picture, comment_to,
                                            comment_to_name, comment_to_profile_picture, comment_reply, comment_datetime));
                                }
                            }
                            insertCommentToDB(mListComment, false, "");
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout", response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(TimelineDetailActivity.this, message);
                        } else {
                            Timber.d("isi error comment list:" + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TimelineDetailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi comment list timelinedetail:" + throwable.toString());
                }
            });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void addComment(String reply) {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");


            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_ADD_COMMENT,
                    _ownerID,accessKey);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, _ownerID);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.REPLY, reply);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.USER_ID,_ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params add comment:"+ params.toString());

            MyApiClient.sentAddComment(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        mProg.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi params add comment:" + response.toString());
                            String data_comments = response.getString(WebParams.DATA_COMMENTS);
                            JSONArray mArrayComment = new JSONArray(data_comments);
                            List<commentModel> mListComment = new ArrayList<commentModel>();
                            for (int i = 0; i < mArrayComment.length(); i++) {
                                int comment_id = Integer.parseInt(mArrayComment.getJSONObject(i).getString(WebParams.ID));
                                boolean flagSameComment = false;

                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                if (listComment.size() > 0) {
                                    for (int index = 0; index < listComment.size(); index++) {
                                        if (listComment.get(index).getComment_id() != comment_id) {
                                            flagSameComment = false;
                                        } else {
                                            flagSameComment = true;
                                            break;
                                        }
                                    }
                                }

                                if(flagSameComment == false) {
                                    String comment_post_id = mArrayComment.getJSONObject(i).getString(WebParams.POST_ID);
                                    String comment_from = mArrayComment.getJSONObject(i).getString(WebParams.FROM);
                                    String comment_from_name = mArrayComment.getJSONObject(i).getString(WebParams.FROM_NAME);
                                    String comment_from_profile_picture = mArrayComment.getJSONObject(i).getString(WebParams.FROM_PROFILE_PICTURE);
                                    String comment_to = mArrayComment.getJSONObject(i).getString(WebParams.TO);
                                    String comment_to_name = mArrayComment.getJSONObject(i).getString(WebParams.TO_NAME);
                                    String comment_to_profile_picture = mArrayComment.getJSONObject(i).getString(WebParams.TO_PROFILE_PICTURE);
                                    String comment_reply = mArrayComment.getJSONObject(i).getString(WebParams.REPLY);
                                    String comment_datetime = mArrayComment.getJSONObject(i).getString(WebParams.DATETIME);

                                    mListComment.add(new commentModel(comment_id, comment_post_id,
                                            comment_from, comment_from_name, comment_from_profile_picture, comment_to,
                                            comment_to_name, comment_to_profile_picture, comment_reply, comment_datetime));
                                }
                            }
                            insertCommentToDB(mListComment, true, data_comments);
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(TimelineDetailActivity.this, message);
                        } else {
                            Timber.d("isi error add comment:" + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TimelineDetailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(mProg.isShowing())
                        mProg.dismiss();
                    Timber.w("Error Koneksi add comment:" + throwable.toString());
                }
            });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void removeComment(String comment_id, final String post_id, String from, String to) {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REMOVE_COMMENT,
                    _ownerID,accessKey);
            params.put(WebParams.COMMENT_ID, comment_id);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.TO, to);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params remove comment:"+ params.toString());

            MyApiClient.sentRemoveComment(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        mProg.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi params add comment:" + response.toString());

                            String data_comments = response.getString(WebParams.DATA_COMMENTS);
                            JSONArray mArrayComment = new JSONArray(data_comments);
                            List<commentModel> mListComment = new ArrayList<commentModel>();
                            for (int i = 0; i < mArrayComment.length(); i++) {
                                int comment_id = Integer.parseInt(mArrayComment.getJSONObject(i).getString(WebParams.ID));
                                boolean flagSameComment = false;

                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                if (listComment.size() > 0) {
                                    for (int index = 0; index < listComment.size(); index++) {
                                        if (listComment.get(index).getComment_id() != comment_id) {
                                            flagSameComment = false;
                                        } else {
                                            flagSameComment = true;
                                            break;
                                        }
                                    }
                                }

                                if(flagSameComment == false) {
                                    String comment_post_id = mArrayComment.getJSONObject(i).getString(WebParams.POST_ID);
                                    String comment_from = mArrayComment.getJSONObject(i).getString(WebParams.FROM);
                                    String comment_from_name = mArrayComment.getJSONObject(i).getString(WebParams.FROM_NAME);
                                    String comment_from_profile_picture = mArrayComment.getJSONObject(i).getString(WebParams.FROM_PROFILE_PICTURE);
                                    String comment_to = mArrayComment.getJSONObject(i).getString(WebParams.TO);
                                    String comment_to_name = mArrayComment.getJSONObject(i).getString(WebParams.TO_NAME);
                                    String comment_to_profile_picture = mArrayComment.getJSONObject(i).getString(WebParams.TO_PROFILE_PICTURE);
                                    String comment_reply = mArrayComment.getJSONObject(i).getString(WebParams.REPLY);
                                    String comment_datetime = mArrayComment.getJSONObject(i).getString(WebParams.DATETIME);

                                    mListComment.add(new commentModel(comment_id, comment_post_id,
                                            comment_from, comment_from_name, comment_from_profile_picture, comment_to,
                                            comment_to_name, comment_to_profile_picture, comment_reply, comment_datetime));
                                }
                            }
                            insertCommentToDB(mListComment, true, data_comments);

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(TimelineDetailActivity.this, message);
                        } else if (code.equals(WebParams.NO_DATA_CODE)) {
                            Timber.d("isi error add comment:" + response.toString());
                            listComment.addAll(commentModel.getByPostId(post_id));
                            commentAdapter.notifyDataSetChanged();

                            listTimeLineModel.updateNumcomments(count, Integer.parseInt(post_id));
                            listTimeLineModel.updateComments("",Integer.parseInt(post_id));
                            listTimeLineModel.updateCommentId1("",Integer.parseInt(post_id));
                            listTimeLineModel.updateCommentId2("",Integer.parseInt(post_id));
                            listTimeLineModel.updateFromname1("",Integer.parseInt(post_id));
                            listTimeLineModel.updateFromname2("",Integer.parseInt(post_id));
                            listTimeLineModel.updateFromprofilepicture1("",Integer.parseInt(post_id));
                            listTimeLineModel.updateFromprofilepicture2("",Integer.parseInt(post_id));
                            listTimeLineModel.updateReply1("",Integer.parseInt(post_id));
                            listTimeLineModel.updateReply2("",Integer.parseInt(post_id));
                        } else {
                            Timber.d("isi error add comment:" + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TimelineDetailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(mProg.isShowing())
                        mProg.dismiss();
                    Timber.w("Error Koneksi remove comment timeline detail:" + throwable.toString());
                }
            });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void getLikeList() {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_LIKE_LIST,
                    _ownerID,accessKey);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get like list:"+ params.toString());

            MyApiClient.getLikeList(this,params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        mProg.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi response like list:" + response.toString());

                            JSONArray mArrayLike = new JSONArray(response.getString(WebParams.DATA_LIKES));
                            List<likeModel> mListLike = new ArrayList<likeModel>();
                            for (int i = 0; i < mArrayLike.length(); i++) {
                                int like_id = Integer.parseInt(mArrayLike.getJSONObject(i).getString(WebParams.ID));
//                                boolean flagSameLike = false;
//
//                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
//                                if (listLike.size() > 0) {
//                                    for (int index = 0; index < listLike.size(); index++) {
//                                        if (listLike.get(index).getLike_id() != like_id) {
//                                            flagSameLike = false;
//                                        } else {
//                                            flagSameLike = true;
//                                            break;
//                                        }
//                                    }
//                                }
//
//                                if(flagSameLike == false) {
                                String like_post_id = mArrayLike.getJSONObject(i).getString(WebParams.POST_ID);
                                String like_from = mArrayLike.getJSONObject(i).getString(WebParams.FROM);
                                String like_from_name = mArrayLike.getJSONObject(i).getString(WebParams.FROM_NAME);
                                String like_from_profile_picture = mArrayLike.getJSONObject(i).getString(WebParams.FROM_PROFILE_PICTURE);
                                String like_to = mArrayLike.getJSONObject(i).getString(WebParams.TO);
                                String like_to_name = mArrayLike.getJSONObject(i).getString(WebParams.TO_NAME);
                                String like_to_profile_picture = mArrayLike.getJSONObject(i).getString(WebParams.TO_PROFILE_PICTURE);
                                String like_datetime = mArrayLike.getJSONObject(i).getString(WebParams.DATETIME);

                                if(like_from.equals(_ownerID)) like = true;

                                mListLike.add(new likeModel(like_id, like_post_id,
                                        like_from, like_from_name, like_from_profile_picture, like_to,
                                        like_to_name, like_to_profile_picture, like_datetime));
//                                }
                            }
                            setImageLove();
                            insertLikeToDB(mListLike);
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(TimelineDetailActivity.this, message);
                        } else if (code.equals(WebParams.NO_DATA_CODE)) {
                            textLove.setText("");
                            Timber.d("isi error like list:" + response.toString());
                        } else {
                            Timber.d("isi error like list:" + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TimelineDetailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(mProg.isShowing())
                        mProg.dismiss();
                    finish();
                    Timber.w("Error Koneksi like list timeline detail:" + throwable.toString());
                }
            });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void addLike() {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");
            like = true;

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_ADD_LIKE,
                    _ownerID,accessKey);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, _ownerID);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params add like:"+ params.toString());

            MyApiClient.sentAddLike(this,params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        mProg.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi response add like:" + response.toString());

                            String data_likes = response.getString(WebParams.DATA_LIKES);
                            JSONArray mArrayLike = new JSONArray(data_likes);
                            List<likeModel> mListLike = new ArrayList<likeModel>();
                            for (int i = 0; i < mArrayLike.length(); i++) {
                                int like_id = Integer.parseInt(mArrayLike.getJSONObject(i).getString(WebParams.ID));
//                                boolean flagSameLike = false;
//
//                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
//                                if (listLike.size() > 0) {
//                                    for (int index = 0; index < listLike.size(); index++) {
//                                        if (listLike.get(index).getLike_id() != like_id) {
//                                            flagSameLike = false;
//                                        } else {
//                                            flagSameLike = true;
//                                            break;
//                                        }
//                                    }
//                                }
//
//                                if(flagSameLike == false) {
                                String like_post_id = mArrayLike.getJSONObject(i).getString(WebParams.POST_ID);
                                String like_from = mArrayLike.getJSONObject(i).getString(WebParams.FROM);
                                String like_from_name = mArrayLike.getJSONObject(i).getString(WebParams.FROM_NAME);
                                String like_from_profile_picture = mArrayLike.getJSONObject(i).getString(WebParams.FROM_PROFILE_PICTURE);
                                String like_to = mArrayLike.getJSONObject(i).getString(WebParams.TO);
                                String like_to_name = mArrayLike.getJSONObject(i).getString(WebParams.TO_NAME);
                                String like_to_profile_picture = mArrayLike.getJSONObject(i).getString(WebParams.TO_PROFILE_PICTURE);
                                String like_datetime = mArrayLike.getJSONObject(i).getString(WebParams.DATETIME);

                                mListLike.add(new likeModel(like_id, like_post_id,
                                        like_from, like_from_name, like_from_profile_picture, like_to,
                                        like_to_name, like_to_profile_picture, like_datetime));
//                                }
                            }
                            listTimeLineModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            listTimeLineModel.updateNumlikes(count, Integer.parseInt(post_id));
                            listTimeLineModel.updateIsLike("1", Integer.parseInt(post_id));
                            insertLikeToDB(mListLike);
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(TimelineDetailActivity.this, message);
                        } else if (code.equals(WebParams.NO_DATA_CODE)) {
                            textLove.setText("");
                            List<likeModel> mListLike = new ArrayList<likeModel>();
                            insertLikeToDB(mListLike);
                            Timber.d("isi error add like:" + response.toString());
                        } else {
                            Timber.d("isi error add like:" + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TimelineDetailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(mProg.isShowing())
                        mProg.dismiss();
                    Timber.w("Error Koneksi add like timeline detail:" + throwable.toString());
                }
            });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void removeLike(String like_id, String from, String to) {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");
            like = false;

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REMOVE_LIKE,
                    _ownerID,accessKey);
            params.put(WebParams.LIKE_ID, like_id);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.TO, to);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params remove like:"+ params.toString());

            MyApiClient.sentRemoveLike(this,params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        mProg.dismiss();
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        Timber.d("isi response remove like:"+ response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi response remove like:"+ response.toString());

                            String data_likes = response.getString(WebParams.DATA_LIKES);
                            JSONArray mArrayLike = new JSONArray(data_likes);
                            List<likeModel> mListLike = new ArrayList<likeModel>();
                            for (int i = 0; i < mArrayLike.length(); i++) {
                                int like_id = Integer.parseInt(mArrayLike.getJSONObject(i).getString(WebParams.ID));
//                                boolean flagSameLike = false;
//
//                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
//                                if (listLike.size() > 0) {
//                                    for (int index = 0; index < listLike.size(); index++) {
//                                        if (listLike.get(index).getLike_id() != like_id) {
//                                            flagSameLike = false;
//                                        } else {
//                                            flagSameLike = true;
//                                            break;
//                                        }
//                                    }
//                                }
//
//                                if(flagSameLike == false) {
                                String like_post_id = mArrayLike.getJSONObject(i).getString(WebParams.POST_ID);
                                String like_from = mArrayLike.getJSONObject(i).getString(WebParams.FROM);
                                String like_from_name = mArrayLike.getJSONObject(i).getString(WebParams.FROM_NAME);
                                String like_from_profile_picture = mArrayLike.getJSONObject(i).getString(WebParams.FROM_PROFILE_PICTURE);
                                String like_to = mArrayLike.getJSONObject(i).getString(WebParams.TO);
                                String like_to_name = mArrayLike.getJSONObject(i).getString(WebParams.TO_NAME);
                                String like_to_profile_picture = mArrayLike.getJSONObject(i).getString(WebParams.TO_PROFILE_PICTURE);
                                String like_datetime = mArrayLike.getJSONObject(i).getString(WebParams.DATETIME);

                                mListLike.add(new likeModel(like_id, like_post_id,
                                        like_from, like_from_name, like_from_profile_picture, like_to,
                                        like_to_name, like_to_profile_picture, like_datetime));
//                                }
                            }
                            listTimeLineModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            listTimeLineModel.updateNumlikes(count, Integer.parseInt(post_id));
                            listTimeLineModel.updateIsLike("0", Integer.parseInt(post_id));
                            insertLikeToDB(mListLike);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+ response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(TimelineDetailActivity.this,message);
                        }
                        else if(code.equals(WebParams.NO_DATA_CODE)){
                            String data_likes = response.getString(WebParams.DATA_LIKES);
                            textLove.setText("");
                            listTimeLineModel.updateLikes(data_likes, Integer.parseInt(post_id));
                            listTimeLineModel.updateNumlikes(count, Integer.parseInt(post_id));
                            listTimeLineModel.updateIsLike("0", Integer.parseInt(post_id));
                            List<likeModel> mListLike = new ArrayList<likeModel>();
                            insertLikeToDB(mListLike);
                            Timber.d("isi error remove like:"+ response.toString());
                        } else {
                            Timber.d("isi error remove like:"+ response.toString());
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TimelineDetailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(mProg.isShowing())
                        mProg.dismiss();
                    Timber.w("Error Koneksi remove like timeline detail:"+ throwable.toString());
                }
            });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void insertLikeToDB(List<likeModel> mListLike){
        likeModel.deleteByPostId(post_id);
        ActiveAndroid.initialize(this);
        ActiveAndroid.beginTransaction();
        likeModel mTm;

        Timber.d("arraylike length:"+ String.valueOf(mListLike.size()));
        if(mListLike.size()>0){
            for (int i = 0; i < mListLike.size(); i++) {
                mTm = new likeModel();
                mTm = mListLike.get(i);
                mTm.save();
                Timber.d("idx array like:"+ String.valueOf(i));
            }
        }

        ActiveAndroid.setTransactionSuccessful();

        ActiveAndroid.endTransaction();

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listLike.clear();
                listLike.addAll(likeModel.getByPostId(post_id));
                setLove();
            }
        });
    }

    public void insertCommentToDB(List<commentModel> mListComment, final boolean addRemove, final String _data_comments){
        ActiveAndroid.initialize(this);
        ActiveAndroid.beginTransaction();
        commentModel mTm;

        Timber.d("arrayComment length:"+ String.valueOf(mListComment.size()));
        if(mListComment.size()>0){
            for (int i = 0; i < mListComment.size(); i++) {
                mTm = new commentModel();
                mTm = mListComment.get(i);
                mTm.save();
                Timber.d("idx array comment:"+ String.valueOf(i));
            }
        }

        ActiveAndroid.setTransactionSuccessful();

        ActiveAndroid.endTransaction();

//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        final Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listComment.clear();
                listComment.addAll(commentModel.getByPostId(post_id));
                commentAdapter.notifyDataSetChanged();
                if(addRemove) {
                    listTimeLineModel.updateNumcomments(Integer.toString(listComment.size()), Integer.parseInt(post_id));
                    listTimeLineModel.updateComments(_data_comments,Integer.parseInt(post_id));
                    if(listComment.size() < 2){
                        listTimeLineModel.updateCommentId1(Integer.toString(listComment.get(0).getComment_id()), Integer.parseInt(post_id));
                        listTimeLineModel.updateFromname1(listComment.get(0).getFrom_name(),Integer.parseInt(post_id));
                        listTimeLineModel.updateFromprofilepicture1(listComment.get(0).getFrom_profile_picture(), Integer.parseInt(post_id));
                        listTimeLineModel.updateReply1(listComment.get(0).getReply(),Integer.parseInt(post_id));
                        listTimeLineModel.updateCommentId2("", Integer.parseInt(post_id));
                        listTimeLineModel.updateFromname2("",Integer.parseInt(post_id));
                        listTimeLineModel.updateFromprofilepicture2("", Integer.parseInt(post_id));
                        listTimeLineModel.updateReply2("",Integer.parseInt(post_id));
                    }
                    else {

                        for (int index = 0; index < listComment.size(); index++) {
                            if (index == listComment.size()-2) {
                                listTimeLineModel.updateCommentId2(Integer.toString(listComment.get(index).getComment_id()), Integer.parseInt(post_id));
                                listTimeLineModel.updateFromname2(listComment.get(index).getFrom_name(),Integer.parseInt(post_id));
                                listTimeLineModel.updateFromprofilepicture2(listComment.get(index).getFrom_profile_picture(), Integer.parseInt(post_id));
                                listTimeLineModel.updateReply2(listComment.get(index).getReply(),Integer.parseInt(post_id));
                            }
                            if (index == listComment.size()-1) {
                                listTimeLineModel.updateCommentId1(Integer.toString(listComment.get(index).getComment_id()), Integer.parseInt(post_id));
                                listTimeLineModel.updateFromname1(listComment.get(index).getFrom_name(), Integer.parseInt(post_id));
                                listTimeLineModel.updateFromprofilepicture1(listComment.get(index).getFrom_profile_picture(), Integer.parseInt(post_id));
                                listTimeLineModel.updateReply1(listComment.get(index).getReply(), Integer.parseInt(post_id));
                            }
                        }
                    }
                }
            }
        });
    }

    ImageView.OnClickListener imageLikeListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(TimelineDetailActivity.this)) {
                if (like == false) {
                    like = true;
                } else if (like == true) {
                    like = false;
                }
//            String custName = sp.getString(CoreApp.CUST_NAME, getString(R.string.text_strip));
                if (like == false) {
                    imageLove.setImageResource(R.drawable.ic_like_inactive);

                    for (int i = 0; i < listLike.size(); i++) {
                        if (listLike.get(i).getFrom_id().equals(_ownerID)) {
                            String like_id = Integer.toString(listLike.get(i).getLike_id());
                            String from = _ownerID;
                            String to = listLike.get(i).getTo_id();
                            removeLike(like_id, from, to);
                        }
                    }

                } else if (like == true) {
                    imageLove.setImageResource(R.drawable.ic_like_active);
                    addLike();
                }
            }

        }
    };

    ImageView.OnClickListener imageCommentListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            String reply = etComment.getText().toString();
            if (!reply.equals("")) {
                if (InetHandler.isNetworkAvailable(TimelineDetailActivity.this)) {
                    addComment(reply);
                    etComment.setText("");
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TimelineDetailActivity.this);
                    builder.setTitle("Connection").setMessage(getResources().getString(R.string.alert_connection))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_timeline_detail;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_timeline_detail));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLove() {
        String peopleLove = "";
        for(int i = 0 ; i < listLike.size() ; i++) {
            if(i == listLike.size()-1) {
                peopleLove += listLike.get(i).getFrom_name() + " Like this.";
            }
            else {
                peopleLove += listLike.get(i).getFrom_name() + ", ";
            }
        }
        textLove.setText(peopleLove);
    }
}
