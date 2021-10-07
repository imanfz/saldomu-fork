package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.bank_biller_model;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.adapter.InvoiceDGIAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DividerItemDecoration;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.InputAmountTagihBillerDialog;
import com.sgo.saldomu.dialogs.PaymentRemarkDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.FeeDGIModel;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.models.PaymentTypeDGIModel;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;


public class FragListInvoiceIndomobil extends BaseFragment {
    View view;
    SecurePreferences sp;

    // declare view objects
    RecyclerView listMenu;
    TableRow row_phone;
    View view_row_phone;
    TextView lbl_total_pay_amount;
    LinearLayout searchLayout;
    Button btnDone, btnCheck, btnReset;
    private AutoCompleteTextView search;
    Spinner sp_payment_type, sp_payment_method;
    String mobile_phone, paymentCode, paymentName, ccy_id, buyer_fee, seller_fee, commission_fee, min_amount, max_amount, noId;
    String paymentMethod, buss_scheme_code, doc_no, doc_id, remain_amount, amount, due_date, bank_code, bank_name;
    String notes, cust_id, anchorId;
    private final ArrayList<bank_biller_model> bankBillerModelArrayList = new ArrayList<>();
    private final ArrayList<PaymentTypeDGIModel> paymentTypeDGIModelArrayList = new ArrayList<>();
    private final ArrayList<FeeDGIModel> feeDGIModelArrayList = new ArrayList<>();
    private final ArrayList<InvoiceDGI> invoiceDGIModelArrayList = new ArrayList<>();
    private final ArrayList<InvoiceDGI> newInvoiceDGIArrayList = new ArrayList<>();
    private ArrayAdapter<String> paymentTypeAdapter;
    private ArrayAdapter<String> paymentMethodAdapter;
    ArrayList<String> paymentTypeArr = new ArrayList<>();
    ArrayList<String> paymentMethodArr = new ArrayList<>();
    ArrayList<String> paymentMethodArrBG = new ArrayList<>();
    String partialPayment, memberCode, commCodeTagih, paymentRemark, txIdPG;
    InvoiceDGIAdapter invoiceDGIAdapter;
    Bundle bundle1 = new Bundle();
    int total;
    boolean isSearchVisible = false;
    boolean isFav = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_list_invoice_dgi, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        dismissProgressDialog();

