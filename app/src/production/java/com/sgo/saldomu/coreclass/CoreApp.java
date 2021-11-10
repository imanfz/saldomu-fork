package com.sgo.saldomu.coreclass;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.multidex.MultiDex;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

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
        if(BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                }
            });


        set_instance(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseAnalytics.getInstance(this);
//        Stetho.initialize(
//                Stetho.newInitializerBuilder(this)
//                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
//                        .build());

        Iconify.with(new FontAwesomeModule());
        CustomSecurePref.initialize(this);
        CustomEncryptedSharedPreferences.initialize(this);
        CustomEncryptedSharedPreferences preferences = CustomEncryptedSharedPreferences.getInstance();
        preferences.putString(DefineValue.ENCRYPTION_PATTERN, "AES/CBC/PKCS5Padding");
        preferences.putString(DefineValue.GOOGLE_MAPS_KEY, getString(R.string.google_maps_key));
        preferences.putString(DefineValue.GOOGLE_MAPS_KEY_WS, getString(R.string.google_maps_key_ws));
        MyApiClient myApiClient = MyApiClient.Initialize(this);
        setsDefSystemLanguage(null);

        RealmManager.init(this, R.raw.saldomurealm);

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
            MyApiClient.COMM_ID_TAGIH = MyApiClient.COMM_ID_TAGIH_PROD;

        }
        else {
            MyApiClient.COMM_ID = MyApiClient.COMM_ID_DEV;
            MyApiClient.COMM_ID_PULSA = MyApiClient.COMM_ID_PULSA_DEV;
            MyApiClient.URL_FAQ = MyApiClient.URL_FAQ_PROD;
            MyApiClient.URL_TERMS = MyApiClient.URL_TERMS_DEV;
            MyApiClient.COMM_ID_TAGIH = MyApiClient.COMM_ID_TAGIH_DEV;
        }

        myApiClient.InitializeAddress();
//        Timber.wtf("isi headaddressfinal:"+MyApiClient.headaddressfinal);
//        Configuration.Builder configurationBuilder = new Configuration.Builder(getApplicationContext());
//        configurationBuilder.addModelClasses(
//                communityModel.class,
//                friendModel.class,
//                myFriendModel.class,
//                listTimeLineModel.class,
//                listHistoryModel.class,
//                likeModel.class,
//                commentModel.class
//        );
//        ActiveAndroid.initialize(configurationBuilder.create());
        registerActivityLifecycleCallbacks(new LifeCycleHandler(this));

        /*registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(LifeCycleHandler.isApplicationVisible()) {
                    if (action.equalsIgnoreCase("android.intent.action.SIM_STATE_CHANGED")) {
                        if (intent.getStringExtra("ss").equalsIgnoreCase("ABSENT")) {
                            if(EasyPermissions.hasPermissions(context, Manifest.permission.READ_PHONE_STATE)) {
                                if (new SMSclass(CoreApp.this).isSimExists()) {
                                    SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                    SecurePreferences.Editor mEditor = prefs.edit();
                                    mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
                                    mEditor.apply();
                                    Intent i = new Intent(CoreApp.this.getApplicationContext(), ErrorActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    i.putExtra(DefineValue.TYPE, ErrorActivity.SIM_CARD_ABSENT);
                                    CoreApp.this.startActivity(i);
                                }
                            }
                        }
                    }
                }
            }
        },new IntentFilter("android.intent.action.SIM_STATE_CHANGED") );
        */
    }

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }

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
