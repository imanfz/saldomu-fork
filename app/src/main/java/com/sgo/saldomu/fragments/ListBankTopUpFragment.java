package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.BankHeaderTopUp;
import com.sgo.saldomu.Beans.ListBankDataTopup;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.adapter.BankListTopupAdapter;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JsonUtil;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.BankListModel;
import com.sgo.saldomu.services.BalanceService;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class ListBankTopUpFragment extends BaseFragment implements InformationDialog.OnDialogOkCallback {

    private final String DEFAULT_OTHER_BANK_CODE = "013"; //bank code permata
    View v, nodata_view, layout_list_view;
    LinearLayout maxtopup_layout;
    TextView tv_textNoData, max_topup_holder;
    Button btn_noData;
    EasyAdapter adapter;
    Boolean is_full_activity = false;
    LevelClass levelClass;
    private InformationDialog dialogI;
    //    Expendable_List_View_Adapter expand_lv_adapter;
//    ExpandableListView expand_lv;
    List<BankHeaderTopUp> listDataHeader;
    HashMap<String, BankDataTopUp> listDataChild;

    RecyclerView bankListRv;
    List<BankHeaderTopUp> listDataHeader2;
    BankListTopupAdapter bankListTopupAdapter;

    String otherAtmBankcode;
    BankDataTopUp temp_other_atm;

    public ListBankTopUpFragment newInstance(Boolean is_full_activity) {
        ListBankTopUpFragment fragment = new ListBankTopUpFragment();
        Bundle mbun = new Bundle();
        mbun.putBoolean(DefineValue.IS_ACTIVITY_FULL, is_full_activity);
        fragment.setArguments(mbun);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_topup, container, false);
        bankListRv = v.findViewById(R.id.frag_bank_list_topup_list);

        nodata_view = v.findViewById(R.id.layout_no_data);
        nodata_view.setVisibility(View.GONE);
        tv_textNoData = nodata_view.findViewById(R.id.txt_alert);
        tv_textNoData.setText(getString(R.string.no_data));
        btn_noData = nodata_view.findViewById(R.id.btnRefresh);
        layout_list_view = v.findViewById(R.id.layout_list);
        max_topup_holder = v.findViewById(R.id.max_topup_textview);
        maxtopup_layout = v.findViewById(R.id.maxtopup_layout);
        if (!sp.getBoolean(DefineValue.IS_AGENT, true)) {
            max_topup_holder.setText("Max Topup: " + parseMaxTopupValue(sp.getString(DefineValue.BALANCE_MAX_TOPUP, "")));
        } else
            maxtopup_layout.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dialogI = InformationDialog.newInstance(this, 0);
        Bundle mArgs = getArguments();
        if (mArgs != null && !mArgs.isEmpty()) {
            is_full_activity = mArgs.getBoolean(DefineValue.IS_ACTIVITY_FULL, false);
            if (is_full_activity)
                setActionBarTitle(getString(R.string.toolbar_title_topup));
        }
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listDataHeader2 = new ArrayList<>();

//        expand_lv_adapter = new Expendable_List_View_Adapter(getActivity(),listDataHeader, listDataChild);
        bankListTopupAdapter = new BankListTopupAdapter(getActivity(), temp_other_atm, listDataHeader2, new BankListTopupAdapter.OnClick() {
            @Override
            public void onClick(ArrayList<listBankModel> bankData) {
                DataManager.getInstance().setBankData(bankData);

                BankProductSelectionBottomSheet dialogs = BankProductSelectionBottomSheet.newDialog(new BankProductSelectionBottomSheet.OnClick() {
                    @Override
                    public void onClick(listBankModel obj) {
                        selectAction(obj);
                    }
                });
                dialogs.show(getFragManager(), "dialog");
            }
        });

        bankListRv.setAdapter(bankListTopupAdapter);
        bankListRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        levelClass = new LevelClass(getActivity(), sp);
        levelClass.refreshData();
//        expand_lv = v.findViewById(R.id.expandableListView);
//        expand_lv.setAdapter(expand_lv_adapter);

        btn_noData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBankList();
            }
        });

