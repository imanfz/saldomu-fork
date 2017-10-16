package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.PasswordValidator;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 1/20/2015.
 */
public class ChangePassword extends BaseActivity implements View.OnClickListener {

    private TextView tv_firsttime_msg;
    private EditText et_pass_current;
    private EditText et_pass_new;
    private EditText et_pass_retype;
    private CheckBox cb_show_pass;
    private Button btn_submit_changepass;
    private Button btn_batal_changepass;
    private SecurePreferences sp;
    private ProgressDialog progdialog;
    private String userID;
    private String accessKey;
    private String member_id;
    private boolean is_first_time;
    private int lenght_auth_min, validIdx;
    private PasswordValidator mPassValid;

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        member_id = sp.getString(DefineValue.MEMBER_ID,"");

        Intent intent    = getIntent();
        if(intent.hasExtra(DefineValue.IS_FIRST))
            is_first_time  = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);

        InitializeToolbar();

        et_pass_current = (EditText) findViewById(R.id.current_pass_value);
        et_pass_new = (EditText) findViewById(R.id.new_pass_value);
        et_pass_retype = (EditText) findViewById(R.id.retype_new_pass_value);
        cb_show_pass = (CheckBox) findViewById(R.id.cb_showPass_changepass);
        btn_submit_changepass = (Button) findViewById(R.id.btn_submit_changePassword);
        btn_batal_changepass = (Button) findViewById(R.id.btn_batal_changepass);
        tv_firsttime_msg = (TextView) findViewById(R.id.changepass_firsttime_msg);

        btn_submit_changepass.setOnClickListener(this);
        btn_batal_changepass.setOnClickListener(this);
        cb_show_pass.setOnCheckedChangeListener(showPassword);
        if(is_first_time)tv_firsttime_msg.setVisibility(View.VISIBLE);

        mPassValid = new PasswordValidator();
        lenght_auth_min = 5;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_password;
    }

    private void InitializeToolbar(){
        if(is_first_time) disableHomeIcon();
        else setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.changepass_ab_changepass));
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_submit_changePassword :
                if(InetHandler.isNetworkAvailable(this)) {
                    if (inputValidation()) {
                        sendChangePassword();
                    }
                }
                else DefinedDialog.showErrorDialog(this, getString(R.string.inethandler_dialog_message));
                break;

            case R.id.btn_batal_changepass :
                if(!is_first_time)
                    setResult(MainPage.RESULT_NORMAL);
                else setResult(MainPage.RESULT_LOGOUT);
                finish();
                break;
        }
    }

    private CheckBox.OnCheckedChangeListener showPassword = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(!b){
                et_pass_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pass_current.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            else {
                et_pass_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pass_current.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        }
    };

    private void sendChangePassword(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_CHANGE_PASSWORD,
                    userID,accessKey);
            params.put(WebParams.USER_ID,userID);
            params.put(WebParams.OLD_PASSWORD,et_pass_current.getText().toString());
            params.put(WebParams.NEW_PASSWORD,et_pass_new.getText().toString());
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, member_id);

            Timber.d("isi params Change Password:" + params.toString());

            MyApiClient.sentChangePassword(this, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response change password:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            //Toast.makeText(ChangePassword.this, sp.getString(CoreApp.IS_FIRST_TIME,""), Toast.LENGTH_LONG).show();
                            Toast.makeText(ChangePassword.this, getString(R.string.changepass_toast_success), Toast.LENGTH_LONG).show();
//                            sp.edit().putString(DefineValue.IS_FIRST_TIME, DefineValue.NO);
                            sp.edit().putString(DefineValue.IS_CHANGED_PASS, DefineValue.STRING_YES).apply();
                            finishChild();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(ChangePassword.this,message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(ChangePassword.this, code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ChangePassword.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ChangePassword.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    progdialog.dismiss();
                    Timber.w("Error Koneksi change password:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void finishChild(){
        if(is_first_time)
            setResult(MainPage.RESULT_FIRST_TIME);
        else
            setResult(MainPage.RESULT_NORMAL);
        this.finish();
    }

    private boolean inputValidation(){
        if(et_pass_current.getText().toString().length()==0){
            et_pass_current.requestFocus();
            et_pass_current.setError(this.getString(R.string.changepass_edit_error_currentpass));
            return false;
        }
        else if(et_pass_new.getText().toString().length()==0){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpass));
            return false;
        }
//        else if(validIdx != 0){
//            et_pass_new.requestFocus();
//            et_pass_new.setError(getString(validIdx));
//            return false;
//        }
        else if(et_pass_new.getText().toString().length()<lenght_auth_min){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpasslength));
            return false;
        }
        else if(et_pass_retype.getText().toString().length()==0){
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass));
            return false;
        } else if (!et_pass_retype.getText().toString().equals(et_pass_new.getText().toString())){
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass_confirm));
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(!is_first_time) super.onBackPressed();
    }
}