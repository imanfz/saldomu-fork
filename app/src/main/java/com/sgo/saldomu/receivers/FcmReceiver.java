package com.sgo.saldomu.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fcm.FCMManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import static com.facebook.stetho.inspector.network.PrettyPrinterDisplayType.JSON;

/**
 * Created by Lenovo on 16/01/2018.
 */

public class FcmReceiver extends BroadcastReceiver {
    public FcmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent.getAction().equals(DefineValue.INTENT_ACTION_FCM_DATA) ) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                int modelNotif          = extras.getInt(DefineValue.MODEL_NOTIF);
                String jsonOptionData   = extras.getString(DefineValue.FCM_OPTIONS);



                if ( modelNotif == FCMManager.MEMBER_RATING_TRX ) {
                    try {
                        JSONObject jsonOptions   = new JSONObject(jsonOptionData);

                        Intent tempIntent = new Intent(context, BBSActivity.class);
                        Bundle tempBundle = new Bundle();
                        tempBundle.putInt(DefineValue.INDEX, BBSActivity.BBSRATINGBYMEMBER);
                        tempBundle.putString(DefineValue.BBS_TX_ID, jsonOptions.getString(WebParams.TX_ID));
                        tempBundle.putString(DefineValue.CATEGORY_NAME, jsonOptions.getString(WebParams.CATEGORY_NAME));
                        tempBundle.putString(DefineValue.AMOUNT, jsonOptions.getString(WebParams.AMOUNT));
                        tempBundle.putString(DefineValue.URL_PROFILE_PICTURE, jsonOptions.getString(WebParams.PROFILE_PICTURE));
                        tempBundle.putString(DefineValue.BBS_SHOP_NAME, jsonOptions.getString(WebParams.SHOP_NAME));
                        tempBundle.putString(DefineValue.BBS_MAXIMUM_RATING, jsonOptions.getString(WebParams.MAXIMUM_RATING));
                        tempBundle.putString(DefineValue.BBS_DEFAULT_RATING, jsonOptions.getString(WebParams.DEFAULT_RATING));
                        tempIntent.putExtras(tempBundle);
                        context.startActivity(tempIntent);
                    } catch (JSONException e) {
                        Timber.d("JSONException FCM Receiver OptionData: "+e.getMessage());
                    }

                }

            }
        }
    }
}
