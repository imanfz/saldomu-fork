package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 6/11/2015.
 */
public class FragRegisterSMSBankingConfirm extends Fragment {

    private View v;
    private View layout_dll;
    private SecurePreferences sp;
    private ProgressDialog progdialog;

    private TextView tvNo;
    private TextView tvName;
    private TextView tvTglLahir;
    private EditText etToken;
    private Button btnConfirm;
    private Boolean isJatim = false;

    private String acc_no;
    private String acc_name;
    private String tgl_lahir;
    private String no_hp;
    private String ccy_id;
    private String token_id;
    private String cust_id;
    private String userID;
    private String accessKey;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        cust_id = sp.getString(DefineValue.CUST_ID,"");

        tvNo = v.findViewById(R.id.rsb_value_no);
        tvName = v.findViewById(R.id.rsb_value_name);
        tvTglLahir  = v.findViewById(R.id.rsb_value_tgl);
        etToken = v.findViewById(R.id.rsb_value_token);
        layout_dll = v.findViewById(R.id.layout_dll);
        btnConfirm = v.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(btnConfTokenListener);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            String bank_name = bundle.getString(DefineValue.BANK_NAME,"");
            Timber.d("isi bundle:"+bundle.toString());
            if(bank_name.toLowerCase().contains("jatim")) {
                isJatim = true;
                no_hp = bundle.getString(DefineValue.USERID_PHONE,"");
                token_id = bundle.getString(DefineValue.TOKEN,"");
                layout_dll.setVisibility(View.GONE);
                tvNo.setText(no_hp);
            }
            else {
                acc_no = bundle.getString(WebParams.ACCT_NO);
                acc_name = bundle.getString(WebParams.ACCT_NAME);
                tgl_lahir = bundle.getString(WebParams.TGL_LAHIR);
                no_hp = bundle.getString(WebParams.NO_HP);
                ccy_id = bundle.getString(WebParams.CCY_ID);

                tvNo.setText(acc_no);
                tvName.setText(acc_name);
                tvTglLahir.setText(tgl_lahir);
                getTokenSB();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_register_sms_banking_confirm, container, false);
        return v;
    }

    private Button.OnClickListener btnConfTokenListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    if (etToken.getText().toString().equals(token_id)) {
                        if (isJatim)
                            confirmTokenSB();
                        else
                            confirmTokenSB();
                    } else Toast.makeText(getActivity(), "Wrong Token!", Toast.LENGTH_SHORT).show();
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private boolean inputValidation() {
        if (etToken.getText().toString().length() == 0 || etToken.getText().toString().equals("")) {
            etToken.requestFocus();
            etToken.setError(getString(R.string.regist2_validation_otp));
            return false;
        }
        return true;
    }

    private void confirmTokenSB() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            HashMap<String, Object> params;
            String url;
            if(!isJatim) {
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_TOKEN_SB);
                url = MyApiClient.LINK_CONFIRM_TOKEN_SB;
            }else {
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_TOKEN_JATIM);
                url = MyApiClient.LINK_CONFIRM_TOKEN_JATIM;
            }

            params.put(WebParams.NO_HP, no_hp);
            params.put(WebParams.CUST_ID, cust_id);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            if(!isJatim){
                params.put(WebParams.ACCT_NO, acc_no);
                params.put(WebParams.ACCT_NAME, acc_name);
                params.put(WebParams.CCY_ID, ccy_id);
            }

            RetrofitService.getInstance().PostJsonObjRequest(url, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            progdialog.dismiss();
                            Timber.d("isi response confirm token SB:"+response.toString());

                            String code;
                            try {
                                code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                                    getActivity().finish();
                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    Timber.d("isi response autologout:"+response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(),message);
                                }
                                else {
                                    Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void getTokenSB() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQUEST_TOKEN_SB);
            params.put(WebParams.NO_HP, no_hp);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQUEST_TOKEN_SB, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    token_id = response.getString(WebParams.TOKEN_ID);
                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    Timber.d("isi response autologout:"+response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(),message);
                                }else {
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleMyBroadcastReceiver(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleMyBroadcastReceiver(false);
    }

    private void toggleMyBroadcastReceiver(Boolean _on){
        if (getActivity() == null)
            return;

        RegisterSMSBankingActivity fca = (RegisterSMSBankingActivity) getActivity();
        fca.togglerBroadcastReceiver(_on,myReceiver);
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle mBundle = intent.getExtras();
            SmsMessage[] mSMS;
            String strMessage = "";
            String _kode_otp = "";
            String _member_code = "";
            String[] kode = context.getResources().getStringArray(R.array.broadcast_kode_compare);
            String[] code = context.getResources().getStringArray(R.array.broadcast_kode_compare_en);

            if(mBundle != null){
                Object[] pdus = (Object[]) mBundle.get("pdus");
                assert pdus != null;
                mSMS = new SmsMessage[pdus.length];

                for (int i = 0; i < mSMS.length ; i++){
                    mSMS[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    strMessage += mSMS[i].getMessageBody();
                    strMessage += "\n";
                }

                String[] words = strMessage.split(" ");
                for (int i = 0 ; i <words.length;i++)
                {
                    if(_kode_otp.equalsIgnoreCase("")){
                        if(words[i].equalsIgnoreCase(kode[0])||words[i].equalsIgnoreCase(code[0])){
                            if(words[i+1].equalsIgnoreCase(kode[1])||words[i-1].equalsIgnoreCase(kode[1])){
                                if(words[i+2].equalsIgnoreCase(kode[4]))
                                    _kode_otp = words[i+3];
                                else
                                    _kode_otp = words[i+2];
                                _kode_otp =  _kode_otp.replace(".","").replace(" ","");
                            }
                        }
                    }

                    if(_member_code.equals("")){
                        if(words[i].equalsIgnoreCase(kode[3])){
                            if(words[i+1].equalsIgnoreCase(kode[4]))
                                _member_code = words[i+2];
                            else
                                _member_code = words[i+1];
                        }

                    }
                }

                insertTokenEdit(_kode_otp,_member_code);
                //Toast.makeText(context,strMessage,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void insertTokenEdit(String _kode_otp, String _member_kode){
        Timber.d("isi _kode_otp, _member_kode, member kode session:"+_kode_otp+ " / " +_member_kode +" / "+ sp.getString(DefineValue.MEMBER_CODE,""));
        if(no_hp.equals(_member_kode)){
            etToken.setText(_kode_otp);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

}
