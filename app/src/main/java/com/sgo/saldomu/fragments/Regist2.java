package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.faber.circlestepview.CircleStepView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.LoginActivity;
import com.sgo.saldomu.activities.Registration;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;

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
    String namaValid, noHPValid, emailValid;
    ProgressDialog progdialog;
    Boolean isFacebook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFacebook = false;
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
            isFacebook = args.getBoolean(DefineValue.IS_FACEBOOK,false);
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

                            namaValid = response.getString(WebParams.CUST_NAME);
                            emailValid = response.getString(WebParams.CUST_EMAIL);
                            noHPValid = response.getString(WebParams.CUST_PHONE);
                            changeActivity(token);
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
}
