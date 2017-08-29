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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.sgo.saldomu.entityRealm.BBSCommModel;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/30/2017.
 */

public class BBSJoinAgentInput extends Fragment {

    public final static String TAG = "com.sgo.saldomu.fragments.BBSJoinAgentInput";

    private View v;
    private ArrayList<BBSCommModel> listDataComm;
    private ArrayAdapter<String> adapterDataComm;
    private ProgressDialog progdialog;
    private ProgressBar progBarComm;
    private EditText etAgentCode;
    private Spinner spComm;
    private String userID, accessKey;
    private ActionListener actionListener;


    public interface ActionListener{
        void onFinishProcess();
        void onCommunityEmpty();
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
                        + " must implement ActionListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        listDataComm = new ArrayList<>();
        ArrayList<String> spinDataComm = new ArrayList<>();
        adapterDataComm = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinDataComm);
        adapterDataComm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        progdialog = DefinedDialog.CreateProgressDialog(getContext(),"");
        progdialog.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_bbs_join_agent_input, container, false);
        spComm = (Spinner) v.findViewById(R.id.bbsjoinagent_value_community);
        progBarComm = (ProgressBar) v.findViewById(R.id.loading_progres_comm);
        etAgentCode = (EditText) v.findViewById(R.id.bbsjoinagent_value_agent_code);
        CheckBox cbAgentCode = (CheckBox) v.findViewById(R.id.agent_code_generate);
        cbAgentCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etAgentCode.setEnabled(!isChecked);
                if(isChecked)
                    etAgentCode.setText("");
            }
        });
        Button btnSubmit = (Button) v.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(submitListener);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        spComm.setAdapter(adapterDataComm);
    }

    Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(inputValidation()){
                CallSentJoinAgent();
            }
        }
    };

    private void CallSentJoinAgent(){
        int test = spComm.getSelectedItemPosition();
        String commCode,commName;
        if(test == -1) {
            commCode = listDataComm.get(0).getComm_code();
            commName = listDataComm.get(0).getComm_name();
        }else {
            commCode = listDataComm.get(test).getComm_code();
            commName = listDataComm.get(test).getComm_name();
        }
        sentJoinAgent(commName,commCode,
                etAgentCode.getText().toString(),userID);
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveComm();
    }

    @Override
    public void onDestroy() {
        MyApiClient.CancelRequestWSByTag(TAG,true);
        super.onDestroy();
    }

    public boolean inputValidation(){
        Timber.d("isi listDataComm = "+ String.valueOf(listDataComm.size()));
        if(etAgentCode.isEnabled()){
            if(etAgentCode.getText().toString().length()==0){
                etAgentCode.requestFocus();
                etAgentCode.setError(getString(R.string.bbsjoinagen_et_hint_agencode));
                return false;
            }
        }
        return listDataComm.size() != 0;
    }

    private void CommunityUIRefresh(){
        if(listDataComm.size() < 1) {
            Toast.makeText(getActivity(), R.string.joinagentbbs_toast_empty_comm, Toast.LENGTH_LONG).show();
            actionListener.onCommunityEmpty();
        }

        if(listDataComm.size() == 1) {
            TextView tvCommName = (TextView) v.findViewById(R.id.tv_comm_name_value);
            tvCommName.setText(listDataComm.get(0).getComm_name());
            tvCommName.setVisibility(View.VISIBLE);
            progBarComm.setVisibility(View.GONE);
            spComm.setVisibility(View.INVISIBLE);
        }
        else {
            spComm.setVisibility(View.VISIBLE);
            progBarComm.setVisibility(View.GONE);
        }
    }

    private void retrieveComm(){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_GLOBAL_COMM,
                    userID,accessKey);
            params.put(WebParams.SCHEME_CODE, DefineValue.BBS);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params retreiveComm:" + params.toString());

            progBarComm.setVisibility(View.VISIBLE);
            MyApiClient.sentRetreiveGlobalComm(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response retreiveComm: "+response.toString());
                        listDataComm.clear();
                        adapterDataComm.clear();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            JSONArray comm = response.optJSONArray(WebParams.COMMUNITY);
                            if(comm != null && comm.length() > 0) {
                                BBSCommModel bbsComm;
                                for (int i = 0; i < comm.length(); i++) {
                                    bbsComm = new BBSCommModel(comm.getJSONObject(i).optString(WebParams.COMM_ID),
                                            comm.getJSONObject(i).optString(WebParams.COMM_CODE),
                                            comm.getJSONObject(i).optString(WebParams.COMM_NAME),
                                            comm.getJSONObject(i).optString(WebParams.API_KEY),
                                            comm.getJSONObject(i).optString(WebParams.MEMBER_CODE),
                                            comm.getJSONObject(i).optString(WebParams.CALLBACK_URL));
                                    listDataComm.add(bbsComm);
                                    adapterDataComm.add(bbsComm.getComm_name());
                                }
                            }
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                        adapterDataComm.notifyDataSetChanged();
                        CommunityUIRefresh();

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
                    Timber.w("Error Koneksi retreiveComm:"+throwable.toString());
                    actionListener.onCommunityEmpty();
                }
            });
        }catch (Exception e){
            Timber.d("httpclient: "+e.getMessage());
        }
    }

    private void sentJoinAgent(final String commName, final String commCode, final String memberCode, String userID){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_JOIN_AGENT,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE,commCode);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.CUST_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params sent Joint Agent:" + params.toString());

            progdialog.show();
            MyApiClient.sentBBSJoinAgent(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response Joint Agent: "+response.toString());

                        if (code.equals(WebParams.SUCCESS_CODE)){
                            String commCodeMsg = getString(R.string.community)+" : " + commName;
                            String memberCodeMsg = getString(R.string.agent_name)+" : " + response.getString(WebParams.MEMBER_CODE);
                            String msg = getString(R.string.bbsjoinagent_dialog_msg_success,commCodeMsg,memberCodeMsg);
                            Dialog dialog = DefinedDialog.MessageDialog(getContext(),
                                    getString(R.string.bbsjoinagent_dialog_title_success), msg, new DefinedDialog.DialogButtonListener() {
                                        @Override
                                        public void onClickButton(View v, boolean isLongClick) {
                                            actionListener.onFinishProcess();
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
                    Timber.w("Error Koneksi Joint Agent:"+throwable.toString());
                    progdialog.dismiss();
                }
            });
        }catch (Exception e){
            Timber.d("httpclient: "+e.getMessage());
        }
    }
}
