package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.myFriendModel;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.PrivacyPolicyActivity;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.DeviceUtils;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.FingerprintDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.LoginCommunityModel;
import com.sgo.saldomu.models.retrofit.LoginModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.FINGERPRINT_SERVICE;

/**
 * Created by Administrator on 7/10/2014.
 */
public class Login extends BaseFragment implements View.OnClickListener {
//public class Login extends BaseFragment implements View.OnClickListener, FingerprintDialog.FingerprintDialogListener {

    private String userIDfinale = null, is_pos;
    private Button btnforgetPass;
    private Button btnRegister;
    private TextView btnPrivacyPolicy;
    private EditText userIDValue;
    private EditText passLoginValue;
    private ImageView image_spinner, toogleViewPass, logo;
    private Button btnLogin;
    private Animation frameAnimation;
    //    private MaterialRippleLayout btnLayout;
    private View v;
    private Bundle argsBundleNextLogin = new Bundle();
    private Boolean isTexted = false;
//    private Boolean isFingerprint = false;
    private FingerprintManager fingerprintManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_login_new, container, false);

        toogleViewPass = v.findViewById(R.id.passLogin_toogle_view);
        userIDValue = v.findViewById(R.id.userID_value);
        passLoginValue = v.findViewById(R.id.passLogin_value);
        btnLogin = v.findViewById(R.id.btn_login);
        btnforgetPass = v.findViewById(R.id.btn_forgetPass);
        btnPrivacyPolicy = v.findViewById(R.id.tv_privacypolicy);
        btnRegister = v.findViewById(R.id.btn_register);
        image_spinner = v.findViewById(R.id.image_spinning_wheel);
        logo = v.findViewById(R.id.logo);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        argsBundleNextLogin = getArguments();


//        btnLayout = (MaterialRippleLayout) v.findViewById(R.id.btn_login_ripple_layout);


        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();


        Bundle m = getArguments();

        if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) { //untuk shorcut dari tombol di activity LoginActivity
            if (m != null && m.containsKey(DefineValue.USER_IS_NEW)) {
                getActivity().findViewById(R.id.userID_value).setVisibility(View.VISIBLE);
            }
            userIDValue.setEnabled(true);
        }

//        if (m != null) {
//            if (m.containsKey(DefineValue.IS_POS)) {
//                if (m.getString(DefineValue.IS_POS).equalsIgnoreCase("Y")) {
//                    is_pos = m.getString(DefineValue.IS_POS, "");
//                    getActivity().findViewById(R.id.userID_value).setVisibility(View.VISIBLE);
//                    userIDValue.setEnabled(true);
//                    userIDValue.setHint("No HP POS yang sudah terdaftar");
//                }
//            }
//        } else if (sp.getString(DefineValue.IS_POS, "N").equalsIgnoreCase("Y")) {
//            getActivity().findViewById(R.id.userID_value).setVisibility(View.VISIBLE);
//            userIDValue.setEnabled(true);
//        }


        if (sp.contains(DefineValue.SENDER_ID) && !sp.getString(DefineValue.IS_POS, "N").equalsIgnoreCase("Y")) {
            userIDfinale = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
            userIDValue.setText(userIDfinale);
            userIDValue.setVisibility(View.GONE);
        } else if (!sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "").isEmpty() && m.getString(DefineValue.IS_POS, "N").equalsIgnoreCase("N")) {
            userIDfinale = NoHPFormat.formatTo62(sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""));
            userIDValue.setText(userIDfinale);
            userIDValue.setVisibility(View.GONE);
