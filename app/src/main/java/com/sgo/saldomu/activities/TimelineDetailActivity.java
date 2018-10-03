package com.sgo.saldomu.activities;

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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.commentModel;
import com.sgo.saldomu.Beans.likeModel;
import com.sgo.saldomu.Beans.listTimeLineModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.TimelineCommentAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.RoundedQuickContactBadge;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.CommentModel;
import com.sgo.saldomu.models.retrofit.LikesModel;
import com.sgo.saldomu.widgets.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/*
  Created by thinkpad on 4/17/2015.
 */
public class TimelineDetailActivity extends BaseActivity {

    private int RESULT;
    private String _ownerID;
    private String accessKey;

    private ImageView imageLove;
    private ImageView imageSendComment;
    private EditText etComment;
    private ListView lvComment;
    private TextView textLove;
    private boolean like = false;
    private List<likeModel> listLike;

    private List<commentModel> listComment;
    private TimelineCommentAdapter commentAdapter;

    private String post_id;
    private String from_id;

    private ProgressDialog mProg;
    private Gson gson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        listLike = new ArrayList<>();
        listComment = new ArrayList<>();

        gson = new Gson();

        RoundedQuickContactBadge iconPicture = findViewById(R.id.icon_picture);
        RoundedQuickContactBadge iconPictureRight = findViewById(R.id.icon_picture_right);
        TextView fromId = findViewById(R.id.from_id);
        TextView toId = findViewById(R.id.to_id);
        TextView messageTransaction = findViewById(R.id.message_transaction);
        TextView amount = findViewById(R.id.amount);
        TextView dateTime = findViewById(R.id.datetime);
        imageLove = findViewById(R.id.image_love);
        imageSendComment = findViewById(R.id.image_comment);
        etComment = findViewById(R.id.detail_value_comment);
        textLove = findViewById(R.id.detail_value_love);
        lvComment = findViewById(R.id.lvComment);
        TextView textStatus = findViewById(R.id.status);

        Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(this);
//        else
//            mPic= Picasso.with(this);

        Intent i = getIntent();
        if(i != null) {
            post_id = i.getStringExtra("post_id");
            String from_name = i.getStringExtra("from_name");
            from_id = i.getStringExtra("from_id");
            String to_name = i.getStringExtra("to_name");
            String to_id = i.getStringExtra("to_id");
            String message = i.getStringExtra("message");
            String datetime = i.getStringExtra("datetime");
            String amountvalue = i.getStringExtra("amount");
            String profpic = i.getStringExtra("profpic");
            String ccy = i.getStringExtra("ccy");
            String tx_status = i.getStringExtra("tx_status");
            String with_profpic = i.getStringExtra("with_profpic");
            String type_post = i.getStringExtra("type_post");

            likeModel.deleteByPostId(post_id);

            if(type_post.equals("5") || type_post.equals("6") || type_post.equals("7")) {
                iconPictureRight.setVisibility(View.VISIBLE);
                if(with_profpic != null && with_profpic.equals(""))
                    GlideManager.sharedInstance().initializeGlide(this, R.drawable.user_unknown_menu, roundedImage, iconPictureRight);
                else
                    GlideManager.sharedInstance().initializeGlide(this, with_profpic, roundedImage, iconPictureRight);

                toId.setText(to_name);
                textStatus.setText(tx_status);
            }
            else {
                iconPictureRight.setVisibility(View.GONE);
                toId.setText(tx_status);
                textStatus.setText(getResources().getString(R.string.doing));
            }

            if(profpic != null && profpic.equals(""))
                GlideManager.sharedInstance().initializeGlide(this, R.drawable.user_unknown_menu, roundedImage, iconPicture);
            else GlideManager.sharedInstance().initializeGlide(this, profpic, getResources().getDrawable(R.drawable.user_unknown_menu), iconPicture);

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
            GlideManager.sharedInstance().initializeGlide(this, R.drawable.user_unknown_menu, roundedImage, iconPicture);
        }

