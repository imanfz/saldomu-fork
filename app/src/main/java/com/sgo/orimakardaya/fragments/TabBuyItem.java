package com.sgo.orimakardaya.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.Biller_Type_Data_Model;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.BillerActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.WebParams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import timber.log.Timber;

/*
  Created by Administrator on 3/3/2015.
 */
public final class TabBuyItem extends ListFragment {

    private View v;
    private View layout_empty;
    ProgressDialog out;
    private SecurePreferences sp;
    private String userID;
    private String accessKey;
    private String biller_type;
    private Biller_Type_Data_Model mBillerType;
    private RealmChangeListener realmListener;
    private List<Biller_Type_Data_Model> mBillerTypeData;
    private ArrayList<String> _data;
    private ListView listView1;
    private EasyAdapter adapter;
    private Realm realm;


    public static TabBuyItem newInstance(String biller_type) {
        TabBuyItem mFrag = new TabBuyItem();
        mFrag.biller_type = biller_type;
        return mFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_tab_buy_item, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        layout_empty = v.findViewById(R.id.empty_layout);
        layout_empty.setVisibility(View.GONE);
        realm = Realm.getDefaultInstance();

        realmListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {
                if(isVisible()){
                    Timber.d("masukk realm listener gannnn");
                    mBillerTypeData = realm.where(Biller_Type_Data_Model.class)
                            .equalTo(WebParams.BILLER_TYPE, biller_type)
                            .findAll();
                    if(mBillerTypeData.size()>0) {
                        layout_empty.setVisibility(View.GONE);
                        listView1.setVisibility(View.VISIBLE);
                        _data.clear();
                        for (int i = 0; i < mBillerTypeData.size(); i++) {
                            _data.add(mBillerTypeData.get(i).getBiller_type_name());
                        }
                        adapter.notifyDataSetChanged();
                        adapter.notifyDataSetInvalidated();
                    }
                    else {
                        layout_empty.setVisibility(View.VISIBLE);
                        listView1.setVisibility(View.GONE);
                    }
                }
            }};
        realm.addChangeListener(realmListener);

