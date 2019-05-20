package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.SMSclass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.dialogs.SMSDialog;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.DataReqModel;
import com.sgo.saldomu.models.retrofit.TopupValidModel;
import com.sgo.saldomu.widgets.BaseFragment;
import android.support.v4.app.FragmentManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

//import com.sgo.saldomu.activities.TagihanActivity;
//import com.sgo.saldomu.widgets.CustomFacebookButton;

/*
  Created by Administrator on 11/5/2014.
 */
public class SgoPlus_input extends BaseFragment implements EasyPermissions.PermissionCallbacks {
    private final static int RC_SENDSMS = 103;
    private HashMap<String, String> listBankName;
    private HashMap<String, String> listBankProduct;
    private List<listBankModel> listDB;
    private ArrayList<String> BankProduct;
    private InformationDialog dialogI;
    private SMSclass smsclass;
    private SMSDialog smsDialog;
    private SentObject sentObject;

    View v;
    Button btn_subSGO;
    Spinner spin_namaBank, spin_produkBank;
    EditText jumlahSGO_value;
    listBankModel listBankModel;
    String topupType, data, bank_name, product_name, bank_code, product_code, pairing_id, is_pairing;
    ProgressDialog progdialog;
    ArrayAdapter<String> adapter3;
    ImageView spinWheelBankName, spinWheelBankProduct;
    Animation frameAnimation;
    Spinner sp_privacy;
    Calendar calendar;
    Long timeDate;
    int privacy;
    boolean isSMSBanking = false, isTagihan = false, isFacebook = false;


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_SENDSMS:
                smsDialog.sentSms();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_SENDSMS:
                Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
                if (progdialog.isShowing())
                    progdialog.dismiss();
                if (smsDialog != null) {
                    smsDialog.dismiss();
                    smsDialog.reset();
                }
                break;
        }
    }
//    CustomFacebookButton facebookButton;

