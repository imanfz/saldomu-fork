package com.sgo.orimakardaya.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.widget.Toast;

import com.sgo.orimakardaya.coreclass.UserProfileHandler;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/12/2016.
 */
public class UserProfileService extends Service {

    private final IBinder testBinder = new MyLocalBinder();
    private boolean isServiceDestroyed;
    private Activity mainPageContext = null;
    public static final long LOOPING_TIME = 5000;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
        }
    };

    private Runnable callUserProfile = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if(mainPageContext != null) {
                UserProfileHandler mBH = new UserProfileHandler(mainPageContext);
                mBH.sentUserProfile();
            }
            Timber.i("Service jalankan call UserProfile Service");
        }
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

    public void setServiceDestroyed(boolean isServiceDestroyed) {
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
