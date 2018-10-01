package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.adapter.DenomItemListAdapter;
import com.sgo.saldomu.adapter.DenomItemOrderListConfirmAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.ReportBillerDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.FailedPinModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class FragmentDenomConfirm extends BaseFragment implements DenomItemListAdapter.listener, ReportBillerDialog.OnDialogOkCallback{

    TextView commCodeTextview, commNameTextview, memberCodeTextview, productBankTextview
    , costTextview, feeTextview, totalTextview;
    Button submitBtn;
    DenomItemOrderListConfirmAdapter itemListAdapter;
    RecyclerView orderListrv;
    LinearLayout OTPlayout;
    EditText OTPedittext;

    SCADMCommunityModel obj;
    ArrayList<DenomListModel> itemList;
    ArrayList<DenomOrderListModel> orderList;
    String productCode, bankCode, productName, commName, commCode, memberCode, amount, fee, totalAmount, ccyID, bankGateway
            , bankName, txID, remark, apiKey, memberIdSACDM, memberName="", commID, item_name="";
    int attempt, failed;
    Boolean isPIN=false;

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
        totalTextview = v.findViewById(R.id.frag_denom_confirm_total_field);
        submitBtn = v.findViewById(R.id.frag_denom_confirm_submit_btn);
        orderListrv = v.findViewById(R.id.frag_denom_confirm_item_list_field);
        OTPedittext = v.findViewById(R.id.frag_denom_confirm_et_otp);
        OTPlayout = v.findViewById(R.id.frag_denom_confirm_otp_layout);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        obj = DataManager.getInstance().getSACDMCommMod();
        itemList = DataManager.getInstance().getItemList();
        Bundle bundle = getArguments();
        assert bundle != null;
        bankGateway = bundle.getString(WebParams.BANK_GATEWAY, "");
        bankName =  bundle.getString(WebParams.BANK_NAME, "");
        attempt = bundle.getInt(DefineValue.ATTEMPT,-1);
        bankCode =bundle.getString(WebParams.BANK_CODE, "");
        productCode =bundle.getString(WebParams.PRODUCT_CODE, "");

        orderList = new ArrayList<>();

        itemListAdapter = new DenomItemOrderListConfirmAdapter(getActivity(), orderList);

        orderListrv.setAdapter(itemListAdapter);
        orderListrv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        orderListrv.setNestedScrollingEnabled(false);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(orderListrv);

        new UtilsLoader(getActivity(),sp).getFailedPIN(userPhoneID,new OnLoadDataListener() { //get pin attempt
            @Override
            public void onSuccess(Object deData) {
                attempt = (int)deData;
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
                if(bankGateway.equalsIgnoreCase("N"))
                {
                    changeToSGOPlus(txID,productCode, productName,bankCode,
                            String.valueOf(amount), String.valueOf(fee), String.valueOf(totalAmount), bankName);
                }
                else if (bankGateway.equalsIgnoreCase("Y")) {
                    if (productCode.equalsIgnoreCase("SCASH")){
                        CallPINinput(attempt);
//                        btn_next.setEnabled(true);
                    }
                    else
                    {
                        if(inputValidation()) {
                            sentInsertTransTopup(OTPedittext.getText().toString(),amount);
                        }
//                        else btn_next.setEnabled(true);
                    }
                }
            }
        });


        getDenomConfirmData();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private boolean inputValidation(){
        if(OTPedittext.getText().toString().length()==0){
            OTPedittext.requestFocus();
            OTPedittext.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    String buildItem(){
        try {

            JSONArray parentArr = new JSONArray();

            for (DenomListModel obj: itemList) {
                if (obj.getOrderList().size()>0) {
                    JSONObject itemIdObj = new JSONObject();
                    JSONArray itemArr = new JSONArray();

                    for (int i=0; i<obj.getOrderList().size(); i++){
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
        i.putExtra(DefineValue.PRODUCT_NAME,product_name);
        i.putExtra(DefineValue.FEE, fee);
        i.putExtra(DefineValue.REMARK, remark);
        i.putExtra(DefineValue.COMMUNITY_CODE,commCode);
        i.putExtra(DefineValue.MEMBER_CODE,memberCode);
        i.putExtra(DefineValue.MEMBER_ID_SCADM,memberIdSACDM);
        i.putExtra(DefineValue.MEMBER_NAME,memberName);
        i.putExtra(DefineValue.TX_ID,tx_id);
        i.putExtra(DefineValue.AMOUNT,amount);
        i.putExtra(DefineValue.TOTAL_AMOUNT,total_amount);
        i.putExtra(DefineValue.COMMUNITY_ID, commID);
        i.putExtra(DefineValue.API_KEY, apiKey);
        i.putExtra(DefineValue.CALLBACK_URL, (DefineValue.CALLBACK_URL));
        i.putExtra(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.TOPUP_IB_TYPE);
        Timber.d("isi args:"+i.toString());
//        btn_next.setEnabled(true);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }



    private void setResultActivity(int result){
        if (getActivity() == null)
            return;

        DenomSCADMActivity fca = (DenomSCADMActivity) getActivity();
        fca.setResultActivity(result);
    }

    private void CallPINinput(int _attempt){
        Intent i = new Intent(getActivity(), InsertPIN.class);
        if(_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT,_attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    void getDenomConfirmData(){

        showLoading();

        extraSignature = obj.getMember_id_scadm()+productCode;

        RequestParams param = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_GET_DENOM_INVOKE, extraSignature);
        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_DENOM_INVOKE, extraSignature);

        params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());
        params.put(WebParams.PRODUCT_CODE, productCode);
        params.put(WebParams.BANK_CODE, bankCode);
        params.put(WebParams.ITEM, buildItem());
        params.put(WebParams.USER_ID, userPhoneID);

        Timber.d("isi params sent get denom invoke:"+params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_DENOM_INVOKE, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            Timber.d("isi response get denom invoke:"+response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);

                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                setDataView(response);

                            } else if(code.equals(WebParams.LOGOUT_CODE)){
                                Timber.d("isi response autologout:"+response.toString());
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                String msg = response.getString(WebParams.ERROR_MESSAGE);
//                            showDialog(msg);
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
                        dismissLoading();

                    }
                });
    }

    private void sentInsertTransTopup(String tokenValue, final String _amount){
        try{
//            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
//            progdialog.show();

            extraSignature = txID+commCode+productCode+tokenValue;

            final RequestParams param = MyApiClient.getSignatureWithParams(commIDLogin
                    ,MyApiClient.LINK_INSERT_TRANS_TOPUP, userPhoneID,accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP, extraSignature);

            params.put(WebParams.TX_ID, txID);
            params.put(WebParams.PRODUCT_CODE, productCode);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.COMM_ID, commName);
            params.put(WebParams.MEMBER_ID,memberIdSACDM);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(tokenValue));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params insertTrxTOpupSGOL:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            FailedPinModel model = getGson().fromJson(object, FailedPinModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                getTrxStatus(txID,commID,_amount);
                                setResultActivity(MainPage.RESULT_BALANCE);

                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {

                                code = model.getError_code() +" : "+ model.getError_message();
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                String message = model.getError_message();
//                            progdialog.dismiss();
//                            btn_next.setEnabled(true);
                                if(isPIN && message.equals("PIN tidak sesuai")){
                                    Intent i = new Intent(getActivity(), InsertPIN.class);

                                    attempt = model.getFailed_attempt();
                                    failed = model.getMax_failed();

                                    if(attempt != -1)
                                        i.putExtra(DefineValue.ATTEMPT,failed-attempt);

                                    startActivityForResult(i, MainPage.REQUEST_FINISH);
                                }
                                else{
                                    getActivity().finish();
                                }

                            }
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void getTrxStatus(final String txId, String comm_id, final String _amount){
        try{

            extraSignature = txId + comm_id;
            RequestParams param = MyApiClient.getSignatureWithParams(commIDLogin,MyApiClient.LINK_GET_TRX_STATUS,
                    userPhoneID,accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature);

            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.TYPE, DefineValue.BIL_PAYMENT_TYPE);
            params.put(WebParams.TX_TYPE, DefineValue.ESPAY);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params sent get Trx Status:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            GetTrxStatusReportModel model = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0003")) {

                                String txstatus = model.getTx_status();
                                showReportBillerDialog(sp.getString(DefineValue.USER_NAME, ""),
                                        DateTimeFormat.formatToID(model.getCreated()),
                                        sp.getString(DefineValue.USERID_PHONE, ""), txId, item_name,
                                        txstatus, model.getTx_remark(), _amount, model.getDenom_detail(), model.getBuss_scheme_code(),
                                        model.getBuss_scheme_name(), model.getProduct_name(), model.getOrder_id(), model.getComm_code(),
                                        model.getMember_code());
                            } else if(code.equals(WebParams.LOGOUT_CODE)){
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(),message);
                            }
                            else {
                                String msg;
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    msg = getString(R.string.network_connection_failure_toast);
                                else msg = model.getError_message();
                                showDialog(msg);
                            }
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showReportBillerDialog(String name,String date,String userId, String txId,String itemName,String txStatus,
                                        String txRemark, String _amount, String denom_detail,
                                        String buss_scheme_code, String buss_scheme_name, String product_name, String order_id,
                                        String comm_code, String member_code) {
        Bundle args = new Bundle();
        ReportBillerDialog dialog = ReportBillerDialog.newInstance(this);
        args.putString(DefineValue.USER_NAME, name);
        args.putString(DefineValue.DATE_TIME, date);
        args.putString(DefineValue.TX_ID, txId);
        args.putString(DefineValue.USERID_PHONE, userId);
        args.putString(DefineValue.DENOM_DATA, itemName);
//        args.putString(DefineValue.DENOM_DATA, response.optString(DefineValue.COMMUNITY_CODE));
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(_amount));
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP);
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(fee));

        Boolean txStat = false;
        if (txStatus.equals(DefineValue.SUCCESS)){
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success));
        }else if(txStatus.equals(DefineValue.ONRECONCILED)){
            txStat = true;
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending));
        }else if(txStatus.equals(DefineValue.SUSPECT)){
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect));
        }
        else if(!txStatus.equals(DefineValue.FAILED)){
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction)+" "+txStatus);
        }
        else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed));
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat);
        if(!txStat)args.putString(DefineValue.TRX_REMARK, txRemark);


        double totalAmount = Double.parseDouble(amount) + Double.parseDouble(fee);
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(String.valueOf(totalAmount)));
        args.putString(DefineValue.DENOM_DETAIL,denom_detail);
        args.putString(DefineValue.BUSS_SCHEME_CODE,buss_scheme_code);
        args.putString(DefineValue.BUSS_SCHEME_NAME,buss_scheme_name);
        args.putString(DefineValue.BANK_PRODUCT,product_name);
        args.putString(DefineValue.ORDER_ID,order_id);
        args.putString(DefineValue.COMMUNITY_CODE,comm_code);
        args.putString(DefineValue.MEMBER_CODE,member_code);

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
                dialog.dismiss();
                //SgoPlusWeb.this.finish();
            }
        });

        dialog.show();
    }

    void setDataView(JSONObject resp){
        try {

            commName = resp.getString("comm_name");
            commCode = resp.getString("comm_code");
            commID = resp.getString("comm_id");
            memberCode = resp.getString("member_code");
            productName = resp.getString("product_name");
            if (productName.equalsIgnoreCase("MANDIRI SMS"))
            {
                OTPlayout.setVisibility(View.VISIBLE);
            }
            amount = CurrencyFormat.format(resp.getString("amount"));
            fee = CurrencyFormat.format(resp.getString("admin_fee"));
            totalAmount = CurrencyFormat.format(resp.getString("total_amount"));
            ccyID = resp.getString("ccy_id");
            txID = resp.getString("tx_id");
            bankName = resp.getString("bank_name");
            bankCode = resp.getString("bank_code");
            memberIdSACDM = resp.getString("member_id");
            memberName = resp.getString("member_name");

            commNameTextview.setText(commName);
            commCodeTextview.setText(commCode);
            memberCodeTextview.setText(memberCode);
            productBankTextview.setText(productName);

            if (amount != null)
                costTextview.setText(ccyID + " " + amount);
            if (fee != null)
                feeTextview.setText(ccyID + " " +fee);
            if (totalAmount != null)
                totalTextview.setText(ccyID + " " +totalAmount);

            if (orderList.size() > 0)
                orderList.clear();

            JSONArray itemArr = resp.getJSONArray("item");
            for (int i=0; i<itemArr.length(); i++){
                JSONObject obj = itemArr.getJSONObject(i);
                orderList.add(new DenomOrderListModel(obj));
            }

            itemListAdapter.notifyDataSetChanged();

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if(requestCode == MainPage.REQUEST_FINISH){
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                String _amount;
                _amount = amount;
                //    Log.d("onActivity result", "Biller Fragment result pin value");
                sentInsertTransTopup(value_pin,_amount);
            }else {
                backToDenomSACDM();
            }
        }
    }

    @Override
    public void onClick(int pos) {

    }

    @Override
    public void onDelete(int pos) {

    }

    @Override
    public void onOkButton() {
        backToDenomSACDM();
    }

    void backToDenomSACDM(){
        getFragManager().popBackStack(DenomSCADMActivity.DENOM_PAYMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
