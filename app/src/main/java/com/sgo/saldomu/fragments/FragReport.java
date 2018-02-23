package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.balysv.materialripple.MaterialRippleLayout;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.ReportAskListModel;
import com.sgo.saldomu.Beans.ReportListEspayModel;
import com.sgo.saldomu.Beans.ReportListModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ReportAskListAdapter;
import com.sgo.saldomu.adapter.ReportCommFeeAdapter;
import com.sgo.saldomu.adapter.ReportListAdapter;
import com.sgo.saldomu.adapter.ReportListEspayAdapter;
import com.sgo.saldomu.coreclass.CollapseExpandAnimation;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.models.ReportListCommFeeModel;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import timber.log.Timber;

/*
  Created by Administrator on 5/19/2015.
 */
public class FragReport extends ListFragment implements ReportBillerDialog.OnDialogOkCallback {

    final static int REPORT_ASK = 0x0299395;
    final static int REPORT_SCASH = 0x0299394;
    final static int REPORT_ESPAY = 0x0299393;
    final static int REPORT_FEE = 0x0299396;
    private final String DATEFROM = "tagFrom";
    private final String DATETO = "tagTo";
    final private String ITEM_DESC_LISTRIK = "Listrik";
    final private String ITEM_DESC_PLN= "Voucher Token Listrik";
    final private String ITEM_DESC_NON= "PLN Non-Taglis";
    final private String ITEM_DESC_BPJS= "BPJS";


    private View v;
    private LinearLayout layout_filter;
    private int height;
    private String OrifromDate;
    private String OritoDate;
    private ListView lv_report;
    private ViewGroup footerLayout;
    private ToggleButton filter_btn;
    private ImageView spining_progress;
    private MaterialRippleLayout btn_loadmore;
    private TextView tv_date_from;
    private TextView tv_date_to ;
    private ProgressDialog out;
    private ListAdapter UniAdapter = null;
    private SecurePreferences sp;
    private Calendar date_from;
    private Calendar date_to;
    private Calendar bak_date_to;
    private Calendar bak_date_from;
    private Animation frameAnimation;
    private Button btn_refresh;
    private int page;
    private int report_type;
    private PtrFrameLayout mPtrFrame;
    private View emptyLayout;


    public static FragReport newInstance(int _report_type) {
        FragReport mFrag = new FragReport();
        mFrag.report_type = _report_type;
        return mFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setV(inflater.inflate(R.layout.frag_report, container, false));
        return getV();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        layout_filter = (LinearLayout) getV().findViewById(R.id.layout_filter);
        lv_report = (ListView) getV().findViewById(android.R.id.list);
        tv_date_from =  (TextView) getV().findViewById(R.id.filter_date_from);
        tv_date_to =  (TextView) getV().findViewById(R.id.filter_date_to);
        layout_filter.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        height = layout_filter.getMeasuredHeight();
        filter_btn = (ToggleButton) getV().findViewById(R.id.filter_toggle_btn);
        footerLayout = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.footer_loadmore, lv_report, false);
        footerLayout.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        spining_progress = (ImageView) footerLayout.findViewById(R.id.image_spinning_wheel);
        btn_loadmore = (MaterialRippleLayout) footerLayout.findViewById(R.id.btn_loadmore);
        emptyLayout = getV().findViewById(R.id.empty_layout);
        emptyLayout.setVisibility(View.GONE);
        btn_refresh = (Button) emptyLayout.findViewById(R.id.btnRefresh);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        OrifromDate = DateTimeFormat.getCurrentDateMinus(6);
        OritoDate = DateTimeFormat.getCurrentDate();
        page = 1;

        filter_btn.setOnClickListener(filterBtnListener);

