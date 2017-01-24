package com.sgo.orimakardaya.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.securities.Md5;
import com.venmo.android.pin.PinFragment;
import com.venmo.android.pin.PinFragmentConfiguration;
import com.venmo.android.pin.PinSaver;
import com.venmo.android.pin.Validator;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 2/11/2016.
 */
public class ChangePIN extends BaseActivity implements PinFragment.Listener {

    private SecurePreferences sp;
    private ProgressDialog progdialog;
    private String currentPin;
    private String newPin;
    private String confirmPin;
    private Fragment insertPin;
    private Fragment createPin;
    private TextView tv_title;
    private String memberID;
    private String commID;
    private String userID;
    private String accessKey;
    private PinFragmentConfiguration configNew;
    private PinFragmentConfiguration configCurrent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        memberID = sp.getString(DefineValue.MEMBER_ID,"");
        commID = sp.getString(DefineValue.COMMUNITY_ID,"");
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        InitializeToolbar();

        View v = this.findViewById(android.R.id.content);
        assert v != null;
        tv_title = (TextView) v.findViewById(R.id.pin_title);
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

    private void InitializeToolbar(){
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

    private void finishChild(){
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

            RequestParams params = MyApiClient.getSignatureWithParams(commID, MyApiClient.LINK_CHANGE_PIN,
                    userID, accessKey);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, commID);
            params.put(WebParams.OLD_PIN, Md5.hashMd5(currentPin));
            params.put(WebParams.NEW_PIN, Md5.hashMd5(newPin));
            params.put(WebParams.CONFIRM_PIN, Md5.hashMd5(confirmPin));
            params.put(WebParams.USER_ID, userID);

            Timber.d("isi params change pin:" + params.toString());

            MyApiClient.sentChangePin(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params change pin:"+response.toString());
                            Toast.makeText(ChangePIN.this, getString(R.string.changepin_toast_success), Toast.LENGTH_LONG).show();
                            finishChild();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout", response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(ChangePIN.this,message);
                        }
                        else {
                            Timber.d("isi error change pin:"+response.toString());
                            Toast.makeText(ChangePIN.this, message, Toast.LENGTH_LONG).show();

                            tv_title.setText(getResources().getString(R.string.changepin_text_currentpin));
                            getFragmentManager().beginTransaction().remove(createPin).commit();
                            setFragmentInsertPin();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        progdialog.dismiss();
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
                        Toast.makeText(ChangePIN.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ChangePIN.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    progdialog.dismiss();
                    Timber.w("Error Koneksi Change PIN", throwable.toString());

                    tv_title.setText(getResources().getString(R.string.changepin_text_currentpin));
                    getFragmentManager().beginTransaction().remove(createPin).commit();
                    setFragmentInsertPin();
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }
}
