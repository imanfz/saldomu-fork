package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.PulsaAgentActivity;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ErrorDefinition;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogFrag;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

/**
 * Created by thinkpad on 9/14/2015.
 */
public class PulsaAgentDescription extends Fragment {

    private View v;
    private SecurePreferences sp;
    private TextView tv_operator_value;
    private TextView tv_id_cust;
    private TextView tv_nominal;
    private Spinner spin_payment_options;
    private Button btn_submit;
    private Button btn_cancel;
    private ProgressDialog progdialog;

    private List<String> paymentData;
    private List<listBankModel> mDataPayment;
    private ArrayAdapter<String> adapterPaymentOptions;
    private listBankModel mTempBank;

    private String cust_id;
    private String member_id;
    private String phone_number;
    private String item_id;
    private String item_name;
    private String payment_name;
    private String comm_id;
    private String comm_name;
    private String comm_code;
    private String api_key;
    private String callback_url;
    private String share_type;
    private String operator_id;
    private String operator_name;
    private String accessKey;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        cust_id = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Bundle bundle = getArguments();
        member_id = bundle.getString(DefineValue.MEMBER_ID);
        phone_number = bundle.getString(DefineValue.PHONE_NUMBER);
        item_id = bundle.getString(DefineValue.DENOM_ITEM_ID);
        item_name = bundle.getString(DefineValue.DENOM_ITEM_NAME);
        share_type = bundle.getString(DefineValue.SHARE_TYPE);
        operator_id = bundle.getString(DefineValue.OPERATOR_ID);
        operator_name = bundle.getString(DefineValue.OPERATOR_NAME);

        tv_id_cust = (TextView) v.findViewById(R.id.pulsatoken_pulsa_id_value);
        tv_operator_value = (TextView) v.findViewById(R.id.pulsatoken_operator_value);
        tv_nominal = (TextView) v.findViewById(R.id.pulsatoken_nominal_value);
        spin_payment_options = (Spinner) v.findViewById(R.id.spinner_pulsainput_payment_options);
        btn_submit = (Button) v.findViewById(R.id.pulsatoken_btn_verification);
        btn_cancel = (Button) v.findViewById(R.id.pulsatoken_btn_cancel);

        btn_submit.setOnClickListener(submitListener);
        btn_cancel.setOnClickListener(cancelListener);

        tv_id_cust.setText(phone_number);
        tv_operator_value.setText(operator_name);
        tv_nominal.setText(item_name);

