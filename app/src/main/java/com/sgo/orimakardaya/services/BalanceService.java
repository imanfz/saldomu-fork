package com.sgo.orimakardaya.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.Log;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.coreclass.BalanceHandler;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.NotificationHandler;
import timber.log.Timber;

/*
  Created by Administrator on 1/13/2015.
 */
public class BalanceService extends Service {

    private final IBinder testBinder = new MyLocalBinder();
    private boolean isServiceDestroyed;
    private Context mainPageContext;


    public static final long LOOPING_TIME_BALANCE =  80000; // 30 detik = 30 * 1000 ms
    public static final long LOOPING_TIME_NOTIF   = 120000;

    //public static final long LOOPING_TIME_BALANCE = 200000; // 30 detik = 30 * 1000 ms
    //public static final long LOOPING_TIME_NOTIF = 150000;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
        }
    };

    private Runnable callBalance = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
            BalanceHandler mBH = new BalanceHandler(mainPageContext, sp);
            mBH.sentData();
            Timber.i("Service jalankan callBalance");
            if(!isServiceDestroyed)mHandler.postDelayed(this, LOOPING_TIME_BALANCE);
        }
    };


    private Runnable callNotif = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
            NotificationHandler mNH = new NotificationHandler(mainPageContext, sp);
            mNH.sentRetrieveNotif();
            Timber.i("Service jalankan callNotif");
            if(!isServiceDestroyed)mHandler.postDelayed(this, LOOPING_TIME_NOTIF);
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

    public void setMainPageContext(Context _context){
        mainPageContext = _context;
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
