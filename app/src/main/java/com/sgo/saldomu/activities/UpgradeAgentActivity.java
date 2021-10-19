package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.permissionx.guolindev.PermissionX;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.widgets.BlinkingEffectClass;
import com.sgo.saldomu.widgets.ProgressRequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.shaohui.advancedluban.Luban;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class UpgradeAgentActivity extends BaseActivity {
    private final int SIUP_TYPE = 5;
    private final int NPWP_TYPE = 4;
    final int RC_CAMERA_STORAGE = 14;
    final int RC_GALLERY = 15;
    final int RC_CAMERA = 16;
    private final int RESULT_GALLERY_SIUP = 104;
    private final int RESULT_GALLERY_NPWP = 105;
    private final int RESULT_CAMERA_SIUP = 204;
    private final int RESULT_CAMERA_NPWP = 205;
    private final int RESULT_CROP = 301;
    private ProgressBar pbSIUP, pbNPWP;
    private ImageButton cameraSIUP, cameraNPWP;
    File siup, npwp;
    private boolean is_agent = false;
    private ProgressDialog progdialog;
    private PickAndCameraUtil pickAndCameraUtil;
    Button btn_proses;
    TextView tv_pb_siup, tv_pb_npwp, tv_reject_siup, tv_reject_npwp;
    private int RESULT;
    private Integer set_result_photo;
    RelativeLayout layout_siup, layout_npwp;
    private EditText et_mothersName;
    String reject_siup, reject_npwp, remark_siup, remark_npwp;
    CheckBox cb_termsncond;
    private File picFile = null, compressFile = null;
    Runnable runnable;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_upgrade_agent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAndCameraUtil = new PickAndCameraUtil(this);

        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        reject_siup = sp.getString(DefineValue.REJECT_SIUP, DefineValue.STRING_NO);
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP, DefineValue.STRING_NO);
        remark_siup = sp.getString(DefineValue.REMARK_SIUP, "");
        remark_npwp = sp.getString(DefineValue.REMARK_NPWP, "");

//        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");

        View v = this.findViewById(android.R.id.content);
        pbSIUP = v.findViewById(R.id.pb1_upgradeAgent);
        pbNPWP = v.findViewById(R.id.pb2_upgradeAgent);
        cameraSIUP = v.findViewById(R.id.camera_siup);
        cameraNPWP = v.findViewById(R.id.camera_npwp);
        btn_proses = v.findViewById(R.id.button_proses);
        btn_proses.setEnabled(false);
        btn_proses.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.grey_300, null));
        tv_pb_siup = v.findViewById(R.id.tv_pb1_upgradeAgent);
        tv_pb_npwp = v.findViewById(R.id.tv_pb2_upgradeAgent);
        tv_reject_siup = v.findViewById(R.id.tv_respon_reject_siup);
        tv_reject_npwp = v.findViewById(R.id.tv_respon_reject_npwp);
        layout_siup = v.findViewById(R.id.layout_foto_siup);
        layout_npwp = v.findViewById(R.id.layout_npwp);
        cb_termsncond = v.findViewById(R.id.cb_termnsncond);
        et_mothersName = v.findViewById(R.id.et_mothersname);
        cameraSIUP.setOnClickListener(setImageCameraSIUP);
        cameraNPWP.setOnClickListener(setImageCameraNPWP);
        btn_proses.setOnClickListener(prosesListener);

