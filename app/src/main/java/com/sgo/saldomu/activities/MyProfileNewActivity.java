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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BankCashoutAdapter;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.BankCashoutModel;
import com.sgo.saldomu.models.retrofit.SentExecCustModel;
import com.sgo.saldomu.models.retrofit.UpdateProfileModel;
import com.sgo.saldomu.models.retrofit.UploadFotoModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.utils.camera.CameraActivity;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.widgets.BlinkingEffectClass;
import com.sgo.saldomu.widgets.ProgressRequestBody;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    TextView tv_dob, tv_pb1, tv_pb2, tv_pb3, tv_verified_member, tv_respon_reject_KTP, tv_respon_reject_selfie, tv_respon_reject_ttd;
    LinearLayout dataMemberBasic, dataVerifiedMember;
    RelativeLayout layoutKTP, layoutSelfie, layoutTTD;
    EditText et_nama, et_noHp, et_email, et_acctNo;
    Spinner sp_bank;
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
    private Integer set_result_photo;
    private final int RESULT_GALLERY_KTP = 101;
    private final int RESULT_GALLERY_SELFIE = 102;
    private final int RESULT_GALLERY_TTD = 103;
    private final int RESULT_CAMERA_KTP = 201;
    private final int RESULT_SELFIE = 202;
    private final int RESULT_CAMERA_TTD = 203;
    final int RC_CAMERA_STORAGE = 14;
    final int RC_GALLERY = 15;
    File ktp, selfie, ttd;
    AlertDialog dialogSuccess = null;
    private boolean is_first_time = false;
    private boolean isRegisteredLevel = false; //saat antri untuk diverifikasi
    private boolean isUpgradeAgent = false; //saat antri untuk diverifikasi upgrade agent
    private boolean is_verified = false;
    private boolean is_agent = false;
