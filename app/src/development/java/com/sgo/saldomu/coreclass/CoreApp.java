package com.sgo.saldomu.coreclass;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import timber.log.Timber;

//import com.facebook.stetho.Stetho;

/*
  Created by Administrator on 8/15/2014.
 */
public class CoreApp extends MultiDexApplication {

    private Activity mCurrentActivity = null;
    private static CoreApp _instance;

    private static CoreApp get_instance() {
        return _instance;
    }

    private static void set_instance(CoreApp _instance) {
        CoreApp._instance = _instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                }
            });


        set_instance(this);


        Iconify.with(new FontAwesomeModule());
        CustomSecurePref.initialize(this);
        MyApiClient myApiClient = MyApiClient.Initialize(this);
        setsDefSystemLanguage();

        RealmManager.init(this, R.raw.saldomudevrealm);

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            DefineValue.VERSION_NAME = pInfo.versionName;
            DefineValue.VERSION_CODE = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (MyApiClient.PROD_FLAG_ADDRESS) {
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_PROD;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_PROD;
            MyApiClient.COMM_ID_TAGIH = MyApiClient.COMM_ID_TAGIH_PROD;
            MyApiClient.URL_FAQ = MyApiClient.URL_FAQ_PROD;
            MyApiClient.URL_TERMS = MyApiClient.URL_TERMS_PROD;
        } else {
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_DEV;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_DEV;
            MyApiClient.COMM_ID_TAGIH = MyApiClient.COMM_ID_TAGIH_DEV;
            MyApiClient.URL_FAQ = MyApiClient.URL_FAQ_DEV;
            MyApiClient.URL_TERMS = MyApiClient.URL_TERMS_DEV;
        }

        myApiClient.InitializeAddress();
        registerActivityLifecycleCallbacks(new LifeCycleHandler(this));
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setsDefSystemLanguage();
    }

    private void setsDefSystemLanguage() {
        DefineValue.sDefSystemLanguage = "in";
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        RetrofitService.dispose();
    }

    public static Context getAppContext() {
        return get_instance().getApplicationContext();
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

}
