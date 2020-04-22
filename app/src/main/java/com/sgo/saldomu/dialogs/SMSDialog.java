package com.sgo.saldomu.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.InqSMSModel;
import com.sgo.saldomu.securities.Md5;

import java.util.Calendar;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 5/19/16.
 */
public class SMSDialog extends DialogFragment {

    private static final int REQUEST_SMS = 1;
    public Long date;
    private ImageView img_view;
    private ProgressBar progBar;
    private TextView progText, tvMessage, tvMessage1;
    private Button btnOk, btnCancel;
    private CountDownTimer cdTimer;
    private static int lenghtTimer; //5 minute
    private final static int intervalTimer = 1000;
    private final static int max_fail_connect = 5; //5 minute
    private static Boolean isStop;
    private String imeiDevice, ICCIDDevice;
    private SMSclass smsClass;
    private String message1, msg, msgFinal;
    private String SMS_VERIFY;
    private String timeStamp, dateTime;
    private SMSclass.SMS_VERIFY_LISTENER smsVerifyListener;
    private Handler handler;
    private int idx_fail;
    private boolean isRetry = false, flag;
    SecurePreferences sp;
    View v;

    public SMSDialog() {
    }

    public Handler getHandler() {
        if (handler == null)
            handler = new Handler();
        return handler;
    }

    public void show(android.support.v4.app.FragmentManager fragmentManager, String s) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, "");
        ft.commit();
    }


    public interface DialogButtonListener {
        void onClickOkButton(View v, boolean isLongClick);

        void onClickCancelButton(View v, boolean isLongClick);

        void onSuccess(int user_is_new);

        void onSuccess(String product_value);
    }

    public DialogButtonListener deListener;

    public static SMSDialog newDialog(String date, boolean flag, DialogButtonListener _listener) {
        SMSDialog dialog = new SMSDialog();
        dialog.dateTime = date;
        dialog.deListener = _listener;
        dialog.flag = flag;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_sms_notification, container, false);
        img_view = v.findViewById(R.id.dialog_pic_msg);
        progBar = v.findViewById(R.id.dialog_probar_inquiry);
        progText = v.findViewById(R.id.dialog_duration_inquiry);
        tvMessage = v.findViewById(R.id.message_dialog);
        tvMessage1 = v.findViewById(R.id.message_dialog1);
        btnOk = v.findViewById(R.id.btn_dialog_ok);
        btnCancel = v.findViewById(R.id.btn_dialog_cancel);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        timeStamp = String.valueOf(DateTimeFormat.getCurrentDateTimeMillis());
        dateTime = String.valueOf(DateTimeFormat.getCurrentDateTimeSMS());

        if (BuildConfig.FLAVOR.equals(DefineValue.DEVELOPMENT))
            lenghtTimer = 150000;
        else
            lenghtTimer = 300000;
        progBar.setMax(100);

        message1 = getActivity().getString(R.string.dialog_sms_msg);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InetHandler.isNetworkAvailable(getActivity())) {
                    progBar.setProgress(0);
                    tvMessage.setText(getActivity().getString(R.string.dialog_sms_msg2));
                    progBar.setVisibility(View.VISIBLE);
                    progText.setVisibility(View.VISIBLE);
                    btnOk.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon_process));
                    } else {
                        img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_process));
                    }

                    isStop = false;
                    idx_fail = 0;
