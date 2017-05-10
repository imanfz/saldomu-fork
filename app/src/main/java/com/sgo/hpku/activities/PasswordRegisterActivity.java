package com.sgo.hpku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.coreclass.BaseActivity;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.InetHandler;
import com.sgo.hpku.dialogs.DefinedDialog;

/**
 * Created by thinkpad on 10/12/2015.
 */
public class PasswordRegisterActivity extends BaseActivity {

    private EditText et_pass_new;
    private EditText et_pass_retype;
    private CheckBox cb_show_pass;
    private Button btn_next;
    private SecurePreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        InitializeToolbar();

        et_pass_new = (EditText) findViewById(R.id.new_pass_value);
        et_pass_retype = (EditText) findViewById(R.id.confirm_pass_value);
        cb_show_pass = (CheckBox) findViewById(R.id.cb_showPass_changepass);
        btn_next = (Button) findViewById(R.id.btn_next_newPass);

        assert btn_next != null;
        btn_next.setOnClickListener(btnSubmitNewPassListener);
        cb_show_pass.setOnCheckedChangeListener(showPassword);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_password_register;
    }


    private void InitializeToolbar(){
        disableHomeIcon();
        setActionBarTitle(getString(R.string.changepass_ab_createpass));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private Button.OnClickListener btnSubmitNewPassListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(PasswordRegisterActivity.this)) {
                if (inputValidation()) {
                    Intent i = new Intent();
                    i.putExtra(DefineValue.NEW_PASSWORD, et_pass_new.getText().toString());
                    i.putExtra(DefineValue.CONFIRM_PASSWORD, et_pass_retype.getText().toString());

                    finishChild(i);
                }
            }
            else DefinedDialog.showErrorDialog(PasswordRegisterActivity.this, getString(R.string.inethandler_dialog_message));
        }
    };

    private CheckBox.OnCheckedChangeListener showPassword = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(!b){
                et_pass_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            else {
                et_pass_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        }
    };

    private void finishChild(Intent data){

//        if (auth.equals(DefineValue.AUTH_TYPE_PIN))
            setResult(LoginActivity.RESULT_PIN,data);
//        else
//            setResult(Registration.RESULT_FINISHING,data);
        finish();
    }

    private boolean inputValidation(){
        if(et_pass_new.getText().toString().length()==0){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpass));
            return false;
        }
        else if(et_pass_new.getText().toString().length()<6){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpasslength));
            return false;
        }
        else if(et_pass_retype.getText().toString().length()==0){
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass));
            return false;
        }
        else if(!et_pass_retype.getText().toString().equals(et_pass_new.getText().toString())){
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass_confirm));
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
    }
}
