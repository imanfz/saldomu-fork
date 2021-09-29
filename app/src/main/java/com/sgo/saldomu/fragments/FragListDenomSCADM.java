package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.DenomSCADMActivity;
import com.sgo.saldomu.adapter.ListDenomSCADMAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/16/2018.
 */

public class FragListDenomSCADM extends BaseFragment implements ListDenomSCADMAdapter.listener{
    View v;
    SecurePreferences sp;
    private ProgressDialog progdialog;
    private RecyclerView recyclerView;
    private ListDenomSCADMAdapter listDenomSCADMAdapter;
    private ArrayList<SCADMCommunityModel> scadmCommunityModelArrayList = new ArrayList<>();
    protected String userPhoneID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_topup_scadm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        recyclerView = v.findViewById(R.id.recyclerView);

        userPhoneID = sp.getString(DefineValue.USERID_PHONE,"");

        scadmCommunityModelArrayList.clear();

        initializeAdapter();

        getListCommunity();

    }

    private void initializeAdapter() {
        listDenomSCADMAdapter = new ListDenomSCADMAdapter(scadmCommunityModelArrayList, this);
        recyclerView.setAdapter(listDenomSCADMAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    public void getListCommunity() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_COMMUNITY_DENOM_SCADM);
            params.put(WebParams.COMM_ID_REMARK, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.USER_NAME, userNameLogin);

            Timber.d("isi params get list community denom scadm:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_COMMUNITY_DENOM_SCADM, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("isi response get list community denom scadm:" + response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    JSONArray mArrayCommunity = new JSONArray(response.getString(WebParams.COMMUNITY));

                                    for (int i = 0; i < mArrayCommunity.length(); i++) {
                                        String comm_id = mArrayCommunity.getJSONObject(i).getString(WebParams.COMM_ID);
                                        String comm_code = mArrayCommunity.getJSONObject(i).getString(WebParams.COMM_CODE);
                                        String comm_name = mArrayCommunity.getJSONObject(i).getString(WebParams.COMM_NAME);
                                        String member_code = mArrayCommunity.getJSONObject(i).getString(WebParams.MEMBER_CODE);
                                        String member_id = mArrayCommunity.getJSONObject(i).getString(WebParams.MEMBER_ID);

                                        SCADMCommunityModel scadmCommunityModel = new SCADMCommunityModel();
                                        scadmCommunityModel.setComm_id(comm_id);
                                        scadmCommunityModel.setComm_code(comm_code);
                                        scadmCommunityModel.setComm_name(comm_name);
                                        scadmCommunityModel.setMember_code(member_code);
                                        scadmCommunityModel.setMember_id_scadm(member_id);

                                        scadmCommunityModelArrayList.add(scadmCommunityModel);
                                    }

                                    listDenomSCADMAdapter.updateData(scadmCommunityModelArrayList);

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity());
                                }else {
                                    Timber.d("Error isi response get list community denom scadm:" + response.toString());
                                    code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);

                                    showDialog(response.getString(WebParams.ERROR_MESSAGE));
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
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    @Override
    public void onClick(SCADMCommunityModel item) {
        Bundle bundle=new Bundle();
        bundle.putString(DefineValue.MEMBER_ID_SCADM, item.getMember_id_scadm());
        bundle.putString(DefineValue.COMMUNITY_CODE, item.getComm_code());
        bundle.putString(DefineValue.COMMUNITY_ID, item.getComm_id());
        bundle.putString(DefineValue.MEMBER_CODE, item.getMember_code());
        DataManager.getInstance().setSCADMCommMod(item);
        Fragment frag = new InputPartnerCode();
        frag.setArguments(bundle);
        SwitchFragment(frag, DenomSCADMActivity.DENOM_PAYMENT, true);

    }

    private void showDialog(String msg) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialog= dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.error));
        Message.setText(msg);

        btnDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        dialog.show();
    }
}
