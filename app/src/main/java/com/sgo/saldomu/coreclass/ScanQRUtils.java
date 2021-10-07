package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import timber.log.Timber;

public class ScanQRUtils  {

    private static ScanQRUtils instance = null;

    private ScanQRUtils(Context mContext){

    }

    public static ScanQRUtils getInstance(Context mContext){

        if(instance == null){
            instance = new ScanQRUtils(mContext);
        }
        return instance;
    }

    public static final String SCAN_QR_SEPARATOR = "%";
    public static final String EQUALS_SEPARATOR = "=";


    public Bitmap generateQRCode(String codeQr, String sourceAcct, String sourceName){


        Bitmap bitmap;
        String value = encryptedValue(codeQr,sourceAcct,sourceName);
        Timber.d("isi qr value:%s", value);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(value, BarcodeFormat.QR_CODE,600,600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            bitmap  = barcodeEncoder.createBitmap(bitMatrix);
//            Bitmap logo = BitmapFactory.decodeResource(mContext.getResources(),
//                    R.mipmap.ic_launcher_pin_only);
//
//            Bitmap bitmapCircleLogo = Bitmap.createBitmap(logo.getWidth(), logo.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmapCircleLogo);
//            Paint paint = new Paint();
//            paint.setColor(Color.WHITE);
//            canvas.drawCircle(logo.getWidth()/ 2.0f + 0.7f,logo.getHeight()/ 2.0f + 0.7f,logo.getWidth()/ 2.0f + 0.7f,paint);
//            canvas.drawBitmap(logo,(bitmapCircleLogo.getWidth()-logo.getWidth())/2,
//                    (bitmapCircleLogo.getHeight()-logo.getHeight())/2,null);
//            canvas.save();
//            canvas.restore();
//            return addLogoToQRCode(bitmap,bitmapCircleLogo);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap addLogoToQRCode(Bitmap src, Bitmap logo) {
        if (src == null || logo == null) {
            return src;
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }
    
    private String encryptedValue(String qrTypeValue, String sourceAcctValue, String sourceNameValue){

        String value = "";
        if(qrTypeValue.equalsIgnoreCase(DefineValue.QR_TYPE_FROM_DEFAULT_ACCOUNT)){
            value =
                    DefineValue.QR_TYPE + EQUALS_SEPARATOR + qrTypeValue + SCAN_QR_SEPARATOR +
                    DefineValue.NO_HP_BENEF + EQUALS_SEPARATOR + sourceAcctValue + SCAN_QR_SEPARATOR +
                    DefineValue.SOURCE_ACCT_NAME + EQUALS_SEPARATOR + sourceNameValue  + SCAN_QR_SEPARATOR;
        }
        return value;
    }

}
