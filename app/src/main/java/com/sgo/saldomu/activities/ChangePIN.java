package com.sgo.saldomu.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DateTimeFormat;
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
import com.sgo.saldomu.securities.AES;
import com.sgo.saldomu.securities.Md5;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseActivity;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.PinSaver;
import com.venmo.android.pin.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by thinkpad on 2/11/2016.
 */
public class ChangePIN extends BaseActivity implements PinFragment.Listener {

    private ProgressDialog progdialog;
    private String currentPin;
    private String newPin;
    private String confirmPin;
    private Fragment insertPin;
    private Fragment createPin;
    private TextView tv_title;

    private PinFragmentConfiguration configNew;
    private PinFragmentConfiguration configCurrent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();

        View v = this.findViewById(android.R.id.content);
        assert v != null;
        tv_title = v.findViewById(R.id.pin_title);
        tv_title.setText(getResources().getString(R.string.changepin_text_currentpin));

        configNew = new PinFragmentConfiguration(this)
                .pinSaver(new PinSaver() {
                    @Override
                    public void save(String pin) {
                        newPin = pin;
                        confirmPin = pin;
                        Timber.d("new pin:" + newPin);
//                        PinHelper.saveDefaultPin(ChangePIN1.this, pin);
                        sendChangePin();
                    }
                });

        configCurrent = new PinFragmentConfiguration(this)
                .validator(new Validator() {
                    @Override
                    public boolean isValid(String input) {
//                        return PinHelper.doesMatchDefaultPin(getApplicationContext(), input);
                        Timber.d("pin current: " + input);
                        currentPin = input;

                        createPin = PinFragment.newInstanceForCreation(configNew);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.root, createPin)
                                .commit();

                        tv_title.setText(getResources().getString(R.string.changepin_text_newpin));
                        return true;
                    }
                });

        setFragmentInsertPin();

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_change_pin;
    }

    private void InitializeToolbar() {
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

    @Override
    public void onValidated() {

    }

    @Override
    public void onPinCreated() {

    }

    private void finishChild() {
        setResult(MainPage.RESULT_NORMAL);
        this.finish();
    }

    private void setFragmentInsertPin() {
        insertPin = PinFragment.newInstanceForVerification(configCurrent);
        getFragmentManager().beginTransaction()
                .add(R.id.root, insertPin)
                .commit();
    }

    private void sendChangePin() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");

            extraSignature = memberIDLogin + currentPin + newPin;
            String link = MyApiClient.LINK_CHANGE_PIN;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link, extraSignature);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.COMM_ID, commIDLogin);
            params.put(WebParams.OLD_PIN, RSA.opensslEncrypt(userPhoneID,currentPin,link.substring(link.indexOf("saldomu/"))));
            params.put(WebParams.NEW_PIN, RSA.opensslEncrypt(userPhoneID,newPin,link.substring(link.indexOf("saldomu/"))));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(userPhoneID,confirmPin,link.substring(link.indexOf("saldomu/"))));
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
                                    if (sp.getString(DefineValue.FORCE_CHANGE_PIN,"").equalsIgnoreCase(DefineValue.STRING_YES))
                                    {
                                        sp.edit().putString(DefineValue.FORCE_CHANGE_PIN,DefineValue.STRING_NO).apply();
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
                                    getFragmentManager().beginTransaction().remove(createPin).commit();
                                    setFragmentInsertPin();
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
}
