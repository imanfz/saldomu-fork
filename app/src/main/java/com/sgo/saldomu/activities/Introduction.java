package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.commentModel;
import com.sgo.saldomu.Beans.likeModel;
import com.sgo.saldomu.Beans.listHistoryModel;
import com.sgo.saldomu.Beans.listTimeLineModel;
import com.sgo.saldomu.Beans.myFriendModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DeviceUtils;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.SMSDialog;
import com.sgo.saldomu.fragments.IntroPage;
import com.sgo.saldomu.fragments.Regist1;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.LoginCommunityModel;
import com.sgo.saldomu.models.retrofit.LoginModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.Md5;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.utils.LocaleManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
 Created by Lenovo Thinkpad on 12/21/2015.
 */
public class Introduction extends AppIntro implements EasyPermissions.PermissionCallbacks {
    private static final int RC_READPHONESTATE_GETACCOUNT_PERM = 500;
    private static final int RC_SENTSMS_PERM = 502;
    private SMSDialog smsDialog;
    private SMSclass smsclass;
    protected String extraSignature = "", userId;
    private String[] perms;
    private ProgressDialog progdialog;
    private Handler handler;

    private String timeDate, timeStamp, fcm_id, msg, msgFinal, imeiDevice;
    private SecurePreferences sp;
    protected Gson gson;
    JsonParser jsonParser;
    private Bundle argsBundleNextLogin = new Bundle();

    private final static int FIRST_SCREEN_LOGIN = 1;
    private final static int FIRST_SCREEN_INTRO = 2;
    private final static int FIRST_SCREEN_SPLASHSCREEN = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        if (InetHandler.isNetworkAvailable(this))
            new UtilsLoader(this).getAppVersion();
        AlertDialogLogout.getInstance();    //inisialisasi alertdialoglogout
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Timber.d("Token intro : " + token);
                        SecurePreferences.Editor mEditor = sp.edit();
                        mEditor.putString(DefineValue.FCM_ID, token);
                        mEditor.putString(DefineValue.FCM_ENCRYPTED, Md5.hashMd5(token));
                        mEditor.apply();
                    }
                });


//        if (sp.getString(DefineValue.USERID_PHONE, "").isEmpty()) {
//            addSlide(IntroPage.newInstance(R.layout.intro_fragment));
//            addSlide(IntroPage.newInstance(R.layout.intro_fragment));
//            addSlide(IntroPage.newInstance(R.layout.intro_fragment));
//        } else
        addSlide(IntroPage.newInstance(R.layout.intro_fragment));


        sp.edit().remove(DefineValue.SENDER_ID).commit();

        setFlowAnimation();
        Button skipbtn = (Button) skipButton;
        Button donebtn = (Button) doneButton;
        skipbtn.setText(getString(R.string.start_now));
        donebtn.setText("");
//        skipbtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
//        donebtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) {
            //cheat kalo diteken lama skip ke register (-1)
            skipbtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent i = new Intent(Introduction.this, LoginActivity.class);
                    i.putExtra(DefineValue.USER_IS_NEW, -1);
                    startActivity(i);
                    Introduction.this.finish();
                    return false;
                }
            });
            //cheat kalo diteken lama next ke Login (-2)
            donebtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent i = new Intent(Introduction.this, LoginActivity.class);
                    i.putExtra(DefineValue.USER_IS_NEW, -2);
                    startActivity(i);
                    Introduction.this.finish();
                    return false;
                }
            });
        }

        donebtn.setOnClickListener(POSlistener);
        skipbtn.setOnClickListener(VerifyListener);

        perms = new String[]{Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE};

        if (EasyPermissions.hasPermissions(this, perms)) {
//            InitializeSmsClass();
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.rational_readphonestate_readcontacts),
                    RC_READPHONESTATE_GETACCOUNT_PERM, perms);
        }
        SMSclass smsClass = new SMSclass(this);
        imeiDevice = smsClass.getDeviceIMEI();
    }

    private Button.OnClickListener VerifyListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
