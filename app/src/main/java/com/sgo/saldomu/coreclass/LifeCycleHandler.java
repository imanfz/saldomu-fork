package com.sgo.saldomu.coreclass;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Locale;

import timber.log.Timber;

/*
  Created by Administrator on 8/15/2014.
 */
public class LifeCycleHandler implements Application.ActivityLifecycleCallbacks {

    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    private CoreApp mApp;

    public LifeCycleHandler(Application app){
        mApp = (CoreApp) app;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
       Timber.w("application is in created");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
        mApp.setCurrentActivity(activity);
        DefineValue.language = Locale.getDefault().getLanguage();
        if(!DefineValue.language.equals("en")) DefineValue.language = "id";
        Timber.w("application is in started ");

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
        mApp.setCurrentActivity(activity);
        Timber.w("isi lang : " + DefineValue.language + "/ application is in resume: " + (resumed > paused));
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        mApp.setCurrentActivity(null);
        Timber.w("application is in foreground: %s", (resumed > paused));
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        mApp.setCurrentActivity(null);
        Timber.w("application is visible: %s", (started > stopped));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Timber.w("application is in saveInstance" );
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mApp.setCurrentActivity(null);
        Timber.w("application is in destroyed");
    }

    public static boolean isApplicationVisible() {
        return started > stopped;
    }

}
