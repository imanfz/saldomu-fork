package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;
import timber.log.Timber;

public class UpdateBBSBirthPlace extends IntentService {

    private Realm realm;

    public UpdateBBSBirthPlace() {
        super("UpdateBBSBirthPlace");
    }

    public static void startUpdateBBSBirthPlace(Context context) {
        Intent intent = new Intent(context, UpdateBBSBirthPlace.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        realm = Realm.getInstance(RealmManager.BBSConfiguration);

        long count = realm.where(List_BBS_Birth_Place.class).count();
        if(count < 2)
            getListBBSBirthPlace();
        else
            Timber.d("table bbs birth place masih terisi");

    }

    private void EndRealm(){
        if(realm.isInTransaction())
            realm.cancelTransaction();

        if(realm != null && !realm.isClosed())
            realm.close();
    }

    private void getListBBSBirthPlace(){
        try{

            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_BBS_BIRTH_PLACE,
                    "");
            Timber.d("params bbs birth place %s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_BIRTH_PLACE, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("Isi response get BBS birth place: %s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    insertToRealm(response.optJSONArray(WebParams.BBS_CITY));
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
        }catch (Exception e){
            Timber.tag("httpclient:").d(e.getMessage());
        }
    }

    private void insertToRealm(JSONArray bbs_city) {

        realm = Realm.getInstance(RealmManager.BBSConfiguration);
        if(bbs_city != null && bbs_city.length() > 0) {
            realm.beginTransaction();
            realm.delete(List_BBS_Birth_Place.class);

            try{
                List_BBS_Birth_Place list_BBS_Birth_Place;

                for(int i = 0 ; i < bbs_city.length() ; i++) {
                    try {
                        list_BBS_Birth_Place= realm.createObjectFromJson(List_BBS_Birth_Place.class, bbs_city.getJSONObject(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Timber.d("REALM Json Error:%s", e.toString());
                        realm.cancelTransaction();
                    }
                }

            }catch(Exception e){
                Timber.d("REALM error:%s", e.toString());
            }finally {
                realm.commitTransaction();
                EndRealm();
            }

        }
    }
}