        paymentData = new ArrayList<>();
        adapterPaymentOptions = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,paymentData){
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        adapterPaymentOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_payment_options.setAdapter(adapterPaymentOptions);
        spin_payment_options.setOnItemSelectedListener(spinnerPaymentListener);

        setActionBarTitle(getString(R.string.toolbar_title_pulsa_agent));
        getBankDAP();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_pulsa_input_description, container, false);
        return v;
    }

    private boolean inputValidation(){
        if(payment_name.equals(getString(R.string.billerinput_text_spinner_default_payment))){
            spin_payment_options.requestFocus();
            Toast.makeText(getActivity(),getString(R.string.billerinput_validation_spinner_default_payment),Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(inputValidation() ){
                    btn_submit.setEnabled(false);
                    sentPaymentDAP();
                }

            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message),null);
        }
    };

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    private Spinner.OnItemSelectedListener spinnerPaymentListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Object item = adapterView.getItemAtPosition(i);
            payment_name = item.toString();
            if (payment_name.equalsIgnoreCase("UNIK"))
            {
                payment_name=getString(R.string.appname);
            }
            if(i>0)
                mTempBank = mDataPayment.get(i-1);

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private void changeToConfirmDAP(String _amount, String _merchant_type, String tx_id, String ccy_id, String fee, String bank_code, String product_code) {

        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.TX_ID,tx_id);
        mArgs.putString(DefineValue.CCY_ID,ccy_id);
        mArgs.putString(DefineValue.AMOUNT, _amount);
        mArgs.putString(DefineValue.ITEM_ID,item_id);
        mArgs.putString(DefineValue.ITEM_NAME,item_name);
        mArgs.putString(DefineValue.PAYMENT_NAME, payment_name);
        mArgs.putString(DefineValue.CUST_ID, cust_id);
        mArgs.putString(DefineValue.API_KEY,api_key);
        mArgs.putString(DefineValue.CALLBACK_URL,callback_url);
        mArgs.putString(DefineValue.FEE, fee);
        double totalAmount = Double.parseDouble(_amount) + Double.parseDouble(fee);
        mArgs.putString(DefineValue.TOTAL_AMOUNT, String.valueOf(totalAmount));
        mArgs.putString(DefineValue.PRODUCT_PAYMENT_TYPE, mTempBank.getProduct_type());

        mArgs.putString(DefineValue.BANK_CODE, bank_code);
        mArgs.putString(DefineValue.PRODUCT_CODE, product_code);
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code);
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id);
        mArgs.putString(DefineValue.SHARE_TYPE, share_type);
        mArgs.putString(DefineValue.AUTHENTICATION_TYPE, _merchant_type);
        mArgs.putString(DefineValue.PHONE_NUMBER, phone_number);
        mArgs.putString(DefineValue.OPERATOR_ID, operator_id);
        mArgs.putString(DefineValue.OPERATOR_NAME, operator_name);

        mArgs.putBoolean(DefineValue.IS_SGO_PLUS, mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB));

        Fragment newFrag = new PulsaAgentConfirm();
        newFrag.setArguments(mArgs);
        switchFragment(newFrag, PulsaAgentActivity.FRAG_PULSA_DESCRIPTION,null,true);
    }

    private void sentPaymentDAP(){
        try{
            progdialog.show();

            final String bank_code = mTempBank.getBank_code();
            final String product_code = mTempBank.getProduct_code();
            final String topupType = mTempBank.getProduct_type();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID_PULSA,MyApiClient.LINK_PAYMENT_DAP,
                    cust_id,accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID_PULSA);
            params.put(WebParams.MEMBER_ID, member_id);
            params.put(WebParams.DENOM_ITEM_ID, item_id);
            params.put(WebParams.DENOM_ITEM_REMARK, phone_number);
            params.put(WebParams.BANK_CODE, bank_code);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.CUST_ID, cust_id);
            params.put(WebParams.USER_ID, cust_id);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.MERCHANT_CODE, sp.getString(DefineValue.COMMUNITY_CODE,""));

            if(topupType.equals(DefineValue.BANKLIST_TYPE_IB))
                params.put(WebParams.PRODUCT_TYPE, DefineValue.BANKLIST_TYPE_IB);
            else if(topupType.equals(DefineValue.BANKLIST_TYPE_SMS))
                params.put(WebParams.PRODUCT_TYPE, DefineValue.BANKLIST_TYPE_SMS);
            else
                params.put(WebParams.PRODUCT_TYPE, DefineValue.BANKLIST_TYPE_EMO);

            Timber.d("isi params sent payment DAP" + params.toString());

            JsonHttpResponseHandler mHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response payment DAP" +response.toString());

                            if(mTempBank.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB)){
                                changeToConfirmDAP(response.getString(WebParams.AMOUNT), response.getString(WebParams.AUTH_TYPE), response.getString(WebParams.TX_ID), response.getString(WebParams.CCY_ID),
                                        response.getString(WebParams.ADMIN_FEE), bank_code,product_code);
                                progdialog.dismiss();
                                btn_submit.setEnabled(true);
                            }
                            else {
                                sentDataReqToken(response.getString(WebParams.AMOUNT), response.getString(WebParams.AUTH_TYPE), response.getString(WebParams.TX_ID), response.getString(WebParams.CCY_ID), product_code, response.getString(WebParams.ADMIN_FEE),
                                        bank_code);
                            }

                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout"+ response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }else {
                            Timber.d("Error isi response payment DAP"+response.toString());
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            progdialog.dismiss();
                            btn_submit.setEnabled(true);
                            getFragmentManager().popBackStack();
                        }
                    } catch (JSONException e) {
                        progdialog.dismiss();
                        btn_submit.setEnabled(true);
                        Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
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

                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    btn_submit.setEnabled(true);
                    Timber.w("Error Koneksi payment DAP:" + throwable.toString());
                }
            };

            MyApiClient.sentPaymentDAP(getActivity(),params, mHandler);

        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }

    private void getBankDAP(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID_PULSA,MyApiClient.LINK_BANK_DAP,
                    cust_id,accessKey);
            params.put(WebParams.MEMBER_ID, member_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID_PULSA);
            params.put(WebParams.USER_ID, cust_id);

            Timber.d("isi params sent Bank DAP", params.toString());

            MyApiClient.getBankDAP(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response Bank DAP", response.toString());
                            String arrayBank = response.getString(WebParams.BANK_DATA);
                            if(!arrayBank.equals("")) {
                                JSONArray mData = new JSONArray(arrayBank);
                                mDataPayment = new ArrayList<>();
                                ArrayList<String> tempDataPaymentName = new ArrayList<>();
                                ArrayList<listBankModel> tempMDataPayment = new ArrayList<>();

                                paymentData.add(getString(R.string.billerinput_text_spinner_default_payment));

                                for (int i = 0; i < mData.length(); i++) {
                                    JSONObject mJob = mData.getJSONObject(i);
                                    listBankModel mBob = new listBankModel(mJob.getString(WebParams.BANK_CODE),
                                            mJob.getString(WebParams.BANK_NAME),
                                            mJob.getString(WebParams.PRODUCT_CODE),
                                            mJob.getString(WebParams.PRODUCT_NAME),
                                            mJob.getString(WebParams.PRODUCT_TYPE),
                                            mJob.getString(WebParams.PRODUCT_H2H)
                                    );

                                    if(mBob.getProduct_code().equals(DefineValue.SCASH)){
                                        String tempProductName = mBob.getProduct_name();
                                        mDataPayment.add(mBob);
                                        paymentData.add(getString(R.string.appname));
                                    }
                                    else {
                                        //mDataPayment.add(mBob);
                                        tempMDataPayment.add(mBob);
                                        tempDataPaymentName.add(mBob.getProduct_name());
                                    }

                                }

                                if(!tempDataPaymentName.isEmpty())
                                    Collections.sort(tempDataPaymentName);
                                if(!tempMDataPayment.isEmpty())
                                    Collections.sort(tempMDataPayment, new PaymentNameComparator());

                                mDataPayment.addAll(tempMDataPayment);
                                paymentData.addAll(tempDataPaymentName);

                                adapterPaymentOptions.notifyDataSetChanged();
                                progdialog.dismiss();
                            }
                            comm_id = response.getString(WebParams.COMM_ID);
                            comm_name = response.getString(WebParams.COMM_NAME);
                            comm_code = response.getString(WebParams.COMM_CODE);
                            api_key = response.getString(WebParams.API_KEY);
                            callback_url = response.getString(WebParams.CALLBACK_URL);
                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            progdialog.dismiss();
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        progdialog.dismiss();
                        Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
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

                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi Biller Type:"+throwable.toString());
                }


            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }

    private void sentDataReqToken(final String _amount, final String _merchant_type, final String _tx_id, final String _ccy_id, final String _product_code, final String fee,
                                  final String _bank_code){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_TOKEN_SGOL,
                    cust_id,accessKey);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.PRODUCT_CODE, _product_code);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, cust_id);

            Timber.d("isi params reqtoken Sgo+" + params.toString());

            MyApiClient.sentDataReqTokenSGOL(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response reqtoken " + response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if(mTempBank.getProduct_type().equalsIgnoreCase(DefineValue.BANKLIST_TYPE_SMS)) {
                                showDialog(_amount, _merchant_type, _tx_id, _ccy_id, fee, _product_code, _bank_code);
                            }
                            else if(_merchant_type.equalsIgnoreCase(DefineValue.AUTH_TYPE_OTP))
                                showDialog(_amount, _merchant_type, _tx_id, _ccy_id, fee, _product_code, _bank_code);
                            else
                                changeToConfirmDAP(_amount,_merchant_type, _tx_id,_ccy_id,fee,_bank_code,_product_code);

                        } else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout" +response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }else {
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            switch (code) {
                                case "0059":
                                    showDialogSMS(mTempBank.getBank_name());
                                    break;
                                case ErrorDefinition.ERROR_CODE_LESS_BALANCE:
                                    String message_dialog = "\"" + code_msg + "\" \n" + getString(R.string.dialog_message_less_balance,getString(R.string.appname));

                                    AlertDialogFrag dialog_frag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                            message_dialog, getString(R.string.ok), getString(R.string.cancel), false);
                                    dialog_frag.setOkListener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent mI = new Intent(getActivity(), TopUpActivity.class);
                                            mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true);
                                            startActivityForResult(mI, MainPage.REQUEST_FINISH);
                                        }
                                    });
                                    dialog_frag.setCancelListener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    dialog_frag.setTargetFragment(PulsaAgentDescription.this, 0);
                                    dialog_frag.show(getActivity().getSupportFragmentManager(), AlertDialogFrag.TAG);
                                    break;
                                default:
                                    code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack();
                                    break;
                            }
                        }
                        btn_submit.setEnabled(true);
                        if(progdialog.isShowing())
                            progdialog.dismiss();
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

                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    btn_submit.setEnabled(true);
                    Timber.w("Error Koneksi request token:"+throwable.toString());
                }

            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
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
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showDialog(final String _amount, final String _merchant_type, final String _tx_id, final String _ccy_id, final String fee, final String product_code, final String bank_code) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.smsBanking_dialog_validation_title));
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.application_name)+" "+getString(R.string.dialog_token_message_sms));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToConfirmDAP(_amount, _merchant_type, _tx_id, _ccy_id, fee, bank_code, product_code);
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private class PaymentNameComparator implements Comparator<listBankModel>
    {
        public int compare(listBankModel left, listBankModel right) {
            return left.getProduct_name().compareTo(right.getProduct_name());
        }
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        PulsaAgentActivity fca = (PulsaAgentActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    private void switchFragment(Fragment i, String name,String next_name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        PulsaAgentActivity fca = (PulsaAgentActivity ) getActivity();
        fca.switchContent(i,name,next_name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        PulsaAgentActivity fca = (PulsaAgentActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()>0)
                    getFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
