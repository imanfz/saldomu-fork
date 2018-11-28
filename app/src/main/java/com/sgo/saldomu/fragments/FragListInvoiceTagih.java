package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.bank_biller_model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.InvoiceDGIAdapter;
import com.sgo.saldomu.adapter.ListJoinSCADMAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DividerItemDecoration;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.InputAmountTagihBillerDialog;
import com.sgo.saldomu.models.FeeDGIModel;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.models.MobilePhoneModel;
import com.sgo.saldomu.models.PaymentTypeDGIModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragListInvoiceTagih extends BaseFragment {
    View view;
    public static ArrayList<InvoiceDGI> listInovice = new ArrayList<>();
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
    Button btnCancel;
    Button btnBack;
    private AutoCompleteTextView search;
    Spinner sp_payment_type, sp_phone_number, sp_payment_method;
    String mobile_phone, paymentCode, paymentName, ccy_id, buyer_fee, seller_fee, commission_fee, min_amount, max_amount;
    String callback_url, paymentMethod, buss_scheme_code, doc_no, doc_id, remain_amount, amount, due_date;
    private ArrayList<MobilePhoneModel> mobilePhoneModelArrayList = new ArrayList<>();
    private ArrayList<bank_biller_model> bankBillerModelArrayList = new ArrayList<bank_biller_model>();
    private ArrayList<PaymentTypeDGIModel> paymentTypeDGIModelArrayList = new ArrayList<>();
    private ArrayList<PaymentTypeDGIModel> paymentMethodDGIModelArrayList = new ArrayList<>();
    private ArrayList<FeeDGIModel> feeDGIModelArrayList = new ArrayList<FeeDGIModel>();
    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList = new ArrayList<InvoiceDGI>();
    private ArrayAdapter<String> mobilePhoneAdapter;
    private ArrayAdapter<String> paymentTypeAdapter;
    private ArrayAdapter<String> paymentMethodAdapter;
    ArrayList<String> mobilePhoneArr = new ArrayList<>();
    ArrayList<String> paymentTypeArr = new ArrayList<>();
    ArrayList<String> paymentMethodArr = new ArrayList<>();

    InvoiceDGIAdapter invoiceDGIAdapter;

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

        Bundle bundle = getArguments();
        if (bundle != null) {
            response = bundle.getString(DefineValue.RESPONSE, "");
        }

        txtAlert = view.findViewById(R.id.txtAlert);
        prgLoading = view.findViewById(R.id.prgLoading);
        contentLayout = view.findViewById(R.id.content);
        listMenu = view.findViewById(R.id.listMenu);
        tabel_footer = view.findViewById(R.id.tabel_footer);
        row_phone = view.findViewById(R.id.row_phone);
        lbl_total_pay_amount = view.findViewById(R.id.lbl_total_pay_amount);

        btnDone = view.findViewById(R.id.btn_done);
        btnCancel = view.findViewById(R.id.btnCancel);
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
                    invoiceDGIAdapter.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                getFragmentManager().popBackStack();
            }
        });

        parseResponse();
    }

    public void initializeRecyclerview() {
        invoiceDGIAdapter = new InvoiceDGIAdapter(invoiceDGIModelArrayList, getActivity(),
                new InvoiceDGIAdapter.OnTap() {
                    @Override
                    public void onTap(int pos) {
                        showInputDialog(pos);
                    }
                });
        listMenu.setAdapter(invoiceDGIAdapter);
        listMenu.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listMenu.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.row_divider),
                false, false));
    }

    void showInputDialog(int pos){
        InputAmountTagihBillerDialog dialog = InputAmountTagihBillerDialog.newDialog(pos, invoiceDGIModelArrayList.get(pos),
                new InputAmountTagihBillerDialog.OnTap() {
                    @Override
                    public void onTap(int pos, String value) {
                        invoiceDGIModelArrayList.get(pos).setInput_amount(value);
                        invoiceDGIAdapter.notifyItemChanged(pos);
                    }
                });

        dialog.show(getFragmentManager(), "input dialog");
    }

    public void parseResponse() {

        try {
            JSONObject obj = new JSONObject(response);

            JSONArray mArrayPaymentMethod = new JSONArray(obj.optString(WebParams.BANK, ""));

            for (int i = 0; i < mArrayPaymentMethod.length(); i++) {
                paymentMethod = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.PRODUCT_CODE);

                bank_biller_model bankBillerModel = new bank_biller_model();
                bankBillerModel.setProduct_code(paymentMethod);

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
}
