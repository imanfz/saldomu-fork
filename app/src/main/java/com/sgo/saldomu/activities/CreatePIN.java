package com.sgo.saldomu.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.*;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.securities.Md5;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.PinSaver;
import com.venmo.android.pin.util.PinHelper;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/2/2015.
 */
public class CreatePIN extends BaseActivity implements PinFragment.Listener {

    private SecurePreferences sp;
    private String mValuePin;
    private String memberID;
    private String commID;
    private String confirmPin;
    private String userID;
    private String accessKey;
    private Boolean isRegist=false;

    private ProgressDialog mProg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        memberID = sp.getString(DefineValue.MEMBER_ID,"");
        commID = sp.getString(DefineValue.COMMUNITY_ID,"");
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

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

//            RequestParams params = MyApiClient.getSignatureWithParams(commID,MyApiClient.LINK_CREATE_PIN,
//                    userID,accessKey);
            RequestParams params = new RequestParams();
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, commID);
            params.put(WebParams.PIN, Md5.hashMd5(mValuePin));
            params.put(WebParams.CONFIRM_PIN, Md5.hashMd5(confirmPin));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params create pin:"+params.toString());

            MyApiClient.sentCreatePin(this, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        mProg.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.IS_HAVE_PIN, DefineValue.STRING_YES);
                            mEditor.apply();

                            Timber.d("isi params create pin:"+response.toString());
                            Toast.makeText(CreatePIN.this, "Success Create PIN", Toast.LENGTH_LONG).show();
                            finishChild();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(CreatePIN.this,message);
                        }
                        else {
                            Timber.d("isi error create pin:"+response.toString());
                            Toast.makeText(CreatePIN.this, message, Toast.LENGTH_LONG).show();
                            recreate();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(CreatePIN.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(CreatePIN.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(mProg.isShowing())
                        mProg.dismiss();
                    Timber.w("Error Koneksi Create PIN:"+throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void finishChild(){
        if(!isRegist) {
            setResult(MainPage.RESULT_NORMAL);
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
