package com.sgo.hpku.coreclass;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.sgo.hpku.BuildConfig;
import com.sgo.hpku.R;
import com.sgo.hpku.dialogs.DefinedDialog;

import java.io.File;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 6/2/16.
 */
public abstract class CameraClass extends BaseActivity {

    public final static String TAG = CameraClass.class.getName();
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 115;

    private static final String DATE_CAMERA_INTENT_STARTED_STATE = BuildConfig.APPLICATION_ID+".android.photo.TakePhotoActivity.dateCameraIntentStarted";
    private static Date dateCameraIntentStarted = null;
    private static final String CAMERA_PIC_URI_STATE = BuildConfig.APPLICATION_ID+".android.photo.TakePhotoActivity.CAMERA_PIC_URI_STATE";
    private static Uri cameraPicUri = null;
    private static final String ROTATE_X_DEGREES_STATE = BuildConfig.APPLICATION_ID+".android.photo.TakePhotoActivity.ROTATE_X_DEGREES_STATE";
    private static int rotateXDegrees = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void startCameraIntent() {
        try {
            dateCameraIntentStarted = new Date();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //NOTE: Do NOT SET: intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPicUri) on Samsung Galaxy S2/S3/.. for the following reasons:
            // 1.) it will break the correct picture orientation
            // 2.) the photo will be stored in two locations (the given path and additionally in the MediaStore)
            String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
            if(!(manufacturer.contains("samsung")) && !(manufacturer.contains("sony"))) {
                String filename = System.currentTimeMillis() + ".jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, filename);
                cameraPicUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPicUri);
            }
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            showWarningDialog(getString(R.string.toast_error_could_not_take_photo));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (dateCameraIntentStarted != null) {
            savedInstanceState.putString(DATE_CAMERA_INTENT_STARTED_STATE,
                    DateTimeFormat.convertDatetoString(dateCameraIntentStarted));
        }
        if (cameraPicUri != null) {
            savedInstanceState.putString(CAMERA_PIC_URI_STATE, cameraPicUri.toString());
        }
        savedInstanceState.putInt(ROTATE_X_DEGREES_STATE, rotateXDegrees);
    }

    @Override
    public void  onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(DATE_CAMERA_INTENT_STARTED_STATE)) {
            dateCameraIntentStarted = DateTimeFormat.convertStringtoCustomDateTime(DATE_CAMERA_INTENT_STARTED_STATE);
        }
        if (savedInstanceState.containsKey(CAMERA_PIC_URI_STATE)) {
            cameraPicUri = Uri.parse(savedInstanceState.getString(CAMERA_PIC_URI_STATE));
        }
        rotateXDegrees = savedInstanceState.getInt(ROTATE_X_DEGREES_STATE);
    }


    protected Uri onTakePhotoActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Cursor myCursor = null;
            Date dateOfPicture;
            try {
                // Create a Cursor to obtain the file Path for the large image
                String[] largeFileProjection = {MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.ORIENTATION,
                        MediaStore.Images.ImageColumns.DATE_TAKEN};
                String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
                myCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        largeFileProjection, null, null, largeFileSort);
                if (myCursor != null) {
                    myCursor.moveToFirst();
                }
                // This will actually give you the file path location of the image.
                assert myCursor != null;
                String largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                Uri tempCameraPicUri = Uri.fromFile(new File(largeImagePath));
                if (tempCameraPicUri != null) {
                    dateOfPicture = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
                    if (dateOfPicture.after(dateCameraIntentStarted)) {
                        cameraPicUri = tempCameraPicUri;
                        rotateXDegrees = myCursor.getInt(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
                    }
                }
            } catch (Exception e) {
//              Log.w("TAG", "Exception - optaining the picture's uri failed: " + e.toString());
            } finally {
                if (myCursor != null) {
                    myCursor.close();
                }
            }

            if (cameraPicUri == null) {
                try {
                    cameraPicUri = intent.getData();
                } catch (Exception e){
                    showWarningDialog(getString(R.string.toast_error_could_not_take_photo));
                }
            }

            return cameraPicUri;

        } else if (resultCode == Activity.RESULT_CANCELED) {
            onCanceled();
        } else {
            onCanceled();
        }
        return null;
    }

    private void showWarningDialog(String message) {
        DefinedDialog.showErrorDialog(this, message, new DefinedDialog.DialogButtonListener() {
            @Override
            public void onClickButton(View v, boolean isLongClick) {

            }
        });
    }

    private void onCanceled()
    {
        logMessage("Camera Intent was canceled");
    }

    private void logMessage(String exceptionMessage)
    {
        Timber.d(getClass().getName(), exceptionMessage);
    }
}
