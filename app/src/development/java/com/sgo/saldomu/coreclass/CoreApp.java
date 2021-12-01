package com.sgo.saldomu.coreclass;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.securities.RSA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
        CustomEncryptedSharedPreferences.initialize(this);
        CustomEncryptedSharedPreferences preferences = CustomEncryptedSharedPreferences.getInstance();
        preferences.putString(DefineValue.GOOGLE_MAPS_KEY, getString(R.string.google_maps_key));
        preferences.putString(DefineValue.GOOGLE_MAPS_KEY_WS, getString(R.string.google_maps_key_ws));
        MyApiClient myApiClient = MyApiClient.Initialize(this);
        setsDefSystemLanguage();

        RealmManager.init(this, R.raw.saldomudevrealm);

//        try {
//            InputStream inputStream = this.getResources().openRawResource(R.raw.public_key);
//            byte[] bytes = new byte[inputStream.available()];
//            inputStream.read(bytes);
//            inputStream.close();
//            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            PublicKey publicKey = keyFactory.generatePublic(spec);
//            RSA.setPublicKey(publicKey);
//            Timber.e("pub key :%s", String.valueOf(RSA.getPublicKey()));
//        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
//            e.printStackTrace();
//        }

        try {
            InputStream inputStream = this.getResources().openRawResource(R.raw.public_key);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            String pubKeyString = byteArrayOutputStream.toString("UTF-8").replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "");

            byte[] encoded = Base64.decode(pubKeyString,Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
            PublicKey publicKey = keyFactory.generatePublic(spec);
            RSA.setPublicKey(publicKey);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

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
