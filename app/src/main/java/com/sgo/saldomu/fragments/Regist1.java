package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CreatePIN;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.PasswordRegisterActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/*
 Created by Administrator on 7/4/2014.
 */
public class Regist1 extends BaseFragment implements EasyPermissions.PermissionCallbacks{

    String namaValid = "" ,emailValid = "",noHPValid = "",token_id = "",member_code = "",max_resend_token = "3", authType, memberID;
    EditText namaValue,emailValue,noHPValue,referalValue;
    Button btnLanjut;
    String flag_change_pwd, flag_change_pin, pass, confPass;
    CheckBox cb_terms;
    View v;
    final int RC_READ_SMS = 10;

    Fragment mFragment;
    ProgressDialog progdialog;

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
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        namaValue=(EditText)getActivity().findViewById(R.id.name_value);
        emailValue=(EditText)getActivity().findViewById(R.id.email_value);
        noHPValue=(EditText)getActivity().findViewById(R.id.noHP_value);
        cb_terms = (CheckBox) v.findViewById(R.id.cb_termsncondition);
        referalValue = v.findViewById(R.id.referal_value);

        btnLanjut = (Button)getActivity().findViewById(R.id.btn_reg1_verification);
        btnLanjut.setOnClickListener(btnNextClickListener);