//            if(!sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID,"").isEmpty())
//            {
//                Intent i = new Intent(Introduction.this,LoginActivity.class);
//                i.putExtra(DefineValue.USER_IS_NEW,-2);
//                i.putExtra(DefineValue.IS_POS, "N");
//                startActivity(i);
//            }else {
//                Intent i = new Intent(Introduction.this, OTPVerificationActivity.class);
//                startActivity(i);
//            }
            sp.edit().putString(DefineValue.IS_POS, DefineValue.N).commit();
            if (!sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "").isEmpty()) {
                Intent i = new Intent(Introduction.this, InsertPIN.class);
                i.putExtra(DefineValue.IS_FORGOT_PASSWORD, true);
                i.putExtra(DefineValue.FOR_LOGIN, true);
                startActivityForResult(i, MainPage.REQUEST_FINISH);
            } else if (!sp.getString(DefineValue.FCM_ID, "").equals("")) {
                sendFCM();
            } else
                InitializeSmsDialog();
        }
    };

    private Button.OnClickListener POSlistener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(Introduction.this, LoginActivity.class);
            i.putExtra(DefineValue.USER_IS_NEW, -2);
            i.putExtra(DefineValue.IS_POS, "Y");
            sp.edit().putString(DefineValue.IS_POS, DefineValue.Y).commit();
            startActivity(i);
        }
    };

//    private void InitializeSmsClass(){
//        if(smsclass == null)
//            smsclass = new SMSclass(this);
//
//        smsDialog = new SMSDialog(this, new SMSDialog.DialogButtonListener() {
//            @Override
//            public void onClickOkButton(View v, boolean isLongClick) {
//                if (EasyPermissions.hasPermissions(Introduction.this,Manifest.permission.SEND_SMS)){
//                    smsDialog.sentSms();
//                }
//                else {
//                    EasyPermissions.requestPermissions(Introduction.this,
//                            getString(R.string.rational_sent_sms),
//                            RC_SENTSMS_PERM, Manifest.permission.SEND_SMS);
//                }
//            }
//
//            @Override
//            public void onClickCancelButton(View v, boolean isLongClick) {
//
//            }
//
//            @Override
//            public void onSuccess(int user_is_new) {
//                openLogin(user_is_new);
//            }
//
//            @Override
//            public void onSuccess(String product_value) {
//
//            }
//        });
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

