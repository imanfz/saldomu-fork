package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.securities.Md5;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;


public class BBSConfirmAcct extends Fragment {
    public final static String TAG = "com.sgo.hpku.fragments.BBSConfirmAcct";
    private final static String TYPE_ACCT = "ACCT";
    private String userID,accessKey;
    private EditText etPassword;
    private ActionListener actionListener;
    private ProgressDialog progdialog;
    private boolean isUpdate = false;

    public BBSConfirmAcct() {
        // Required empty public constructor
    }

    public interface ActionListener{
        void onSuccess();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Bundle bundle = getArguments();
        isUpdate = bundle.getBoolean(DefineValue.IS_UPDATE,false);

        progdialog = DefinedDialog.CreateProgressDialog(getContext(),"");
        progdialog.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        View v = inflater.inflate(R.layout.fragment_bbsconfirm_acct, container, false);

        TextView tvComm = (TextView) v.findViewById(R.id.tv_comm_value);
        tvComm.setText(bundle.getString(DefineValue.COMMUNITY_NAME,""));

        TextView tvBenefBank = (TextView) v.findViewById(R.id.tv_benef_bank_value);
        tvBenefBank.setText(bundle.getString(DefineValue.BANK_NAME));

        TextView tvBenefAcctNo = (TextView) v.findViewById(R.id.tv_benef_acct_no_value);
        tvBenefAcctNo.setText(bundle.getString(DefineValue.ACCT_NO));

        TextView tvBenefAcctName = (TextView) v.findViewById(R.id.tv_benef_acct_name_value);
        tvBenefAcctName.setText(bundle.getString(DefineValue.ACCT_NAME));

        if(bundle.getString(DefineValue.ACCT_TYPE,"").equalsIgnoreCase(TYPE_ACCT)) {
            View layoutCity = v.findViewById(R.id.bbsregistacct_city_layout);
            layoutCity.setVisibility(View.VISIBLE);
            TextView tvBenefAcctCity = (TextView) v.findViewById(R.id.tv_benef_acct_city_value);
            tvBenefAcctCity.setText(bundle.getString(DefineValue.ACCT_CITY_NAME));
        }

        etPassword = (EditText) v.findViewById(R.id.et_password_value);
        Button btnSave = (Button) v.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputValidation()){
                    sentConfirmAcct(bundle.getString(DefineValue.COMMUNITY_CODE),
                            bundle.getString(DefineValue.MEMBER_CODE),
                            bundle.getString(DefineValue.TX_ID),
                            etPassword.getText().toString());
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof ActionListener) {
            actionListener = (ActionListener) getTargetFragment();
        } else {
            if(context instanceof ActionListener){
                actionListener = (ActionListener) context;
            }
            else {
                throw new RuntimeException(context.toString()
                        + " must implement ActionListener BBS confirm acct");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    public boolean inputValidation(){
        if(etPassword.getText().toString().length()==0){
            etPassword.requestFocus();
            etPassword.setError(getString(R.string.login_validation_pass));
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        MyApiClient.CancelRequestWSByTag(TAG,true);
        super.onDestroy();
    }

    private void sentConfirmAcct(final String commCode, final String memberCode, final String txId, final String tokenId){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_CONFIRM_ACCT,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.TOKEN_ID, Md5.hashMd5(tokenId));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params confirmAcct:" + params.toString());

            progdialog.show();
            MyApiClient.sentBBSConfirmAcct(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response confirmAcct: "+response.toString());

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            String name = getArguments().getString(DefineValue.ACCT_NAME);
                            String no = getArguments().getString(DefineValue.ACCT_NO);
                            String title = "", msg = "";
                            if (isUpdate) {
                                title = getString(R.string.bbsconfirmacct_dialog_success_title_update);
                                msg = getString(R.string.bbsconfirmacct_dialog_success_msg_update,name,no);
                            }
                            else {
                                title = getString(R.string.bbsconfirmacct_dialog_success_title);
                                msg = getString(R.string.bbsconfirmacct_dialog_success_msg,name,no);
                            }

                            Dialog dialog = DefinedDialog.MessageDialog(getContext(),
                                    title, msg,
                                    new DefinedDialog.DialogButtonListener() {
                                        @Override
                                        public void onClickButton(View v, boolean isLongClick) {
                                            actionListener.onSuccess();
                                        }
                                    });
                            dialog.show();
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                        progdialog.dismiss();

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
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi confirmAcct:"+throwable.toString());
                  progdialog.dismiss();
                }
            });
        }catch (Exception e){
            Timber.d("httpclient: "+e.getMessage());
        }
    }
}
