package com.sgo.orimakardaya.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

import com.balysv.materialripple.MaterialRippleLayout;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.BalanceModel;
import com.sgo.orimakardaya.Beans.myFriendModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.LoginActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.DeviceUtils;
import com.sgo.orimakardaya.coreclass.InetHandler;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.NoHPFormat;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.securities.AES;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
  Created by Administrator on 7/10/2014.
 */
public class Login extends Fragment implements View.OnClickListener {

    String userIDfinale = null;
    Fragment newFrag;
    Button btnforgetPass, btnRegister;
    EditText userIDValue,passLoginValue;
    ImageView image_spinner;
    Button btnLogin;
    Animation frameAnimation;
    MaterialRippleLayout btnLayout;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_login, container, false);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userIDValue = (EditText) v.findViewById(R.id.userID_value);
        userIDValue.requestFocus();
        passLoginValue = (EditText) v.findViewById(R.id.passLogin_value);

        btnLogin = (Button) v.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        btnLayout = (MaterialRippleLayout) v.findViewById(R.id.btn_login_ripple_layout);

        btnforgetPass = (Button) v.findViewById(R.id.btn_forgetPass);
        btnforgetPass.setOnClickListener(this);

        btnRegister = (Button) v.findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);

        image_spinner = (ImageView) v.findViewById(R.id.image_spinning_wheel);
        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);


