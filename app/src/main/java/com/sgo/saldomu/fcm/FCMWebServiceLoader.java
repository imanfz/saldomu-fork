package com.sgo.saldomu.fcm;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
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
 * Created by yuddistirakiki on 8/28/17.
 */

public class FCMWebServiceLoader {

    private Context mContext;
    private LoaderListener loaderListener;
    private SecurePreferences sp;

    public interface LoaderListener{
        void onSuccessLoader();
        void onFailedLoader();
    }

    public FCMWebServiceLoader(Context _context, LoaderListener listener){
        this.mContext = _context;
        this.loaderListener = listener;
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    public static FCMWebServiceLoader getInstance(Context _context){
        return new FCMWebServiceLoader(_context,null);
    }

    public static FCMWebServiceLoader getInstance(Context _context, LoaderListener listener){
        return new FCMWebServiceLoader(_context,listener);
    }

    private RequestParams setupSignatureParams(){
        String deviceID = DeviceUtils.getAndroidID();
        String token = FCMManager.getTokenFCM();
        RequestParams requestParams = MyApiClient.getSignatureWithParamsFCM(token,
                deviceID, BuildConfig.AppID);
        requestParams.put(WebParams.DEVICE_ID, DeviceUtils.getAndroidID());
        requestParams.put(WebParams.GCM_ID, token);
        requestParams.put(WebParams.APP_ID, BuildConfig.AppID);
        requestParams.put(WebParams.DEVICE_NAME, DeviceUtils.getDeviceName());
        requestParams.put(WebParams.DEVICE_OS, DeviceUtils.getDeviceOS());
        requestParams.put(WebParams.DEVICE_API, DeviceUtils.getDeviceAPILevel());
        requestParams.put(WebParams.DEVICE_MEMORY, DeviceUtils.getDeviceMemory());
        requestParams.put(WebParams.TIMEZONE, DeviceUtils.getDeviceTimeZone());
        requestParams.put(WebParams.DEV_MODEL, DeviceUtils.getDeviceModel());
        requestParams.put(WebParams.APP_TYPE, mContext.getString(R.string.appname));
        return requestParams;
    }

    //Register Ulang fcm hanya isi userId dan email
    public void sentTokenAtLogin(Boolean isSync, String userID, String email){
        RequestParams requestParams = setupSignatureParams();
        requestParams.put(WebParams.USER_ID,userID);
        requestParams.put(WebParams.EMAIL,email);
        sentTokenToServer(isSync,requestParams);
    }

    //Register fcm permulaan buka aplikasi
    public void sentTokenToServer(Boolean isSync){
        RequestParams requestParams = setupSignatureParams();
        sentTokenToServer(isSync,requestParams);
    }

    private void sentTokenToServer(Boolean isSync, RequestParams requestParams){

        Timber.d("isi params reg token fcm to server : "+requestParams );
        MyApiClient.sentReqTokenFCM(mContext,isSync,requestParams, new SmartResponseHandler() {
            @Override
            public void onSuccessResponse(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Timber.d("isi response reg token fcm to server : "+response.toString() );
                    String code = response.getString(WebParams.ERROR_CODE);
                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        CustomSecurePref.getInstance().insertString(DefineValue.FCM_SERVER_UUID,
                                response.optString(WebParams.UID,""));
                        if(loaderListener != null)
                            loaderListener.onSuccessLoader();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(loaderListener != null)
                        loaderListener.onFailedLoader();
                }
            }

            @Override
            public void onFailedResponse(Throwable throwable) {
                Timber.d("isi failed reg token fcm to server : "+throwable.toString());
                if(loaderListener != null)
                    loaderListener.onSuccessLoader();
            }
        });

    }
}
