package com.sgo.saldomu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.activities.ErrorActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LifeCycleHandler;
import com.sgo.saldomu.coreclass.SMSclass;

import timber.log.Timber;

/**
 * Created by Lenovo on 07/03/2018.
 */

public class SimStateChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("simStateChangedReceiver Triggered");
        String action = intent.getAction();
        if(LifeCycleHandler.isApplicationVisible()) {
            if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
                if (intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")) {
                    if(new SMSclass(context).isSimExists()) {
                        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                        SecurePreferences.Editor mEditor = prefs.edit();
                        mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
                        mEditor.apply();
                        Intent i = new Intent(context.getApplicationContext(), ErrorActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        i.putExtra(DefineValue.TYPE, ErrorActivity.SIM_CARD_ABSENT);
                        context.startActivity(i);
                    }
                }
            }
        }
    }
}
