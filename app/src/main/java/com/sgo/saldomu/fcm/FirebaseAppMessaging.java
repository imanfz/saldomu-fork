package com.sgo.saldomu.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.BbsMemberLocationActivity;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.BundleToJSON;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JobScheduleManager;
import com.sgo.saldomu.coreclass.WebParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.sgo.saldomu.fcm.FCMManager.SYNC_BBS_DATA;

/**
 * Created by yuddistirakiki on 8/16/17.
 */

public class FirebaseAppMessaging extends FirebaseMessagingService {

    NotificationManager mNotificationManager;
    private SecurePreferences sp;
    private BundleToJSON bundleToJSON = new BundleToJSON();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.d("From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Timber.d("Message data payload: " + remoteMessage.getData());
            if(remoteMessage.getData().containsKey(WebParams.SYNC_CODE)){
                    switch (Integer.valueOf(remoteMessage.getData().get(WebParams.SYNC_CODE))){
                        case SYNC_BBS_DATA :
                            scheduleJob();
                            break;
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
        Intent intent   = null;
        FCMManager fcmManager = new FCMManager(this);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
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
//



        if ( msg.containsKey("model_notif") && msg.getString("model_notif") != null ) {

            int modelNotif = Integer.parseInt(msg.getString("model_notif"));
            Bundle bundle = new Bundle();

            bundle.putInt("model_notif", modelNotif);
            switch (modelNotif) {
                case FCMManager.AGENT_LOCATION_SET_SHOP_LOCATION:
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


                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }

                    }

                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:

                    break;
                case FCMManager.AGENT_LOCATION_KEY_REJECT_TRANSACTION:
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


                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }

                    }

                    break;
                case FCMManager.AGENT_LOCATION_SHOP_REJECT_TRANSACTION:

                    break;
                case FCMManager.MEMBER_CONFIRM_CASHOUT_TRANSACTION:


                    bundle.putInt(DefineValue.INDEX, BBSActivity.CONFIRMCASHOUT);


                    break;
                case FCMManager.SHOP_ACCEPT_TRX:

                    if ( msg.containsKey("options") && msg.getString("options") != null ) {
                        try {
                            JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                            bundle.putString(DefineValue.BBS_TX_ID, jsonOptions.getJSONObject(0).getString("tx_id"));
                            bundle.putString(DefineValue.CATEGORY_NAME, jsonOptions.getJSONObject(0).getString("category_name"));
                            bundle.putString(DefineValue.AMOUNT, jsonOptions.getJSONObject(0).getString("amount"));


                        } catch (JSONException e) {
                            Timber.d("JSONException: "+e.getMessage());
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



                        } catch (JSONException e) {
                            Timber.d("JSONException: " + e.getMessage());
                        }
                    }
                    break;
                default:

                    break;
            }

