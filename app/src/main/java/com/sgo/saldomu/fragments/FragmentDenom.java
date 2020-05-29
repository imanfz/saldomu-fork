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

import com.google.gson.Gson;
import com.sgo.saldomu.Beans.DenomBankListData;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.adapter.DenomItemListAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DenomItemDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class FragmentDenom extends BaseFragment implements DenomItemListAdapter.listener {

    View v;
    ArrayAdapter<String> bankProductAdapter;

    TextView CommCodeTextview, CommNameTextview, MemberCodeTextview;
    Spinner ProductBankSpinner;
    RecyclerView itemListRv;
    DenomItemListAdapter itemListAdapter;
    Button submitBtn;
    RelativeLayout toogleDenomList;

    ArrayList<DenomListModel> itemList;
    ArrayList<String> bankProductList, itemListString;
    ArrayList<DenomBankListData> bankDataList;
    SCADMCommunityModel obj;

    String memberCode, commCode, memberId, commId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_denom, container, false);
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
        itemListAdapter = new DenomItemListAdapter(getActivity(), itemList, this, false);
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
        bankDataList = new ArrayList<>();

        bankProductAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankProductList);
        bankProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ProductBankSpinner.setAdapter(bankProductAdapter);

        Bundle bundle = getArguments();

        commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        commId = bundle.getString(DefineValue.COMMUNITY_ID, "");

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()) {
                    Fragment frag = new FragmentDenomConfirm();

                    Bundle bundle = new Bundle();
                    bundle.putString(WebParams.BANK_NAME, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getBankName());
                    bundle.putString(WebParams.BANK_GATEWAY, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getBankGateway());
                    bundle.putString(WebParams.BANK_CODE, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getBankCode());
                    bundle.putString(WebParams.PRODUCT_CODE, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getProductCode());

                    frag.setArguments(bundle);

                    SwitchFragment(frag, DenomSCADMActivity.DENOM_PAYMENT, true);
                } else
                    Toast.makeText(getActivity(), "Daftar denom kosong", Toast.LENGTH_SHORT).show();
            }
        });

        toogleDenomList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemListRv.getVisibility() == View.VISIBLE) {
                    itemListRv.setVisibility(View.GONE);
                } else itemListRv.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        getBankProductList();
    }

    boolean checkInput() {

        for (DenomListModel obj : itemList) {
            if (obj.getOrderList().size() > 0) {
                DataManager.getInstance().setItemList(itemList);
                return true;
            }
        }

        return false;
    }

    void getBankProductList() {
        try {

            showProgressDialog();

            extraSignature = obj.getMember_id_scadm();
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_BANK_DENOM_SCADM, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());

            Timber.d("isi params sent get bank list denom:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_BANK_DENOM_SCADM, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {

                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                Timber.d("isi response get bank list denom:" + response.toString());
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    if (bankProductList.size() > 0) {
                                        bankProductList.clear();
                                        bankDataList.clear();
                                    }

                                    JSONArray bankArr = response.getJSONArray("bank");
                                    for (int i = 0; i < bankArr.length(); i++) {
                                        JSONObject bankObj = bankArr.getJSONObject(i);
                                        bankDataList.add(new DenomBankListData(bankObj));
                                        bankProductList.add(bankObj.optString("product_name"));
                                    }

                                    bankProductAdapter.notifyDataSetChanged();

                                    getDenomList();

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                } else {
                                    String msg = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
//                            showDialogUpdate(msg);
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
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("httpclient:" + e.getMessage());
        }

    }

    void getDenomList() {
        try {

            extraSignature = obj.getMember_id_scadm();
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_DENOM_LIST, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, obj.getMember_id_scadm());

            Timber.d("isi params sent get denom list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_DENOM_LIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);

                                Timber.d("isi response get denom list:" + response.toString());
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    if (itemList.size() > 0) {
                                        itemList.clear();
                                        itemListString.clear();
                                    }

                                    JSONArray dataArr = response.getJSONArray("item");

                                    for (int i = 0; i < dataArr.length(); i++) {
                                        JSONObject dataObj = dataArr.getJSONObject(i);
                                        DenomListModel denomObj = new DenomListModel(dataObj);

                                        itemListString.add(denomObj.getItemName());
                                        itemList.add(denomObj);
                                    }

                                    itemListAdapter.notifyDataSetChanged();
//                            denomListSpinAdapter.notifyDataSetChanged();


                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                }else {
                                    String msg = response.getString(WebParams.ERROR_MESSAGE);
//                            showDialogUpdate(msg);
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
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("httpclient:" + e.getMessage());
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
