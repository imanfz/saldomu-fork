package com.sgo.orimakardaya.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.Biller_Data_Model;
import com.sgo.orimakardaya.Beans.Biller_Type_Data_Model;
import com.sgo.orimakardaya.Beans.Denom_Data_Model;
import com.sgo.orimakardaya.Beans.bank_biller_model;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;

import org.apache.http.Header;
import org.joda.time.DateTimeComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import timber.log.Timber;

/*
  Created by Administrator on 1/30/2015.
 */
public class BillerActivityRF extends Fragment{

    public static final String BILLERACTIV_TAG = "billerActivRF";

    View v;
    String userID,accessKey;
    SecurePreferences sp;
    private Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        realm = Realm.getDefaultInstance();

    }


    public void getBillerList(final String _biller_type, final String _biller_type_id, final Boolean withDenom){

        if(realm == null)
            realm = Realm.getDefaultInstance();

        int compare = 100;
        Biller_Data_Model tempBillerData = realm.where(Biller_Data_Model.class).
                        equalTo(WebParams.BILLER_TYPE,_biller_type).
                        findFirst();

        if(tempBillerData != null) {
            String date = tempBillerData.getLast_update();


            if (date != null) {
                Date dob;
                Date now;
                dob = DateTimeFormat.convertCustomDate(date);
                now = DateTimeFormat.getCurrDate();

                if (dob != null) {
                    if (now != null) {
                        compare = DateTimeComparator.getDateOnlyInstance().compare(dob, now);
                    }
                }
            }
        }

        if (compare != 0)
            getBiller(_biller_type, _biller_type_id, withDenom);
    }


    public void getBiller(final String _biller_type, final String _biller_type_id, final Boolean withDenom){
        try{
            sp = CustomSecurePref.getInstance().getmSecurePrefs();
            userID = sp.getString(DefineValue.USERID_PHONE, "");
            accessKey = sp.getString(DefineValue.ACCESS_KEY, "");


            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_LIST_BILLER,
                    userID,accessKey);
            //params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.BILLER_TYPE, _biller_type);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get biller list merchantnya:" + params.toString());

            MyApiClient.sentListBiller(getActivity(),params,new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response get Biller list:"+response.toString());
                            final String arrayBiller = response.getString(WebParams.BILLER_DATA);
                            insertUpdateData(new JSONArray(arrayBiller),_biller_type_id, withDenom);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
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
                    Timber.w("Error Koneksi biller list tabbuyitem:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void insertUpdateData(final JSONArray arrayBiller, final String _biller_type_id, final Boolean withDenom){

        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Biller_Type_Data_Model mBillerTypeData = realm.where(Biller_Type_Data_Model.class)
                        .equalTo("biller_type_id", _biller_type_id)
                        .findFirst();

                Timber.d("isi mbillerdata isinya "+ mBillerTypeData.getBiller_data_models().size());
                List<Biller_Data_Model> temptData = new ArrayList<>();
                if(mBillerTypeData.getBiller_data_models().size()>0){
                    Biller_Data_Model mObj;
                    List<Biller_Data_Model> refObj =  mBillerTypeData.getBiller_data_models() ;
                    for (int i = 0; i < mBillerTypeData.getBiller_data_models().size();i++){
                        mObj = realm.where(Biller_Data_Model.class).
                                equalTo("comm_id", refObj.get(i).getComm_id()).
                                equalTo("comm_name", refObj.get(i).getComm_name()).
                                findFirst();
                        if(mObj.getItem_id().isEmpty())
                            temptData.add(realm.copyFromRealm(mObj));
                        mObj.removeFromRealm();
                    }
                }

                mBillerTypeData.getBiller_data_models().deleteAllFromRealm();

                JSONArray jsonBankBiller;
                String curr_date = DateTimeFormat.getCurrentDate();
                Biller_Data_Model mObj = null;
                Denom_Data_Model mDenomData;
                bank_biller_model refObj;
                try {
                    if (arrayBiller.length() > 0) {
                        for (int i = 0; i < arrayBiller.length(); i++) {
                            mObj = realm.createObjectFromJson(Biller_Data_Model.class, arrayBiller.getJSONObject(i));
                            mObj.setLast_update(curr_date);


                            if(temptData.size() > 0) {
                                for (int j = 0; j < temptData.size(); j++) {
                                    if (mObj.getComm_id().equals(temptData.get(j).getComm_id()) &&
                                            mObj.getComm_name().equals(temptData.get(j).getComm_name())) {
                                        for (int k = 0; k < temptData.get(j).getDenom_data_models().size(); k++) {
                                            mDenomData = realm.where(Denom_Data_Model.class).
                                                    equalTo(WebParams.DENOM_ITEM_ID, temptData.get(j).getDenom_data_models().get(k).getItem_id()).
                                                    findFirst();
                                            mObj.getDenom_data_models().add(mDenomData);
                                        }
                                        break;
                                    }
                                }
                            }

                            jsonBankBiller = new JSONArray(arrayBiller.getJSONObject(i).getString(WebParams.BANK_BILLER));
                            if(jsonBankBiller.length()>0) {
                                for (int j = 0; j < jsonBankBiller.length(); j++) {
                                    JSONObject mJob = jsonBankBiller.getJSONObject(j);
                                    refObj= new bank_biller_model();
                                    refObj.setBank_code(mJob.optString(WebParams.BANK_CODE,""));
                                    refObj.setBank_name(mJob.optString(WebParams.BANK_NAME,""));
                                    refObj.setProduct_code(mJob.optString(WebParams.PRODUCT_CODE,""));
                                    refObj.setProduct_name(mJob.optString(WebParams.PRODUCT_NAME,""));
                                    refObj.setProduct_type(mJob.optString(WebParams.PRODUCT_TYPE,""));
                                    refObj.setProduct_h2h(mJob.optString(WebParams.PRODUCT_H2H,""));
                                    refObj.setLast_update(curr_date);

                                    realm.copyToRealmOrUpdate(refObj);
                                    mObj.getBank_biller_models().add(refObj);
                                }
                            }

                            mBillerTypeData.getBiller_data_models().add(mObj);
                            Timber.d("isi array biller realm idx : " + arrayBiller.getJSONObject(i).getString(WebParams.COMM_ID));
                        }

                        if(mObj != null){
                            if(withDenom){
                                getDenomRetail(mObj.getComm_id(), mObj.getComm_name());
                            }
                            else {
                                if(arrayBiller.length() == 1) {
                                    getDenomRetail(mObj.getComm_id(), mObj.getComm_name());
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }


    public void getDenomRetail(final String _comm_id, final String _comm_name){

        Biller_Data_Model mBillerData = realm.copyFromRealm(realm.where(Biller_Data_Model.class).
                equalTo(WebParams.COMM_ID,_comm_id).
                equalTo(WebParams.COMM_NAME,_comm_name).
                findFirst());

        String date;
        int compare = 100;
        if(mBillerData.getDenom_data_models().size() > 0 ){
            date = mBillerData.getDenom_data_models().get(0).getLast_update();
            if(date != null) {
                Date dob;
                Date now;
                dob = DateTimeFormat.convertCustomDate(date);
                now = DateTimeFormat.getCurrDate();

                if (dob != null) {
                    if (now != null) {
                        compare = DateTimeComparator.getDateOnlyInstance().compare(dob,now);
                    }
                }
            }
        }

        if(compare != 0)
            getDenom(_comm_id,_comm_name);
    }

    public void getDenom(final String _comm_id, final String _comm_name){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(_comm_id,MyApiClient.LINK_DENOM_RETAIL,
                    userID,accessKey);
            params.put(WebParams.COMM_ID, _comm_id);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE,""));

            Timber.d("isi params sent Denom Retail:" + params.toString());

            MyApiClient.sentDenomRetail(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response Denom Retail:"+response.toString());
                            String arrayDenom = response.getString(WebParams.DENOM_DATA);
                            initializeDenom(new JSONArray(arrayDenom), _comm_id, _comm_name);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(),message);
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
                    Timber.w("Error Koneksi get denom retail:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void initializeDenom(JSONArray denom_data, String _comm_id, String _comm_name){
        if(denom_data.length() > 0){
            try {
                realm.beginTransaction();

                Biller_Data_Model mBillerData = realm.where(Biller_Data_Model.class)
                        .equalTo(WebParams.COMM_ID, _comm_id)
                        .equalTo(WebParams.COMM_NAME, _comm_name)
                        .findFirst();

                if(mBillerData.getDenom_data_models().size()>0){
                    Denom_Data_Model mObj;
                    List<Denom_Data_Model> refObj =  mBillerData.getDenom_data_models() ;
                    for (int i = 0; i < mBillerData.getDenom_data_models().size();i++){
                        mObj = realm.where(Denom_Data_Model.class).
                                equalTo(WebParams.DENOM_ITEM_ID,refObj.get(i).getItem_id()).
                                findFirst();
                        mObj.removeFromRealm();
                    }
                }

                mBillerData.getDenom_data_models().deleteAllFromRealm();

                String curr_date = DateTimeFormat.getCurrentDate();
                Denom_Data_Model mObjRealm;

                for (int i = 0 ;i< denom_data.length();i++){
                    mObjRealm = realm.createObjectFromJson(Denom_Data_Model.class,denom_data.getJSONObject(i));
                    mObjRealm.setLast_update(curr_date);
                    mBillerData.getDenom_data_models().add(mObjRealm);

                }
                realm.commitTransaction();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed())
            realm.close();
        super.onDestroy();
    }
}