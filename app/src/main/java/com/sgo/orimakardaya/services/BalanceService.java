package com.sgo.orimakardaya.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;

import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.BalanceModel;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.NotificationHandler;
import com.sgo.orimakardaya.interfaces.OnLoadDataListener;
import com.sgo.orimakardaya.loader.UtilsLoader;

import timber.log.Timber;

/*
  Created by Administrator on 1/13/2015.
 */
public class BalanceService extends Service {

    public static final String INTENT_ACTION_BALANCE = "com.sgo.orimakardaya.INTENT_ACTION_BALANCE";

    private final IBinder testBinder = new MyLocalBinder();
    private boolean isServiceDestroyed;
    private Activity mainPageContext = null;
    private Messenger messenger;

    public static final long LOOPING_TIME_BALANCE =  50000; // 30 detik = 30 * 1000 ms
    public static final long LOOPING_TIME_NOTIF   = 120000;
    private SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
    private UtilsLoader mBl;

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
                    Message msg = Message.obtain();
                    msg.obj = deData;
                    msg.arg1 = 0;
                    Intent i = new Intent(INTENT_ACTION_BALANCE);
                    BalanceModel mObj = (BalanceModel) deData;

                    i.putExtra(BalanceModel.BALANCE_PARCELABLE, mObj);
                    try {
                        messenger.send(msg);
                        LocalBroadcastManager.getInstance(BalanceService.this)
                                .sendBroadcast(i);
                    } catch (android.os.RemoteException e1) {
                        Timber.w(getClass().getName(), "Exception sending message", e1);
                    }
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

        if (extras!=null) {
            messenger=(Messenger)extras.get(DefineValue.DATA);
        }
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
