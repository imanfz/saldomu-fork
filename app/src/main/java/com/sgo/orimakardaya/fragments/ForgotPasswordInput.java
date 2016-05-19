package com.sgo.orimakardaya.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.sgo.orimakardaya.activities.InsertPIN;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.securities.Md5;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

/*
  Created by Administrator on 7/6/2015.
 */
public class ForgotPasswordInput extends Fragment {

    View v;
    EditText et_userid, et_pass_new, et_pass_retype;
    CheckBox cb_show_pass;
    Button btn_submit,btn_batal;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_forgot_password_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		
        et_pass_new = (EditText) v.findViewById(R.id.new_pass_value);
        et_pass_retype = (EditText) v.findViewById(R.id.retype_new_pass_value);
        et_userid = (EditText) v.findViewById(R.id.userID_value);
        cb_show_pass = (CheckBox) v.findViewById(R.id.cb_showPass);
        btn_submit = (Button) v.findViewById(R.id.btn_verification);
        btn_batal = (Button) v.findViewById(R.id.btn_cancel);

        cb_show_pass.setOnCheckedChangeListener(showPassword);

        btn_submit.setOnClickListener(submitListener);
        btn_batal.setOnClickListener(cancelListener);

        et_userid.requestFocus();
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

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {

                        CallPINinput();

                }
            } else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message),null);
        }
    };


    Button.OnClickListener cancelListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager().popBackStack();
        }
    };

    private void CallPINinput(){
        Intent i = new Intent(getActivity(), InsertPIN.class);
        i.putExtra(DefineValue.IS_FORGOT_PASSWORD,true);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainPage.REQUEST_FINISH){
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
//                try {
                    sentData(et_pass_new.getText().toString(),value_pin);
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    public void sentData(String newPassword, String value_pin){
        try{
            final ProgressDialog progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = new RequestParams();
            params.put(WebParams.USER_ID,et_userid.getText().toString());
            params.put(WebParams.NEW_PASSWORD,newPassword);
            params.put(WebParams.PIN, value_pin);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params insert password:" + params.toString());

            MyApiClient.sentInsertPassword(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();
                    Timber.d("isi params response insert password:" + response.toString());
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            showDialog();
                        }
                        else if(code.equals(ErrorDefinition.ERROR_CODE_WRONG_PIN)){
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            CallPINinput();
                        }
                        else {
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

                    Timber.w("Error Koneksi insrt password forgot pass input:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
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
        if(et_userid.getText().toString().length()==0){
            et_userid.requestFocus();
            et_userid.setError(this.getString(R.string.forgetpass_edittext_validation));
            return false;
        }
        else if(et_pass_new.getText().toString().length()==0){
            et_pass_new.requestFocus();
            et_pass_new.setError(this.getString(R.string.changepass_edit_error_newpass));
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

        return true;
    }
}