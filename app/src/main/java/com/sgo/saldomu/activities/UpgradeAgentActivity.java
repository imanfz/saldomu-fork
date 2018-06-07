package com.sgo.saldomu.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.utils.PickAndCameraUtil;
import com.sgo.saldomu.widgets.BaseActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class UpgradeAgentActivity extends BaseActivity {
    private final int SIUP_TYPE = 1;
    private final int NPWP_TYPE = 2;
    final int RC_CAMERA_STORAGE = 14;
    final int RC_GALLERY = 15;
    private final int RESULT_GALLERY_SIUP = 104;
    private final int RESULT_GALLERY_NPWP = 105;
    private final int RESULT_CAMERA_SIUP = 204;
    private final int RESULT_CAMERA_NPWP = 205;
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

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_upgrade_agent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAndCameraUtil = new PickAndCameraUtil(this);

        is_agent = sp.getBoolean(DefineValue.IS_AGENT, false);

        View v = this.findViewById(android.R.id.content);
        pbSIUP = v.findViewById(R.id.pb1_upgradeAgent);
        pbNPWP = v.findViewById(R.id.pb2_upgradeAgent);
        cameraSIUP = v.findViewById(R.id.camera_siup);
        cameraNPWP = v.findViewById(R.id.camera_npwp);
        btn_proses = v.findViewById(R.id.button_proses);
        tv_pb_siup = v.findViewById(R.id.tv_pb1_upgradeAgent);
        tv_pb_npwp = v.findViewById(R.id.tv_pb2_upgradeAgent);
        tv_reject_siup = v.findViewById(R.id.tv_respon_reject_siup);
        tv_reject_npwp = v.findViewById(R.id.tv_respon_reject_npwp);
        cameraSIUP.setOnClickListener(setImageCameraSIUP);
        cameraNPWP.setOnClickListener(setImageCameraNPWP);

    }

    private ImageButton.OnClickListener setImageCameraSIUP= new ImageButton.OnClickListener ()
    {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraSIUP");
            set_result_photo = RESULT_CAMERA_SIUP;
            camera_dialog();
        }
    };

    private ImageButton.OnClickListener setImageCameraNPWP= new ImageButton.OnClickListener ()
    {
        @Override
        public void onClick(View v) {
            Timber.d("Masuk ke setImageCameraSIUP");
            set_result_photo = RESULT_CAMERA_NPWP;
            camera_dialog();
        }
    };

    @AfterPermissionGranted(RC_CAMERA_STORAGE)
    public void camera_dialog()
    {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this,perms)) {
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
                            }

                        }
                    }
            );
            a.create();
            a.show();
        }else {
            EasyPermissions.requestPermissions(this,getString(R.string.rationale_camera_and_storage),
                    RC_CAMERA_STORAGE,perms);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onBackPressed() {
            RESULT = MainPage.RESULT_REFRESH_NAVDRAW;
            closethis();
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
                case SIUP_TYPE :
                    GlideManager.sharedInstance().initializeGlideProfile(UpgradeAgentActivity.this, file,cameraSIUP);
//                    Picasso.with(MyProfileNewActivity.this).load(file).centerCrop().fit().into(cameraKTP);
                    siup = file;
                    uploadFileToServer(siup, SIUP_TYPE);
                    break;
                case NPWP_TYPE :
                    GlideManager.sharedInstance().initializeGlideProfile(UpgradeAgentActivity.this, file,cameraNPWP);
//                    Picasso.with(MyProfileNewActivity.this).load(file).centerCrop().fit().into(selfieKTP);
                    npwp = file;
                    uploadFileToServer(npwp, NPWP_TYPE);
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

        RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_UPLOAD_KTP,
                userPhoneID,accessKey,extraSignature);
        try {
            params.put(WebParams.USER_ID,userPhoneID);
            params.put(WebParams.USER_IMAGES, photoFile);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.TYPE, flag);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Timber.d("params upload foto: " + params.toString());
        Timber.d("params upload foto type: " + flag);

        MyApiClient.sentPhotoKTP(this, params, new JsonHttpResponseHandler() {

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                Timber.d("sebelum proses uploadSIUPNPWP " +bytesWritten);
                Timber.d("sebelum proses uploadSIUPNPWP " +totalSize);
                proses = (int) (100 * bytesWritten / totalSize);
                if(proses < 100 || proses == 100)
                {
                    if(flag==SIUP_TYPE)
                    {
                        Timber.d("sebelum proses uploadSIUP" +proses);
                        pbSIUP.setProgress((int) (100 * bytesWritten / totalSize));
                        Timber.d("proses uploadSIUP " +proses);
                        tv_pb_siup.setText(proses + "%");
                    }
                    else if(flag==NPWP_TYPE)
                    {
                        pbNPWP.setProgress((int) (100 * bytesWritten / totalSize));
                        tv_pb_npwp.setText(proses + "%");
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
                        Timber.d("isi response Upload Foto SIUP NPWP:"+ response.toString());

                    } else if (error_code.equals(WebParams.LOGOUT_CODE)) {

                        Timber.d("isi response autologout:" + response.toString());
                        String message = response.getString(WebParams.ERROR_MESSAGE);

                        AlertDialogLogout test = AlertDialogLogout.getInstance();
                        test.showDialoginActivity(UpgradeAgentActivity.this, message);
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
                    Toast.makeText(UpgradeAgentActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    if(flag==1)
                    {
                        Timber.d("masuk failure siup");
                        pbSIUP.setProgress( 0 );
                        tv_pb_siup.setText("0 %");
                    }
                    if (flag==2)
                    {
                        Timber.d("masuk failure npwp");
                        pbNPWP.setProgress( 0);
                        tv_pb_npwp.setText("0 %");

                    }
                }

                else
                    Toast.makeText(UpgradeAgentActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                Timber.w("Error Koneksi data update foto siup npwp:" + throwable.toString());
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RESULT_GALLERY_SIUP:
                if(resultCode == RESULT_OK){
                    new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_SIUP:
                if(resultCode == RESULT_OK){
                    if( pickAndCameraUtil.getCaptureImageUri()!=null){
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                        }
                        else {
                            new UpgradeAgentActivity.ImageCompressionAsyncTask(SIUP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
                        }
                    }
                    else{
                        Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_GALLERY_NPWP:
                if(resultCode == RESULT_OK){
                    new UpgradeAgentActivity.ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(data.getDataString()));
                }
                break;
            case RESULT_CAMERA_NPWP :
                if(resultCode == RESULT_OK && pickAndCameraUtil.getCaptureImageUri()!=null){
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new UpgradeAgentActivity.ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getRealPathFromURI(pickAndCameraUtil.getCaptureImageUri()));
                    }
                    else {
                        new UpgradeAgentActivity.ImageCompressionAsyncTask(NPWP_TYPE).execute(pickAndCameraUtil.getCurrentPhotoPath());
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

    private void closethis(){
        setResult(RESULT);
        this.finish();
    }
}

