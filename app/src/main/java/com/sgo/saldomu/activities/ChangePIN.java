package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.widgets.KeyboardPin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 2/11/2016.
 */
public class ChangePIN extends BaseActivity implements KeyboardPin.KeyboardPinListener {

    private ProgressDialog progdialog;
    private String currentPin;
    private String newPin;
    private String confirmPin;
    private TextView tv_title;

    KeyboardPin keyboardPin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeToolbar();

        View v = this.findViewById(android.R.id.content);
        assert v != null;
        tv_title = v.findViewById(R.id.pin_title);
        keyboardPin = findViewById(R.id.keyboard);
        tv_title.setText(getResources().getString(R.string.changepin_text_currentpin));

        keyboardPin.setListener(this);
        keyboardPin.hideFingerprint();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.change_pin;
    }

    private void initializeToolbar() {
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

    private void finishChild() {
        setResult(MainPage.RESULT_NORMAL);
        this.finish();
    }

    private void sendChangePin() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");

            String link = MyApiClient.LINK_CHANGE_PIN;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = memberIDLogin + currentPin + newPin;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.COMM_ID, commIDLogin);
            params.put(WebParams.OLD_PIN, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, currentPin, subStringLink));
            params.put(WebParams.NEW_PIN, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, newPin, subStringLink));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, confirmPin, subStringLink));
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params change pin:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(link, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                jsonModel model = getGson().fromJson(String.valueOf(response), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("isi params change pin:" + response.toString());
                                    Toast.makeText(ChangePIN.this, getString(R.string.changepin_toast_success), Toast.LENGTH_LONG).show();
                                    if (sp.getString(DefineValue.FORCE_CHANGE_PIN, "").equalsIgnoreCase(DefineValue.STRING_YES)) {
                                        sp.edit().putString(DefineValue.FORCE_CHANGE_PIN, DefineValue.STRING_NO).apply();
                                    }
                                    finishChild();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout", response.toString());
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(ChangePIN.this, message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(ChangePIN.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(ChangePIN.this, model.getError_message());
                                } else {
                                    Toast.makeText(ChangePIN.this, message, Toast.LENGTH_LONG).show();
                                    tv_title.setText(getResources().getString(R.string.changepin_text_currentpin));
                                    resetLayout();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Timber.e(throwable.getMessage());
                            progdialog.dismiss();
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

    private void resetLayout() {
        tv_title.setText(getResources().getString(R.string.changepin_text_currentpin));
        currentPin = null;
        newPin = null;
        confirmPin = null;
        keyboardPin.reset();
    }

    @Override
    public void getCharSequenceKeyboard(CharSequence text) {
        if (text.length() == 6) {
            keyboardPin.reset();
            if (tv_title.getText() == getResources().getString(R.string.changepin_text_currentpin)) {
                currentPin = text.toString();
                tv_title.setText(getResources().getString(R.string.changepin_text_newpin));
            } else if (tv_title.getText() == getResources().getString(R.string.changepin_text_newpin)) {
                newPin = text.toString();
                tv_title.setText(getResources().getString(R.string.changepin_text_retypenewpin));
            } else if (tv_title.getText() == getResources().getString(R.string.changepin_text_retypenewpin))
                confirmPin = text.toString();

            if (currentPin != null && newPin != null && confirmPin != null)
                if (currentPin.length() == 6 && newPin.length() == 6 && confirmPin.length() == 6) {
                    Timber.d("current pin : " + currentPin);
                    Timber.d("new pin : " + newPin);
                    Timber.d("confirm pin : " + confirmPin);
                    sendChangePin();
                }
        }
    }

    @Override
    public void useFingerprint() {

    }
}
