package com.sgo.hpku.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.RegisterSMSBankingActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.InetHandler;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.ReqPermissionClass;
import com.sgo.hpku.coreclass.SMSclass;
import com.sgo.hpku.coreclass.ToggleKeyboard;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.dialogs.SMSDialog;
import com.sgo.hpku.entityRealm.List_BBS_City;
import com.sgo.hpku.fragments.CashOutBBS_confirm_agent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by thinkpad on 4/21/2017.
 */

public class BBSTransaksiInformasi extends Fragment {
    public final static String TAG = "com.sgo.hpku.fragments.BBSTransaksiInformasi";
    private final String MANDIRISMS = "MANDIRISMS";

    private View v, cityLayout, noHPPengirimLayout;
    private ProgressDialog progdialog;
    private Activity act;
    private TextView tvTitle;
    private EditText etNoBenefAcct, etNameBenefAcct, etNoHp, etRemark;
    private ImageView spinwheelCity;
    private AutoCompleteTextView spBenefCity;
    private Animation frameAnimation;
    private Realm realm;
    private ArrayList<List_BBS_City> list_bbs_cities;
    private ArrayList<String> list_name_bbs_cities;
    private Integer CityAutocompletePos = -1;
    private Button btnNext, btnBack;
    private SMSclass smSclass;
    private SMSDialog smsDialog;
    private ReqPermissionClass reqPermissionClass;
    private Boolean isSMSBanking = false, isSimExist = false;
    private BBSTransaksiInformasi.ActionListener actionListener;
    private String userID, accessKey, comm_code, member_code, source_product_code, source_product_type,
            benef_product_code, benef_product_name, benef_product_type, source_product_h2h,
            api_key, callback_url, source_product_name, productValue="", comm_id, city_id, amount, transaksi;