//        if (contactCenter.equals("")) {
//            getHelpList();
//        } else {
//            try {
//                JSONArray arrayContact = new JSONArray(contactCenter);
//                for (int i = 0; i < arrayContact.length(); i++) {
//                    if (i == 0) {
//                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
//                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        initializeToolbar();

        cb_termsncond.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btn_proses.setEnabled(true);
                btn_proses.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.colorPrimaryDark, null));
            } else {
                btn_proses.setEnabled(false);
                btn_proses.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.grey_300, null));
            }
        });

        if (reject_siup.equalsIgnoreCase(DefineValue.STRING_YES) || reject_npwp.equalsIgnoreCase(DefineValue.STRING_YES)) {
            if (reject_siup.equalsIgnoreCase(DefineValue.STRING_YES)) {
                cameraSIUP.setEnabled(true);
                tv_reject_siup.setText("Alasan : " + remark_siup);
            } else layout_siup.setVisibility(View.GONE);

            if (reject_npwp.equalsIgnoreCase(DefineValue.STRING_YES)) {
                cameraNPWP.setEnabled(true);
                tv_reject_npwp.setText("Alasan : " + remark_npwp);
            } else layout_npwp.setVisibility(View.GONE);
        }

        if (is_agent && reject_npwp.equalsIgnoreCase("")) {
            layout_siup.setVisibility(View.GONE);
            layout_npwp.setVisibility(View.VISIBLE);
        }
    }

    private ImageButton.OnClickListener setImageCameraSIUP = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraSIUP");
            set_result_photo = RESULT_CAMERA_SIUP;
            camera_dialog();
        }
    };

    private ImageButton.OnClickListener setImageCameraNPWP = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraSIUP");
            set_result_photo = RESULT_CAMERA_NPWP;
            camera_dialog();
        }
    };

    private Button.OnClickListener prosesListener = v -> {
        if (validationPhoto()) {
            sentExecAgent();
        }
    };

    public Boolean validationPhoto() {
        if (et_mothersName.getText().toString().length() == 0) {
            et_mothersName.requestFocus();
            et_mothersName.setError(this.getString(R.string.validation_text_birth_mother));
            return false;
        } else if (layout_siup.getVisibility() == View.VISIBLE || reject_siup.equalsIgnoreCase(DefineValue.STRING_YES)) {
            if (siup == null) {
                DefinedDialog.showErrorDialog(UpgradeAgentActivity.this, "Foto SIUP/Surat Keterangan RT/RW tidak boleh kosong!");
                return false;
            }
        } else if (reject_npwp.equalsIgnoreCase(DefineValue.STRING_YES)) {
            if (npwp == null) {
                DefinedDialog.showErrorDialog(UpgradeAgentActivity.this, "Foto NPWP tidak boleh kosong!");
                return false;
            }
        }

        return true;
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    public void camera_dialog() {
        PermissionX.init(this).permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onForwardToSettings((scope, deniedList) -> {
                    String message = "Please allow following permissions in settings";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        final String[] items = {"Choose from Gallery", "Take a Photo"};

                        AlertDialog.Builder a = new AlertDialog.Builder(UpgradeAgentActivity.this);
                        a.setCancelable(true);
                        a.setTitle("Choose Profile Picture");
                        a.setAdapter(new ArrayAdapter<>(UpgradeAgentActivity.this, android.R.layout.simple_list_item_1, items),
                                (dialog, which) -> {
                                    if (which == 0) {
                                        if (set_result_photo == RESULT_CAMERA_SIUP) {
                                            pickAndCameraUtil.chooseGallery(RESULT_GALLERY_SIUP);
                                        } else if (set_result_photo == RESULT_CAMERA_NPWP) {
                                            pickAndCameraUtil.chooseGallery(RESULT_GALLERY_NPWP);
                                        }
                                    } else if (which == 1) {
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                                            RxImagePicker.with(this).requestImage(Sources.CAMERA)
                                                    .flatMap(new Function<Uri, ObservableSource<File>>() {
                                                        @Override
                                                        public ObservableSource<File> apply(@NonNull Uri uri) throws Exception {
                                                            return RxImageConverters.uriToFile(getApplicationContext(), uri, prepareUploadFileTemp());
                                                        }
                                                    }).subscribe(new Consumer<File>() {
                                                @Override
                                                public void accept(@NonNull File file) throws Exception {
                                                    // Do something with your file copy
                                                    picFile = file;
                                                    if (set_result_photo == RESULT_CAMERA_SIUP)
                                                        convertImage(SIUP_TYPE);
                                                    else if (set_result_photo == RESULT_CAMERA_NPWP)
                                                        convertImage(NPWP_TYPE);
                                                }
                                            });
                                        } else
                                            pickAndCameraUtil.runCamera(set_result_photo);
                                    }

                                }
                        );
                        a.create();
                        a.show();
                    }
                });
    }

    private void convertImage(int flag) {
        int fileSize = Integer.parseInt(String.valueOf(picFile.length() / 1024));
        Timber.tag("TAG").e("size: %s", fileSize);
        if (fileSize > 500) {
            Luban.compress(this, picFile)
                    .setMaxSize(500)
                    .putGear(Luban.CUSTOM_GEAR)
                    .asObservable()
                    .subscribe(new Observer<File>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(File file) {
                            compressFile = file;
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            uploadFileToServer(compressFile, flag);
                        }
                    });
        } else {
            compressFile = picFile;
            uploadFileToServer(compressFile, flag);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onBackPressed() {
        RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
        closethis();
    }

    private class ImageCompressionAsyncTask extends AsyncTask<String, Void, File> {
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
                case SIUP_TYPE:
                    GlideManager.sharedInstance().initializeGlideProfile(UpgradeAgentActivity.this, file, cameraSIUP);
                    siup = file;
                    uploadFileToServer(siup, SIUP_TYPE);
                    pbSIUP.setProgress(0);
                    break;
                case NPWP_TYPE:
                    GlideManager.sharedInstance().initializeGlideProfile(UpgradeAgentActivity.this, file, cameraNPWP);
                    npwp = file;
                    uploadFileToServer(npwp, NPWP_TYPE);
                    pbNPWP.setProgress(0);
                    break;
            }
        }
    }

    private void uploadFileToServer(File photoFile, final int flag) {
        pbSIUP.setVisibility(View.VISIBLE);
        pbNPWP.setVisibility(View.VISIBLE);
        tv_pb_siup.setVisibility(View.VISIBLE);
        tv_pb_npwp.setVisibility(View.VISIBLE);
        tv_reject_siup.setVisibility(View.GONE);
        tv_reject_npwp.setVisibility(View.GONE);

        extraSignature = String.valueOf(flag);

        HashMap<String, RequestBody> params = RetrofitService.getInstance()
                .getSignature2(MyApiClient.LINK_UPLOAD_SIUP_NPWP, extraSignature);

        RequestBody requestFile = new ProgressRequestBody(photoFile,
                percentage -> {
                    switch (flag) {
                        case SIUP_TYPE:
                            pbSIUP.setProgress(percentage);
                            break;
                        case NPWP_TYPE:
                            pbNPWP.setProgress(percentage);
                            break;
                    }

                });
//                RequestBody.create(MediaType.parse("image/*"), photoFile);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(WebParams.USER_IMAGES, photoFile.getName(),
                requestFile);
        RequestBody userPhone = RequestBody.create(MediaType.parse("text/plain"),
                userPhoneID);
        RequestBody commid = RequestBody.create(MediaType.parse("text/plain"),
                MyApiClient.COMM_ID);
        RequestBody flags = RequestBody.create(MediaType.parse("text/plain"),
                String.valueOf(flag));

        params.put(WebParams.USER_ID, userPhone);