//    @Override
//    public void onSuccessLogin() {
//        isFacebook = true;
//        Timber.wtf("masuk Success login Facebook");
//    }
//
//    @Override
//    public void onFailedLogin() {
//        isFacebook = false;
//        Timber.wtf("masuk failed login Facebook");
//    }
//
//    @Override
//    public void onCheckedChange(Boolean isChecked) {
////        if(isChecked) {
////            v.findViewById(R.id.txtFacebookNote).setVisibility(View.VISIBLE);
////            isFacebook = true;
////        }else {
////            v.findViewById(R.id.txtFacebookNote).setVisibility(View.GONE);
////            isFacebook = false;
////        }
//    }

    private class SentObject {
        String tx_id;
        String product_code;
        String bank_kode;
        String product_name;
        String comm_code;
        String fee;
        String ccy_id;
        String nama_bank;
        String amount;
        String productValue;

        SentObject() {
        }

        public String getData() {
            String tempt = this.tx_id + "//" +
                    this.product_code + "//" +
                    this.bank_kode + "//" +
                    this.product_name + "//" +
                    this.comm_code + "//" +
                    this.fee + "//" +
                    this.ccy_id + "//" +
                    this.nama_bank + "//" +
                    this.amount + "//" +
                    this.productValue;
            return tempt;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        smsDialog=new SMSDialog();

        if (sentObject == null)
            sentObject = new SentObject();
        if (args.containsKey(DefineValue.TAGIHAN))
            isTagihan = args.getBoolean(DefineValue.TAGIHAN, false);

//        if(isTagihan){
//            data = getArguments().getString(DefineValue.BANKLIST_DATA, "");
//            topupType = args.getString(DefineValue.TRANSACTION_TYPE);
//            pairing_id = args.getString(DefineValue.PAIRING_ID);
//            is_pairing = args.getString(DefineValue.IS_PAIRING);
//            if(topupType.equals(DefineValue.SMS_BANKING))
//                isSMSBanking = true;
//        }
        else {
            listBankModel = new listBankModel(
                    args.getString(DefineValue.BANK_CODE, ""),
                    args.getString(DefineValue.BANK_NAME, ""),
                    args.getString(DefineValue.PRODUCT_CODE, ""),
                    args.getString(DefineValue.PRODUCT_NAME, ""),
                    args.getString(DefineValue.PRODUCT_TYPE, ""),
                    args.getString(DefineValue.PRODUCT_H2H, ""));
            if (listBankModel.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS))
                isSMSBanking = true;
        }


        if (isSMSBanking) {
            isSMSBanking = true;
            dialogI = InformationDialog.newInstance(this, 3);

            initializeSmsClass();
            smsDialog = SMSDialog.newDialog(timeDate,checkFailedVerify(), new SMSDialog.DialogButtonListener() {
                @Override
                public void onClickOkButton(View v, boolean isLongClick) {
                    if (EasyPermissions.hasPermissions(getActivity(), Manifest.permission.CAMERA)) {
                        smsDialog.sentSms();
                    } else {
                        EasyPermissions.requestPermissions(SgoPlus_input.this,
                                getString(R.string.rational_sent_sms),
                                RC_SENDSMS, Manifest.permission.CAMERA);
                    }
                }

                @Override
                public void onClickCancelButton(View v, boolean isLongClick) {
                    if (progdialog.isShowing())
                        progdialog.dismiss();
                }

                @Override
                public void onSuccess(int user_is_new) {

                }

                @Override
                public void onSuccess(String product_value) {
                    if (sentObject != null) {
                        sentObject.productValue = product_value;
                        Timber.d("onSuccess SMS verifikasi " + sentObject.getData());
                        smsDialog.dismiss();
                        smsDialog.reset();
                        sentDataReqToken();
                    }
                }
            });
        } else
            dialogI = InformationDialog.newInstance(this, 2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_sgoplus_input, container, false);
//        facebookButton = (CustomFacebookButton) v.findViewById(R.id.fb_button);
//        facebookButton.setFragment(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        InitializeData();

    }

    private void initializeSmsClass() {
        SMSclass smSclass = new SMSclass(getActivity());

        smSclass.isSimExists(new SMSclass.SMS_SIM_STATE() {
            @Override
            public void sim_state(Boolean isExist, String msg) {
                if (!isExist) {
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }
        });

        try {
            getActivity().unregisterReceiver(smSclass.simStateReceiver);
        } catch (Exception ignored) {
        }
        getActivity().registerReceiver(smSclass.simStateReceiver, SMSclass.simStateIntentFilter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void setActionBarTitle(String _title) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof TopUpActivity) {
            TopUpActivity fca = (TopUpActivity) getActivity();
            fca.setToolbarTitle(_title);
        }
//        else if(getActivity() instanceof TagihanActivity){
//            TagihanActivity fca = (TagihanActivity) getActivity();
//            fca.setToolbarTitle(_title);
//        }
    }

    private void InitializeData() {

        jumlahSGO_value = v.findViewById(R.id.jumlahSGOplus_value);
        btn_subSGO = v.findViewById(R.id.btn_submit_sgoplus_input);
        sp_privacy = v.findViewById(R.id.payfriend_privacy_spinner);
        spin_namaBank = v.findViewById(R.id.spinner_nameBank);
        spin_produkBank = v.findViewById(R.id.spinner_productBank);
        String titleAb;
//            if(isTagihan){
//
//                spinWheelBankName = (ImageView) v.findViewById(R.id.spinning_wheel_bank_name);
//                spinWheelBankProduct = (ImageView) v.findViewById(R.id.spinning_wheel_bank_product);
//                frameAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_animation);
//                frameAnimation.setRepeatCount(Animation.INFINITE);
//
//                String amountTagihan = getArguments().getString(DefineValue.AMOUNT_TAGIHAN, "");
//                jumlahSGO_value.setText(amountTagihan);
//                jumlahSGO_value.setEnabled(false);
//
//                spin_namaBank.setVisibility(View.VISIBLE);
//                spin_produkBank.setVisibility(View.VISIBLE);
//
//                JSONArray mData = new JSONArray(getArguments().getString(DefineValue.BANKLIST_DATA,""));
//
//                ArrayList<String> bankName = new ArrayList<>();
//                BankProduct = new ArrayList<>();
//                listBankName = new HashMap<>();
//                listDB = new ArrayList<>();
//
//                for (int i = 0 ; i < mData.length(); i++) {
//                    JSONObject mObj = mData.getJSONObject(i);
//                    listBankModel mDObj = new listBankModel(mObj.getString(WebParams.BANK_CODE),
//                            mObj.getString(WebParams.BANK_NAME),
//                            mObj.getString(WebParams.PRODUCT_CODE),
//                            mObj.getString(WebParams.PRODUCT_NAME),
//                            mObj.getString(WebParams.PRODUCT_TYPE),
//                            mObj.getString(WebParams.PRODUCT_H2H));
//                    listDB.add(mDObj);
//
//                    if(!bankName.contains(mDObj.getBank_name())) {
//                        bankName.add(mDObj.getBank_name());
//                        listBankName.put(mDObj.getBank_name(),mDObj.getBank_code());
//                    }
//
//                }
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankName);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spin_namaBank.setAdapter(adapter);
//                spin_namaBank.setOnItemSelectedListener(spinnerNamaBankListener);
//
//                adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, BankProduct);
//                adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spin_produkBank.setAdapter(adapter3);
//
//                if(isSMSBanking)
//                    titleAb = getString(R.string.smsBanking_ab_title);
//                else
//                    titleAb = getString(R.string.internetBanking_ab_title);
//            }
//            else {

        spin_namaBank.setVisibility(View.GONE);
        spin_produkBank.setVisibility(View.GONE);
        TextView productname = v.findViewById(R.id.text_productbank);
        TextView bankname = v.findViewById(R.id.text_namebank);

        bank_name = listBankModel.getBank_name();
        bank_code = listBankModel.getBank_code();
        product_name = listBankModel.getProduct_name();
        product_code = listBankModel.getProduct_code();

        productname.setText(product_name);
        bankname.setText(bank_name);
        titleAb = getString(R.string.toolbar_title_topup) + " - " + bank_name;
//            }

        setActionBarTitle(titleAb);

        btn_subSGO.setOnClickListener(prosesSGOplusListener);
        jumlahSGO_value.addTextChangedListener(jumlahChangeListener);

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);
        calendar = Calendar.getInstance();
        timeDate = calendar.getTimeInMillis();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        facebookButton.callBackFacebook(requestCode, resultCode, data);
    }

    Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i + 1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    TextWatcher jumlahChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().equals("0")) jumlahSGO_value.setText("");
            if (s.length() > 0 && s.charAt(0) == '0') {
                int i = 0;
                for (; i < s.length(); i++) {
                    if (s.charAt(i) != '0') break;
                }
                jumlahSGO_value.setText(s.toString().substring(i));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {

            spin_produkBank.setVisibility(View.GONE);
            spinWheelBankProduct.setVisibility(View.VISIBLE);
            spinWheelBankProduct.startAnimation(frameAnimation);

            Object item = adapterView.getItemAtPosition(i);
            sentObject.nama_bank = item.toString();
            Timber.d("nama bank:" + sentObject.nama_bank);

            BankProduct.clear();
            listBankProduct = new HashMap<>();

            Thread deproses = new Thread() {
                @Override
                public void run() {
                    for (listBankModel aListDB : listDB) {
                        //Timber.d("isi semua", aListDB.getProduct_type()+"; "+aListDB.getProduct_name()+"; "+aListDB.getProduct_code());
                        if (aListDB.getBank_name().equals(sentObject.nama_bank)) {
                            //Timber.d("isi product name", aListDB.getProduct_name());
                            BankProduct.add(aListDB.getProduct_name());
                            listBankProduct.put(aListDB.getProduct_name(), aListDB.getProduct_code());
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinWheelBankProduct.clearAnimation();
                            spinWheelBankProduct.setVisibility(View.GONE);
                            spin_produkBank.setVisibility(View.VISIBLE);
                            adapter3.notifyDataSetChanged();
                        }
                    });
                }
            };
            deproses.start();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    Button.OnClickListener prosesSGOplusListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    if (isTagihan) {
//                        changeTagihanPreview(listBankName.get(spin_namaBank.getSelectedItem().toString()),
//                                spin_namaBank.getSelectedItem().toString(),
//                                listBankProduct.get(spin_produkBank.getSelectedItem().toString()),
//                                spin_produkBank.getSelectedItem().toString());
                    } else {
                        sentDataValidTopup(bank_code,
                                bank_name,
                                product_code,
                                product_name);
                    }
                }
            } else
                DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    public void sentDataValidTopup(final String bank_kode, final String bank_name, String product_code, final String product_name) {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            final String amount = String.valueOf(jumlahSGO_value.getText());

            extraSignature = memberIDLogin + product_code + MyApiClient.CCY_VALUE + amount;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_VALID_TOPUP, extraSignature);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.BANK_CODE, bank_kode);
            params.put(WebParams.PRODUCT_CODE, product_code);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.AMOUNT, amount);
            params.put(WebParams.PAYMENT_REMARK, "");
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params sgoplusinput:" + params.toString());


            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_VALID_TOPUP, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            TopupValidModel model = getGson().fromJson(object, TopupValidModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                if (isSMSBanking) {
                                    if (sentObject == null)
                                        sentObject = new SentObject();
                                    sentObject.tx_id = model.getTx_id();
                                    sentObject.product_code = model.getProduct_code();
                                    sentObject.bank_kode = bank_kode;
                                    sentObject.nama_bank = bank_name;
                                    sentObject.comm_code = model.getComm_code();
                                    sentObject.fee = model.getFee();
                                    sentObject.amount = amount;
                                    sentObject.product_name = product_name;
                                    sentObject.ccy_id = MyApiClient.CCY_VALUE;
                                    Timber.d("Valid topup " + sentObject.getData());
                                    smsDialog.show(getFragmentManager(),"aa");
                                } else {
                                    changeTopUpSgoPlus(model.getTx_id(), model.getProduct_code(), bank_kode
                                            , product_name, model.getComm_code(), model.getFee(),
                                            MyApiClient.CCY_VALUE, bank_name, amount, false, null);
                                }
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                code = model.getError_code() + " : " + model.getError_message();

                                Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
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
            Timber.d("httpclient:" + e.getMessage());
        }
    }

