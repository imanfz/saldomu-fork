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
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.ChangeEmailModel;
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

        initializeToolbar();

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

            String link = MyApiClient.LINK_CHANGE_EMAIL;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            String newEmail = et_new_email.getText().toString();
            extraSignature = memberIDLogin + newEmail + value_pin;
            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.EMAIL, newEmail);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PRODUCT_VALUE, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, value_pin, subStringLink));
            params.put(WebParams.MEMBER_ID, memberIDLogin);

            Timber.d("isi params Change Email:%s", params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            ChangeEmailModel model = RetrofitService.getInstance().getGson().fromJson(object, ChangeEmailModel.class);
                            if (!model.getOn_error()) {

                                String code = model.getError_code();
                                String message = model.getError_message();

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    dialogSuccessChangeEmail();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout.getInstance().showDialoginActivity(ChangeEmail.this, model.getError_message());
                                }else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(ChangeEmail.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", object.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(ChangeEmail.this);
                                } else {
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
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void dialogSuccessChangeEmail() {
        Dialog dialognya = DefinedDialog.MessageDialog(ChangeEmail.this, this.getString(R.string.dialog_change_email),
                this.getString(R.string.dialog_change_email1),
                () -> finish()
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

    private void initializeToolbar() {
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
