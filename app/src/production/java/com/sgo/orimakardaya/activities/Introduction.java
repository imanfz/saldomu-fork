package com.sgo.orimakardaya.activities;

import android.Manifest;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.sgo.orimakardaya.BuildConfig;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.SMSclass;
import com.sgo.orimakardaya.dialogs.AlertDialogFrag;
import com.sgo.orimakardaya.dialogs.SMSDialog;
import com.sgo.orimakardaya.fragments.IntroPage;

import timber.log.Timber;


public class Introduction extends AppIntro {

    SMSDialog smsDialog;
    SMSclass smSclass;
    AlertDialogFrag chargeDialog;
    private static final int PERMISSIONS_REQ_READPHONESTATE = 0x123;
    private static final int PERMISSIONS_SEND_SMS = 0x124;


    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQ_READPHONESTATE);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }

        chargeDialog = AlertDialogFrag.newInstance(null,getString(R.string.appname)+ " " +getString(R.string.dialog_sms_msg),
                getString(R.string.sent), getString(R.string.cancel), false);

        chargeDialog.setOkListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                smsDialog.sentSms();
            }
        });
        chargeDialog.setCancelListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                smsDialog.reset();
            }
        });

        addSlide(IntroPage.newInstance(R.layout.intro1_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro2_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro3_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro4_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro5_fragment));
        addSlide(IntroPage.newInstance(R.layout.intro6_fragment));

        setFlowAnimation();
        Button skipbtn = (Button)skipButton;
        Button donebtn = (Button)doneButton;
        skipbtn.setText(getString(R.string.start_now));
        donebtn.setText(getString(R.string.done));


        smSclass = new SMSclass(this);
        smsDialog = new SMSDialog(this, new SMSDialog.DialogButtonListener() {
            @Override
            public void onClickOkButton(View v, boolean isLongClick) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Introduction.this.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Introduction.this.requestPermissions(new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_SEND_SMS);
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                }
                else {
                    chargeDialog.show(getSupportFragmentManager(),AlertDialogFrag.TAG);
                }
            }

            @Override
            public void onClickCancelButton(View v, boolean isLongClick) {

            }

            @Override
            public void onSuccess(int user_is_new) {
                openLogin(user_is_new);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQ_READPHONESTATE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
                smsDialog.dismiss();
            }
        }
        else if(requestCode == PERMISSIONS_SEND_SMS){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                smsDialog.sentSms();
            } else {
                Toast.makeText(this, getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("isinya : "+ String.valueOf(requestCode) +"/"+ String.valueOf(resultCode));
    }

    @Override
    public void onSkipPressed() {
        doAction();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        doAction();
    }

    private void doAction(){
        if(smSclass.isSimSameSP()){
            openLogin(-1);
        }
        else {
            smsDialog.show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onAttachFragment(android.support.v4.app.Fragment fragment) {
        super.onAttachFragment(fragment);

        smsclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if(!isExist){
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }


    private void openLogin(int user_is_new){
        if(smsDialog.isShowing())
            smsDialog.dismiss();
        smsDialog.dismiss();
        Intent i = new Intent(this,LoginActivity.class);
        if(user_is_new != -1)
            i.putExtra(DefineValue.USER_IS_NEW,user_is_new);
        startActivity(i);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
