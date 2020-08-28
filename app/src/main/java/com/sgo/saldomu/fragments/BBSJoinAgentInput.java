package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.BBSJoinAgentCommModel;
import com.sgo.saldomu.models.retrofit.BBSJoinAgentModel;
import com.sgo.saldomu.models.retrofit.CommunityModel;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 1/30/2017.
 */

public class BBSJoinAgentInput extends Fragment {

    public final static String TAG = "com.sgo.saldomu.fragments.BBSJoinAgentInput";

    private View v;
    private ArrayList<BBSCommModel> listDataComm;
    private ArrayList<CommunityModel> listComm;
    private ArrayAdapter<String> adapterDataComm;
    private ProgressDialog progdialog;
    private ProgressBar progBarComm;
    private EditText etAgentCode;
    private Spinner spComm;
    private String userID, accessKey;
    private ActionListener actionListener;


    public interface ActionListener {
        void onFinishProcess();

        void onCommunityEmpty();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof ActionListener) {
            actionListener = (ActionListener) getTargetFragment();
        } else {
            if (context instanceof ActionListener) {
                actionListener = (ActionListener) context;
            } else {
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
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        listDataComm = new ArrayList<>();
        listComm = new ArrayList<>();
        ArrayList<String> spinDataComm = new ArrayList<>();
        adapterDataComm = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinDataComm);
        adapterDataComm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        progdialog = DefinedDialog.CreateProgressDialog(getContext(), "");
        progdialog.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_bbs_join_agent_input, container, false);
        spComm = v.findViewById(R.id.bbsjoinagent_value_community);
        progBarComm = v.findViewById(R.id.loading_progres_comm);
        etAgentCode = v.findViewById(R.id.bbsjoinagent_value_agent_code);
        CheckBox cbAgentCode = v.findViewById(R.id.agent_code_generate);
        cbAgentCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etAgentCode.setEnabled(!isChecked);
                if (isChecked)
                    etAgentCode.setText("");
            }
        });
        Button btnSubmit = v.findViewById(R.id.btn_submit);
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
            if (inputValidation()) {
                CallSentJoinAgent();
            }
        }
    };

    private void CallSentJoinAgent() {
        int test = spComm.getSelectedItemPosition();
        String commCode, commName;
        if (test == -1) {
            commCode = listComm.get(0).getComm_code();
            commName = listComm.get(0).getComm_name();
        } else {
            commCode = listComm.get(test).getComm_code();
            commName = listComm.get(test).getComm_name();
        }
        sentJoinAgent(commName, commCode,
                etAgentCode.getText().toString(), userID);
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveComm();
    }

    @Override
    public void onDestroy() {
        RetrofitService.dispose();
        super.onDestroy();
    }

    public boolean inputValidation() {
        Timber.d("isi listDataComm = " + String.valueOf(listDataComm.size()));
        if (etAgentCode.isEnabled()) {
            if (etAgentCode.getText().toString().length() == 0) {
                etAgentCode.requestFocus();
                etAgentCode.setError(getString(R.string.bbsjoinagen_et_hint_agencode));
                return false;
            }
        }
        return listComm.size() != 0;
    }

    private void CommunityUIRefresh() {
        if (listComm.size() < 1) {
            Toast.makeText(getActivity(), R.string.joinagentbbs_toast_empty_comm, Toast.LENGTH_LONG).show();
            actionListener.onCommunityEmpty();
        }

        if (listComm.size() == 1) {
            TextView tvCommName = v.findViewById(R.id.tv_comm_name_value);
            tvCommName.setText(listComm.get(0).getComm_name());
            tvCommName.setVisibility(View.VISIBLE);
//            progBarComm.setVisibility(View.GONE);
            spComm.setVisibility(View.INVISIBLE);
        } else {
            spComm.setVisibility(View.VISIBLE);
//            progBarComm.setVisibility(View.GONE);
        }
    }

    private void retrieveComm() {
        try {
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_GLOBAL_COMM);
            params.put(WebParams.SCHEME_CODE, DefineValue.BBS);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params retreiveComm:" + params.toString());

            progBarComm.setVisibility(View.VISIBLE);

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BBS_GLOBAL_COMM, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            BBSJoinAgentCommModel model = gson.fromJson(object, BBSJoinAgentCommModel.class);

                            String code = model.getError_code();
                            adapterDataComm.clear();
                            listComm.clear();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                JSONArray comm = response.optJSONArray(WebParams.COMMUNITY);
                                if (model.getCommunity().size() > 0) {

                                    listComm.addAll(model.getCommunity());

//                                    BBSCommModel bbsComm;
                                    for (int i = 0; i < model.getCommunity().size(); i++) {
//                                        bbsComm = new BBSCommModel(comm.getJSONObject(i).optString(WebParams.COMM_ID),
//                                                comm.getJSONObject(i).optString(WebParams.COMM_CODE),
//                                                comm.getJSONObject(i).optString(WebParams.COMM_NAME),
//                                                comm.getJSONObject(i).optString(WebParams.API_KEY),
//                                                comm.getJSONObject(i).optString(WebParams.MEMBER_CODE),
//                                                comm.getJSONObject(i).optString(WebParams.CALLBACK_URL));
//                                        listDataComm.add(bbsComm);

                                        adapterDataComm.add(model.getCommunity().get(i).getComm_name());
                                    }
                                }

                                adapterDataComm.notifyDataSetChanged();
                                CommunityUIRefresh();
                            } else {
                                code = model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
                                actionListener.onCommunityEmpty();
                            }

                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {

                            progBarComm.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient: " + e.getMessage());
        }
    }

    private void sentJoinAgent(final String commName, final String commCode, final String memberCode, String userID) {
        try {
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_JOIN_AGENT);
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.CUST_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params sent Joint Agent:" + params.toString());

            progdialog.show();

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BBS_JOIN_AGENT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            Gson gson = new Gson();
                            BBSJoinAgentModel model = gson.fromJson(object, BBSJoinAgentModel.class);

                            String code = model.getError_code();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                String commCodeMsg = getString(R.string.community) + " : " + commName;
                                String memberCodeMsg = getString(R.string.agent_name) + " : " + model.getMember_code();
                                String msg = getString(R.string.bbsjoinagent_dialog_msg_success, commCodeMsg, memberCodeMsg);
                                Dialog dialog = DefinedDialog.MessageDialog(getContext(),
                                        getString(R.string.bbsjoinagent_dialog_title_success), msg, new DefinedDialog.DialogButtonListener() {
                                            @Override
                                            public void onClickButton(View v, boolean isLongClick) {
                                                actionListener.onFinishProcess();
                                            }
                                        });
                                dialog.show();
                            } else {
                                code = model.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_SHORT).show();
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
            Timber.d("httpclient: " + e.getMessage());
        }
    }
}
