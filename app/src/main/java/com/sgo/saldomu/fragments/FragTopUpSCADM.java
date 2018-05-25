package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.TopUpSCADMActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 5/17/2018.
 */

public class FragTopUpSCADM extends BaseFragment {
    View v;
    SecurePreferences sp;
    Spinner spinner_bank_product;
    EditText et_jumlah, et_pesan;
    Button btn_next;
    private ProgressDialog progdialog;
    String memberIDLogin, commIDLogin, userPhoneID, accessKey, member_id_scadm, comm_id_scadm, selectedProductCode, selectedBankCode;
    String tx_id, member_id, member_code, member_name, comm_id, comm_code, comm_name, bank_code, bank_name,
            product_code, product_name, ccy_id, amount, admin_fee, total_amount, payment_remark, api_key;
    String bank_gateway, selectedBankGateway;
    private ArrayList<listBankModel> scadmListBankTopUp = new ArrayList<>();
    private ArrayList<String> spinnerContentStrings = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

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

        memberIDLogin = sp.getString(DefineValue.MEMBER_ID, "");
        commIDLogin = sp.getString(DefineValue.COMMUNITY_ID, "");
        userPhoneID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        Bundle bundle = getArguments();
        comm_name = bundle.getString(DefineValue.COMMUNITY_NAME);
        comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
        comm_id_scadm = bundle.getString(DefineValue.COMM_ID_SCADM);
        comm_code = bundle.getString(DefineValue.COMMUNITY_CODE);
        api_key = bundle.getString(DefineValue.API_KEY);
        member_code = bundle.getString(DefineValue.MEMBER_CODE);
        member_id_scadm = bundle.getString(DefineValue.MEMBER_ID_SCADM);