        btn_loadmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataReport(page, CalToString(date_from), CalToString(date_to), false);
            }
        });

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPtrFrame.autoRefresh();
            }
        });

        date_from = StringToCal(OrifromDate);
        date_to = StringToCal(OritoDate);
        bak_date_from = (Calendar) date_from.clone();
        bak_date_to = (Calendar) date_to.clone();

        String dedate = getString(R.string.from)+" :\n"+date_from.get(Calendar.DAY_OF_MONTH)+"-"+(date_from.get(Calendar.MONTH)+1)+"-"+date_from.get(Calendar.YEAR);
        tv_date_from.setText(dedate);
        dedate = getString(R.string.to)+" :\n"+date_to.get(Calendar.DAY_OF_MONTH)+"-"+(date_to.get(Calendar.MONTH)+1)+"-"+date_to.get(Calendar.YEAR);
        tv_date_to.setText(dedate);

        tv_date_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filter_btn.setChecked(false);
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        dobPickerSetListener,
                        date_from.get(Calendar.YEAR),
                        date_from.get(Calendar.MONTH),
                        date_from.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show(getActivity().getFragmentManager(), DATEFROM);
            }
        });

        tv_date_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_btn.setChecked(false);
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        dobPickerSetListener,
                        date_to.get(Calendar.YEAR),
                        date_to.get(Calendar.MONTH),
                        date_to.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show(getActivity().getFragmentManager(), DATETO);
            }
        });

        //Adapter list data

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        setLoadMore(true);
        if(report_type == REPORT_SCASH){
            ArrayList <ReportListModel> mData = new ArrayList<>();
            ReportListAdapter adapter = new ReportListAdapter(getActivity(),R.layout.list_transaction_report_item,mData);
            lv_report.setAdapter(adapter);
        }
        else if(report_type == REPORT_ESPAY) {
            ArrayList <ReportListEspayModel> mData = new ArrayList<>();
            ReportListEspayAdapter adapter = new ReportListEspayAdapter(getActivity(),R.layout.list_transaction_report_espay_item,mData);
            lv_report.setAdapter(adapter);
        }
        else  if(report_type == REPORT_ASK){
            ArrayList <ReportAskListModel> mData = new ArrayList<>();
            ReportAskListAdapter adapter = new ReportAskListAdapter(getActivity(),R.layout.list_request_report_item,mData);
            lv_report.setAdapter(adapter);
        }
        else  if(report_type == REPORT_FEE){
            ArrayList <ReportListCommFeeModel> mData = new ArrayList<>();
            ReportCommFeeAdapter adapter = new ReportCommFeeAdapter(getActivity(),R.layout.list_report_comm_fee,mData);
            lv_report.setAdapter(adapter);
        }
        setLoadMore(false);
        if(getUniAdapter() == null ||!getUniAdapter().isEmpty()){
            ListAdapter tempAdap = ((HeaderViewListAdapter) lv_report.getAdapter()).getWrappedAdapter();
            if(tempAdap != null)
                setUniAdapter(tempAdap);
            else setUniAdapter(lv_report.getAdapter());
        }

        lv_report.setOnItemClickListener(reportItemListener);

        mPtrFrame = (PtrFrameLayout) getV().findViewById(R.id.rotate_header_list_view_frame);

        final MaterialHeader header = new MaterialHeader(getActivity());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, 15, 0, 10);
        header.setPtrFrameLayout(mPtrFrame);

        mPtrFrame.setHeaderView(header);
        mPtrFrame.addPtrUIHandler(header);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                page = 1;
                if (filter_btn.isChecked())
                    getDataReport(0, CalToString(date_from), CalToString(date_to), null);
                else
                    getDataReport(0, OrifromDate, OritoDate, null);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                //return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return canScroolUp();
            }
        });

        //getDataReport(0, from, to, true);
        mPtrFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPtrFrame.autoRefresh(false);
            }
        }, 50);
    }

    private boolean canScroolUp() {
        return lv_report != null && (lv_report.getAdapter().getCount() == 0 || lv_report.getFirstVisiblePosition() == 0 && lv_report.getChildAt(0).getTop() == 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Calendar StringToCal(String src){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",new Locale("id","INDONESIA"));
        Calendar tempCalendar = Calendar.getInstance();

        try {
            tempCalendar.setTime(format.parse(src));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tempCalendar;
    }

    private String CalToString(Calendar src){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",new Locale("id","INDONESIA"));
        return format.format(src.getTime());
    }

    private ToggleButton.OnClickListener filterBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean on = ((ToggleButton) v).isChecked();
            filter_btn.setOnClickListener(null);
            if (on) {
                getDataReport(0, CalToString(date_from),CalToString(date_to),true);
            } else {
//                Log.e("isi from & to", OrifromDate +" / "+ OritoDate);
                getDataReport(0, OrifromDate, OritoDate, true);
            }
            page = 1;
        }
    };

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String dedate;

            if(view.getTag().equals(DATEFROM)){
                dedate = getString(R.string.from)+" :\n"+dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
                bak_date_from = (Calendar) date_from.clone();
                date_from.set(year,monthOfYear,dayOfMonth);
                tv_date_from.setText(dedate);
            }
            else {
                dedate = getString(R.string.to)+" :\n"+dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
                bak_date_to = (Calendar) date_to.clone();
                date_to.set(year, monthOfYear, dayOfMonth);
                tv_date_to.setText(dedate);
            }
        }
    };


    private void getDataReport(int _page, final String _date_from, String _date_to, final Boolean isRefresh){
        try{
            if(isRefresh == null){
                Timber.wtf("masuk ptr");
            }
            else if(isRefresh){
                Timber.wtf("masuk refresh");
                out = DefinedDialog.CreateProgressDialog(getActivity(), null);
                out.show();
                mPtrFrame.setEnabled(true);
                mPtrFrame.setVisibility(View.VISIBLE);
            }
            else {
                Timber.wtf("masuk load more");
                btn_loadmore.setVisibility(View.GONE);
                spining_progress.setVisibility(View.VISIBLE);
                spining_progress.startAnimation(frameAnimation);
            }

            String user_id = sp.getString(DefineValue.USERID_PHONE,"");
            String access_key = sp.getString(DefineValue.ACCESS_KEY,"");

            UUID uuid = MyApiClient.getUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();
            String webserviceScash = MyApiClient.getWebserviceName(MyApiClient.LINK_TRANSACTION_REPORT);
            String signatureScash = MyApiClient.getSignature(uuid, dtime, webserviceScash, MyApiClient.COMM_ID + user_id, access_key);

            RequestParams paramsScash = new RequestParams();
            paramsScash.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            paramsScash.put(WebParams.COMM_ID,MyApiClient.COMM_ID);
            paramsScash.put(WebParams.PAGE, _page);
            paramsScash.put(WebParams.DATE_FROM, _date_from);
            paramsScash.put(WebParams.DATE_TO, _date_to);
            paramsScash.put(WebParams.CUST_ID,sp.getString(DefineValue.CUST_ID,""));
            paramsScash.put(WebParams.USER_ID, user_id);
            paramsScash.put(WebParams.RC_UUID, uuid.toString());
            paramsScash.put(WebParams.RC_DTIME, dtime);
            paramsScash.put(WebParams.SIGNATURE, signatureScash);



            String webserviceEspay = MyApiClient.getWebserviceName(MyApiClient.LINK_REPORT_ESPAY);
            String signatureEspay = MyApiClient.getSignature(uuid, dtime, webserviceEspay, MyApiClient.COMM_ID + user_id, access_key);

            RequestParams paramsEspay = new RequestParams();
            paramsEspay.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            paramsEspay.put(WebParams.COMM_ID,MyApiClient.COMM_ID);
            paramsEspay.put(WebParams.PAGE, _page);
            paramsEspay.put(WebParams.DATE_FROM, _date_from);
            paramsEspay.put(WebParams.DATE_TO, _date_to);
            paramsEspay.put(WebParams.CUST_ID,sp.getString(DefineValue.CUST_ID,""));
            paramsEspay.put(WebParams.USER_ID, user_id);
            paramsEspay.put(WebParams.RC_UUID, uuid.toString());
            paramsEspay.put(WebParams.RC_DTIME, dtime);
            paramsEspay.put(WebParams.SIGNATURE, signatureEspay);

            String webserviceAsk = MyApiClient.getWebserviceName(MyApiClient.LINK_REPORT_MONEY_REQUEST);
            String signatureAsk = MyApiClient.getSignature(uuid, dtime, webserviceAsk, MyApiClient.COMM_ID + user_id, access_key);

            RequestParams paramsAsk = new RequestParams();
            paramsAsk.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            paramsAsk.put(WebParams.COMM_ID,MyApiClient.COMM_ID);
            paramsAsk.put(WebParams.PAGE, _page);
            paramsAsk.put(WebParams.DATE_FROM, _date_from);
            paramsAsk.put(WebParams.DATE_TO, _date_to);
            paramsAsk.put(WebParams.USER_ID, user_id);
            paramsAsk.put(WebParams.RC_UUID, uuid.toString());
            paramsAsk.put(WebParams.RC_DTIME, dtime);
            paramsAsk.put(WebParams.SIGNATURE, signatureAsk);

            String webserviceFee = MyApiClient.getWebserviceName(MyApiClient.LINK_REPORT_COMM_FEE);
            String signatureFee = MyApiClient.getSignature(uuid, dtime, webserviceAsk, MyApiClient.COMM_ID + user_id, access_key);

            RequestParams paramsFee = new RequestParams();
            paramsFee.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            paramsFee.put(WebParams.COMM_ID,MyApiClient.COMM_ID);
            paramsFee.put(WebParams.PAGE, _page);
            paramsFee.put(WebParams.DATE_FROM, _date_from);
            paramsFee.put(WebParams.DATE_TO, _date_to);
            paramsFee.put(WebParams.OFFSET, sp.getString(DefineValue.OFFSET,""));
            paramsFee.put(WebParams.CUST_ID,sp.getString(DefineValue.CUST_ID,""));
            paramsFee.put(WebParams.RC_UUID, uuid.toString());
            paramsFee.put(WebParams.RC_DTIME, dtime);
            paramsFee.put(WebParams.USER_ID, user_id);
            paramsFee.put(WebParams.SIGNATURE, signatureFee);

            JsonHttpResponseHandler deHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if(isAdded()){
                            if(isRefresh != null ){
                                if(isRefresh)
                                    out.dismiss();
                            }

                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if(isRefresh == null) {
                                    mPtrFrame.refreshComplete();
                                    ClearDataAdapter();
                                }
                                else if(isRefresh)
                                    ClearDataAdapter();
                                else {
                                    btn_loadmore.setVisibility(View.VISIBLE);
                                    spining_progress.setVisibility(View.GONE);
                                    spining_progress.setAnimation(null);
                                }

                                if(lv_report.getVisibility() == View.GONE ){
                                    lv_report.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                }

                                JSONArray arrayData = new JSONArray(response.getString(WebParams.REPORT_DATA)) ;
                                JSONObject mObj;
                                if(report_type == REPORT_SCASH){
                                    Timber.d("Isi response transaction report:"+response.toString());
                                    ReportListModel mTempData;
                                    for(int i = 0 ; i <arrayData.length() ; i++){
                                        mObj = arrayData.getJSONObject(i);
                                        mTempData = new ReportListModel(mObj.optString(WebParams.DATE_TIME, ""),
                                                mObj.optString(WebParams.TYPE,""),
                                                mObj.optString(WebParams.CCY_ID,""),
                                                mObj.optString(WebParams.AMOUNT,""),
                                                mObj.optString(WebParams.TRX_ID,""),
                                                mObj.optString(WebParams.DESCRIPTION,""),
                                                mObj.optString(WebParams.REMARK,""),
                                                mObj.optString(WebParams.DETAIL,""),
                                                sp.getString(DefineValue.COMMUNITY_ID,""),
                                                mObj.optString(WebParams.TO_ALIAS));
                                        AddNewData(mTempData);
                                    }
                                }
                                else if(report_type == REPORT_ESPAY){
                                    Timber.d("Isi response Espay report:"+response.toString());
                                    ReportListEspayModel mTempData;
                                    for(int i = 0 ; i <arrayData.length() ; i++){
                                        mObj = arrayData.getJSONObject(i);
                                        mTempData = new ReportListEspayModel(mObj.optString(WebParams.CREATED, ""),
                                                mObj.optString(WebParams.BUSS_SCHEME_NAME,""),
                                                mObj.optString(WebParams.COMM_NAME,""),
                                                mObj.optString(WebParams.CCY_ID,""),
                                                mObj.optString(WebParams.AMOUNT,""),
                                                mObj.optString(WebParams.ADMIN_FEE,""),
                                                mObj.optString(WebParams.DESCRIPTION,""),
                                                mObj.optString(WebParams.REMARK,""),
                                                mObj.optString(WebParams.TX_ID,""),
                                                mObj.optString(WebParams.COMM_ID,""),
                                                mObj.optString(WebParams.BANK_NAME,""),
                                                mObj.optString(WebParams.PRODUCT_NAME,""),
                                                mObj.optString(WebParams.TX_STATUS,"")
                                                );

                                        if(mTempData.getDescription().contains(ITEM_DESC_PLN)||
                                                mTempData.getDescription().contains(ITEM_DESC_LISTRIK)||
                                                mTempData.getDescription().contains(ITEM_DESC_NON)){
                                            mTempData.setType_desc(ITEM_DESC_PLN);
                                        }

                                        if(mTempData.getDescription().contains(ITEM_DESC_BPJS)){
                                            mTempData.setType_desc(ITEM_DESC_BPJS);
                                        }

                                        AddNewData(mTempData);
                                    }
                                }
								else if(report_type == REPORT_ASK){
                                    Timber.d("Isi response ask report:"+response.toString());
                                    ReportAskListModel mTempData;
                                    for(int i = 0 ; i <arrayData.length() ; i++){
                                        mObj = arrayData.getJSONObject(i);
                                        mTempData = new ReportAskListModel(mObj.optString(WebParams.DATE_TIME, ""),
                                                mObj.optString(WebParams.TYPE,""),
                                                mObj.optString(WebParams.CCY_ID,""),
                                                mObj.optString(WebParams.AMOUNT,""),
                                                mObj.optString(WebParams.TRX_ID,""),
                                                mObj.optString(WebParams.DESCRIPTION,""),
                                                mObj.optString(WebParams.REMARK,""),
                                                mObj.optString(WebParams.DETAIL,""),
                                                mObj.optString(WebParams.TO_ALIAS,""),
                                                mObj.optString(WebParams.STATUS,""),
                                                mObj.optString(WebParams.REASON,""));
                                        AddNewData(mTempData);
                                    }
                                }
                                else if(report_type == REPORT_FEE){
                                    Timber.d("Isi response report comm fee:"+response.toString());
                                    ReportListCommFeeModel mTempData;
                                    for(int i = 0 ; i <arrayData.length() ; i++){
                                        mObj = arrayData.getJSONObject(i);
                                        mTempData = new ReportListCommFeeModel(mObj.optString(WebParams.CREATED, ""),
                                                mObj.optString(WebParams.BBS_NAME,""),
                                                mObj.optString(WebParams.COMM_NAME,""),
                                                mObj.optString(WebParams.CCY_ID,""),
                                                mObj.optString(WebParams.AMOUNT,""),
                                                mObj.optString(WebParams.STATUS,""));
                                        AddNewData(mTempData);
                                    }
                                }

                                int _page = response.optInt(WebParams.NEXT,0);
                                if(_page!=0){
                                    page++;
                                    setLoadMore(false);
                                    setLoadMore(true);
                                }
                                else {
                                    setLoadMore(false);
                                }
                                NotifyDataChange();

                                if(isRefresh == null || isRefresh) {
                                    lv_report.setSelection(0);
                                    lv_report.smoothScrollToPosition(0);
                                    lv_report.setSelectionAfterHeaderView();
                                }

                                bak_date_from = (Calendar) date_from.clone();
                                bak_date_to = (Calendar) date_to.clone();

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                Timber.d("isi response autologout:"+response.toString());
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginMain(getActivity(),message);
                            }
                            else if(code.equals("0003")) {
                                bak_date_from = (Calendar) date_from.clone();
                                bak_date_to = (Calendar) date_to.clone();
                                mPtrFrame.refreshComplete();
                                setLoadMore(false);
                                lv_report.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                ClearDataAdapter();
                                NotifyDataChange();
                            } else {
                                date_from = (Calendar) bak_date_from.clone();
                                String dedate = getString(R.string.from)+" :\n"+date_from.get(Calendar.DAY_OF_MONTH)+"-"+(date_from.get(Calendar.MONTH)+1)+"-"+date_from.get(Calendar.YEAR);
                                tv_date_from.setText(dedate);
                                date_to = (Calendar) bak_date_to.clone();
                                dedate = getString(R.string.to)+" :\n"+date_to.get(Calendar.DAY_OF_MONTH)+"-"+(date_to.get(Calendar.MONTH)+1)+"-"+date_to.get(Calendar.YEAR);
                                tv_date_to.setText(dedate);
                                filter_btn.setChecked(false);
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }

                            filter_btn.setOnClickListener(filterBtnListener);
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
                    if(isAdded()) {
                        if (MyApiClient.PROD_FAILURE_FLAG)
                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                        bak_date_from = (Calendar) date_from.clone();
                        bak_date_to = (Calendar) date_to.clone();
                        mPtrFrame.refreshComplete();
                        setLoadMore(false);
                        lv_report.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                        ClearDataAdapter();
                        NotifyDataChange();
                    }
                    Timber.w("Error Koneksi get data report report:"+throwable.toString());
                }

            };

            if(report_type == REPORT_SCASH) {
                Timber.d("Webservice:"+webserviceScash);
                Timber.d("Isi params report scash:" + paramsScash.toString());
                MyApiClient.sentGetTrxReport(getActivity(), paramsScash, deHandler);
            }
            else if(report_type == REPORT_ESPAY) {
                Timber.d("Webservice:"+webserviceEspay);
                Timber.d("Isi params report espay:"+paramsEspay.toString());
                MyApiClient.sentReportEspay(getActivity(), paramsEspay, deHandler);
            }
            else if(report_type == REPORT_ASK) {
                Timber.d("Webservice:"+webserviceAsk);
                Timber.d("Isi params report ask:"+paramsAsk.toString());
                MyApiClient.sentReportAsk(getActivity(), paramsAsk, deHandler);
            }
            else if(report_type == REPORT_FEE) {
                Timber.d("Webservice:"+webserviceFee);
                Timber.d("Isi params report comm fee:"+paramsFee.toString());
                MyApiClient.sentReportCommFee(getActivity(), paramsFee, deHandler);
            }
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void ClearDataAdapter(){
        if(report_type == REPORT_SCASH){
            ReportListAdapter ya = (ReportListAdapter) getUniAdapter();
            ya.clear();
        }
        else if(report_type == REPORT_ESPAY){
            ReportListEspayAdapter ya = (ReportListEspayAdapter) getUniAdapter();
            ya.clear();
        }
        else if(report_type == REPORT_ASK) {
            ReportAskListAdapter ya = (ReportAskListAdapter) getUniAdapter();
            ya.clear();
        }
    }

    private void NotifyDataChange(){
        if(report_type == REPORT_SCASH){
            ReportListAdapter ya = (ReportListAdapter) getUniAdapter();
            ya.notifyDataSetChanged();
        }
        else if(report_type == REPORT_ESPAY){
            ReportListEspayAdapter ya = (ReportListEspayAdapter) getUniAdapter();
            ya.notifyDataSetChanged();
        }
        else if(report_type == REPORT_ASK){
            ReportAskListAdapter ya = (ReportAskListAdapter) getUniAdapter();
            ya.notifyDataSetChanged();
        }
        else if(report_type == REPORT_FEE){
            ReportCommFeeAdapter ya = (ReportCommFeeAdapter) getUniAdapter();
            ya.notifyDataSetChanged();
        }
    }

    private void AddNewData(Object ok){
        if(report_type == REPORT_SCASH){
            ReportListAdapter ya = (ReportListAdapter) getUniAdapter();
            ReportListModel obj = (ReportListModel) ok;
            ya.add(obj);
        }
        else if(report_type == REPORT_ESPAY){
            ReportListEspayAdapter ya = (ReportListEspayAdapter) getUniAdapter();
            ReportListEspayModel obj = (ReportListEspayModel) ok;
            ya.add(obj);
        }
        else if(report_type == REPORT_ASK){
            ReportAskListAdapter ya = (ReportAskListAdapter) getUniAdapter();
            ReportAskListModel obj = (ReportAskListModel) ok;
            ya.add(obj);
        }
        else if(report_type == REPORT_FEE){
            ReportCommFeeAdapter ya = (ReportCommFeeAdapter) getUniAdapter();
            ReportListCommFeeModel obj = (ReportListCommFeeModel) ok;
            ya.add(obj);
        }
    }


    private ListView.OnItemClickListener reportItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(report_type == REPORT_ASK) {
                ReportAskListModel mobj = (ReportAskListModel) getListView().getAdapter().getItem(position);
                showReportAskDialog(mobj.getDatetime(), mobj.getDetail(), mobj.getTrxId(), mobj.getType(), mobj.getDescription(),
                        mobj.getAmount(), mobj.getCcyID(), mobj.getRemark(), mobj.getAlias(), mobj.getStatus(), mobj.getReason());
            }
            else {
                getTrxStatus(getListView().getAdapter().getItem(position));
                lv_report.setOnItemClickListener(null);
            }
        }
    };


    private void getTrxStatus(final Object _object){
        try{
            out = DefinedDialog.CreateProgressDialog(getActivity(), null);
            out.show();

            String user_id = sp.getString(DefineValue.USERID_PHONE,"");
            String access_key = sp.getString(DefineValue.ACCESS_KEY,"");

            UUID uuid = MyApiClient.getUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();
            String webservice = MyApiClient.getWebserviceName(MyApiClient.LINK_GET_TRX_STATUS);
            Timber.d("Webservice:"+webservice);

            String _tx_id = "";
            String _comm_id ="";
            RequestParams params = new RequestParams();

            if(report_type == REPORT_SCASH) {
                ReportListModel mobj = (ReportListModel) _object;
                _tx_id = mobj.getTrxId();
                _comm_id = mobj.getCommId();
                params.put(WebParams.TX_TYPE, DefineValue.EMO);
            }
            else if(report_type == REPORT_ESPAY) {
                ReportListEspayModel mobj = (ReportListEspayModel) _object;
                _tx_id = mobj.getTx_id();
                _comm_id = mobj.getComm_id();
                params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
                if(mobj.getType_desc().equals(ITEM_DESC_PLN)||mobj.getType_desc().equals(ITEM_DESC_BPJS)){
                    params.put(WebParams.IS_DETAIL, DefineValue.STRING_YES);
                }
            }


            String signature = MyApiClient.getSignature(uuid, dtime, webservice, _comm_id + user_id, access_key);

            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.COMM_ID, _comm_id);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE,""));
            params.put(WebParams.RC_UUID, uuid.toString());
            params.put(WebParams.RC_DTIME, dtime);
            params.put(WebParams.SIGNATURE, signature);
            Timber.d("isi params sent get Trx Status:"+params.toString());

            MyApiClient.sentGetTRXStatus(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        out.dismiss();

                        Timber.d("isi response sent get Trx Status:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            ShowDialog(_object,response.optString(WebParams.TX_STATUS, ""),response.optString(WebParams.TX_REMARK,""),response);

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            String msg = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        }

                        lv_report.setOnItemClickListener(reportItemListener);

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

                    if(out.isShowing())
                        out.dismiss();
                    lv_report.setOnItemClickListener(reportItemListener);
                    Timber.w("Error Koneksi trx stat report:"+throwable.toString());

                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void ShowDialog(Object _object, String txstatus, String txremark, JSONObject response){
        if(report_type == REPORT_SCASH) {
            ReportListModel mobj = (ReportListModel) _object;
            showReportBillerDialog(mobj.getDatetime(), mobj.getDetail(), mobj.getTrxId(), mobj.getType(), mobj.getDescription(),
                    mobj.getAmount(), mobj.getCcyID(), mobj.getRemark(), txstatus , txremark, mobj.getAlias());
        }
        else if(report_type == REPORT_ESPAY) {
            ReportListEspayModel mobj = (ReportListEspayModel) _object;
            showReportEspayDialog(mobj.getDatetime(), mobj.getTx_id(), mobj.getBuss_scheme_name(), mobj.getComm_name(), mobj.getAmount(),
                    mobj.getAdmin_fee(), mobj.getCcy_id(), mobj.getDescription(), mobj.getRemark(),txstatus,txremark, mobj.getBank_name(),
                    mobj.getProduct_name(), mobj.getType_desc(),response);
        }
    }

    private void slidingView(final View vFrom){

        CollapseExpandAnimation anim ;

        if (vFrom.getVisibility() == View.VISIBLE) {
            anim = new CollapseExpandAnimation(vFrom, 250, CollapseExpandAnimation.COLLAPSE) ;
            height = anim.getHeight();
            vFrom.startAnimation(anim);
        } else {
            anim = new CollapseExpandAnimation(vFrom, 250, CollapseExpandAnimation.EXPAND) ;
            anim.setHeight(height);
            vFrom.startAnimation(anim);
        }
    }

    private void showReportAskDialog(String date,String detail, String txId, String type,String description,
                                        String amount, String ccyId, String remark, String alias, String status, String reason) {
        Bundle args = new Bundle();
        args.putString(DefineValue.DATE_TIME,date);
        args.putString(DefineValue.TX_ID,txId);
        args.putString(DefineValue.DETAIL,detail);
        args.putString(DefineValue.TYPE,type);
        args.putString(DefineValue.REMARK, remark);
        args.putString(DefineValue.DESCRIPTION, description);
        args.putString(DefineValue.AMOUNT, ccyId + " " + CurrencyFormat.format(amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.REQUEST);
        args.putString(DefineValue.CONTACT_ALIAS, alias);
        args.putString(DefineValue.STATUS, status);
        args.putString(DefineValue.REASON, reason);

        ReportBillerDialog dialog = new ReportBillerDialog();
        dialog.setArguments(args);
        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(),ReportBillerDialog.TAG);
    }

    private void showReportBillerDialog(String date,String detail, String txId, String type,String description,
                                        String amount, String ccyId, String remark, String txStatus, String txRemark, String alias) {
        Bundle args = new Bundle();
        args.putString(DefineValue.DATE_TIME,date);
        args.putString(DefineValue.TX_ID,txId);
        args.putString(DefineValue.DETAIL,detail);
        args.putString(DefineValue.TYPE,type);
        args.putString(DefineValue.REMARK, remark);
        args.putString(DefineValue.DESCRIPTION, description);
        args.putString(DefineValue.AMOUNT, ccyId + " " + CurrencyFormat.format(amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TRANSACTION);
        args.putString(DefineValue.CONTACT_ALIAS, alias);

        showBillerDialog(args, txStatus, txRemark);
    }

    private void showReportEspayDialog(String date, String txId, String buss_scheme_name,String comm_name,
                                       String amount,String fee, String ccy_id,String description, String remark,
                                       String txStatus, String txRemark, String bankName, String productName,
                                       String type_desc,
                                       JSONObject response ){
        Bundle args = new Bundle();
        args.putString(DefineValue.DATE_TIME,DateTimeFormat.formatToID(date));
        args.putString(DefineValue.TX_ID,txId);
        args.putString(DefineValue.BUSS_SCHEME_NAME,buss_scheme_name);
        args.putString(DefineValue.COMMUNITY_NAME,comm_name);
        args.putString(DefineValue.AMOUNT,ccy_id+" "+CurrencyFormat.format(amount));
        args.putString(DefineValue.FEE,ccy_id+" "+CurrencyFormat.format(fee));

        Double total_amount = Double.parseDouble(amount) + Double.parseDouble(fee);
        args.putString(DefineValue.TOTAL_AMOUNT,ccy_id+" "+CurrencyFormat.format(total_amount));

        args.putString(DefineValue.DESCRIPTION, description);
        args.putString(DefineValue.REMARK, remark);
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TRANSACTION_ESPAY);
        args.putString(DefineValue.BANK_NAME, bankName);
        args.putString(DefineValue.PRODUCT_NAME, productName);

        if(type_desc.equals(ITEM_DESC_PLN)||type_desc.equals(ITEM_DESC_BPJS)){
            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
            if(type_desc.equals(ITEM_DESC_BPJS))
                args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_BPJS);
            try {
                args.putString(DefineValue.DETAILS_BILLER,response.getString(WebParams.DETAIL));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        showBillerDialog(args, txStatus, txRemark);

    }

    private void showBillerDialog(Bundle args, String txStatus, String txRemark){
        ReportBillerDialog dialog = new ReportBillerDialog();

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)){
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        }else if(txStatus.equals(DefineValue.ONRECONCILED)){
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }else if(txStatus.equals(DefineValue.SUSPECT)){
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        }
        else if(!txStatus.equals(DefineValue.FAILED)){
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction)+" "+txStatus);
        }
        else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if(!txStat)args.putString(DefineValue.TRX_REMARK, txRemark);

        dialog.setArguments(args);
        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(),ReportBillerDialog.TAG);

    }


    private void setLoadMore(boolean isLoading)
    {
        if (isLoading) {
            lv_report.addFooterView(footerLayout,null,false);
        }
        else {
            lv_report.removeFooterView(footerLayout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_filter:
                slidingView(layout_filter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View getV() {
        return v;
    }

    private void setV(View v) {
        this.v = v;
    }

    @Override
    public void onOkButton() {

    }

    private ListAdapter getUniAdapter() {
        return UniAdapter;
    }

    private void setUniAdapter(ListAdapter adapter) {
        this.UniAdapter = adapter;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}