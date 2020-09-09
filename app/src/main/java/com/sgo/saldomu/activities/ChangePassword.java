package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.PasswordValidator;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fragments.ForgotPassword;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;

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
    private Button btnForgetPass;
    private ProgressDialog progdialog;
    private boolean is_first_time;
    private int lenght_auth_min, validIdx;
    private PasswordValidator mPassValid;

    FrameLayout frameLayout;

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra(DefineValue.IS_FIRST))
            is_first_time = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);

        initializeToolbar();

        et_pass_current = findViewById(R.id.current_pass_value);
        btnForgetPass = findViewById(R.id.btn_forgetPass);
        et_pass_new = findViewById(R.id.new_pass_value);
        et_pass_retype = findViewById(R.id.retype_new_pass_value);
        cb_show_pass = findViewById(R.id.cb_showPass_changepass);
        btn_submit_changepass = findViewById(R.id.btn_submit_changePassword);
        btn_batal_changepass = findViewById(R.id.btn_batal_changepass);
        tv_firsttime_msg = findViewById(R.id.changepass_firsttime_msg);
        frameLayout = findViewById(R.id.changePassContent);

        btnForgetPass.setOnClickListener(this);
        btn_submit_changepass.setOnClickListener(this);
        btn_batal_changepass.setOnClickListener(this);
        cb_show_pass.setOnCheckedChangeListener(showPassword);
        if (is_first_time) tv_firsttime_msg.setVisibility(View.VISIBLE);

        mPassValid = new PasswordValidator();
        lenght_auth_min = 5;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_password;
    }

    private void initializeToolbar() {
        if (is_first_time) disableHomeIcon();
        else setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_setting_change_pass));
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
                if (!is_first_time) {
                    setResult(MainPage.RESULT_NORMAL);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit_changePassword:
                if (InetHandler.isNetworkAvailable(this)) {
                    if (inputValidation()) {
                        sendChangePassword();
                    }
                } else
                    DefinedDialog.showErrorDialog(this, getString(R.string.inethandler_dialog_message));
                break;

            case R.id.btn_batal_changepass:
                if (!is_first_time)
                    setResult(MainPage.RESULT_NORMAL);
                else setResult(MainPage.RESULT_LOGOUT);
                finish();
                break;

            case R.id.btn_forgetPass:
                frameLayout.setVisibility(View.VISIBLE);
                Fragment newFrag = new ForgotPassword();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.changePassContent, newFrag, "forgot password")
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

    private CheckBox.OnCheckedChangeListener showPassword = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!b) {
                et_pass_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pass_current.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                et_pass_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pass_current.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        }
    };

    private void sendChangePassword() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            String link = MyApiClient.LINK_CHANGE_PASSWORD;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            String oldPassword = et_pass_current.getText().toString();
            String newPassword = et_pass_new.getText().toString();
            extraSignature = memberIDLogin + oldPassword + newPassword;
            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.OLD_PASSWORD, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, oldPassword, subStringLink));
            params.put(WebParams.NEW_PASSWORD, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, newPassword, subStringLink));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);

            Timber.d("isi params Change Password:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(object, jsonModel.class);

                            if (!model.getOn_error()) {

                                String code = model.getError_code();

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    //Toast.makeText(ChangePassword.this, sp.getString(CoreApp.IS_FIRST_TIME,""), Toast.LENGTH_LONG).show();
                                    Toast.makeText(ChangePassword.this, getString(R.string.changepass_toast_success), Toast.LENGTH_LONG).show();
//                            sp.edit().putString(DefineValue.IS_FIRST_TIME, DefineValue.NO);
                                    sp.edit().putString(DefineValue.IS_CHANGED_PASS, DefineValue.STRING_YES).apply();
                                    sp.edit().remove(DefineValue.USER_PASSWORD).apply();
                                    finishChild();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
//                                    Timber.d("isi response autologout:"+response.toString());
//                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(ChangePassword.this, model.getError_message());
                                }else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(ChangePassword.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + object.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(ChangePassword.this, model.getError_message());
                                }
//                                else if(code.equals("0301")){
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassword.this);
//                                    builder.setTitle(getString(R.string.password_validation))
//                                            .setMessage(getString(R.string.password_clue))
//                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            });
//                                    AlertDialog dialog = builder.create();
//                                    dialog.show();
//                                }
                                    else {
//                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(ChangePassword.this, model.getError_message(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(ChangePassword.this, model.getError_message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progdialog.dismiss();
                        }
                    } );
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void finishChild() {
        if (is_first_time)
            setResult(MainPage.RESULT_FIRST_TIME);
        else
            setResult(MainPage.RESULT_NORMAL);
        this.finish();
    }

    private boolean inputValidation() {
        if (et_pass_current.getText().toString().length() == 0) {
            et_pass_current.requestFocus();
            et_pass_current.setError(this.getString(R.string.changepass_edit_error_currentpass));
            return false;
        } else if (et_pass_new.getText().toString().length() == 0) {
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpass));
            return false;
        }
//        else if(validIdx != 0){
//            et_pass_new.requestFocus();
//            et_pass_new.setError(getString(validIdx));
//            return false;
//        }
        else if (et_pass_new.getText().toString().length() < 8 || et_pass_new.getText().toString().length() >20) {
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpasslength));
            return false;
        } else if (et_pass_retype.getText().toString().length() == 0) {
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass));
            return false;
        } else if (!et_pass_retype.getText().toString().equals(et_pass_new.getText().toString())) {
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass_confirm));
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!is_first_time) super.onBackPressed();
    }
}