        final Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCodeTagih = bundle.getString(DefineValue.COMMUNITY_CODE, "");
            txIdPG = bundle.getString(DefineValue.TXID_PG, "");
            if (bundle.getBoolean(DefineValue.IS_FAVORITE)) {
                isFav = true;
                notes = bundle.getString(DefineValue.NOTES, "");
                cust_id = bundle.getString(DefineValue.CUST_ID, "");
                anchorId = bundle.getString(DefineValue.ANCHOR_ID, "");
            }
        }

        listMenu = view.findViewById(R.id.listMenu);
        row_phone = view.findViewById(R.id.row_phone);
        view_row_phone = view.findViewById(R.id.view_row_phone);
        lbl_total_pay_amount = view.findViewById(R.id.lbl_total_pay_amount);

        btnDone = view.findViewById(R.id.btn_done);
        btnCheck = view.findViewById(R.id.btn_check);
        btnReset = view.findViewById(R.id.btnReset);
        sp_payment_type = view.findViewById(R.id.cbo_payment_type);
        sp_payment_method = view.findViewById(R.id.sp_metode_pembayaran);
        search = view.findViewById(R.id.search);
        searchLayout = view.findViewById(R.id.layout_search);

        row_phone.setVisibility(View.GONE);
        view_row_phone.setVisibility(View.GONE);
        initializeRecyclerview();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                invoiceDGIAdapter.getFilter().filter(editable.toString());
            }
        });

        btnReset.setOnClickListener(view -> resetData());

        btnDone.setOnClickListener(v -> {
            if (lbl_total_pay_amount.getText().toString().equalsIgnoreCase("0")) {
                Toast.makeText(getActivity(), "Tidak ada invoice yang dibayarkan", Toast.LENGTH_SHORT).show();
            } else {
                PaymentRemarkDialog dialog = PaymentRemarkDialog.newDialog((msg, s, dedate) -> {
                    paymentRemark = msg;
                    if (s.isEmpty()) {
                        noId = "";
                    } else
                        noId = s;

                    if (dedate.isEmpty()) {
                        due_date = "";
                    } else
                        due_date = dedate;

                    checkOutPayment(msg, noId, due_date);
                    if (bundle != null) {
                        bundle.putString(DefineValue.REMARK, paymentRemark);
                    }
                }, paymentCode);
                if (getFragmentManager() != null) {
                    dialog.show(getFragmentManager(), "paymentremark dialog");
                }
            }
        });

        btnCheck.setOnClickListener(view -> checkNewInvoice());
        disableButton(btnCheck);

        getListInvoice();
    }

    private void getListInvoice() {
        showProgressDialog();

        extraSignature = commCodeTagih + memberCode;
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_INVOICE_DGI, extraSignature);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.MEMBER_CODE, memberCode);
        params.put(WebParams.COMM_CODE, commCodeTagih);
        params.put(WebParams.USER_ID, userPhoneID);

        Timber.d("params list invoice : %s", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_INVOICE_DGI, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            String errorMessage = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                parseResponse(response);
                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.COMM_CODE_DGI, response.getString(WebParams.COMM_CODE_DGI));
                                mEditor.apply();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                jsonModel model = getGson().fromJson(response.toString(), jsonModel.class);
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        dismissProgressDialog();
                        getFragManager().popBackStack();
                    }

                    @Override
                    public void onComplete() {
                        dismissProgressDialog();
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.ab_notification, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.favorite).setVisible(false);
        menu.findItem(R.id.notifications).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.search).setVisible(true);
        menu.findItem(R.id.cancel).setVisible(true);
        menu.findItem(R.id.scan).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getActivity().invalidateOptionsMenu();
        if (item.getItemId() == R.id.search) {
            isSearchVisible = !isSearchVisible;
            searchLayout.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);
            if (!isSearchVisible) {
                search.getText().clear();
            }
        } else if (item.getItemId() == R.id.cancel) {
            DataManager.getInstance().setListInvoice(invoiceDGIModelArrayList);

            Fragment newFrag = new ListCancelDGI();
            Bundle bundle2 = new Bundle();
            bundle2.putString(DefineValue.MEMBER_CODE, memberCode);
            bundle2.putString(DefineValue.COMMUNITY_CODE, commCodeTagih);
            newFrag.setArguments(bundle2);
            TagihActivity ftf = (TagihActivity) getActivity();
            ftf.switchContent(newFrag, getString(R.string.cancel_transaction), true);
        } else if (item.getItemId() == R.id.scan) {
            IntentIntegrator.forSupportFragment(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                    .setCameraId(0)
                    .setOrientationLocked(true)
                    .setPrompt("Scanning")
                    .setBeepEnabled(true)
                    .setBarcodeImageEnabled(true)
                    .setCaptureActivity(CaptureActivity.class)
                    .initiateScan();
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeRecyclerview() {
        invoiceDGIAdapter = new InvoiceDGIAdapter(invoiceDGIModelArrayList,
                this::showInputDialog);
        listMenu.setAdapter(invoiceDGIAdapter);
        listMenu.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listMenu.addItemDecoration(new DividerItemDecoration(ResourcesCompat.getDrawable(getResources(), R.drawable.row_divider, null),
                false, false));
    }

    void showInputDialog(InvoiceDGI model) {
        int pos = invoiceDGIModelArrayList.indexOf(model);
        InputAmountTagihBillerDialog dialog = InputAmountTagihBillerDialog.newDialog(pos, invoiceDGIModelArrayList.get(pos), partialPayment,
                (pos1, value) -> {
                    invoiceDGIModelArrayList.get(pos1).setInput_amount(value);
                    invoiceDGIAdapter.notifyItemChanged(pos1);

                    countTotalPrice();

                    bundle1.putString(DefineValue.TOTAL_AMOUNT, String.valueOf(total));

                });

        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), "input dialog");
        }
    }

    void countTotalPrice() {
        total = 0;
        for (InvoiceDGI obj : invoiceDGIModelArrayList
        ) {
            if (Integer.parseInt(obj.getInput_amount()) != 0) {
                total += Integer.parseInt(obj.getInput_amount());
            }
        }

        lbl_total_pay_amount.setText(CurrencyFormat.format(total));
    }

    void resetData() {
        for (InvoiceDGI obj : invoiceDGIModelArrayList
        ) {
            obj.setInput_amount("0");
        }

        invoiceDGIAdapter.notifyDataSetChanged();

        countTotalPrice();
    }

    JSONArray getInvoice(List<InvoiceDGI> temp) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (InvoiceDGI obj : invoiceDGIModelArrayList
            ) {
                if (Integer.parseInt(obj.getInput_amount()) != 0) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(WebParams.DOC_ID, obj.getDoc_id());
                    jsonObject.put(WebParams.DOC_NO, obj.getDoc_no());
                    jsonObject.put(WebParams.AMOUNT, obj.getInput_amount());
                    jsonArray.put(jsonObject);
                    temp.add(obj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    public void parseResponse(JSONObject obj) {
        try {
            partialPayment = obj.optString("partial_payment", "");

            JSONArray mArrayMobilePhone = new JSONArray(obj.optString(WebParams.PHONE_DATA, ""));

            mobile_phone = mArrayMobilePhone.getJSONObject(0).getString(WebParams.MOBILE_PHONE);

            JSONArray mArrayPaymentType = new JSONArray(obj.optString(WebParams.PAYMENT_TYPE));

            for (int i = 0; i < mArrayPaymentType.length(); i++) {
                paymentCode = mArrayPaymentType.getJSONObject(i).getString(WebParams.PAYMENT_CODE);
                paymentName = mArrayPaymentType.getJSONObject(i).getString(WebParams.PAYMENT_NAME);

                PaymentTypeDGIModel paymentTypeDGIModel = new PaymentTypeDGIModel();
                paymentTypeDGIModel.setPayment_code(paymentCode);
                paymentTypeDGIModel.setPayment_name(paymentName);

                paymentTypeDGIModelArrayList.add(paymentTypeDGIModel);
            }

            paymentTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_text_primary_dark, paymentTypeArr);
            sp_payment_type.setAdapter(paymentTypeAdapter);

            initializePaymentType();

            sp_payment_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        paymentCode = mArrayPaymentType.getJSONObject(position).getString(WebParams.PAYMENT_CODE);
                        if (paymentCode.equalsIgnoreCase("BG") || paymentCode.equalsIgnoreCase("TS")) {
                            getBankCashout();
                        } else {
                            JSONArray mArrayPaymentMethod = new JSONArray(obj.optString(WebParams.BANK, ""));
                            bankBillerModelArrayList.clear();
                            for (int i = 0; i < mArrayPaymentMethod.length(); i++) {
                                paymentMethod = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.PRODUCT_CODE);
                                bank_code = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.BANK_CODE);

                                bank_biller_model bankBillerModel = new bank_biller_model();
                                bankBillerModel.setProduct_code(paymentMethod);
                                bankBillerModel.setBank_code(bank_code);

                                bankBillerModelArrayList.add(bankBillerModel);
                            }
                            paymentMethodAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_text_primary_dark, paymentMethodArr);
                            initializePaymentMethod();
                            sp_payment_method.setAdapter(paymentMethodAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            JSONArray mArrayFee = new JSONArray(obj.optString(WebParams.FEE_DATA));

            for (int i = 0; i < mArrayFee.length(); i++) {
                ccy_id = mArrayFee.getJSONObject(i).getString(WebParams.CCY_ID);
                buyer_fee = mArrayFee.getJSONObject(i).getString(WebParams.BUYER_FEE);
                seller_fee = mArrayFee.getJSONObject(i).getString(WebParams.SELLER_FEE);
                commission_fee = mArrayFee.getJSONObject(i).getString(WebParams.COMMISSION_FEE);
                min_amount = mArrayFee.getJSONObject(i).getString(WebParams.MIN_AMOUNT);
                max_amount = mArrayFee.getJSONObject(i).getString(WebParams.MAX_AMOUNT);

                FeeDGIModel feeDGIModel = new FeeDGIModel();
                feeDGIModel.setCcy_id(ccy_id);
                feeDGIModel.setBuyer_fee(buyer_fee);
                feeDGIModel.setSeller_fee(seller_fee);
                feeDGIModel.setCommission_fee(commission_fee);
                feeDGIModel.setMin_amount(min_amount);
                feeDGIModel.setMax_amount(max_amount);

                feeDGIModelArrayList.add(feeDGIModel);
            }

            JSONArray mArrayInvoice = new JSONArray(obj.optString(WebParams.INVOICE_DATA));

            if (invoiceDGIModelArrayList.size() > 0)
                invoiceDGIModelArrayList.clear();

            for (int i = 0; i < mArrayInvoice.length(); i++) {
                buss_scheme_code = mArrayInvoice.getJSONObject(i).getString(WebParams.BUSS_SCHEME_CODE);
                doc_no = mArrayInvoice.getJSONObject(i).getString(WebParams.DOC_NO);
                doc_id = mArrayInvoice.getJSONObject(i).getString(WebParams.DOC_ID);
                amount = mArrayInvoice.getJSONObject(i).getString(WebParams.AMOUNT);
                remain_amount = mArrayInvoice.getJSONObject(i).getString(WebParams.REMAIN_AMOUNT);
                due_date = mArrayInvoice.getJSONObject(i).getString(WebParams.DUE_DATE);
                ccy_id = mArrayInvoice.getJSONObject(i).getString(WebParams.CCY);

                InvoiceDGI invoiceDGI = new InvoiceDGI();
                invoiceDGI.setBuss_scheme_code(buss_scheme_code);
                invoiceDGI.setDoc_no(doc_no);
                invoiceDGI.setDoc_id(doc_id);
                invoiceDGI.setAmount(amount);
                invoiceDGI.setRemain_amount(remain_amount);
                invoiceDGI.setDue_date(due_date);
                invoiceDGI.setCcy(ccy_id);


                invoiceDGIModelArrayList.add(invoiceDGI);
            }
            invoiceDGIAdapter.updateData(invoiceDGIModelArrayList);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initializePaymentType() {
        if (paymentTypeArr.isEmpty())
            for (int i = 0; i < paymentTypeDGIModelArrayList.size(); i++) {
                paymentTypeArr.add(paymentTypeDGIModelArrayList.get(i).getPayment_name());
            }

        paymentTypeAdapter.notifyDataSetChanged();
    }

    public void initializePaymentMethod() {
        if (paymentMethodArr.isEmpty())
            for (int i = 0; i < bankBillerModelArrayList.size(); i++) {
                if (bankBillerModelArrayList.get(i).getProduct_code().equals(DefineValue.SCASH)) {
                    paymentMethodArr.add(getString(R.string.appname));
                    bankBillerModelArrayList.get(i).setProduct_name(getString(R.string.appname));
                }
            }
        paymentMethodAdapter.notifyDataSetChanged();
    }

    public void initializePaymentMethodBG() {
        if (paymentMethodArrBG.isEmpty())
            for (int i = 0; i < bankBillerModelArrayList.size(); i++) {
                paymentMethodArrBG.add(bankBillerModelArrayList.get(i).getBank_name());
            }

        paymentMethodAdapter.notifyDataSetChanged();
    }

    void checkOutPayment(String remark, String noId, String due_date) {

        List<InvoiceDGI> temp = new ArrayList<>();

        JSONArray invoiceList = getInvoice(temp);

        String extraSignature = memberCode + commCodeTagih + mobile_phone;
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_INVOICE_DGI, extraSignature);

        params.put(WebParams.MEMBER_CODE, memberCode);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.COMM_CODE, commCodeTagih);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.SALES_ID, userPhoneID);
        params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
        params.put(WebParams.BUYER_FEE, feeDGIModelArrayList.get(0).getBuyer_fee());
        params.put(WebParams.COMMISSION_FEE, feeDGIModelArrayList.get(0).getCommission_fee());
        params.put(WebParams.PAYMENT_TYPE, paymentTypeDGIModelArrayList.get(sp_payment_type.getSelectedItemPosition()).getPayment_code());
        params.put(WebParams.PAYMENT_REMARK, remark);
        params.put(WebParams.SOURCE_ACCT_BANK, bankBillerModelArrayList.get(sp_payment_method.getSelectedItemPosition()).getBank_code());
        params.put(WebParams.PHONE_NO, mobile_phone);
        params.put(WebParams.BANK_CODE, "008");
        if (!paymentCode.equalsIgnoreCase("CT")) {
            params.put(WebParams.PRODUCT_CODE, "SCASH");
        } else
            params.put(WebParams.PRODUCT_CODE, bankBillerModelArrayList.get(sp_payment_method.getSelectedItemPosition()).getProduct_code());
        params.put(WebParams.INVOICE, invoiceList);
        params.put(WebParams.LATITUDE, sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0));
        params.put(WebParams.LONGITUDE, sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0));
        params.put(WebParams.LOC_TX_ID, txIdPG);
        params.put(WebParams.SOURCE_ACCT_NO, noId);
        params.put(WebParams.DUE_DATE, due_date);

        Timber.d("params req token DGI : %s", params.toString());

        DataManager.getInstance().setListInvoice(temp);
        DataManager.getInstance().setInvoiceParam(params);

        Fragment newFrag = new FragInvoiceDGIConfirm();
        bundle1.putString(DefineValue.PAYMENT_TYPE, paymentTypeDGIModelArrayList.get(sp_payment_type.getSelectedItemPosition()).getPayment_code());
        bundle1.putString(DefineValue.PAYMENT_TYPE_DESC, paymentTypeDGIModelArrayList.get(sp_payment_type.getSelectedItemPosition()).getPayment_name());
        bundle1.putString(DefineValue.CCY_ID, ccy_id);
        bundle1.putString(DefineValue.PRODUCT_CODE, "SCASH");
        bundle1.putString(DefineValue.REMARK, remark);
        bundle1.putString(DefineValue.MOBILE_PHONE, mobile_phone);
        if (isFav) {
            bundle1.putBoolean(DefineValue.IS_FAVORITE, true);
            bundle1.putString(DefineValue.CUST_ID, cust_id);
            bundle1.putString(DefineValue.NOTES, notes);
            bundle1.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.DGI);
            bundle1.putString(DefineValue.PRODUCT_TYPE, DefineValue.DGI);
            bundle1.putString(DefineValue.COMMUNITY_CODE, commCodeTagih);
            bundle1.putString(DefineValue.ANCHOR_ID, anchorId);
        }
        newFrag.setArguments(bundle1);
        if (getActivity() == null) {
            return;
        }
        TagihActivity ftf = (TagihActivity) getActivity();
        ftf.switchContent(newFrag, "Konfirmasi", true);
    }

    public void getBankCashout() {
        try {
            showProgressDialog();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BANKCASHOUT, memberIDLogin);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params get Bank cashout:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BANKCASHOUT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            dismissProgressDialog();
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Timber.tag("getBankCashout").e(object.get("bank_cashout").toString());
                                try {
                                    JSONArray mArrayPaymentMethod = new JSONArray(object.get("bank_cashout").toString());
                                    bankBillerModelArrayList.clear();
                                    for (int i = 0; i < mArrayPaymentMethod.length(); i++) {
                                        bank_code = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.BANK_CODE);
                                        bank_name = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.BANK_NAME);

                                        bank_biller_model bankBillerModel = new bank_biller_model();
                                        bankBillerModel.setBank_code(bank_code);
                                        bankBillerModel.setBank_name(bank_name);
                                        bankBillerModelArrayList.add(bankBillerModel);
                                    }
                                    paymentMethodAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_text_primary_dark, paymentMethodArrBG);
                                    initializePaymentMethodBG();
                                    sp_payment_method.setAdapter(paymentMethodAdapter);

                                    sp_payment_method.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            bank_code = bankBillerModelArrayList.get(position).getBank_code();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {

                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

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
                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            try {
                JSONObject jsonObject = new JSONObject(result.getContents());
                InvoiceDGI invoiceDGI = new InvoiceDGI();
                String docNo = jsonObject.optString(WebParams.DOC_NO, "");
                boolean docNoExist = false;
                for (InvoiceDGI obj : invoiceDGIModelArrayList) {
                    if (obj.getDoc_no().equals(docNo)) {
                        docNoExist = true;
                        break;
                    }
                }
                if (!docNoExist) {
                    invoiceDGI.setDoc_no(jsonObject.optString(WebParams.DOC_NO, ""));
                    invoiceDGI.setRemain_amount(jsonObject.optString(WebParams.AMOUNT, ""));
                    invoiceDGI.setDue_date(jsonObject.optString(WebParams.DUE_DATE, ""));
                    invoiceDGI.setMember_code(jsonObject.optString(WebParams.MEMBER_CODE, ""));
                    invoiceDGI.setReference_number(jsonObject.optString(WebParams.REFERENCE_NUMBER, ""));
                    invoiceDGI.setDevice_key(jsonObject.optString(WebParams.DEVICE_KEY, ""));

                    newInvoiceDGIArrayList.add(invoiceDGI);
                    invoiceDGIModelArrayList.add(invoiceDGI);
                    invoiceDGIAdapter.updateData(invoiceDGIModelArrayList);
                    enableButton(btnCheck);
                    disableButton(btnDone);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.invoice_exist), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkNewInvoice() {
        showProgressDialog();

        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_NEW_INVOICE, commCodeTagih);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.COMM_CODE_TAGIH, commCodeTagih);
        params.put(WebParams.MEMBER_CODE_KEYIN, memberCode);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
        params.put(WebParams.INVOICES, createJsonInvoice());
        params.put(WebParams.TOTAL_INVOICE, newInvoiceDGIArrayList.size());
        Timber.d("params new invoices : %s", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_NEW_INVOICE, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            String errorMessage = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                disableButton(btnCheck);
                                enableButton(btnDone);
                                JSONObject invoicesSuccess = response.getJSONObject(WebParams.INVOICES_SUCCESS);
                                JSONObject invoicesFailed = response.getJSONObject(WebParams.INVOICES_FAILED);
                                updateInvoiceSuccess(invoicesSuccess);
                                showInvoiceFailed(invoicesFailed);
                                newInvoiceDGIArrayList.clear();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                jsonModel model = getGson().fromJson(response.toString(), jsonModel.class);
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        dismissProgressDialog();
                    }

                    @Override
                    public void onComplete() {
                        dismissProgressDialog();
                    }
                });
    }

    private void updateInvoiceSuccess(JSONObject invoices) throws JSONException {
        for (InvoiceDGI invoiceDGI1 : newInvoiceDGIArrayList) {
            JSONObject invoice = invoices.optJSONObject(invoiceDGI1.getDoc_no());
            if (invoice != null) {
                String docRefNo = invoice.optString(WebParams.DOC_REF_NO);
                for (InvoiceDGI invoiceDGI2 : invoiceDGIModelArrayList) {
                    if (invoiceDGI2.getDoc_no().equals(invoiceDGI1.getDoc_no()))
                        invoiceDGI2.setDoc_id(docRefNo);
                }
            }
        }
    }

    private void showInvoiceFailed(JSONObject invoices) throws JSONException {
        StringBuilder message = new StringBuilder();
        ArrayList<InvoiceDGI> tempInvoiceDGIArrayList = new ArrayList<>();
        for (InvoiceDGI invoiceDGI1 : newInvoiceDGIArrayList) {
            JSONObject invoice = invoices.optJSONObject(invoiceDGI1.getDoc_no());
            if (invoice != null) {
                for (InvoiceDGI invoiceDGI2 : invoiceDGIModelArrayList) {
                    if (invoiceDGI2.getDoc_no().equals(invoiceDGI1.getDoc_no())) {
                        tempInvoiceDGIArrayList.add(invoiceDGI2);

                        message.append("Invoice ").append(invoiceDGI1.getDoc_no()).append(" ").append(invoice.getString(WebParams.ERROR_CODE)).append(" ").append(invoice.getString(WebParams.ERROR_MSG)).append(System.lineSeparator());
                    }
                }
            }
        }
        invoiceDGIModelArrayList.removeAll(tempInvoiceDGIArrayList);
        invoiceDGIAdapter.notifyDataSetChanged();
        if (!message.toString().equals(""))
            showDialog(message.toString());
    }

    private void showDialog(String message) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOk = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.invoice_failed));
        Message.setText(message);

        btnDialogOk.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private String createJsonInvoice() {
        JSONArray parentArray = new JSONArray();

        for (InvoiceDGI obj : newInvoiceDGIArrayList) {
            JSONObject parentObject = new JSONObject();
            try {
                parentObject.put(WebParams.DOC_NO, obj.getDoc_no());
                parentObject.put(WebParams.MEMBER_CODE, obj.getMember_code());
                parentObject.put(WebParams.MEMBER_NAME, "");
                parentObject.put(WebParams.AMOUNT, obj.getRemain_amount());
                parentObject.put(WebParams.DUE_DATE, obj.getDue_date());
                parentObject.put(WebParams.REFERENCE_NUMBER, obj.getReference_number());
                parentObject.put(WebParams.DEVICE_KEY, obj.getDevice_key());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            parentArray.put(parentObject);
        }

        return parentArray.toString();
    }

    private void disableButton(Button button) {
        button.setEnabled(false);
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_background_button_disabled, null));
    }

    private void enableButton(Button button) {
        button.setEnabled(true);
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_background_blue, null));
    }
}
