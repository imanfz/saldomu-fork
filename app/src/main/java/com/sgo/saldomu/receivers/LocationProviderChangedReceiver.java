package com.sgo.saldomu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.services.AgentShopService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static com.activeandroid.Cache.getContext;

/**
 * Created by Lenovo on 06/02/2018.
 */

public class LocationProviderChangedReceiver extends BroadcastReceiver {
    private final static String TAG = "LocationProviderChanged";
    public static final String INTENT_ACTION_LOCATION_DATA = "com.sgo.saldomu.INTENT_ACTION_LOCATION_DATA";

    private SecurePreferences sp;
    String newShopClosed        = "";
    Context newContext          = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        newContext  = context;

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED") )
        {

            Boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);
            if ( isLogin() && isAgent ) {
                Boolean isCallWebService    = false;
                String currentShopClosed    = sp.getString(DefineValue.AGENT_SHOP_CLOSED, DefineValue.STRING_NO);

                if(currentShopClosed == null)
                    currentShopClosed = DefineValue.STRING_NO;

                if ( !GlobalSetting.isLocationEnabled(context) ) {
                    Timber.d("Location Service Changed Receiver. Location Service is disabled.");

                    if ( currentShopClosed.equals(DefineValue.STRING_NO) ) {
                        isCallWebService    = true;
                        newShopClosed       = DefineValue.SHOP_CLOSE;
                    }
                } else {
                    Timber.d("Location Service Changed Receiver. Location Service is enabled.");

                    if ( currentShopClosed.equals(DefineValue.STRING_YES) ) {
                        isCallWebService    = true;
                        newShopClosed       = DefineValue.SHOP_OPEN;
                    }
                }

                if ( isCallWebService ) {
                    RequestParams params    = new RequestParams();
                    String shopStatus       = DefineValue.SHOP_OPEN;

                    UUID rcUUID = UUID.randomUUID();
                    String dtime = DateTimeFormat.getCurrentDateTime();

                    params.put(WebParams.RC_UUID, rcUUID);
                    params.put(WebParams.RC_DATETIME, dtime);
                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.SHOP_ID, sp.getString(DefineValue.BBS_SHOP_ID, ""));
                    params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.BBS_MEMBER_ID, ""));
                    params.put(WebParams.SHOP_STATUS, newShopClosed);


                    String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + sp.getString(DefineValue.BBS_MEMBER_ID, "") + sp.getString(DefineValue.BBS_SHOP_ID, "") + BuildConfig.APP_ID + newShopClosed));

                    params.put(WebParams.SIGNATURE, signature);

                    MyApiClient.updateCloseShopToday(getContext(), params, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {

                            try {

                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    SecurePreferences.Editor mEditor = sp.edit();
                                    if ( newShopClosed.equals(DefineValue.SHOP_OPEN) ) {
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
                        public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                            ifFailure(throwable);
                        }

                        @Override
                        public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            ifFailure(throwable);
                        }

                        private void ifFailure(Throwable throwable) {
                            Timber.w("Error Koneksi UpdateShopClosed Location Changed Receiver:" + throwable.toString());
                        }
                    });
                }
            }

        }
    }

    private Boolean isLogin(){
        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        return flagLogin.equals(DefineValue.STRING_YES);
    }

}
