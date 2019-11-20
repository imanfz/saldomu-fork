package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsMapViewByAgentActivity;
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
 * create an instance of this fragment.
 */
public class FragOnProgressAgent extends Fragment {

    public final static String TAG = "com.sgo.saldomu.fragments.Frag_OnProgress_Agent";
    View v;
    ProgressDialog progdialog;
    private SecurePreferences sp;
    private String userId;

    public FragOnProgressAgent() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.frag_on_progress_agent, container, false);



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

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userId                  = sp.getString(DefineValue.USERID_PHONE, "");
        progdialog              = DefinedDialog.CreateProgressDialog(getActivity(), getString(R.string.searching_onprogress_trx));

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_ONPROGRESS_BY_AGENT);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.USER_ID, userId);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_TRX_ONPROGRESS_BY_AGENT, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);



                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                SecurePreferences.Editor mEditor = prefs.edit();
                                mEditor.putString(DefineValue.BBS_MEMBER_ID, response.getString(WebParams.MEMBER_ID));
                                mEditor.putString(DefineValue.BBS_SHOP_ID, response.getString(WebParams.SHOP_ID));
                                mEditor.putString(DefineValue.BBS_TX_ID, response.getString(WebParams.TX_ID));
                                mEditor.putDouble(DefineValue.AGENT_LATITUDE, response.getDouble(WebParams.SHOP_LATITUDE));
                                mEditor.putDouble(DefineValue.AGENT_LONGITUDE, response.getDouble(WebParams.SHOP_LONGITUDE));
                                mEditor.putString(DefineValue.KEY_CCY, response.getString(DefineValue.KEY_CCY));
                                mEditor.putString(DefineValue.KEY_AMOUNT, response.getString(DefineValue.KEY_AMOUNT));
                                mEditor.putString(DefineValue.KEY_ADDRESS, response.getString(DefineValue.KEY_ADDRESS));
                                mEditor.putString(DefineValue.KEY_CODE, response.getString(DefineValue.KEY_CODE));
                                mEditor.putString(DefineValue.KEY_NAME, response.getString(DefineValue.KEY_NAME));
                                mEditor.putDouble(DefineValue.BENEF_LATITUDE, response.getDouble(DefineValue.KEY_LATITUDE));
                                mEditor.putDouble(DefineValue.BENEF_LONGITUDE, response.getDouble(DefineValue.KEY_LONGITUDE));
                                mEditor.apply();

                                Intent i = new Intent(getContext(), BbsMapViewByAgentActivity.class);
                                startActivity(i);
                                getActivity().finish();


                            } else {

                                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setTitle(getString(R.string.alertbox_title_information));
                                alertDialog.setMessage(getString(R.string.alertbox_message_information));
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                getActivity().finish();

                                            }
                                        });
                                alertDialog.show();
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
                        if( progdialog.isShowing() )
                            progdialog.dismiss();
                    }
                });
    }
}
