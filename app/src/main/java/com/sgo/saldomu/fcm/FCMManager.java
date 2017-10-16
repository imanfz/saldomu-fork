package com.sgo.saldomu.fcm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sgo.saldomu.BuildConfig;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/21/17.
 */

public class FCMManager {
    public final static int SYNC_BBS_DATA = 70;
    public final static int OPEN_PLAYSTORE = 80;

    final private static String AGENT_TOPIC = "agent";
    final private static String ALL_TOPIC = BuildConfig.TOPIC_FCM_ALL_DEVICE;

    private Context mContext;

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

    public static void subscribeAll(){
        Timber.d("Subscribe All");
        FirebaseMessaging.getInstance().subscribeToTopic(ALL_TOPIC);
    }

    public Intent checkingAction(int type){
        Intent i = null;
        Timber.d("isi index type "+ String.valueOf(type));
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
        }
        return i;
    }

}
