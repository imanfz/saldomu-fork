package com.sgo.saldomu.loader;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/12/2016.
 */
public class UserProfileHandler {
    private Context mContext = null;
    private SecurePreferences sp;

    public UserProfileHandler(Context s) {
        mContext = s;
    }

    public void sentUserProfile() {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String userID = sp.getString(DefineValue.USERID_PHONE, "");
        String memberID = sp.getString(DefineValue.MEMBER_ID, "");

        if(! userID.isEmpty() && !memberID.isEmpty()) {
            try {
                String extraSignature = memberID;
                RequestParams params = new RequestParams(extraSignature);
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
                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.PROFILE_DOB, response.getString(WebParams.DOB));
                                mEditor.putString(DefineValue.PROFILE_ADDRESS, response.getString(WebParams.ADDRESS));
                                mEditor.putString(DefineValue.PROFILE_BIO, response.getString(WebParams.BIO));
                                mEditor.putString(DefineValue.PROFILE_COUNTRY, response.getString(WebParams.COUNTRY));
                                mEditor.putString(DefineValue.PROFILE_EMAIL, response.getString(WebParams.EMAIL));
                                mEditor.putString(DefineValue.PROFILE_FULL_NAME, response.getString(WebParams.FULL_NAME));
                                mEditor.putString(DefineValue.PROFILE_SOCIAL_ID, response.getString(WebParams.SOCIAL_ID));
                                mEditor.putString(DefineValue.PROFILE_HOBBY, response.getString(WebParams.HOBBY));
                                mEditor.putString(DefineValue.PROFILE_POB, response.getString(WebParams.POB));
                                mEditor.putString(DefineValue.PROFILE_GENDER, response.getString(WebParams.GENDER));
                                mEditor.putString(DefineValue.PROFILE_ID_TYPE, response.getString(WebParams.ID_TYPE));
                                mEditor.putString(DefineValue.PROFILE_VERIFIED, response.getString(WebParams.VERIFIED));
                                mEditor.putString(DefineValue.PROFILE_BOM, response.getString(WebParams.MOTHER_NAME));
                                mEditor.apply();
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
            } catch (Exception e) {
                Timber.d("httpclient:" + e.getMessage());
            }
        }
    }
}
