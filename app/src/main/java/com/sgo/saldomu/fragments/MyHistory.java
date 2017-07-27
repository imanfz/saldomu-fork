package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listHistoryModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.HistoryDetailActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.HistoryRecycleAdapter;
import com.sgo.saldomu.coreclass.*;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.interfaces.OnLoadMoreListener;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import timber.log.Timber;

/*
  Created by Administrator on 12/2/2014.
 */
public class MyHistory extends BaseFragmentMainPage {

    private SecurePreferences sp;

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
    private int start = 0;

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
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new HistoryRecycleAdapter(listHistory, R.layout.list_recycle_timeline_item,
                getActivity(),mRecyclerView);
        mAdapter.setOnLoadMoreListener(onLoadMoreListener);
        mAdapter.setVisibleThreshold(1);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(new HistoryRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(getActivity(), HistoryDetailActivity.class);
                i.putExtra(DefineValue.POST_ID, Integer.toString(listHistory.get(position).getHistory_id()));
                i.putExtra(DefineValue.FROM_NAME, listHistory.get(position).getOwner());
                i.putExtra(DefineValue.FROM_ID, listHistory.get(position).getOwner_id());
                i.putExtra(DefineValue.TO_NAME, listHistory.get(position).getWith());
                i.putExtra(DefineValue.TO_ID, listHistory.get(position).getWith_id());
                i.putExtra(DefineValue.MESSAGE, listHistory.get(position).getPost());
                i.putExtra(DefineValue.DATE_TIME, listHistory.get(position).getDatetime());
                i.putExtra(DefineValue.CCY_ID, listHistory.get(position).getCcy_id());
                i.putExtra(DefineValue.AMOUNT, listHistory.get(position).getAmount());
                i.putExtra(DefineValue.PROF_PIC, listHistory.get(position).getOwner_profile_picture());
                i.putExtra(DefineValue.TX_STATUS, listHistory.get(position).getTypecaption());
                i.putExtra(DefineValue.WITH_PROF_PIC, listHistory.get(position).getWith_profile_picture());
                i.putExtra(DefineValue.POST_TYPE, listHistory.get(position).getTypepost());
                Timber.d("isi extra intennt history detail activity:"+i.getExtras().toString());
                switchActivity(i);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = "0";
                getHistoryList(null,0);
            }
        });

        if(isTimelineNew.equals(DefineValue.NO)){
            initializeDataPost();
        }
        getHistoryList(null,0);
    }



    private OnLoadMoreListener onLoadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            //add null , so the adapter will check view_type and show progress bar at bottom
//            listHistory.add(null);
//            mAdapter.notifyItemInserted(listHistory.size() - 1);
//            getHistoryList(null,0);
//            Log.d("masuk onloadmore myhistory","masssuukk");
        }
    };

    public void ScrolltoItem(int _post_id){
        for (int i = 0;i< listHistory.size();i++){
            if(listHistory.get(i).getHistory_id() == _post_id){
                getCurrentLayoutManag().scrollToPositionWithOffset(i,20);
                break;
            }
        }
    }

    private void initializeDataPost(){
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

    private void getHistoryList(final PtrFrameLayout frameLayout, final int mPage) {
        try {

            sp = CustomSecurePref.getInstance().getmSecurePrefs();
            _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
            accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

            RequestParams params =  MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_TIMELINE_LIST,
                    _ownerID,accessKey);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PRIVACY, privacy);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.PAGE, mPage);
            params.put(WebParams.COUNT, DefineValue.COUNT);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get history list:" + params.toString());

            MyApiClient.getTimelineList(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi params history list:"+response.toString());
                            Timber.d("list listHistory:"+Integer.toString(listHistory.size()));

                            List<listHistoryModel> mListHistory = new ArrayList<>();
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

                                if (!flagSame && isAdded()) {
                                    String post = mArrayPost.getJSONObject(i).getString(WebParams.POST);
                                    String amount = mArrayPost.getJSONObject(i).getString(WebParams.AMOUNT);
                                    String balance = mArrayPost.getJSONObject(i).getString(WebParams.BALANCE);
                                    String ccy_id = mArrayPost.getJSONObject(i).getString(WebParams.CCY_ID);
                                    String datetime = mArrayPost.getJSONObject(i).getString(WebParams.DATETIME);
                                    String owner = mArrayPost.getJSONObject(i).getString(WebParams.OWNER);
                                    if(owner.equalsIgnoreCase("you"))
                                        owner = getString(R.string.you);
                                    String owner_id = mArrayPost.getJSONObject(i).getString(WebParams.OWNER_ID);
                                    String owner_profile_picture = mArrayPost.getJSONObject(i).getString(WebParams.OWNER_PROFILE_PICTURE);
                                    String with_id = mArrayPost.getJSONObject(i).getString(WebParams.WITH_ID);
                                    String with = mArrayPost.getJSONObject(i).getString(WebParams.WITH);
                                    if(with.equalsIgnoreCase("you"))
                                        with = getString(R.string.you);
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
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            Timber.d("isi error history list:"+response.toString());
                            //code = response.getString(WebParams.ERROR_MESSAGE);
                            if(mPage == 0 && code.equals("0003")) {
                                listHistoryModel.deleteAll();
                                initializeDataPost();
                            }
                            mAdapter.setLoadingLoadMore(true);
                        }
                        if(frameLayout != null)
                            frameLayout.refreshComplete();
                        if(mPage == 0)
                            mAdapter.setLoadingLoadMore(false);
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
//                    if(MyApiClient.PROD_FAILURE_FLAG)
//                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
//                    else
//                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    Timber.w("Error Koneksi history list myhistory:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void insertPostToDB(List<listHistoryModel> mListTimeline){
        ActiveAndroid.beginTransaction();
        listHistoryModel mTm;
        new listHistoryModel();
        if(mListTimeline.size()>0){
            for (int i = 0; i < mListTimeline.size(); i++) {
                mTm = mListTimeline.get(i);
                mTm.save();
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
        return mAdapter.getItemCount() == 0 || mRecyclerView == null || getCurrentLayoutManag().findFirstCompletelyVisibleItemPosition() == 0 && mRecyclerView.getChildAt(0).getTop() > 0;

    }

    @Override
    public void refresh(PtrFrameLayout frameLayout) {
        int p = Integer.parseInt(page) + 1;
        page = Integer.toString(p);
        getHistoryList(frameLayout,0);
    }

    @Override
    public void goToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    private LinearLayoutManager getCurrentLayoutManag() {
        return currentLayoutManag;
    }

    private void setCurrentLayoutManag(LinearLayoutManager currentLayoutManag) {
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