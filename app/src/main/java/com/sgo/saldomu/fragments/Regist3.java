package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CreatePIN;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.PasswordRegisterActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.CreatePassModel;
import com.sgo.saldomu.models.retrofit.CreatePinModel;
import com.sgo.saldomu.models.retrofit.RegModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.joda.time.DateTime;

import java.util.HashMap;

import timber.log.Timber;

/*
 Created by Administrator on 7/4/2014.
 */
public class Regist3 extends BaseFragment {

    SecurePreferences sp;
    Button btnResend, btnSubmit, btnCancel;
    String noHPValue, namaValue, emailValue, authType, custID, token, pass, confPass, memberID, emailToken;
    int max_resend_sms;// max_resend_email;
    EditText TokenValue;
    TextView mNoHPValue, mNamaValue, mEmail, txtToken;
    ProgressDialog progdialog;
    View v, layout_resend;
    CircleStepView mCircleStepView;
    Boolean isFacebook;
    Activity act;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist3, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        getActivity().getWindow().setBackgroundDrawableResource(R.drawable.background);
        act = getActivity();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        Bundle args = getArguments();
        if (args != null) {
            noHPValue = args.getString(DefineValue.CUST_PHONE, "");
            namaValue = args.getString(DefineValue.CUST_NAME, "");
            emailValue = args.getString(DefineValue.CUST_EMAIL, "-");
            emailToken = args.getString(DefineValue.TOKEN, "");
//            max_resend_sms = Integer.parseInt(args.getString(DefineValue.MAX_RESEND, "3"));
//            max_resend_email = Integer.parseInt(args.getString(DefineValue.MAX_RESEND,"3"));
            max_resend_sms = 3;
//            max_resend_email = 3;
        }

        txtToken = v.findViewById(R.id.token_text);
        TokenValue = v.findViewById(R.id.reg2_token_value);
        mNoHPValue = v.findViewById(R.id.reg2_noHP_value);
        mNoHPValue.setText(noHPValue);
        mNamaValue = v.findViewById(R.id.reg2_nama_value);
        mNamaValue.setText(namaValue);
        mEmail = v.findViewById(R.id.reg2_email_value);
        mEmail.setText(emailValue);
        btnSubmit = v.findViewById(R.id.btn_reg2_verification);
        btnCancel = v.findViewById(R.id.btn_reg2_cancel);
        btnResend = v.findViewById(R.id.btn_reg2_resend_token);
        layout_resend = v.findViewById(R.id.reg2_layout_resend);

        TokenValue.requestFocus();

        if (max_resend_sms != 0) {
            btnResend.setText(getString(R.string.reg3_btn_text_resend_token_sms) + " (" + max_resend_sms + ")");
        }
//        else if(max_resend_email > 0)  {
//            btnResend.setText(getString(R.string.reg2_btn_text_resend_token_email) + " (" + max_resend_email + ")");
//        }
        else if (max_resend_sms == 0) {
            layout_resend.setVisibility(View.GONE);
            showDialogEmptyToken();
        }

        btnResend.setOnClickListener(resendListener);
        btnSubmit.setOnClickListener(submitListener);
        btnCancel.setOnClickListener(cancelListener);

    }


    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    sentData();
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (max_resend_sms != 0) requestResendToken("Y", "N");
//                else if(max_resend_email > 0) requestResendToken("N","Y");
//                else Toast.makeText(getActivity(),getString(R.string.reg2_notif_max_resend_token_empty),Toast.LENGTH_LONG).show();
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
//            DefineValue.NOBACK = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(act)
                    .setMessage(getString(R.string.reg3_cancel_message))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    private void switchActivity(Intent i) {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchActivity(i);
    }


    private void switchActivityPIN(Intent i) {
        /*if (getActivity() == null)
            return;

        Registration fca = (Registration) getActivity();
        fca.switchActivity(i, Registration.ACTIVITY_RESULT);*/
        startActivityForResult(i, LoginActivity.ACTIVITY_RESULT);
    }

    public void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (max_resend_sms != 0)
                    btnResend.setText(getString(R.string.reg3_btn_text_resend_token_sms) + " (" + max_resend_sms + ")");
