package com.sgo.saldomu.fcm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileNewActivity;
import com.sgo.saldomu.activities.SourceOfFundActivity;
import com.sgo.saldomu.activities.UpgradeAgentActivity;
import com.sgo.saldomu.coreclass.BundleToJSON;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/21/17.
 */

public class FCMManager {
    public final static int SYNC_BBS_DATA = 70;
    public final static int OPEN_PLAYSTORE = 80;

    public final static int AGENT_LOCATION_SET_SHOP_LOCATION        = 1000;
    public final static int AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT  = 1001;
    public final static int AGENT_LOCATION_SHOP_REJECT_TRANSACTION  = 1002;
    public final static int AGENT_LOCATION_KEY_ACCEPT_TRANSACTION   = 1003;
    public final static int AGENT_LOCATION_KEY_REJECT_TRANSACTION   = 1004;
    public final static int MEMBER_CONFIRM_CASHOUT_TRANSACTION      = 1005;
    public final static int SHOP_ACCEPT_TRX                         = 1006;
    public final static int SHOP_NOTIF_TRANSACTION                  = 1007;
    public final static int MEMBER_RATING_TRX                       = 1008;
    public final static int REJECT_UPGRADE_MEMBER                   = 2;
    public final static int REJECT_UPGRADE_AGENT                    = 202;
    public final static int BLAST_INFO                              = 1009;
    public final static int SOURCE_OF_FUND                          = 1010;
    public final static int VERIFY_ACC                              = 1011;

    final private static String AGENT_TOPIC = "agent";
    final private static String ALL_TOPIC = BuildConfig.TOPIC_FCM_ALL_DEVICE;

    private Bundle bundleNextLogin  = new Bundle();
    private Context mContext;
    private SecurePreferences sp;
    private BundleToJSON bundleToJSON = new BundleToJSON();

    public FCMManager(Context context){
        this.mContext = context;
    }

    public static FCMManager getInstance(Context context){
        return new FCMManager(context);
    }

    public static String getTokenFCM(){
        return FirebaseInstanceId.getInstance().getToken();
    }

    public static void subscribeAgent(){
        FirebaseMessaging.getInstance().subscribeToTopic(AGENT_TOPIC);
    }

    public static void unsubscribeAgent(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(AGENT_TOPIC);
    }

    public Bundle getBundleNextLogin() {
        return bundleNextLogin;
    }

    public static void subscribeAll(){
        Timber.d("Subscribe All");
        FirebaseMessaging.getInstance().subscribeToTopic(ALL_TOPIC);
    }

