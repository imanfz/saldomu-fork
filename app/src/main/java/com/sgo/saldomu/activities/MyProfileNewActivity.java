package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.CountryModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GeneralizeImage;
import com.sgo.saldomu.coreclass.InetHandler;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private SecurePreferences sp;
    TextView tv_dob, tv_pb1, tv_pb2, tv_pb3, tv_verified_member;
    LinearLayout dataMemberBasic , dataVerifiedMember;
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
    private Uri mCapturedImageURI;
    File ktp, selfie, ttd;
    AlertDialog dialogSuccess = null;
    private boolean is_first_time = false;
    private Boolean isRegisteredLevel; //saat antri untuk diverifikasi
    private boolean is_verified = false;

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

        Intent intent    = getIntent();
        if(intent.hasExtra(DefineValue.IS_FIRST))
            is_first_time  = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);

        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL,false);

        InitializeToolbar();

        View v = this.findViewById(android.R.id.content);

        assert v != null;
        dataMemberBasic = (LinearLayout) findViewById(R.id.data_member_basic);
        dataVerifiedMember = (LinearLayout) findViewById(R.id.data_verified_member);
        pb1 = (ProgressBar) v.findViewById(R.id.pb1_myprofileactivity);
        pb2 = (ProgressBar) v.findViewById(R.id.pb2_myprofileactivity);
        pb3 = (ProgressBar) v.findViewById(R.id.pb3_myprofileactivity);
        tv_pb1 = (TextView) v.findViewById(R.id.tv_pb1_myprofileactivity);
        tv_pb2 = (TextView) v.findViewById(R.id.tv_pb2_myprofileactivity);
        tv_pb3 = (TextView) v.findViewById(R.id.tv_pb3_myprofileactivity);
        tv_dob = (TextView) v.findViewById(R.id.myprofile_value_dob);
        tv_verified_member = (TextView) v.findViewById(R.id.group_title2);
        et_noHp = (EditText) v.findViewById(R.id.myprofile_value_hp);
        et_nama = (EditText) v.findViewById(R.id.myprofile_value_name);
        et_email = (EditText) v.findViewById(R.id.myprofile_value_email);
        cameraKTP = (ImageButton) v.findViewById(R.id.camera_ktp_paspor);
        selfieKTP = (ImageButton) v.findViewById(R.id.camera_selfie_ktp_paspor);
        cameraTTD = (ImageButton) v.findViewById(R.id.camera_ttd);
        btn1 = (Button) v.findViewById(R.id.button1);
        btn2 = (Button) v.findViewById(R.id.button2);
        levelClass = new LevelClass(this,sp);

        if(isRegisteredLevel) { DialogSuccessUploadPhoto(); }

        if(!levelClass.isLevel1QAC())
        {
            et_nama.setEnabled(false);
            tv_dob.setEnabled(false);
            btn1.setVisibility(View.GONE);
            tv_verified_member.setText("Data Verfied Member Sudah Terverifikasi");
            dataVerifiedMember.setVisibility(View.GONE);
            cameraKTP.setEnabled(false);
            selfieKTP.setEnabled(false);
            cameraTTD.setEnabled(false);
            btn2.setVisibility(View.GONE);
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

        initializeData();

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
            if (levelClass.isLevel1QAC()){
                android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MyProfileNewActivity.this);
                builder1.setTitle(R.string.upgrade_member);
                builder1.setMessage(R.string.message_upgrade_member);
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(inputValidation())
                                {
                                    sendDataUpdate();
                                }
                                dataVerifiedMember.setVisibility(View.VISIBLE);
                                et_nama.setEnabled(false);
                                tv_dob.setEnabled(false);
                                btn1.setVisibility(View.GONE);
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(inputValidation())
                                {
                                    sendDataUpdate();
                                    finish();
                                }
                            }
                        });

                android.support.v7.app.AlertDialog alert11 = builder1.create();
                alert11.show();
            }
