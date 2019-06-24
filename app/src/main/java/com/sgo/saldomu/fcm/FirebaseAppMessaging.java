package com.sgo.saldomu.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileNewActivity;
import com.sgo.saldomu.activities.SourceOfFundActivity;
import com.sgo.saldomu.activities.UpgradeAgentActivity;
import com.sgo.saldomu.coreclass.BundleToJSON;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JobScheduleManager;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.utils.UserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.sgo.saldomu.fcm.FCMManager.MEMBER_RATING_TRX;
import static com.sgo.saldomu.fcm.FCMManager.SYNC_BBS_DATA;
import static com.sgo.saldomu.fcm.FCMManager.VERIFY_ACC;

/**
 * Created by yuddistirakiki on 8/16/17.
 */

public class FirebaseAppMessaging extends FirebaseMessagingService {

    NotificationManager mNotificationManager;
    private SecurePreferences sp;
    private BundleToJSON bundleToJSON = new BundleToJSON();
    private String flagLogin;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.d("From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Timber.d("Message data payload: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey(WebParams.SYNC_CODE)) {
                switch (Integer.valueOf(remoteMessage.getData().get(WebParams.SYNC_CODE))) {
                    case SYNC_BBS_DATA:
                        if (UserUtils.isLogin())
                            scheduleJob();
                        else
                            CustomSecurePref.getSecurePrefsInstance().edit().putBoolean(DefineValue.IS_MUST_UPDATE_BBS_DATA, true).apply();
                        break;
                }
            }