//    private void changeTagihanPreview(String _bank_kode, String _bank_name, String _product_code, String _product_name) {
//
//        getActivity().getSupportFragmentManager().popBackStack();
//        Fragment newFrag = new PreviewTagihanViaBank();
//        Bundle mArgs = new Bundle();
//
//        mArgs.putString(DefineValue.TOPUP_TYPE, DefineValue.EMONEY);
//        mArgs.putString(DefineValue.PRODUCT_CODE,_product_code);
//        mArgs.putString(DefineValue.PRODUCT_NAME,_product_name);
//        mArgs.putString(DefineValue.COMMUNITY_ID,  getArguments().getString(DefineValue.COMMUNITY_ID, ""));
//        mArgs.putString(DefineValue.COMMUNITY_CODE, getArguments().getString(DefineValue.COMMUNITY_CODE, ""));
//        mArgs.putString(DefineValue.COMMUNITY_NAME, getArguments().getString(DefineValue.COMMUNITY_NAME, ""));
//        mArgs.putString(DefineValue.CALLBACK_URL, getArguments().getString(DefineValue.CALLBACK_URL, ""));
//        mArgs.putString(DefineValue.API_KEY, getArguments().getString(DefineValue.API_KEY, ""));
//        mArgs.putString(DefineValue.BANK_CODE, _bank_kode);
//        mArgs.putString(DefineValue.BANK_NAME, _bank_name);
//        mArgs.putString(DefineValue.INVOICES, getArguments().getString(DefineValue.INVOICES, ""));
//        mArgs.putString(DefineValue.AMOUNT, getArguments().getString(DefineValue.AMOUNT_TAGIHAN, ""));
//        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
//        mArgs.putBoolean(DefineValue.IS_SMS_BANKING, isSMSBanking);
//        mArgs.putString(DefineValue.IS_PAIRING, is_pairing);
//        mArgs.putString(DefineValue.PAIRING_ID, pairing_id);
//
//        newFrag.setArguments(mArgs);
//
//        switchTagihanFragment(newFrag, getString(R.string.toolbar_title_sekolahku),true);
//        if(isTagihan) {
//            spin_namaBank.setSelection(0);
//            spin_produkBank.setSelection(0);
//        }
//        else {
//            jumlahSGO_value.setText("");
//        }
//    }

    private void changeTopUpSgoPlus(String _tx_id, String _product_code, String bank_kode, String productBank_name,
                                    String _comm_code, String _fee, String _ccy_id, String _nama_bank, String _amount,
                                    Boolean isSmsBanking, String productValue) {
        Timber.d("change topup bankname " + sentObject.getData());
        Fragment newFrag = new TopUpToken();
        Bundle mArgs = new Bundle();
        mArgs.putString(DefineValue.TOPUP_TYPE, DefineValue.EMONEY);
        mArgs.putString(DefineValue.TX_ID, _tx_id);
        mArgs.putString(DefineValue.PRODUCT_CODE, _product_code);
        mArgs.putString(DefineValue.PRODUCT_NAME, productBank_name);
        mArgs.putString(DefineValue.COMMUNITY_CODE, _comm_code);
        mArgs.putString(DefineValue.BANK_CODE, bank_kode);
        mArgs.putString(DefineValue.BANK_NAME, _nama_bank);
        mArgs.putString(DefineValue.CCY_ID, _ccy_id);
        mArgs.putString(DefineValue.FEE, _fee);
        mArgs.putBoolean(DefineValue.IS_SMS_BANKING, isSmsBanking);
        mArgs.putString(DefineValue.AMOUNT, _amount);
        mArgs.putString(DefineValue.SHARE_TYPE, String.valueOf(privacy));
        mArgs.putBoolean(DefineValue.IS_FACEBOOK, isFacebook);

        if (isSmsBanking)
            mArgs.putString(DefineValue.PRODUCT_VALUE, productValue);

        newFrag.setArguments(mArgs);

        switchFragment(newFrag, getString(R.string.toolbar_title_topup), true);
        if (isTagihan) {
//            spin_namaBank.setSelection(0);
//            spin_produkBank.setSelection(0);
        } else {
            jumlahSGO_value.setText("");
        }
    }

    public void sentDataReqToken() {
        try {

            extraSignature = sentObject.tx_id + sentObject.comm_code + sentObject.product_code;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature);
            params.put(WebParams.COMM_CODE, sentObject.comm_code);
            params.put(WebParams.TX_ID, sentObject.tx_id);
            params.put(WebParams.PRODUCT_CODE, sentObject.product_code);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.PRODUCT_VALUE, sentObject.productValue);

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            DataReqModel model = getGson().fromJson(object, DataReqModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                sentObject.productValue = model.getProduct_value();
                                showDialog();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(getActivity(), message);
                            } else {
                                if (code.equals("0059") || code.equals("0164")) {
                                    showDialogErrorSMS(sentObject.nama_bank, code, model.getError_message());
                                } else {
                                    code = model.getError_code() + " : " + model.getError_message();

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

    private void showDialog() {
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
        Title.setText(getResources().getString(R.string.regist1_notif_title_verification));
        Message.setText(getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms));
        Timber.d("showdialog  " + sentObject.getData());
        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeTopUpSgoPlus(sentObject.tx_id,
                        sentObject.product_code,
                        sentObject.bank_kode,
                        sentObject.product_name,
                        sentObject.comm_code,
                        sentObject.fee,
                        sentObject.ccy_id,
                        sentObject.nama_bank,
                        sentObject.amount,
                        true,
                        sentObject.productValue);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void showDialogErrorSMS(final String _nama_bank, String error_code, String error_msg) {
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
        Title.setText(getString(R.string.topup_dialog_not_registered));
        if (error_code.equals("0059")) {
//            Message.setText(getString(R.string.topup_not_registered,_nama_bank));
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.firstscreen_button_daftar));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newIntent = new Intent(getActivity(), RegisterSMSBankingActivity.class);
                    newIntent.putExtra(DefineValue.BANK_NAME, _nama_bank);
                    switchActivity(newIntent, MainPage.ACTIVITY_RESULT);

                    dialog.dismiss();
                }
            });
        } else if (error_code.equals("0164")) {
            Message.setText(error_msg);
            btnDialogOTP.setText(getString(R.string.close));
            btnDialogOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    getActivity().finish();
                }
            });
        }
        if (isTagihan) {
//            spin_namaBank.setSelection(0);
//            spin_produkBank.setSelection(0);
        } else {
            jumlahSGO_value.setText("");
        }

        dialog.show();
    }


    private void switchActivity(Intent mIntent, int j) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof TopUpActivity) {
            TopUpActivity fca = (TopUpActivity) getActivity();
            fca.switchActivity(mIntent, j);
        }
