package com.sgo.orimakardaya.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.listbankModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.TopUpActivity;
import com.sgo.orimakardaya.activities.TopUpAtmActivity;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.InformationDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class ListTopUp extends ListFragment implements InformationDialog.OnDialogOkCallback {

    View v;
    SecurePreferences sp;
    ArrayList<String> _listType;
    String listBankIB, listBankSMS,userID,accessKey,memberID;
    EasyAdapter adapter;
    ArrayList<listbankModel> mlistbankIB = null, mlistbankSMS = null;
    Boolean is_full_activity = false;
    private InformationDialog dialogI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_topup, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        memberID = sp.getString(DefineValue.MEMBER_ID, "");

        dialogI = InformationDialog.newInstance(this,0);

        listBankIB = null;
        listBankSMS = null;

        Bundle mArgs = getArguments();
        if(mArgs != null && !mArgs.isEmpty())
            is_full_activity = mArgs.getBoolean(DefineValue.IS_ACTIVITY_FULL,false);

        _listType = new ArrayList<String>();

        adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _listType);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);
        if(isAdded()) {
            getBankList();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                                    String atm_topup_data = response.optString(WebParams.ATM_TOPUP_DATA,"");
                                    Timber.d("atm topup:" + atm_topup_data);
                                    String bank_code = "";
                                    String no_va = "";
                                    String bank_name = "";
                                    JSONArray mArrayATM;
                                    if(atm_topup_data != null && !atm_topup_data.isEmpty() && !atm_topup_data.equals("null"))
                                        mArrayATM = new JSONArray(atm_topup_data);
                                    else
                                        mArrayATM = new JSONArray();
                                    for (int i = 0; i < mArrayATM.length(); i++) {
                                        if (i == mArrayATM.length() - 1) {
                                            bank_code += mArrayATM.getJSONObject(i).getString(WebParams.BANK_CODE);
                                            no_va += mArrayATM.getJSONObject(i).getString(WebParams.NO_VA);
                                            bank_name += mArrayATM.getJSONObject(i).getString(WebParams.BANK_NAME);
                                        } else {
                                            bank_code += mArrayATM.getJSONObject(i).getString(WebParams.BANK_CODE) + ",";
                                            no_va += mArrayATM.getJSONObject(i).getString(WebParams.NO_VA) + ",";
                                            bank_name += mArrayATM.getJSONObject(i).getString(WebParams.BANK_NAME) + ",";
                                        }
                                    }
                                    Timber.d("atm topup:" + bank_name);

                                    SecurePreferences.Editor mEditor = sp.edit();
                                    mEditor.putString(DefineValue.BANK_ATM_CODE, bank_code);
                                    mEditor.putString(DefineValue.NO_VA, no_va);
                                    mEditor.putString(DefineValue.BANK_ATM_NAME, bank_name);
                                    mEditor.apply();

                                    if(!no_va.isEmpty()){
                                        Collections.addAll(_listType, getResources().getStringArray(R.array.topup_list_item));
                                    }

                                    insertBankList(new JSONArray(response.getString(WebParams.BANK_DATA)));
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
                        }
                        Timber.w("Error Koneksi bank list list topup:"+throwable.toString());
                    }
                });
            }
        }catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void insertBankList(JSONArray arrayJson){
        listbankModel mLB;
        try {

            if(mlistbankIB != null)
                mlistbankIB.clear();

            if(mlistbankSMS != null)
                mlistbankSMS.clear();


            for (int i = 0; i < arrayJson.length(); i++) {
                mLB = new listbankModel();
                mLB.setBank_name(arrayJson.getJSONObject(i).getString(WebParams.BANK_NAME));
                mLB.setBank_code(arrayJson.getJSONObject(i).getString(WebParams.BANK_CODE));
                mLB.setProduct_code(arrayJson.getJSONObject(i).getString(WebParams.PRODUCT_CODE));
                mLB.setProduct_name(arrayJson.getJSONObject(i).getString(WebParams.PRODUCT_NAME));
                mLB.setProduct_type(arrayJson.getJSONObject(i).getString(WebParams.PRODUCT_TYPE));
                mLB.setProduct_h2h(arrayJson.getJSONObject(i).optString(WebParams.PRODUCT_H2H, ""));

                if(mLB.getProduct_type().equals(DefineValue.BANKLIST_TYPE_IB)){
                    if(mlistbankIB == null)
                        mlistbankIB = new ArrayList<listbankModel>();

                    mlistbankIB.add(mLB);

                }

                if(mLB.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS)){
                    if(mlistbankSMS == null)
                        mlistbankSMS = new ArrayList<listbankModel>();

                    mlistbankSMS.add(mLB);
                }
            }

            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            final Gson gson = gsonBuilder.create();

            if(mlistbankIB != null) {
                if(!mlistbankIB.isEmpty()){
                    _listType.add(getString(R.string.internetBanking_ab_title));
                    listBankIB = gson.toJson(mlistbankIB);
                }
            }

            if (mlistbankSMS != null) {
                if(!mlistbankSMS.isEmpty()) {
                    _listType.add(getString(R.string.smsBanking_ab_title));
                    listBankSMS = gson.toJson(mlistbankSMS);
                }
            }
            Timber.wtf("isi data :"+listBankIB);
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i;
        String itemName = String.valueOf(l.getAdapter().getItem(position));
        if((position == 0) && itemName.equals(getString(R.string.atm_ab_title))) {
            i = new Intent(getActivity(), TopUpAtmActivity.class);
            if(is_full_activity)
                switchActivityTopUpActivity(i);
            else
                switchActivity(i);
        }
        else {
            if(is_full_activity){
                Fragment mFrag = new SgoPlus_input();
                Bundle mBun = new Bundle();

                if (itemName.equals(getString(R.string.internetBanking_ab_title))) {
                    mBun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.INTERNET_BANKING);
                    mBun.putString(DefineValue.BANKLIST_DATA, listBankIB);
                } else if (itemName.equals(getString(R.string.smsBanking_ab_title))) {
                    mBun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.SMS_BANKING);
                    mBun.putString(DefineValue.BANKLIST_DATA, listBankSMS);
                }

                mFrag.setArguments(mBun);
                switchFragmentTopUpActivity(mFrag,itemName,true);
            }
            else {
                i = new Intent(getActivity(), TopUpActivity.class);
                if (itemName.equals(getString(R.string.internetBanking_ab_title))) {
                    i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.INTERNET_BANKING);
                    i.putExtra(DefineValue.BANKLIST_DATA, listBankIB);
                } else if (itemName.equals(getString(R.string.smsBanking_ab_title))) {
                    i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.SMS_BANKING);
                    i.putExtra(DefineValue.BANKLIST_DATA, listBankSMS);
                }
                switchActivity(i);
            }
        }

    }

    private void switchFragmentTopUpActivity(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
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

    private void switchActivityTopUpActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
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