    public Intent checkingAction(int type, Map<String, String> data){
        Intent i = null;
        Timber.d("isi index type "+ String.valueOf(type));

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor mEditor = null;

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        Bundle msg = new Bundle();
        for (String key : data.keySet()) {
            Timber.e(key, data.get(key));
            msg.putString(key, data.get(key));
        }

        if ( msg.containsKey("model_notif") && msg.getString("model_notif") != null ) {

            int modelNotif = Integer.parseInt(msg.getString("model_notif"));
            bundleNextLogin.putInt("model_notif", modelNotif);

            String bundleToJSONString = "";

            switch (modelNotif) {
                case FCMManager.AGENT_LOCATION_SET_SHOP_LOCATION:
                    i = new Intent(mContext, BbsMemberLocationActivity.class);
                    if ( msg.containsKey("options") && msg.getString("options") != null ) {
                        try {
                            JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                            bundleNextLogin.putString("memberId", jsonOptions.getJSONObject(0).getString("member_id"));
                            bundleNextLogin.putString("shopId", jsonOptions.getJSONObject(0).getString("shop_id"));
                            bundleNextLogin.putString("shopName", jsonOptions.getJSONObject(0).getString("shop_name"));
                            bundleNextLogin.putString("memberType", jsonOptions.getJSONObject(0).getString("member_type"));
                            bundleNextLogin.putString("memberName", jsonOptions.getJSONObject(0).getString("member_name"));
                            bundleNextLogin.putString("commName", jsonOptions.getJSONObject(0).getString("comm_name"));

                            bundleNextLogin.putString("province", jsonOptions.getJSONObject(0).getString("province"));
                            bundleNextLogin.putString("district", jsonOptions.getJSONObject(0).getString("district"));
                            bundleNextLogin.putString("address", jsonOptions.getJSONObject(0).getString("address"));
                            bundleNextLogin.putString("category", "");
                            bundleNextLogin.putString("isMobility", jsonOptions.getJSONObject(0).getString("is_mobility"));
                            i.putExtras(bundleNextLogin);

                            if ( flagLogin.equals(DefineValue.STRING_NO) ) {
                                bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);
                                mEditor = sp.edit();
                                mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                                mEditor.apply();
                            }

                        } catch (JSONException e) {
                            Timber.d("JSONException: "+e.getMessage());
                        }

                    }

                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:

                    i = new Intent(mContext, BBSActivity.class);
                    i.putExtra(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);

                    bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);
                    mEditor = sp.edit();
                    mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                    mEditor.apply();

                    break;
                case FCMManager.AGENT_LOCATION_KEY_REJECT_TRANSACTION:
                    i = new Intent(mContext, BbsSearchAgentActivity.class);
                    if ( msg.containsKey("options") && msg.getString("options") != null ) {
                        try {
                            JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                            String keyCode          = jsonOptions.getJSONObject(0).getString("key_code");
                            String keyAmount        = jsonOptions.getJSONObject(0).getString("amount");
                            String categoryName     = jsonOptions.getJSONObject(0).getString("category_name");
                            String categoryId       = jsonOptions.getJSONObject(0).getString("category_id");
                            Double benefLatitude    = Double.valueOf(jsonOptions.getJSONObject(0).getString("benef_latitude"));
                            Double benefLongitude    = Double.valueOf(jsonOptions.getJSONObject(0).getString("benef_longitude"));

                            bundleNextLogin.putString(DefineValue.CATEGORY_ID, categoryId);
                            bundleNextLogin.putString(DefineValue.CATEGORY_NAME, categoryName);
                            bundleNextLogin.putString(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                            bundleNextLogin.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(keyAmount)));
                            bundleNextLogin.putString(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);
                            bundleNextLogin.putDouble(DefineValue.LAST_CURRENT_LATITUDE, benefLatitude);
                            bundleNextLogin.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, benefLongitude);

                            i.putExtras(bundleNextLogin);

                            if ( flagLogin.equals(DefineValue.STRING_NO) ) {
                                bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);
                                mEditor = sp.edit();
                                mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                                mEditor.apply();
                            }

                        } catch (JSONException e) {
                            Timber.d("JSONException: "+e.getMessage());
                        }

                    }

                    break;
                case FCMManager.AGENT_LOCATION_SHOP_REJECT_TRANSACTION:
                    i = new Intent(mContext, MainPage.class);


                    break;
                case FCMManager.MEMBER_CONFIRM_CASHOUT_TRANSACTION:


                    bundleNextLogin.putInt(DefineValue.INDEX, BBSActivity.CONFIRMCASHOUT);

                    i = new Intent(mContext, BBSActivity.class);
                    i.putExtras(bundleNextLogin);

                    bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);
                    mEditor = sp.edit();
                    mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                    mEditor.apply();

                    break;
                case FCMManager.SHOP_ACCEPT_TRX:
                    i = new Intent(mContext, BbsMapViewByMemberActivity.class);

                    if ( msg.containsKey("options") && msg.getString("options") != null ) {
                        try {
                            JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                            bundleNextLogin.putString(DefineValue.BBS_TX_ID, jsonOptions.getJSONObject(0).getString("tx_id"));
                            bundleNextLogin.putString(DefineValue.CATEGORY_NAME, jsonOptions.getJSONObject(0).getString("category_name"));
                            bundleNextLogin.putString(DefineValue.AMOUNT, jsonOptions.getJSONObject(0).getString("amount"));

                            i.putExtras(bundleNextLogin);

                            //if ( flagLogin.equals(DefineValue.STRING_NO) ) {
                                bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);
                                mEditor = sp.edit();
                                mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                                mEditor.apply();
                            //}

                        } catch (JSONException e) {
                            Timber.d("JSONException: "+e.getMessage());
                        }

                    }


                    break;
                case FCMManager.SHOP_NOTIF_TRANSACTION:

                    if ( msg.containsKey("options") && msg.getString("options") != null )
                    {


                        try {
                            JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                            String keyCode          = jsonOptions.getJSONObject(0).getString("key_code");
                            String keyAmount        = jsonOptions.getJSONObject(0).getString("amount");
                            String keySchemeCode    = jsonOptions.getJSONObject(0).getString("scheme_code");

                            bundleNextLogin.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                            if (keySchemeCode.equals(DefineValue.CTA)) {
                                bundleNextLogin.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                            } else if (keySchemeCode.equals(DefineValue.ATC)) {
                                bundleNextLogin.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                            }

                            bundleNextLogin.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(keyAmount)));
                            bundleNextLogin.putString(DefineValue.KEY_CODE, keyCode);

                            i = new Intent(mContext, BBSActivity.class);
                            i.putExtras(bundleNextLogin);

                            bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);

                            mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                            mEditor.apply();

                        } catch (JSONException e) {
                            Timber.d("JSONException: "+e.getMessage());
                        }
                    }
                    break;
                case FCMManager.AGENT_LOCATION_KEY_ACCEPT_TRANSACTION:

                    if ( msg.containsKey("options") && msg.getString("options") != null )
                    {

                        try {
                            JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                            String keyCode          = jsonOptions.getJSONObject(0).getString("key_code");
                            String keyAmount        = jsonOptions.getJSONObject(0).getString("amount");
                            String keySchemeCode    = jsonOptions.getJSONObject(0).getString("scheme_code");

                            bundleNextLogin.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                            if (keySchemeCode.equals(DefineValue.CTA)) {
                                bundleNextLogin.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                            } else if (keySchemeCode.equals(DefineValue.ATC)) {
                                bundleNextLogin.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                            }

                            bundleNextLogin.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                            if (keySchemeCode.equals(DefineValue.CTA)) {
                                bundleNextLogin.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                            } else if (keySchemeCode.equals(DefineValue.ATC)) {
                                bundleNextLogin.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                            }

                            bundleNextLogin.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(keyAmount)));
                            bundleNextLogin.putString(DefineValue.KEY_CODE, keyCode);

                            i = new Intent(mContext, BBSActivity.class);
                            i.putExtras(bundleNextLogin);


                            bundleToJSONString = bundleToJSON.getJson(bundleNextLogin);
                            mEditor = sp.edit();
                            mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                            mEditor.apply();


                        } catch (JSONException e) {
                            Timber.d("JSONException: "+e.getMessage());
                        }
                    }
                    break;
                case FCMManager.REJECT_UPGRADE_MEMBER:
                    sp = CustomSecurePref.getInstance().getmSecurePrefs();
                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));
                            Integer isRegisteredLevel = jsonOptions.getJSONObject(0).getInt("is_registered");
                            String reject_ktp = jsonOptions.getJSONObject(0).getString("reject_ktp");
                            String reject_foto = jsonOptions.getJSONObject(0).getString("reject_foto");
                            String reject_ttd = jsonOptions.getJSONObject(0).getString("reject_ttd");
                            String remark_ktp = jsonOptions.getJSONObject(0).getString("remark_ktp");
                            String remark_foto = jsonOptions.getJSONObject(0).getString("remark_foto");
                            String remark_ttd = jsonOptions.getJSONObject(0).getString("remark_ttd");

                            sp.edit().putInt(DefineValue.IS_REGISTERED_LEVEL,isRegisteredLevel).apply();
                            sp.edit().putString(DefineValue.REJECT_KTP,reject_ktp).apply();
                            sp.edit().putString(DefineValue.REJECT_FOTO,reject_foto).apply();
                            sp.edit().putString(DefineValue.REJECT_TTD,reject_ttd).apply();
                            sp.edit().putString(DefineValue.REMARK_KTP,remark_ktp).apply();
                            sp.edit().putString(DefineValue.REMARK_FOTO,remark_foto).apply();
                            sp.edit().putString(DefineValue.REMARK_TTD,remark_ttd).apply();
                            sp.edit().putString(DefineValue.DATA_REJECT_UPGRADE_MEMBER, jsonOptions.toString()).apply();

                            i = new Intent(mContext, MyProfileNewActivity.class);
                        }
                        catch (JSONException e)
                        {
                            Timber.d("JSONException: " + e.getMessage());
                        }
                    }
                    break;
                case FCMManager.REJECT_UPGRADE_AGENT:
                    sp = CustomSecurePref.getInstance().getmSecurePrefs();
                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));
                            String reject_siup = jsonOptions.getJSONObject(0).getString("reject_siup");
                            String reject_npwp = jsonOptions.getJSONObject(0).getString("reject_npwp");
                            String remark_siup = jsonOptions.getJSONObject(0).getString("remark_siup");
                            String remark_npwp = jsonOptions.getJSONObject(0).getString("remark_npwp");

                            sp.edit().putString(DefineValue.REJECT_SIUP,reject_siup).apply();
                            sp.edit().putString(DefineValue.REJECT_NPWP,reject_npwp).apply();
                            sp.edit().putString(DefineValue.REMARK_SIUP,remark_siup).apply();
                            sp.edit().putString(DefineValue.REMARK_NPWP,remark_npwp).apply();
                            sp.edit().putBoolean(DefineValue.IS_UPGRADE_AGENT,false).apply();
                            sp.edit().putString(DefineValue.DATA_REJECT_UPGRADE_AGENT, jsonOptions.toString()).apply();

                            i = new Intent(mContext, UpgradeAgentActivity.class);
                        }
                        catch (JSONException e)
                        {
                            Timber.d("JSONException: " + e.getMessage());
                        }
                    }
                    break;
                case FCMManager.BLAST_INFO:
                    i = new Intent(mContext, MainPage.class);
                    break;
                case FCMManager.SOURCE_OF_FUND:
                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));
                            String txId = jsonOptions.getJSONObject(0).getString("tx_id");
                            bundleNextLogin.putString(DefineValue.TX_ID,txId);
                            bundleNextLogin.putString(DefineValue.IS_INAPP,"Y");

                            i = new Intent(mContext, SourceOfFundActivity.class);
                            i.putExtras(bundleNextLogin);
                        }
                        catch (JSONException e)
                        {
                            Timber.d("JSONException: " + e.getMessage());
                        }
                    }
                    break;
                default:
                    i = new Intent(mContext, MainPage.class);
                    break;
            }

        }
        return i;
    }

    public Intent checkingAction(int type){
        Timber.d("isi index type2 "+ String.valueOf(type));
        Intent i;
        switch (type) {
            case OPEN_PLAYSTORE:
                Timber.d("masuk open playstore");
                String appPackageName = mContext.getPackageName(); // getPackageName() from Context or Activity object

                try {
                    i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                } catch (android.content.ActivityNotFoundException anfe) {
                    i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                }
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            default:
                i =  new Intent();
                break;
        }
        return i;
    }

}
