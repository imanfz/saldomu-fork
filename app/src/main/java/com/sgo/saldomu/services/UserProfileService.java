package com.sgo.saldomu.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/12/2016.
 */
public class UserProfileService extends Service {

    private final IBinder testBinder = new MyLocalBinder();
    private boolean isServiceDestroyed;
    private Activity mainPageContext = null;
    private static final long LOOPING_TIME = 5000;

    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        }
    }

    private MyHandler mHandler = new MyHandler();

    private Runnable callUserProfile = () -> {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        if(mainPageContext != null) {
//                UserProfileHandler mBH = new UserProfileHandler(mainPageContext);
//                mBH.sentUserProfile();
        }
        Timber.i("Service jalankan call UserProfile Service");
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("Masuk onCreate call UserProfile Service");
        setServiceDestroyed(false);
        mHandler.removeCallbacks(callUserProfile);
        mHandler.postDelayed(callUserProfile, LOOPING_TIME);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.i("Masuk onBind Service UserProfile");
        return testBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("Masuk onStartCommand");
        return START_STICKY;
    }

    public boolean isServiceDestroyed() {
        return isServiceDestroyed;
    }

    private void setServiceDestroyed(boolean isServiceDestroyed) {
        this.isServiceDestroyed = isServiceDestroyed;
    }

    public class MyLocalBinder extends Binder {
        public UserProfileService getService() {
            return UserProfileService.this;
        }
    }

    public void setMainPageContext(Activity _context){
        mainPageContext = _context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("Masuk onDestroy UserProfile Service");
        setServiceDestroyed(true);
        mHandler.removeCallbacks(callUserProfile);
    }

}
