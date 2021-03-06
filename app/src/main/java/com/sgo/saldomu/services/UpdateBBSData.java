package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.CommDataModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

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
    final String CTR = "CTR";

    SecurePreferences sp;
    private ResultReceiver localResultReceiver;
    String userID;
    String accessKey;
    String curr_date;
    boolean ctaState = false;
    boolean atcState = false;
    boolean ctrState = false;

    public UpdateBBSData() {
        super("UpdateBBSData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra("receiverTag"))
            localResultReceiver = intent.getParcelableExtra("receiverTag");

        curr_date = DateTimeFormat.getCurrentDate();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        setUpdatingData(true);

        if (!userID.isEmpty() && !accessKey.isEmpty()) {
            if (BBSDataManager.isDataCTANotValid())
                getBBSdata(CTA);
            else
                ctaState = true;

            if (BBSDataManager.isDataATCNotValid())
                getBBSdata(ATC);
            else
                atcState = true;

            if (BBSDataManager.isDataCTRNotValid())
                getBBSdata(CTR);
            else
                ctrState=true;


        } else
            Timber.d("user id atau access key kosong semua");

        if (!ctaState && !atcState && !ctrState) {
            sentFailed(null);
            setIsDataUpdated(false);
        } else {
            sentSuccess(null);
            setIsDataUpdated(true);
            if (getMustUpdateBBSData())
                setMustUpdateBBSData(false);
        }
//        EndRealm();
        setUpdatingData(false);
    }

    void setUpdatingData(Boolean value) {
        sp.edit().putBoolean(DefineValue.IS_UPDATING_BBS_DATA, value).apply();
    }

    void setIsDataUpdated(Boolean value) {
        sp.edit().putBoolean(DefineValue.IS_BBS_DATA_UPDATED, value).apply();
    }

    void setDateDataCTA(String value) {
        sp.edit().putString(DefineValue.UPDATE_TIME_BBS_CTA_DATA, value).apply();
    }

    void setDateDataATC(String value) {
        sp.edit().putString(DefineValue.UPDATE_TIME_BBS_ATC_DATA, value).apply();
    }

    void setDateDataCTR(String value) {
        sp.edit().putString(DefineValue.UPDATE_TIME_BBS_CTR_DATA, value).apply();
    }

    boolean getMustUpdateBBSData() {
        return sp.getBoolean(DefineValue.IS_MUST_UPDATE_BBS_DATA, false);
    }

    void setMustUpdateBBSData(boolean value) {
        sp.edit().putBoolean(DefineValue.IS_MUST_UPDATE_BBS_DATA, value).apply();
    }

//    private Realm getRealm(){
//        if (realm.isClosed() || realm == null)
//            realm = Realm.getInstance(RealmManager.BBSConfiguration);
////            realm = RealmManager.getInstance().getBbsRealm();
//        return realm;
//    }
//
//    private void EndRealm(){
//        if(realm.isInTransaction())
//            realm.cancelTransaction();
//
//        if(realm != null && !realm.isClosed())
//            realm.close();
//    }

    private void getBBSdata(final String schemeCode) {
        try {
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_LIST_COMMUNITY_ALL);

            params.put(WebParams.COMM_ID_REMARK, MyApiClient.COMM_ID);
            params.put(WebParams.SCHEME_CODE, schemeCode);
            params.put(WebParams.CUST_ID, userID);
            params.put(WebParams.USER_ID, userID);

            Timber.d("params list community %1$s : %2$s", schemeCode, params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BBS_LIST_COMMUNITY_ALL, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            try {
//                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (object.get("error_code").getAsString().equals(WebParams.SUCCESS_CODE)) {
                                    Gson gson = new Gson();
                                    CommDataModel model = gson.fromJson(gson.toJson(object), CommDataModel.class);

//                                    String code = model.getError_code();
                                    insertToRealm(new JSONArray(gson.toJson(model.getCommunity())), schemeCode);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient %1$s : %2$s", schemeCode, e.getMessage());
        }
    }

    private void clearDataRealm(String scheme_code, Realm realm) {
        RealmResults<BBSCommModel> jumlahDataComm = realm.where(BBSCommModel.class).
                equalTo(BBSCommModel.SCHEME_CODE, scheme_code).findAll();
        RealmResults<BBSBankModel> jumlahDataBank = realm.where(BBSBankModel.class).
                equalTo(BBSBankModel.SCHEME_CODE, scheme_code).findAll();

        if (jumlahDataComm.size() > 0) {
            jumlahDataComm.deleteAllFromRealm();
        }
        if (jumlahDataBank.size() > 0) {
            jumlahDataBank.deleteAllFromRealm();
        }
        if (scheme_code.equalsIgnoreCase(ATC))
            realm.delete(BBSAccountACTModel.class);
    }

    private void insertToRealm(JSONArray communityData, String scheme_code) {

        Realm realm = Realm.getInstance(RealmManager.BBSConfiguration);

        realm.beginTransaction();

        clearDataRealm(scheme_code, realm);
        if (communityData != null && communityData.length() > 0) {

            BBSCommModel tempBBSCommModel;
            BBSBankModel tempBBSBankModel;
            JSONArray tempBankComm;

            if (scheme_code.equalsIgnoreCase(CTA)) {
                ctaState = true;
                setDateDataCTA(curr_date);
            } else if (scheme_code.equalsIgnoreCase(CTR)) {
                ctrState = true;
                setDateDataCTR(curr_date);
            }else {
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
                if (tempBankComm != null && tempBankComm.length() > 0) {
                    for (int j = 0; j < tempBankComm.length(); j++) {
                        tempBBSBankModel = realm.createObjectFromJson(BBSBankModel.class, tempBankComm.getJSONObject(j));
                        tempBBSBankModel.setComm_type("SOURCE");
                        tempBBSBankModel.setComm_id(tempBBSCommModel.getComm_id());
                        tempBBSBankModel.setScheme_code(scheme_code);
                        tempBBSBankModel.setLast_update(curr_date);
                    }
                }

                //insert to bank model benef
                tempBankComm = communityData.getJSONObject(i).optJSONArray(WebParams.COMM_BENEF);
                if (tempBankComm != null && tempBankComm.length() > 0) {
                    if (scheme_code.equalsIgnoreCase(ATC)) {
                        BBSAccountACTModel bbsAccountACTModel;
                        for (int j = 0; j < tempBankComm.length(); j++) {
                            bbsAccountACTModel = realm.createObjectFromJson(BBSAccountACTModel.class,
                                    tempBankComm.getJSONObject(j));

                            bbsAccountACTModel.setComm_id(tempBBSCommModel.getComm_id());
                            bbsAccountACTModel.setScheme_code(scheme_code);
                            bbsAccountACTModel.setLast_update(curr_date);
                        }
                    } else {
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
            } finally {
                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                SecurePreferences.Editor mEditor = prefs.edit();
                mEditor.putBoolean(DefineValue.IS_SAME_PREVIOUS_USER, true);
                mEditor.apply();
            }
//            }
        }

        if (realm.isInTransaction())
            realm.commitTransaction();

    }

    void sentFailed(Bundle bundle) {
        if (localResultReceiver != null)
            localResultReceiver.send(FAILED, bundle);

        Intent i = new Intent(INTENT_ACTION_BBS_DATA);
        i.putExtra(DefineValue.IS_SUCCESS, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        Timber.d("Sent Failed");
    }

    void sentSuccess(Bundle bundle) {
        if (localResultReceiver != null)
            localResultReceiver.send(SUCCESS, bundle);

        Intent i = new Intent(INTENT_ACTION_BBS_DATA);
        i.putExtra(DefineValue.IS_SUCCESS, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        Timber.d("Sent Success");
    }
}
