package com.sgo.saldomu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.services.AgentShopService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Lenovo on 06/02/2018.
 */

public class LocationProviderChangedReceiver extends BroadcastReceiver {

    private SecurePreferences sp;
    String newShopClosed = "";
    Context newContext = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        newContext = context;

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);
            if (isLogin() && isAgent) {
                Boolean isCallWebService = false;
                String currentShopClosed = sp.getString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_NO);

                if (currentShopClosed == null)
                    currentShopClosed = DefineValue.STRING_NO;

                if (!GlobalSetting.isLocationEnabled(context)) {
                    Timber.d("Location Service Changed Receiver. Location Service is disabled.");

                    if (currentShopClosed.equals(DefineValue.STRING_NO)) {
                        isCallWebService = true;
                        newShopClosed = DefineValue.SHOP_CLOSE;
                    }
                } else {
                    Timber.d("Location Service Changed Receiver. Location Service is enabled.");

                    if (currentShopClosed.equals(DefineValue.STRING_YES)) {
                        isCallWebService = true;
                        newShopClosed = DefineValue.SHOP_OPEN;
                    }
                }

                if (isCallWebService) {

                    String extraSignature = sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "");

                    HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY,
                            extraSignature);

                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
                    params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
                    params.put(WebParams.SHOP_STATUS, newShopClosed);
                    params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_CLOSE_SHOP_TODAY, params,
                            new ObjListeners() {
                                @Override
                                public void onResponses(JSONObject response) {
                                    try {

                                        String code = response.getString(WebParams.ERROR_CODE);
                                        if (code.equals(WebParams.SUCCESS_CODE)) {
                                            SecurePreferences.Editor mEditor = sp.edit();
                                            if (newShopClosed.equals(DefineValue.SHOP_OPEN)) {
                                                mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_NO);
                                            } else {
                                                mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_YES);
                                            }

                                            Timber.d("UpdateShopClosed Location Service Changed Receiver ShopStatus:" + newShopClosed);
                                            mEditor.apply();
                                        } else {

                                        }

                                        Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                                        LocalBroadcastManager.getInstance(newContext).sendBroadcast(i);

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
                }
            }

        }
    }

    private Boolean isLogin() {
        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        return flagLogin.equals(DefineValue.STRING_YES);
    }

}
