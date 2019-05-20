package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DeviceUtils;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.SMSDialog;
import com.sgo.saldomu.fragments.IntroPage;

import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.loader.UtilsLoader;
import com.sgo.saldomu.securities.Md5;

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
public class Introduction extends AppIntro implements EasyPermissions.PermissionCallbacks{
    private static final int RC_READPHONESTATE_GETACCOUNT_PERM = 500;
    private static final int RC_SENTSMS_PERM = 502;
    private SMSDialog smsDialog;
    private SMSclass smsclass;
    protected String extraSignature = "";
    private String[] perms;
    private ProgressDialog progdialog;

    Long timeDate;
    SecurePreferences sp;
    Calendar calendar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        if(InetHandler.isNetworkAvailable(this))
            new UtilsLoader(this).getAppVersion();

        addSlide(IntroPage.newInstance(R.layout.intro_fragment));


        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        sp.edit().remove(DefineValue.SENDER_ID).commit();
        calendar = Calendar.getInstance();
        timeDate = calendar.getTimeInMillis();

        setFlowAnimation();
        Button skipbtn = (Button)skipButton;
        Button donebtn = (Button)doneButton;
        skipbtn.setText(getString(R.string.start_now));
        donebtn.setText("POS");
        skipbtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        donebtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        if(BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) {
            //cheat kalo diteken lama skip ke register (-1)
            skipbtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent i = new Intent(Introduction.this,LoginActivity.class);
                    i.putExtra(DefineValue.USER_IS_NEW,-1);
                    startActivity(i);
                    Introduction.this.finish();
                    return false;
                }
            });
            //cheat kalo diteken lama next ke Login (-2)
            donebtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent i = new Intent(Introduction.this,LoginActivity.class);
                    i.putExtra(DefineValue.USER_IS_NEW,-2);
                    startActivity(i);
                    Introduction.this.finish();
                    return false;
                }
            });
        }

        donebtn.setOnClickListener(POSlistener);
        skipbtn.setOnClickListener(VerifyOTPListener);

        perms = new String[]{Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE};

        if (EasyPermissions.hasPermissions(this, perms)) {
//            InitializeSmsClass();
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.rational_readphonestate_readcontacts),
                    RC_READPHONESTATE_GETACCOUNT_PERM, perms);
        }

    }

    private Button.OnClickListener VerifyOTPListener = new Button.OnClickListener(){
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
            sendFCM();
        }
    };

    private Button.OnClickListener POSlistener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(Introduction.this,LoginActivity.class);
                    i.putExtra(DefineValue.USER_IS_NEW,-2);
                    i.putExtra(DefineValue.IS_POS, "Y");
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
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
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
    SecurePreferences getSP(){
        if (sp == null)
            sp = CustomSecurePref.getInstance().getmSecurePrefs();
        return sp;
    }

    private void sendFCM() {
        showProgLoading("",true);
        String fcm_id=CustomSecurePref.getInstance().getmSecurePrefs().getString(DefineValue.FCM_SERVER_UUID,"");
        try {
            HashMap<String,Object> params= RetrofitService
                    .getInstance().getSignatureSecretKey(MyApiClient.LINK_FCM,"");
            params.put(WebParams.FCM_ID,fcm_id);
            params.put(WebParams.REFERENCE_ID, Md5.hashMd5(fcm_id));
            Timber.d("isi params fcm:" + params.toString());
            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_FCM, params, new ResponseListener() {
                @Override
                public void onResponses(JsonObject object) {
                    Timber.d("isi response fcm:"+object);
                    InitializeSmsDialog();
                }

                @Override
                public void onError(Throwable throwable) {
                    Timber.d("isi error fcm:"+throwable);
                }

                @Override
                public void onComplete() {
                    showProgLoading("",false);
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void InitializeSmsDialog() {
        smsDialog= SMSDialog.newDialog(timeDate, checkFailedVerify(), new SMSDialog.DialogButtonListener() {
                    @Override
                    public void onClickOkButton(View v, boolean isLongClick) {

                    }

                    @Override
                    public void onClickCancelButton(View v, boolean isLongClick) {

                    }

                    @Override
                    public void onSuccess(int user_is_new) {

                    }

                    @Override
                    public void onSuccess(String product_value) {

                    }
                });
                smsDialog.show(getSupportFragmentManager(), "");
    }

    private void checkIsSimExist() {
        if(smsclass != null) {
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
    void showProgLoading(String msg,boolean show) {
        if (show){
            progdialog = DefinedDialog.CreateProgressDialog(this, msg);
            progdialog.show();
        }else{
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
        if (smsclass!=null)
            smsclass.Close();
        super.onDestroy();
    }

    private void openLogin(int user_is_new){

        Intent i = new Intent(this,LoginActivity.class);
        if(user_is_new != -1)
            i.putExtra(DefineValue.USER_IS_NEW,user_is_new);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch(requestCode) {
            case RC_READPHONESTATE_GETACCOUNT_PERM:
                for (int i = 0 ; i < perms.size() ; i++){
                    if(perms.get(i).equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE)) {
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
    boolean checkFailedVerify(){
        String temp_iccid = getSP().getString(DefineValue.TEMP_ICCID, "");
        String temp_imei = getSP().getString(DefineValue.TEMP_IMEI, "");
        boolean temp_is_sent = getSP().getBoolean(DefineValue.TEMP_IS_SENT, false);

        if(!temp_iccid.equals("") && !temp_imei.equals("")){
            String diccid = smsclass.getDeviceICCID();
            String dimei = smsclass.getDeviceIMEI();
            boolean biccid = diccid.equalsIgnoreCase(temp_iccid);
            boolean bimei = dimei.equalsIgnoreCase(temp_imei);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", new Locale("ID","INDONESIA"));
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
        }else return false;
    }

}
