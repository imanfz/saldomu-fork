package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.SummaryReportFeeModel;
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
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.GetReportDataModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.ReportDataModel;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import timber.log.Timber;

/*
  Created by Administrator on 5/19/2015.
 */
public class FragReport extends ListFragment implements ReportBillerDialog.OnDialogOkCallback {

    public static int REPORT_ASK = 0x0299395;
    public static int REPORT_SCASH = 0x0299394;
    public static int REPORT_ESPAY = 0x0299393;
    public static int REPORT_FEE = 0x0299396;
    private final String DATEFROM = "tagFrom";
    private final String DATETO = "tagTo";
    final private String ITEM_DESC_LISTRIK = "Listrik";
    final private String ITEM_DESC_PLN = "Voucher Token Listrik";
    final private String ITEM_DESC_NON = "PLN Non-Taglis";
    final private String ITEM_DESC_BPJS = "BPJS";

    private TextView tv_date_from, tv_date_to, sumTotalTrx, sumRelAmount, sumRelTrx, sumUnrelTrx, sumUnrelAmount, tv_txId;

    private View v;
    private LinearLayout layout_filter, layout_summary;
    private int height;
    private String OrifromDate, comm_id_tagih;
    private String OritoDate;
    private ListView lv_report;
    private ViewGroup footerLayout;
    private ToggleButton filter_btn;
    private ImageView spining_progress;
    private MaterialRippleLayout btn_loadmore;

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
    private Boolean isReport = false;
    private Boolean isMemberCTA = false;

    SummaryReportFeeModel SummaryFeeModel;

    GetReportDataModel reportListModel;
    List<ReportDataModel> reportData;

    ReportListAdapter reportListAdapter;
    ReportListEspayAdapter reportListEspayAdapter;
    ReportAskListAdapter reportAskListAdapter;
    ReportCommFeeAdapter reportCommFeeAdapter;