    public interface ActionListener{
        void ChangeActivityFromCashInput(Intent data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof BBSTransaksiInformasi.ActionListener) {
            actionListener = (BBSTransaksiInformasi.ActionListener) getTargetFragment();
        } else {
            if(context instanceof BBSTransaksiInformasi.ActionListener){
                actionListener = (BBSTransaksiInformasi.ActionListener) context;
            }
            else {
                throw new RuntimeException(context.toString()
                        + " must implement ActionListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v =  inflater.inflate(R.layout.bbs_transaksi_informasi, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        act = getActivity();
        realm = Realm.getDefaultInstance();

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Bundle bundle = getArguments();
        if(bundle!= null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            amount = bundle.getString(DefineValue.AMOUNT);
            comm_id = bundle.getString(DefineValue.COMMUNITY_ID);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            member_code = bundle.getString(DefineValue.MEMBER_CODE);
            callback_url = bundle.getString(DefineValue.CALLBACK_URL);
            api_key = bundle.getString(DefineValue.API_KEY);
            source_product_code = bundle.getString(DefineValue.SOURCE_PRODUCT_CODE);
            source_product_type = bundle.getString(DefineValue.SOURCE_PRODUCT_TYPE);
            source_product_h2h = bundle.getString(DefineValue.SOURCE_PRODUCT_H2H);
            source_product_name = bundle.getString(DefineValue.SOURCE_PRODUCT_NAME);
            benef_product_code = bundle.getString(DefineValue.BENEF_PRODUCT_CODE);
            benef_product_name = bundle.getString(DefineValue.BENEF_PRODUCT_NAME);
            benef_product_type = bundle.getString(DefineValue.BENEF_PRODUCT_TYPE);

            if (source_product_code.equalsIgnoreCase(MANDIRISMS)) {
                isSMSBanking = true;
            } else
                isSMSBanking = false;

            CircleStepView mCircleStepView = ((CircleStepView) v.findViewById(R.id.circle_step_view));
            mCircleStepView.setTextBelowCircle(getString(R.string.jumlah), getString(R.string.informasi), getString(R.string.konfirmasi));
            mCircleStepView.setCurrentCircleIndex(1, false);

            tvTitle = (TextView) v.findViewById(R.id.tv_title);
            etNoBenefAcct = (EditText) v.findViewById(R.id.no_tujuan_value);
            etNameBenefAcct = (EditText) v.findViewById(R.id.name_value);
            etNoHp = (EditText) v.findViewById(R.id.no_hp_pengirim_value);
            etRemark = (EditText) v.findViewById(R.id.message_value);
            btnNext = (Button) v.findViewById(R.id.proses_btn);
            btnBack = (Button) v.findViewById(R.id.back_btn);
            cityLayout = v.findViewById(R.id.bbscashin_city_layout);
            noHPPengirimLayout = v.findViewById(R.id.no_hp_pengirim_layout);
            spBenefCity = (AutoCompleteTextView) v.findViewById(R.id.bbscashin_value_city_benef);
            spinwheelCity = (ImageView) v.findViewById(R.id.spinning_wheel_bbscashin_city);
            frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
            frameAnimation.setRepeatCount(Animation.INFINITE);

            tvTitle.setText(transaksi);
            if (transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                noHPPengirimLayout.setVisibility(View.VISIBLE);
                setBBSCity();

                if (benef_product_type.equalsIgnoreCase(DefineValue.EMO)) {
                    cityLayout.setVisibility(View.GONE);
                    etNoBenefAcct.setHint(R.string.number_hp_destination_hint);
                } else {
                    cityLayout.setVisibility(View.VISIBLE);
                    etNoBenefAcct.setHint(R.string.number_destination_hint);
                }
            } else {
                cityLayout.setVisibility(View.GONE);
                noHPPengirimLayout.setVisibility(View.GONE);

                etNoBenefAcct.setHint(getString(R.string.no_member_hint));
            }

            btnBack.setOnClickListener(backListener);
            btnNext.setOnClickListener(nextListener);
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getFragmentManager().getBackStackEntryCount() > 0)
                getFragmentManager().popBackStack();
            else
                getActivity().finish();
        }
    };

    Button.OnClickListener nextListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                    if (isSMSBanking) {
                        if (reqPermissionClass == null) {
                            reqPermissionClass = new ReqPermissionClass(getActivity());
                            reqPermissionClass.setTargetFragment(BBSTransaksiInformasi.this);
                        }
                        if (reqPermissionClass.checkPermission(Manifest.permission.READ_PHONE_STATE, ReqPermissionClass.PERMISSIONS_REQ_READPHONESTATE)) {
                            initializeSmsClass();
                            if (isSimExist)
                                SubmitAction();
                        }
                    } else {
                        SubmitAction();
                    }
                }
                else {
                    btnNext.setEnabled(false);
                    if (inputValidation()) {
                        sentInsertA2C();
                    }
                    else btnNext.setEnabled(true);
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private void setBBSCity() {
        spBenefCity.setVisibility(View.GONE);
        spinwheelCity.setVisibility(View.VISIBLE);
        spinwheelCity.startAnimation(frameAnimation);

        Thread proses = new Thread(){

            @Override
            public void run() {
                RealmResults results = realm.where(List_BBS_City.class).findAll();
                list_bbs_cities = new ArrayList<>(results);
                list_name_bbs_cities = new ArrayList<>();
                if(list_bbs_cities.size() > 0) {
                    for (int i = 0; i < list_bbs_cities.size(); i++) {
                        list_name_bbs_cities.add(list_bbs_cities.get(i).getCity_name());
                    }
                }
                else {
                    //                    UpdateBBSCity.startUpdateBBSCity(getActivity());
                }

                final ArrayAdapter<String> city_adapter = new ArrayAdapter<String>
                        (getActivity(),android.R.layout.select_dialog_item, list_name_bbs_cities);

                spBenefCity.setThreshold(1);
                spBenefCity.setAdapter(city_adapter);

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinwheelCity.clearAnimation();
                        spinwheelCity.setVisibility(View.GONE);
                        spBenefCity.setVisibility(View.VISIBLE);
//                        adapter.notifyDataSetChanged();
                        city_adapter.notifyDataSetChanged();
                    }
                });
            }
        };
        proses.run();

    }

    private void SubmitAction(){
        btnNext.setEnabled(false);
        if (inputValidation()) {
            sentInsertC2A();
        } else btnNext.setEnabled(true);
    }

    private void sentInsertC2A() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_GLOBAL_BBS_INSERT_C2A,
                    userID, accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.MEMBER_CODE, member_code);
            params.put(WebParams.SOURCE_PRODUCT_CODE, source_product_code);
            params.put(WebParams.SOURCE_PRODUCT_TYPE, source_product_type);
            params.put(WebParams.BENEF_PRODUCT_CODE, benef_product_code);
            params.put(WebParams.BENEF_PRODUCT_TYPE, benef_product_type);
            params.put(WebParams.BENEF_PRODUCT_VALUE_CODE, etNoBenefAcct.getText().toString());
            params.put(WebParams.BENEF_PRODUCT_VALUE_NAME, etNameBenefAcct.getText().toString());
            if(benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
                city_id = list_bbs_cities.get(CityAutocompletePos).getCity_id();
                params.put(WebParams.BENEF_PRODUCT_VALUE_CITY, city_id);
            }
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, etRemark.getText().toString());
            params.put(WebParams.MEMBER_SHOP_PHONE, etNoHp.getText().toString());
            params.put(WebParams.USER_COMM_CODE, MyApiClient.COMM_CODE);

            Log.d("params insert c2a", params.toString());
            MyApiClient.sentGlobalBBSInsertC2A(getActivity(),params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                    btnNext.setEnabled(true);
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response sent insert C2A:"+response.toString());
                            if(isSMSBanking) {
                                smsDialog = new SMSDialog(getActivity(), new SMSDialog.DialogButtonListener() {
                                    @Override
                                    public void onClickOkButton(View v, boolean isLongClick) {
                                        if (reqPermissionClass.checkPermission(Manifest.permission.SEND_SMS, ReqPermissionClass.PERMISSIONS_SEND_SMS)) {
                                            smsDialog.sentSms();
                                            RegSimCardReceiver(true);
                                        }
                                    }

                                    @Override
                                    public void onClickCancelButton(View v, boolean isLongClick) {
                                        if(progdialog.isShowing())
                                            progdialog.dismiss();
                                    }

                                    @Override
                                    public void onSuccess(int user_is_new) {

                                    }

                                    @Override
                                    public void onSuccess(String product_value) {
                                        productValue = product_value;
                                        smsDialog.dismiss();
                                        smsDialog.reset();
                                        try {
                                            sentDataReqToken(response.getString(WebParams.TX_ID), response.getString(WebParams.TX_PRODUCT_CODE),
                                                    response.getString(WebParams.TX_PRODUCT_NAME), response.getString(WebParams.TX_BANK_CODE),
                                                    response.getString(WebParams.AMOUNT), response.getString(WebParams.ADMIN_FEE),
                                                    response.getString(WebParams.TOTAL_AMOUNT), response.getString(WebParams.TX_BANK_NAME),
                                                    response.getString(WebParams.MAX_RESEND_TOKEN), response.getString(WebParams.BENEF_PRODUCT_VALUE_CODE),
                                                    response.getString(WebParams.BENEF_PRODUCT_VALUE_NAME));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                if(isSimExist)
                                    smsDialog.show();
                            }
                            else if(source_product_h2h.equalsIgnoreCase("Y") && source_product_type.equalsIgnoreCase(DefineValue.EMO)) {
                                sentDataReqToken(response.getString(WebParams.TX_ID), response.getString(WebParams.TX_PRODUCT_CODE),
                                        response.getString(WebParams.TX_PRODUCT_NAME), response.getString(WebParams.TX_BANK_CODE),
                                        response.getString(WebParams.AMOUNT), response.getString(WebParams.ADMIN_FEE),
                                        response.getString(WebParams.TOTAL_AMOUNT), response.getString(WebParams.TX_BANK_NAME),
                                        response.getString(WebParams.MAX_RESEND_TOKEN), response.getString(WebParams.BENEF_PRODUCT_VALUE_CODE),
                                        response.getString(WebParams.BENEF_PRODUCT_VALUE_NAME));
                            }
                            else {
                                changeToConfirmCashIn(response.getString(WebParams.TX_ID), response.getString(WebParams.TX_PRODUCT_CODE),
                                        response.getString(WebParams.TX_PRODUCT_NAME), response.getString(WebParams.TX_BANK_CODE),
                                        response.getString(WebParams.AMOUNT), response.getString(WebParams.ADMIN_FEE),
                                        response.getString(WebParams.TOTAL_AMOUNT), response.getString(WebParams.TX_BANK_NAME),
                                        response.getString(WebParams.MAX_RESEND_TOKEN), response.getString(WebParams.BENEF_PRODUCT_VALUE_CODE),
                                        response.getString(WebParams.BENEF_PRODUCT_VALUE_NAME));
                            }

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        }else {
                            Timber.d("isi error sent insert C2A:"+response.toString());
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
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
                    btnNext.setEnabled(true);
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi sent insert C2A:"+throwable.toString());
                }

            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void sentInsertA2C() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_GLOBAL_BBS_INSERT_A2C,
                    userID, accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.MEMBER_CODE, member_code);
            params.put(WebParams.SOURCE_PRODUCT_CODE, source_product_code);
            params.put(WebParams.SOURCE_PRODUCT_TYPE, source_product_type);
            params.put(WebParams.SOURCE_PRODUCT_VALUE, etNoBenefAcct.getText().toString());
            params.put(WebParams.BENEF_PRODUCT_CODE, benef_product_code);
            params.put(WebParams.BENEF_PRODUCT_TYPE, benef_product_type);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, etRemark.getText().toString());

            Log.d("params insert a2c", params.toString());
            MyApiClient.sentGlobalBBSInsertA2C(getActivity(),params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                    btnNext.setEnabled(true);
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response sent insert a2c:"+response.toString());

                            sentDataReqToken(response.getString(WebParams.TX_ID), response.getString(WebParams.TX_PRODUCT_CODE),
                                    response.getString(WebParams.TX_PRODUCT_NAME), response.getString(WebParams.TX_BANK_CODE),
                                    response.getString(WebParams.AMOUNT), response.getString(WebParams.TX_BANK_NAME), null, null,
                                    null, null, null);

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        }else {
                            Timber.d("isi error sent insert a2c:"+response.toString());
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code_msg, Toast.LENGTH_LONG).show();
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
                    btnNext.setEnabled(true);
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi sent insert a2c:"+throwable.toString());
                }

            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sentDataReqToken(final String _tx_id, final String _product_code, final String _product_name, final String _bank_code,
                                 final String _amount, final String fee, final String totalAmount, final String _bank_name,
                                 final String _max_resend_token, final String _benef_acct_no, final String _benef_acct_name) {
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(comm_id,MyApiClient.LINK_REQ_TOKEN_SGOL,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.PRODUCT_CODE, _product_code);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, comm_id);
            if(isSMSBanking)
                params.put(WebParams.PRODUCT_VALUE,productValue);

            Timber.d("isi params regtoken Sgo+:"+params.toString());

            MyApiClient.sentDataReqTokenSGOL(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response reqtoken :"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
                                if (isSMSBanking)
                                    showDialog(_tx_id, _product_code, _product_name, _bank_code,
                                            _amount, fee, totalAmount, _bank_name, _max_resend_token,
                                            _benef_acct_no, _benef_acct_name);
                                else
                                    changeToConfirmCashIn(_tx_id, _product_code, _product_name, _bank_code,
                                            _amount, fee, totalAmount, _bank_name, _max_resend_token,
                                            _benef_acct_no, _benef_acct_name);
                            }
                            else {
                                changeToConfirmCashout(_tx_id, _product_code, _product_name, _bank_code,
                                        _amount, _bank_name);
                            }
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            if(code.equals("0059")||code.equals("0164")){
                                showDialogErrorSMS(_bank_name,code,response.optString(WebParams.ERROR_MESSAGE,""));
                            }
                            else {
                                code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
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
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi reg token sgo input:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialog(final String _tx_id, final String _product_code, final String _product_name, final String _bank_code,
                            final String _amount, final String fee, final String totalAmount, final String _bank_name,
                            final String _max_resend_token, final String _benef_acct_no, final String _benef_acct_name) {
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
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.appname)+" "+getString(R.string.dialog_token_message_sms));
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToConfirmCashIn(_tx_id, _product_code, _product_name, _bank_code,
                        _amount, fee, totalAmount, _bank_name, _max_resend_token,
                        _benef_acct_no, _benef_acct_name);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeToConfirmCashIn(String _tx_id, String _product_code, String _product_name, String _bank_code,
                                       String _amount, String fee, String totalAmount, String _bank_name, String _max_resend_token,
                                       String _benef_acct_no, String _benef_acct_name) {

        Bundle mArgs = new Bundle();
        if(benef_product_type.equalsIgnoreCase(DefineValue.ACCT)) {
            String city_name = spBenefCity.getText().toString();
            mArgs.putString(DefineValue.BENEF_CITY, city_name);
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, _product_code);
        mArgs.putString(DefineValue.BANK_CODE, _bank_code);
        mArgs.putString(DefineValue.BANK_NAME, _bank_name);
        mArgs.putString(DefineValue.PRODUCT_NAME,_product_name);
        mArgs.putString(DefineValue.FEE, fee);
        mArgs.putString(DefineValue.COMMUNITY_CODE,comm_code);
        mArgs.putString(DefineValue.TX_ID,_tx_id);
        mArgs.putString(DefineValue.AMOUNT,_amount);
        mArgs.putString(DefineValue.TOTAL_AMOUNT,totalAmount);
        mArgs.putString(DefineValue.SHARE_TYPE,"1");
        mArgs.putString(DefineValue.CALLBACK_URL,callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, MyApiClient.COMM_ID);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.NAME_BENEF, _benef_acct_name);
        mArgs.putString(DefineValue.NO_BENEF, _benef_acct_no);
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type);
        mArgs.putString(DefineValue.NO_HP_BENEF, etNoHp.getText().toString());
        mArgs.putString(DefineValue.REMARK, etRemark.getText().toString());
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.MAX_RESEND, _max_resend_token);
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        btnNext.setEnabled(true);

        Fragment mFrag = new BBSCashInConfirm();
        mFrag.setArguments(mArgs);

        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent , mFrag, BBSCashInConfirm.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_in), true);
    }

    private void changeToConfirmCashout(String _tx_id, String _product_code, String _product_name, String _bank_code,
                                        String _amount, String _bank_name) {

        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h);
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type);
        mArgs.putString(DefineValue.PRODUCT_CODE, _product_code);
        mArgs.putString(DefineValue.BANK_CODE, _bank_code);
        mArgs.putString(DefineValue.BANK_NAME, _bank_name);
        mArgs.putString(DefineValue.PRODUCT_NAME,_product_name);
        mArgs.putString(DefineValue.COMMUNITY_CODE,comm_code);
        mArgs.putString(DefineValue.TX_ID,_tx_id);
        mArgs.putString(DefineValue.AMOUNT,_amount);
        mArgs.putString(DefineValue.SHARE_TYPE,"1");
        mArgs.putString(DefineValue.CALLBACK_URL,callback_url);
        mArgs.putString(DefineValue.API_KEY, api_key);
        mArgs.putString(DefineValue.COMMUNITY_ID, MyApiClient.COMM_ID);
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name);
        mArgs.putString(DefineValue.USER_ID, etNoBenefAcct.getText().toString());
        mArgs.putString(DefineValue.REMARK, etRemark.getText().toString());
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name);
        mArgs.putString(DefineValue.TRANSACTION, transaksi);
        btnNext.setEnabled(true);

        Fragment mFrag = new CashOutBBS_confirm_agent();
        mFrag.setArguments(mArgs);
        getFragmentManager().beginTransaction().addToBackStack(TAG)
                .replace(R.id.bbsTransaksiFragmentContent , mFrag, CashOutBBS_confirm_agent.TAG).commit();
        ToggleKeyboard.hide_keyboard(act);
