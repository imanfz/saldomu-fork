package com.sgo.orimakardaya.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
* Created by thinkpad on 3/20/2015.
*/
public class ChangePIN extends BaseActivity {

    TextView tv_firsttime_msg;
    EditText et_pin_current, et_pin_new, et_pin_retype;
    CheckBox cb_show_pin;
    Button btn_submit_changepin, btn_batal_changepin;
    SecurePreferences sp;
    ProgressDialog progdialog;

    String memberID, commID,userID,accessKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        memberID = sp.getString(DefineValue.MEMBER_ID, "");
        commID = sp.getString(DefineValue.COMMUNITY_ID, "");
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        InitializeToolbar();

        et_pin_current = (EditText) findViewById(R.id.current_pin_value);
        et_pin_new = (EditText) findViewById(R.id.new_pin_value);
        et_pin_retype = (EditText) findViewById(R.id.retype_new_pin_value);
        cb_show_pin = (CheckBox) findViewById(R.id.cb_showPin_changepin);
        btn_submit_changepin = (Button) findViewById(R.id.btn_submit_changePin);
        btn_batal_changepin = (Button) findViewById(R.id.btn_batal_changepin);
        tv_firsttime_msg = (TextView) findViewById(R.id.changepin_firsttime_msg);

        btn_submit_changepin.setOnClickListener(btnSubmitChangePinListener);
        btn_batal_changepin.setOnClickListener(btnBatalChangePinListener);
        cb_show_pin.setOnCheckedChangeListener(showPin);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_pin;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.changepin_ab_changepin));
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
                setResult(MainPage.RESULT_NORMAL);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendChangePin() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");

            RequestParams params = MyApiClient.getSignatureWithParams(commID,MyApiClient.LINK_CHANGE_PIN,
                    userID,accessKey);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, commID);
            params.put(WebParams.OLD_PIN, Md5.hashMd5(et_pin_current.getText().toString()));
            params.put(WebParams.NEW_PIN, Md5.hashMd5(et_pin_new.getText().toString()));
            params.put(WebParams.CONFIRM_PIN, Md5.hashMd5(et_pin_retype.getText().toString()));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params change pin:" + params.toString());

            MyApiClient.sentChangePin(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        progdialog.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params change pin:"+response.toString());
                            Toast.makeText(ChangePIN.this, getString(R.string.changepin_toast_success), Toast.LENGTH_LONG).show();
                            finishChild();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(ChangePIN.this,message);
                        }
                        else {
                            Timber.d("isi error change pin:"+response.toString());
                            Toast.makeText(ChangePIN.this, message, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        progdialog.dismiss();
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
                        Toast.makeText(ChangePIN.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ChangePIN.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    progdialog.dismiss();
                    Timber.w("Error Koneksi Change PIN", throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void finishChild(){
        setResult(MainPage.RESULT_NORMAL);
        this.finish();
    }

    Button.OnClickListener btnSubmitChangePinListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inputValidation()){
                sendChangePin();
            }
        }
    };

    Button.OnClickListener btnBatalChangePinListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    CheckBox.OnCheckedChangeListener showPin = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(!b){
                et_pin_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pin_current.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pin_retype.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            else {
                et_pin_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pin_current.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pin_retype.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        }
    };

    public boolean inputValidation(){
        if(et_pin_current.getText().toString().length()==0){
            et_pin_current.requestFocus();
            et_pin_current.setError(this.getString(R.string.changepin_edit_error_currentpin));
            return false;
        }
        else if(et_pin_new.getText().toString().length()==0){
            et_pin_new.requestFocus();
            et_pin_new.setError(this.getString(R.string.changepin_edit_error_newpin));
            return false;
        }
        else if(et_pin_new.getText().toString().length()<5){
            et_pin_new.requestFocus();
            et_pin_new.setError(this.getString(R.string.changepin_edit_error_newpinlength));
            return false;
        }
        else if(et_pin_retype.getText().toString().length()==0){
            et_pin_retype.requestFocus();
            et_pin_retype.setError(this.getString(R.string.changepin_edit_error_retypenewpin));
            return false;
        }
        else if(!et_pin_retype.getText().toString().equals(et_pin_new.getText().toString())){
            et_pin_retype.requestFocus();
            et_pin_retype.setError(this.getString(R.string.changepin_edit_error_retypenewpin_confirm));
            return false;
        }
        return true;
    }
}
