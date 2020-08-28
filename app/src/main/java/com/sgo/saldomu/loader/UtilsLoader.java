package com.sgo.saldomu.loader;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.util.Patterns;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CoreApp;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.OnLoadDataListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.FailedPinModel;
import com.sgo.saldomu.models.retrofit.GetAppVersionModel;
import com.sgo.saldomu.models.retrofit.GetBalanceModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.services.BalanceService;

import java.util.HashMap;

import timber.log.Timber;


/*
 Created by yuddistirakiki on 2/15/16.
 */
public class UtilsLoader {

    private Activity mActivity;
    private SecurePreferences sp;

    public UtilsLoader() {

    }

    public UtilsLoader(Activity mAct) {
        this.setmActivity(mAct);
        this.sp = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    public UtilsLoader(Activity mAct, SecurePreferences _sp) {
        this.setmActivity(mAct);
        this.sp = _sp;
    }

    private Activity getmActivity() {
        return mActivity;
    }

    private void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void getDataBalance(Boolean is_auto, final OnLoadDataListener mListener) {
        try {
            String member_id = sp.getString(DefineValue.MEMBER_ID, "");
            String access_key = sp.getString(DefineValue.ACCESS_KEY, "");
            if (!member_id.isEmpty() && !access_key.isEmpty()) {

                HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_SALDO, member_id);
                params.put(WebParams.MEMBER_ID, member_id);
                params.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.ACCESS_KEY, sp.getString(DefineValue.ACCESS_KEY, ""));
                if (sp.getString(DefineValue.IS_MANUAL, "N").equalsIgnoreCase("Y")) {
                    params.put(WebParams.IS_MANUAL, "Y");
                }
                String isAuto = (is_auto) ? DefineValue.STRING_YES : DefineValue.STRING_NO;
                params.put(WebParams.IS_AUTO, isAuto);
                params.put(WebParams.ACCESS_KEY, sp.getString(DefineValue.ACCESS_KEY, ""));

                Timber.d("isi params get Balance Loader:" + params.toString());
                if (!member_id.isEmpty()) {

                    RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_SALDO, params,
                            new ResponseListener() {
                                @Override
                                public void onResponses(JsonObject object) {
                                    Gson gson = new Gson();

                                    GetBalanceModel model = gson.fromJson(object, GetBalanceModel.class);

                                    String code = model.getError_code();
                                    if (code.equals(WebParams.SUCCESS_CODE)) {
                                        Timber.v("masuk sini new balance caller Loader");
//                                        String unread = sp.getString(WebParams.UNREAD_NOTIF, "");
//                                        if (unread.equals("")) {
//                                            SecurePreferences.Editor mEditor = sp.edit();
//                                            mEditor.putString(WebParams.UNREAD_NOTIF, model.getUnread_notif());
//
//                                            mEditor.apply();

                                        setNotifCount(model.getUnread_notif());
//                                        }

                                        SecurePreferences.Editor mEditor = sp.edit();
                                        mEditor.putString(DefineValue.BALANCE_AMOUNT, model.getAmount());
                                        mEditor.putString(DefineValue.BALANCE_MAX_TOPUP, model.getMax_topup());
                                        mEditor.putString(DefineValue.BALANCE_CCYID, model.getCcy_id());
                                        mEditor.putString(DefineValue.BALANCE_REMAIN_LIMIT, model.getRemain_limit());
                                        mEditor.putString(DefineValue.BALANCE_PERIOD_LIMIT, model.getPeriod_limit());
                                        mEditor.putString(DefineValue.BALANCE_NEXT_RESET, model.getNext_reset());
                                        mEditor.putString(DefineValue.IS_DORMANT, model.getIs_dormant());
                                        mEditor.remove(DefineValue.IS_MANUAL);
                                        mEditor.apply();
                                        mEditor.commit();

                                        mListener.onSuccess(true);
                                        Intent i = new Intent(BalanceService.INTENT_ACTION_BALANCE);
                                        LocalBroadcastManager.getInstance(getmActivity()).sendBroadcast(i);
                                    } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                        String message = model.getError_message();
                                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                                        test.showDialoginMain(getmActivity(), message);
                                    } else if (code.equals(DefineValue.ERROR_9333)) {
                                        Timber.d("isi response app data:" + model.getApp_data());
                                        final AppDataModel appModel = model.getApp_data();
                                        AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                        alertDialogUpdateApp.showDialogUpdate(getmActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                    } else if (code.equals(DefineValue.ERROR_0066)) {
                                        Timber.d("isi response maintenance:" + object.toString());
                                        AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                        alertDialogMaintenance.showDialogMaintenance(getmActivity(), model.getError_message());
                                    } else {
                                        code = model.getError_message();
                                        Toast.makeText(getmActivity(), code, Toast.LENGTH_LONG).show();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(DefineValue.ERROR, code);
                                        bundle.putString(DefineValue.ERROR_CODE, model.getError_code());
                                        mListener.onFail(bundle);
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    mListener.onFailure(throwable.toString());
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void getFailedPIN(String user_id, final OnLoadDataListener mListener) {
        HashMap<String, Object> params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_GET_FAILED_PIN);
        params.put(WebParams.USER_ID, user_id);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.FLAG_LOGIN, DefineValue.STRING_YES);

        getFailedPIN(params, mListener);
    }

    public void getFailedPINNo(String user_id, final OnLoadDataListener mListener) {
        HashMap<String, Object> params = RetrofitService.getInstance()
                .getSignatureSecretKeyPIN(MyApiClient.LINK_GET_FAILED_PIN, "", user_id);
        params.put(WebParams.USER_ID, user_id);
        params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        params.put(WebParams.FLAG_LOGIN, DefineValue.STRING_NO);

        getFailedPIN(params, mListener);
    }

    void getFailedPIN(HashMap<String, Object> params, final OnLoadDataListener mListener) {
        try {

            Timber.d("isi params get FailedPin Loader:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_FAILED_PIN, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            FailedPinModel model = gson.fromJson(object, FailedPinModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                int attempt = model.getFailed_attempt();
                                int failed = model.getMax_failed();
                                if (attempt != -1)
                                    mListener.onSuccess(failed - attempt);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getmActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getmActivity(), model.getError_message());
                            } else {
                                code = model.getError_message();
                                Toast.makeText(getmActivity(), code, Toast.LENGTH_LONG).show();
                                Bundle bundle = new Bundle();
                                bundle.putString(DefineValue.ERROR, code);
                                bundle.putString(DefineValue.ERROR_CODE, model.getError_code());
                                mListener.onFail(bundle);
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            mListener.onFailure(throwable.toString());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void setNotifCount(String _count) {
        if (mActivity == null)
            return;

        MainPage fca = (MainPage) mActivity;
        fca.setNotifAmount(_count);
    }

    public void getAppVersion() {
        try {

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignatureSecretKey(MyApiClient.LINK_APP_VERSION, "");

            Timber.d("params get app version:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_APP_VERSION, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = RetrofitService.getInstance().getGson().fromJson(object, GetAppVersionModel.class);

                            if (!model.getOn_error()) {
                                String code = model.getError_code();
                                if (code.equals(WebParams.SUCCESS_CODE)) {
//                                    Timber.d("Isi response get App Version:"+response.toString());

                                    final AppDataModel appModel = model.getApp_data();
                                    sp.edit().putString(DefineValue.SHORT_URL_APP, appModel.getShortUrl()).apply();
                                    if (appModel.getDisable().equals("1")) {
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
                                        AlertDialog alertDialog = DefinedDialog.BuildAlertDialog(getmActivity(), getmActivity().getString(R.string.maintenance),
                                                message, android.R.drawable.ic_dialog_alert, false,
                                                getmActivity().getString(R.string.ok), okListener);
                                        alertDialog.show();
                                    } else {
                                        String package_version = appModel.getPackageVersion();
                                        final String package_name = appModel.getPackageName();
                                        final String type = appModel.getType();
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
                                                        download_url = appModel.getDownloadUrl();
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
                                } else if (code.equals("0381")) {
                                    String message = model.getError_message();
                                    DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getmActivity().finish();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            System.exit(0);
                                            getmActivity().getParent().finish();
                                        }
                                    };
                                    AlertDialog alertDialog = DefinedDialog.BuildAlertDialog(getmActivity(), getmActivity().getString(R.string.maintenance),
                                            message, android.R.drawable.ic_dialog_alert, false,
                                            getmActivity().getString(R.string.ok), okListener);
                                    alertDialog.show();
                                } else {
                                    code = model.getError_message();
                                    Toast.makeText(CoreApp.getAppContext(), code, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

}
