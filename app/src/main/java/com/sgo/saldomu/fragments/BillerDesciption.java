package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.bank_biller_model;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BillerActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.models.retrofit.InqBillerModel;
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import timber.log.Timber;

/*
  Created by Administrator on 5/21/2015.
 */
public class BillerDesciption extends BaseFragment {

    public final static int REQUEST_BillerInqReq = 22 ;
    public final static String TAG = "BILLER_DESCRIPTION";

    private View v;
    private View layout_biller_name;
    private String tx_id;
    private String biller_name;
    private String biller_comm_id;
    private String biller_comm_code;
    private String biller_api_key;
    private String ccy_id;
    private String amount;
    private String item_name;
    private String description;
    private String cust_id;
    private String item_id;
    private String payment_name;
    private String shareType;
    private String callback_url;
    private String biller_type_code;
    private String value_item_data;
    private TextView tv_biller_name_value;
    private TextView tv_item_name_value;
    private TextView tv_amount_value, tv_total_value;
    private TextView tv_id_cust;
    private EditText et_desired_amount;
    private Button btn_submit;
    private Button btn_cancel;
    private int buy_type;
    private Boolean  is_input_amount;
    private Boolean is_display_amount;
    Boolean isFacebook = false;
    private Boolean isShowDescription = false;
    private ProgressDialog progdialog;
    private ImageView mIconArrow;
    private TableLayout mTableLayout;
    private listBankModel mTempBank;
    private Spinner spin_payment_options;
    private List<String> paymentData;
    private ArrayAdapter<String> adapterPaymentOptions;
    private Biller_Data_Model mBillerData;
    private List<bank_biller_model> mListBankBiller;
    private Realm realm;
    Boolean isPLN = false;
    String fee ="0", deAmount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_biller_description, container, false);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tv_item_name_value = v.findViewById(R.id.billertoken_item_name_value);
        tv_amount_value = v.findViewById(R.id.billertoken_amount_value);
        tv_total_value = v.findViewById(R.id.billertoken_total_value);
        btn_submit = v.findViewById(R.id.billertoken_btn_verification);
        btn_cancel = v.findViewById(R.id.billertoken_btn_cancel);
        layout_biller_name = v.findViewById(R.id.billertoken_layout_biller_name);
        tv_id_cust = v.findViewById(R.id.billertoken_biller_id_value);
        spin_payment_options = v.findViewById(R.id.spinner_billerinput_payment_options);

        btn_submit.setOnClickListener(submitListener);
        btn_cancel.setOnClickListener(cancelListener);

        realm = Realm.getInstance(RealmManager.BillerConfiguration);

        initializeData();

        if(isAdded())
            sentInquiryBiller();

    }

    private void initializeData(){
        Bundle args = getArguments();
        Timber.d("isi args biller description:"+args.toString());

        biller_comm_id = args.getString(DefineValue.COMMUNITY_ID);
        biller_name = args.getString(DefineValue.COMMUNITY_NAME, "");
        shareType = args.getString(DefineValue.SHARE_TYPE,"");
        item_id = args.getString(DefineValue.ITEM_ID,"");
        cust_id = args.getString(DefineValue.CUST_ID,"");
        buy_type = args.getInt(DefineValue.BUY_TYPE, 0);
        biller_type_code = args.getString(DefineValue.BILLER_TYPE);
        if(args.containsKey(DefineValue.VALUE_ITEM_DATA))
            value_item_data = args.getString(DefineValue.VALUE_ITEM_DATA);

        mBillerData = realm.where(Biller_Data_Model.class).
                equalTo(WebParams.COMM_ID,biller_comm_id).
                equalTo(WebParams.COMM_NAME,biller_name).
                findFirst();

        mListBankBiller = realm.copyFromRealm(mBillerData.getBank_biller_models());

        biller_comm_code = mBillerData.getComm_code();
        biller_api_key = mBillerData.getApi_key();
        callback_url = mBillerData.getCallback_url();

        if(biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS)||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_NON_TAG)||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN)||
                biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN_TKN)){
            isPLN = true;
        }

    }

    private void initializeLayout() {

        tv_item_name_value.setText(item_name);
        tv_id_cust.setText(cust_id);
        tv_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(amount));
        View amount_layout = v.findViewById(R.id.billertoken_amount_layout);
        if (is_display_amount) {
            amount_layout.setVisibility(View.VISIBLE);
        }
        int buy_type2 = buy_type;
        if (buy_type == BillerActivity.PURCHASE_TYPE) {
            tv_biller_name_value = v.findViewById(R.id.billertoken_biller_name_value);
            tv_biller_name_value.setText(biller_name);
            if (is_display_amount && biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_PLN_TKN)) {
                initializeDescriptionLayout();
            }
        } else if (buy_type == BillerActivity.PAYMENT_TYPE) {
            layout_biller_name.setVisibility(View.GONE);
            if (is_display_amount) {
                initializeDescriptionLayout();
            }

            if (is_input_amount) {
                View inputAmountLayout = v.findViewById(R.id.billertoken_layout_amount_desired);
                inputAmountLayout.setVisibility(View.VISIBLE);
                et_desired_amount = v.findViewById(R.id.billertoken_amount_desired_value);
                et_desired_amount.addTextChangedListener(jumlahChangeListener);
            }
        }

