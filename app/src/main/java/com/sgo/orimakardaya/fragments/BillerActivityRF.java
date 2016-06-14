package com.sgo.orimakardaya.fragments;

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
import com.sgo.orimakardaya.activities.BillerActivity;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;

import org.apache.http.Header;
import org.joda.time.DateTimeComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import timber.log.Timber;

/*
  Created by Administrator on 1/30/2015.
 */
public class BillerActivityRF extends Fragment{

    public static final String BILLERACTIV_TAG = "billerActivRF";

    View v;
    String userID,accessKey;
    SecurePreferences sp;
    Boolean isBillerDataExe;
    Boolean isDenomExe;
    private Realm realm;
    private RealmChangeListener realmListener;

    private ArrayList<Object> Queing;

    public class exeBillerData{
        private Boolean isWithDenom;
        private JSONArray dataArray;
        private String billerTypeCode;


        public exeBillerData(JSONArray _dataArray, String _billerTypeCode, Boolean _isWithDenom){
            this.setDataArray(_dataArray);
            this.setWithDenom(_isWithDenom);
            this.setBillerTypeCode(_billerTypeCode);
        }

        public Boolean getWithDenom() {
            return isWithDenom;
        }

        public void setWithDenom(Boolean withDenom) {
            isWithDenom = withDenom;
        }

        public JSONArray getDataArray() {
            return dataArray;
        }

        public void setDataArray(JSONArray dataArray) {
            this.dataArray = dataArray;
        }


        public String getBillerTypeCode() {
            return billerTypeCode;
        }

        public void setBillerTypeCode(String billerTypeCode) {
            this.billerTypeCode = billerTypeCode;
        }
    }

    public class exeDenomData{
        private JSONArray dataArray;
        private String commID;
        private String commName;

        public exeDenomData(JSONArray _dataArray, String _commID, String _commName){
            this.setDataArray(_dataArray);
            this.setCommID(_commID);
            this.setCommName(_commName);
        }

        public JSONArray getDataArray() {
            return dataArray;
        }

        public void setDataArray(JSONArray dataArray) {
            this.dataArray = dataArray;
        }

        public String getCommID() {
            return commID;
        }

        public void setCommID(String commID) {
            this.commID = commID;
        }

        public String getCommName() {
            return commName;
        }

        public void setCommName(String commName) {
            this.commName = commName;
        }
    }