//            if (!sp.getString(DefineValue.USER_PASSWORD, "").equals("")) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    fingerprintManager =
//                            (FingerprintManager) getActivity().getSystemService(FINGERPRINT_SERVICE);
//
//                    //Check whether the device has a fingerprint sensor//
//                    if (!fingerprintManager.isHardwareDetected() ||
//                            (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
//                            || !fingerprintManager.hasEnrolledFingerprints()) {
//                        // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
//
//                    } else if (sp.getString(DefineValue.USER_PASSWORD, "") != "") {
//                        // Create and show the dialog.
//                        isFingerprint = false;
//                        FingerprintDialog fingerprintDialog = new FingerprintDialog();
//                        fingerprintDialog.setTargetFragment(Login.this, 300);
//                        fingerprintDialog.setCancelable(true);
//                        fingerprintDialog.show(getActivity().getSupportFragmentManager(), "FingerprintDialog");
//                    }
//                }
//            }

        } else if (m != null) {
            if (m.containsKey(DefineValue.IS_POS)) {
                if (m.getString(DefineValue.IS_POS).equalsIgnoreCase("Y")) {
                    is_pos = m.getString(DefineValue.IS_POS, "");
                    logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_pos));
                    getActivity().findViewById(R.id.userID_value).setVisibility(View.VISIBLE);
                    userIDValue.setEnabled(true);
                    userIDValue.setHint("No HP POS yang sudah terdaftar");
                }
            }
        } else if (sp.getString(DefineValue.IS_POS, "N").equalsIgnoreCase("Y")) {
            getActivity().findViewById(R.id.userID_value).setVisibility(View.VISIBLE);
            userIDValue.setEnabled(true);
        }

        btnLogin.setOnClickListener(this);
        btnforgetPass.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        toogleViewPass.setOnClickListener(this);
        passLoginValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    toogleViewPass.setVisibility(View.VISIBLE);
                } else {
                    toogleViewPass.setVisibility(View.INVISIBLE);
                }
            }
        });


        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), PrivacyPolicyActivity.class);
                startActivity(i);
            }
        });

//        String mcAddress = new DeviceUtils(getActivity()).getWifiMcAddress();
//        String deviceModel = new DeviceUtils(getActivity()).getDeviceModelID();
//        String androidId = new DeviceUtils(getActivity()).getAndroidID();
//        Timber.e("mcaddress: " + mcAddress + " // devicemodel: " + deviceModel + " // androidid: " + androidId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (InetHandler.isNetworkAvailable(getActivity())) {
                    if (inputValidation()) {
                        userIDfinale = NoHPFormat.formatTo62(userIDValue.getText().toString());
                        sentDatas();
                    }
                } else
                    DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
                break;
            case R.id.btn_forgetPass:
                Fragment newFrag = new ForgotPassword();
                switchFragment(newFrag, "forgot password", true);
                break;
            case R.id.btn_register:
                newFrag = new Regist1();
                switchFragment(newFrag, "reg1", true);
                break;

            case R.id.passLogin_toogle_view:
//                toogleViewPass.setOnTouchListener((v1, event) -> {
//                    switch ( event.getAction() ) {
                if (isTexted == false) {
//                    case MotionEvent.ACTION_DOWN:
                    passLoginValue.setInputType(InputType.TYPE_CLASS_TEXT);
                    passLoginValue.setTypeface(Typeface.DEFAULT_BOLD);
                    passLoginValue.setSelection(passLoginValue.getText().length());
                    isTexted = true;
                    break;
                } else {
//                        case MotionEvent.ACTION_UP:
                    passLoginValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passLoginValue.setTypeface(Typeface.DEFAULT_BOLD);
                    passLoginValue.setSelection(passLoginValue.getText().length());
                    isTexted = false;
                    break;
                }
//                    return true;
//                });
//                break;
        }

    }

    boolean checkIsPOS() {
        if (is_pos != null)
            return !is_pos.equalsIgnoreCase("");
        return false;
    }

