package com.sgo.saldomu.fcm;

import android.app.NotificationManager;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import timber.log.Timber;

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

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Timber.d("onDeleteMessage");
    }

    private void sendNotification(Map<String, String> data) {

        // handle notification here
        /*
		 * types of notification 1. result update 2. circular update 3. student
		 * corner update 4. App custom update 5. Custom Message 6. Notice from
		 * College custom
		 */
        int num = ++NOTIFICATION_ID;
        Bundle msg = new Bundle();
        for (String key : data.keySet()) {
            Log.e(key, data.get(key));
            msg.putString(key, data.get(key));
        }


        pref = getSharedPreferences("UPDATE_INSTANCE", MODE_PRIVATE);
        edit = pref.edit();
        Intent backIntent;
        Intent intent = null;
        PendingIntent pendingIntent = null;
        backIntent = new Intent(getApplicationContext(), MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SharedPreferences sp;
        Editor editor;

        switch (Integer.parseInt(msg.getString("type"))) {

            case 1:
                break;
            case 2:

                backIntent = new Intent(getApplicationContext(),
                        DailogeNotice.class);
                backIntent.putExtras(msg);

                stackBuilder = TaskStackBuilder.create(this);
                // Adds the back stack for the Intent (but not the Intent itself)
                // stackBuilder.addParentStack(AppUpdate.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(backIntent);
                pendingIntent = stackBuilder.getPendingIntent(num,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                break;
            case 3:
                backIntent = new Intent(getApplicationContext(),
                        CustomeWebView.class);

                backIntent.putExtras(msg);
                // The stack builder object will contain an artificial back stack
                // for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out
                // of
                // your application to the Home screen.
                stackBuilder = TaskStackBuilder.create(this);
                // Adds the back stack for the Intent (but not the Intent itself)
                // stackBuilder.addParentStack(AppUpdate.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(backIntent);
                pendingIntent = stackBuilder.getPendingIntent(num,
                        PendingIntent.FLAG_UPDATE_CURRENT);


                break;

            case 4:
                backIntent = new Intent(getApplicationContext(),
                        MainActivity.class);

                // The stack builder object will contain an artificial back stack
                // for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out
                // of
                // your application to the Home screen.
                stackBuilder = TaskStackBuilder.create(this);
                // Adds the back stack for the Intent (but not the Intent itself)
                // stackBuilder.addParentStack(AppUpdate.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(backIntent);
                pendingIntent = stackBuilder.getPendingIntent(num,
                        PendingIntent.FLAG_UPDATE_CURRENT);


                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                db.addContact(
                        new News(msg.getString("title"), msg.getString("msg"),
                                msg.getString("link"), msg.getString("image")),
                        DatabaseHandler.TABLE_NEWS);

                Intent intent_notify = new Intent(FCMActivity.NEW_NOTIFICATION);
                intent_notify.putExtra("DUMMY","MUST");
                sendBroadcast(intent_notify);
                break;
            default:
                break;
        }
        if (!is_noty) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this);

            mBuilder.setSmallIcon(R.drawable.ic_stat_fcm)
                    .setContentTitle(msg.getString("title"))
                    .setStyle(
                            new NotificationCompat.BigTextStyle().bigText(msg
                                    .getString("msg").toString()))
                    .setAutoCancel(true)
                    .setContentText(msg.getString("msg"));

            if (Integer.parseInt(msg.getString("type")) != 1) {
                mBuilder.setContentIntent(pendingIntent);
            }

            mBuilder.setDefaults(Notification.DEFAULT_ALL);

            mNotificationManager.notify(++NOTIFICATION_ID, mBuilder.build());
        }
    }
}
