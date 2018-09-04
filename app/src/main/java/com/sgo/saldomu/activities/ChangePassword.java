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

import com.google.gson.JsonObject;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.PasswordValidator;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.securities.RSA;

import java.util.HashMap;

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
    private ProgressDialog progdialog;
    private boolean is_first_time;
    private int lenght_auth_min, validIdx;
    private PasswordValidator mPassValid;

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent    = getIntent();
        if(intent.hasExtra(DefineValue.IS_FIRST))
            is_first_time  = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);

        InitializeToolbar();

        et_pass_current = findViewById(R.id.current_pass_value);
        et_pass_new = findViewById(R.id.new_pass_value);
        et_pass_retype = findViewById(R.id.retype_new_pass_value);
        cb_show_pass = findViewById(R.id.cb_showPass_changepass);
        btn_submit_changepass = findViewById(R.id.btn_submit_changePassword);
        btn_batal_changepass = findViewById(R.id.btn_batal_changepass);
        tv_firsttime_msg = findViewById(R.id.changepass_firsttime_msg);

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

            extraSignature = memberIDLogin+et_pass_current.getText().toString()+et_pass_new.getText().toString();

            RequestParams param = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_CHANGE_PASSWORD,
                    userPhoneID,accessKey, extraSignature);
            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_CHANGE_PASSWORD, extraSignature);
            params.put(WebParams.USER_ID,userPhoneID);
            params.put(WebParams.OLD_PASSWORD, RSA.opensslEncrypt(et_pass_current.getText().toString()));
            params.put(WebParams.NEW_PASSWORD, RSA.opensslEncrypt(et_pass_new.getText().toString()));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);

            Timber.d("isi params Change Password:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CHANGE_PASSWORD, params
                    , new ObjListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            progdialog.dismiss();
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(object, jsonModel.class);

                            if (!model.getOn_error()){

                                String code = model.getError_code();

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    //Toast.makeText(ChangePassword.this, sp.getString(CoreApp.IS_FIRST_TIME,""), Toast.LENGTH_LONG).show();
                                    Toast.makeText(ChangePassword.this, getString(R.string.changepass_toast_success), Toast.LENGTH_LONG).show();
//                            sp.edit().putString(DefineValue.IS_FIRST_TIME, DefineValue.NO);
                                    sp.edit().putString(DefineValue.IS_CHANGED_PASS, DefineValue.STRING_YES).apply();
                                    finishChild();
                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
//                                    Timber.d("isi response autologout:"+response.toString());
//                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(ChangePassword.this,model.getError_message());
                                }
                                else {
//                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(ChangePassword.this, model.getError_message(), Toast.LENGTH_LONG).show();
                                }
                            }else {
                                if(MyApiClient.PROD_FAILURE_FLAG)
                                    Toast.makeText(ChangePassword.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ChangePassword.this, model.getError_message(), Toast.LENGTH_SHORT).show();

//                                Timber.w("Error Koneksi change password:"+throwable.toString());
                            }
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