//    @Override
//    public void onFinishFingerprintDialog(boolean result) {
//        isFingerprint = result;
//        if (isFingerprint)
//            sentDatas();
//    }

    private void sentDatas() {
        ToggleKeyboard toggleKeyboard = new ToggleKeyboard();
        toggleKeyboard.hide_keyboard(getActivity());
        try {
            String comm_id = MyApiClient.COMM_ID;
//            String encrypted_password = RSA.opensslEncrypt(passLoginValue.getText().toString()
//                    , BuildConfig.OPENSSL_ENCRYPT_KEY, BuildConfig.OPENSSL_ENCRYPT_IV);

            btnLogin.setEnabled(false);
            userIDValue.setEnabled(false);
            btnRegister.setEnabled(false);
            passLoginValue.setEnabled(false);
            btnforgetPass.setEnabled(false);
            btnLogin.setVisibility(View.INVISIBLE);
            image_spinner.setVisibility(View.VISIBLE);
            image_spinner.startAnimation(frameAnimation);
//            if (isFingerprint) {
//                extraSignature = sp.getString(DefineValue.EXTRA_SIGNATURE, "");
//            } else {
                extraSignature = userIDfinale + passLoginValue.getText().toString();
//            }
            params = RetrofitService.getInstance()
                    .getSignatureSecretKey(MyApiClient.LINK_LOGIN, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userIDfinale);
//            if (isFingerprint) {
//                params.put(WebParams.PASSWORD_LOGIN, sp.getString(DefineValue.USER_PASSWORD, ""));
//            } else {
                params.put(WebParams.PASSWORD_LOGIN, RSA.opensslEncrypt(passLoginValue.getText().toString()));
//            }
//            params.put(WebParams.PASSWORD_LOGIN, encrypted_password);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.MAC_ADDR, new DeviceUtils().getWifiMcAddress());
            params.put(WebParams.DEV_MODEL, new DeviceUtils().getDeviceModelID());
            params.put(WebParams.CLIENT_APP, DefineValue.ANDROID);
            if (checkIsPOS())
                params.put(WebParams.IS_POS, is_pos);
            if (sp.getString(DefineValue.FCM_ID, "") != null)
                params.put(WebParams.FCM_ID, sp.getString(DefineValue.FCM_ID, ""));

            Timber.d("isi params login:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_LOGIN, params, new ResponseListener() {

                @Override
                public void onResponses(JsonObject response) {
                    LoginModel loginModel = getGson().fromJson(response.toString(), LoginModel.class);

                    image_spinner.clearAnimation();
                    image_spinner.setVisibility(View.INVISIBLE);
                    btnLogin.setEnabled(true);
                    userIDValue.setEnabled(true);
                    btnRegister.setEnabled(true);
                    passLoginValue.setEnabled(true);
                    btnforgetPass.setEnabled(true);
                    btnLogin.setVisibility(View.VISIBLE);

                    String code = loginModel.getError_code();

                    if (code.equalsIgnoreCase(WebParams.SUCCESS_CODE)) {
                        String unregist_member = loginModel.getCommunity().get(0).getUnregisterMember();
                        sp.edit().putString(DefineValue.IS_POS, is_pos).commit();
                        sp.edit().putString(DefineValue.EXTRA_SIGNATURE, extraSignature).commit();
//                        if (!isFingerprint)
//                            sp.edit().putString(DefineValue.USER_PASSWORD, RSA.opensslEncrypt(passLoginValue.getText().toString())).commit();
                        if (checkCommunity(loginModel.getCommunity())) {
                            if (unregist_member.equals("N")) {
                                Toast.makeText(getActivity(), getString(R.string.login_toast_loginsukses), Toast.LENGTH_LONG).show();
                                setLoginProfile(loginModel);

                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString(DefineValue.USER_ID, userIDValue.getText().toString());
                                bundle.putBoolean(DefineValue.IS_UNREGISTER_MEMBER, true);
                                Fragment newFrag = new Regist1();
                                newFrag.setArguments(bundle);
                                switchFragment(newFrag, "reg1", true);
                            }
                        }
                    } else if (code.equals(DefineValue.ERROR_0042)) {

                        String message;

                        if (checkIsPOS()) {
                            message = loginModel.getError_message();
                        } else {
                            int failed = Integer.valueOf(loginModel.getFailedAttempt());
                            int max = Integer.valueOf(loginModel.getMaxFailed());

                            if (max - failed == 0) {
                                message = getString(R.string.login_failed_attempt_3);
                            } else {
                                message = getString(R.string.login_failed_attempt_1, max - failed);
                            }
                        }
                        showDialog(message);
                    } else if (code.equals(DefineValue.ERROR_0126)) {
                        showDialog(getString(R.string.login_failed_attempt_3));
                    } else if (code.equals(DefineValue.ERROR_0018) || code.equals(DefineValue.ERROR_0017)) {
                        showDialog(getString(R.string.login_failed_inactive));
                    } else if (code.equals(DefineValue.ERROR_0127)) {
                        showDialog(getString(R.string.login_failed_dormant));
                    } else if (code.equals(DefineValue.ERROR_0004)) {
                        String msg = loginModel.getError_message();
                        if (msg != null && !msg.isEmpty()) {
                            showDialog(msg);
                        } else
                            showDialog(getString(R.string.login_failed_wrong_pass));
                    } else if (code.equals(DefineValue.ERROR_0002)) {
                        showDialog(getString(R.string.login_failed_wrong_id));
                    } else {
                        Toast.makeText(getActivity(), loginModel.getError_message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {
                    image_spinner.clearAnimation();
                    image_spinner.setVisibility(View.INVISIBLE);
                    btnLogin.setEnabled(true);
                    userIDValue.setEnabled(true);
                    passLoginValue.setEnabled(true);
                    btnforgetPass.setEnabled(true);
                    btnRegister.setEnabled(true);
                    btnLogin.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void showDialog(String message) {
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
        Title.setText(getString(R.string.login_failed_attempt_title));
        Message.setText(message);


        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void changeActivity() {
        Intent i = new Intent(getActivity(), MainPage.class);
        if (argsBundleNextLogin != null)
            i.putExtras(argsBundleNextLogin);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
//        getActivity().finish();

    }

    private boolean checkCommunity(List<LoginCommunityModel> model) {
        if (model != null) {
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).getCommId().equals(MyApiClient.COMM_ID)) {
                    Timber.w("check comm id yg bener: " + model.get(i).getCommId());
                    return true;
                }
            }
        }

        Toast.makeText(getActivity(), getString(R.string.login_validation_comm), Toast.LENGTH_LONG).show();
        return false;
    }

    private void setLoginProfile(LoginModel model) {

        try {
            SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
            SecurePreferences.Editor mEditor = prefs.edit();
            String arraynya;
            String userId = model.getUserId();
            String prevContactFT = prefs.getString(DefineValue.PREVIOUS_CONTACT_FIRST_TIME, "");

            if (prefs.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "").equals(userId)) {
                mEditor.putString(DefineValue.CONTACT_FIRST_TIME, prevContactFT);
                mEditor.putString(DefineValue.BALANCE_AMOUNT, prefs.getString(DefineValue.PREVIOUS_BALANCE, "0"));
                mEditor.putBoolean(DefineValue.IS_SAME_PREVIOUS_USER, true);
            } else {
                if (prevContactFT.equals(DefineValue.NO)) {
                    myFriendModel.deleteAll();
                    mEditor.putString(DefineValue.CONTACT_FIRST_TIME, DefineValue.YES);
                }
                mEditor.putString(DefineValue.BALANCE_AMOUNT, "0");
                mEditor.putBoolean(DefineValue.IS_SAME_PREVIOUS_USER, false);
                BBSDataManager.resetBBSData();
            }

            mEditor.putString(DefineValue.USERID_PHONE, userId);
            mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_YES);
            mEditor.putString(DefineValue.USER_NAME, model.getUserName());
            mEditor.putString(DefineValue.CUST_ID, model.getCustId());
            mEditor.putString(DefineValue.CUST_NAME, model.getCustName());
            mEditor.putString(DefineValue.IS_MEMBER_SHOP_DGI, model.getIs_member_shop_dgi());

            mEditor.putString(DefineValue.PROFILE_DOB, model.getDateOfBirth());
            mEditor.putString(DefineValue.PROFILE_ADDRESS, model.getAddress());
            mEditor.putString(DefineValue.PROFILE_BIO, model.getBio());
            mEditor.putString(DefineValue.PROFILE_COUNTRY, model.getCountry());
            mEditor.putString(DefineValue.PROFILE_EMAIL, model.getEmail());
            mEditor.putString(DefineValue.PROFILE_FULL_NAME, model.getFullName());
            mEditor.putString(DefineValue.PROFILE_SOCIAL_ID, model.getSocialId());
            mEditor.putString(DefineValue.PROFILE_HOBBY, model.getHobby());
            mEditor.putString(DefineValue.PROFILE_POB, model.getBirthPlace());
            mEditor.putString(DefineValue.PROFILE_GENDER, model.getGender());
            mEditor.putString(DefineValue.PROFILE_ID_TYPE, model.getIdtype());
            mEditor.putString(DefineValue.PROFILE_VERIFIED, model.getVerified());
            mEditor.putString(DefineValue.PROFILE_BOM, model.getMotherName());

            mEditor.putString(DefineValue.LIST_ID_TYPES, getGson().toJson(model.getIdTypes()));
//            mEditor.putString(DefineValue.LIST_CONTACT_CENTER,response.getString(WebParams.CONTACT_CENTER));

            mEditor.putString(DefineValue.IS_FIRST, model.getUserIsNew());
            mEditor.putString(DefineValue.IS_CHANGED_PASS, model.getChangedPass());

            mEditor.putString(DefineValue.IMG_URL, model.getImgUrl());
            mEditor.putString(DefineValue.IMG_SMALL_URL, model.getImgSmallUrl());
            mEditor.putString(DefineValue.IMG_MEDIUM_URL, model.getImgMediumUrl());
            mEditor.putString(DefineValue.IMG_LARGE_URL, model.getImgLargeUrl());

            mEditor.putString(DefineValue.ACCESS_KEY, model.getAccessKey());
            mEditor.putString(DefineValue.ACCESS_SECRET, model.getAccessSecret());

            mEditor.putString(DefineValue.LINK_APP, model.getSocialSignature());
            mEditor.putString(DefineValue.IS_DORMANT, model.getIs_dormant());

            if (Integer.valueOf(model.getIsRegistered()) == 0)
                mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
            else
                mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, true);

            if (!model.getCommunity().isEmpty()) {
                mEditor.putInt(DefineValue.COMMUNITY_LENGTH, model.getCommunity().size());
                for (int i = 0; i < model.getCommunity().size(); i++) {
                    LoginCommunityModel commModel = model.getCommunity().get(i);
                    if (commModel.getCommId().equals(MyApiClient.COMM_ID)) {
                        mEditor.putString(DefineValue.COMMUNITY_ID, commModel.getCommId());
                        mEditor.putString(DefineValue.CALLBACK_URL_TOPUP, commModel.getCallbackUrl());
                        mEditor.putString(DefineValue.API_KEY_TOPUP, commModel.getApiKey());
                        mEditor.putString(DefineValue.COMMUNITY_CODE, commModel.getCommCode());
                        mEditor.putString(DefineValue.COMMUNITY_NAME, commModel.getCommName());
                        mEditor.putString(DefineValue.BUSS_SCHEME_CODE, commModel.getBussSchemeCode());
                        mEditor.putString(DefineValue.AUTHENTICATION_TYPE, commModel.getAuthenticationType());
                        mEditor.putString(DefineValue.LENGTH_AUTH, commModel.getLengthAuth());
                        mEditor.putString(DefineValue.IS_HAVE_PIN, commModel.getIsHavePin());
                        mEditor.putString(DefineValue.AGENT_TYPE, commModel.getAgent_type());
                        mEditor.remove(DefineValue.SENDER_ID);

                        mEditor.putInt(DefineValue.LEVEL_VALUE, Integer.valueOf(commModel.getMemberLevel()));
                        if (commModel.getAllowMemberLevel().equals(DefineValue.STRING_YES)) {

                            mEditor.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL, true);
                        } else
                            mEditor.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);

                        mEditor.putString(DefineValue.IS_NEW_BULK, commModel.getIsNewBulk());

                        mEditor.putBoolean(DefineValue.IS_AGENT, commModel.getIsAgent() > 0);

                        String arrJson = toJson(commModel.getAgent_scheme_codes()).toString();
                        mEditor.putString(DefineValue.AGENT_SCHEME_CODES, arrJson);
                        mEditor.putString(DefineValue.IS_AGENT_TRX_REQ, commModel.getIs_agent_trx_request());
                        Timber.w("isi comm id yg bener:" + commModel.getCommId());

                        break;
                    }
                }
            }

            if (!model.getShopIdAgent().equals("")) {
                mEditor.putString(DefineValue.IS_AGENT_SET_LOCATION, DefineValue.STRING_NO);
                mEditor.putString(DefineValue.IS_AGENT_SET_OPENHOUR, DefineValue.STRING_NO);
                mEditor.putString(DefineValue.SHOP_AGENT_DATA, getGson().toJson(model.getShopIdAgent()));
            }

            if (model.getSettings() != null) {
                mEditor.putInt(DefineValue.MAX_MEMBER_TRANS, Integer.valueOf(model.getSettings().get(0).getMaxMemberTransfer()));

            }

            mEditor.apply();

            changeActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean inputValidation() {
        if (userIDValue.getText().toString().length() == 0) {
            userIDValue.requestFocus();
            userIDValue.setError(this.getString(R.string.login_validation_userID));
            return false;
        } else if (passLoginValue.getText().toString().length() == 0) {
            passLoginValue.requestFocus();
            passLoginValue.setError(this.getString(R.string.login_validation_pass));
            return false;
        }
        return true;
    }


}