package com.sgo.orimakardaya.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.balysv.materialripple.MaterialRippleLayout;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.CountryModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.*;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;
import com.sgo.orimakardaya.dialogs.DefinedDialog;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/*
  Created by Administrator on 1/18/2015.
 */
public class MyProfileActivity extends BaseActivity {

    private final int RESULT_GALERY = 100;
    private final int RESULT_CAMERA = 200;

    String[] list_hobby;

    SecurePreferences sp;
    TextView tv_dob, tv_verified;
    EditText et_name,et_address,et_email, et_socialID,et_bio,et_pob,et_bom;
    Button btn_submit_update_profile;
    Spinner spinner_country,spinner_hobby,spinner_gender,spinner_id_types;
    ImageView spinWheelCountry,profilePicContent, profileVerified;
    Animation frameAnimation;
    ArrayAdapter<String> adapter2, adapterHobby;
    String selectedCountry,selectedHobby;
    String date_dob, tempCountry,tempHobby,userID,accessKey;
    DateFormat fromFormat,toFormat,toFormat2;
    ProgressDialog progdialog;
    String dedate;
    Uri mCapturedImageURI;
    ProgressBar prgLoading;
    int RESULT;
    boolean is_verified = false,is_first_time = false;
    String dateNow;
    DatePickerDialog dpd;
    String[] gender_value= new String[]{"L","P"};
    Boolean isLevel1,isRegisteredLevel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE, "");
        accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        Intent intent    = getIntent();
        if(intent.hasExtra(DefineValue.IS_FIRST))
            is_first_time  = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);

        int i = sp.getInt(DefineValue.LEVEL_VALUE,0);
        isLevel1 = i == 1;
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL,false);


        InitializeToolbar();

        View v = this.findViewById(android.R.id.content);

        prgLoading = (ProgressBar) v.findViewById(R.id.prgLoading);
        profilePicContent = (ImageView) v.findViewById(R.id.myprofile_pic_content);
        et_name = (EditText) v.findViewById(R.id.myprofile_value_name);
        et_address = (EditText) v.findViewById(R.id.myprofile_value_address);
        et_email = (EditText) v.findViewById(R.id.myprofile_value_email);
        et_pob = (EditText) v.findViewById(R.id.myprofile_value_pob);
        et_socialID = (EditText) v.findViewById(R.id.myprofile_value_social_id);
        et_bio = (EditText) v.findViewById(R.id.myprofile_value_bio);
        et_bom = (EditText) v.findViewById(R.id.myprofile_value_birth_mother);
        tv_dob = (TextView) v.findViewById(R.id.myprofile_value_bod);
        btn_submit_update_profile = (Button) v.findViewById(R.id.btn_submit_update_profile);
        spinner_country = (Spinner) v.findViewById(R.id.myprofile_spinner_negara);
        spinner_hobby = (Spinner) v.findViewById(R.id.myprofile_spinner_hobby);
        spinner_gender = (Spinner) v.findViewById(R.id.myprofile_spinner_gender);
        spinner_id_types = (Spinner) v.findViewById(R.id.myprofile_spinner_socialid_type);
        spinWheelCountry = (ImageView) v.findViewById(R.id.spinning_wheel_myprofile_negara);
        tv_verified = (TextView) v.findViewById(R.id.myprofile_text_verified);
        profileVerified = (ImageView) v.findViewById(R.id.myprofile_image_verified);


        if (is_first_time) {
            View tv_first_time = v.findViewById(R.id.firsttime_msg);
            View layout_mob = v.findViewById(R.id.layout_mother_name);
            layout_mob.setVisibility(View.VISIBLE);
            MaterialRippleLayout btn_cancel = (MaterialRippleLayout)
                    v.findViewById(R.id.btn_cancel_update_profile);
            tv_first_time.setVisibility(View.VISIBLE);
            btn_cancel.setVisibility(View.VISIBLE);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RESULT = MainPage.RESULT_LOGOUT;
                    closethis();
                }
            });

        }
        else {
            if(isLevel1) {
                if (isRegisteredLevel) {
                    et_name.setEnabled(false);
                    et_address.setEnabled(false);
                    et_pob.setEnabled(false);
                    et_socialID.setEnabled(false);
                    tv_dob.setEnabled(false);
                    et_email.setEnabled(false);
                    spinner_gender.setEnabled(false);
                    spinner_id_types.setEnabled(false);
                }
                else {
                    et_name.setEnabled(true);
                    et_address.setEnabled(true);
                    et_pob.setEnabled(true);
                    et_socialID.setEnabled(true);
                    et_email.setEnabled(false);
                    tv_dob.setEnabled(false);
                    spinner_gender.setEnabled(true);
                }
            }
            else {
                et_name.setEnabled(false);
                et_address.setEnabled(false);
                et_pob.setEnabled(false);
                et_socialID.setEnabled(false);
                tv_dob.setEnabled(false);
                et_email.setEnabled(false);
                spinner_gender.setEnabled(false);
                spinner_id_types.setEnabled(false);
            }
        }

        tv_dob.setOnClickListener(textDOBListener);

        frameAnimation = AnimationUtils.loadAnimation(this, R.anim.spinner_animation);
        frameAnimation.setRepeatCount(Animation.INFINITE);

        btn_submit_update_profile.setOnClickListener(btnSubmitUpdateListener);

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


    @Override
    public void onBackPressed() {
        if(!is_first_time) {
            RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
            closethis();
        }
    }


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_myprofile;
    }

    public void InitializeToolbar(){
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

    public void initializeData(){

        RESULT = MainPage.RESULT_NORMAL;

        et_name.setText(sp.getString(DefineValue.PROFILE_FULL_NAME, ""));
        et_address.setText(sp.getString(DefineValue.PROFILE_ADDRESS,""));
        et_email.setText(sp.getString(DefineValue.PROFILE_EMAIL,""));
        et_socialID.setText(sp.getString(DefineValue.PROFILE_SOCIAL_ID,""));
        et_pob.setText(sp.getString(DefineValue.PROFILE_POB,""));
        et_bio.setText(sp.getString(DefineValue.PROFILE_BIO,""));
        et_bom.setText(sp.getString(DefineValue.PROFILE_BOM,""));
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
        selectedCountry = sp.getString(DefineValue.PROFILE_COUNTRY,"");

        spinner_country.setVisibility(View.GONE);
        spinWheelCountry.setVisibility(View.VISIBLE);
        spinWheelCountry.startAnimation(frameAnimation);

        final ArrayList<String> dataCountry = new ArrayList<>();
        dataCountry.add(getString(R.string.myprofile_spinner_default));
        dataCountry.add(CountryModel.Indonesia);
        dataCountry.addAll(Arrays.asList(CountryModel.allCountry));

        Thread deproses = new Thread(){
            @Override
            public void run() {
                adapter2 = new ArrayAdapter<>(MyProfileActivity.this, android.R.layout.simple_spinner_item, dataCountry);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_country.setAdapter(adapter2);

                for(int i = 0 ; i< dataCountry.size();i++){
                    if(dataCountry.get(i).contains(selectedCountry)){
                        spinner_country.setSelection(i,false);
                        break;
                    }
                }

                MyProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinWheelCountry.clearAnimation();
                        spinWheelCountry.setVisibility(View.GONE);
                        spinner_country.setVisibility(View.VISIBLE);
                        adapter2.notifyDataSetChanged();
                    }
                });
            }
        };
        deproses.run();

        selectedHobby = sp.getString(DefineValue.PROFILE_HOBBY,"");

        list_hobby = getResources().getStringArray(R.array.list_myprofile_hobby);
        adapterHobby = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list_hobby) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }

                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        adapterHobby.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_hobby.setAdapter(adapterHobby);

        for(int i = 0 ; i<list_hobby.length;i++){
            if(list_hobby[i].equals(selectedHobby)){
                spinner_hobby.setSelection(i,false);
                break;
            }
        }

        adapterHobby.notifyDataSetChanged();

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,R.array.gender_type, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_gender.setAdapter(genderAdapter);


        String _tempData = sp.getString(DefineValue.PROFILE_GENDER,"");
        if(!_tempData.isEmpty()){
            if(_tempData.equalsIgnoreCase(gender_value[0]))
                spinner_gender.setSelection(0,false);
            else
                spinner_gender.setSelection(1,false);
        }

        JSONArray mData;
        try {
            mData = new JSONArray(sp.getString(DefineValue.LIST_ID_TYPES,""));
            String[] dataSpinnerSocialID = new String[mData.length()];

            for (int i = 0 ; i < mData.length(); i++) {
                dataSpinnerSocialID[i] = mData.getJSONObject(i).getString(WebParams.TYPE);
            }

            ArrayAdapter<String> socialidAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,dataSpinnerSocialID );
            socialidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_id_types.setAdapter(socialidAdapter);

            _tempData = sp.getString(DefineValue.PROFILE_ID_TYPE,"");
            if(!_tempData.equals("")){
                spinner_id_types.setSelection(socialidAdapter.getPosition(_tempData),false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        setImageProfPic();
        changeVerified();
    }


    private void changeVerified(){
        if(is_verified){
            tv_verified.setVisibility(View.GONE);
            profileVerified.setImageResource(R.drawable.ic_circle_ok_green);
        }
        else {
            tv_verified.setVisibility(View.VISIBLE);
            profileVerified.setImageResource(R.drawable.ic_circle_ok_yellow);
        }

    }

    TextView.OnClickListener textDOBListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {

            dpd.show(getFragmentManager(), "Datepickerdialog");
        }
    };

    DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
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

    Button.OnClickListener btnSubmitUpdateListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
                if(InetHandler.isNetworkAvailable(MyProfileActivity.this)) {
                        if (inputValidation()) {
                            if(is_first_time){
                                AlertDialog.Builder alertbox=new AlertDialog.Builder(MyProfileActivity.this);
                                alertbox.setTitle(getString(R.string.confirmation));
                                alertbox.setMessage(getString(R.string.myprofile_warning_message_firsttime));
                                alertbox.setPositiveButton(getString(R.string.ok), new
                                        DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                sendDataUpdate();
                                            }
                                        });
                                alertbox.setNegativeButton(getString(R.string.cancel), new
                                        DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface arg0, int arg1) {}
                                        });
                                alertbox.show();
                            }
                            else
                                sendDataUpdate();
                        }
                }
                else DefinedDialog.showErrorDialog(MyProfileActivity.this, getString(R.string.inethandler_dialog_message));

        }
    };

    public void setImageProfPic(){
        float density = getResources().getDisplayMetrics().density;
        String _url_profpic;

        if(density <= 1) _url_profpic = sp.getString(DefineValue.IMG_SMALL_URL, null);
        else if(density < 2) _url_profpic = sp.getString(DefineValue.IMG_MEDIUM_URL, null);
        else _url_profpic = sp.getString(DefineValue.IMG_LARGE_URL, null);

        Timber.wtf("url prof pic:"+ _url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(this);
        else
            mPic= Picasso.with(this);

        if(_url_profpic != null && _url_profpic.isEmpty()){
            mPic.load(R.drawable.user_unknown_menu)
                .error(roundedImage)
                .fit().centerInside()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation()).into(profilePicContent);
        }
        else {
            mPic.load(_url_profpic)
                .error(roundedImage)
                .fit()
                .centerCrop()
                .placeholder(R.anim.progress_animation)
                .transform(new RoundImageTransformation())
                .into(profilePicContent);
        }

    }

    public void sendDataUpdate(){
        try{
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            tempCountry = spinner_country.getSelectedItem().toString();
            tempHobby = spinner_hobby.getSelectedItem().toString();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPDATE_PROFILE,
                    userID,accessKey);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID,sp.getString(DefineValue.MEMBER_ID,""));
            params.put(WebParams.SOCIAL_ID,et_socialID.getText().toString());
            params.put(WebParams.USER_ID,userID);
            params.put(WebParams.EMAIL,et_email.getText().toString());
            params.put(WebParams.FULL_NAME,et_name.getText().toString());
            params.put(WebParams.POB,et_pob.getText().toString());
            params.put(WebParams.ID_TYPE,spinner_id_types.getSelectedItem().toString());

            if(dedate.equals(""))params.put(WebParams.DOB,"");
            else params.put(WebParams.DOB,date_dob);

            if(!CountryModel.allCountry[0].equals(tempCountry))
                params.put(WebParams.COUNTRY,tempCountry);
            else params.put(WebParams.COUNTRY,"");

            params.put(WebParams.ADDRESS, et_address.getText().toString());
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());

            if(tempHobby.equals(list_hobby[0])) params.put(WebParams.HOBBY,"");
            else params.put(WebParams.HOBBY,tempHobby);

            if(spinner_gender.getSelectedItemPosition()==0)
                params.put(WebParams.GENDER, gender_value[0]);
            else
                params.put(WebParams.GENDER, gender_value[1]);

            params.put(WebParams.BIO, et_bio.getText().toString());
            params.put(WebParams.MOTHER_NAME, et_bom.getText().toString());
            params.put(WebParams.IS_REGISTER, "N");

            Timber.d("isi params update profile:"+ params.toString());

            MyApiClient.sentUpdateProfile(this,params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    progdialog.dismiss();
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            setLoginProfile(response);
                            Toast.makeText(MyProfileActivity.this,getString(R.string.myprofile_toast_update_success),Toast.LENGTH_LONG).show();
                            Timber.d("isi response Update Profile:"+ response.toString());
                            if(is_first_time) {
                                RESULT = MainPage.RESULT_FIRST_TIME;
                            }
                            closethis();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+ response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(MyProfileActivity.this,message);
                        }
                        else {
                            Timber.d("Error Update Profile:"+ response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(MyProfileActivity.this, code, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MyProfileActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MyProfileActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi data update myprofile:"+ throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }


    public void myprofile_pic_dialog (View view){
        final String[] items = {"Choose from Gallery" , "Take a Photo"};

        AlertDialog.Builder a = new AlertDialog.Builder(MyProfileActivity.this);
        a.setCancelable(true);
        a.setTitle("Choose Profile Picture");
        a.setAdapter(new ArrayAdapter<String>(MyProfileActivity.this, android.R.layout.simple_list_item_1, items),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Timber.wtf("masuk gallery");
                            chooseGallery();
                        } else if (which == 1) {
                            chooseCamera();
                        }

                    }
                }
        );
        a.create();
        a.show();
    }

    private void chooseGallery() {
        Timber.wtf("masuk gallery");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_GALERY);
    }

    private void chooseCamera() {
        String fileName = "temp.jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);

        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        startActivityForResult(takePictureIntent, RESULT_CAMERA);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCapturedImageURI != null) {
            outState.putString("cameraImageUri", String.valueOf(mCapturedImageURI));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RESULT_GALERY:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    String filePath;
                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        filePath = cursor.getString(columnIndex);
                    }
                    else
                        filePath = selectedImage.getPath();

