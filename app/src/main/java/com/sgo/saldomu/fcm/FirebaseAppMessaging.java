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
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JobScheduleManager;
import com.sgo.saldomu.coreclass.WebParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

import static com.sgo.saldomu.fcm.FCMManager.OPEN_PLAYSTORE;
import static com.sgo.saldomu.fcm.FCMManager.SYNC_BBS_DATA;

/**
 * Created by yuddistirakiki on 8/16/17.
 */

public class FirebaseAppMessaging extends FirebaseMessagingService {

    NotificationManager mNotificationManager;

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
            } else {

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

        PendingIntent contentIntent = null;
        Intent intent   = null;
        FCMManager fcmManager = new FCMManager(this);

        if ( msg.containsKey("model_notif") && msg.getString("model_notif") != null ) {

            int modelNotif = Integer.parseInt(msg.getString("model_notif"));
            Bundle bundle = new Bundle();

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            switch (modelNotif) {
                case FCMManager.AGENT_LOCATION_SET_SHOP_LOCATION:
                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:
                    intent = new Intent(this, BbsApprovalAgentActivity.class);

                    stackBuilder.addParentStack(BbsApprovalAgentActivity.class);
                    stackBuilder.addNextIntent(intent);

                    contentIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    break;
                case FCMManager.AGENT_LOCATION_KEY_REJECT_TRANSACTION:
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
                case FCMManager.AGENT_LOCATION_KEY_ACCEPT_TRANSACTION:

                    if ( msg.containsKey("options") && msg.getString("options") != null )
                    {
                        try {
                            JSONObject jsonOptionObject = new JSONObject(msg.getString("options"));

                            String keyCode          = jsonOptionObject.getString("key_code");
                            String keyAmount        = jsonOptionObject.getString("amount");
                            String keySchemeCode    = jsonOptionObject.getString("scheme_code");

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
                            Timber.d("JSONException: "+e.getMessage());
                        }
                    }
                    break;
                default:

                    break;
            }

        } else if(msg.containsKey("type")) {

            int msgType = Integer.parseInt(msg.getString("type"));

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            switch (msgType) {
                case FCMManager.OPEN_PLAYSTORE:
                    intent = fcmManager.checkingAction(msgType);
                    contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    break;
                case FCMManager.AGENT_LOCATION_MEMBER_REQ_TRX_TO_AGENT:
                    intent = new Intent(this, BbsApprovalAgentActivity.class);

                    stackBuilder.addParentStack(BbsApprovalAgentActivity.class);
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
                        .setSound(defaultSoundUri);

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
