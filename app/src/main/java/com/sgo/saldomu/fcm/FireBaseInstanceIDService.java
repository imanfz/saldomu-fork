package com.sgo.saldomu.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DeviceUtils;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.SmartResponseHandler;
import com.sgo.saldomu.coreclass.WebParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/15/17.
 */

public class FireBaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        Timber.d("Refreshed token: ");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.d("Refreshed token: " + refreshedToken);
        sentTokenToServer(refreshedToken);
    }

    void sentTokenToServer(String token){
        String deviceID = DeviceUtils.getAndroidID();
        RequestParams requestParams = MyApiClient.getSignatureWithParamsFCM(token,
                deviceID, BuildConfig.AppID);
        requestParams.put(WebParams.GCM_ID, token);
        requestParams.put(WebParams.DEVICE_NAME, DeviceUtils.getDeviceName());
        requestParams.put(WebParams.DEVICE_OS, DeviceUtils.getDeviceOS());
        requestParams.put(WebParams.DEVICE_API, DeviceUtils.getDeviceAPILevel());
        requestParams.put(WebParams.DEVICE_MEMORY, DeviceUtils.getDeviceMemory());
        requestParams.put(WebParams.DEVICE_ID, DeviceUtils.getAndroidID());
        requestParams.put(WebParams.TIMEZONE, DeviceUtils.getDeviceTimeZone());
        requestParams.put(WebParams.DEV_MODEL, DeviceUtils.getDeviceModel());
        requestParams.put(WebParams.APP_TYPE, getString(R.string.appname));
        requestParams.put(WebParams.APP_ID, BuildConfig.AppID);
        Timber.d("isi params reg token fcm to server : "+requestParams );
        MyApiClient.sentReqTokenFCM(this,true,requestParams, new SmartResponseHandler() {
            @Override
            public void onSuccessResponse(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Timber.d("isi response reg token fcm to server : "+response.toString() );
                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        JSONObject jsonObject = response.getJSONObject(WebParams.DATA);
                        if(jsonObject != null) {
                            CustomSecurePref.getInstance().insertString(DefineValue.FCM_SERVER_UUID,
                                    jsonObject.optString(WebParams.UID,""));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedResponse(Throwable throwable) {
                Timber.d("isi failed reg token fcm to server : "+throwable.toString());
            }
        });

    }
}
