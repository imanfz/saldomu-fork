package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragCancelTrxRequest#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragCancelTrxRequest extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "FragCancelTrxRequest";

    // TODO: Rename and change types of parameters
    private Button btnProses, btnCancel;
    private EditText etReason;
    private String txId, userId;

    CancelTrxRequestListener cpl;
    ProgressDialog progdialog;

    public interface CancelTrxRequestListener {
        void onSuccessCancelTrx();
    }

    public FragCancelTrxRequest() {
        // Required empty public constructor
        super();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragCancelTrxRequest.
     */
    // TODO: Rename and change types and number of parameters
    public static FragCancelTrxRequest newInstance(String param1, String param2) {
        FragCancelTrxRequest fragment = new FragCancelTrxRequest();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            txId = getArguments().getString(DefineValue.TX_ID);
            userId = getArguments().getString(DefineValue.CUST_ID);
        }

        try {
            cpl = (FragCancelTrxRequest.CancelTrxRequestListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement CancelTrxRequestListener interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_cancel_trx_request, container, false);

        btnProses   = v.findViewById(R.id.btnProses);
        btnCancel   = v.findViewById(R.id.btnCancel);
        etReason    = v.findViewById(R.id.etReason);

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason       = etReason.getText().toString().trim();
                Boolean hasError    = false;

                if ( reason.equals("") ) {
                    hasError = true;
                    etReason.setError(getString(R.string.err_empty_cancel_reason));
                }

                if ( !hasError ) {
                    //call webservice

                    progdialog              = DefinedDialog.CreateProgressDialog(getContext());

                    String extraSignature   = txId;
                    HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_SEARCH_AGENT,
                            extraSignature);

                    params.put(WebParams.APP_ID, BuildConfig.APP_ID);
                    params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
                    params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
                    params.put(WebParams.TX_ID, txId);
                    params.put(WebParams.CUST_ID, userId);
                    params.put(WebParams.TX_REMARKS, reason);
                    params.put(WebParams.USER_ID, userId);

                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CANCEL_SEARCH_AGENT, params,
                            new ObjListeners() {
                                @Override
                                public void onResponses(JSONObject response) {
                                    try {

                                        String code = response.getString(WebParams.ERROR_CODE);
                                        if (code.equals(WebParams.SUCCESS_CODE)) {
                                            cpl.onSuccessCancelTrx();
                                        } else {
                                            Toast.makeText(getContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                                        }

                                        getDialog().dismiss();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {

                                }

                                @Override
                                public void onComplete() {
                                    if ( progdialog.isShowing())
                                        progdialog.dismiss();
                                }
                            });

                }
                //getDialog().dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
