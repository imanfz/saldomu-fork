package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TopUpSCADMActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.NoHPFormat;
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
import com.sgo.saldomu.utils.NumberTextWatcherForThousand;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/17/2018.
 */

public class FragTopUpSCADM extends BaseFragment {
    View v;
    SecurePreferences sp;
    Spinner spinner_bank_product;
    EditText et_jumlah, et_pesan, et_membercode;
    Button btn_next;
    ImageView iv_clear_partner_code;
    private ProgressDialog progdialog;
    String userPhoneID, member_id_scadm, comm_id_scadm, selectedProductCode, selectedBankCode;
    String tx_id, member_code, member_name, comm_code, comm_name, bank_code, bank_name,
            product_code, product_name, amount, admin_fee, total_amount, api_key, storeName, storeAddress, storeCode;
    String bank_gateway, selectedBankGateway;
    private ArrayList<listBankModel> scadmListBankTopUp = new ArrayList<>();
    private ArrayList<String> spinnerContentStrings = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private Switch favoriteSwitch;
    private EditText notesEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_topup_scadm, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();


        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");

        spinner_bank_product = v.findViewById(R.id.spinner_bank_produk);
        et_jumlah = v.findViewById(R.id.et_jumlah);
        et_pesan = v.findViewById(R.id.et_remark);
        et_membercode = v.findViewById(R.id.et_member_code);
        btn_next = v.findViewById(R.id.btn_next);
        iv_clear_partner_code = v.findViewById(R.id.iv_clear_partner_code);
        favoriteSwitch = v.findViewById(R.id.favorite_switch);
        notesEditText = v.findViewById(R.id.notes_edit_text);

        Bundle bundle = getArguments();
        if (bundle != null) {
            comm_name = bundle.getString(DefineValue.COMMUNITY_NAME);
            comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
            comm_id_scadm = bundle.getString(DefineValue.COMM_ID_SCADM);
            api_key = bundle.getString(DefineValue.API_KEY);
            member_code = bundle.getString(DefineValue.MEMBER_CODE);
            member_id_scadm = bundle.getString(DefineValue.MEMBER_ID_SCADM);
            if (bundle.containsKey(DefineValue.CUST_ID))
                et_membercode.setText(bundle.getString(DefineValue.CUST_ID));
            else
                et_membercode.setText(userPhoneID);
        }

        iv_clear_partner_code.setOnClickListener(v -> et_membercode.setText(""));

        scadmListBankTopUp.clear();
        spinnerContentStrings.clear();
        initiateAdapterAndSpinner();
        getListBank();

