package com.sgo.saldomu.activities;


import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.ScanQRUtils;
import com.sgo.saldomu.models.QrModel;

import java.io.IOException;
import java.io.InputStream;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import timber.log.Timber;

public class ScanQRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private ImageButton galleryImageButton;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    public static final String Name = "nameKey";
    private String bundleType, bundleSource, scanResult;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    public static final String QRVALUE = "qrvalue";
    QrModel parcelQRData;
    private static final int SELECT_IMAGE = 111;


    private String qrType, benef, benefName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscan_activity);
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        mScannerView = findViewById(R.id.scanner_view);
        galleryImageButton = findViewById(R.id.gallery_image_button);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null) {
            assert b != null;
            bundleType = b.getString("bundleType");
            bundleSource = b.getString("bundleSource");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (getFromPref(this, ALLOW_KEY)) {

                showSettingsAlert();

            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        }

        galleryImageButton.setOnClickListener(view -> {
            Intent intent2 = new Intent();
            intent2.setType("image/*");
            intent2.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent2, "Select Picture"), SELECT_IMAGE);
        });

    }

    public static void saveToPreferences(Context context, String key,
                                         Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences
                (CAMERA_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(ScanQRActivity context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences
                (CAMERA_PREF, Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }


    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(ScanQRActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);


                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    //finish();
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                (dialog, which) -> {
                    dialog.dismiss();
                    startInstalledAppDetailsActivity(ScanQRActivity.this);

                });
        alertDialog.show();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            super.onBackPressed(); //replaced
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                if (parentIntent == null) {
                    finish();
                    return true;
                } else {
                    parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(parentIntent);
                    finish();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString());
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Scan Result");
//        builder.setMessage(rawResult.getText());
//        AlertDialog alert1 = builder.create();
//        alert1.show();`
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//        editor.putString(Name, rawResult.getText());
//        editor.apply();
//        submitQR(rawResult.getText());


        divideResult(rawResult.getText());
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//        editor.putString(QRVALUE, rawResult.getText());
//        editor.apply();


        if (qrType == null) {
            Toast.makeText(ScanQRActivity.this, "QrCode tidak sesuai!", Toast.LENGTH_SHORT).show();
        } else if (!qrType.isEmpty()) {
            QrModel qrModel = new QrModel(benef, benefName, qrType);
            Timber.d("Isi qrOBJ name:" + qrModel.getSourceName() + " id:" + qrModel.getSourceAcct() + " type:" + qrModel.getQrType());
            Intent intent = new Intent();
            intent.putExtra(DefineValue.QR_OBJ, qrModel);
            setResult(RESULT_OK, intent);
        } else {
            Toast.makeText(ScanQRActivity.this, "Result QR:" + rawResult.getText(), Toast.LENGTH_SHORT).show();
        }
        mScannerView.resumeCameraPreview(this);
        this.finish();


//        if(qrType != null ){
//            parcelQRData = new QrModel(benef,benefName,qrType);
//
//            int type = 0;
//
//            // pindah intent
//            Intent intent = null;
//            intent = new Intent(this,ActivityTransfer.class);
//            intent.putExtra(DefineValue.TYPE,DefineValue.MONEY_TRANSFER);
//            intent.putExtra(DefineValue.IS_SCAN_QR,true);
//            intent.putExtra(DefineValue.TRANSFER_TYPE,type);
//            intent.putExtra(DefineValue.CODE_QR, qrType);
//            intent.putExtra(DefineValue.SOURCE_ACCT,benef);
//            intent.putExtra(DefineValue.SOURCE_NAME,benefName);
//            intent.putExtra(DefineValue.AMOUNT,amount);
//
//
//
//            startActivity(intent);
//        }
//        else {
//            Intent intent = new Intent(ScanQRActivity.this, ShortcutBarcodeTransaction.class);
//            intent.putExtra("DATA_SCAN", rawResult.getText());
//            startActivity(intent);
//        }

//        mScannerView.resumeCameraPreview(this);
//        this.finish();

    }

    public void divideResult(String rawResult) {


        String result = rawResult;
        String[] array = new String[10];

        int i = 0;
        for (String value : result.split(ScanQRUtils.SCAN_QR_SEPARATOR)) {
            Timber.d("splitt string:" + value);
            array[i] = value;

            if (array[i].contains(DefineValue.QR_TYPE)) {
                qrType = array[i].substring(array[i].indexOf(ScanQRUtils.EQUALS_SEPARATOR) + 1);
            } else if (array[i].contains(DefineValue.NO_HP_BENEF)) {
                benef = array[i].substring(array[i].indexOf(ScanQRUtils.EQUALS_SEPARATOR) + 1);
                Timber.d("TEST benef:" + benef);
            } else if (array[i].contains(DefineValue.SOURCE_ACCT_NAME)) {
                benefName = array[i].substring(array[i].indexOf(ScanQRUtils.EQUALS_SEPARATOR) + 1);
                Timber.d("TEST benefName:" + benefName);
            }
            i++;
        }
//        Timber.d("isi qrType:"+ qrType +" benef:"+benef+" amount:"+amount+" messages:"+messages);
    }

    private void PassResult() {
        Intent intent = new Intent(this, ScanQRActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("bundleType", bundleType);
        bundle.putString("bundleType", bundleType);
        intent.putExtras(bundle);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Bitmap bitmap1 = null;
                    try {
//                        bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                        InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                        bitmap1 = BitmapFactory.decodeStream(inputStream);
                    } catch (IOException e) {
                        Log.d("bitmap1 QR", e.getLocalizedMessage());
                        e.printStackTrace();
                    }
//                    bitmap1 = getBitmap(data.getData());
                    WallpaperManager image = null;


                    Bitmap bMap = bitmap1;
                    String contents = null;

                    int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];

                    bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

                    LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    Reader reader = new MultiFormatReader();
                    Result result = null;
                    try {
                        result = reader.decode(bitmap);
                        contents = result.getText();
                        Log.d("Result QR", "Ini Isinya :" + contents);

                        divideResult(result.getText());

                        if (qrType == null) {
                            Toast.makeText(ScanQRActivity.this, "QrCode tidak sesuai!", Toast.LENGTH_SHORT).show();
                        } else if (!qrType.isEmpty()) {
                            QrModel qrModel = new QrModel(benef, benefName, qrType);
                            Timber.d("Isi qrOBJ name:" + qrModel.getSourceName() + " id:" + qrModel.getSourceAcct() + " type:" + qrModel.getQrType());
                            Intent intent = new Intent();
                            intent.putExtra(DefineValue.QR_OBJ, qrModel);
                            setResult(RESULT_OK, intent);
                        } else {
                            Toast.makeText(ScanQRActivity.this, "Result QR:" + result.getText(), Toast.LENGTH_SHORT).show();
                        }
                        mScannerView.resumeCameraPreview(this);
                        this.finish();

                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (ChecksumException e) {
                        e.printStackTrace();
                    } catch (com.google.zxing.FormatException e) {
                        e.printStackTrace();
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
        }
    }
}
