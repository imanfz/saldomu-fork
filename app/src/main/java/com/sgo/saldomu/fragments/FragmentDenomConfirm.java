package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.adapter.DenomItemOrderListConfirmAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.FailedPinModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class FragmentDenomConfirm extends BaseFragment implements ReportBillerDialog.OnDialogOkCallback {

    TextView commCodeTextview, commNameTextview, memberCodeTextview, productBankTextview, costTextview, feeTextview, totalTextview, storeNameTextView, storeAddressTextView, totalDiscountTextview;
    Button submitBtn;
    DenomItemOrderListConfirmAdapter itemListAdapter;
    RecyclerView orderListrv;
    LinearLayout OTPlayout;
    EditText OTPedittext;

    SCADMCommunityModel obj;
    ArrayList<DenomListModel> itemList;
    ArrayList<DenomOrderListModel> orderList;
    String productCode, bankCode, productName, commName, commCode, memberCode, amount, fee, totalAmount, ccyID, bankGateway, bankName, txID,
            memberIdSACDM, memberName = "", commID, item_name = "", storeName, storeAddress, cust_id, product_type, tx_favorite_type, notes, promoCode, totalDiscount;
    int attempt, failed;
    Boolean isPIN = false, isFav = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_denom_confirm, container, false);

        commCodeTextview = v.findViewById(R.id.frag_denom_confirm_comm_code_field);
        commNameTextview = v.findViewById(R.id.frag_denom_confirm_comm_name_field);
        memberCodeTextview = v.findViewById(R.id.frag_denom_confirm_member_code_field);
        productBankTextview = v.findViewById(R.id.frag_denom_confirm_product_bank_field);
        costTextview = v.findViewById(R.id.frag_denom_confirm_cost_field);
        feeTextview = v.findViewById(R.id.frag_denom_confirm_fee_field);
        totalDiscountTextview = v.findViewById(R.id.frag_denom_confirm_discount_field);
        totalTextview = v.findViewById(R.id.frag_denom_confirm_total_field);
        submitBtn = v.findViewById(R.id.frag_denom_confirm_submit_btn);
        orderListrv = v.findViewById(R.id.frag_denom_confirm_item_list_field);
        OTPedittext = v.findViewById(R.id.frag_denom_confirm_et_otp);
        OTPlayout = v.findViewById(R.id.frag_denom_confirm_otp_layout);
        storeNameTextView = v.findViewById(R.id.frag_denom_confirm_store_name);
        storeAddressTextView = v.findViewById(R.id.frag_denom_store_address);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        obj = DataManager.getInstance().getSACDMCommMod();
        itemList = DataManager.getInstance().getItemList();
        Bundle bundle = getArguments();
        assert bundle != null;
        if (bundle != null) {
            Timber.d("isi bundle : " + bundle.toString());
            bankGateway = bundle.getString(WebParams.BANK_GATEWAY, "");
            bankName = bundle.getString(WebParams.BANK_NAME, "");
            attempt = bundle.getInt(DefineValue.ATTEMPT, -1);
            bankCode = bundle.getString(WebParams.BANK_CODE, "");
            productCode = bundle.getString(WebParams.PRODUCT_CODE, "");
            memberCode = bundle.getString(WebParams.MEMBER_REMARK, "");
            storeName = bundle.getString(WebParams.STORE_NAME, "");
            storeAddress = bundle.getString(WebParams.STORE_ADDRESS, "");
            promoCode = bundle.getString(WebParams.PROMO_CODE, "");
            if (bundle.getBoolean(DefineValue.IS_FAVORITE)) {
                isFav = true;
                notes = bundle.getString(DefineValue.NOTES, "");
                cust_id = bundle.getString(DefineValue.CUST_ID, "");
                tx_favorite_type = bundle.getString(DefineValue.TX_FAVORITE_TYPE, "");
                product_type = bundle.getString(DefineValue.PRODUCT_TYPE, "");
            }
        }

        orderList = new ArrayList<>();

        itemListAdapter = new DenomItemOrderListConfirmAdapter(getActivity(), orderList);

        orderListrv.setAdapter(itemListAdapter);
        orderListrv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        orderListrv.setNestedScrollingEnabled(false);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(orderListrv);

        new UtilsLoader(getActivity(), sp).getFailedPIN(userPhoneID, new OnLoadDataListener() { //get pin attempt
            @Override
            public void onSuccess(Object deData) {
                attempt = (int) deData;
            }

            @Override
            public void onFail(Bundle message) {

            }

            @Override
            public void onFailure(String message) {

            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBtn.setEnabled(false);
//                sentInquiry();
                if (isFav) {
                    onSaveToFavorite();
                } else
                    confirmToken();
            }
        });


        getDenomConfirmData();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private boolean inputValidation() {
        if (OTPedittext.getText().toString().length() == 0) {
            OTPedittext.requestFocus();
            OTPedittext.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    String buildItem() {
        try {

            JSONArray parentArr = new JSONArray();

            for (DenomListModel obj : itemList) {
                if (obj.getOrderList().size() > 0) {
                    JSONObject itemIdObj = new JSONObject();
                    JSONArray itemArr = new JSONArray();

                    for (int i = 0; i < obj.getOrderList().size(); i++) {
                        JSONObject childObj = new JSONObject();
                        try {
                            childObj.put("item_qty", obj.getOrderList().get(i).getPulsa());
                            childObj.put("item_phone", obj.getOrderList().get(i).getPhoneNumber());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        itemArr.put(childObj);
                    }
                    itemIdObj.put(obj.getItemID(), itemArr);
                    parentArr.put(itemIdObj);
                }
            }
            return parentArr.toString();

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void changeToSGOPlus(String tx_id, String product_code, String product_name, String bank_code,
                                 String amount, String fee, String total_amount, String bank_name) {

        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(DefineValue.PRODUCT_CODE, product_code);
        i.putExtra(DefineValue.BANK_CODE, bank_code);
        i.putExtra(DefineValue.BANK_NAME, bank_name);
        i.putExtra(DefineValue.PRODUCT_NAME, product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.REMARK, "");
        i.putExtra(DefineValue.COMMUNITY_CODE, commCode);
        i.putExtra(DefineValue.MEMBER_CODE, memberCode);
        i.putExtra(DefineValue.MEMBER_ID_SCADM, memberIdSACDM);
        i.putExtra(DefineValue.MEMBER_NAME, memberName);
        i.putExtra(DefineValue.TX_ID, tx_id);
        i.putExtra(DefineValue.AMOUNT, amount);
        i.putExtra(DefineValue.TOTAL_AMOUNT, total_amount);
        i.putExtra(DefineValue.COMMUNITY_ID, commID);
        i.putExtra(DefineValue.API_KEY, "");
        i.putExtra(DefineValue.CALLBACK_URL, (DefineValue.CALLBACK_URL));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        Timber.d("isi args:" + i.toString());
//        btn_next.setEnabled(true);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }


    private void setResultActivity() {
        if (getActivity() == null)
            return;

        DenomSCADMActivity fca = (DenomSCADMActivity) getActivity();
        fca.setResultActivity();
    }

    private void CallPINinput(int _attempt) {
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    public void confirmToken() {
        showProgressDialog();

//        extraSignature = tx_id + comm_code;
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_PAYMENT_DGI);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.TX_ID, txID);
        params.put(WebParams.COMM_CODE, commCode);
        params.put(WebParams.USER_COMM_CODE, sp.getString(DefineValue.COMMUNITY_CODE, ""));
        params.put(WebParams.USER_ID, userPhoneID);
        Timber.d("params confirm payment denom scadm : %s", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();
                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            Timber.d("response confirm payment denom scadm : %s", response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                sentInquiry();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:%s", response.toString());
                                AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                showDialog(message);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    void getDenomConfirmData() {

        showProgressDialog();

        extraSignature = obj.getMember_id_scadm() + obj.getComm_id() + MyApiClient.CCY_VALUE;

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_DENOM_INVOKE_NEW, extraSignature);

//        params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());
        params.put(WebParams.ITEM, buildItem());

        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.MEMBER_ID_GOWORLD, obj.getMember_id_scadm());
        params.put(WebParams.COMM_ID_GOWORLD, obj.getComm_id());
        params.put(WebParams.MEMBER_CODE_GOWORLD, obj.getMember_code());
        params.put(WebParams.COMM_CODE_GOWORLD, obj.getComm_code());
        params.put(WebParams.BANK_CODE, bankCode);
        params.put(WebParams.PRODUCT_CODE, productCode);
        params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
        params.put(WebParams.MEMBER_REMARK, memberCode);
        params.put(WebParams.PROMO_CODE, promoCode);

        Timber.d("isi params sent get denom invoke:" + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_DENOM_INVOKE_NEW, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                            Timber.d("isi response get denom invoke:%s", response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                setDataView(response);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:%s", response.toString());
                                AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", response.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                showDialog(message);
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

    public void sentInquiry() {
        try {
            showProgressDialog();

            extraSignature = txID + commCode + productCode;

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);

            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, commIDLogin);
            Timber.d("isi params InquiryTrx denom scadm:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response InquiryTrx denom scadm: %s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    if (bankGateway.equalsIgnoreCase(DefineValue.STRING_NO)) {
                                        changeToSGOPlus(txID, productCode, productName, bankCode,
                                                String.valueOf(amount), String.valueOf(fee), String.valueOf(totalAmount), bankName);
                                    } else if (bankGateway.equalsIgnoreCase(DefineValue.STRING_YES)) {
                                        if (productCode.equalsIgnoreCase("SCASH")) {
                                            CallPINinput(attempt);
                                        } else {
                                            if (inputValidation()) {
                                                sentInsertTransTopup(OTPedittext.getText().toString());
                                            }
                                        }
                                    }
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    showDialog(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
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

    private void sentInsertTransTopup(String tokenValue) {
        try {
            showProgressDialog();
            String link = MyApiClient.LINK_INSERT_TRANS_TOPUP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = txID + commCode + productCode + tokenValue;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.COMM_ID, commName);
            params.put(WebParams.MEMBER_ID, memberIdSACDM);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncryptCommID(commName, uuid, dateTime, userPhoneID, tokenValue, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            FailedPinModel model = getGson().fromJson(object, FailedPinModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                getTrxStatus(txID, commID);
                                setResultActivity();

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
                                Toast.makeText(getActivity(), code + " : " + message, Toast.LENGTH_LONG).show();
                                showDialog(message);
                                if (isPIN && message.equals("PIN tidak sesuai")) {
                                    Intent i = new Intent(getActivity(), InsertPIN.class);

                                    attempt = model.getFailed_attempt();
                                    failed = model.getMax_failed();

                                    if (attempt != -1)
                                        i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void getTrxStatus(final String txId, String comm_id) {
        try {
            showProgressDialog();
            extraSignature = txID + comm_id;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params sent get Trx Status:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""),
                                        DateTimeFormat.formatToID(model.getCreated()),
                                        sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                        model.getTx_status(), model.getTx_remark(), model.getTotal_amount(), model.getTx_fee(), getGson().toJson(model.getDenom_detail()), model.getBuss_scheme_code(),
                                        model.getBuss_scheme_name(), model.getProduct_name(), model.getOrder_id(), model.getComm_code(),
                                        model.getMember_code(), model.getStore_name(), model.getStore_address(), model.getStore_code(),
                                        model.getMember_cust_name(), model.getMember_cust_id(), model);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                Timber.d("isi response autologout:%s", object.toString());
                                AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", object.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                showDialog(message);
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
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showReportBillerDialog(String name, String date, String userId, String txId, String itemName, String txStatus,
                                        String txRemark, String totalAmount, String txFee, String denom_detail,
                                        String buss_scheme_code, String buss_scheme_name, String product_name, String order_id,
                                        String comm_code, String member_code, String store_name, String store_address, String store_code,
                                        String agent_name, String agent_phone, GetTrxStatusReportModel model) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
//        args.putString(DefineValue.DENOM_DATA, response.optString(DefineValue.COMMUNITY_CODE));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.getTotal_gross()));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(txFee));

        boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)) {
            txStat = true;
        } else if (txStatus.equals(DefineValue.ONRECONCILED)) {
            txStat = true;
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        args.putString(DefineValue.TRX_STATUS_REMARK, model.getTx_status_remark());
        if (!txStat) args.putString(DefineValue.TRX_REMARK, txRemark);

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));
        args.putString(DefineValue.DENOM_DETAIL, denom_detail);
        args.putString(DefineValue.BUSS_SCHEME_CODE, buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME, buss_scheme_name);
        args.putString(DefineValue.BANK_PRODUCT, product_name);
        args.putString(DefineValue.ORDER_ID, order_id);
        args.putString(DefineValue.COMMUNITY_CODE, comm_code);
        args.putString(DefineValue.MEMBER_CODE, member_code);
        args.putString(DefineValue.STORE_NAME, store_name);
        args.putString(DefineValue.STORE_ADDRESS, store_address);
        args.putString(DefineValue.STORE_CODE, store_code);
        args.putString(DefineValue.AGENT_NAME, agent_name);
        args.putString(DefineValue.AGENT_PHONE, agent_phone);
        args.putString(DefineValue.TOTAL_DISC, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(model.getTotal_disc())));

        dialog.setArguments(args);
        FragmentTransaction ft = getFragManager().beginTransaction();
        ft.add(dialog, ReportBillerDialog.TAG);
        ft.commitAllowingStateLoss();
    }

    private void showDialog(String msg) {
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
        Title.setText(getString(R.string.error));
        Message.setText(msg);

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissProgressDialog();
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        });

        dialog.show();
    }

    void setDataView(JSONObject resp) {
        try {

            commName = obj.getComm_name();
            commCode = obj.getComm_code();
            commID = obj.getComm_id();
            productName = resp.getString("product_name");
            if (productName.equalsIgnoreCase("MANDIRI SMS")) {
                OTPlayout.setVisibility(View.VISIBLE);
            }
            amount = CurrencyFormat.format(resp.getString("total_gross"));
            fee = CurrencyFormat.format(resp.getString("admin_fee"));
            totalDiscount = CurrencyFormat.format(resp.getString("total_disc"));
            totalAmount = CurrencyFormat.format(resp.getString("total_amount"));
            ccyID = resp.getString("ccy_id");
            txID = resp.getString("tx_id");
            bankName = resp.getString("bank_name");
            bankCode = bankCode;
            memberIdSACDM = obj.getMember_id_scadm();
            memberName = resp.getString(WebParams.MEMBER_NAME);

            commNameTextview.setText(commName);
            commCodeTextview.setText(commCode);
            memberCodeTextview.setText(memberCode);
            productBankTextview.setText(productName);
            storeNameTextView.setText(storeName);
            storeAddressTextView.setText(storeAddress);

            if (amount != null)
                costTextview.setText(ccyID + " " + amount);
            if (fee != null)
                feeTextview.setText(ccyID + " " + fee);
            if (totalDiscount != null)
                totalDiscountTextview.setText(ccyID + " " + totalDiscount);
            if (totalAmount != null)
                totalTextview.setText(ccyID + " " + totalAmount);

            if (orderList.size() > 0)
                orderList.clear();

            JSONArray itemArr = resp.getJSONArray("item");
//            Iterator<String> keys = itemArr.keys();
//            while (keys.hasNext()) {
//                JSONObject obj = itemArr.getJSONObject(keys.next());
//                orderList.add(new DenomOrderListModel(obj));
//            }
            for (int i = 0; i < itemArr.length(); i++) {
                JSONObject obj = itemArr.getJSONObject(i);
                orderList.add(new DenomOrderListModel(obj));
            }

            itemListAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onSaveToFavorite() {
        extraSignature = cust_id + product_type + tx_favorite_type;
        Log.e("extraSignature params ", extraSignature);
        String url = MyApiClient.LINK_TRX_FAVORITE_SAVE;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(url, extraSignature);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.PRODUCT_TYPE, product_type);
        params.put(WebParams.CUSTOMER_ID, cust_id);
        params.put(WebParams.TX_FAVORITE_TYPE, tx_favorite_type);
        params.put(WebParams.COMM_ID, commID);
        params.put(WebParams.NOTES, notes);
        params.put(WebParams.DENOM_ITEM_ID, "");

        Log.e("params fav b2btopup :", params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(url, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(response.toString(), jsonModel.class);
                            Log.e("onResponse fav b2btopup", response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("onResponse fav b2btopup", throwable.getLocalizedMessage());
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        confirmToken();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                String _amount;
                _amount = amount;
                //    Log.d("onActivity result", "Biller Fragment result pin value");
                sentInsertTransTopup(value_pin);
            } else {
                backToDenomSACDM();
            }
        }
    }

    @Override
    public void onOkButton() {
        backToDenomSACDM();
    }

    void backToDenomSACDM() {
//        getActivity().onBackPressed();
        List<Fragment> frags = getFragmentManager().getFragments();
        for (Fragment f : frags) {
            if (f.getTag().equals(DenomSCADMActivity.DENOM_PAYMENT)) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(f).commit();
            }
        }
        getFragManager().popBackStack(DenomSCADMActivity.DENOM_PAYMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
