package com.sgo.saldomu.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

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

            RequestParams params            = new RequestParams();
            UUID rcUUID                     = UUID.randomUUID();
            String  dtime                   = DateTimeFormat.getCurrentDateTime();
            params.put(WebParams.RC_UUID, rcUUID);
            params.put(WebParams.RC_DATETIME, dtime);
            params.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID );
            params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID );
            params.put(WebParams.CUSTOMER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
            params.put(WebParams.FLAG_APPROVE, flagApprove);

            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime +
                    DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID +
                    sp.getString(DefineValue.USERID_PHONE, "") + BuildConfig.APP_ID + flagApprove));

            params.put(WebParams.SIGNATURE, signature);

            MyApiClient.getMemberShopList(this,params, true, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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
                    Timber.w("Error Koneksi get Agent Shop:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Log.d("httpclient:",e.getMessage());
        }
    }

}