    private Gson gson;

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

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        super.onActivityCreated(savedInstanceState);
        layout_filter = getV().findViewById(R.id.layout_filter);
        lv_report = getV().findViewById(android.R.id.list);
        tv_date_from = getV().findViewById(R.id.filter_date_from);
        tv_date_to = getV().findViewById(R.id.filter_date_to);
        layout_filter.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        height = layout_filter.getMeasuredHeight();
        filter_btn = getV().findViewById(R.id.filter_toggle_btn);
        footerLayout = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.footer_loadmore, lv_report, false);
        footerLayout.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        spining_progress = footerLayout.findViewById(R.id.image_spinning_wheel);
        btn_loadmore = footerLayout.findViewById(R.id.btn_loadmore);
        emptyLayout = getV().findViewById(R.id.empty_layout);
        emptyLayout.setVisibility(View.GONE);
        btn_refresh = emptyLayout.findViewById(R.id.btnRefresh);
        layout_summary = getV().findViewById(R.id.table_summary);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        OrifromDate = DateTimeFormat.getCurrentDateMinus(6);
        OritoDate = DateTimeFormat.getCurrentDate();
        page = 1;

        SummaryFeeModel = new SummaryReportFeeModel();
        reportListModel = new GetReportDataModel();
        reportData = new ArrayList<>();

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

        String dedate = getString(R.string.from) + " :\n" + date_from.get(Calendar.DAY_OF_MONTH) + "-" + (date_from.get(Calendar.MONTH) + 1) + "-" + date_from.get(Calendar.YEAR);
        tv_date_from.setText(dedate);
        dedate = getString(R.string.to) + " :\n" + date_to.get(Calendar.DAY_OF_MONTH) + "-" + (date_to.get(Calendar.MONTH) + 1) + "-" + date_to.get(Calendar.YEAR);
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
        if (report_type == REPORT_SCASH) {
            reportListAdapter = new ReportListAdapter(getActivity(), R.layout.list_transaction_report_item, reportData);
            lv_report.setAdapter(reportListAdapter);
        } else if (report_type == REPORT_ESPAY) {
            reportListEspayAdapter = new ReportListEspayAdapter(getActivity(), R.layout.list_transaction_report_espay_item, reportData);
            lv_report.setAdapter(reportListEspayAdapter);
        } else if (report_type == REPORT_ASK) {
            reportAskListAdapter = new ReportAskListAdapter(getActivity(), R.layout.list_request_report_item, reportData);
            lv_report.setAdapter(reportAskListAdapter);
        } else if (report_type == REPORT_FEE) {
            reportCommFeeAdapter = new ReportCommFeeAdapter(getActivity(), R.layout.list_report_comm_fee, reportData);
            sumTotalTrx = getV().findViewById(R.id.tv_total_transaction);
            sumRelAmount = getV().findViewById(R.id.tv_amount_released);
            sumRelTrx = getV().findViewById(R.id.tv_tx_released);
            sumUnrelAmount = getV().findViewById(R.id.tv_amount_unreleased);
            sumUnrelTrx = getV().findViewById(R.id.tv_tx_unreleased);
            lv_report.setAdapter(reportCommFeeAdapter);
        }
        setLoadMore(false);
        if (getUniAdapter() == null || !getUniAdapter().isEmpty()) {
            ListAdapter tempAdap = ((HeaderViewListAdapter) lv_report.getAdapter()).getWrappedAdapter();
            if (tempAdap != null)
                setUniAdapter(tempAdap);
            else setUniAdapter(lv_report.getAdapter());
        }
        if (report_type == REPORT_ESPAY || report_type == REPORT_SCASH) {
            lv_report.setOnItemClickListener(reportItemListener);
        }

        mPtrFrame = getV().findViewById(R.id.rotate_header_list_view_frame);

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

    private Calendar StringToCal(String src) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", new Locale("id", "INDONESIA"));
        Calendar tempCalendar = Calendar.getInstance();

        try {
            tempCalendar.setTime(format.parse(src));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tempCalendar;
    }

    private String CalToString(Calendar src) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", new Locale("id", "INDONESIA"));
        return format.format(src.getTime());
    }

    private ToggleButton.OnClickListener filterBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean on = ((ToggleButton) v).isChecked();
            filter_btn.setOnClickListener(null);
            if (on) {
                getDataReport(0, CalToString(date_from), CalToString(date_to), true);
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

            if (view.getTag().equals(DATEFROM)) {
                dedate = getString(R.string.from) + " :\n" + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                bak_date_from = (Calendar) date_from.clone();
                date_from.set(year, monthOfYear, dayOfMonth);
                tv_date_from.setText(dedate);
            } else {
                dedate = getString(R.string.to) + " :\n" + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                bak_date_to = (Calendar) date_to.clone();
                date_to.set(year, monthOfYear, dayOfMonth);
                tv_date_to.setText(dedate);
            }
        }
    };

    private void getDataReport(int _page, final String _date_from, String _date_to, final Boolean isRefresh) {
        try {
            if (isRefresh == null) {
                Timber.wtf("masuk ptr");
            } else if (isRefresh) {
                Timber.wtf("masuk refresh");
                out = DefinedDialog.CreateProgressDialog(getActivity(), null);
                out.show();
                mPtrFrame.setEnabled(true);
                mPtrFrame.setVisibility(View.VISIBLE);
            } else {
                Timber.wtf("masuk load more");
                btn_loadmore.setVisibility(View.GONE);
                spining_progress.setVisibility(View.VISIBLE);
                spining_progress.startAnimation(frameAnimation);
            }

            String user_id = sp.getString(DefineValue.USERID_PHONE, "");
            String member_id = sp.getString(DefineValue.MEMBER_ID, "");
            String url = "", signature = "";

            if (report_type == REPORT_SCASH) {
                url = MyApiClient.LINK_TRANSACTION_REPORT;
                signature = member_id;
            } else if (report_type == REPORT_ESPAY) {
                url = MyApiClient.LINK_REPORT_ESPAY;
            } else if (report_type == REPORT_ASK) {
                url = MyApiClient.LINK_REPORT_MONEY_REQUEST;
            } else if (report_type == REPORT_FEE) {
                url = MyApiClient.LINK_REPORT_COMM_FEE;
            }

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(url, signature);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID, ""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PAGE, _page);
            params.put(WebParams.DATE_FROM, _date_from);
            params.put(WebParams.DATE_TO, _date_to);
            params.put(WebParams.USER_ID, user_id);

            if (report_type == REPORT_SCASH) {
                params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
            } else if (report_type == REPORT_ESPAY) {
                params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
            } else if (report_type == REPORT_ASK) {
            } else if (report_type == REPORT_FEE) {
                params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
                params.put(WebParams.OFFSET, sp.getString(DefineValue.OFFSET, ""));
            }

            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {

                                String code;
                                JSONObject temp = new JSONObject(getGson().toJson(object));

                                if (temp.optString("report_data", "").equals("")){
                                    code = "0003";
                                }else {

                                    reportListModel = getGson().fromJson(object, GetReportDataModel.class);

                                    code = reportListModel.getError_code();

                                    reportData.clear();

                                    reportData.addAll(reportListModel.getReport_data());
                                }

                                if (isAdded()) {
                                    if (isRefresh != null) {
                                        if (isRefresh)
                                            out.dismiss();
                                    }


                                    if (code.equals(WebParams.SUCCESS_CODE)) {
                                        if (isRefresh == null) {
                                            mPtrFrame.refreshComplete();
                                        }
//                                    else if (isRefresh)
                                        else {
                                            btn_loadmore.setVisibility(View.VISIBLE);
                                            spining_progress.setVisibility(View.GONE);
                                            spining_progress.setAnimation(null);
                                        }

                                        if (lv_report.getVisibility() == View.GONE) {
                                            lv_report.setVisibility(View.VISIBLE);
                                            emptyLayout.setVisibility(View.GONE);
                                        }

                                        if (report_type == REPORT_FEE){
                                            SummaryFeeModel = new SummaryReportFeeModel();
                                            SummaryFeeModel.setTotal_transaction(reportListModel.getReport_data().size());
                                            for (ReportDataModel model: reportListModel.getReport_data()) {
                                                getSummaryFee(SummaryFeeModel, model);
                                            }

                                            setSummarytoView(SummaryFeeModel);
                                        }

                                        int _page = Integer.valueOf(reportListModel.getNext());
                                        if (_page != 0) {
                                            page++;
                                            setLoadMore(false);
                                            setLoadMore(true);
                                        } else {
                                            setLoadMore(false);
                                        }
                                        NotifyDataChange();

                                        if (isRefresh == null || isRefresh) {
                                            lv_report.setSelection(0);
                                            lv_report.smoothScrollToPosition(0);
                                            lv_report.setSelectionAfterHeaderView();
                                        }

                                        bak_date_from = (Calendar) date_from.clone();
                                        bak_date_to = (Calendar) date_to.clone();

                                    } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                        String message = reportListModel.getError_message();
                                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                                        test.showDialoginMain(getActivity(), message);
                                    } else if (code.equals("0003")) {
                                        bak_date_from = (Calendar) date_from.clone();
                                        bak_date_to = (Calendar) date_to.clone();
                                        mPtrFrame.refreshComplete();
                                        setLoadMore(false);
                                        lv_report.setVisibility(View.GONE);
                                        emptyLayout.setVisibility(View.VISIBLE);
                                        NotifyDataChange();
                                    } else {
                                        date_from = (Calendar) bak_date_from.clone();
                                        String dedate = getString(R.string.from) + " :\n" + date_from.get(Calendar.DAY_OF_MONTH) + "-" + (date_from.get(Calendar.MONTH) + 1) + "-" + date_from.get(Calendar.YEAR);
                                        tv_date_from.setText(dedate);
                                        date_to = (Calendar) bak_date_to.clone();
                                        dedate = getString(R.string.to) + " :\n" + date_to.get(Calendar.DAY_OF_MONTH) + "-" + (date_to.get(Calendar.MONTH) + 1) + "-" + date_to.get(Calendar.YEAR);
                                        tv_date_to.setText(dedate);
                                        filter_btn.setChecked(false);
                                        code = reportListModel.getError_message();

                                        Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();

                                        bak_date_from = (Calendar) date_from.clone();
                                        bak_date_to = (Calendar) date_to.clone();
                                        mPtrFrame.refreshComplete();
                                        setLoadMore(false);
                                        lv_report.setVisibility(View.GONE);
                                        emptyLayout.setVisibility(View.VISIBLE);
                                        NotifyDataChange();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (out != null && out.isShowing())
                                out.dismiss();

                            filter_btn.setOnClickListener(filterBtnListener);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void getSummaryFee(SummaryReportFeeModel obj, ReportDataModel model) {
        String a = model.getAmount();
        if (model.getStatus().equalsIgnoreCase("Released")) {
            obj.setReleased_trx(obj.getReleased_trx() + 1);
            obj.setReleased_amount(obj.getReleased_amount() + Integer.valueOf(a));
        } else if (model.getStatus().equalsIgnoreCase("Unreleased")) {
            obj.setUnreleased_trx(obj.getUnreleased_trx() + 1);
            obj.setUnreleased_amount(obj.getUnreleased_amount() + Integer.valueOf(a));
        }
    }

    private void setSummarytoView(SummaryReportFeeModel obj) {
        sumTotalTrx.setText(String.valueOf(obj.getTotal_transaction()));
        sumUnrelTrx.setText(String.valueOf(obj.getUnreleased_trx()));
        sumUnrelAmount.setText(CurrencyFormat.format(String.valueOf(obj.getUnreleased_amount())));
        sumRelTrx.setText(String.valueOf(obj.getReleased_trx()));
        sumRelAmount.setText(CurrencyFormat.format(String.valueOf(obj.getReleased_amount())));

        layout_summary.setVisibility(View.VISIBLE);
    }

    private void NotifyDataChange() {
        if (report_type == REPORT_SCASH) {
            reportListAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_ESPAY) {
            reportListEspayAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_ASK) {
            reportAskListAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_FEE) {
            reportCommFeeAdapter.notifyDataSetChanged();
        }

    }

    private ListView.OnItemClickListener reportItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            getTrxStatus(reportData.get(position));
        }
    };


    private void getTrxStatus(final ReportDataModel _object) {
        try {
            out = DefinedDialog.CreateProgressDialog(getActivity(), null);
            out.show();

            String _tx_id = "";
            String _comm_id = "";
            String tx_type = DefineValue.EMO;
            boolean isdetail = false;
            boolean call = true;


            if (report_type == REPORT_SCASH) {
                _tx_id = _object.getTrx_id();
                _comm_id = sp.getString(DefineValue.COMMUNITY_ID, "");
                if (_object.getBuss_scheme_code().equalsIgnoreCase("RF") ||
                        _object.getBuss_scheme_code().equalsIgnoreCase("RA")) {
                    call = false;
                }
//                call =
            } else if (report_type == REPORT_ESPAY) {
                _tx_id = _object.getTx_id();
                _comm_id = _object.getComm_id();
                tx_type = DefineValue.ESPAY;
//                if (smobj.getType_desc().equals(ITEM_DESC_PLN) || mobj.getType_desc().equals(ITEM_DESC_BPJS)) {
////                    idetail = true;
//                }
            }

            String extraSignature = _tx_id + _comm_id;

            if (call) {

                HashMap<String, Object> params = RetrofitService.getInstance()
                        .getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

                params.put(WebParams.TX_ID, _tx_id);
                params.put(WebParams.COMM_ID, _comm_id);
                params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
                params.put(WebParams.TX_TYPE, tx_type);
                params.put(WebParams.IS_DETAIL, DefineValue.STRING_YES);
                Timber.d("isi params sent get Trx Status:" + params.toString());

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {
                                GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                                String code = model.getError_code();
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    ShowDialog(_object, model);

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginMain(getActivity(), message);
                                } else {
                                    String msg = model.getError_message();

                                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                if (out.isShowing())
                                    out.dismiss();
                            }
                        });
            }
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }


    private void ShowDialog(ReportDataModel _object, GetTrxStatusReportModel response) {
        if (report_type == REPORT_SCASH) {
            String ccyId = response.getCcy_id();
            if (_object.getBuss_scheme_code().equals("OC")) {

                showReportCashOutBankDialog(sp.getString(DefineValue.USER_NAME, ""),
                        DateTimeFormat.getCurrentDateTime(),
                        sp.getString(DefineValue.USERID_PHONE, ""), ccyId,
                        response);

            } else if (_object.getBuss_scheme_code().equals("OR") || _object.getBuss_scheme_code().equals("ORP")) {
                showReportBillerDialog(_object, response, ccyId);
            } else if (_object.getBuss_scheme_code().equals("IR")) {
                showReportBillerDialog(_object, response, ccyId);
            }
        } else if (report_type == REPORT_ESPAY) {
            if (_object.getBuss_scheme_code().equals("BIL")) {
                showReportEspayBillerDialog(sp.getString(DefineValue.USER_NAME, ""), response);
//
            } else if (_object.getBuss_scheme_code().equals("CTA")) {
                if (sp.getString(DefineValue.USERID_PHONE, "").equals(response.getMember_phone())) {
                    showReportCTADialog(response);
                } else {
                    isMemberCTA = true;
                    showReportCTADialog(response);
                }

            } else if (_object.getBuss_scheme_code().equals("ATC")) {
                Timber.d(sp.getString(DefineValue.USERID_PHONE, "") + "user_id");
                if (sp.getString(DefineValue.USERID_PHONE, "").equals(response.getMember_phone())) {
                    showReportATCAgentDialog(response);
                } else {
                    isReport = true;
                    showReportATCMemberDialog(response);
                }
            } else if (_object.getBuss_scheme_code().equals("EMO") || _object.getBuss_scheme_code().equalsIgnoreCase("TOP")) {
                showReportEMODialog(response);
            } else if (_object.getBuss_scheme_code().equals("BDK")) {
                showReportBDKDialog(response);
            }else if (_object.getBuss_scheme_code().equals("DGI")) {
                showReportCollectorDialog(response);
            }
        }
    }

    private void slidingView(final View vFrom) {

        CollapseExpandAnimation anim;

        if (vFrom.getVisibility() == View.VISIBLE) {
            anim = new CollapseExpandAnimation(vFrom, 250, CollapseExpandAnimation.COLLAPSE);
            height = anim.getHeight();
            vFrom.startAnimation(anim);
        } else {
            anim = new CollapseExpandAnimation(vFrom, 250, CollapseExpandAnimation.EXPAND);
            anim.setHeight(height);
            vFrom.startAnimation(anim);
        }
    }

    private void showReportCashOutBankDialog(String _name, String _date, String _userId, String ccyid, GetTrxStatusReportModel model) {

        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, _name);
        args.putString(DefineValue.DATE_TIME, _date);
        args.putString(DefineValue.USERID_PHONE, _userId);
        args.putString(DefineValue.TX_ID, model.getTx_id());
        args.putString(DefineValue.BANK_NAME, model.getPayment_bank());
        args.putString(DefineValue.ACCOUNT_NUMBER, model.getPayment_phone());
        args.putString(DefineValue.ACCT_NAME, model.getPayment_name());
        args.putString(DefineValue.NOMINAL, ccyid + " " + CurrencyFormat.format(model.getTx_amount()));
        args.putString(DefineValue.FEE, ccyid + " " + CurrencyFormat.format(model.getAdmin_fee()));
        args.putString(DefineValue.TOTAL_AMOUNT, ccyid + " " + CurrencyFormat.format(model.getTotal_amount()));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.CASHOUT);
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.getBuss_scheme_name());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportATCMemberDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);

        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

        Boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putString(DefineValue.OTP_MEMBER, response.getOtp_member());
        args.putString(DefineValue.MEMBER_PHONE, response.getMember_phone());
        args.putBoolean(DefineValue.IS_REPORT, isReport);

        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());
        args.putString(DefineValue.MEMBER_SHOP_NAME, response.getMember_shop_name());
        args.putString(DefineValue.MEMBER_SHOP_NO, response.getMember_shop_phone());

        args.putString(DefineValue.MEMBER_NAME, response.getMember_name());
        args.putString(DefineValue.SOURCE_ACCT, response.getSource_bank_name());
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.getSource_acct_name());
        args.putString(DefineValue.BANK_BENEF, response.getBenef_bank_name());
        args.putString(DefineValue.NO_BENEF, response.getBenef_acct_no());
        args.putString(DefineValue.NAME_BENEF, response.getBenef_acct_name());
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.getMember_shop_phone());
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportEMODialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));

        double dAmount = Double.valueOf(response.getTx_amount());
        double dFee = Double.valueOf(response.getAdmin_fee());
        double total_amount = dAmount + dFee;

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

        Boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.COMMUNITY_CODE, response.getComm_code());
        args.putString(DefineValue.MEMBER_CODE, response.getMember_code());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportBDKDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);

        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));

        double dAmount = Double.valueOf(response.getTx_amount());
        double dFee = Double.valueOf(response.getAdmin_fee());
        double total_amount = dAmount + dFee;

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

        Boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.COMMUNITY_CODE, response.getComm_code());
        args.putString(DefineValue.MEMBER_CODE, response.getMember_code());
        args.putString(DefineValue.DENOM_DETAIL, getGson().toJson(response.getDenom_detail()));
        args.putString(DefineValue.ORDER_ID, response.getOrder_id());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportATCAgentDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

        Boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());
        args.putString(DefineValue.MEMBER_SHOP_NAME, response.getMember_shop_name());
        args.putString(DefineValue.MEMBER_SHOP_NO, response.getMember_shop_phone());


        args.putString(DefineValue.MEMBER_NAME, response.getMember_name());
        args.putString(DefineValue.SOURCE_ACCT, response.getSource_bank_name());
        args.putString(DefineValue.SOURCE_ACCT_NO, response.getSource_acct_no());
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.getSource_acct_name());
        args.putString(DefineValue.BANK_BENEF, response.getBenef_bank_name());
        args.putString(DefineValue.NO_BENEF, response.getBenef_acct_no());
        args.putString(DefineValue.NAME_BENEF, response.getBenef_acct_name());
        args.putString(DefineValue.PRODUCT_NAME, response.getProduct_name());
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.getMember_shop_phone());
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportCTADialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

        Boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());
        args.putString(DefineValue.MEMBER_NAME, response.getMember_name());
        args.putString(DefineValue.SOURCE_ACCT, response.getSource_bank_name());
        args.putString(DefineValue.SOURCE_ACCT_NO, response.getSource_acct_no());
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.getSource_acct_name());
        args.putString(DefineValue.BANK_BENEF, response.getBenef_bank_name());
        args.putString(DefineValue.TYPE_BENEF, response.getBenef_acct_type());
        args.putString(DefineValue.NO_BENEF, response.getBenef_acct_no());
        args.putString(DefineValue.NAME_BENEF, response.getBenef_acct_name());
        args.putString(DefineValue.PRODUCT_NAME, response.getProduct_name());
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.getMember_shop_phone());
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putBoolean(DefineValue.IS_MEMBER_CTA, isMemberCTA);

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportAskDialog(String date, String detail, String txId, String type, String description,
                                     String amount, String ccyId, String remark, String alias, String status,
                                     String reason, String buss_scheme_code, String buss_scheme_name) {
        Bundle args = new Bundle();
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.DETAIL, detail);
        args.putString(DefineValue.TYPE, type);
        args.putString(DefineValue.REMARK, remark);
        args.putString(DefineValue.DESCRIPTION, description);
        args.putString(DefineValue.AMOUNT, ccyId + " " + CurrencyFormat.format(amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.REQUEST);
        args.putString(DefineValue.CONTACT_ALIAS, alias);
        args.putString(DefineValue.STATUS, status);
        args.putString(DefineValue.REASON, reason);
        args.putString(DefineValue.BUSS_SCHEME_CODE, buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME, buss_scheme_name);

        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportBillerDialog(ReportDataModel _object, GetTrxStatusReportModel response, String ccyId) {
        Bundle args = new Bundle();
        args.putString(DefineValue.DATE_TIME, _object.getDatetime());
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.DETAIL, _object.getDetail());
        args.putString(DefineValue.TYPE, _object.getType());
        args.putString(DefineValue.REMARK, response.getPayment_remark());
        args.putString(DefineValue.DESCRIPTION, _object.getDescription());
        args.putString(DefineValue.AMOUNT, ccyId + " " + CurrencyFormat.format(_object.getAmount()));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TRANSACTION);
        args.putString(DefineValue.CONTACT_ALIAS, _object.getTo_alias());
        args.putString(DefineValue.BUSS_SCHEME_CODE, _object.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, _object.getBuss_scheme_name());
        args.putString(DefineValue.MEMBER_PHONE, response.getMember_phone());
        args.putString(DefineValue.MEMBER_NAME, response.getMember_name());
        args.putString(DefineValue.PAYMENT_PHONE, response.getPayment_phone());
        args.putString(DefineValue.PAYMENT_NAME, response.getPayment_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));

        showBillerDialog(args, response.getTx_status(), response.getTx_remark());
    }

    private void showReportEspayBillerDialog(String name, GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.getCreated()));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.USERID_PHONE, response.getMember_cust_id());
        args.putString(DefineValue.DENOM_DATA, response.getPayment_name());
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        args.putString(DefineValue.PRODUCT_NAME, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
//        args.putString(DefineValue.DESTINATION_REMARK, userId);
//        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);

        Boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());


        double totalAmount = Double.parseDouble(response.getTx_amount()) + Double.parseDouble(response.getAdmin_fee());
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));


        args.putString(DefineValue.DETAILS_BILLER, response.getDetail());

        if (!String.valueOf(response.getBiller_detail()).equalsIgnoreCase("")) {
            JsonParser jsonParser = new JsonParser();
            Gson gson = new Gson();
            args.putString(DefineValue.BILLER_DETAIL, jsonParser.parse(gson.toJson(response.getBiller_detail())).toString()
//                response.getBiller_detail().getPhoneNumber()
            );
        }
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());

        dialog.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }

    private void showReportCollectorDialog(GetTrxStatusReportModel resp) {

        try {
            JSONObject response = new JSONObject(getGson().toJson(resp));

            Bundle args = new Bundle();
            String txStatus = response.optString(WebParams.TX_STATUS);
            ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
            args.putString(DefineValue.USER_NAME, resp.getMember_cust_name());
            args.putString(DefineValue.DATE_TIME, response.optString(WebParams.CREATED));
            args.putString(DefineValue.TX_ID, response.optString(WebParams.TX_ID));
            args.putString(DefineValue.REPORT_TYPE, DefineValue.DGI);

            args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT)));

            args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.ADMIN_FEE)));
            args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT)));
            args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, true);

            Boolean txStat = false;
            if (txStatus.equals(DefineValue.SUCCESS)) {
                txStat = true;
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
            } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
                txStat = true;
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
            } else if (txStatus.equals(DefineValue.SUSPECT)) {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
            } else if (!txStatus.equals(DefineValue.FAILED)) {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
            } else {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
            }
            args.putBoolean(DefineValue.TRX_STATUS, txStat);
            if (!txStat)
                args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK));


            args.putString(DefineValue.DETAILS_BILLER, response.optString(WebParams.DETAIL, ""));


            args.putString(DefineValue.INVOICE, resp.getInvoice());
            args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE));
            args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME));
            args.putString(DefineValue.PRODUCT_NAME, resp.getProduct_name());
            args.putString(DefineValue.PAYMENT_TYPE_DESC, resp.getPayment_type_desc());
            args.putString(DefineValue.DGI_MEMBER_NAME, resp.getDgi_member_name());
            args.putString(DefineValue.DGI_ANCHOR_NAME, resp.getDgi_anchor_name());
            args.putString(DefineValue.DGI_COMM_NAME, resp.getDgi_comm_name());

            dialog.setArguments(args);
