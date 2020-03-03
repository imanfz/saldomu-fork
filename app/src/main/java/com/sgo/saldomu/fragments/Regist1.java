package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CreatePIN;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.PasswordRegisterActivity;
import com.sgo.saldomu.activities.TermsAndCondition;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.TNCDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.CreatePassModel;
import com.sgo.saldomu.models.retrofit.CreatePinModel;
import com.sgo.saldomu.models.retrofit.RegModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
 Created by Administrator on 7/4/2014.
 */
public class Regist1 extends BaseFragment implements EasyPermissions.PermissionCallbacks {

    String namaValid = "", emailValid = "", noHPValid = "", token_id = "", member_code = "", max_resend_token = "3", authType, memberID;
    EditText namaValue, emailValue, noHPValue, referalValue;
    Button btnLanjut;
    String flag_change_pwd, flag_change_pin, pass, confPass;
    View v;
    final int RC_READ_SMS = 10;
    Fragment mFragment;
    ProgressDialog progdialog;
    SecurePreferences sp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
        progdialog.dismiss();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist1, container, false);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        openTNC();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        namaValue = getActivity().findViewById(R.id.name_value);
        emailValue = getActivity().findViewById(R.id.email_value);
        noHPValue = getActivity().findViewById(R.id.noHP_value);

        referalValue = v.findViewById(R.id.referal_value);

        btnLanjut = getActivity().findViewById(R.id.btn_reg1_verification);
        btnLanjut.setOnClickListener(btnNextClickListener);