//            params.put(WebParams.USER_IMAGES, photoFile);
        params.put(WebParams.COMM_ID, commid);
        params.put(WebParams.TYPE, flags);
        Timber.d("params upload foto: %s", params.toString());
        Timber.d("params upload foto type: %s", flag);

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_SIUP_NPWP, params, filePart,
                object -> {

                    Gson gson = new Gson();
                    jsonModel model = gson.fromJson(object, jsonModel.class);

                    String error_code = model.getError_code();
                    String error_message = model.getError_message();
                    if (error_code.equalsIgnoreCase("0000")) {
                        switch (flag) {
                            case SIUP_TYPE:
                                pbSIUP.setProgress(100);
                                BlinkingEffectClass.blink(layout_siup);
                                break;
                            case NPWP_TYPE:
                                pbNPWP.setProgress(100);
                                BlinkingEffectClass.blink(layout_npwp);
                                break;
                        }

                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {
                        AlertDialogLogout.getInstance().showDialoginActivity(UpgradeAgentActivity.this, error_message);
                    } else if (error_code.equals(DefineValue.ERROR_9333)) {
                        Timber.d("isi response app data:%s", model.getApp_data());
                        final AppDataModel appModel = model.getApp_data();
                        AlertDialogUpdateApp.getInstance().showDialogUpdate(UpgradeAgentActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                    } else if (error_code.equals(DefineValue.ERROR_0066)) {
                        Timber.d("isi response maintenance:%s", object.toString());
                        AlertDialogMaintenance.getInstance().showDialogMaintenance(UpgradeAgentActivity.this);
                    } else {
                        Timber.d("Masuk failure");
                        if (MyApiClient.PROD_FAILURE_FLAG) {
                            Timber.d("Masuk if prod failure flag");
                            Toast.makeText(UpgradeAgentActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                            if (flag == SIUP_TYPE) {
                                Timber.d("masuk failure siup");
                                pbSIUP.setProgress(0);
                                tv_pb_siup.setText("0 %");
                            }
                            if (flag == NPWP_TYPE) {
                                Timber.d("masuk failure npwp");
                                pbNPWP.setProgress(0);
                                tv_pb_npwp.setText("0 %");
                            }
                        } else {
                            Toast.makeText(UpgradeAgentActivity.this, error_message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    private void sentExecAgent() {
        try {

            if (progdialog == null)
                progdialog = DefinedDialog.CreateProgressDialog(UpgradeAgentActivity.this, "");
            else
                progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_EXEC_AGENT, memberIDLogin);
            params.put(WebParams.CUST_ID, sp.getString(DefineValue.CUST_ID, ""));
            params.put(WebParams.MEMBER_ID, memberIDLogin);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.MOTHER_NAME, et_mothersName.getText().toString());

            Timber.d("isi params execute agent:%s", params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_EXEC_AGENT, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                Gson gson = new Gson();
                                jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);
                                Timber.d("response execute agent:%s", response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    SecurePreferences.Editor mEdit = sp.edit();
                                    mEdit.putBoolean(DefineValue.IS_UPGRADE_AGENT, true);
                                    mEdit.remove(DefineValue.REJECT_SIUP);
                                    mEdit.remove(DefineValue.REJECT_NPWP);
                                    mEdit.remove(DefineValue.REMARK_SIUP);
                                    mEdit.remove(DefineValue.REMARK_NPWP);
                                    mEdit.remove(DefineValue.REMARK_NPWP);
                                    mEdit.remove(DefineValue.MODEL_NOTIF);
                                    mEdit.apply();
                                    DialogSuccessUploadPhoto();
                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:%s", response.toString());
                                    AlertDialogLogout.getInstance().showDialoginActivity(UpgradeAgentActivity.this, message);
                                } else if (code.equals(DefineValue.ERROR_9333)) {
                                    Timber.d("isi response app data:%s", model.getApp_data());
                                    final AppDataModel appModel = model.getApp_data();
                                    AlertDialogUpdateApp.getInstance().showDialogUpdate(UpgradeAgentActivity.this, appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                                } else if (code.equals(DefineValue.ERROR_0066)) {
                                    Timber.d("isi response maintenance:%s", response.toString());
                                    AlertDialogMaintenance.getInstance().showDialogMaintenance(UpgradeAgentActivity.this);
                                } else {
                                    Toast.makeText(UpgradeAgentActivity.this, message, Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack();
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
            Timber.d("httpclient:%s", e.getMessage());
        }
    }

    private void DialogSuccessUploadPhoto() {
        Dialog dialognya = DefinedDialog.MessageDialog(UpgradeAgentActivity.this, this.getString(R.string.upgrade_agent_dialog_finish_title),
                this.getString(R.string.level_dialog_waiting),
                () -> finish()
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Handler handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Timber.tag("TAG").e("call: ");
                if (requestCode == RESULT_GALLERY_SIUP)
                    convertImage(SIUP_TYPE);
                else if (requestCode == RESULT_GALLERY_NPWP)
                    convertImage(NPWP_TYPE);
                handler.removeCallbacks(this);
            }
        };
        switch (requestCode) {
            case RESULT_GALLERY_SIUP:
            case RESULT_GALLERY_NPWP:
                if (resultCode == RESULT_OK) {
                    try {
                        RxImageConverters.uriToFile(this, data.getData(), prepareUploadFileTemp())
                                .subscribe(file -> {
                                    Timber.tag("TAG").e("accept: ");
                                    picFile = file;
                                    handler.postDelayed(runnable, 2000);
                                });
                    } catch (IOException e) {
                        Timber.tag("TAG").e("err");
                        e.printStackTrace();
                    }
                }
                break;
            case RESULT_CAMERA_SIUP:
                if (resultCode == RESULT_OK) {
                    if (pickAndCameraUtil.getCaptureImageUri() != null) {
                        new ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                    } else {
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_CAMERA_NPWP:
                if (resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri() != null) {
                    new ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                } else {
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void closethis() {
        setResult(RESULT);
        this.finish();
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.upgrade_agent));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String prepareFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + "_";
    }

    private static File createImageFile() throws IOException {
        String imageFileName = prepareFileName();
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), BuildConfig.APP_ID + "Image.JPEG");
        storageDir.mkdirs();

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null;
            }
        }
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static File prepareUploadFileTemp() throws IOException {
        return createImageFile();
    }
}

