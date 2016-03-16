package com.sgo.orimakardaya.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

/*
  Created by Administrator on 7/6/2015.
 */
public class ForgotPasswordInput extends Fragment {

    View v;
    String userId,tokenId;
    EditText et_pass_new, et_pass_retype,et_token;
    CheckBox cb_show_pass;
    int max_resend = 3, minPass, maxPass;
    Button btn_submit,btn_batal, btn_resend;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_forgot_password_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle mBun = getArguments();
        userId  =   mBun.getString(DefineValue.USERID_PHONE);
        tokenId = mBun.getString(DefineValue.TOKEN);
        minPass = mBun.getInt(DefineValue.MIN_PASS);
        maxPass = mBun.getInt(DefineValue.MAX_PASS);

        et_pass_new = (EditText) v.findViewById(R.id.new_pass_value);
        et_pass_retype = (EditText) v.findViewById(R.id.retype_new_pass_value);
        cb_show_pass = (CheckBox) v.findViewById(R.id.cb_showPass);
        et_token = (EditText) v.findViewById(R.id.token_otp_value);
        btn_submit = (Button) v.findViewById(R.id.btn_verification);
        btn_batal = (Button) v.findViewById(R.id.btn_cancel);
        btn_resend = (Button) v.findViewById(R.id.btn_resend_token);
        TextView tv_userid = (TextView) v.findViewById(R.id.noHP_value);

        et_pass_new.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxPass)});
        et_pass_retype.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxPass)});

        tv_userid.setText(userId);

        cb_show_pass.setOnCheckedChangeListener(showPassword);

        btn_submit.setOnClickListener(submitListener);
        btn_batal.setOnClickListener(cancelListener);
        btn_resend.setOnClickListener(resendListener);
        changeTextBtnSub();


        et_pass_new.requestFocus();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    CheckBox.OnCheckedChangeListener showPassword = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(!b){
                et_pass_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            else {
                et_pass_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                et_pass_retype.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        }
    };

    public void changeTextBtnSub() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_resend.setText(getString(R.string.reg2_btn_text_resend_token)+" ("+max_resend+")");
            }
        });
    }

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    try {
                        sentData(Md5.hashMd5(et_pass_new.getText().toString()));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            } else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener resendListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (max_resend != 0) requestResendToken();
                else
                    Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_LONG).show();
            } else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager().popBackStack();
        }
    };

    public void sentData(String newPassword){
        try{
            final ProgressDialog progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = new RequestParams();
            params.put(WebParams.USER_ID,userId);
            params.put(WebParams.NEW_PASSWORD,newPassword);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params insert password", params.toString());

            MyApiClient.sentInsertPassword(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    Timber.d("isi params response insert password", response.toString());
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            showDialog();
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progdialog.dismiss();
                    Timber.w("Error Koneksi forgot password", throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }

    public void requestResendToken(){
        try{
            final ProgressDialog progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = new RequestParams();
            params.put(WebParams.USER_ID,userId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params forgot password", params.toString());

            MyApiClient.sentForgotPassword(params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    Timber.d("isi params response forgot password", response.toString());
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            tokenId = response.getString(WebParams.TOKEN_ID);
                            max_resend--;
                            changeTextBtnSub();
                            Toast.makeText(getActivity(),getString(R.string.reg2_notif_text_resend_token),Toast.LENGTH_SHORT).show();
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }

                        if(max_resend == 0 ){
                            btn_resend.setEnabled(false);
                            Toast.makeText(getActivity(), getString(R.string.reg2_notif_max_resend_token_empty), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progdialog.dismiss();
                    Timber.w("Error Koneksi forgot password", throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient", e.getMessage());
        }
    }


    void showDialog(){
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOK = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.mainpage_dialog_changepass_title));
        Message.setText(getResources().getString(R.string.forgotpass_text_message_success));

        btnDialogOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        dialog.show();
    }


    public boolean inputValidation(){
        if(et_pass_new.getText().toString().length()==0){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpass));
            return false;
        }
        else if(et_pass_new.getText().toString().length()<minPass){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.validation_minimal)+" "+minPass+" "+this.getString(R.string.validation_character));
            return false;
        }
        else if(et_pass_retype.getText().toString().length()==0){
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass));
            return false;
        }
        else if(!et_pass_retype.getText().toString().equals(et_pass_new.getText().toString())){
            et_pass_retype.requestFocus();
            et_pass_retype.setError(this.getString(R.string.changepass_edit_error_retypenewpass_confirm));
            return false;
        }
        if(et_token.getText().toString().length()==0){
            et_token.requestFocus();
            et_token.setError(this.getString(R.string.regist2_validation_otp));
            return false;
        }
        else {
            if(!et_token.getText().toString().equals(tokenId)){
                et_token.requestFocus();
                et_token.setError(this.getString(R.string.regist2_validation_token_invalid));
                return false;
            }
        }

        return true;
    }
}