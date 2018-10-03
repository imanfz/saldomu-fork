package com.sgo.saldomu.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
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
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.models.retrofit.InqSMSModel;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 5/19/16.
 */
public class SMSDialog extends Dialog {

    private ImageView img_view;
    private ProgressBar progBar;
    private TextView progText, tvMessage;
    private Button btnOk, btnCancel;
    private CountDownTimer cdTimer;
    private static int lenghtTimer; //5 minute
    private final static int intervalTimer = 1000;
    private final static int max_fail_connect = 5; //5 minute
    private static Boolean isStop;
    private String imeiDevice, ICCIDDevice;
    private SMSclass smsClass;
    private String message1;
    private String timeStamp;
    private SMSclass.SMS_VERIFY_LISTENER smsVerifyListener;
    private Handler handler;
    private int idx_fail;


    public interface DialogButtonListener {
        void onClickOkButton(View v, boolean isLongClick);

        void onClickCancelButton(View v, boolean isLongClick);

        void onSuccess(int user_is_new);

        void onSuccess(String product_value);
    }

    private DialogButtonListener deListener;

    public SMSDialog(Context context, DialogButtonListener _listener) {
        super(context);
        this.deListener = _listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        setContentView(R.layout.dialog_sms_notification);

        if (BuildConfig.FLAVOR.equals(DefineValue.DEVELOPMENT))
            lenghtTimer = 150000;
        else
            lenghtTimer = 300000;

        smsClass = new SMSclass(getContext());
        // set values for custom dialog components - text, image and button

        img_view = findViewById(R.id.dialog_pic_msg);
        progBar = findViewById(R.id.dialog_probar_inquiry);
        progText = findViewById(R.id.dialog_duration_inquiry);
        tvMessage = findViewById(R.id.message_dialog);

        progBar.setMax(100);

        message1 = getContext().getString(R.string.appname) + " " + getContext().getString(R.string.dialog_sms_msg);

        btnOk = findViewById(R.id.btn_dialog_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InetHandler.isNetworkAvailable(getContext())) {
                    progBar.setProgress(0);
                    tvMessage.setText(getContext().getString(R.string.dialog_sms_msg2));
                    progBar.setVisibility(View.VISIBLE);
                    progText.setVisibility(View.VISIBLE);
                    btnOk.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon_process));
                    } else {
                        img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon_process));
                    }

                    isStop = false;
                    idx_fail = 0;
                    sentInquirySMS();
                    cdTimer.start();
                    if (deListener != null)
                        deListener.onClickOkButton(v, false);
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.inethandler_dialog_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel = findViewById(R.id.btn_dialog_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                tvMessage.setText(message1);
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon));
                } else {
                    img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon_process));
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

                i = 0;
                tvMessage.setText(getContext().getString(R.string.dialog_sms_msg3));
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon_fail));
                } else {
                    img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon_fail));
                }
            }
        };

        SMSclass test = new SMSclass(getContext());
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
                tvMessage.setText(getContext().getString(R.string.dialog_sms_msg3));
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon_fail));
                } else {
                    img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon_fail));
                }
                cdTimer.cancel();
                RetrofitService.dispose();
                isStop = true;
            }
        };
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        timeStamp = String.valueOf(DateTimeFormat.getCurrentDateTimeMillis());
        Timber.i("isi timestamp : " + timeStamp);
    }

    public void reset() {
        tvMessage.setText(message1);
        progText.setVisibility(View.GONE);
        progBar.setVisibility(View.GONE);
        btnOk.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon));
        } else {
            img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon));
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
            String mobileNetworkCode = NoHPFormat.getMNC(ICCIDDevice);
            String mobileDestination    = NoHPFormat.getSMSVerifyDestination(mobileNetworkCode);
            Timber.d("ICC ID: "+ICCIDDevice+ ", Network Code : "+ mobileNetworkCode + ", mobile Dest : " + mobileDestination);
            Timber.d("jalanin sentSMSVerify "+ICCIDDevice);

            smsClass.sendSMSVerify(mobileDestination, imeiDevice, ICCIDDevice, timeStamp, smsVerifyListener);
        }
    }

    private void sentInquirySMS() {
        try {
            Timber.d("idx fail = " + String.valueOf(idx_fail));
            if (idx_fail <= max_fail_connect && InetHandler.isNetworkAvailable(getContext())) {
                if (!isStop) {
                    String extraSignature = ICCIDDevice + imeiDevice;

                    HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_ADD_COMMENT, extraSignature);
                    params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                    params.put(WebParams.IMEI, imeiDevice);
                    params.put(WebParams.ICCID, ICCIDDevice);
                    params.put(WebParams.SENT, timeStamp);

                    Timber.d("isi params inquiry sms:" + params.toString());

                    RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LOGIN, params
                            , new ObjListener() {
                                @Override
                                public void onResponses(JsonObject object) {

                                    Gson gson = new Gson();

                                    final InqSMSModel model = gson.fromJson(object, InqSMSModel.class);

                                    String code = model.getError_code();
                                    if (handler == null)
                                        handler = new Handler();

                                    if (code.equals(WebParams.SUCCESS_CODE)) {
                                        isStop = true;
                                        tvMessage.setText(getContext().getString(R.string.dialog_sms_msg4));
                                        progText.setVisibility(View.GONE);
                                        progBar.setVisibility(View.GONE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon_success));
                                        } else {
                                            img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon_success));
                                        }
                                        cdTimer.cancel();

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                saveData(model);
                                            }
                                        }, 3000);

                                    } else {
//                                            if ()
//                                idx_fail++;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                sentInquirySMS();
                                            }
                                        }, 3000);
                                    }
                                }
                            });
                }
            } else {
                tvMessage.setText(getContext().getString(R.string.dialog_sms_msg3));
                progText.setVisibility(View.GONE);
                progBar.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    img_view.setImageDrawable(getContext().getDrawable(R.drawable.phone_sms_icon_fail));
                } else {
                    img_view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.phone_sms_icon_fail));
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
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setListener(DialogButtonListener dialogButtonListener) {
        this.deListener = dialogButtonListener;
    }
}
