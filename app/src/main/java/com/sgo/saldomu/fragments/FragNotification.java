package com.sgo.saldomu.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.NotificationModelClass;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.NotificationActivity;
import com.sgo.saldomu.adapter.NotificationListAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DividerItemDecoration;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.NotificationHandler;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.models.retrofit.NotifModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import timber.log.Timber;
/*
  Created by thinkpad on 3/19/2015.
 */
public class FragNotification extends BaseFragment {

    private View v;

    private final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
    private NotificationListAdapter mAdapter;
    private String _memberId,_userid,accessKey,_profpic;
    private ArrayList<NotificationModelClass> mData;
    private ArrayList<JSONObject> mDataNotifDetail;
    private RecyclerView mRecyclerView;
    private PtrFrameLayout mPtr;
    private View empty_layout;
    private NotificationModelClass tempMData;
    private ProgressDialog out;
    private SecurePreferences sp;
    private boolean flagClaim = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_notification, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _memberId = sp.getString(DefineValue.MEMBER_ID, "");
        _userid = sp.getString(DefineValue.USERID_PHONE, "");
        _profpic = sp.getString(DefineValue.IMG_URL, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        empty_layout = v.findViewById(R.id.empty_layout);
        empty_layout.setVisibility(View.GONE);
        Button btn_refresh = empty_layout.findViewById(R.id.btnRefresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPtr.autoRefresh();
            }
        });

        mPtr = v.findViewById(R.id.rotate_header_list_view_frame);
        mPtr.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                sentRetrieveNotif(false);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                //return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return canScroolUp();
            }
        });

        mRecyclerView = v.findViewById(R.id.notification_recycle_list);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), null);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mData = new ArrayList<>();
        mDataNotifDetail = new ArrayList<>();
        mAdapter = new NotificationListAdapter(this,getActivity(), mData, new NotificationListAdapter.OnItemClickListener() {
            @Override
            public void onItemClickView(View view, int position, Boolean isLongClick) {
                NotificationItemClickAction(position);
            }

            @Override
            public void onItemBtnAccept(View view, int position, Boolean isLongClick) {
                NotificationItemClickAction(position);
            }

            @Override
            public void onItemBtnClaim(View view, int position, Boolean isLongClick) {
                NotificationItemClickAction(position);
            }

        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(mAdapter.getItemCount() - 1);

                /*if(viewHolder != null)
                    Log.d("on item visible idx position", "visible itemnya");
                else
                    Log.d("on item visible idx position", "gone itemnya");*/

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    tempMData = mData.get(i);
                    if (tempMData.getNotif_type() == NotificationActivity.TYPE_DECLINE) {
                        if (recyclerView.getChildAt(i).getVisibility() == View.VISIBLE) {
                            if (!tempMData.isRead()) {
                                Timber.d("on item visible idx position");
                                sentReadNotif(tempMData.getNotif_id(), i);
                            }
                        }

                    }

                }
            }
        });
        sentRetrieveNotif(true);
        getActivity().setResult(MainPage.RESULT_NORMAL);
    }


    private void NotificationItemClickAction(int position){
        NotificationModelClass mObj = mData.get(position);
        JSONObject mObjDetail = mDataNotifDetail.get(position);
        try {
            Timber.d("isi notif type:"+String.valueOf(mObj.getNotif_type()));

            switch (mObj.getNotif_type()){
                case NotificationActivity.TYPE_TRANSFER:
                    int pay_status = mObjDetail.getInt(WebParams.STATUS);
                    if(pay_status == NotificationActivity.P2PSTAT_PENDING){
                        sentReadNotif(mData.get(position).getNotif_id(), position);
                        Intent data = new Intent();
                        data.putExtra(DefineValue.AMOUNT, mObjDetail.getString(WebParams.AMOUNT));
                        data.putExtra(DefineValue.CUST_NAME, mObj.getFrom_name());
                        data.putExtra(DefineValue.USERID_PHONE, mObjDetail.getString(WebParams.FROM));
                        data.putExtra(DefineValue.MESSAGE, mObjDetail.getString(WebParams.DESC));
                        data.putExtra(DefineValue.REQUEST_ID, mObjDetail.getString(WebParams.REQUEST_ID));
                        data.putExtra(DefineValue.TRX, mObjDetail.getString(WebParams.TRX_ID));
                        data.putExtra(DefineValue.NOTIF_TYPE,NotificationActivity.TYPE_TRANSFER);
                        getActivity().setResult(MainPage.RESULT_NOTIF, data);
                        getActivity().finish();

                    }
                    else if(pay_status == NotificationActivity.P2PSTAT_PAID){
                        Toast.makeText(getActivity(), getString(R.string.notifications_p2p_status_paid), Toast.LENGTH_SHORT).show();
                    }

                    break;
                case NotificationActivity.TYPE_PAID:
                case NotificationActivity.TYPE_COMMENT:
                case NotificationActivity.TYPE_LIKE:
                    sentReadNotif(mObj.getNotif_id(), position);
                    Intent data = new Intent();
                    String tempFromID;
                    data.putExtra(DefineValue.POST_ID,mObjDetail.getString(WebParams.POST_ID));
                    data.putExtra(DefineValue.TO_ID,mObj.getTo_id());
//                    data.putExtra(DefineValue.TO_NAME, mObj.getFrom_name());

                    if(mObj.getNotif_type() == NotificationActivity.TYPE_PAID) {
                        data.putExtra(DefineValue.MESSAGE, mObjDetail.getString(WebParams.DESC));
                        tempFromID = mObjDetail.getString(WebParams.FROM);
                    }else {
                        data.putExtra(DefineValue.MESSAGE, mObjDetail.getString(WebParams.MESSAGE));
                        tempFromID = mObjDetail.getString(WebParams.FROM_USER_ID);
                    }

                    if(tempFromID.equals(_userid)){
                        data.putExtra(DefineValue.FROM_NAME,getString(R.string.you));
                        data.putExtra(DefineValue.PROF_PIC,_profpic);
                        data.putExtra(DefineValue.TO_NAME, mObj.getFrom_name());
                        data.putExtra(DefineValue.WITH_PROF_PIC,mObj.getFrom_profile_picture());

                    }else {
                        data.putExtra(DefineValue.FROM_NAME,mObj.getFrom_name());
                        data.putExtra(DefineValue.PROF_PIC,mObj.getFrom_profile_picture());
                        data.putExtra(DefineValue.TO_NAME, getString(R.string.you));
                        data.putExtra(DefineValue.WITH_PROF_PIC,_profpic);
                    }
//                    if(mObj.getTo_id().equals(_userid))
//                        data.putExtra(DefineValue.FROM_NAME,getString(R.string.you));

                    data.putExtra(DefineValue.FROM_ID,mObj.getFrom_id());
                    data.putExtra(DefineValue.DATE_TIME,mObj.getDate_time());
                    data.putExtra(DefineValue.CCY_ID,mObjDetail.getString(WebParams.CCY_ID));
                    data.putExtra(DefineValue.AMOUNT,mObjDetail.getString(WebParams.AMOUNT));
//                    data.putExtra(DefineValue.PROF_PIC,_profpic);
                    data.putExtra(DefineValue.TX_STATUS,mObjDetail.getString(WebParams.TYPECAPTION));
//                    data.putExtra(DefineValue.WITH_PROF_PIC,mObj.getFrom_profile_picture());
                    data.putExtra(DefineValue.POST_TYPE, mObjDetail.getString(WebParams.TYPEPOST));
                    if(mObj.getNotif_type() == NotificationActivity.TYPE_LIKE)
                        data.putExtra(DefineValue.NOTIF_TYPE, NotificationActivity.TYPE_LIKE);
                    else
                        data.putExtra(DefineValue.NOTIF_TYPE, NotificationActivity.TYPE_COMMENT);
                    Timber.d("isi extra intennt history detail activity:"+data.getExtras().toString());
                    getActivity().setResult(MainPage.RESULT_NOTIF, data);
                    getActivity().finish();
                    break;
                case NotificationActivity.TYPE_NON_MEMBER:
                    if(!flagClaim)
                        sentClaimTransfer(true, mObj.getId_result());
                    break;

                case NotificationActivity.REJECTED_KTP:
                    SecurePreferences.Editor editor = sp.edit();
                    editor.putString(DefineValue.REJECT_KTP,mObjDetail.optString(WebParams.REJECT_KTP,"N"));
                    editor.putString(DefineValue.REJECT_FOTO,mObjDetail.optString(WebParams.REJECT_FOTO,"N"));
                    editor.putString(DefineValue.REJECT_TTD,mObjDetail.optString(WebParams.REJECT_TTD,"N"));
                    editor.putString(DefineValue.REMARK_KTP,mObjDetail.optString(WebParams.REMARK_KTP,"N"));
                    editor.putString(DefineValue.REMARK_FOTO,mObjDetail.optString(WebParams.REMARK_FOTO,"N"));
                    editor.putString(DefineValue.REMARK_TTD,mObjDetail.optString(WebParams.REMARK_TTD,"N"));
                    editor.putString(DefineValue.IS_REGISTERED_LEVEL,mObjDetail.optString(WebParams.IS_REGISTERED,"N"));
                    editor.apply();
                    Intent dataProfile = new Intent();
                    dataProfile.putExtra(DefineValue.NOTIF_TYPE,NotificationActivity.REJECTED_KTP);
                    getActivity().setResult(MainPage.RESULT_NOTIF, dataProfile);
                    getActivity().finish();
                    break;
                case NotificationActivity.REJECTED_SIUP_NPWP:
                    SecurePreferences.Editor editor1 = sp.edit();
                    editor1.putString(DefineValue.REJECT_SIUP,mObjDetail.optString(WebParams.REJECT_SIUP,"N"));
                    editor1.putString(DefineValue.REJECT_NPWP,mObjDetail.optString(WebParams.REJECT_NPWP,"N"));
                    editor1.putString(DefineValue.REMARK_SIUP,mObjDetail.optString(WebParams.REMARK_SIUP,"N"));
                    editor1.putString(DefineValue.REMARK_NPWP,mObjDetail.optString(WebParams.REMARK_NPWP,"N"));
                    editor1.putString(DefineValue.IS_AGENT,mObjDetail.optString(WebParams.IS_AGENT,"N"));
                    editor1.apply();
                    Intent dataUpgradeAgent = new Intent();
                    dataUpgradeAgent.putExtra(DefineValue.NOTIF_TYPE,NotificationActivity.REJECTED_KTP);
                    getActivity().setResult(MainPage.RESULT_NOTIF, dataUpgradeAgent);
                    getActivity().finish();
                    break;
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            if(!mObj.isRead())
                sentReadNotif(mObj.getNotif_id(),position);
        }
    }

    private boolean canScroolUp() {
        //Log.wtf(" adapter get item count", String.valueOf(mAdapter.getItemCount()));
        //Log.wtf(" Recycle view sama dengan null", String.valueOf(mRecyclerView == null));
        //Log.wtf(" layout manager find first completely visilble item position", String.valueOf(mLayoutManager.findFirstCompletelyVisibleItemPosition()));
        //Log.wtf(" Recycle view get child at 0 get top", String.valueOf(mRecyclerView.getChildAt(0).getTop()));

        try {
            return mAdapter.getItemCount() == 0 || mRecyclerView == null || mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 && mRecyclerView.getChildAt(0).getTop() == 0;

        }
        catch (Exception ex){
            Timber.wtf("Exception checkCandoRefresh:" + ex.getMessage());
        }
        return false;
    }

    private void sentReadNotif(String _notif_id, final int position){
        try{
            extraSignature = _notif_id;
            RequestParams param = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_NOTIF_READ,
                    _userid,accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_NOTIF_READ, extraSignature);
            params.put(WebParams.USER_ID,_userid);
            params.put(WebParams.NOTIF_ID_READ,_notif_id);
            params.put(WebParams.MEMBER_ID, _memberId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

            Timber.d("isi params Read Notif:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_NOTIF_READ, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            jsonModel model = getGson().fromJson(object, jsonModel.class);
                            String code = model.getError_code();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if(getActivity()!=null){
                                    mData.get(position).setRead(true);
                                    getActivity().setResult(MainPage.RESULT_NOTIF);
                                    mAdapter.notifyItemChanged(position);
                                    CheckNotification();
                                }
                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                code = model.getError_code() + ":" + model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void CheckNotification(){
        Thread mth = new Thread(){
            @Override
            public void run() {
                Activity mContext = getActivity().getParent();
                if(mContext instanceof MainPage) {
                    MainPage mMainPage = (MainPage)mContext;
                    NotificationHandler mNoHand = new NotificationHandler(mMainPage, sp);
                    mNoHand.sentRetrieveNotif();
                }
            }
        };
        mth.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setResult(MainPage.RESULT_NOTIF);
    }

    private void sentRetrieveNotif(final Boolean isDialog){
        try{

            if(isDialog){
                out = DefinedDialog.CreateProgressDialog(getActivity(), "");
                if(!out.isShowing())
                out.show();
            }

            RequestParams param = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_NOTIF_RETRIEVE,
                    _userid,accessKey);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_NOTIF_RETRIEVE);
            params.put(WebParams.USER_ID,_userid);
            params.put(WebParams.MEMBER_ID, _memberId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

            Timber.d("isi params Retrieve Notif:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_NOTIF_RETRIEVE, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
                                if (isDialog) {
                                    if(out.isShowing())
                                        out.dismiss();
                                }

                                NotifModel model = getGson().fromJson(object, NotifModel.class);

                                String code = model.getError_message();

                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    if (mRecyclerView.getVisibility() == View.GONE) {
                                        mRecyclerView.setVisibility(View.VISIBLE);
                                        empty_layout.setVisibility(View.GONE);
                                    }

                                    JSONArray mArrayData = new JSONArray(getGson().toJson(model.getData_user_notif()));

                                    String title = null, detail = "", time, to_id, from_name, from_id, notif_id, from_profile_picture, date_time, id_result;
                                    mData.clear();
                                    mDataNotifDetail.clear();
                                    int notif_type, image = 0;
                                    boolean read;
                                    Date time1;
                                    PrettyTime p = new PrettyTime(new Locale(DefineValue.sDefSystemLanguage));
                                    JSONObject notif_detail, mObject;
                                    NotificationModelClass mObj;

                                    try {
                                        for (int i = 0; i < mArrayData.length(); i++) {
                                            mObject = mArrayData.getJSONObject(i);

                                            notif_id = mObject.getString(WebParams.NOTIF_ID);
                                            notif_type = mObject.getInt(WebParams.NOTIF_TYPE);
                                            from_name = mObject.getString(WebParams.FROM_NAME);
                                            from_id = mObject.getString(WebParams.FROM_USER_ID);
                                            to_id = mObject.getString(WebParams.TO_USER_ID);
                                            from_profile_picture = mObject.getString(WebParams.FROM_PROFILE_PICTURE);
                                            date_time = mObject.getString(WebParams.CREATED_DATE);
                                            read = (mObject.getInt(WebParams.NOTIF_READ) == 1);
                                            id_result = mObject.getString(WebParams.ID_RESULT);

                                            String notif_detail_string = mObject.optString(WebParams.NOTIF_DETAIL, "");

                                            if (!notif_detail_string.isEmpty()) {
                                                notif_detail = new JSONArray(notif_detail_string).getJSONObject(0);
                                                switch (notif_type) {
                                                    case NotificationActivity.TYPE_LIKE:
                                                        image = 0;
                                                        title = from_name + " " + getString(R.string.notif_text_like_name) + " : ";
                                                        if(!notif_detail.optString(WebParams.MESSAGE,"").isEmpty())
                                                            detail = "\"" + notif_detail.getString(WebParams.MESSAGE) + "\"";
                                                        break;
                                                    case NotificationActivity.TYPE_COMMENT:
                                                        image = 0;
                                                        title = from_name + " " + getString(R.string.notif_text_comment_name) + " : ";
                                                        if(!notif_detail.optString(WebParams.MESSAGE,"").isEmpty())
                                                            detail = "\"" + notif_detail.getString(WebParams.MESSAGE) + "\"";
                                                        break;
                                                    case NotificationActivity.TYPE_TRANSFER:
                                                        image = R.drawable.ic_cash_in;
                                                        title = getString(R.string.notif_text_ask4money_name) + " " + from_name;
                                                        detail = notif_detail.getString(WebParams.CCY_ID) + " " + notif_detail.getString(WebParams.AMOUNT) +
                                                                "\n" + notif_detail.get(WebParams.DESC);
                                                        break;
                                                    case NotificationActivity.TYPE_PAID:
                                                        image = R.drawable.ic_cash_out;
                                                        title = getString(R.string.notif_text_paid_name) + " " + from_name;
                                                        detail = notif_detail.getString(WebParams.CCY_ID) + " " + notif_detail.getString(WebParams.AMOUNT) +
                                                                "\n" + notif_detail.get(WebParams.DESC);
                                                        break;
                                                    case NotificationActivity.TYPE_DECLINE:
                                                        image = R.drawable.ic_cash_in;
                                                        title = getString(R.string.notif_text_decline_name) + " " + from_name;
                                                        detail = notif_detail.getString(WebParams.CCY_ID) + " " + notif_detail.getString(WebParams.AMOUNT) +
                                                                "\n" + notif_detail.get(WebParams.REMARK);
                                                        break;
                                                    case NotificationActivity.TYPE_NON_MEMBER:
                                                        image = R.drawable.ic_cash_out;
                                                        title = getString(R.string.notif_text_non_member) + " " + from_name;
                                                        detail = notif_detail.getString(WebParams.CCY_ID) + " " + notif_detail.getString(WebParams.AMOUNT);
                                                        break;
                                                    case NotificationActivity.CLAIM_NON_MEMBER:
                                                        image = R.drawable.ic_cash_in;
                                                        title = from_name + " " + getString(R.string.notif_text_claim_non_member);
                                                        detail = notif_detail.getString(WebParams.CCY_ID) + " " + notif_detail.getString(WebParams.AMOUNT) +
                                                                "\n" + from_name + " " + getString(R.string.notif_detail_claim_non_member);
                                                        break;
                                                    case NotificationActivity.REJECTED_KTP:
                                                        image = R.drawable.ic_photo_camera_rejected;
                                                        title = getString(R.string.notif_title_photo_ktp_rejected);
                                                        break;
                                                }

                                                if (notif_type == NotificationActivity.TYPE_LIKE ||
                                                        notif_type == NotificationActivity.TYPE_COMMENT ||
                                                        notif_type == NotificationActivity.TYPE_TRANSFER ||
                                                        notif_type == NotificationActivity.TYPE_PAID ||
                                                        notif_type == NotificationActivity.TYPE_DECLINE ||
                                                        notif_type == NotificationActivity.TYPE_NON_MEMBER ||
                                                        notif_type == NotificationActivity.CLAIM_NON_MEMBER ||
                                                        notif_type == NotificationActivity.REJECTED_KTP
                                                        ) {
                                                    mDataNotifDetail.add(notif_detail);

                                                    time1 = DateTimeFormat.convertStringtoCustomDateTime(date_time);
                                                    time = p.formatDuration(time1);

                                                    mObj = new NotificationModelClass(notif_id, image, title, to_id, from_name,
                                                            from_id, detail, time, notif_type, read, notif_detail, from_profile_picture, date_time, id_result);
                                                    mData.add(mObj);
                                                }

                                            }
                                        }
                                        if (mData.size() != 0) {
                                            mAdapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(getActivity(), "Tidak ada Notifikasi", Toast.LENGTH_LONG).show();
                                            mRecyclerView.setVisibility(View.GONE);
                                            empty_layout.setVisibility(View.VISIBLE);
                                            mData.clear();
                                            mAdapter.notifyDataSetChanged();
                                            getActivity().setResult(MainPage.RESULT_NOTIF);
                                        }

                                        getActivity().setResult(MainPage.RESULT_NOTIF);
                                    } catch (Exception ex) {
                                        Timber.d("isi exception Notification:"+ex.getMessage());
                                    }


                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {

                                    if (code.equals("0003")) {
                                        if(FragNotification.this.isVisible()) {
                                            Toast.makeText(getActivity(), getString(R.string.notifications_empty), Toast.LENGTH_LONG).show();
                                            mRecyclerView.setVisibility(View.GONE);
                                            empty_layout.setVisibility(View.VISIBLE);
                                            mData.clear();
                                            mAdapter.notifyDataSetChanged();
                                            getActivity().setResult(MainPage.RESULT_NOTIF);

                                        }
                                    }
                                }

                                if (mPtr.isShown())
                                    mPtr.refreshComplete();

                                if (model.getOn_error()) {
                                    if(MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }

                            } catch (JSONException e) {

                                Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            String err = (e.getMessage()==null)?"Connection failed":e.getMessage();
            Timber.e("http err:"+err);
        }
    }

    private void sentClaimTransfer(final Boolean isDialog, String _hold_id){
        try{
            flagClaim = true;
            if(isDialog){
                out = DefinedDialog.CreateProgressDialog(getActivity(), "");
                if(!out.isShowing())
                    out.show();
            }

            extraSignature = _hold_id + commIDLogin;

            RequestParams param = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CLAIM_TRANSFER_NON_MEMBER,
                    userPhoneID, accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CLAIM_TRANSFER_NON_MEMBER, extraSignature);
            params.put(WebParams.USER_ID,_userid);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.HOLD_ID, _hold_id);

            Timber.d("isi params sent claim non member:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CLAIM_TRANSFER_NON_MEMBER, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            if (isDialog)
                                if(out.isShowing())
                                    out.dismiss();

                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            if (!model.getOn_error()){
                                String code = model.getError_code();

//                        if (code.equals(WebParams.SUCCESS_CODE)) {
//
//                        } else
                                if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                }

                                sentRetrieveNotif(true);
                            }else {
                                if (MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                                getActivity().setResult(MainPage.RESULT_NOTIF);
                                getActivity().finish();
                            }
                        }
                    });
        } catch (Exception e) {
            String err = (e.getMessage()==null)?"Connection failed":e.getMessage();
            Timber.e("http err:"+err);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshAdapter() {
        sentRetrieveNotif(true);
    }
}