//    private void doAction(){
//        if(InetHandler.isNetworkAvailable(this)) {
//            if (smsclass.isSimSameSP()) {
//                openLogin(-1);
//            } else {
//                smsDialog.show();
//            }
//        }
//        else DefinedDialog.showErrorDialog(this, getString(R.string.inethandler_dialog_message), null);
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (EasyPermissions.hasPermissions(this, perms))
//            checkIsSimExist();
//        else {
//            EasyPermissions.requestPermissions(this,
//                    getString(R.string.rational_readphonestate_readcontacts),
//                    RC_READPHONESTATE_GETACCOUNT_PERM, perms);
//        }
    }

    SecurePreferences getSP() {
        if (sp == null)
            sp = CustomSecurePref.getInstance().getmSecurePrefs();
        return sp;
    }

    private void sendFCM() {
        getSMSContent();
        fcm_id = sp.getString(DefineValue.FCM_ID, "");
        showProgLoading("", true);
        try {
            HashMap<String, Object> params = RetrofitService
                    .getInstance().getSignatureSecretKey(MyApiClient.LINK_FCM, "");
            params.put(WebParams.FCM_ID, fcm_id);
            params.put(WebParams.IMEI_ID, imeiDevice.toUpperCase());
            params.put(WebParams.REFERENCE_ID, sp.getString(DefineValue.SMS_CONTENT_ENCRYPTED, ""));
            Timber.d("isi params fcm:" + params.toString());
            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_FCM, params, new ResponseListener() {
                @Override
                public void onResponses(JsonObject object) {
                    Timber.d("isi response fcm:" + object);
                    InitializeSmsDialog();
                }

                @Override
                public void onError(Throwable throwable) {
                    Timber.d("isi error fcm intro:" + throwable);
                }

                @Override
                public void onComplete() {
                    showProgLoading("", false);
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void InitializeSmsDialog() {
        smsDialog = SMSDialog.newDialog(timeDate, checkFailedVerify(), new SMSDialog.DialogButtonListener() {
            @Override
            public void onClickOkButton(View v, boolean isLongClick) {

            }

            @Override
            public void onClickCancelButton(View v, boolean isLongClick) {

            }

            @Override
            public void onSuccess(int user_is_new) {
                if (user_is_new == 1) {
                    openLogin(1);
                } else {
                    openLogin(-2);
                }
            }

            @Override
            public void onSuccess(String product_value) {


            }
        });
        smsDialog.show(getSupportFragmentManager(), "");
    }

    private void checkIsSimExist() {
        if (smsclass != null) {
            smsclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
                @Override
                public void sim_state(Boolean isExist, String msg) {
                    if (!isExist && !Introduction.this.isFinishing()) {
                        DefinedDialog.showErrorDialog(Introduction.this, msg, new DefinedDialog.DialogButtonListener() {
                            @Override
                            public void onClickButton(View v, boolean isLongClick) {
                                finish();
                            }
                        });
                    }
                }
            });
        }
    }

    void showProgLoading(String msg, boolean show) {
        if (show) {
            progdialog = DefinedDialog.CreateProgressDialog(this, msg);
            progdialog.show();
        } else {
            progdialog.dismiss();
        }

    }

    @Override
    public void onSkipPressed() {
//        doAction();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
//        doAction();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    protected void onDestroy() {
        if (smsclass != null)
            smsclass.Close();
        super.onDestroy();
    }

    private void openLogin(int user_is_new) {

        Intent i = new Intent(this, LoginActivity.class);
        if (user_is_new != -1)
            i.putExtra(DefineValue.USER_IS_NEW, user_is_new);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_READPHONESTATE_GETACCOUNT_PERM:
                for (int i = 0; i < perms.size(); i++) {
                    if (perms.get(i).equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE)) {
//                        InitializeSmsClass();
                    }
                }
                break;
//            case RC_SENTSMS_PERM:
//                smsDialog.sentSms();
//                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_READPHONESTATE_GETACCOUNT_PERM:
                Toast.makeText(this, getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
                finish();
                break;
//            case RC_SENTSMS_PERM:
//                smsDialog.dismiss();
//                smsDialog.reset();
//                break;
        }
    }

    boolean checkFailedVerify() {
        String temp_iccid = getSP().getString(DefineValue.TEMP_ICCID, "");
        String temp_imei = getSP().getString(DefineValue.TEMP_IMEI, "");
        boolean temp_is_sent = getSP().getBoolean(DefineValue.TEMP_IS_SENT, false);

        if (!temp_iccid.equals("") && !temp_imei.equals("")) {
            String diccid = smsclass.getDeviceICCID();
            String dimei = smsclass.getDeviceIMEI();
            boolean biccid = diccid.equalsIgnoreCase(temp_iccid);
            boolean bimei = dimei.equalsIgnoreCase(temp_imei);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", new Locale("ID", "INDONESIA"));
            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.SECOND, 10);


            boolean ddate = false;
            try {
                Date savedDate, currDate = Calendar.getInstance().getTime();
                savedDate = df.parse(getSP().getString(DefineValue.LAST_SMS_SENT, ""));
//                currDate = df.parse(getSP().getString(DefineValue.LAST_SMS_SENT, ""));

                currDate.setTime(cal.getTimeInMillis());

                Long a = currDate.getTime(), b = savedDate.getTime();
                Long calc = a - b;

//                ddate = currDate.compareTo(savedDate) > 0;
                Long sec = TimeUnit.MILLISECONDS.toMinutes(calc);
                ddate = sec < 30;
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return biccid && bimei && temp_is_sent && ddate;
        } else return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
                pinLogin(value_pin);
            }
            if (resultCode == InsertPIN.RESULT_FINGERPRINT_LOGIN) {
                fingerprintLogin();
            }
        }
    }

    private void pinLogin(String value_pin) {
        showProgLoading("Sending Data", true);
        extraSignature = sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "") + value_pin;
        HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_PIN_LOGIN, extraSignature);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.USER_ID, sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""));
        params.put(WebParams.USER_PIN, sp.getString(DefineValue.PIN_CODE, ""));
        params.put(WebParams.RC_DATETIME, DateTimeFormat.getCurrentDateTime());
        params.put(WebParams.MAC_ADDR, new DeviceUtils().getWifiMcAddress());
        params.put(WebParams.DEV_MODEL, new DeviceUtils().getDeviceModelID());
        params.put(WebParams.FCM_ID, sp.getString(DefineValue.FCM_ID, ""));
        params.put(WebParams.IS_POS, sp.getString(DefineValue.IS_POS, "N"));
        params.put(WebParams.IMEI_ID, imeiDevice.toUpperCase());
        Timber.d("isi param pin login:" + params);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_PIN_LOGIN, params, new ObjListeners() {
            @Override
            public void onResponses(JSONObject response) {
                try {
                    String errorCode = response.getString(WebParams.ERROR_CODE);
                    if (errorCode.equals(WebParams.SUCCESS_CODE)) {
                        LoginModel loginModel = getGson().fromJson(response.toString(), LoginModel.class);

                        Toast.makeText(getApplicationContext(), getString(R.string.login_toast_loginsukses), Toast.LENGTH_LONG).show();
                        setLoginProfile(loginModel);
                    } else if (errorCode.equals("0324")) {
                        sp.edit().remove(DefineValue.PREVIOUS_LOGIN_USER_ID).apply();
                        getSMSContent();
                        InitializeSmsDialog();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getApplicationContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                showProgLoading("", false);
            }
        });
    }

    private void fingerprintLogin() {
        showProgLoading("Sending Data", true);
        extraSignature = sp.getString(DefineValue.EXTRA_SIGNATURE, "");
        String userID = NoHPFormat.formatTo62(sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""));
        HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_LOGIN, extraSignature);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.USER_ID, userID);
        params.put(WebParams.PASSWORD_LOGIN, sp.getString(DefineValue.USER_PASSWORD, ""));
        params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
        params.put(WebParams.MAC_ADDR, new DeviceUtils().getWifiMcAddress());
        params.put(WebParams.DEV_MODEL, new DeviceUtils().getDeviceModelID());
        params.put(WebParams.IMEI_ID, imeiDevice.toUpperCase());
        params.put(WebParams.IS_POS, "N");
        if (sp.getString(DefineValue.FCM_ID, "") != null)
            params.put(WebParams.FCM_ID, sp.getString(DefineValue.FCM_ID, ""));

        Timber.d("isi params login:" + params.toString());

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LOGIN, params, new ResponseListener() {
            @Override
            public void onResponses(JsonObject response) {
                LoginModel loginModel = getGson().fromJson(response.toString(), LoginModel.class);
                String code = loginModel.getError_code();

                if (code.equalsIgnoreCase(WebParams.SUCCESS_CODE)) {
                    sp.edit().putString(DefineValue.IS_POS, "N").commit();
                    sp.edit().putString(DefineValue.EXTRA_SIGNATURE, extraSignature).commit();
                    Toast.makeText(Introduction.this, getString(R.string.login_toast_loginsukses), Toast.LENGTH_LONG).show();
                    setLoginProfile(loginModel);
                } else if (code.equals(DefineValue.ERROR_0042)) {
                    String message;
                    int failed = Integer.valueOf(loginModel.getFailedAttempt());
                    int max = Integer.valueOf(loginModel.getMaxFailed());

                    if (max - failed == 0) {
                        message = getString(R.string.login_failed_attempt_3);
                    } else {
                        message = getString(R.string.login_failed_attempt_1, max - failed);
                    }

                    showDialog(message);
                } else if (code.equals(DefineValue.ERROR_0126)) {
                    showDialog(getString(R.string.login_failed_attempt_3));
                } else if (code.equals(DefineValue.ERROR_0018) || code.equals(DefineValue.ERROR_0017)) {
                    showDialog(getString(R.string.login_failed_inactive));
                } else if (code.equals(DefineValue.ERROR_0127)) {
                    showDialog(getString(R.string.login_failed_dormant));
                } else if (code.equals(DefineValue.ERROR_0004)) {
                    String msg = loginModel.getError_message();
                    if (msg != null && !msg.isEmpty()) {
                        showDialog(msg);
                    } else
                        showDialog(getString(R.string.login_failed_wrong_pass));
                } else if (code.equals(DefineValue.ERROR_0002)) {
                    showDialog(getString(R.string.login_failed_wrong_id));
                } else if (code.equals(DefineValue.ERROR_9333)) {
                    Timber.d("isi response app data:" + loginModel.getApp_data());
                    final AppDataModel appModel = loginModel.getApp_data();
                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                    alertDialogUpdateApp.showDialogUpdate(Introduction.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                } else if (code.equals(DefineValue.ERROR_0066)) {
                    Timber.d("isi response maintenance:" + response.toString());
                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                    alertDialogMaintenance.showDialogMaintenance(Introduction.this, loginModel.getError_message());
                } else {
                    Toast.makeText(Introduction.this, loginModel.getError_message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                showProgLoading("", false);
            }
        });
    }

    private void getSMSContent() {
        String SMS_VERIFY = "REG EMO " + MyApiClient.COMM_ID;
        SMSclass smSclass = new SMSclass(getApplicationContext());
        timeStamp = String.valueOf(DateTimeFormat.getCurrentDateTimeMillis());
        sp.edit().putString(DefineValue.TIMESTAMP, timeStamp).apply();
        timeDate = String.valueOf(DateTimeFormat.getCurrentDateTimeSMS());
        imeiDevice = smSclass.getDeviceIMEI();
        String ICCIDDevice = smSclass.getDeviceICCID();
        Timber.wtf("device imei/ICCID : " + imeiDevice + "/" + ICCIDDevice);
        msg = (SMS_VERIFY + " " + imeiDevice + "_" + ICCIDDevice + "_" + timeStamp + "_" + MyApiClient.APP_ID + "_" + timeDate + "_").toUpperCase();
        String msg_hashed = Md5.hashMd5(msg).toUpperCase();
        msgFinal = msg + msg_hashed;
        Timber.wtf("content sms: " + msgFinal);

        sp.edit().putString(DefineValue.SMS_CONTENT, msg).apply();
        sp.edit().putString(DefineValue.SMS_CONTENT_ENCRYPTED, msg_hashed).apply();
    }

    protected Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private void setLoginProfile(LoginModel model) {

        try {
            SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
            SecurePreferences.Editor mEditor = prefs.edit();
            userId = model.getUserId();
            String prevContactFT = prefs.getString(DefineValue.PREVIOUS_CONTACT_FIRST_TIME, "");

            if (prefs.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "").equals(userId)) {
                mEditor.putString(DefineValue.CONTACT_FIRST_TIME, prevContactFT);
                mEditor.putString(DefineValue.BALANCE_AMOUNT, prefs.getString(DefineValue.PREVIOUS_BALANCE, "0"));
                mEditor.putBoolean(DefineValue.IS_SAME_PREVIOUS_USER, true);
            } else {
                if (prevContactFT.equals(DefineValue.NO)) {
                    myFriendModel.deleteAll();
                    mEditor.putString(DefineValue.CONTACT_FIRST_TIME, DefineValue.YES);
                }
                mEditor.putString(DefineValue.BALANCE_AMOUNT, "0");
                mEditor.putBoolean(DefineValue.IS_SAME_PREVIOUS_USER, false);
                BBSDataManager.resetBBSData();
            }

            mEditor.putString(DefineValue.USERID_PHONE, userId);
            mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_YES);
            mEditor.putString(DefineValue.USER_NAME, model.getUserName());
            mEditor.putString(DefineValue.CUST_ID, model.getCustId());
            mEditor.putString(DefineValue.CUST_NAME, model.getCustName());
            mEditor.putString(DefineValue.IS_MEMBER_SHOP_DGI, model.getIs_member_shop_dgi());

            mEditor.putString(DefineValue.PROFILE_DOB, model.getDateOfBirth());
            mEditor.putString(DefineValue.PROFILE_ADDRESS, model.getAddress());
            mEditor.putString(DefineValue.PROFILE_BIO, model.getBio());
            mEditor.putString(DefineValue.PROFILE_COUNTRY, model.getCountry());
            mEditor.putString(DefineValue.PROFILE_EMAIL, model.getEmail());
            mEditor.putString(DefineValue.PROFILE_FULL_NAME, model.getFullName());
            mEditor.putString(DefineValue.PROFILE_SOCIAL_ID, model.getSocialId());
            mEditor.putString(DefineValue.PROFILE_HOBBY, model.getHobby());
            mEditor.putString(DefineValue.PROFILE_POB, model.getBirthPlace());
            mEditor.putString(DefineValue.PROFILE_GENDER, model.getGender());
            mEditor.putString(DefineValue.PROFILE_ID_TYPE, model.getIdtype());
            mEditor.putString(DefineValue.PROFILE_VERIFIED, model.getVerified());
            mEditor.putString(DefineValue.PROFILE_BOM, model.getMotherName());

            mEditor.putString(DefineValue.LIST_ID_TYPES, getGson().toJson(model.getIdTypes()));

            mEditor.putString(DefineValue.IS_FIRST, model.getUserIsNew());
            mEditor.putString(DefineValue.IS_CHANGED_PASS, model.getChangedPass());

            mEditor.putString(DefineValue.IMG_URL, model.getImgUrl());
            mEditor.putString(DefineValue.IMG_SMALL_URL, model.getImgSmallUrl());
            mEditor.putString(DefineValue.IMG_MEDIUM_URL, model.getImgMediumUrl());
            mEditor.putString(DefineValue.IMG_LARGE_URL, model.getImgLargeUrl());

            mEditor.putString(DefineValue.ACCESS_KEY, model.getAccessKey());
            mEditor.putString(DefineValue.ACCESS_SECRET, model.getAccessSecret());

            mEditor.putString(DefineValue.LINK_APP, model.getSocialSignature());
            mEditor.putString(DefineValue.IS_DORMANT, model.getIs_dormant());

            if (Integer.valueOf(model.getIsRegistered()) == 0)
                mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
            else
                mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, true);

            if (!model.getCommunity().isEmpty()) {
                mEditor.putInt(DefineValue.COMMUNITY_LENGTH, model.getCommunity().size());
                for (int i = 0; i < model.getCommunity().size(); i++) {
                    LoginCommunityModel commModel = model.getCommunity().get(i);
                    if (commModel.getCommId().equals(MyApiClient.COMM_ID)) {
                        mEditor.putString(DefineValue.COMMUNITY_ID, commModel.getCommId());
                        mEditor.putString(DefineValue.CALLBACK_URL_TOPUP, commModel.getCallbackUrl());
                        mEditor.putString(DefineValue.API_KEY_TOPUP, commModel.getApiKey());
                        mEditor.putString(DefineValue.COMMUNITY_CODE, commModel.getCommCode());
                        mEditor.putString(DefineValue.COMMUNITY_NAME, commModel.getCommName());
                        mEditor.putString(DefineValue.BUSS_SCHEME_CODE, commModel.getBussSchemeCode());
                        mEditor.putString(DefineValue.AUTHENTICATION_TYPE, commModel.getAuthenticationType());
                        mEditor.putString(DefineValue.LENGTH_AUTH, commModel.getLengthAuth());
                        mEditor.putString(DefineValue.IS_HAVE_PIN, commModel.getIsHavePin());
                        mEditor.putString(DefineValue.AGENT_TYPE, commModel.getAgent_type());
                        mEditor.putString(DefineValue.COMPANY_TYPE, commModel.getCompany_type());
                        mEditor.putString(DefineValue.FORCE_CHANGE_PIN, commModel.getForce_change_pin());
                        mEditor.remove(DefineValue.SENDER_ID);

                        mEditor.putInt(DefineValue.LEVEL_VALUE, Integer.valueOf(commModel.getMemberLevel()));
                        if (commModel.getAllowMemberLevel().equals(DefineValue.STRING_YES)) {

                            mEditor.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL, true);
                        } else
                            mEditor.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);

                        mEditor.putString(DefineValue.IS_NEW_BULK, commModel.getIsNewBulk());

                        mEditor.putBoolean(DefineValue.IS_AGENT, commModel.getIsAgent() > 0);

                        String arrJson = toJson(commModel.getAgent_scheme_codes()).toString();
                        mEditor.putString(DefineValue.AGENT_SCHEME_CODES, arrJson);
                        mEditor.putString(DefineValue.IS_AGENT_TRX_REQ, commModel.getIs_agent_trx_request());
                        mEditor.putString(DefineValue.COMM_UPGRADE_MEMBER, commModel.getComm_upgrade_member());
                        mEditor.putString(DefineValue.MEMBER_CREATED, commModel.getMember_created());

                        break;
                    }
                }
            }

            if (!model.getShopIdAgent().equals("")) {
                mEditor.putString(DefineValue.IS_AGENT_SET_LOCATION, DefineValue.STRING_NO);
                mEditor.putString(DefineValue.IS_AGENT_SET_OPENHOUR, DefineValue.STRING_NO);
                mEditor.putString(DefineValue.SHOP_AGENT_DATA, getGson().toJson(model.getShopIdAgent()));
            }

            if (model.getSettings() != null) {
                mEditor.putInt(DefineValue.MAX_MEMBER_TRANS, Integer.valueOf(model.getSettings().get(0).getMaxMemberTransfer()));

            }

            mEditor.apply();

            changeActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected JsonElement toJson(Object model) {
        return getJsonParser().parse(getGson().toJson(model));
    }

    protected JsonParser getJsonParser() {
        if (jsonParser == null) {
            jsonParser = new JsonParser();
        }
        return jsonParser;
    }

    private void changeActivity() {
        Intent i = new Intent(Introduction.this, MainPage.class);
        if (argsBundleNextLogin != null)
            i.putExtras(argsBundleNextLogin);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Timber.d("Logging attachBaseContext.....");
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    private void showDialog(String message) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.login_failed_attempt_title));
        Message.setText(message);


        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean checkCommunity(List<LoginCommunityModel> model) {
        if (model != null) {
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).getCommId().equals(MyApiClient.COMM_ID)) {
                    Timber.w("check comm id yg bener: " + model.get(i).getCommId());
                    return true;
                }
            }
        }

        Toast.makeText(getApplicationContext(), getString(R.string.login_validation_comm), Toast.LENGTH_LONG).show();
        return false;
    }

    public void switchLogout() {
        sentLogout();
    }

    private void sentLogout() {
        try {
            if (progdialog == null) {
                progdialog = DefinedDialog.CreateProgressDialog(this, getString(R.string.please_wait));
                progdialog.show();
            }

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LOGOUT);
//            RequestParams params = MyApiClient.getInstance().getSignatureWithParams(MyApiClient.LINK_LOGOUT);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""));

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LOGOUT, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(object, jsonModel.class);

                            if (model.getError_code().equals(WebParams.SUCCESS_CODE)) {
                                //stopService(new Intent(MainPage.this, UpdateLocationService.class));
                                Logout(FIRST_SCREEN_INTRO);

                            } else {
                                Toast.makeText(Introduction.this, model.getError_message(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Introduction.this.finish();
                        }

                        @Override
                        public void onComplete() {
                            if (progdialog != null) {
                                if (progdialog.isShowing())
                                    progdialog.dismiss();
                            }
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void Logout(int logoutTo) {

        String balance = sp.getString(DefineValue.BALANCE_AMOUNT, "");
        String contact_first_time = sp.getString(DefineValue.CONTACT_FIRST_TIME, "");
        deleteData();
        SecurePreferences.Editor mEditor = sp.edit();
        mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (sp.getString(DefineValue.IS_POS, "N").equals(DefineValue.N)) {
            mEditor.putString(DefineValue.PREVIOUS_LOGIN_USER_ID, userId);
        } else
            mEditor.putString(DefineValue.PREVIOUS_LOGIN_USER_ID, "");
        mEditor.putString(DefineValue.PREVIOUS_BALANCE, balance);
        mEditor.putString(DefineValue.PREVIOUS_CONTACT_FIRST_TIME, contact_first_time);

        mEditor.putString(DefineValue.IS_AGENT_APPROVE, "");
        mEditor.putString(DefineValue.AGENT_NAME, "");
        mEditor.putString(DefineValue.AGENT_SHOP_CLOSED, "");
        mEditor.putString(DefineValue.BBS_MEMBER_ID, "");
        mEditor.putString(DefineValue.BBS_SHOP_ID, "");
        mEditor.putString(DefineValue.IS_AGENT_SET_LOCATION, "");
        mEditor.putString(DefineValue.IS_AGENT_SET_OPENHOUR, "");
        mEditor.putString(DefineValue.SHOP_AGENT_DATA, "");
        mEditor.putString(DefineValue.IS_MEMBER_SHOP_DGI, "");
        mEditor.putString(DefineValue.IS_POS, "");
        mEditor.remove(DefineValue.IS_DORMANT);
        mEditor.remove(DefineValue.IS_REGISTERED_LEVEL);
        mEditor.remove(DefineValue.CATEGORY);
        mEditor.remove(DefineValue.SAME_BANNER);
        mEditor.remove(DefineValue.DATA_BANNER);
        mEditor.remove(DefineValue.IS_POS);
        mEditor.remove(DefineValue.COMM_UPGRADE_MEMBER);
        mEditor.remove(DefineValue.MEMBER_CREATED);
        mEditor.remove(DefineValue.LAST_CURRENT_LONGITUDE);
        mEditor.remove(DefineValue.LAST_CURRENT_LATITUDE);
        mEditor.remove(DefineValue.COMPANY_TYPE);
        mEditor.remove(DefineValue.SMS_CONTENT);
        mEditor.remove(DefineValue.SMS_CONTENT_ENCRYPTED);
        mEditor.remove(DefineValue.PROFILE_DOB);
        mEditor.remove(DefineValue.IS_INQUIRY_SMS);

        //di commit bukan apply, biar yakin udah ke di write datanya
        mEditor.commit();
        openFirstScreen(logoutTo);
    }

    private void openFirstScreen(int index) {
        Intent i;
        switch (index) {
            case FIRST_SCREEN_LOGIN:
                i = new Intent(this, LoginActivity.class);
                break;
            case FIRST_SCREEN_INTRO:
                i = new Intent(this, Introduction.class);
                break;
            case FIRST_SCREEN_SPLASHSCREEN:
                if (LocaleManager.getLocale(getResources()).getLanguage().equals("in")) {
                    CustomSecurePref.getInstance().setBoolean(DefineValue.IS_BAHASA, true);
                } else {
                    CustomSecurePref.getInstance().setBoolean(DefineValue.IS_BAHASA, false);
                }
                i = new Intent(this, SplashScreen.class);
                break;
            default:
                i = new Intent(this, LoginActivity.class);
                break;
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        this.finish();
    }

    private void deleteData() {
        CustomSecurePref.getInstance().ClearAllCustomData();
        listTimeLineModel.deleteAll();
        listHistoryModel.deleteAll();
        commentModel.deleteAll();
        likeModel.deleteAll();
    }

}
