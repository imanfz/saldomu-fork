package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.ChangePIN;
import com.sgo.saldomu.activities.ChangePassword;
import com.sgo.saldomu.activities.CreatePIN;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.MyProfileActivity;
import com.sgo.saldomu.activities.PasswordRegisterActivity;
import com.sgo.saldomu.activities.Registration;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.securities.Md5;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by thinkpad on 10/21/2016.
 */

public class Regist2 extends Fragment {
    View v;
    EditText etToken;
    TextView currEmail;
    Button btnProses, btnCancel;
    String namaValid, noHPValid, emailValid, authType, token, pass, confPass, memberID;
    String flag_change_pwd, flag_change_pin;
    ProgressDialog progdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_regist2, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        getActivity().getWindow().setBackgroundDrawableResource(R.drawable.background);

        Bundle args = getArguments();
        if(args != null) {
            noHPValid = args.getString(DefineValue.CUST_PHONE, "");
            namaValid = args.getString(DefineValue.CUST_NAME, "");
            emailValid = args.getString(DefineValue.CUST_EMAIL, "-");
        }

        etToken = (EditText) v.findViewById(R.id.token_value);
        currEmail = (TextView) v.findViewById(R.id.text_email);
        btnProses = (Button) v.findViewById(R.id.btn_token);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel);

        currEmail.setText(getString(R.string.validasi_email_text) + " " + emailValid);

        btnProses.setOnClickListener(btnProsesClickListener);
        btnCancel.setOnClickListener(btnCancelClickListener);
    }

    Button.OnClickListener btnProsesClickListener= new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            if(InetHandler.isNetworkAvailable(getActivity())){
                if(inputValidation()){
                    sentData(etToken.getText().toString());
                }
            }else DefinedDialog.showErrorDialog(getActivity(),getString(R.string.inethandler_dialog_message));
        }
    };

    Button.OnClickListener btnCancelClickListener= new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            DefineValue.NOBACK = false;
            getFragmentManager().popBackStack();
        }
    };

    public boolean inputValidation(){
        if(etToken.getText().toString().length()==0){
            etToken.requestFocus();
            etToken.setError(getResources().getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    public void sentData(final String token){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            btnProses.setEnabled(false);

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.CUST_PHONE, noHPValid);
            params.put(WebParams.CUST_NAME,namaValid);
            params.put(WebParams.CUST_EMAIL, emailValid);
            params.put(WebParams.EMAIL_TOKEN, token);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.IS_SMS, "Y");
            params.put(WebParams.IS_EMAIL, "N");

            Timber.d("isi params reg2:" + params.toString());

            MyApiClient.sentRegStep2(getActivity(),params,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    btnProses.setEnabled(true);
                    Timber.d("response reg 2:"+response.toString());
                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if(code.equals(WebParams.SUCCESS_CODE)){
                            String flag_process = response.getString(WebParams.FLAG_PROCESS);
                            if(flag_process.equals("N"))
                            {
                                namaValid = response.getString(WebParams.CUST_NAME);
                                emailValid = response.getString(WebParams.CUST_EMAIL);
                                noHPValid = response.getString(WebParams.CUST_PHONE);
                                changeActivity(token);
                            }else{
                                    flag_change_pwd = response.optString(WebParams.FLAG_CHANGE_PWD, "Y");
                                    flag_change_pin = response.optString(WebParams.FLAG_CHANGE_PIN, "Y");
                                    check();
                            }
                        }
                        else {
                            Timber.d("Error Reg2:"+response.toString());
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
                    btnProses.setEnabled(true);
                    Timber.w("Error Koneksi reg2 proses reg2:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    public void sendCreatePass(){
              try{
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            RequestParams params = new RequestParams();
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PASS, pass);
            params.put(WebParams.CONF_PASS, confPass);
            params.put(WebParams.CUST_ID, noHPValid);

            Timber.d("params create pass:"+params.toString());

            MyApiClient.sentCreatePass(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        progdialog.dismiss();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            memberID = response.getString(WebParams.MEMBER_ID);
                            flag_change_pwd="N";
                            check();
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

            RequestParams params = new RequestParams();
            params.put(WebParams.USER_ID, noHPValid);
            params.put(WebParams.MEMBER_ID, memberID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, Md5.hashMd5(data.getStringExtra(DefineValue.PIN_VALUE)));
            params.put(WebParams.CONFIRM_PIN, Md5.hashMd5(data.getStringExtra(DefineValue.CONF_PIN)));

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
                            flag_change_pin="N";
                            check();
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

    private void check(){
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
        else showDialog();
    }

    private void switchActivityPIN(Intent i){
        /*if (getActivity() == null)
            return;

        Registration fca = (Registration) getActivity();
        fca.switchActivity(i, Registration.ACTIVITY_RESULT);*/
        startActivityForResult(i, LoginActivity.ACTIVITY_RESULT);
    }

    void showDialog(){
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

        Title.setText(getResources().getString(R.string.regist2_notif_title));
        Message.setText(getResources().getString(R.string.regist2_notif_message_1));
        Message2.setText(noHPValid);
        Message2.setTextSize(getResources().getDimension(R.dimen.abc_text_size_small_material));
        Message3.setText(getResources().getString(R.string.regist2_notif_message_3));

        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(true);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("isi regist 2 requestCode:"+String.valueOf(requestCode));
        if (requestCode == LoginActivity.ACTIVITY_RESULT) {
            Timber.d("isi regist 2 resultcode:"+String.valueOf(resultCode));
            if (resultCode == LoginActivity.RESULT_PIN) {
                Timber.d("isi regist 2 authtype:"+authType);

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

    public void changeActivity(String token){
        DefineValue.NOBACK = true; //fragment selanjutnya tidak bisa menekan tombol BACK
        Fragment mFragment = new Regist3();
        Bundle mBun = getArguments();
        mBun.putString(DefineValue.TOKEN,token);
        mFragment.setArguments(mBun);
        switchFragment(mFragment, "reg3", true);
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

    public void changeFragment(Boolean submit){
        if(submit){
            DefineValue.NOBACK = false; //fragment selanjutnya bisa menekan tombol BACK
            Intent i = new Intent(getActivity(),LoginActivity.class);
            switchActivity(i);
        }
        else{
            getFragmentManager().popBackStack();
        }
    }
}
