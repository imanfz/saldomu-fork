package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.InsertPIN;
import com.sgo.saldomu.activities.LoginActivity;
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
import com.sgo.saldomu.models.retrofit.ContactDataModel;
import com.sgo.saldomu.models.retrofit.ForgorPasswordModel;
import com.sgo.saldomu.models.retrofit.GetHelpModel;
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
    private Spinner spin_tipe_notif;
    private String userIDfinale;
    private ProgressDialog progdialog;
    private String is_sms, is_email;
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
        spin_tipe_notif = v.findViewById(R.id.forgotpass_spin_notif);
        Button btn_submit = v.findViewById(R.id.btn_submit_forgot_pass);
        TextView textMsg = v.findViewById(R.id.textForgotPassmsg);
        String msg = getString(R.string.forgotpass_text_instruction, getString(R.string.appname));
        textMsg.setText(msg);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if (sp.contains(DefineValue.SENDER_ID)) {
            userIDfinale = NoHPFormat.formatTo62(sp.getString(DefineValue.SENDER_ID, ""));
            et_user_id.setText(userIDfinale);
        }

        if (BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("development")) { //untuk shorcut dari tombol di activity LoginActivity
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

            if (i==0)
            {
                is_sms = DefineValue.STRING_YES;
                is_email = DefineValue.STRING_NO;
            }

//            if (i == 1) {
//                is_email = DefineValue.STRING_YES;
//                is_sms = DefineValue.STRING_NO;
//            } else if (i == 2) {
//                is_sms = DefineValue.STRING_YES;
//                is_email = DefineValue.STRING_NO;
//            } else if (i == 3) {
//                is_sms = DefineValue.STRING_YES;
//                is_email = DefineValue.STRING_YES;
//            } else {
//                is_sms = "";
//                is_email = "";
//            }


        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

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
            params.put(WebParams.IS_EMAIL, is_email);
            params.put(WebParams.IS_SMS, is_sms);

            Timber.d(params.toString());

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            ForgorPasswordModel model = getGson().fromJson(object, ForgorPasswordModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                    Timber.d("response forgot password" + response.toString());
                                showDialog(getString(R.string.forgotpass_text_message_success));
                                sp.edit().remove(DefineValue.USER_PASSWORD).apply();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            }else {
//                                    Timber.d("error forgot password" + response.toString());
                                String codemessage = model.getError_message();
                                switch (code) {
                                    case "0097":
                                        attempt = model.getFailed_attempt();
                                        failed = model.getMax_failed();

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
                                        if (MyApiClient.PROD_FAILURE_FLAG) {
                                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(getActivity(), codemessage, Toast.LENGTH_LONG).show();
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
            Timber.d("httpclient" + e.getMessage());
        }
    }

    private void getHelpPin(final ProgressBar progDialog, final TextView Message) {
        try {
            progDialog.setIndeterminate(true);
            progDialog.setVisibility(View.VISIBLE);

            params = RetrofitService.getInstance().getSignatureSecretKey(MyApiClient.LINK_HELP_PIN, "");

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_HELP_PIN, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Message.setVisibility(View.VISIBLE);

                            GetHelpModel model = getGson().fromJson(object, GetHelpModel.class);

                            try {
                                String message_value;

                                if (ForgotPassword.this.isVisible()) {
                                    for (int i = 0; i < model.getContact_data().size(); i++) {
                                        ContactDataModel mObject = model.getContact_data().get(i);
                                        if (i == 0) {
                                            message_value = Message.getText().toString() + "\n" +
                                                    mObject.getDescription() + " " +
                                                    mObject.getName() + "\n" +
                                                    mObject.getContact_phone() + " " +
                                                    getString(R.string.or) + " " +
                                                    mObject.getContact_email();
                                            Message.setText(message_value);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progDialog.setIndeterminate(false);
                            progDialog.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient" + e.getMessage());
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
        ProgressBar progBar = dialog.findViewById(R.id.progressBarDialogNotif);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);
        Message.setVisibility(View.VISIBLE);

        Title.setText(getResources().getString(R.string.mainpage_dialog_changepass_title));
        Message.setText(message_error);
        getHelpPin(progBar, Message);

        btnDialogOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        dialog.show();
    }


    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        LoginActivity fca = (LoginActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }


    private boolean inputValidation() {
        if (et_user_id.getText().toString().length() == 0) {
            DefinedDialog.showErrorDialog(getActivity(), getString(R.string.forgetpass_edittext_validation), null);
            return false;
        }
//        if (spin_tipe_notif.getSelectedItemPosition() == 0) {
//            TextView errorText = (TextView) spin_tipe_notif.getSelectedView();
//            errorText.setTextColor(getResources().getColor(R.color.red));
//            errorText.setError(errorText.getText().toString());
//            errorText.setBackgroundColor(getResources().getColor(R.color.grey_900));
//            return false;
//        }
        return true;
    }
}