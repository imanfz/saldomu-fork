package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.bank_biller_model;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TagihActivity;
import com.sgo.saldomu.adapter.InvoiceDGIAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DividerItemDecoration;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.InputAmountTagihBillerDialog;
import com.sgo.saldomu.dialogs.PaymentRemarkDialog;
import com.sgo.saldomu.models.FeeDGIModel;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.models.MobilePhoneModel;
import com.sgo.saldomu.models.PaymentTypeDGIModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FragListInvoiceTagih extends BaseFragment {
    View view;
    SecurePreferences sp;
    String response;

    // declare view objects
    RecyclerView listMenu;
    ProgressBar prgLoading;
    TextView txtAlert;
    TextView lbl_header;
    TableLayout tabel_footer;
    TableRow row_phone;
    TextView lbl_total_pay_amount;
    RelativeLayout contentLayout;
    Button btnDone;
    Button btnReset;
    Button btnBack;
    private AutoCompleteTextView search;
    Spinner sp_payment_type, sp_phone_number, sp_payment_method;
    String mobile_phone, paymentCode, paymentName, ccy_id, buyer_fee, seller_fee, commission_fee, min_amount, max_amount;
    String callback_url, paymentMethod, buss_scheme_code, doc_no, doc_id, remain_amount, amount, due_date, bank_code;
    private ArrayList<MobilePhoneModel> mobilePhoneModelArrayList = new ArrayList<>();
    private ArrayList<bank_biller_model> bankBillerModelArrayList = new ArrayList<>();
    private ArrayList<PaymentTypeDGIModel> paymentTypeDGIModelArrayList = new ArrayList<>();
    private ArrayList<PaymentTypeDGIModel> paymentMethodDGIModelArrayList = new ArrayList<>();
    private ArrayList<FeeDGIModel> feeDGIModelArrayList = new ArrayList<>();
    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList = new ArrayList<>();
    private ArrayAdapter<String> mobilePhoneAdapter;
    private ArrayAdapter<String> paymentTypeAdapter;
    private ArrayAdapter<String> paymentMethodAdapter;
    ArrayList<String> mobilePhoneArr = new ArrayList<>();
    ArrayList<String> paymentTypeArr = new ArrayList<>();
    ArrayList<String> paymentMethodArr = new ArrayList<>();

    String partialPayment, memberCode, commCodeTagih, paymentRemark;

    InvoiceDGIAdapter invoiceDGIAdapter;

    Bundle bundle1 = new Bundle();

    int total;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_list_invoice_dgi, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        dismissProgressDialog();

        final Bundle bundle = getArguments();
        if (bundle != null) {
            response = bundle.getString(DefineValue.RESPONSE, "");
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCodeTagih = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        }

        txtAlert = view.findViewById(R.id.txtAlert);
        prgLoading = view.findViewById(R.id.prgLoading);
        contentLayout = view.findViewById(R.id.content);
        listMenu = view.findViewById(R.id.listMenu);
        tabel_footer = view.findViewById(R.id.tabel_footer);
        row_phone = view.findViewById(R.id.row_phone);
        lbl_total_pay_amount = view.findViewById(R.id.lbl_total_pay_amount);

        btnDone = view.findViewById(R.id.btn_done);
        btnReset = view.findViewById(R.id.btnReset);
        btnBack = view.findViewById(R.id.btn_back);
        sp_payment_type = view.findViewById(R.id.cbo_payment_type);
        sp_payment_method = view.findViewById(R.id.sp_metode_pembayaran);
        sp_phone_number = view.findViewById(R.id.cbo_phone_number);
        search = view.findViewById(R.id.search);

        invoiceDGIModelArrayList = new ArrayList<>();

        initializeRecyclerview();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.equals("")) {

                } else {
//                    invoiceDGIAdapter.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                invoiceDGIAdapter.getFilter().filter(editable.toString());
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                getFragmentManager().popBackStack();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetData();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lbl_total_pay_amount.getText().toString().equalsIgnoreCase("0")){
                    Toast.makeText(getActivity(), "Tidak ada invoice yang dibayarkan", Toast.LENGTH_SHORT).show();
                }else {
                    PaymentRemarkDialog dialog = PaymentRemarkDialog.newDialog(new PaymentRemarkDialog.onTap() {
                        @Override
                        public void onOK(String msg) {
                            paymentRemark = msg;
                            checkOutPayment(msg);
                            bundle.putString(DefineValue.REMARK, paymentRemark);
                        }
                    });
                    dialog.show(getFragmentManager(), "paymentremark dialog");
                }
            }
        });

        parseResponse();
    }

    public void initializeRecyclerview() {
        invoiceDGIAdapter = new InvoiceDGIAdapter(invoiceDGIModelArrayList, getActivity(),
                new InvoiceDGIAdapter.OnTap() {
                    @Override
                    public void onTap(InvoiceDGI model) {
                        showInputDialog(model);
                    }
                });
        listMenu.setAdapter(invoiceDGIAdapter);
        listMenu.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listMenu.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.row_divider),
                false, false));
    }

    void showInputDialog(InvoiceDGI model) {
        int pos = invoiceDGIModelArrayList.indexOf(model);
        InputAmountTagihBillerDialog dialog = InputAmountTagihBillerDialog.newDialog(pos, invoiceDGIModelArrayList.get(pos), partialPayment,
                new InputAmountTagihBillerDialog.OnTap() {
                    @Override
                    public void onTap(int pos, String value) {
                        invoiceDGIModelArrayList.get(pos).setInput_amount(value);
                        invoiceDGIAdapter.notifyItemChanged(pos);

                        countTotalPrice();

                        bundle1.putString(DefineValue.TOTAL_AMOUNT, String.valueOf(total));

                    }
                });

        dialog.show(getFragmentManager(), "input dialog");
    }

    void countTotalPrice() {
        total = 0;
        for (InvoiceDGI obj : invoiceDGIModelArrayList
                ) {
            if (Integer.valueOf(obj.getInput_amount()) != 0) {
                total += Integer.valueOf(obj.getInput_amount());
            }
        }

        lbl_total_pay_amount.setText(String.valueOf(total));
    }

    void resetData()
    {
        for (InvoiceDGI obj: invoiceDGIModelArrayList
             ) {
            obj.setInput_amount("0");
        }

        invoiceDGIAdapter.notifyDataSetChanged();

        countTotalPrice();
    }

    JSONArray getInv(List<InvoiceDGI> temp) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (InvoiceDGI obj : invoiceDGIModelArrayList
                    ) {
                if (Integer.valueOf(obj.getInput_amount()) != 0) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("doc_id", obj.getDoc_id());
                    jsonObject.put("doc_no", obj.getDoc_no());
                    jsonObject.put("amount", obj.getInput_amount());
                    jsonArray.put(jsonObject);
                    temp.add(obj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    public void parseResponse() {

        try {
            JSONObject obj = new JSONObject(response);

            partialPayment = obj.optString("partial_payment", "");

            JSONArray mArrayPaymentMethod = new JSONArray(obj.optString(WebParams.BANK, ""));

            for (int i = 0; i < mArrayPaymentMethod.length(); i++) {
                paymentMethod = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.PRODUCT_CODE);
                bank_code = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.BANK_CODE);

                bank_biller_model bankBillerModel = new bank_biller_model();
                bankBillerModel.setProduct_code(paymentMethod);
                bankBillerModel.setBank_code(bank_code);

                bankBillerModelArrayList.add(bankBillerModel);
            }

            paymentMethodAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, paymentMethodArr);
            sp_payment_method.setAdapter(paymentMethodAdapter);

            initializePaymentMethod();

            JSONArray mArrayMobilePhone = new JSONArray(obj.optString(WebParams.PHONE_DATA, ""));

            for (int i = 0; i < mArrayMobilePhone.length(); i++) {
                mobile_phone = mArrayMobilePhone.getJSONObject(i).getString(WebParams.MOBILE_PHONE);

                MobilePhoneModel mobilePhoneModel = new MobilePhoneModel();
                mobilePhoneModel.setMobile_phone(mobile_phone);

                mobilePhoneModelArrayList.add(mobilePhoneModel);
            }

            mobilePhoneAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mobilePhoneArr);
            sp_phone_number.setAdapter(mobilePhoneAdapter);

            initializeMobilePhone();

            JSONArray mArrayPaymentType = new JSONArray(obj.optString(WebParams.PAYMENT_TYPE));

            for (int i = 0; i < mArrayPaymentType.length(); i++) {
                paymentCode = mArrayPaymentType.getJSONObject(i).getString(WebParams.PAYMENT_CODE);
                paymentName = mArrayPaymentType.getJSONObject(i).getString(WebParams.PAYMENT_NAME);

                PaymentTypeDGIModel paymentTypeDGIModel = new PaymentTypeDGIModel();
                paymentTypeDGIModel.setPayment_code(paymentCode);
                paymentTypeDGIModel.setPayment_name(paymentName);

                paymentTypeDGIModelArrayList.add(paymentTypeDGIModel);
            }

            paymentTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, paymentTypeArr);
            sp_payment_type.setAdapter(paymentTypeAdapter);

            sp_payment_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            initializePaymentType();

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

    public void initializeMobilePhone() {
        for (int i = 0; i < mobilePhoneModelArrayList.size(); i++) {
            mobilePhoneArr.add(mobilePhoneModelArrayList.get(i).getMobile_phone());
        }

        mobilePhoneAdapter.notifyDataSetChanged();
    }

    public void initializePaymentType() {
        for (int i = 0; i < paymentTypeDGIModelArrayList.size(); i++) {
            paymentTypeArr.add(paymentTypeDGIModelArrayList.get(i).getPayment_name());
        }

        paymentTypeAdapter.notifyDataSetChanged();
    }

    public void initializePaymentMethod() {

        ArrayList<String> tempDataPaymentName = new ArrayList<>();

//        for (int i = 0; i < bankBillerModelArrayList.size(); i++) {
//            paymentMethodArr.add(bankBillerModelArrayList.get(i).getProduct_code());
//        }

        for (int i = 0; i < bankBillerModelArrayList.size(); i++) {
            if (bankBillerModelArrayList.get(i).getProduct_code().equals(DefineValue.SCASH)) {
                paymentMethodArr.add(getString(R.string.appname));
//                    tempDataPaymentName.add(getString(R.string.appname));
                bankBillerModelArrayList.get(i).setProduct_name(getString(R.string.appname));
            } else {
                tempDataPaymentName.add(bankBillerModelArrayList.get(i).getProduct_code());
            }
        }

        paymentMethodAdapter.notifyDataSetChanged();
    }

    void checkOutPayment(String remark) {

        String phone_no = mobilePhoneArr.get(sp_phone_number.getSelectedItemPosition());

        List<InvoiceDGI> temp = new ArrayList<>();

        JSONArray invoiceList = getInv(temp);

        String extraSignature = memberCode + commCodeTagih + phone_no;
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_INVOICE_DGI, extraSignature);

