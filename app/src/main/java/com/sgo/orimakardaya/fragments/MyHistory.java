package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.listHistoryModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.HistoryDetailActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.HistoryRecycleAdapter;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/*
  Created by Administrator on 12/2/2014.
 */
public class MyHistory extends BaseFragmentMainPage {

    SecurePreferences sp;

    private LinearLayout layout_alert, layout_list;
    private Button btnRefresh;
    private TextView txtAlert;
    private ImageView imgAlert;

    private RecyclerView mRecyclerView;
    private HistoryRecycleAdapter mAdapter;
    private LinearLayoutManager currentLayoutManag;
    private String _ownerID,accessKey;
    private String page = "0";
    private String privacy = "1";
    private String isTimelineNew;

    private List<listHistoryModel> listHistory;
    int start = 0;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActiveAndroid.initialize(getActivity());

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        isTimelineNew = sp.getString(DefineValue.TIMELINE_FIRST_TIME,"");

        layout_alert = (LinearLayout) mView.findViewById(R.id.layout_alert_history);
        layout_list = (LinearLayout) mView.findViewById(R.id.layout_list_history);
        btnRefresh = (Button) mView.findViewById(R.id.btnRefresh);
        txtAlert = (TextView) mView.findViewById(R.id.txt_alert);
        imgAlert = (ImageView) mView.findViewById(R.id.img_alert);

        listHistory = new ArrayList<>();

        mRecyclerView = (RecyclerView)mView.findViewById(R.id.myhistory_recycle_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setCurrentLayoutManag(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new HistoryRecycleAdapter(listHistory, R.layout.list_recycle_timeline_item, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(new HistoryRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(getActivity(), HistoryDetailActivity.class);
                i.putExtra("post_id", Integer.toString(listHistory.get(position).getHistory_id()));
                i.putExtra("from_name", listHistory.get(position).getOwner());
                i.putExtra("from_id", listHistory.get(position).getOwner_id());
                i.putExtra("to_name", listHistory.get(position).getWith());
                i.putExtra("to_id", listHistory.get(position).getWith_id());
                i.putExtra("message", listHistory.get(position).getPost());
                i.putExtra("datetime", listHistory.get(position).getDatetime());
                i.putExtra("ccy", listHistory.get(position).getCcy_id());
                i.putExtra("amount", listHistory.get(position).getAmount());
                i.putExtra("profpic", listHistory.get(position).getOwner_profile_picture());
                i.putExtra("tx_status", listHistory.get(position).getTypecaption());
                i.putExtra("with_profpic", listHistory.get(position).getWith_profile_picture());
                switchActivity(i);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = "0";
                getHistoryList();
            }
        });

        if(isTimelineNew.equals(DefineValue.NO)){
            initializeDataPost();
        }
        getHistoryList();
    }