//    private String listContactPhone = "";
//    private String listAddress = "";
    private String contactCenter;
    private String is_new_bulk, reject_KTP, reject_selfie, reject_ttd, respon_reject_ktp, respon_reject_selfie,
            respon_reject_ttd, reject_npwp;
    private ProgressDialog progdialog;
    private PickAndCameraUtil pickAndCameraUtil;
    Gson gson;
    private LinearLayout lytVerifiedMember;
    CheckBox cb_termsncond;
    BankCashoutAdapter adapter;
    List<BankCashoutModel> listBankCashOut = new ArrayList<>();
    private String bankCode = "";

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_my_profile_new;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (dataVerifiedMember.getVisibility() == View.VISIBLE) {
            savedInstanceState.putBoolean("isVerifiedMember", true);
            if (ktp != null) {
                savedInstanceState.putSerializable("KTP", ktp);
            }
            if (selfie != null) {
                savedInstanceState.putSerializable("selfieKtp", selfie);
            }
            if (ttd != null) {
                savedInstanceState.putSerializable("TTD", ttd);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isVerifiedMember = savedInstanceState.getBoolean("isVerifiedMember");
        ktp = (File) savedInstanceState.getSerializable("KTP");
        selfie = (File) savedInstanceState.getSerializable("selfieKtp");
        ttd = (File) savedInstanceState.getSerializable("TTD");

        if (isVerifiedMember) {
            btn1.setVisibility(View.GONE);
            dataVerifiedMember.setVisibility(View.VISIBLE);
            if (ktp != null) {
                Timber.d("ktp :" + ktp);
                GlideManager.sharedInstance().initializeGlideProfile(MyProfileNewActivity.this, ktp, cameraKTP);
                uploadFileToServer(ktp, KTP_TYPE);
            }

            if (selfie != null) {
                GlideManager.sharedInstance().initializeGlideProfile(MyProfileNewActivity.this, selfie, selfieKTP);
                uploadFileToServer(selfie, SELFIE_TYPE);
            }

            if (ttd != null) {
                GlideManager.sharedInstance().initializeGlideProfile(MyProfileNewActivity.this, ttd, cameraTTD);
                uploadFileToServer(ttd, TTD_TYPE);
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickAndCameraUtil = new PickAndCameraUtil(this);
        gson = new Gson();

        Intent intent = getIntent();
        if (intent.hasExtra(DefineValue.IS_FIRST)) {
            is_first_time = intent.getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);
        }

        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        is_new_bulk = sp.getString(DefineValue.IS_NEW_BULK, "N");
        reject_KTP = sp.getString(DefineValue.REJECT_KTP, "N");
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP, "N");
        reject_selfie = sp.getString(DefineValue.REJECT_FOTO, "N");
        reject_ttd = sp.getString(DefineValue.REJECT_TTD, "N");
        respon_reject_ktp = sp.getString(DefineValue.REMARK_KTP, "");
        respon_reject_selfie = sp.getString(DefineValue.REMARK_FOTO, "");
        respon_reject_ttd = sp.getString(DefineValue.REMARK_TTD, "");
        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");
        isUpgradeAgent = sp.getBoolean(DefineValue.IS_UPGRADE_AGENT, false);

        initializeToolbar();

        View v = this.findViewById(android.R.id.content);

        assert v != null;
        dataMemberBasic = findViewById(R.id.data_member_basic);
        dataVerifiedMember = findViewById(R.id.data_verified_member);
        layoutKTP = findViewById(R.id.layout_foto_ktp);
        layoutSelfie = findViewById(R.id.layout_selfie);
        layoutTTD = findViewById(R.id.layout_ttd);
        pb1 = v.findViewById(R.id.pb1_myprofileactivity);
        pb2 = v.findViewById(R.id.pb2_myprofileactivity);
        pb3 = v.findViewById(R.id.pb3_myprofileactivity);
        tv_pb1 = v.findViewById(R.id.tv_pb1_myprofileactivity);
        tv_pb2 = v.findViewById(R.id.tv_pb2_myprofileactivity);
        tv_pb3 = v.findViewById(R.id.tv_pb3_myprofileactivity);
        tv_dob = v.findViewById(R.id.myprofile_value_dob);
        tv_verified_member = v.findViewById(R.id.group_title2);
        tv_respon_reject_KTP = v.findViewById(R.id.tv_respon_reject_ktp);
        tv_respon_reject_selfie = v.findViewById(R.id.tv_respon_reject_selfie);
        tv_respon_reject_ttd = v.findViewById(R.id.tv_respon_reject_ttd);
        et_noHp = v.findViewById(R.id.myprofile_value_hp);
        et_nama = v.findViewById(R.id.myprofile_value_name);
        et_email = v.findViewById(R.id.myprofile_value_email);
        et_acctNo = v.findViewById(R.id.bank_acc_no);
        cameraKTP = v.findViewById(R.id.camera_ktp);
        selfieKTP = v.findViewById(R.id.camera_selfie_ktp);
        cameraTTD = v.findViewById(R.id.camera_ttd);
        sp_bank = v.findViewById(R.id.spinner_nameBank);
        btn1 = v.findViewById(R.id.button1);
        btn2 = v.findViewById(R.id.button2);
        btn2.setEnabled(false);
        btn2.setBackground(getResources().getDrawable(R.drawable.rounded_background_button_disabled));
        lytVerifiedMember = v.findViewById(R.id.lyt_verifying_member);
        cb_termsncond = v.findViewById(R.id.cb_termnsncond);

        levelClass = new LevelClass(this, sp);
        adapter = new BankCashoutAdapter(this, android.R.layout.simple_spinner_item);
        sp_bank.setAdapter(adapter);
        sp_bank.setOnItemSelectedListener(spinnerNamaBankListener);
//        if(levelClass.isLevel1QAC() && isRegisteredLevel) { DialogSuccessUploadPhoto(); }

        if (!is_agent && !levelClass.isLevel1QAC() && !isUpgradeAgent) {
            androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(MyProfileNewActivity.this);
            builder1.setTitle(R.string.level_dialog_agent);
            builder1.setMessage(R.string.level_dialog_agent1);
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    getString(R.string.level_dialog_btn_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent intent = new Intent(MyProfileNewActivity.this, UpgradeAgentActivity.class);
                            startActivity(intent);
                        }
                    });

            builder1.setNegativeButton(
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            tv_dob.setEnabled(false);
//                            if (is_first_time) {
//                                RESULT = MainPage.RESULT_FIRST_TIME;
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                                finish();
//                            } else {
//                                Intent intent1 = new Intent(MyProfileNewActivity.this, MainPage.class);
//                                startActivity(intent1);
//                            }
                            dialog.dismiss();
                        }
                    });

            androidx.appcompat.app.AlertDialog alert11 = builder1.create();
            alert11.show();
        }

        if (!is_first_time) {
            tv_dob.setEnabled(false);
        }

        if (levelClass.isLevel1QAC()) {
            btn1.setVisibility(View.GONE);

        }

        if (!levelClass.isLevel1QAC() || is_agent) {
            et_nama.setEnabled(false);
            tv_dob.setEnabled(false);
            tv_verified_member.setText("Data Verified Member Sudah Terverifikasi");
            dataVerifiedMember.setVisibility(View.GONE);
            cameraKTP.setEnabled(false);
            selfieKTP.setEnabled(false);
            cameraTTD.setEnabled(false);
            btn2.setVisibility(View.GONE);
            if (is_new_bulk.equals("Y")) {
                btn1.setVisibility(View.VISIBLE);
            } else
                btn1.setVisibility(View.GONE);

        }

        if (isUpgradeAgent && !is_agent) {
            DialogWaitingUpgradeAgent();
        }

        if (is_agent && reject_npwp.equalsIgnoreCase("")) {
            finish();
            Intent intent1 = new Intent(MyProfileNewActivity.this, UpgradeAgentActivity.class);
            startActivity(intent1);
        }

        cb_termsncond.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn2.setEnabled(true);
                    btn2.setBackground(getResources().getDrawable(R.drawable.rounded_background_blue));
                } else {
                    btn2.setEnabled(false);
                    btn2.setBackground(getResources().getDrawable(R.drawable.rounded_background_button_disabled));
                }
            }
        });

        dataMemberBasic.setOnClickListener(member_basic_click);
        dataVerifiedMember.setOnClickListener(verified_member_click);
        tv_dob.setOnClickListener(textDOBListener);
        btn1.setOnClickListener(nextListener);
        btn2.setOnClickListener(submitListener);
        cameraKTP.setOnClickListener(setImageCameraKTP);
        selfieKTP.setOnClickListener(setImageSelfieKTP);
        cameraTTD.setOnClickListener(setImageCameraTTD);

        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        toFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID", "INDONESIA"));
        toFormat2 = new SimpleDateFormat("dd-M-yyyy", new Locale("ID", "INDONESIA"));

        Calendar c = Calendar.getInstance();
        dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:" + dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        if (reject_KTP.equals("Y") || reject_selfie.equals("Y") || reject_ttd.equals("Y")) {
            et_nama.setEnabled(false);
            tv_dob.setEnabled(false);
            btn1.setVisibility(View.GONE);
            dataVerifiedMember.setVisibility(View.VISIBLE);
            btn2.setVisibility(View.VISIBLE);

            if (reject_KTP.equals("Y")) {
                cameraKTP.setEnabled(true);
                tv_respon_reject_KTP.setText("Alasan : " + respon_reject_ktp);
            } else layoutKTP.setVisibility(View.GONE);

            if (reject_selfie.equals("Y")) {
                selfieKTP.setEnabled(true);
                tv_respon_reject_selfie.setText("Alasan : " + respon_reject_selfie);
            } else layoutSelfie.setVisibility(View.GONE);

            if (reject_ttd.equals("Y")) {
                cameraTTD.setEnabled(true);
                tv_respon_reject_ttd.setText("Alasan : " + respon_reject_ttd);
            } else layoutTTD.setVisibility(View.GONE);
        }

        initializeData();
        getBankCashout();
    }


    private void initializeToolbar() {
        if (is_first_time) disableHomeIcon();
        else {
            setActionBarIcon(R.drawable.ic_arrow_left);
        }
        setActionBarTitle(getString(R.string.data_customer));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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

            dpd.show(getSupportFragmentManager(), "Datepickerdialog");
        }
    };

    private TextView.OnClickListener member_basic_click = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private TextView.OnClickListener verified_member_click = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private Button.OnClickListener nextListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inputValidation()) {
                sendDataUpdate();
            }
        }
    };

    private Button.OnClickListener submitListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ValidationPhoto() && bankValidation()) {
                sentExecCust();
            }
        }
    };

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker dob");
            try {
                date_dob = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : " + date_dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
        }
    };

    private ImageButton.OnClickListener setImageCameraKTP = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraKTP di MyprofileactivityNew");
            set_result_photo = RESULT_CAMERA_KTP;
            camera_dialog();
        }
    };
    private ImageButton.OnClickListener setImageSelfieKTP = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageSelfieKTP di MyprofileactivityNew");
            set_result_photo = RESULT_SELFIE;
            camera_dialog();
        }
    };
    private ImageButton.OnClickListener setImageCameraTTD = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraTTD di MyprofileactivityNew");
            set_result_photo = RESULT_CAMERA_TTD;
            camera_dialog();
        }
    };

    Spinner.OnItemSelectedListener spinnerNamaBankListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {
            BankCashoutModel model = listBankCashOut.get(i);
            bankCode = model.getBank_code();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    public void camera_dialog() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //final String[] items = {"Choose from Gallery", "Take a Photo"};
            final String[] items = {"Take a Photo"};

            android.app.AlertDialog.Builder a = new android.app.AlertDialog.Builder(MyProfileNewActivity.this);
            a.setCancelable(true);
            a.setTitle("Choose Profile Picture");
            a.setAdapter(new ArrayAdapter<>(MyProfileNewActivity.this, android.R.layout.simple_list_item_1, items),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
//                            if (which == 0) {
//                                if (set_result_photo == RESULT_CAMERA_KTP) {
//                                    pickAndCameraUtil.chooseGallery(RESULT_GALLERY_KTP);
//                                } else if (set_result_photo == RESULT_SELFIE) {
//                                    pickAndCameraUtil.chooseGallery(RESULT_GALLERY_SELFIE);
//                                } else if (set_result_photo == RESULT_CAMERA_TTD) {
//                                    pickAndCameraUtil.chooseGallery(RESULT_GALLERY_TTD);
//                                }
//                            } else
                            if (which == 0) {
                                if (set_result_photo == RESULT_CAMERA_KTP || set_result_photo == RESULT_CAMERA_TTD) {
                                    CameraActivity.openCertificateCamera(MyProfileNewActivity.this, CameraActivity.TYPE_COMPANY_PORTRAIT);
                                } else {
                                    pickAndCameraUtil.runCamera(set_result_photo);
                                }
                            }

                        }
                    }
            );
            a.create();
            a.show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE, perms);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void initializeData() {
        et_noHp.setText(sp.getString(DefineValue.CUST_ID, ""));
        et_noHp.setEnabled(false);
        et_nama.setText(sp.getString(DefineValue.PROFILE_FULL_NAME, ""));
        et_nama.setEnabled(false);
        et_email.setText(sp.getString(DefineValue.PROFILE_EMAIL, ""));
        if (is_new_bulk.equals("Y")) {
            et_email.setEnabled(true);
        } else {
            et_email.setEnabled(false);
        }


        dedate = sp.getString(DefineValue.PROFILE_DOB, "");
        if (dedate.equals("")) {
            tv_dob.setEnabled(true);
            btn1.setVisibility(View.VISIBLE);

        } else {
            Timber.d("TEST Log lvl...." + levelClass.isLevel1QAC());
            if (levelClass.isLevel1QAC() && !isRegisteredLevel) {
                lytVerifiedMember.setVisibility(View.VISIBLE);
            }
        }

        if (!dedate.equals("")) {
            Calendar c = Calendar.getInstance();

            try {
                c.setTime(fromFormat.parse(dedate));
                dedate = toFormat.format(fromFormat.parse(dedate));
                date_dob = fromFormat.format(toFormat2.parse(dedate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_dob.setText(dedate);
//
//            dpd = DatePickerDialog.newInstance(
//                    dobPickerSetListener,
//                    c.get(Calendar.YEAR),
//                    c.get(Calendar.MONTH),
//                    c.get(Calendar.DAY_OF_MONTH)
//            );
        }
        is_verified = sp.getInt(DefineValue.PROFILE_VERIFIED, 0) == 1;
        Timber.d("isi is verified:" + String.valueOf(sp.getInt(DefineValue.PROFILE_VERIFIED, 0)) + " " + is_verified);
    }

    private void sendDataUpdate() {
        try {
            if (progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(MyProfileNewActivity.this, "");
            else
                progdialog.show();
            String extraSignature = memberIDLogin;
            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_UPDATE_PROFILE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.USER_ID, et_noHp.getText().toString());
            params.put(WebParams.EMAIL, et_email.getText().toString());
            params.put(WebParams.FULL_NAME, et_nama.getText().toString());
            params.put(WebParams.MOTHER_NAME, et_nama.getText().toString());
            if (dedate.equals("")) params.put(WebParams.DOB, "");
            else params.put(WebParams.DOB, date_dob);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.IS_REGISTER, "N");
            params.put(WebParams.SOCIAL_ID, "");
            params.put(WebParams.POB, "");
            params.put(WebParams.ID_TYPE, "");

//            if(!CountryModel.allCountry[0].equals(tempCountry))
//                params.put(WebParams.COUNTRY,tempCountry);
//            else
            params.put(WebParams.COUNTRY, "");

            params.put(WebParams.ADDRESS, "");
//            if(tempHobby.equals(list_hobby[0])) params.put(WebParams.HOBBY,"");
//            else
            params.put(WebParams.HOBBY, "");
//
//            if(spinner_gender.getSelectedItemPosition()==0)
//                params.put(WebParams.GENDER, gender_value[0]);
//            else
//                params.put(WebParams.GENDER, gender_value[1]);
            params.put(WebParams.GENDER, "");

            params.put(WebParams.BIO, "");

            Timber.d("isi params update profile:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_UPDATE_PROFILE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateProfileModel model = gson.fromJson(object, UpdateProfileModel.class);

                            String code = model.getError_code();
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                sp.edit().putString(DefineValue.IS_NEW_BULK, "N");
                                setLoginProfile(model);
                                Toast.makeText(MyProfileNewActivity.this, getString(R.string.myprofile_toast_update_success), Toast.LENGTH_LONG).show();
                                sp.edit().putString(DefineValue.IS_FIRST, DefineValue.NO).apply();
//                                    Timber.d("isi response Update Profile:"+ response.toString());
//                                if (levelClass.isLevel1QAC()) {
//                                    android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MyProfileNewActivity.this);
//                                    builder1.setTitle(R.string.upgrade_member);
//                                    builder1.setMessage(R.string.message_upgrade_member);
//                                    builder1.setCancelable(true);
//
//                                    builder1.setPositiveButton(
//                                            "Yes",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    dataVerifiedMember.setVisibility(View.VISIBLE);
//                                                    et_nama.setEnabled(false);
//                                                    tv_dob.setEnabled(false);
//                                                    btn1.setVisibility(View.GONE);
//                                                    if (is_first_time) {
//                                                        setResult(MainPage.RESULT_FIRST_TIME);
//                                                    }
//                                                }
//                                            });
//
//                                    builder1.setNegativeButton(
//                                            "No",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    tv_dob.setEnabled(false);
//                                                    if (is_first_time) {
//                                                        RESULT = MainPage.RESULT_FIRST_TIME;
//                                                        setResult(MainPage.RESULT_FIRST_TIME);
//                                                        finish();
//                                                    } else
//                                                        finish();
//                                                }
//                                            });
//
//                                    android.support.v7.app.AlertDialog alert11 = builder1.create();
//                                    alert11.show();
//                                }
//                                else {
                                finish();
//                                }
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(MyProfileNewActivity.this, message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(MyProfileNewActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(MyProfileNewActivity.this, model.getError_message());
                            } else {
                                code = model.getError_message();
                                Toast.makeText(MyProfileNewActivity.this, code, Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        if (is_first_time) {
            RESULT = MainPage.RESULT_FIRST_TIME;
        } else {
            RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
            closethis();
        }
    }

    private void setLoginProfile(UpdateProfileModel response) {
        SecurePreferences prefs = sp;
        SecurePreferences.Editor mEditor = prefs.edit();

        mEditor.putString(DefineValue.PROFILE_DOB, response.getDate_of_birth());
        mEditor.putString(DefineValue.PROFILE_EMAIL, response.getEmail());
        mEditor.putString(DefineValue.PROFILE_FULL_NAME, response.getFull_name());
        mEditor.putString(DefineValue.PROFILE_BOM, response.getFull_name());
        mEditor.putString(DefineValue.CUST_NAME, response.getFull_name());
        mEditor.putString(DefineValue.USER_NAME, response.getFull_name());
        mEditor.putString(DefineValue.MEMBER_NAME, response.getFull_name());
        mEditor.putString(DefineValue.IS_NEW_BULK, "N");
        mEditor.putBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        is_verified = Integer.valueOf(response.getVerified()) == 1;
        mEditor.putString(DefineValue.PROFILE_VERIFIED, response.getVerified());
        mEditor.apply();
    }

    private boolean inputValidation() {

        int compare = 100;
        if (date_dob != null) {
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
            Timber.d("compare date:" + Integer.toString(compare));
        }

        if (et_nama.getText().toString().length() == 0) {
            et_nama.requestFocus();
            et_nama.setError(getResources().getString(R.string.regist1_validation_nama));
            return false;
        } else if (et_email.getText().toString().length() == 0) {
            et_email.requestFocus();
            et_email.setError(getResources().getString(R.string.regist1_validation_email_length));
            return false;
        } else if (et_email.getText().toString().length() > 0 && !isValidEmail(et_email.getText())) {
            et_email.requestFocus();
            et_email.setError(getString(R.string.regist1_validation_email));
            return false;
        } else if (compare == 100) {
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
        } else if (compare >= 0) {
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

    public Boolean ValidationPhoto() {
        if (layoutKTP.getVisibility() == View.VISIBLE) {
            if (ktp == null) {
                DefinedDialog.showErrorDialog(MyProfileNewActivity.this, getString(R.string.ktp_photo));
                return false;
            }
        }
        if (layoutSelfie.getVisibility() == View.VISIBLE) {
            if (selfie == null) {
                DefinedDialog.showErrorDialog(MyProfileNewActivity.this, getString(R.string.selfie_ktp_photo));
                return false;
            }
        }
        if (layoutTTD.getVisibility() == View.VISIBLE) {
            if (ttd == null) {
                DefinedDialog.showErrorDialog(MyProfileNewActivity.this, getString(R.string.ttd_photo));
                return false;
            }
        }

        return true;
    }

    private boolean bankValidation() {
        if (et_acctNo.getText().toString().length() == 0) {
            et_acctNo.requestFocus();
            et_acctNo.setError(getResources().getString(R.string.cashout_accno_validation));
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GALLERY_KTP:
                if (resultCode == RESULT_OK) {
                    new ImageCompressionAsyncTask(KTP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_KTP:
                if (resultCode == RESULT_OK) {
                    if (pickAndCameraUtil.getCaptureImageUri() != null) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            new ImageCompressionAsyncTask(KTP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                        } else {
                            new ImageCompressionAsyncTask(KTP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                        }
                    } else {
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_GALLERY_SELFIE:
                if (resultCode == RESULT_OK) {
                    new ImageCompressionAsyncTask(SELFIE_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_SELFIE:
                if (resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri() != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new ImageCompressionAsyncTask(SELFIE_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    } else {
                        new ImageCompressionAsyncTask(SELFIE_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
                } else {
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            case RESULT_GALLERY_TTD:
                if (resultCode == RESULT_OK) {
                    new ImageCompressionAsyncTask(TTD_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_TTD:
                if (resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri() != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new ImageCompressionAsyncTask(TTD_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    } else {
                        new ImageCompressionAsyncTask(TTD_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
                } else {
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            case CameraActivity.REQUEST_CODE:
                if (data != null) {
                    if (CameraActivity.getResult(data) != null) {
                        final String path = CameraActivity.getResult(data);
                        if (set_result_photo == RESULT_CAMERA_KTP) {
                            new ImageCompressionAsyncTask(KTP_TYPE).execute(path);
                        } else {
                            new ImageCompressionAsyncTask(TTD_TYPE).execute(path);
                        }
                    } else {
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                    }
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

        extraSignature = String.valueOf(flag);

        HashMap<String, RequestBody> params = RetrofitService.getInstance()
                .getSignature2(MyApiClient.LINK_UPLOAD_KTP, extraSignature);

        RequestBody request1 = RequestBody.create(MediaType.parse("text/plain"),
                et_noHp.getText().toString());
        RequestBody request2 = RequestBody.create(MediaType.parse("text/plain"),
                MyApiClient.COMM_ID);
        RequestBody request3 = RequestBody.create(MediaType.parse("text/plain"),
                String.valueOf(flag));
        RequestBody request4 = RequestBody.create(MediaType.parse("text/plain"),
                userPhoneID);

        params.put(WebParams.USER_ID, request1);
//        params.put(WebParams.USER_IMAGES, photoFile);
        params.put(WebParams.COMM_ID, request2);
        params.put(WebParams.TYPE, request3);
        params.put(WebParams.CUST_ID, request4);
        Timber.d("params upload foto ktp: " + params.toString());
        Timber.d("params upload foto type: " + flag);

//                RequestBody.create(MediaType.parse("image/*"), photoFile);
        RequestBody requestFile = new ProgressRequestBody(photoFile,
                new ProgressRequestBody.UploadCallbacks() {
                    @Override
                    public void onProgressUpdate(int percentage) {
                        switch (flag) {
                            case KTP_TYPE:
                                pb1.setProgress(percentage);
                                break;
                            case SELFIE_TYPE:
                                pb2.setProgress(percentage);
                                break;
                            case TTD_TYPE:
                                pb3.setProgress(percentage);
                                break;
                        }
                    }
                });

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(WebParams.USER_IMAGES, photoFile.getName(),
                requestFile);

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_KTP, params, filePart,
                new ObjListener() {
                    @Override
                    public void onResponses(JsonObject object) {

                        UploadFotoModel model = gson.fromJson(object, UploadFotoModel.class);

                        String error_code = model.getError_code();
                        String error_message = model.getError_message();
                        if (error_code.equalsIgnoreCase("0000")) {

                            switch (flag) {
                                case KTP_TYPE:
                                    pb1.setProgress(100);
                                    BlinkingEffectClass.blink(layoutKTP);
                                    break;
                                case SELFIE_TYPE:
                                    pb2.setProgress(100);
                                    BlinkingEffectClass.blink(layoutSelfie);
                                    break;
                                case TTD_TYPE:
                                    pb3.setProgress(100);
                                    BlinkingEffectClass.blink(layoutTTD);
                                    break;
                            }

                            Timber.d("onsuccess upload foto type: " + flag);
//                                Timber.d("isi response Upload Foto:"+ response.toString());

                        } else if (error_code.equals(WebParams.LOGOUT_CODE)) {

                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(MyProfileNewActivity.this, error_message);
                        } else if (error_code.equals(DefineValue.ERROR_9333)) {
                            Timber.d("isi response app data:" + model.getApp_data());
                            final AppDataModel appModel = model.getApp_data();
                            AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                            alertDialogUpdateApp.showDialogUpdate(MyProfileNewActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                        } else if (error_code.equals(DefineValue.ERROR_0066)) {
                            Timber.d("isi response maintenance:" + object.toString());
                            AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                            alertDialogMaintenance.showDialogMaintenance(MyProfileNewActivity.this, model.getError_message());
                        } else {
                            Toast.makeText(MyProfileNewActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();


                        }
                    }
                });

    }

    private void DialogSuccessUploadPhoto() {
        Dialog dialognya = DefinedDialog.MessageDialog(MyProfileNewActivity.this, this.getString(R.string.level_dialog_finish_title),
                this.getString(R.string.level_dialog_waiting),
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        finish();
                        Intent i = new Intent(getApplicationContext(),MainPage.class);
                        startActivityForResult(i, MainPage.REQUEST_FINISH);
                    }
                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    private void DialogWaitingUpgradeAgent() {
        Dialog dialognya = DefinedDialog.MessageDialog(MyProfileNewActivity.this, this.getString(R.string.upgrade_agent_dialog_finish_title),
                this.getString(R.string.level_dialog_waiting),
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        finish();
                    }
                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

    }

    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void closethis() {
        setResult(RESULT);
        this.finish();
    }

    private void sentExecCust() {
        try {

            if (progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(MyProfileNewActivity.this, "");
            else
                progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance()
                    .getSignature(MyApiClient.LINK_EXEC_CUST, memberIDLogin);
            params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
            params.put(WebParams.CUST_NAME, et_nama.getText().toString());
            params.put(WebParams.CUST_ID_TYPE, "");
            params.put(WebParams.CUST_ID_NUMBER, "");
            params.put(WebParams.CUST_ADDRESS, "");
            params.put(WebParams.CUST_COUNTRY, "");
            params.put(WebParams.CUST_BIRTH_PLACE, "");
            params.put(WebParams.CUST_MOTHER_NAME, et_nama.getText().toString());
            params.put(WebParams.CUST_CONTACT_EMAIL, et_email.getText().toString());
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.IS_REGISTER, "Y");
            params.put(WebParams.CUST_BIRTH_DATE, date_dob);
            params.put(WebParams.CUST_GENDER, "");
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.BANK_CODE, bankCode);
            params.put(WebParams.SOURCE_ACCT_NO, et_acctNo.getText().toString());

            Timber.d("isi params execute customer:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_EXEC_CUST, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            SentExecCustModel model = gson.fromJson(object, SentExecCustModel.class);

                            String code = model.getError_code();
//                                Timber.d("response execute customer:"+response.toString());
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
                                mEdit.putBoolean(DefineValue.IS_REGISTERED_LEVEL, true);
                                mEdit.putString(DefineValue.PROFILE_DOB, tv_dob.getText().toString());
                                mEdit.putString(DefineValue.PROFILE_FULL_NAME, et_nama.getText().toString());
                                mEdit.putString(DefineValue.CUST_NAME, et_nama.getText().toString());
                                mEdit.putString(DefineValue.USER_NAME, et_nama.getText().toString());
                                mEdit.putString(DefineValue.MEMBER_NAME, et_nama.getText().toString());
//                            mEdit.putInt(DefineValue.LEVEL_VALUE, response.optInt(WebParams.MEMBER_LEVEL, 1));
                                if (model.getAllow_member_level().equals(DefineValue.STRING_YES))
                                    mEdit.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL, true);
                                else
                                    mEdit.putBoolean(DefineValue.ALLOW_MEMBER_LEVEL, false);

                                mEdit.apply();
                                DialogSuccessUploadPhoto();
                            } else if (code.equals(WebParams.LOGOUT_CODE)) {
//                                    Timber.d("isi response autologout:"+response.toString());
                                String message = model.getError_message();
                                AlertDialogLogout test = AlertDialogLogout.getInstance();
                                test.showDialoginActivity(MyProfileNewActivity.this, message);
                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(MyProfileNewActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(MyProfileNewActivity.this, model.getError_message());
                            } else {
                                code = model.getError_message();

                                Toast.makeText(MyProfileNewActivity.this, code, Toast.LENGTH_LONG).show();
                                getFragmentManager().popBackStack();
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

    public class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {
        private int type;


        ImageCompressionAsyncTask(int type) {
            this.type = type;
        }

        @Override
        protected File doInBackground(String... params) {
            return pickAndCameraUtil.compressImage(params[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            switch (type) {
                case KTP_TYPE:
                    GlideManager.sharedInstance().initializeGlideProfile(MyProfileNewActivity.this, file, cameraKTP);
//                    Picasso.with(MyProfileNewActivity.this).load(file).centerCrop().fit().into(cameraKTP);
                    ktp = file;
                    uploadFileToServer(ktp, KTP_TYPE);
                    break;
                case SELFIE_TYPE:
                    GlideManager.sharedInstance().initializeGlideProfile(MyProfileNewActivity.this, file, selfieKTP);
                    selfie = file;
                    uploadFileToServer(selfie, SELFIE_TYPE);
                    break;
                case TTD_TYPE:
                    GlideManager.sharedInstance().initializeGlideProfile(MyProfileNewActivity.this, file, cameraTTD);
                    ttd = file;
                    uploadFileToServer(ttd, TTD_TYPE);
                    break;
            }
        }
    }

    public void getBankCashout() {
        try {
            final ProgressDialog prodDialog = DefinedDialog.CreateProgressDialog(this, "");

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BANKCASHOUT, memberIDLogin);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.USER_ID, userPhoneID);

            Timber.d("isi params get Bank cashout:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_BANKCASHOUT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {

                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(object.toString(), jsonModel.class);

                            Log.e("getBankCashout", object.get("bank_cashout").toString());

                            Type type = new TypeToken<List<BankCashoutModel>>() {
                            }.getType();
                            Gson gson2 = new Gson();
                            listBankCashOut = gson2.fromJson(object.get("bank_cashout"), type);

                            Log.e("getBankCashout", listBankCashOut.toString());

                            adapter.updateAdapter(listBankCashOut);

                            if (object.get("error_code").equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(MyProfileNewActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (object.get("error_code").equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + object.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(MyProfileNewActivity.this, model.getError_message());
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

        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }
}
