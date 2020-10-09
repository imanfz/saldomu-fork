package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CashoutActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.BankCashoutAdapter;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.BankCashoutModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.utils.NumberTextWatcherForThousand;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by thinkpad on 3/18/2015.
 */
public class FragCashOut extends BaseFragment {
    public final static String TAG = "com.sgo.indonesiakoe.fragments.FragCashout";

    View v;
    LinearLayout layout_acc_name;
    Spinner sp_privacy, sp_bank;
    EditText etAccNo, etNominal, etAccName;
    TextView txtBalance;
    Button btnProcess;
    ProgressDialog progdialog;

    int privacy, start = 0;
    String balance, bankCashout, bankCode="", bankName, bankGateway;
    ArrayList<String> arrBankName;
    ArrayList<String> arrBankCode;
    ArrayList<String> arrBankGateway;
    boolean isBankGateway = false;
    List<BankCashoutModel> listBankCashOut = new ArrayList<>();
    BankCashoutAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(start > 0) {
            Timer buttonTimer = new Timer();
            buttonTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if(FragCashOut.this.isVisible()) {
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (FragCashOut.this.isVisible()) {
                                    balance = MyApiClient.CCY_VALUE + " " + CurrencyFormat.format(sp.getString(DefineValue.BALANCE_AMOUNT, ""));
                                    Timber.d("refresh balance:"+balance);
                                    txtBalance.setText(balance);
                                }
                            }
                        });
                    }
                }
            }, 2000);
        }
        start++;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_cash_out, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        balance = MyApiClient.CCY_VALUE + " " +CurrencyFormat.format(sp.getString(DefineValue.BALANCE_AMOUNT,""));
        bankCashout = sp.getString(DefineValue.BANK_CASHOUT,"");

        initializeBankCashout();

        layout_acc_name = v.findViewById(R.id.layout_bankcashout_acc_name);
        etAccName = v.findViewById(R.id.cashout_value_bank_acc_name);
        etAccNo = v.findViewById(R.id.cashout_value_bank_acc_no);
        etNominal = v.findViewById(R.id.cashout_value_nominal);
        txtBalance = v.findViewById(R.id.cashout_balance);
        sp_privacy = v.findViewById(R.id.cashout_privacy_spinner);
        sp_bank = v.findViewById(R.id.cashout_spinner_nameBank);
        btnProcess = v.findViewById(R.id.cashout_btn_process);

        etNominal.addTextChangedListener(new NumberTextWatcherForThousand(etNominal));

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

