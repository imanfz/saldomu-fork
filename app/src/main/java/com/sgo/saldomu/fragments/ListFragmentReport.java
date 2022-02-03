package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.SummaryAdditionalFeeModel;
import com.sgo.saldomu.Beans.SummaryReportFeeModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.ReportAdditionalFeeAdapter;
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
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
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
import java.util.Objects;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import timber.log.Timber;

/*
  Created by Administrator on 5/19/2015.
 */
public class ListFragmentReport extends ListFragment implements ReportBillerDialog.OnDialogOkCallback {

    public static int REPORT_ASK = 0x0299395;
    public static int REPORT_SCASH = 0x0299394;
    public static int REPORT_ESPAY = 0x0299393;
    public static int REPORT_PENDING = 0x0299398;
    public static int REPORT_FEE = 0x0299396;
    public static int REPORT_ADDITIONAL_FEE = 0x0299397;
    private final String DATEFROM = "tagFrom";
    private final String DATETO = "tagTo";

    private TextView tv_date_from, tv_date_to, sumTotalTrx, sumRelAmount, sumRelTrx, sumUnrelTrx, sumUnrelAmount, sumTotalAmount;

    private View v;
    private LinearLayout layout_filter, layout_summary;
    TableLayout tableCommFee, tableAdditionalFee;
    private int height;
    private String OrifromDate;
    private String OritoDate;
    private ListView lv_report;
    private ViewGroup footerLayout;
    private ToggleButton filter_btn;
    private ImageView spining_progress;
    private MaterialRippleLayout btn_loadmore;

    private ProgressDialog progressDialog;
    private ListAdapter UniAdapter = null;
    private SecurePreferences sp;
    private Calendar date_from;
    private Calendar date_to;
    private Calendar bak_date_to;
    private Calendar bak_date_from;
    private Animation frameAnimation;
    private int page;
    private int report_type;
    private PtrFrameLayout mPtrFrame;
    private View emptyLayout;
    private Boolean isReport = false;
    private Boolean isMemberCTA = false;

    SummaryReportFeeModel SummaryFeeModel;
    SummaryAdditionalFeeModel summaryAdditionalFeeModel;

    GetReportDataModel reportListModel;
    List<ReportDataModel> reportData;

    ReportListAdapter reportListAdapter;
    ReportListEspayAdapter reportListEspayAdapter;
    ReportAskListAdapter reportAskListAdapter;
    ReportCommFeeAdapter reportCommFeeAdapter;
    ReportAdditionalFeeAdapter reportAdditionalFeeAdapter;

    private Gson gson;

    public static ListFragmentReport newInstance(int _report_type) {
        ListFragmentReport mFrag = new ListFragmentReport();
        mFrag.report_type = _report_type;
        return mFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        footerLayout = (ViewGroup) Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.footer_loadmore, lv_report, false);
        footerLayout.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        spining_progress = footerLayout.findViewById(R.id.image_spinning_wheel);
        btn_loadmore = footerLayout.findViewById(R.id.btn_loadmore);
        emptyLayout = getV().findViewById(R.id.empty_layout);
        emptyLayout.setVisibility(View.GONE);
        Button btn_refresh = emptyLayout.findViewById(R.id.btnRefresh);
        layout_summary = getV().findViewById(R.id.table_summary);
//        layout_summary_additionalfee = getV().findViewById(R.id.table_summary_additionalfee);
        tableCommFee = getV().findViewById(R.id.table_summary_commfee);
        tableAdditionalFee = getV().findViewById(R.id.table_summary_additionalfee);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        OrifromDate = DateTimeFormat.getCurrentDateMinus(6);
        OritoDate = DateTimeFormat.getCurrentDate();
        page = 1;

        SummaryFeeModel = new SummaryReportFeeModel();
        reportListModel = new GetReportDataModel();
        reportData = new ArrayList<>();

        filter_btn.setOnClickListener(filterBtnListener);

        btn_loadmore.setOnClickListener(v -> getDataReport(page, CalToString(date_from), CalToString(date_to), false));

        btn_refresh.setOnClickListener(v -> mPtrFrame.autoRefresh());

        date_from = StringToCal(OrifromDate);
        date_to = StringToCal(OritoDate);
        bak_date_from = (Calendar) date_from.clone();
        bak_date_to = (Calendar) date_to.clone();

