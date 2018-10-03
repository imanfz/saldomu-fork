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
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.PinSaver;
import com.venmo.android.pin.util.PinHelper;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/2/2015.
 */
public class CreatePIN extends BaseActivity implements PinFragment.Listener {

    private String mValuePin;
    private String confirmPin;
    private Boolean isRegist=false;

    private ProgressDialog mProg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        isRegist = i.getBooleanExtra(DefineValue.REGISTRATION, false);

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

    private void InitializeToolbar(){
        if(!isRegist) setActionBarIcon(R.drawable.ic_arrow_left);
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
                if(!isRegist) setResult(MainPage.RESULT_LOGOUT);
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
        if(!isRegist) sendCreatePin();
        else finishChild();
    }

    private void sendCreatePin() {
        try{
            mProg = DefinedDialog.CreateProgressDialog(this, "");
            extraSignature = memberIDLogin + userPhoneID + mValuePin;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_CREATE_PIN, extraSignature);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.COMM_ID, commIDLogin);
            params.put(WebParams.PIN, RSA.opensslEncrypt(mValuePin));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(confirmPin));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params create pin:"+params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CREATE_PIN, params,
                    new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            mProg.dismiss();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.IS_HAVE_PIN, DefineValue.STRING_YES);
                                mEditor.apply();

                                Toast.makeText(CreatePIN.this, "Success Create PIN", Toast.LENGTH_LONG).show();
                                finishChild();
                            }
                            else if(code.equals(WebParams.LOGOUT_CODE)){
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(CreatePIN.this,message);
                            }
                            else {
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(CreatePIN.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(CreatePIN.this, message, Toast.LENGTH_LONG).show();
                                recreate();
                            }

                            if(mProg.isShowing())
                                mProg.dismiss();
                        }
                    });
        }
        catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void finishChild(){
        if(!isRegist) {
            setResult(MainPage.RESULT_FIRST_TIME);
        }
        else {
            Intent i = new Intent();
            i.putExtra(DefineValue.PIN_VALUE,mValuePin);
            i.putExtra(DefineValue.CONF_PIN,confirmPin);

            setResult(LoginActivity.RESULT_FINISHING, i);
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {

        if(!isRegist) {
            setResult(MainPage.RESULT_LOGOUT);
            finish();
        }
    }
}
