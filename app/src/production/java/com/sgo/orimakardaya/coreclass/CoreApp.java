package com.sgo.orimakardaya.coreclass;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.sgo.orimakardaya.Beans.BalanceModel;
import com.sgo.orimakardaya.Beans.commentModel;
import com.sgo.orimakardaya.Beans.communityModel;
import com.sgo.orimakardaya.Beans.friendModel;
import com.sgo.orimakardaya.Beans.likeModel;
import com.sgo.orimakardaya.Beans.listHistoryModel;
import com.sgo.orimakardaya.Beans.listTimeLineModel;
import com.sgo.orimakardaya.Beans.myFriendModel;
import com.sgo.orimakardaya.BuildConfig;
import com.sgo.orimakardaya.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

/*
  Created by Administrator on 8/15/2014.
 */
public class CoreApp extends Application {

    private Activity mCurrentActivity = null;
    private static CoreApp _instance;

    private static CoreApp get_instance() {
        return _instance;
    }

    private static void set_instance(CoreApp _instance) {
        CoreApp._instance = _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        if(BuildConfig.DEBUG)
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
        setsDefSystemLanguage(null);
        int checkExistence;

        if(BuildConfig.FLAVOR.equals("development"))
            checkExistence = CoreApp.this.getResources().getIdentifier("akardayadev", "raw", CoreApp.this.getPackageName());
        else
            checkExistence = CoreApp.this.getResources().getIdentifier("akardaya", "raw", CoreApp.this.getPackageName());


        if ( checkExistence != 0 ) {
            Timber.d("test ada raw");
            copyBundledRealmFile(CoreApp.this.getResources().openRawResource(checkExistence), getString(R.string.realmname));
        }
        else {
            Timber.d("test gak ada raw");
            deleteBundledRealmFile(getString(R.string.realmname));
        }

        RealmConfiguration config = new RealmConfiguration.Builder(CoreApp.this)
                .name(getString(R.string.realmname))
                .schemaVersion(getResources().getInteger(R.integer.realscheme))
                .migration(new CustomRealMigration())
                .build();

        Realm.setDefaultConfiguration(config);



        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            DefineValue.VERSION_NAME = pInfo.versionName;
            DefineValue.VERSION_CODE = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(MyApiClient.PROD_FLAG_ADDRESS){
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_PROD;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_PROD;
            MyApiClient.URL_FAQ = MyApiClient.URL_FAQ_PROD;
            MyApiClient.URL_TERMS = MyApiClient.URL_TERMS_PROD;
        }
        else {
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_DEV;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_DEV;
            MyApiClient.URL_FAQ = MyApiClient.URL_FAQ_PROD;
            MyApiClient.URL_TERMS = MyApiClient.URL_TERMS_DEV;
        }

        myApiClient.InitializeAddress();
        Timber.wtf("isi headaddressfinal:"+MyApiClient.headaddressfinal);
        Configuration.Builder configurationBuilder = new Configuration.Builder(getApplicationContext());
        configurationBuilder.addModelClasses(
                communityModel.class,
                friendModel.class,
                myFriendModel.class,
                listTimeLineModel.class,
                listHistoryModel.class,
                likeModel.class,
                commentModel.class,
                BalanceModel.class
        );
        ActiveAndroid.initialize(configurationBuilder.create());
        registerActivityLifecycleCallbacks(new LifeCycleHandler(this));

    }

    private void deleteBundledRealmFile(String outFileName) {
        File file = new File(this.getFilesDir(), outFileName);
        if(file.exists()) {
            if(file.delete())
                Timber.d("delete "+getString(R.string.success));
            else
                Timber.d("delete "+getString(R.string.failed));

        }
    }


    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getFilesDir(), outFileName);
            long sizeraw = inputStream.available();
            long sizefile = 0;
            if(file.exists()) {
                sizefile = file.length();
                Timber.d("sizeRaw / sizeFile = "+ String.valueOf(sizeraw)+" / "+String.valueOf(sizefile));
            }

            if(sizeraw != sizefile) {
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
                outputStream.close();
                Timber.d("file baru dicopy");
                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timber.d("file tidak dicopy");
        return null;
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

        DefineValue.sDefSystemLanguage = "in";

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
