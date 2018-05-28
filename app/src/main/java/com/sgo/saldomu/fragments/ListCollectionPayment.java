package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CollectionActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.CollectionBankAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class ListCollectionPayment extends ListFragment {

    private View v;

    private ProgressDialog progdialog;
    private String commID;
    private String userID;
    private String accessKey;
    private ArrayList<String> _listType;
    private ArrayList<TempObjectData> listBankIB;
    private ArrayList<TempObjectData> listBankSMS;
    private ArrayList<TempObjectData> listBankScash;
    private CollectionBankAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_collection_payment, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        _listType = new ArrayList<>();

        listBankIB = new ArrayList<>();
        listBankSMS = new ArrayList<>();
        listBankScash = new ArrayList<>();

        Bundle bundle = getArguments();
        commID = bundle.getString(DefineValue.COMMUNITY_ID);

        setActionBarTitle(bundle.getString(DefineValue.COMMUNITY_NAME));

        sentBankCollect();

        adapter = new CollectionBankAdapter(getActivity(),R.layout.list_view_item_with_arrow, _listType);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment mFrag;

                Bundle mbun = getArguments();
                String title;

                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                final Gson gson = gsonBuilder.create();

                if(_listType.get(position).equalsIgnoreCase("Internet Banking")) {
                    String listBankIBJson = gson.toJson(listBankIB);
                    Timber.d("isi json build:"+listBankIBJson);
                    mbun.putString(DefineValue.BANKLIST_DATA, listBankIBJson);
                    mbun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.INTERNET_BANKING);

                    title = getToolbarTitle()+"-"+getString(R.string.internetBanking_ab_title);
                }
                else if(_listType.get(position).equalsIgnoreCase("SMS Banking")) {
                    String listBankSMSJson = gson.toJson(listBankSMS);
                    mbun.putString(DefineValue.BANKLIST_DATA, listBankSMSJson);
                    mbun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.SMS_BANKING);

                    title = getToolbarTitle()+"-"+getString(R.string.smsBanking_ab_title);
                }
                else {
                    String listBankScashJson = gson.toJson(listBankScash);
                    mbun.putString(DefineValue.BANKLIST_DATA, listBankScashJson);
                    mbun.putString(DefineValue.TRANSACTION_TYPE, DefineValue.EMONEY);

                    title = getToolbarTitle()+"-"+getString(R.string.scash);
                }

                mFrag = new CollectionInput();
                mFrag.setArguments(mbun);
                switchFragment(mFrag,title,true);

            }
        });
    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private class TempObjectData {

        private String bank_name;
        private String bank_code;
        private String product_code;
        private String product_name;
        private String product_type;
        private String product_h2h;

        public TempObjectData(String _bank_code, String _bank_name,String _product_code, String _product_name,
                             String _product_type, String _product_h2h){
            this.setBank_code(_bank_code);
            this.setBank_name(_bank_name);
            this.setProduct_code(_product_code);
            this.setProduct_name(_product_name);
            this.setProduct_type(_product_type);
            this.setProduct_h2h(_product_h2h);
        }

        public String getBank_code() {
            return bank_code;
        }

        public void setBank_code(String bank_code) {
            this.bank_code = bank_code;
        }

        public String getBank_name() {
            return bank_name;
        }

        public void setBank_name(String bank_name) {
            this.bank_name = bank_name;
        }

        public String getProduct_code() {
            return product_code;
        }

        public void setProduct_code(String product_code) {
            this.product_code = product_code;
        }

        public String getProduct_h2h() {
            return product_h2h;
        }

        public void setProduct_h2h(String product_h2h) {
            this.product_h2h = product_h2h;
        }

        public String getProduct_name() {
            return product_name;
        }

        public void setProduct_name(String product_name) {
            this.product_name = product_name;
        }

        public String getProduct_type() {
            return product_type;
        }

        public void setProduct_type(String product_type) {
            this.product_type = product_type;
        }
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private String getToolbarTitle(){
        if (getActivity() == null)
            return null;

        CollectionActivity fca = (CollectionActivity) getActivity();
        return fca.getToolbarTitle();
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        CollectionActivity fca = (CollectionActivity) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Timber.d("attach list top up");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void separateType(JSONArray _mdata) {
        try {
            for(int j=0 ; j<_mdata.length() ; j++) {
                boolean flagSame = false;
                if(_listType.size() > 0) {
                    Timber.d("list type length flagSame:"+Integer.toString(_listType.size()));
                    for (String a_listType : _listType) {
                        if (a_listType.equalsIgnoreCase(_mdata.getJSONObject(j).getString(WebParams.PRODUCT_TYPE))) {
                            flagSame = true;
                            break;
                        } else {
                            flagSame = false;
                        }
                    }
                }

                if (!flagSame) {
                    if (_mdata.getJSONObject(j).getString(WebParams.PRODUCT_TYPE).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_IB)) {
                        _listType.add(_mdata.getJSONObject(j).getString(WebParams.PRODUCT_TYPE));
                    } else if (_mdata.getJSONObject(j).getString(WebParams.PRODUCT_TYPE).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_SMS)) {
                        _listType.add(_mdata.getJSONObject(j).getString(WebParams.PRODUCT_TYPE));
                    } else {
                        _listType.add(_mdata.getJSONObject(j).getString(WebParams.PRODUCT_TYPE));
                    }
                }
            }

            for(int i=0 ; i<_mdata.length() ; i++) {
                String bank_code = _mdata.getJSONObject(i).getString(WebParams.BANK_CODE);
                String bank_name = _mdata.getJSONObject(i).getString(WebParams.BANK_NAME);
                String product_code = _mdata.getJSONObject(i).getString(WebParams.PRODUCT_CODE);
                String product_h2h = _mdata.getJSONObject(i).getString(WebParams.PRODUCT_H2H);
                String product_name = _mdata.getJSONObject(i).getString(WebParams.PRODUCT_NAME);
                String product_type = _mdata.getJSONObject(i).getString(WebParams.PRODUCT_TYPE);

                if(_mdata.getJSONObject(i).getString(WebParams.PRODUCT_TYPE).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_IB)) {
                    listBankIB.add(new TempObjectData(bank_code, bank_name, product_code, product_name, product_type, product_h2h));
                }
                else if(_mdata.getJSONObject(i).getString(WebParams.PRODUCT_TYPE).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_SMS)) {
                    listBankSMS.add(new TempObjectData(bank_code, bank_name, product_code, product_name, product_type, product_h2h));
                }
                else if(_mdata.getJSONObject(i).getString(WebParams.PRODUCT_TYPE).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_EMO)) {
                    listBankScash.add(new TempObjectData(bank_code, bank_name, product_code, product_name, product_type, product_h2h));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i=0 ; i<_listType.size() ; i++) {
            if(_listType.get(i).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_IB)) _listType.set(i, "Internet Banking");
            else if(_listType.get(i).equalsIgnoreCase(DefineValue.BANKLIST_TYPE_SMS)) _listType.set(i, "SMS Banking");
            else _listType.set(i, "S-Cash");

        }
        Timber.d("list type length:"+Integer.toString(_listType.size()));
        adapter.notifyDataSetChanged();
    }

    private void sentBankCollect(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();



            RequestParams params = MyApiClient.getSignatureWithParams(commID,MyApiClient.LINK_BANK_ACCOUNT_COLLECTION,
                    userID,accessKey, commID);
            params.put(WebParams.COMM_ID, commID);
            params.put(WebParams.TYPE, DefineValue.BANKLIST_TYPE_ALL);
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params bank collection:" + params.toString());

            MyApiClient.sentBankAccountCollection(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Timber.d("isi response bank collection:"+response.toString());
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            JSONArray mData = new JSONArray(response.getString(WebParams.BANK_DATA));
                            separateType(mData);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }
                        progdialog.dismiss();

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

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi bank collect collectpayment:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


}