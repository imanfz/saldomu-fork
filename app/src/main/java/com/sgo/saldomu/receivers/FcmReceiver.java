package com.sgo.saldomu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.coreclass.BundleToJSON;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fcm.FCMManager;

import org.json.JSONArray;
import org.json.JSONException;

import timber.log.Timber;

/**
 * Created by Lenovo on 16/01/2018.
 */

public class FcmReceiver extends BroadcastReceiver {

    SecurePreferences sp;

    public FcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("FCM Receiver Trigger");
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        if (intent.getAction().equals(DefineValue.INTENT_ACTION_FCM_DATA)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int modelNotif = extras.getInt(DefineValue.MODEL_NOTIF);
                String jsonOptionData = extras.getString(DefineValue.FCM_OPTIONS);


                if (modelNotif == FCMManager.MEMBER_RATING_TRX) {

                    Bundle tempBundle = new Bundle();
                    try {
                        JSONArray jsonOptions = new JSONArray(jsonOptionData);

                        tempBundle.putInt(DefineValue.INDEX, BBSActivity.BBSRATINGBYMEMBER);
                        tempBundle.putString(DefineValue.BBS_TX_ID, jsonOptions.getJSONObject(0).getString(WebParams.TX_ID));
                        tempBundle.putString(DefineValue.CATEGORY_NAME, jsonOptions.getJSONObject(0).getString(WebParams.CATEGORY_NAME));
                        tempBundle.putString(DefineValue.AMOUNT, jsonOptions.getJSONObject(0).getString(WebParams.AMOUNT));
                        tempBundle.putString(DefineValue.URL_PROFILE_PICTURE, jsonOptions.getJSONObject(0).getString(WebParams.PROFILE_PICTURE));
                        tempBundle.putString(DefineValue.BBS_SHOP_NAME, jsonOptions.getJSONObject(0).getString(WebParams.SHOP_NAME));
                        tempBundle.putString(DefineValue.BBS_MAXIMUM_RATING, jsonOptions.getJSONObject(0).getString(WebParams.MAXIMUM_RATING));
                        tempBundle.putString(DefineValue.BBS_DEFAULT_RATING, jsonOptions.getJSONObject(0).getString(WebParams.DEFAULT_RATING));

                        Intent tempIntent = new Intent(context, BBSActivity.class);
                        tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        tempIntent.putExtras(tempBundle);
                        context.startActivity(tempIntent);
                    } catch (JSONException e) {
                        Timber.d("JSONException FCM Receiver OptionData: %s", e.getMessage());
                    }

                } else if (modelNotif == FCMManager.VERIFY_ACC) {
                    if (sp.getBoolean(DefineValue.IS_INQUIRY_SMS, false) == false) {
                        Bundle tempBundle = new Bundle();
                        try {
                            JSONArray jsonOptions = new JSONArray(jsonOptionData);

                            tempBundle.putString(DefineValue.USER_ID, jsonOptions.getJSONObject(0).getString(WebParams.USER_ID));

                            Intent tempIntent = new Intent(context, LoginActivity.class);
                            tempIntent.putExtras(tempBundle);
                            context.startActivity(tempIntent);
                        } catch (JSONException e) {
                            Timber.d("JSONException FCM Receiver OptionData Verifiy Acc: %s", e.getMessage());
                        }
                    }
                }

            }
        }
    }
}
