package com.sgo.saldomu.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.PinSaver;
import com.venmo.android.pin.util.PinHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/2/2015.
 */
public class CreatePIN extends BaseActivity implements PinFragment.Listener {

    private String mValuePin;
    private String confirmPin;
    private String tokenID;
    private Boolean isRegist = false;
    private Boolean isResetPIN = false;

    private ProgressDialog mProg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        isRegist = i.getBooleanExtra(DefineValue.REGISTRATION, false);
        isResetPIN = i.getBooleanExtra(DefineValue.RESET_PIN, false);
        tokenID = i.getStringExtra(DefineValue.TOKEN_ID);

        InitializeToolbar();

        PinFragmentConfiguration config = new PinFragmentConfiguration(this)
                .pinSaver(new PinSaver() {
                    @Override
                    public void save(String pin) {
                        mValuePin = pin;
                        confirmPin = pin;
                        Timber.d("pin:" + mValuePin);
                        PinHelper.saveDefaultPin(CreatePIN.this, pin);
//                    }
//                }).validator(new Validator() {
//
//                    @Override
//                    public boolean isValid(String input) {
//                        Log.d("input", input);
//                        return PinHelper.doesMatchDefaultPin(getApplicationContext(), input);

                    }
                });

//        Fragment toShow = PinHelper.hasDefaultPinSaved(this) ?
//                PinFragment.newInstanceForVerification(config) :
//                PinFragment.newInstanceForCreation(config);

        Fragment toShow = PinFragment.newInstanceForCreation(config);


        getFragmentManager().beginTransaction()
                .replace(R.id.root, toShow)
                .commit();

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.create_pin;
    }

    private void InitializeToolbar() {
        if (!isRegist) setActionBarIcon(R.drawable.ic_arrow_left);
        else disableHomeIcon();
        setActionBarTitle(getString(R.string.create_pin));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!isRegist) setResult(MainPage.RESULT_LOGOUT);
                else setResult(LoginActivity.RESULT_NORMAL);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidated() {
        Toast.makeText(this, "Validated PIN!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPinCreated() {
        if (!isRegist)
            if (!isResetPIN)
                sendCreatePin();
            else
                sendResetPin();
        else finishChild();
    }

    private void sendCreatePin() {
        try {
            mProg = DefinedDialog.CreateProgressDialog(this, "");
            extraSignature = memberIDLogin + userPhoneID + mValuePin;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_CREATE_PIN, extraSignature);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.COMM_ID, commIDLogin);
            params.put(WebParams.PIN, RSA.opensslEncrypt(mValuePin));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(confirmPin));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params create pin:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CREATE_PIN, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.IS_HAVE_PIN, DefineValue.STRING_YES);
                                mEditor.apply();

                                Toast.makeText(CreatePIN.this, "Success Create PIN", Toast.LENGTH_LONG).show();
                                finishChild();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(CreatePIN.this, message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(CreatePIN.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(CreatePIN.this, model.getError_message());
                            } else {

                                Toast.makeText(CreatePIN.this, message, Toast.LENGTH_LONG).show();
                                recreate();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void sendResetPin() {
        showProgressDialog();

        extraSignature = sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "") + tokenID + mValuePin;

        HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_CONFIRM_RESET_PIN, extraSignature);
        params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(tokenID));
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.NEW_PIN, RSA.opensslEncrypt(mValuePin));
        params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(confirmPin));
        params.put(WebParams.USER_ID, sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""));

        Timber.d("isi param confirm otp reset pin: " + params);
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_RESET_PIN, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        dismissProgressDialog();
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            if (code.equals(WebParams.SUCCESS_CODE)){
                                Toast.makeText(getApplicationContext(),getString(R.string.success_reset_pin),Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(),code,Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        dismissProgressDialog();
                    }

                    @Override
                    public void onComplete() {
                        dismissProgressDialog();
                    }
                });
    }

    private void finishChild() {
        if (!isRegist) {
            setResult(MainPage.RESULT_FIRST_TIME);
        } else {
            Intent i = new Intent();
            i.putExtra(DefineValue.PIN_VALUE, mValuePin);
            i.putExtra(DefineValue.CONF_PIN, confirmPin);

            setResult(LoginActivity.RESULT_FINISHING, i);
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {

        if (!isRegist) {
            setResult(MainPage.RESULT_LOGOUT);
            finish();
        }
    }
}
