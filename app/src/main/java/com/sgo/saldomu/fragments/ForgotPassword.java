package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.*;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/*
  Created by Administrator on 1/21/2015.
 */
public class ForgotPassword extends BaseFragment {

    private View v;
    private EditText et_user_id;
    private Spinner spin_tipe_notif;
    private String userIDfinale;
    private ProgressDialog progdialog;
    private String is_sms, is_email;
    private int attempt,failed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_forgot_password, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        et_user_id = (EditText) v.findViewById(R.id.forgotpass_userid_value);
        spin_tipe_notif = (Spinner) v.findViewById(R.id.forgotpass_spin_notif);
        Button btn_submit = (Button) v.findViewById(R.id.btn_submit_forgot_pass);
        TextView textMsg = (TextView) v.findViewById(R.id.textForgotPassmsg);
        String msg = getString(R.string.forgotpass_text_instruction,getString(R.string.appname));
        textMsg.setText(msg);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if(sp.contains(DefineValue.SENDER_ID)) {
            userIDfinale = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
            et_user_id.setText(userIDfinale);
        }

        if(BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")){ //untuk shorcut dari tombol di activity LoginActivity
            et_user_id.setEnabled(true);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item_white,
                getResources().getStringArray(R.array.list_tipe_notif));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_tipe_notif.setAdapter(adapter);
        spin_tipe_notif.setOnItemSelectedListener(spinnerTipeNotif);

        btn_submit.setOnClickListener(submitForgotPassListener);
        et_user_id.requestFocus();
        ToggleKeyboard.show_keyboard(getActivity());

//        et_user_id.setText(sp.getString(DefineValue.SENDER_ID,""));
    }

    private Spinner.OnItemSelectedListener spinnerTipeNotif = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {

            if(i == 1) {
                is_email = DefineValue.STRING_YES;
                is_sms = DefineValue.STRING_NO;
            }
            else if(i == 2) {
                is_sms = DefineValue.STRING_YES;
                is_email = DefineValue.STRING_NO;
            }
            else if(i == 3){
                is_sms = DefineValue.STRING_YES;
                is_email = DefineValue.STRING_YES;
            }
            else {
                is_sms = "";
                is_email = "";
            }



        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Button.OnClickListener submitForgotPassListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(inputValidation()){
                    userIDfinale = NoHPFormat.formatTo62(et_user_id.getText().toString());
                    CallPINinput(0);
                }
            }else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainPage.REQUEST_FINISH){
            if(resultCode == InsertPIN.RESULT_PIN_VALUE){
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
//                Toast.makeText(getActivity(),value_pin,Toast.LENGTH_SHORT).show();
                sentData(value_pin);
            }
        }
    }

    private void CallPINinput(int _attempt){
        Intent i = new Intent(getActivity(), InsertPIN.class);
        i.putExtra(DefineValue.IS_FORGOT_PASSWORD, true);
        i.putExtra(DefineValue.USERID_PHONE,userIDfinale);
        if(_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT,_attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private void sentData(final String value_pin){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = userIDfinale + value_pin;
            RequestParams params = MyApiClient.getSignatureWithParamsWithoutLogin(MyApiClient.COMM_ID, MyApiClient.LINK_FORGOT_PASSWORD,
                    BuildConfig.SECRET_KEY, extraSignature );
            params.put(WebParams.USER_ID, userIDfinale);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, RSA.opensslEncrypt(value_pin));
            params.put(WebParams.IS_EMAIL, is_email);
            params.put(WebParams.IS_SMS, is_sms);

            Timber.d(params.toString());

            MyApiClient.sentForgotPassword(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("response forgot password" + response.toString());
                            showDialog(getString(R.string.forgotpass_text_message_success));

                        } else {
                            Timber.d("error forgot password" + response.toString());
                            String codemessage = response.getString(WebParams.ERROR_MESSAGE);
                            switch (code) {
                                case "0097":
                                    attempt = response.optInt(WebParams.FAILED_ATTEMPT, -1);
                                    failed = response.optInt(WebParams.MAX_FAILED, 0);

                                    if (attempt == -1)
                                        CallPINinput(0);
                                    else
                                        CallPINinput(failed - attempt);

                                    Toast.makeText(getActivity(), codemessage, Toast.LENGTH_LONG).show();
                                    break;
                                case "0133":
                                    showDialog(codemessage);
                                    break;
                                default:
                                    Toast.makeText(getActivity(), codemessage, Toast.LENGTH_LONG).show();
                                    break;
                            }


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

                    Timber.w("Error forgot pass" + throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient"+e.getMessage());
        }
    }

    private void getHelpPin(final ProgressBar progDialog, final TextView Message){
        try{
            progDialog.setIndeterminate(true);
            progDialog.setVisibility(View.VISIBLE);

            MyApiClient.getHelpPIN(getActivity(), new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    String message_value;
                    try {
                        JSONArray arrayContact = new JSONArray(response.optString(WebParams.CONTACT_DATA));
                        JSONObject mObject;
                        if (ForgotPassword.this.isVisible()) {
                            for (int i = 0; i < arrayContact.length(); i++) {
                                mObject = arrayContact.getJSONObject(i);
//                                id = mObject.optString(WebParams.ID, "0");
                                if (i==0) {
                                    message_value = Message.getText().toString()+"\n"+
                                            mObject.optString(WebParams.DESCRIPTION, "") + " " +
                                            mObject.optString(WebParams.NAME, "") + "\n" +
                                            mObject.optString(WebParams.CONTACT_PHONE, "") + " " +
                                            getString(R.string.or) + " " +
                                            mObject.optString(WebParams.CONTACT_EMAIL, "");
                                    Message.setText(message_value);
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progDialog.setIndeterminate(false);
                    progDialog.setVisibility(View.GONE);
                    Message.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure();
                }

                private void failure(){
                    progDialog.setIndeterminate(false);
                    progDialog.setVisibility(View.GONE);
                    Message.setVisibility(View.VISIBLE);
                }
            });

        }catch (Exception e){
            Timber.d("httpclient"+e.getMessage());
        }
    }

    private void showDialog(String message_error){
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOK = (Button)dialog.findViewById(R.id.btn_dialog_notification_ok);
        ProgressBar progBar = (ProgressBar) dialog.findViewById(R.id.progressBarDialogNotif);
        TextView Title = (TextView)dialog.findViewById(R.id.title_dialog);
        TextView Message = (TextView)dialog.findViewById(R.id.message_dialog);Message.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.mainpage_dialog_changepass_title));
        Message.setText(message_error);
        getHelpPin(progBar,Message);

        btnDialogOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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


    private boolean inputValidation(){
        if(et_user_id.getText().toString().length()==0){
            DefinedDialog.showErrorDialog(getActivity(),getString(R.string.forgetpass_edittext_validation),null);
            return false;
        }
        if(spin_tipe_notif.getSelectedItemPosition()==0){
            TextView errorText = (TextView)spin_tipe_notif.getSelectedView();
            errorText.setTextColor(getResources().getColor(R.color.red));
            errorText.setError(errorText.getText().toString());
            errorText.setBackgroundColor(getResources().getColor(R.color.grey_900));
            return false;
        }
        return true;
    }
}