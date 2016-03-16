package com.sgo.orimakardaya.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.activeandroid.ActiveAndroid;
import com.balysv.materialripple.MaterialRippleLayout;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.communityModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.LoginActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.activities.Registration;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
  Created by Administrator on 7/10/2014.
 */
public class Login extends Fragment {

    String userIDfinale = null,userEmail = "";

    Button btnforgetPass;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userIDValue = (EditText) v.findViewById(R.id.userID_value);
        passLoginValue = (EditText) v.findViewById(R.id.passLogin_value);

        btnLogin = (Button) v.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(loginListener);

        btnLayout = (MaterialRippleLayout) v.findViewById(R.id.btn_login_ripple_layout);

        btnforgetPass = (Button) v.findViewById(R.id.btn_forgetPass);
        btnforgetPass.setOnClickListener(forgetpassListener);

        image_spinner = (ImageView) v.findViewById(R.id.image_spinning_wheel);
        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

    }

    Button.OnClickListener loginListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            //passLoginValue.setText("12345678");
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(inputValidation()){
                    userIDfinale = NoHPFormat.editNoHP(userIDValue.getText().toString());
                    sentData();
                }
            }else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));

        }
    };

    Button.OnClickListener forgetpassListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment newFrag = new ForgotPassword();
            switchFragment(newFrag,"forgot password",true);
        }
    };

    public void sentData(){
        try{
            btnLogin.setEnabled(false);
            userIDValue.setEnabled(false);
            passLoginValue.setEnabled(false);
            btnforgetPass.setEnabled(false);
            btnLayout.setVisibility(View.INVISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            image_spinner.setVisibility(View.VISIBLE);
            image_spinner.startAnimation(frameAnimation);

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID,MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID,userIDfinale);
            params.put(WebParams.PASSWORD_LOGIN, Md5.hashMd5(passLoginValue.getText().toString()));
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

            Timber.d("isi params login:"+params.toString());

            MyApiClient.sentDataLogin(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    image_spinner.clearAnimation();
                    image_spinner.setVisibility(View.INVISIBLE);
                    btnLogin.setEnabled(true);
                    userIDValue.setEnabled(true);
                    passLoginValue.setEnabled(true);
                    btnforgetPass.setEnabled(true);
                    btnLayout.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params response login:"+response.toString());
                            if(checkCommunity(response)){
                                Toast.makeText(getActivity(), getString(R.string.login_toast_loginsukses), Toast.LENGTH_LONG).show();
                                setLoginProfile(response);
                                changeActivity();
                            }
                        } else {
                            if(code.equals(DefineValue.ERROR_0004) || code.equals(DefineValue.ERROR_0042)){
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

                            if(code.equals(DefineValue.ERROR_0018)){
                                showDialog(getString(R.string.login_failed_attempt_3));
                            }

                            Timber.d("isi error login", response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
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
        fca.switchContent(i,name,isBackstack);
    }

    private void changeActivity() {
        Intent i = new Intent(getActivity(),MainPage.class);
        startActivity(i);
        Registration.fa.finish();
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
            mEditor.putString(DefineValue.USERID_PHONE, response.getString(WebParams.USER_ID));
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
            mEditor.putString(DefineValue.PROFILE_VERIFIED,response.getString(WebParams.VERIFIED));

            mEditor.putString(DefineValue.IS_FIRST_TIME,response.getString(WebParams.USER_IS_NEW));

            mEditor.putString(DefineValue.IMG_URL, response.getString(WebParams.IMG_URL));
            mEditor.putString(DefineValue.IMG_SMALL_URL, response.getString(WebParams.IMG_SMALL_URL));
            mEditor.putString(DefineValue.IMG_MEDIUM_URL, response.getString(WebParams.IMG_MEDIUM_URL));
            mEditor.putString(DefineValue.IMG_LARGE_URL, response.getString(WebParams.IMG_LARGE_URL));

            mEditor.putString(DefineValue.ACCESS_KEY, response.getString(WebParams.ACCESS_KEY));
            mEditor.putString(DefineValue.ACCESS_SECRET, response.getString(WebParams.ACCESS_SECRET));

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
                        Log.w("isi comm id yg bener", arrayJson.getJSONObject(i).getString(WebParams.COMM_ID));
                        break;
                    }
                }
            }

            arraynya = response.getString(WebParams.SETTINGS);
            if(!arraynya.isEmpty()){
                JSONArray arrayJson = new JSONArray(arraynya);
                mEditor.putString(DefineValue.MAX_MEMBER_TRANS, arrayJson.getJSONObject(0).getString(WebParams.MAX_MEMBER_TRANSFER));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        mEditor.apply();
    }

    public void insertCommunityToDB(JSONArray arrayJson){
        ActiveAndroid.initialize(getActivity());
        ActiveAndroid.beginTransaction();
        communityModel mCM;
        try {
            communityModel.deleteAll();

            for (int i = 0; i < arrayJson.length(); i++) {
                //if(arrayJson.getJSONObject(i).getString("buss_scheme_code").equals("EMO")){
                    mCM = new communityModel();
                    mCM.setComm_code(arrayJson.getJSONObject(i).getString(WebParams.COMM_CODE));
                    mCM.setComm_id(arrayJson.getJSONObject(i).getString(WebParams.COMM_ID));
                    mCM.setComm_name(arrayJson.getJSONObject(i).getString(WebParams.COMM_NAME));
                    mCM.setBuss_scheme_code(arrayJson.getJSONObject(i).getString(WebParams.BUSS_SCHEME_CODE));
                    mCM.save();
                //}

            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            ActiveAndroid.endTransaction();
        }
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