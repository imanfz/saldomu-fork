package com.sgo.saldomu.activities;

import android.app.Dialog;
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

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.ChangeEmailModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;

import java.util.HashMap;

import timber.log.Timber;

public class ChangeEmail extends BaseActivity {

    TextView tv_curr_email;
    EditText et_new_email;
    Button btn_save;
    SecurePreferences sp;
    private int attempt = 0, failed;
    private ProgressDialog progdialog;
    String message;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_email;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        InitializeToolbar();

        tv_curr_email = findViewById(R.id.tv_current_email);
        et_new_email = findViewById(R.id.et_new_email);
        btn_save = findViewById(R.id.btn_save);

        tv_curr_email.setText(sp.getString(DefineValue.PROFILE_EMAIL, ""));

        btn_save.setOnClickListener(view -> {
            if (inputValidation()) {
                CallPINinput(attempt);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Timber.d("onActivity result", "Biller Fragment"+" / "+requestCode+" / "+resultCode);
        if (requestCode == MainPage.REQUEST_FINISH) {
            //  Log.d("onActivity result", "Biller Fragment masuk request exit");
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                sentChangeEmail(value_pin);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getFailedPin();
    }

    void getFailedPin() {
        new UtilsLoader(this, sp).getFailedPIN(userPhoneID, new OnLoadDataListener() {
            @Override
            public void onSuccess(Object deData) {
                attempt = (int) deData;
            }

            @Override
            public void onFail(Bundle message) {

            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    public void sentChangeEmail(String value_pin) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            extraSignature = memberIDLogin + et_new_email.getText().toString() + value_pin;

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_CHANGE_EMAIL, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.EMAIL, et_new_email.getText().toString());
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(value_pin));
            params.put(WebParams.MEMBER_ID, memberIDLogin);

            Timber.d("isi params Change Email:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CHANGE_EMAIL, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            ChangeEmailModel model = RetrofitService.getInstance().getGson().fromJson(object, ChangeEmailModel.class);
                            String code;
                            if (!model.getOn_error()) {

                                code = model.getError_code();

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    dialogSuccessChangeEmail();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(ChangeEmail.this, model.getError_message());
                                } else {
                                    message = model.getError_message();
                                    Toast.makeText(ChangeEmail.this, message, Toast.LENGTH_LONG).show();
                                    if (code.equals("0097")) {
                                        Intent i = new Intent(ChangeEmail.this, InsertPIN.class);

                                        attempt = model.getFailed_attempt();
                                        failed = model.getMax_failed();

                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt);

                                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                                    } else {
                                        Toast.makeText(ChangeEmail.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void dialogSuccessChangeEmail() {
        Dialog dialognya = DefinedDialog.MessageDialog(ChangeEmail.this, this.getString(R.string.dialog_change_email),
                this.getString(R.string.dialog_change_email1),
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        finish();
                    }
                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    private void CallPINinput(int _attempt) {
        Intent i = new Intent(this, InsertPIN.class);
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    public boolean inputValidation() {
        if (et_new_email.getText().length() == 0) {
            et_new_email.requestFocus();
            et_new_email.setError(this.getString(R.string.validasiemail_edit_error_email));
            return false;
        }
        return true;
    }

    private void InitializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_setting_change_email));
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

}
