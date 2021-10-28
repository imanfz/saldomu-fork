package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.List_BBS_City;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by thinkpad on 1/26/2017.
 */

public class UpdateBBSCity extends IntentService {

    private Realm realm;

    public UpdateBBSCity() {
        super("UpdateBBSCity");
    }

    public static void startUpdateBBSCity(Context context) {
        Intent intent = new Intent(context, UpdateBBSCity.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        realm = Realm.getDefaultInstance();

        long count = realm.where(List_BBS_City.class).count();
        if(count < 2)
            getListBBSCity();
        else
            Timber.d("table bbs city masih terisi");

    }

    private void EndRealm(Realm realm){
//        if(realm.isInTransaction())
//            realm.cancelTransaction();

        if(realm != null && !realm.isClosed())
            realm.close();
    }

    private void getListBBSCity(){
        try{
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_BBS_CITY,
                    "");

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_CITY, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("Isi response get BBS city: %s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    insertToRealm(response.optJSONArray(WebParams.BBS_CITY));
                                }else {
                                    code = response.getString(WebParams.ERROR_MESSAGE);
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

        Realm realm = Realm.getDefaultInstance();

        if(bbs_city != null && bbs_city.length() > 0) {

            realm.beginTransaction();

            realm.delete(List_BBS_City.class);

            List_BBS_City list_bbs_city;

            for(int i = 0 ; i < bbs_city.length() ; i++) {
                try {
                    list_bbs_city = realm.createObjectFromJson(List_BBS_City.class, bbs_city.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                    realm.cancelTransaction();
                }
            }

            if(realm.isInTransaction())
                realm.commitTransaction();

            EndRealm(realm);
        }

//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//
//            }
//        });
    }
}