        String dedate = getString(R.string.from) + " :\n" + date_from.get(Calendar.DAY_OF_MONTH) + "-" + (date_from.get(Calendar.MONTH) + 1) + "-" + date_from.get(Calendar.YEAR);
        tv_date_from.setText(dedate);
        dedate = getString(R.string.to) + " :\n" + date_to.get(Calendar.DAY_OF_MONTH) + "-" + (date_to.get(Calendar.MONTH) + 1) + "-" + date_to.get(Calendar.YEAR);
        tv_date_to.setText(dedate);

        tv_date_from.setOnClickListener(v -> {

            filter_btn.setChecked(false);
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    date_from.get(Calendar.YEAR),
                    date_from.get(Calendar.MONTH),
                    date_from.get(Calendar.DAY_OF_MONTH)
            );

            if (getFragmentManager() != null) {
                dpd.show(getFragmentManager(), DATEFROM);
            }
        });

        tv_date_to.setOnClickListener(v -> {
            filter_btn.setChecked(false);
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    date_to.get(Calendar.YEAR),
                    date_to.get(Calendar.MONTH),
                    date_to.get(Calendar.DAY_OF_MONTH)
            );

            if (getFragmentManager() != null) {
                dpd.show(getFragmentManager(), DATETO);
            }
        });

        //Adapter list data

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        setLoadMore(true);
        if (report_type == REPORT_SCASH) {
            reportListAdapter = new ReportListAdapter(getActivity(), R.layout.list_transaction_report_item, reportData);
            lv_report.setAdapter(reportListAdapter);
        } else if (report_type == REPORT_ESPAY || report_type == REPORT_PENDING) {
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
        } else if (report_type == REPORT_ADDITIONAL_FEE) {
            reportAdditionalFeeAdapter = new ReportAdditionalFeeAdapter(getActivity(), R.layout.list_report_comm_fee, reportData);
            sumTotalTrx = getV().findViewById(R.id.tv_total_transaction_additionalfee);
            sumTotalAmount = getV().findViewById(R.id.total_amount_additionalfee);
            lv_report.setAdapter(reportAdditionalFeeAdapter);
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
        mPtrFrame.postDelayed(() -> mPtrFrame.autoRefresh(false), 50);
    }

    private boolean canScroolUp() {
        return lv_report != null && (lv_report.getAdapter().getCount() == 0 || lv_report.getFirstVisiblePosition() == 0 && lv_report.getChildAt(0).getTop() == 0);
    }

    private Calendar StringToCal(String src) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", new Locale("id", "INDONESIA"));
        Calendar tempCalendar = Calendar.getInstance();

        try {
            tempCalendar.setTime(Objects.requireNonNull(format.parse(src)));
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

            if (view.getTag() != null) {
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
        }
    };

    private void getDataReport(int _page, final String _date_from, String _date_to, final Boolean isRefresh) {
        try {
            if (isRefresh == null) {
                Timber.wtf("masuk ptr");
            } else if (isRefresh) {
                Timber.wtf("masuk refresh");
                progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), null);
                if (progressDialog != null) {
                    progressDialog.show();
                }
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
            } else if (report_type == REPORT_PENDING) {
                url = MyApiClient.LINK_TRANSACTION_REPORT_PENDING;
            } else if (report_type == REPORT_ESPAY) {
                url = MyApiClient.LINK_REPORT_ESPAY;
            } else if (report_type == REPORT_ASK) {
                url = MyApiClient.LINK_REPORT_MONEY_REQUEST;
            } else if (report_type == REPORT_FEE) {
                url = MyApiClient.LINK_REPORT_COMM_FEE;
            } else if (report_type == REPORT_ADDITIONAL_FEE) {
                url = MyApiClient.LINK_REPORT_ADDITIONAL_FEE;
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
            } else if (report_type == REPORT_ESPAY || report_type == REPORT_PENDING) {
                params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
            } else if (report_type == REPORT_FEE || report_type == REPORT_ADDITIONAL_FEE) {
                params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
                params.put(WebParams.OFFSET, sp.getString(DefineValue.OFFSET, ""));
            }
            Timber.d("isi param report : %s", params);
            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {

                                String code;
                                String message = "";
                                JSONObject temp = new JSONObject(getGson().toJson(object));

                                if (temp.optString("report_data", "").equals("")) {
                                    code = "0003";
                                } else {

                                    reportListModel = getGson().fromJson(object, GetReportDataModel.class);

                                    code = reportListModel.getError_code();
                                    message = reportListModel.getError_message();

                                    reportData.clear();

                                    reportData.addAll(reportListModel.getReport_data());
                                }

                                if (isAdded()) {
                                    if (isRefresh != null) {
                                        if (isRefresh)
                                            progressDialog.dismiss();
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

                                        if (report_type == REPORT_FEE) {
                                            SummaryFeeModel = new SummaryReportFeeModel();
                                            SummaryFeeModel.setTotal_transaction(reportListModel.getReport_data().size());
                                            for (ReportDataModel model : reportListModel.getReport_data()) {
                                                getSummaryFee(SummaryFeeModel, model);
                                            }

                                            setSummarytoView(SummaryFeeModel);
                                        }

                                        if (report_type == REPORT_ADDITIONAL_FEE) {
                                            summaryAdditionalFeeModel = new SummaryAdditionalFeeModel();
                                            List<SummaryAdditionalFeeModel> summaryAdditionalFeeModelList = reportListModel.getSummary();
                                            summaryAdditionalFeeModel.setCount_trx(summaryAdditionalFeeModelList.get(0).getCount_trx());
                                            summaryAdditionalFeeModel.setTotal_trx(summaryAdditionalFeeModelList.get(0).getTotal_trx());
                                            setSummaryAdditionalFee(summaryAdditionalFeeModel);
                                        }

                                        int _page = Integer.parseInt(reportListModel.getNext());
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
                                        AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                    } else if (code.equals("0003")) {
                                        bak_date_from = (Calendar) date_from.clone();
                                        bak_date_to = (Calendar) date_to.clone();
                                        mPtrFrame.refreshComplete();
                                        setLoadMore(false);
                                        lv_report.setVisibility(View.GONE);
                                        emptyLayout.setVisibility(View.VISIBLE);
                                        NotifyDataChange();
                                    } else if (code.equals(DefineValue.ERROR_9333)) {
                                        Timber.d("isi response app data:%s", reportListModel.getApp_data());
                                        final AppDataModel appModel = reportListModel.getApp_data();
                                        AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                    } else if (code.equals(DefineValue.ERROR_0066)) {
                                        Timber.d("isi response maintenance:%s", object.toString());
                                        AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                    } else {
                                        date_from = (Calendar) bak_date_from.clone();
                                        String dedate = getString(R.string.from) + " :\n" + date_from.get(Calendar.DAY_OF_MONTH) + "-" + (date_from.get(Calendar.MONTH) + 1) + "-" + date_from.get(Calendar.YEAR);
                                        tv_date_from.setText(dedate);
                                        date_to = (Calendar) bak_date_to.clone();
                                        dedate = getString(R.string.to) + " :\n" + date_to.get(Calendar.DAY_OF_MONTH) + "-" + (date_to.get(Calendar.MONTH) + 1) + "-" + date_to.get(Calendar.YEAR);
                                        tv_date_to.setText(dedate);
                                        filter_btn.setChecked(false);

                                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

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
                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            filter_btn.setOnClickListener(filterBtnListener);
                        }
                    });
        } catch (
                Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void getSummaryFee(SummaryReportFeeModel obj, ReportDataModel model) {
        String a = model.getAmount();
        if (model.getStatus().equalsIgnoreCase("Released")) {
            obj.setReleased_trx(obj.getReleased_trx() + 1);
            obj.setReleased_amount(obj.getReleased_amount() + Integer.parseInt(a));
        } else if (model.getStatus().equalsIgnoreCase("Unreleased")) {
            obj.setUnreleased_trx(obj.getUnreleased_trx() + 1);
            obj.setUnreleased_amount(obj.getUnreleased_amount() + Integer.parseInt(a));
        }
    }

    private void setSummarytoView(SummaryReportFeeModel obj) {
        sumTotalTrx.setText(String.valueOf(obj.getTotal_transaction()));
        sumUnrelTrx.setText(String.valueOf(obj.getUnreleased_trx()));
        sumUnrelAmount.setText(CurrencyFormat.format(String.valueOf(obj.getUnreleased_amount())));
        sumRelTrx.setText(String.valueOf(obj.getReleased_trx()));
        sumRelAmount.setText(CurrencyFormat.format(String.valueOf(obj.getReleased_amount())));

        layout_summary.setVisibility(View.VISIBLE);
        tableCommFee.setVisibility(View.VISIBLE);
    }

    private void setSummaryAdditionalFee(SummaryAdditionalFeeModel obj) {
        sumTotalTrx.setText(obj.getCount_trx());
        sumTotalAmount.setText(CurrencyFormat.format(obj.getTotal_trx()));

        layout_summary.setVisibility(View.VISIBLE);
        tableAdditionalFee.setVisibility(View.VISIBLE);
    }

    private void NotifyDataChange() {
        if (report_type == REPORT_SCASH) {
            reportListAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_ESPAY || report_type == REPORT_PENDING) {
            reportListEspayAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_ASK) {
            reportAskListAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_FEE) {
            reportCommFeeAdapter.notifyDataSetChanged();
        } else if (report_type == REPORT_ADDITIONAL_FEE) {
            reportAdditionalFeeAdapter.notifyDataSetChanged();
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
            progressDialog = DefinedDialog.CreateProgressDialog(getActivity(), null);
            if (progressDialog != null) {
                progressDialog.show();
            }

            String _tx_id = "";
            String _comm_id = "";
            String tx_type = DefineValue.EMO;
            boolean call = true;


            if (report_type == REPORT_SCASH) {
                _tx_id = _object.getTrx_id();
                _comm_id = sp.getString(DefineValue.COMMUNITY_ID, "");
                if (_object.getBuss_scheme_code().equalsIgnoreCase("RF") ||
                        _object.getBuss_scheme_code().equalsIgnoreCase("RA")) {
                    call = false;
                }
//                call =
            } else if (report_type == REPORT_ESPAY || report_type == REPORT_PENDING) {
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
                Timber.d("isi params sent get Trx Status:%s", params.toString());

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {
                                GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                                String code = model.getError_code();
                                String message = model.getError_message();
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    showDialog(_object, model);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", object.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        });
            }
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }


    private void showDialog(ReportDataModel _object, GetTrxStatusReportModel response) {
        if (report_type == REPORT_SCASH) {
            String ccyId = response.getCcy_id();
            switch (_object.getBuss_scheme_code()) {
                case "OC":
                    showReportCashOutBankDialog(sp.getString(DefineValue.USER_NAME, ""),
                            DateTimeFormat.getCurrentDateTime(),
                            sp.getString(DefineValue.USERID_PHONE, ""), ccyId,
                            response);
                    break;
                case "OR":
                case "ORP":
                case "IR":
                    showReportBillerDialog(_object, response, ccyId);
                    break;
            }
        } else if (report_type == REPORT_ESPAY || report_type == REPORT_PENDING) {
            if (_object.getBuss_scheme_code().equals(DefineValue.BIL)) {
                showReportEspayBillerDialog(sp.getString(DefineValue.USER_NAME, ""), response);
            } else if (_object.getBuss_scheme_code().equals(DefineValue.CTA)) {
                if (!sp.getString(DefineValue.USERID_PHONE, "").equals(response.getMember_phone())) {
                    isMemberCTA = true;
                }
                showReportCTADialog(response);
            } else if (_object.getBuss_scheme_code().equals(DefineValue.ATC)) {
                Timber.d("%suser_id", sp.getString(DefineValue.USERID_PHONE, ""));
                if (sp.getString(DefineValue.USERID_PHONE, "").equals(response.getMember_phone())) {
                    showReportATCAgentDialog(response);
                } else {
                    isReport = true;
                    showReportATCMemberDialog(response);
                }
            } else if (_object.getBuss_scheme_code().equals(DefineValue.EMO) || _object.getBuss_scheme_code().equalsIgnoreCase(DefineValue.TOPUP_B2B)) {
                showReportEMODialog(response);
            } else if (_object.getBuss_scheme_code().equals(DefineValue.DENOM_B2B) || _object.getBuss_scheme_code().equals(DefineValue.EBD)) {
                showReportBDKDialog(response);
            } else if (_object.getBuss_scheme_code().equals(DefineValue.DGI)) {
                showReportCollectorDialog(response);
            } else if (_object.getBuss_scheme_code().equals(DefineValue.SG3)) {
                showReportSOFDialog(response);
            } else if (_object.getBuss_scheme_code().equals(DefineValue.QRS)) {
                showReportQRSDialog(response);
            }
        }
    }

    private void slidingView(final View vFrom) {

        CollapseExpandAnimation anim;

        if (vFrom.getVisibility() == View.VISIBLE) {
            anim = new CollapseExpandAnimation(vFrom, 250, CollapseExpandAnimation.COLLAPSE);
            height = anim.getHeight();
        } else {
            anim = new CollapseExpandAnimation(vFrom, 250, CollapseExpandAnimation.EXPAND);
            anim.setHeight(height);
        }
        vFrom.startAnimation(anim);
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
        args.putString(DefineValue.TRX_STATUS_REMARK, model.getTx_status_remark());


        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportATCMemberDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);

        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdditional_fee()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }

        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
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
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportEMODialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));

        double dAmount = Double.parseDouble(Objects.requireNonNull(response.getTx_amount()));
        double dFee = Double.parseDouble(Objects.requireNonNull(response.getAdmin_fee()));
        double total_amount = dAmount + dFee;

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.COMMUNITY_CODE, response.getComm_code());
        args.putString(DefineValue.MEMBER_CODE, response.getMember_code());
        args.putString(DefineValue.MEMBER_ID_CUST, response.getMember_cust_id());
        args.putString(DefineValue.MEMBER_CUST_NAME, response.getMember_cust_name());
        args.putString(DefineValue.STORE_NAME, response.getStore_name());
        args.putString(DefineValue.STORE_ADDRESS, response.getStore_address());
        args.putString(DefineValue.STORE_CODE, response.getStore_code());

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportBDKDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);

        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_gross()));

        double dAmount = Double.parseDouble(Objects.requireNonNull(response.getTx_amount()));
        double dFee = Double.parseDouble(Objects.requireNonNull(response.getAdmin_fee()));
        double total_amount = dAmount + dFee;

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.COMMUNITY_CODE, response.getComm_code());
        args.putString(DefineValue.MEMBER_CODE, response.getMember_code());
        args.putString(DefineValue.DENOM_DETAIL, getGson().toJson(response.getDenom_detail()));
        args.putString(DefineValue.ORDER_ID, response.getOrder_id());
        args.putString(DefineValue.STORE_CODE, response.getStore_code());
        args.putString(DefineValue.STORE_NAME, response.getStore_name());
        args.putString(DefineValue.STORE_ADDRESS, response.getStore_address());
        args.putString(DefineValue.AGENT_NAME, response.getMember_cust_name());
        args.putString(DefineValue.AGENT_PHONE, response.getMember_cust_id());
        args.putString(DefineValue.TOTAL_DISC, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_disc()));

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportATCAgentDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdditional_fee()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
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
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void showReportCTADialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, response.getMember_name());
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN);
        args.putString(DefineValue.USERID_PHONE, response.getMember_phone());
        args.putString(DefineValue.BANK_NAME, response.getTx_bank_name());
        args.putString(DefineValue.BANK_PRODUCT, response.getProduct_name());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdditional_fee()));
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
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
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
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
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());

        showBillerDialog(args, Objects.requireNonNull(response.getTx_status()), response.getTx_remark());
    }

    private void showReportEspayBillerDialog(String name, GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.USERID_PHONE, response.getMember_cust_id());
        args.putString(DefineValue.DENOM_DATA, response.getPayment_name());
        double amount = Double.parseDouble(Objects.requireNonNull(response.getTotal_amount())) -
                Double.parseDouble(Objects.requireNonNull(response.getAdmin_fee())) -
                Double.parseDouble(Objects.requireNonNull(response.getAdditional_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER);
        args.putString(DefineValue.PRODUCT_NAME, response.getProduct_name());
        args.putString(DefineValue.DESTINATION_REMARK, response.getPayment_remark());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdditional_fee()));
