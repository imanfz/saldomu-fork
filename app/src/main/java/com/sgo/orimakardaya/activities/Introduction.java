package com.sgo.orimakardaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.github.paolorotolo.appintro.AppIntro;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.fragments.IntroPage;


public class Introduction extends AppIntro {
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(IntroPage.newInstance(R.layout.intro1_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro2_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro3_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro4_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro5_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro6_fragment));

        setFlowAnimation();
        Button skipbtn = (Button)skipButton;
        Button donebtn = (Button)doneButton;
        skipbtn.setText(getString(R.string.start_now));
        donebtn.setText(getString(R.string.done));
    }

    @Override
    public void onSkipPressed() {
        openLogin();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        openLogin();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onSlideChanged() {

    }

    public void openLogin(){
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
        this.finish();
    }
}
