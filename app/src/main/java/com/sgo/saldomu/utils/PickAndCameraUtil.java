package com.sgo.saldomu.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.desmond.squarecamera.CameraActivity;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.CameraViewActivity;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.GeneralizeImage;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 11/27/17.
 */

public class PickAndCameraUtil {
    private Activity mActivity;
    private Fragment mFragment;
    private Uri mCapturedImageURI;
    private String mCurrentPhotoPath;
    private GeneralizeImage generalizeImage;

    public PickAndCameraUtil(Activity activity){
        this.mActivity = activity;
    }

    public PickAndCameraUtil(Activity activity,Fragment fragment){
        this.mActivity = activity;
        this.mFragment = fragment;
    }

    public Uri getCaptureImageUri(){
        return mCapturedImageURI;
    }

    public String getCurrentPhotoPath(){
        return mCurrentPhotoPath;
    }

    public String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = mActivity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String stringIdx = cursor.getString(index);
            cursor.close();
            return stringIdx;
        }
    }

    public String getRealPathFromURI(String contentURI) {
        return getRealPathFromURI(Uri.parse(contentURI));

    }

    public File compressImage(String stringUri){
        if(generalizeImage == null)
            generalizeImage = new GeneralizeImage(mActivity);
        return generalizeImage.compressImage(stringUri);
    }

    private void startActivityForResult(Intent i, int type ){
        if(mFragment != null){
            mFragment.startActivityForResult(i,type);
        }
        else {
            if(mActivity != null)
                mActivity.startActivityForResult(i,type);
        }
    }

    private PackageManager getPackageManager(){
        return mActivity.getPackageManager();
    }


    public void chooseGallery(int reqCode) {
        Timber.wtf("masuk gallery");
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, reqCode);
    }


    public void runCamera(int reqCode){
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        Intent takePictureIntent = new Intent(mActivity,CameraActivity.class);
//        Intent takePictureIntent = new Intent(mActivity, CameraViewActivity.class);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                String timeStamp = DateTimeFormat.getCurrentDateTime();
                String imageFileName = "JPEG_" + timeStamp + "_" + BuildConfig.APP_ID;

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, imageFileName);

                mCapturedImageURI = mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
            else {

                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Timber.e(ex.getMessage());
                }

                if (photoFile != null) {
                    mCapturedImageURI = FileProvider.getUriForFile(mActivity,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                }
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(takePictureIntent, reqCode);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
//        String timeStamp = DateTimeFormat.getCurrentDateTime();
        DateFormat df = new SimpleDateFormat("yyyMMdd_HHmmss", new Locale("ID","INDONESIA"));
        String timeStamp = df.format(Calendar.getInstance().getTime());
        String imageFileName = "JPEG_" + timeStamp + "_" + BuildConfig.APP_ID;
//        String imageFileName = "IMG_" + timeStamp;
        File storageDir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
//        mCurrentPhotoPath =image.toString();
        return image;
    }



}