        cb_terms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    btnLanjut.setEnabled(true);
                else
                    btnLanjut.setEnabled(false);
            }
        });

        if(EasyPermissions.hasPermissions(getContext(),Manifest.permission.READ_PHONE_STATE)) {
            if (isSimExists()) {

                TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                String Nomor1 = tm.getLine1Number();

                noHPValue.setText(Nomor1);
            }
        }
        else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_check_phone_number),
                    RC_READ_SMS, Manifest.permission.READ_PHONE_STATE);
        }


        noHPValue.requestFocus();
        ToggleKeyboard.show_keyboard(getActivity());

        TextView tv_termsnconditions = (TextView) v.findViewById(R.id.tv_termsncondition);
        tv_termsnconditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermsNConditionWeb mfrag = new TermsNConditionWeb();
                switchFragment(mfrag,getString(R.string.termsncondition_title),true);
            }
        });

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if(sp.contains(DefineValue.SENDER_ID)) {
            noHPValid = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
            noHPValue.setText(noHPValid);
        }

        if(BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")){
            Bundle m = getArguments();
            if(m != null && m.containsKey(DefineValue.USER_IS_NEW)) {
                v.findViewById(R.id.noHP_value).setVisibility(View.VISIBLE);
                noHPValue.setEnabled(true);
            }
        }

        Bundle bundle = getArguments();
        if (bundle!=null)
        {
            Boolean is_unregister_member = bundle.getBoolean(DefineValue.IS_UNREGISTER_MEMBER, false);
            String userId = bundle.getString(DefineValue.USER_ID);
            if (is_unregister_member)
            {
                noHPValue.setText(userId);
                noHPValue.setEnabled(false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    Button.OnClickListener btnNextClickListener= new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            if(view == btnLanjut){
                if(InetHandler.isNetworkAvailable(getActivity())){
                    if(inputValidation()){
                        sentData(NoHPFormat.formatTo62(noHPValue.getText().toString()));
                    }
                }else DefinedDialog.showErrorDialog(getActivity(),getString(R.string.inethandler_dialog_message));
            }
        }
    };

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("isi regist 1 requestCode:"+String.valueOf(requestCode));
        if (requestCode == LoginActivity.ACTIVITY_RESULT) {
            Timber.d("isi regist 1 resultcode:"+String.valueOf(resultCode));
            if (resultCode == LoginActivity.RESULT_PIN) {
                Timber.d("isi regist 1 authtype:"+authType);

                pass = data.getStringExtra(DefineValue.NEW_PASSWORD);
                confPass = data.getStringExtra(DefineValue.CONFIRM_PASSWORD);

//                Intent i = new Intent(getActivity(), CreatePIN.class);
//                i.putExtra(DefineValue.REGISTRATION, true);
//                switchActivityPIN(i);

                sendCreatePass();
            }
            else if(resultCode == LoginActivity.RESULT_FINISHING){
//                if(authType.equals(DefineValue.AUTH_TYPE_OTP)){
//                    pass = data.getStringExtra(DefineValue.NEW_PASSWORD);
//                    confPass = data.getStringExtra(DefineValue.CONFIRM_PASSWORD);
//                }
                sendCreatePin(data);
            }
        }
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private void switchActivity(Intent i){
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchActivity(i);
    }

    private void SaveIMEIICCID(){
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.SaveImeiICCIDDevice();
    }

    public void sentData(final String noHP){
        try{
                progdialog.show();

            btnLanjut.setEnabled(false);
            noHPValue.setEnabled(false);
            namaValue.setEnabled(false);
            emailValue.setEnabled(false);

            extraSignature = noHP;
            RequestParams params = MyApiClient.getSignatureWithParamsWithoutLogin(MyApiClient.COMM_ID, MyApiClient.LINK_REG_STEP1,
                    BuildConfig.SECRET_KEY, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHP);
            params.put(WebParams.CUST_NAME,namaValue.getText());
            params.put(WebParams.CUST_EMAIL, emailValue.getText());
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.FLAG_NEW_FLOW, DefineValue.Y);
            if (referalValue.getText().toString()!=null)
            {
                params.put(WebParams.REFERAL_NO, referalValue.getText());
            }else params.put(WebParams.REFERAL_NO, "");

            Timber.d("isi params reg1:" + params.toString());

            MyApiClient.sentRegStep1(getActivity(),params,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode,Header[] headers, JSONObject response) {
                    btnLanjut.setEnabled(true);
                    noHPValue.setEnabled(true);
                    namaValue.setEnabled(true);
                    emailValue.setEnabled(true);
                    Timber.d("response register:"+response.toString());
                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if(code.equals(WebParams.SUCCESS_CODE)) {

                            if (response.has(WebParams.FLAG_PROCESS))
                            {
                                String flag_process = response.getString(WebParams.FLAG_PROCESS);
                                if(flag_process.equals("N"))
                                {
                                    namaValid = response.getString(WebParams.CUST_NAME);
                                    emailValid = response.getString(WebParams.CUST_EMAIL);
                                    noHPValid = response.getString(WebParams.CUST_PHONE);
                                    Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                                    i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                                    switchActivityPIN(i);
                                }else{
                                    flag_change_pwd = response.optString(WebParams.FLAG_CHANGE_PWD, "Y");
                                    flag_change_pin = response.optString(WebParams.FLAG_CHANGE_PIN, "Y");
                                    check(code);
                                }
                            } else {
                                namaValid = response.getString(WebParams.CUST_NAME);
                                emailValid = response.getString(WebParams.CUST_EMAIL);
                                noHPValid = response.getString(WebParams.CUST_PHONE);

                                Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                                i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                                switchActivityPIN(i);
                            }
                        }
                        else if(code.equals("0002")){
                            showDialog(code);
                        }
                        else {
                            Timber.d("Error Reg1:"+response.toString());
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
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    btnLanjut.setEnabled(true);
                    noHPValue.setEnabled(true);
                    namaValue.setEnabled(true);
                    emailValue.setEnabled(true);
                    Timber.w("Error Koneksi reg1 proses reg1:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sendCreatePass(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            extraSignature = noHPValid + pass;
            RequestParams params = MyApiClient.getSignatureWithParamsWithoutLogin(MyApiClient.COMM_ID, MyApiClient.LINK_CREATE_PASS,
                    BuildConfig.SECRET_KEY, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PASS, RSA.opensslEncrypt(pass));
            params.put(WebParams.CONF_PASS, RSA.opensslEncrypt(confPass));
            params.put(WebParams.CUST_ID, noHPValid);
            params.put(WebParams.FLAG_NEW_FLOW, DefineValue.Y);

            Timber.d("params create pass:"+params.toString());

            MyApiClient.sentCreatePass(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        Timber.d("response create pass:" + response.toString());
                        progdialog.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            memberID = response.getString(WebParams.MEMBER_ID);
                            if (response.has(WebParams.FLAG_CHANGE_PWD))
                            {
                                flag_change_pwd="N";
                                check(code);
                            }
                            else
                            {
                                showDialog(code);
                            }
                        } else {
                            Timber.d("isi error create pass:" + response.toString());
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                            i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                            switchActivityPIN(i);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if (progdialog.isShowing())
                        progdialog.dismiss();
                    Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
                    i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
                    switchActivityPIN(i);
                    Timber.w("Error Koneksi create pass reg2:" + throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sendCreatePin(Intent data){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            extraSignature = memberID + noHPValid + data.getStringExtra(DefineValue.PIN_VALUE);
            RequestParams params = MyApiClient.getSignatureWithParamsWithoutLogin(MyApiClient.COMM_ID, MyApiClient.LINK_CREATE_PIN,
                    BuildConfig.SECRET_KEY, extraSignature);
            params.put(WebParams.USER_ID, noHPValid);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, RSA.opensslEncrypt(data.getStringExtra(DefineValue.PIN_VALUE)));
            params.put(WebParams.CONFIRM_PIN, RSA.opensslEncrypt(data.getStringExtra(DefineValue.CONF_PIN)));

            Timber.d("params create pin:"+params.toString());

            MyApiClient.sentCreatePin(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);
                        Timber.d("response create pin:"+response.toString());

                        progdialog.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if (response.has(WebParams.FLAG_CHANGE_PIN))
                            {
                                flag_change_pin="N";
                                check(code);
                            }
                            else
                                showDialog(code);
                        } else {
                            Timber.d("isi error create pin:" + response.toString());
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getActivity(), CreatePIN.class);
                            i.putExtra(DefineValue.REGISTRATION, true);
                            switchActivity(i);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    if (progdialog.isShowing())
                        progdialog.dismiss();
                    Intent i = new Intent(getActivity(), CreatePIN.class);
                    i.putExtra(DefineValue.REGISTRATION, true);
                    switchActivity(i);
                    Timber.w("Error Koneksi create pin reg2:" + throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void changeActivity(Boolean login){
        if(login){
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            SaveIMEIICCID();
            Fragment test = new Login();
            switchFragment(test,"Login",false);
        }
        else{
            DefineValue.NOBACK = true; //fragment selanjutnya tidak bisa menekan tombol BACK
            mFragment = new Regist2();
            Bundle mBun = new Bundle();
            mBun.putString(DefineValue.CUST_NAME,namaValid);
            mBun.putString(DefineValue.CUST_PHONE,noHPValid);
            mBun.putString(DefineValue.CUST_EMAIL,emailValid);
//            mBun.putString(DefineValue.TOKEN,token_id);
//            mBun.putString(DefineValue.MAX_RESEND,max_resend_token);
//            mBun.putString(DefineValue.AUTHENTICATION_TYPE,auth_type);
            mFragment.setArguments(mBun);
            switchFragment(mFragment, "reg2", true);
        }
    }
    private void check(String code){
        if (flag_change_pwd.equals("Y"))
        {
            Intent i = new Intent(getActivity(), PasswordRegisterActivity.class);
            i.putExtra(DefineValue.AUTHENTICATION_TYPE, authType);
            switchActivityPIN(i);
        }
        else if(flag_change_pin.equals("Y"))
        {
            Intent i = new Intent(getActivity(), CreatePIN.class);
            i.putExtra(DefineValue.REGISTRATION, true);
            switchActivityPIN(i);
        }
        else showDialog(code);
    }

    void showDialog(final String code) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);
        TextView Message2 = (TextView)dialog.findViewById(R.id.message_dialog2);Message2.setVisibility(View.VISIBLE);
        TextView Message3 = (TextView)dialog.findViewById(R.id.message_dialog3);Message3.setVisibility(View.VISIBLE);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getResources().getString(R.string.regist1_notif_title));
        if(code.equals("0002")){
            Title.setText(getResources().getString(R.string.regist1_notif_title_registered));
            Message.setText(getResources().getString(R.string.regist1_notif_message_registered));
        }
        else if(code.equals(WebParams.SUCCESS_CODE)){
            Title.setText(getResources().getString(R.string.regist2_notif_title));
            Message.setText(getResources().getString(R.string.regist2_notif_message_1));
            Message2.setText(noHPValue.getText().toString());
            Message2.setTextSize(getResources().getDimension(R.dimen.abc_text_size_small_material));
            Message3.setText(getResources().getString(R.string.regist2_notif_message_3));
        }

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(code.equals(WebParams.SUCCESS_CODE) || code.equals("0002"))
                    changeActivity(true);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void switchActivityPIN(Intent i){
        /*if (getActivity() == null)
            return;

        Registration fca = (Registration) getActivity();
        fca.switchActivity(i, Registration.ACTIVITY_RESULT);*/
        startActivityForResult(i, LoginActivity.ACTIVITY_RESULT);
    }

    //----------------------------------------------------------------------------------------------------------------

    public boolean inputValidation(){
        if(noHPValue.getText().toString().length()==0){
            noHPValue.requestFocus();
            noHPValue.setError(getResources().getString(R.string.regist1_validation_nohp));
            return false;
        }
        else if(namaValue.getText().toString().length()<2){
            namaValue.requestFocus();
            namaValue.setError(getResources().getString(R.string.regist1_validation_nama));
            return false;
        }
        else if(emailValue.getText().toString().length()==0){
            emailValue.requestFocus();
            emailValue.setError(getResources().getString(R.string.regist1_validation_email_length));
            return false;
        }
        else if(emailValue.getText().toString().length()>0 && !isValidEmail(emailValue.getText()) ){
            emailValue.requestFocus();
            emailValue.setError(getString(R.string.regist1_validation_email));
            return false;
        }
        else if (referalValue.getText().toString()!=null)
        {
            if (referalValue.length()<9||referalValue.length()>13)
            {
                referalValue.requestFocus();
                referalValue.setError("Masukkan No. HP Referal yang sesuai!");
                return false;
            }else if (referalValue.getText().toString().equalsIgnoreCase(DefineValue.CUST_ID))
            {
                referalValue.requestFocus();
                referalValue.setError("Nomor Referal tidak boleh sama dengan No. HP Pelanggan!");
                return false;
            }
        }
        return true;
    }

    public boolean isSimExists()
    {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        int SIM_STATE = telephonyManager.getSimState();

        if(SIM_STATE == TelephonyManager.SIM_STATE_READY)
            return true;
        else
        {
            switch(SIM_STATE)
            {
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
        if (requestCode == RC_READ_SMS){
            if (isSimExists()) {
                TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                String Nomor1 = tm.getLine1Number();

                noHPValue.setText(Nomor1);
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}