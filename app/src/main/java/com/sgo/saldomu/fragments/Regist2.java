package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.CreatePassModel;
import com.sgo.saldomu.models.retrofit.CreatePinModel;
import com.sgo.saldomu.models.retrofit.RegModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 10/21/2016.
 */

public class Regist2 extends BaseFragment {
    View v;
    EditText etToken;
    TextView currEmail;
    Button btnProses, btnCancel;
    String namaValid, noHPValid, emailValid, authType, token, pass, confPass, memberID, custID = "";
    String flag_change_pwd, flag_change_pin;
    ProgressDialog progdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist2, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        getActivity().getWindow().setBackgroundDrawableResource(R.drawable.background);
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if (sp.contains(DefineValue.SENDER_ID)) {
            custID = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
        }

        Bundle args = getArguments();
        if (args != null) {
            noHPValid = args.getString(DefineValue.CUST_PHONE, "");
            namaValid = args.getString(DefineValue.CUST_NAME, "");
            emailValid = args.getString(DefineValue.CUST_EMAIL, "-");
        }

        etToken = v.findViewById(R.id.token_value);
        currEmail = v.findViewById(R.id.text_email);
        btnProses = v.findViewById(R.id.btn_token);
        btnCancel = v.findViewById(R.id.btn_cancel);

        currEmail.setText(getString(R.string.validasi_email_text) + " " + emailValid);

        btnProses.setOnClickListener(btnProsesClickListener);
        btnCancel.setOnClickListener(btnCancelClickListener);
    }

    Button.OnClickListener btnProsesClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    sentData(etToken.getText().toString());
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener btnCancelClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            DefineValue.NOBACK = false;
            getFragmentManager().popBackStack();
        }
    };

    public boolean inputValidation() {
        if (etToken.getText().toString().length() == 0) {
            etToken.requestFocus();
            etToken.setError(getResources().getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    public void sentData(final String token) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            btnProses.setEnabled(false);

            HashMap<String, Object> params = new HashMap<>();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHPValid);
            params.put(WebParams.CUST_NAME, namaValid);
            params.put(WebParams.CUST_EMAIL, emailValid);
            params.put(WebParams.EMAIL_TOKEN, token);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.IS_SMS, "Y");
            params.put(WebParams.IS_EMAIL, "N");

            Timber.d("isi params reg2:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REG_STEP2, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            RegModel model = getGson().fromJson(object, RegModel.class);

                            String code = model.getError_code();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                String flag_process = model.getFlag_process();
                                if (flag_process.equals("N")) {
                                    namaValid = model.getCust_name();
                                    emailValid = model.getCust_email();
                                    noHPValid = model.getCust_phone();
                                    changeActivity(token);
                                } else {
                                    flag_change_pwd = model.getFlag_change_pwd();
                                    flag_change_pin = model.getFlag_change_pin();
                                    check();
                                }
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
                            btnProses.setEnabled(true);
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sendCreatePass() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            String link = MyApiClient.LINK_CREATE_PASS;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = noHPValid + pass;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PASS, RSA.opensslEncrypt(uuid, dateTime, noHPValid, pass, subStringLink));
            params.put(WebParams.CONF_PASS, RSA.opensslEncrypt(uuid, dateTime, noHPValid, confPass, subStringLink));
            params.put(WebParams.CUST_ID, noHPValid);

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
                                flag_change_pwd = "N";
                                check();
                            } else if(code.equals("0301")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getActivity().getResources().getString(R.string.logout)).setMessage(model.getError_message())
                                        .setCancelable(false)
                                        .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                            }else {

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
            extraSignature = memberID + noHPValid + pin;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.USER_ID, noHPValid);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, RSA.opensslEncrypt(uuid, dateTime, noHPValid, pin, subStringLink));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(uuid, dateTime, noHPValid, confirmPin, subStringLink));

            Timber.d("params create pin:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            CreatePinModel model = getGson().fromJson(object, CreatePinModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                flag_change_pin = "N";
                                check();
                            } else {

                                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getActivity(), CreatePIN.class);
                                i.putExtra(DefineValue.REGISTRATION, true);
                                switchActivity(i);
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
                    } );
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void check() {
        if (flag_change_pwd.equals("Y")) {
            Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
            i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
            switchActivityPIN(i);
        } else if (flag_change_pin.equals("Y")) {
            Intent i = new Intent(getActivity(), CreatePIN.class);
            i.putExtra(DefineValue.REGISTRATION, true);
            switchActivityPIN(i);
        } else showDialog();
    }

    private void switchActivityPIN(Intent i) {
        startActivityForResult(i, LoginActivity.ACTIVITY_RESULT);
    }

    void showDialog() {
        SaveIMEIICCID();

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
        Message2.setText(noHPValid);
        Message2.setTextSize(getResources().getDimension(R.dimen.abc_text_size_small_material));
        Message3.setText(getResources().getString(R.string.regist2_notif_message_3));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Fragment test = new Login();
                switchFragment(test, "Login", false);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("isi regist 2 requestCode:" + String.valueOf(requestCode));
        if (requestCode == LoginActivity.ACTIVITY_RESULT) {
            Timber.d("isi regist 2 resultcode:" + String.valueOf(resultCode));
            if (resultCode == LoginActivity.RESULT_PIN) {
                Timber.d("isi regist 2 authtype:" + authType);

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

    private void SaveIMEIICCID() {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.SaveImeiICCIDDevice();
    }

    public void changeActivity(String token) {
        DefineValue.NOBACK = true; //fragment selanjutnya tidak bisa menekan tombol BACK
        Fragment mFragment = new Regist3();
        Bundle mBun = getArguments();
        mBun.putString(DefineValue.TOKEN, token);
        mFragment.setArguments(mBun);
        switchFragment(mFragment, "reg3", true);
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private void switchActivity(Intent i) {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchActivity(i);
    }
}
