package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 3/18/2015.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class GeneralizeImage {

    private Context mContext;
    private String mFilePath;
    private Uri mUri;
    private Bitmap mBitmap;

//    public GeneralizeImage(Context _context, String _file_path){
//        this.mFilePath = _file_path;
//        this.mContext = _context;
//    }

    public GeneralizeImage(Context _context){
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
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
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

        float imgRatio = imageWidth / imageHeight;
        float maxRatio = maxWidth / maxHeight;


        if (imageHeight > maxHeight || imageWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / imageHeight;
                imageWidth = (int) (imgRatio * imageWidth);
                imageHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / imageWidth;
                imageHeight = (int) (imgRatio * imageHeight);
                imageWidth = (int) maxWidth;
            } else {
                imageHeight = (int) maxHeight;
                imageWidth = (int) maxWidth;
            }
        }


//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);
//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];



        Bitmap scaledBitmap = finaleBitmap;
        try {
            scaledBitmap = Bitmap.createBitmap(imageWidth,imageHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = imageWidth / (float) options.outWidth;
        float ratioY = imageHeight / (float) options.outHeight;
        float middleX = imageWidth / 2.0f;
        float middleY = imageHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(finaleBitmap, middleX - imageWidth / 2, middleY - imageHeight / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//        if(imageHeight > 3800 || imageWidth > 3800  ){
//            imageHeight = imageHeight/2;
//            imageWidth  = imageWidth/2;
//            finaleBitmap = Bitmap.createScaledBitmap(finaleBitmap, imageWidth, imageHeight, false);
//        }

        Bitmap newBitmap = setOrientationBitmap(scaledBitmap);

        String destFolder = mContext.getCacheDir().getAbsolutePath();
        String mFileName = "temp"+ DateTimeFormat.getCurrentDateTime()+".jpeg";
        File mfile = new File(destFolder, mFileName);

        FileOutputStream out;
        try {
            out = new FileOutputStream(mfile);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return mfile;
    }

    public File compressImage(String stringUri) {

//        String filePath = getRealPathFromURI(stringUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(stringUri, options);
//        Bitmap bmp = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/SquareCamera/IMG_20190628_094028", options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(stringUri, options);
//            bmp = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/SquareCamera/IMG_20190628_094028", options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(stringUri);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Timber.d("Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Timber.d("Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Timber.d("Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Timber.d("Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out;
        String filename = getFilename();

        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new File(filename);

    }

    private String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), mContext.getString(R.string.appname)+"Image");
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/"+ System.currentTimeMillis() + ".jpg");

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
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
