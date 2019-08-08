package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.NoHPFormat;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.ScanQRUtils;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.UploadPPModel;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.widgets.ProgressRequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class ActivityProfileQr extends BaseActivity implements ProgressRequestBody.UploadCallbacks {
    // DATA
    String sourceAcct = "", sourceAcctName ="" , lvlMember = "";
    private LevelClass levelClass;
    private boolean is_first_time = false;
    private String reject_npwp;
    private String listContactPhone = "";
    private String listAddress = "";
    private String contactCenter="";
    private String userID;
    private boolean is_agent = false;//saat antri untuk diverifikasi
    private boolean isUpgradeAgent =false; //saat antri untuk diverifikasi upgrade agent
    private boolean isRegisteredLevel = false;
    private ImageView custImage;
    private DateFormat fromFormat;
    private DateFormat dobFormat;
    private PickAndCameraUtil pickAndCameraUtil;
    private final int RESULT_GALERY = 100;
    private final int RESULT_CAMERA = 200;
    final int RC_CAMERA_STORAGE = 14;

    // UI LAYOUT
    TextView tv_name, tv_phone_no, tv_lvl_member_value, currencyLimit, limitValue, tv_email, tv_dob;
    CardView btn_upgrade;
    ImageView imageQR;
    ProgressDialog progdialog;
    RelativeLayout lytUpgrade,lytDetail;
    LinearLayout llBalanceDetail;

    // Listener
    RelativeLayout.OnClickListener detailOnClick = new RelativeLayout.OnClickListener() {
        @Override
        public void onClick(View v) {

//            if (isUpgradeAgent && !is_agent)
//            {
                showDialogMessage();
//            }
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile_qr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(this,sp);
        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        dobFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID", "INDONESIA"));
        pickAndCameraUtil = new PickAndCameraUtil(ActivityProfileQr.this);


        InitializeToolbar();
        initData();

        checkContactCenter();

        initLayout();
        checkAgent();
        setView();

    }

    private String getLvl(){

        int tempLvl = sp.getInt(DefineValue.LEVEL_VALUE,1);
        boolean isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        if(isAgent){
            return getString(R.string.lbl_member_lvl_agent);
        }else{
            if(tempLvl == 1){
                return getString(R.string.lbl_member_lvl_silver);
            } else if(tempLvl == 2){
                return getString(R.string.lbl_member_lvl_gold);
            }
        }
        return "";
    }
    private void initData() {
        if(getIntent() != null){
            sourceAcct =  NoHPFormat.formatTo08(sp.getString(DefineValue.USERID_PHONE,""));;
            sourceAcctName = sp.getString(DefineValue.CUST_NAME,"");
            contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER,"");

            if(getIntent().hasExtra(DefineValue.IS_FIRST)) {
                is_first_time = getIntent().getStringExtra(DefineValue.IS_FIRST).equals(DefineValue.YES);
            }
        }

        isRegisteredLevel = sp.getBoolean(DefineValue.IS_REGISTERED_LEVEL, false);
        isUpgradeAgent = sp.getBoolean(DefineValue.IS_UPGRADE_AGENT, false);
        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP,"N");

    }

    private void setView() {

        imageQR.setImageBitmap(ScanQRUtils.getInstance(this).generateQRCode(DefineValue.QR_TYPE_FROM_DEFAULT_ACCOUNT,sourceAcct,sourceAcctName));
        if(isShowUpgradeStatus()){
            viewOnProggressUpgrade();
        }else{
            hideOnProgUpgrade();
        }

    }

    private void initLayout() {
        tv_name = findViewById(R.id.tv_name);
        tv_phone_no = findViewById(R.id.tv_phone_no);
        tv_lvl_member_value = findViewById(R.id.tv_lvl_member_value);
        tv_email=findViewById(R.id.tv_current_email);
        tv_dob=findViewById(R.id.tv_dob);
        btn_upgrade = findViewById(R.id.btn_upgrade);
        imageQR = findViewById(R.id.iv_qr);
        lytUpgrade = findViewById(R.id.lyt_upgrade_detail);
        lytDetail = findViewById(R.id.lyt_detail);
        custImage = findViewById(R.id.cust_image);
        llBalanceDetail = findViewById(R.id.llBalanceDetail);
        currencyLimit = findViewById(R.id.currency_limit_value);
        limitValue = findViewById(R.id.limit_value);


        tv_name.setText(sourceAcctName);
        tv_phone_no.setText(sourceAcct);
        tv_email.setText(sp.getString(DefineValue.PROFILE_EMAIL, ""));
        try {
            tv_dob.setText(dobFormat.format(fromFormat.parse(sp.getString(DefineValue.PROFILE_DOB, ""))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tv_lvl_member_value.setText(getLvl());
        currencyLimit.setText(sp.getString(DefineValue.BALANCE_CCYID, ""));
        limitValue.setText(CurrencyFormat.format(sp.getString(DefineValue.BALANCE_REMAIN_LIMIT, "")));
        setImageProfPic();


        viewOnProggressUpgrade();
        if (!sp.getBoolean(DefineValue.IS_AGENT, false)){
            llBalanceDetail.setVisibility(View.VISIBLE);
        }else {
            llBalanceDetail.setVisibility(View.GONE);
        }

        if (!levelClass.isLevel1QAC())
        {
            lytUpgrade.setVisibility(View.GONE);
            btn_upgrade.setVisibility(View.GONE);
        }


        btn_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Toast.makeText(ActivityProfileQr.this,"We're sorry this feature currently unavailable",Toast.LENGTH_SHORT).show();
                checkIsLv1();

//                checkIsLv2();


            }
        });
        custImage.setOnClickListener(v -> {
            final String[] items = {"Choose from Gallery", "Take a Photo"};

            AlertDialog.Builder a = new AlertDialog.Builder(ActivityProfileQr.this);
            a.setCancelable(true);
            a.setTitle("Choose Profile Picture");
            a.setAdapter(new ArrayAdapter<>(ActivityProfileQr.this, android.R.layout.simple_list_item_1, items),
                    (dialog, which) -> {
                        if (which == 0) {
                            Timber.wtf("masuk gallery");
                            pickAndCameraUtil.chooseGallery(RESULT_GALERY);
                        } else if (which == 1) {
                            chooseCamera();
                        }

                    }
            );
            a.create();
            a.show();
        });
    }

    private boolean isShowUpgradeStatus(){

        if(levelClass.isLevel1QAC() && isRegisteredLevel){
            return true;
        }

        if (isUpgradeAgent && !is_agent)
        {
            return true;
        }
        return false;
    }

    private void InitializeToolbar(){

        setActionBarTitle(getString(R.string.lbl_member_saya));
        setActionBarIcon(R.drawable.ic_arrow_left);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount()>0)
            getFragmentManager().popBackStack();
        else super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()>0)
                    getFragmentManager().popBackStack();
                else finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchViewUpgradeVerified(){
        Intent i = new Intent(this,UpgradeMemberActivity.class);
        startActivity(i);
    }


    private void viewOnProggressUpgrade(){
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorSecondaryWhiteFixed));
        btn_upgrade.setEnabled(false);

        lytUpgrade.setVisibility(View.VISIBLE);
        lytDetail.setOnClickListener(detailOnClick);

    }
    private void hideOnProgUpgrade(){
        btn_upgrade.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        btn_upgrade.setEnabled(true);
        lytUpgrade.setVisibility(View.GONE);

    }
    private void checkIsLv1(){



        if(levelClass.isLevel1QAC())
        {
            android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(ActivityProfileQr.this);
            builder1.setTitle(R.string.upgrade_member);
            builder1.setMessage(R.string.message_upgrade_member);
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            switchViewUpgradeVerified();
//                            dataVerifiedMember.setVisibility(View.VISIBLE);
//                            et_nama.setEnabled(false);
//                            tv_dob.setEnabled(false);
//                            btn1.setVisibility(View.GONE);
//                            if(is_first_time) {
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                            }
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.dismiss();
//                            tv_dob.setEnabled(false);
//                            if(is_first_time) {
//                                RESULT = MainPage.RESULT_FIRST_TIME;
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                                finish();
//                            }else
//                                finish();
                        }
                    });

            android.support.v7.app.AlertDialog alert11 = builder1.create();
            alert11.show();

        }
    }

    private void checkIsLv2(){
        if (!is_agent && !levelClass.isLevel1QAC() && !isUpgradeAgent)
        {
            android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(ActivityProfileQr.this);
            builder1.setTitle(R.string.upgrade_agent);
            builder1.setMessage(R.string.message_upgrade_agent);
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

//                            checkAgent();

                            finish();
                            Intent intent = new Intent(ActivityProfileQr.this, UpgradeAgentActivity.class);
                            startActivity(intent);
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.dismiss();
//                            if(is_first_time) {
//                                RESULT = MainPage.RESULT_FIRST_TIME;
//                                setResult(MainPage.RESULT_FIRST_TIME);
//                                finish();
//                            }else
//                                finish();
                        }
                    });

            android.support.v7.app.AlertDialog alert11 = builder1.create();
            alert11.show();
        }


    }



    private void checkAgent(){

        if (is_agent && !levelClass.isLevel1QAC() && !isUpgradeAgent)
        {
            lytUpgrade.setVisibility(View.GONE);
            btn_upgrade.setVisibility(View.GONE);
        }else if(is_agent && !reject_npwp.isEmpty()){
            finish();
            Intent intent1 = new Intent(ActivityProfileQr.this, UpgradeAgentActivity.class);
            startActivity(intent1);
        }
    }

    private void showDialogMessage()
    {

        final Dialog dialognya = DefinedDialog.MessageDialog(ActivityProfileQr.this, this.getString(R.string.upgrade_dialog_finish_title),
                this.getString(R.string.level_dialog_finish_message) + "\n" + listAddress + "\n" +
                        this.getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {

                    }
                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    private void checkContactCenter(){

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
    }


    private void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(this, "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_USER_CONTACT_INSERT);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_USER_CONTACT_INSERT, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
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
                                    test.showDialoginActivity(ActivityProfileQr.this,message);
                                }
                                else {
                                    Timber.d("isi error help list:"+response.toString());
                                    Toast.makeText(ActivityProfileQr.this, message, Toast.LENGTH_LONG).show();
                                }



                            } catch (JSONException e) {
                                e.printStackTrace();
                                Timber.d("Error JSON catch contact:"+e.toString());
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }
        catch (Exception e){
            if(progdialog.isShowing())
                progdialog.dismiss();
            Timber.d("httpclient:"+e.getMessage());
        }
    }
    private void setImageProfPic() {
        String _url_profpic;
        _url_profpic = sp.getString(DefineValue.IMG_URL, null);

        Timber.wtf("url prof pic:" + _url_profpic);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        if (_url_profpic != null && _url_profpic.isEmpty()) {
            GlideManager.sharedInstance().initializeGlide(ActivityProfileQr.this, R.drawable.user_unknown_menu, roundedImage, custImage);
        } else {
            GlideManager.sharedInstance().initializeGlide(ActivityProfileQr.this, _url_profpic, roundedImage, custImage);
        }

    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    private void chooseCamera() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getApplicationContext(), perms)) {
            pickAndCameraUtil.runCamera(RESULT_CAMERA);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GALERY:
                if (resultCode == RESULT_OK) {
                    new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA:
                if (resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri() != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    } else {
                        new ImageCompressionAsyncTask().execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
                } else {
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        Log.d("okhttp", "percentage :" + percentage);
        if (progdialog.isShowing())
            progdialog.setProgress(percentage);
    }

    private class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            return pickAndCameraUtil.compressImage(params[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            uploadFileToServer(file);
        }
    }
    private void uploadFileToServer(File photoFile) {

        progdialog = DefinedDialog.CreateProgressDialog(ActivityProfileQr.this, "");

        if (accessKey == null)
            accessKey = sp.getString(DefineValue.ACCESS_KEY, "");

        if (userID == null)
            userID = sp.getString(DefineValue.USERID_PHONE, "");

        HashMap<String, RequestBody> params2 = RetrofitService.getInstance()
                .getSignature2(MyApiClient.LINK_UPLOAD_PROFILE_PIC, "");

        RequestBody requestFile =
                new ProgressRequestBody(photoFile, ActivityProfileQr.this);
//                RequestBody.create(MediaType.parse("image/*"), photoFile);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(WebParams.USER_FILE, photoFile.getName(),
                requestFile);
        RequestBody req1 = RequestBody.create(MediaType.parse("text/plain"),
                sp.getString(DefineValue.CUST_ID, ""));
        RequestBody req2 = RequestBody.create(MediaType.parse("text/plain"),
                MyApiClient.COMM_ID);

        params2.put(WebParams.USER_ID, req1);
        params2.put(WebParams.COMM_ID, req2);

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_PROFILE_PIC, params2, filePart,
                object -> {
                    UploadPPModel model = gson.fromJson(object, UploadPPModel.class);

                    String error_code = model.getError_code();
                    String error_message = model.getError_message();
//                            Timber.d("response upload profile picture:" + response.toString());
                    if (error_code.equalsIgnoreCase("0000")) {
                        SecurePreferences.Editor mEditor = sp.edit();

                        mEditor.putString(DefineValue.IMG_URL, model.getImg_url());
                        mEditor.putString(DefineValue.IMG_SMALL_URL, model.getImg_small_url());
                        mEditor.putString(DefineValue.IMG_MEDIUM_URL, model.getImg_medium_url());
                        mEditor.putString(DefineValue.IMG_LARGE_URL, model.getImg_large_url());

                        mEditor.commit();

                        setImageProfPic();
                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
//                                Timber.d("isi response autologout:" + response.toString());
//                                String message = response.getString(WebParams.ERROR_MESSAGE);

                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(ActivityProfileQr.this, error_message);
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityProfileQr.this);
                        alert.setTitle("Upload Image");
                        alert.setMessage("Upload Image : " + error_message);
                        alert.setPositiveButton("OK", null);
                        alert.show();

                    }

                    progdialog.dismiss();

                });
    }
}
