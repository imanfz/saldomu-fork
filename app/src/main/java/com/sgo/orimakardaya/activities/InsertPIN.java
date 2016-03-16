package com.sgo.orimakardaya.activities;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.Validator;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/2/2015.
 */
public class InsertPIN extends BaseActivity implements PinFragment.Listener {

    public static final int RESULT_PIN_VALUE = 302;

    SecurePreferences sp;
    String valuePin;
    Boolean IsForgotPassword;
    Fragment toShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        InitializeToolbar();

        View v = this.findViewById(android.R.id.content);

        final Boolean is_md5 = getIntent().getBooleanExtra(DefineValue.IS_MD5,true);
        IsForgotPassword = getIntent().getBooleanExtra(DefineValue.IS_FORGOT_PASSWORD,false);
        int attempt = getIntent().getIntExtra(DefineValue.ATTEMPT,0);


        if(attempt != 0){
            TextView tv_attempt = (TextView) v.findViewById(R.id.pin_tries_value);
            String attempt_text = getString(R.string.login_failed_attempt_1)+" "+
                    String.valueOf(attempt)+" "+
                    getString(R.string.login_failed_attempt_2);
            tv_attempt.setText(attempt_text);
            tv_attempt.setVisibility(View.VISIBLE);
        }



        PinFragmentConfiguration config = new PinFragmentConfiguration(getApplicationContext())
                .validator(new Validator() {
                    @Override
                    public boolean isValid(String input) {
//                        return PinHelper.doesMatchDefaultPin(getApplicationContext(), input);
                        Timber.d("pin yg di confirm " + input);
                        valuePin = input;
                        SecurePreferences.Editor mEditor = sp.edit();
                        Intent i = new Intent();
                        try {
                            mEditor.putString(DefineValue.PIN_CODE, Md5.hashMd5(input));
                            if(is_md5)
                                i.putExtra(DefineValue.PIN_VALUE,Md5.hashMd5(input));
                            else
                                i.putExtra(DefineValue.PIN_VALUE,input);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                        mEditor.apply();
                        setResult(RESULT_PIN_VALUE,i);
                        finish();
                        return true;
                    }
                });

        toShow = PinFragment.newInstanceForVerification(config);

        getFragmentManager().beginTransaction()
                .add(R.id.root, toShow)
                .commit();

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.create_pin;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.input_pin));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(IsForgotPassword)
            getMenuInflater().inflate(R.menu.forgot_pin, menu);
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
            case R.id.action_forgot_pin:
                showDialogForgotPin();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidated() {

    }

    @Override
    public void onPinCreated() {

    }

    void showDialogForgotPin(){
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOK = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        ProgressBar progDialog = (ProgressBar)dialog.findViewById(R.id.progressBarDialogNotif);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);


        Title.setText(getResources().getString(R.string.forgotpin));
        Message.setVisibility(View.GONE);
        Message.setText(getString(R.string.forgotpin_message));

        progDialog.setIndeterminate(true);
        progDialog.setVisibility(View.VISIBLE);
        getHelpPin(progDialog,Message);

        btnDialogOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void getHelpPin(final ProgressBar progDialog, final TextView Message){
        try{
            MyApiClient.getHelpPIN(new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    String id,message_value;
                    try {
                        JSONArray arrayContact = new JSONArray(response.optString(WebParams.CONTACT_DATA));
                        JSONObject mObject;
                        Log.d("getHelpPin",response.toString());
                        for(int i=0; i < arrayContact.length() ; i++ ) {
                            mObject = arrayContact.getJSONObject(i);
//                            id = mObject.optString(WebParams.ID, "0");
                            if(i==1) {
                                message_value = mObject.optString(WebParams.DESCRIPTION,"")+" "+
                                                mObject.optString(WebParams.NAME,"")+"\n"+
                                                mObject.optString(WebParams.CONTACT_PHONE,"")+" "+
                                                getString(R.string.or)+" "+
                                                mObject.optString(WebParams.CONTACT_EMAIL,"");
                                Message.setText(message_value);
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progDialog.setIndeterminate(false);
                    progDialog.setVisibility(View.GONE);
                    Message.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    progDialog.setIndeterminate(false);
                    progDialog.setVisibility(View.GONE);
                    Message.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    progDialog.setIndeterminate(false);
                    progDialog.setVisibility(View.GONE);
                    Message.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    progDialog.setIndeterminate(false);
                    progDialog.setVisibility(View.GONE);
                    Message.setVisibility(View.VISIBLE);
                }
            });

        }catch (Exception e){
            Timber.d("httpclient"+e.getMessage());
        }
    }

}
