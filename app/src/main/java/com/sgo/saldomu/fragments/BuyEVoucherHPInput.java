package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.Beans.DenomModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.EvoucherHPActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.SgoPlusWeb;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel;
import com.sgo.saldomu.models.retrofit.MemberPulsaModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/*
  Created by Administrator on 1/15/2015.
 */
public class BuyEVoucherHPInput extends BaseFragment {

    private String[] namaProductBank;
    private String[] produkMANDIRI = {"MANDIRIIB", "MANDIRISMS"};
    private String[] listDenomName;
    private ArrayList<DenomModel> mArrayListDenom;

    private String _jumlah, _denomPayment, member_pulsa_id;
    private String _noHPdestination = "";

    private View v;
    private Button btn_submit_evoucher;
    private Spinner spin_produkBank;
    private Spinner spin_denom;
    private EditText noHP_value;
    private String bank_kode;
    private String produckBank_kode;
    private String memberID;
    private String topupType;
    String nama_bank;
    private ProgressDialog progdialog;
    private ArrayAdapter<String> adapter2;
    private ImageView spinWheelBankProduct;
    private ImageView spinWheelDenom;
    private Animation frameAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_buy_evoucher_input, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        topupType = args.getString(DefineValue.TRANSACTION_TYPE);

        if (topupType.equals(DefineValue.INTERNET_BANKING)) {
            memberID = sp.getString(DefineValue.MEMBER_ID, "");
            MyApiClient.IS_INTERNET_BANKING = true;
        } else if (topupType.equals(DefineValue.SMS_BANKING)) {
            getMemberPulsa();
            MyApiClient.IS_INTERNET_BANKING = false;
        }


        spin_produkBank = v.findViewById(R.id.spinner_evoucher_productBank);
        spin_denom = v.findViewById(R.id.spinner_evoucher_denom);
        noHP_value = v.findViewById(R.id.noHP_eVoucher_value);
        btn_submit_evoucher = v.findViewById(R.id.btn_submit_evoucher_input);
        spinWheelBankProduct = v.findViewById(R.id.spinning_wheel_evoucher_bank_product);
        spinWheelDenom = v.findViewById(R.id.spinning_wheel_evoucher_denom);

        frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        InitializeSpinner();

