package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.RegisterSMSBankingActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by thinkpad on 6/11/2015.
 */
public class FragRegisterSMSBanking extends BaseFragment {

    private View v;
    private View layout_dll;

    private listBankModel mLB;
    private ArrayList<String> bankName;
    private ProgressDialog progdialog;
    private EditText etPhone;
    private EditText etAccNo;
    private Spinner spinBankName;
    private TextView tvDOB;
    private Button btnRegister;

    private String dedate = null;
    private String date_dob = null;
    private String custID;
    private String bank_name;

    private DateFormat fromFormat;
    private DateFormat toFormat2;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        custID = sp.getString(DefineValue.CUST_ID,"");
        bank_name = getArguments().getString(DefineValue.BANK_NAME,"");

        etPhone = v.findViewById(R.id.rsb_value_phone);
        etAccNo =  v.findViewById(R.id.rsb_value_acc_no);
        tvDOB = v.findViewById(R.id.rsb_value_dob);
        btnRegister = v.findViewById(R.id.btn_register);
        spinBankName = v.findViewById(R.id.spinner_nameBank);
        layout_dll =  v.findViewById(R.id.layout_dll);

        tvDOB.setOnClickListener(textDOBListener);
        btnRegister.setOnClickListener(btnGetTokenListener);

        fromFormat = new SimpleDateFormat("yyyy-MM-dd");
        toFormat2 = new SimpleDateFormat("dd-M-yyyy");

        bankName = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, bankName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBankName.setAdapter(adapter);
        spinBankName.setOnItemSelectedListener(spinnerNamaBankListener);