//        expand_lv.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
//            @Override
//            public void onGroupCollapse(int groupPosition) {
//                ArrayList<ListBankDataTopup> data = listDataChild.get(listDataHeader.get(groupPosition).getHeader()).getBankData();
//
//                for(int i = 0; i<data.size(); i++){
//                    if(data.get(i).getProductType().equals(DefineValue.BANKLIST_TYPE_ATM)){
//                        data.get(i).setVisible(false);
//                    }
//                }
//            }
//        });
    }

    private void selectAction(listBankModel model) {
//        ListBankDataTopup model = listDataChild.get(listDataHeader.get(groupPos).getHeader()).getBankData().get(childPos);
        if (is_full_activity) {
            Fragment mFrag = new SgoPlus_input();
            String titleToolbar = model.getBank_name() + " - " + model.getProduct_name();
            Bundle mBun = new Bundle();
            mBun.putString(DefineValue.BANK_CODE, model.getBank_code());
            mBun.putString(DefineValue.BANK_NAME, model.getBank_name());
            mBun.putString(DefineValue.PRODUCT_NAME, model.getProduct_name());
            mBun.putString(DefineValue.PRODUCT_CODE, model.getProduct_code());
            mBun.putString(DefineValue.PRODUCT_TYPE, model.getProduct_type());
            mBun.putString(DefineValue.PRODUCT_H2H, model.getProduct_h2h());
            mFrag.setArguments(mBun);
            if (model.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS)) {
                if (!levelClass.isLevel1QAC()) {
                    switchFragmentTopUpActivity(mFrag, titleToolbar, true);
                } else
                    levelClass.showDialogLevel();
            } else
                switchFragmentTopUpActivity(mFrag, titleToolbar, true);
        } else {
            Intent i = new Intent(getActivity(), TopUpActivity.class);
            i.putExtra(DefineValue.BANK_CODE, model.getBank_code());
            i.putExtra(DefineValue.BANK_NAME, model.getBank_name());
            i.putExtra(DefineValue.PRODUCT_NAME, model.getProduct_name());
            i.putExtra(DefineValue.PRODUCT_CODE, model.getProduct_code());
            i.putExtra(DefineValue.PRODUCT_TYPE, model.getProduct_type());
            i.putExtra(DefineValue.PRODUCT_H2H, model.getProduct_h2h());
            if (model.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS)) {
                if (!levelClass.isLevel1QAC()) {
                    switchActivity(i);
                } else
                    levelClass.showDialogLevel();
            } else
                switchActivity(i);

        }
    }

    private void switchFragmentTopUpActivity(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isAdded()) {
            getBankList();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void setActionBarTitle(String _title) {
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    public void getBankList() {
        try {
            if (isAdded() || isVisible()) {
                final ProgressDialog prodDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
                HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BANK_LIST, memberIDLogin);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.MEMBER_ID, memberIDLogin);
                params.put(WebParams.TYPE, DefineValue.BANKLIST_TYPE_ALL);
                params.put(WebParams.USER_ID, userPhoneID);

                Timber.d("isi params get BankList:" + params.toString());

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BANK_LIST, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {
                                BankListModel model = getGson().fromJson(object, BankListModel.class);

                                String code = model.getError_code();
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    if (isAdded()) {
                                        if (nodata_view.getVisibility() == View.VISIBLE) {
                                            nodata_view.setVisibility(View.GONE);
                                            layout_list_view.setVisibility(View.VISIBLE);
                                        }
                                        otherAtmBankcode = model.getOther_atm();
                                        insertBankLists(model.getBank_data(), model.getOther_atm());

                                    }
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    String message = model.getError_message();
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    if (is_full_activity)
                                        test.showDialoginActivity(getActivity(), message);
                                    else
                                        test.showDialoginMain(getActivity(), message);
                                } else {
                                    code = model.getError_message();

                                    if (nodata_view.getVisibility() == View.GONE) {
                                        nodata_view.setVisibility(View.VISIBLE);
                                        layout_list_view.setVisibility(View.GONE);
                                    }
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                prodDialog.dismiss();
                            }
                        });
            }
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void insertBankLists(JsonObject data, String other_atm) {
        try {

            JSONObject bank_data = new JSONObject(String.valueOf(data));

            if (bank_data.length() > 0) {
                if (listDataHeader2.size() > 0)
                    listDataHeader2.clear();
                //Siapin Json tambahan dari file raw
                JSONObject otherItemVA = new JsonUtil(getActivity()).readFromRaw(R.raw.topup_atm_item);
                //temp_list_data_bank untuk menyimpan data bank per bank code
                BankDataTopUp temp_list_data_bank;
                //temp_other_atm untuk menyimpan data bank khusus ditampilkan di other atm
                temp_other_atm = null;
                //tempListBankModels menyimpan data product bank bentuk array untuk dimasukan ke temp_list_data_bank(BankDataTopup)
                ArrayList<ListBankDataTopup> tempListBankModels;
                String bankCode;
                for (int i = 0; i < bank_data.length(); i++) {
                    //ambil bankcode dari json response
                    bankCode = bank_data.names().getString(i);
                    //ambil list product bank sesuai dengan bankcode index sekrang
                    JSONArray listProduct = bank_data.getJSONArray(bankCode);
                    tempListBankModels = new ArrayList<>();
                    String fee = "", va = "";

                    BankHeaderTopUp bankData = new BankHeaderTopUp(listProduct.getJSONObject(0).getString(WebParams.BANK_NAME)
                            , bankCode, other_atm);
                    ArrayList<listBankModel> tempListBankModel = new ArrayList<>();

                    for (int j = 0; j < listProduct.length(); j++) {
                        //siapin listDataBank
                        listBankModel listBankModel = new listBankModel(bankCode,
                                listProduct.getJSONObject(j).getString(WebParams.BANK_NAME),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_CODE),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_NAME),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_TYPE),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_H2H),
                                listProduct.getJSONObject(j).getString(WebParams.NO_VA),
                                listProduct.getJSONObject(j).getString(WebParams.FEE));

                        //simpen data va dan fee jika tipenya atm
                        if (listBankModel.getProduct_type().equalsIgnoreCase(DefineValue.BANKLIST_TYPE_ATM)) {
                            fee = listProduct.getJSONObject(j).getString(WebParams.FEE);
                            va = listProduct.getJSONObject(j).getString(WebParams.NO_VA);
                        }

                        //isi data produk other atm jika ada
                        if (!va.isEmpty() && other_atm.equals(bankCode) && listBankModel.getProduct_type().equals(DefineValue.BANKLIST_TYPE_ATM)) {
                            String vaOtherATM = bankCode + " + " + va;
                            ArrayList<ListBankDataTopup> listBankModels = new ArrayList<>();
                            listBankModels.add(new ListBankDataTopup(listBankModel));
                            temp_other_atm = new BankDataTopUp(listBankModels, bankCode, vaOtherATM, fee);
                            DataManager.getInstance().setTemp_other_atm(temp_other_atm);
                        }

                        tempListBankModels.add(new ListBankDataTopup(listBankModel));
                        tempListBankModel.add(listBankModel);
                    }

                    bankData.setBankData(tempListBankModel);

                    //masukan data product other atm bank ke templistbankModel sebagai tambahan product yg didapat dari raw json
                    if (!va.isEmpty() && otherItemVA.has(bankCode)) {
                        JSONArray otherProduct = otherItemVA.getJSONArray(bankCode);
                        for (int j = 0; j < otherProduct.length(); j++) {
                            listBankModel listBankModel = new listBankModel(bankCode,
                                    otherProduct.getJSONObject(j).getString(WebParams.BANK_NAME),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_CODE),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_NAME),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_TYPE),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_H2H));
