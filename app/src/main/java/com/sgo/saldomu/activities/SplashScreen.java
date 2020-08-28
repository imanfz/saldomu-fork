package com.sgo.saldomu.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.github.paolorotolo.appintro.AppIntro;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.fragments.IntroPage;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.utils.LocaleManager;

import timber.log.Timber;

public class SplashScreen extends AppIntro {
    private SecurePreferences sp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        if (InetHandler.isNetworkAvailable(this))
            new UtilsLoader(this).getAppVersion();

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        addSlide(IntroPage.newInstance(R.layout.splash_screen_1));
        addSlide(IntroPage.newInstance(R.layout.splash_screen_2));
        addSlide(IntroPage.newInstance(R.layout.splash_screen_3));
        addSlide(IntroPage.newInstance(R.layout.splash_screen_4));
        addSlide(IntroPage.newInstance(R.layout.splash_screen_5));

        setFlowAnimation();
        Button skipbtn = (Button) skipButton;
        Button donebtn = (Button) doneButton;
        skipbtn.setText(getString(R.string.skip));
        donebtn.setText(getString(R.string.start_now));
//        skipbtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
//        donebtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        donebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SplashScreen.this, Perkenalan.class);
                startActivity(i);
                SplashScreen.this.finish();
            }
        });
    }

    @Override
    public void onSkipPressed() {
        Intent i = new Intent(SplashScreen.this, Perkenalan.class);
        startActivity(i);
        SplashScreen.this.finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {

    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Timber.d("Logging attachBaseContext.....");
        //Timber.d("is Firt time : " + sp.getString(DefineValue.IS_FIRST, ""));

//        if (sp.getString(DefineValue.IS_FIRST, "").equalsIgnoreCase(DefineValue.YES)) {
//            if (LocaleManager.getLocale(getResources()).getLanguage().equals("id")) {
//                CustomSecurePref.getInstance().setBoolean(DefineValue.IS_BAHASA, true);
//            }
//        }

        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }
}