        et_jumlah.addTextChangedListener(new NumberTextWatcherForThousand(et_jumlah));
        amount = NumberTextWatcherForThousand.trimCommaOfString(et_jumlah.getText().toString());

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                amount = NumberTextWatcherForThousand.trimCommaOfString(et_jumlah.getText().toString());
                if (inputValidation()) {
                    if (!member_code.equals(userPhoneID)) {
                        member_code = NoHPFormat.formatTo62(et_membercode.getText().toString());
                    } else
                        member_code = et_membercode.getText().toString();

                    if (selectedProductCode.equalsIgnoreCase("SCASH")) {
                        sentInsertTopUpSCASH();
                    } else
                        sentInsertTopUp();

                }

            }
        });

        favoriteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notesEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            notesEditText.setEnabled(isChecked);
        });
    }

    public boolean inputValidation() {

        amount = NumberTextWatcherForThousand.trimCommaOfString(et_jumlah.getText().toString());
        if (et_membercode == null || et_membercode.getText().toString().isEmpty()) {
            et_membercode.requestFocus();
            et_membercode.setError(getString(R.string.member_code_validation));
            return false;
        } else if (et_jumlah == null || et_jumlah.getText().toString().isEmpty()) {
            et_jumlah.requestFocus();
            et_jumlah.setError(getString(R.string.sgoplus_validation_jumlahSGOplus));
            return false;
        } else if ((Integer.parseInt(amount) % 1000) != 0) {
            et_jumlah.requestFocus();
            et_jumlah.setError(getString(R.string.amount_validation_scadm));
            return false;
        } else if (favoriteSwitch.isChecked() && notesEditText.getText().toString().length() == 0) {
            notesEditText.requestFocus();
            notesEditText.setError(getString(R.string.payfriends_notes_zero));
            return false;
        }
        return true;
    }

    public void initiateAdapterAndSpinner() {
        //fill your custom layout
        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        //fill your custom layout
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_bank_product.setAdapter(arrayAdapter);

        spinner_bank_product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedProductCode = scadmListBankTopUp.get(i).getProduct_code();
                selectedBankCode = scadmListBankTopUp.get(i).getBank_code();
                selectedBankGateway = scadmListBankTopUp.get(i).getBank_gateway();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void getListBank() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            extraSignature = member_id_scadm;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_BANK_TOPUP_SCADM, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, member_id_scadm);

            Timber.d("isi params get list bank topup scadm:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_BANK_TOPUP_SCADM, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("isi response get list bank topup scadm:" + response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {

                                    JSONArray mArrayBank = new JSONArray(response.getString(WebParams.BANK));

                                    for (int i = 0; i < mArrayBank.length(); i++) {
                                        bank_code = mArrayBank.getJSONObject(i).getString(WebParams.BANK_CODE);
                                        bank_name = mArrayBank.getJSONObject(i).getString(WebParams.BANK_NAME);
                                        product_code = mArrayBank.getJSONObject(i).getString(WebParams.PRODUCT_CODE);
                                        product_name = mArrayBank.getJSONObject(i).getString(WebParams.PRODUCT_NAME);
                                        bank_gateway = mArrayBank.getJSONObject(i).getString(WebParams.BANK_GATEWAY);

                                        listBankModel listBankModel = new listBankModel();
                                        listBankModel.setBank_code(bank_code);
                                        listBankModel.setBank_name(bank_name);
                                        listBankModel.setProduct_code(product_code);
                                        listBankModel.setProduct_name(product_name);
                                        listBankModel.setBank_gateway(bank_gateway);

                                        scadmListBankTopUp.add(listBankModel);
                                        spinnerContentStrings.add(product_name);

                                    }
                                    arrayAdapter.addAll(spinnerContentStrings);
                                    arrayAdapter.notifyDataSetChanged();

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
                                } else {
                                    Timber.d("Error isi response get list bank topup scadm:" + response.toString());
                                    code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);

                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
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
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sentInsertTopUpSCASH() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            extraSignature = member_id_scadm + comm_id_scadm + MyApiClient.CCY_VALUE;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_TOPUP_SCADM_NEW, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_GOWORLD, member_id_scadm);
            params.put(WebParams.COMM_ID_GOWORLD, comm_id_scadm);
            params.put(WebParams.MEMBER_CODE_GOWORLD, member_code);
            params.put(WebParams.COMM_CODE_GOWORLD, comm_code);
            params.put(WebParams.BANK_CODE, selectedBankCode);
            params.put(WebParams.BANK_GATEWAY, selectedBankGateway);
            params.put(WebParams.PRODUCT_CODE, selectedProductCode);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, et_pesan.getText().toString());
            params.put(WebParams.MEMBER_REMARK, member_code);

            Timber.d("isi params confirm topup scadm:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_TOPUP_SCADM_NEW, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response confirm topup scadm:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    tx_id = response.getString(WebParams.TX_ID);
                                    comm_name = response.getString(WebParams.COMM_NAME);
//                                    bank_code = response.getString(WebParams.BANK_CODE);
                                    bank_name = response.getString(WebParams.BANK_NAME);
//                                    product_code = response.getString(WebParams.PRODUCT_CODE);
                                    product_name = response.getString(WebParams.PRODUCT_NAME);
//                                    ccy_id = response.getString(WebParams.CCY_ID);
                                    amount = response.getString(WebParams.AMOUNT);
                                    admin_fee = response.getString(WebParams.ADMIN_FEE);
                                    total_amount = response.getString(WebParams.TOTAL_AMOUNT);
                                    storeName = response.getString(WebParams.STORE_NAME);
                                    storeAddress = response.getString(WebParams.STORE_ADDRESS);
                                    storeCode = response.getString(WebParams.STORE_CODE);
//                            payment_remark = response.getString(WebParams.PAYMENT_REMARK);

                                    changeToConfirmTopup();

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
                                    Timber.d("Error isi response confirm topup  scadm:%s", response.toString());
                                    Toast.makeText(getActivity(), code + ":" + message, Toast.LENGTH_LONG).show();
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

    public void sentInsertTopUp() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            extraSignature = member_id_scadm + selectedProductCode + MyApiClient.CCY_VALUE + amount;
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_TOPUP_SCADM, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, member_id_scadm);
            params.put(WebParams.BANK_CODE, selectedBankCode);
            params.put(WebParams.BANK_GATEWAY, selectedBankGateway);
            params.put(WebParams.PRODUCT_CODE, selectedProductCode);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, et_pesan.getText().toString());

            Timber.d("isi params confirm topup scadm:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_TOPUP_SCADM, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("isi response confirm topup scadm:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    tx_id = response.getString(WebParams.TX_ID);
                                    member_code = response.getString(WebParams.MEMBER_CODE);
                                    member_name = response.getString(WebParams.MEMBER_NAME);
                                    comm_code = response.getString(WebParams.COMM_CODE);
                                    comm_name = response.getString(WebParams.COMM_NAME);
                                    bank_code = response.getString(WebParams.BANK_CODE);
                                    bank_name = response.getString(WebParams.BANK_NAME);
                                    product_code = response.getString(WebParams.PRODUCT_CODE);
                                    product_name = response.getString(WebParams.PRODUCT_NAME);
                                    amount = response.getString(WebParams.AMOUNT);
                                    admin_fee = response.getString(WebParams.ADMIN_FEE);
                                    total_amount = response.getString(WebParams.TOTAL_AMOUNT);
                                    storeName = response.getString(WebParams.STORE_NAME);
                                    storeAddress = response.getString(WebParams.STORE_ADDRESS);
                                    storeCode = response.getString(WebParams.STORE_CODE);
                                    changeToConfirmTopup();

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
                                    Timber.d("Error isi response confirm topup  scadm:%s", response.toString());
                                    Toast.makeText(getActivity(), code + ":" + message, Toast.LENGTH_LONG).show();
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

    public void changeToConfirmTopup() {

        Bundle bundle1 = new Bundle();
        bundle1.putString(DefineValue.TX_ID, tx_id);
        bundle1.putString(DefineValue.MEMBER_ID_SCADM, member_id_scadm);
        bundle1.putString(DefineValue.MEMBER_CODE, member_code);
        bundle1.putString(DefineValue.MEMBER_NAME, member_name);
        bundle1.putString(DefineValue.COMM_ID_SCADM, comm_id_scadm);
        bundle1.putString(DefineValue.COMMUNITY_CODE, comm_code);
        bundle1.putString(DefineValue.COMMUNITY_NAME, comm_name);
        bundle1.putString(DefineValue.BANK_GATEWAY, selectedBankGateway);
        bundle1.putString(DefineValue.BANK_CODE, selectedBankCode);
        bundle1.putString(DefineValue.BANK_NAME, bank_name);
        bundle1.putString(DefineValue.PRODUCT_CODE, selectedProductCode);
        bundle1.putString(DefineValue.PRODUCT_NAME, product_name);
        bundle1.putString(DefineValue.CCY_ID, MyApiClient.CCY_VALUE);
        bundle1.putString(DefineValue.AMOUNT, amount);
        bundle1.putString(DefineValue.FEE, admin_fee);
        bundle1.putString(DefineValue.TOTAL_AMOUNT, total_amount);
        bundle1.putString(DefineValue.REMARK, et_pesan.getText().toString());
        bundle1.putString(DefineValue.API_KEY, api_key);
        bundle1.putString(DefineValue.STORE_NAME, storeName);
        bundle1.putString(DefineValue.STORE_ADDRESS, storeAddress);
        bundle1.putString(DefineValue.STORE_CODE, storeCode);
        if (favoriteSwitch.isChecked()) {
            bundle1.putBoolean(DefineValue.IS_FAVORITE, true);
            bundle1.putString(DefineValue.CUST_ID, member_code);
            bundle1.putString(DefineValue.NOTES, notesEditText.getText().toString());
            bundle1.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.B2B);
            bundle1.putString(DefineValue.PRODUCT_TYPE, DefineValue.TOPUP_B2B);
        }
        Fragment mFrag = new FragTopUpConfirmSCADM();
        mFrag.setArguments(bundle1);
        SwitchFragmentTop(mFrag, TopUpSCADMActivity.TOPUP, true);
    }
}
