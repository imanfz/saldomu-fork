package com.sgo.saldomu.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by yuddistirakiki on 8/21/17.
 */

public class FCMManager {
    final private static String AGENT_TOPIC = "agent";
    final private static String ALL_TOPIC = "allAndroidApp";

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
        FirebaseMessaging.getInstance().subscribeToTopic(ALL_TOPIC);
    }

}
