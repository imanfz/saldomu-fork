package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 10/23/2017.
 */

public class MyProfileNewActivity extends BaseActivity {
    private final int KTP_TYPE = 1;
    private final int SELFIE_TYPE = 2;
    private final int TTD_TYPE = 3;


    private SecurePreferences sp;
    TextView tv_dob, tv_pb1, tv_pb2, tv_pb3, tv_verified_member, tv_respon_reject_KTP, tv_respon_reject_selfie, tv_respon_reject_ttd;
    LinearLayout dataMemberBasic , dataVerifiedMember;
    RelativeLayout layoutKTP, layoutSelfie, layoutTTD;
    EditText et_nama, et_noHp, et_email;
    private String userID;
    private String accessKey;
    private ProgressBar pb1, pb2, pb3;
    private ImageButton cameraKTP, selfieKTP, cameraTTD;
    private Button btn1, btn2;
    private LevelClass levelClass;
    private DatePickerDialog dpd;
    private DateFormat fromFormat;
    private DateFormat toFormat;
    private DateFormat toFormat2;
    private String dateNow;
    private String dedate;
    private String date_dob;
    private int RESULT;
    private Integer proses;
    private final int RESULT_GALLERY_KTP = 101;
    private final int RESULT_GALLERY_SELFIE = 102;
    private final int RESULT_GALLERY_TTD = 103;
    private final int RESULT_CAMERA_KTP = 201;
    private final int RESULT_SELFIE = 202;
    private final int RESULT_CAMERA_TTD = 203;
    final int RC_CAMERA_STORAGE = 14;
    File ktp, selfie, ttd;
    AlertDialog dialogSuccess = null;
    private boolean is_first_time = false;
    private boolean isRegisteredLevel =false; //saat antri untuk diverifikasi
    private boolean is_verified = false;
    private String listContactPhone = "";
    private String listAddress = "";
    private String contactCenter;
    private String is_new_bulk, reject_KTP, reject_selfie, reject_ttd, respon_reject_ktp, respon_reject_selfie, respon_reject_ttd;
    private ProgressDialog progdialog;
    private PickAndCameraUtil pickAndCameraUtil;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_my_profile_new;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");
        pickAndCameraUtil = new PickAndCameraUtil(this);

