package com.sgo.orimakardaya.loader;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.webkit.WebView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.BalanceModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.CoreApp;
import com.sgo.orimakardaya.coreclass.CurrencyFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.interfaces.OnLoadDataListener;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
 Created by yuddistirakiki on 2/15/16.
 */
public class UtilsLoader {

    private Activity mActivity;
    private SecurePreferences sp;

    public UtilsLoader(){

    }
    public UtilsLoader(Activity mAct){
        this.setmActivity(mAct);
    }

    public UtilsLoader(Activity mAct, SecurePreferences _sp){
        this.setmActivity(mAct);
        this.sp = _sp;
    }

    private Activity getmActivity() {
        return mActivity;
    }

    private void setmActivity(Activity mActivity) {
                    this.mActivity = mActivity;
                }

    public void getDataBalance(Boolean is_auto,final OnLoadDataListener mListener){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_SALDO,
                    sp.getString(DefineValue.USERID_PHONE,""), sp.getString(DefineValue.ACCESS_KEY,""));
            String member_id = sp.getString(DefineValue.MEMBER_ID, "");
            params.put(WebParams.MEMBER_ID, member_id);
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            String isAuto = (is_auto)? DefineValue.STRING_YES:DefineValue.STRING_NO;
            params.put(WebParams.IS_AUTO,isAuto);

            Timber.d("isi params get Balance Loader:" + params.toString());
            if(!member_id.isEmpty()) {
                MyApiClient.getSaldo(getmActivity(), params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String code = response.getString(WebParams.ERROR_CODE);
                            Timber.d("Isi response getBalance Loader:" + response.toString());
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Timber.v("masuk sini new balance caller Loader");

                                String unread = sp.getString(WebParams.UNREAD_NOTIF,"");
                                if(unread.equals("")) {
                                    SecurePreferences.Editor mEditor = sp.edit();
                                    mEditor.putString(WebParams.UNREAD_NOTIF, response.getString(WebParams.UNREAD_NOTIF));
                                    mEditor.apply();

                                    setNotifCount(response.getString(WebParams.UNREAD_NOTIF));
                                }

                                SecurePreferences.Editor mEditor = sp.edit();
                                mEditor.putString(DefineValue.BALANCE, CurrencyFormat.format(response.getDouble(WebParams.AMOUNT)));
                                mEditor.putString(DefineValue.MAX_TOPUP,response.optString(WebParams.MAX_TOPUP, ""));
                                mEditor.apply();

                                BalanceModel mBal = BalanceModel.load(BalanceModel.class,1);
                                if(mBal == null) {
                                    mBal = new BalanceModel(response);
                                    mBal.save();
                                }
                                else {
                                    mBal.setAmount(response.optString(WebParams.AMOUNT, ""));
                                    mBal.setCcy_id(response.optString(WebParams.CCY_ID, ""));
                                    mBal.setRemain_limit(response.optString(WebParams.REMAIN_LIMIT, ""));
                                    mBal.setPeriod_limit(response.optString(WebParams.PERIOD_LIMIT, ""));
                                    mBal.setNext_reset(response.optString(WebParams.NEXT_RESET, ""));
                                    mBal.save();
                                }

                                mListener.onSuccess(mBal);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginMain(getmActivity(), message);
                            } else {
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getmActivity(), code, Toast.LENGTH_LONG).show();
                                mListener.onFail(code);
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
                        Timber.w("Error Koneksi get Saldo Loader:" + throwable.toString());
                        mListener.onFailure();
                    }
                });
            }
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void getFailedPIN(String user_id , final OnLoadDataListener mListener){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_GET_FAILED_PIN,
                    user_id, sp.getString(DefineValue.ACCESS_KEY,""));
            params.put(WebParams.USER_ID, user_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get FailedPin Loader:" + params.toString());

            MyApiClient.sentGetFailedPIN(getmActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response getFailedPin Loader:" + response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            int attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                            int failed = response.optInt(WebParams.MAX_FAILED,0);
                            if(attempt != -1)
                                mListener.onSuccess(failed-attempt);
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getmActivity(), message);
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getmActivity(), code, Toast.LENGTH_LONG).show();
                            mListener.onFail(code);
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
                    Timber.w("Error Koneksi get PIN attempt Loader:" + throwable.toString());
                    mListener.onFailure();
                }
            });

        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void setNotifCount(String _count){
        if (mActivity == null)
            return;

        MainPage fca = (MainPage) mActivity;
        fca.setNotifAmount(_count);
    }

    public void getAppVersion(){
        try{
            MyApiClient.getAppVersion(getmActivity(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("Isi response get App Version:"+response.toString());
                            String arrayApp = response.optString(WebParams.APP_DATA,"");
                            if(!arrayApp.isEmpty() && !arrayApp.equalsIgnoreCase(null)) {
                                final JSONObject mObject = new JSONObject(arrayApp);
                                if(mObject.getString(WebParams.DISABLE).equals("1")) {
                                    String message = getmActivity().getResources().getString(R.string.maintenance_message);
                                    DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getmActivity().finish();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                            getmActivity().getParent().finish();
                                        }
                                    };
                                    AlertDialog alertDialog =  DefinedDialog.BuildAlertDialog(getmActivity(), getmActivity().getString(R.string.maintenance),
                                            message,android.R.drawable.ic_dialog_alert,false,
                                            getmActivity().getString(R.string.ok),okListener);
                                    alertDialog.show();
                                }
                                else {
                                    String package_version = mObject.getString(WebParams.PACKAGE_VERSION);
                                    final String package_name = mObject.getString(WebParams.PACKAGE_NAME);
                                    final String type = mObject.getString(WebParams.TYPE);
                                    Timber.d("Isi Version Name / version code:" + DefineValue.VERSION_NAME + " / " + DefineValue.VERSION_CODE);
                                    if (!package_version.equals(DefineValue.VERSION_NAME)) {
                                        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (type.equalsIgnoreCase("1")) {
                                                    try {
                                                        getmActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                                                    } catch (android.content.ActivityNotFoundException anfe) {
                                                        getmActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
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
                                                    getmActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(download_url)));
                                                }
                                                getmActivity().finish();
                                                android.os.Process.killProcess(android.os.Process.myPid());
                                                System.exit(0);
                                                getmActivity().getParent().finish();
                                            }
                                        };
                                        AlertDialog alertDialog = DefinedDialog.BuildAlertDialog(getmActivity(), getmActivity().getString(R.string.update),
                                                getmActivity().getString(R.string.update_msg), android.R.drawable.ic_dialog_alert, false,
                                                getmActivity().getString(R.string.ok), okListener);
                                        alertDialog.show();
                                    }
                                }
                            }
                        }
                        else if (code.equals("0381")) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getmActivity().finish();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                            getmActivity().getParent().finish();
                                        }
                                    };
                            AlertDialog alertDialog =  DefinedDialog.BuildAlertDialog(getmActivity(), getmActivity().getString(R.string.maintenance),
                                    message,android.R.drawable.ic_dialog_alert,false,
                                    getmActivity().getString(R.string.ok),okListener);
                            alertDialog.show();
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(CoreApp.getAppContext(), code, Toast.LENGTH_LONG).show();
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
                    Timber.w("Error Koneksi app info :"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }
}
