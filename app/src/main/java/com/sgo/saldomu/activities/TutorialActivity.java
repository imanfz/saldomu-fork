        package com.sgo.saldomu.activities;

        import android.os.Bundle;
        import android.os.PersistableBundle;
        import android.support.annotation.Nullable;
        import android.widget.Button;

        import com.github.paolorotolo.appintro.AppIntro;
        import com.securepreferences.SecurePreferences;
        import com.sgo.saldomu.R;
        import com.sgo.saldomu.coreclass.CustomSecurePref;
        import com.sgo.saldomu.coreclass.DefineValue;
        import com.sgo.saldomu.fragments.IntroPage;

/**
 * Created by Lenovo Thinkpad on 7/20/2017.
 */

public class TutorialActivity extends AppIntro {
    private int intType;
    public static final int tutorial_cash_in=1;
    public static final int tutorial_cash_out=2;
    public static final int tutorial_registerAgen=3;
    public static final int tutorial_tambahRekening=4;
    public static final int tutorial_konfirmasi_cashout_bbs = 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        intType = getIntent().getIntExtra(DefineValue.TYPE,0);
        if(intType==0)
        {
            this.finish();
        }
        else if (intType==tutorial_cash_in)
        {
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_in_1));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_in_2));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_in_3));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_in_4));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_in_5));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_in_6));

            setFlowAnimation();
            Button skipbtn = (Button)skipButton;
            Button donebtn = (Button)doneButton;
            skipbtn.setText(getString(R.string.start_now));
            donebtn.setText(getString(R.string.done));
        }else if (intType==tutorial_cash_out)
        {
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_1));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_2));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_3));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_4));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_5));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_6));
            addSlide(IntroPage.newInstance(R.layout.tutorial_cash_out_7));

            setFlowAnimation();
            Button skipbtn = (Button)skipButton;
            Button donebtn = (Button)doneButton;
            skipbtn.setText(getString(R.string.start_now));
            donebtn.setText(getString(R.string.done));
        }
        else if (intType==tutorial_registerAgen)
        {
            addSlide(IntroPage.newInstance(R.layout.tutorial_register_agen));

            setFlowAnimation();
            Button skipbtn = (Button)skipButton;
            Button donebtn = (Button)doneButton;
            skipbtn.setText(getString(R.string.start_now));
            donebtn.setText(getString(R.string.done));
        }
        else if (intType==tutorial_tambahRekening)
        {
            addSlide(IntroPage.newInstance(R.layout.tutorial_tambah_rekening_1));
            addSlide(IntroPage.newInstance(R.layout.tutorial_tambah_rekening_2));
            addSlide(IntroPage.newInstance(R.layout.tutorial_tambah_rekening_3));

            setFlowAnimation();
            Button skipbtn = (Button)skipButton;
            Button donebtn = (Button)doneButton;
            skipbtn.setText(getString(R.string.start_now));
            donebtn.setText(getString(R.string.done));
        }
        else if (intType==tutorial_konfirmasi_cashout_bbs)
        {
            addSlide(IntroPage.newInstance(R.layout.tutorial_konfirmasi_cashout_1));
            addSlide(IntroPage.newInstance(R.layout.tutorial_konfirmasi_cashout_2));
            addSlide(IntroPage.newInstance(R.layout.tutorial_konfirmasi_cashout_3));

            setFlowAnimation();
            Button skipbtn = (Button)skipButton;
            Button donebtn = (Button)doneButton;
            skipbtn.setText(getString(R.string.start_now));
            donebtn.setText(getString(R.string.done));
        }
    }

    @Override
    public void onSkipPressed() {
        show();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        show();
    }

    @Override
    public void onSlideChanged() {

    }
    private void show(){
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor mEditor=sp.edit();

        if (intType==tutorial_cash_in)
        {
            mEditor.putBoolean(DefineValue.TUTORIAL_CASHIN,false);
        }
        else if (intType==tutorial_cash_out)
        {
            mEditor.putBoolean(DefineValue.TUTORIAL_CASHOUT,false);
        }
        else if (intType==tutorial_registerAgen)
        {
            mEditor.putBoolean(DefineValue.TUTORIAL_REGISTER_AGEN,false);
        }
        else if (intType==tutorial_tambahRekening)
        {
            mEditor.putBoolean(DefineValue.TUTORIAL_TAMBAH_REKENING,false);
        }
        else if (intType==tutorial_konfirmasi_cashout_bbs)
        {
            mEditor.putBoolean(DefineValue.TUTORIAL_KONFIRMASI_CASHOUT_BBS ,false);
        }
        mEditor.apply();
        finish();
    }
}
