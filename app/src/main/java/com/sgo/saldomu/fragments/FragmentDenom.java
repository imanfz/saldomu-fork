package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.DenomItemListAdapter;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DenomItemDialog;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

public class FragmentDenom extends BaseFragment implements DenomItemListAdapter.listener{

    View v;
    ArrayAdapter<String> bankProductAdapter;

    TextView CommCodeTextview, CommNameTextview, MemberCodeTextview;
    Spinner ProductBankSpinner;
    RecyclerView itemListRv;
    DenomItemListAdapter itemListAdapter;
    Button submitBtn;
    RelativeLayout toogleDenomList;

    ArrayList<DenomListModel> itemList;
    ArrayList<String> bankProductList, productCodeList, bankCodeList, itemListString;
    SCADMCommunityModel obj;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_denom, container,false);
        CommCodeTextview = v.findViewById(R.id.frag_denom_comm_code_field);
        CommNameTextview = v.findViewById(R.id.frag_denom_comm_name_field);
        MemberCodeTextview = v.findViewById(R.id.frag_denom_member_code_field);
        ProductBankSpinner = v.findViewById(R.id.frag_denom_bank_product_spinner);
        itemListRv = v.findViewById(R.id.frag_denom_list_rv);
        submitBtn = v.findViewById(R.id.frag_denom_submit_btn);
        toogleDenomList = v.findViewById(R.id.frag_denom_toogle_denom_list);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemList = new ArrayList<>();
        itemListString = new ArrayList<>();
        itemListAdapter = new DenomItemListAdapter(getActivity(), itemList,this, false);
//        denomListSpinAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, itemListString);
//        denomListSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        denomListSpinner.setAdapter(denomListSpinAdapter);

        itemListRv.setAdapter(itemListAdapter);
        itemListRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        itemListRv.setNestedScrollingEnabled(false);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(itemListRv);

        obj = DataManager.getInstance().getSACDMCommMod();

        CommCodeTextview.setText(obj.getComm_code());
        CommNameTextview.setText(obj.getComm_name());
        MemberCodeTextview.setText(obj.getMember_code());

        bankProductList = new ArrayList<>();
        productCodeList = new ArrayList<>();
        bankCodeList = new ArrayList<>();
        bankProductAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankProductList);
        bankProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ProductBankSpinner.setAdapter(bankProductAdapter);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()){
                    Fragment frag = new FragmentDenomConfirm();

                    Bundle bundle = new Bundle();
                    bundle.putString(WebParams.PRODUCT_CODE, productCodeList.get(ProductBankSpinner.getSelectedItemPosition()));
                    bundle.putString(WebParams.PRODUCT_NAME, bankProductList.get(ProductBankSpinner.getSelectedItemPosition()));
                    bundle.putString(WebParams.BANK_CODE, bankCodeList.get(ProductBankSpinner.getSelectedItemPosition()));

                    frag.setArguments(bundle);

                    SwitchFragment(frag, "Confirm Denom", true);
                }else
                    Toast.makeText(getActivity(), "Daftar denom kosong", Toast.LENGTH_SHORT).show();
            }
        });

        toogleDenomList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemListRv.getVisibility() == View.VISIBLE){
                    itemListRv.setVisibility(View.GONE);
                }else itemListRv.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        getBankProductList();
    }

    boolean checkInput(){

        for (DenomListModel obj: itemList) {
            if (obj.getOrderList().size()>0) {
                DataManager.getInstance().setOrderList(itemList);
                return true;
            }
        }

        return false;
    }

    void getBankProductList(){
        try{

            showLoading();

            extraSignature = obj.getMember_id_scadm();
            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin,MyApiClient.LINK_GET_LIST_BANK_DENOM_SCADM,
                    userPhoneID,accessKey, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());

            Timber.d("isi params sent get bank list denom:"+params.toString());

            MyApiClient.getListBankDenomSCADM(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {

                        Timber.d("isi response get bank list denom:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if (bankProductList.size()>0) {
                                bankProductList.clear();
                                productCodeList.clear();
                                bankCodeList.clear();
                            }

                            JSONArray bankArr = response.getJSONArray("bank");
                            for (int i=0; i<bankArr.length(); i++){
                                JSONObject bankObj = bankArr.getJSONObject(i);
                                bankProductList.add(bankObj.optString("product_name"));
                                productCodeList.add(bankObj.optString("product_code"));
                                bankCodeList.add(bankObj.optString("bank_code"));
                            }

                            bankProductAdapter.notifyDataSetChanged();

                            getDenomList();

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

                    Timber.w("Error Koneksi get bank list denom:"+throwable.toString());
                    dismissLoading();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Timber.d("httpclient:"+e.getMessage());
        }

    }

    void getDenomList(){
        try{

            extraSignature = obj.getMember_id_scadm();
            RequestParams params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_GET_DENOM_LIST, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());

            Timber.d("isi params sent get denom list:"+params.toString());

            MyApiClient.getDenomList(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {

                        Timber.d("isi response get denom list:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if (itemList.size()>0) {
                                itemList.clear();
                                itemListString.clear();
                            }

                            JSONArray dataArr = response.getJSONArray("item");

                            for (int i=0; i<dataArr.length(); i++){
                                JSONObject dataObj = dataArr.getJSONObject(i);
                                DenomListModel denomObj = new DenomListModel(dataObj);

                                itemListString.add(denomObj.getItemName());
                                itemList.add(denomObj);
                            }

                            itemListAdapter.notifyDataSetChanged();
//                            denomListSpinAdapter.notifyDataSetChanged();
                            dismissLoading();

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

                    Timber.w("Error Koneksi get denom list:"+throwable.toString());
                    dismissLoading();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Timber.d("httpclient:"+e.getMessage());
        }

    }

    @Override
    public void onClick(final int pos) {
//        DenomOrderListModel model = new DenomOrderListModel();
//        itemList.get(pos).getOrderList().add(model);
        DenomItemDialog dialog = DenomItemDialog.newDialog(itemList.get(pos).getItemName(), itemList.get(pos).getOrderList(),
                new DenomItemDialog.listener() {
                    @Override
                    public void onOK(ArrayList<DenomOrderListModel> orderList) {
                        itemList.get(pos).setOrderList(orderList);

                        itemListAdapter.notifyItemChanged(pos);
                    }
                });
        dialog.show(getFragmentManager(), "denom_dialog");
    }

    @Override
    public void onDelete(int pos) {
        itemList.get(pos).getOrderList().remove(pos);

        itemListAdapter.notifyItemChanged(pos);
        itemListAdapter.notifyItemRangeChanged(pos, itemList.get(pos).getOrderList().size());
    }
}
