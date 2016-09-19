package com.sgo.orimakardaya.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Patterns;
import android.view.Menu;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sgo.orimakardaya.BuildConfig;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.InetHandler;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.fragments.Login;
import com.sgo.orimakardaya.fragments.Regist1;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 11/4/2014.
 */
public class LoginActivity extends BaseActivity {

    public static final int REQUEST_EXIT = 0 ;
    public static final int RESULT_PIN = 1 ;
    public static final int RESULT_NORMAL = 2 ;
    public static final int RESULT_FINISHING = 5 ;
    public static final int ACTIVITY_RESULT = 3;

    FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(InetHandler.isNetworkAvailable(this))
            getAppVersion();

        if (findViewById(R.id.loginContent) != null) {
            if (savedInstanceState != null) {
                return;
            }


            Login login = new Login();
            fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.loginContent, login,"login");
            fragmentTransaction.commit();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LoginActivity.ACTIVITY_RESULT){
            if(resultCode == LoginActivity.RESULT_FINISHING)
                this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void switchContent(Fragment mFragment,String fragName,Boolean isBackstack) {

        if(isBackstack){
            Timber.d("backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.loginContent, mFragment, fragName)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            Timber.d("bukan backstack");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.loginContent, mFragment, fragName)
                    .commit();

        }
    }

    public void switchActivity(Intent mIntent, int activity_type) {
        switch (activity_type){
            case ACTIVITY_RESULT:
                startActivityForResult(mIntent, REQUEST_EXIT);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentByTag("reg2") == null)
            super.onBackPressed();
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
                            Timber.d("Isi response get App Version:" + response.toString());
                            String arrayApp = response.getString(WebParams.APP_DATA);
                            if (!arrayApp.isEmpty() && !arrayApp.equalsIgnoreCase(null)) {
                                JSONObject mObject;
                                mObject = new JSONObject(arrayApp);
                                String package_version;
                                package_version = mObject.getString(WebParams.PACKAGE_VERSION);
                                final String package_name = mObject.getString(WebParams.PACKAGE_NAME);
                                final String type = mObject.getString(WebParams.TYPE);
                                Timber.d("Isi Version Name / version code:" + DefineValue.VERSION_NAME + " / " + DefineValue.VERSION_CODE);
                                if (!package_version.equals(DefineValue.VERSION_NAME)) {
                                    if (!isFinishing()) {
                                        final JSONObject finalMObject = mObject;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
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
                                                        LoginActivity.this.finish();
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
                        } else if (code.equals("0381")) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Maintenance")
                                    .setMessage(message)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            LoginActivity.this.finish();
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

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(LoginActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi app version registration:" + throwable.toString());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApiClient.CancelRequestWS(this, true);
    }
}