//        args.putString(DefineValue.DESTINATION_REMARK, userId);
//        args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, isShowDescription);

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());


//        double totalAmount = Double.parseDouble(response.getTx_amount()) + Double.parseDouble(response.getAdmin_fee());
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTotal_amount()));
//        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));


        args.putString(DefineValue.DETAILS_BILLER, response.getDetail());

        if (!String.valueOf(response.getBiller_detail()).equalsIgnoreCase("")) {
            JsonParser jsonParser = new JsonParser();
            Gson gson = new Gson();
            args.putString(DefineValue.BILLER_DETAIL, jsonParser.parse(gson.toJson(response.getBiller_detail())).toString());
        }
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.BILLER_TYPE, response.getBiller_type());
        args.putString(DefineValue.PAYMENT_REMARK, response.getPayment_remark());

        dialog.setArguments(args);
        FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
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

            boolean txStat = false;
            if (txStatus.equals(DefineValue.SUCCESS)) {
                txStat = true;
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
            } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
                txStat = true;
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
            }
//            else if (txStatus.equals(DefineValue.SUSPECT)) {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//            } else if (!txStatus.equals(DefineValue.FAILED)) {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//            } else {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//            }
            args.putBoolean(DefineValue.TRX_STATUS, txStat);

            args.putString(DefineValue.TRX_STATUS_REMARK, resp.getTx_status_remark());
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
            args.putString(DefineValue.TRX_ID_REF, response.optString(WebParams.TRX_ID_REF));

            dialog.setArguments(args);