        getBankList();
    }


    private Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {

            Object item = adapterView.getItemAtPosition(i);
            Timber.d("isi bank name:" + item.toString());
            if(item.toString().toLowerCase().contains("mandiri")) {
                Timber.d("masuk layout  visible");
                layout_dll.setVisibility(View.VISIBLE);
            }
            else layout_dll.setVisibility(View.GONE);

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_register_sms_banking, container, false);
        return v;
    }

    private void getBankList(){
        try{
            if(progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(getActivity(),"");
            else
                progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_BANK_SMS_REGIST, memberIDLogin);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.TYPE, DefineValue.BANKLIST_TYPE_SMS);
            params.put(WebParams.USER_ID, userPhoneID);

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_BANK_SMS_REGIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("response Listbank sms regist:"+response.toString());

                                    JSONArray bank_data = new JSONArray(response.optString(WebParams.BANK_DATA,""));

                                    if(bank_data.length() > 0){

                                        insertBankList(bank_data);
                                    }
                                    else {
                                        Toast.makeText(getActivity(),getString(R.string.data_not_found),Toast.LENGTH_SHORT).show();
                                        getActivity().finish();
                                    }
                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    Timber.d("isi response autologout:"+response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(),message);
                                }
                                else {
                                    Timber.d("Error ListMember comlist:"+response.toString());
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if (isVisible() || progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }


    private void insertBankList(JSONArray arrayJson){
        try {

            mLB = new listBankModel();
            for (int i = 0; i < arrayJson.length(); i++) {

                String temp_bank_name = arrayJson.getJSONObject(i).getString(WebParams.BANK_NAME);

                mLB.setBank_name(arrayJson.getJSONObject(i).getString(WebParams.BANK_NAME));
                mLB.setBank_code(arrayJson.getJSONObject(i).getString(WebParams.BANK_CODE));
                mLB.setProduct_code(arrayJson.getJSONObject(i).getString(WebParams.PRODUCT_CODE));
                mLB.setProduct_name(arrayJson.getJSONObject(i).getString(WebParams.PRODUCT_NAME));
                mLB.setProduct_type(arrayJson.getJSONObject(i).getString(WebParams.PRODUCT_TYPE));
                mLB.setProduct_h2h(arrayJson.getJSONObject(i).optString(WebParams.PRODUCT_H2H, ""));
                if(bank_name.isEmpty()) {
                    bankName.add(mLB.getBank_name());
                }
                else if(temp_bank_name.equals(bank_name)){
                    bankName.add(mLB.getBank_name());
                    break;
                }

            }

            ((ArrayAdapter<String>)spinBankName.getAdapter()).notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private TextView.OnClickListener textDOBListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
        }
    };

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
            Timber.d("masuk date picker dob");
            try {
                date_dob = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob:"+date_dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvDOB.setText(dedate);
        }
    };

    private Button.OnClickListener btnGetTokenListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    String bankName = spinBankName.getSelectedItem().toString();

                    if (bankName.toLowerCase().contains("jatim"))
                        sentInquiryMobileJTM(bankName);
                    else
                        getDataSB();
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };

    private void sentInquiryMobileJTM(final String _bank_name){
        try{
            if(progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(getActivity(),"");
            else
                progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_MOBILE_JATIM);
            params.put(WebParams.NO_HP, etPhone.getText().toString());
            params.put(WebParams.CUST_ID, custID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get BankList:"+params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INQUIRY_MOBILE_JATIM, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("response Listbank:" + response.toString());

                                    showDialog(_bank_name, response.optString(WebParams.NO_HP, ""), response.optString(WebParams.TOKEN_ID, ""));
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(), message);
                                } else {
                                    Timber.d("Error ListMember comlist:" + response.toString());
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
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
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void getDataSB() {
        try {
            if(progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(getActivity(),"");
            else
                progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_MOBILE);
            params.put(WebParams.NO_HP, NoHPFormat.formatTo62(etPhone.getText().toString()) );
            params.put(WebParams.TGL_LAHIR, tvDOB.getText().toString());
            params.put(WebParams.CUST_ID, custID);
            params.put(WebParams.ACCT_NO, etAccNo.getText().toString());
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params data SB:"+params.toString());


            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_INQUIRY_MOBILE, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    String acc_no = "";
                                    String acc_name = "";

                                    String tgl_lahir = response.getString(WebParams.TGL_LAHIR);
                                    String no_hp = response.getString(WebParams.NO_HP);
                                    String ccy_id = response.getString(WebParams.CCY_ID);
                                    JSONArray account_data = new JSONArray(response.optString(WebParams.ACCOUNT_DATA, ""));

                                    boolean flagDOB;
                                    boolean flagAccNo = false;

                                    flagDOB = date_dob.equalsIgnoreCase(tgl_lahir);

                                    for (int i = 0; i < account_data.length(); i++) {
                                        JSONObject mObj = account_data.getJSONObject(i);

                                        acc_no = mObj.optString(WebParams.ACCOUNT_NO, "");
                                        acc_name = mObj.optString(WebParams.ACCOUNT_NAME, "");

                                        if (etAccNo.getText().toString().equalsIgnoreCase(acc_no)) {
                                            flagAccNo = true;
                                            break;
                                        } else {
                                            flagAccNo = false;
                                        }
                                    }

                                    if (flagDOB && flagAccNo) {
                                        Bundle args = new Bundle();
                                        args.putString(WebParams.ACCT_NO, acc_no);
                                        args.putString(WebParams.ACCT_NAME, acc_name);
                                        args.putString(WebParams.TGL_LAHIR, tgl_lahir);
                                        args.putString(WebParams.NO_HP, no_hp);
                                        args.putString(WebParams.CCY_ID, ccy_id);

                                        Fragment f = new FragRegisterSMSBankingConfirm();
                                        f.setArguments(args);
                                        switchFragment(f, getResources().getString(R.string.title_register_sms_banking), false);
                                    } else if (!flagDOB) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Alert").setMessage(getResources().getString(R.string.rsb_alert_dob))
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });

                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    } else if (!flagAccNo) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Alert").setMessage(getResources().getString(R.string.rsb_alert_acc_no))
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });

                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }

                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    Timber.d("isi response autologout:"+response.toString());
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(getActivity(),message);
                                }
                                else {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
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
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private void showDialog(final String _bank_name, final String _no_hp, final String _token_id) {
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
        Message.setText(getResources().getString(R.string.dialog_token_message_sms));


        btnDialogOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f = new FragRegisterSMSBankingConfirm();
                Bundle mBun = new Bundle();
                mBun.putString(DefineValue.USERID_PHONE, _no_hp);
                mBun.putString(DefineValue.TOKEN, _token_id);
                mBun.putString(DefineValue.BANK_NAME, _bank_name);
                f.setArguments(mBun);
                switchFragment(f, getResources().getString(R.string.title_register_sms_banking), false);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean inputValidation() {
        if (etPhone.getText().toString().length() == 0 || etPhone.getText().toString().equals("")) {
            etPhone.requestFocus();
            etPhone.setError(getString(R.string.regist1_validation_nohp));
            return false;
        }
        else if(layout_dll.getVisibility() == View.VISIBLE){
            if (tvDOB.getText().toString().equals(getResources().getString(R.string.rsb_hint_dob))) {
                Toast.makeText(getActivity(), "Date of Birth required!", Toast.LENGTH_LONG).show();
                return false;
            }
            if(etAccNo.getText().toString().length() == 0 || etAccNo.getText().toString().equals("")) {
                etAccNo.requestFocus();
                etAccNo.setError(getString(R.string.rsb_validation_acc_no));
                return false;
            }
        }

        return true;
    }

    private void switchFragment(android.support.v4.app.Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        RegisterSMSBankingActivity fca = (RegisterSMSBankingActivity) getActivity();
        fca.switchContent(i, name, isBackstack);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
