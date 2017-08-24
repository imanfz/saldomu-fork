package com.sgo.saldomu.fcm;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by yuddistirakiki on 8/21/17.
 */

public class FCMManager {
    final static String AGENT_TOPIC = "agent";


    public static void subscribeAgent(){
        FirebaseMessaging.getInstance().subscribeToTopic(AGENT_TOPIC);
    }

    public static void unsubscribeAgent(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(AGENT_TOPIC);
    }
}
