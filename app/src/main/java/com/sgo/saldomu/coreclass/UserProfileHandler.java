package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.os.Bundle;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.interfaces.OnLoadDataListener;

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

    public void sentUserProfile(final OnLoadDataListener onLoadDataListener, final String _is_new_bulk) {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String userID = sp.getString(DefineValue.USERID_PHONE, "");
        String memberID = sp.getString(DefineValue.MEMBER_ID, "");

        String extraSignature = memberID;
        try{
            RequestParams params = new RequestParams(extraSignature);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            if(_is_new_bulk.equalsIgnoreCase(DefineValue.STRING_YES))
                params.put(WebParams.IS_NEW_BULK, _is_new_bulk);

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
                            mEditor.putString(DefineValue.PROFILE_ADDRESS,response.getString(WebParams.ADDRESS));
                            mEditor.putString(DefineValue.PROFILE_BIO,response.getString(WebParams.BIO));
                            mEditor.putString(DefineValue.PROFILE_COUNTRY,response.getString(WebParams.COUNTRY));
                            mEditor.putString(DefineValue.PROFILE_EMAIL,response.getString(WebParams.EMAIL));
                            mEditor.putString(DefineValue.PROFILE_FULL_NAME,response.getString(WebParams.FULL_NAME));
                            mEditor.putString(DefineValue.PROFILE_SOCIAL_ID,response.getString(WebParams.SOCIAL_ID));
                            mEditor.putString(DefineValue.PROFILE_HOBBY,response.getString(WebParams.HOBBY));
                            mEditor.putString(DefineValue.PROFILE_POB,response.getString(WebParams.POB));
                            mEditor.putString(DefineValue.PROFILE_GENDER,response.getString(WebParams.GENDER));
                            mEditor.putString(DefineValue.PROFILE_ID_TYPE,response.getString(WebParams.ID_TYPE));
                            mEditor.putString(DefineValue.PROFILE_VERIFIED,response.getString(WebParams.VERIFIED));
                            mEditor.putString(DefineValue.PROFILE_BOM,response.getString(WebParams.MOTHER_NAME));
//                            mEditor.apply();

                            if(mEditor.commit()) {
                                if (onLoadDataListener != null)
                                    onLoadDataListener.onSuccess(response);
                            }
                        } else {
                            if (onLoadDataListener != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString(DefineValue.ERROR_CODE,response.optString(WebParams.ERROR_CODE,""));
                                bundle.putString(DefineValue.ERROR,response.optString(WebParams.ERROR_MESSAGE,""));
                                onLoadDataListener.onFail(bundle);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (onLoadDataListener != null)
                            onLoadDataListener.onFailure(e.getMessage());
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
                    if (onLoadDataListener != null) {
                        onLoadDataListener.onFailure(throwable.toString());
                    }
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
            if (onLoadDataListener != null)
                onLoadDataListener.onFailure(e.getMessage());
        }
    }
}
