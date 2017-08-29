package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.receivers.LocalResultReceiver;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by thinkpad on 1/26/2017.
 */
public class UpdateBBSData extends IntentService {
    public static final String INTENT_ACTION_BBS_DATA = "com.sgo.saldomu.INTENT_ACTION_BBS_DATA";

    final public static int SUCCESS = 11;
    final public static int FAILED = 10;
    final String CTA = "CTA";
    final String ATC = "ATC";


    private Realm realm;
    SecurePreferences sp;
    private ResultReceiver localResultReceiver;
    String userID;
    String accessKey;
    String curr_date;
    boolean ctaState = false;
    boolean atcState = false;

    public UpdateBBSData() {
        super("UpdateBBSData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.hasExtra("receiverTag"))
            localResultReceiver = intent.getParcelableExtra("receiverTag");

        realm = Realm.getInstance(RealmManager.BBSConfiguration);
        curr_date = DateTimeFormat.getCurrentDate();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        setUpdatingData(true);

        if(!userID.isEmpty() && !accessKey.isEmpty()) {
            getBBSdata(CTA);
            getBBSdata(ATC);
        }
        else
            Timber.d("user id atau access key kosong semua");

        if(!ctaState && !atcState){
            sentFailed(null);
            setIsDataUpdated(false);
        }
        else {
            sentSuccess(null);
            setIsDataUpdated(true);
        }
        EndRealm();
        setUpdatingData(false);
    }

    void setUpdatingData(Boolean value){
        sp.edit().putBoolean(DefineValue.IS_UPDATING_BBS_DATA,value).commit();
    }

    void setIsDataUpdated(Boolean value){
        sp.edit().putBoolean(DefineValue.IS_BBS_DATA_UPDATED,value).commit();
    }

    void setDateDataCTA(String value){
        sp.edit().putString(DefineValue.UPDATE_TIME_BBS_CTA_DATA,value).commit();
    }
    void setDateDataATC(String value){
        sp.edit().putString(DefineValue.UPDATE_TIME_BBS_ATC_DATA,value).commit();
    }

    private void EndRealm(){
        if(realm.isInTransaction())
            realm.cancelTransaction();

        if(realm != null && !realm.isClosed())
            realm.close();
    }

    private void getBBSdata(final String schemeCode){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,
                    MyApiClient.LINK_BBS_LIST_COMMUNITY_ALL, userID, accessKey);

            params.put(WebParams.COMM_ID_REMARK, MyApiClient.COMM_ID);
            params.put(WebParams.SCHEME_CODE,schemeCode);
            params.put(WebParams.CUST_ID,userID);

            Timber.d("params list community %1$s : %2$s",schemeCode,params.toString());

            MyApiClient.sentBBSListCommunityAllSync(this,params,new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response listcommunity %1$s : %2$s",schemeCode,response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            insertToRealm(response.optJSONArray(WebParams.COMMUNITY),schemeCode);
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

                private void failure(Throwable throwable) {
                    Timber.e("Error Koneksi get BBS city %1$s : %2$s",schemeCode,throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient %1$s : %2$s",schemeCode,e.getMessage());
        }
    }

    private void clearDataRealm(String scheme_code){
        RealmResults<BBSCommModel> jumlahDataComm = realm.where(BBSCommModel.class).
                equalTo(BBSCommModel.SCHEME_CODE,scheme_code).findAll();
        RealmResults<BBSBankModel> jumlahDataBank = realm.where(BBSBankModel.class).
                equalTo(BBSBankModel.SCHEME_CODE,scheme_code).findAll();


        realm.beginTransaction();
        if(jumlahDataComm.size() > 0) {
            jumlahDataComm.deleteAllFromRealm();
        }
        if(jumlahDataBank.size() > 0) {
            jumlahDataBank.deleteAllFromRealm();
        }
        if(scheme_code.equalsIgnoreCase(ATC))
            realm.delete(BBSAccountACTModel.class);
        realm.commitTransaction();
    }

    private void insertToRealm(JSONArray communityData, String scheme_code) {

        clearDataRealm(scheme_code);
        if(communityData != null && communityData.length() > 0) {

            realm.beginTransaction();

            BBSCommModel tempBBSCommModel;
            BBSBankModel tempBBSBankModel;
            JSONArray tempBankComm;

            if(scheme_code.equalsIgnoreCase(CTA)) {
                ctaState = true;
                setDateDataCTA(curr_date);
            } else {
                atcState = true;
                setDateDataATC(curr_date);
            }

            int i = 0;
//            for(int i = 0 ; i < communityData.length() ; i++) {
                try {
                    //insert to comm model
                    tempBBSCommModel = realm.createObjectFromJson(BBSCommModel.class, communityData.getJSONObject(i));
                    tempBBSCommModel.setScheme_code(scheme_code);
                    tempBBSCommModel.setLast_update(curr_date);

                    //insert to bank model source
                    tempBankComm = communityData.getJSONObject(i).optJSONArray(WebParams.COMM_SOURCE);
                    if(tempBankComm != null && tempBankComm.length() > 0){
                        for(int j = 0; j < tempBankComm.length() ; j++) {
                            tempBBSBankModel = realm.createObjectFromJson(BBSBankModel.class, tempBankComm.getJSONObject(j));
                            tempBBSBankModel.setComm_type("SOURCE");
                            tempBBSBankModel.setComm_id(tempBBSCommModel.getComm_id());
                            tempBBSBankModel.setScheme_code(scheme_code);
                            tempBBSBankModel.setLast_update(curr_date);
                        }
                    }

                    //insert to bank model benef
                    tempBankComm = communityData.getJSONObject(i).optJSONArray(WebParams.COMM_BENEF);
                    if(tempBankComm != null && tempBankComm.length() > 0){
                        if (scheme_code.equalsIgnoreCase(ATC)){
                            BBSAccountACTModel bbsAccountACTModel;
                            for(int j = 0; j < tempBankComm.length() ; j++) {
                                bbsAccountACTModel = realm.createObjectFromJson(BBSAccountACTModel.class,
                                        tempBankComm.getJSONObject(j));

                                bbsAccountACTModel.setComm_id(tempBBSCommModel.getComm_id());
                                bbsAccountACTModel.setScheme_code(scheme_code);
                                bbsAccountACTModel.setLast_update(curr_date);
                            }
                        }
                        else {
                            for (int j = 0; j < tempBankComm.length(); j++) {
                                tempBBSBankModel = realm.createObjectFromJson(BBSBankModel.class, tempBankComm.getJSONObject(j));

                                tempBBSBankModel.setComm_type("BENEF");
                                tempBBSBankModel.setComm_id(tempBBSCommModel.getComm_id());
                                tempBBSBankModel.setScheme_code(scheme_code);
                                tempBBSBankModel.setLast_update(curr_date);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    realm.cancelTransaction();
                }
//            }
        }

        if(realm.isInTransaction())
            realm.commitTransaction();
    }

    void sentFailed(Bundle bundle){
        if(localResultReceiver != null)
            localResultReceiver.send(FAILED,bundle);

        Intent i = new Intent(INTENT_ACTION_BBS_DATA);
        i.putExtra(DefineValue.IS_SUCCESS,false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        Timber.d("Sent Failed");
    }
    void sentSuccess(Bundle bundle){
        if(localResultReceiver != null)
            localResultReceiver.send(SUCCESS,bundle);

        Intent i = new Intent(INTENT_ACTION_BBS_DATA);
        i.putExtra(DefineValue.IS_SUCCESS,true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        Timber.d("Sent Success");
    }
}
