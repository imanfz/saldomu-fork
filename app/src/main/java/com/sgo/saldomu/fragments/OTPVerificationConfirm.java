package com.sgo.saldomu.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chaos.view.PinView;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.OnBackPressed;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.OTPModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPVerificationConfirm extends BaseFragment implements OnBackPressed {

    TextView tvHpValue, tvReffIdValue;
    PinView pinView;
    TextView tvCountDown, tv_version;
    Button btSend, btResend;
    Boolean allowBackPress = false;

    private ProgressDialog progdialog;
    private String is_new;
    private String user_id;
    private String device_name;
    private String refference_id, imeiDevice, ICCIDDevice;
    private SecurePreferences sp;
    private CountDownTimer countDownTimer;

    public OTPVerificationConfirm() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_otp_verification_2, container, false);
        pinView = view.findViewById(R.id.pin_view);
        tvHpValue = view.findViewById(R.id.tv_hp_value);
        tvReffIdValue = view.findViewById(R.id.tv_otp_reffid);
        tvCountDown = view.findViewById(R.id.tv_countdown);
        tv_version = view.findViewById(R.id.tv_version);
        btSend = view.findViewById(R.id.btnSend);
        btResend = view.findViewById(R.id.btnResend);
        btResend.setEnabled(false);
        tv_version.setText(getString(R.string.appname) + " " + BuildConfig.VERSION_NAME + " (" +BuildConfig.VERSION_CODE +")");

        SMSclass test = new SMSclass(getActivity());
        imeiDevice = test.getDeviceAndroidId();
//        ICCIDDevice = test.getDeviceICCID();
//        Timber.wtf("device imei/ICCID : " + imeiDevice + "/" + ICCIDDevice);

        initiateData();
        initiateCountDownTimerForResendOTP();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();


        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputValidation()) {
                    confirmOTP();
                }
            }
        });

        btResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinView.setText("");
                getOTP();
            }
        });

        if (!btResend.isEnabled())
            btResend.setBackground(getActivity().getResources().getDrawable(R.color.transparant));

    }

    private void initiateCountDownTimerForResendOTP() {
        countDownTimer = new CountDownTimer(300000, 1000) {

            String sisa;

            @Override
            public void onTick(long l) {
                tvCountDown.setText("Sisa Waktu: " + (TimeUnit.MILLISECONDS.toMinutes(l)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l))) + ":"
                        + (TimeUnit.MILLISECONDS.toSeconds(l)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                sisa = (TimeUnit.MILLISECONDS.toMinutes(l)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l))) + ":"
                        + (TimeUnit.MILLISECONDS.toSeconds(l)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
                Timber.d("sisa ontick timer " + sisa);
                allowBackPress = false;
                btResend.setEnabled(false);
            }

            @Override
            public void onFinish() {
                Timber.d("sisa onfinish timer " + sisa);
                allowBackPress = true;
                btResend.setEnabled(true);
                btResend.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_background_blue));
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        countDownTimer.cancel();
    }

    private void initiateData() {
        Bundle args = getArguments();
        if (args != null) {
            user_id = args.getString(DefineValue.USER_ID, "");
            device_name = args.getString(DefineValue.DEVICE_NAME, "");
//            refference_id = args.getString(DefineValue.REFFERENCE_ID, "");
        }

        tvHpValue.setText(NoHPFormat.formatTo08(user_id));

        if (refference_id==null)
            tvReffIdValue.setText(getString(R.string.otp_reff_id) + " : " + args.getString(DefineValue.REFFERENCE_ID, ""));
        else
            tvReffIdValue.setText(getString(R.string.otp_reff_id) + " : " + refference_id);
    }

    public boolean inputValidation() {
        if (pinView.getText().toString().length() != 6) {
            Toast.makeText(getActivity(), getString(R.string.otp_validation_login), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void confirmOTP() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String encrypted_pin;
            String key;
            String link = MyApiClient.LINK_CONFIRM_OTP;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            String pin = pinView.getText().toString();

            extraSignature = user_id;
            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignatureSecretKey(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            key = uuid + dateTime + BuildConfig.APP_ID + subStringLink + MyApiClient.COMM_ID + user_id;
            Timber.d("key : " + key);
            encrypted_pin = RSA.opensslEncryptLogin(key, pin);
            Timber.d("encrypted pin : " + encrypted_pin);

            params.put(WebParams.USER_ID, user_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.OTP, encrypted_pin);
            params.put(WebParams.IMEI_ID, imeiDevice.toUpperCase());


            Timber.d("isi params confirm OTP:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            OTPModel model = getGson().fromJson(object, OTPModel.class);

                            if (!model.getOn_error()) {
                                String code = model.getError_code();
                                Timber.d("isi response confirm OTP : " + object.toString());

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("Sukses");
                                    pinView.setText("");
                                    is_new = model.getIs_new();
                                    Intent intent = new Intent(getContext(), LoginActivity.class);
                                    sp.edit().putString(DefineValue.SENDER_ID, user_id).commit();

                                    if (is_new.equalsIgnoreCase("Y")) {
                                        intent.putExtra(DefineValue.USER_IS_NEW, 1);
                                    } else {
                                        intent.putExtra(DefineValue.USER_IS_NEW, -2);
                                    }
                                    intent.putExtra(DefineValue.IS_POS, "N");
                                    startActivity(intent);
                                    getActivity().finish();

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), model.getError_message());
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
                                    Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Timber.d("eror confirm otp");
                        }

                        @Override
                        public void onComplete() {
                            progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void getOTP() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = user_id;

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignatureSecretKey(MyApiClient.LINK_GET_OTP, extraSignature);
            params.put(WebParams.USER_ID, user_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.DEVICE_NAME, device_name);

            Timber.d("isi params get OTP:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_OTP, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            OTPModel model = getGson().fromJson(object, OTPModel.class);


                            if (!model.getOn_error()) {

                                String code = model.getError_code();
                                Timber.d("isi response get OTP : " + object.toString());

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    pinView.setText("");
                                    refference_id = model.getRef_id();
                                    initiateData();
                                    initiateCountDownTimerForResendOTP();
                                    Toast.makeText(getActivity(), getString(R.string.resend_verification_code), Toast.LENGTH_LONG).show();
                                    btResend.setEnabled(false);
                                    btResend.setBackground(getActivity().getResources().getDrawable(R.color.transparant));
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), model.getError_message());
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
                                    Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        return !allowBackPress;
    }
}
