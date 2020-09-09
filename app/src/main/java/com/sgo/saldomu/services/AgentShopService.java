package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AgentShopService extends IntentService {
    public static final String INTENT_ACTION_AGENT_SHOP = "com.sgo.saldomu.INTENT_ACTION_AGENT_SHOP";
    SecurePreferences sp;

    public AgentShopService() {
        super("AgentShopService");
    }

    public static void getAgentShop(Context context) {
        Intent intent = new Intent(context, AgentShopService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getAgentShop();
    }

    private void getAgentShop() {
        sp                              = CustomSecurePref.getInstance().getmSecurePrefs();
        try{

            String flagApprove             = DefineValue.STRING_NO;

            String extraSignature = flagApprove;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_SHOP_LIST, extraSignature);

            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
            params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
            params.put(WebParams.FLAG_APPROVE, flagApprove);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_MEMBER_SHOP_LIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("Isi response get Agent Shop: "+response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    JSONArray members = response.getJSONArray("member");

                                    for (int i = 0; i < members.length(); i++) {
                                        JSONObject object = members.getJSONObject(i);

                                        SecurePreferences.Editor mEditor = sp.edit();
                                        mEditor.putString(DefineValue.IS_AGENT_APPROVE, DefineValue.STRING_YES);
                                        mEditor.putString(DefineValue.AGENT_NAME, object.getString("shop_name"));
                                        mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, object.getString("shop_closed"));
                                        mEditor.putString(DefineValue.BBS_MEMBER_ID, object.getString("member_id"));
                                        mEditor.putString(DefineValue.BBS_SHOP_ID, object.getString("shop_id"));
                                        mEditor.apply();
                                        break;
                                    }

                                } else {
                                    SecurePreferences.Editor mEditor = sp.edit();
                                    mEditor.putString(DefineValue.IS_AGENT_APPROVE, DefineValue.STRING_NO);
                                    mEditor.apply();
                                }

                                Intent i = new Intent(AgentShopService.INTENT_ACTION_AGENT_SHOP);
                                LocalBroadcastManager.getInstance(AgentShopService.this).sendBroadcast(i);

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
        }catch (Exception e){
            Log.d("httpclient:",e.getMessage());
        }
    }

}
