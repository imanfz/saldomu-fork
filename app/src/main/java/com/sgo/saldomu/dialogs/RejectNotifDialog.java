package com.sgo.saldomu.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 10/23/2015.
 */
public class RejectNotifDialog extends DialogFragment implements Dialog.OnClickListener {

    public static final String TAG = "Reject Dialog";

    private SecurePreferences sp;
    private EditText etRemark;
    private TextView btnOk;
    private TextView btnCancel;

    private String _userId;
    private String req_id;
    private String trx_id;
    private String from;
    private String amount;
    private String ccy_id;

    private OnItemSelectedListener mListener;

    public interface OnItemSelectedListener {
        void onItemSelected(final boolean success);
    }

    public void setOnItemSelectedListener(final OnItemSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        sentAsk4MoneyReject();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reject_notif, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _userId = sp.getString(DefineValue.USERID_PHONE, "");

        Bundle bundle = getArguments();
        if (bundle != null) {
            req_id = bundle.getString(DefineValue.REQUEST_ID);
            trx_id = bundle.getString(DefineValue.TRX_ID);
            from = bundle.getString(DefineValue.FROM);
            amount = bundle.getString(DefineValue.AMOUNT);
            ccy_id = bundle.getString(DefineValue.CCY_ID);
        }

        etRemark = view.findViewById(R.id.text_remark);
        btnOk = view.findViewById(R.id.btnOK);
        btnCancel = view.findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOk.setEnabled(false);
                sentAsk4MoneyReject();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }


    private void sentAsk4MoneyReject() {
        try {
//
//            UUID uuid = MyApiClient.getUUID();
//            String dtime = DateTimeFormat.getCurrentDateTime();
//            String webservice = MyApiClient.getWebserviceName(MyApiClient.LINK_ASK4MONEY_REJECT);
//            Timber.d("Webservice:"+webservice);
            String extraSignature = req_id + trx_id + from;
//            String signature = MyApiClient.getSignature(webservice, MyApiClient.COMM_ID
//                    , _userId, _accessKey, extraSignature);

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_ASK4MONEY_REJECT,
                    extraSignature);
            params.put(WebParams.USER_ID, _userId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.REQUEST_ID, req_id);
            params.put(WebParams.TRX_ID, trx_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.REMARK, etRemark.getText().toString());
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.CCY_ID, ccy_id);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
//            params.put(WebParams.RC_UUID, uuid.toString());
//            params.put(WebParams.RC_DTIME, dtime);
//            params.put(WebParams.SIGNATURE, signature);

            Timber.d("isi params ask for money reject:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_ASK4MONEY_REJECT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (mListener != null) {
                                    mListener.onItemSelected(true);
                                }
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity());
                            }else {
                                code = model.getError_code() + " : " + model.getError_message();

                                if (!getActivity().isFinishing()) {
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            dismiss();
                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

}
