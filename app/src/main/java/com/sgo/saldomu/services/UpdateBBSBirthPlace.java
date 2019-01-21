package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;
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
            RequestParams params = MyApiClient.getSignatureWithParamsWithoutLogin(MyApiClient.COMM_ID, MyApiClient.LINK_BBS_BIRTH_PLACE,
                    BuildConfig.SECRET_KEY);
            Timber.d("params bbs birth place " +params.toString());

            MyApiClient.getBBSBirthPlace(this,true, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response get BBS birth place: "+response.toString());
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
                    Timber.w("Error Koneksi get BBS birth place:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Log.d("httpclient:",e.getMessage());
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
                        Timber.d("REALM Json Error:"+e.toString());
                        realm.cancelTransaction();
                    }
                }

            }catch(Exception e){
                Timber.d("REALM error:"+e.toString());
            }finally {
                realm.commitTransaction();
                EndRealm();
            }

        }


        RealmResults<List_BBS_Birth_Place> results = realm.where(List_BBS_Birth_Place.class).findAll();
        Timber.d("REALM isi realm results:"+results.toString());

//        if(realm.isInTransaction())
//            realm.commitTransaction();


    }
}