//            else {
//                btn1.setVisibility(View.GONE);
//                dataVerifiedMember.setVisibility(View.VISIBLE);
//            }
        }
    };

    private Button.OnClickListener submitListener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if(ValidationPhoto())
            {
                uploadFileToServer(ktp, 1);
                uploadFileToServer(selfie, 2);
                uploadFileToServer(ttd, 3);
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
                            Timber.wtf("masuk gallery");
                            if(TipeFoto == RESULT_CAMERA_KTP) {
                                chooseGallery(RESULT_GALLERY_KTP);
                            }
                            else if(TipeFoto == RESULT_SELFIE) {
                                chooseGallery(RESULT_GALLERY_SELFIE);
                            }
                            else if (TipeFoto == RESULT_CAMERA_TTD){
                                chooseGallery(RESULT_GALLERY_TTD);
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

    private void chooseGallery(int TipeFoto) {
        Timber.wtf("masuk gallery");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, TipeFoto);
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    private void chooseCamera(int TipeFoto) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this,perms)) {
            runCamera(TipeFoto);
        }
        else {
            EasyPermissions.requestPermissions(this,getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE,perms);
        }
    }

    private void runCamera(int TipeFoto){
        String fileName = "temp.jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);

        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        startActivityForResult(takePictureIntent, TipeFoto);
    }

    private void initializeData(){

        RESULT = MainPage.RESULT_NORMAL;

        et_noHp.setText(sp.getString(DefineValue.CUST_ID,""));
        et_noHp.setEnabled(false);
        et_nama.setText(sp.getString(DefineValue.PROFILE_FULL_NAME, ""));
        et_email.setText(sp.getString(DefineValue.PROFILE_EMAIL,""));
        et_email.setEnabled(false);

        dedate = sp.getString(DefineValue.PROFILE_DOB, "");

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
//        changeVerified();
    }

    private void sendDataUpdate(){
        try{

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
                            setLoginProfile(response);
                            Toast.makeText(MyProfileNewActivity.this,getString(R.string.myprofile_toast_update_success),Toast.LENGTH_LONG).show();
                            Timber.d("isi response Update Profile:"+ response.toString());
                            if(is_first_time) {
                            RESULT = MainPage.RESULT_FIRST_TIME;
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
                    Timber.w("Error Koneksi data update myprofile:"+ throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void setLoginProfile(JSONObject response){
        SecurePreferences prefs = sp;
        SecurePreferences.Editor mEditor = prefs.edit();

        try {
            mEditor.putString(DefineValue.PROFILE_DOB, response.getString(WebParams.DOB));
            mEditor.putString(DefineValue.PROFILE_EMAIL,response.getString(WebParams.EMAIL));
            mEditor.putString(DefineValue.PROFILE_FULL_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.CUST_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.USER_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.MEMBER_NAME,response.getString(WebParams.FULL_NAME));
            is_verified = response.getInt(WebParams.VERIFIED) == 1;
            mEditor.putString(DefineValue.PROFILE_VERIFIED,response.getString(WebParams.VERIFIED));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mEditor.apply();
//        changeVerified();
        RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
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
        if(ktp==null)
        {
            DefinedDialog.showErrorDialog(MyProfileNewActivity.this, "Foto KTP tidak boleh kosong!");
            return false;
        }
        else if(selfie==null)
        {
            DefinedDialog.showErrorDialog(MyProfileNewActivity.this, "Foto Selfie dengan KTP tidak boleh kosong!");
            return false;
        }
        else if(ttd==null)
        {
            DefinedDialog.showErrorDialog(MyProfileNewActivity.this, "Foto Tanda Tangan tidak boleh kosong!");
            return false;
        }
        return true;
    }

    public File setmGalleryImage (Intent data)
    {
        Bitmap photo = null;
        Uri _urinya = data.getData();
        if(data.getData() == null) {
            photo = (Bitmap)data.getExtras().get("data");
        } else {
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GeneralizeImage mGI = new GeneralizeImage(this,photo,_urinya);

        return mGI.Convert();
    }

    public File setmCapturedImage(Intent data)
    {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(mCapturedImageURI, projection, null, null, null);
        String filePath;
        if (cursor != null) {
            cursor.moveToFirst();
            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            filePath = cursor.getString(column_index_data);
        }
        else
            filePath = data.getData().getPath();
//                    File photoFile = new File(filePath);

        GeneralizeImage mGI = new GeneralizeImage(this,filePath);
//                    getOrientationImage();
//        uploadFileToServer(mGI.Convert());
        assert cursor != null;
        cursor.close();

        return mGI.Convert();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RESULT_GALLERY_KTP:
                if(resultCode == RESULT_OK){
                    Picasso.with(this).load(setmGalleryImage(data)).centerCrop().fit().into(cameraKTP);
                    ktp = setmGalleryImage(data);
//                    files.add(setmGalleryImage(data));
//                    uploadFileToServer(setmGalleryImage(data), 1);
                }
                break;
            case RESULT_GALLERY_SELFIE:
                if(resultCode == RESULT_OK){
                    Picasso.with(this).load(setmGalleryImage(data)).centerCrop().fit().into(selfieKTP);
                    selfie = setmGalleryImage(data);
                }
                break;
            case RESULT_GALLERY_TTD :
                if(resultCode == RESULT_OK){
                    Picasso.with(this).load(setmGalleryImage(data)).centerCrop().fit().into(cameraTTD);
                    ttd = setmGalleryImage(data);
                }
                break;
            case RESULT_CAMERA_KTP:
                if(resultCode == RESULT_OK && mCapturedImageURI!=null){
                    Timber.d("isi mcapture image "+mCapturedImageURI.getPath());
                    Picasso.with(this).load(setmCapturedImage(data)).centerCrop().fit().into(cameraKTP);
                    ktp = setmCapturedImage(data);
                }
                break;
            case RESULT_SELFIE :
                if(resultCode == RESULT_OK && mCapturedImageURI!=null){
                    Timber.d("isi mcapture image "+mCapturedImageURI.getPath());
                    Picasso.with(this).load(setmCapturedImage(data)).centerCrop().fit().into(selfieKTP);
                    selfie = setmCapturedImage(data);
                }
                break;
            case RESULT_CAMERA_TTD:
                if(resultCode == RESULT_OK && mCapturedImageURI!=null){
                    Timber.d("isi mcapture image "+mCapturedImageURI.getPath());
                    Picasso.with(this).load(setmCapturedImage(data)).centerCrop().fit().into(cameraTTD);
                    ttd= setmCapturedImage(data);
                }
                break;
            default:
                break;
        }
    }

    private void uploadFileToServer(File photoFile, final int flag) {
//        Picasso.with(this).load(R.drawable.progress_animation).into(cameraKTP);
        pb1.setVisibility(View.VISIBLE);
        pb2.setVisibility(View.VISIBLE);
        pb3.setVisibility(View.VISIBLE);
        tv_pb1.setVisibility(View.VISIBLE);
        tv_pb2.setVisibility(View.VISIBLE);
        tv_pb3.setVisibility(View.VISIBLE);

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
                proses = (int) (100 * bytesWritten / totalSize);
                if(flag==1)
                {

                    pb1.setProgress((int) (100 * bytesWritten / totalSize));
                    tv_pb1.setText(proses + "%");
                }
                else if(flag==2)
                {
                    pb2.setProgress((int) (100 * bytesWritten / totalSize));
                    tv_pb2.setText(proses + "%");
                }
                else if(flag==3)
                {
                    pb3.setProgress((int) (100 * bytesWritten / totalSize));
                    tv_pb3.setText(proses + "%");
                }
                validasiSuccess();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String error_code = response.getString("error_code");
                    String error_message = response.getString("error_message");
//                    prgLoading.setVisibility(View.GONE);
//                    Timber.d("response Listbank:" + response.toString());
                    if (error_code.equalsIgnoreCase("0000")) {
                        SecurePreferences.Editor mEditor = sp.edit();
                        Timber.d("onsuccess upload foto type: " + flag);
                        mEditor.putString(DefineValue.IMG_URL, response.getString(WebParams.IMG_URL));
                        mEditor.putString(DefineValue.IMG_SMALL_URL, response.getString(WebParams.IMG_SMALL_URL));
                        mEditor.putString(DefineValue.IMG_MEDIUM_URL, response.getString(WebParams.IMG_MEDIUM_URL));
                        mEditor.putString(DefineValue.IMG_LARGE_URL, response.getString(WebParams.IMG_LARGE_URL));

                        mEditor.apply();

                        Toast.makeText(MyProfileNewActivity.this,getString(R.string.myprofile_toast_update_foto_success),Toast.LENGTH_SHORT).show();
                        Timber.d("isi response Upload Foto:"+ response.toString());

                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
                        Timber.d("isi response autologout:" + response.toString());
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(MyProfileNewActivity.this, message);
                    } else {
//                        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MyProfileNewActivity.this);
//                        alert.setTitle("Upload Image");
//                        alert.setMessage("Upload Image : " + error_message);
//                        alert.setPositiveButton("OK", null);
//                        alert.show();

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
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(MyProfileNewActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyProfileNewActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
//                if (prgLoading.getVisibility() == View.VISIBLE)
//                    prgLoading.setVisibility(View.GONE);
//                setImageCameraKTP();
                Timber.w("Error Koneksi data update foto ktp:" + throwable.toString());
            }

        });
    }

    private void DialogSuccessUploadPhoto()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success")
                .setMessage(getString(R.string.myprofile_upload_photo_finish))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        if(dialogSuccess==null)
        {
            dialogSuccess = builder.create();
            dialogSuccess.show();
        }
    }

    private void validasiSuccess()
    {
        if (tv_pb1.getText().toString().equalsIgnoreCase("100%"))
        {
            cameraKTP.setEnabled(false);
            if(tv_pb2.getText().toString().equalsIgnoreCase("100%") && tv_pb3.getText().toString().equalsIgnoreCase("100%"))
            {
                DialogSuccessUploadPhoto();
            }

        }
        else if (tv_pb2.getText().toString().equalsIgnoreCase("100%"))
        {
            selfieKTP.setEnabled(false);
            if(tv_pb1.getText().toString().equalsIgnoreCase("100%") && tv_pb3.getText().toString().equalsIgnoreCase("100%"))
            {
               DialogSuccessUploadPhoto();
            }
        }
        else if (tv_pb3.getText().toString().equalsIgnoreCase("100%"))
        {
            cameraTTD.setEnabled(false);
            if(tv_pb1.getText().toString().equalsIgnoreCase("100%") && tv_pb2.getText().toString().equalsIgnoreCase("100%"))
            {
                DialogSuccessUploadPhoto();
            }
        }
    }

    private void changeVerified(){
        if(is_verified) {
            dataMemberBasic.setVisibility(View.VISIBLE);
            et_nama.setEnabled(false);
            tv_dob.setEnabled(false);
            btn1.setVisibility(View.GONE);
            dataVerifiedMember.setVisibility(View.GONE);
//            cameraKTP.setEnabled(false);
//            selfieKTP.setEnabled(false);
//            cameraTTD.setEnabled(false);
//            btn2.setVisibility(View.GONE);
        }
    }

    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void closethis(){
        setResult(RESULT);
        this.finish();
    }
}
