package com.sgo.saldomu.loader;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import org.apache.http.Header;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 3/1/16.
 */
class GCMLoader {

    private static final String BASE_URL = "http://116.90.162.173:18080/gcm/registerGCM.php";
    private static final String APP_TYPE = "PIN";
    public GCMLoader(){

    }

    public static void sendRegistrationIdToBackend(String regId) {
        //

        RequestParams params = new RequestParams();
        params.put("email", "yuddistira.kiki@gmail.com");
        params.put("gcm_id", regId);
        params.put("app_type", APP_TYPE);


        Timber.e("param  "+params.toString());
        MyApiClient.getClient().get(BASE_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Timber.d("isi registerGCM server",response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Timber.d("isi failed registerGCM server", responseString);
            }
        });

    }
}
