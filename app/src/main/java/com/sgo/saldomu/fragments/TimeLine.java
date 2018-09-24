package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.google.gson.JsonObject;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listTimeLineModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TimelineDetailActivity;
import com.sgo.saldomu.adapter.TimeLineRecycleAdapter;
import com.sgo.saldomu.coreclass.BaseFragmentMainPage;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.models.retrofit.HistoryListModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import timber.log.Timber;

/*
  Created by Administrator on 12/2/2014.
 */
public class TimeLine extends BaseFragmentMainPage {

    private SecurePreferences sp;

    private RecyclerView currentRecyclerView;
    private TimeLineRecycleAdapter currentAdapter;
    private LinearLayoutManager currentLayoutManag;

    private LinearLayout layout_alert, layout_list;
    private Button btnRefresh;
    private TextView txtAlert;
    private ImageView imgAlert;

    private String _ownerID,accessKey;
    private String page = "0";
    private String privacy = "2";
    private String isTimelineNew;

    private List<listTimeLineModel> listTimeline;
    private int start = 0;
    private RecyclerView mRecyclerView;

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_time_line, container, false);
        return v;
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TimeLineRecycleAdapter mAdapter;
        ActiveAndroid.initialize(getActivity());
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        isTimelineNew = sp.getString(DefineValue.TIMELINE_FIRST_TIME,"");

        layout_alert = (LinearLayout) mView.findViewById(R.id.layout_alert_timeline);
        layout_list = (LinearLayout) mView.findViewById(R.id.layout_list_timeline);
        btnRefresh = (Button) mView.findViewById(R.id.btnRefresh);
        txtAlert = (TextView) mView.findViewById(R.id.txt_alert);
        imgAlert = (ImageView) mView.findViewById(R.id.img_alert);

        listTimeline = new ArrayList<>();

        mRecyclerView = (RecyclerView)mView.findViewById(R.id.timeline_recycle_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TimeLineRecycleAdapter(listTimeline, R.layout.list_recycle_timeline_item, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(new TimeLineRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(getActivity(), TimelineDetailActivity.class);
                i.putExtra("post_id", Integer.toString(listTimeline.get(position).getTimeline_id()));
                i.putExtra("from_name", listTimeline.get(position).getOwner());
                i.putExtra("from_id", listTimeline.get(position).getOwner_id());
                i.putExtra("to_name", listTimeline.get(position).getWith());
                i.putExtra("to_id", listTimeline.get(position).getWith_id());
                i.putExtra("message", listTimeline.get(position).getPost());
                i.putExtra("datetime", listTimeline.get(position).getDatetime());
                i.putExtra("ccy", listTimeline.get(position).getCcy_id());
                i.putExtra("amount", listTimeline.get(position).getAmount());
                i.putExtra("profpic", listTimeline.get(position).getOwner_profile_picture());
                i.putExtra("with_profpic", listTimeline.get(position).getWith_profile_picture());
                i.putExtra("tx_status", listTimeline.get(position).getTypecaption());
                i.putExtra("type_post", listTimeline.get(position).getTypepost());
                switchActivity(i);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = "0";
                getTimelineList(null, 0);
            }
        });

        setCurrentRecyclerView(mRecyclerView);
        setCurrentLayoutManag(mLayoutManager);
        setCurrentAdapter(mAdapter);

        if(isTimelineNew.equals(DefineValue.NO)){
            initializeDataPost();
        }
        getTimelineList(null,0);

    }

    private void initializeDataPost(){
        listTimeline.addAll(listTimeLineModel.getAll());
        if(listTimeline.size() > 0) {
            layout_alert.setVisibility(View.GONE);
            layout_list.setVisibility(View.VISIBLE);
        }
        else {
            layout_alert.setVisibility(View.VISIBLE);
            layout_list.setVisibility(View.GONE);
            txtAlert.setText("Data not found");
            imgAlert.setImageResource(R.drawable.ic_data_not_found);
        }
        getCurrentAdapter().notifyDataSetChanged();
    }

    public void ScrolltoItem(int _post_id){
        for (int i = 0;i< listTimeline.size();i++){
            if(listTimeline.get(i).getTimeline_id() == _post_id){
                mRecyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }

    private void getTimelineList(final PtrFrameLayout frameLayout, final int mPage) {
        try {

            sp = CustomSecurePref.getInstance().getmSecurePrefs();
            _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
            accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

            RequestParams param = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_TIMELINE_LIST,
                    _ownerID,accessKey);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TIMELINE_LIST);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PRIVACY, privacy);
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDate());
            params.put(WebParams.PAGE, mPage);
            params.put(WebParams.COUNT, DefineValue.COUNT);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get timeline list:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TIMELINE_LIST, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            HistoryListModel model = getGson().fromJson(object, HistoryListModel.class);

                            try {
                                String code = model.getError_code();
                                String count = model.getCount();

                                if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                                    List<listTimeLineModel> mListTimeline = new ArrayList<>();

                                    JSONArray mArrayPost = new JSONArray(getGson().toJson(model.getData_posts()));
                                    for (int i = 0; i < mArrayPost.length(); i++) {
                                        int id = Integer.parseInt(mArrayPost.getJSONObject(i).getString(WebParams.ID));

                                        boolean flagSame = false;

                                        // cek apakah ada id yang sama.. kalau ada tidak dimasukan ke array
                                        if (listTimeline.size() > 0) {
                                            for (listTimeLineModel aListTimeline : listTimeline) {
                                                if (aListTimeline.getTimeline_id() != id) {
                                                    flagSame = false;
                                                } else {
                                                    flagSame = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!flagSame) {
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
                                                mListTimeline.add(new listTimeLineModel(id, post, amount, balance, ccy_id, datetime, owner, owner_id,
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
                                                mListTimeline.add(new listTimeLineModel(id, post, amount, balance, ccy_id, datetime, owner, owner_id,
                                                        owner_profile_picture, with_id, with, with_profile_picture, tx_status, typepost, typecaption,
                                                        privacy, numcomments, numviews, numlikes, share, comments, likes, comment_id_1, from_name_1, from_profile_picture_1,
                                                        reply_1, comment_id_2, from_name_2, from_profile_picture_2, reply_2, isLike));
                                            }
                                        }

                                    }
                                    insertPostToDB(mListTimeline);
                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginMain(getActivity(),message);
                                }
                                else {
                                    if(code.equals("0003")) {
                                        listTimeLineModel.deleteAll();
                                        initializeDataPost();
                                    }
                                }
                                if(frameLayout !=null)
                                    frameLayout.refreshComplete();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void insertPostToDB(List<listTimeLineModel> mListTimeline){
        ActiveAndroid.beginTransaction();
        listTimeLineModel mTm;

        Timber.d("arrayPost length:"+String.valueOf(mListTimeline.size()));
        if(mListTimeline.size()>0){
            for (int i = 0; i < mListTimeline.size(); i++) {
                new listTimeLineModel();
                mTm = mListTimeline.get(i);
                mTm.save();
                Timber.d("idx array posts:"+String.valueOf(i));
            }
        }

        ActiveAndroid.setTransactionSuccessful();

        ActiveAndroid.endTransaction();
        sp.edit().putString(DefineValue.TIMELINE_FIRST_TIME, DefineValue.NO).apply();
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listTimeline.clear();
                    listTimeline.addAll(listTimeLineModel.getAll());
                    if(listTimeline.size() > 0) {
                        layout_alert.setVisibility(View.GONE);
                        layout_list.setVisibility(View.VISIBLE);
                    }
                    else {
                        layout_alert.setVisibility(View.VISIBLE);
                        layout_list.setVisibility(View.GONE);
                        txtAlert.setText(getString(R.string.data_not_found));
                        imgAlert.setImageResource(R.drawable.ic_data_not_found);
                    }
                    getCurrentAdapter().notifyDataSetChanged();
                }
            });
            Timber.d("finish initialize timeline");
        }
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    @Override
    protected int getInflateFragmentLayout() {
        return R.layout.frag_time_line;
    }

    @Override
    public boolean checkCanDoRefresh() {
        try {
            return getCurrentAdapter().getItemCount() == 0 || getCurrentRecyclerView() == null || getCurrentLayoutManag().findFirstCompletelyVisibleItemPosition() == 0 && getCurrentRecyclerView().getChildAt(0).getTop() > 0;
        }
        catch (Exception ex){
            Timber.wtf("Exception checkCandoRefresh:"+ex.getMessage());
        }
        return false;
    }

    @Override
    public void refresh(PtrFrameLayout frameLayout) {
        int p = Integer.parseInt(page) + 1;
        page = Integer.toString(p);
        getTimelineList(frameLayout,0);
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

    private TimeLineRecycleAdapter getCurrentAdapter() {
        return currentAdapter;
    }

    private void setCurrentAdapter(TimeLineRecycleAdapter currentAdapter) {
        this.currentAdapter = currentAdapter;
    }

    private RecyclerView getCurrentRecyclerView() {
        return currentRecyclerView;
    }

    private void setCurrentRecyclerView(RecyclerView currentRecyclerView) {
        this.currentRecyclerView = currentRecyclerView;
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
            listTimeline.clear();
            listTimeline.addAll(listTimeLineModel.getAll());
            getCurrentAdapter().notifyDataSetChanged();
        }
        start++;
    }
}