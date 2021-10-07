package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/16/2018.
 */

public class FragJoinCommunityConfirm extends BaseFragment {
    View v;
    SecurePreferences sp;
    TextView community_name, tvmember_code, tvmember_name, community_code;
    Button btn_next;
    String comm_name, member_code, member_name, comm_id_scadm, comm_code;
    protected String userPhoneID;
    private ProgressDialog progdialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_join_community_confirm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");

        Bundle bundle = getArguments();
        comm_name = bundle.getString(DefineValue.COMMUNITY_NAME);
        comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
        comm_id_scadm = bundle.getString(DefineValue.COMM_ID_SCADM);
        member_code = bundle.getString(DefineValue.MEMBER_CODE);
        member_name = bundle.getString(DefineValue.MEMBER_NAME);

        community_name = v.findViewById(R.id.community_name);
        community_code = v.findViewById(R.id.community_code);
        tvmember_code = v.findViewById(R.id.member_code);
        tvmember_name = v.findViewById(R.id.member_name);
        btn_next = v.findViewById(R.id.btn_next);

        community_name.setText(comm_name);
        community_code.setText(comm_code);
        tvmember_code.setText(member_code);
        tvmember_name.setText(member_name);

        btn_next.setOnClickListener(view -> confirmJoinCommunity());
    }

    public void confirmJoinCommunity() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            extraSignature = comm_id_scadm + member_code;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_COMMUNITY_SCADM, extraSignature);

            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID_SCADM, comm_id_scadm);
            params.put(WebParams.MEMBER_CODE, member_code);

            Timber.d("isi params confirm join community scadm:%s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_COMMUNITY_SCADM, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response confirm join community scadm:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    successDialog();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                                } else {
                                    Timber.d("Error isi response confirm join community scadm:%s", response.toString());

                                    Toast.makeText(getActivity(), code + ":" + message, Toast.LENGTH_LONG).show();
                                    getActivity().finish();
                                }

                            } catch (JSONException e) {
                                Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();

                        }

                        @Override
                        public void onComplete() {
                            if (progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });

        } catch (Exception e) {
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void successDialog() {
        Dialog dialognya = DefinedDialog.MessageDialog(getActivity(), "Sukses!", "Selamat, anda berhasil bergabung dalam komunitas!",
                () -> getActivity().finish()
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
