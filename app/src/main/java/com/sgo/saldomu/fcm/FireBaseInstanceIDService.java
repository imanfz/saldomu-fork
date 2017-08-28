package com.sgo.saldomu.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DeviceUtils;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.SmartResponseHandler;
import com.sgo.saldomu.coreclass.WebParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/15/17.
 */

public class FireBaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        Timber.d("Refreshed token: ");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.d("Refreshed token: " + refreshedToken);
        sentTokenToServer();
    }

    void sentTokenToServer() {
        FCMWebServiceLoader.getInstance(this, new FCMWebServiceLoader.LoaderListener() {
            @Override
            public void onSuccessLoader() {
                FCMManager.subscribeAll();
            }

            @Override
            public void onFailedLoader() {

            }
        }).sentTokenToServer(true);
    }
}
