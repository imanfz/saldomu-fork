package com.sgo.saldomu.fcm;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DeviceUtils;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.FcmModel;

import java.util.HashMap;

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

    private HashMap<String, Object> setupSignatureParams(){
        String deviceID = DeviceUtils.getAndroidID();
        String token = FCMManager.getTokenFCM();
        HashMap<String, Object> requestParams = RetrofitService.getInstance().getSignatureWithParamsFCM(token,
                deviceID, BuildConfig.APP_ID);
        requestParams.put(WebParams.DEVICE_ID, DeviceUtils.getAndroidID());
        requestParams.put(WebParams.GCM_ID, token);
        requestParams.put(WebParams.APP_ID, BuildConfig.APP_ID);
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
        HashMap<String, Object> requestParams = setupSignatureParams();
        requestParams.put(WebParams.USER_ID,userID);
        requestParams.put(WebParams.EMAIL,email);
        sentTokenToServer(isSync,requestParams);
    }

    //Register fcm permulaan buka aplikasi
    public void sentTokenToServer(Boolean isSync){
        HashMap<String, Object> requestParams = setupSignatureParams();
        sentTokenToServer(isSync,requestParams);
    }

    private void sentTokenToServer(Boolean isSync, HashMap<String, Object> requestParams){

        Timber.d("isi params reg token fcm to server : "+requestParams );

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REG_TOKEN_FCM, requestParams,
                new ResponseListener() {
                    @Override
                    public void onResponses(JsonObject object) {
                        try {
                            Gson gson = new Gson();
                            FcmModel model = gson.fromJson(object, FcmModel.class);
                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                CustomSecurePref.getInstance().insertString(DefineValue.FCM_SERVER_UUID,
                                        model.getUid());
                                if(loaderListener != null)
                                    loaderListener.onSuccessLoader();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if(loaderListener != null)
                                loaderListener.onFailedLoader();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if(loaderListener != null)
                            loaderListener.onSuccessLoader();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
