package com.sgo.saldomu.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/15/17.
 */

public class FireBaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = null;
        try {
            refreshedToken = FirebaseInstanceId.getInstance().getToken("451695092641","FCM");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
