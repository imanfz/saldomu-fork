package com.sgo.saldomu.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import com.sgo.saldomu.loader.UtilsLoader;

import timber.log.Timber;

/*
  Created by Administrator on 1/13/2015.
 */
public class AppInfoService extends Service {

    private final IBinder testBinder = new MyLocalBinder();
    private Activity mainPageContext = null;
    private UtilsLoader utilsLoader;

    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        }
    }

    private MyHandler mHandler = new MyHandler();

    private Runnable callBalance = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if(mainPageContext != null) {
                if(utilsLoader == null)
                    utilsLoader = new UtilsLoader(mainPageContext);
//                utilsLoader.getAppVersion();
            }
            Timber.i("Service jalankan call AppInfo Service");
//            if(!isServiceDestroyed)mHandler.postDelayed(this, LOOPING_TIME);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("Masuk onCreate call AppInfo Service");
        mHandler.removeCallbacks(callBalance);
//        mHandler.postDelayed(callBalance, LOOPING_TIME);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.i("Masuk onBind Service App Info");
        return testBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("Masuk onStartCommand");
        return START_STICKY;
    }

    public class MyLocalBinder extends Binder {
        public AppInfoService getService() {
            return AppInfoService.this;
        }
    }

    public void setMainPageContext(Activity _context){
        mainPageContext = _context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("Masuk onDestroy App Info Service");
        mHandler.removeCallbacks(callBalance);
    }

    public void StopCallAppInfo(){
        mHandler.removeCallbacks(callBalance);
    }

    public void StartCallAppInfo(){
//        mHandler.postDelayed(callBalance, LOOPING_TIME);
    }

}
