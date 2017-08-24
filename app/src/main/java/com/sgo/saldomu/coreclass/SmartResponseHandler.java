package com.sgo.saldomu.coreclass;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by yuddistirakiki on 8/21/17.
 */

public abstract class SmartResponseHandler extends JsonHttpResponseHandler {


    public abstract void onSuccessResponse(int statusCode, Header[] headers, JSONObject response);
    public abstract void onFailedResponse(Throwable throwable);

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);
        onSuccessResponse(statusCode,headers,response);
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
       onFailedResponse(throwable);
    }
}
