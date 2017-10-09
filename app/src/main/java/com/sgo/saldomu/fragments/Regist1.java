package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Dialog;
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
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;

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
public class Regist1 extends Fragment implements EasyPermissions.PermissionCallbacks{

    String namaValid = "" ,emailValid = "",noHPValid = "",token_id = "",member_code = "",max_resend_token = "3", auth_type = "";
    EditText namaValue,emailValue,noHPValue;
    Button btnLanjut;
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

    public void sentData(final String noHP){
        try{
                progdialog.show();

            btnLanjut.setEnabled(false);
            noHPValue.setEnabled(false);
            namaValue.setEnabled(false);
            emailValue.setEnabled(false);

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHP);
            params.put(WebParams.CUST_NAME,namaValue.getText());
            params.put(WebParams.CUST_EMAIL, emailValue.getText());
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

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
                        if(code.equals(WebParams.SUCCESS_CODE)){

                            namaValid = response.getString(WebParams.CUST_NAME);
                            emailValid = response.getString(WebParams.CUST_EMAIL);
                            noHPValid = response.getString(WebParams.CUST_PHONE);

                            showDialog(code);
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

    public void changeActivity(Boolean login){
        if(login){
            DefineValue.NOBACK = false; //fragment selanjutnya tidak bisa menekan tombol BACK
            Intent i = new Intent(getActivity(),LoginActivity.class);
            switchActivity(i);
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
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getResources().getString(R.string.regist1_notif_title));
        if(code.equals("0002")){
            Title.setText(getResources().getString(R.string.regist1_notif_title_registered));
            Message.setText(getResources().getString(R.string.regist1_notif_message_registered));
        }
        else if(code.equals(WebParams.SUCCESS_CODE)){
            Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
            Message.setText(getString(R.string.appname)+" "+getString(R.string.regist1_notif_message_email));
        }

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(code.equals("0002")) changeActivity(true);
                else if(code.equals(WebParams.SUCCESS_CODE)) changeActivity(false);

                dialog.dismiss();
            }
        });

        dialog.show();
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