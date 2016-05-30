package com.sgo.orimakardaya.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 5/27/16.
 */
public class SimChangedReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_SIM_ABSENT = "com.sgo.orimakardaya.INTENT_ACTION_SIM_ABSENT";
    public static final IntentFilter filter = new IntentFilter(SimChangedReceiver.INTENT_ACTION_SIM_ABSENT);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
           if(intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")){
               Intent i = new Intent(INTENT_ACTION_SIM_ABSENT);
               LocalBroadcastManager.getInstance(context).sendBroadcast(i);
           }

        }
    }
}