        initializeData();

    }

    private void initializeData(){

        mBillerTypeData = realm.where(Biller_Type_Data_Model.class).
                equalTo(WebParams.BILLER_TYPE,biller_type)
                .findAll();

        _data = new ArrayList<>();

        if(mBillerTypeData.size() > 0) {
            for (int i = 0; i < mBillerTypeData.size(); i++) {
               _data.add(mBillerTypeData.get(i).getBiller_type_name());
            }
        }
        adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);
    }

    private class NameComparator implements Comparator<String>
    {
        public int compare(String left, String right) {
            return left.compareTo(right);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String _listItem = l.getAdapter().getItem(position).toString();
        Timber.d("isi click listitem "+_listItem);
        mBillerType = realm.where(Biller_Type_Data_Model.class).
                equalTo(WebParams.BILLER_TYPE_ID,mBillerTypeData.get(position).getBiller_type_id()).
                findFirst();
//        getBillerList(mBillerType.getBiller_type_code(), mBillerType.getBiller_type_name(),mBillerType.getBiller_type_id());
        openIntentBiller(mBillerType.getBiller_type_code(), mBillerType.getBiller_type_name());

    }

//   public void getBillerList(final String _biller_type, final String _biller_name, final String _biller_type_id){
//        try{
//            out = DefinedDialog.CreateProgressDialog(getActivity(), null);
//            out.show();
//
//            //String comm_id = sp.getString(CoreApp.COMMUNITY_ID,"");
//
//            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_LIST_BILLER,
//                    userID,accessKey);
//            //params.put(WebParams.COMM_ID, comm_id);
//            params.put(WebParams.BILLER_TYPE, _biller_type);
//            params.put(WebParams.USER_ID, userID);
//            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
//
//            Timber.d("isi params get biller list merchantnya:" + params.toString());
//
//            MyApiClient.sentListBiller(getActivity(),params,new JsonHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    try {
//                        out.dismiss();
//                        String code = response.getString(WebParams.ERROR_CODE);
//                        if (code.equals(WebParams.SUCCESS_CODE)) {
//                            Timber.d("Isi response get Biller list:"+response.toString());
//                            final String arrayBiller = response.getString(WebParams.BILLER_DATA);
//
//                            final Realm realm = Realm.getDefaultInstance();
//                            realm.executeTransactionAsync(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    Biller_Type_Data_Model mBillerTypeData = realm.where(Biller_Type_Data_Model.class)
//                                                                                    .equalTo("biller_type_id", _biller_type_id)
//                                                                                    .findFirst();
//
//                                    if(mBillerTypeData.getBiller_data_models().size()>0){
//                                        Denom_Data_Model mObj;
//                                        List<Biller_Data_Model> refObj =  mBillerTypeData.getBiller_data_models() ;
//                                        for (int i = 0; i < mBillerTypeData.getBiller_data_models().size();i++){
//                                            mObj = realm.where(Denom_Data_Model.class).
//                                                    equalTo("comm_id", refObj.get(i).getComm_id()).
//                                                    equalTo("comm_name", refObj.get(i).getComm_name()).
//                                                    findFirst();
//                                            mObj.removeFromRealm();
//                                        }
//                                    }
//
//                                    mBillerTypeData.getBiller_data_models().deleteAllFromRealm();
//
//                                    JSONArray jsonBiller;
//                                    String curr_date = DateTimeFormat.getCurrentDateMinus();
//                                    Biller_Data_Model mObj;
//                                    try {
//                                        jsonBiller = new JSONArray(arrayBiller);
//                                        if (jsonBiller.length() > 0) {
//                                            for (int i = 0; i < jsonBiller.length(); i++) {
//                                                mObj = realm.createObjectFromJson(Biller_Data_Model.class, jsonBiller.getJSONObject(i));
//                                                mObj.setLast_update(curr_date);
//                                                mBillerTypeData.getBiller_data_models().add(mObj);
//                                                Timber.d("isi array biller realm idx : " + jsonBiller.getJSONObject(i).getString(WebParams.COMM_ID));
//                                            }
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//                            }, new Realm.Transaction.OnSuccess() {
//                                @Override
//                                public void onSuccess() {
//                                    realm.close();
//                                }
//                            }, new Realm.Transaction.OnError() {
//                                @Override
//                                public void onError(Throwable error) {
//                                    realm.close();
//                                }
//                            });
//
//                            openIntentBiller(arrayBiller, _biller_name, _biller_type);
//                        }
//                        else if(code.equals(WebParams.LOGOUT_CODE)){
//                            Timber.d("isi response autologout:"+response.toString());
//                            String message = response.getString(WebParams.ERROR_MESSAGE);
//                            AlertDialogLogout test = AlertDialogLogout.getInstance();
//                            test.showDialoginMain(getActivity(),message);
//                        }
//                        else {
//                            code = response.getString(WebParams.ERROR_MESSAGE);
//                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    super.onFailure(statusCode, headers, responseString, throwable);
//                    failure(throwable);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                    super.onFailure(statusCode, headers, throwable, errorResponse);
//                    failure(throwable);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                    super.onFailure(statusCode, headers, throwable, errorResponse);
//                    failure(throwable);
//                }
//
//                private void failure(Throwable throwable){
//                    if (MyApiClient.PROD_FAILURE_FLAG)
//                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
//                    else
//                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
//                    if(out.isShowing())
//                        out.dismiss();
//                    Timber.w("Error Koneksi biller list tabbuyitem:"+throwable.toString());
//                }
//            });
//        }catch (Exception e){
//            Timber.d("httpclient:"+e.getMessage());
//        }
//    }

//    public void openIntentBiller(String _arrayBiller, String _biller_name, String _biller_type){
//        Intent i = new Intent(getActivity(), BillerActivity.class);
//        i.putExtra(DefineValue.BILLER_DATA,_arrayBiller);
//        i.putExtra(DefineValue.BILLER_NAME,_biller_name);
//        i.putExtra(DefineValue.BILLER_TYPE,_biller_type);
//        switchActivity(i);
//    }

    private void openIntentBiller(String _biller_type, String _biller_name){
        Intent i = new Intent(getActivity(), BillerActivity.class);
        i.putExtra(DefineValue.BILLER_TYPE,_biller_type);
        i.putExtra(DefineValue.BILLER_NAME,_biller_name);
        switchActivity(i);
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed()) {
            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }
}