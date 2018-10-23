package com.sgo.saldomu.loader;

import android.content.Context;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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
//                String extraSignature = memberID;
//                RequestParams params = new RequestParams(extraSignature);
                HashMap<String, Object> params = new HashMap<>();
                params.put(WebParams.USER_ID, userID);
                params.put(WebParams.MEMBER_ID, memberID);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

                Timber.d("isi params sent user profile:" + params.toString());

                RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_USER_PROFILE, params,
                        new ObjListeners() {
                            @Override
                            public void onResponses(JSONObject response) {
                                try {
                                    Timber.d("isi request sent user profile:" + response.toString());

                                    String code = response.getString(WebParams.ERROR_CODE);
                                    if (code.equals(WebParams.SUCCESS_CODE)) {
                                        SecurePreferences.Editor mEditor = sp.edit();
                                        mEditor.putString(DefineValue.PROFILE_DOB, response.optString(WebParams.DOB, ""));
                                        mEditor.putString(DefineValue.PROFILE_ADDRESS, response.optString(WebParams.ADDRESS, ""));
                                        mEditor.putString(DefineValue.PROFILE_BIO, response.optString(WebParams.BIO, ""));
                                        mEditor.putString(DefineValue.PROFILE_COUNTRY, response.optString(WebParams.COUNTRY, ""));
                                        mEditor.putString(DefineValue.PROFILE_EMAIL, response.optString(WebParams.EMAIL, ""));
                                        mEditor.putString(DefineValue.PROFILE_FULL_NAME, response.optString(WebParams.FULL_NAME, ""));
                                        mEditor.putString(DefineValue.PROFILE_SOCIAL_ID, response.optString(WebParams.SOCIAL_ID, ""));
                                        mEditor.putString(DefineValue.PROFILE_HOBBY, response.optString(WebParams.HOBBY, ""));
                                        mEditor.putString(DefineValue.PROFILE_POB, response.optString(WebParams.POB, ""));
                                        mEditor.putString(DefineValue.PROFILE_GENDER, response.optString(WebParams.GENDER, ""));
                                        mEditor.putString(DefineValue.PROFILE_ID_TYPE, response.optString(WebParams.ID_TYPE, ""));
                                        mEditor.putString(DefineValue.PROFILE_VERIFIED, response.optString(WebParams.VERIFIED, ""));
                                        mEditor.putString(DefineValue.PROFILE_BOM, response.optString(WebParams.MOTHER_NAME, ""));
                                        mEditor.apply();
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
            } catch (Exception e) {
                Timber.d("httpclient:" + e.getMessage());
            }
        }
    }
}
