package com.sgo.hpku.coreclass;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 3/24/16.
 */
class CustomResponseHandler extends JsonHttpResponseHandler {


    public CustomResponseHandler(){
        super();
    }


    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        super.onFailure(statusCode, headers, responseString, throwable);
        Timber.d("onFailure Throwable");
        try {
            if(responseString != null)
                onFailure2(statusCode, headers, responseString, throwable);
            else
                onFailure2(statusCode, headers, throwable.toString(), throwable);
        }
        catch (Exception ex){
            Timber.d("onFailure JsonObject"+ex.toString());
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);
        Timber.d("onFailure JsonObject");
        try {
            if(errorResponse != null) {
                onFailure2(statusCode, headers, errorResponse.toString(), throwable);
            }
            else
                onFailure2(statusCode, headers, throwable.toString(), throwable);
        }
        catch (Exception ex){
            Timber.d("onFailure JsonObject"+ex.toString());
        }

    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);
        Timber.d("onFailure JsonArray");
        try {
            if(errorResponse != null)
                onFailure2(statusCode, headers, errorResponse.toString(), throwable);
            else
                onFailure2(statusCode, headers, throwable.toString(), throwable);
        }
        catch (Exception ex){
            Timber.d("onFailure JsonObject"+ex.toString());
        }
    }

    private void onFailure2(int statusCode, Header[] headers, String response, Throwable throwable) {
        Timber.d("statusCode : "+ String.valueOf(statusCode) + "response : "+ response);
    }
}