//        else if(getActivity() instanceof TagihanActivity){
//            TagihanActivity fca = (TagihanActivity) getActivity();
//            fca.switchActivity(mIntent,j);
//        }
    }

//    private void switchTagihanFragment(Fragment i, String name, Boolean isBackstack){
//        if (getActivity() == null)
//            return;
//
//        hiddenKeyboard(getView());
//        TagihanActivity fca = (TagihanActivity) getActivity();
//        fca.switchContent(i, name, isBackstack);
//    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack) {
        if (getActivity() == null)
            return;

        hiddenKeyboard(getView());

        if (getActivity() instanceof TopUpActivity) {
            TopUpActivity fca = (TopUpActivity) getActivity();
            fca.switchContent(i, name, isBackstack);
        }
//        else if(getActivity() instanceof TagihanActivity){
//            TagihanActivity fca = (TagihanActivity) getActivity();
//            fca.switchContent(i, name, isBackstack);
//        }
    }

    private void hiddenKeyboard(View v) {
        if (this.isAdded()) {
            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public boolean inputValidation() {
        if (jumlahSGO_value.getText().toString().length() == 0) {
            jumlahSGO_value.requestFocus();
            jumlahSGO_value.setError(this.getString(R.string.sgoplus_validation_jumlahSGOplus));
            return false;
        } else if (Long.parseLong(jumlahSGO_value.getText().toString()) < 1) {
            jumlahSGO_value.requestFocus();
            jumlahSGO_value.setError(getString(R.string.payfriends_amount_zero));
            return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isTagihan)
            inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
            case R.id.action_information:
                if (!dialogI.isAdded())
                    dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (smsDialog != null) {
            smsDialog.DestroyDialog();
            if (this.isVisible())
                smsDialog.dismiss();
        }
    }
    SecurePreferences getSP(){
        if (sp == null)
            sp = CustomSecurePref.getInstance().getmSecurePrefs();
        return sp;
    }
    boolean checkFailedVerify(){
        String temp_iccid = getSP().getString(DefineValue.TEMP_ICCID, "");
        String temp_imei = getSP().getString(DefineValue.TEMP_IMEI, "");
        boolean temp_is_sent = getSP().getBoolean(DefineValue.TEMP_IS_SENT, false);

        if(!temp_iccid.equals("") && !temp_imei.equals("")){
            String diccid = smsclass.getDeviceICCID();
            String dimei = smsclass.getDeviceIMEI();
            boolean biccid = diccid.equalsIgnoreCase(temp_iccid);
            boolean bimei = dimei.equalsIgnoreCase(temp_imei);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", new Locale("ID","INDONESIA"));
            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.SECOND, 10);


            boolean ddate = false;
            try {
                Date savedDate, currDate = Calendar.getInstance().getTime();
                savedDate = df.parse(getSP().getString(DefineValue.LAST_SMS_SENT, ""));
//                currDate = df.parse(getSP().getString(DefineValue.LAST_SMS_SENT, ""));

                currDate.setTime(cal.getTimeInMillis());

                Long a = currDate.getTime(), b = savedDate.getTime();
                Long calc = a - b;

//                ddate = currDate.compareTo(savedDate) > 0;
                Long sec = TimeUnit.MILLISECONDS.toMinutes(calc);
                ddate = sec < 30;
            } catch (ParseException e) {
                e.printStackTrace();
            }




            return biccid && bimei && temp_is_sent && ddate;
        }else return false;
    }
}