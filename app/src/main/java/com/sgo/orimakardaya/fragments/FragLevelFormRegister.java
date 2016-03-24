package com.sgo.orimakardaya.fragments;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.CountryModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.InetHandler;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
import com.sgo.orimakardaya.dialogs.MessageDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/*
  Created by Administrator on 11/19/2015.
 */
public class FragLevelFormRegister extends Fragment {

    View v;
    EditText et_socialid;
    EditText et_address;
    EditText et_pob;
    EditText et_name;
//    EditText et_bom;
    EditText et_email;
    TextView tv_dob;
    String dedate = "",userID,accessKey,custID,contactCenter,listContactPhone = "", listAddress = "";
    Spinner sp_socialid,sp_country, sp_gender;
    DatePickerDialog dpd;
    SecurePreferences sp;
    DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
    DateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID","INDONESIA"));
    ProgressDialog progdialog;
    String[] gender_value= new String[]{"L","P"};
    Calendar nowCalendar;
    ArrayList<String> dataCountry = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_level_form_register, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        custID = sp.getString(DefineValue.CUST_ID,"");
        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER,"");

        try {
            JSONArray arrayContact = new JSONArray(contactCenter);
            for(int i=0 ; i<arrayContact.length() ; i++) {
//                String contactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
//                if(i == arrayContact.length()-1) {
//                    listContactPhone += contactPhone;
//                }
//                else {
//                    listContactPhone += contactPhone + " atau ";
//                }

                if(i == 0) {
                    listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                    listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        et_socialid = (EditText) v.findViewById(R.id.level_value_social_id);
        et_address = (EditText) v.findViewById(R.id.level_value_address);
        et_pob = (EditText) v.findViewById(R.id.level_value_pob);
        tv_dob = (TextView) v.findViewById(R.id.level_value_bod);
        tv_dob.setEnabled(false);
        et_name = (EditText) v.findViewById(R.id.level_value_name);
//        et_bom = (EditText) v.findViewById(R.id.level_value_birth_mother);
        et_email = (EditText) v.findViewById(R.id.level_value_email);

        Button btnproses = (Button) v.findViewById(R.id.btn_submit_level_register);
        Button btncancel = (Button) v.findViewById(R.id.btn_cancel_level_register);


        btnproses.setOnClickListener(prosesListener);

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        nowCalendar = Calendar.getInstance();
        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                nowCalendar.get(Calendar.YEAR),
                nowCalendar.get(Calendar.MONTH),
                nowCalendar.get(Calendar.DAY_OF_MONTH)
        );
        tv_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
            }
        });


        sp_socialid = (Spinner) v.findViewById(R.id.level_spinner_socialid_type);

        sp_gender = (Spinner) v.findViewById(R.id.level_spinner_gender);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gender_type, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_gender.setAdapter(genderAdapter);

        sp_country = (Spinner) v.findViewById(R.id.level_spinner_country);


        Thread deproses = new Thread(){
            @Override
            public void run() {
                dataCountry.add(getString(R.string.myprofile_spinner_default));
                dataCountry.add(CountryModel.Indonesia);
                dataCountry.addAll(Arrays.asList(CountryModel.allCountry));
                final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataCountry);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_country.setAdapter(adapter2);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter2.notifyDataSetChanged();
                    }
                });

            }
        };
        deproses.run();

