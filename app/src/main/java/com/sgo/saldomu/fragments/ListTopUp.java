package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.BankHeaderTopUp;
import com.sgo.saldomu.Beans.ListBankDataTopup;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.adapter.Expendable_List_View_Adapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JsonUtil;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.services.BalanceService;

import org.apache.http.Header;
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
public class ListTopUp extends Fragment implements InformationDialog.OnDialogOkCallback {

    private final String DEFAULT_OTHER_BANK_CODE = "013"; //bank code permata
    View v,nodata_view, layout_list_view;
    LinearLayout maxtopup_layout;
    TextView tv_textNoData, max_topup_holder;
    Button btn_noData;
    SecurePreferences sp;
    String userID;
    String accessKey;
    String memberID;
    EasyAdapter adapter;
    Boolean is_full_activity = false;
    LevelClass levelClass;
    private InformationDialog dialogI;
    Expendable_List_View_Adapter expand_lv_adapter;
    ExpandableListView expand_lv;
    List<BankHeaderTopUp> listDataHeader;
    HashMap<String, BankDataTopUp> listDataChild;

    public ListTopUp newInstance(Boolean is_full_activity){
        ListTopUp fragment = new ListTopUp();
        Bundle mbun = new Bundle();
        mbun.putBoolean(DefineValue.IS_ACTIVITY_FULL,is_full_activity);
        fragment.setArguments(mbun);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_topup, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nodata_view = v.findViewById(R.id.layout_no_data);
        nodata_view.setVisibility(View.GONE);
        tv_textNoData = (TextView) nodata_view.findViewById(R.id.txt_alert);
        tv_textNoData.setText(getString(R.string.no_data));
        btn_noData = (Button) nodata_view.findViewById(R.id.btnRefresh);
        layout_list_view = v.findViewById(R.id.layout_list);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        memberID = sp.getString(DefineValue.MEMBER_ID, "");
        max_topup_holder = (TextView) v.findViewById(R.id.max_topup_textview);
        maxtopup_layout = (LinearLayout) v.findViewById(R.id.maxtopup_layout);
        if (!sp.getBoolean(DefineValue.IS_AGENT, true)){
            max_topup_holder.setText("Max Topup: "+parseMaxTopupValue(sp.getString(DefineValue.BALANCE_MAX_TOPUP, "")));
        }else
            maxtopup_layout.setVisibility(View.GONE);

        dialogI = InformationDialog.newInstance(this, 0);
        Bundle mArgs = getArguments();
        if(mArgs != null && !mArgs.isEmpty()) {
            is_full_activity = mArgs.getBoolean(DefineValue.IS_ACTIVITY_FULL, false);
            if(is_full_activity)
                setActionBarTitle(getString(R.string.topuplist_ab_title));
        }
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        expand_lv_adapter = new Expendable_List_View_Adapter(getActivity(),listDataHeader, listDataChild);

        levelClass = new LevelClass(getActivity(),sp);
        levelClass.refreshData();
        expand_lv = (ExpandableListView) v.findViewById(R.id.expandableListView);
        expand_lv.setAdapter(expand_lv_adapter);

        if(isAdded()) {
            getBankList();
        }

        btn_noData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBankList();
            }
        });

        expand_lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String productType = listDataChild.get(listDataHeader.get(groupPosition).getHeader()).getBankData().get(childPosition).getProductType();
                if (productType.equals(DefineValue.BANKLIST_TYPE_ATM)) {
                    expand_lv_adapter.toggleVisible(v,groupPosition,childPosition);
                } else {
                    selectAction(groupPosition,childPosition);
                }
                return false;
            }
        });

        expand_lv.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                ArrayList<ListBankDataTopup> data = listDataChild.get(listDataHeader.get(groupPosition).getHeader()).getBankData();

                for(int i = 0; i<data.size(); i++){
                    if(data.get(i).getProductType().equals(DefineValue.BANKLIST_TYPE_ATM)){
                        data.get(i).setVisible(false);
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    public void getBankList(){
        try {
            if (isAdded() || isVisible()) {
                final ProgressDialog prodDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
                RequestParams params =  MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BANK_LIST,
                        userID,accessKey);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.MEMBER_ID, memberID );
                params.put(WebParams.TYPE, DefineValue.BANKLIST_TYPE_ALL);
                params.put(WebParams.USER_ID, userID);

                Timber.d("isi params get BankList:" + params.toString());

                MyApiClient.getBankList(getActivity(),params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Timber.d("response Listbank:"+response.toString());
                                if (isAdded()) {
                                    if(nodata_view.getVisibility() == View.VISIBLE) {
                                        nodata_view.setVisibility(View.GONE);
                                        layout_list_view.setVisibility(View.VISIBLE);
                                    }
                                    insertBankList(response.getJSONObject(WebParams.BANK_DATA), response.optString(WebParams.OTHER_ATM,""));
                                    prodDialog.dismiss();
                                }
                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                Timber.d("isi response autologout:"+response.toString());
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                if(is_full_activity)
                                    test.showDialoginActivity(getActivity(),message);
                                else
                                    test.showDialoginMain(getActivity(),message);
                            }
                            else {
                                Timber.d("Error ListMember comlist:"+response.toString());
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                prodDialog.dismiss();
                                if(nodata_view.getVisibility() == View.GONE) {
                                    nodata_view.setVisibility(View.VISIBLE);
                                    layout_list_view.setVisibility(View.GONE);
                                }
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
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
                        if(getActivity()!=null && !getActivity().isFinishing()) {
                            if (MyApiClient.PROD_FAILURE_FLAG)
                                Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                            if (prodDialog.isShowing())
                                prodDialog.dismiss();
                            if(nodata_view.getVisibility() == View.GONE) {
                                nodata_view.setVisibility(View.VISIBLE);
                                layout_list_view.setVisibility(View.GONE);
                            }
                        }
                        Timber.w("Error Koneksi bank list list topup:"+throwable.toString());
                    }
                });
            }
        }catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void insertBankList(JSONObject bank_data, String other_atm){
        try {
            if(bank_data.length() > 0){
                //Siapin Json tambahan dari file raw
                JSONObject otherItemVA = new JsonUtil(getActivity()).readFromRaw(R.raw.topup_atm_item);
                //temp_list_data_bank untuk menyimpan data bank per bank code
                BankDataTopUp temp_list_data_bank;
                //temp_other_atm untuk menyimpan data bank khusus ditampilkan di other atm
                BankDataTopUp temp_other_atm = null;
                //tempListBankModels menyimpan data product bank bentuk array untuk dimasukan ke temp_list_data_bank(BankDataTopup)
                ArrayList<ListBankDataTopup> tempListBankModels;
                String bankCode;
                for (int i = 0; i < bank_data.length(); i++) {
                    //ambil bankcode dari json response
                    bankCode = bank_data.names().getString(i);
                    //ambil list product bank sesuai dengan bankcode index sekrang
                    JSONArray listProduct = bank_data.getJSONArray(bankCode);
                    tempListBankModels = new ArrayList<>();
                    String fee="",va="";

                    for (int j = 0 ; j < listProduct.length() ; j++){
                        //siapin listDataBank
                        listBankModel listBankModel = new listBankModel(bankCode,
                                listProduct.getJSONObject(j).getString(WebParams.BANK_NAME),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_CODE),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_NAME),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_TYPE),
                                listProduct.getJSONObject(j).getString(WebParams.PRODUCT_H2H));

                        //simpen data va dan fee jika tipenya atm
                        if(listBankModel.getProduct_type().equalsIgnoreCase(DefineValue.BANKLIST_TYPE_ATM)){
                            fee = listProduct.getJSONObject(j).getString(WebParams.FEE);
                            va = listProduct.getJSONObject(j).getString(WebParams.NO_VA);
                        }

                        //isi data produk other atm jika ada
                        if(!va.isEmpty() && other_atm.equals(bankCode) && listBankModel.getProduct_type().equals(DefineValue.BANKLIST_TYPE_ATM)){
                            String vaOtherATM = bankCode+" + "+va;
                            ArrayList<ListBankDataTopup> listBankModels = new ArrayList<>();
                            listBankModels.add(new ListBankDataTopup(listBankModel));
                            temp_other_atm = new BankDataTopUp(listBankModels,bankCode,vaOtherATM,fee);
                        }

                        tempListBankModels.add(new ListBankDataTopup(listBankModel));
                    }

                    //masukan data product other atm bank ke templistbankModel sebagai tambahan product yg didapat dari raw json
                    if(!va.isEmpty() && otherItemVA.has(bankCode)){
                        JSONArray otherProduct = otherItemVA.getJSONArray(bankCode);
                        for(int j = 0; j < otherProduct.length();j++){
                            listBankModel listBankModel = new listBankModel(bankCode,
                                    otherProduct.getJSONObject(j).getString(WebParams.BANK_NAME),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_CODE),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_NAME),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_TYPE),
                                    otherProduct.getJSONObject(j).getString(WebParams.PRODUCT_H2H));

                            tempListBankModels.add(new ListBankDataTopup(listBankModel));
                        }
                    }

                    //masukin data listbankmodel, va , fee digabung ke class BankDataTopUp
                    String bankName = tempListBankModels.get(0).getListBankModel().getBank_name();
                    temp_list_data_bank = new BankDataTopUp(tempListBankModels,bankCode,va,fee);

                    //masukin nama bank untuk title header dan nama bank, BankDataTopup ke hashmap child
                    listDataHeader.add(new BankHeaderTopUp(bankName));
                    listDataChild.put(bankName,temp_list_data_bank);
                }

                //sorting title header
                Collections.sort(listDataHeader,new BankHeaderTopUp.CustomComparator());

                //tambahin ATM lain ke header dan child jika ada
                if(!other_atm.isEmpty() && temp_other_atm !=null){
                    listDataHeader.add(new BankHeaderTopUp(getString(R.string.other_atm)));
                    listDataChild.put(getString(R.string.other_bank),temp_other_atm);
                }
                expand_lv_adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void selectAction(int groupPos, int childPos){
        ListBankDataTopup model = listDataChild.get(listDataHeader.get(groupPos).getHeader()).getBankData().get(childPos);
        if(is_full_activity){
            Fragment mFrag = new SgoPlus_input();
            String titleToolbar = model.getBankName()+" - "+model.getProductName();
            Bundle mBun = new Bundle();
            mBun.putString(DefineValue.BANK_CODE,model.getBankCode());
            mBun.putString(DefineValue.BANK_NAME,model.getBankName());
            mBun.putString(DefineValue.PRODUCT_NAME,model.getProductName());
            mBun.putString(DefineValue.PRODUCT_CODE,model.getProductCode());
            mBun.putString(DefineValue.PRODUCT_TYPE,model.getProductType());
            mBun.putString(DefineValue.PRODUCT_H2H,model.getProductH2H());
            mFrag.setArguments(mBun);
            if(model.getProductType().equals(DefineValue.BANKLIST_TYPE_SMS)) {
                if (!levelClass.isLevel1QAC()) {
                    switchFragmentTopUpActivity(mFrag,titleToolbar,true);
                } else
                    levelClass.showDialogLevel();
            }
            else
                switchFragmentTopUpActivity(mFrag,titleToolbar,true);
        }
        else {
            Intent i = new Intent(getActivity(), TopUpActivity.class);
            i.putExtra(DefineValue.BANK_CODE,model.getBankCode());
            i.putExtra(DefineValue.BANK_NAME,model.getBankName());
            i.putExtra(DefineValue.PRODUCT_NAME,model.getProductName());
            i.putExtra(DefineValue.PRODUCT_CODE,model.getProductCode());
            i.putExtra(DefineValue.PRODUCT_TYPE,model.getProductType());
            i.putExtra(DefineValue.PRODUCT_H2H,model.getProductH2H());
            if(model.getProductType().equals(DefineValue.BANKLIST_TYPE_SMS)) {
                if (!levelClass.isLevel1QAC()) {
                    switchActivity(i);
                }
                else
                    levelClass.showDialogLevel();
            }
            else
                switchActivity(i);

        }
    }


    private void switchFragmentTopUpActivity(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private String parseMaxTopupValue(String value){
        try {
            String split[] = value.split(" ");
            return split[0] + " " + CurrencyFormat.format(split[1]);
        }catch (Exception e){
            return "";
        }

    }

    //broadcast service balance

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("receiver service balance");
            if (!sp.getBoolean(DefineValue.IS_AGENT, true)){
                max_topup_holder.setText("Max Topup: "+parseMaxTopupValue(sp.getString(DefineValue.BALANCE_MAX_TOPUP, "")));
            }else
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Timber.d("attach list top up");
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
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onOkButton() {

    }
}