    public void initializeDataPost(){
        listHistory.addAll(listHistoryModel.getAll());
        if(listHistory.size() > 0) {
            layout_alert.setVisibility(View.GONE);
            layout_list.setVisibility(View.VISIBLE);
        }
        else {
            layout_alert.setVisibility(View.VISIBLE);
            layout_list.setVisibility(View.GONE);
            txtAlert.setText("Data not found");
            imgAlert.setImageResource(R.drawable.ic_data_not_found);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void getHistoryList() {
        try {

            RequestParams params =  MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_TIMELINE_LIST,
                    _ownerID,accessKey);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PRIVACY, privacy);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.PAGE, page);
            params.put(WebParams.COUNT, DefineValue.COUNT);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get history list", params.toString());

            MyApiClient.getTimelineList(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && count != "0") {
                            Timber.d("isi params history list", response.toString());
                            Timber.d("list listHistory", Integer.toString(listHistory.size()));

                            List<listHistoryModel> mListHistory = new ArrayList<listHistoryModel>();
                            JSONArray mArrayPost = new JSONArray(response.getString(WebParams.DATA_POSTS));
                            for (int i = 0; i < mArrayPost.length(); i++) {
                                int id = Integer.parseInt(mArrayPost.getJSONObject(i).getString(WebParams.ID));
                                boolean flagSame = false;

                                // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                if (listHistory.size() > 0) {
                                    for (int index = 0; index < listHistory.size(); index++) {
                                        if (listHistory.get(index).getHistory_id() != id) {
                                            flagSame = false;
                                        } else {
                                            flagSame = true;
                                            break;
                                        }
                                    }
                                }

                                if (flagSame == false) {
                                    String post = mArrayPost.getJSONObject(i).getString(WebParams.POST);
                                    String amount = mArrayPost.getJSONObject(i).getString(WebParams.AMOUNT);
                                    String balance = mArrayPost.getJSONObject(i).getString(WebParams.BALANCE);
                                    String ccy_id = mArrayPost.getJSONObject(i).getString(WebParams.CCY_ID);
                                    String datetime = mArrayPost.getJSONObject(i).getString(WebParams.DATETIME);
                                    String owner = mArrayPost.getJSONObject(i).getString(WebParams.OWNER);
                                    String owner_id = mArrayPost.getJSONObject(i).getString(WebParams.OWNER_ID);
                                    String owner_profile_picture = mArrayPost.getJSONObject(i).getString(WebParams.OWNER_PROFILE_PICTURE);
                                    String with_id = mArrayPost.getJSONObject(i).getString(WebParams.WITH_ID);
                                    String with = mArrayPost.getJSONObject(i).getString(WebParams.WITH);
                                    String with_profile_picture = mArrayPost.getJSONObject(i).getString(WebParams.WITH_PROFILE_PICTURE);
                                    String tx_status = mArrayPost.getJSONObject(i).getString(WebParams.TX_STATUS);
                                    String typepost = mArrayPost.getJSONObject(i).getString(WebParams.TYPEPOST);
                                    String typecaption = mArrayPost.getJSONObject(i).getString(WebParams.TYPECAPTION);
                                    String privacy = mArrayPost.getJSONObject(i).getString(WebParams.PRIVACY);
                                    String numcomments = mArrayPost.getJSONObject(i).getString(WebParams.NUMCOMMENTS);
                                    String numviews = mArrayPost.getJSONObject(i).getString(WebParams.NUMVIEWS);
                                    String numlikes = mArrayPost.getJSONObject(i).getString(WebParams.NUMLIKES);
                                    String share = mArrayPost.getJSONObject(i).getString(WebParams.SHARE);
                                    String comments = mArrayPost.getJSONObject(i).getString(WebParams.COMMENTS);
                                    String likes = mArrayPost.getJSONObject(i).getString(WebParams.LIKES);

                                    String isLike = "0";
                                    if(likes.equals("")){
                                        isLike = "0";
                                    }
                                    else {
                                        JSONArray mArrayLike = new JSONArray(likes);
                                        for(int index = 0; index < mArrayLike.length(); index++){
                                            String from = mArrayLike.getJSONObject(index).getString(WebParams.FROM);
                                            if(_ownerID.equals(from)) isLike = "1";
                                        }
                                    }

                                    if(comments.equals("")) {
                                        mListHistory.add(new listHistoryModel(id, post, amount, balance, ccy_id, datetime, owner, owner_id,
                                                owner_profile_picture, with_id, with, with_profile_picture, tx_status, typepost, typecaption,
                                                privacy, numcomments, numviews, numlikes, share, comments, likes,  "","","","","","","","",isLike));
                                    }
                                    else{
                                        JSONArray mArrayComment = new JSONArray(comments);
                                        int lengthComment = mArrayComment.length();
                                        String comment_id_1 = "", from_name_1 = "", from_profile_picture_1 = "", reply_1 = "",
                                                comment_id_2 = "", from_name_2 = "", from_profile_picture_2 = "", reply_2 = "";
                                        if(lengthComment == 1) {
                                            for (int index = 0; index < mArrayComment.length(); index++) {
                                                comment_id_1 = mArrayComment.getJSONObject(index).getString(WebParams.COMMENT_ID);
                                                from_name_1 = mArrayComment.getJSONObject(index).getString(WebParams.FROM_NAME);
                                                from_profile_picture_1 = mArrayComment.getJSONObject(index).getString(WebParams.FROM_PROFILE_PICTURE);
                                                reply_1 = mArrayComment.getJSONObject(index).getString(WebParams.REPLY);
                                            }
                                        }
                                        if(lengthComment == 2) {
                                            for (int index = 0; index < mArrayComment.length(); index++) {
                                                if(index == 0) {
                                                    comment_id_1 = mArrayComment.getJSONObject(index).getString(WebParams.COMMENT_ID);
                                                    from_name_1 = mArrayComment.getJSONObject(index).getString(WebParams.FROM_NAME);
                                                    from_profile_picture_1 = mArrayComment.getJSONObject(index).getString(WebParams.FROM_PROFILE_PICTURE);
                                                    reply_1 = mArrayComment.getJSONObject(index).getString(WebParams.REPLY);
                                                }
                                                if(index == 1) {
                                                    comment_id_2 = mArrayComment.getJSONObject(index).getString(WebParams.COMMENT_ID);
                                                    from_name_2 = mArrayComment.getJSONObject(index).getString(WebParams.FROM_NAME);
                                                    from_profile_picture_2 = mArrayComment.getJSONObject(index).getString(WebParams.FROM_PROFILE_PICTURE);
                                                    reply_2 = mArrayComment.getJSONObject(index).getString(WebParams.REPLY);
                                                }
                                            }
                                        }
                                        mListHistory.add(new listHistoryModel(id, post, amount, balance, ccy_id, datetime, owner, owner_id,
                                                owner_profile_picture, with_id, with, with_profile_picture, tx_status, typepost, typecaption,
                                                privacy, numcomments, numviews, numlikes, share, comments, likes, comment_id_1, from_name_1, from_profile_picture_1,
                                                reply_1, comment_id_2, from_name_2, from_profile_picture_2, reply_2, isLike));
                                    }
                                }
                            }
                            insertPostToDB(mListHistory);

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            Timber.d("isi error history list", response.toString());
                            //code = response.getString(WebParams.ERROR_MESSAGE);
                            if(code.equals("0003")) {
                                listHistoryModel.deleteAll();
                                initializeDataPost();
                            }
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

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    Timber.w("Error Koneksi history list myhistory:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void insertPostToDB(List<listHistoryModel> mListTimeline){
        ActiveAndroid.beginTransaction();
        listHistoryModel mTm;

        if(mListTimeline.size()>0){
            for (int i = 0; i < mListTimeline.size(); i++) {
                mTm = new listHistoryModel();
                mTm = mListTimeline.get(i);
                mTm.save();
                Timber.d("idx array posts", String.valueOf(i));
            }
        }

        ActiveAndroid.setTransactionSuccessful();

        ActiveAndroid.endTransaction();
        sp.edit().putString(DefineValue.TIMELINE_FIRST_TIME, DefineValue.NO).apply();
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listHistory.clear();
                    listHistory.addAll(listHistoryModel.getAll());
                    if(listHistory.size() > 0) {
                        layout_alert.setVisibility(View.GONE);
                        layout_list.setVisibility(View.VISIBLE);
                    }
                    else {
                        layout_alert.setVisibility(View.VISIBLE);
                        layout_list.setVisibility(View.GONE);
                        txtAlert.setText("Data not found");
                        imgAlert.setImageResource(R.drawable.ic_data_not_found);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
            Timber.d("finish initialize myhistory");
        }
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    @Override
    protected int getInflateFragmentLayout() {
        return R.layout.frag_myhistory;
    }

    @Override
    public boolean checkCanDoRefresh() {
        if (mAdapter.getItemCount() == 0 || mRecyclerView == null) {
            return true;
        }
        return getCurrentLayoutManag().findFirstCompletelyVisibleItemPosition() == 0 && mRecyclerView.getChildAt(0).getTop() > 0 ;

    }

    @Override
    public void refresh() {
        int p = Integer.parseInt(page) + 1;
        page = Integer.toString(p);
        getHistoryList();
    }

	public LinearLayoutManager getCurrentLayoutManag() {
        return currentLayoutManag;
    }

    public void setCurrentLayoutManag(LinearLayoutManager currentLayoutManag) {
        this.currentLayoutManag = currentLayoutManag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(ActiveAndroid.inTransaction()){
            ActiveAndroid.endTransaction();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(start > 0) {
            listHistory.clear();
            listHistory.addAll(listHistoryModel.getAll());
            mAdapter.notifyDataSetChanged();
        }
        start++;
    }
}