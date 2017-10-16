package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.List_BBS_City;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private void EndRealm(){
        if(realm.isInTransaction())
            realm.cancelTransaction();

        if(realm != null && !realm.isClosed())
            realm.close();
    }

    private void getListBBSCity(){
        try{

            MyApiClient.getBBSCity(this,true,new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response get BBS city: "+response.toString());
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
                    Timber.w("Error Koneksi get BBS city:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Log.d("httpclient:",e.getMessage());
        }
    }

    private void insertToRealm(JSONArray bbs_city) {
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
        }

        if(realm.isInTransaction())
            realm.commitTransaction();

        EndRealm();
    }
}