//                    File photoFile = new File(filePath);

                    GeneralizeImage mGI = new GeneralizeImage(this,filePath);
                    //setImageProfPic(photoFile);
                    //getOrientationImage();
                    uploadFileToServer(mGI.Convert());

                }
                break;
            case RESULT_CAMERA:
                if(resultCode == RESULT_OK && mCapturedImageURI!=null){
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
                    //getOrientationImage();
                    uploadFileToServer(mGI.Convert());
                }
                else{
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void uploadFileToServer(File photoFile) {
        Picasso.with(this).load(R.anim.progress_animation).into(profilePicContent);
        prgLoading.setVisibility(View.VISIBLE);

        RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPLOAD_PROFILE_PIC,
                userID,accessKey);

        try {
            params.put(WebParams.USER_ID,userID);
            params.put(WebParams.USER_FILE, photoFile);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        MyApiClient.sentProfilePicture(this, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String error_code = response.getString("error_code");
                    String error_message = response.getString("error_message");
                    prgLoading.setVisibility(View.GONE);
                    Timber.d("response Listbank:" + response.toString());
                    if (error_code.equalsIgnoreCase("0000")) {
                        SecurePreferences.Editor mEditor = sp.edit();

                        mEditor.putString(DefineValue.IMG_URL, response.getString(WebParams.IMG_URL));
                        mEditor.putString(DefineValue.IMG_SMALL_URL, response.getString(WebParams.IMG_SMALL_URL));
                        mEditor.putString(DefineValue.IMG_MEDIUM_URL, response.getString(WebParams.IMG_MEDIUM_URL));
                        mEditor.putString(DefineValue.IMG_LARGE_URL, response.getString(WebParams.IMG_LARGE_URL));

                        mEditor.apply();

                        setImageProfPic();

                        RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
                        Timber.d("isi response autologout:" + response.toString());
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(MyProfileActivity.this, message);
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
                        alert.setTitle("Upload Image");
                        alert.setMessage("Upload Image : " + error_message);
                        alert.setPositiveButton("OK", null);
                        alert.show();
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
                    Toast.makeText(MyProfileActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyProfileActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                if (prgLoading.getVisibility() == View.VISIBLE)
                    prgLoading.setVisibility(View.GONE);
                setImageProfPic();
                Timber.w("Error Koneksi data update myprofile:" + throwable.toString());
            }

        });
    }

    public void setLoginProfile(JSONObject response){
        SecurePreferences prefs = sp;
        SecurePreferences.Editor mEditor = prefs.edit();

        try {
            mEditor.putString(DefineValue.PROFILE_DOB, response.getString(WebParams.DOB));
            mEditor.putString(DefineValue.PROFILE_ADDRESS,response.getString(WebParams.ADDRESS));
            mEditor.putString(DefineValue.PROFILE_BIO,response.getString(WebParams.BIO));
            mEditor.putString(DefineValue.PROFILE_COUNTRY,response.getString(WebParams.COUNTRY));
            mEditor.putString(DefineValue.PROFILE_SOCIAL_ID,response.getString(WebParams.SOCIAL_ID));
            mEditor.putString(DefineValue.PROFILE_EMAIL,response.getString(WebParams.EMAIL));
            mEditor.putString(DefineValue.PROFILE_FULL_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.CUST_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.USER_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.MEMBER_NAME,response.getString(WebParams.FULL_NAME));
            mEditor.putString(DefineValue.PROFILE_HOBBY,response.getString(WebParams.HOBBY));
            mEditor.putString(DefineValue.PROFILE_POB,response.getString(WebParams.POB));
            mEditor.putString(DefineValue.PROFILE_BOM,response.optString(WebParams.MOTHER_NAME,et_bom.getText().toString()));
            mEditor.putString(DefineValue.PROFILE_GENDER,response.getString(WebParams.GENDER));
            mEditor.putString(DefineValue.PROFILE_ID_TYPE,response.getString(WebParams.ID_TYPE ));
            is_verified = response.getInt(WebParams.VERIFIED) == 1;
            mEditor.putString(DefineValue.PROFILE_VERIFIED,response.getString(WebParams.VERIFIED));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mEditor.apply();
        changeVerified();
        RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
    }

    public boolean inputValidation(){

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

        if(et_name.getText().toString().length()==0){
            et_name.requestFocus();
            et_name.setError(getResources().getString(R.string.regist1_validation_nama));
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
        else if(is_first_time){
            if(et_bom.getText().toString().length()==0){
                et_bom.requestFocus();
                et_bom.setError(getResources().getString(R.string.myprofile_validation_bom));
                return false;
            }
        }
        else if(!isLevel1) { //jika level 2
            if(et_pob.getText().toString().length()==0){
                et_pob.requestFocus();
                et_pob.setError(getResources().getString(R.string.myprofile_validation_pob));
                return false;
            }
            else if(et_address.getText().toString().length()==0){
                et_address.requestFocus();
                et_address.setError(getResources().getString(R.string.myprofile_validation_address));
                return false;
            }
            else if(et_socialID.getText().toString().length()==0){
                et_socialID.requestFocus();
                et_socialID.setError(getResources().getString(R.string.myprofile_validation_socialid));
                return false;
            }
        }
        return true;
    }

    private void closethis(){
        setResult(RESULT);
        this.finish();
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}