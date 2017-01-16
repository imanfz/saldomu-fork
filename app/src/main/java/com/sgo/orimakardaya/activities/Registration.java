package com.sgo.orimakardaya.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.InetHandler;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.fragments.FirstScreen;
import com.sgo.orimakardaya.fragments.TermsNConditionWeb;
import com.sgo.orimakardaya.loader.UtilsLoader;

import timber.log.Timber;


public class Registration extends BaseActivity{

    private static Activity fa;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fa = this;

        if(InetHandler.isNetworkAvailable(this))
            new UtilsLoader(this).getAppVersion();

        if (findViewById(R.id.myfragment) != null) {
            if (savedInstanceState != null) {
                return;
            }

            FirstScreen fs = new FirstScreen();
            fs.setArguments(getIntent().getExtras());
            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.myfragment, fs,"fs");
            fragmentTransaction.commit();
        }
    }

    @Override
    protected int getLayoutResource() {

        return R.layout.activity_register;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }


    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.myfragment, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.myfragment, mFragment, fragName)
                    .commit();

        }

    }

    private void switchActivity(Intent mIntent) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        startActivity(mIntent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        TermsNConditionWeb mFrag = (TermsNConditionWeb)getSupportFragmentManager().findFragmentByTag(getString(R.string.termsncondition_title));
        if(mFrag !=null && mFrag.isVisible()) {
            Timber.d("Masukk onBackpressed");
            getSupportFragmentManager().popBackStack();
        }
        else if (!DefineValue.NOBACK) {
            super.onBackPressed();
        }
    }



    public void togglerBroadcastReceiver(Boolean _on, BroadcastReceiver _myreceiver){

        if(_on){
            Timber.wtf("masuk turnOnBR");
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            filter.addCategory("android.intent.category.DEFAULT");
            registerReceiver(_myreceiver,filter);
        }
        else {
            Timber.wtf("masuk turnOffBR");
            unregisterReceiver(_myreceiver);
        }

    }

    void showDialog(){
        // Create custom dialog object
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
//        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);
//        TextView Message2 = (TextView)dialog.findViewById(R.id.message_dialog2);Message2.setVisibility(View.VISIBLE);
        TextView Message3 = (TextView)dialog.findViewById(R.id.message_dialog3);Message3.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.regist2_notif_title));
//        Message.setText(getResources().getString(R.string.regist2_notif_message_1));
//        Message2.setText(noHPValue);
//        Message2.setTextSize(getResources().getDimension(R.dimen.abc_text_size_medium_material));
        Message3.setText(getResources().getString(R.string.regist2_notif_message_3));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeFragment(){
        Intent i = new Intent(this,LoginActivity.class);
        switchActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("isi request code:"+ String.valueOf(requestCode));

        Timber.d("isi result Code:" + String.valueOf(resultCode));
        /*if (requestCode == REQUEST_FINISH) {
            if (resultCode == RESULT_PIN) {
                Intent i = new Intent(this, CreatePIN.class);
                i.putExtra(CoreApp.REGISTRATION, true);
                switchActivity(i, ACTIVITY_RESULT);
            }
            if(resultCode == RESULT_LOGIN){
                showDialog();
            }
        }*/
        super.onActivityResult(requestCode,resultCode,data);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApiClient.CancelRequestWS(this,true);
    }
}
