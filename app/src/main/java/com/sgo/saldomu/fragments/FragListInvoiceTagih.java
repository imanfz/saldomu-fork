package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.WebParams;
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
    ListView listMenu;
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
    String mobile_phone, paymentCode, paymentName, docNo, remainAmount, dueDate, ccy_id, buyer_fee, seller_fee, commission_fee, min_amount, max_amount;
    String biller_comm_id, biller_name, biller_comm_code, biller_api_key,callback_url, paymentMethod;
    private ArrayList<MobilePhoneModel> mobilePhoneModelArrayList = new ArrayList<>();
    private ArrayList<bank_biller_model> bankBillerModelArrayList = new ArrayList<bank_biller_model>();
    private ArrayList<PaymentTypeDGIModel> paymentTypeDGIModelArrayList = new ArrayList<>();
    private ArrayList<FeeDGIModel> feeDGIModelArrayList = new ArrayList<FeeDGIModel>();
    private ArrayAdapter<String> mobilePhoneAdapter;
    private ArrayAdapter<String> paymentTypeAdapter;
    ArrayList<String> mobilePhoneArr = new ArrayList<>();
    ArrayList<String> paymentTypeArr = new ArrayList<>();

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
            response = bundle.getString(DefineValue.RESPONSE,"");
        }

        txtAlert = (TextView) view.findViewById(R.id.txtAlert);
        prgLoading = (ProgressBar) view.findViewById(R.id.prgLoading);
        contentLayout = view.findViewById(R.id.content);
        listMenu   = (ListView) view.findViewById(R.id.listMenu);
        tabel_footer = (TableLayout) view.findViewById(R.id.tabel_footer);
        row_phone = (TableRow) view.findViewById(R.id.row_phone);
        lbl_total_pay_amount = (TextView) view.findViewById(R.id.lbl_total_pay_amount);

        btnDone               = (Button) view.findViewById(R.id.btn_done);
        btnCancel             = (Button) view.findViewById(R.id.btnCancel);
        btnBack               = (Button) view.findViewById(R.id.btn_back);
        sp_payment_type       = (Spinner)view.findViewById(R.id.cbo_payment_type);
        sp_payment_method     = (Spinner)view.findViewById(R.id.sp_metode_pembayaran);
        sp_phone_number       = (Spinner)view.findViewById(R.id.cbo_phone_number);
        search                = (AutoCompleteTextView) view.findViewById(R.id.search);

        parseResponse();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.equals("")){

                }else{
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


    }

    public void parseResponse()
    {

        try {
            JSONObject obj = new JSONObject(response);

            JSONArray mArrayPaymentMethod = new JSONArray(obj.optString(WebParams.BANK,""));

            for (int i = 0; i < mArrayPaymentMethod.length(); i++) {
                paymentMethod = mArrayPaymentMethod.getJSONObject(i).getString(WebParams.PRODUCT_CODE);

                bank_biller_model bankBillerModel = new bank_biller_model();
                bankBillerModel.setProduct_code(paymentMethod);

                bankBillerModelArrayList.add(bankBillerModel);
            }

            JSONArray mArrayMobilePhone = new JSONArray(obj.optString(WebParams.PHONE_DATA,""));

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



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initializeMobilePhone()
    {
        for(int i=0; i<mobilePhoneModelArrayList.size(); i++) {
            mobilePhoneArr.add(mobilePhoneModelArrayList.get(i).getMobile_phone());
        }

        mobilePhoneAdapter.notifyDataSetChanged();
    }

    public void initializePaymentType()
    {
        for(int i=0; i<paymentTypeDGIModelArrayList.size(); i++) {
            paymentTypeArr.add(paymentTypeDGIModelArrayList.get(i).getPayment_name());
        }

        paymentTypeAdapter.notifyDataSetChanged();
    }
}
