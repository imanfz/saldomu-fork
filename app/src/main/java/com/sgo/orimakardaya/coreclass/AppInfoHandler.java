package com.sgo.orimakardaya.coreclass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 8/18/2014.
 */
public class AppInfoHandler {

    Context mContext = null;

    public static final long DISCONNECT_TIMEOUT = 30000; // 30 detik = 30 * 1000 ms

    public AppInfoHandler(Context s) {
        mContext = s;
    }

    public void getAppVersion(){
        try{
            MyApiClient.getAppVersion(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        final Activity currentActivity = ((CoreApp)mContext.getApplicationContext()).getCurrentActivity();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response get App Version:"+response.toString());
                            String arrayApp = response.getString(WebParams.APP_DATA);
                            if(!arrayApp.isEmpty() && !arrayApp.equalsIgnoreCase(null)) {
                                final JSONObject mObject = new JSONObject(arrayApp);
                                String package_version = mObject.getString(WebParams.PACKAGE_VERSION);
                                final String package_name = mObject.getString(WebParams.PACKAGE_NAME);
                                final String type = mObject.getString(WebParams.TYPE);
                                if (!package_version.equals(DefineValue.VERSION_NAME)) {
                                    if (!currentActivity.isFinishing()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity)
                                                .setTitle("Update")
                                                .setMessage("Application is out of date,  Please update immediately")
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        final String appPackageName = package_name;
                                                        if (type.equalsIgnoreCase("1")) {
                                                            try {
                                                                currentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                                currentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                            }
                                                        } else if (type.equalsIgnoreCase("2")) {
                                                            String download_url = "";
                                                            try {
                                                                download_url = mObject.getString(WebParams.DOWNLOAD_URL);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                            if (!Patterns.WEB_URL.matcher(download_url).matches())
                                                                download_url = "http://www.google.com";
                                                            currentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(download_url)));
                                                        }
                                                        currentActivity.finish();
                                                        android.os.Process.killProcess(android.os.Process.myPid());
                                                        System.exit(0);
                                                    }
                                                });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }
                                }
                            }
                        }
                        else if(code.equals("0381")) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            if (!currentActivity.isFinishing()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity)
                                        .setTitle("Maintenance")
                                        .setMessage(message)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                currentActivity.finish();
                                                android.os.Process.killProcess(android.os.Process.myPid());
                                                System.exit(0);
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(currentActivity.getApplicationContext(), code, Toast.LENGTH_LONG).show();
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
                    Timber.w("Error Koneksi get App Version:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


}
