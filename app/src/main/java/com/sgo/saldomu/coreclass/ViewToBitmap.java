package com.sgo.saldomu.coreclass;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.sgo.saldomu.BluetoothPrinter.zj.BluetoothService;
import com.sgo.saldomu.BluetoothPrinter.zj.Command;
import com.sgo.saldomu.BluetoothPrinter.zj.PrintPicture;
import com.sgo.saldomu.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 6/28/16.
 */
public class ViewToBitmap {

    private Context context;
    private BluetoothService mService;

    public ViewToBitmap(Context _context){
        this.context = _context;
    }

    public Boolean Convert(View view, String _filename) {
        return Convert(view, _filename, false) != null;
    }

    public Boolean ConvertToPrint(View view, String _filename, BluetoothService mService) {
        return ConvertToPrint(view, _filename, false, mService) != null;
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

    private String ConvertToPrint(View view, String _filename, Boolean isCache, BluetoothService mService) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        String path = "";
        this.mService = mService;

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
            Timber.w("Path Img: %s", path.toString());
            context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bm1 = BitmapFactory.decodeFile(path, options);

                int nMode = 0;

                int nPaperWidth = 384;
                if(bm1 != null)
                {
                    byte[] data = PrintPicture.POS_PrintBMP(bm1, nPaperWidth, nMode);
                    SendDataByte(Command.ESC_Init);
                    SendDataByte(Command.LF);
                    SendDataByte(data);
//                    SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(20));
//                    SendDataByte(PrinterCommand.POS_Set_Cut(1));

                }

            f.delete();

            if(isCache)
                Timber.d(context.getString(R.string.success_saved_gallery));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public boolean shareIntentApp(Context context, View view,String _filename){
        String path = Convert(view,_filename,true);

        if(path == null)
            return false;
        try
        {
            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("image/jpeg");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT,context.getString(R.string.appname));

            File bitmapFile = new File(path);
            Uri myUri;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                myUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", bitmapFile);
            }else myUri = Uri.fromFile(bitmapFile);

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

    private void SendDataByte(byte[] data) {

        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        mService.write(data);
    }

    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }
}