            if ( flagLogin.equals(DefineValue.STRING_NO) ) {
                intent = new Intent(this, LoginActivity.class);

                intent.putExtras(bundle);
                stackBuilder.addParentStack(LoginActivity.class);
                stackBuilder.addNextIntent(intent);

                if ( bundle != null ) {
                    String bundleToJSONString = bundleToJSON.getJson(bundle);
                    if ( !bundleToJSONString.equals("") ) {
                        SecurePreferences.Editor mEditor = sp.edit();
                        mEditor.putString(DefineValue.NOTIF_DATA_NEXT_LOGIN,bundleToJSONString);
                        mEditor.apply();
                    }
                }

                contentIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
            } else {

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


                            } catch (JSONException e) {
                                Timber.d("JSONException: " + e.getMessage());
                            }

                        }
                        stackBuilder.addParentStack(BbsMemberLocationActivity.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
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


                            } catch (JSONException e) {
                                Timber.d("JSONException: " + e.getMessage());
                            }

                        }
                        stackBuilder.addParentStack(BbsSearchAgentActivity.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        break;
                    case FCMManager.AGENT_LOCATION_SHOP_REJECT_TRANSACTION:
                        intent = new Intent(this, MainPage.class);

                        stackBuilder.addParentStack(MainPage.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        break;
                    case FCMManager.MEMBER_CONFIRM_CASHOUT_TRANSACTION:


                        bundle.putInt(DefineValue.INDEX, BBSActivity.CONFIRMCASHOUT);

                        intent = new Intent(this, BBSActivity.class);
                        intent.putExtras(bundle);

                        stackBuilder.addParentStack(BBSActivity.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        break;
                    case FCMManager.SHOP_ACCEPT_TRX:
                        intent = new Intent(this, BbsMapViewByMemberActivity.class);

                        if ( msg.containsKey("options") && msg.getString("options") != null ) {
                            try {
                                JSONArray jsonOptions   = new JSONArray(msg.getString("options"));

                                bundle.putString(DefineValue.BBS_TX_ID, jsonOptions.getJSONObject(0).getString("tx_id"));
                                bundle.putString(DefineValue.CATEGORY_NAME, jsonOptions.getJSONObject(0).getString("category_name"));
                                bundle.putString(DefineValue.AMOUNT, jsonOptions.getJSONObject(0).getString("amount"));

                                intent.putExtras(bundle);

                            } catch (JSONException e) {
                                Timber.d("JSONException: "+e.getMessage());
                            }

                        }

                        stackBuilder.addParentStack(BbsMapViewByMemberActivity.class);
                        stackBuilder.addNextIntent(intent);

                        contentIntent =
                                stackBuilder.getPendingIntent(
                                        getNotifId(),
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
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

                                stackBuilder.addParentStack(BBSActivity.class);
                                stackBuilder.addNextIntent(intent);

                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                0,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );

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

                                stackBuilder.addParentStack(BBSActivity.class);
                                stackBuilder.addNextIntent(intent);

                                contentIntent =
                                        stackBuilder.getPendingIntent(
                                                0,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );

                            } catch (JSONException e) {
                                Timber.d("JSONException: " + e.getMessage());
                            }
                        }
                        break;
                    default:

                        break;
                }
            }
        }
        else if(msg.containsKey("type")) {

            int msgType = Integer.parseInt(msg.getString("type"));

            Map<String, String> mapData = new HashMap<String,String>();

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


//            case 3:
//                backIntent = new Intent(getApplicationContext(),
//                        CustomeWebView.class);
//
//                backIntent.putExtras(msg);
//                // The stack builder object will contain an artificial back stack
//                // for the
//                // started Activity.
//                // This ensures that navigating backward from the Activity leads out
//                // of
//                // your application to the Home screen.
//                stackBuilder = TaskStackBuilder.create(this);
//                // Adds the back stack for the Intent (but not the Intent itself)
//                // stackBuilder.addParentStack(AppUpdate.class);
//                // Adds the Intent that starts the Activity to the top of the stack
//                stackBuilder.addNextIntent(backIntent);
//                pendingIntent = stackBuilder.getPendingIntent(num,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//                break;
//
//            case 4:
//                backIntent = new Intent(getApplicationContext(),
//                        MainActivity.class);
//
//                // The stack builder object will contain an artificial back stack
//                // for the
//                // started Activity.
//                // This ensures that navigating backward from the Activity leads out
//                // of
//                // your application to the Home screen.
//                stackBuilder = TaskStackBuilder.create(this);
//                // Adds the back stack for the Intent (but not the Intent itself)
//                // stackBuilder.addParentStack(AppUpdate.class);
//                // Adds the Intent that starts the Activity to the top of the stack
//                stackBuilder.addNextIntent(backIntent);
//                pendingIntent = stackBuilder.getPendingIntent(num,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
//                db.addContact(
//                        new News(msg.getString("title"), msg.getString("msg"),
//                                msg.getString("link"), msg.getString("image")),
//                        DatabaseHandler.TABLE_NEWS);
//
//                Intent intent_notify = new Intent(FCMActivity.NEW_NOTIFICATION);
//                intent_notify.putExtra("DUMMY","MUST");
//                sendBroadcast(intent_notify);
//                break;
//            default:
//                break;
        }


        Timber.d("Debug 2: " + msg.toString());

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif_logo)
                        .setContentTitle(notification.getBody())
                        .setContentText(msg.getString("msg", ""))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg.getString("msg", "")));

        if (contentIntent != null)
            mBuilder.setContentIntent(contentIntent);

        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(getNotifId(), mBuilder.build());

    }

    int getNotifId(){
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        return Integer.valueOf(last4Str);
    }


}
