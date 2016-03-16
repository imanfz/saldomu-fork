package com.sgo.orimakardaya.coreclass;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.sgo.orimakardaya.Beans.*;
import com.sgo.orimakardaya.BuildConfig;

import java.util.Locale;

import timber.log.Timber;

/*
  Created by Administrator on 8/15/2014.
 */
public class CoreApp extends Application {

    public Activity mCurrentActivity = null;
    private static CoreApp _instance;

    public static CoreApp get_instance() {
        return _instance;
    }

    public static void set_instance(CoreApp _instance) {
        CoreApp._instance = _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.

        set_instance(this);
        if(BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                }
            });

        Iconify.with(new FontAwesomeModule());
        CustomSecurePref.initialize(this);
        MyApiClient.initialize(this);
        setsDefSystemLanguage(null);

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            DefineValue.VERSION_NAME = pInfo.versionName;
            DefineValue.VERSION_CODE = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        MyApiClient.initializeAddress();
        Timber.wtf("isi headaddressfinal:"+MyApiClient.headaddressfinal);

        if(MyApiClient.PROD_FLAG_ADDRESS){
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_PROD;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_PROD;
        }
        else {
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_DEV;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_DEV;
        }

        Configuration.Builder configurationBuilder = new Configuration.Builder(getApplicationContext());
        configurationBuilder.addModelClasses(
                communityModel.class,
                friendModel.class,
                myFriendModel.class,
                listTimeLineModel.class,
                listHistoryModel.class,
                likeModel.class,
                commentModel.class
        );
        ActiveAndroid.initialize(configurationBuilder.create());
        registerActivityLifecycleCallbacks(new LifeCycleHandler(this));
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setsDefSystemLanguage(newConfig);
    }

    private void setsDefSystemLanguage (android.content.res.Configuration newConfig){

        String delanguage ;
        if(newConfig == null){
            delanguage = Locale.getDefault().getLanguage();
        }
        else {
            delanguage = newConfig.locale.getLanguage();
        }

        Timber.d("isi delanguage system:"+delanguage);
        if(delanguage.equals("en")) {
           DefineValue.sDefSystemLanguage = delanguage.toUpperCase();
        }
        else DefineValue.sDefSystemLanguage = "ID";

    }

	
	@Override
    public void onTerminate() {
        super.onTerminate();
        MyApiClient.CancelRequestWS(this, true);
        ActiveAndroid.dispose();
    }

    public static Context getAppContext(){
        return get_instance().getApplicationContext();
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }



}