//        dialog.show(getFragmentManager(), "report biller dialog");
//        dialog.setTargetFragment(this, 0);
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.add(dialog, ReportBillerDialog.TAG);
            ft.commitAllowingStateLoss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showBillerDialog(Bundle args, String txStatus, String txRemark) {
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);

        boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }
//        else if (txStatus.equals(DefineValue.SUSPECT)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//        } else if (!txStatus.equals(DefineValue.FAILED)) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if (!txStat) args.putString(DefineValue.TRX_REMARK, txRemark);
        args.getString(DefineValue.TX_ID, args.getString(DefineValue.TX_ID));

        dialog.setArguments(args);
//        dialog.setTargetFragment(this,0);
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);

    }

    private void showReportSOFDialog(GetTrxStatusReportModel resp) {
        try {
            JSONObject response = new JSONObject(getGson().toJson(resp));

            String txStatus = response.optString(WebParams.TX_STATUS);

            Bundle args = new Bundle();
            ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
            args.putString(DefineValue.TX_ID, response.optString(WebParams.TX_ID));
            args.putString(DefineValue.REPORT_TYPE, DefineValue.SG3);
            args.putString(DefineValue.DATE_TIME, response.optString(WebParams.CREATED));
            args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT)));
            args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.ADMIN_FEE)));
            args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT)));

            boolean txStat = false;
            if (txStatus.equals(DefineValue.SUCCESS)) {
                txStat = true;
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
            } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
                txStat = true;
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
            }
