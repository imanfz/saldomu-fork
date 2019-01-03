package com.sgo.saldomu.activities;

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

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.Validator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 4/2/2015.
 */
public class InsertPIN extends BaseActivity implements PinFragment.Listener {

    public static final int RESULT_PIN_VALUE = 302;
    public static final int RESULT_CANCEL_ORDER = 303;

    SecurePreferences sp;
    String valuePin;
    Boolean IsForgotPassword;
    Fragment toShow;
    TextView tv_attempt ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        View v = this.findViewById(android.R.id.content);
        if (v != null) {
            tv_attempt = v.findViewById(R.id.pin_tries_value);
        }
        Timber.d("masuk UtilsLoader");
        String userId  = sp.getString(DefineValue.USERID_PHONE,"");
        if(userId.isEmpty())
            userId = getIntent().getExtras().getString(DefineValue.USERID_PHONE,"");

        new UtilsLoader(this,sp).getFailedPIN(userId, new OnLoadDataListener() {
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

        InitializeToolbar();

        final Boolean is_md5 = getIntent().getBooleanExtra(DefineValue.IS_MD5, false);
        IsForgotPassword = getIntent().getBooleanExtra(DefineValue.IS_FORGOT_PASSWORD,false);
        final int attempt = getIntent().getIntExtra(DefineValue.ATTEMPT,0);

        if(attempt != 0){
            setTextAttempt(String.valueOf(attempt));
        }

        PinFragmentConfiguration config = new PinFragmentConfiguration(getApplicationContext())
                .validator(new Validator() {
                    @Override
                    public boolean isValid(String input) {
//                        return PinHelper.doesMatchDefaultPin(getApplicationContext(), input);
                        Timber.d("pin yg di confirm "+input);
                        valuePin = input;
                        SecurePreferences.Editor mEditor = sp.edit();
                        Intent i = new Intent();
                        mEditor.putString(DefineValue.PIN_CODE, RSA.opensslEncrypt(input));
                        if(is_md5)
                            i.putExtra(DefineValue.PIN_VALUE,RSA.opensslEncrypt(input));
                        else
                            i.putExtra(DefineValue.PIN_VALUE,input);
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

    private void setTextAttempt(String attempt){
        if(attempt == null || attempt.isEmpty())
            attempt = "0";

        String attempt_text = getString(R.string.login_failed_attempt_1,Integer.valueOf(attempt));
        tv_attempt.setText(attempt_text);
        if(attempt.equalsIgnoreCase("1"))
            tv_attempt.setVisibility(View.VISIBLE);
        else
            tv_attempt.setVisibility(View.GONE);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.insert_pin;
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
                setResult(RESULT_CANCEL_ORDER);
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
        Button btnDialogOK = dialog.findViewById(R.id.btn_dialog_notification_ok);
        ProgressBar progDialog = dialog.findViewById(R.id.progressBarDialogNotif);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);


        Title.setText(getResources().getString(R.string.forgotpin));
        Message.setVisibility(View.GONE);
        Message.setText(getString(R.string.forgotpin_message)+" "+
                getString(R.string.appname)+" "+
                getString(R.string.forgotpin_message2));

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

            HashMap<String, Object>  params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_HELP_PIN, "");

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_HELP_PIN, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            String message_value;
                            try {
                                JSONArray arrayContact = new JSONArray(response.optString(WebParams.CONTACT_DATA));
                                JSONObject mObject;
                                Log.d("getHelpPin", response.toString());
                                for (int i = 0; i < arrayContact.length(); i++) {
                                    mObject = arrayContact.getJSONObject(i);
//                            id = mObject.optString(WebParams.ID, "0");
                                    if (i == 1) {
                                        message_value = mObject.optString(WebParams.DESCRIPTION, "") + " " +
                                                mObject.optString(WebParams.NAME, "") + "\n" +
                                                mObject.optString(WebParams.CONTACT_PHONE, "") + " " +
                                                getString(R.string.or) + " " +
                                                mObject.optString(WebParams.CONTACT_EMAIL, "");
                                        Message.setText(message_value);
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progDialog.setIndeterminate(false);
                            progDialog.setVisibility(View.GONE);
                            Message.setVisibility(View.VISIBLE);
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient"+e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(RESULT_CANCEL_ORDER);
        finish();
    }
}
