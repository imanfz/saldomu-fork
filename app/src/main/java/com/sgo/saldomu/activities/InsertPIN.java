package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.FingerprintDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.widgets.KeyboardPin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/2/2015.
 */
public class InsertPIN extends BaseActivity implements KeyboardPin.KeyboardPinListener {

    public static final int RESULT_PIN_VALUE = 302;
    public static final int RESULT_CANCEL_ORDER = 303;
    public static final int RESULT_FINGERPRINT_LOGIN = 304;

    SecurePreferences sp;
    String valuePin;
    Boolean IsForgotPassword;
    TextView tv_attempt, tv_version;
    FingerprintManager fingerprintManager;
    KeyboardPin keyboardPin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        View v = this.findViewById(android.R.id.content);
        if (v != null) {
            tv_attempt = v.findViewById(R.id.pin_tries_value);
            tv_version = v.findViewById(R.id.tv_version);
        }
        keyboardPin = findViewById(R.id.keyboard);

        Timber.d("masuk UtilsLoader");
        String userId = sp.getString(DefineValue.USERID_PHONE, "");
        if (userId.isEmpty()) {
            userId = sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "");
            if (userId.isEmpty()) {
                userId = getIntent().getStringExtra(DefineValue.USERID_PHONE);
            }
        }

        IsForgotPassword = getIntent().getBooleanExtra(DefineValue.IS_FORGOT_PASSWORD, false);
        if (getIntent().getBooleanExtra(DefineValue.NOT_YET_LOGIN, false) && !IsForgotPassword && !sp.getString(DefineValue.USER_PASSWORD, "").equals("")) {
            tv_version.setText(getString(R.string.appname) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
            if (!sp.getString(DefineValue.USER_PASSWORD, "").equals("") && !sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "").isEmpty())
                showDialogFingerprint();
        } else {
            keyboardPin.hideFingerprint();
        }

        new UtilsLoader(this, sp).getFailedPINNo(userId, new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {
                String _dedata = String.valueOf(deData);
                setTextAttempt(_dedata);
            }

            @Override
            public void onFail(Bundle message) {

            }

            @Override
            public void onFailure(String message) {

            }
        });

        initializeToolbar();

        final int attempt = getIntent().getIntExtra(DefineValue.ATTEMPT, 0);

        if (attempt != 0) {
            setTextAttempt(String.valueOf(attempt));
        }

        keyboardPin.setListener(this);
    }

    private void showDialogFingerprint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            if (fingerprintManager != null) {
                try {

                    if ((fingerprintManager.isHardwareDetected() ||
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED)
                            || fingerprintManager.hasEnrolledFingerprints()) && sp.getString(DefineValue.USER_PASSWORD, "") != null) {

                        FingerprintDialog fingerprintDialog = FingerprintDialog.newDialog(result -> {
                            if (result) {
                                setResult(RESULT_FINGERPRINT_LOGIN);
                                finish();
                            }
                        });
                        fingerprintDialog.setCancelable(false);
                        fingerprintDialog.show(getSupportFragmentManager(), "FingerprintDialog");
                    } else {
                        keyboardPin.hideFingerprint();
                    }
                } catch (NullPointerException e) {
                    Timber.e(e);
                }
            } else {
                keyboardPin.hideFingerprint();
            }
        }
    }

    private void setTextAttempt(String attempt) {
        if (attempt == null || attempt.isEmpty())
            attempt = "0";

        String attempt_text = getString(R.string.login_failed_attempt_1, Integer.valueOf(attempt));
        tv_attempt.setText(attempt_text);
        if (attempt.equalsIgnoreCase("1"))
            tv_attempt.setVisibility(View.VISIBLE);
        else
            tv_attempt.setVisibility(View.GONE);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.insert_pin;
    }

    public void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.input_pin));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (IsForgotPassword)
            getMenuInflater().inflate(R.menu.forgot_pin, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCEL_ORDER);
                finish();
                return true;
            case R.id.action_forgot_pin:
//                showDialogForgotPin();
                Intent i = new Intent(getApplicationContext(), ForgotPin.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(RESULT_CANCEL_ORDER);
        finish();
    }

    @Override
    public void getCharSequenceKeyboard(CharSequence text) {
        if (text.length() == 6) {
            valuePin = text.toString();
            Intent i = new Intent();
            i.putExtra(DefineValue.PIN_VALUE, valuePin);
            setResult(RESULT_PIN_VALUE, i);
            finish();
        }
    }

    @Override
    public void useFingerprint() {
        showDialogFingerprint();
    }
}