//            else if (txStatus.equals(DefineValue.SUSPECT)) {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
//            } else if (!txStatus.equals(DefineValue.FAILED)) {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus);
//            } else {
//                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
//            }
            args.putBoolean(DefineValue.TRX_STATUS, txStat);
            args.putString(DefineValue.TRX_STATUS_REMARK, resp.getTx_status_remark());
            if (!txStat)
                args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK));


            args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE));
            args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME));
            args.putString(DefineValue.COMMUNITY_NAME, response.optString(WebParams.COMM_NAME));
            args.putString(DefineValue.REMARK, response.optString(WebParams.PAYMENT_REMARK));

            dialog.setArguments(args);
//        dialog.show(getFragmentManager(), "report biller dialog");
//        dialog.setTargetFragment(this, 0);
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.add(dialog, ReportBillerDialog.TAG);
            ft.commitAllowingStateLoss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showReportQRSDialog(GetTrxStatusReportModel response) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.getCreated())));
        args.putString(DefineValue.TX_ID, response.getTx_id());
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getAdmin_fee()));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTx_amount()));
        args.putString(DefineValue.TIPS_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.getTips_amount()));

        double amount = Double.parseDouble(Objects.requireNonNull(response.getTx_amount()));
        double fee = Double.parseDouble(Objects.requireNonNull(response.getAdmin_fee()));
        double tipFee = Double.parseDouble(Objects.requireNonNull(response.getTips_amount()));
        double total_amount = amount + fee + tipFee;

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount));

        boolean txStat = false;
        String txStatus = response.getTx_status();
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, response.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.getTx_remark());

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.getBuss_scheme_code());
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.getBuss_scheme_name());
        args.putString(DefineValue.MERCHANT_NAME, response.getMerchant_name());
        args.putString(DefineValue.MERCHANT_CITY, response.getMerchant_city());
        args.putString(DefineValue.MERCHANT_PAN, response.getMerchant_pan());
        args.putString(DefineValue.TERMINAL_ID, response.getTerminal_id());
        args.putString(DefineValue.TRX_ID_REF, response.getTrx_id_ref());
        args.putString(DefineValue.INDICATOR_TYPE, response.getIndicator_type());

        dialog.setArguments(args);
        dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ReportBillerDialog.TAG);
    }

    private void setLoadMore(boolean isLoading) {
        if (isLoading) {
            lv_report.addFooterView(footerLayout, null, false);
        } else {
            lv_report.removeFooterView(footerLayout);
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