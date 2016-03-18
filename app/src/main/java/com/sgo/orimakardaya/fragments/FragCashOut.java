package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.NominalAdapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by thinkpad on 3/18/2015.
 */
public class FragCashOut extends Fragment {

    View v;
    SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
    Spinner sp_privacy, sp_bank;
    EditText etAccNo, etNominal;
    TextView txtBalance;
    Button btnProcess;
    ProgressDialog progdialog;

    int privacy, start = 0;
    String userID, accessKey, memberId, balance, bankCashout, bankCode, bankName;
    ArrayList<String> arrBankName;
    ArrayList<String> arrBankCode;

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
                                    balance = MyApiClient.CCY_VALUE + " " + sp.getString(DefineValue.BALANCE, "");
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

        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        memberId = sp.getString(DefineValue.MEMBER_ID,"");
        balance = MyApiClient.CCY_VALUE + " " +sp.getString(DefineValue.BALANCE,"");
        bankCashout = sp.getString(DefineValue.BANK_CASHOUT,"");

        initializeBankCashout();

        etAccNo = (EditText) v.findViewById(R.id.cashout_value_bank_acc_no);
        etNominal = (EditText) v.findViewById(R.id.cashout_value_nominal);
        txtBalance = (TextView) v.findViewById(R.id.cashout_balance);
        sp_privacy = (Spinner) v.findViewById(R.id.cashout_privacy_spinner);
        sp_bank = (Spinner) v.findViewById(R.id.cashout_spinner_nameBank);
        btnProcess = (Button) v.findViewById(R.id.cashout_btn_process);

        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.privacy_list, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_privacy.setAdapter(spinAdapter);
        sp_privacy.setOnItemSelectedListener(spinnerPrivacy);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrBankName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_bank.setAdapter(adapter);
        sp_bank.setOnItemSelectedListener(spinnerNamaBankListener);

        txtBalance.setText(balance);

        btnProcess.setOnClickListener(btnProcessListener);
    }

    Button.OnClickListener btnProcessListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {

                String accNo = etAccNo.getText().toString();
                String nominal = etNominal.getText().toString();

                if (inputValidation()) {
                    reqCashout(accNo, nominal);
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

            Object item = adapterView.getItemAtPosition(i);
            bankCode = arrBankCode.get(i);
            bankName = item.toString();
            Timber.d("isi bank name cashout:"+item.toString() + bankCode);
//            if(item.toString().toLowerCase().contains("mandiri")) {
//
//            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public void reqCashout(final String _acctNo, final String _amount){
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_REQUEST_CASHOUT,
                    userID,accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.MEMBER_ID, memberId);
            params.put(WebParams.AMOUNT, _amount);
            params.put(WebParams.CCY_ID, MyApiClient.CCY_VALUE);
            params.put(WebParams.BANK_CODE, bankCode);
            params.put(WebParams.ACCT_NO, _acctNo);
//            params.put(WebParams.PRIVACY, privacy);

            Timber.d("isi params req cashout:" + params.toString());

            MyApiClient.sentReqCashout(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi response req cashout:"+response.toString());

                            sp_bank.setSelection(0);
                            etAccNo.setText("");
                            etNominal.setText("");
                            sp_privacy.setSelection(0);

                            String tx_id = response.getString(WebParams.TX_ID);
                            String acct_name = response.getString(WebParams.ACCT_NAME);
                            String ccyId = response.getString(WebParams.CCY_ID);
                            String fee = response.getString(WebParams.FEE);
                            String total = response.getString(WebParams.TOTAL);

//                            Intent i = new Intent(getActivity(), CashoutActivity.class);
//                            i.putExtra(DefineValue.TX_ID, tx_id);
//                            i.putExtra(DefineValue.BANK_NAME, bankName);
//                            i.putExtra(DefineValue.ACCOUNT_NUMBER, _acctNo);
//                            i.putExtra(DefineValue.CCY_ID, ccyId);
//                            i.putExtra(DefineValue.NOMINAL, _amount);
//                            i.putExtra(DefineValue.ACCT_NAME, acct_name);
//                            i.putExtra(DefineValue.FEE, fee);
//                            i.putExtra(DefineValue.TOTAL_AMOUNT, total);
//                            switchActivity(i);

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
                            switchContent(i);

                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(), message);
                        } else {
                            Timber.d("isi error req cashout:"+response.toString());
                            String code_msg = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(code_msg)
                                    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
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

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi req cash out cashout:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    public void initializeBankCashout(){

        arrBankName = new ArrayList<>();
        arrBankCode = new ArrayList<>();
        try {
            JSONArray arrbank = new JSONArray(bankCashout);
            for(int i=0 ; i<arrbank.length() ; i++){
                arrBankCode.add(arrbank.getJSONObject(i).getString(WebParams.BANK_CODE));
                arrBankName.add(arrbank.getJSONObject(i).getString(WebParams.BANK_NAME));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean inputValidation(){
        if(etAccNo.getText().toString().length()==0){
            etAccNo.requestFocus();
            etAccNo.setError(getString(R.string.cashout_accno_validation));
            return false;
        }
        if(etNominal.getText().toString().length() == 0){
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

    private void switchContent(Fragment mFrag){
        if (getActivity() == null)
            return;

        CashoutActivity fca = (CashoutActivity) getActivity();
        fca.switchContent(mFrag, getString(R.string.menu_item_title_cash_out), true);
    }
}
