package com.sgo.orimakardaya.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.InetHandler;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.fragments.FirstScreen;
import com.sgo.orimakardaya.fragments.TermsNConditionWeb;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;


public class Registration extends BaseActivity{

    public static final int REQUEST_EXIT = 0 ;
    public static final int RESULT_PIN = 1 ;
    public static final int RESULT_NORMAL = 2 ;
    public static final int RESULT_FINISHING = 5 ;
    public static final int ACTIVITY_RESULT = 3;

    android.support.v4.app.FragmentManager fragmentManager;

    public static Activity fa;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fa = this;

        if(InetHandler.isNetworkAvailable(this))
            getAppVersion();

        if (findViewById(R.id.myfragment) != null) {
            if (savedInstanceState != null) {
                return;
            }

            FirstScreen fs = new FirstScreen();
            fs.setArguments(getIntent().getExtras());
            fragmentManager = getSupportFragmentManager();
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

    public void switchActivity(Intent mIntent) {
        getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    public void getAppVersion(){
        try{
            Timber.d("getAppVersionRegistration");
            MyApiClient.getAppVersion(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response get App Version:"+ response.toString());
                            String arrayApp = response.getString(WebParams.APP_DATA);
                            if(!arrayApp.isEmpty() && !arrayApp.equalsIgnoreCase(null)) {
                                JSONObject mObject = null;
                                if (arrayApp != null)
                                    mObject = new JSONObject(arrayApp);


                                if (mObject != null) {
                                    String package_version;
                                    package_version = mObject.getString(WebParams.PACKAGE_VERSION);

                                    final String package_name = mObject.getString(WebParams.PACKAGE_NAME);
                                    final String type = mObject.getString(WebParams.TYPE);
                                    Timber.d("Isi Version Name / version code:"+ DefineValue.VERSION_NAME + " / " + DefineValue.VERSION_CODE);
                                    if (!package_version.equals(DefineValue.VERSION_NAME)) {
                                        if (!isFinishing()) {
                                            final JSONObject finalMObject = mObject;
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this)
                                                    .setTitle("Update")
                                                    .setMessage("Application is out of date,  Please update immediately")
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            if (type.equalsIgnoreCase("1")) {
                                                                try {
                                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                                                                }
                                                            } else if (type.equalsIgnoreCase("2")) {
                                                                String download_url = "";
                                                                try {
                                                                    download_url = finalMObject.getString(WebParams.DOWNLOAD_URL);
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                if (!Patterns.WEB_URL.matcher(download_url).matches())
                                                                    download_url = "http://www.google.com";
                                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(download_url)));
                                                            }
                                                            Registration.this.finish();
                                                            android.os.Process.killProcess(android.os.Process.myPid());
                                                            System.exit(0);
                                                            getParent().finish();
                                                        }
                                                    });
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                        }
                                    }
                                }
                            }
                        } else if (code.equals("0381")) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this)
                                    .setTitle("Maintenance")
                                    .setMessage(message)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Registration.this.finish();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                            getParent().finish();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
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
                    Timber.w("Error Koneksi app version registration:"+ throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
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
                changeFragment(true);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void changeFragment(Boolean submit){
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

    public void switchActivity(Intent mIntent, int activity_type) {
        switch (activity_type){
            case ACTIVITY_RESULT:
                startActivityForResult(mIntent, REQUEST_EXIT);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApiClient.CancelRequestWS(this,true);
    }
}
