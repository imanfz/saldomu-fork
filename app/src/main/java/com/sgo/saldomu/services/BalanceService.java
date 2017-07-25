package com.sgo.saldomu.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.NotificationHandler;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;

import timber.log.Timber;

/*
  Created by Administrator on 1/13/2015.
 */
public class BalanceService extends Service {

    public static final String INTENT_ACTION_BALANCE = "com.sgo.orimakardaya.INTENT_ACTION_BALANCE";

    private final IBinder testBinder = new MyLocalBinder();
    private boolean isServiceDestroyed;
    private Activity mainPageContext = null;

    private static final long LOOPING_TIME_BALANCE =  50000; // 30 detik = 30 * 1000 ms
    private static final long LOOPING_TIME_NOTIF   = 120000;
    private SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
    private UtilsLoader mBl;
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        }
    }

    MyHandler mHandler = new MyHandler();

    private Runnable callBalance = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if(mainPageContext != null) {
                runBalance();
            }
            Timber.i("Service jalankan callBalance");
            if(!isServiceDestroyed)mHandler.postDelayed(this, LOOPING_TIME_BALANCE);
        }
    };

    public void runBalance(){
        if(!isServiceDestroyed()) {
            mBl.getDataBalance(true,new OnLoadDataListener() {
                @Override
                public void onSuccess(Object deData) {
                    Timber.d("runBalance service onsuccess");
                }

                @Override
                public void onFail(String message) {

                }

                @Override
                public void onFailure() {

                }
            });
        }
    }


    private Runnable callNotif = new Runnable() {
        @Override
        public void run() {
            if(!isServiceDestroyed()) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
                NotificationHandler mNH = new NotificationHandler(mainPageContext, sp);
                mNH.sentRetrieveNotif();
                Timber.i("Service jalankan callNotif");
                if (!isServiceDestroyed) mHandler.postDelayed(this, LOOPING_TIME_NOTIF);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("Masuk onCreate BalanceService");
        setServiceDestroyed(false);
        mHandler.removeCallbacks(callNotif);
        mHandler.removeCallbacks(callBalance);
        mHandler.postDelayed(callNotif,LOOPING_TIME_NOTIF);
        mHandler.postDelayed(callBalance, LOOPING_TIME_BALANCE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.i("Masuk onBind Service");
        Bundle extras=intent.getExtras();

//        if (extras!=null) {
//            messenger=(Messenger)extras.get(DefineValue.DATA);
//        }
        return testBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("Masuk onStartCommand");
        return START_STICKY;
    }

    private boolean isServiceDestroyed() {
        return isServiceDestroyed;
    }

    private void setServiceDestroyed(boolean isServiceDestroyed) {
        this.isServiceDestroyed = isServiceDestroyed;
    }

    public class MyLocalBinder extends Binder {
        public BalanceService getService() {
            return BalanceService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("Masuk onDestroy Balance Service");
        setServiceDestroyed(true);
        mHandler.removeCallbacks(callBalance);
        mHandler.removeCallbacks(callNotif);
    }

    public void setMainPageContext(Activity _context){
        mainPageContext = _context;
        mBl = new UtilsLoader(mainPageContext,sp);
    }

    public void StopCallBalance(){
        mHandler.removeCallbacks(callBalance);
        mHandler.removeCallbacks(callNotif);
    }

    public void StartCallBalance(){
        mHandler.removeCallbacks(callNotif);
        mHandler.removeCallbacks(callBalance);
        mHandler.postDelayed(callBalance, LOOPING_TIME_BALANCE);
        mHandler.postDelayed(callNotif, LOOPING_TIME_NOTIF);
    }

}