        spinner_bank_product = v.findViewById(R.id.spinner_bank_produk);
        et_jumlah = v.findViewById(R.id.et_jumlah);
        et_pesan = v.findViewById(R.id.et_remark);
        btn_next = v.findViewById(R.id.btn_next);
        scadmListBankTopUp.clear();
        initiateAdapterAndSpinner();
        getListBank();

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputValidation())
                    sentInsertTopUp();
            }
        });
    }

    public boolean inputValidation()
    {
        if (et_jumlah == null || et_jumlah.getText().toString().isEmpty()) {
            et_jumlah.requestFocus();
            et_jumlah.setError("Jumlah harus diisi!");
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
            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_GET_LIST_BANK_TOPUP_SCADM,
                    userPhoneID, accessKey, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, member_id_scadm);

            Timber.d("isi params get list bank topup scadm:" + params.toString());

            JsonHttpResponseHandler mHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {
                    try {
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

                            progdialog.dismiss();

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else {
                            Timber.d("Error isi response get list bank topup scadm:" + response.toString());
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);

                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        }
                        if (progdialog.isShowing())
                            progdialog.dismiss();

                    } catch (JSONException e) {
                        progdialog.dismiss();
                        Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if (progdialog.isShowing())
                        progdialog.dismiss();
                    getFragmentManager().popBackStack();
                    Timber.w("Error Koneksi get list bank topup scadm:" + throwable.toString());
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    if (progdialog.isShowing())
                        progdialog.dismiss();
                }
            };

            MyApiClient.getListBankTopupSCADM(getActivity(), params, mHandler);

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void sentInsertTopUp() {
        try {

            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            extraSignature = member_id_scadm + selectedProductCode + MyApiClient.CCY_VALUE + et_jumlah.getText().toString();
            RequestParams params = MyApiClient.getSignatureWithParams(commIDLogin, MyApiClient.LINK_CONFIRM_TOPUP_SCADM,
                    userPhoneID, accessKey, extraSignature);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID_SCADM, member_id_scadm);
            params.put(WebParams.BANK_CODE, selectedBankCode);
            params.put(WebParams.BANK_GATEWAY, selectedBankGateway);
            params.put(WebParams.PRODUCT_CODE, selectedProductCode);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, et_jumlah.getText().toString());
            params.put(WebParams.PAYMENT_REMARK, et_pesan.getText().toString());

            Timber.d("isi params confirm topup scadm:" + params.toString());

            JsonHttpResponseHandler mHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("isi response confirm topup scadm:" + response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            tx_id = response.getString(WebParams.TX_ID);
                            member_id = response.getString(WebParams.MEMBER_ID);
                            member_code = response.getString(WebParams.MEMBER_CODE);
                            member_name = response.getString(WebParams.MEMBER_NAME);
                            comm_id = response.getString(WebParams.COMM_ID);
                            comm_code = response.getString(WebParams.COMM_CODE);
                            comm_name = response.getString(WebParams.COMM_NAME);
                            bank_code = response.getString(WebParams.BANK_CODE);
                            bank_name = response.getString(WebParams.BANK_NAME);
                            product_code = response.getString(WebParams.PRODUCT_CODE);
                            product_name = response.getString(WebParams.PRODUCT_NAME);
                            ccy_id = response.getString(WebParams.CCY_ID);
                            amount = response.getString(WebParams.AMOUNT);
                            admin_fee = response.getString(WebParams.ADMIN_FEE);
                            total_amount = response.getString(WebParams.TOTAL_AMOUNT);
//                            payment_remark = response.getString(WebParams.PAYMENT_REMARK);

                            changeToConfirmTopup();

                            progdialog.dismiss();

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:" + response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else {
                            Timber.d("Error isi response confirm topup  scadm:" + response.toString());
                            code = response.getString(WebParams.ERROR_CODE) + ":" + response.getString(WebParams.ERROR_MESSAGE);

                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
//                            getActivity().finish();
                        }
                        if (progdialog.isShowing())
                            progdialog.dismiss();

                    } catch (JSONException e) {
                        progdialog.dismiss();
                        Toast.makeText(getActivity(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if (progdialog.isShowing())
                        progdialog.dismiss();
                    getFragmentManager().popBackStack();
                    Timber.w("Error Koneksi confirm topup  scadm:" + throwable.toString());
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    if (progdialog.isShowing())
                        progdialog.dismiss();
                }
            };

            MyApiClient.confirmTopupScadm(getActivity(), params, mHandler);

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void changeToConfirmTopup() {

        Bundle bundle1 = new Bundle();
        bundle1.putString(DefineValue.TX_ID, tx_id);
        bundle1.putString(DefineValue.MEMBER_ID_SCADM, member_id);
        bundle1.putString(DefineValue.MEMBER_CODE, member_code);
        bundle1.putString(DefineValue.MEMBER_NAME, member_name);
        bundle1.putString(DefineValue.COMM_ID_SCADM, comm_id);
        bundle1.putString(DefineValue.COMMUNITY_CODE, comm_code);
        bundle1.putString(DefineValue.COMMUNITY_NAME, comm_name);
        bundle1.putString(DefineValue.BANK_GATEWAY, selectedBankGateway);
        bundle1.putString(DefineValue.BANK_CODE, bank_code);
        bundle1.putString(DefineValue.BANK_NAME, bank_name);
        bundle1.putString(DefineValue.PRODUCT_CODE, product_code);
        bundle1.putString(DefineValue.PRODUCT_NAME, product_name);
        bundle1.putString(DefineValue.CCY_ID, ccy_id);
        bundle1.putString(DefineValue.AMOUNT, amount);
        bundle1.putString(DefineValue.FEE, admin_fee);
        bundle1.putString(DefineValue.TOTAL_AMOUNT, total_amount);
        bundle1.putString(DefineValue.REMARK, et_pesan.getText().toString());
        bundle1.putString(DefineValue.API_KEY, api_key);
//        bundle1.putString(DefineValue.PRODUCT_NAME, spinnerContentStrings.get(spinner_bank_product.getSelectedItemPosition()));
        Fragment mFrag = new FragTopUpConfirmSCADM();
        mFrag.setArguments(bundle1);

        TopUpSCADMActivity ftf = (TopUpSCADMActivity) getActivity();
        ftf.switchContent(mFrag, "Konfirmasi Tambah Saldo", true);
    }
}