//        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.MEMBER_CODE, memberCode);
        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.COMM_CODE, commCodeTagih);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.SALES_ID, userPhoneID);
        params.put(WebParams.CCY_ID, "IDR");
        params.put(WebParams.BUYER_FEE, feeDGIModelArrayList.get(0).getBuyer_fee());
        params.put(WebParams.COMMISSION_FEE, feeDGIModelArrayList.get(0).getCommission_fee());
        params.put(WebParams.PAYMENT_TYPE, paymentTypeDGIModelArrayList.get(sp_payment_type.getSelectedItemPosition()).getPayment_code());
        params.put(WebParams.PAYMENT_REMARK, remark);
        params.put(WebParams.SOURCE_ACCT_BANK, "");
        params.put(WebParams.PHONE_NO, phone_no);
        params.put(WebParams.BANK_CODE, bankBillerModelArrayList.get(sp_payment_method.getSelectedItemPosition()).getBank_code());
        params.put(WebParams.PRODUCT_CODE, bankBillerModelArrayList.get(sp_payment_method.getSelectedItemPosition()).getProduct_code());
        params.put(WebParams.INVOICE, invoiceList);

        Timber.d("params list invoice DGI : " + params.toString());

        DataManager.getInstance().setListInvoice(temp);
        DataManager.getInstance().setInvoiceParam(params);

        Fragment newFrag = new FragInvoiceDGIConfirm();
        bundle1.putString(DefineValue.PAYMENT_TYPE, paymentTypeDGIModelArrayList.get(sp_payment_type.getSelectedItemPosition()).getPayment_code());
        bundle1.putString(DefineValue.PAYMENT_TYPE_DESC, paymentTypeDGIModelArrayList.get(sp_payment_type.getSelectedItemPosition()).getPayment_name());
        bundle1.putString(DefineValue.CCY_ID, ccy_id);
        bundle1.putString(DefineValue.PRODUCT_CODE, bankBillerModelArrayList.get(sp_payment_method.getSelectedItemPosition()).getProduct_code());
        bundle1.putString(DefineValue.REMARK, remark);
        bundle1.putString(DefineValue.MOBILE_PHONE, phone_no);
        newFrag.setArguments(bundle1);
        if(getActivity() == null){
            return;
        }
        TagihActivity ftf = (TagihActivity) getActivity();
        ftf.switchContent(newFrag,"Konfirmasi",true);


    }
}
