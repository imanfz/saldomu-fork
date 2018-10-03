package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.models.retrofit.UpdateProfileModel;

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

    public void sentUserProfile(final OnLoadDataListener onLoadDataListener, final String _is_new_bulk) {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        String userID = sp.getString(DefineValue.USERID_PHONE, "");
        String memberID = sp.getString(DefineValue.MEMBER_ID, "");

        String extraSignature = memberID;
        try{
//            RequestParams param = new RequestParams(extraSignature);
//            HashMap<String, Object> params = RetrofitService.getInstance()
//                    .getSignature(MyApiClient.LINK_FORGOT_PASSWORD, extraSignature);
            HashMap<String, Object> params = new HashMap<>();
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            if(_is_new_bulk.equalsIgnoreCase(DefineValue.STRING_YES))
                params.put(WebParams.IS_NEW_BULK, _is_new_bulk);

            Timber.d("isi params sent user profile:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_USER_PROFILE, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();

                            UpdateProfileModel model = gson.fromJson(object, UpdateProfileModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.PROFILE_DOB, model.getDate_of_birth());
                                mEditor.putString(DefineValue.PROFILE_ADDRESS, model.getAddress());
                                mEditor.putString(DefineValue.PROFILE_BIO, model.getBio());
                                mEditor.putString(DefineValue.PROFILE_COUNTRY, model.getCountry());
                                mEditor.putString(DefineValue.PROFILE_EMAIL, model.getEmail());
                                mEditor.putString(DefineValue.PROFILE_FULL_NAME, model.getFull_name());
                                mEditor.putString(DefineValue.PROFILE_SOCIAL_ID, model.getSocial_id());
                                mEditor.putString(DefineValue.PROFILE_HOBBY, model.getHobby());
                                mEditor.putString(DefineValue.PROFILE_POB, model.getBirth_place());
                                mEditor.putString(DefineValue.PROFILE_GENDER, model.getGender());
                                mEditor.putString(DefineValue.PROFILE_ID_TYPE, model.getIdtype());
                                mEditor.putString(DefineValue.PROFILE_VERIFIED, model.getVerified());
                                mEditor.putString(DefineValue.PROFILE_BOM, model.getMother_name());
//                            mEditor.apply();

                                if(mEditor.commit()) {
                                    if (onLoadDataListener != null)
                                        onLoadDataListener.onSuccess(model);
                                }
                            } else {
                                if (onLoadDataListener != null) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(DefineValue.ERROR_CODE,model.getError_code());
                                    bundle.putString(DefineValue.ERROR, model.getError_message());
                                    onLoadDataListener.onFail(bundle);
                                }
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
