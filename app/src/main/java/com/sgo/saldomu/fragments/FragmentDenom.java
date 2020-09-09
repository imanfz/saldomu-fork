package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.sgo.saldomu.Beans.DenomBankListData;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
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

public class FragmentDenom extends BaseFragment {

    View v;
    ArrayAdapter<String> bankProductAdapter;

    NestedScrollView nestedScrollView;
    TextView CommCodeTextview, CommNameTextview, MemberCodeTextview, StoreNameTextview, StoreAddressTextview;
    Spinner ProductBankSpinner;
    Button submitBtn;

    ArrayList<String> bankProductList;
    ArrayList<DenomBankListData> bankDataList;
    SCADMCommunityModel obj;

    String memberCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_denom, container, false);
        nestedScrollView = v.findViewById(R.id.nested_scroll_view);
        CommCodeTextview = v.findViewById(R.id.frag_denom_comm_code_field);
        CommNameTextview = v.findViewById(R.id.frag_denom_comm_name_field);
        MemberCodeTextview = v.findViewById(R.id.frag_denom_member_code_field);
        ProductBankSpinner = v.findViewById(R.id.frag_denom_bank_product_spinner);
        submitBtn = v.findViewById(R.id.frag_denom_submit_btn);
        StoreNameTextview = v.findViewById(R.id.frag_denom_store_name);
        StoreAddressTextview = v.findViewById(R.id.frag_denom_store_address);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");

        obj = DataManager.getInstance().getSACDMCommMod();

        CommCodeTextview.setText(obj.getComm_code());
        CommNameTextview.setText(obj.getComm_name());
        MemberCodeTextview.setText(memberCode);

        bankProductList = new ArrayList<>();
        bankDataList = new ArrayList<>();

        bankProductAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankProductList);
        bankProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ProductBankSpinner.setAdapter(bankProductAdapter);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    nestedScrollView.scrollTo(0, 0);
                    Fragment frag = new FragmentDenomInputItemList();

                    Bundle bundle = new Bundle();
                    bundle.putString(WebParams.BANK_NAME, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getBankName());
                    bundle.putString(WebParams.BANK_GATEWAY, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getBankGateway());
                    bundle.putString(WebParams.BANK_CODE, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getBankCode());
                    bundle.putString(WebParams.PRODUCT_CODE, bankDataList.get(ProductBankSpinner.getSelectedItemPosition()).getProductCode());
                    bundle.putString(WebParams.MEMBER_REMARK, memberCode);
                    bundle.putString(WebParams.STORE_NAME, StoreNameTextview.getText().toString());
                    bundle.putString(WebParams.STORE_ADDRESS, StoreAddressTextview.getText().toString());

                    frag.setArguments(bundle);

                    SwitchFragment(frag, DenomSCADMActivity.DENOM_PAYMENT, true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        getBankProductList();
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
                                    showDialog(msg);
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

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("httpclient:" + e.getMessage());
        }

    }

    void getDenomList() {
        try {

            showProgressDialog();

            extraSignature = obj.getMember_id_scadm();
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_DENOM_LIST, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_REMARK, memberCode);
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
                                    StoreNameTextview.setText(response.getString(WebParams.STORE_NAME));
                                    StoreAddressTextview.setText(response.getString(WebParams.STORE_ADDRESS));
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
                                    showDialog(msg);
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
                getActivity().onBackPressed();
            }
        });

        dialog.show();
    }
}