            if (remoteMessage.getData().containsKey(DefineValue.MODEL_NOTIF)) {
                int modelNotif = Integer.parseInt(remoteMessage.getData().get(DefineValue.MODEL_NOTIF));
                String jsonOptions = remoteMessage.getData().get(DefineValue.FCM_OPTIONS);

                if (modelNotif == MEMBER_RATING_TRX) {
                    sp = CustomSecurePref.getInstance().getmSecurePrefs();
                    flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
                    if (flagLogin == null)
                        flagLogin = DefineValue.STRING_NO;

                    try {
                        JSONArray jsonObj = new JSONArray(jsonOptions);
                        JSONObject jsonObj2 = jsonObj.getJSONObject(0);
                        jsonObj2.put("model_notif", modelNotif);

                        SecurePreferences.Editor mEditor = sp.edit();
                        mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, jsonObj2.toString());
                        mEditor.apply();
                    } catch (JSONException e) {
                        Timber.d("JSONException FCM Messaging OptionData: " + e.getMessage());
                    }

                    if (flagLogin.equals(DefineValue.STRING_YES)) {
                        Intent broadcast = new Intent();
                        broadcast.setAction(DefineValue.INTENT_ACTION_FCM_DATA);
                        broadcast.putExtra(DefineValue.MODEL_NOTIF, modelNotif);
                        broadcast.putExtra(DefineValue.FCM_OPTIONS, jsonOptions);
                        sendBroadcast(broadcast);
                    }
                }
                if (modelNotif == VERIFY_ACC) {

                    sp = CustomSecurePref.getInstance().getmSecurePrefs();
                    try {
                        JSONArray jsonObj = new JSONArray(jsonOptions);
                        JSONObject jsonObj2 = jsonObj.getJSONObject(0);
                        jsonObj2.put("model_notif", modelNotif);

                        SecurePreferences.Editor mEditor = sp.edit();
                        mEditor.putString(DefineValue.SENDER_ID, jsonObj2.getString(WebParams.USER_ID));
                        mEditor.apply();

                        if (flagLogin == null)
                            flagLogin = DefineValue.STRING_NO;

                        if (flagLogin.equals(DefineValue.STRING_YES)) {

                        } else {
                            Intent broadcast = new Intent(this, LoginActivity.class);
                            broadcast.setAction(DefineValue.INTENT_ACTION_FCM_DATA);
                            broadcast.putExtra(DefineValue.MODEL_NOTIF, modelNotif);
                            broadcast.putExtra(DefineValue.FCM_OPTIONS, jsonOptions);
                            broadcast.putExtra(DefineValue.USER_ID, sp.getString(DefineValue.SENDER_ID,""));
                            broadcast.putExtra(DefineValue.USER_IS_NEW, Integer.parseInt(jsonObj2.getString(WebParams.IS_NEW_USER)));
                            broadcast.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(broadcast);
                        }

                    } catch (JSONException e) {
                        Timber.d("JSONException FCM Messaging OptionData: " + e.getMessage());
                    }
                }
            }


        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            Timber.d("Message Notification Body: title : %1$s, tag : %2$s , body : %3$s, messageType : %4$s, collapseKey: %5$s",
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getTag(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getMessageType(),
                    remoteMessage.getCollapseKey()

            );
            sendNotification(remoteMessage.getData(), remoteMessage.getNotification());

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Timber.d("onDeleteMessage");
    }

    private void scheduleJob() {
        JobScheduleManager.getInstance(this).scheduleUpdateDataBBS();
    }

    private void sendNotification(Map<String, String> data, RemoteMessage.Notification notification) {

        // handle notification here
        /*
         * types of notification 1. result update 2. circular update 3. student
         * corner update 4. App custom update 5. Custom Message 6. Notice from
         * College custom
         */
//        int num = ++NOTIFICATION_ID;

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        PendingIntent contentIntent = null;
        Intent intent = null;
        FCMManager fcmManager = new FCMManager(this);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        String bundleToJSONString = null;
        SecurePreferences.Editor mEditor = null;

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (flagLogin == null)
            flagLogin = DefineValue.STRING_NO;


        Bundle msg = new Bundle();
        for (String key : data.keySet()) {
            Timber.e(key, data.get(key));
            msg.putString(key, data.get(key));
        }
//
//
//        pref = getSharedPreferences("UPDATE_INSTANCE", MODE_PRIVATE);
//        edit = pref.edit();
//        Intent backIntent;
//        Intent intent = null;
//        PendingIntent pendingIntent = null;
//        backIntent = new Intent(getApplicationContext(), MainActivity.class);
//        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        SharedPreferences sp;
//        Editor editor;

        if (msg.containsKey("model_notif") && msg.getString("model_notif") != null) {

            int modelNotif = Integer.parseInt(msg.getString("model_notif"));
            Bundle bundle = new Bundle();

            bundle.putInt("model_notif", modelNotif);


            switch (modelNotif) {
                case FCMManager.AGENT_LOCATION_SET_SHOP_LOCATION:
                    intent = new Intent(this, BbsMemberLocationActivity.class);
                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));


                            bundle.putString("memberId", jsonOptions.getJSONObject(0).getString("member_id"));
                            bundle.putString("shopId", jsonOptions.getJSONObject(0).getString("shop_id"));
                            bundle.putString("shopName", jsonOptions.getJSONObject(0).getString("shop_name"));
                            bundle.putString("memberType", jsonOptions.getJSONObject(0).getString("member_type"));
                            bundle.putString("memberName", jsonOptions.getJSONObject(0).getString("member_name"));
                            bundle.putString("commName", jsonOptions.getJSONObject(0).getString("comm_name"));

                            bundle.putString("province", jsonOptions.getJSONObject(0).getString("province"));
                            bundle.putString("district", jsonOptions.getJSONObject(0).getString("district"));
                            bundle.putString("address", jsonOptions.getJSONObject(0).getString("address"));
                            bundle.putString("category", "");
                            bundle.putString("isMobility", jsonOptions.getJSONObject(0).getString("is_mobility"));
                            intent.putExtras(bundle);

                            if (flagLogin.equals(DefineValue.STRING_NO)) {
                                bundleToJSONString = bundleToJSON.getJson(bundle);
                                mEditor = sp.edit();
                                mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                                mEditor.apply();

                            } else {
                                stackBuilder.addParentStack(BbsMemberLocationActivity.class);
                                stackBuilder.addNextIntent(intent);

                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                1,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                            }

                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }

                    }

                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:

                    intent = new Intent(this, BBSActivity.class);
                    intent.putExtra(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);

                    bundleToJSONString = bundleToJSON.getJson(bundle);
                    mEditor = sp.edit();
                    mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                    mEditor.apply();

                    if (flagLogin.equals(DefineValue.STRING_YES)) {
                        stackBuilder.addParentStack(BBSActivity.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        1,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                    }


                    break;
                case FCMManager.AGENT_LOCATION_KEY_REJECT_TRANSACTION:
                    intent = new Intent(this, BbsSearchAgentActivity.class);
                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));

                            String keyCode = jsonOptions.getJSONObject(0).getString("key_code");
                            String keyAmount = jsonOptions.getJSONObject(0).getString("amount");
                            String categoryName = jsonOptions.getJSONObject(0).getString("category_name");
                            String categoryId = jsonOptions.getJSONObject(0).getString("category_id");
                            Double benefLatitude = Double.valueOf(jsonOptions.getJSONObject(0).getString("benef_latitude"));
                            Double benefLongitude = Double.valueOf(jsonOptions.getJSONObject(0).getString("benef_longitude"));

                            bundle.putString(DefineValue.CATEGORY_ID, categoryId);
                            bundle.putString(DefineValue.CATEGORY_NAME, categoryName);
                            bundle.putString(DefineValue.BBS_AGENT_MOBILITY, DefineValue.STRING_NO);
                            bundle.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(keyAmount)));
                            bundle.putString(DefineValue.IS_AUTOSEARCH, DefineValue.STRING_YES);
                            bundle.putDouble(DefineValue.LAST_CURRENT_LATITUDE, benefLatitude);
                            bundle.putDouble(DefineValue.LAST_CURRENT_LONGITUDE, benefLongitude);

                            intent.putExtras(bundle);

                            if (flagLogin.equals(DefineValue.STRING_NO)) {
                                bundleToJSONString = bundleToJSON.getJson(bundle);
                                mEditor = sp.edit();
                                mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                                mEditor.apply();

                            } else {
                                stackBuilder.addParentStack(BbsSearchAgentActivity.class);
                                stackBuilder.addNextIntent(intent);

                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                1,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                            }

                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }

                    }
                    break;
                case FCMManager.AGENT_LOCATION_SHOP_REJECT_TRANSACTION:
                    intent = new Intent(this, MainPage.class);
                    if (flagLogin.equals(DefineValue.STRING_YES)) {
                        stackBuilder.addParentStack(MainPage.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        1,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                    } else {
                        bundleToJSONString = bundleToJSON.getJson(bundle);
                        mEditor = sp.edit();
                        mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                        mEditor.apply();
                    }
                    break;
                case FCMManager.MEMBER_CONFIRM_CASHOUT_TRANSACTION:

                    Timber.d("MASUK SINI FCM APP MESSAGING");
                    bundle.putInt(DefineValue.INDEX, BBSActivity.CONFIRMCASHOUT);

                    bundleToJSONString = bundleToJSON.getJson(bundle);
                    mEditor = sp.edit();
                    mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                    mEditor.apply();

                    if (flagLogin.equals(DefineValue.STRING_YES)) {
                        intent = new Intent(this, BBSActivity.class);
                        intent.putExtras(bundle);


                        stackBuilder.addParentStack(BBSActivity.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        1,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                    }
                    break;
                case FCMManager.SHOP_ACCEPT_TRX:
                    intent = new Intent(this, BbsMapViewByMemberActivity.class);

                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));

                            bundle.putString(DefineValue.BBS_TX_ID, jsonOptions.getJSONObject(0).getString("tx_id"));
                            bundle.putString(DefineValue.CATEGORY_NAME, jsonOptions.getJSONObject(0).getString("category_name"));
                            bundle.putString(DefineValue.AMOUNT, jsonOptions.getJSONObject(0).getString("amount"));

                            intent.putExtras(bundle);

                            bundleToJSONString = bundleToJSON.getJson(bundle);
                            mEditor = sp.edit();
                            mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                            mEditor.apply();

                            if (flagLogin.equals(DefineValue.STRING_NO)) {


                            } else {
                                stackBuilder.addParentStack(BbsMapViewByMemberActivity.class);
                                stackBuilder.addNextIntent(intent);

                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                getNotifId(),
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                            }

                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }

                    }

                    break;
                case FCMManager.SHOP_NOTIF_TRANSACTION:

                    if (msg.containsKey("options") && msg.getString("options") != null) {


                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));

                            String keyCode = jsonOptions.getJSONObject(0).getString("key_code");
                            String keyAmount = jsonOptions.getJSONObject(0).getString("amount");
                            String keySchemeCode = jsonOptions.getJSONObject(0).getString("scheme_code");

                            bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                            if (keySchemeCode.equals(DefineValue.CTA)) {
                                bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                            } else if (keySchemeCode.equals(DefineValue.ATC)) {
                                bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                            }

                            bundle.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(keyAmount)));
                            bundle.putString(DefineValue.KEY_CODE, keyCode);

                            intent = new Intent(this, BBSActivity.class);
                            intent.putExtras(bundle);

                            bundleToJSONString = bundleToJSON.getJson(bundle);
                            mEditor = sp.edit();
                            mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                            mEditor.apply();

                            if (flagLogin.equals(DefineValue.STRING_NO)) {

                            } else {
                                stackBuilder.addParentStack(BBSActivity.class);
                                stackBuilder.addNextIntent(intent);

                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                1,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                            }
                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }
                    }
                    break;
                case FCMManager.AGENT_LOCATION_KEY_ACCEPT_TRANSACTION:

                    if (msg.containsKey("options") && msg.getString("options") != null) {

                            /*intent = new Intent(this, BbsMapViewByMemberActivity.class);

                            stackBuilder.addParentStack(BbsMapViewByMemberActivity.class);
                            stackBuilder.addNextIntent(intent);

                            contentIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );*/

                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));

                            String keyCode = jsonOptions.getJSONObject(0).getString("key_code");
                            String keyAmount = jsonOptions.getJSONObject(0).getString("amount");
                            String keySchemeCode = jsonOptions.getJSONObject(0).getString("scheme_code");

                            bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                            if (keySchemeCode.equals(DefineValue.CTA)) {
                                bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                            } else if (keySchemeCode.equals(DefineValue.ATC)) {
                                bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                            }

                            bundle.putInt(DefineValue.INDEX, BBSActivity.TRANSACTION);
                            if (keySchemeCode.equals(DefineValue.CTA)) {
                                bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHIN);
                            } else if (keySchemeCode.equals(DefineValue.ATC)) {
                                bundle.putString(DefineValue.TYPE, DefineValue.BBS_CASHOUT);
                            }

                            bundle.putString(DefineValue.AMOUNT, String.format("%.0f", Double.valueOf(keyAmount)));
                            bundle.putString(DefineValue.KEY_CODE, keyCode);

                            intent = new Intent(this, BBSActivity.class);
                            intent.putExtras(bundle);

                            bundleToJSONString = bundleToJSON.getJson(bundle);
                            mEditor = sp.edit();
                            mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN, bundleToJSONString);
                            mEditor.apply();

                            if (flagLogin.equals(DefineValue.STRING_NO)) {

                            } else {
                                stackBuilder.addParentStack(BBSActivity.class);
                                stackBuilder.addNextIntent(intent);
                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                1,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );
                            }
                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }
                    }
                    break;
                case FCMManager.REJECT_UPGRADE_MEMBER:

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

                            sp.edit().putInt(DefineValue.IS_REGISTERED_LEVEL, isRegisteredLevel).apply();
                            sp.edit().putString(DefineValue.REJECT_KTP, reject_ktp).apply();
                            sp.edit().putString(DefineValue.REJECT_FOTO, reject_foto).apply();
                            sp.edit().putString(DefineValue.REJECT_TTD, reject_ttd).apply();
                            sp.edit().putString(DefineValue.REMARK_KTP, remark_ktp).apply();
                            sp.edit().putString(DefineValue.REMARK_FOTO, remark_foto).apply();
                            sp.edit().putString(DefineValue.REMARK_TTD, remark_ttd).apply();
                            sp.edit().putString(DefineValue.DATA_REJECT_UPGRADE_MEMBER, jsonOptions.toString()).apply();

                            intent = new Intent(this, MyProfileNewActivity.class);

                            stackBuilder.addParentStack(MyProfileNewActivity.class);
                            stackBuilder.addNextIntent(intent);

                            contentIntent =
                                    stackBuilder.getPendingIntent(
                                            1,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }


                    }
                    break;
                case FCMManager.REJECT_UPGRADE_AGENT:

                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));
                            String reject_siup = jsonOptions.getJSONObject(0).getString("reject_siup");
                            String reject_npwp = jsonOptions.getJSONObject(0).getString("reject_npwp");
                            String remark_siup = jsonOptions.getJSONObject(0).getString("remark_siup");
                            String remark_npwp = jsonOptions.getJSONObject(0).getString("remark_npwp");

                            sp.edit().putString(DefineValue.REJECT_SIUP, reject_siup).apply();
                            sp.edit().putString(DefineValue.REJECT_NPWP, reject_npwp).apply();
                            sp.edit().putString(DefineValue.REMARK_SIUP, remark_siup).apply();
                            sp.edit().putString(DefineValue.REMARK_NPWP, remark_npwp).apply();
                            sp.edit().putBoolean(DefineValue.IS_UPGRADE_AGENT, false).apply();
                            sp.edit().putString(DefineValue.DATA_REJECT_UPGRADE_AGENT, jsonOptions.toString()).apply();

                            intent = new Intent(this, UpgradeAgentActivity.class);

                            stackBuilder.addParentStack(UpgradeAgentActivity.class);
                            stackBuilder.addNextIntent(intent);

                            contentIntent =
                                    stackBuilder.getPendingIntent(
                                            1,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }


                    }
                    break;

                case FCMManager.BLAST_INFO:
                    intent = new Intent(this, MainPage.class);
                    if (flagLogin.equals(DefineValue.STRING_NO)) {


                    } else {
                        stackBuilder.addParentStack(MainPage.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        getNotifId(),
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                    }
                case FCMManager.SOURCE_OF_FUND:

                    if (msg.containsKey("options") && msg.getString("options") != null) {
                        try {
                            JSONArray jsonOptions = new JSONArray(msg.getString("options"));
                            String txId = jsonOptions.getJSONObject(0).getString("tx_id");
                            bundle.putString(DefineValue.TX_ID, txId);
                            bundle.putString(DefineValue.IS_INAPP, "Y");
                            intent = new Intent(this, SourceOfFundActivity.class);
                            intent.putExtras(bundle);
                            stackBuilder.addParentStack(SourceOfFundActivity.class);
                            stackBuilder.addNextIntent(intent);

                            contentIntent =
                                    stackBuilder.getPendingIntent(
                                            1,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }


                    }
                    break;
                case FCMManager.VERIFY_ACC:
                    break;
                default:
                    break;
            }

        } else if (msg.containsKey("type")) {

            int msgType = Integer.parseInt(msg.getString("type"));

            Map<String, String> mapData = new HashMap<String, String>();

            switch (msgType) {
                case FCMManager.OPEN_PLAYSTORE:
                    intent = fcmManager.checkingAction(msgType, mapData);
                    contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:

                    intent = new Intent(this, BBSActivity.class);
                    intent.putExtra(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);

                    stackBuilder.addParentStack(BBSActivity.class);
                    stackBuilder.addNextIntent(intent);

                    contentIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    break;
                default:

                    break;
            }
        }


        Timber.d("Debug 2: " + msg.toString());

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "channel_name";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_pin_bw);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(notification.getTitle())
                        .setContentText(msg.getString("msg", ""))
                        .setSmallIcon(R.mipmap.ic_launcher_pin_only)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setChannelId(CHANNEL_ID)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg.getString("msg", "")));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(largeIcon);
        } else {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_pin_only);
        }

        if (contentIntent == null) {
            contentIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        }

        mBuilder.setContentIntent(contentIntent);


        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotifyMgr.createNotificationChannel(mChannel);
        }

        // Builds the notification and issues it.
        mNotifyMgr.notify(getNotifId(), mBuilder.build());
    }

    int getNotifId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        return Integer.valueOf(last4Str);
    }


}