        Intent intent    = getIntent();
        if(intent.hasExtra(DefineValue.IS_FIRST)) {
            is_first_time = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);
        }

        is_new_bulk = sp.getString(DefineValue.IS_NEW_BULK,"N");
        reject_KTP = sp.getString(DefineValue.REJECT_KTP,"N");
        reject_selfie = sp.getString(DefineValue.REJECT_FOTO,"N");
        reject_ttd = sp.getString(DefineValue.REJECT_TTD,"N");
        respon_reject_ktp = sp.getString(DefineValue.REMARK_KTP,"");
        respon_reject_selfie = sp.getString(DefineValue.REMARK_FOTO,"");
        respon_reject_ttd = sp.getString(DefineValue.REMARK_TTD,"");
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER,"");

        if(contactCenter.equals("")) {
            getHelpList();
        }
        else {
            try {
                JSONArray arrayContact = new JSONArray(contactCenter);
                for(int i=0 ; i<arrayContact.length() ; i++) {
                    if(i == 0) {
                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        InitializeToolbar();

        View v = this.findViewById(android.R.id.content);

        assert v != null;
        dataMemberBasic = (LinearLayout) findViewById(R.id.data_member_basic);
        dataVerifiedMember = (LinearLayout) findViewById(R.id.data_verified_member);
        layoutKTP = (RelativeLayout) findViewById(R.id.layout_foto_ktp);
        layoutSelfie = (RelativeLayout) findViewById(R.id.layout_selfie);
        layoutTTD = (RelativeLayout) findViewById(R.id.layout_ttd);
        pb1 = (ProgressBar) v.findViewById(R.id.pb1_myprofileactivity);
        pb2 = (ProgressBar) v.findViewById(R.id.pb2_myprofileactivity);
        pb3 = (ProgressBar) v.findViewById(R.id.pb3_myprofileactivity);
        tv_pb1 = (TextView) v.findViewById(R.id.tv_pb1_myprofileactivity);
        tv_pb2 = (TextView) v.findViewById(R.id.tv_pb2_myprofileactivity);
        tv_pb3 = (TextView) v.findViewById(R.id.tv_pb3_myprofileactivity);
        tv_dob = (TextView) v.findViewById(R.id.myprofile_value_dob);
        tv_verified_member = (TextView) v.findViewById(R.id.group_title2);
        tv_respon_reject_KTP = (TextView) v.findViewById(R.id.tv_respon_reject_ktp);
        tv_respon_reject_selfie = (TextView) v.findViewById(R.id.tv_respon_reject_selfie);
        tv_respon_reject_ttd = (TextView) v.findViewById(R.id.tv_respon_reject_ttd);
        et_noHp = (EditText) v.findViewById(R.id.myprofile_value_hp);
        et_nama = (EditText) v.findViewById(R.id.myprofile_value_name);
        et_email = (EditText) v.findViewById(R.id.myprofile_value_email);
        cameraKTP = (ImageButton) v.findViewById(R.id.camera_ktp_paspor);
        selfieKTP = (ImageButton) v.findViewById(R.id.camera_selfie_ktp_paspor);
        cameraTTD = (ImageButton) v.findViewById(R.id.camera_ttd);
        btn1 = (Button) v.findViewById(R.id.button1);
        btn2 = (Button) v.findViewById(R.id.button2);
        levelClass = new LevelClass(this,sp);

//        if(levelClass.isLevel1QAC() && isRegisteredLevel) { DialogSuccessUploadPhoto(); }

        if(levelClass.isLevel1QAC() && isRegisteredLevel) { DialogSuccessUploadPhoto(); }


        if(!is_first_time)
        {
            tv_dob.setEnabled(false);
        }

        if(levelClass.isLevel1QAC())
        {
            btn1.setVisibility(View.VISIBLE);
            dataVerifiedMember.setVisibility(View.GONE);
        }

        if(!levelClass.isLevel1QAC())
        {
            et_nama.setEnabled(false);
            tv_dob.setEnabled(false);
            tv_verified_member.setText("Data Verfied Member Sudah Terverifikasi");
            dataVerifiedMember.setVisibility(View.GONE);
            cameraKTP.setEnabled(false);
            selfieKTP.setEnabled(false);
            cameraTTD.setEnabled(false);
            btn2.setVisibility(View.GONE);
            if(is_new_bulk.equals("Y")) {
                btn1.setVisibility(View.VISIBLE);
            }else
            btn1.setVisibility(View.GONE);

        }

        dataMemberBasic.setOnClickListener(member_basic_click);
        dataVerifiedMember.setOnClickListener(verified_member_click);
        tv_dob.setOnClickListener(textDOBListener);
        btn1.setOnClickListener(nextListener);
        btn2.setOnClickListener(submitListener);
        cameraKTP.setOnClickListener(setImageCameraKTP);
        selfieKTP.setOnClickListener(setImageSelfieKTP);
        cameraTTD.setOnClickListener(setImageCameraTTD);

        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
        toFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID","INDONESIA"));
        toFormat2 = new SimpleDateFormat("dd-M-yyyy", new Locale("ID","INDONESIA"));

        Calendar c = Calendar.getInstance();
        dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:"+dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        if(reject_KTP.equals("Y") || reject_selfie.equals("Y") || reject_ttd.equals("Y")) {
            et_nama.setEnabled(false);
            tv_dob.setEnabled(false);
            btn1.setVisibility(View.GONE);
            dataVerifiedMember.setVisibility(View.VISIBLE);
            btn2.setVisibility(View.VISIBLE);

            if (reject_KTP.equals("Y"))
            {
                cameraKTP.setEnabled(true);
                tv_respon_reject_KTP.setText("Alasan : " +respon_reject_ktp);
            }
            else layoutKTP.setVisibility(View.GONE);

            if (reject_selfie.equals("Y"))
            {
                selfieKTP.setEnabled(true);
                tv_respon_reject_selfie.setText("Alasan : " +respon_reject_selfie);
            }
            else layoutSelfie.setVisibility(View.GONE);

            if (reject_ttd.equals("Y"))
            {
                cameraTTD.setEnabled(true);
                tv_respon_reject_ttd.setText("Alasan : " +respon_reject_ttd);
            }
            else layoutTTD.setVisibility(View.GONE);
        }

        initializeData();

//        if(et_noHp!=null && et_nama!=null && et_email!=null && tv_dob!=null && !isRegisteredLevel)
//        {
//            dialogUpgradeMember();
//        }

    }


    private void InitializeToolbar() {
        if(is_first_time)disableHomeIcon();
        else {
        setActionBarIcon(R.drawable.ic_arrow_left);
        }
        setActionBarTitle(getString(R.string.myprofile_ab_title));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
                if (!is_first_time) {
                    closethis();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private TextView.OnClickListener textDOBListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {

            dpd.show(getFragmentManager(), "Datepickerdialog");
        }
    };

    private TextView.OnClickListener member_basic_click = new TextView.OnClickListener()
    {
        @Override
        public void onClick(View v) {
        }
    };

    private TextView.OnClickListener verified_member_click = new TextView.OnClickListener()
    {
        @Override
        public void onClick(View v) {

        }
    };

    private Button.OnClickListener nextListener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (inputValidation()){
                sendDataUpdate();
            }
        }
    };

    private Button.OnClickListener submitListener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if(ValidationPhoto())
            {
                sentExecCust();
            }
        }
    };

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
            Timber.d("masuk date picker dob");
            try {
                date_dob = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : "+date_dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
        }
    };

    private ImageButton.OnClickListener setImageCameraKTP= new ImageButton.OnClickListener ()
    {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraKTP di MyprofileactivityNew");
            camera_dialog(RESULT_CAMERA_KTP);
        }
    };
    private ImageButton.OnClickListener setImageSelfieKTP= new ImageButton.OnClickListener ()
    {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageSelfieKTP di MyprofileactivityNew");
            camera_dialog(RESULT_SELFIE);
        }
    };
    private ImageButton.OnClickListener setImageCameraTTD= new ImageButton.OnClickListener ()
    {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraTTD di MyprofileactivityNew");
            camera_dialog(RESULT_CAMERA_TTD);
        }
    };

    public void camera_dialog(final int TipeFoto)
    {
        final String[] items = {"Choose from Gallery" , "Take a Photo"};

        android.app.AlertDialog.Builder a = new android.app.AlertDialog.Builder(MyProfileNewActivity.this);
        a.setCancelable(true);
        a.setTitle("Choose Profile Picture");
        a.setAdapter(new ArrayAdapter<>(MyProfileNewActivity.this, android.R.layout.simple_list_item_1, items),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if(TipeFoto == RESULT_CAMERA_KTP) {
                                pickAndCameraUtil.chooseGallery(RESULT_GALLERY_KTP);
                            }
                            else if(TipeFoto == RESULT_SELFIE) {
                                pickAndCameraUtil.chooseGallery(RESULT_GALLERY_SELFIE);
                            }
                            else if (TipeFoto == RESULT_CAMERA_TTD){
                                pickAndCameraUtil.chooseGallery(RESULT_GALLERY_TTD);
                            }
                        } else if (which == 1) {
                            chooseCamera(TipeFoto);
                        }

                    }
                }
        );
        a.create();
        a.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    private void chooseCamera(int TipeFoto) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this,perms)) {
            pickAndCameraUtil.runCamera(TipeFoto);
        }
        else {
            EasyPermissions.requestPermissions(this,getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE,perms);
        }
    }

    private void initializeData(){
        et_noHp.setText(sp.getString(DefineValue.CUST_ID,""));
        et_noHp.setEnabled(false);
        et_nama.setText(sp.getString(DefineValue.PROFILE_FULL_NAME, ""));
        et_nama.setEnabled(false);
        et_email.setText(sp.getString(DefineValue.PROFILE_EMAIL,""));
        if(is_new_bulk.equals("Y"))
        {
            et_email.setEnabled(true);
        }else
        {
            et_email.setEnabled(false);
        }


        dedate = sp.getString(DefineValue.PROFILE_DOB, "");
        if (dedate.equals("")){
            tv_dob.setEnabled(true);
        }

        if(!dedate.equals("")){
            Calendar c = Calendar.getInstance();

            try {
                c.setTime(fromFormat.parse(dedate));
                dedate = toFormat.format(fromFormat.parse(dedate));
                date_dob = fromFormat.format(toFormat2.parse(dedate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);

            dpd = DatePickerDialog.newInstance(
                    dobPickerSetListener,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
        }
        is_verified = sp.getInt(DefineValue.PROFILE_VERIFIED, 0) == 1;
        Timber.d("isi is verified:"+String.valueOf(sp.getInt(DefineValue.PROFILE_VERIFIED, 0)) + " " + is_verified);
    }

    private void sendDataUpdate(){
        try{
            if(progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(MyProfileNewActivity.this, "");
            else
                progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPDATE_PROFILE,
                    userID,accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            params.put(WebParams.USER_ID,et_noHp.getText().toString());
            params.put(WebParams.EMAIL,et_email.getText().toString());
            params.put(WebParams.FULL_NAME,et_nama.getText().toString());
            params.put(WebParams.MOTHER_NAME,et_nama.getText().toString());
            if(dedate.equals(""))params.put(WebParams.DOB,"");
            else params.put(WebParams.DOB,date_dob);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.IS_REGISTER, "N");
            params.put(WebParams.SOCIAL_ID, "");
            params.put(WebParams.POB,"");
            params.put(WebParams.ID_TYPE,"");

//            if(!CountryModel.allCountry[0].equals(tempCountry))
//                params.put(WebParams.COUNTRY,tempCountry);
//            else
            params.put(WebParams.COUNTRY,"");

            params.put(WebParams.ADDRESS, "");
//            if(tempHobby.equals(list_hobby[0])) params.put(WebParams.HOBBY,"");
//            else
            params.put(WebParams.HOBBY,"");
//
//            if(spinner_gender.getSelectedItemPosition()==0)
//                params.put(WebParams.GENDER, gender_value[0]);
//            else
//                params.put(WebParams.GENDER, gender_value[1]);
            params.put(WebParams.GENDER, "");

            params.put(WebParams.BIO, "");

            Timber.d("isi params update profile:"+ params.toString());

            MyApiClient.sentUpdateProfile(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                            sp.edit().putString(DefineValue.IS_NEW_BULK, "N");
                            setLoginProfile(response);
                            Toast.makeText(MyProfileNewActivity.this,getString(R.string.myprofile_toast_update_success),Toast.LENGTH_LONG).show();
                            Timber.d("isi response Update Profile:"+ response.toString());
                            if (levelClass.isLevel1QAC()){
                                    android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MyProfileNewActivity.this);
                                    builder1.setTitle(R.string.upgrade_member);
                                    builder1.setMessage(R.string.message_upgrade_member);
                                    builder1.setCancelable(true);

                                    builder1.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dataVerifiedMember.setVisibility(View.VISIBLE);
                                                    et_nama.setEnabled(false);
                                                    tv_dob.setEnabled(false);
                                                    btn1.setVisibility(View.GONE);
                                                    if(is_first_time) {
                                                        setResult(MainPage.RESULT_FIRST_TIME);
                                                    }
                                                }
                                            });

                                    builder1.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    tv_dob.setEnabled(false);
                                                    if(is_first_time) {
                                                        RESULT = MainPage.RESULT_FIRST_TIME;
                                                        setResult(MainPage.RESULT_FIRST_TIME);
                                                        finish();
                                                    }else
                                                    finish();
                                                }
                                            });

                                    android.support.v7.app.AlertDialog alert11 = builder1.create();
                                    alert11.show();
                                }else
                            {
                                finish();
                            }
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+ response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(MyProfileNewActivity.this,message);
                        }
                        else {
                            Timber.d("Error Update Profile:"+ response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(MyProfileNewActivity.this, code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MyProfileNewActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MyProfileNewActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi data update myprofile:"+ throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if(is_first_time) {
            RESULT = MainPage.RESULT_FIRST_TIME;
        }
        else {
            RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
            closethis();
        }
    }

    private void setLoginProfile(JSONObject response){
        SecurePreferences prefs = sp;
        SecurePreferences.Editor mEditor = prefs.edit();

        try {
            mEditor.putString(DefineValue.PROFILE_DOB, response.getString(WebParams.DOB));
            mEditor.putString(DefineValue.PROFILE_EMAIL,response.getString(WebParams.EMAIL));
            mEditor.putString(DefineValue.PROFILE_FULL_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.PROFILE_BOM, response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.CUST_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.USER_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.MEMBER_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.IS_NEW_BULK,"N");
//            mEditor.putString(DefineValue.IS_REGISTERED_LEVEL, response.getString(WebParams.IS_REGISTER));
            is_verified = response.getInt(WebParams.VERIFIED) == 1;
            mEditor.putString(DefineValue.PROFILE_VERIFIED,response.getString(WebParams.VERIFIED));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mEditor.apply();
    }

    private boolean inputValidation(){

        int compare = 100;
        if(date_dob != null) {
            Date dob = null;
            Date now = null;
            try {
                dob = fromFormat.parse(date_dob);
                now = fromFormat.parse(dateNow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (dob != null) {
                if (now != null) {
                    compare = dob.compareTo(now);
                }
            }
            Timber.d("compare date:"+ Integer.toString(compare));
        }

        if(et_nama.getText().toString().length()==0){
            et_nama.requestFocus();
            et_nama.setError(getResources().getString(R.string.regist1_validation_nama));
            return false;
        }
        else if(et_email.getText().toString().length()==0){
            et_email.requestFocus();
            et_email.setError(getResources().getString(R.string.regist1_validation_email_length));
            return false;
        }
        else if(et_email.getText().toString().length()>0 && !isValidEmail(et_email.getText()) ){
            et_email.requestFocus();
            et_email.setError(getString(R.string.regist1_validation_email));
            return false;
        }
        else if(compare == 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date_empty))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        else if(compare >= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.myprofile_validation_date))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

    public Boolean ValidationPhoto()
    {
        if(layoutKTP.getVisibility()==View.VISIBLE)
        {
            if(ktp==null)
            {
                DefinedDialog.showErrorDialog(MyProfileNewActivity.this, "Foto KTP tidak boleh kosong!");
                return false;
            }
        }
        if (layoutSelfie.getVisibility()==View.VISIBLE)
        {
            if(selfie==null)
            {
                DefinedDialog.showErrorDialog(MyProfileNewActivity.this, "Foto Selfie dengan KTP tidak boleh kosong!");
                return false;
            }
        }
        if (layoutTTD.getVisibility()==View.VISIBLE)
        {
            if(ttd==null)
            {
                DefinedDialog.showErrorDialog(MyProfileNewActivity.this, "Foto Tanda Tangan tidak boleh kosong!");
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RESULT_GALLERY_KTP:
                if(resultCode == RESULT_OK){
                    new ImageCompressionAsyncTask(KTP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_KTP:
                if(resultCode == RESULT_OK){
                    if( pickAndCameraUtil.getCaptureImageUri()!=null){
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            new ImageCompressionAsyncTask(KTP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                        }
                        else {
                            new ImageCompressionAsyncTask(KTP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                        }
                    }
                    else{
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_GALLERY_SELFIE:
                if(resultCode == RESULT_OK){
                    new ImageCompressionAsyncTask(SELFIE_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_SELFIE :
                if(resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri()!=null){
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new ImageCompressionAsyncTask(SELFIE_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    }
                    else {
                        new ImageCompressionAsyncTask(SELFIE_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
                }
                else{
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            case RESULT_GALLERY_TTD :
                if(resultCode == RESULT_OK){
                    new ImageCompressionAsyncTask(TTD_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_TTD:
                if(resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri()!=null){
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new ImageCompressionAsyncTask(TTD_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    }
                    else {
                        new ImageCompressionAsyncTask(TTD_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
                }
                else{
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void uploadFileToServer(File photoFile, final int flag) {
        pb1.setVisibility(View.VISIBLE);
        pb2.setVisibility(View.VISIBLE);
        pb3.setVisibility(View.VISIBLE);
        tv_pb1.setVisibility(View.VISIBLE);
        tv_pb2.setVisibility(View.VISIBLE);
        tv_pb3.setVisibility(View.VISIBLE);
        tv_respon_reject_KTP.setVisibility(View.GONE);
        tv_respon_reject_selfie.setVisibility(View.GONE);
        tv_respon_reject_ttd.setVisibility(View.GONE);

        RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPLOAD_KTP,
                userID,accessKey);
        try {
            params.put(WebParams.USER_ID,et_noHp.getText().toString());
            params.put(WebParams.USER_IMAGES, photoFile);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.TYPE, flag);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Timber.d("params upload foto ktp: " + params.toString());
        Timber.d("params upload foto type: " + flag);

        MyApiClient.sentPhotoKTP(this, params, new JsonHttpResponseHandler() {

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                Timber.d("sebelum proses uploadKTP " +bytesWritten);
                Timber.d("sebelum proses uploadKTP " +totalSize);
                proses = (int) (100 * bytesWritten / totalSize);
                if(proses < 100 || proses == 100)
                {
                    if(flag==KTP_TYPE)
                    {
                        Timber.d("sebelum proses uploadKTP " +proses);
                        pb1.setProgress((int) (100 * bytesWritten / totalSize));
                        Timber.d("proses uploadKTP " +proses);
                        tv_pb1.setText(proses + "%");
                    }
                    else if(flag==SELFIE_TYPE)
                    {
                        pb2.setProgress((int) (100 * bytesWritten / totalSize));
                        tv_pb2.setText(proses + "%");
                    }
                    else if(flag==TTD_TYPE)
                    {
                        pb3.setProgress((int) (100 * bytesWritten / totalSize));
                        tv_pb3.setText(proses + "%");
                    }
                }

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String error_code = response.getString("error_code");
                    String error_message = response.getString("error_message");
                    if (error_code.equalsIgnoreCase("0000")) {

                        Timber.d("onsuccess upload foto type: " + flag);
                        Timber.d("isi response Upload Foto:"+ response.toString());

                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {

                        Timber.d("isi response autologout:" + response.toString());
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(MyProfileNewActivity.this, message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
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

            private void failure(Throwable throwable) {
                Timber.d("Masuk failure");
                if (MyApiClient.PROD_FAILURE_FLAG)
                {
                    Timber.d("Masuk if prod failure flag");
                    Toast.makeText(MyProfileNewActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    if(flag==1)
                    {
                        Timber.d("masuk failure ktp");
                        pb1.setProgress( 0 );
                        tv_pb1.setText("0 %");
                    }
                    if (flag==2)
                    {
                        Timber.d("masuk failure selfie");
                        pb2.setProgress( 0);
                        tv_pb2.setText("0 %");

                    }
                    if (flag==3)
                    {
                        Timber.d("masuk failure ttd");
                        pb3.setProgress(0);
                        tv_pb3.setText("0 %");
                    }
                }

                else
                    Toast.makeText(MyProfileNewActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                Timber.w("Error Koneksi data update foto ktp:" + throwable.toString());
            }

        });
    }

    private void DialogSuccessUploadPhoto()
    {
        Dialog dialognya = DefinedDialog.MessageDialog(MyProfileNewActivity.this, this.getString(R.string.level_dialog_finish_title),
        this.getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                this.getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        finish();
                    }
                }
            );

        dialognya.show();
    }

    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void closethis(){
        setResult(RESULT);
        this.finish();
    }

    private void sentExecCust(){
        try{

            if(progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(MyProfileNewActivity.this, "");
            else
                progdialog.show();

            final RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID, MyApiClient.LINK_EXEC_CUST,
                    userID, accessKey);
            params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID,""));
            params.put(WebParams.CUST_NAME, et_nama.getText().toString());
            params.put(WebParams.CUST_ID_TYPE, "");
            params.put(WebParams.CUST_ID_NUMBER, "");
            params.put(WebParams.CUST_ADDRESS, "");
            params.put(WebParams.CUST_COUNTRY, "");
            params.put(WebParams.CUST_BIRTH_PLACE, "");
            params.put(WebParams.CUST_MOTHER_NAME, et_nama.getText().toString());
            params.put(WebParams.CUST_CONTACT_EMAIL, et_email.getText().toString());
            params.put(WebParams.MEMBER_ID, sp.getString(DefineValue.MEMBER_ID,""));
            params.put(WebParams.IS_REGISTER, "Y");

//            final String dob = nowCalendar.get(Calendar.YEAR)+"-"+ (nowCalendar.get(Calendar.MONTH)+1) +"-"+nowCalendar.get(Calendar.DAY_OF_MONTH);
            params.put(WebParams.CUST_BIRTH_DATE, date_dob);

//            final String gender;
//            if(sp_gender.getSelectedItemPosition()==0)
//                gender = gender_value[0];
//            else
//                gender = gender_value[1];
            params.put(WebParams.CUST_GENDER,"");
            params.put(WebParams.USER_ID, sp.getString(DefineValue.USER_ID,""));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params execute customer:" + params.toString());

            MyApiClient.sentExecCust(MyProfileNewActivity.this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("response execute customer:"+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            SecurePreferences.Editor mEdit = sp.edit();
//                            int member_level = sp.getInt(DefineValue.LEVEL_VALUE,1);
                            mEdit.remove(DefineValue.REJECT_KTP);
                            mEdit.remove(DefineValue.REJECT_FOTO);
                            mEdit.remove(DefineValue.REJECT_TTD);
                            mEdit.remove(DefineValue.REMARK_KTP);
                            mEdit.remove(DefineValue.REMARK_FOTO);
                            mEdit.remove(DefineValue.REMARK_TTD);
                            mEdit.remove(DefineValue.MODEL_NOTIF);
                            mEdit.putBoolean(DefineValue.IS_REGISTERED_LEVEL,true);
                            mEdit.putString(DefineValue.PROFILE_DOB, tv_dob.getText().toString());
                            mEdit.putString(DefineValue.PROFILE_FULL_NAME,et_nama.getText().toString());
                            mEdit.putString(DefineValue.CUST_NAME,et_nama.getText().toString());
                            mEdit.putString(DefineValue.USER_NAME,et_nama.getText().toString());
                            mEdit.putString(DefineValue.MEMBER_NAME, et_nama.getText().toString());
//                            mEdit.putInt(DefineValue.LEVEL_VALUE, response.optInt(WebParams.MEMBER_LEVEL, 1));
                            if (response.optString(WebParams.ALLOW_MEMBER_LEVEL, DefineValue.STRING_NO).equals(DefineValue.STRING_YES))
                                mEdit.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL,true );
                            else
                                mEdit.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL,false );

                            mEdit.apply();
                            DialogSuccessUploadPhoto();
                        } else if (code.equals(WebParams.LOGOUT_CODE)) {
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(MyProfileNewActivity.this, message);
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(MyProfileNewActivity.this, code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MyProfileNewActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MyProfileNewActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

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

    private void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_INSERT,
                    userID,accessKey);
            params.put(WebParams.USER_ID, userID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            MyApiClient.getHelpList(MyProfileNewActivity.this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            Timber.d("isi params help list:"+response.toString());

                            contactCenter = response.getString(WebParams.CONTACT_DATA);

                            SecurePreferences.Editor mEditor = sp.edit();
                            mEditor.putString(DefineValue.LIST_CONTACT_CENTER, response.getString(WebParams.CONTACT_DATA));
                            mEditor.apply();

                            try {
                                JSONArray arrayContact = new JSONArray(contactCenter);
                                for(int i=0 ; i<arrayContact.length() ; i++) {
                                    if(i == 0) {
                                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(MyProfileNewActivity.this,message);
                        }
                        else {
                            Timber.d("isi error help list:"+response.toString());
                            Toast.makeText(MyProfileNewActivity.this, message, Toast.LENGTH_LONG).show();
                        }

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
                        Toast.makeText(MyProfileNewActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MyProfileNewActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();

                    Timber.w("Error Koneksi help list help:"+throwable.toString());
                }
            });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

    private class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {
        private int type;


        ImageCompressionAsyncTask(int type){
            this.type = type;
        }

        @Override
        protected File doInBackground(String... params) {
            return pickAndCameraUtil.compressImage(params[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            switch (type){
                case KTP_TYPE :
                    Picasso.with(MyProfileNewActivity.this).load(file).centerCrop().fit().into(cameraKTP);
                    ktp = file;
                    uploadFileToServer(ktp, KTP_TYPE);
                    break;
                case SELFIE_TYPE :
                    Picasso.with(MyProfileNewActivity.this).load(file).centerCrop().fit().into(selfieKTP);
                    selfie = file;
                    uploadFileToServer(selfie, SELFIE_TYPE);
                    break;
                case TTD_TYPE:
                    Picasso.with(MyProfileNewActivity.this).load(file).centerCrop().fit().into(cameraTTD);
                    ttd = file;
                    uploadFileToServer(ttd, TTD_TYPE);
                    break;
            }
        }
    }
}