    public void addToQueeing(Object obj){
        if(Queing == null)
            Queing = new ArrayList<>();

        Queing.add(obj);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isBillerDataExe = false;
        isDenomExe = false;

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        Queing = new ArrayList<>();
        realm = Realm.getDefaultInstance();


        realmListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                runQueing();
            }
        };
    }
    
    public void runQueing(){
        if(Queing.size()>0){
            for (Object obj : Queing){
                if(obj instanceof exeBillerData){
                    exeBillerData _obj =(exeBillerData)obj;
                    insertUpdateData(_obj.getDataArray(),_obj.getBillerTypeCode(),_obj.getWithDenom());
                    Queing.remove(obj);
                }
                else if(obj instanceof exeDenomData){
                    exeDenomData _obj =(exeDenomData)obj;
                    initializeDenom(_obj.getDataArray(),_obj.getCommID(),_obj.getCommName());
                    Queing.remove(obj);
                }
            }
        }
    }


    public void getBillerList(final String _biller_type_code, final Boolean withDenom){

        if(realm == null)
            realm = Realm.getDefaultInstance();

        int compare = 100;
        Biller_Data_Model tempBillerData = realm.where(Biller_Data_Model.class).
                        equalTo(WebParams.BILLER_TYPE,_biller_type_code).
                        findFirst();

        if(tempBillerData != null) {
            String date = tempBillerData.getLast_update();


            if (date != null) {
                Date dob;
                Date now;
                dob = DateTimeFormat.convertStringtoCustomDate(date);
                now = DateTimeFormat.getCurrDate();

                if (dob != null) {
                    if (now != null) {
                        compare = DateTimeComparator.getDateOnlyInstance().compare(dob, now);
                    }
                }
            }
        }

        if (compare != 0)
            getBiller(_biller_type_code, withDenom);
    }


    public void getBiller(final String _biller_type_code, final Boolean withDenom){
        try{
            sp = CustomSecurePref.getInstance().getmSecurePrefs();
            userID = sp.getString(DefineValue.USERID_PHONE, "");
            accessKey = sp.getString(DefineValue.ACCESS_KEY, "");


            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_LIST_BILLER,
                    userID,accessKey);
            //params.put(WebParams.COMM_ID, comm_id);
            params.put(WebParams.BILLER_TYPE, _biller_type_code);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get biller list merchantnya:" + params.toString());

            MyApiClient.sentListBiller(getActivity(),params,new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response get Biller list:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            final String arrayBiller = response.getString(WebParams.BILLER_DATA);
                            insertUpdateData(new JSONArray(arrayBiller),_biller_type_code, withDenom);
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            if(code.equals("0003"))
                                insertUpdateData(null,_biller_type_code,withDenom);
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

    private void insertUpdateData(final JSONArray arrayBiller, final String _biller_type_code, final Boolean withDenom){

        if(realm == null)
            realm = Realm.getDefaultInstance();

        if(!realm.isInTransaction() && isUIFragmentValid()) {
            realm.beginTransaction();
            Biller_Type_Data_Model mBillerTypeData = realm.where(Biller_Type_Data_Model.class)
                    .equalTo(WebParams.BILLER_TYPE_CODE, _biller_type_code)
                    .findFirst();

            RealmList<Biller_Data_Model> ResultBillerData = mBillerTypeData.getBiller_data_models();

            Timber.d("isi mbillerdata isinya " + mBillerTypeData.getBiller_data_models().size());
            List<Biller_Data_Model> temptData = new ArrayList<>();
            if (mBillerTypeData.getBiller_data_models().size() > 0) {
                Biller_Data_Model mObj;
                List<Biller_Data_Model> refObj = realm.copyFromRealm(mBillerTypeData.getBiller_data_models());
                for (int i = 0; i < refObj.size(); i++) {
                    mObj = realm.where(Biller_Data_Model.class).
                            equalTo(WebParams.COMM_ID, refObj.get(i).getComm_id()).
                            equalTo(WebParams.COMM_NAME, refObj.get(i).getComm_name()).
                            findFirst();
                    if (mObj.getItem_id().isEmpty())
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
            int i;
            int j;
            int k;
            try {
                if (arrayBiller != null && arrayBiller.length() > 0) {
                    for ( i = 0; i < arrayBiller.length(); i++) {
                        mObj = realm.createObjectFromJson(Biller_Data_Model.class, arrayBiller.getJSONObject(i));
                        mObj.setLast_update(curr_date);

                        if (temptData.size() > 0) {
                            for (j = 0; j < temptData.size(); j++) {
                                if (mObj.getComm_id().equals(temptData.get(j).getComm_id()) &&
                                        mObj.getComm_name().equals(temptData.get(j).getComm_name())) {
                                    for (k = 0; k < temptData.get(j).getDenom_data_models().size(); k++) {
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
                        if (jsonBankBiller.length() > 0) {
                            for ( j = 0; j < jsonBankBiller.length(); j++) {
                                JSONObject mJob = jsonBankBiller.getJSONObject(j);
                                refObj = new bank_biller_model();
                                refObj.setBank_code(mJob.optString(WebParams.BANK_CODE, ""));
                                refObj.setBank_name(mJob.optString(WebParams.BANK_NAME, ""));
                                refObj.setProduct_code(mJob.optString(WebParams.PRODUCT_CODE, ""));
                                refObj.setProduct_name(mJob.optString(WebParams.PRODUCT_NAME, ""));
                                refObj.setProduct_type(mJob.optString(WebParams.PRODUCT_TYPE, ""));
                                refObj.setProduct_h2h(mJob.optString(WebParams.PRODUCT_H2H, ""));
                                refObj.setLast_update(curr_date);

                                realm.copyToRealmOrUpdate(refObj);
                                mObj.getBank_biller_models().add(refObj);
                            }
                        }

                        mBillerTypeData.getBiller_data_models().add(mObj);
                        Timber.d("isi array biller realm idx : " + arrayBiller.getJSONObject(i).getString(WebParams.COMM_ID));
                    }

                    Boolean isHave = false;
                    Denom_Data_Model delList;
                    for(i = 0 ; i < temptData.size() ; i++){
                        for (j = 0 ; j < ResultBillerData.size() ; j++){
                            if(temptData.get(i).getComm_name().equals(ResultBillerData.get(j).getComm_name()) &&
                                    temptData.get(i).getComm_id().equals(ResultBillerData.get(i).getComm_id())){
                                isHave = true;
                            }
                        }

                        if(!isHave){
                            if(temptData.get(i).getItem_id().isEmpty() && temptData.get(i).getDenom_data_models().size() > 0){
                                for ( j = 0; j < temptData.get(i).getDenom_data_models().size(); j++) {
                                    delList = realm.where(Denom_Data_Model.class).
                                            equalTo(WebParams.DENOM_ITEM_ID, temptData.get(i).getDenom_data_models().get(j).getItem_id()).
                                            findFirst();
                                    delList.removeFromRealm();
                                }
                            }

                        }
                        isHave = false;
                    }


                    if (mObj != null) {
                        if (withDenom) {
                            getDenomRetail(mObj.getComm_id(), mObj.getComm_name());
                        } else {
                            if (arrayBiller.length() == 1) {
                                getDenomRetail(mObj.getComm_id(), mObj.getComm_name());
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            realm.commitTransaction();
        }
        else {
            addToQueeing(new exeBillerData(arrayBiller,_biller_type_code,withDenom));
        }
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
                dob = DateTimeFormat.convertStringtoCustomDate(date);
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

        if(!realm.isInTransaction() && isUIFragmentValid()) {
            if (denom_data.length() > 0) {
                try {
                    realm.beginTransaction();

                    Biller_Data_Model mBillerData = realm.where(Biller_Data_Model.class)
                            .equalTo(WebParams.COMM_ID, _comm_id)
                            .equalTo(WebParams.COMM_NAME, _comm_name)
                            .findFirst();

                    if (mBillerData.getDenom_data_models().size() > 0) {
                        Denom_Data_Model mObj;
                        List<Denom_Data_Model> refObj = mBillerData.getDenom_data_models();
                        for (int i = 0; i < mBillerData.getDenom_data_models().size(); i++) {
                            mObj = realm.where(Denom_Data_Model.class).
                                    equalTo(WebParams.DENOM_ITEM_ID, refObj.get(i).getItem_id()).
                                    findFirst();
                            mObj.removeFromRealm();
                        }
                    }

                    mBillerData.getDenom_data_models().deleteAllFromRealm();

                    String curr_date = DateTimeFormat.getCurrentDate();
                    Denom_Data_Model mObjRealm;

                    for (int i = 0; i < denom_data.length(); i++) {
                        mObjRealm = realm.createObjectFromJson(Denom_Data_Model.class, denom_data.getJSONObject(i));
                        mObjRealm.setLast_update(curr_date);
                        mBillerData.getDenom_data_models().add(mObjRealm);

                    }
                    realm.commitTransaction();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            addToQueeing(new exeDenomData(denom_data,_comm_id,_comm_name));
        }
    }

    public Boolean isUIFragmentValid(){
        if(getActivity() == null)
            return null;

        BillerActivity mObj = (BillerActivity)getActivity();
        return mObj.isFragmentValid();
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed())
            realm.close();
        super.onDestroy();
    }
}