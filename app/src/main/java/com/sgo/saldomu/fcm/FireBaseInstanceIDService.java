package com.sgo.saldomu.fcm;

/**
 * Created by yuddistirakiki on 8/15/17.
 */

//public class FireBaseInstanceIDService extends FirebaseInstanceIdService {
//
//    @Override
//    public void onTokenRefresh() {
//        String refreshedToken = null;
//            refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        Timber.d("Refreshed token: " + refreshedToken);
//        sentTokenToServer();
//    }
//
//    void sentTokenToServer() {
//        FCMWebServiceLoader.getInstance(this, new FCMWebServiceLoader.LoaderListener() {
//            @Override
//            public void onSuccessLoader() {
//                FCMManager.subscribeAll();
//            }
//
//            @Override
//            public void onFailedLoader() {
//
//            }
//        }).sentTokenToServer(true);
//    }
//}