//        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listBankCashOut);
        adapter = new BankCashoutAdapter(getActivity(), android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_bank.setAdapter(adapter);
        sp_bank.setOnItemSelectedListener(spinnerNamaBankListener);

        txtBalance.setText(balance);

        btnProcess.setOnClickListener(btnProcessListener);

        getBankCashout();
    }

    Button.OnClickListener btnProcessListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {

                String accNo = etAccNo.getText().toString();
                String nominal = NumberTextWatcherForThousand.trimCommaOfString(etNominal.getText().toString());
                String accName = etAccName.getText().toString();

                if (inputValidation()) {
                    reqCashout(accNo, nominal, accName);
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    Spinner.OnItemSelectedListener spinnerPrivacy = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            privacy = i+1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {

            BankCashoutModel model = listBankCashOut.get(i);
//            if (model.getBank_gateway().equalsIgnoreCase("Y")) {
//                layout_acc_name.setVisibility(View.GONE);
//            } else {
//                layout_acc_name.setVisibility(View.VISIBLE);
//            }


//            Object item = adapterView.getItemAtPosition(i);
            bankCode = model.getBank_code();
            bankName = model.getBank_name().toString();
//            if(!arrBankGateway.isEmpty()) bankGateway = arrBankGateway.get(i);
            Timber.d("isi bank name cashout:"+model.toString() + bankCode + bankGateway);

//            if(isBankGateway) {
//                if (bankGateway.equalsIgnoreCase("Y")) {
//                    layout_acc_name.setVisibility(View.GONE);
//                } else if (bankGateway.equalsIgnoreCase("N")) {
//                    layout_acc_name.setVisibility(View.VISIBLE);
//                }
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public void reqCashout(final String _acctNo, final String _amount, final String _accName){
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            extraSignature = memberIDLogin+bankCode+_acctNo+MyApiClient.CCY_VALUE+_amount;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQUEST_CASHOUT, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.AMOUNT, _amount);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.BANK_CODE, bankCode);
            params.put(WebParams.ACCT_NO, _acctNo);
            params.put(WebParams.IS_TABUNG, DefineValue.STRING_NO);

//            params.put(WebParams.PRIVACY, privacy);
            if(isBankGateway) {
                if (bankGateway.equalsIgnoreCase("N")) {
                    params.put(WebParams.ACCT_NAME, _accName);
                }
            }

            Timber.d("isi params req cashout:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQUEST_CASHOUT, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("isi response req cashout:"+response.toString());

                                    sp_bank.setSelection(0);
                                    etAccNo.setText("");
                                    etNominal.setText("");
                                    etAccName.setText("");
                                    sp_privacy.setSelection(0);

                                    String tx_id = response.getString(WebParams.TX_ID);
                                    String acct_name = response.getString(WebParams.ACCT_NAME);
                                    String ccyId = response.getString(WebParams.CCY_ID);
                                    String fee = response.getString(WebParams.FEE);
                                    String total = response.getString(WebParams.TOTAL);

                                    Fragment i = new FragCashoutConfirm();
                                    Bundle args = new Bundle();
                                    args.putString(DefineValue.TX_ID, tx_id);
                                    args.putString(DefineValue.BANK_NAME, bankName);
                                    args.putString(DefineValue.ACCOUNT_NUMBER, _acctNo);
                                    args.putString(DefineValue.CCY_ID, ccyId);
                                    args.putString(DefineValue.NOMINAL, _amount);
                                    args.putString(DefineValue.ACCT_NAME, acct_name);
                                    args.putString(DefineValue.FEE, fee);
                                    args.putString(DefineValue.TOTAL_AMOUNT, total);
                                    i.setArguments(args);
                                    switchContent(i,FragCashoutConfirm.TAG);

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:"+response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginMain(getActivity(), message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + response.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                }else {
                                    Timber.d("isi error req cashout:"+response.toString());
                                    String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage(code_msg)
                                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError(Throwable e) {
                            if(MyApiClient.PROD_FAILURE_FLAG)
                                Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public void initializeBankCashout(){
        CashoutActivity fca = (CashoutActivity) getActivity();
        fca.setTitleToolbar(getString(R.string.title_cashout_bank));

        arrBankName = new ArrayList<>();
        arrBankCode = new ArrayList<>();
        arrBankGateway = new ArrayList<>();
        try {
            JSONArray arrbank = new JSONArray(bankCashout);
            for(int i=0 ; i<arrbank.length() ; i++){
                arrBankCode.add(arrbank.getJSONObject(i).getString(WebParams.BANK_CODE));
                arrBankName.add(arrbank.getJSONObject(i).getString(WebParams.BANK_NAME));
                if(arrbank.getJSONObject(i).has(WebParams.BANK_GATEWAY)) {
                    isBankGateway = true;
                    arrBankGateway.add(arrbank.getJSONObject(i).getString(WebParams.BANK_GATEWAY));
                }
                else isBankGateway = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean inputValidation(){
        if(bankCode.isEmpty()){
            sp_bank.requestFocus();
            Toast.makeText(getActivity(),getString(R.string.cashout_validation_bank),Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(etAccNo.getText().toString().length()==0){
            etAccNo.requestFocus();
            etAccNo.setError(getString(R.string.cashout_accno_validation));
            return false;
        }
        else if(isBankGateway) {
            if (bankGateway.equalsIgnoreCase("N")) {
                if (etAccName.getText().toString().length() == 0) {
                    etAccName.requestFocus();
                    etAccName.setError(getString(R.string.cashout_accname_validation));
                    return false;
                }
            }
        }
        if(etNominal.getText().toString().length() == 0 ){
            etNominal.requestFocus();
            etNominal.setError(getString(R.string.cashout_nominal_validation));
            return false;
        }
        else if(Long.parseLong(etNominal.getText().toString()) < 1){
            etNominal.requestFocus();
            etNominal.setError(getString(R.string.cashout_nominal_zero));
            return false;
        }
        return true;
    }
    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        CashoutActivity fca = (CashoutActivity) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    private void switchContent(Fragment mFrag, String tag){
        if (getActivity() == null)
            return;

        CashoutActivity fca = (CashoutActivity) getActivity();
        fca.switchContent(mFrag, getString(R.string.menu_item_title_cash_out), true, tag);
    }

    public void getBankCashout(){
        try {
            if (isAdded() || isVisible()) {
                final ProgressDialog prodDialog = DefinedDialog.CreateProgressDialog(getActivity(), "");

                HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BANKCASHOUT, memberIDLogin);
                params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
                params.put(WebParams.MEMBER_ID, memberIDLogin );
                params.put(WebParams.USER_ID, userPhoneID);

                Timber.d("isi params get Bank cashout:" + params.toString());

                RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BANKCASHOUT, params,
                        new ResponseListener() {
                            @Override
                            public void onResponses(JsonObject object) {

                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(object.toString(), jsonModel.class);

                                Log.e("getBankCashout", object.get("bank_cashout").toString());

                                Type type = new TypeToken<List<BankCashoutModel>>() {}.getType();
                                Gson gson2 = new Gson();
                                listBankCashOut = gson2.fromJson(object.get("bank_cashout"), type);

                                Log.e("getBankCashout", listBankCashOut.toString());

                                adapter.updateAdapter(listBankCashOut);

                                if (object.get("error_code").equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:" + model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                    alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (object.get("error_code").equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:" + object.toString());
                                    AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                    alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                if (prodDialog.isShowing())
                                    prodDialog.dismiss();
                            }
                        });
            }
        }catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }
}