//                    sentInquirySMS();
                    sentSms();
                    cdTimer.start();
                    if (deListener != null)
                        deListener.onClickOkButton(v, false);
                } else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.inethandler_dialog_message), Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessage.setText(message1);
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon));
                } else {
                    img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_process));
                }
                cdTimer.cancel();
                RetrofitService.dispose();
                isStop = true;
                dismiss();
                if (deListener != null)
                    deListener.onClickCancelButton(v, false);
            }
        });


        tvMessage.setText(message1);


        cdTimer = new CountDownTimer(lenghtTimer, intervalTimer) {
            int i = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                progText.setText(DateTimeFormat.convertMilisToMinute(millisUntilFinished));
                i++;
                int prog = (i * 100000) / lenghtTimer;

                progBar.setProgress(prog);
            }

            @Override
            public void onFinish() {
                isStop = true;
                RetrofitService.dispose();
                isRetry = true;
                i = 0;
                tvMessage.setText(getActivity().getString(R.string.dialog_sms_msg3));
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon_fail));
                } else {
                    img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_fail));
                }
            }
        };

        SMSclass test = new SMSclass(getActivity());
        imeiDevice = test.getDeviceIMEI();
        ICCIDDevice = test.getDeviceICCID();
        Timber.wtf("device imei/ICCID : " + imeiDevice + "/" + ICCIDDevice);

        smsVerifyListener = new SMSclass.SMS_VERIFY_LISTENER() {
            @Override
            public void success() {
                Timber.i("sms terkirim sukses");
            }

            @Override
            public void failed() {
                Timber.i("sms terkirim gagal");
                tvMessage.setText(getActivity().getString(R.string.dialog_sms_msg3));
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                isRetry = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon_fail));
                } else {
                    img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_fail));
                }
                cdTimer.cancel();
                RetrofitService.dispose();
                isStop = true;
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();
//        dateTime = DateTimeFormat.getCurrentDateTimeMillis();


