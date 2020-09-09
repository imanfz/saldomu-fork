package com.sgo.saldomu.fragments;


import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.OTPVerificationActivity;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.OTPModel;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;

import timber.log.Timber;

//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class OTPVerification extends BaseFragment {

    View v;

    private ProgressDialog progdialog;

    String user_id;

    Button btn_send;
    ImageButton btn_warn;
    EditText et_phone_value;
    private TextView tv_version;

    public OTPVerification() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_otp_verification_1, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDeviceName();

        tv_version = v.findViewById(R.id.tv_version);
        et_phone_value = v.findViewById(R.id.userID_value);
        btn_send = v.findViewById(R.id.btn_send);
        btn_warn = v.findViewById(R.id.btn_warn);

        tv_version.setText(getString(R.string.appname) + " " + BuildConfig.VERSION_NAME + " (" +BuildConfig.VERSION_CODE +")");

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputValidation()) {

                    user_id = NoHPFormat.formatTo62(et_phone_value.getText().toString());
                    getOTP();
                }
            }
        });

        btn_warn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFrag = new FragHelp();
                Bundle bundle = new Bundle();
                bundle.putBoolean(DefineValue.NOT_YET_LOGIN,true);
                newFrag.setArguments(bundle);
                switchFragment(newFrag, "Help", true);
            }
        });

    }

    public void  getOTP()
    {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = user_id;

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignatureSecretKey(MyApiClient.LINK_GET_OTP, extraSignature);
            params.put(WebParams.USER_ID, user_id);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.DEVICE_NAME, getDeviceName());

            Timber.d("isi params get OTP:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_OTP, params
                    , new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            OTPModel model = getGson().fromJson(object, OTPModel.class);

                            if (!model.getOn_error()) {

                                String code = model.getError_code();
                                Timber.d("isi response get OTP : "+object.toString());

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("Sukses");
                                    Fragment mFragment = new OTPVerificationConfirm();
                                    Bundle mBun = new Bundle();
                                    mBun.putString(DefineValue.USER_ID, user_id);
                                    mBun.putString(DefineValue.DEVICE_NAME, getDeviceName());
                                    mBun.putString(DefineValue.REFFERENCE_ID, model.getRef_id());
                                    mFragment.setArguments(mBun);
                                    switchFragment(mFragment, "OtpConfirmation", true);
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), model.getError_message());
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + object.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                }
                                else {
                                    Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), model.getError_message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progdialog.dismiss();
                        }
                    } );
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }


    public boolean inputValidation()
    {
        if (et_phone_value.length()==0 || et_phone_value.equals(""))
        {
            et_phone_value.requestFocus();
            et_phone_value.setError(getActivity().getString(R.string.login_validation_userID));
            return false;
        }
        return true;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        OTPVerificationActivity fca = (OTPVerificationActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }
}
