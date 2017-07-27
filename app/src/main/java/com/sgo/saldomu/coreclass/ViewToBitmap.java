package com.sgo.saldomu.coreclass;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.sgo.saldomu.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 6/28/16.
 */
public class ViewToBitmap {

    private Context context;

    public ViewToBitmap(Context _context){
        this.context = _context;
    }

    public Boolean Convert(View view, String _filename) {
        return Convert(view, _filename, false) != null;
    }

    private String Convert(View view, String _filename, Boolean isCache) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        String path = "";

        try {
            String direktori_path = File.separator + "DCIM"+ File.separator + context.getString(R.string.foldername_struk);
            File f;
            if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory()+direktori_path) ;
                if (!file.exists()) {
                    if(!file.mkdirs())
                        return null;
                }
                f = new File(Environment.getExternalStorageDirectory() + direktori_path + File.separator + _filename + ".png");
                path = f.getAbsolutePath();
            }
            else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + direktori_path);
                if (!file.exists()) {
                    if(!file.mkdirs())
                        return null;

                }
                f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + direktori_path + File.separator + _filename + ".png");
                path = f.getAbsolutePath();
            }


            FileOutputStream output = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,path);

            context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if(isCache)
                Timber.d(context.getString(R.string.success_saved_gallery));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public boolean shareIntentApp(View view,String _filename){
        String path = Convert(view,_filename,true);

        if(path == null)
            return false;
        try
        {
            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("image/jpeg");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT,context.getString(R.string.appname));

            File bitmapFile = new File(path);
            Uri myUri = Uri.fromFile(bitmapFile);
            i.putExtra(Intent.EXTRA_STREAM, myUri);
            context.startActivity(Intent.createChooser(i, context.getString(R.string.share_title)));
        }
        catch(Exception e)
        {
            Timber.d(e.toString());
            return false;
        }
        return true;
    }
}
