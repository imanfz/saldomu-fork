package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.ToggleKeyboard;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.ForgorPasswordModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;

import timber.log.Timber;

/*
  Created by Administrator on 1/21/2015.
 */
public class ForgotPassword extends BaseFragment {

    private View v;
    private EditText et_user_id;
    private String userIDfinale;
    private ProgressDialog progdialog;
    private int attempt, failed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_forgot_password, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        et_user_id = v.findViewById(R.id.forgotpass_userid_value);
        Button btn_submit = v.findViewById(R.id.btn_submit_forgot_pass);
        TextView textMsg = v.findViewById(R.id.textForgotPassmsg);
        String msg = getString(R.string.forgotpass_text_instruction, getString(R.string.appname));
        textMsg.setText(msg);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if (sp.contains(DefineValue.SENDER_ID)) {
            userIDfinale = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
            et_user_id.setText(userIDfinale);
        } else if (sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, "") != null) {
            userIDfinale = NoHPFormat.formatTo62(sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""));
            et_user_id.setText(userIDfinale);
        }

        if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) { //untuk shorcut dari tombol di activity LoginActivity
            et_user_id.setEnabled(false);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item_white,
                getResources().getStringArray(R.array.list_tipe_notif));

        btn_submit.setOnClickListener(submitForgotPassListener);
        et_user_id.requestFocus();
        ToggleKeyboard.show_keyboard(getActivity());
    }

    private Button.OnClickListener submitForgotPassListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    userIDfinale = NoHPFormat.formatTo62(et_user_id.getText().toString());
                    CallPINinput(0);
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                String value_pin = data.getStringExtra(DefineValue.PIN_VALUE);
//                Toast.makeText(getActivity(),value_pin,Toast.LENGTH_SHORT).show();
                sentData(value_pin);
            }
        }
    }

    private void CallPINinput(int _attempt) {
        Intent i = new Intent(getActivity(), InsertPIN.class);
        i.putExtra(DefineValue.IS_FORGOT_PASSWORD, true);
        i.putExtra(DefineValue.USERID_PHONE, userIDfinale);
        i.putExtra(DefineValue.NOT_YET_LOGIN, true);
        sp.edit().putString(DefineValue.CURR_USERID, userIDfinale).apply();
        if (_attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, _attempt);
        startActivityForResult(i, MainPage.REQUEST_FINISH);
    }

    private void sentData(final String value_pin) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            String link = MyApiClient.LINK_FORGOT_PASSWORD;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = userIDfinale + value_pin;
            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignatureSecretKey(link, extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.USER_ID, userIDfinale);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PIN, RSA.opensslEncrypt(uuid, dateTime, userIDfinale, value_pin, subStringLink));
            params.put(WebParams.IS_EMAIL, DefineValue.STRING_NO);
            params.put(WebParams.IS_SMS, DefineValue.STRING_YES);

            Timber.d(params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            ForgorPasswordModel model = getGson().fromJson(object, ForgorPasswordModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                    Timber.d("response forgot password" + response.toString());
                                showDialog(getString(R.string.forgotpass_text_message_success));
                                sp.edit().remove(DefineValue.USER_PASSWORD).apply();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", object.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
//                                    Timber.d("error forgot password" + response.toString());
                                switch (code) {
                                    case "0097":
                                        attempt = model.getFailed_attempt();
                                        failed = model.getMax_failed();

                                        if (attempt == -1)
                                            CallPINinput(0);
                                        else
                                            CallPINinput(failed - attempt);

                                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                        break;
                                    case "0133":
                                        showDialog(message);
                                        break;
                                    default:
                                        if (MyApiClient.PROD_FAILURE_FLAG) {
                                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                        break;
                                }


                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progdialog.dismiss();
                        }
                    });


        } catch (Exception e) {
            Timber.d("httpclient%s", e.getMessage());
        }
    }

    private void showDialog(String message_error) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOK = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);
        Message.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.mainpage_dialog_changepass_title));
        Message.setText(message_error);
//        getHelpPin(progBar, Message);

        btnDialogOK.setOnClickListener(view -> {
            dialog.dismiss();
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        dialog.show();
    }


    private boolean inputValidation() {
        if (et_user_id.getText().toString().length() == 0) {
            DefinedDialog.showErrorDialog(getActivity(), getString(R.string.forgetpass_edittext_validation), null);
            return false;
        }
        return true;
    }
}