//        sentInqCust();
        try {
            FillLayoutData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    Button.OnClickListener prosesListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(InetHandler.isNetworkAvailable(getActivity())) {
                if (inputValidation()) {
                    sentExecCust();
                }
            }
            else DefinedDialog.showErrorDialog(getActivity(), getString(R.string.inethandler_dialog_message));
        }
    };


    DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
            nowCalendar.set(year,(monthOfYear),dayOfMonth);
            tv_dob.setText(dedate);

        }
    };


    public void FillLayoutData() throws JSONException {
        et_socialid.setText(sp.getString(DefineValue.PROFILE_SOCIAL_ID,""));
        et_address.setText(sp.getString(DefineValue.PROFILE_ADDRESS,""));
        et_pob.setText(sp.getString(DefineValue.PROFILE_POB,""));
        et_name.setText(sp.getString(DefineValue.PROFILE_FULL_NAME,""));
//        et_bom.setText(sp.getString(DefineValue.PROFILE_BOM,""));
//        et_email.setText(response.optString(WebParams.CUST_EMAIL,""));
        et_email.setText(sp.getString(DefineValue.PROFILE_EMAIL,""));

        String _tempData = sp.getString(DefineValue.PROFILE_DOB,"");
        if(!_tempData.equals("")){
            Date _date;
            try {
                _date = fromFormat.parse(_tempData);
                dedate = toFormat.format(_date);
                nowCalendar.setTime(_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
            dpd = DatePickerDialog.newInstance(dobPickerSetListener,
                    nowCalendar.get(Calendar.YEAR),
                    nowCalendar.get(Calendar.MONTH),
                    nowCalendar.get(Calendar.DAY_OF_MONTH));
        }


        String temp_list_id = sp.getString(DefineValue.LIST_ID_TYPES,"");
        if(!temp_list_id.isEmpty()) {
            JSONArray mData = new JSONArray(temp_list_id);

            String[] dataSpinnerSocialID = new String[mData.length()];

            for (int i = 0; i < mData.length(); i++) {
                dataSpinnerSocialID[i] = mData.getJSONObject(i).getString(WebParams.TYPE);
            }

            ArrayAdapter<String> socialidAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataSpinnerSocialID);
            socialidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_socialid.setAdapter(socialidAdapter);


            _tempData = sp.getString(DefineValue.PROFILE_ID_TYPE, "");
            if (!_tempData.equals("")) {
                sp_socialid.setSelection(socialidAdapter.getPosition(_tempData));
            }
        }

        _tempData = sp.getString(DefineValue.PROFILE_GENDER,"");
        if(!_tempData.isEmpty()){
            if(_tempData.equalsIgnoreCase(gender_value[0]))
                sp_gender.setSelection(0);
            else
                sp_gender.setSelection(1);
        }

        final String _country = sp.getString(DefineValue.PROFILE_COUNTRY,"");
        if(!_country.isEmpty()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0 ; i< dataCountry.size();i++){
                        if(dataCountry.get(i).equalsIgnoreCase(_country)){
                            sp_country.setSelection(i,false);
                            break;
                        }
                    }
                }
            });
        }

    }


    public void sentExecCust(){
        try{

            if(progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            else
                progdialog.show();

            final RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_EXEC_CUST,
                    userID, accessKey);
            params.put(WebParams.CUST_ID, custID);
            params.put(WebParams.CUST_NAME, et_name.getText().toString());
            params.put(WebParams.CUST_ID_TYPE, sp_socialid.getSelectedItem().toString());
            params.put(WebParams.CUST_ID_NUMBER, et_socialid.getText().toString());
            params.put(WebParams.CUST_ADDRESS, et_address.getText().toString());
            params.put(WebParams.CUST_COUNTRY, sp_country.getSelectedItem().toString());
            params.put(WebParams.CUST_BIRTH_PLACE, et_pob.getText().toString());
            params.put(WebParams.CUST_MOTHER_NAME, sp.getString(DefineValue.PROFILE_BOM, ""));
            params.put(WebParams.CUST_CONTACT_EMAIL, et_email.getText().toString());
            params.put(WebParams.IS_REGISTER, "Y");

            final String dob = nowCalendar.get(Calendar.YEAR)+"-"+ (nowCalendar.get(Calendar.MONTH)+1) +"-"+nowCalendar.get(Calendar.DAY_OF_MONTH);
            params.put(WebParams.CUST_BIRTH_DATE,dob);

            final String gender;
            if(sp_gender.getSelectedItemPosition()==0)
                gender = gender_value[0];
            else
                gender = gender_value[1];
            params.put(WebParams.CUST_GENDER,gender);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params execute customer:" + params.toString());

            MyApiClient.sentExecCust(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response execute customer:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            SecurePreferences.Editor mEdit = sp.edit();
                            int member_level = sp.getInt(DefineValue.LEVEL_VALUE,1);
                            mEdit.putBoolean(DefineValue.IS_REGISTERED_LEVEL,true);
                            mEdit.putString(DefineValue.PROFILE_DOB, dob);
                            mEdit.putString(DefineValue.PROFILE_ADDRESS,et_address.getText().toString());
                            mEdit.putString(DefineValue.PROFILE_COUNTRY,sp_country.getSelectedItem().toString());
                            mEdit.putString(DefineValue.PROFILE_SOCIAL_ID,et_socialid.getText().toString());
                            mEdit.putString(DefineValue.PROFILE_FULL_NAME,et_name.getText().toString());
                            mEdit.putString(DefineValue.CUST_NAME,et_name.getText().toString());
                            mEdit.putString(DefineValue.USER_NAME,et_name.getText().toString());
                            mEdit.putString(DefineValue.MEMBER_NAME, et_name.getText().toString());
                            mEdit.putString(DefineValue.PROFILE_POB, et_pob.getText().toString());
                            mEdit.putInt(DefineValue.LEVEL_VALUE, response.optInt(WebParams.MEMBER_LEVEL, 1));
                            if (response.optString(WebParams.ALLOW_MEMBER_LEVEL, DefineValue.STRING_NO).equals(DefineValue.STRING_YES))
                                mEdit.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL,true );
                            else
                                mEdit.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL,false );
//                        mEditor.putString(DefineValue.CAN_TRANSFER,arrayJson.getJSONObject(i).optString(WebParams.CAN_TRANSFER, DefineValue.STRING_NO));
                            mEdit.putString(DefineValue.PROFILE_GENDER, gender);
                            mEdit.putString(DefineValue.PROFILE_ID_TYPE,sp_socialid.getSelectedItem().toString());

                            mEdit.apply();


                            if(response.optInt(WebParams.MEMBER_LEVEL, 1) == 2){
                                Toast.makeText(getActivity(),getString(R.string.level_dialog_finish_message_auto),Toast.LENGTH_LONG).show();
                                FragFriendsViewDetail.successUpgrade = true;
                                getActivity().setResult(MainPage.RESULT_REFRESH_NAVDRAW);
                                getActivity().finish();
                            }
                            else {
                                MessageDialog dialognya = new MessageDialog(getActivity(), getString(R.string.level_dialog_finish_title),
                                        getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                                        getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone);
                                dialognya.setDialogButtonClickListener(new MessageDialog.DialogButtonListener() {
                                    @Override
                                    public void onClickButton(View v, boolean isLongClick) {
                                        FragFriendsViewDetail.successUpgrade = true;
                                        getActivity().setResult(MainPage.RESULT_REFRESH_NAVDRAW);
                                        getActivity().finish();
                                    }
                                });
                                dialognya.show();
                            }



                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(getActivity(), message);
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                            getFragmentManager().popBackStack();

                        }
                        if (progdialog.isShowing())
                            progdialog.dismiss();
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
                    getFragmentManager().popBackStack();
                    Timber.w("Error Koneksi exec customer level req:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    public boolean inputValidation(){
        if(et_name.getText().toString().length()==0){
            et_name.requestFocus();
            et_name.setError(this.getString(R.string.myprofile_validation_name));
            return false;
        }
        else if(et_email.getText().toString().length()==0){
            et_email.requestFocus();
            et_email.setError(this.getString(R.string.myprofile_validation_email));
            return false;
        }
        else if(et_socialid.getText().toString().length()== 0){
            et_socialid.requestFocus();
            et_socialid.setError(this.getString(R.string.myprofile_validation_socialid));
            return false;
        }
        else if(et_address.getText().toString().length()==0){
            et_address.requestFocus();
            et_address.setError(this.getString(R.string.myprofile_validation_address));
            return false;
        }
        else if(et_pob.getText().toString().length()==0){
            et_pob.requestFocus();
            et_pob.setError(this.getString(R.string.myprofile_validation_pob));
            return false;
        }
//        else if(et_bom.getText().toString().length()==0){
//            et_bom.requestFocus();
//            et_bom.setError(this.getString(R.string.myprofile_validation_bom));
//            return false;
//        }
        else if(dedate == null || dedate.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}