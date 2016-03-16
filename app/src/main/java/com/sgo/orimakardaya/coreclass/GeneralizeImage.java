package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 3/18/2015.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneralizeImage {

    Context mContext;
    String mFilePath;

    public GeneralizeImage(Context _context, String _file_path){
        this.mFilePath = _file_path;
        this.mContext = _context;
    }

    public File Convert(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mFilePath, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        Bitmap oldBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mFilePath), imageWidth/2, imageHeight/2, true);
//        Bitmap oldBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mFilePath),100, 100, true);
        Bitmap newBitmap = setOrientationBitmap(oldBitmap);

        String destFolder = mContext.getCacheDir().getAbsolutePath();
        String mFileName = "temp.jpeg";
        File mfile = new File(destFolder, mFileName);

        FileOutputStream out;
        try {
            out = new FileOutputStream(mfile);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return mfile;
    }

    private Bitmap setOrientationBitmap(Bitmap bm) {

        ExifInterface exif = null;
        try {
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
