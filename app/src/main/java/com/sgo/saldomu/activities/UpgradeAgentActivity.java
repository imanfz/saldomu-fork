package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.widgets.BaseActivity;
import com.sgo.saldomu.widgets.BlinkingEffectClass;
import com.sgo.saldomu.widgets.ProgressRequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

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
    private Integer proses;
    private Integer set_result_photo;
    RelativeLayout layout_siup, layout_npwp;
    private String contactCenter;
    private String listContactPhone = "";
    private String listAddress = "";
    private EditText et_mothersName;
    String reject_siup, reject_npwp, remark_siup, remark_npwp;
    CheckBox cb_termsncond;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_upgrade_agent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAndCameraUtil = new PickAndCameraUtil(this);

        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);
        reject_siup = sp.getString(DefineValue.REJECT_SIUP, "N");
        reject_npwp = sp.getString(DefineValue.REJECT_NPWP, "N");
        remark_siup = sp.getString(DefineValue.REMARK_SIUP, "");
        remark_npwp = sp.getString(DefineValue.REMARK_NPWP, "");

        contactCenter = sp.getString(DefineValue.LIST_CONTACT_CENTER, "");

        View v = this.findViewById(android.R.id.content);
        pbSIUP = v.findViewById(R.id.pb1_upgradeAgent);
        pbNPWP = v.findViewById(R.id.pb2_upgradeAgent);
        cameraSIUP = v.findViewById(R.id.camera_siup);
        cameraNPWP = v.findViewById(R.id.camera_npwp);
        btn_proses = v.findViewById(R.id.button_proses);
        btn_proses.setEnabled(false);
        btn_proses.setBackground(getResources().getDrawable(R.color.grey_300));
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

        if (contactCenter.equals("")) {
            getHelpList();
        } else {
            try {
                JSONArray arrayContact = new JSONArray(contactCenter);
                for (int i = 0; i < arrayContact.length(); i++) {
                    if (i == 0) {
                        listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                        listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        InitializeToolbar();

        cb_termsncond.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn_proses.setEnabled(true);
                    btn_proses.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
                }else{
                    btn_proses.setEnabled(false);
                    btn_proses.setBackground(getResources().getDrawable(R.color.grey_300));
                }
            }
        });

        if (reject_siup.equalsIgnoreCase("Y") || reject_npwp.equalsIgnoreCase("Y")) {
            if (reject_siup.equalsIgnoreCase("Y")) {
                cameraSIUP.setEnabled(true);
                tv_reject_siup.setText("Alasan : " + remark_siup);
            } else layout_siup.setVisibility(View.GONE);

            if (reject_npwp.equalsIgnoreCase("Y")) {
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

    private Button.OnClickListener prosesListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validationPhoto()) {
                sentExecAgent();
            }
        }
    };

    private void getHelpList() {
        try {
            showProgressDialog();

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
                                    Timber.d("isi params help list:" + response.toString());

                                    contactCenter = response.getString(WebParams.CONTACT_DATA);

                                    SecurePreferences.Editor mEditor = sp.edit();
                                    mEditor.putString(DefineValue.LIST_CONTACT_CENTER, response.getString(WebParams.CONTACT_DATA));
                                    mEditor.apply();

                                    try {
                                        JSONArray arrayContact = new JSONArray(contactCenter);
                                        for (int i = 0; i < arrayContact.length(); i++) {
                                            if (i == 0) {
                                                listContactPhone = arrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE);
                                                listAddress = arrayContact.getJSONObject(i).getString(WebParams.ADDRESS);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else if (code.equals(WebParams.LOGOUT_CODE)) {
                                    Timber.d("isi response autologout:" + response.toString());
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(UpgradeAgentActivity.this, message);
                                } else {
                                    Timber.d("isi error help list:" + response.toString());
                                    Toast.makeText(UpgradeAgentActivity.this, message, Toast.LENGTH_LONG).show();
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
                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    public Boolean validationPhoto() {
        if (et_mothersName.getText().toString().length() == 0) {
            et_mothersName.requestFocus();
            et_mothersName.setError(this.getString(R.string.validation_text_birth_mother));
            return false;
        } else if (layout_siup.getVisibility() == View.VISIBLE || reject_siup.equalsIgnoreCase("Y")) {
            if (siup == null) {
                DefinedDialog.showErrorDialog(UpgradeAgentActivity.this, "Foto SIUP/Surat Keterangan RT/RW tidak boleh kosong!");
                return false;
            }
        } else if (reject_npwp.equalsIgnoreCase("Y")) {
            if (npwp == null) {
                DefinedDialog.showErrorDialog(UpgradeAgentActivity.this, "Foto NPWP tidak boleh kosong!");
                return false;
            }
        }

        return true;
    }

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    public void camera_dialog() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            final String[] items = {"Choose from Gallery", "Take a Photo"};

            android.app.AlertDialog.Builder a = new android.app.AlertDialog.Builder(UpgradeAgentActivity.this);
            a.setCancelable(true);
            a.setTitle("Choose Profile Picture");
            a.setAdapter(new ArrayAdapter<>(UpgradeAgentActivity.this, android.R.layout.simple_list_item_1, items),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                if (set_result_photo == RESULT_CAMERA_SIUP) {
                                    pickAndCameraUtil.chooseGallery(RESULT_GALLERY_SIUP);
                                } else if (set_result_photo == RESULT_CAMERA_NPWP) {
                                    pickAndCameraUtil.chooseGallery(RESULT_GALLERY_NPWP);
                                }
                            } else if (which == 1) {
                                pickAndCameraUtil.runCamera(set_result_photo);
//                                Intent intent=new Intent(getApplicationContext(),CameraViewActivity.class);
//                                startActivityForResult(intent,set_result_photo);

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
                    uploadFileToServer(type, siup, SIUP_TYPE);
                    pbSIUP.setProgress(0);
                    break;
                case NPWP_TYPE:
                    GlideManager.sharedInstance().initializeGlideProfile(UpgradeAgentActivity.this, file, cameraNPWP);
                    npwp = file;
                    uploadFileToServer(type, npwp, NPWP_TYPE);
                    pbNPWP.setProgress(0);
                    break;
            }
        }
    }

    private void uploadFileToServer(int type, File photoFile, final int flag) {
        int _type = type;

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
                new ProgressRequestBody.UploadCallbacks() {
                    @Override
                    public void onProgressUpdate(int percentage) {
                        switch (_type) {
                            case SIUP_TYPE:
                                pbSIUP.setProgress(percentage);
                                break;
                            case NPWP_TYPE:
                                pbNPWP.setProgress(percentage);
                                break;
                        }

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
        Timber.d("params upload foto: " + params.toString());
        Timber.d("params upload foto type: " + flag);

        RetrofitService.getInstance().MultiPartRequest(MyApiClient.LINK_UPLOAD_SIUP_NPWP, params, filePart,
                new ObjListener() {
                    @Override
                    public void onResponses(JsonObject object) {

                        Gson gson = new Gson();
                        jsonModel model = gson.fromJson(object, jsonModel.class);

                        String error_code = model.getError_code();
                        String error_message = model.getError_message();
                        if (error_code.equalsIgnoreCase("0000")) {
                            switch (_type) {
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
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(UpgradeAgentActivity.this, error_message);
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

            Timber.d("isi params execute agent:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_EXEC_AGENT, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("response execute agent:" + response.toString());
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
                                    Timber.d("isi response autologout:" + response.toString());
                                    String message = response.getString(WebParams.ERROR_MESSAGE);
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(UpgradeAgentActivity.this, message);
                                } else {
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(UpgradeAgentActivity.this, code, Toast.LENGTH_LONG).show();
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
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void DialogSuccessUploadPhoto() {
        Dialog dialognya = DefinedDialog.MessageDialog(UpgradeAgentActivity.this, this.getString(R.string.upgrade_agent_dialog_finish_title),
                this.getString(R.string.level_dialog_finish_message) + listAddress + "\n" +
                        this.getString(R.string.level_dialog_finish_message_2) + "\n" + listContactPhone,
                new DefinedDialog.DialogButtonListener() {
                    @Override
                    public void onClickButton(View v, boolean isLongClick) {
                        finish();
                    }
                }
        );

        dialognya.setCanceledOnTouchOutside(false);
        dialognya.setCancelable(false);

        dialognya.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_GALLERY_SIUP:
                if (resultCode == RESULT_OK) {
                    new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_SIUP:
                if (resultCode == RESULT_OK) {
                    if (pickAndCameraUtil.getCaptureImageUri() != null) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                        } else {
                            new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
//                            new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                        }
                    } else {
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_GALLERY_NPWP:
                if (resultCode == RESULT_OK) {
                    new UpgradeAgentActivity.ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_NPWP:
                if (resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri() != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new UpgradeAgentActivity.ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    } else {
                        new UpgradeAgentActivity.ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                    }
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

    private void InitializeToolbar() {
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
}

