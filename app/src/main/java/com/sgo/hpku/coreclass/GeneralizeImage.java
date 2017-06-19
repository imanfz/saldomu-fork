package com.sgo.hpku.coreclass;/*
  Created by Administrator on 3/18/2015.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneralizeImage {

    private Context mContext;
    private String mFilePath;
    private Uri mUri;
    private Bitmap mBitmap;

    public GeneralizeImage(Context _context, String _file_path){
        this.mFilePath = _file_path;
        this.mContext = _context;
    }

    public GeneralizeImage(Context _context, Bitmap _file, Uri _uri){
        if (_file == null)
            return;
        this.mBitmap = _file;
        this.mUri = _uri;
        this.mContext = _context;
    }

    public File Convert(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imageHeight;
        int imageWidth;
        Bitmap finaleBitmap;
        if(mFilePath == null || mFilePath.isEmpty()){
            finaleBitmap = this.mBitmap;
            imageHeight = finaleBitmap.getHeight();
            imageWidth = finaleBitmap.getWidth();
        }
        else {
            BitmapFactory.decodeFile(mFilePath, options);
            imageHeight = options.outHeight;
            imageWidth = options.outWidth;
            finaleBitmap = BitmapFactory.decodeFile(mFilePath);
        }

        if(imageHeight > 3800 || imageWidth > 3800  ){
            imageHeight = imageHeight/2;
            imageWidth  = imageWidth/2;
            finaleBitmap = Bitmap.createScaledBitmap(finaleBitmap, imageWidth, imageHeight, false);
        }

        Bitmap newBitmap = setOrientationBitmap(finaleBitmap);

        String destFolder = mContext.getCacheDir().getAbsolutePath();
        String mFileName = "temp.jpeg";
        File mfile = new File(destFolder, mFileName);

        FileOutputStream out;
        try {
            out = new FileOutputStream(mfile);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return mfile;
    }

    public static File ConvertCapturedImage(Context mContext, Uri mUri, int rotateXDegree){
        int imageHeight = 0;
        int imageWidth = 0;
        Bitmap finaleBitmap;

        finaleBitmap = readBitmap(mContext,mUri);
        if (finaleBitmap != null) {
            imageHeight = finaleBitmap.getHeight();
            imageWidth = finaleBitmap.getWidth();
        }

        if(imageHeight > 3000 ){
            imageHeight = imageHeight/4;
            imageWidth  = imageWidth/4;
        }
        else {
            imageHeight = imageHeight/2;
            imageWidth  = imageWidth/2;
        }

        Bitmap oldBitmap = Bitmap.createScaledBitmap(finaleBitmap, imageWidth, imageHeight, true);

        Matrix matrix = new Matrix();
        matrix.postRotate(rotateXDegree);
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(),
                matrix, false);

        String destFolder = mContext.getCacheDir().getAbsolutePath();
        String mFileName = "temp.jpeg";
        File mfile = new File(destFolder, mFileName);

        FileOutputStream out;
        try {
            out = new FileOutputStream(mfile);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.gc();

        return mfile;
    }

    private static Bitmap readBitmap(Context context, Uri selectedImage) {
        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inScaled = false;
//      options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(selectedImage, "r");
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            try {
                bm = BitmapFactory.decodeFileDescriptor(
                        fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                return null;
            }
        }
        return bm;
    }

    private Bitmap setOrientationBitmap(Bitmap bm) {

        ExifInterface exif = null;
        try {
            if(mFilePath == null || mFilePath.isEmpty() )
                exif = new ExifInterface(this.mUri.getPath());
            else
                exif = new ExifInterface(mFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        assert exif != null;
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);

        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
}