//        if(isPLN){
            View layout_fee = v.findViewById(R.id.billertoken_fee_layout);
            View layout_total = v.findViewById(R.id.billertoken_total_layout);
            ((TextView)(layout_fee.findViewById(R.id.billertoken_fee_value))).setText(ccy_id + ". " +CurrencyFormat.format(fee));
            layout_fee.setVisibility(View.VISIBLE);
            ((TextView)(layout_total.findViewById(R.id.billertoken_total_value))).setText(ccy_id + ". " +CurrencyFormat.format(amount));
            layout_total.setVisibility(View.VISIBLE);
            double mAmount = Double.parseDouble(amount) - Double.parseDouble(fee);
            deAmount = String.valueOf(mAmount);
            tv_amount_value.setText(ccy_id + ". " + CurrencyFormat.format(mAmount));
            tv_total_value.setText(ccy_id + ". " + CurrencyFormat.format(amount));
//            }

        paymentData = new ArrayList<>();
        adapterPaymentOptions = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paymentData);
        adapterPaymentOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_payment_options.setAdapter(adapterPaymentOptions);
        spin_payment_options.setOnItemSelectedListener(spinnerPaymentListener);

        if (isVisible()) {
            ArrayList<String> tempDataPaymentName = new ArrayList<>();
            paymentData.add(getString(R.string.billerinput_text_spinner_default_payment));

            for (int i = 0; i < mListBankBiller.size(); i++) {
                if (mListBankBiller.get(i).getProduct_code().equals(DefineValue.SCASH)) {
                    paymentData.add(getString(R.string.appname));
//                    tempDataPaymentName.add(getString(R.string.appname));
                    mListBankBiller.get(i).setProduct_name(getString(R.string.appname));
                } else {
                    tempDataPaymentName.add(mListBankBiller.get(i).getProduct_name());
                }
            }
            if (!tempDataPaymentName.isEmpty())
                Collections.sort(tempDataPaymentName);

            paymentData.addAll(tempDataPaymentName);
            adapterPaymentOptions.notifyDataSetChanged();

            spin_payment_options.setSelection(1); //set metode pembayaran jadi saldomu
        }

        if (progdialog != null && progdialog.isShowing())
            progdialog.dismiss();
    }
    private void initializeDescriptionLayout(){
        isShowDescription = true;
        try {
            View mDescLayout = v.findViewById(R.id.billertoken_layout_deskripsi);
            mDescLayout.setVisibility(View.VISIBLE);

            mTableLayout = v.findViewById(R.id.billertoken_layout_table);
            mIconArrow = mDescLayout.findViewById(R.id.billertoken_arrow_desc);

            mIconArrow.setOnClickListener(descriptionClickListener);
            mDescLayout.setOnClickListener(descriptionClickListener);

            JSONObject mDataDesc = new JSONObject(description);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;
            String value_detail_field,value_detail_value;
            Iterator keys = mDataDesc.keys();
            List<String> tempList = new ArrayList<>();

            //jika BPJS sorting fieldnya sesuai format
//            if(biller_type_code.equalsIgnoreCase(DefineValue.BILLER_TYPE_BPJS)) {
//                tempList = JsonSorting.BPJSInquirySortingField();
//            }
//            else {
                while (keys.hasNext()) {
                    tempList.add((String) keys.next());
                }
//            }
//            Collections.sort(tempList);

            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT,8f);
            rowParams.setMargins(6,6,6,6);

            mTableLayout.removeAllViews();
            for (String aTempList : tempList) {
                value_detail_field = aTempList;
                value_detail_value = mDataDesc.getString(aTempList);

                detail_field = new TextView(getActivity());
                detail_field.setGravity(Gravity.LEFT);
                detail_field.setLayoutParams(rowParams);
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.RIGHT);
                detail_value.setLayoutParams(rowParams);
                detail_value.setTypeface(Typeface.DEFAULT_BOLD);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(value_detail_field);
                detail_value.setText(value_detail_value);
                mTableLayout.addView(layout_table_row);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("0")) et_desired_amount.setText("");
            if(s.length() > 0 && s.charAt(0) == '0'){
                int i = 0;
                for (; i < s.length(); i++){
                    if(s.charAt(i) != '0')break;
                }
                et_desired_amount.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerPaymentListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Object item = adapterView.getItemAtPosition(i);
            payment_name = item.toString();
//            if(payment_name.equals(getString(R.string.appname)))
//            {
//                payment_name.equalsIgnoreCase("S-Cash");
//            }
            for (i = 0; i < mListBankBiller.size() ; i++ ){
                if(payment_name.equals(mListBankBiller.get(i).getProduct_name())){
                    mTempBank = new listBankModel(mListBankBiller.get(i).getBank_code(),
                                                    mListBankBiller.get(i).getBank_name(),
                                                    mListBankBiller.get(i).getProduct_code(),
                                                    mListBankBiller.get(i).getProduct_name(),
                                                    mListBankBiller.get(i).getProduct_type(),
                                                    mListBankBiller.get(i).getProduct_h2h());
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    private View.OnClickListener descriptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Animation mRotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_arrow);
            mRotate.setInterpolator(new LinearInterpolator());
            mRotate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIconArrow.invalidate();
                    if(mTableLayout.getVisibility() == View.VISIBLE){
                        mIconArrow.setImageResource(R.drawable.ic_circle_arrow_down);
                        mTableLayout.setVisibility(View.GONE);
                    }
                    else {
                        mIconArrow.setImageResource(R.drawable.ic_circle_arrow);
                        mTableLayout.setVisibility(View.VISIBLE);
                    }
                    mIconArrow.invalidate();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mIconArrow.startAnimation(mRotate);

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(inputValidation() ){
                    btn_submit.setEnabled(false);
                    String _amount;

                    if(is_input_amount)
                        _amount = et_desired_amount.getText().toString();
                    else
                        _amount = amount;

                    sentPaymentBiller(_amount);
                }

            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    private void sentInquiryBiller(){
        try{

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            extraSignature = biller_comm_id+item_id+cust_id;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_BILLER, extraSignature);
            params.put(WebParams.DENOM_ITEM_ID, item_id);
            params.put(WebParams.DENOM_ITEM_REMARK, cust_id);
            params.put(WebParams.COMM_ID, biller_comm_id);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID_REMARK,MyApiClient.COMM_ID);

            Timber.d("isi params sent inquiry biller:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_BILLER, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            InqBillerModel model = getGson().fromJson(object, InqBillerModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                is_input_amount = model.getBiller_input_amount().equals(DefineValue.STRING_YES);
                                is_display_amount = model.getBiller_display_amount().equals(DefineValue.STRING_YES);

                                tx_id = model.getTx_id();
                                item_id = model.getItem_id();
                                ccy_id = model.getCcy_id();
                                amount = model.getAmount();
                                item_name = model.getItem_name();
                                description = model.getDescription().getPhoneNumber();
//                            if(isPLN ) {
                                fee = model.getAdmin_fee();
//                            }

                                if(isAdded())
                                    initializeLayout();
                                else
                                    progdialog.dismiss();

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                code = model.getError_code() + " : " + model.getError_message();
                                if(isVisible()) {

                                    if(MyApiClient.PROD_FAILURE_FLAG)
                                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack();
                                }
                            }
                            if(progdialog.isShowing())
                                progdialog.dismiss();

                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void sentPaymentBiller(final String _amount){
        try{

            progdialog.show();

            final String bank_code = mTempBank.getBank_code();
            final String product_code = mTempBank.getProduct_code();

            extraSignature = tx_id+item_id+biller_comm_id+product_code;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAYMENT_BILLER, extraSignature);
            params.put(WebParams.DENOM_ITEM_ID, item_id);
            params.put(WebParams.DENOM_ITEM_REMARK, cust_id );

            params.put(WebParams.TX_ID, tx_id);
            params.put(WebParams.AMOUNT, _amount);

            params.put(WebParams.BANK_CODE, bank_code);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.COMM_ID,biller_comm_id);
            params.put(WebParams.MEMBER_CUST, sp.getString(DefineValue.CUST_ID,""));
            params.put(WebParams.DATETIME, DateTimeFormat.getCurrentDateTime());

            params.put(WebParams.COMM_CODE,biller_comm_code);
            params.put(WebParams.USER_COMM_CODE,sp.getString(DefineValue.COMMUNITY_CODE,""));

            params.put(WebParams.PRODUCT_H2H,mTempBank.getProduct_h2h());
            params.put(WebParams.PRODUCT_TYPE,mTempBank.getProduct_type());
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params sent payment biller:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_PAYMENT_BILLER, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            SentPaymentBillerModel model = getGson().fromJson(object, SentPaymentBillerModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                if(mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB)){
                                    changeToConfirmBiller(fee, model.getMerchant_type(),
                                            bank_code,product_code,-1);
                                    progdialog.dismiss();
                                    btn_submit.setEnabled(true);
                                }
                                else {
                                    int attempt = model.getFailed_attempt();
                                    if(attempt != -1)
                                        attempt = model.getMax_failed() - attempt ;

                                    sentDataReqToken(tx_id,product_code,biller_comm_code,fee,
                                            model.getMerchant_type(),bank_code,attempt);
                                }

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                code = model.getError_code() + " : " + model.getError_message();
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                progdialog.dismiss();
                                btn_submit.setEnabled(true);
                                getFragmentManager().popBackStack();
                            }

                            if(progdialog.isShowing())
                                progdialog.dismiss();
                            btn_submit.setEnabled(true);
                        }
                    });

        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void sentDataReqToken(final String _tx_id, final String _product_code, final String _comm_code, final String fee,
                                  final String merchant_type, final String _bank_code, final int _attempt){
        try{

            extraSignature = tx_id+_comm_code+_product_code;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);
            params.put(WebParams.COMM_CODE, _comm_code);
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.PRODUCT_CODE, _product_code);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params regtoken Sgo+:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
//                                Timber.d("response reqtoken :"+response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                if(mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS))
                                    showDialog(fee,merchant_type,_product_code,_bank_code);
                                else if(merchant_type.equals(DefineValue.AUTH_TYPE_OTP))
                                    showDialog(fee,merchant_type,_product_code,_bank_code);
                                else
                                    changeToConfirmBiller(fee,merchant_type,_bank_code,_product_code,_attempt);

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else if(code.equals(ErrorDefinition.WRONG_PIN_BILLER)){
                                code = model.getError_message();
                                showDialogError(code);

                            }
                            else {
                                String code_msg = model.getError_message();
                                switch (code) {
                                    case "0059":
                                        showDialogSMS(mTempBank.getBank_name());
                                        break;
                                    case ErrorDefinition.ERROR_CODE_LESS_BALANCE:
                                        String message_dialog = "\"" + code_msg + "\" \n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname));

                                        AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                                message_dialog, getString(R.string.ok), getString(R.string.cancel), false);
                                        dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent mI = new Intent(getActivity(), TopUpActivity.class);
                                                mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                                                startActivityForResult(mI, REQUEST_BillerInqReq);
                                            }
                                        });
                                        dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                sentInquiryBiller();
                                            }
                                        });
                                        dialog_frag.setTargetFragment(BillerDesciption.this, 0);
                                        dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
                                        break;
                                    default:
                                        code = model.getError_code() + " : " + model.getError_message();
                                        Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                        getFragmentManager().popBackStack();
                                        break;
                                }

                            }
                            btn_submit.setEnabled(true);
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialogError(String message){
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), getString(R.string.error),
                message,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        getFragmentManager().popBackStack();
                    }
                }
        );
        dialognya.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BillerInqReq) {
                sentInquiryBiller();
        }
    }

    private void showDialogSMS(final String _nama_bank) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        final LevelClass levelClass = new LevelClass(getActivity());
        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.topup_dialog_not_registered));
        Message.setText(getString(R.string.topup_not_registered,_nama_bank));
        btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));
        if(levelClass.isLevel1QAC())
            btnDialogOTP.setText(getString(R.string.ok));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!levelClass.isLevel1QAC()) {
                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME, _nama_bank);
                    switchActivity(newIntent);
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDialog(final String fee, final String merchant_type, final String product_code, final String bank_code) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.smsBanking_dialog_validation_title));
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.appname)+" "+getString(R.string.dialog_token_message_sms));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToConfirmBiller(fee, merchant_type, bank_code, product_code,-1);

                dialog.dismiss();
            }
        });


        dialog.show();
    }



    private void changeToConfirmBiller(String fee, String merchant_type, String bank_code,
                                       String product_code, int attempt) {

        Bundle mArgs = new Bundle();
        mArgs.putBoolean(DefineValue.IS_SHOW_DESCRIPTION,isShowDescription);
        mArgs.putString(DefineValue.TX_ID,tx_id);
        mArgs.putString(DefineValue.CCY_ID,ccy_id);
        mArgs.putString(DefineValue.AMOUNT, deAmount);
        mArgs.putString(DefineValue.ITEM_NAME,item_name);
        mArgs.putString(DefineValue.BILLER_COMM_ID,biller_comm_id);
        mArgs.putString(DefineValue.BILLER_NAME,biller_name);
        mArgs.putString(DefineValue.BILLER_ITEM_ID, item_id);
        mArgs.putString(DefineValue.PAYMENT_NAME, payment_name);
        mArgs.putString(DefineValue.CUST_ID, cust_id);
        mArgs.putInt(DefineValue.BUY_TYPE, buy_type);
        mArgs.putString(DefineValue.BILLER_COMM_CODE,biller_comm_code);
        mArgs.putString(DefineValue.BILLER_API_KEY,biller_api_key);
        mArgs.putString(DefineValue.CALLBACK_URL,callback_url);
        mArgs.putString(DefineValue.ITEM_ID,item_id);
        mArgs.putString(DefineValue.FEE, fee);
        double totalAmount = Double.parseDouble(amount) + Double.parseDouble(fee);
        mArgs.putString(DefineValue.TOTAL_AMOUNT, amount);
        mArgs.putString(DefineValue.PRODUCT_PAYMENT_TYPE, mTempBank.getProduct_type());
        mArgs.putString(DefineValue.BILLER_TYPE, biller_type_code);

        mArgs.putString(DefineValue.BANK_CODE, bank_code);
        mArgs.putString(DefineValue.PRODUCT_CODE, product_code);

        mArgs.putBoolean(DefineValue.IS_DISPLAY, is_display_amount);
        mArgs.putBoolean(DefineValue.IS_INPUT, is_input_amount);
        mArgs.putString(DefineValue.SHARE_TYPE, shareType);

        mArgs.putBoolean(DefineValue.IS_SGO_PLUS, mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB));
        mArgs.putString(DefineValue.AUTHENTICATION_TYPE, merchant_type);
        mArgs.putInt(DefineValue.ATTEMPT, attempt);

        if(is_display_amount)
            mArgs.putString(DefineValue.DESCRIPTION,description);

        if(is_input_amount) {
            String desired_amount = et_desired_amount.getText().toString();
            totalAmount = Double.parseDouble(desired_amount) +  Double.parseDouble(fee);
            mArgs.putString(DefineValue.AMOUNT_DESIRED,desired_amount );
            mArgs.putString(DefineValue.TOTAL_AMOUNT, String.valueOf(totalAmount));
        }
        if(isPLN){
            double mAmount = Double.parseDouble(amount) - Double.parseDouble(fee);
            String deAmount = String.valueOf(mAmount);
            mArgs.putString(DefineValue.AMOUNT, deAmount);
            mArgs.putString(DefineValue.TOTAL_AMOUNT, String.valueOf(amount));
            }

        Fragment newFrag = new BillerConfirm();
        newFrag.setArguments(mArgs);
        switchFragment(newFrag, BillerActivity.FRAG_BIL_DESCRIPTION,null,true, BillerConfirm.TAG);
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name,String next_name, Boolean isBackstack, String tag){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity ) getActivity();
        fca.switchContent(i,name,next_name,isBackstack, tag);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        BillerActivity fca = (BillerActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }


    private Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager().popBackStack();
        }
    };


    private boolean inputValidation(){
        if(is_input_amount){
            if(et_desired_amount.getText().toString().length()==0){
                et_desired_amount.requestFocus();
                et_desired_amount.setError(this.getString(R.string.billertoken_validation_payment_input_amount));
                return false;
            }
            else if(Long.parseLong(et_desired_amount.getText().toString()) < 1){
                et_desired_amount.requestFocus();
                et_desired_amount.setError(getString(R.string.payfriends_amount_zero));
                return false;
            }
        }

        if(payment_name!=null) {
            if (payment_name.equals(getString(R.string.billerinput_text_spinner_default_payment))) {
                spin_payment_options.requestFocus();
                Toast.makeText(getActivity(), getString(R.string.billerinput_validation_spinner_default_payment), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else
            return false;

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if(realm != null && !realm.isInTransaction() && !realm.isClosed()) {
            realm.close();
        }
        super.onDestroy();
    }
}