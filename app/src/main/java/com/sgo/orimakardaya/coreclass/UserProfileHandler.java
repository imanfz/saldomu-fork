package com.sgo.orimakardaya.coreclass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/12/2016.
 */
public class UserProfileHandler {
    Context mContext = null;
    SecurePreferences sp;
    String userID, memberID;

    public UserProfileHandler(Context s) {
        mContext = s;
    }

    public void sentUserProfile() {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        memberID = sp.getString(DefineValue.MEMBER_ID, "");

        try{
            RequestParams params = new RequestParams();
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sent user profile:" + params.toString());

            MyApiClient.sentUserProfile(mContext, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {

                        Timber.d("isi request sent user profile:" + response.toString());

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(mContext, code, Toast.LENGTH_LONG).show();
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
                    Timber.w("Error Koneksi sent user profile" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }
}