//        dialog.show(getFragmentManager(), "report biller dialog");
//        dialog.setTargetFragment(this, 0);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.add(dialog, ReportBillerDialog.TAG);
            ft.commitAllowingStateLoss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void showReportEspayDialog(String user_name, String date, String user_id, String txId, String payment_name,
//                                       String txStatus, String txRemark, String txAmount, String biller_detail,
//                                       String buss_scheme_code, String buss_scheme_name, String ccy_id){
//        Bundle args = new Bundle();
//        args.putString(DefineValue.DATE_TIME,DateTimeFormat.formatToID(date));
//        args.putString(DefineValue.TX_ID,txId);
//        args.putString(DefineValue.BUSS_SCHEME_NAME,buss_scheme_name);
////        args.putString(DefineValue.COMMUNITY_NAME,comm_name);
//        args.putString(DefineValue.AMOUNT,ccy_id+" "+CurrencyFormat.format(txAmount));
//        args.putString(DefineValue.FEE,ccy_id+" "+CurrencyFormat.format(fee));
//
//        Double total_amount = Double.parseDouble(amount) + Double.parseDouble(fee);
//        args.putString(DefineValue.TOTAL_AMOUNT,ccy_id+" "+CurrencyFormat.format(total_amount));
//
//        args.putString(DefineValue.DESCRIPTION, description);
//        args.putString(DefineValue.REMARK, remark);
//        args.putString(DefineValue.REPORT_TYPE, DefineValue.TRANSACTION_ESPAY);
//        args.putString(DefineValue.BANK_NAME, bankName);
//        args.putString(DefineValue.PRODUCT_NAME, productName);
//
//        if(type_desc.equals(ITEM_DESC_PLN)||type_desc.equals(ITEM_DESC_BPJS)){
//            args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_PLN);
//            if(type_desc.equals(ITEM_DESC_BPJS))
//                args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER_BPJS);
//            try {
//                args.putString(DefineValue.DETAILS_BILLER,response.getString(WebParams.DETAIL));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        args.putString(DefineValue.BUSS_SCHEME_CODE,buss_scheme_code);
//
//        showBillerDialog(args, txStatus, txRemark);
//
//    }

    private void showBillerDialog(Bundle args, String txStatus, String txRemark) {
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        } else if (txStatus.equals(DefineValue.SUSPECT)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        } else if (!txStatus.equals(DefineValue.FAILED)) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, txRemark);
        args.getString(DefineValue.TX_ID, args.getString(DefineValue.TX_ID) );

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(getActivity().getSupportFragmentManager(), ReportBillerDialog.TAG);

    }


    private void setLoadMore(boolean isLoading) {
        if (isLoading) {
            lv_report.addFooterView(footerLayout, null, false);
        } else {
            lv_report.removeFooterView(footerLayout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
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

    public Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }
}