//        switchFragment(mFrag, getString(R.string.cash_out), true);
    }

    void showDialogErrorSMS(final String _nama_bank, String error_code, String error_msg) {
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
        Title.setText(getString(R.string.topup_dialog_not_registered));
        if(error_code.equals("0059")){
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME,_nama_bank);
                    actionListener.ChangeActivityFromCashInput(newIntent);
                    dialog.dismiss();
                }
            });
        }
        else if(error_code.equals("0164")) {
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.close));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
        }

        dialog.show();
    }

    private void initializeSmsClass(){
        if(smSclass == null)
            smSclass = new SMSclass(getActivity(), CustomSimcardListener);

        smSclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if(!isExist){
                    isSimExist = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else
                    isSimExist = true;
            }
        });
    }

    private BroadcastReceiver CustomSimcardListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
                if(intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")){
                    if(smsDialog!= null && smsDialog.isShowing()) {
                        Toast.makeText(getActivity(), R.string.smsclass_simcard_listener_absent_toast, Toast.LENGTH_LONG).show();
                        smsDialog.dismiss();
                        smsDialog.reset();
                    }
                }

            }
        }
    };

    private void RegSimCardReceiver(Boolean isReg){
        if (isSMSBanking) {
            if(isReg){
                try{
                    getActivity().unregisterReceiver(CustomSimcardListener);
                }
                catch (Exception ignored){}
                getActivity().registerReceiver(CustomSimcardListener,SMSclass.simStateIntentFilter);
            }
            else {
                try{
                    getActivity().unregisterReceiver(CustomSimcardListener);
                }
                catch (Exception ignored){}
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (reqPermissionClass.checkOnPermissionResult(requestCode, grantResults, ReqPermissionClass.PERMISSIONS_SEND_SMS)) {
            smsDialog.sentSms();
        }
        else if (reqPermissionClass.checkOnPermissionResult(requestCode, grantResults, ReqPermissionClass.PERMISSIONS_REQ_READPHONESTATE)) {
            initializeSmsClass();
            if(isSimExist)
                SubmitAction();
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
            if(requestCode == ReqPermissionClass.PERMISSIONS_SEND_SMS) {
                if(progdialog.isShowing())
                    progdialog.dismiss();
                if (smsDialog != null) {
                    smsDialog.dismiss();
                    smsDialog.reset();
                }
            }
        }
    }

    private boolean inputValidation() {
        if(noHPPengirimLayout.getVisibility() == View.VISIBLE) {
            if (etNoHp.getText().toString().length() == 0) {
                etNoHp.requestFocus();
                etNoHp.setError(getString(R.string.no_hp_pengirim_validation));
                return false;
            }
        }
        if(transaksi.equalsIgnoreCase(getString(R.string.cash_in))) {
            if (benef_product_type.equalsIgnoreCase(DefineValue.EMO)) {
                if (etNoBenefAcct.getText().toString().length() == 0) {
                    etNoBenefAcct.requestFocus();
                    etNoBenefAcct.setError(getString(R.string.no_member_validation));
                    return false;
                } else if (etNoBenefAcct.getText().toString().length() < 7) {
                    etNoBenefAcct.requestFocus();
                    etNoBenefAcct.setError(getString(R.string.no_member_validation));
                    return false;
                }
            }
            else {
                if (etNoBenefAcct.getText().toString().length() == 0) {
                    etNoBenefAcct.requestFocus();
                    etNoBenefAcct.setError(getString(R.string.no_rekening_validation));
                    return false;
                } else if (etNoBenefAcct.getText().toString().length() < 7) {
                    etNoBenefAcct.requestFocus();
                    etNoBenefAcct.setError(getString(R.string.no_rekening_validation));
                    return false;
                }
            }
        }
        else {
            if(etNoBenefAcct.getText().toString().length()==0){
                etNoBenefAcct.requestFocus();
                etNoBenefAcct.setError(getString(R.string.no_member_validation));
                return false;
            }
        }
        if(etNameBenefAcct.getText().toString().length()==0){
            etNameBenefAcct.requestFocus();
            etNameBenefAcct.setError(getString(R.string.nama_rekening_validation));
            return false;
        }

        if(cityLayout.getVisibility() == View.VISIBLE) {
            String autocomplete_text = spBenefCity.getText().toString();

            if (autocomplete_text.equals("")){
                spBenefCity.requestFocus();
                spBenefCity.setError("Kota tujuan harus diisi!");
                return false;
            }else if (!list_name_bbs_cities.contains(autocomplete_text)){

                spBenefCity.requestFocus();
                spBenefCity.setError("Nama kota tidak ditemukan!");
                return false;
            }else {
                Log.d("tes", "index: " + list_name_bbs_cities.indexOf(autocomplete_text));
                CityAutocompletePos = list_name_bbs_cities.indexOf(autocomplete_text);
                spBenefCity.setError(null);
            }
        }

        return true;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.bbs_reg_acct, menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;

//            case R.id.action_reg_acct:
//                Fragment mFrag = new ListAccountBBS();
//                switchFragment(mFrag, ListAccountBBS.TAG, true);
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void switchFragment(Fragment i, String name, Boolean isBackstack){
//        if (getActivity() == null)
//            return;
//
//        BBSActivity fca = (BBSActivity ) getActivity();
//        fca.switchContent(i,name,isBackstack);
//    }

    @Override
    public void onResume() {
        super.onResume();
        RegSimCardReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        RegSimCardReceiver(false);
    }
}