//        timeStamp = String.valueOf(DateTimeFormat.getCurrentDateTimeMillis());
        Timber.i("isi timestamp : " + timeStamp);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("fcmData"));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            isStop = true;
            tvMessage.setText(getActivity().getString(R.string.dialog_sms_msg4));
            progText.setVisibility(View.GONE);
            progBar.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon_success));
            } else {
                img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_success));
            }
            cdTimer.cancel();

            if (handler == null)
                handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    saveData(new JSONObject());
                }
            }, 10000);

            cdTimer.cancel();
        }
    };

    public void reset() {
        tvMessage.setText(message1);
        progText.setVisibility(View.GONE);
        progBar.setVisibility(View.GONE);
        btnOk.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon));
        } else {
            img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon));
        }
        DestroyDialog();
    }

    public void DestroyDialog() {
        if (cdTimer != null) {
            isStop = true;
            cdTimer.cancel();
        }
    }

    public void sentSms() {
        if (!isStop) {
            SMS_VERIFY = "REG EMO " + MyApiClient.COMM_ID;
//            if (!sp.getString(DefineValue.TIMESTAMP, "").isEmpty()) {
                timeStamp = sp.getString(DefineValue.TIMESTAMP, "");
//            } else
            dateTime = String.valueOf(DateTimeFormat.getCurrentDateTimeSMS());
            Timber.wtf("device imei/ICCID : " + imeiDevice + "/" + ICCIDDevice);
            Timber.d("jalanin sentSMSVerify " + ICCIDDevice);
            String mobileNetworkCode = NoHPFormat.getMNC(ICCIDDevice);
            String mobileDestination = NoHPFormat.getSMSVerifyDestination(mobileNetworkCode);
            if (isRetry) {
                timeStamp = String.valueOf(DateTimeFormat.getCurrentDateTimeMillis());
                msg = SMS_VERIFY + " " + imeiDevice + "_" + ICCIDDevice + "_" + timeStamp + "_" + MyApiClient.APP_ID + "_" + dateTime + "_";
                msgFinal = msg.toUpperCase() + Md5.hashMd5(msg).toUpperCase();
            } else {
                msgFinal = sp.getString(DefineValue.SMS_CONTENT, "") + sp.getString(DefineValue.SMS_CONTENT_ENCRYPTED, "");
            }
            Uri uri = Uri.parse("smsto:" + mobileDestination);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", msgFinal);
            Timber.d("content sms : " + msgFinal);
            startActivityForResult(intent, REQUEST_SMS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SMS) {
            regFCM();
//            sentInquirySMS();
        }
    }

    private void regFCM()
    {
        try {
            HashMap<String, Object> params = RetrofitService
                    .getInstance().getSignatureSecretKey(MyApiClient.LINK_FCM, "");
            params.put(WebParams.IMEI_ID, imeiDevice.toUpperCase());
            params.put(WebParams.FCM_ID, sp.getString(DefineValue.FCM_ID,""));
            params.put(WebParams.REFERENCE_ID, sp.getString(DefineValue.SMS_CONTENT_ENCRYPTED,""));
            Timber.d("isi params fcm:" + params.toString());
            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_FCM, params, new ResponseListener() {
                @Override
                public void onResponses(JsonObject object) {
                    Timber.d("isi response fcm:" + object);

                    sentInquirySMS();
                }

                @Override
                public void onError(Throwable throwable) {
                    Timber.d("isi error fcm:" + throwable);
                }

                @Override
                public void onComplete() {

                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void sentInquirySMS() {
        try {
            Timber.d("idx fail = " + String.valueOf(idx_fail));

            if (idx_fail <= max_fail_connect && InetHandler.isNetworkAvailable(getActivity())) {
                if (!isStop) {
                    String extraSignature = ICCIDDevice + imeiDevice;

                    HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_INQUIRY_SMS, extraSignature);
                    params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                    params.put(WebParams.IMEI, imeiDevice);
                    params.put(WebParams.ICCID, ICCIDDevice);
                    params.put(WebParams.SENT, timeStamp);
                    params.put(WebParams.REFERENCE_ID, sp.getString(DefineValue.SMS_CONTENT_ENCRYPTED, ""));

                    Timber.d("isi params inquiry sms:" + params.toString());

                    RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_SMS, params
                            , new ResponseListener() {
                                @Override
                                public void onResponses(JsonObject object) {
                                    Gson gson = new Gson();

                                    final InqSMSModel model = gson.fromJson(object, InqSMSModel.class);

                                    String code = model.getError_code();
                                    Timber.d("isi response inquiry sms:" + object.toString());

                                    sp.edit().remove(DefineValue.TIMESTAMP).apply();

                                    if (code.equals(WebParams.SUCCESS_CODE)) {
                                        isStop = true;
                                        tvMessage.setText(getActivity().getString(R.string.dialog_sms_msg4));
                                        progText.setVisibility(View.GONE);
                                        progBar.setVisibility(View.GONE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon_success));
                                        } else {
                                            img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_success));
                                        }
                                        cdTimer.cancel();



                                        getHandler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                saveData(model);
                                            }
                                        }, 10000);

                                    } else if (code.equals(DefineValue.ERROR_0004)) {
                                        cdTimer.cancel();
                                        tvMessage.setText(getActivity().getString(R.string.wrong_sms_format));
                                        progText.setVisibility(View.GONE);
                                        progBar.setVisibility(View.GONE);
                                        btnOk.setVisibility(View.GONE);
                                        btnCancel.setVisibility(View.VISIBLE);
                                        btnCancel.setText(getString(R.string.ok));
                                        RetrofitService.dispose();
                                        DestroyDialog();
                                        idx_fail = 0;
                                    } else if (code.equals(DefineValue.ERROR_9333)) {
                                        Timber.d("isi response app data:" + model.getApp_data());
                                        final AppDataModel appModel = model.getApp_data();
                                        AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                        alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                    } else if (code.equals(DefineValue.ERROR_0066)) {
                                        Timber.d("isi response maintenance:" + object.toString());
                                        AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                        alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                    } else {
//                                            if ()
//                                idx_fail++;
                                        getHandler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                sentInquirySMS();
                                            }
                                        }, 10000);
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    getHandler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            sentInquirySMS();
                                        }
                                    }, 10000);
                                }

                                @Override
                                public void onComplete() {
                                    isRetry = true;
                                }
                            });
                }
            } else {
                tvMessage.setText(getActivity().getString(R.string.dialog_sms_msg3));
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getActivity().getDrawable(R.drawable.phone_sms_icon_fail));
                } else {
                    img_view.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.phone_sms_icon_fail));
                }

                RetrofitService.dispose();
                DestroyDialog();
                idx_fail = 0;
            }
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void saveData(InqSMSModel model) {
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor edit = sp.edit();
        edit.putString(DefineValue.SENDER_ID, model.getSender_id());
        edit.putBoolean(DefineValue.IS_INQUIRY_SMS, true);
        edit.apply();

        //check apakah user register dari ATM atau tidak

        if (model.getIs_new_user() == 0) {
            edit.putString(DefineValue.DEIMEI, imeiDevice);
            edit.putString(DefineValue.DEICCID, ICCIDDevice);
            edit.commit();

            deListener.onSuccess(0);
        } else {
            edit.commit();
            deListener.onSuccess(1);
        }
        deListener.onSuccess(model.getSender_id());

        dismiss();

    }


    public void setListener(DialogButtonListener dialogButtonListener) {
        this.deListener = dialogButtonListener;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}