//                else if(max_resend_email > 0)
//                    btnResend.setText(getString(R.string.reg2_btn_text_resend_token_email) + " (" + max_resend_email + ")");
//                else if(max_resend_email == 0)
//                    btnResend.setText(getString(R.string.reg2_btn_text_resend_token_email) + " (" + max_resend_email + ")");

            }
        });
    }

    CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long l) {
            DateTime time = new DateTime(l);
            btnResend.setText(getString(R.string.btnresend_text_try_again_after, time.toString("mm:ss")));
        }

        @Override
        public void onFinish() {
            btnResend.setEnabled(true);
            changeTextBtnSub();
        }
    };


    //Resend Token
    public void requestResendToken(final String is_sms, final String is_email) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            btnCancel.setEnabled(false);
            btnResend.setEnabled(false);
            btnSubmit.setEnabled(false);
            TokenValue.setEnabled(false);

            HashMap<String, Object> params = new HashMap<>();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHPValue);
            params.put(WebParams.CUST_NAME, namaValue);
            params.put(WebParams.CUST_EMAIL, emailValue);
            params.put(WebParams.EMAIL_TOKEN, emailToken);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.IS_SMS, is_sms);
            params.put(WebParams.IS_EMAIL, is_email);

            Timber.d("isi params resend token:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REG_STEP2, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                AlertDialog dialogToken;
                                if (is_sms.equalsIgnoreCase("Y")) {
                                    --max_resend_sms;

                                    if (max_resend_sms == 0) {
                                        layout_resend.setVisibility(View.GONE);
                                        showDialogEmptyToken();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage(getString(R.string.reg3_dialog_token_message_sms))
                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        dialogToken = builder.create();
                                        dialogToken.show();
                                        countDownTimer.start();
                                    }
                                }
                                changeTextBtnSub();

                            } else {

                                code = model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            btnResend.setEnabled(true);
                        }

                        @Override
                        public void onComplete() {
                            btnCancel.setEnabled(true);
                            btnSubmit.setEnabled(true);
                            TokenValue.setEnabled(true);

                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    //Submit
    public void sentData() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            btnSubmit.setEnabled(false);

            HashMap<String, Object> params = new HashMap<>();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHPValue);
            params.put(WebParams.CUST_NAME, namaValue);
            params.put(WebParams.CUST_EMAIL, emailValue);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.SMS_TOKEN, TokenValue.getText().toString());

            Timber.d("isi params reg 3 submit:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REG_STEP3, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            RegModel model = getGson().fromJson(object, RegModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                custID = model.getCust_phone();
                                authType = model.getAuthentication_type();
                                token = TokenValue.getText().toString();
                                Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                                i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                                switchActivityPIN(i);
                            } else {
                                code = model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                            btnCancel.setEnabled(true);
                            btnSubmit.setEnabled(true);
                            TokenValue.setEnabled(true);
                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("isi regist 3 requestCode:" + String.valueOf(requestCode));
        if (requestCode == LoginActivity.ACTIVITY_RESULT) {
            Timber.d("isi regist 3 resultcode:" + String.valueOf(resultCode));
            if (resultCode == LoginActivity.RESULT_PIN) {
                Timber.d("isi regist 3 authtype:" + authType);

                pass = data.getStringExtra(DefineValue.NEW_PASSWORD);
                confPass = data.getStringExtra(DefineValue.CONFIRM_PASSWORD);

//                Intent i = new Intent(getActivity(), CreatePIN.class);
//                i.putExtra(DefineValue.REGISTRATION, true);
//                switchActivityPIN(i);

                sendCreatePass();
            } else if (resultCode == LoginActivity.RESULT_FINISHING) {
//                if(authType.equals(DefineValue.AUTH_TYPE_OTP)){
//                    pass = data.getStringExtra(DefineValue.NEW_PASSWORD);
//                    confPass = data.getStringExtra(DefineValue.CONFIRM_PASSWORD);
//                }
                sendCreatePin(data);
            }
        }
    }

    public void sendCreatePass() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            String link = MyApiClient.LINK_CREATE_PASS;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = custID + pass;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PASS, RSA.opensslEncrypt(uuid, dateTime, custID, pass, subStringLink));
            params.put(WebParams.CONF_PASS, RSA.opensslEncrypt(uuid, dateTime, custID, confPass, subStringLink));
            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(uuid, dateTime, custID, token, subStringLink));
            params.put(WebParams.CUST_ID, custID);

            Timber.d("params create pass:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            CreatePassModel model = getGson().fromJson(object, CreatePassModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                memberID = model.getMember_id();

                                Intent i = new Intent(getActivity(), CreatePIN.class);
                                i.putExtra(DefineValue.REGISTRATION, true);
                                switchActivityPIN(i);
                            }else if(code.equals("0301")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getActivity().getResources().getString(R.string.logout)).setMessage(model.getError_message())
                                        .setCancelable(false)
                                        .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                            } else {

                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                                i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                                switchActivityPIN(i);
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }

    }

    public void sendCreatePin(Intent data) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            String link = MyApiClient.LINK_CREATE_PIN;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            String pin = data.getStringExtra(DefineValue.PIN_VALUE);
            String confirmPin = data.getStringExtra(DefineValue.CONF_PIN);
            extraSignature = memberID + custID + pin;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.USER_ID, custID);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, RSA.opensslEncrypt(uuid, dateTime, custID, pin, subStringLink));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(uuid, dateTime, custID, confirmPin, subStringLink));

            Timber.d("params create pin:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            CreatePinModel model = getGson().fromJson(object, CreatePinModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                showDialog();
                            } else {

                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getActivity(), CreatePIN.class);
                                i.putExtra(DefineValue.REGISTRATION, true);
                                switchActivityPIN(i);
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }

    }

    void showDialog() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);
        Message.setVisibility(View.VISIBLE);
        TextView Message2 = dialog.findViewById(R.id.message_dialog2);
        Message2.setVisibility(View.VISIBLE);
        TextView Message3 = dialog.findViewById(R.id.message_dialog3);
        Message3.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.regist2_notif_title));
        Message.setText(getResources().getString(R.string.regist2_notif_message_1));
        Message2.setText(noHPValue);
        Message2.setTextSize(getResources().getDimension(R.dimen.abc_text_size_small_material));
        Message3.setText(getResources().getString(R.string.regist2_notif_message_3));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack(null, android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Fragment test = new Login();
                switchFragment(test, "Login", false);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private void showDialogEmptyToken() {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage(getString(R.string.reg3_notif_max_resend_token_empty))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public boolean inputValidation() {
        if (TokenValue.getText().toString().length() == 0) {
            TokenValue.requestFocus();
            TokenValue.setError(this.getString(R.string.regist3_validation_otp));
            return false;
        }
        return true;
    }

    private void toggleMyBroadcastReceiver(Boolean _on) {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.togglerBroadcastReceiver(_on, myReceiver);
    }

    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Timber.wtf("masuk Receiver ");
            Bundle mBundle = intent.getExtras();
            SmsMessage[] mSMS;
            String strMessage = "";
            String _kode_otp = "";
            String _member_code = "";
            String[] kode = context.getResources().getStringArray(R.array.broadcast_regist_kode_compare);

            if (mBundle != null) {
                Object[] pdus = (Object[]) mBundle.get("pdus");
                mSMS = new SmsMessage[pdus.length];

                for (int i = 0; i < mSMS.length; i++) {
                    mSMS[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    strMessage += mSMS[i].getMessageBody();
                    strMessage += "\n";
                }

                String[] words = strMessage.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (_kode_otp.equalsIgnoreCase("")) {
                        if (words[i].equalsIgnoreCase(kode[0])) {
                            if (words[i + 1].equalsIgnoreCase(kode[1])) {
                                _kode_otp = words[i + 3];
                                _kode_otp = _kode_otp.replace(".", "").replace(" ", "");
                            }
                        }
                    }
                    Timber.d("isi words:" + words[i]);
                }
                TokenValue.setText(_kode_otp + _member_code);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleMyBroadcastReceiver(false);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }
}