//                                    otherProduct.getJSONObject(j).getString(WebParams.NO_VA),
//                                    otherProduct.getJSONObject(j).getString(WebParams.FEE

//                            JsonParser jsonParser = new JsonParser();
//                            JsonElement element = jsonParser.parse(otherProduct.toString());

//                            listBankModel listBankModel2 = getGson().fromJson(element, listBankModel.class);
//                            listBankModel2.setBank_code(bankCode);

                            tempListBankModels.add(new ListBankDataTopup(listBankModel));
                        }
                    }

                    //masukin data listbankmodel, va , fee digabung ke class BankDataTopUp
                    String bankName = tempListBankModels.get(0).getListBankModel().getBank_name();
                    temp_list_data_bank = new BankDataTopUp(tempListBankModels, bankCode, va, fee);

                    //masukin nama bank untuk title header dan nama bank, BankDataTopup ke hashmap child
                    listDataHeader.add(new BankHeaderTopUp(bankName));
                    listDataChild.put(bankName, temp_list_data_bank);

                    listDataHeader2.add(bankData);
                }

                //sorting title header
                Collections.sort(listDataHeader, new BankHeaderTopUp.CustomComparator());

                //tambahin ATM lain ke header dan child jika ada
                if (!other_atm.isEmpty() && temp_other_atm != null) {
                    listDataHeader.add(new BankHeaderTopUp(getString(R.string.other_bank)));
                    listDataHeader2.add(new BankHeaderTopUp(getString(R.string.other_bank)));
                    listDataChild.put(getString(R.string.other_bank), temp_other_atm);
                }
//                expand_lv_adapter.notifyDataSetChanged();
            }

            bankListTopupAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchActivity(Intent mIntent) {
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private String parseMaxTopupValue(String value) {
        try {
            String split[] = value.split(" ");
            return split[0] + " " + CurrencyFormat.format(split[1]);
        } catch (Exception e) {
            return "";
        }

    }

    //broadcast service balance

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("receiver service balance");
            if (!sp.getBoolean(DefineValue.IS_AGENT, true)) {
                max_topup_holder.setText("Max Topup: " + parseMaxTopupValue(sp.getString(DefineValue.BALANCE_MAX_TOPUP, "")));
            } else
                maxtopup_layout.setVisibility(View.GONE);
        }
    };

    private IntentFilter filter = new IntentFilter(BalanceService.INTENT_ACTION_BALANCE);

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
            case R.id.action_information:
                if (!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onOkButton() {

    }
}