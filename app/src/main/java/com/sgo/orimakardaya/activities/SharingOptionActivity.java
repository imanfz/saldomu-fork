package com.sgo.mdevcash.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.mdevcash.R;
import com.sgo.mdevcash.coreclass.*;
import com.sgo.mdevcash.dialogs.AlertDialogLogout;
import com.sgo.mdevcash.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

import timber.log.Timber;

/*
  Created by Administrator on 10/2/2015.
 */
public class SharingOptionActivity extends BaseActivity {

    FacebookFunction mFaceFunction;

    ToggleButton tbFacebook;
    ProgressDialog progdialog;
    SecurePreferences sp;
    TextView tvFaceName;
    String userID,accessKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        mFaceFunction = FacebookFunction.getInstance();

        tvFaceName = (TextView) this.findViewById(R.id.facebook_user_name);
        tbFacebook = (ToggleButton) this.findViewById(R.id.facebook_toggle_btn);


        if(mFaceFunction.getFinalAT() == null) {
            tbFacebook.setChecked(false);
        }
        else {
            tbFacebook.setChecked(true);
        }

        if(mFaceFunction.isLogin()&&mFaceFunction.getFinalProf() != null){
            Log.d("isi profil", "isi get name");
            tvFaceName.setText(mFaceFunction.getFinalProf().getName());
        }
        else
            tvFaceName.setText("");

        tbFacebook.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
//                    LoginManager.getInstance().logInWithPublishPermissions(SharingOptionActivity.this, Arrays.asList("publish_actions"));
                    LoginManager.getInstance().logInWithReadPermissions(SharingOptionActivity.this, Arrays.asList("email", "public_profile", "user_friends"));
                    tbFacebook.setEnabled(false);
                    LoginManager.getInstance().registerCallback(mFaceFunction.getmCallBackManager(), faceCallback);
                }
                else{
                    if(mFaceFunction.isLogin()) {
                        tvFaceName.setText("");
                        sentUpdateDataFace(null, true);
                    }
                }
            }
        });

        mFaceFunction.getmATT().startTracking();
        mFaceFunction.getmPT().startTracking();
        setResult(MainPage.RESULT_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFaceFunction.getmATT().startTracking();
        mFaceFunction.getmPT().startTracking();
    }

    FacebookCallback<LoginResult> faceCallback =  new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            tbFacebook.setEnabled(true);
            AccessToken mAT = loginResult.getAccessToken();
            if(mAT != null) {
                mFaceFunction.setFinalAT(mAT);
                Log.d("granted permission", mAT.getPermissions().toString());
                Log.d("denied permission", mAT.getDeclinedPermissions().toString());
                getUserData();
            }
            else
                mFaceFunction.setFinalAT(null);
            tbFacebook.setEnabled(true);
        }

        @Override
        public void onCancel() {
            tbFacebook.setEnabled(true);
            tbFacebook.setChecked(false);
        }

        @Override
        public void onError(FacebookException error) {
            tbFacebook.setEnabled(true);
            tbFacebook.setChecked(false);
            Toast.makeText(SharingOptionActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            Log.d("FacebookException", error.toString());
        }
    };

    public void getUserData(){
        progdialog = DefinedDialog.CreateProgressDialog(this, "");
        GraphRequest.GraphJSONObjectCallback getDataCallback = new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    Log.d("isi response ", object.toString());
                    tvFaceName.setText(Profile.getCurrentProfile().getName());

                    sentUpdateDataFace(object, false);

                } catch (Exception e) {
                    Toast.makeText(SharingOptionActivity.this, "error is: " + e.toString(), Toast.LENGTH_LONG).show();
                    Log.d("FacebookException", e.toString());
                    progdialog.dismiss();
                }
            }
        };
        mFaceFunction.getUserData(SharingOptionActivity.this,getDataCallback);

    }

    public void sentUpdateDataFace(JSONObject dataFace, final boolean isLogout){
        try{
            this.setResult(MainPage.RESULT_REFRESH_NAVDRAW);
            if(isLogout)
                progdialog = DefinedDialog.CreateProgressDialog(this, "");

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPDATE_SOCMED,
                    userID,accessKey);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.FULL_NAME, Profile.getCurrentProfile().getName());
            params.put(WebParams.VALIDATE_ID,Profile.getCurrentProfile().getId());
            params.put(WebParams.FACEBOOK_CONNECT, DefineValue.YES);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            if(!isLogout && dataFace !=null){
                params.put(WebParams.FACEBOOK_CONNECT, 1);
                params.put(WebParams.EMAIL, dataFace.optString(WebParams.EMAIL,""));
                params.put(WebParams.GENDER, dataFace.optString(WebParams.GENDER,""));

                String localeFace = dataFace.getString(WebParams.LOCALE);
                Locale mLoc = LocaleUtils.fromString(localeFace);
                params.put(WebParams.COUNTRY, mLoc.getDisplayCountry());
                params.put(WebParams.LANGUAGE, mLoc.getDisplayLanguage());

                params.put(WebParams.LOCATION,"");
                params.put(WebParams.PROFILE_URL, "https://graph.facebook.com/" + dataFace.optString(WebParams.ID,"") + "/picture?type=large");

            }
            Timber.d("isi params update facebook:" + params.toString());

            MyApiClient.sentUpdateSocMed(this,params,new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    Timber.d("isi response update facebook: " + response.toString());
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            SecurePreferences prefs = sp;
                            SecurePreferences.Editor mEditor = prefs.edit();


                            String nama = response.optString(WebParams.FULL_NAME,"");
                            String email = response.optString(WebParams.EMAIL,"");
                            String country = response.optString(WebParams.COUNTRY,"");
                            String img_url = response.getString(WebParams.IMG_URL);

                            if(!nama.isEmpty()) {
                                mEditor.putString(DefineValue.USER_NAME, nama);
                                mEditor.putString(DefineValue.CUST_NAME, nama);
                                mEditor.putString(DefineValue.PROFILE_FULL_NAME,nama);
                            }

                            if(!country.isEmpty())
                                mEditor.putString(DefineValue.PROFILE_COUNTRY,country);

                            if(!email.isEmpty())
                                mEditor.putString(DefineValue.PROFILE_EMAIL,email);

                            if(!img_url.isEmpty()) {
                                mEditor.putString(DefineValue.IMG_URL, img_url);
                                mEditor.putString(DefineValue.IMG_SMALL_URL, response.getString(WebParams.IMG_SMALL_URL));
                                mEditor.putString(DefineValue.IMG_MEDIUM_URL, response.getString(WebParams.IMG_MEDIUM_URL));
                                mEditor.putString(DefineValue.IMG_LARGE_URL, response.getString(WebParams.IMG_LARGE_URL));
                            }

                            mEditor.apply();
                            if(isLogout)
                                mFaceFunction.Logout();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Log.d("isi response autologout", response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(SharingOptionActivity.this,message);
                        }
                        else {
                            Toast.makeText(SharingOptionActivity.this,getString(R.string.update_facebook_failed_toast),Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    if(MyApiClient.IS_PROD)
                        Toast.makeText(SharingOptionActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(SharingOptionActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi update facebook:" + throwable.toString());
                }
            });

        }catch (Exception e){
            Log.d("httpclient", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFaceFunction.getmCallBackManager().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_sharing_option;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.shareoption_title));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFaceFunction.getmATT().stopTracking();
        mFaceFunction.getmPT().stopTracking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFaceFunction.getmATT().stopTracking();
        mFaceFunction.getmPT().stopTracking();
    }
}