//        String mcAddress = new DeviceUtils(getActivity()).getWifiMcAddress();
//        String deviceModel = new DeviceUtils(getActivity()).getDeviceModelID();
//        String androidId = new DeviceUtils(getActivity()).getAndroidID();
//        Timber.e("mcaddress: " + mcAddress + " // devicemodel: " + deviceModel + " // androidid: " + androidId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login :
                if(InetHandler.isNetworkAvailable(getActivity())){
                    if(inputValidation()){
                        userIDfinale = NoHPFormat.editNoHP(userIDValue.getText().toString());
                        sentData();
                    }
                }
                else
                    DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
                break;
            case R.id.btn_forgetPass :
                newFrag = new ForgotPassword();
                switchFragment(newFrag,"forgot password",true);
                break;
            case R.id.btn_register :
                newFrag = new Regist1();
                switchFragment(newFrag, "reg1", true);
                break;
        }
    }


    public void sentData(){
        try{
            btnLogin.setEnabled(false);
            userIDValue.setEnabled(false);
            btnRegister.setEnabled(false);
            passLoginValue.setEnabled(false);
            btnforgetPass.setEnabled(false);
            btnLayout.setVisibility(View.INVISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            image_spinner.setVisibility(View.VISIBLE);
            image_spinner.startAnimation(frameAnimation);

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID,MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID,userIDfinale);
            params.put(WebParams.PASSWORD_LOGIN, AES.aes_encrypt(passLoginValue.getText().toString(), userIDfinale));
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.MAC_ADDR, new DeviceUtils(getActivity()).getWifiMcAddress());
            params.put(WebParams.DEV_MODEL, new DeviceUtils(getActivity()).getDeviceModelID());

            Timber.d("isi params login:" + params.toString());

            MyApiClient.sentDataLogin(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    image_spinner.clearAnimation();
                    image_spinner.setVisibility(View.INVISIBLE);
                    btnLogin.setEnabled(true);
                    userIDValue.setEnabled(true);
                    btnRegister.setEnabled(true);
                    passLoginValue.setEnabled(true);
                    btnforgetPass.setEnabled(true);
                    btnLayout.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String msg = response.getString(WebParams.ERROR_MESSAGE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params response login:"+response.toString());
                            if(checkCommunity(response)){
                                Toast.makeText(getActivity(), getString(R.string.login_toast_loginsukses), Toast.LENGTH_LONG).show();
                                setLoginProfile(response);
                                changeActivity();
                            }
                        } else {
                            if(code.equals(DefineValue.ERROR_0042)){
                                int failed = response.optInt(WebParams.FAILED_ATTEMPT,0);
                                int max = response.optInt(WebParams.MAX_FAILED,0);
                                String message = "";

                                switch ((max-failed)){
                                    case 0 : message = getString(R.string.login_failed_attempt_3);
                                        break;
                                    case 1 : message = getString(R.string.login_failed_attempt_1)+" 1 "+getString(R.string.login_failed_attempt_2);
                                        break;
                                    case 2 : message = getString(R.string.login_failed_attempt_1)+" 2 "+getString(R.string.login_failed_attempt_2);
                                        break;
                                    case 3 : message = getString(R.string.login_failed_attempt_1)+" 3 "+getString(R.string.login_failed_attempt_2);
                                        break;
                                }

                                showDialog(message);
                            }
                            else if(code.equals(DefineValue.ERROR_0126)){
                                showDialog(getString(R.string.login_failed_attempt_3));
                            }
                            else if(code.equals(DefineValue.ERROR_0018)||code.equals(DefineValue.ERROR_0017)){
                                showDialog(getString(R.string.login_failed_inactive));
                            }
                            else if(code.equals(DefineValue.ERROR_0127)){
                                showDialog(getString(R.string.login_failed_dormant));
                            }
                            else if(code.equals(DefineValue.ERROR_0004)){
                                if(msg != null && !msg.isEmpty())
                                    showDialog(msg);
                                else
                                    showDialog(getString(R.string.login_failed_wrong_pass));
                            }
                            else if(code.equals(DefineValue.ERROR_0002)){
                                showDialog(getString(R.string.login_failed_wrong_id));
                            }
                            else {
                                code = response.getString(WebParams.ERROR_MESSAGE);
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                            Timber.d("isi error login:" + response.toString());

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    ifFailure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    ifFailure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    ifFailure(throwable);
                }

                private void ifFailure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    image_spinner.clearAnimation();
                    image_spinner.setVisibility(View.INVISIBLE);
                    btnLogin.setEnabled(true);
                    userIDValue.setEnabled(true);
                    passLoginValue.setEnabled(true);
                    btnforgetPass.setEnabled(true);
                    btnLayout.setVisibility(View.VISIBLE);
                    btnRegister.setEnabled(true);
                    btnLogin.setVisibility(View.VISIBLE);
                    Timber.w("Error Koneksi login:" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    void showDialog(String message) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

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

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private void changeActivity() {
        Intent i = new Intent(getActivity(),MainPage.class);
        startActivity(i);
        getActivity().finish();

    }

    private boolean checkCommunity(JSONObject response){
        String arraynya = null;
        try {
            arraynya = response.getString(WebParams.COMMUNITY);
            if(!arraynya.isEmpty()){
                JSONArray arrayJson = new JSONArray(arraynya);
                for(int i = 0 ; i < arrayJson.length();i++){
                    if( arrayJson.getJSONObject(i).getString(WebParams.COMM_ID).equals(MyApiClient.COMM_ID)){
                        Timber.w("check comm id yg bener:"+arrayJson.getJSONObject(i).getString(WebParams.COMM_ID));
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(), getString(R.string.login_validation_comm), Toast.LENGTH_LONG).show();
        return false;
    }

    public void setLoginProfile(JSONObject response){
        getActivity();
        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        SecurePreferences.Editor mEditor = prefs.edit();
        String arraynya;
        try {
            String userId = response.getString(WebParams.USER_ID);
            String prevContactFT = prefs.getString(DefineValue.PREVIOUS_CONTACT_FIRST_TIME,"");

            if(prefs.getString(DefineValue.PREVIOUS_LOGIN_USER_ID,"").equals(userId)){
                mEditor.putString(DefineValue.CONTACT_FIRST_TIME,prevContactFT);
                mEditor.putString(DefineValue.BALANCE,prefs.getString(DefineValue.PREVIOUS_BALANCE,"0"));
            }
            else {
                if(prevContactFT.equals(DefineValue.NO)) {
                    myFriendModel.deleteAll();
                    mEditor.putString(DefineValue.CONTACT_FIRST_TIME, DefineValue.YES);
                }
                BalanceModel.deleteAll();
                mEditor.putString(DefineValue.BALANCE, "0");

            }

            mEditor.putString(DefineValue.USERID_PHONE, userId);
            mEditor.putString(DefineValue.FLAG_LOGIN, DefineValue.STRING_YES);
            mEditor.putString(DefineValue.USER_NAME, response.getString(WebParams.USER_NAME));
            mEditor.putString(DefineValue.CUST_ID,response.getString(WebParams.CUST_ID));
            mEditor.putString(DefineValue.CUST_NAME,response.getString(WebParams.CUST_NAME));

            mEditor.putString(DefineValue.PROFILE_DOB, response.getString(WebParams.DOB));
            mEditor.putString(DefineValue.PROFILE_ADDRESS,response.getString(WebParams.ADDRESS));
            mEditor.putString(DefineValue.PROFILE_BIO,response.getString(WebParams.BIO));
            mEditor.putString(DefineValue.PROFILE_COUNTRY,response.getString(WebParams.COUNTRY));
            mEditor.putString(DefineValue.PROFILE_EMAIL,response.getString(WebParams.EMAIL));
            mEditor.putString(DefineValue.PROFILE_FULL_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.PROFILE_SOCIAL_ID,response.getString(WebParams.SOCIAL_ID));
            mEditor.putString(DefineValue.PROFILE_HOBBY,response.getString(WebParams.HOBBY));
            mEditor.putString(DefineValue.PROFILE_POB,response.getString(WebParams.POB));
            mEditor.putString(DefineValue.PROFILE_GENDER,response.getString(WebParams.GENDER));
            mEditor.putString(DefineValue.PROFILE_ID_TYPE,response.getString(WebParams.ID_TYPE));
            mEditor.putString(DefineValue.PROFILE_VERIFIED,response.getString(WebParams.VERIFIED));
            mEditor.putString(DefineValue.PROFILE_BOM,response.getString(WebParams.MOTHER_NAME));

            mEditor.putString(DefineValue.LIST_ID_TYPES,response.getString(WebParams.ID_TYPES));
//            mEditor.putString(DefineValue.LIST_CONTACT_CENTER,response.getString(WebParams.CONTACT_CENTER));

//            mEditor.putString(DefineValue.IS_FIRST_TIME,response.getString(WebParams.USER_IS_NEW));
            mEditor.putString(DefineValue.IS_CHANGED_PASS,response.optString(WebParams.CHANGE_PASS, ""));

            mEditor.putString(DefineValue.IMG_URL, response.getString(WebParams.IMG_URL));
            mEditor.putString(DefineValue.IMG_SMALL_URL, response.getString(WebParams.IMG_SMALL_URL));
            mEditor.putString(DefineValue.IMG_MEDIUM_URL, response.getString(WebParams.IMG_MEDIUM_URL));
            mEditor.putString(DefineValue.IMG_LARGE_URL, response.getString(WebParams.IMG_LARGE_URL));

            mEditor.putString(DefineValue.ACCESS_KEY, response.getString(WebParams.ACCESS_KEY));
            mEditor.putString(DefineValue.ACCESS_SECRET, response.getString(WebParams.ACCESS_SECRET));

            mEditor.putString(DefineValue.LINK_APP,response.optString(WebParams.SOCIAL_SIGNATURE,""));

            if (response.optInt(WebParams.IS_REGISTERED,0) == 0)
                mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
            else
                mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, true);

            arraynya = response.getString(WebParams.COMMUNITY);
            if(!arraynya.isEmpty()){
                JSONArray arrayJson = new JSONArray(arraynya);
                mEditor.putInt(DefineValue.COMMUNITY_LENGTH,arrayJson.length());
                for(int i = 0 ; i < arrayJson.length();i++){
                    if( arrayJson.getJSONObject(i).getString(WebParams.COMM_ID).equals(MyApiClient.COMM_ID)){
                        mEditor.putString(DefineValue.COMMUNITY_ID, arrayJson.getJSONObject(i).getString(WebParams.COMM_ID));
                        mEditor.putString(DefineValue.CALLBACK_URL_TOPUP, arrayJson.getJSONObject(i).getString(WebParams.CALLBACK_URL));
                        mEditor.putString(DefineValue.API_KEY_TOPUP, arrayJson.getJSONObject(i).getString(WebParams.API_KEY));
                        mEditor.putString(DefineValue.COMMUNITY_CODE, arrayJson.getJSONObject(i).getString(WebParams.COMM_CODE));
                        mEditor.putString(DefineValue.COMMUNITY_NAME,arrayJson.getJSONObject(i).getString(WebParams.COMM_NAME));
                        mEditor.putString(DefineValue.BUSS_SCHEME_CODE,arrayJson.getJSONObject(i).getString(WebParams.BUSS_SCHEME_CODE));
                        mEditor.putString(DefineValue.AUTHENTICATION_TYPE, arrayJson.getJSONObject(i).getString(WebParams.AUTHENTICATION_TYPE));
                        mEditor.putString(DefineValue.LENGTH_AUTH, arrayJson.getJSONObject(i).getString(WebParams.LENGTH_AUTH));
                        mEditor.putString(DefineValue.IS_HAVE_PIN, arrayJson.getJSONObject(i).getString(WebParams.IS_HAVE_PIN));
                        mEditor.putInt(DefineValue.LEVEL_VALUE, arrayJson.getJSONObject(i).optInt(WebParams.MEMBER_LEVEL, 0));
                        if (arrayJson.getJSONObject(i).optString(WebParams.ALLOW_MEMBER_LEVEL, DefineValue.STRING_NO).equals(DefineValue.STRING_YES))
                            mEditor.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL,true);
                        else
                            mEditor.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL,false);
//                        mEditor.putString(DefineValue.CAN_TRANSFER,arrayJson.getJSONObject(i).optString(WebParams.CAN_TRANSFER, DefineValue.STRING_NO));
                        Timber.w("isi comm id yg bener:" + arrayJson.getJSONObject(i).getString(WebParams.COMM_ID));
                        break;
                    }
                }
            }

            arraynya = response.getString(WebParams.SETTINGS);
            if(!arraynya.isEmpty()){
                JSONArray arrayJson = new JSONArray(arraynya);
                mEditor.putInt(DefineValue.MAX_MEMBER_TRANS, arrayJson.getJSONObject(0).getInt(WebParams.MAX_MEMBER_TRANSFER));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        mEditor.apply();
    }

    public boolean inputValidation(){
        if(userIDValue.getText().toString().length()==0){
            userIDValue.requestFocus();
            userIDValue.setError(this.getString(R.string.login_validation_userID));
            return false;
        }
        else if(passLoginValue.getText().toString().length()==0){
            passLoginValue.requestFocus();
            passLoginValue.setError(this.getString(R.string.login_validation_pass));
            return false;
        }
        return true;
    }

}