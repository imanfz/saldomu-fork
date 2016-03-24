package com.sgo.orimakardaya.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import timber.log.Timber;

/**
 * Created by thinkpad on 10/23/2015.
 */
public class RejectNotifDialog extends DialogFragment implements Dialog.OnClickListener {

    public static final String TAG = "Reject Dialog";

    SecurePreferences sp;
    EditText etRemark;
    TextView btnOk, btnCancel;

    String _userId, _accessKey, req_id, trx_id, from, amount, ccy_id;

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
        _userId = sp.getString(DefineValue.USERID_PHONE,"");
        _accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Bundle bundle = getArguments();
        if(bundle != null) {
            req_id = bundle.getString(DefineValue.REQUEST_ID);
            trx_id = bundle.getString(DefineValue.TRX_ID);
            from = bundle.getString(DefineValue.FROM);
            amount = bundle.getString(DefineValue.AMOUNT);
            ccy_id = bundle.getString(DefineValue.CCY_ID);
        }

        etRemark = (EditText) view.findViewById(R.id.text_remark);
        btnOk = (TextView) view.findViewById(R.id.btnOK);
        btnCancel = (TextView) view.findViewById(R.id.btnCancel);

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


    private void sentAsk4MoneyReject(){
        try{

            UUID uuid = MyApiClient.getUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();
            String webservice = MyApiClient.getWebserviceName(MyApiClient.LINK_ASK4MONEY_REJECT);
            Timber.d("Webservice:"+webservice);
            String signature = MyApiClient.getSignature(uuid, dtime, webservice, MyApiClient.COMM_ID + _userId, _accessKey);

            RequestParams params = new RequestParams();
            params.put(WebParams.USER_ID, _userId);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.REQUEST_ID, req_id);
            params.put(WebParams.TRX_ID, trx_id);
            params.put(WebParams.FROM, from);
            params.put(WebParams.REMARK, etRemark.getText().toString());
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.CCY_ID, ccy_id);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.RC_UUID, uuid.toString());
            params.put(WebParams.RC_DTIME, dtime);
            params.put(WebParams.SIGNATURE, signature);

            Timber.d("isi params ask for money reject:" + params.toString());

            MyApiClient.sentAsk4MoneyReject(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        dismiss();
                        //btnOk.setEnabled(true);
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.w("isi response ask for money reject:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if (mListener != null) {
                                mListener.onItemSelected(true);
                            }
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else {
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        if (!getActivity().isFinishing())
                            Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
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

                    if (!getActivity().isFinishing()) {
                        if (MyApiClient.PROD_FAILURE_FLAG)
                            Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    }

                    dismiss();
                    Timber.w("Error ask for money reject:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

}