//        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_PHONE_STATE)) {
//            if (isSimExists()) {
//
//                TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//                String Nomor1 = tm.getLine1Number();
//
//                noHPValue.setText(Nomor1);
//            }
//        } else {
//            EasyPermissions.requestPermissions(this, getString(R.string.rationale_check_phone_number),
//                    RC_READ_SMS, Manifest.permission.READ_PHONE_STATE);
//        }


        noHPValue.requestFocus();
        ToggleKeyboard.show_keyboard(getActivity());

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();

        sp.edit().remove(DefineValue.USER_PASSWORD).apply();


        if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) {
            Bundle m = getArguments();
            if (m != null && m.containsKey(DefineValue.USER_IS_NEW)) {
                v.findViewById(R.id.noHP_value).setVisibility(View.VISIBLE);
                noHPValue.setEnabled(true);
            }
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            Boolean is_unregister_member = bundle.getBoolean(DefineValue.IS_UNREGISTER_MEMBER, false);
            String userId = bundle.getString(DefineValue.USER_ID);
            if (is_unregister_member) {
                noHPValue.setText(userId);
                noHPValue.setEnabled(false);
            }
        }

        if (sp.contains(DefineValue.SENDER_ID)) {
            noHPValid = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
            noHPValue.setText(noHPValid);
            noHPValue.setEnabled(false);
        }


    }

    private void openTNC() {
        DialogFragment dialog = TNCDialog.newDialog(dialog1 -> dialog1.dismiss());
        dialog.show(getActivity().getSupportFragmentManager(), "Dialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    Button.OnClickListener btnNextClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view == btnLanjut) {
                if (InetHandler.isNetworkAvailable(getActivity())) {
                    if (inputValidation()) {
                        sentData(NoHPFormat.formatTo62(noHPValue.getText().toString()));
                    }
                } else
                    DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("isi regist 1 requestCode:" + String.valueOf(requestCode));
        if (requestCode == LoginActivity.ACTIVITY_RESULT) {
            Timber.d("isi regist 1 resultcode:" + String.valueOf(resultCode));
            if (resultCode == LoginActivity.RESULT_PIN) {
                Timber.d("isi regist 1 authtype:" + authType);

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

    private void SaveIMEIICCID() {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.SaveImeiICCIDDevice();
    }

    public void sentData(final String noHP) {
        try {
            progdialog.show();

            btnLanjut.setEnabled(false);
            noHPValue.setEnabled(false);
            namaValue.setEnabled(false);
            emailValue.setEnabled(false);

            extraSignature = noHP;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_REG_STEP1, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHP);
            params.put(WebParams.CUST_NAME, namaValue.getText());
            params.put(WebParams.CUST_EMAIL, emailValue.getText());
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.FLAG_NEW_FLOW, DefineValue.Y);
            params.put(WebParams.LATITUDE, sp.getDouble(DefineValue.LATITUDE_UPDATED,0.0));
            params.put(WebParams.LONGITUDE, sp.getDouble(DefineValue.LONGITUDE_UPDATED,0.0));
            if (referalValue.getText().toString().trim().length() >0) {
                params.put(WebParams.REFERAL_NO, referalValue.getText());
            } else params.put(WebParams.REFERAL_NO, "");

            Timber.d("isi params reg1:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REG_STEP1, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {


                            RegModel model = getGson().fromJson(object, RegModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                if (model.getFlag_process() != null) {
                                    String flag_process = model.getFlag_process();
                                    if (flag_process.equals("N")) {
                                        namaValid = model.getCust_name();
                                        emailValid = model.getCust_email();
                                        noHPValid = model.getCust_phone();
                                        Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                                        i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                                        switchActivityPIN(i);
                                    } else {
                                        flag_change_pwd = model.getFlag_change_pwd();
                                        flag_change_pin = model.getFlag_change_pin();
                                        check(code);
                                    }
                                }  else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + object.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                }else {
                                    namaValid = model.getCust_name();
                                    emailValid = model.getCust_email();
                                    noHPValid = model.getCust_phone();

                                    Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                                    i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                                    switchActivityPIN(i);
                                }
                            } else if (code.equals("0002")) {
                                showDialog(code);
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
                            btnLanjut.setEnabled(true);
                            noHPValue.setEnabled(true);
                            namaValue.setEnabled(true);
                            emailValue.setEnabled(true);

                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    } );
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sendCreatePass() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            extraSignature = noHPValid + pass;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_CREATE_PASS, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PASS, RSA.opensslEncrypt(pass));
            params.put(WebParams.CONF_PASS, RSA.opensslEncrypt(confPass));
            params.put(WebParams.CUST_ID, noHPValid);
            params.put(WebParams.FLAG_NEW_FLOW, DefineValue.Y);

            Timber.d("params create pass:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_CREATE_PASS, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            CreatePassModel model = getGson().fromJson(object, CreatePassModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                memberID = model.getMember_id();
                                if (model.getFlag_change_pwd() != null) {
                                    flag_change_pwd = "N";
                                    check(code);
                                } else {
                                    showDialog(code);
                                }
//                            }  else if(code.equals("0301")){
//                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                                builder.setTitle(getString(R.string.password_validation))
//                                        .setMessage(getString(R.string.password_clue))
//                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                dialog.dismiss();
//                                            }
//                                        });
//                                AlertDialog dialog = builder.create();
//                                dialog.show();
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
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

            extraSignature = memberID + noHPValid + data.getStringExtra(DefineValue.PIN_VALUE);
            HashMap<String, Object> params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_CREATE_PIN, extraSignature);
            params.put(WebParams.USER_ID, noHPValid);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, RSA.opensslEncrypt(data.getStringExtra(DefineValue.PIN_VALUE)));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(data.getStringExtra(DefineValue.CONF_PIN)));

            Timber.d("params create pin:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_ADD_LIKE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            CreatePinModel model = getGson().fromJson(object, CreatePinModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                    if (response.has(WebParams.FLAG_CHANGE_PIN))
                                if (model.getFlag_change_pin() != null) {
                                    flag_change_pin = "N";
                                    check(code);
                                } else
                                    showDialog(code);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            }else {
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

    public void changeActivity(Boolean login) {
        if (login) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            SaveIMEIICCID();
            Fragment test = new Login();
            Bundle mBun = new Bundle();
            mBun.putString(DefineValue.IS_POS, "N");
            test.setArguments(mBun);
            switchFragment(test, "Login", false);
        } else {
            DefineValue.NOBACK = true; //fragment selanjutnya tidak bisa menekan tombol BACK
            mFragment = new Regist2();
            Bundle mBun = new Bundle();
            mBun.putString(DefineValue.CUST_NAME, namaValid);
            mBun.putString(DefineValue.CUST_PHONE, noHPValid);
            mBun.putString(DefineValue.CUST_EMAIL, emailValid);
//            mBun.putString(DefineValue.TOKEN,token_id);
//            mBun.putString(DefineValue.MAX_RESEND,max_resend_token);
//            mBun.putString(DefineValue.AUTHENTICATION_TYPE,auth_type);
            mFragment.setArguments(mBun);
            switchFragment(mFragment, "reg2", true);
        }
    }

    private void check(String code) {
        if (flag_change_pwd.equals("Y")) {
            Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
            i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
            switchActivityPIN(i);
        } else if (flag_change_pin.equals("Y")) {
            Intent i = new Intent(getActivity(), CreatePIN.class);
            i.putExtra(DefineValue.REGISTRATION, true);
            switchActivityPIN(i);
        } else showDialog(code);
    }

    void showDialog(final String code) {
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

        sp.edit().putString(DefineValue.PREVIOUS_LOGIN_USER_ID,noHPValue.getText().toString()).commit();

        Message.setVisibility(View.VISIBLE);
        Title.setText(getResources().getString(R.string.regist1_notif_title));
        if (code.equals("0002")) {
            Title.setText(getResources().getString(R.string.regist1_notif_title_registered));
            Message.setText(getResources().getString(R.string.regist1_notif_message_registered));
        } else if (code.equals(WebParams.SUCCESS_CODE)) {
            Title.setText(getResources().getString(R.string.regist2_notif_title));
            Message.setText(getResources().getString(R.string.regist2_notif_message_1));
            Message2.setText(noHPValue.getText().toString());
            Message2.setTextSize(getResources().getDimension(R.dimen.abc_text_size_small_material));
            Message3.setText(getResources().getString(R.string.regist2_notif_message_3));
        }

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (code.equals(WebParams.SUCCESS_CODE) || code.equals("0002"))
                    changeActivity(true);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void switchActivityPIN(Intent i) {
        /*if (getActivity() == null)
            return;

        Registration fca = (Registration) getActivity();
        fca.switchActivity(i, Registration.ACTIVITY_RESULT);*/
        startActivityForResult(i, LoginActivity.ACTIVITY_RESULT);
    }

    //----------------------------------------------------------------------------------------------------------------

    public boolean inputValidation() {
        if (noHPValue.getText().toString().length() == 0) {
            noHPValue.requestFocus();
            noHPValue.setError(getResources().getString(R.string.regist1_validation_nohp));
            return false;
        } else if (namaValue.getText().toString().length() < 2) {
            namaValue.requestFocus();
            namaValue.setError(getResources().getString(R.string.regist1_validation_nama));
            return false;
        } else if (emailValue.getText().toString().length() == 0) {
            emailValue.requestFocus();
            emailValue.setError(getResources().getString(R.string.regist1_validation_email_length));
            return false;
        } else if (emailValue.getText().toString().length() > 0 && !isValidEmail(emailValue.getText())) {
            emailValue.requestFocus();
            emailValue.setError(getString(R.string.regist1_validation_email));
            return false;
        } else if (referalValue.getText().toString().length() != 0) {
            if (referalValue.length() < 9 || referalValue.length() > 13) {
                referalValue.requestFocus();
                referalValue.setError("Masukkan No. Referal yang sesuai!");
                return false;
            } else if (referalValue.getText().toString().equals(noHPValue.getText().toString())) {
                referalValue.requestFocus();
                referalValue.setError("Nomor Referal tidak boleh sama dengan No. HP Pelanggan!");
                return false;
            }
        }
        return true;
    }

    public boolean isSimExists() {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        int SIM_STATE = telephonyManager.getSimState();

        if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
            return true;
        else {
            switch (SIM_STATE) {
                case TelephonyManager.SIM_STATE_ABSENT: //SimState = "No Sim Found!";
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                    break;
            }
            return false;
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == RC_READ_SMS) {
            if (isSimExists()) {
                TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                String Nomor1 = tm.getLine1Number();

                noHPValue.setText(Nomor1);
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }


}