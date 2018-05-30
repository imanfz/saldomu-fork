package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.DenomItemListAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

public class FragmentDenomConfirm extends BaseFragment implements DenomItemListAdapter.listener{

    TextView commCodeTextview, commNameTextview, memberCodeTextview, productBankTextview
    , costTextview, feeTextview, totalTextview;
    Button submitBtn;
    DenomItemListAdapter itemListAdapter;
    RecyclerView orderListrv;

    SCADMCommunityModel obj;
    ArrayList<DenomListModel> orderList;
    String productCode, bankCode, productName, commName, commCode, memberCode, amount, fee, totalAmount, ccyID;

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

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        obj = DataManager.getInstance().getSACDMCommMod();
        orderList = DataManager.getInstance().getOrderList();
        Bundle bundle = getArguments();
        assert bundle != null;
        productCode = bundle.getString(WebParams.PRODUCT_CODE, "");
        bankCode = bundle.getString(WebParams.BANK_CODE, "");
        productName =  bundle.getString(WebParams.PRODUCT_NAME, "");

        itemListAdapter = new DenomItemListAdapter(getActivity(), orderList,this, true);

    }

    @Override
    public void onResume() {
        super.onResume();
        getDenomConfirmData();
    }

    String buildItem(){
        try {

            JSONArray parentArr = new JSONArray();

            for (DenomListModel obj: orderList) {
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

    void getDenomConfirmData(){

        showLoading();

        extraSignature = obj.getMember_id_scadm()+productCode;

        RequestParams params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_GET_DENOM_INVOKE, extraSignature);

        params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());
        params.put(WebParams.PRODUCT_CODE, productCode);
        params.put(WebParams.BANK_CODE, bankCode);
        params.put(WebParams.ITEM, buildItem());

        Timber.d("isi params sent get denom invoke:"+params.toString());

        params.put(WebParams.USER_ID, userPhoneID);

        MyApiClient.getDenomInvoke(getActivity(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    Timber.d("isi response get denom invoke:"+response.toString());
                    String code = response.getString(WebParams.ERROR_CODE);
                    setDataView(response);
                    if (code.equals(WebParams.SUCCESS_CODE)) {



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
                dismissLoading();

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

                Timber.w("Error Koneksi get denom invoke:"+throwable.toString());
                dismissLoading();
            }
        });
    }

    void setDataView(JSONObject resp){
        try {

            commName = resp.getString("comm_name");
            commCode = resp.getString("comm_code");
            memberCode = resp.getString("member_code");
            productName = resp.getString("product_name");
            amount = CurrencyFormat.format(resp.getString("amount"));
            fee = CurrencyFormat.format(resp.getString("admin_fee"));
            totalAmount = CurrencyFormat.format(resp.getString("total_amount"));
            ccyID = resp.getString("ccy_id");

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

//            orderListrv.setAdapter(itemListAdapter);
//            orderListrv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//            orderListrv.setNestedScrollingEnabled(false);
//            SnapHelper snapHelper = new LinearSnapHelper();
//            snapHelper.attachToRecyclerView(orderListrv);

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(int pos) {

    }

    @Override
    public void onDelete(int pos) {

    }
}
