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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.HashMap;

import io.realm.Realm;
import timber.log.Timber;


public class BBSConfirmAcct extends BaseFragment {

    private final static String TYPE_ACCT = "ACCT";
    private EditText etPassword;
    private ActionListener actionListener;
    private ProgressDialog progdialog;
    private boolean isUpdate = false;
    private Realm realm;
    private BBSCommModel bbsCommModel;
    private BBSAccountACTModel bbsAccountACTModel;

    public BBSConfirmAcct() {
        // Required empty public constructor
    }

    public interface ActionListener {
        void onSuccess();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        isUpdate = bundle.getBoolean(DefineValue.IS_UPDATE, false);
        realm = RealmManager.getRealmBBS();

        bbsCommModel = new BBSCommModel();
        bbsCommModel.setComm_id(bundle.getString(DefineValue.COMMUNITY_ID));
        bbsCommModel.setComm_code(bundle.getString(DefineValue.COMMUNITY_CODE));
        bbsCommModel.setComm_name(bundle.getString(DefineValue.COMMUNITY_NAME));
        bbsCommModel.setMember_code(bundle.getString(DefineValue.MEMBER_CODE));

        bbsAccountACTModel = new BBSAccountACTModel();
        bbsAccountACTModel.setProduct_code(bundle.getString(DefineValue.BANK_CODE));
        bbsAccountACTModel.setProduct_name(bundle.getString(DefineValue.BANK_NAME));
        bbsAccountACTModel.setProduct_type(bundle.getString(DefineValue.ACCT_TYPE));
        bbsAccountACTModel.setAccount_no(bundle.getString(DefineValue.ACCT_NO));
        bbsAccountACTModel.setAccount_name(bundle.getString(DefineValue.ACCT_NAME));
        bbsAccountACTModel.setAccount_city(bundle.getString(DefineValue.ACCT_CITY_NAME));
        bbsAccountACTModel.setScheme_code(DefineValue.ATC);
        bbsAccountACTModel.setComm_id(bbsCommModel.getComm_id());

        progdialog = DefinedDialog.CreateProgressDialog(getContext(), "");
        progdialog.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        View v = inflater.inflate(R.layout.fragment_bbsconfirm_acct, container, false);

        TextView tvComm = v.findViewById(R.id.tv_comm_value);
        tvComm.setText(bundle.getString(DefineValue.COMMUNITY_NAME, ""));

        TextView tvBenefBank = v.findViewById(R.id.tv_benef_bank_value);
        tvBenefBank.setText(bundle.getString(DefineValue.BANK_NAME));

        TextView tvBenefAcctNo = v.findViewById(R.id.tv_benef_acct_no_value);
        tvBenefAcctNo.setText(bundle.getString(DefineValue.ACCT_NO));

        TextView tvBenefAcctName = v.findViewById(R.id.tv_benef_acct_name_value);
        tvBenefAcctName.setText(bundle.getString(DefineValue.ACCT_NAME));

        if (bundle.getString(DefineValue.ACCT_TYPE, "").equalsIgnoreCase(TYPE_ACCT)) {
            View layoutCity = v.findViewById(R.id.bbsregistacct_city_layout);
            layoutCity.setVisibility(View.VISIBLE);
            TextView tvBenefAcctCity = v.findViewById(R.id.tv_benef_acct_city_value);
            tvBenefAcctCity.setText(bundle.getString(DefineValue.ACCT_CITY_NAME));
        }

        etPassword = v.findViewById(R.id.et_password_value);
        Button btnSave = v.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValidation()) {
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
            if (context instanceof ActionListener) {
                actionListener = (ActionListener) context;
            } else {
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

    public boolean inputValidation() {
        if (etPassword.getText().toString().length() == 0) {
            etPassword.requestFocus();
            etPassword.setError(getString(R.string.login_validation_pass));
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        RetrofitService.dispose();
        RealmManager.closeRealm(realm);
        super.onDestroy();
    }

    private void sentConfirmAcct(final String commCode, final String memberCode, final String txId, final String tokenId) {
        try {
            String link = MyApiClient.LINK_BBS_CONFIRM_ACCT;
            String subStringLink = link.substring(link.indexOf("saldomu/"));
            String uuid;
            String dateTime;
            extraSignature = txId + tokenId + commCode + memberCode;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(link,
                    extraSignature);
            uuid = params.get(WebParams.RC_UUID).toString();
            dateTime = params.get(WebParams.RC_DTIME).toString();
            params.put(WebParams.COMM_CODE, commCode);
            params.put(WebParams.MEMBER_CODE, memberCode);
            params.put(WebParams.TX_ID, txId);
            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(uuid, dateTime, userPhoneID, tokenId, subStringLink));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params confirmAcct:" + params.toString());

            progdialog.show();

            RetrofitService.getInstance().PostObjectRequest(link, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel model = getGson().fromJson(object, jsonModel.class);

                            String code = model.getError_code();
                            String message = model.getError_message();

                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                String name = getArguments().getString(DefineValue.ACCT_NAME);
                                String no = getArguments().getString(DefineValue.ACCT_NO);
                                String title, msg;
                                if (isUpdate) {
                                    title = getString(R.string.bbsconfirmacct_dialog_success_title_update);
                                    msg = getString(R.string.bbsconfirmacct_dialog_success_msg_update, name, no);
                                    updateDataToRealm();
                                } else {
                                    title = getString(R.string.bbsconfirmacct_dialog_success_title);
                                    msg = getString(R.string.bbsconfirmacct_dialog_success_msg, name, no);
                                    insertDataToRealm();
                                }

                                Dialog dialog = DefinedDialog.MessageDialog(getContext(),
                                        title, msg,
                                        () -> actionListener.onSuccess());
                                dialog.show();
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:%s", model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:%s", object.toString());
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(getActivity());
                            } else {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
            Timber.d("httpclient: %s", e.getMessage());
        }
    }

    void insertDataToRealm() {
        String date = DateTimeFormat.getCurrentDate();
        bbsAccountACTModel.setLast_update(date);
        realm.beginTransaction();
        realm.copyToRealm(bbsAccountACTModel);
        realm.commitTransaction();
    }

    void updateDataToRealm() {
        BBSAccountACTModel tempBBSAccount = realm.where(BBSAccountACTModel.class)
                .equalTo(BBSAccountACTModel.PRODUCT_CODE, bbsAccountACTModel.getProduct_code())
                .equalTo(BBSAccountACTModel.ACCOUNT_NO, getArguments().getString(DefineValue.ACCT_NO_CURRENT))
                .findFirst();
        if (tempBBSAccount != null) {
            String date = DateTimeFormat.getCurrentDate();
            realm.beginTransaction();
            tempBBSAccount.setAccount_city(bbsAccountACTModel.getAccount_city());
            tempBBSAccount.setAccount_name(bbsAccountACTModel.getAccount_name());
            tempBBSAccount.setAccount_no(bbsAccountACTModel.getAccount_no());
            tempBBSAccount.setLast_update(date);
            realm.commitTransaction();
        }

    }

}