        btn_submit_evoucher.setOnClickListener(prosesTopupPulsaSGOListener);
    }

    private void InitializeSpinner() {
        namaProductBank = getResources().getStringArray(R.array.evoucer_productbank_list);
        mArrayListDenom = new ArrayList<>();
        listDenomName = new String[DenomModel.allDenom.length];

        bank_kode = "008";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, namaProductBank);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_produkBank.setAdapter(adapter);
        spin_produkBank.setOnItemSelectedListener(spinnerProductBankListener);


        adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listDenomName);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_denom.setAdapter(adapter2);
        spin_denom.setOnItemSelectedListener(spinnerDenomListener);

        spin_denom.setVisibility(View.GONE);
        spinWheelDenom.setVisibility(View.VISIBLE);
        spinWheelDenom.startAnimation(frameAnimation);

        Thread deproses = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < DenomModel.allDenom.length; i++) {
                    mArrayListDenom.add(new DenomModel(DenomModel.allDenom[i][0], DenomModel.allDenom[i][1], DenomModel.allDenom[i][2]));
                    listDenomName[i] = DenomModel.allDenom[i][2];
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinWheelDenom.clearAnimation();
                        spinWheelDenom.setVisibility(View.GONE);
                        spin_denom.setVisibility(View.VISIBLE);
                        adapter2.notifyDataSetChanged();
                    }
                });
            }
        };
        deproses.run();
    }

    private Spinner.OnItemSelectedListener spinnerProductBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            //Object item = adapterView.getItemAtPosition(i);
            if (MyApiClient.IS_INTERNET_BANKING) produckBank_kode = produkMANDIRI[0];
            else produckBank_kode = produkMANDIRI[1];

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener spinnerDenomListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            _jumlah = mArrayListDenom.get(i).getPrice();
            _denomPayment = mArrayListDenom.get(i).getId();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    private Button.OnClickListener prosesTopupPulsaSGOListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inputValidation()) {
                sentDataValidTopupPulsaRetail();
            }
        }
    };

    private void getMemberPulsa() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_MEMBER_PULSA);
            params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
            params.put(WebParams.DATE_TIME, produckBank_kode);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get Member Pulsa:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_MEMBER_PULSA, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            MemberPulsaModel response = getGson().fromJson(object, MemberPulsaModel.class);

                            String code = response.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                String arraynya = getGson().toJson(response.getMember_data());
                                setMemberPulsa(arraynya);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = response.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), response.getError_message());
                            } else {
                                code = response.getError_code();
                                if (code.equals("0003")) showDialogError();
                                else {
                                    code = response.getError_code() + ":" + response.getError_message();
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

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

    private void setMemberPulsa(String response) {

        try {
            JSONArray arrayJson = new JSONArray(response);
            member_pulsa_id = arrayJson.getJSONObject(0).getString(WebParams.MEMBER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sentDataValidTopupPulsaRetail() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();
            String _member_id;
            _noHPdestination = NoHPFormat.formatTo62(noHP_value.getText().toString());
            String denomPattern = _noHPdestination + "|" + _denomPayment;

            if (topupType.equals(DefineValue.INTERNET_BANKING)) {
                if (MyApiClient.IS_PROD) _member_id = MyApiClient.PROD_MEMBER_ID_PULSA_RETAIL;
                else _member_id = MyApiClient.DEV_MEMBER_ID_PULSA_RETAIL;
            } else _member_id = member_pulsa_id;

            String url;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TOPUP_PULSA_RETAIL);
            url = MyApiClient.LINK_TOPUP_PULSA_RETAIL;

            if (MyApiClient.IS_INTERNET_BANKING) {
                if (MyApiClient.IS_PROD) {
                    params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PROD_TOPUP_RETAIL);
                    url = MyApiClient.LINK_PROD_TOPUP_RETAIL;
                }
            }

            params.put(WebParams.MEMBER_ID, _member_id);
            params.put(WebParams.BANK_CODE, bank_kode);
            params.put(WebParams.PRODUCT_CODE, produckBank_kode);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, _jumlah);
            params.put(WebParams.PAYMENT_REMARK, denomPattern);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params topup pulsa retail:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(url, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            GetTrxStatusReportModel response = getGson().fromJson(object, GetTrxStatusReportModel.class);

                            String code = response.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Timber.d("isi response topup pulsa retail:" + response.toString());

                                if (topupType.equals(DefineValue.INTERNET_BANKING)) {

                                    changeToSGOPlus(response.getTx_id(), response.getComm_code());
                                } else {
                                    sentDataReqToken(response.getTx_id(), produckBank_kode, response.getComm_code());
                                }

                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = response.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), response.getError_message());
                            } else {
                                code = response.getError_code() + ":" + response.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

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


    private void sentDataReqToken(final String _tx_id, final String _product_code, final String _comm_code) {
        try {

            extraSignature = _tx_id + _comm_code + _product_code;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);
            params.put(WebParams.COMM_CODE, _comm_code);
            params.put(WebParams.TX_ID, _tx_id);
            params.put(WebParams.PRODUCT_CODE, _product_code);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params regtoken pulsa retail:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            jsonModel response = getGson().fromJson(object, jsonModel.class);

                            String code = response.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                showDialog(_tx_id, _product_code, _comm_code);
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = response.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            }else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + response.getApp_data());
                                final AppDataModel appModel = response.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), response.getError_message());
                            } else {
                                Timber.d("Error req token pulsa retail:" + response.toString());
                                code = response.getError_code() + " : " + response.getError_message();
                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

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

    private void showDialog(final String _tx_id, final String _product_code, final String _comm_code) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.smsBanking_dialog_validation_title));
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getResources().getString(R.string.dialog_token_message_sms));


        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment newFrag = new TopUpToken();
                Bundle mArgs = new Bundle();
                mArgs.putString(DefineValue.TRANSACTION_TYPE, DefineValue.PULSA);
                mArgs.putString(DefineValue.BANK_CHANNEL, spin_produkBank.getSelectedItem().toString());
                mArgs.putString(WebParams.TX_ID, _tx_id);
                mArgs.putString(WebParams.PRODUCT_CODE, _product_code);
                mArgs.putString(WebParams.COMM_CODE, _comm_code);
                mArgs.putString(WebParams.AMOUNT, _jumlah);
                mArgs.putString(WebParams.PRODUCT_VALUE, _noHPdestination);

                newFrag.setArguments(mArgs);
                switchFragment(newFrag, getString(R.string.toolbar_title_topup), true);

                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showDialogError() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification);

        // set values for custom dialog components - text, image and button
        Button btnDialogOTP = dialog.findViewById(R.id.btn_dialog_notification_ok);
        TextView Title = dialog.findViewById(R.id.title_dialog);
        TextView Message = dialog.findViewById(R.id.message_dialog);

        Message.setVisibility(View.VISIBLE);
        Title.setText(getString(R.string.evoucer_sb_dialog_title));
        Message.setText(getString(R.string.evoucer_sb_dialog_message));


        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getFragmentManager().popBackStack();
            }
        });

        dialog.show();
    }

    private void changeToSGOPlus(String _tx_id, String _comm_code) {
        Intent i = new Intent(getActivity(), SgoPlusWeb.class);
        i.putExtra(WebParams.PRODUCT_CODE, produckBank_kode);
        i.putExtra(WebParams.BANK_CODE, bank_kode);
        i.putExtra(WebParams.COMM_CODE, _comm_code);
        i.putExtra(WebParams.TX_ID, _tx_id);
        i.putExtra(DefineValue.TRANSACTION_TYPE, DefineValue.PULSA);

        //if(MyApiClient.PROD_FLAG)i.putExtra(WebParams.API_KEY,MyApiClient.PROD_API_KEY);
        //else i.putExtra(WebParams.API_KEY,MyApiClient.DEV_API_KEY);

        switchActivity(i);
    }

    private void switchActivity(Intent mIntent) {
        if (getActivity() == null)
            return;

        EvoucherHPActivity fca = (EvoucherHPActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        EvoucherHPActivity fca = (EvoucherHPActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    private boolean inputValidation() {
        if (noHP_value.getText().toString().length() == 0) {
            noHP_value.requestFocus();
            noHP_value.setError(this.getString(R.string.regist1_validation_nohp));
            return false;
        } else if (noHP_value.getText().toString().length() < 10) {
            noHP_value.requestFocus();
            noHP_value.setError(this.getString(R.string.regist1_validation_length_nohp));
            return false;
        }
        return true;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

}