        commentAdapter = new TimelineCommentAdapter(getApplicationContext(), listComment);
        lvComment.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lvComment.setAdapter(commentAdapter);

        lvComment.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int index = position;
                if(listComment.get(position).getFrom_id().equalsIgnoreCase(_ownerID)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TimelineDetailActivity.this);
                    builder.setTitle(getString(R.string.delete_comment));
                    builder.setMessage(getString(R.string.delete_comment_ask));
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDelete = builder.create();
                    alertDelete.show();
                }

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

    private void setImageLove() {
        if(!like) {
            imageLove.setImageResource(R.drawable.ic_like_inactive);
        }
        else {
            imageLove.setImageResource(R.drawable.ic_like_active);
        }
    }

    private void getCommentList() {
        try {

            extraSignature = post_id + from_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_COMMENT_LIST, extraSignature);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get comment list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_COMMENT_LIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String count = response.getString(WebParams.COUNT);

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                                    Timber.d("isi params comment list:" + response.toString());

                                    JSONArray mArrayComment = new JSONArray(response.getString(WebParams.DATA_COMMENTS));
                                    List<commentModel> mListComment = new ArrayList<>();
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

                                        if(!flagSameComment) {
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
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void addComment(String reply) {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");

            extraSignature = post_id + from_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_ADD_COMMENT, extraSignature);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, _ownerID);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.REPLY, reply);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.USER_ID,_ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params add comment:"+ params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_ADD_COMMENT, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {

                                CommentModel model = gson.fromJson(object, CommentModel.class);

                                String code = model.getError_code();
                                String count = model.getCount();

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                                    String data_comments = gson.toJson(model.getData_comments());
                                    JSONArray mArrayComment = new JSONArray(data_comments);
                                    List<commentModel> mListComment = new ArrayList<>();
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

                                        if(!flagSameComment) {
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
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(TimelineDetailActivity.this, message);
                                } else {
                                    if (MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(TimelineDetailActivity.this, model.getError_message(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void removeComment(String comment_id, final String post_id, String from, String to) {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");

            extraSignature = post_id + from_id + comment_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REMOVE_COMMENT, extraSignature);
            params.put(WebParams.COMMENT_ID, comment_id);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.TO, to);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params remove comment:"+ params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REMOVE_COMMENT, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                CommentModel model = gson.fromJson(object, CommentModel.class);

                                String code = model.getError_code();
                                String count = model.getCount();

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                                    String data_comments = gson.toJson(model.getData_comments());
                                    JSONArray mArrayComment = new JSONArray(data_comments);
                                    List<commentModel> mListComment = new ArrayList<>();
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

                                        if(!flagSameComment) {
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
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(TimelineDetailActivity.this, message);
                                } else if (code.equals(WebParams.NO_DATA_CODE)) {
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
                                    if (MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(TimelineDetailActivity.this, model.getError_message(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void getLikeList() {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");

            extraSignature = post_id + from_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIKE_LIST, extraSignature);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get like list:"+ params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LIKE_LIST, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {

                                LikesModel model = gson.fromJson(object, LikesModel.class);

                                String code = model.getError_code();
                                String count = model.getCount();

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {

                                    JSONArray mArrayLike = new JSONArray(gson.toJson(model.getData_likes()));
                                    List<likeModel> mListLike = new ArrayList<>();
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
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(TimelineDetailActivity.this, message);
                                } else if (code.equals(WebParams.NO_DATA_CODE)) {
                                    textLove.setText("");
                                } else {
                                    if (MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(TimelineDetailActivity.this, model.getError_message(), Toast.LENGTH_SHORT).show();


                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void addLike() {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");
            like = true;

            extraSignature = post_id + from_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_ADD_LIKE, extraSignature);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, _ownerID);
            params.put(WebParams.TO, from_id);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params add like:"+ params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_ADD_LIKE, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                LikesModel model = gson.fromJson(object, LikesModel.class);

                                String code = model.getError_code();
                                String count = model.getCount();

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {

                                    String data_likes = gson.toJson(model.getData_likes());
                                    JSONArray mArrayLike = new JSONArray(data_likes);
                                    List<likeModel> mListLike = new ArrayList<>();
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
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(TimelineDetailActivity.this, message);
                                } else if (code.equals(WebParams.NO_DATA_CODE)) {
                                    textLove.setText("");
                                    List<likeModel> mListLike = new ArrayList<>();
                                    insertLikeToDB(mListLike);
                                } else {
                                    if (MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(TimelineDetailActivity.this, model.getError_message(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void removeLike(String like_id, String from, String to) {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");
            like = false;

            extraSignature = post_id + like_id + to;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REMOVE_LIKE, extraSignature);
            params.put(WebParams.LIKE_ID, like_id);
            params.put(WebParams.POST_ID, post_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.TO, to);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params remove like:"+ params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REMOVE_LIKE, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                LikesModel model = gson.fromJson(object, LikesModel.class);

                                String code = model.getError_code();
                                String count = model.getCount();

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {

                                    String data_likes = gson.toJson(model.getData_likes());
                                    JSONArray mArrayLike = new JSONArray(data_likes);
                                    List<likeModel> mListLike = new ArrayList<>();
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
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(TimelineDetailActivity.this,message);
                                }
                                else if(code.equals(WebParams.NO_DATA_CODE)){
                                    textLove.setText("");
                                    String data_likes = gson.toJson(model.getData_likes());
                                    listTimeLineModel.updateLikes(data_likes, Integer.parseInt(post_id));
                                    listTimeLineModel.updateNumlikes(count, Integer.parseInt(post_id));
                                    listTimeLineModel.updateIsLike("0", Integer.parseInt(post_id));
                                    List<likeModel> mListLike = new ArrayList<>();
                                    insertLikeToDB(mListLike);
                                } else {
                                    if(MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(TimelineDetailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(TimelineDetailActivity.this, model.getError_message(), Toast.LENGTH_SHORT).show();


                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        }
        catch(Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void insertLikeToDB(List<likeModel> mListLike){
        likeModel.deleteByPostId(post_id);
        ActiveAndroid.initialize(this);
        ActiveAndroid.beginTransaction();
        likeModel mTm;

        Timber.d("arraylike length:"+ String.valueOf(mListLike.size()));
        if(mListLike.size()>0){
            for (int i = 0; i < mListLike.size(); i++) {
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

    private void insertCommentToDB(List<commentModel> mListComment, final boolean addRemove, final String _data_comments){
        ActiveAndroid.initialize(this);
        ActiveAndroid.beginTransaction();
        commentModel mTm;

        Timber.d("arrayComment length:"+ String.valueOf(mListComment.size()));
        if(mListComment.size()>0){
            for (int i = 0; i < mListComment.size(); i++) {
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

    private ImageView.OnClickListener imageLikeListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(TimelineDetailActivity.this)) {
                like = !like;
//            String custName = sp.getString(CoreApp.CUST_NAME, getString(R.string.text_strip));
                if (!like) {
                    imageLove.setImageResource(R.drawable.ic_like_inactive);

                    for (int i = 0; i < listLike.size(); i++) {
                        if (listLike.get(i).getFrom_id().equals(_ownerID)) {
                            String like_id = Integer.toString(listLike.get(i).getLike_id());
                            String from = _ownerID;
                            String to = listLike.get(i).getTo_id();
                            removeLike(like_id, from, to);
                        }
                    }

                } else {
                    imageLove.setImageResource(R.drawable.ic_like_active);
                    addLike();
                }
            }

        }
    };

    private ImageView.OnClickListener imageCommentListener = new ImageView.OnClickListener() {
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

    private void InitializeToolbar(){
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

    private void setLove() {
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
