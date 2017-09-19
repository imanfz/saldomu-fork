package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;

import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 10/24/2016.
 */

public class ValidasiEmailActivity extends BaseActivity {

    SecurePreferences sp;
    ProgressDialog progdialog;
    String userID, accessKey, email;
    View change_layout;
    EditText etToken, etEmail;
    TextView currEmail;
    Button btnProses, btnAsk, btnChange;
    private boolean is_first_time;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_validasi_email;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        email = sp.getString(DefineValue.PROFILE_EMAIL,"");

        Intent intent    = getIntent();
        if(intent.hasExtra(DefineValue.IS_FIRST)) {
            is_first_time = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);
            if(is_first_time)
                setResult(MainPage.RESULT_FIRST_TIME);
        }

        InitializeToolbar();

        change_layout = findViewById(R.id.change_email_layout);
        etToken = (EditText) findViewById(R.id.token_value);
        etEmail = (EditText) findViewById(R.id.email_value);
        currEmail = (TextView) findViewById(R.id.text_email);
        btnProses = (Button) findViewById(R.id.btn_token);
        btnAsk = (Button) findViewById(R.id.btn_ask_change);
        btnChange = (Button) findViewById(R.id.btn_change);

        currEmail.setText(getString(R.string.validasi_email_text) + " " + email);

        btnAsk.setOnClickListener(askListener);
        btnChange.setOnClickListener(changeListener);
        btnProses.setOnClickListener(prosesListener);

        sentReqChangeEmail("", true);
    }


    Button.OnClickListener askListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            change_layout.setVisibility(View.VISIBLE);
        }
    };

    Button.OnClickListener changeListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(ValidasiEmailActivity.this)) {
                if(inputValidationEmail()) {
                    email = etEmail.getText().toString();
                    sentReqChangeEmail(email, false);
                }
            }
            else DefinedDialog.showErrorDialog(ValidasiEmailActivity.this, getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener prosesListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(ValidasiEmailActivity.this)) {
                if(inputValidationToken()) {
                    sentConfirmChangeEmail(email, etToken.getText().toString());
                }
            }
            else DefinedDialog.showErrorDialog(ValidasiEmailActivity.this, getString(R.string.inethandler_dialog_message));
        }
    };

    public boolean inputValidationEmail(){
        if(etEmail.getText().toString().length()==0){
            etEmail.requestFocus();
            etEmail.setError(this.getString(R.string.validasiemail_edit_error_email));
            return false;
        }
        return true;
    }

    public boolean inputValidationToken(){
        if(etToken.getText().toString().length()==0){
            etToken.requestFocus();
            etToken.setError(this.getString(R.string.validasiemail_edit_error_token));
            return false;
        }
        return true;
    }

    public void sentReqChangeEmail(String _email, final boolean firstCall){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQ_CHANGE_EMAIL,
                    userID,accessKey);
            params.put(WebParams.USER_ID,userID);
            params.put(WebParams.CUST_PHONE,userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            if(!_email.equals(""))
                params.put(WebParams.CUST_EMAIL, _email);

            Timber.d("isi params req change email:" + params.toString());

            MyApiClient.sentReqChangeEmail(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response req change email:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
//                            email = response.getString(WebParams.CUST_EMAIL);
                            if(!firstCall) {
                                etEmail.setText("");
                                change_layout.setVisibility(View.GONE);
                                currEmail.setText(getString(R.string.validasi_email_text) + " " + email);
                                Toast.makeText(ValidasiEmailActivity.this, getString(R.string.changeemail_toast_success), Toast.LENGTH_LONG).show();
                            }
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(ValidasiEmailActivity.this,message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(ValidasiEmailActivity.this, code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ValidasiEmailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ValidasiEmailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    progdialog.dismiss();
                    Timber.w("Error Koneksi req change email:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sentConfirmChangeEmail(String _email, String _token){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_CONFIRM_CHANGE_EMAIL,
                    userID,accessKey);
            params.put(WebParams.USER_ID,userID);
            params.put(WebParams.CUST_PHONE,userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_EMAIL, _email);
            params.put(WebParams.EMAIL_TOKEN, _token);
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID,""));

            Timber.d("isi params confirm change email:" + params.toString());

            MyApiClient.sentConfirmChangeEmail(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response confirm change email:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Toast.makeText(ValidasiEmailActivity.this, getString(R.string.validasiemail_toast_success), Toast.LENGTH_LONG).show();
                            ChangeNewBulkData();
                            finishChild();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(ValidasiEmailActivity.this,message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(ValidasiEmailActivity.this, code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ValidasiEmailActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ValidasiEmailActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    progdialog.dismiss();
                    Timber.w("Error Koneksi confirm change email:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void ChangeNewBulkData(){
        sp.edit().putString(DefineValue.IS_NEW_BULK,DefineValue.STRING_NO).apply();
    }

    public void finishChild(){

        this.finish();
    }

    public void InitializeToolbar(){
        if(is_first_time) disableHomeIcon();
        else setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.verifikasi_email));
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
                if(!is_first_time){
                    setResult(MainPage.RESULT_NORMAL);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!is_first_time) super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
